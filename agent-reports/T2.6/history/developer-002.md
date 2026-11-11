# Developer Report — T2.6 Concurrency Test Follow-up

## 1. Task ID

- **VERIFIED** (direct tasking): `T2.6` — strengthen only the different-Document concurrent-writer test to prove overlapping optimistic transactions.

## 2. Execution identifier and date

- **VERIFIED** (direct tasking): `T2.6-EXEC-005-DEV`.
- **VERIFIED** (environment): 2026-11-11.

## 3. Agent role and model

- **VERIFIED:** Developer role; model `opencode-go/gpt-6-luna` (`gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Address Reviewer Minor #1 only in `OptimisticConcurrencyIntegrationTest.java`; do not modify production code or address Reviewer Minor #2.
- **VERIFIED** (direct tasking): Preserve unrelated worktree changes; do not switch branches, commit, push, merge, rebase, or change governance/requirements. Run the focused concurrency test and `.\mvnw.cmd -B clean test`, report, then stop.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md` and `agent-reports/T2.6/reviewer-report.md` (Minor #1 synchronization gap and report format/history conventions).
- **VERIFIED:** Prior `agent-reports/T2.6/developer-report.md`; `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`.
- **VERIFIED:** Relevant implementation files `src/main/java/com/humanit/challenge/service/ClientService.java`, `DocumentService.java`, `src/main/java/com/humanit/challenge/repository/ClientRepository.java`, and `src/main/java/com/humanit/challenge/domain/Client.java`.
- **VERIFIED:** `pom.xml`; final focused Surefire result `target/surefire-reports/com.humanit.challenge.service.OptimisticConcurrencyIntegrationTest.txt`.

## 6. Files changed

- **VERIFIED:** `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java` — test-only deterministic synchronization and stronger winner/conflict/persistence assertions.
- **VERIFIED:** `agent-reports/T2.6/developer-report.md` — this execution report.
- **VERIFIED:** `agent-reports/T2.6/history/developer-001.md` — prior primary Developer report archived before replacement; its prior execution identifier/content are retained.
- **VERIFIED:** No production, requirements, governance, decision-log, or other agent-report file was changed by this execution. Existing unrelated worktree changes were preserved.

## 7. Commands executed

- **VERIFIED:** `.\mvnw.cmd -B -Dtest=OptimisticConcurrencyIntegrationTest test` — first exploratory run failed while evaluating an attempted repository-spy synchronization hook; the validation latch timed out. This was discarded and is not a final result.
- **VERIFIED:** `.\mvnw.cmd -B -Dtest=OptimisticConcurrencyIntegrationTest test` — next exploratory run failed because both writers succeeded when the initial transaction reads were synchronized but the persistence context was retained through service execution. This exposed the need to clear that test-side read state before invoking the actual update path.
- **VERIFIED:** `.\mvnw.cmd -B -Dtest=OptimisticConcurrencyIntegrationTest test` — passed after adding `EntityManager.clear()` after the validation barrier.
- **VERIFIED (final focused run):** `.\mvnw.cmd -B -Dtest=OptimisticConcurrencyIntegrationTest test` — BUILD SUCCESS; 3 tests, 0 failures, 0 errors, 0 skipped. The final run includes the strict optimistic-lock exception assertion.
- **VERIFIED (required full-suite run):** `.\mvnw.cmd -B clean test` — BUILD SUCCESS; 308 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED:** `git status --short`; `git diff --check`; `git diff --no-index -- agent-reports/T2.6/developer-report.md agent-reports/T2.6/history/developer-001.md` (before replacing the primary) were used to inspect/preserve the worktree and archive. No Git mutation was performed.

## 8. Evidence and observed results

- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java`, different-Document test and `compete` helper): Both worker transactions use the existing two-worker `ExecutorService` and `TransactionTemplate`. A test-only `ValidationGate` uses `CountDownLatch(2)` (`validatedWriters`) and `CountDownLatch(1)` (`release`). After the common start latch opens, each transaction loads the Client with `clientRepository.findById`, records its actual loaded version, and checks it equals the fixture's expected version. Each then signals `validatedWriters` and blocks on `release` while its transaction remains open; therefore neither callback can finish or commit before both version checks complete.
- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java:182-188`): The test thread awaits both validations, checks that both recorded versions exactly equal the same fixture version, then counts down `release` so both writers proceed. Each worker clears its transaction-bound persistence context after release and before invoking its corresponding service update, ensuring the test's preliminary read does not substitute for the service's normal locked-load path.
- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java`, `assertOneWinnerAndOptimisticLockConflict`): The different-Document case requires exactly two outcomes, exactly one success, and an `OptimisticLockingFailureException` or JPA `OptimisticLockException` in the losing failure's cause chain. A bare stale-version HTTP conflict is not accepted as evidence for this assertion.
- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java`, persistence assertions): After both transactions finish, reloaded Documents must show exactly one writer's identifying number; the winning Document's number, description and expiration date are checked, while the losing target retains its initial number and description. The existing assertion that the persisted Client version advanced remains in place. Existing same-Document and scalar-writer assertions were not weakened or modified.
- **VERIFIED** (focused Maven output and Surefire report): Final focused run passed all 3 tests (0 failures/errors/skips).
- **VERIFIED** (full Maven output): `.\mvnw.cmd -B clean test` passed all 308 tests (0 failures/errors/skips).
- **VERIFIED** (worktree status observed before reporting): Pre-existing changes to `agent-reports/decisions.md`, Client production files, other Client/Document tests, governance reconstruction folders, and the untracked T2.6 artifacts remained present and were not edited by this execution.

