# Developer Report — T2.2 Implementation Part 2/3: Jackson Tri-State PATCH Binding, PATCH DTOs, Validation and Focused Tests

## 1. Task ID

**VERIFIED:** T2.2 — Implementation Part 2/3 (Jackson tri-state PATCH binding, PATCH DTOs, PATCH validation and focused tests), authorized by the Human Tech Lead in the tasking for this execution. Decisions D-10, D-11, D-12 and D-13 remain authoritative. Part 3 (dedicated mappers, mapper tests, final regression and any Git mutation) is **not** implemented here.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.2-EXEC-010-DEV-02` — **RECONSTRUCTED:** the tasking supplied no execution identifier. The label is the next sequence number after the highest identifier found in-project (`T2.2-EXEC-009-DEV-01`, the Part 1 Developer report recorded in `agent-reports/T2.2/history/developer-003.md`). Search for identifiers: `Select-String` over `agent-reports/T2.2/**` and `agent-reports/decisions.md`. |
| Date | 2026-11-10 (Europe/Lisbon). **VERIFIED:** Maven log timestamps of the runs in this execution (e.g. `2026-11-10 16:23:30`). |
| Branch | **VERIFIED:** `git branch --show-current` = `feature/T2.2-dtos-mappers` (confirmed, not switched). |
| Base commit | **VERIFIED:** `git log --oneline -5` head = `8810cd2 docs(architecture): formalize D-11 layer separation and approval`. |
| Previous primary archived | **VERIFIED:** the Part 1 Developer report (`T2.2-EXEC-009-DEV-01`) was copied byte-for-byte to `agent-reports/T2.2/history/developer-003.md`. SHA-256 of primary and archive: `E781DB177832705854BFF55FC7C41C7E77B93EE8E176F770EC43B993C17C4B12` (identical, `Get-FileHash`). Earlier archives `developer-001.md` and `developer-002.md` were not touched. |

## 3. Agent role and model

- **VERIFIED:** Role — Developer, senior Java and Spring Boot engineer.
- **VERIFIED:** Model — `claude-haiku-5-5` (exact model ID `opencode-go/claude-haiku-5-5`), as stated in the execution environment.

## 4. Authorized scope

**VERIFIED (Human Tech Lead tasking):**

- Phase 2: project-owned generic tri-state `PatchValue<T>` (ABSENT, PRESENT_NULL, PRESENT_VALUE) with Jackson binding; no external JSON-nullable dependency.
- Phase 3: `ClientPatchRequest`, `ClientDocumentPatchRequest`, `DocumentPatchRequest`.
- Phase 4: Jakarta Bean Validation for PATCH, including nested entries and generic `PatchValue` values.
- Phase 5: Part 1 regression preservation.
- Phase 6: focused tests for the 15 scenarios listed in the tasking.
- Packages confirmed by the Tech Lead: `com.humanit.challenge.dto.client`, `com.humanit.challenge.dto.document`, and `com.humanit.challenge.dto.patch` for the tri-state infrastructure.

**VERIFIED (explicit prohibitions honoured):** No Git staging, commit, push or merge; no change to T2.1 entities, repositories, Flyway migrations or schema; no services, REST controllers or dedicated mappers; no change to `pom.xml`, Maven Wrapper or configuration; no change to requirements, implementation plan, decisions log, other agents' reports or agent definitions; no change to RF2-1 tracking documents.

## 5. Files inspected

| Artifact | Evidence / relevance |
|---|---|
| `agent-reports/README.md` | **VERIFIED:** 13-section format, evidence markers, history convention. |
| `agent-reports/decisions.md` | **VERIFIED:** D-10, D-11, D-12, D-13. The D-13 entry was read in full (the first read was truncated at 2000 characters; the full entry was read by a second read). |
| `requirements-decisions.md` | **VERIFIED:** Sections 2, 4, 6, 8, 9, 11, 19, 20 (PATCH semantics, nested Client PATCH, validation limits, concurrency contract). |
| `implementation-plan.md` | **VERIFIED:** Phase 2 boundaries and T2.2 acceptance criteria. |
| `agent-reports/T2.2/architect-proposal.md` | **VERIFIED:** D-13 approval record (five approved design decisions). |
| `agent-reports/T2.2/developer-report.md` | **VERIFIED:** Part 1 report (read; archived unchanged before replacement). Its conclusions were used only as a pointer to the work remaining; they were not treated as evidence. |
| `src/main/java/com/humanit/challenge/dto/client/*.java`, `dto/document/*.java` (Part 1) | **VERIFIED:** Existing `ClientCreateRequest`, `ClientPutRequest`, `ClientDocumentPutRequest`, `ClientResponse`, `DocumentCreateRequest`, `DocumentPutRequest`, `DocumentResponse`. Read; **not modified**. |
| `src/test/java/com/humanit/challenge/dto/DtoValidationSupport.java`, `dto/client/ClientRequestBindingTest.java`, `dto/client/ClientPutRequestValidationTest.java` (lines 1–80), other Part 1 tests (grep only) | **VERIFIED:** Conventions and regression baseline. Not modified. |
| `src/main/java/com/humanit/challenge/domain/Client.java`, `Document.java` | **VERIFIED:** Column lengths, lazy `documents`, `@Version`. Not modified. |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | **VERIFIED:** Constraints on `src/main/resources` (profiles, `spring.factories`) and on `src/main/java` (no `EnvironmentPostProcessor`, no `spring.factories` string). The new ValueExtractor service file was checked against it: it passes. |
| `pom.xml` | **VERIFIED:** Spring Boot 3.5.16, Java 21, `spring-boot-starter-validation` (Hibernate Validator), `spring-boot-starter-web` (Jackson, `Jackson2ObjectMapperBuilder`), `spring-boot-starter-test`. **No dependency change.** |
| Surefire reports under `target/surefire-reports/` | **VERIFIED:** Used to diagnose the first failing run (XML `failure` messages). |

## 6. Files changed

### 6.1 Created — application code

| Path | Purpose |
|---|---|
| `src/main/java/com/humanit/challenge/dto/patch/PatchValue.java` | Generic tri-state holder. States: `ABSENT`, `PRESENT_NULL`, `PRESENT_VALUE`. Factories `absent()`, `presentNull()`, `of(T)` (`of(null)` yields `PRESENT_NULL`). `get()` throws unless `PRESENT_VALUE`. `toString()` reports the state only, never the value. Annotated `@JsonDeserialize(using = PatchValueDeserializer.class)` at class level. |
| `src/main/java/com/humanit/challenge/dto/patch/PatchValueDeserializer.java` | Jackson deserializer. `ContextualDeserializer`: the value type is resolved from the declaring property (`property.getType().containedType(0)`) or the root type, so generic types such as `PatchValue<List<ClientDocumentPatchRequest>>` bind correctly. `getNullValue(...)` returns `PatchValue.presentNull()`, which is how an explicit JSON `null` becomes `PRESENT_NULL`. A missing property never reaches the deserializer and keeps the DTO field default `PatchValue.absent()`. |
| `src/main/java/com/humanit/challenge/dto/patch/PatchValueExtractor.java` | Bean Validation `ValueExtractor<PatchValue<@ExtractedValue ?>>`. Exposes only a `PRESENT_VALUE` to the validator, so type-argument constraints such as `PatchValue<@NotBlank @Size(max = 100) String>` apply to supplied values only. |
| `src/main/resources/META-INF/services/jakarta.validation.valueextraction.ValueExtractor` | ServiceLoader registration of `PatchValueExtractor` for the default Hibernate Validator factory (the one Spring uses). **New resource file; see finding F-P2-01.** |
| `src/main/java/com/humanit/challenge/dto/patch/PatchRequired.java`, `PatchRequiredValidator.java` | Custom constraint: rejects `PRESENT_NULL` (and a `null` holder). Accepts `ABSENT` and `PRESENT_VALUE`. Used for required PATCH properties. |
| `src/main/java/com/humanit/challenge/dto/client/ClientPatchRequest.java` | Client PATCH body. `version` required (`@NotNull @PositiveOrZero`). `firstName`, `lastName`, `taxIdentifier`, `email`, `phoneNumber`: `PatchValue` with `@PatchRequired` and value constraints (`@NotBlank`, `@Size`, and `@Email` for email). `documents`: `PatchValue<List<@NotNull @Valid ClientDocumentPatchRequest>>` with `@PatchRequired`. |
| `src/main/java/com/humanit/challenge/dto/client/ClientDocumentPatchRequest.java` | Nested Client PATCH Document entry. `id` and `version` are plain `Long` (identity is not tri-state). `version` `@PositiveOrZero`. `number`: `@PatchRequired`, `@NotBlank @Size(max = 100)`. `description`: `@Size(max = 1000)`, `PRESENT_NULL` allowed (clears). `expirationDate`: `@PatchRequired`. Class-level `@AssertTrue` rules: `isIdentityPairConsistent()` (both or neither of `id`/`version`) and `isNewDocumentFieldsComplete()` (a new entry must supply `number` and `expirationDate` as `PRESENT_VALUE`). Both are `@JsonIgnore`d. |
| `src/main/java/com/humanit/challenge/dto/document/DocumentPatchRequest.java` | Document PATCH body. `version` required (`@NotNull @PositiveOrZero`). `number`: `@PatchRequired`, `@NotBlank @Size(max = 100)`. `description`: `@Size(max = 1000)`, `PRESENT_NULL` allowed (clears). `expirationDate`: `@PatchRequired`, no expiry rule (expired Documents are valid). |

### 6.2 Created — tests

| Path | Purpose |
|---|---|
| `src/test/java/com/humanit/challenge/dto/patch/PatchValueTest.java` | Unit tests of the tri-state holder (8 tests). |
| `src/test/java/com/humanit/challenge/dto/patch/PatchValueJacksonBindingTest.java` | Jackson binding, including generic root types, nested PATCH DTOs, lists of nested PATCH DTOs, and the Spring-configured `ObjectMapper` (14 tests). |
| `src/test/java/com/humanit/challenge/dto/patch/PatchValueValidationWiringTest.java` | Proves the ValueExtractor is active in Spring's `LocalValidatorFactoryBean`, the validator the application uses (2 tests). |
| `src/test/java/com/humanit/challenge/dto/client/ClientPatchRequestValidationTest.java` | Client PATCH validation: scalars, lengths, email (including a parity check against the POST `@Email` rule), `documents` collection rules, and nested identity and version rules (49 tests). |
| `src/test/java/com/humanit/challenge/dto/document/DocumentPatchRequestValidationTest.java` | Document PATCH validation: version, required scalars, description clearing and length (16 tests). |

### 6.3 Archive and report

| Path | Change |
|---|---|
| `agent-reports/T2.2/history/developer-003.md` | **VERIFIED:** Created as an unchanged copy of the previous primary (Part 1 report). SHA-256 identical. |
| `agent-reports/T2.2/developer-report.md` | **VERIFIED:** Replaced with this report (after archival). |

### 6.4 Not changed (VERIFIED)

No Part 1 DTO file, no T2.1 domain/repository/migration file, no `pom.xml`, no configuration under `src/main/resources` other than the new ValueExtractor service file, no `reports/*.md`, no `requirements-decisions.md`, no `implementation-plan.md`, no `agent-reports/decisions.md`, no Architect or Reviewer report, no agent definition. The pre-existing uncommitted documentation changes and untracked history archives present at the start were not touched by this execution.

## 7. Commands executed

| # | Command (workdir `C:\Users\pedro\human-it-challenge`) | Purpose | Result |
|---|---|---|---|
| 1 | `git branch --show-current`; `git status --short`; `git log --oneline -5` | Phase 1 checks | **VERIFIED:** branch `feature/T2.2-dtos-mappers`; pre-existing doc modifications and untracked history archives; Part 1 `dto/` untracked. |
| 2 | `Get-ChildItem -Recurse -File agent-reports` and reads listed in §5 | Inspection | **VERIFIED.** |
| 3 | `.\mvnw.cmd -B test` (baseline, before Part 2 code) | Phase 1 baseline | **VERIFIED:** `Tests run: 144, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. |
| 4 | `.\mvnw.cmd -B -q compile` (after first PATCH draft) | Compile check | **VERIFIED:** compile OK. |
| 5 | `.\mvnw.cmd -B test` (first run with PATCH tests) | Verification | **VERIFIED — FAILED:** `Tests run: 230, Failures: 9`. All nine failures were nested Client PATCH cases (`ClientPatchRequestValidationTest$NestedDocumentIdentity` and `existingDocumentRequiredScalarExplicitNullIsRejected`, etc.). Surefire XML showed `Expecting value to be true but was false`. **Root cause (VERIFIED by diagnosis):** constraints inside a list held by `PatchValue` were never evaluated, because `PatchValue<T>` exposes the list only through an erased type variable and Hibernate Validator does not iterate it. This was a **design defect in my first draft**, not a test defect. |
| 6 | Design change: removed my custom `PatchText`, `PatchTextValidator`, `PatchNoNullElements`, `PatchNoNullElementsValidator`; removed the `@Valid getValue()` hook from `PatchValue`; added `PatchValueExtractor` and the `META-INF/services` registration; moved value constraints onto type arguments in the three PATCH DTOs. | Fix for command 5 | Applied. See §10, decision 1. |
| 7 | `.\mvnw.cmd -B test` | Verification | **VERIFIED — compile error:** wrong import (`jakarta.validation.valueextraction.ValueReceiver`; the type is nested as `ValueExtractor.ValueReceiver`). Fixed. |
| 8 | `.\mvnw.cmd -B test` | Verification | **VERIFIED — FAILED:** `Tests run: 230, Failures: 1`. Nested cases all passed. The one failure was my own assumed path for a null list element (`documents[0]`); the **observed** path is `documents[0].<list element>` with constraint `NotNull` (observed through a temporary probe assertion, `ClientPatchRequestValidationTest.nullEntryInsideDocumentsIsRejected`). The assertion was corrected to the observed path. |
| 9 | `.\mvnw.cmd -B test "-Dtest=ClientPatchRequestValidationTest*" -Dsurefire.failIfNoSpecifiedTests=false` | Targeted run | **VERIFIED — inconclusive:** produced no usable new report. The following full run (command 10) showed 6 errors `Unresolved compilation problems` from stale compiled test classes. These were **not** source defects. A clean build resolved them. Recorded for transparency. |
| 10 | `.\mvnw.cmd -B test` | Verification | **VERIFIED — errors caused by stale test-class output (command 9)**; not used as evidence. |
| 11 | `.\mvnw.cmd -B clean test` | Verification | **VERIFIED:** `Tests run: 230, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. |
| 12 | Added `PatchValueValidationWiringTest` (2 tests); `.\mvnw.cmd -B clean test` | Verification | **VERIFIED:** `Tests run: 232, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. |
| 13 | Line-ending normalization (CRLF to LF) of three test files written through PowerShell (no logic change) | Hygiene | **VERIFIED:** `crlf=0` after the change. |
| 14 | Added `springConfiguredObjectMapperPreservesTheThreeStates` (Spring `Jackson2ObjectMapperBuilder`); `.\mvnw.cmd -B clean test` | Final verification | **VERIFIED:** `Tests run: 233, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, exit code `0`. |
| 15 | `Get-ChildItem` + PowerShell scan of every new `dto/` and `META-INF` file: non-ASCII bytes, trailing whitespace, tabs, and a heuristic literal-secret pattern (`password|secret|token|apikey|api_key` followed by `=` or `:` and a quoted literal) | Phase 8 hygiene | **VERIFIED:** 0 non-ASCII, 0 trailing whitespace, 0 tabs, 0 literal-secret matches in every new file. (The secret scan is heuristic and only matches literal assignments; it is not a full secret scanner.) |
| 16 | `git status --short` | Phase 8 | **VERIFIED:** see §8.6. |
| 17 | `git diff --check` | Phase 8 | **VERIFIED:** exit code `0`. Output contains only CRLF-conversion warnings for the **pre-existing** modified tracked documents (`architect-proposal.md`, `developer-report.md`, `reviewer-report.md`, `decisions.md`, `implementation-plan.md`, `requirements-decisions.md`). No whitespace error was reported. `git diff --check` does not cover untracked files; the scan in command 15 covers them. |
| 18 | `git diff --stat` | Phase 8 | **VERIFIED:** only the six modified documents: `6 files changed, 441 insertions(+), 160 deletions(-)` measured before this report was written, and `6 files changed, 448 insertions(+), 160 deletions(-)` after it (the +7 lines are this report's own change to the tracked `developer-report.md`). The other five documents' changes pre-date this execution. **No tracked `src/` file is modified.** |
| 19 | `Get-FileHash` on primary and archive | Archive verification | **VERIFIED:** identical SHA-256. |

**VERIFIED:** No `git add`, `git commit`, `git push`, `git merge`, `git checkout`, `git reset`, `git restore` or `git stash` was run. No branch was switched.

## 8. Evidence and observed results

### 8.1 Build and test results

- **VERIFIED (command 14, final):** `.\mvnw.cmd -B clean test` — `Tests run: 233, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, exit code `0`. Log: `$env:TEMP\t22p2-final.log` (outside the repository).
- **VERIFIED (arithmetic):** 233 = 144 (Part 1 baseline, verified at command 3) + 89 new Part 2 tests.
- **VERIFIED (new Part 2 tests, by class from the final Surefire output):**
  - `PatchValueTest`: 8
  - `PatchValueJacksonBindingTest`: 14
  - `PatchValueValidationWiringTest`: 2
  - `ClientPatchRequestValidationTest`: 49 (`OmittedAndSuppliedScalars` 4, `RequiredScalars` 11, `LengthLimits` 5, `EmailFormat` 12, `DocumentsCollection` 4, `NestedDocumentIdentity` 13)
  - `DocumentPatchRequestValidationTest`: 16 (`VersionField` 4, `RequiredScalars` 7, `OptionalDescription` 5)
- **VERIFIED (existing regression classes, final run, all passing):** `ClientRequestBindingTest` 8, `ClientPutRequestValidationTest` 20, `ClientRequestValidationTest` 33, `ClientResponseShapeTest` 2, `DocumentRequestValidationTest` 20, `DomainPersistenceTest` 19, `DocumentAssociationTest` 1, `GlobalExceptionHandlerTest` 8, `HumanItChallengeApplicationTests` 1, and the `config.*` classes (`SourceScanAuditTest` 2, `JwtSecretValidationTest` 6, and others).
- **Failed:** none in the final run. Intermediate failures are listed in §7 (commands 5, 7, 8, 9/10); each was diagnosed and fixed, and none is present in the final run.
- **Skipped:** 0.

### 8.2 Scenario coverage (tasking Phase 6 list)

| # | Scenario | Evidence (tests) |
|---|---|---|
| 1 | ABSENT vs PRESENT_NULL vs PRESENT_VALUE | `PatchValueTest`; `PatchValueJacksonBindingTest.documentPatchDescriptionHasThreeDistinguishableStates` |
| 2 | Generic Jackson deserialization | `genericRootStringBindsToPatchValue`, `genericRootCollectionOfNestedPatchDtosBinds`, `localDateScalarBindsWithTriStateSemantics` |
| 3 | Missing properties and explicit null | `missingPropertiesRemainAbsent`, `explicitNullBecomesPresentNull`, `genericRootNullBindsToPresentNull` |
| 4 | Nested PATCH DTO deserialization | `nestedDocumentsKeepPerScalarTriStateStates` |
| 5 | Collections of nested PATCH DTOs | `genericRootCollectionOfNestedPatchDtosBinds`, `emptyDocumentsArrayIsPresentValueWithNoEntries`, `nullElementInsideDocumentsBindsAsNullEntry` |
| 6 | Client required scalar validation | `ClientPatchRequestValidationTest.RequiredScalars` (explicit null for all five; blank values; omitted accepted) |
| 7 | Document required scalar validation | `DocumentPatchRequestValidationTest.RequiredScalars` |
| 8 | Optional description clearing | `DocumentPatchRequestValidationTest.OptionalDescription`; `ClientPatchRequestValidationTest.NestedDocumentIdentity.existingDocumentDescriptionMayBeCleared` |
| 9 | Client PATCH `documents: null` rejection | `ClientPatchRequestValidationTest.DocumentsCollection.explicitNullDocumentsIsRejected` |
| 10 | Client PATCH omitted and empty `documents` | `DocumentsCollection.omittedDocumentsPreserveExistingDocuments`, `emptyDocumentsPreserveExistingDocuments` |
| 11 | Nested existing/new Document identity rules | `NestedDocumentIdentity` (13 tests: existing valid; new valid; new missing number or date; explicit null number on new; id-only; version-only; etc.) |
| 12 | Negative expected versions | `ClientPatchRequestValidationTest.OmittedAndSuppliedScalars.negativeVersionIsRejected`; `NestedDocumentIdentity.negativeNestedVersionIsRejected`; `DocumentPatchRequestValidationTest.VersionField.negativeVersionIsRejected` |
| 13 | Length limits and email validation | `LengthLimits` (exact boundary accepted, boundary+1 rejected for every Client scalar, including email 255/256); `EmailFormat` (invalid samples rejected; valid accepted; PATCH-vs-POST parity for six samples); Document `number` 100/101 and `description` 1000/1001 |
| 14 | Part 1 POST/PUT behaviour | Part 1 suites all pass (see §8.1): POST `documents` omitted/null/empty valid; PUT `documents` omitted/empty preserve; PUT `documents: null` invalid |
| 15 | T2.1 regression | `DomainPersistenceTest` 19 and `DocumentAssociationTest` 1 pass unchanged |

### 8.3 PATCH contract coverage (requirements-decisions §4, §20; D-13)

- **VERIFIED — Client PATCH scalars:** omitted preserves (no violation, `ABSENT`); explicit null invalid (`@PatchRequired`); non-null validated and applied (`@NotBlank`, `@Size`, `@Email`).
- **VERIFIED — Client PATCH documents:** omitted and empty preserve; `documents: null` invalid; null entries invalid (`documents[0].<list element>` `NotNull`).
- **VERIFIED — Nested existing Document:** `id` and `version` both required (pair rule); partial scalars (`ABSENT` allowed); explicit-null required scalars invalid; `description: null` clears.
- **VERIFIED — Nested new Document:** neither `id` nor `version`; `number` and `expirationDate` must be `PRESENT_VALUE` (`newDocumentFieldsComplete`).
- **VERIFIED — Document PATCH:** `version` required and non-negative; `number` and `expirationDate` required (explicit null invalid); `description` optional, clearable, length ≤ 1000.
- **VERIFIED — Expired Documents:** accepted in Document PATCH and nested Client PATCH (no expiry rule).
- **VERIFIED — No country-specific formats** added; no ownership, unknown-ID, version-comparison or merge logic in the DTO layer.
- **Not in DTO scope (by design, D-11/D-13):** ownership resolution, unknown-ID rejection, version comparison (HTTP 409), aggregate merge, transactions, and the `X-Client-Version` header. These belong to later service/controller tasks.

### 8.4 Jackson binding evidence

- **VERIFIED:** A missing property never calls the deserializer; the field default `PatchValue.absent()` is kept.
- **VERIFIED:** An explicit JSON `null` yields `PRESENT_NULL` for the plain `JsonMapper` used by the tests (`explicitNullBecomesPresentNull`) and for the Spring-configured `ObjectMapper` built from `Jackson2ObjectMapperBuilder` (`springConfiguredObjectMapperPreservesTheThreeStates`).
- **VERIFIED:** Root-level `readValue("null", TypeReference<PatchValue<String>>)` yields `PRESENT_NULL` (`genericRootNullBindsToPresentNull`).
- **VERIFIED:** No global Jackson configuration change; the only Jackson hook is the class-level `@JsonDeserialize` on `PatchValue`.

### 8.5 Validation wiring evidence

- **VERIFIED:** Hibernate Validator discovers `PatchValueExtractor` through `META-INF/services` in both the plain `Validation` factory (used by the DTO tests) and Spring's `LocalValidatorFactoryBean` (`PatchValueValidationWiringTest`).
- **VERIFIED:** Constraints on the type argument are evaluated for `PRESENT_VALUE` only (`springValidatorIgnoresAbsentAndPresentNullTypeArgumentConstraints`).

### 8.6 Git state (read-only)

- **VERIFIED:** `git status --short` lists the six pre-existing modified documents, the six pre-existing untracked history archives, and the untracked directories `src/main/java/com/humanit/challenge/dto/`, `src/main/resources/META-INF/`, `src/test/java/com/humanit/challenge/dto/`. The Part 1 `dto/` files are also untracked (they were never committed).
- **VERIFIED:** No tracked `src/` file is modified (`git diff --stat` shows documents only).
- **VERIFIED:** Nothing staged, committed, pushed or merged.

### 8.7 Evidence markers summary

- **VERIFIED:** Everything in §8.1–8.6 comes from the Maven runs in §7, the Surefire XML reports, the source files in §6, and `git` read-only commands.
- **RECONSTRUCTED:** Execution identifier (§2).
- **NOT AVAILABLE:** Independent Tester verification and Reviewer audit of Part 2. Neither has been performed or read, and no such report is claimed.
- **PENDING VERIFICATION:** HTTP-level binding through Spring MVC `@RequestBody` (no controllers exist in T2.2; to be verified with MockMvc in T2.3/T2.4). Also PENDING VERIFICATION: behaviour under the production profile with PostgreSQL (not relevant to DTOs; recorded for completeness).

## 9. Findings and severity

| ID | Severity | Finding | Disposition |
|---|---|---|---|
| **F-P2-01** | **Minor (review focus)** | Constraint evaluation on `PatchValue` depends on a new service registration file `src/main/resources/META-INF/services/jakarta.validation.valueextraction.ValueExtractor`. It is a validation-only SPI registration (no Jackson or global ObjectMapper change). It is active for any Hibernate Validator factory that uses default ServiceLoader discovery, which includes the application's Spring validator. Verified by `PatchValueValidationWiringTest`. Because this is a new resource-file kind, the Tech Lead and Reviewer should explicitly confirm that it is within the approved T2.2 scope. | **Open — for Reviewer / Tech Lead confirmation.** |
| **F-P2-02** | Info | An explicit `"id": null` on a nested Client PATCH entry is indistinguishable from an omitted `id` (identity is a plain `Long`, not tri-state). Such an entry is treated as a new Document and must then supply `number` and `expirationDate`. Explicit null on `version` behaves the same way. | Not changed. Intentional under D-13 (identity is not a PATCH scalar). The service layer should treat `id` and `version` as identity, not PATCH fields. |
| **F-P2-03** | Info (carried from F-P1-06) | Unknown JSON properties are not rejected at binding. The Spring Boot default `ObjectMapper` ignores them; the plain `JsonMapper` in the unit tests fails on them. | Not changed (no global config). Decide at the REST layer (T2.3/T2.4). |
| **F-P2-04** | Info | Scalar coercion follows Jackson defaults (for example a JSON number bound into a `String` property is coerced). Part 1 DTOs behave the same way. | Not changed; consistent with Part 1. |
| **F-P2-05** | Info | Email validation is the standard Hibernate `@Email` (same as POST/PUT). Parity is verified only for six sample inputs, not exhaustively. | Accepted. |
| **F-P2-06** | Info | Nested generic depth tested: `PatchValue<scalar>`, `PatchValue<LocalDate>`, `PatchValue<List<DTO>>`. Deeper nesting such as `PatchValue<List<PatchValue<...>>>` is not used by any approved DTO and is not tested. | Accepted as out of scope. |
| **F-P2-07** | Info (process) | The first full run after the PATCH draft failed 9 tests (design defect, fixed; §7 commands 5–8). A later targeted run produced stale output, which caused misleading `Unresolved compilation problems` errors (§7 command 9). Only clean-build results are used as evidence. | Closed; recorded for transparency. |
| **F-P2-08** | Info | Hygiene: three test files were written through PowerShell with CRLF line endings. They were normalized to LF. No logic change. | Closed. |
| **F-P1-03 (preserve)** | Info | Client PUT `documents: null` rejection (Part 1 mechanism) is unchanged and still covered by `ClientRequestBindingTest.putWithExplicitNullDocumentsIsRejected` and `ClientPutRequestValidationTest`. | Preserved; passing. |
| **F-P1-01 (resolved)** | — | Package question from Part 1 is resolved by the Tech Lead's confirmation (`dto.client`, `dto.document`, `dto.patch`). | Closed. |
| **RF2-1 (carried)** | Minor, non-blocking | Project-level tracking artifacts (`reports/evidence-gap-register.md` G-07, `reports/task-status-report.md`) describe a stale status. | **Not touched** (out of scope). |

**Critical:** none identified. **Major:** none identified by the Developer. (The Developer's own first-draft defect in §7 command 5 was a Major functional defect that was fixed before the final run.)

## 10. Decisions and reasoning

1. **Generic PATCH validation: `ValueExtractor` on type arguments (chosen).** The first draft used custom per-type constraints (`@PatchText`) and a `@Valid` getter cascade. The cascade did not reach list elements (§7 command 5), and custom text constraints would have needed a hand-rolled email rule. The chosen design puts standard constraints on the type argument (`PatchValue<@NotBlank @Email @Size(max = 255) String>`). Hibernate Validator then evaluates them for `PRESENT_VALUE` only, through a small project-owned `ValueExtractor` registered via ServiceLoader. Benefits: standard `@Email`/`@Size`/`@NotBlank` semantics, exact parity with POST/PUT, clean property paths (`documents[0].number`), and no custom text constraint. Cost: F-P2-01.
2. **Required-ness is state-based (`@PatchRequired`).** `ABSENT` is valid for every PATCH property; `PRESENT_NULL` is invalid for required ones. The extractor cannot express this, because it only sees values.
3. **Expected version fields are plain `Long`.** Versions are not PATCH scalars; omitted and explicit-null versions are both rejected by `@NotNull`.
4. **Nested identity is plain `Long` plus class-level `@AssertTrue` rules.** Mirrors the Part 1 nested PUT rule and the D-13 identity model. New-entry completeness is an `@AssertTrue` on `isNewDocumentFieldsComplete()` because it depends on two fields.
5. **`PatchValue.of(null)` yields `PRESENT_NULL`.** A supplied Java `null` is never reported as `ABSENT`, so that an "omitted" state can only come from a missing JSON property.
6. **`PatchValue.toString()` reports the state only.** Avoids leaking Document numbers or other values into logs.
7. **Class-level `@JsonDeserialize` on `PatchValue`.** Keeps Jackson configuration local; no `ObjectMapper` customisation and no global change, per the tasking.
8. **Alternatives considered and rejected:** `Optional` (prohibited by D-13); `JsonNullable` (excluded by D-13); `JsonNode`-only DTOs (prohibited); plain nullable fields (cannot express `ABSENT` vs `PRESENT_NULL`); a global Jackson `Nulls` configuration (too broad); getter-based unwrapped views (path names would leak into API errors); a hand-written email regex as the primary rule (divergence risk from POST).

## 11. Final status

**PART 2 — COMPLETE WITHIN ITS SCOPE, PENDING INDEPENDENT TESTER AND REVIEWER VERIFICATION AND HUMAN TECH LEAD ACCEPTANCE.**

- **Tests:** `.\mvnw.cmd -B clean test` — 233 run, 0 failed, 0 errors, 0 skipped, `BUILD SUCCESS`, exit code `0` (VERIFIED).
- **Implemented:** tri-state `PatchValue<T>` with Jackson binding and validation extractor; `ClientPatchRequest`, `ClientDocumentPatchRequest`, `DocumentPatchRequest`; PATCH validation; 89 new focused tests.
- **Not claimed:** T2.2 acceptance, Part 3 completion, Tech Lead approval of any phase gate, Tester verification, Reviewer audit, or any Git mutation.

## 12. Outstanding issues

1. **F-P2-01 (Minor, needs confirmation):** Confirm that the ValueExtractor ServiceLoader registration is within approved T2.2 scope (it is the only new resource file).
2. **F-P2-02 (Info):** Explicit `id: null` on nested PATCH entries is treated as omitted (identity model). Service/REST layer must handle identity explicitly.
3. **F-P2-03 (Info, carried):** Unknown-property policy is undecided; decide at the REST layer.
4. **PENDING VERIFICATION:** HTTP-level PATCH binding through Spring MVC `@RequestBody` (MockMvc) — no controllers exist in T2.2.
5. **NOT AVAILABLE:** Independent Tester verification and Reviewer audit of Part 2.
6. **RF2-1 (carried, Minor):** Stale project tracking status; not touched.
7. **Deferred to Part 3 and later services:** dedicated mappers (`com.humanit.challenge.mapper`), mapper tests, final regression pass, and any Git commit, which requires separate explicit authorization.

## 13. Handoff to next agent

**Tester (independent, before reading this report):** Run `.\mvnw.cmd -B clean test` on the working tree. Verify the tri-state semantics directly from `src/main/java/com/humanit/challenge/dto/patch/` and the PATCH DTOs: omitted vs `null` vs value for each PATCH property; required fields reject `null`; `description: null` is accepted; `documents: null` is rejected; nested identity; negative versions; length and email rules. Verify that the ValueExtractor is discovered by Spring's validator. Do not treat the 233-test count as proof without checking the assertions.

**Reviewer (independent audit, before reading this report, the Tester report or the Architect report):** Audit `src/main/java/com/humanit/challenge/dto/patch/**`, the three PATCH DTOs, and `src/main/resources/META-INF/services/**`. Focus on: (a) whether the ValueExtractor SPI registration is an acceptable scope change (F-P2-01); (b) correctness of `PRESENT_NULL` handling in `PatchValueDeserializer.getNullValue`; (c) whether the nested identity rules match D-13; (d) whether any ownership, version-comparison or merge logic leaked into the DTO layer.

**Tech Lead:** Decide on F-P2-01 and authorize (or not) Part 3. Part 3 is: dedicated mappers under `com.humanit.challenge.mapper`, mapper tests, regression run, and only if separately authorized, one consolidated Git commit. No Git mutation has been performed in this execution.

**Part 3 readiness:** Part 2 provides the PATCH request contracts a mapper or service will consume. Part 3 must not change `PatchValue` semantics. Part 3 must map `PRESENT_NULL` for `description` to a clear, and must not map an `ABSENT` value.

---
**Report status:** Written for `T2.2-EXEC-010-DEV-02` (RECONSTRUCTED identifier; see §2). Previous primary archived unchanged at `agent-reports/T2.2/history/developer-003.md`.
**Outstanding findings:** F-P2-01 (Minor, scope confirmation), F-P2-02 / F-P2-03 (Info), PENDING VERIFICATION of HTTP binding, Tester and Reviewer NOT AVAILABLE, RF2-1 (Minor, carried, not touched).
