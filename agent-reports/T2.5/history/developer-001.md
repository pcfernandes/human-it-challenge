# Developer Report — T2.5 Aggregate Update Semantics and Document Identity Preservation

## 1. Task ID

**VERIFIED (`implementation-plan.md`, Section 6):** T2.5 — Aggregate update semantics and Document identity preservation.

## 2. Execution identifier and date

**VERIFIED (direct tasking/environment):** `T2.5-EXEC-002-DEV` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Developer; `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct tasking):** Human Tech Lead authorized implementation of the approved T2.5 scope: focused deterministic persistence-backed tests for aggregate update semantics and Document identity preservation, with a service-layer fix only if a test demonstrates a concrete defect. No new endpoints, dependencies, migrations, unrelated refactoring, Git commit/push, self-approval, or expansion into T2.6/T2.7 was authorized.

## 5. Files inspected

- **VERIFIED:** `implementation-plan.md` (T2.5, adjacent task boundaries), `requirements-decisions.md` (update, ownership and consistency contracts), `agent-reports/decisions.md` (D-10, D-11, D-13), `agent-reports/T2.5/architect-proposal.md`, and `agent-reports/README.md`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`, `DocumentService.java`, `mapper/ClientMapper.java`, `mapper/DocumentMapper.java`, `domain/Client.java`, `domain/Document.java`, `repository/ClientRepository.java`, `DocumentRepository.java`, and the Client/Document request and response DTOs used by the update paths.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java` and `DocumentServiceTest.java`.
- **VERIFIED:** No earlier `agent-reports/T2.5/developer-report.md` or Developer history file existed when this execution began; no prior report required archival.

## 6. Files changed

- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java` — added aggregate PUT/PATCH persistence scenarios for collection preservation, in-place update, creation, omission, not-found and cross-owner rejection.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java` — strengthened standalone PUT/PATCH identity/owner checks and added persistence-backed comparison of standalone and aggregate Document field semantics.
- **VERIFIED:** `agent-reports/T2.5/developer-report.md` — this execution report.
- **VERIFIED:** No production source, DTO, mapper, entity, repository, migration, configuration, dependency or endpoint was changed. No Git commit or push was performed.

## 7. Commands executed

- **VERIFIED:** The initial focused command `.\mvnw.cmd -B -Dtest=ClientServiceTest,DocumentServiceTest test` was rejected by PowerShell parsing at the comma before Maven ran.
- **VERIFIED:** `.\mvnw.cmd -B "-Dtest=ClientServiceTest,DocumentServiceTest" test` was run three times. The first run exposed invalid duplicate-number test data (the existing global uniqueness rule correctly returned 409); the next run exposed an overly broad global row-count assertion in the test; after correcting those test assumptions, the focused run passed: 26 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED:** Required full validation was run once after focused tests: `.\mvnw.cmd -B clean test` — BUILD SUCCESS; 285 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED:** `git status --short`, `git diff --stat`, and `git diff --check` were used for change-scope/whitespace inspection. `git diff --check` reported no whitespace errors; Git printed only line-ending conversion warnings for the two modified test files.

## 8. Evidence and observed results

- **VERIFIED (`ClientServiceTest.java`; focused run output and `target/surefire-reports/com.humanit.challenge.service.ClientServiceTest.txt`):** Client PUT with omitted and explicitly empty `documents` preserves all existing Document IDs and rows.
- **VERIFIED (same evidence):** Client PUT updates a supplied existing Document in place, creates a new Document from an entry without ID/version, and retains an existing Document omitted from the supplied list. Persisted Document count confirms no removal or unexpected extra row in this isolated test context.
- **VERIFIED (same evidence):** Client PATCH with omitted and explicitly empty `documents` preserves the collection. Nested PATCH changes the supplied description only, keeps the existing Document ID/owner and omitted number/date, and retains the other Document.
- **VERIFIED (same evidence):** Unknown nested Document ID returns HTTP 404 and does not create a Document. A Document owned by a different Client returns HTTP 409; the persisted owner and both Client collections remain associated with their original Document IDs.
- **VERIFIED (`DocumentServiceTest.java`; focused run output and `target/surefire-reports/com.humanit.challenge.service.DocumentServiceTest.txt`):** Standalone Document PUT retains ID and owner while replacing editable fields. Standalone PATCH retains ID and owner, updates a supplied field, and preserves omitted fields. A persistence-backed comparison confirms matching PUT/PATCH editable-field behavior between standalone and Client aggregate paths. Collection counts scoped to the test Client remain unchanged by updates.
- **VERIFIED (focused run):** `ClientServiceTest`: 14 tests passed; `DocumentServiceTest`: 12 tests passed; total 26 passed.
- **VERIFIED (full run output):** `.\mvnw.cmd -B clean test` completed successfully with 285 tests passed. The build emitted existing environment/runtime warnings (including restricted Flyway metadata access and Spring `open-in-view`), but these did not fail the build.
- **VERIFIED (observed service behavior and passing regression tests):** No implicit deletion occurs on tested PUT/PATCH paths; existing Documents not supplied remain persisted. The tests invoke services with H2/JPA-backed `@DataJpaTest` contexts rather than mocks.
- **PENDING VERIFICATION:** Independent Tester verification and independent Reviewer audit have not yet occurred in this execution. These Developer test results do not substitute for either role.

## 9. Findings and severity

- **VERIFIED — No concrete production defect found (Critical/Major/Minor: none).** The inspected service logic passed the added persistence-backed scenarios; no service-layer production fix was justified, so aggregate logic was not rewritten.
- **VERIFIED — Test-authoring corrections only (not product defects):** Early focused attempts used duplicate global Document numbers and an unscoped total row count in a suite that retains rows across non-transactional test methods. The test data/assertion were corrected; final focused and full suites pass.
- **PENDING VERIFICATION — API-level binding/status behavior:** This execution exercised service methods with request DTOs; it did not add a new MockMvc-level check for JSON omission/null binding or the controller's Problem Details response body. Existing DTO binding tests and endpoint behavior were not changed. The requested service/use-case semantics are persistence-backed and tested here.

## 10. Decisions and reasoning

- **VERIFIED (D-11, D-13, approved plan):** Existing services, DTOs, mappers and repositories were reused. Because no required scenario demonstrated a production defect, the smallest compliant implementation was to add regression tests only.
- **VERIFIED (test evidence):** Test assertions are scoped to stable observable outcomes—IDs, editable field values, owner IDs, HTTP status exceptions and row counts scoped to a Client where other test methods persist rows. No ordering/transaction-wide rollback guarantees outside T2.5 were added.
- **VERIFIED (scope boundary):** Version checks required to submit existing nested updates are used as request preconditions; concurrency race behavior, parent force-increment contract, and transaction-wide rollback are not claimed as newly validated. Those remain T2.6/T2.7 scope.
- **RECONSTRUCTED (based on H2-backed service tests and the task):** The new tests establish persistence behavior for the required merge/identity cases under the project's test database. They do not establish PostgreSQL-specific behavior or independent acceptance.

## 11. Final status

**VERIFIED — IMPLEMENTATION COMPLETE; READY FOR TESTER: YES.** Only the two focused test classes and this report changed. Focused tests and the required full clean test suite passed. This is not a Reviewer recommendation or Human Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Tester must independently verify the T2.5 scenarios before reading this Developer report, per `agent-reports/README.md`.
2. **PENDING VERIFICATION:** Reviewer must independently audit before reading the Architect, Developer and Tester reports.
3. **VERIFIED (scope boundary):** T2.6 version/concurrency guarantees and T2.7 transaction-wide rollback remain outside this execution; no claims are made for those requirements.
4. **NOT AVAILABLE:** Human Tech Lead final acceptance of T2.5. No approval is claimed or implied.

## 13. Handoff to next agent

- **VERIFIED:** Tester may begin independent T2.5 verification. Do not read this report until that verification is complete.
- **VERIFIED:** Reviewer must complete the independent audit before reading this report or the other role reports.
- **PENDING VERIFICATION:** Human Tech Lead to decide final T2.5 acceptance after Tester and Reviewer reports. No additional Developer change is indicated unless independent verification identifies a concrete, authorized defect.