## 9. Findings and severity

1. **VERIFIED — Reviewer Minor #1 addressed in test scope:** The different-Document concurrent-writer test now has an explicit two-transaction version-validation barrier, a simultaneous release, an optimistic-lock-specific losing-outcome assertion, and per-Document persistence checks.
2. **VERIFIED — Not addressed, per authorization:** Reviewer Minor #2 (unnecessary post-transaction read for a no-op nested PATCH) remains outside this execution's scope.
3. **VERIFIED:** No production changes were made. No new dependency or sleep was introduced.

## 10. Decisions and reasoning

- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java`): Used only test-side synchronization; no test hooks or production changes were needed. The transaction callbacks explicitly observe/check the same Client version before either can leave the callback and commit.
- **VERIFIED** (focused test observations): Clearing the persistence context after releasing the barrier is required for the service call to perform its usual locked-load operation instead of reusing the setup read. The exploratory run without this clear let both writers succeed; that version was not retained.
- **VERIFIED** (final test assertions): Tightened only the target test's failed-writer classification to require an optimistic-lock exception type in the cause chain, rather than allowing any HTTP 409 to stand in for the requested optimistic conflict. Existing assertions for the other concurrency cases were preserved.
- **VERIFIED** (`@SpringBootTest` test configuration): Concurrency verification continues to use H2. This task did not add PostgreSQL verification or alter production transaction behavior.

## 11. Final status

**VERIFIED — IMPLEMENTED AND TESTED.** The final focused test passed (3/3) and `.\mvnw.cmd -B clean test` passed (308/308). The specified concurrency test now checks shared initial version observation, one successful transaction, one optimistic locking conflict, and only the winner's persisted Document change. **Ready for independent Tester: YES.** This is not Reviewer approval or Human Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Independent Tester verification and subsequent Reviewer audit remain outstanding. The Tester must verify independently before reading Developer conclusions; the Reviewer must audit independently before reading role reports.
2. **VERIFIED — scope boundary:** Reviewer Minor #2 was not addressed, as explicitly instructed.
3. **VERIFIED — test environment limitation:** This concurrency test uses H2; PostgreSQL concurrency behavior was not verified in this execution.
4. **PENDING APPROVAL:** Only the Human Tech Lead may determine final acceptance. No approval is claimed.

## 13. Handoff to next agent

- **VERIFIED:** Updated primary report: `agent-reports/T2.6/developer-report.md`; previous report archived at `agent-reports/T2.6/history/developer-001.md`.
- **VERIFIED:** Ready for independent Tester: **YES**. Do not read this report before completing the Tester’s independent verification. The Reviewer should likewise complete the independent audit before reading role reports.
- **PENDING APPROVAL:** Human Tech Lead retains final acceptance authority. No commit, push, merge, rebase or branch switch was performed. Stop after reporting.
