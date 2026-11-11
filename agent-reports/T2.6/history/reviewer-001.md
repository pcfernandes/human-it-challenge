# Reviewer Report — T2.6 Independent Code Review

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2, T2.6): T2.6 — Concurrency control and version validation.

## 2. Execution identifier and date

- **VERIFIED (direct tasking):** `T2.6-EXEC-004-REV`.
- **VERIFIED (environment):** 2026-11-11.

## 3. Agent role and model

- **VERIFIED:** Reviewer role; model `opencode-go/gpt-6-luna` (`gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED (direct tasking and approved project artifacts):** Independently audit T2.6 implementation correctness, approved scope, tests, response-version behavior, conflict rollback, and readiness for Human Tech Lead acceptance.
- **VERIFIED (direct tasking):** Review only. No production/test/configuration/requirements/planning/decision-log changes, no Git state changes, no Maven execution, no branch switch, and no self-approval. Preserve pending governance reconstruction.
- **VERIFIED** (`implementation-plan.md`, T2.6/T2.7): T2.6 includes optimistic version validation, parent Client force-increment for Document mutations, stale-version conflicts, and rollback for version conflicts. General T2.7 transaction redesign remains out of scope.
- **VERIFIED (direct tasking):** Option A is approved for this review: initial Client POST with Documents does not request `OPTIMISTIC_FORCE_INCREMENT`.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `agent-reports/decisions.md` (D-17); `requirements-decisions.md` (Sections 8 and 20); `implementation-plan.md` (T2.5–T2.7).
- **VERIFIED — independent source inspection before opening the complete role reports:** `src/main/java/com/humanit/challenge/controller/ClientController.java`; `service/ClientService.java`; `service/DocumentService.java`; `repository/ClientRepository.java`; `domain/Client.java`; `domain/Document.java`; `dto/client/ClientPatchRequest.java`; `dto/client/ClientPutRequest.java`; `dto/patch/PatchValue.java`; `exception/GlobalExceptionHandler.java`.
- **VERIFIED — independent test inspection before opening the complete role reports:** `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`; `service/ClientServiceTest.java`; `controller/ClientAggregateUpdateHttpIntegrationTest.java`; `controller/ClientControllerTest.java`; `controller/DocumentControllerTest.java`.
- **VERIFIED:** Relevant tracked Git diffs were inspected for `ClientController.java`, `ClientService.java`, and the T2.6-modified controller/service tests. The untracked `OptimisticConcurrencyIntegrationTest.java` was inspected directly.
- **VERIFIED — comparison after independent source inspection:** `agent-reports/T2.6/architect-proposal.md`; `developer-report.md`; `tester-report.md`.
- **VERIFIED:** No prior `agent-reports/T2.6/reviewer-report.md` or `agent-reports/T2.6/history/` archive existed when checked; no Reviewer archive was required.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.6/reviewer-report.md` — this review report only.
- **VERIFIED:** No production code, test code, configuration, requirements, planning, decision-log, or other role-report file was changed. Existing modified/untracked governance files were left untouched.

## 7. Commands executed

- **VERIFIED:** `git status --short --branch`; `git diff --stat`; `git diff --name-only`.
- **VERIFIED:** `git diff -- src/main/java/com/humanit/challenge/controller/ClientController.java src/main/java/com/humanit/challenge/service/ClientService.java src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java src/test/java/com/humanit/challenge/controller/ClientControllerTest.java src/test/java/com/humanit/challenge/controller/DocumentControllerTest.java src/test/java/com/humanit/challenge/service/ClientServiceTest.java src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`.
- **VERIFIED:** No Maven command was executed, in accordance with the Reviewer restrictions. Git was used read-only; no branch switch or Git mutation occurred.
- **VERIFIED (worktree snapshot):** The current branch was `feature/T2.5-aggregate-updates`. `agent-reports/decisions.md` was modified and `agent-reports/GOV-DECISIONS-RECONSTRUCTION-001/` and `-002/` were untracked in the inspected worktree; all were preserved.

## 8. Evidence and observed results

### Contract, API, and scope

- **VERIFIED** (`requirements-decisions.md`, Sections 8/20; `implementation-plan.md`, T2.6): The contract requires Client DELETE `X-Client-Version`; 400 for missing/malformed/invalid required versions; 409 Problem Details for stale versions; and rollback on conflict. Client/Document update version locations remain the approved body/header locations.
- **VERIFIED** (`ClientController.java:71-76`; `ClientService.java:115-125`; `ClientAggregateUpdateHttpIntegrationTest.java:153-188`): Client DELETE binds `X-Client-Version`, validates negative versions in the service, compares the loaded Client version before deleting, flushes within the service transaction, and returns 204 on success. The HTTP test asserts 400 for absent, malformed and negative values; 409 for stale; and verifies the Client and Document rows remain after those failures. A current version deletes both rows. Unknown Client behavior remains 404.
- **VERIFIED** (`GlobalExceptionHandler.java:92-100,142-156,159-166`; controller integration tests): Conversion failures and `ResponseStatusException` paths are represented as Problem Details with their intended 400/409 status. The stale DELETE test checks `application/problem+json` and the 409 status field.
- **VERIFIED** (reviewed Git diff and worktree status): T2.6 production changes are limited to `ClientController.java` and `ClientService.java`; tests were added/extended in the controller/service test files and the untracked concurrency integration test. No DTO, entity, repository, migration, dependency, or schema change appears in the inspected T2.6 diff/status. The pending decision-log and governance-reconstruction work was not changed by this review.
- **VERIFIED** (`ClientService.java:169-229`; existing aggregate tests): The nested PUT/PATCH merge still resolves existing Document IDs, verifies parent ownership and each expected Document version, and updates existing entities in place. No ownership transfer or replacement identity behavior was introduced. Omitted/empty collections remain preserving behavior.
- **VERIFIED** (`ClientService.create`, lines 38-53; `ClientAggregateUpdateHttpIntegrationTest.java:100-118`): Client POST does not call `lockById`; the regression test creates initial Documents without an expected Client version and compares the response version to persistence without assuming an exact version. This follows the approved Option A in the tasking.

### Aggregate locking, transaction boundaries, and rollback

- **VERIFIED** (`ClientRepository.java:20-22`; `ClientService.java:70-76,88-96,141-147`; `DocumentService.java:35-46,62-127`): The existing repository query requests `OPTIMISTIC_FORCE_INCREMENT`. Aggregate PUT uses it before loading/mutating nested Documents when the supplied list is non-empty. PATCH requests it when a nested item is new or has editable values. These calls occur inside the existing `@Transactional` Client service methods. Standalone Document operations retain their existing use of the same parent lock inside their service transactions.
- **VERIFIED** (`ClientServiceTest.java:241-256,299-354`): The inspected tests assert that omitted and empty nested collections preserve Documents without parent version increments; an identity/version-only nested PATCH also does not increment. Scalar-only updates continue through the ordinary load path.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.java:190-227`): The conflict rollback test issues a real HTTP aggregate PUT that changes a Client scalar and an earlier nested Document before a later nested Document's stale version causes 409. After clearing and reloading persistence, the earlier scalar and Document values remain unchanged, the later Document remains unchanged, and the attempted replacement number is absent. This covers conflict rollback on the aggregate stale-child path; it is not a general T2.7 atomicity matrix.
- **VERIFIED** (`ClientService.java:181-190,207-219`; `Document.java:38-45`; `Client.java:45-52`): Existing Document identity and owning Client association are preserved; the service validates child versions before applying updates, and both rows retain their own JPA `@Version` fields.

### Concurrency test assessment

- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java:63-103,105-117,120-165`): The test creates two separate `TransactionTemplate` executions on worker threads, uses a two-party latch and a start latch, performs different-Document, same-Document and scalar-writer cases, asserts exactly one success and a conflict, and reloads state to assert only the winner's write is present.
- **VERIFIED (review interpretation):** The test coordinates transaction callbacks before invoking the service, not a barrier after both service calls have loaded and validated the same Client version. Scheduler timing can therefore let one service call finish before the second reaches the version check. The asserted one-winner outcome is valuable and the source uses the approved parent force-increment, but the test does not deterministically prove that both writers observed the same initial parent version before either could commit.
- **RECONSTRUCTED (test limitation):** A losing outcome can arise from the ordinary stale expected-Client-version check if the second service invocation runs only after the first transaction commits. That outcome alone does not establish the commit-time optimistic conflict path for two transactions that both read the same initial version. A test-only synchronization point after both operations have loaded/validated their initial versions and before commit/write completion would make that specific race deterministic.
- **VERIFIED (reports only; not independently executed by this Reviewer):** The Developer and Tester reports both state that the focused suite passed 63 tests and the full suite passed 308 tests. These outcomes are recorded as **RECONSTRUCTED** from those reports; this Reviewer did not run Maven and cannot independently attest the results.
- **NOT AVAILABLE:** No PostgreSQL-specific concurrency result was inspected for T2.6. The concurrency integration test configures H2.

