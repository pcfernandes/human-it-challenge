# Tester Report — T2.4 Part 1: Reviewer Fixes

## 1. Task ID

**VERIFIED (direct tasking):** T2.4 Part 1 — verify fixes for F-01, F-02 and F-03.

## 2. Execution identifier and date

**VERIFIED (direct tasking/environment):** `T2.4-EXEC-005-TEST-FIX` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Tester; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct tasking):** Independently inspect the exception handler, DocumentService, and requested regression tests; run only `DocumentServiceTest` and `CommitTimeOptimisticLockIntegrationTest`; then read and compare the Developer fix report. Do not modify application/tests, run the full suite, mutate Git, or self-approve.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` and `src/main/java/com/humanit/challenge/service/DocumentService.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/exception/CommitTimeOptimisticLockIntegrationTest.java` and `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`.
- **VERIFIED:** `agent-reports/T2.4/developer-report.md`, read only after independent source inspection and the focused test run.
- **VERIFIED (after verification):** Existing `agent-reports/T2.4/tester-report.md` was archived unchanged as `agent-reports/T2.4/history/tester-001.md` before replacing the primary report.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.4/history/tester-001.md` — archival copy of the previous Tester report.
- **VERIFIED:** `agent-reports/T2.4/tester-report.md` — this execution's report.
- **VERIFIED:** No application code or test files were changed by this execution.

## 7. Commands executed

- **VERIFIED:** `.\mvnw.cmd -B '-Dtest=DocumentServiceTest,CommitTimeOptimisticLockIntegrationTest' test` — completed successfully; details in Section 8. No full Maven suite was run.
- **VERIFIED:** `git status --short; git diff -- src/main src/test` — read-only inspection showed the tracked production diff only in `GlobalExceptionHandler.java`; status also showed the pre-existing T2.4 `DocumentService.java` and test files as untracked. No Git mutation was performed.

## 8. Evidence and observed results

- **VERIFIED (`GlobalExceptionHandler.java:159-166`):** `@ExceptionHandler` explicitly covers Spring `OptimisticLockingFailureException` (including its `ObjectOptimisticLockingFailureException` subtype) and Jakarta Persistence `OptimisticLockException`; both are mapped to HTTP 409 with `application/problem+json`, a `ProblemDetail` title of `Conflict`, and generic detail.
- **VERIFIED (`CommitTimeOptimisticLockIntegrationTest.java:83-93`):** The fixture calls `DocumentService.create`, then registers a transaction synchronization that throws `ObjectOptimisticLockingFailureException` from `beforeCommit`. Spring invokes this commit callback after the annotated controller method body returns. The request therefore exercises exception dispatch outside the completed service body and at the transaction commit boundary.
- **VERIFIED (integration test source and focused run):** The integration test asserts 409, Problem Details media type, status 409 and title `Conflict`; it then asserts the created document number is absent from the repository. The latter is an observable rollback assertion.
- **VERIFIED (test limitation):** This is a deliberately injected, simulated commit-phase exception. It does **not** run two concurrent database transactions or demonstrate a real database/provider-generated race. The selected run also does not directly execute the JPA `OptimisticLockException` handler path; that mapping is confirmed by source inspection only.
- **VERIFIED (`DocumentService.java:114-143`; `DocumentServiceTest.java:159-181`):** Null and negative expected Client versions and null and negative Document versions each produce `ResponseStatusException` with status 400 in the service tests. Client cases use create; Document cases use delete.
- **VERIFIED (focused Maven output):** `CommitTimeOptimisticLockIntegrationTest`: 1 test, 0 failures/errors/skips. `DocumentServiceTest`: 11 tests, 0 failures/errors/skips. Total: 12 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`. Maven emitted a metadata HTTP 401 warning for an unrelated Flyway community repository; it did not prevent the run.
- **VERIFIED (working-tree inspection):** The tracked production patch adds only the optimistic-lock handler to `GlobalExceptionHandler.java`. `DocumentService.java` is untracked in the current Git state; the earlier T2.4 Developer execution report records it as the previously implemented service file, and this execution's inspected code contains the expected negative-version guards. As an untracked file, its exact delta against HEAD cannot be established by `git diff`.
- **VERIFIED (Developer comparison):** The Developer report's claims about commit callback timing, rollback assertion, the four invalid-version cases, and the 12-test focused rerun match the source and output inspected here. The Developer's full-suite result was not rerun or independently verified because this task explicitly prohibits running the full suite. No substantive disagreement was found on the requested fixes.

## 9. Findings and severity

- **F-01 — PASS WITH FINDINGS (minor coverage limitation):** Handler source maps both requested Spring/JPA exception families to 409 Problem Details. The passing integration test verifies the Spring translated exception at the commit boundary and rollback. The commit-phase failure is simulated, not a real concurrent database conflict, and the JPA-specific exception type is not directly exercised by the requested test selection.
- **F-02 — PASS:** Focused service regression assertions verify null and negative Client and Document versions yield 400. The assertions inspect service-raised status exceptions; this run did not verify an HTTP endpoint for these errors (the project has no DocumentController in the inspected T2.4 implementation).
- **F-03 — PASS:** Focused assertions verify conflict response status/media type/title and persisted-state rollback; the invalid-version parameterized cases assert 400 for all four null/negative inputs.
- **Unrelated production changes — no unrelated delta observed, with baseline limitation:** The tracked production diff is limited to the requested exception mapping. The untracked DocumentService is an earlier T2.4 implementation file and includes the requested version checks, but its complete change delta is not derivable from Git diff while untracked.

## 10. Decisions and reasoning

- **VERIFIED:** The `beforeCommit` injection is sufficient to verify global exception mapping after the service body returns, but is not evidence of concurrent database behavior; these claims are kept separate.
- **VERIFIED:** Status is **PASS WITH FINDINGS** because all selected tests pass and the requested behavior is present, while real concurrency, direct JPA exception execution, and an exact Git baseline for the untracked service remain unverified.
- **VERIFIED:** No Tech Lead approval or final acceptance is claimed.

## 11. Final status

**PASS WITH FINDINGS.** F-01: verified with simulation/JPA-test limitations. F-02: verified at service status-exception level. F-03: verified. Focused tests: 12 passed, 0 failures/errors/skips.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** A real concurrent provider/database conflict has not been exercised; the supplied integration test deliberately injects a Spring optimistic-lock exception at `beforeCommit`.
2. **PENDING VERIFICATION:** No direct runtime regression assertion for the Jakarta Persistence `OptimisticLockException` mapping was executed in the requested test selection.
3. **PENDING VERIFICATION:** Exact production delta of untracked `DocumentService.java` cannot be compared to HEAD using the observed Git diff.
4. **NOT AVAILABLE:** No final Tech Lead acceptance is recorded or claimed.

## 13. Handoff to next agent

- **VERIFIED:** Ready for Reviewer: **YES**. Reviewer should independently audit this execution and preserve the distinction between simulated commit-time failure and real concurrent transactions.
- **PENDING VERIFICATION:** Final acceptance belongs only to the human Tech Lead.
