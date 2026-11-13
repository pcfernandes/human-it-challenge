# T6.2 — Acceptance Scenario Validation (Architect Proposal)

## 1. Task ID

**T6.2 — Acceptance scenario validation.**

## 2. Execution identifier and date

**Execution:** `T6.2-EXEC-001-ARCH-01`
**Date:** 2026-11-13.

## 3. Agent role and model

**Role:** Architect
**Model:** `opencode-go/gpt-6-luna`.

## 4. Authorized scope

**VERIFIED** — Read-only review of repository instructions, T6.2 scope, approved requirements, existing tests and available T6.1 records; produce an acceptance matrix and recommend the smallest evidence additions. This report is the only authorized write. No application code, tests, requirements, plans, configuration, or other agents' reports are to be changed; no tests or Maven suite are to be run; no commit or push is in scope.

## 5. Files inspected

**VERIFIED** — Reporting and task guidance:

- `.opencode/agents/architect.md` — role boundaries, report requirements, and shell prohibition.
- `agent-reports/README.md` — mandatory thirteen-section format, evidence markers, history convention.
- `implementation-plan.md` — T6.2 (lines 534–542), Phase 2 CRUD/integrity/concurrency/pagination criteria (T2.2–T2.8), and Phase 3 JWT criteria.
- `requirements.md` and `requirements-decisions.md` — API, uniqueness, transaction, ownership, concurrency, pagination, and security contracts.
- `README.md` — repository capability and API context.
- `AGENTS.md` search — **NOT AVAILABLE**; no file matching that instruction name was found in the repository.
- `agent-reports/T6.1/**` and `agent-reports/T6.2/**` were searched; neither task directory had report files before this execution. **NOT AVAILABLE** — no T6.1 report was present to inspect; the duplicate-tax-identifier concern is also independently checked against the current test sources below.

**RECONSTRUCTED** — The T6.2 evidence assessment is reconstructed from the current plan, requirements, and inspected test-source assertions because no T6.1 report artifact was available. No historical T6.1 verdict, test run, or conclusion is reconstructed.

**VERIFIED** — Tests read or searched for the matrix:

- `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java`
- `src/test/java/com/humanit/challenge/controller/DocumentControllerTest.java`
- `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`
- `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java`
- `src/test/java/com/humanit/challenge/security/SecurityConfigurationIntegrationTest.java`
- `src/test/java/com/humanit/challenge/security/SecurityProblemHandlersTest.java`
- `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`
- `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`
- `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`
- `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`
- `src/test/java/com/humanit/challenge/exception/CommitTimeOptimisticLockIntegrationTest.java`
- `src/test/java/com/humanit/challenge/exception/GlobalExceptionHandlerTest.java`
- Client and Document request DTO validation/binding tests under `src/test/java/com/humanit/challenge/dto/`.

## 6. Files changed

**VERIFIED** — Created only `agent-reports/T6.2/architect-proposal.md`, as authorized. No previous T6.2 Architect report existed, so no archive was required. No application, test, plan, requirements, configuration, Developer, Tester, Reviewer, or decision-log file was changed.

## 7. Commands executed

**VERIFIED** — None. No shell commands or Maven commands were executed. This is source-and-test assertion inspection only, not an independent test run; no execution result or test pass is claimed.

## 8. Evidence and observed results

**VERIFIED** — The matrix maps each T6.2 scenario to inspected test assertions. “Covered” means the inspected test source contains assertions for the behavior; it does not claim the test was executed or passed in this task.

