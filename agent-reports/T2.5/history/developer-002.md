# Developer Report — T2.5 Minor Test-Coverage Findings

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2, T2.5): T2.5 — Aggregate update semantics and Document identity preservation.

## 2. Execution identifier and date

**VERIFIED** (direct tasking/environment): `T2.5-EXEC-005-DEV` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Developer; `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED** (direct Human Tech Lead tasking): Authorized to add exactly two Minor regression coverage findings from the independent T2.5 Tester and Reviewer: persistence-backed Client scalar PATCH omission coverage, and aggregate-update HTTP 404/409 Problem Details coverage exercising actual service behavior. Test-source changes only; no production changes, endpoints, DTOs, mappers, services, dependencies, configuration, migrations, unrelated refactoring, decisions/planning edits, T2.6/T2.7 expansion, Git commit/merge/push, or self-approval.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`, `implementation-plan.md` (T2.5 and adjacent scope boundaries), `requirements-decisions.md`, and `agent-reports/decisions.md`.
- **VERIFIED:** Previous T2.5 role reports: `agent-reports/T2.5/developer-report.md` (execution `T2.5-EXEC-002-DEV`), `tester-report.md` (execution `T2.5-EXEC-003-TEST`) and `reviewer-report.md` (execution `T2.5-EXEC-004-REV`). The Tester and Reviewer both identified the same two Minor test coverage findings; those earlier findings are preserved as historical evidence and not overwritten.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`, `controller/ClientControllerTest.java`, `controller/DocumentControllerTest.java`, `exception/GlobalExceptionHandlerTest.java` and `exception/CommitTimeOptimisticLockIntegrationTest.java`.
- **VERIFIED:** Relevant production contracts inspected to construct test-only calls: `ClientController.java`, `ClientService.java`, `GlobalExceptionHandler.java`, `ClientPatchRequest.java`, `ClientPutRequest.java`, `ClientDocumentPutRequest.java`, `ClientCreateRequest.java`, `DocumentCreateRequest.java`, `ClientResponse.java`, and `StartupTestHarness.java`. No production source was edited.
- **VERIFIED:** The prior primary Developer report was archived unchanged as `agent-reports/T2.5/history/developer-001.md` before replacing the primary report. The previous report execution identifier and content remain represented in that archive.

## 6. Files changed

- **VERIFIED (direct edits in this execution):** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java` — one persistence-backed scalar PATCH omission regression test.
- **VERIFIED (direct edits in this execution):** `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java` — new two-case MockMvc test class with the real application, `ClientService`, and H2/JPA persistence.
- **VERIFIED (reporting):** `agent-reports/T2.5/history/developer-001.md` — unchanged archive copy of the previous primary Developer report; `agent-reports/T2.5/developer-report.md` — this execution report.
- **VERIFIED (scope):** No production source, DTO, mapper, service, repository, dependency, configuration, migration, planning file, requirement, decision log, or other agent report was edited in this execution. No Git commit, merge, or push was performed.
- **VERIFIED (`git status --short` snapshot after test execution):** Git also reported `agent-reports/decisions.md` and `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java` as modified, and two `agent-reports/GOV-DECISIONS-RECONSTRUCTION-*` directories as untracked. This execution's edits did not target those paths. **NOT AVAILABLE:** No initial `git status --short` snapshot was taken before editing, so their pre-existing status or attribution cannot be established from this execution; they were not modified or cleaned up.

## 7. Commands executed

- **VERIFIED:** Focused affected test command: `.\mvnw.cmd -B "-Dtest=ClientServiceTest,ClientAggregateUpdateHttpIntegrationTest" test` — BUILD SUCCESS; 17 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED:** Required full suite: `.\mvnw.cmd -B clean test` — BUILD SUCCESS; 288 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED:** `git status --short`, `git diff --check`, and `git diff --stat` were run. `git diff --check` emitted no whitespace-error output; Git reported line-ending conversion warnings. The status/stat output also showed the unrelated paths described in Section 6.
- **VERIFIED:** No commit, merge, or push command was run.

## 8. Evidence and observed results

