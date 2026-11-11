# Architect Proposal — T2.6 Versioning and Optimistic Concurrency

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2): T2.6 — Concurrency control and version validation.

## 2. Execution identifier and date

- **VERIFIED (direct tasking/environment):** `T2.6-EXEC-001-ARCH`.
- **VERIFIED (environment):** 2026-11-11.
- **VERIFIED (direct tasking):** The Human Tech Lead states that T2.5 has been merged into `main` and T2.6 is the next planned implementation task. This report preserves accepted T2.5 aggregate-update semantics and does not infer any additional T2.5 requirements.

## 3. Agent role and model

- **VERIFIED:** Role — Architect, senior Java and Spring Boot software architect.
- **VERIFIED (runtime instructions):** Model — `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED (direct tasking):** Analyze approved T2.6 requirements against the current implementation; prepare this architecture proposal and handoff only.
- **VERIFIED (direct tasking):** No production code, tests, migrations, configuration, requirements, implementation plan, or existing decisions may be changed. No implementation authorization, Git operation, or self-approval is granted.
- **VERIFIED (`implementation-plan.md`, T2.6/T2.7):** T2.6 covers required expected versions, optimistic locking, parent Client force-increment for Document mutations, stale-version conflicts, conflict rollback, and equivalent concurrency rules. T2.7 separately covers transaction-wide atomicity/deletion rules and transaction boundaries. This proposal limits rollback analysis and tests to the conflict behavior explicitly required by T2.6; it does not expand into T2.7.
- **VERIFIED (`agent-reports/decisions.md`, D-10 through D-17; T2.5 reports):** Preserve accepted T2.1 persistence behavior, D-11 layer boundaries, D-13 DTO contracts, and T2.5 identity/collection-preserving update semantics. Do not reopen those decisions.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (Phase 2, especially T2.1–T2.7); `requirements-decisions.md` (Sections 2–8, 11, 15 and 20); and `agent-reports/decisions.md` (D-10 through D-17, including T2.5 scope and current approval-record context).
- **VERIFIED:** `agent-reports/T2.2/architect-proposal.md`; `agent-reports/T2.4/developer-report.md`; `agent-reports/T2.4/reviewer-report.md`; `agent-reports/T2.5/architect-proposal.md`; `agent-reports/T2.5/developer-report.md`; `agent-reports/T2.5/tester-report.md`; and `agent-reports/T2.5/reviewer-report.md`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/domain/Client.java`; `domain/Document.java`; `service/ClientService.java`; `service/DocumentService.java`; `controller/ClientController.java`; `controller/DocumentController.java`; `exception/GlobalExceptionHandler.java`; `repository/ClientRepository.java`; and `repository/DocumentRepository.java`.
- **VERIFIED:** Client and Document request/response DTOs under `src/main/java/com/humanit/challenge/dto/client/` and `dto/document/`; `mapper/ClientMapper.java`; `mapper/DocumentMapper.java`; and `src/main/resources/db/migration/V2__create_clients_and_documents.sql`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`; `service/ClientServiceTest.java`; `service/DocumentServiceTest.java`; `controller/ClientControllerTest.java`; `controller/DocumentControllerTest.java`; `controller/ClientAggregateUpdateHttpIntegrationTest.java`; `exception/GlobalExceptionHandlerTest.java`; and `exception/CommitTimeOptimisticLockIntegrationTest.java`.
- **NOT AVAILABLE:** No prior T2.6 Architect report or T2.6 report history was found in the searched `agent-reports/T2.6/` path.
- **NOT AVAILABLE:** No separate architecture-document directory/file matching the searched architecture-name patterns was found. Existing approved architecture guidance was taken from `implementation-plan.md`, `requirements-decisions.md`, D-11/D-13 and the prior task reports listed above.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.6/architect-proposal.md` — this report.
- **VERIFIED:** No prior primary T2.6 Architect report was found, so no history archive was required.
- **VERIFIED:** No application, test, migration, configuration, requirement, plan, decision-log, or other role-report file was changed.

## 7. Commands executed

- **VERIFIED:** None. No shell, Maven, or Git command was executed. Project artifacts were inspected with file and search tools only.

## 8. Evidence and observed results

### Approved contract