| Required scenario | Existing test evidence | Evidence assessment / remaining gap |
|---|---|---|
| Request validation failures | `ClientControllerTest.invalidCreateReturnsBadRequestProblemDetails`; `ClientControllerTest.malformedClientUpdateVersionsReturnBadRequestProblemDetails`; `DocumentControllerTest.missingAndNegativeVersionInputsReturnBadRequest` and `requiredDocumentVersionHeadersRejectMissingAndMalformedValues`; Client/Document DTO validation and Jackson binding tests; `GlobalExceptionHandlerTest.invalidBodyReturns400ProblemDetails` | **VERIFIED — PARTIAL.** Client invalid-body HTTP behavior and Document version-input HTTP behavior are asserted; Document DTO field constraints are validated at DTO level. **NOT AVAILABLE** — no inspected HTTP test sends an invalid Document create/update body and asserts the resulting 400 Problem Details response. |
| Not found | `ClientControllerTest.missingClientReturnsNotFound` (mocked service); `DocumentControllerTest.missingClientDocumentListReturnsNotFoundProblemDetails` (mocked service); `ClientAggregateUpdateHttpIntegrationTest.putWithUnknownNestedDocumentIdReturns404ProblemDetails` and `clientDeletePreservesNotFoundBehavior`; `ClientServiceTest.missingClientThrowsNotFound`; `DocumentServiceTest.missingClientAndDocumentReturnNotFound` | **VERIFIED — COVERED.** HTTP 404 mapping and real integration/service not-found cases are represented. |
| Duplicate tax identifier | `DomainPersistenceTest.taxIdentifierIsGloballyUnique` asserts a database `DataIntegrityViolationException`; no tax-duplicate scenario was found in `ClientControllerTest`, `ClientAggregateUpdateHttpIntegrationTest`, or `ClientServiceTest` | **VERIFIED** — database uniqueness is tested. **NOT AVAILABLE** — no inspected client-create HTTP assertion proves duplicate `taxIdentifier` maps to 409 Problem Details. This independently substantiates the concern stated in tasking; the referenced T6.1 report itself is also **NOT AVAILABLE** in the workspace. |
| Duplicate document number | `DocumentServiceTest.duplicateDocumentNumberReturnsConflict`; `DomainPersistenceTest.documentNumberIsGloballyUniqueAcrossDifferentClients`; `ClientAggregateUpdateHttpIntegrationTest.postWithDuplicateNestedDocumentNumbersRollsBackClientAndDocuments` asserts HTTP 409 and no partial persistence | **VERIFIED — COVERED for the scenario.** An HTTP duplicate-number conflict is demonstrated through Client creation with nested Documents. The standalone Document POST route has a happy-path assertion in `DocumentControllerTest.postDocumentReturnsCreated`. **NOT AVAILABLE** — a direct duplicate-number conflict assertion for that standalone route. |
| Transaction rollback | `ClientAggregateUpdateHttpIntegrationTest.postWithDuplicateNestedDocumentNumbersRollsBackClientAndDocuments`; `staleNestedDocumentVersionRollsBackEarlierAggregateChanges`; `patchWithLaterStaleNestedDocumentRollsBackEarlierAggregateChanges`; `CommitTimeOptimisticLockIntegrationTest.commitTimeOptimisticLockFailureReturns409ProblemDetailsAndRollsBack`; `OptimisticConcurrencyIntegrationTest` checks competing writers and absence of partial writes | **VERIFIED — COVERED.** Both failed Client-with-Documents creation and stale aggregate/commit conflict rollback are represented. |
| Cascade deletion | `DomainPersistenceTest.deletingClientThroughJpaDeletesAssociatedDocuments`; `databaseOnDeleteCascadeDeletesDocumentsWhenClientIsDeletedOutsideJpa`; `ClientServiceTest.clientDeleteWithCurrentVersionRemovesClientAndDocuments`; `ClientAggregateUpdateHttpIntegrationTest.clientDeleteEnforcesVersionAndDoesNotDeleteOnInvalidOrStalePrecondition` asserts HTTP deletion and absence of both rows | **VERIFIED — COVERED.** Persistence and HTTP deletion evidence are present. |
| Identity preservation | `ClientServiceTest.clientPutUpdatesExistingCreatesNewAndPreservesOmittedDocuments`; `nestedPatchChangesSuppliedFieldsOnlyAndPreservesDocumentIdentity`; `DocumentServiceTest.putAndPatchUpdateEditableFieldsAndKeepOwner`; related persistence identity/association tests | **VERIFIED — COVERED.** Existing Document IDs and owners are asserted through service/persistence use cases. |
| Collection omission | `ClientRequestBindingTest.putWithOmittedDocumentsPropertyIsValidAndKeepsEmptyDefault`; `ClientPatchRequestValidationTest.omittedDocumentsPreserveExistingDocuments`; `ClientServiceTest.clientPutWithOmittedOrEmptyDocumentsPreservesDocumentIds`; `clientPatchWithOmittedOrEmptyDocumentsPreservesCollection` | **VERIFIED — COVERED.** Binding and service behavior both represent omitted collections preserving existing Documents. |
| Empty collection | `ClientRequestBindingTest.putWithEmptyDocumentsArrayIsValid`; `ClientPatchRequestValidationTest.emptyDocumentsPreserveExistingDocuments`; the parameterized PUT/PATCH service tests above include explicit-empty cases; `ClientRequestValidationTest.emptyDocumentsIsAccepted` covers POST | **VERIFIED — COVERED.** Explicit empty collection semantics are asserted separately from omission. |
| Ownership violations | `ClientAggregateUpdateHttpIntegrationTest.putWithDocumentOwnedByAnotherClientReturns409ProblemDetails`; `ClientServiceTest.crossClientNestedDocumentAssociationReturnsConflictWithoutTransfer`; `DocumentServiceTest.staleVersionsConflictAndNestedAccessDoesNotTransferOwnership` asserts service-level wrong-owner GET is 404; `DomainPersistenceTest.associationHelpersPreventOwnershipTransfer` | **VERIFIED — PARTIAL.** HTTP 409 for cross-owner aggregate association is asserted, and service-level wrong-owner access is asserted. **NOT AVAILABLE** — no inspected HTTP request asserts the distinct nested-resource-under-wrong-Client 404 using a real foreign Document. |
| JWT success/failure | `AuthControllerIntegrationTest.validCredentialsReturnSignedHs256JwtThatCanAccessProtectedEndpoint`; `invalidCredentialsReturnGenericUnauthorizedProblem`; `SecurityConfigurationIntegrationTest.rejectsJwtSignedWithDifferentSecretWithoutLeakingSensitiveValues` and `rejectsExpiredJwt` | **VERIFIED — COVERED.** Login success/signature and invalid-credential, invalid-signature, and expiry failures are represented. |
| Protected endpoints | `SecurityConfigurationIntegrationTest.protectedEndpointWithoutJwtReturnsSafeUnauthorizedProblem` checks unauthenticated Client and nested Document list routes; `AuthControllerIntegrationTest.authenticatedClientAndDocumentCrudPreservesPhaseTwoContracts` exercises authenticated Client/Document CRUD | **VERIFIED — COVERED.** Authenticated API behavior and unauthenticated protected-route rejection are represented. |
| Optimistic locking conflicts | `ClientControllerTest.versionConflictReturnsConflict` (HTTP/controller mapping with mocked service); `DocumentControllerTest.serviceNotFoundAndConflictErrorsRemainProblemDetails` (mocked conflict mapping); `ClientAggregateUpdateHttpIntegrationTest` stale nested update rollback and stale Client delete; `CommitTimeOptimisticLockIntegrationTest`; `OptimisticConcurrencyIntegrationTest`; Client/Document service and repository tests | **VERIFIED — COVERED for the required conflict scenario.** Actual HTTP 409 plus rollback is asserted for aggregate updates and delete; concurrency and stale-version service/persistence behavior are also represented. The commit-time test uses a test-only route and is not production-endpoint evidence by itself. |
| Pagination | `ClientControllerTest.listClients`, `emptyAndOutOfRangeClientPagesReturnEmptyPageEnvelope`, `invalidClientPaginationReturnsBadRequestProblemDetails`; corresponding `DocumentControllerTest` methods; `DomainPersistenceTest.documentRepositoryQueriesFilterByOwnerAndReturnDeterministicPages`; `NPlusOneQueryCountTest` empty-page assertions | **VERIFIED — COVERED.** HTTP page metadata, empty/out-of-range pages, invalid parameters, ordering and repository paging are represented. |
| Problem Details | `GlobalExceptionHandlerTest` validation, malformed-body, type mismatch, 404, 405, 500 and status-preservation assertions; Client/Document controller 400/404/409 assertions; aggregate integration 404/409; `SecurityProblemHandlersTest`; `SecurityConfigurationIntegrationTest` and `AuthControllerIntegrationTest` security errors | **VERIFIED — PARTIAL.** The common response format and several status mappings are represented. **NOT AVAILABLE** — duplicate-tax HTTP 409 and invalid Document-body HTTP 400 endpoint assertions (see rows above). |

