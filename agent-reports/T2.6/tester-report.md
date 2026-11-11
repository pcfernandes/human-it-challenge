# Tester Report — T2.6 Concurrency Test Follow-up Verification

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 2, T2.6): T2.6 — Concurrency control and version validation.
- **VERIFIED** (direct tasking): Follow-up verification of `OptimisticConcurrencyIntegrationTest.java`.

## 2. Execution identifier and date

- **VERIFIED** (direct tasking): `T2.6-EXEC-006-TEST`.
- **VERIFIED** (environment): 2026-11-11.

## 3. Agent role and model

- **VERIFIED:** Tester role; model `opencode-go/gpt-6-luna` (`gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Read reporting conventions and approved T2.6 concurrency requirements; inspect the specified test; run the exact focused Maven command; compare with the Developer report only after source inspection and test execution; report only.
- **VERIFIED** (direct tasking): No source changes, governance edits, branch switch, commit, push or self-approval; stop after reporting.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (T2.6); `requirements-decisions.md` (versioning/optimistic-locking requirements); `agent-reports/decisions.md` (scope/approval context).
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`.
- **VERIFIED (after source inspection and Maven verification):** `agent-reports/T2.6/developer-report.md`; the previous `agent-reports/T2.6/tester-report.md` was read to archive it unchanged.
- **VERIFIED:** Surefire output `target/surefire-reports/com.humanit.challenge.service.OptimisticConcurrencyIntegrationTest.txt`; Git status and production-source diff, before and after the focused run.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.6/tester-report.md` — this execution report.
- **VERIFIED:** `agent-reports/T2.6/history/tester-001.md` — archived prior primary Tester report before replacing it.
- **VERIFIED:** No production or test source, requirements, decision log, or other agent's report was changed by this Tester execution. The pre-existing worktree already showed modified Client production files before the test command, and the same production diff remained after it; those changes were not made here.

## 7. Commands executed

- **VERIFIED:** `.\mvnw.cmd -B clean -Dtest=OptimisticConcurrencyIntegrationTest test` — `BUILD SUCCESS`; 3 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED:** `git status --short; git diff -- src/main; git diff -- src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java; git diff --check` — inspected before/after status and diffs; production modifications were present before the run and persisted unchanged afterward. The target test is untracked, so Git has no tracked before/after diff for it.

## 8. Evidence and observed results

### Requirement-by-requirement verification

1. **VERIFIED — both transactions observed the same initial Client version before commit:** In the target different-Document test, `compete` starts two `TransactionTemplate` callbacks. With the `ValidationGate` enabled, each callback loads the Client, adds the loaded version to the concurrent queue, checks it equals the fixture version, signals `validatedWriters`, and blocks on the `release` latch while still inside the transaction callback. The coordinating thread waits for both validations and asserts exactly two observed versions equal the fixture version before releasing either writer. A callback cannot finish/commit while blocked inside this transaction callback.
2. **VERIFIED — no sleep-based synchronization:** The test coordinates with `CountDownLatch`, `Future`, bounded `await` calls and `ExecutorService`; no `Thread.sleep` or sleep-based timing was found.
3. **VERIFIED — losing writer fails due to optimistic locking:** `assertOneWinnerAndOptimisticLockConflict` requires an `OptimisticLockingFailureException` or JPA `OptimisticLockException` in the losing failure's cause chain. The fallback generic HTTP 409 classification used by other scenarios is not used for this target assertion.
4. **VERIFIED — exactly one writer succeeds:** The target assertion requires two outcomes and exactly one success. The focused execution passed this assertion.
5. **VERIFIED — only the winner's changes persist:** The test reloads both target Documents, asserts exactly one has a `DOC-WINNER-` value, checks the winner's number/description/expiration date, and checks the other Document retains its initial number and description. The parent Client version is also asserted to have advanced.
6. **VERIFIED — no production code modified by this Tester execution:** The initial `git status`/`git diff -- src/main` inspection showed modified `ClientController.java` and `ClientService.java` before Maven ran. After Maven, the same status and production diff were present. No production file was edited by this execution. **NOT AVAILABLE:** This worktree inspection does not establish the historical author of those pre-existing changes.
7. **VERIFIED with baseline limitation — existing assertions not weakened:** The current source still runs same-Document and scalar-writer cases through `assertOneWinnerAndConflict`, which requires exactly one success and a conflict, and retains their persisted-winner assertions. The target different-Document case adds the stricter optimistic-lock exception assertion and field-level persistence checks. **NOT AVAILABLE:** A prior source snapshot or Git-tracked baseline for this untracked test was not available for a textual before/after comparison; therefore exact source-line preservation cannot be independently proven from Git history.

### Test execution and scope

- **VERIFIED** (Maven output and Surefire report): The exact requested Maven invocation completed successfully with 3 tests, 0 failures, 0 errors and 0 skipped. H2 was used by the test configuration.
- **VERIFIED:** No sleeps were added or present in the inspected target test. No dependencies or production source changes were made by this Tester execution.
- **NOT AVAILABLE:** This focused execution does not establish PostgreSQL-specific concurrency behavior or the entire project test-suite status.

### Independence-process qualification

- **VERIFIED (execution history):** Before Maven verification was complete, a broad T2.6 content search unexpectedly returned excerpts from the Developer report, including claims about synchronization and assertions. The target source had already been independently read, but these report conclusions were exposed before the required test run. The full Developer report was not opened until after the exact Maven run and before/after Git verification. This means the strict independence sequence was not fully maintained; the findings above are tied to direct source inspection and the Maven/Surefire evidence, and the process deviation is not concealed.

## 9. Findings and severity

1. **PASS — requested concurrency assertions:** All seven requested criteria are supported by the inspected source and, where runtime behavior is relevant, the passing focused test.
2. **PROCESS QUALIFICATION:** The grep/search output exposed Developer conclusions before test execution. This is a verification-process deviation, not a defect in the test's observed behavior.
3. **EVIDENCE LIMITATION:** The test is untracked and no prior source snapshot was available, so non-weakening is assessed by the retained observable assertions currently present, not by a textual diff against its previous version.
4. **No production change finding:** Production source files were already modified before this execution and remained unchanged in the before/after diff inspection; this Tester made no source changes.

## 10. Decisions and reasoning

- **VERIFIED:** The explicit validation barrier guarantees that both preliminary Client reads and version checks complete before either callback is released to invoke its mutation; latch synchronization, not elapsed-time assumptions, coordinates the writers.
- **VERIFIED:** The failed outcome is tested specifically for an optimistic-lock exception cause, and the focused test passes all three concurrency test methods.
- **VERIFIED (Developer comparison after verification):** Developer report `T2.6-EXEC-005-DEV` claims a shared-version validation barrier, no sleeps, strict optimistic-lock classification, winner-only persisted Document changes and unchanged same-Document/scalar assertions. The inspected source and this execution support those behavioral claims. No discrepancy was found in the code-behavior claims compared. **NOT AVAILABLE:** The Developer's separately reported 308-test full-suite run was not repeated in this follow-up; the task requested only the focused Maven command.
- **VERIFIED:** The strict independence procedure was qualified due to the early search excerpt exposure; this report does not claim an unqualified independent sequence.
- **VERIFIED:** No Tech Lead approval or final acceptance is claimed.

## 11. Final status

**PASS — WITH PROCESS QUALIFICATION.** The specified focused Maven test passed 3/3 and the seven requested test/source checks are satisfied by observed code and artifacts. The independence sequence was not strictly maintained because a search excerpt exposed Developer conclusions before the test run. Existing assertions are retained in the current source, but an exact before/after test-source diff is **NOT AVAILABLE**. This is not Reviewer approval or Human Tech Lead acceptance.

## 12. Outstanding issues

1. **VERIFIED — process:** Reviewer should note the early Developer-report excerpt exposure when assessing the independence of this verification.
2. **NOT AVAILABLE:** Exact source-level before/after comparison for the untracked test is unavailable; the current retained assertions and Developer report statement were inspected instead.
3. **NOT AVAILABLE:** PostgreSQL concurrency behavior was not tested in this focused run.
4. **PENDING APPROVAL:** Only the Human Tech Lead may make final acceptance decisions.

## 13. Handoff to next agent

- **VERIFIED:** The previous Tester report is archived at `agent-reports/T2.6/history/tester-001.md`; this primary report records execution `T2.6-EXEC-006-TEST`.
- **PENDING VERIFICATION:** Reviewer may independently audit the source and this report, accounting for the process qualification.
- **PENDING APPROVAL:** Human Tech Lead retains final acceptance authority. No source changes, governance edits, branch changes, commits, pushes, or self-approval were performed; stop after reporting.
