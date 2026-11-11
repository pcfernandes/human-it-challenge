# Developer Report — T2.7 Transactional Atomicity and Rollback

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 2, T2.7): T2.7 — transactional atomicity and deletion rules.
- **VERIFIED** (direct Tech Lead tasking): `T2.7-EXEC-002-DEV`.

## 2. Execution identifier and date

- **VERIFIED** (direct tasking/environment): Execution `T2.7-EXEC-002-DEV`, performed on 2026-11-11.

## 3. Agent role and model

- **VERIFIED** (runtime instructions): Developer role; `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct Tech Lead tasking): Implement the requested Client POST and PATCH persistence-backed rollback tests, extend Document DELETE service coverage, and synchronize the in-memory Client/Document relationship before the existing explicit Document row deletion. Preserve transaction boundaries and T2.5/T2.6 behavior. No commit or push.
- **VERIFIED** (direct tasking): No new DTOs, endpoints, dependencies, migrations, transaction abstractions, optional negative-header HTTP tests, unrelated refactoring, governance/requirements edits, branch switch, merge, or other out-of-scope changes.
- **VERIFIED** (`implementation-plan.md`, T2.7; `requirements-decisions.md`, Section 20): Failed operations roll back; stale versions return HTTP 409 Problem Details; Document changes use the existing parent Client optimistic-force-increment behavior in the same transaction.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `agent-reports/T2.7/architect-proposal.md`; `implementation-plan.md` (T2.7); `requirements-decisions.md` (version/transaction contract).
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`; `src/main/java/com/humanit/challenge/service/DocumentService.java`; `src/main/java/com/humanit/challenge/domain/Client.java`; `src/main/java/com/humanit/challenge/repository/ClientRepository.java`; `src/main/java/com/humanit/challenge/repository/DocumentRepository.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`; `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/dto/client/ClientPatchRequest.java` and `ClientDocumentPatchRequest.java` were inspected to construct requests consistent with the existing PATCH contract.
- **VERIFIED** (file/glob inspection): Before this execution, no `agent-reports/T2.7/developer-report.md` existed, so no prior Developer report required archiving.

## 6. Files changed

- **VERIFIED** (`git diff`): `src/main/java/com/humanit/challenge/service/DocumentService.java` — use the already-loaded Client returned by `lockClient` and call `client.removeDocument(document)` before the existing explicit `documentRepository.delete(document)` and flushes. The existing `@Transactional` boundary and explicit row deletion remain unchanged.
- **VERIFIED** (`git diff`): `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java` — added one POST duplicate-nested-Document-number rollback test and one PATCH late-stale-nested-Document rollback test.
- **VERIFIED** (`git diff`): `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java` — extended failed/successful DELETE assertions, including fresh Client collection loading and parent version checks.
- **VERIFIED** (report file reread): `agent-reports/T2.7/developer-report.md` — this execution report.
- **VERIFIED** (`git status --short --untracked-files=all` before report creation): Pre-existing worktree changes included `agent-reports/decisions.md`, `agent-reports/GOV-DECISIONS-RECONSTRUCTION-001/`, `agent-reports/GOV-DECISIONS-RECONSTRUCTION-002/`, and T2.7 Architect proposal/history files. Those files were not changed by this execution. No branch switch, commit, push, or merge was performed.

## 7. Commands executed

- **VERIFIED** (terminal output): `git status --short` — showed pre-existing changes described in Section 6 before application edits.
- **VERIFIED** (terminal output): Initial focused command `.\mvnw.cmd -B -Dtest=DocumentServiceTest,ClientAggregateUpdateHttpIntegrationTest test` — PowerShell parsed the unquoted comma as syntax and did not start Maven.
- **VERIFIED** (Maven output): `.\mvnw.cmd -B "-Dtest=DocumentServiceTest,ClientAggregateUpdateHttpIntegrationTest" test` — first run executed 22 tests; 20 passed and 2 errored in the new reload assertions with `LazyInitializationException` because the test accessed the lazy collection after the repository transaction closed. The tests were adjusted to reload the Client with a fetch join; no production redesign was made.
- **VERIFIED** (Maven output and Surefire reports under `target/surefire-reports/`): Repeated `.\mvnw.cmd -B "-Dtest=DocumentServiceTest,ClientAggregateUpdateHttpIntegrationTest" test` — 22 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED** (Maven output): `.\mvnw.cmd -B clean test` — full clean suite succeeded: 310 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED** (terminal output): `git diff --check; git status --short --untracked-files=all` — no whitespace errors were reported; status showed the existing unrelated items and the three intended application source/test files. Git emitted line-ending conversion warnings only.
- **VERIFIED** (Maven output): The build logged a non-fatal HTTP 401 while checking metadata for the configured Flyway community DB-support repository; Maven continued and both requested test runs succeeded.

## 8. Evidence and observed results

- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest` Surefire output): The duplicate-number POST test sends a Client with two nested Documents using the same globally unique number, receives HTTP 409 with `application/problem+json`, `about:blank` type, `Conflict` title, and status 409; after `EntityManager.clear()`, neither the Client tax identifier nor the duplicate Document number is found.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest` Surefire output): The PATCH test changes a Client scalar and first Document, supplies a stale version for the second Document, receives HTTP 409 Problem Details, clears the persistence context, and verifies the Client's scalar fields/version and both Documents' fields/versions remain at their original values. Neither attempted replacement Document number exists after reload.
- **VERIFIED** (`DocumentServiceTest` Surefire output): Failed DELETE attempts using stale Client and stale Document versions leave the reloaded Client version unchanged, retain the target and sibling Documents, and leave both IDs in the reloaded Client collection.
- **VERIFIED** (`DocumentServiceTest` Surefire output): Successful DELETE removes the requested row only, retains the sibling, reloads the Client's collection to exactly the sibling Document, and observes the Client version advance by exactly one from the pre-delete version.
- **VERIFIED** (`Document.java`/`Client.java` mapping and `DocumentServiceTest`): The service continues explicit repository deletion; the passing test demonstrates no unintended sibling deletion and expected parent-version change for the successful operation. No transaction boundary or cascade mapping was changed.
- **VERIFIED** (focused Surefire reports): `ClientAggregateUpdateHttpIntegrationTest` — 9 tests, 0 failures/errors; `DocumentServiceTest` — 13 tests, 0 failures/errors. Total focused run: 22 tests, all passed.
- **VERIFIED** (full Maven output): `clean test` completed with 310 tests, 0 failures, 0 errors, and 0 skipped, including the existing T2.5/T2.6 regression suites.
- **VERIFIED** (Maven output): The duplicate nested number attempt produced the expected database uniqueness violation, which was translated to the asserted 409 response; the transaction left no Client or Document behind.

## 9. Findings and severity

1. **VERIFIED — No application regression identified in the requested scope.** Focused tests and the full suite passed after adjusting the fresh-collection test query.
2. **VERIFIED — Test setup issue corrected during this execution (LOW):** The first focused run's new assertions encountered `LazyInitializationException` by traversing a detached lazy collection. A fetch-join reload helper fixed the verification approach; the issue did not require changing application transaction boundaries or JPA mapping.
3. **VERIFIED — No partial Client creation or aggregate PATCH persistence observed:** Both new HTTP tests establish rollback after the expected 409 conflict by clearing and reloading persistence.
4. **VERIFIED — Explicit Document DELETE keeps row deletion and collection synchronization distinct:** The Client helper updates the in-memory inverse collection, while the repository call remains responsible for deleting the row; the sibling and collection assertions passed.
5. **NOT AVAILABLE:** No independent Tester report, Reviewer audit, or Human Tech Lead final acceptance is part of this Developer execution.

## 10. Decisions and reasoning

- **VERIFIED** (diff and direct scope): Kept service transaction annotations and existing flushes as-is; the approved minimal relationship consistency change uses the Client already loaded by `lockClient`.
- **VERIFIED** (`Client.java` and direct scope): Paired `client.removeDocument(document)` with the pre-existing `documentRepository.delete(document)`; did not rely on inverse collection removal as database deletion.
- **VERIFIED** (tests and Maven output): Tested the removal and parent-version behavior instead of modifying JPA cascade mappings, repository interfaces, transaction ownership, or version handling.
- **VERIFIED** (direct tasking): Did not add the optional negative-header HTTP cases because they were expressly excluded from this execution.
- **VERIFIED** (git command outputs): Preserved unrelated worktree changes and did not perform any commit, push, merge, or branch switch.

## 11. Final status

**IMPLEMENTATION COMPLETE; REQUESTED VALIDATION PASSED.** **VERIFIED** (focused Maven output and full clean test output): 22/22 focused tests and 310/310 full-suite tests passed. The T2.7 code/test scope is implemented. This is not a Reviewer audit, independent Tester verification, or Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Independent Tester verification and Reviewer audit remain for their respective authorized workflow stages.
2. **PENDING VERIFICATION** (Human Tech Lead acceptance): Only the Human Tech Lead can accept T2.7; no acceptance is claimed here.
3. **VERIFIED:** The optional negative Document DELETE header HTTP integration tests were not added, as specifically excluded by the authorization.
4. **VERIFIED:** No application defect was identified by the completed focused and full-suite runs. The non-fatal configured repository metadata HTTP 401 did not prevent the build or tests from succeeding.

## 13. Handoff to next agent

- **VERIFIED:** Implementation and report are available for independent Tester verification and Reviewer audit. The Tester should perform independent verification before reading this report; the Reviewer should audit independently before reading Developer/Tester conclusions.
- **PENDING VERIFICATION** (Human Tech Lead acceptance): Human Tech Lead to decide final acceptance after the required verification/audit workflow. No commit or push has been made, per authorization.