## 9. Findings and severity

1. **F-01 — Major acceptance evidence gap: duplicate tax identifier HTTP 409 is unverified.** **VERIFIED** — the persistence test establishes global uniqueness only by asserting the database exception. **NOT AVAILABLE** — no inspected test sends a duplicate Client create request over HTTP and asserts `409 application/problem+json`; the T6.1 report itself was not found. The duplicate-tax concern is stated in the tasking and the test-source gap is independently confirmed. This is an evidence gap, not proof of a production defect.
2. **F-02 — Minor acceptance evidence gap: invalid Document request body HTTP mapping.** **VERIFIED** — Document DTO constraints and request-version failures have tests. **NOT AVAILABLE** — an invalid Document body assertion through the controller with 400 Problem Details.
3. **F-03 — Minor acceptance evidence gap: wrong-owner nested Document read HTTP 404.** **VERIFIED** — service-level wrong-owner access and HTTP cross-owner aggregate 409 are covered. **NOT AVAILABLE** — the distinct nested GET under a non-owning Client asserted over HTTP.
4. **F-04 — Informational traceability gap: direct Document POST duplicate-number HTTP assertion.** **VERIFIED** — duplicate-number conflict is asserted in service tests and by HTTP during nested Client creation. **NOT AVAILABLE** — only a direct duplicate-number assertion for the standalone Document POST route. This is not counted as a missing scenario, but can be added only if endpoint-specific traceability is required.
5. **F-05 — Verification limitation.** **VERIFIED** — no commands or tests were run, as instructed. Test execution/pass status is **PENDING VERIFICATION** for the independent Tester.

