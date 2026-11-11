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
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`; `src/main/java/com/humanit/challenge/service/DocumentService.java`; `src/main/java/com/humanit/challenge/domain/Client.java`; `src/main/java/com/humanit/challenge/domain/Document.java`; `src/main/java/com/humanit/challenge/repository/ClientRepository.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`; `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`; `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`; `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`; `src/test/java/com/humanit/challenge/exception/CommitTimeOptimisticLockIntegrationTest.java`; `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`.
- **VERIFIED:** T2.5 Architect proposal and T2.6 Architect, Developer, Tester, and Reviewer reports were read to preserve the prior task contracts and accurately assess the current T2.6 interaction.
- **NOT AVAILABLE:** No prior T2.7 Architect primary report or T2.7 Architect history archive was found by the `agent-reports/**` path search.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.7/architect-proposal.md` — this report only.
- **VERIFIED:** No prior T2.7 Architect report existed in the searched report paths; no history archive was required.
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
- **RECONSTRUCTED (Spring transaction semantics plus observed service code):** Current service methods rely on Spring's normal rollback for unchecked exceptions. The observed business failures (`ResponseStatusException`, data-integrity exceptions, and optimistic-lock exceptions) are unchecked. No evidence in the inspected paths requires broad `rollbackFor` configuration or a second transaction abstraction. A commit-time optimistic failure occurs after the service body and must propagate through the transaction interceptor so the whole unit of work rolls back.

### Existing test coverage — sufficient for these behaviors

- **VERIFIED (`ClientAggregateUpdateHttpIntegrationTest.java`):** `staleNestedDocumentVersionRollsBackEarlierAggregateChanges` sends a Client PUT that first changes Client/Document state and then encounters a stale later nested Document version. It expects HTTP 409, clears the persistence context, reloads, and verifies that Client and Document fields and the attempted unique Document number did not persist. This is direct existing coverage of aggregate PUT rollback when a later nested Document fails.
- **VERIFIED (`ClientAggregateUpdateHttpIntegrationTest.java`):** Client DELETE tests missing, malformed, negative, and stale Client-version preconditions, verifies Client and associated Document rows remain after failed preconditions, and verifies current-version DELETE removes both. This covers the HTTP version gate and successful cascade path.
- **VERIFIED (`ClientServiceTest.java`):** Client deletion success verifies Client and associated Document removal; stale/missing/negative service preconditions verify no Client/document deletion. Existing T2.5 tests exercise collection preservation, identity retention, and no transfer; these must remain regression controls.
- **VERIFIED (`DomainPersistenceTest.java`):** Tests cover JPA cascade deletion, database cascade deletion, orphan-FK rejection, explicit in-memory collection-preserving behavior, and both entities' optimistic locking. These establish persistence/cascade behavior, not all service-level rollback cases.
- **VERIFIED (`DocumentServiceTest.java`):** Tests cover standalone Document creation and PUT/PATCH modification, owner/ID preservation, explicit deletion removing only the requested Document, stale Client/Document delete preconditions, and invalid service versions. The stale delete assertions establish that the Document remains but do not directly assert that its Client version and any sibling rows remain unchanged.
- **VERIFIED (`OptimisticConcurrencyIntegrationTest.java`):** Real concurrent Client aggregate and standalone Document update scenarios use separate transactions. They require one winner/one conflict and verify persisted winning/losing state; aggregate different-Document writers also verify only the winner's child change persists. This is relevant T2.6 regression coverage and should not be duplicated or weakened in T2.7.
- **VERIFIED (`CommitTimeOptimisticLockIntegrationTest.java`):** The test injects a failure during commit after a transactional request has called `DocumentService.create`; it verifies HTTP 409 Problem Details and absence of the attempted Document afterward. It provides a commit-time rollback path, but is intentionally an injected exception and is not a substitute for the missing create-with-nested-Documents rollback assertion.
- **VERIFIED (T2.6 role reports):** The Developer report records T2.6 full-suite results, the Tester report records focused concurrency verification, and the Reviewer report identifies its recommendation/findings. These are historical task reports, not a test run performed in this Architect execution and not a new T2.7 acceptance decision.

### Actual coverage gaps

- **VERIFIED (comparison of `ClientServiceTest.java` and `ClientAggregateUpdateHttpIntegrationTest.java` against T2.7):** Existing Client POST coverage confirms successful creation with nested Documents. A duplicate nested Document number scenario asserts a conflict in the service test, but does not assert after a fresh persistence-context read that neither the Client nor any earlier Document was persisted. The separate HTTP integration rollback test covers PUT, not POST creation.
- **VERIFIED (same comparison):** Existing Client aggregate late-failure rollback evidence is specifically PUT. A later nested Document failure in Client PATCH is not directly asserted with persisted-state reload.
- **VERIFIED (`DocumentServiceTest.java` and T2.7 contract):** Existing tests establish normal Document modification/deletion and version preconditions, but do not directly prove that a failed Document mutation leaves the Client version and all relevant rows unchanged. T2.6's actual concurrent-update tests cover optimistic conflict rollback; avoid duplicating those concurrency cases as generic T2.7 work.
- **PENDING VERIFICATION:** The minimal additional tests below should be run by the authorized Developer/Tester workflow. No tests were run in this Architect execution.

## 9. Findings and severity

1. **VERIFIED — No transaction-boundary implementation defect identified from inspection.** All listed service mutations already have service-level `@Transactional` annotations, and existing code performs operations/flushes within them. Do not replace or broaden those boundaries without a test demonstrating a concrete defect.
2. **MODERATE — Coverage gap: Client creation rollback is not proven by a fresh-state integration assertion.** Existing duplicate-number coverage observes the conflict but does not establish the absence of a partially persisted Client/earlier Document after rollback.
3. **MODERATE — Coverage gap: Client PATCH late nested failure rollback is not directly demonstrated.** Existing HTTP rollback coverage exercises PUT; PATCH follows a distinct service branch and is part of the same approved aggregate atomicity rule.
4. **LOW — Coverage gap: Failed Document deletion does not assert all relevant state.** Current tests assert the target Document remains after stale preconditions; explicit assertions for the owning Client version and a sibling Document would make atomicity/referential-consistency evidence direct. This is a small assertion addition, not a production defect claim.
5. **VERIFIED — T2.6 interaction is a constraint, not a proposed change.** The existing optimistic lock/version comparisons and parent force-increment must stay inside each service transaction; do not add an independent transaction, manual version update, or weaken stale-conflict behavior.

## 10. Decisions and reasoning

### Smallest proposed implementation

- **VERIFIED / RECOMMENDED:** First retain the existing `@Transactional` boundaries in `ClientService` and `DocumentService`. The current transaction annotations, exception propagation, and flush points align with the service-layer rule; no new transaction helper, facade, propagation mode, dependency, endpoint, DTO, entity behavior, or migration is justified by inspected evidence.
- **VERIFIED / RECOMMENDED:** Preserve existing T2.5 behavior in `ClientService`: no child deletion during PUT/PATCH, preserve existing Document IDs, reject foreign ownership as already specified, and perform explicit Document deletion only through `DocumentService.delete`. Do not call collection replacement/removal as a shortcut for deletion.
- **VERIFIED / RECOMMENDED:** Preserve T2.6 version checks and `OPTIMISTIC_FORCE_INCREMENT` in the same existing mutation transaction. Do not manually increment `Client.version`; JPA owns this field. A conflict or late nested failure must escape as an unchecked exception so Spring rolls back the aggregate.
- **RECONSTRUCTED / RECOMMENDED:** If a test demonstrates a transaction-boundary gap, make the smallest correction in the corresponding existing service method. Add `rollbackFor` or adjust exception translation only if evidence identifies a checked exception or a caught exception that otherwise permits commit; none was observed in the inspected paths.

### Exact files and minimal test scenarios

1. **Recommended — Client creation rollback:** Extend `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java` (preferred, because it verifies the public failure response and committed database state) with POST of a valid Client and two individually valid nested Documents sharing one globally unique number, where that number does not already exist. This reaches the aggregate flush/unique constraint after creation work begins. Expect HTTP 409 Problem Details, then clear/reload and assert the Client tax identifier and attempted Document number are absent. Existing happy-path nested Client creation remains the success control. Do not add a new endpoint or contract.
2. **Recommended — Client PATCH rollback on later nested failure:** In the same integration test class, submit PATCH with a scalar change and two existing nested Documents: the first has a current version and a change; the later one has a stale expected version. Expect HTTP 409, clear/reload, and assert the Client scalar, first Document fields/version, second Document fields/version, and parent version are unchanged. This narrowly covers the PATCH branch while the existing PUT rollback test remains intact. Reuse the existing test fixture/body utilities as practical; no new DTO is needed.
3. **Recommended — explicit Document DELETE referential assertions:** Extend `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java` in `staleClientOrDocumentVersionPreventsStandaloneDocumentDelete` to retain/assert a sibling Document and capture the current Client version. After both stale-precondition failures, assert target and sibling still exist, target still belongs to the same Client, and the Client version is unchanged. The existing `deleteRemovesOnlyTheRequestedDocument` remains the successful explicit-delete scenario. This adds assertions rather than a new test architecture.
4. **Existing coverage to reuse, not duplicate:** `OptimisticConcurrencyIntegrationTest` already exercises actual optimistic-lock conflicts and winner-only persistence; `CommitTimeOptimisticLockIntegrationTest` already proves rollback when a commit-time failure follows Document creation; `DomainPersistenceTest` already verifies cascade and no-orphan database constraints; `ClientAggregateUpdateHttpIntegrationTest` already proves Client PUT late-child-failure rollback and Client DELETE version/cascade behavior.

### Alternatives and trade-offs

- **Rejected — broad transaction refactor:** Every target service mutation already has an appropriate transaction annotation. Refactoring would increase scope and could break the same-transaction relationship between version checks, force increment, persistence, and rollback without evidence of benefit.
- **Rejected — catch-and-return success or swallow persistence exceptions:** This would permit partial commit or obscure the established 409/error contract. Keep failures propagating after translation to an unchecked status exception.
- **Rejected — deleting Documents by replacing/removing the Client collection:** T2.5/T2.7 require collection-preserving updates and deletion only through explicit DELETE; entity removal from the inverse collection alone is not row deletion.
- **Rejected — new locking/version mechanism:** T2.6 already selects JPA `@Version` and parent `OPTIMISTIC_FORCE_INCREMENT`; duplicate manual version logic risks stale or double increments.

### Decisions requiring Tech Lead approval

- **VERIFIED:** No change to the approved API, DTOs, ownership, version, error status, or cascade contract is proposed; those are not open redesign decisions.
- **PENDING TECH LEAD APPROVAL:** Approve or revise this narrow T2.7 scope before implementation. In particular, decide whether to include the minimal Client POST rollback and Client PATCH late-failure tests as proposed; they are verification gaps, not evidence of a production defect.
- **VERIFIED:** No decision-log append is proposed or made. Any new or changed requirement would require explicit Tech Lead direction and the normal append-only decision process.

## 11. Final status

**PROPOSAL COMPLETE — AWAITING HUMAN TECH LEAD APPROVAL.** Existing transaction boundaries appear aligned with the approved scope; the proposal focuses on three small, persistence-backed test gaps. No implementation, test run, Git inspection, or acceptance is claimed. Stop after this proposal.

## 12. Outstanding issues

1. **PENDING APPROVAL:** Human Tech Lead review/approval is required before any implementation or test changes.
2. **PENDING VERIFICATION:** Add/run focused evidence for POST-with-Documents rollback and PATCH later-nested-failure rollback; retain and rerun the current PUT rollback scenario.
3. **PENDING VERIFICATION:** Add direct no-partial-delete assertions for owning Client version and sibling Document state after stale Document DELETE preconditions.
4. **VERIFIED:** Existing T2.5 identity/ownership and collection-preserving behavior and T2.6 optimistic-lock/force-increment behavior must remain unchanged.
5. **NOT AVAILABLE:** Git branch/status was not inspected due the no-shell constraint and lack of a Git-status tool.
6. **VERIFIED:** No tests or build were run, no application files were changed, and no Tech Lead approval or final acceptance is claimed.

## 13. Handoff to next agent

- **VERIFIED:** Human Tech Lead: review/approve or revise this proposal; no implementation may begin before explicit approval.
- **PENDING VERIFICATION:** After authorization, Developer: reuse the current service boundaries; fix production code only if focused testing demonstrates a concrete rollback defect; add only the approved integration assertions/scenarios; preserve T2.5 and T2.6 contracts.
- **VERIFIED (reporting workflow):** Tester verifies independently before reading Developer conclusions. Reviewer audits independently before reading Developer/Tester reports or this proposal. Only the Human Tech Lead makes final acceptance decisions.
- **VERIFIED:** Stop after this proposal. No branch switch, status command, implementation, Maven run, decision-log edit, commit, push, merge, or self-approval was performed.
