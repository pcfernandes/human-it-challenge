# Tester Report — T3.3 Independent Login and JWT Verification

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 3, T3.3; direct tasking): `T3.3` — independently verify login and JWT behavior, focused and full tests, regressions, and scope limits.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T3.3-TEST-001`, 2026-11-12.
- **VERIFIED** (`agent-reports/README.md`, Section 6; workspace search): No earlier `agent-reports/T3.3/tester-report.md` or Tester history file was present, so no prior Tester report required archiving.

## 3. Agent role and model

- **VERIFIED** (execution environment): Senior Java/Spring Boot QA engineer / Tester; model `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct tasking and `implementation-plan.md`, Phase 3, T3.3): Independently inspect the login implementation before the Developer report, run focused T3.3 tests and the full Maven suite once, verify the listed security/contract/regression requirements, compare findings with the Developer report afterward, and report without approval or completion claims.
- **VERIFIED** (direct tasking): No production-code edits, unnecessary tests/refactoring, commits, pushes, merges, or branch switching; preserve unrelated worktree changes.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): Use the required thirteen-section report structure, evidence markers, and report-history convention; author only this Tester report.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`; `implementation-plan.md` (Phase 3, T3.1–T3.3 and Phase 3 exit criteria); initial `git status --short` / `git diff --stat` output.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/AuthController.java`; `src/main/java/com/humanit/challenge/dto/auth/LoginRequest.java`; `src/main/java/com/humanit/challenge/dto/auth/TokenResponse.java`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java`; `SingleUserProvisioningConfiguration.java`; `SecurityProblemHandlers.java`; `src/main/java/com/humanit/challenge/config/AppJwtProperties.java`; `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java`; `SecurityConfigurationIntegrationTest.java`; `SecurityProblemHandlersTest.java`; `src/test/java/com/humanit/challenge/config/StartupTestHarness.java`; `pom.xml`.
- **VERIFIED** (after independent source inspection and test execution): `agent-reports/T3.3/developer-report.md`, read only for post-verification comparison.
- **VERIFIED**: `target/surefire-reports/com.humanit.challenge.security.AuthControllerIntegrationTest.txt`.

## 6. Files changed

- **VERIFIED**: Only `agent-reports/T3.3/tester-report.md` was authored by this Tester execution. No production or test source was modified; no tests were added because no material uncovered behavior was identified that justified a test edit.
- **VERIFIED**: No git branch operation, commit, push, merge, staging, or unrelated-worktree edit was performed during this Tester execution.

## 7. Commands executed

- **VERIFIED**: `git status --short; git diff --stat` — recorded the initial worktree state; it contained modified/untracked implementation and unrelated report paths. Those were preserved.
- **VERIFIED**: `.\mvnw.cmd -Dtest=AuthControllerIntegrationTest test` — focused login integration test run; 2 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- **VERIFIED**: `.\mvnw.cmd test` — full Maven suite, run once; 331 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- **VERIFIED**: `.\mvnw.cmd "-Dtest=AuthControllerIntegrationTest,SecurityConfigurationIntegrationTest,SecurityProblemHandlersTest" test` — focused login/security/regression run; 9 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- **VERIFIED**: Read operations and searches using the workspace tools for the files listed in Section 5; no other command altered the worktree.

## 8. Evidence and observed results

- **VERIFIED** (`SecurityConfiguration.java`, lines 47–50): `/auth/login` is explicitly `permitAll()` and the remaining routes require authentication. **VERIFIED** (`AuthControllerIntegrationTest`, successful MockMvc POST): valid unauthenticated login returned HTTP 200, directly confirming the endpoint is publicly accessible.
- **VERIFIED** (`AuthController.java`, lines 48–63; `SecurityConfiguration.java`, lines 59–71): credentials are passed to the existing `AuthenticationManager`; the endpoint uses the configured `JwtEncoder`, explicitly requests `MacAlgorithm.HS256`, sets the authenticated name as `sub`, sets `iat`, and computes `exp` from `AppJwtProperties.expirationMinutes()`.
- **VERIFIED** (`AuthControllerIntegrationTest`, successful-login test): the application `JwtDecoder` decoded the issued token; assertions checked `alg=HS256`, expected subject, non-null issued-at, expiration exactly the configured interval after issued-at, and expiration in the future. Successful decoding with the configured decoder verifies the issued token's signature and timestamp validity.
- **VERIFIED** (`SecurityConfiguration.java`, lines 65–71; `SecurityConfigurationIntegrationTest`): decoder is restricted to HS256 and has `JwtTimestampValidator`; existing tests verify acceptance of valid signed tokens and rejection of a different-secret signature and expired token. The issued login token also successfully accessed `GET /api/clients` with a bearer header.
- **VERIFIED** (`AuthController.java`, lines 31, 45–52; `AuthControllerIntegrationTest`, invalid-credentials test): bad password and unknown username both returned HTTP 401 and the same generic `Invalid username or password.` detail; bodies excluded the tested usernames/passwords and BCrypt hash. Login authentication failures are caught without returning exception detail.
- **VERIFIED** (`TokenResponse.java`; `AuthController.java`, lines 61–63): successful login response DTO contains only `accessToken` and `tokenType`, with token type `Bearer`; the token is the intended login response value. No password or hash field is defined in the response DTO. The login controller contains no credential/token logging.
- **VERIFIED** (`SecurityConfigurationIntegrationTest` and `SecurityProblemHandlersTest`): existing T3.1 regression tests cover no-session behavior, safe unauthenticated and invalid/expired-token responses, public OpenAPI access and safe 403 handling. Existing credential tests continue to verify BCrypt-backed credential behavior for T3.2.
- **VERIFIED** (`AuthController.java`, `pom.xml`, and controller/security route source inspected): T3.3 adds one login route and no registration/account-management/refresh-token flow, role/permission behavior, or dependency change. Credential provisioning remains the existing one-user configuration.
- **VERIFIED** (Maven output): standalone `AuthControllerIntegrationTest` ran 2/2; the related focused run ran 9/9; the one full Maven run ran 331/331. All reported 0 failures, 0 errors and 0 skipped tests.
- **VERIFIED** (Maven output): Maven printed a Flyway GitHub metadata retrieval HTTP 401 warning and Java/Mockito agent warnings during test execution; none prevented compilation or test success. No application password, hash, or JWT appeared in the observed login test log output.
- **RECONSTRUCTED** (source and test coverage): No separate integration assertion captures every possible application/runtime log configuration. The relevant login code contains no logging call, auth failures are caught and mapped generically, and the observed test output did not expose tested credentials or tokens; this is code/output evidence, not a guarantee about external deployment log collectors.

## 9. Findings and severity

| ID | Severity | Finding / evidence |
|---|---|---|
| T33-TEST-01 | Info | **VERIFIED** (Maven output): Maven logged HTTP 401 while attempting Flyway remote metadata discovery. Focused and full builds nevertheless completed successfully; no build blocker found. |
| T33-TEST-02 | None | **VERIFIED** (inspected implementation and test results): No material T3.3 login/JWT defect or T3.1/T3.2 regression was observed in the requested checks. This is not a Reviewer audit or Tech Lead acceptance. |

## 10. Decisions and reasoning

- **VERIFIED**: No tests were added or modified. Existing integration tests plus source inspection covered the identified material T3.3 behaviors, and the focused and full suites passed.
- **VERIFIED**: Verification included both the login integration test alone and the 9-test login/security focus so that existing token rejection and T3.1/T3.2 regression checks were exercised. The full Maven suite was run once as requested.
- **VERIFIED** (comparison with Developer report after independent verification): The Developer's reported 9-test focused run and 331-test full run are consistent with this execution's corresponding results. The first, login-class-only run in this execution was 2 tests and also passed. No substantive discrepancy in test outcomes was found.
- **VERIFIED** (Developer report comparison): Developer report records branch creation before this Tester execution. This Tester did not switch branches or perform any branch operation; implementation and unrelated worktree state were left untouched.
- **VERIFIED**: Passing tests indicate verification success only. Per task instruction, no approval or task-completion decision is made here.

## 11. Final status

**PASS — independent T3.3 verification and requested test runs passed. T3.3 is not approved or marked complete.** The focused 9-test run and full 331-test run both passed with no failures, errors, or skips. Reviewer audit and human Tech Lead acceptance remain outstanding.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Independent Reviewer audit remains required by `implementation-plan.md` and `agent-reports/README.md`.
- **PENDING VERIFICATION**: Human Tech Lead acceptance is not established by this report and remains outstanding; this Tester does not approve or mark T3.3 complete.
- **VERIFIED**: Maven metadata 401 and runtime warnings were non-blocking in both requested verification paths.
- **RECONSTRUCTED**: Runtime logging safety evidence is limited to code inspection of login/error paths and observed test output; an external production logging pipeline was not available or exercised.

## 13. Handoff to next agent

- **VERIFIED**: Tester report path: `agent-reports/T3.3/tester-report.md`.
- **VERIFIED**: Focused related security tests: 9 passed, 0 failures/errors/skips. Full Maven suite: 331 passed, 0 failures/errors/skips.
- **PENDING VERIFICATION**: Reviewer to independently audit the code and requirements before reading this or the Developer report, as required by the reporting workflow.
- **PENDING VERIFICATION**: Human Tech Lead to make the final acceptance decision; no completion or approval is claimed here.