### Response version and reread

- **VERIFIED** (`ClientService.update`/`patch`, `@Transactional`; `ClientController.java:51-69`; `ClientAggregateUpdateHttpIntegrationTest.java:120-151`): The controller receives the proxied service result after its service transaction completes, and for non-empty nested PUT/PATCH inputs it performs a separate `getById` reread. The HTTP test compares the returned Client version with a persistence reload after each nested-only operation and asserts it advanced from the preceding version. This is a reasonable way to expose the provider-applied force-increment without manual `@Version` mutation or response-contract change.
- **VERIFIED (concurrency window, not a demonstrated defect):** Because the reread is a separate service call/transaction, another writer could commit after the mutation transaction and before this reread. In that interleaving the response can reflect the newer committed aggregate rather than an operation-specific snapshot; the returned version would still correspond to the reread state. No such interleaving is covered by the inspected HTTP test, and no concrete response corruption was observed.
- **VERIFIED — Minor:** `ClientController.patch` rereads whenever the nested list is present and non-empty (`ClientController.java:64-66`), while `ClientService.patch` correctly avoids force-increment for an existing entry that contains only ID/version (`ClientService.java:92-95,224-229`). For that no-op request, the extra `getById` and aggregate mapping read are unnecessary. This does not undermine response-version correctness for actual nested mutations.

