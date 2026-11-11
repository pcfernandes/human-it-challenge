# Architect Proposal — T2.7 Transactional Atomicity and Rollback

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2, T2.7): T2.7 — Transactional atomicity and deletion rules.

## 2. Execution identifier and date

- **VERIFIED (direct tasking/environment):** `T2.7-EXEC-001-ARCH`.
- **VERIFIED (environment):** 2026-11-11.

## 3. Agent role and model

- **VERIFIED:** Architect role.
- **VERIFIED (runtime instructions):** `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED (direct tasking):** Read the requested governance, requirements, service, and integration-test artifacts; propose the smallest implementation/test scope for T2.7; stop for Human Tech Lead review. No implementation is authorized by this tasking.
- **VERIFIED (`implementation-plan.md`, Sections 2 and 6):** The proposal must preserve approved Client/Document API behavior, service-layer transaction ownership, T2.5 aggregate identity/ownership semantics, and T2.6 optimistic-version/conflict behavior.
- **VERIFIED (direct tasking):** Do not modify application code, tests, migrations, configuration, requirements, plans, or decisions; do not commit, push, merge, or switch branches.
- **NOT AVAILABLE:** Git branch/status could not be inspected. The available file/search tools do not expose Git status, and shell commands are prohibited by the Architect role instructions; no shell/Git command was executed.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (Phase 2, T2.1–T2.7); `requirements-decisions.md` (Sections 2, 4–8, 15 and 20); `agent-reports/decisions.md` (D-10–D-17, including D-11/D-13 and T2.5/T2.6 scope records).
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`; `src/main/java/com/humanit/challenge/service/DocumentService.java`; `src/main/java/com/humanit/challenge/domain/Client.java`; `src/main/java/com/humanit/challenge/domain/Document.java`; `src/main/java/com/humanit/challenge/repository/ClientRepository.java`; `src/main/java/com/humanit/challenge/controller/ClientController.java`; `src/main/java/com/humanit/challenge/controller/DocumentController.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`; `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`; `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`; `src/test/java/com/humanit/challenge/controller/DocumentControllerTest.java`; `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`; `src/test/java/com/humanit/challenge/exception/CommitTimeOptimisticLockIntegrationTest.java`; `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`.
- **VERIFIED:** T2.5 Architect proposal and T2.6 Architect, Tester, and Reviewer reports were read to preserve the prior task contracts and assess the T2.6 interaction. The T2.6 Developer report was not inspected in this execution.
- **VERIFIED:** An existing T2.7 primary Architect report was found and preserved unchanged at `agent-reports/T2.7/history/architect-001.md` before this primary report was revised.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.7/architect-proposal.md` — revised primary Architect report for this task.
- **VERIFIED:** `agent-reports/T2.7/history/architect-001.md` — unchanged archive of the primary report that existed before this revision.
- **VERIFIED:** No application/test/configuration/requirements/plan/decision file or another role's report was changed.

## 7. Commands executed

- **VERIFIED:** None. No shell, Maven, or Git command was executed. File and glob/search tools were used for inspection and report creation.
- **NOT AVAILABLE:** Read-only Git branch/status evidence is not available for this execution because the permitted tools do not provide Git status and shell execution is prohibited.

## 8. Evidence and observed results

### Approved T2.7 contract

- **VERIFIED (`implementation-plan.md`, T2.7; `requirements-decisions.md`, Sections 5, 7, 8, and 20):** Service-layer transactions must cover Client creation with Documents, Client aggregate updates, Client deletion with associated Documents, individual Document modifications, and Document deletion. Any failed operation must roll back the complete transaction. Client DELETE requires `X-Client-Version`; Document DELETE requires `X-Client-Version` and `X-Document-Version`; invalid/missing versions return 400 and stale versions return 409. Deleting a Document removes only that Document.
- **VERIFIED (`implementation-plan.md`, T2.5 and T2.6; D-11/D-13 and approved requirements):** Aggregate updates preserve existing Document identities and ownership, omitted/empty collections preserve Documents, and deletion is explicit rather than implied by PUT/PATCH. Existing optimistic checks, parent Client `OPTIMISTIC_FORCE_INCREMENT`, conflict status, and rollback semantics must remain within the same service transaction.

### Existing service boundaries and behavior

- **VERIFIED (`ClientService.java`):** Mutating entry points `create`, `update`, `patch`, and `delete` are annotated `@Transactional`; retrieval/list operations are `@Transactional(readOnly = true)`. Client creation maps the aggregate then persists/flushed it with `saveAndFlush`; aggregate PUT/PATCH mutate the managed Client/nested Documents and flush before returning; Client deletion deletes and flushes the Client. Current T2.6 aggregate code requests the existing force-increment lock when nested Documents are actually being changed.
- **VERIFIED (`DocumentService.java`):** Mutating entry points `create`, `update`, `patch`, and `delete` are annotated `@Transactional`; retrieval/list are read-only. Document mutation/deletion locks and checks the Client version, checks the Document version where applicable, and flushes the relevant repository/repositories within that transaction. The existing `ClientRepository.lockById` uses `OPTIMISTIC_FORCE_INCREMENT`.
- **VERIFIED (`Client.java`, `Document.java`, `DomainPersistenceTest.java`):** Client deletion has JPA REMOVE cascade and database `ON DELETE CASCADE` evidence; Document ownership is a required, non-updatable Client relationship. The service/domain design does not permit transfer. `Client.removeDocument` is documented as in-memory collection removal only; a Document DELETE must explicitly delete the row and must not be conflated with aggregate collection merging.
- **VERIFIED (`DocumentService.java` compared with `Client.java`):** Standalone Document deletion explicitly calls `documentRepository.delete(document)` and flushes, but does not call `client.removeDocument(document)`. The entity helper's documented contract says service-layer deletion pairs in-memory collection removal with explicit row deletion in the same transaction. This is an in-memory relationship-consistency gap; the existing database delete still explicitly removes only the target row.
- **RECONSTRUCTED (Spring transaction semantics plus observed service code):** Current service methods rely on Spring's normal rollback for unchecked exceptions. The observed business failures (`ResponseStatusException`, data-integrity exceptions, and optimistic-lock exceptions) are unchecked. No evidence in the inspected paths requires broad `rollbackFor` configuration or a second transaction abstraction. A commit-time optimistic failure occurs after the service body and must propagate through the transaction interceptor so the whole unit of work rolls back.

### Existing test coverage — sufficient for these behaviors

- **VERIFIED (`ClientAggregateUpdateHttpIntegrationTest.java`):** `staleNestedDocumentVersionRollsBackEarlierAggregateChanges` sends a Client PUT that first changes Client/Document state and then encounters a stale later nested Document version. It expects HTTP 409, clears the persistence context, reloads, and verifies that Client and Document fields and the attempted unique Document number did not persist. This is direct existing coverage of aggregate PUT rollback when a later nested Document fails.
- **VERIFIED (`ClientAggregateUpdateHttpIntegrationTest.java`):** Client DELETE tests missing, malformed, negative, and stale Client-version preconditions, verifies Client and associated Document rows remain after failed preconditions, and verifies current-version DELETE removes both. This covers the HTTP version gate and successful cascade path.
- **VERIFIED (`DocumentControllerTest.java`, `DocumentServiceTest.java`):** The controller test verifies valid DELETE header binding, missing/malformed headers, and a 409 response mapping; service tests verify stale delete preconditions and reject negative Document versions. These are layered tests, not a single HTTP-to-persistence integration case for negative DELETE headers.
- **VERIFIED (`ClientServiceTest.java`):** Client deletion success verifies Client and associated Document removal; stale/missing/negative service preconditions verify no Client/document deletion. Existing T2.5 tests exercise collection preservation, identity retention, and no transfer; these must remain regression controls.
- **VERIFIED (`DomainPersistenceTest.java`):** Tests cover JPA cascade deletion, database cascade deletion, orphan-FK rejection, explicit in-memory collection-preserving behavior, and both entities' optimistic locking. These establish persistence/cascade behavior, not all service-level rollback cases.
- **VERIFIED (`DocumentServiceTest.java`):** Tests cover standalone Document creation and PUT/PATCH modification, owner/ID preservation, explicit deletion removing only the requested Document, stale Client/Document delete preconditions, and invalid service versions. The stale delete assertions establish that the Document remains but do not directly assert that its Client version and any sibling rows remain unchanged.
- **VERIFIED (`OptimisticConcurrencyIntegrationTest.java`):** Real concurrent Client aggregate and standalone Document update scenarios use separate transactions. They require one winner/one conflict and verify persisted winning/losing state; aggregate different-Document writers also verify only the winner's child change persists. This is relevant T2.6 regression coverage and should not be duplicated or weakened in T2.7.
- **VERIFIED (`CommitTimeOptimisticLockIntegrationTest.java`):** The test injects a failure during commit after a transactional request has called `DocumentService.create`; it verifies HTTP 409 Problem Details and absence of the attempted Document afterward. It provides a commit-time rollback path, but is intentionally an injected exception and is not a substitute for the missing create-with-nested-Documents rollback assertion.

### Actual coverage gaps

- **VERIFIED (comparison of `ClientServiceTest.java` and `ClientAggregateUpdateHttpIntegrationTest.java` against T2.7):** Existing Client POST coverage confirms successful creation with nested Documents. A duplicate nested Document number scenario asserts a conflict in the service test, but does not assert after a fresh persistence-context read that neither the Client nor any earlier Document was persisted. The separate HTTP integration rollback test covers PUT, not POST creation.
- **VERIFIED (same comparison):** Existing Client aggregate late-failure rollback evidence is specifically PUT. A later nested Document failure in Client PATCH is not directly asserted with persisted-state reload.
- **VERIFIED (`DocumentServiceTest.java` and T2.7 contract):** Existing tests establish normal Document modification/deletion and version preconditions, but do not directly prove that a failed Document mutation leaves the Client version and all relevant rows unchanged. T2.6's actual concurrent-update tests cover optimistic conflict rollback; avoid duplicating those concurrency cases as generic T2.7 work.
- **VERIFIED (T2.7 contract compared with tests):** The approved Client/Document DELETE version rules are represented across controller and service tests; a real HTTP integration assertion for semantically negative Document DELETE header values is not present. Existing Client DELETE integration already covers negative `X-Client-Version`.
- **PENDING VERIFICATION:** The minimal additional tests below should be run by the authorized Developer/Tester workflow. No tests were run in this Architect execution.

## 9. Findings and severity

1. **VERIFIED — Existing service transaction boundaries align with the plan.** All target mutation methods already have service-level `@Transactional` annotations. No propagation, rollback-rule, or broad boundary refactor is supported by inspected evidence.
2. **MODERATE — Coverage gap: Client creation rollback is not proven by a fresh-state integration assertion.** Existing duplicate-number coverage observes the conflict but does not establish the absence of a partially persisted Client/earlier Document after rollback.
3. **MODERATE — Coverage gap: Client PATCH late nested failure rollback is not directly demonstrated.** Existing HTTP rollback coverage exercises PUT; PATCH follows a distinct service branch and is part of the same approved aggregate atomicity rule.
4. **LOW — In-memory Document relationship consistency:** `DocumentService.delete` explicitly deletes the row but does not remove the Document from the loaded Client's inverse collection, contrary to the documented `Client.removeDocument` service-use contract. Recommend the one-line helper call within the existing transaction before repository deletion; do not treat collection removal alone as deletion.
5. **LOW — Coverage gap: Failed Document deletion does not assert all relevant state.** Current tests assert the target Document remains after stale preconditions; assertions for the owning Client version and a sibling Document would make deletion rollback evidence direct.
6. **LOW — Coverage gap: Negative Document DELETE header values are not exercised end-to-end.** Existing controller and service tests cover the boundaries separately; one real integration assertion may close this narrow contract-evidence gap.
7. **VERIFIED — T2.6 interaction is a constraint, not a proposed redesign.** Existing optimistic lock/version checks and parent force-increment must stay inside each service transaction; do not add an independent transaction, manual version update, or weaken stale-conflict behavior.

## 10. Decisions and reasoning

### Smallest proposed implementation

- **VERIFIED / RECOMMENDED:** First retain the existing `@Transactional` boundaries in `ClientService` and `DocumentService`. The current transaction annotations, exception propagation, and flush points align with the service-layer rule; no new transaction helper, facade, propagation mode, dependency, endpoint, DTO, entity behavior, or migration is justified by inspected evidence.
- **VERIFIED / RECOMMENDED:** Preserve existing T2.5 behavior in `ClientService`: no child deletion during PUT/PATCH, preserve existing Document IDs, reject foreign ownership as already specified, and perform explicit Document deletion only through `DocumentService.delete`. Do not call collection replacement/removal as a shortcut for deletion.
- **VERIFIED / RECOMMENDED:** In `DocumentService.delete`, use the already-loaded Client reference from `lockClient` to call `client.removeDocument(document)` within the same transaction before `documentRepository.delete(document)`. Keep the explicit repository delete; the entity helper only synchronizes the in-memory inverse collection and is not configured as orphan removal.
- **VERIFIED / RECOMMENDED:** Preserve T2.6 version checks and `OPTIMISTIC_FORCE_INCREMENT` in the same existing mutation transaction. Do not manually increment `Client.version`; JPA owns this field. A conflict or late nested failure must escape as an unchecked exception so Spring rolls back the aggregate.
- **RECONSTRUCTED / RECOMMENDED:** If a test demonstrates a transaction-boundary gap, make the smallest correction in the corresponding existing service method. Add `rollbackFor` or adjust exception translation only if evidence identifies a checked exception or a caught exception that otherwise permits commit; none was observed in the inspected paths.

### Exact files and minimal test scenarios

1. **Recommended — Client creation rollback:** Extend `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java` with POST of a valid Client and two individually valid nested Documents sharing one globally unique number not already present. Expect HTTP 409 Problem Details, then clear/reload and assert the Client tax identifier and attempted Document number are absent. Existing successful nested Client creation remains the success control.
2. **Recommended — Client PATCH rollback on later nested failure:** In the same integration class, submit PATCH with a scalar change and two existing nested Documents: the first has a current version and a change; the later one has a stale expected version. Expect HTTP 409, clear/reload, and assert the Client scalar, first Document fields/version, second Document fields/version, and parent version are unchanged. Retain the existing PUT rollback test; use existing fixtures/utilities, with no new DTO.
3. **Recommended — failed Document DELETE preserves the complete aggregate state:** Extend `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java` in `staleClientOrDocumentVersionPreventsStandaloneDocumentDelete` to create/retain a sibling, capture the Client version, then assert after stale Client and stale Document version failures that both Documents remain, ownership is unchanged, and the Client version is unchanged. Keep `deleteRemovesOnlyTheRequestedDocument` as success coverage; extend it to assert the Client's reloaded document collection contains only the sibling.
4. **Recommended — keep in-memory relationship aligned on explicit deletion:** Production change only in `src/main/java/com/humanit/challenge/service/DocumentService.java`: call `client.removeDocument(document)` before the existing explicit repository delete. No entity, schema, endpoint, DTO, or transaction-boundary change is proposed.
5. **Optional narrow end-to-end header assertion:** If the Tech Lead requires the negative Document DELETE header rule to be verified through the full HTTP/persistence path (rather than existing controller + service layer tests), extend `ClientAggregateUpdateHttpIntegrationTest` with negative `X-Client-Version` and `X-Document-Version` DELETE requests and assert 400 plus unchanged Document/Client state. The existing controller tests already cover missing/malformed inputs and header binding, so this is not otherwise an essential additional test.
6. **Existing coverage to reuse, not duplicate:** `OptimisticConcurrencyIntegrationTest` already exercises actual optimistic-lock conflicts and winner-only persistence; `CommitTimeOptimisticLockIntegrationTest` already proves rollback when a commit-time failure follows Document creation; `DomainPersistenceTest` already verifies cascade and no-orphan database constraints; `ClientAggregateUpdateHttpIntegrationTest` already proves Client PUT late-child-failure rollback and Client DELETE version/cascade behavior.

### Alternatives and trade-offs

- **Rejected — broad transaction refactor:** Every target service mutation already has an appropriate transaction annotation. Refactoring would increase scope and could break the same-transaction relationship between version checks, force increment, persistence, and rollback without evidence of benefit.
- **Rejected — catch-and-return success or swallow persistence exceptions:** This would permit partial commit or obscure the established 409/error contract. Keep failures propagating after translation to an unchecked status exception.
- **Rejected — deleting Documents by replacing/removing the Client collection:** T2.5/T2.7 require collection-preserving updates and deletion only through explicit DELETE; entity removal from the inverse collection alone is not row deletion.
- **Rejected — relying on inverse collection removal alone:** The in-memory helper does not delete the database row. The proposed Document DELETE continues to call `DocumentRepository.delete` explicitly in the same transaction.
- **Rejected — new locking/version mechanism:** T2.6 already selects JPA `@Version` and parent `OPTIMISTIC_FORCE_INCREMENT`; duplicate manual version logic risks stale or double increments.

### Decisions requiring Tech Lead approval

- **VERIFIED:** No change to the approved API, DTOs, ownership, version, error status, or cascade contract is proposed; those are not open redesign decisions.
- **PENDING TECH LEAD APPROVAL:** Approve or revise the narrow T2.7 scope before implementation, including the minimal `DocumentService.delete` relationship-synchronization call and the Client POST/PATCH rollback test additions. Decide whether the optional negative Document DELETE header integration case is required.
- **VERIFIED:** No decision-log append is proposed or made. Any new or changed requirement would require explicit Tech Lead direction and the normal append-only decision process.

## 11. Final status

**PROPOSAL COMPLETE — AWAITING HUMAN TECH LEAD APPROVAL.** Existing transaction boundaries appear aligned with the approved scope. Proposed changes are one small in-memory relationship synchronization in `DocumentService.delete` plus focused persistence-backed tests for Client POST/PATCH rollback and failed Document deletion. No implementation, test run, Git inspection, or acceptance is claimed. Stop after this proposal.

## 12. Outstanding issues

1. **PENDING APPROVAL:** Human Tech Lead review/approval is required before any implementation or test changes.
2. **PENDING VERIFICATION:** Add/run focused evidence for POST-with-Documents rollback and PATCH later-nested-failure rollback; retain and rerun the current PUT rollback scenario.
3. **PENDING VERIFICATION:** Add direct no-partial-delete assertions for the owning Client version and sibling Document state after stale Document DELETE preconditions; if approved, synchronize the in-memory inverse collection in `DocumentService.delete` while retaining explicit row deletion.
4. **PENDING APPROVAL / OPTIONAL:** Tech Lead decides whether negative Document DELETE headers need a full HTTP-to-persistence integration test in addition to existing controller/service tests.
5. **VERIFIED:** Existing T2.5 identity/ownership and collection-preserving behavior and T2.6 optimistic-lock/force-increment behavior must remain unchanged.
6. **NOT AVAILABLE:** Git branch/status was not inspected due the no-shell constraint and lack of a Git-status tool.
7. **VERIFIED:** No tests or build were run, no application files were changed, and no Tech Lead approval or final acceptance is claimed.

## 13. Handoff to next agent

- **VERIFIED:** Human Tech Lead: review/approve or revise this proposal; no implementation may begin before explicit approval.
- **PENDING VERIFICATION:** After authorization, Developer: retain current transaction boundaries; apply only the approved `DocumentService.delete` collection-synchronization change and focused tests; preserve explicit Document row deletion and T2.5/T2.6 contracts. No change is proposed to Client cascade deletion or CRUD boundaries absent a concrete test failure.
- **VERIFIED (reporting workflow):** Tester verifies independently before reading Developer conclusions. Reviewer audits independently before reading Developer/Tester reports or this proposal. Only the Human Tech Lead makes final acceptance decisions.
- **VERIFIED:** Stop after this proposal. No branch switch, status command, implementation, Maven run, decision-log edit, commit, push, merge, or self-approval was performed.