- **VERIFIED (`implementation-plan.md`, T2.6, and `requirements-decisions.md`, Sections 8 and 20):** Both Client and Document have JPA optimistic versions. Client POST requires no version; Document POST requires `X-Client-Version`. Client PUT/PATCH require expected Client `version` in the body. Document PUT/PATCH require expected Document `version` in the body and `X-Client-Version`. Client aggregate PUT/PATCH require the expected Client version and expected versions for each existing nested Document being updated; new nested Documents have no version.
- **VERIFIED (same approved requirements):** Client DELETE requires `X-Client-Version`; Document DELETE requires `X-Client-Version` and `X-Document-Version`. Missing, malformed or invalid required versions return HTTP 400; stale versions return HTTP 409 Problem Details. Each Document create, update or delete increments the owning Client version with JPA `OPTIMISTIC_FORCE_INCREMENT` in the same transaction. Conflicts roll back the entire transaction and cannot partially persist.

### Current implementation

- **VERIFIED (`Client.java:50-52`; `Document.java:43-45`; migration V2):** Both entities already declare `@Version Long version`, mapped to non-null BIGINT `version` columns. The schema is already present; no migration is indicated by the T2.6 version requirements.
- **VERIFIED (`ClientPutRequest.java`, `ClientPatchRequest.java`, `ClientDocumentPutRequest.java`, `ClientDocumentPatchRequest.java`, `DocumentPutRequest.java`, `DocumentPatchRequest.java`):** Client and Document update DTOs already carry their approved expected versions and use `@NotNull`/`@PositiveOrZero` where applicable. Existing nested-document DTO rules pair ID and version for existing Documents and omit both for new Documents. These contracts reflect T2.2/D-13 and do not need redesign for T2.6.
- **VERIFIED (`ClientService.java:70-107`, `128-132`, `145-198`):** Client PUT/PATCH compare the expected Client version with the loaded Client and compare each existing nested Document's expected version with its loaded entity version. Version mismatches are expressed as HTTP 409. Request DTO validation rejects missing/negative body versions at the HTTP boundary.
- **VERIFIED (`DocumentService.java:35-46`, `62-77`, `80-97`, `100-127`, `134-144`):** Document POST/PUT/PATCH/DELETE call `lockClient`; the service rejects missing or negative Client versions as HTTP 400, uses the repository's force-increment lock query, and compares the expected Client version. Document PUT/PATCH/DELETE also require a non-negative expected Document version and compare it with the loaded Document. This existing standalone Document path is substantially aligned with T2.6.
- **VERIFIED (`ClientRepository.java:20-23`):** `lockById` already uses `@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)`. `DomainPersistenceTest.lockByIdForcesClientVersionIncrementOnTransactionCompletion` verifies an increment after transaction completion.
- **VERIFIED (`ClientService.java:110-119`; `ClientController.java:60-64`):** Client DELETE currently accepts only the path ID and has no expected-version argument/check. The approved `X-Client-Version` precondition is absent from this endpoint.
- **VERIFIED (`ClientController.java:50-57`; `ClientPutRequest.java`; `ClientPatchRequest.java`):** Client PUT/PATCH bodies have expected Client versions, but ClientService's aggregate workflows do not invoke `ClientRepository.lockById` when nested Documents are created or updated.
- **VERIFIED (`ClientService.java:145-198`; `Client.java:45-52`):** The aggregate service mutates managed nested Documents and adds new ones while preserving T2.5 identity/collection semantics. `Client.documents` is the inverse (`mappedBy`) side. A Document row update does not itself make the Client row dirty; therefore JPA's Document `@Version` alone does not satisfy the approved parent Client version increment for aggregate Document mutations.
- **VERIFIED (`DocumentService.java`; `ClientService.java`; relevant controller signatures):** Service methods already use Spring `@Transactional`; Document standalone operations flush Document/Client repositories within these boundaries. No transaction-boundary redesign is proposed here.
- **VERIFIED (`GlobalExceptionHandler.java:142-166`):** `ResponseStatusException` preserves its status in RFC 9457 Problem Details; optimistic locking exceptions map to HTTP 409 Problem Details. The generic unexpected-exception handler sanitizes its response. The handler has no dedicated override for missing request headers; Spring MVC's framework handling is in play, but exact response format should be verified for required version headers.
- **VERIFIED (`ClientController.java:60-64`; `DocumentController.java:36-80`):** Document create/update/delete already bind their required Client version headers and Document DELETE binds the Document version header. Client DELETE does not. `Long` header conversion errors are covered by the handler's type-mismatch mapping path, but not all required-version HTTP cases have direct endpoint tests.

