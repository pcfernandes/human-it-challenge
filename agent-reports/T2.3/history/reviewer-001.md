# Reviewer Report — T2.3 Part 1: ClientService retry

## 1. Task ID
T2.3

## 2. Execution identifier and date
T2.3-EXEC-004-REV-01-RETRY — 2026-11-10.

## 3. Agent role and model
Reviewer (Java/Spring Boot). Model: gpt-6-luna.

## 4. Authorized scope
**VERIFIED:** Review `ClientService` and its tests for transactional boundaries, integrity conflicts, delete flush, CRUD/nested documents, and D-11/D-13 compliance. F-03 is accepted for MVP and was not reopened. No T2.2 reconsideration, code changes, Git mutations, or test execution.

## 5. Files inspected
- **VERIFIED:** `agent-reports/README.md`, `implementation-plan.md`, `requirements-decisions.md`, `agent-reports/decisions.md`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`, `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/domain/Client.java`, `Document.java`; `repository/ClientRepository.java`, `DocumentRepository.java`; `mapper/ClientMapper.java`, `DocumentMapper.java`; `dto/client/ClientDocumentPutRequest.java`, `ClientDocumentPatchRequest.java`, `ClientPatchRequest.java`, `ClientPutRequest.java`.
- **VERIFIED:** Existing `agent-reports/T2.3/developer-report.md` and `tester-report.md` were read only after independent code inspection.

## 6. Files changed
`agent-reports/T2.3/reviewer-report.md` only. **VERIFIED:** No existing reviewer primary report was present, so no history archive was required. No application or test files were changed.

## 7. Commands executed
None. **VERIFIED:** Per task instruction, no shell/Bash commands or Maven execution were used.

## 8. Evidence and observed results
- **VERIFIED — transaction boundaries:** `ClientService.java:38,56,63,70,86,110` annotate every remaining public operation transactionally (reads are read-only); create/update/patch/delete flush before returning or translating persistence errors. The former aliases cited by the Tester (`put`, `get`, `findAll`, `deleteById`) are absent from the inspected service. D-11 layer separation is followed: service owns orchestration/transactions, repositories perform persistence, and mappers do DTO conversion.
- **VERIFIED — integrity and delete:** `ClientService.java:48-53,78-83,102-107,114-118` maps database integrity conflicts to HTTP 409; update/delete additionally translate optimistic-lock failures. Delete explicitly calls `clientRepository.flush()` inside its transaction, allowing delete-time failures to be translated before method return. Runtime conflict exceptions cause transaction rollback under the declared Spring boundary. `Client.java:45-48` supplies cascading removal.
- **VERIFIED — CRUD/nested rules:** Service code has create/get/list/update/patch/delete workflows (`ClientService.java:38-119`). Nested PUT/PATCH preserve omitted/empty collections, create new entries, resolve existing IDs, check owner and expected nested version, and reject unknown IDs/cross-owner association (`ClientService.java:145-198`). D-13 DTO contracts enforce identity-pair and required-field validation; mappers do not access repositories or overwrite identity/ownership. D-13/D-11 constraints are observed; T2.6 version-force-increment and T2.8 pagination are separate plan tasks, not reopened here.
- **VERIFIED — tests inspected:** `ClientServiceTest.java:42-173` covers create/retrieve, one PUT, 404, stale client version, duplicate nested document number on create, and a nested create plus scalar PATCH/collection preservation scenario. No test invokes `ClientService.list()` or `delete()`.
- **PENDING VERIFICATION:** Developer report (`agent-reports/T2.3/developer-report.md:28-34`) reports 248 passing tests and six `ClientServiceTest` tests; Tester report (`agent-reports/T2.3/tester-report.md:28-35`) is from an earlier execution and reports five service tests/247 aggregate tests. The current source has six `@Test` methods, but no test run or Surefire artifacts were independently checked in this review; these historical counts are not treated as current verification.

## 9. Findings and severity
- **Minor — F-REV-01, service-test coverage:** `ClientServiceTest.java:42-173` has no successful list or delete test, and therefore does not protect the newly important delete-flush path or cascade behavior. Add service integration coverage for successful delete/cascade and list mapping; add a delete conflict test if the persistence fixture can induce one. This is a coverage gap, not an observed implementation defect.
- **No Critical or Major findings.** Prior Tester findings F-01 (alias transaction bypass), F-02 (misleading create integrity message) and F-04 (missing delete flush) are no longer present in inspected code (`ClientService.java:38-119`, particularly 48-53 and 110-118). F-03 is accepted for MVP per tasking and is not reopened.

## 10. Decisions and reasoning
- **VERIFIED:** Transaction annotations are on the remaining service entry points; removing self-delegating aliases eliminates the cited proxy-bypass route. Conflict translation and explicit delete flush are present.
- **VERIFIED:** The Developer report's fixes for F-01, F-02 and F-04 agree with the inspected source. The Tester report records the pre-fix state and is stale for those findings; its earlier 247/5 test count does not supersede the later source/report, but the later test result remains **PENDING VERIFICATION** because tests were not run here.
- **VERIFIED:** No service workflow or mapping boundary violation against D-11/D-13 was found. F-03 acceptance was honored. No claim of test execution or Tech Lead approval is made.

## 11. Final status
**PASS WITH FINDINGS** — one Minor test-coverage finding; no blocking code finding identified. Not self-approved.

## 12. Outstanding issues
- **Minor:** Add service tests for list and delete/cascade, including coverage of delete flush conflict mapping where practical.
- **PENDING VERIFICATION:** Independently rerun the authorized test suite before acceptance; test execution was not part of this review.
- **VERIFIED:** Ready for ClientController: **YES**. The reviewed service workflows and layer boundaries are suitable for controller integration; controller endpoint behavior and full endpoint coverage remain for the authorized next work.

## 13. Handoff to next agent
**VERIFIED:** Handoff to ClientController implementation: YES, with F-REV-01 retained as a Minor coverage follow-up. Preserve service-to-repository delegation and service transaction boundaries. Human Tech Lead retains final acceptance authority.
