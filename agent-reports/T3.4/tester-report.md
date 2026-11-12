# Tester Report — T3.4 Independent Phase 3 Contract Regression

## 1. Task ID

- **VERIFIED** (direct tasking): `T3.4` — Independent Phase 3 contract regression testing.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T3.4-TEST-001`, 2026-11-12.
- **VERIFIED** (`agent-reports/README.md`, Section 6; task-directory search before writing): No prior `tester-report.md` or Tester history file existed for T3.4; no archive was required.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Tester; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking and `implementation-plan.md`, T3.4 / Phase 3 exit criteria): Independently inspect the implementation and tests, run focused affected security integration tests and the full Maven suite once, and report test evidence, findings, blockers and the comparison to the Developer report.
- **VERIFIED** (direct tasking): Do not modify production code or credential configuration, switch branches, manipulate any Git stash, commit, push, create a PR, merge, or approve/mark T3.4 complete. Preserve unrelated worktree changes.
- **VERIFIED** (execution sequence): Requirements, production implementation, existing Phase 2 tests and T3.4 security tests were inspected and the requested test runs completed before opening `agent-reports/T3.4/developer-report.md`.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`; `implementation-plan.md` (T3.4 and Phase 3 exit criteria); `pom.xml`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java`, `SecurityConfigurationIntegrationTest.java`, `SecurityProblemHandlersTest.java`, `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java`, `DocumentControllerTest.java`, `ClientAggregateUpdateHttpIntegrationTest.java`, and `src/test/java/com/humanit/challenge/config/StartupTestHarness.java`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java`, `SecurityProblemHandlers.java`, `SingleUserProvisioningConfiguration.java`, `src/main/java/com/humanit/challenge/controller/AuthController.java`, `ClientController.java`, `DocumentController.java`; `src/main/resources/application.yml`, `application-dev.yml`, and `application-prod.yml`.
- **VERIFIED** (after independent verification): `agent-reports/T3.4/developer-report.md` for comparison; focused and full-suite Surefire text reports under `target/surefire-reports/`; captured full Maven output at `C:\Users\pedro\.local\share\opencode\tool-output\tool_1cfb78ab700151Z0Ssn4dzaZ2f`.

## 6. Files changed

- **VERIFIED**: Only `agent-reports/T3.4/tester-report.md` was authored in this execution.
- **VERIFIED** (`git status --short --branch` before and after verification): The two security test files and `agent-reports/T3.4/` were already modified/untracked at the start of this Tester execution. They were preserved; this Tester did not modify them.
- **VERIFIED** (`git diff --name-only -- src/main/java src/main/resources pom.xml`): No production source, resource/configuration, or dependency changes were present. No credential configuration was changed by this Tester.

## 7. Commands executed

- **VERIFIED**: `git status --short --branch` — confirmed branch `feature/T3.4-security-regression` and recorded the pre-existing worktree changes.
- **VERIFIED**: `git diff -- src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java src/test/java/com/humanit/challenge/security/SecurityConfigurationIntegrationTest.java; git diff -- src/main/java src/main/resources pom.xml` — inspected the in-scope test changes and confirmed no corresponding production/configuration/dependency diff.
- **VERIFIED**: Initial attempted command `.\mvnw.cmd -Dtest=AuthControllerIntegrationTest,SecurityConfigurationIntegrationTest,SecurityProblemHandlersTest test` was rejected by PowerShell parsing (`Missing argument in parameter list`); Maven did not run for this attempt.
- **VERIFIED**: Focused command `.\mvnw.cmd '-Dtest=AuthControllerIntegrationTest,SecurityConfigurationIntegrationTest,SecurityProblemHandlersTest' test` — successful.
- **VERIFIED**: Full command `.\mvnw.cmd test` — run once and successful.
- **VERIFIED**: `git diff --name-only -- src/main/java src/main/resources pom.xml; git diff --check` — no production/configuration/dependency paths listed and no diff-check errors. Git printed line-ending conversion warnings for the pre-existing modified test files.
- **VERIFIED**: `git status --short --branch` — final check; no branch switch or stash command was run by this Tester.

## 8. Evidence and observed results