### Independence and report comparison

- **VERIFIED — process limitation:** Before source inspection was complete, a content-search request intended to find T2.6 decisions unexpectedly returned excerpts from the T2.6 Architect, Developer and Tester reports. I did not open the complete reports until after direct production/test inspection and Git diff review, but the excerpt exposure means the strict independence sequence was not fully maintained. Findings above are grounded in direct source/test inspection and diffs; this limitation is disclosed rather than claiming perfect independence.
- **VERIFIED (comparison):** The Architect's pre-implementation DELETE/aggregate-lock gaps are addressed in the inspected source. Its concern about initial POST force-increment is resolved by the explicit Option A tasking and matching code/test.
- **VERIFIED (comparison and disagreement):** The Developer report describes the competing-writer test as “deterministically coordinated,” and the Tester report describes coordinated real transactions. I agree the test starts two independent transactions/workers and verifies the outcomes, but disagree that its current barriers deterministically force both service operations to observe the same initial parent version before commit. The source-level force-increment implementation itself is present and correctly placed within the aggregate transaction.
- **VERIFIED (comparison):** The Tester’s Minor finding on the identity/version-only PATCH reread is confirmed. No evidence supports elevating it to a response correctness defect. No unsupported claim of independently rerun Maven results is made here.

## 9. Findings and severity

