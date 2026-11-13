# Tester Report — T6.2 Acceptance Scenario Validation

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, T6.2): `T6.2 — Acceptance scenario validation`.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T6.2-TST-001`, 2026-11-13.
- **VERIFIED** (`agent-reports/T6.2/` search before writing): No prior Tester primary report or Tester history archive was present; no archive was required.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Tester; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Independently inspect repository instructions, T6.2 plan, requirements, implementation and the three new HTTP acceptance tests; execute affected classes and focused existing scenario tests; report counts and a concise acceptance matrix.
- **VERIFIED** (direct tasking): Reuse tests, do not add or modify application/test code, do not run the full suite unless necessary, and do not commit or push.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): Use the 13-section report format and preserve prior executions. No previous Tester report existed.

## 5. Files inspected

- **VERIFIED**: `README.md`, `implementation-plan.md` (T6.2), `requirements.md`, `requirements-decisions.md`, and `agent-reports/README.md`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`, `DocumentControllerTest.java`, and `ClientControllerTest.java`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`, `DocumentServiceTest.java`, and `OptimisticConcurrencyIntegrationTest.java`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java`, `SecurityConfigurationIntegrationTest.java`, and `SecurityProblemHandlersTest.java`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/exception/CommitTimeOptimisticLockIntegrationTest.java` and `GlobalExceptionHandlerTest.java`; Client/Document DTO validation tests under `src/test/java/com/humanit/challenge/dto/`.
- **VERIFIED** (after test execution): `agent-reports/T6.2/developer-report.md` and `architect-proposal.md`, for comparison only.
- **VERIFIED**: Maven output at `C:\Users\pedro\.local\share\opencode\tool-output\tool_1d591d1c7001hD42N9k1740FTx` and generated Surefire reports under `target/surefire-reports/`.

## 6. Files changed

- **VERIFIED**: Created only `agent-reports/T6.2/tester-report.md` in this execution.
- **VERIFIED** (`git status --short` before and after test execution): The two controller test files were already modified before this execution; they were not modified by this Tester execution. No application or test source was edited.
- **VERIFIED**: No previous Tester report was replaced; no history archive was required. No commit or push was performed.

## 7. Commands executed

- **VERIFIED**: `git status --short` and `git status --short; git diff --stat` — checked initial/final worktree state; existing test changes remained scoped to the two controller tests.
- **VERIFIED**: `.\mvnw.cmd "-Dtest=ClientAggregateUpdateHttpIntegrationTest,DocumentControllerTest,ClientControllerTest,ClientServiceTest,DocumentServiceTest,DomainPersistenceTest,AuthControllerIntegrationTest,SecurityConfigurationIntegrationTest,OptimisticConcurrencyIntegrationTest,CommitTimeOptimisticLockIntegrationTest,GlobalExceptionHandlerTest,DocumentRequestValidationTest,ClientRequestValidationTest" test` — focused execution of the 13 named test classes.
- **VERIFIED**: `.\mvnw.cmd "-Dtest=SecurityProblemHandlersTest" test` — focused execution of the security Problem Details handler test.
- **VERIFIED**: `git diff --stat` — confirmed only the pre-existing two test-file diffs; no test or production source was changed by this execution.
- **VERIFIED**: No full-suite command, commit, or push was run.

## 8. Evidence and observed results

- **VERIFIED** (Maven output and Surefire reports): First focused execution ran **164 tests**, with **0 failures, 0 errors, 0 skipped**; Maven reported `BUILD SUCCESS`.
- **VERIFIED** (Maven output): Second focused execution ran **1 test**, with **0 failures, 0 errors, 0 skipped**; Maven reported `BUILD SUCCESS`. Combined actual execution count: **165 tests, 0 failures, 0 errors, 0 skipped** across two targeted Maven invocations.
- **VERIFIED** (first Maven output): Counts by selected class: `ClientAggregateUpdateHttpIntegrationTest` 11; `DocumentControllerTest` 13; `ClientControllerTest` 11; `ClientServiceTest` 23; `DocumentServiceTest` 13; `DomainPersistenceTest` 19; `AuthControllerIntegrationTest` 3; `SecurityConfigurationIntegrationTest` 6; `OptimisticConcurrencyIntegrationTest` 3; `CommitTimeOptimisticLockIntegrationTest` 1; `GlobalExceptionHandlerTest` 8; `ClientRequestValidationTest` 33; and `DocumentRequestValidationTest` 20. The separate `SecurityProblemHandlersTest` run executed 1.
- **VERIFIED** (new `ClientAggregateUpdateHttpIntegrationTest` assertions and successful execution): Duplicate Client `taxIdentifier` POST returns `409 application/problem+json` Problem Details; after clearing persistence context the existing Client and original Document remain and the attempted nested Document is absent.
- **VERIFIED** (new `DocumentControllerTest` assertion and successful execution): Invalid Document POST body returns `400 application/problem+json` Problem Details, and the mocked `DocumentService` has no interactions.
- **VERIFIED** (new `ClientAggregateUpdateHttpIntegrationTest` assertion and successful execution): GET of an existing Document through a different, non-owning Client route returns `404 application/problem+json` Problem Details, including the expected detail and request instance.

### Acceptance matrix

| Required T6.2 scenario | Executed evidence | Result |
|---|---|---|
| Request validation failures | `ClientControllerTest`, `DocumentControllerTest`, `ClientRequestValidationTest`, `DocumentRequestValidationTest` | **VERIFIED — PASS**; Client and Document invalid inputs covered; invalid Document POST HTTP response is 400 Problem Details. |
| Not found | `ClientControllerTest`, `DocumentControllerTest`, `ClientAggregateUpdateHttpIntegrationTest`, `ClientServiceTest`, `DocumentServiceTest` | **VERIFIED — PASS**; both Client/Document not-found paths represented; wrong-owner nested Document GET returns 404 Problem Details. |
| Duplicate tax identifier | `ClientAggregateUpdateHttpIntegrationTest.postWithDuplicateClientTaxIdentifierReturns409WithoutPartialPersistence`; `DomainPersistenceTest.taxIdentifierIsGloballyUnique` | **VERIFIED — PASS**; endpoint returns 409 Problem Details and persisted state has no partial write. |
| Duplicate Document number | `ClientAggregateUpdateHttpIntegrationTest.postWithDuplicateNestedDocumentNumbersRollsBackClientAndDocuments`; `DocumentServiceTest.duplicateDocumentNumberReturnsConflict`; `DomainPersistenceTest.documentNumberIsGloballyUniqueAcrossDifferentClients` | **VERIFIED — PASS**; duplicate-number conflict and aggregate rollback are asserted. |
| Transaction rollback | `ClientAggregateUpdateHttpIntegrationTest` duplicate-create and stale nested PUT/PATCH rollback tests; `CommitTimeOptimisticLockIntegrationTest`; `OptimisticConcurrencyIntegrationTest` | **VERIFIED — PASS**; database state and competing-write outcomes assert no partial persistence. |
| Cascade deletion | `ClientServiceTest.clientDeleteWithCurrentVersionRemovesClientAndDocuments`; `DomainPersistenceTest` JPA and database cascade tests; aggregate HTTP delete test | **VERIFIED — PASS**; Client and owned Documents are removed together. |
| Document identity preservation | `ClientServiceTest` aggregate PUT/PATCH identity tests; `DocumentServiceTest.putAndPatchUpdateEditableFieldsAndKeepOwner` | **VERIFIED — PASS**; updates preserve existing Document IDs and owner. |
| Omitted/empty collections | Parameterized PUT/PATCH tests in `ClientServiceTest`; binding/validation tests | **VERIFIED — PASS**; omitted and empty collections preserve existing Documents. |
| Ownership violations | New wrong-owner nested GET HTTP test; `ClientAggregateUpdateHttpIntegrationTest.putWithDocumentOwnedByAnotherClientReturns409ProblemDetails`; service and persistence ownership tests | **VERIFIED — PASS**; nested read is 404 and attempted aggregate transfer is 409 without transfer. |
| JWT success/failure | `AuthControllerIntegrationTest`; `SecurityConfigurationIntegrationTest` | **VERIFIED — PASS**; successful signed JWT/login and bad credentials, wrong-signature, and expired-token rejection are exercised. |
| Protected endpoints | `SecurityConfigurationIntegrationTest.protectedEndpointWithoutJwtReturnsSafeUnauthorizedProblem`; authenticated CRUD in `AuthControllerIntegrationTest` | **VERIFIED — PASS**; unauthenticated access is 401 Problem Details; valid JWT grants access. |
| Optimistic locking | `OptimisticConcurrencyIntegrationTest`, `CommitTimeOptimisticLockIntegrationTest`, service, repository and HTTP tests | **VERIFIED — PASS**; stale/concurrent conflicts and rollback are asserted, including HTTP 409 Problem Details. |
| Pagination | `ClientControllerTest`, `DocumentControllerTest`, `ClientServiceTest`, `DocumentServiceTest`, `DomainPersistenceTest` | **VERIFIED — PASS**; metadata, ordering, empty/out-of-range pages and invalid limits are covered. |
| Problem Details | Controller/integration tests, `GlobalExceptionHandlerTest`, `SecurityProblemHandlersTest`, auth/security tests | **VERIFIED — PASS**; observed 400, 401, 404 and 409 responses use the required Problem Details media type and relevant assertions. |

- **VERIFIED** (Maven output): The first invocation logged a 401 warning retrieving metadata from the private Flyway community repository; compilation/tests completed and Maven returned `BUILD SUCCESS`.
- **NOT AVAILABLE**: Full-suite result for this execution; full suite was not run, consistent with the request.
- **NOT AVAILABLE**: Reviewer audit and human Tech Lead final acceptance for this execution.
- **VERIFIED — process deviation**: An initial broad `grep` for `T6.2` returned short excerpts from both Developer and Architect reports before this Tester execution completed verification. I did not open the reports in full until after the test runs, but exposure to these excerpts violated the strict requirement not to read agent conclusions before independent verification. Direct requirements/test inspection and the focused executions were still performed and recorded above; strict process independence cannot be claimed without qualification.

## 9. Findings and severity

1. **T62-TST-01 — Major process finding: early report-excerpt exposure.** **VERIFIED** (tool output/history above): A broad search surfaced partial Developer and Architect report text before verification completed. Full report review occurred only after tests, but the mandatory no-conclusions-before-verification order was breached. Test outcomes are directly evidenced; this report does not claim an unqualified independent process.
2. **No acceptance behavior failure observed — Info.** **VERIFIED** (165 focused tests): All executed T6.2 scenario checks passed; no production defect was demonstrated. This is not a full-suite claim.
3. **Repository metadata warning — Info.** **VERIFIED** (Maven log): Private repository metadata returned 401 during the first focused run, without blocking compilation or tests.

## 10. Decisions and reasoning

- **VERIFIED**: Executed the three requested HTTP tests within their existing test classes, then selected existing classes that cover the remaining T6.2 scenarios; no tests were added to this execution.
- **VERIFIED**: Did not run the full suite because the selected focused executions covered the requested scenario matrix and passed.
- **VERIFIED** (comparison with `developer-report.md`): The Developer reports 11 aggregate integration tests and 13 Document controller tests passing (24 combined); this matches the class counts and successful execution in this run. The Developer's account of an earlier invalid-title assertion mismatch followed by a corrected final test is not contradicted by this run; the current test expects `Validation failed` and passes.
- **VERIFIED** (comparison with `architect-proposal.md`): The Architect's three HTTP evidence gaps (duplicate tax ID 409, invalid Document body 400, wrong-owner nested GET 404) were marked as pre-implementation gaps; the current test diff contains those three cases, and their focused execution passed. The proposal's status is historical and should not be read as the current scenario status.
- **VERIFIED**: No behavior/result discrepancy was found between my executed test evidence and the Developer's final focused-test result. The process deviation above remains a discrepancy against the strict required execution sequence, not a test-result discrepancy.
- **VERIFIED**: No application or test source change was justified or authorized.

## 11. Final status

**PARTIAL — focused acceptance scenarios PASS; independence-process qualification required.** **VERIFIED**: The executed 165 tests passed, including all three new HTTP acceptance tests and focused evidence for the remaining T6.2 scenarios. **VERIFIED**: Early grep output exposed report excerpts before verification finished, so strict independent-process compliance cannot be claimed. No final acceptance is claimed; it is reserved for the human Tech Lead.

## 12. Outstanding issues

- **VERIFIED**: Strict independence sequence was breached by early excerpt exposure; the Reviewer and human Tech Lead should consider this qualification when assessing this report.
- **NOT AVAILABLE / PENDING VERIFICATION**: Full-suite status was not established; run only if separately considered necessary/authorized.
- **PENDING VERIFICATION**: Independent Reviewer audit and human Tech Lead final acceptance remain outstanding; no approval is recorded or implied.
- **VERIFIED**: No production or test source edits, commit or push were made by this execution.

## 13. Handoff to next agent

- **VERIFIED**: The acceptance matrix and actual focused test counts are recorded above; all executed checks passed.
- **PENDING VERIFICATION**: Reviewer should audit test assertions and account for the process deviation before forming conclusions.
- **PENDING VERIFICATION**: Human Tech Lead alone decides final T6.2 acceptance.