- **VERIFIED** (`AuthControllerIntegrationTest`, `StartupTestHarness`, focused test output/Surefire report): Login using the existing test-only demo username/password succeeds, returns a nonblank token with `Bearer` type, verifies as signed HS256 JWT with expected subject/expiry, and the issued token authorizes a protected Client list request.
- **VERIFIED** (`AuthControllerIntegrationTest`): An authenticated integration scenario exercises Client create/list/get/PUT/PATCH/delete and nested Document create/list/get/PUT/PATCH/delete through `MockMvc` with the application security filter chain. It asserts the established create/read/update/delete status categories and representative response fields; the current version is fed into subsequent body/header writes, and subsequent operations succeed. Post-delete gets return 404 with `application/problem+json`.
- **VERIFIED** (`SecurityConfigurationIntegrationTest`): Missing JWT is asserted as 401 with safe Problem Details for Client and nested Document lists. Different-signature and expired JWTs are rejected with 401 on a nested Document route. Invalid-token body checks exclude token and secret material. Public OpenAPI route and valid signed token access remain covered.
- **VERIFIED** (`SecurityProblemHandlersTest`): The existing access-denied handler test asserts 403, `application/problem+json`, generic safe content and absence of exception/token content. This verifies handler behavior directly; no artificial forbidden endpoint was introduced.
- **VERIFIED** (`SecurityConfiguration.java`, `SingleUserProvisioningConfiguration.java`, source search): Security rules permit the documented login/OpenAPI routes and require authentication otherwise; the route policy has no role/authority matcher. Single-user provisioning calls `User.withUsername(...).password(...).build()` without configuring roles/authorities. No artificial authorization rules were found.
- **VERIFIED** (focused Maven output and Surefire reports): Focused suite ran 10 tests total — 0 failures, 0 errors, 0 skipped (AuthControllerIntegrationTest 3; SecurityConfigurationIntegrationTest 6; SecurityProblemHandlersTest 1). Build succeeded.
- **VERIFIED** (full Maven output and Surefire reports): Full suite ran 332 tests — 0 failures, 0 errors, 0 skipped. Build succeeded in 53.342 seconds. Existing Phase 2 Client/Document controller, aggregate HTTP, service, optimistic concurrency and related regression tests passed as part of this suite.
- **VERIFIED** (captured full Maven console output): Warning markers matched 60 lines: one Maven `[WARNING]` for an HTTP 401 while transferring Flyway community-repository metadata, 55 runtime `WARN` log lines, and four JVM `WARNING:` lines. The runtime warnings include framework defaults and expected validation/fail-fast/unique-constraint test paths. The output also contains a Mockito inline-mock-maker self-attach advisory and a JVM class-sharing warning. These did not fail the Maven build or tests.
- **VERIFIED** (Developer report comparison): Developer reported 9 focused tests without `SecurityProblemHandlersTest`; this Tester command additionally included that existing safe-403 unit test and therefore reports 10. Both the Developer and Tester evidence record the same full-suite total and zero failures/errors/skips.
- **VERIFIED** (Developer report comparison): No material disagreement on successful login, CRUD filter-chain coverage, missing/invalid/expired token rejection, the existing safe-403 handler, absence of role rules, test-only change scope, or full-suite result.
- **VERIFIED** (independent source/test inspection; narrower test-strength observation): The authenticated CRUD scenario consumes returned `version` values for subsequent requests and demonstrates successful versioned writes, but does not explicitly assert the exact returned version values or increments. The Phase 2 tests provide broader version/contract assertions outside the security filter chain. This limits the precision of the new filter-chain-specific version regression evidence; it is not a test failure.
- **VERIFIED** (Developer report comparison): Developer report says the authenticated CRUD test checks “version headers/body” and that established response contract is checked. The test indeed sends the returned version values in required locations, but does not directly assert exact output versions/increments. This is recorded as a low-severity coverage qualification, not a discrepancy in observed test outcome.
- **NOT AVAILABLE**: Whether the Flyway metadata HTTP 401 is transient or requires repository authentication is not established by this test execution.
- **NOT AVAILABLE**: Reviewer audit and human Tech Lead acceptance were not performed or recorded by this Tester execution.

## 9. Findings and severity

| ID | Severity | Finding / disposition |
|---|---|---|
| T34-TST-01 | Low | **VERIFIED**: Authenticated CRUD exercises version-bearing requests and successful follow-up writes, but exact returned version values/increments are not explicitly asserted in the new security-chain scenario. Existing Phase 2 tests passed with broader contract assertions, though those controller tests disable filters. Consider adding explicit version assertions in a separately authorized test change if stronger filter-chain-specific version regression is required. No failure observed. |
| T34-TST-02 | Info | **VERIFIED**: Focused test command completed 10 tests, including the existing safe 403 handler test; all passed. This is one more focused test than the Developer's reported 9-test selection, with no conflicting result. |
| T34-TST-03 | Info | **VERIFIED**: Full suite passed 332 tests with zero failures, errors or skips. Maven emitted a repository metadata HTTP 401 warning and runtime/JVM warnings described in Section 8; they were non-fatal. |
| T34-TST-04 | Info | **VERIFIED**: No production, configuration, dependency, credential, or test file was changed by this Tester; unrelated worktree changes were preserved. |

## 10. Decisions and reasoning

- **VERIFIED**: Ran the security integration tests and complete Maven suite without modifying the implementation, existing tests, credentials, configuration, Git branch, or stash.
- **VERIFIED**: Included `SecurityProblemHandlersTest` in focused verification because it is the existing test for safe 403 handler behavior called out in the task.
- **VERIFIED**: Treated version exactness as a low-severity test coverage qualification because current version values are consumed by authenticated follow-up writes and the full Phase 2 test suite passed, but exact response-version transitions are not explicitly asserted in the security-chain test.
- **VERIFIED**: Compared the Developer report only after independent inspection and both requested test runs. No test failure/result discrepancy was found; the focused test selection differed by one included handler test.
- **VERIFIED**: This report does not grant acceptance, approve the work, or mark T3.4 complete.

## 11. Final status

**PASS — independent focused and full Maven verification succeeded; T3.4 is not approved or marked complete.** One low-severity coverage qualification remains regarding explicit version value assertions on the security-filtered CRUD path.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Reviewer must independently audit the implementation and test evidence.
- **PENDING APPROVAL**: Human Tech Lead alone decides final T3.4/Phase 3 acceptance; no such approval is recorded here.
- **VERIFIED**: Consider whether to strengthen the authenticated CRUD test with explicit exact version field/increment assertions; no changes were made because this execution was verification/reporting only.
- **NOT AVAILABLE**: Cause and remediation for the non-fatal Flyway community repository metadata HTTP 401 warning are not established.

## 13. Handoff to next agent

- **VERIFIED**: Independent findings and test evidence are recorded at `agent-reports/T3.4/tester-report.md`.
- **VERIFIED**: Branch remains `feature/T3.4-security-regression`; this Tester did not switch branches or run any stash operation. Existing test changes and untracked task report files were preserved.
- **PENDING VERIFICATION**: Reviewer should independently inspect the implementation and evidence before reading agent conclusions. Human Tech Lead alone decides acceptance.