1. **MINOR — Concurrent-writer test does not deterministically synchronize after initial version reads.** **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java:120-145`): Both transaction callbacks synchronize before `mutation.apply(...)`; there is no synchronization proving that both service calls have loaded/validated the same parent version before either can commit. The test still exercises concurrent workers and asserts one winner, but a sequential stale-version rejection can satisfy the losing assertion without exercising the intended overlapping-read/commit race. **Suggested improvement:** add a test-only barrier at a point after both operations have read and validated the same initial Client/Document versions, then release both mutations/commits together; continue reloading and asserting only the winner's state remains. This is a test-strength limitation, not evidence that the inspected production lock is absent.
2. **MINOR — Unnecessary post-transaction read for a no-op nested PATCH.** **VERIFIED** (`ClientController.java:64-66`; `ClientService.java:92-95,224-229`; `ClientServiceTest.java:337-354`): An existing nested item with only ID/version does not mutate a Document or force-increment the Client, but the controller still calls `getById` because the list is non-empty. **Suggested improvement:** align the reread predicate with the service's “actual nested mutation” predicate or otherwise expose whether a force-increment was requested. The service already tests the no-force behavior; add a controller-level interaction assertion if the optimization is changed.

**VERIFIED:** No Critical or Major production-code defect was identified in the inspected T2.6 scope. General T2.7 atomicity redesign remains out of scope.

## 10. Decisions and reasoning

- **VERIFIED:** The Client DELETE behavior, aggregate parent force-increment placement, invalid/stale status handling, Option A behavior, T2.5 identity/ownership semantics, and rollback on stale nested version are consistent with the approved T2.6 contract in the inspected code and tests.
- **VERIFIED:** The post-transaction reread is justified for actual force-incremented nested mutations because JPA may apply the increment at transaction completion. The single-writer HTTP tests confirm the returned version matches persistence. The identity/version-only PATCH is a narrower unnecessary-read case; no response correctness failure was demonstrated.
- **VERIFIED:** The concurrency test's setup is not a deterministic proof of the exact overlapping-read race claimed in the Developer report. Because the implementation itself does use the approved optimistic force-increment inside the transaction, and the test exercises separate concurrent transactions with persisted-state assertions, this is rated Minor rather than a Major implementation defect.
- **VERIFIED:** Recommendation is **PASS WITH FINDINGS**: no Critical/Major code issue was observed; retain the two Minor findings and accurately characterize the concurrency test's synchronization limits. This is only the Reviewer's recommendation. The reported Maven results are not independently rerun here, and final acceptance belongs to the Human Tech Lead.

## 11. Final status

**PASS WITH FINDINGS — REVIEW COMPLETE (Reviewer recommendation only).** **VERIFIED:** 0 Critical, 0 Major, and 2 Minor findings. T2.6 source behavior and scope are substantially aligned with approved requirements; the deterministic overlap claim and avoidable no-op PATCH reread remain documented. **PENDING APPROVAL:** No Human Tech Lead acceptance is claimed.

## 12. Outstanding issues

1. **VERIFIED — MINOR:** Strengthen the concurrency integration test with a barrier after both writers have observed/validated the same initial versions if deterministic commit-race evidence is required.
2. **VERIFIED — MINOR:** Avoid the unnecessary post-transaction `getById` for identity/version-only nested PATCH input.
3. **RECONSTRUCTED:** Test totals (63 focused / 308 full suite) are known only from the Developer and Tester reports; this Reviewer did not execute Maven.
4. **NOT AVAILABLE:** PostgreSQL concurrency verification was not established by the inspected T2.6 test artifacts.
5. **PENDING APPROVAL:** The Human Tech Lead alone decides final acceptance. No commit, push, merge, or self-approval was performed.

## 13. Handoff to next agent

- **VERIFIED:** The Reviewer recommendation is **PASS WITH FINDINGS**; the two Minor findings and the independence-process limitation are recorded above.
- **PENDING VERIFICATION:** Human Tech Lead may decide whether to accept the documented Minor findings or authorize follow-up. Any follow-up implementation or test changes require the appropriate authorization and a subsequent independent review as required by the project workflow.
- **PENDING APPROVAL:** Do not treat this report as final acceptance. Stop after this report; no source edits or Git mutations were performed.