- **VERIFIED** (`ClientServiceTest.java`, focused Surefire output): Added `clientPatchChangesSuppliedScalarAndPreservesOmittedScalarsAfterReload`. It creates/persists a Client with all five editable scalar values populated and a Document, PATCHes only `firstName` through the existing `ClientPatchRequest`/`PatchValue.of` contract using the expected DTO version, flushes and clears the persistence context, reloads the Client through `ClientRepository`, then asserts the supplied `firstName` changed and omitted `lastName`, `taxIdentifier`, `email`, and `phoneNumber` remain unchanged.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.java`, focused MockMvc run): Added a real Spring Boot/H2 application test with MockMvc filters disabled under the existing controller-test convention. Client and Document fixtures are created by the real `ClientService`; HTTP PUT requests enter `ClientController` and exercise the actual aggregate service and persistence-backed ownership/unknown-ID branches.
- **VERIFIED** (same test and run): Unknown nested Document ID produces HTTP 404 with `application/problem+json` and asserted Problem Details `type=about:blank`, title `Not Found`, status `404`, detail `Document not found`, and request-path `instance`.
- **VERIFIED** (same test and run): A nested Document persisted for another Client produces HTTP 409 with `application/problem+json` and asserted Problem Details `type=about:blank`, title `Conflict`, status `409`, detail `Document does not belong to client`, and request-path `instance`.
- **VERIFIED** (focused Maven output): `ClientServiceTest`: 15 tests passed; `ClientAggregateUpdateHttpIntegrationTest`: 2 tests passed; total 17, no failures/errors/skips.
- **VERIFIED** (full Maven output): `.\mvnw.cmd -B clean test` completed successfully: 288 tests, 0 failures, 0 errors, 0 skipped. Existing runtime and environment warnings were non-fatal.
- **PENDING VERIFICATION:** Independent Tester verification and independent Reviewer audit of this execution have not occurred. Developer-run results do not substitute for either.

## 9. Findings and severity

- **VERIFIED — Prior Minor finding 1 addressed with regression coverage:** Client scalar PATCH omission is now asserted against values reloaded from persistence after PATCH; the test is confined to the authorized T2.5 behavior and does not assert T2.6 concurrency behavior.
- **VERIFIED — Prior Minor finding 2 addressed with HTTP regression coverage:** Unknown and foreign-owned nested Document IDs are covered through aggregate Client PUT using MockMvc, the actual controller, real aggregate service, and H2/JPA persistence; both status and required Problem Details fields are asserted.
- **VERIFIED — No production defect observed or corrected:** Both changes are test-only. No new test framework or dependency was introduced.
- **PENDING VERIFICATION:** Findings are considered implemented by this Developer execution only; independent Tester and Reviewer disposition remains outstanding.

## 10. Decisions and reasoning

- **VERIFIED (direct tasking and approved T2.5 scope):** Exactly two coverage additions were made. The scalar case uses direct DTO construction in `ClientServiceTest`, which matches existing service integration tests and verifies actual persistence after an explicit EntityManager clear.
- **VERIFIED (existing test infrastructure):** Aggregate HTTP cases reuse Spring Boot Test, MockMvc, H2, `StartupTestHarness.supplyDevExternals`, and the existing application/controller/service/exception handler. PUT was selected because the request contract permits a concrete nested existing-Document identity pair and both requested failure cases apply to aggregate update behavior.
- **VERIFIED (scope boundary):** The tests use request version values only to satisfy the existing DTO/service preconditions. They do not make concurrency race, optimistic-lock rollback, or T2.7 transaction-wide rollback claims.
- **VERIFIED (prior report distinction):** The earlier Developer execution `T2.5-EXEC-002-DEV` and independent Tester/Reviewer executions `T2.5-EXEC-003-TEST` and `T2.5-EXEC-004-REV` remain historical results. This report records only current execution changes and outcomes; it does not rewrite or independently verify their earlier claims.
- **NOT AVAILABLE:** Human Tech Lead final acceptance of T2.5 or approval of these test outcomes. No self-approval is made.

## 11. Final status

**VERIFIED — IMPLEMENTATION COMPLETE; READY FOR INDEPENDENT VERIFICATION: YES.** Both authorized test additions are implemented, and focused plus full test runs passed. This is not a Tester result, Reviewer recommendation, or Human Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Tester to independently verify the two regressions before reading this Developer report, following `agent-reports/README.md`.
2. **PENDING VERIFICATION:** Reviewer to independently audit before reading the Architect, Developer, and Tester conclusions.
3. **PENDING VERIFICATION:** Human Tech Lead retains final acceptance authority; no acceptance is claimed.
4. **VERIFIED (scope boundary):** T2.6 concurrency/version guarantees and T2.7 transaction-wide rollback guarantees were not tested or claimed.
5. **NOT AVAILABLE:** Attribution/timing of the unrelated paths visible in the current Git status cannot be established because the initial Git status was not recorded. They remain untouched by this execution.

## 13. Handoff to next agent

- **VERIFIED:** Two regression gaps identified by the previous Tester and Reviewer have corresponding tests in this execution; report and archive are available for subsequent workflow stages.
- **VERIFIED:** Tester must independently verify first without reading this report; Reviewer must perform the independent audit first, as specified in the reporting convention.
- **PENDING VERIFICATION:** After independent Tester and Reviewer work, the Human Tech Lead decides acceptance. No commit is authorized or performed by this execution.