### Current test evidence and limitations

- **VERIFIED (`DomainPersistenceTest.java:247-285`):** Tests establish that both entity versions increment on dirty updates, and that stale JPA updates to either entity fail. A separate test covers parent force-increment. These are useful persistence-level tests, not full service-level concurrency proof.
- **VERIFIED (`ClientServiceTest.java`; `DocumentServiceTest.java`):** Tests cover Client stale body version, Document stale Client/Document versions, invalid missing/negative Document-service versions, and T2.5 aggregate behaviors. Existing tests do not establish that aggregate nested Document create/update advances the parent version or that a failed stale aggregate operation leaves earlier attempted changes unpersisted.
- **VERIFIED (`ClientControllerTest.java:107-113`):** Client DELETE currently tests no-content behavior without any version header, reflecting the implementation gap.
- **VERIFIED (`DocumentControllerTest.java:128-165`):** Some Document missing/negative-version HTTP cases are covered, but not the full required header matrix (including missing/malformed/negative header cases for every applicable endpoint and DELETE's Document header).
- **VERIFIED (`CommitTimeOptimisticLockIntegrationTest.java`):** Existing integration coverage verifies Problem Details 409 mapping and no Document persistence when an optimistic exception is raised at commit through a test controller. The exception is deliberately injected; this does not prove that two actual application transactions conflict through the current optimistic-lock paths.
- **PENDING VERIFICATION:** No tests or build were run in this Architect execution. Proposed behavior and coverage require authorized Developer/Tester verification.

## 9. Findings and severity

1. **MAJOR — Client DELETE does not enforce the approved expected Client version.** **VERIFIED** (`ClientController.java`, `ClientService.java`, T2.6 contract). Add the required header binding and compare the supplied version before deletion. JPA's delete-time `@Version` predicate only protects a race after loading; it does not enforce the caller's explicit stale-version precondition.
2. **MAJOR — aggregate Document changes do not force-increment the Client version.** **VERIFIED** (`ClientService.java`, `ClientRepository.java`, `Client.java`, T2.6 contract). Call the existing `OPTIMISTIC_FORCE_INCREMENT` repository operation within the same aggregate update transaction whenever the accepted operation creates or updates nested Documents. This is necessary even when Client scalar values are unchanged; a Document row's own version does not update its inverse parent row.
3. **MAJOR — aggregate lost-update/concurrency behavior is not established.** **RECONSTRUCTED (approved force-increment contract + observed gap):** If two Client aggregate requests use the same expected Client version to mutate distinct Documents, per-Document `@Version` checks alone may permit both because the Client row is not currently force-incremented. The parent force-increment must make the competing aggregate operation lose with HTTP 409, with its writes rolled back.
4. **MODERATE — required Client DELETE version errors and consistent HTTP shape lack coverage.** **VERIFIED** (controller/test inspection). Missing, malformed and negative required version headers must return 400, and stale values 409; endpoint-level tests should verify status and Problem Details media type/body where the approved contract requires it.
5. **MODERATE — conflict rollback is not sufficiently proven on real service paths.** **VERIFIED** (test inspection). Existing commit-time integration coverage injects a failure rather than producing one from two real versioned transactions; aggregate stale-child rollback is not directly asserted after persistence reload.
6. **MODERATE — T2.6's new parent-force increment could make Client responses expose an out-of-date version if response mapping occurs before the force-increment value is visible.** **RECONSTRUCTED (ClientService maps a ClientResponse before the transactional proxy commits; `DomainPersistenceTest` verifies the increment after transaction completion).** Confirm through an integration test that a nested-only mutation returns a Client `version` matching the persisted post-transaction value. Do not assume an exact increment amount or hand-adjust the version without provider evidence.
7. **LOW / OPEN CONTRACT INTERPRETATION — Client POST with nested Documents.** **VERIFIED (contract wording):** The requirement says every Document create increments the parent Client via `OPTIMISTIC_FORCE_INCREMENT`, while Client POST with nested Documents requires no input version and creates a new parent in the same transaction. The plan does not explicitly exempt initial nested Documents from the word “every.” Resolve whether the force-increment rule applies to Documents created as part of Client POST, and what version is expected in that create response; do not add a new expected-version input to POST.

## 10. Decisions and reasoning

### Approved requirements-to-code gap summary

| T2.6 requirement | Existing state | Minimal proposal |
|---|---|---|
| Both entities use JPA `@Version` | **VERIFIED:** Present and mapped to existing non-null DB version columns. | No entity or migration change unless tests reveal a concrete persistence defect. |
| Client PUT/PATCH expected Client version | **VERIFIED:** Body DTO validation and service comparison exist. | Preserve; add stale/concurrent regression assertions. |
| Nested existing Document expected version | **VERIFIED:** DTO and service checks exist. | Preserve; test 409 and rollback in aggregate path. |
| Document endpoint expected Client + Document versions | **VERIFIED:** Required header/body bindings and service comparisons exist. | Preserve unless targeted tests expose a gap. |
| Client DELETE expected Client version | **VERIFIED:** Missing from controller and service contract. | Add required header to controller and expected-version check to service. |
| Document mutation force-increments Client | **VERIFIED:** Present for standalone Document endpoint through `lockById`; missing from Client aggregate nested create/update. | Reuse existing repository force-increment operation from the aggregate service in the same transaction. |
| Stale versions map to 409 Problem Details | **VERIFIED:** ResponseStatus and optimistic-lock exception paths exist. | Preserve mapping; verify actual transaction conflict and rollback over HTTP/service integration. |
| Invalid/missing/malformed required versions map to 400 | **VERIFIED:** DTO validation, service validation for Document versions, type-mismatch handling exist; Client DELETE has no version input. | Add Client DELETE precondition validation and complete the endpoint input matrix; explicitly verify missing-header response shape. |

### Proposed production scope by file

- **RECONSTRUCTED (observed package structure and requirements): `src/main/java/com/humanit/challenge/controller/ClientController.java`.** Add required `X-Client-Version` binding only to DELETE; keep route, response status, and all other API contracts unchanged.
- **RECONSTRUCTED: `src/main/java/com/humanit/challenge/service/ClientService.java`.** Accept and validate the expected Client version in `delete`; return 400 for negative/invalid values reaching the service and 409 when the persisted value is stale. Reuse the existing `ClientRepository.lockById` force-increment mechanism for aggregate PUT/PATCH operations that create or update nested Documents, within their existing transactions. Preserve current T2.5 merge/identity/ownership behavior.
- **VERIFIED / no planned change:** `ClientRepository.java` already provides `OPTIMISTIC_FORCE_INCREMENT`; `DocumentService.java` already applies it for standalone Document create/update/delete; Client/Document entity mappings and V2 migration already supply JPA versions. Do not edit these absent a concrete defect demonstrated by testing.
- **VERIFIED / no planned DTO or handler contract change:** Client/Document DTOs already contain the approved expected-version fields. `GlobalExceptionHandler.java` already maps explicit stale-version conflicts and optimistic-lock exceptions to RFC 9457 409 and type mismatches to 400. Only adjust it if an endpoint-level test shows required missing headers fail the approved 400 Problem Details behavior.
- **RECONSTRUCTED (test layout):** Extend existing persistence-backed service/repository tests and controller/integration tests. A dedicated T2.6 concurrency integration test is justified only if existing classes cannot prove the required independent-transaction behavior clearly.

### Required regression tests

1. **VERIFIED (approved contract) / recommended:** Client PUT and PATCH accept current non-negative version, reject a stale version with 409 Problem Details, and reject missing/negative body versions with 400 before service mutation.
2. **VERIFIED / recommended:** Document POST, PUT, PATCH and DELETE exercise current/stale Client version; PUT/PATCH/DELETE exercise current/stale Document version; missing, malformed and negative required headers/body versions return 400. Assert status and `application/problem+json`; assert `status` in Problem Details. Preserve current not-found/ownership status contracts.
3. **VERIFIED / recommended:** Client DELETE with current `X-Client-Version` succeeds; stale value returns 409; missing, malformed and negative values return 400. Verify no deletion on invalid/stale requests and retain the existing 204 success response.
4. **VERIFIED (approved aggregate contract) / recommended:** Nested Document create and update through Client PUT and PATCH advance the Client version and the affected Document's own version as required; omitted/empty `documents` remains preserving and does not act as a Document mutation. Assert versions after clearing/reloading. Do not assert an exact increment count unless provider behavior is an approved contract.
5. **VERIFIED / recommended:** Standalone Document create/update/delete each advance the parent Client version; stale Client version prevents persistence. Existing `lockById` persistence test should be kept, not replaced.
6. **VERIFIED / recommended:** Aggregate operation with one valid nested Document change followed by a stale nested Document version returns 409; clear/reload and prove the earlier Client scalar and nested Document changes were not persisted. This is conflict-specific rollback required by T2.6, not a general T2.7 rollback matrix.
7. **VERIFIED / recommended:** Exercise actual separate persistence transactions where two writers start from the same Client version and mutate different Documents under that Client; exactly one operation should complete, the other should conflict, and no partial write from the losing operation remains. Add equivalent same-Client scalar and same-Document stale-writer coverage if existing tests do not adequately establish those JPA races.
8. **PENDING VERIFICATION / recommended:** Test a nested-only aggregate mutation's returned `ClientResponse.version` against the version reloaded after the HTTP transaction completes. For Client POST containing Documents, test the Tech Lead's resolution to open decision 7 without introducing a version input.
9. **VERIFIED / recommended:** Keep successful Client POST-without-version and existing T2.5 collection-preservation/identity tests green as regression controls.

### Error handling, database behavior and trade-offs

- **VERIFIED (JPA mappings/tests):** `@Version` guards updates/deletes of each individual row. A child-row version does not, by itself, serialize sibling Document changes or advance the owning Client row. `OPTIMISTIC_FORCE_INCREMENT` on the Client is the approved and already-used mechanism to make Document-level work participate in aggregate concurrency.
- **RECONSTRUCTED (Spring transaction behavior, service annotations, approved contract):** A stale version detected before mutation is rejected as a 409 response status exception. A version race detected at flush/commit must escape or be translated to an optimistic-lock exception, mark the transaction for rollback, and reach the existing 409 handler. Do not catch-and-continue after a persistence conflict.
- **VERIFIED:** No schema migration, dependency, new lock strategy, API route, version header redesign, mapper responsibility change, or entity-level service behavior is needed for the identified gaps.
- **Alternative rejected — manual `Client.version` adjustment:** **RECONSTRUCTED (JPA `@Version` ownership):** Manually incrementing a version field would bypass provider concurrency semantics and risk double increments or incorrect response values. Use the existing JPA lock and prove returned-version semantics through integration tests instead.
- **Alternative rejected — pessimistic/database lock or ETag redesign:** **VERIFIED (approved T2.6 contract):** The requirement explicitly selects JPA optimistic locking and request DTO/header versions; new locking infrastructure or different HTTP preconditions would broaden the approved contract without need.

### Open decisions for Human Tech Lead

1. **PENDING TECH LEAD DECISION:** Does “every Document create” include nested Documents created as part of `POST /clients`, where Client itself is new and the contract explicitly requires no supplied version? If yes, clarify the intended initialization/force-increment and create-response version semantics. If no, record/confirm that the force-increment rule is for mutations against an already existing Client. Do not infer an exclusion or add a POST version input.
2. **PENDING TECH LEAD DECISION ONLY IF A CONTRACT CHANGE IS CONTEMPLATED:** Existing Client response DTO includes `version`, but the requirements do not define an exact numeric increment or explicitly state how the create/update response version is exposed after a commit-time force increment. First test the current provider behavior. If it cannot return a current version without a contract change or unsafe manual adjustment, obtain a decision before changing the response contract.
3. **VERIFIED:** No decision is needed to change the Client DELETE header requirement, expected-version behavior, stale-version status, or parent force-increment for mutations of an existing Client; these are explicit approved T2.6 requirements.

### Explicit out-of-scope items

- **VERIFIED (`implementation-plan.md`, T2.7):** No redesign of service transaction boundaries or transaction-wide rollback rules beyond rollback caused by a T2.6 version conflict.
- **VERIFIED (D-11, D-13, T2.5 reports):** No changes to aggregate collection-preserving semantics, Document identity/ownership, DTO tri-state rules, mapper layering, endpoints other than required Client DELETE header binding, or response shape.
- **VERIFIED (scope):** No database migration, dependency/infrastructure addition, pessimistic locking, new API contracts, unrelated validation/error taxonomy, OpenAPI work, security work, pagination work, Git operation, or acceptance/approval claim.

### Recommended implementation sequence (proposal, not authorization)

1. **PENDING TECH LEAD DECISION:** Resolve the POST-with-nested-Documents interpretation and record any needed clarification through the normal Tech Lead process; do not edit the append-only decision log without explicit authorization.
2. **RECONSTRUCTED / proposed:** Implement Client DELETE's expected-version input/check using the existing service/repository pattern. Add endpoint and service regression tests for 400/409/success and no-delete-on-conflict.
3. **RECONSTRUCTED / proposed:** Apply the existing Client force-increment repository operation to nested Document create/update work in Client PUT/PATCH. Avoid altering the accepted aggregate merge algorithm; test parent and child versions and response version.
4. **RECONSTRUCTED / proposed:** Add focused integration coverage for actual competing transactions, conflict rollback, HTTP Problem Details and the approved request-version matrix. Preserve existing repository tests and T2.5 tests.
5. **PENDING VERIFICATION:** Run focused tests and the authorized relevant suite; Tester verifies independently before reading Developer conclusions, then Reviewer audits independently before reading Developer/Tester reports or this proposal. Human Tech Lead alone decides final acceptance.

### Acceptance criteria

- **VERIFIED (approved T2.6 contract):** Client and Document versions are required in precisely the approved DTO/header locations; POST Client remains version-free.
- **VERIFIED (approved T2.6 contract):** Client DELETE requires `X-Client-Version`; Document DELETE requires both approved version headers; invalid required versions return 400 and stale versions return 409 Problem Details.
- **VERIFIED (approved T2.6 contract):** Every Document create/update/delete against an existing Client advances that Client through `OPTIMISTIC_FORCE_INCREMENT` in the same transaction, including nested aggregate updates; each relevant Document's own `@Version` is enforced.
- **VERIFIED (approved T2.6 contract):** Concurrent stale writers, including different Documents under one Client, cannot silently overwrite or bypass aggregate concurrency; conflict results in HTTP 409 and no partial persistence.
- **VERIFIED (D-11/D-13/T2.5):** Existing collection preservation, identity preservation, ownership, validation and error contracts remain unchanged.
- **PENDING VERIFICATION:** The response-version behavior and POST-with-nested-Documents policy are demonstrated/handled per the Tech Lead's resolution; no assumption about exact version increments is introduced.

## 11. Final status

**PROPOSAL COMPLETE — READY FOR HUMAN TECH LEAD REVIEW: YES.** The current implementation has identified T2.6 gaps and a minimal proposed scope. This is analysis only; implementation is not authorized by this report. No tests or builds were run, and no acceptance is claimed.

## 12. Outstanding issues

1. **PENDING TECH LEAD DECISION:** Clarify whether Client POST with nested Documents is covered by the unqualified “every Document create” parent force-increment requirement.
2. **PENDING VERIFICATION:** Confirm after implementation that a nested-only aggregate update response carries the persisted current Client version; avoid guessing exact provider increments.
3. **VERIFIED — implementation gaps:** Client DELETE lacks its required expected Client version; nested aggregate Document mutations lack parent `OPTIMISTIC_FORCE_INCREMENT`.
4. **PENDING VERIFICATION:** Prove actual concurrent aggregate writer conflict and full rollback of that conflict, as distinct from the existing injected commit-time exception test.
5. **VERIFIED:** No T2.6 tests/builds were run in this Architect execution. No implementation authorization or Human Tech Lead acceptance is claimed.
6. **VERIFIED (scope):** T2.7 general transaction-wide atomicity/deletion work remains out of scope; only T2.6 version-conflict rollback is included.

## 13. Handoff to next agent

- **VERIFIED:** Human Tech Lead: review this proposal and resolve open decision 1 before authorizing implementation; a separately explicit implementation authorization is required.
- **PENDING VERIFICATION:** After authorization, Developer should implement only Client DELETE expected-version validation and aggregate parent force-increment changes, reuse existing repository/exception infrastructure, preserve T2.5 semantics, and add the focused tests/acceptance evidence listed above.
- **VERIFIED (reporting workflow):** Tester must independently verify before reading Developer conclusions. Reviewer must audit independently before reading Developer/Tester reports or this Architect proposal. Human Tech Lead retains final acceptance authority.
- **VERIFIED:** Stop after this proposal; no implementation, test execution, commit, merge, push, decision-log append, or self-approval was performed.
