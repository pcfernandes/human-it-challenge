# Reviewer Report — T2.4 Part 1: Final Review of Fixes

## 1. Task ID

**VERIFIED (direct tasking):** T2.4 Part 1 — Final review of fixes for F-01, F-02 and F-03.

## 2. Execution identifier and date

**VERIFIED (direct tasking/environment):** `T2.4-EXEC-006-REV-FIX` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Reviewer; `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct tasking):** Review only the corrections for F-01, F-02 and F-03: global optimistic-lock mapping, expected-version validation, commit-boundary integration test, and updated `DocumentServiceTest`; independently inspect these before reading the latest Developer and Tester reports. No code changes, Maven execution, Git mutations or self-approval.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`.
- **VERIFIED (independent inspection before reports):** `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java`, `src/main/java/com/humanit/challenge/service/DocumentService.java`, `src/test/java/com/humanit/challenge/exception/CommitTimeOptimisticLockIntegrationTest.java`, and `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`.
- **VERIFIED (after independent inspection):** `agent-reports/T2.4/developer-report.md`, `agent-reports/T2.4/tester-report.md`, and the previous `agent-reports/T2.4/reviewer-report.md`.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.4/history/reviewer-001.md` — unchanged archive of the prior Reviewer report, created before replacing the primary report.
- **VERIFIED:** `agent-reports/T2.4/reviewer-report.md` — this execution's report.
- **VERIFIED:** No application code, tests, configuration or other agent reports were changed.

## 7. Commands executed

- **VERIFIED:** None. No shell/Git command and no Maven command was run. File inspection used project-relative paths.

## 8. Evidence and observed results

- **VERIFIED (`GlobalExceptionHandler.java:159-165`):** Spring `OptimisticLockingFailureException` (including `ObjectOptimisticLockingFailureException`) and Jakarta `OptimisticLockException` map to HTTP 409 Problem Details, with generic detail and `application/problem+json`; this handler is outside service-local exception catches.
- **VERIFIED (`CommitTimeOptimisticLockIntegrationTest.java:58-70,83-94`):** The test makes an HTTP request that calls `DocumentService.create`, registers a transaction `beforeCommit` callback which throws `ObjectOptimisticLockingFailureException`, asserts 409 and Problem Details status/title/media type, then verifies the created document is absent. This demonstrates dispatch and rollback for an injected commit-phase Spring exception.
- **VERIFIED (test limitation):** The callback injects/simulates an exception at commit time; it does **not** use competing database transactions or prove a real database/provider-generated race. This limitation is not represented as concurrency verification.
- **VERIFIED (`DocumentService.java:114-126,134-144`; `DocumentServiceTest.java:159-181`):** Client and Document version helpers reject null and negative expected values as 400 and compare non-negative values for mismatch (409). Parameterized test cases exercise missing/negative Client versions through create and Document versions through delete.
- **VERIFIED (`DocumentServiceTest.java:141-181`; `CommitTimeOptimisticLockIntegrationTest.java:58-70`):** Current focused additions assert stale-version 409 in existing service coverage, all four null/negative cases, and rollback/HTTP conflict in the commit-boundary fixture. They are targeted regression assertions for the prior findings.
- **VERIFIED (Developer/Tester reports, after independent inspection):** The Developer records focused test success (12 tests) and full-suite success; the Tester independently records focused test success (12 tests). This Reviewer did not execute Maven and treats those test outcomes as report evidence, not as a fresh run.
- **VERIFIED (comparison):** Developer and Tester both distinguish injected commit-phase failure from actual concurrent database transactions. Their conclusion that F-01 mapping/rollback and F-02 validation are addressed matches the source. Tester notes the missing direct runtime test for the Jakarta exception subtype; source mapping itself is verified. No disagreement affects the requested MVP corrections.
- **PENDING VERIFICATION:** Actual provider-generated optimistic-lock exceptions under real competing transactions were not exercised; no concrete evidence indicates they escape as an unmapped exception.

## 9. Findings and severity

- **F-01 — RESOLVED for the requested exception-mapping/commit-boundary behavior.** The global mapping covers both standard Spring and JPA optimistic-lock exception families, and the integration test verifies 409 plus persisted-state rollback for an injected exception at `beforeCommit`. **PENDING VERIFICATION:** A real competing-transaction/provider-generated failure was not run. This is a verification limitation, not a demonstrated remaining blocker for this correction.
- **F-02 — RESOLVED.** Shared service guards reject null and negative Client/Document versions with 400; focused tests assert the null/negative cases.
- **F-03 — ADEQUATE FOR MVP.** Focused tests directly exercise the newly corrected invalid-version behavior and the F-01 HTTP conflict/rollback path. Broader assertions mentioned in the previous report (such as every mutation's parent-version increment or PATCH omission) are not part of these fixes and are not elevated absent a concrete correctness defect.
- **Severity:** No outstanding Critical or Major finding. The real-concurrency distinction above remains **PENDING VERIFICATION**, but no demonstrated blocking issue remains.

## 10. Decisions and reasoning

- **VERIFIED:** Global exception mapping addresses commit-time optimistic-lock exceptions that occur after service-local catches; the injected callback test demonstrates the mapping and rollback boundary.
- **VERIFIED:** `beforeCommit` injection is not a real concurrent database transaction. It is sufficient for this scoped regression because it deterministically reaches the global mapping after the service operation and proves rollback; actual race reproduction remains distinct.
- **VERIFIED:** The shared expected-version service guards handle null and negative inputs before equality checks, and focused tests assert expected 400 status.
- **VERIFIED:** The remaining F-03 requests are test-coverage breadth rather than observed correctness defects; current focused assertions are enough for the MVP correction review.
- **VERIFIED:** No Tech Lead acceptance or approval is claimed. This review is not self-approval.

## 11. Final status

**PASS WITH FINDINGS** — F-01, F-02 and F-03 corrections are adequate for this scoped MVP review. Real concurrent provider behavior remains unverified; no demonstrated blocking issue remains. This is not Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** No real concurrent database transaction/provider-generated conflict was exercised; do not interpret the injected `beforeCommit` failure as concurrency evidence.
2. **VERIFIED:** No concrete blocking issue remains in the reviewed corrections. Broader F-03 coverage is not required for this handoff.
3. **NOT AVAILABLE:** No Tech Lead acceptance or approval is recorded or claimed.

## 13. Handoff to next agent

- **VERIFIED:** Ready for DocumentController: **YES**. F-01 is covered at the exception/transaction boundary, F-02 validation is present, and F-03 focused assertions suffice for this MVP correction review.
- **PENDING VERIFICATION:** Review DocumentController endpoint bindings and HTTP contracts in its own authorized task. Final acceptance belongs only to the human Tech Lead.