## 10. Decisions and reasoning

- **Smallest recommended additions:**
  1. Add one real HTTP integration test to `ClientAggregateUpdateHttpIntegrationTest`: create a Client, POST a second Client with the same tax identifier and distinct other values, assert HTTP 409, `application/problem+json`, Problem Details `status: 409` (and preferably `title: Conflict`), then clear/reload and assert the original Client remains and no second row exists. This is the explicit T6.1-flagged acceptance gap. If the assertion fails, first determine whether the failure is an actual defect; do not preemptively alter production code.
  2. In `DocumentControllerTest`, add one invalid Document create body assertion (e.g., blank/missing required `number` or missing `expirationDate`) for HTTP 400, Problem Details media type and `status: 400`. Reuse its existing MockMvc/exception-handler setup.
  3. Extend `ClientAggregateUpdateHttpIntegrationTest` with a GET of a real Document under a different Client's nested route and assert HTTP 404 Problem Details. The current ownership-conflict test already supplies both Clients and the foreign Document, so this can be a small additional request/assertion.
- **No duplication recommended:** existing service, persistence, binding, rollback, cascade, JWT, pagination, concurrency and Problem Details tests already cover the remaining scenario behaviors. Preserve them rather than adding parallel tests at another layer without a specific uncovered contract.
- **Document-number direct-route case:** no mandatory addition is proposed at this stage because the duplicate-number scenario already has a real HTTP 409 through nested Client creation plus service and database-constraint evidence. Add a standalone Document POST conflict assertion only if the Tech Lead requires endpoint-by-endpoint acceptance evidence.
- **Production changes:** none proposed without an observed failing acceptance test demonstrating a defect. This follows the scoped request to locate evidence gaps, not infer implementation defects from missing tests.
- **Trade-off:** these focused additions improve API contract traceability at the layer where status codes and Problem Details are observable, while avoiding a broad duplicate suite or full-suite execution.
- **Assumption:** acceptance is against the approved behavior in `implementation-plan.md` and `requirements-decisions.md`; no new API contract is introduced by this proposal.

## 11. Final status

**ARCHITECT REVIEW COMPLETE — PARTIAL / EVIDENCE GAPS IDENTIFIED.** **VERIFIED** — Existing source assertions cover most required scenarios. Duplicate tax identifier HTTP 409 remains a Major acceptance evidence gap; invalid Document-body HTTP 400 and wrong-owner nested GET HTTP 404 are smaller endpoint-level evidence gaps. This report does not claim tests passed, does not declare final acceptance, and does not authorize implementation. Human Tech Lead approval remains required before any proposed code/test changes.

## 12. Outstanding issues

- **PENDING VERIFICATION** — Independent Tester to execute the approved acceptance verification (not the full Maven suite unless separately authorized) and report actual outcomes.
- **PENDING VERIFICATION** — Add and execute the focused duplicate-tax HTTP 409 assertion; if it fails, provide evidence before considering production changes.
- **PENDING VERIFICATION** — Decide whether the two Minor HTTP evidence gaps (invalid Document body and wrong-owner nested GET) are included in the smallest accepted test patch.
- **NOT AVAILABLE** — T6.1 report artifacts were not found in `agent-reports/T6.1/`; historical T6.1 conclusions beyond the duplicate-tax concern stated in tasking cannot be corroborated from the workspace.
- No Tech Lead approval or final acceptance is recorded or implied by this proposal.

## 13. Handoff to next agent

- **To the human Tech Lead:** review F-01 through F-04 and approve or reject the focused test additions before implementation. No next implementation step is proposed before your decision.
- **To the Developer, after authorization:** prefer the existing `ClientAggregateUpdateHttpIntegrationTest` and `DocumentControllerTest`; add only approved test assertions and avoid production code changes unless a failing assertion demonstrates a defect.
- **To the Tester:** independently inspect requirements and implementation and verify before reading the Developer's conclusions, preserving the Tester independence rule. No test results are asserted in this Architect report.
- **To the Reviewer:** perform the independent audit before reading Developer/Tester reports or this proposal, preserving the Reviewer independence rule.
- **Final acceptance:** reserved exclusively for the human Tech Lead.
