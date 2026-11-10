# Reviewer Report — T2.2 Part 2 PATCH DTO Quick Architectural Review

## 1. Task ID

**VERIFIED:** T2.2 — Part 2 (Jackson tri-state PATCH binding, PATCH DTOs, PATCH validation, focused tests). Review scope: PATCH DTO implementation and supporting infrastructure only.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.2-EXEC-012-REV-02` |
| Date | 2026-11-10 |
| Prior Reviewer primary archived | `agent-reports/T2.2/history/reviewer-003.md` (unchanged copy of `T2.2-EXEC-008-REV-01`) |

## 3. Agent role and model

- **VERIFIED:** Role — Reviewer, senior Java and Spring Boot code reviewer.
- Model — `opencode-go/beam-free`.

## 4. Authorized scope

**VERIFIED (tasking):** Quick architectural review of the PATCH DTO implementation and supporting infrastructure only. Checks: (1) `PatchValue` tri-state; (2) Jackson deserialization for normal JSON; (3) Jakarta Validation for required and nested fields; (4) nested document ID/version rules; (5) no persistence/ownership logic in DTOs; (6) simplicity/maintainability. No code or test changes. No Maven rerun unless a concrete defect requires verification. ValueExtractor SPI registration approved by Tech Lead.

## 5. Files inspected

Independent inspection of implementation completed **before** reading the Architect, Developer and Tester reports.

| Artifact | Evidence |
|---|---|
| `src/main/java/com/humanit/challenge/dto/patch/PatchValue.java` | **VERIFIED:** tri-state holder (ABSENT/PRESENT_NULL/PRESENT_VALUE), factories, equality, `get()` guard, state-only `toString()`. |
| `src/main/java/com/humanit/challenge/dto/patch/PatchValueDeserializer.java` | **VERIFIED:** contextual deserializer, `getNullValue()` → `presentNull()`, missing property keeps `absent()` default. |
| `src/main/java/com/humanit/challenge/dto/patch/PatchValueExtractor.java` | **VERIFIED:** exposes only PRESENT_VALUE to validator. |
| `src/main/java/com/humanit/challenge/dto/patch/PatchRequired.java`, `PatchRequiredValidator.java` | **VERIFIED:** rejects PRESENT_NULL (and null holder), accepts ABSENT/PRESENT_VALUE. |
| `src/main/java/com/humanit/challenge/dto/client/ClientPatchRequest.java` | **VERIFIED.** |
| `src/main/java/com/humanit/challenge/dto/client/ClientDocumentPatchRequest.java` | **VERIFIED:** identity pair rule + new-document completeness rule. |
| `src/main/java/com/humanit/challenge/dto/document/DocumentPatchRequest.java` | **VERIFIED.** |
| `src/main/resources/META-INF/services/jakarta.validation.valueextraction.ValueExtractor` | **VERIFIED:** SPI registration of `PatchValueExtractor`. |
| `src/test/java/com/humanit/challenge/dto/patch/*`, `*PatchRequestValidationTest.java` | **VERIFIED (read after inspection):** test evidence. |
| `agent-reports/T2.2/{architect-proposal,developer-report,tester-report}.md` | **VERIFIED (read after independent inspection):** cross-checked. |

## 6. Files changed

None (application code/tests untouched). Only this report and its archive:
- `agent-reports/T2.2/history/reviewer-003.md` (archive of prior primary, unchanged).
- `agent-reports/T2.2/reviewer-report.md` (this report).

## 7. Commands executed

No Maven/build commands. Read-only inspection only. No Git mutation.

## 8. Evidence and observed results

- **CHECK 1 (tri-state):** `PatchValue` defines exactly three states; `absent()`, `presentNull()`, `of(T)` (`of(null)` → `PRESENT_NULL`); `get()` throws otherwise; `toString()` reports state only. **VERIFIED** (source + `PatchValueTest` 8/8).
- **CHECK 2 (Jackson):** `PatchValueDeserializer` contextualizes value type from declaring property/root type; explicit JSON `null` → `getNullValue()` → `PRESENT_NULL`; missing property keeps `PatchValue.absent()` default. **VERIFIED** (`PatchValueJacksonBindingTest` 14/14, incl. Spring `ObjectMapper`).
- **CHECK 3 (Validation):** `PatchRequiredValidator` rejects `PRESENT_NULL`; `PatchValueExtractor` exposes only `PRESENT_VALUE`, so type-argument constraints (`@NotBlank`, `@Size`, `@Email`, `@Valid`, `@NotNull`) apply to supplied values only; SPI registration active in Spring `LocalValidatorFactoryBean`. **VERIFIED** (`PatchValueValidationWiringTest` 2/2; validation suites pass).
- **CHECK 4 (nested ID/version):** `isIdentityPairConsistent()` requires both or neither of `id`/`version`; `isNewDocumentFieldsComplete()` requires `number` + `expirationDate` as `PRESENT_VALUE` for new entries. No ownership/version-comparison/merge logic in DTOs. **VERIFIED** (source + validation tests).
- **CHECK 5 (no leaks):** DTOs contain only request contracts, Bean Validation and Jackson binding. No repositories, services, transactions or ownership resolution. **VERIFIED**.
- **CHECK 6 (simplicity):** Small, focused classes; constraints on type arguments reuse standard validators; no custom text/email constraints. **VERIFIED**.
- **Suite:** Tester reported 231 tests, 0 failures, 0 errors (independent run, BUILD SUCCESS). Developer reported 233; discrepancy is a test-count wording issue only (Minor, non-blocking).

## 9. Findings and severity

### Critical
None.

### Major
None.

### Minor
- **T2.2-P2-T-01 (carried, documentation):** Developer report states 233 tests; independent run measured 231 (`DocumentRequestValidationTest`: reported 20, measured 18). No code/test defect; wording/counting discrepancy only. Non-blocking.
- **F-P2-01 (scope confirmation):** ValueExtractor SPI registration (`META-INF/services/...`) is the only new resource file. Per tasking, approved by Tech Lead. No further action.

No other findings. F-P2-02..F-P2-08 are Info-level design notes, not defects.

## 10. Decisions and reasoning

1. `PatchValue` correctly distinguishes the three PATCH states; `of(null)` is `PRESENT_NULL`, never `ABSENT`. **VERIFIED.**
2. Jackson binding is correct for normal JSON input, including generic/nested types and explicit null. **VERIFIED.**
3. Jakarta Validation works for required (`@PatchRequired`) and nested (`@Valid` on list elements) fields, including Spring's validator via SPI. **VERIFIED.**
4. Nested document ID/version rules are consistent and contain no service/ownership logic. **VERIFIED.**
5. No persistence or ownership logic leaked into DTOs. **VERIFIED.**
6. Implementation is reasonably simple and maintainable. **VERIFIED.**
7. HTTP-level `@RequestBody` binding remains PENDING VERIFICATION (no controllers in T2.2); deferred to T2.3/T2.4. Not a blocker for Part 3.

## 11. Final status

**PASS WITH FINDINGS — 0 Critical, 0 Major, 2 Minor (non-blocking: T2.2-P2-T-01 documentation count; F-P2-01 scope confirmation resolved/approved).**

The PATCH DTO implementation and supporting infrastructure satisfy the scoped checks. No application code or tests were modified by this review.

## 12. Outstanding issues

1. T2.2-P2-T-01: correct test total in Developer report (231, not 233) — documentation only.
2. HTTP-level PATCH binding (MockMvc) — deferred to T2.3/T2.4 (PENDING VERIFICATION).
3. RF2-1 carried (Minor, project tracking) — unchanged, out of scope.

## 13. Handoff to next agent

- **Tech Lead:** Part 2 PATCH DTO implementation is sound and tests pass; may authorize Part 3 (dedicated mappers, mapper tests, regression, Git commit) when ready. This review does not itself approve T2.2 or authorize Part 3.
- **Part 3 readiness:** YES (pending explicit Tech Lead authorization). Part 3 must not change `PatchValue` semantics; must map `PRESENT_NULL` (description) to a clear and must not map `ABSENT`.

---

**Report status:** Written for `T2.2-EXEC-012-REV-02`. Prior Reviewer primary archived unchanged at `agent-reports/T2.2/history/reviewer-003.md`. Current primary: `agent-reports/T2.2/reviewer-report.md`.
