# Reviewer Report — T2.6 Final Concurrency Test Review

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 2, T2.6): T2.6 — concurrency control and version validation.

## 2. Execution identifier and date

- **VERIFIED (direct tasking):** `T2.6-EXEC-007-REV`.
- **VERIFIED (environment):** 2026-11-11.

## 3. Agent role and model

- **VERIFIED:** Reviewer role; model `opencode-go/gpt-6-luna` (`gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED (direct tasking):** Final review of `OptimisticConcurrencyIntegrationTest.java` against the approved T2.6 concurrency requirements: shared initial Client version observation, deterministic synchronization, strict optimistic-lock failure, one winner, winner-only persisted changes, retained same-Document/scalar assertions, and no production changes in the follow-up.
- **VERIFIED (direct tasking):** Review/report only. No source, configuration, governance or requirements changes; no Maven execution, Git mutation, branch switch, commit, merge, push or self-approval. Stop after reporting.
- **VERIFIED** (`implementation-plan.md`, T2.6; `requirements-decisions.md`, Section 20): Document mutations must participate in parent Client optimistic locking within the same transaction; stale versions conflict and transactions roll back. General T2.7 redesign is outside this follow-up review.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (T2.6); `requirements-decisions.md` (optimistic versioning and locking requirements).
- **VERIFIED — independent source inspection completed before reading full role reports:** `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`; `src/main/java/com/humanit/challenge/service/ClientService.java`; `src/main/java/com/humanit/challenge/repository/ClientRepository.java`; `src/main/java/com/humanit/challenge/domain/Client.java`.
- **VERIFIED — comparison after source inspection:** `agent-reports/T2.6/developer-report.md`; `agent-reports/T2.6/tester-report.md`; prior `agent-reports/T2.6/reviewer-report.md`.
- **VERIFIED:** `agent-reports/decisions.md` was checked for current approval context; no T2.6 entry was found in the inspected end-of-file section. The direct tasking supplies the approved-requirements scope for this review.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.6/reviewer-report.md` — this execution's report.
- **VERIFIED:** `agent-reports/T2.6/history/reviewer-001.md` — unchanged archive of the prior primary Reviewer report, created before replacing the primary.
- **VERIFIED:** No production code, test code, configuration, requirements, planning, decision-log or other role report was changed. No Git state mutation was performed.

## 7. Commands executed

- **VERIFIED:** Read-only Git command: `git status --short; git diff --name-only; git diff -- src/main/java/com/humanit/challenge/service/ClientService.java src/main/java/com/humanit/challenge/repository/ClientRepository.java`.
- **VERIFIED:** No Maven command was executed, as prohibited for the Reviewer. No Git mutation was performed.
- **VERIFIED (process record):** A broad content search run while locating T2.6 requirements returned excerpts from T2.6 role reports before the source review was complete. The required files and source were then reviewed directly; the exposure is disclosed in Section 8 and qualifies the independence process.

## 8. Evidence and observed results

### Requirement-by-requirement source review

1. **VERIFIED — both transactions observe the same initial Client version before either can commit:** In the different-Document case, two separate `TransactionTemplate` callbacks run on the two-worker executor. The optional `ValidationGate` path loads the Client in each transaction, records its loaded version, checks it against the fixture version, signals the two-party validation latch, and blocks on the release latch inside the transaction callback. The coordinating thread waits for both checks and asserts the two recorded versions equal the fixture version before releasing either callback. Neither callback can finish/commit while blocked there.
2. **VERIFIED — deterministic synchronization at the shared-version validation point:** Coordination is by `CountDownLatch`, bounded `await`, and `Future`; no sleep is used. The main thread releases both callbacks only after both checks and exact-version assertions complete.
3. **VERIFIED — a real optimistic-lock exception is required:** `assertOneWinnerAndOptimisticLockConflict` requires an `OptimisticLockingFailureException` or JPA `OptimisticLockException` in the loser failure's cause chain. A `ResponseStatusException` representing HTTP 409 alone does not satisfy this assertion.
4. **VERIFIED — exactly one transaction succeeds:** The target assertion requires two outcomes and exactly one successful outcome.
5. **VERIFIED — only winner changes persist:** The test reloads both Documents, requires exactly one winner marker, checks the winning Document's number, description and expiration date, checks the losing Document retains its initial number and description, and requires the parent Client version to advance.
6. **VERIFIED with baseline limitation — existing cases retain their observable assertions:** Same-Document and scalar-writer cases still call `assertOneWinnerAndConflict`, which requires one success and one conflict; each continues to check persisted winner state. The concurrency test is currently untracked, and no earlier source snapshot was available for an exact before/after assertion diff (**NOT AVAILABLE**).
7. **VERIFIED with historical-evidence limitation — no production change attributable to the follow-up is reported:** The Developer report lists the concurrency test and its report/history as changed and states no production changes; the Tester report records production files already modified before its run and the same production diff afterward. This review's current `git status` also shows the existing `ClientController.java` and `ClientService.java` worktree modifications relative to HEAD, so the current snapshot alone cannot establish their historical author or timing (**NOT AVAILABLE**). No evidence reviewed attributes a production edit to the concurrency-test follow-up.

### `EntityManager.clear()` and overlap

- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java`, validation-gate branch): `entityManager.clear()` runs only after both version checks have completed and the release latch opens. It clears the transaction-bound persistence context; it does not commit/end the enclosing `TransactionTemplate` transaction. Thus both transactions remain open after their shared-version observations.
- **VERIFIED** (`ClientService.java:70-85,141-147`; `ClientRepository.java:20-22`): The subsequent service call joins the surrounding transaction and reloads via the repository's `OPTIMISTIC_FORCE_INCREMENT` query for the nested Document mutation. Clearing prevents the preflight managed Client from being reused as the update's locked load.
- **VERIFIED (interpretation):** A successful run cannot pass by merely receiving the stale-version HTTP 409 path: the cause-chain assertion requires an optimistic-lock exception. If a writer commits before the other performs its post-clear locked load, the latter can instead see the advanced version and fail the strict assertion; this is a failed test, not a false-positive pass. The source deterministically gates the two initial version observations and leaves both transaction callbacks open; it does not add a second barrier after both post-clear service lock-loads.
- **RECONSTRUCTED from role reports, not independently run here:** Developer and Tester reports say the focused test passed all three concurrency scenarios. No Maven/Surefire output was independently inspected or executed during this Reviewer run, so runtime success is not independently attested here.

### Report comparison and process qualification

- **VERIFIED (comparison):** Developer report `T2.6-EXEC-005-DEV` claims the follow-up changed only the test, added the shared-version gate, cleared the persistence context, strengthened optimistic-lock classification, retained other writer assertions, and passed focused/full test runs. Direct source inspection supports the described test structure and assertions; production-change attribution and runtime results are supported by the report/tester artifacts rather than an independent before/after snapshot/run in this execution.
- **VERIFIED (comparison):** Tester report `T2.6-EXEC-006-TEST` independently describes the same latch ordering, strict exception check, winner persistence assertions, and retained assertions. Its report also discloses that report excerpts were exposed before its test run. The same process issue occurred in this Reviewer execution: search output exposed report excerpts before independent source inspection was complete. This is disclosed; the audit conclusions above are grounded in direct source inspection, and the report comparison occurred after that inspection.
- **VERIFIED (comparison):** Prior Reviewer Minor #1 identified absence of proof that both concurrent callbacks observed the same initial Client version before either could commit. The new explicit two-reader gate directly addresses that gap. Reviewer Minor #2 (unnecessary post-transaction reread for an identity/version-only nested PATCH) was not part of this test follow-up and remains documented without being reassessed as resolved.

## 9. Findings and severity

1. **VERIFIED — Reviewer Minor #1: RESOLVED.** The new validation gate makes both transactions load/check the same fixture Client version and wait before either callback may proceed/commit; the explicit assertion checks both observations. `EntityManager.clear()` does not end either transaction and allows the actual service update to exercise the normal force-increment load path. The strict failure assertion rejects a generic HTTP 409. A post-clear load that misses the overlap causes the test to fail rather than pass without optimistic locking. The source meets the requested shared-version and real-conflict proof; no new Critical or Major finding was identified.
2. **VERIFIED — Reviewer Minor #2 remains documented:** The prior review's avoidable post-transaction `getById` for identity/version-only nested PATCH input remains an unrelated Minor finding. This concurrency-only follow-up neither addresses nor reopens it.

**VERIFIED:** No new Critical or Major finding was identified in this follow-up review. No change to T2.6 production behavior is identified as part of the test-only follow-up. PostgreSQL-specific behavior remains **NOT AVAILABLE** in the inspected test artifacts.

## 10. Decisions and reasoning

- **VERIFIED:** The explicit gate satisfies the requirement that both transactions observe/check the same initial Client version before either can commit. The two transactions remain in their callbacks until the coordinating thread releases them after checking both observations.
- **VERIFIED:** `EntityManager.clear()` detaches the preflight entities but does not finish the surrounding transactions. This separation ensures the service performs its normal repository force-increment load while preserving overlapping transaction lifetimes.
- **VERIFIED:** Requiring a real optimistic-lock exception in the loser prevents the test from treating a sequential stale-version HTTP conflict as a successful concurrency assertion. The exactly-one-success and persisted-field checks establish outcome and rollback expectations in source.
- **VERIFIED (limitation):** No second synchronization barrier is placed after the service's post-clear load. Hence scheduling can cause a strict test failure if one transaction completes too early; this does not permit a false passing result because the loser must have an optimistic-lock exception. The existing deterministic barrier and strict assertion resolve the prior review gap without a new finding for this follow-up.
- **VERIFIED:** Reviewer Minor #1 is **RESOLVED**. Reviewer Minor #2 remains documented. Recommendation is **PASS WITH FINDINGS**, because Minor #2 remains open; this is the Reviewer's recommendation only, not Human Tech Lead acceptance.

## 11. Final status

**PASS WITH FINDINGS — Reviewer recommendation only.** **VERIFIED:** Reviewer Minor #1 is **RESOLVED**; Reviewer Minor #2 remains documented. No new Critical or Major finding was identified. **PENDING APPROVAL:** No Human Tech Lead final acceptance is claimed. Runtime results are reconstructed from the Tester/Developer reports and were not rerun by this Reviewer.

## 12. Outstanding issues

1. **VERIFIED — MINOR:** Reviewer Minor #2 remains documented: unnecessary post-transaction reread for identity/version-only nested PATCH input.
2. **NOT AVAILABLE:** This execution did not independently run Maven or inspect Surefire output; reported focused/full test results remain attributed to the Developer/Tester reports.
3. **NOT AVAILABLE:** Exact before/after test-source diff is unavailable because the concurrency test is untracked and no earlier source snapshot was found.
4. **NOT AVAILABLE:** Current Git status cannot independently prove the historical timing/author of pre-existing production-file modifications; reviewed reports do not attribute production edits to this follow-up.
5. **NOT AVAILABLE:** PostgreSQL-specific concurrency verification was not established by the inspected T2.6 test artifacts.
6. **PENDING APPROVAL:** Only the Human Tech Lead may approve final acceptance; no approval is claimed.

## 13. Handoff to next agent

- **VERIFIED:** Archive created at `agent-reports/T2.6/history/reviewer-001.md`; current execution report is `agent-reports/T2.6/reviewer-report.md`.
- **VERIFIED:** Recommendation is **PASS WITH FINDINGS**; Reviewer Minor #1 is **RESOLVED**, Reviewer Minor #2 remains documented, and no new Critical/Major finding was identified.
- **PENDING APPROVAL:** Human Tech Lead retains final acceptance authority. No source changes, Maven run, Git mutation, branch change, commit, merge, push or self-approval was performed. Stop after reporting.
