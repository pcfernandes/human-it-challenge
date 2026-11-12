# Developer Report — T3.3 Login API and JWT issuance

## 1. Task ID

- **VERIFIED** (direct Tech Lead tasking and `implementation-plan.md`, Phase 3, T3.3): `T3.3` — implement `POST /auth/login`, authenticate against the existing account source, and issue the configured signed JWT response.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T3.3-DEV-001`, 2026-11-12.
- **VERIFIED** (`agent-reports/README.md`, Section 6; workspace search): No prior `agent-reports/T3.3/developer-report.md` existed, so there was no report to archive before creating this primary report.

## 3. Agent role and model

- **VERIFIED** (execution environment): Senior Java/Spring Boot Developer; model `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct Tech Lead tasking): Implement only the login endpoint and token contract: use the existing `AuthenticationManager`, issue JWT using existing `JwtEncoder`, explicitly set HS256, include subject/issued-at/expiration, use external JWT configuration, return `accessToken` and `tokenType: Bearer`, and respond with generic 401 for invalid credentials.
- **VERIFIED** (direct Tech Lead tasking and `implementation-plan.md`, Phase 3, T3.3): No registration, user management, roles, refresh tokens, added dependencies or unnecessary abstractions; do not log passwords, hashes or tokens; run focused tests and the full Maven suite once; no commit, push, PR or merge; do not self-approve.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): Use the mandatory thirteen-section report format, evidence markers and execution-history convention; author only this Developer report.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`, `implementation-plan.md` (Phase 3, T3.3), `agent-reports/T3.1/reviewer-report.md` (including T31-REV-M01), and T3.2 Developer/Reviewer reports.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java`, `SingleUserProvisioningConfiguration.java`, `config/AppJwtProperties.java`, `exception/GlobalExceptionHandler.java`, `controller/ClientController.java`, and `dto/client/ClientCreateRequest.java`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/security/SecurityConfigurationIntegrationTest.java`, `config/StartupTestHarness.java`, `config/SourceScanAuditTest.java`, and `pom.xml`.
- **VERIFIED** (`git status`, branch and remote inspection): Existing unrelated worktree state included modified `agent-reports/decisions.md` and untracked governance/performance report directories. Those paths were preserved and are not part of this task's changes.

## 6. Files changed

- **VERIFIED** (`src/main/java/com/humanit/challenge/controller/AuthController.java`): Added `POST /auth/login`; delegates credential verification to the existing `AuthenticationManager`, catches authentication failures as the same generic unauthorized response, and issues claims using the existing `JwtEncoder` and `AppJwtProperties` expiration.
- **VERIFIED** (`src/main/java/com/humanit/challenge/dto/auth/LoginRequest.java`): Added minimal validated username/password request record.
- **VERIFIED** (`src/main/java/com/humanit/challenge/dto/auth/TokenResponse.java`): Added minimal `accessToken`/`tokenType` response record.
- **VERIFIED** (`src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java`): Added integration tests for successful login, HS256 decoding/signature validation, subject and expiration claims, protected endpoint access, and indistinguishable generic 401 response for bad password versus unknown username.
- **VERIFIED** (`src/test/java/com/humanit/challenge/security/SecurityConfigurationIntegrationTest.java`): Updated the public OpenAPI route test and removed its obsolete expectation that login is not implemented.
- **VERIFIED** (`agent-reports/T3.3/developer-report.md`): Created this required execution report; no other agent report or decision log was changed.
- **VERIFIED** (`git status --short --branch`): Pre-existing `agent-reports/decisions.md` modification and unrelated untracked report directories remain present; they were not authored or changed by this task. No dependency, configuration, plan or other agent report was changed.
- **VERIFIED**: No commit, push, PR, merge or application branch switch after feature branch creation was performed.

## 7. Commands executed

- **VERIFIED**: `git status --short --branch`, `git branch --show-current`, `git branch -vv`, `git remote -v`, `git status --porcelain=v1`, and `git log -1 --oneline --decorate` — inspected initial Git state and current branch.
- **VERIFIED**: `git fetch origin` — refreshed remote references; `origin/main` advanced to `232db57d2b7a05464bd9fda97c049410189dc1a3`.
- **VERIFIED**: `git log --oneline --decorate --graph -15 origin/main`; `git branch -r --contains bf2da5399301b3b9487984246efffd1eb6369e23`; `git merge-base --is-ancestor bf2da5399301b3b9487984246efffd1eb6369e23 origin/main` — verified the T3.2 implementation commit is contained in `origin/main` (merge PR #15).
- **VERIFIED**: `git diff --quiet HEAD origin/main` — confirmed local tracked tree matched fetched `origin/main` before branch creation; unrelated dirty/untracked work was preserved.
- **VERIFIED**: `git switch -c feature/T3.3-jwt-login origin/main` — created the requested feature branch directly from `origin/main`.
- **VERIFIED**: `git status --short --branch`, `git diff --check`, and `git diff --stat` — reviewed working tree/whitespace. Git reported line-ending conversion warnings for the existing decisions file and modified security test; no whitespace errors were reported.
- **VERIFIED**: `git merge-base --short HEAD origin/main` — this inspection command was invalid because `git merge-base` does not support `--short`; it did not change Git state. The branch-starting-point evidence is the preceding branch creation output, `HEAD` SHA, and `origin/main` comparison.
- **VERIFIED**: `git rev-parse --short HEAD`; `git log -1 --oneline --decorate`; `git diff --name-only`; `git status --porcelain=v1` — final branch/status inspection. No commit was created.
- **VERIFIED**: `.\mvnw.cmd "-Dtest=AuthControllerIntegrationTest,SecurityConfigurationIntegrationTest,SecurityProblemHandlersTest" test` — focused tests passed, 9 tests total.
- **VERIFIED**: `.\mvnw.cmd test` — full Maven suite passed once, 331 tests total.

## 8. Evidence and observed results

- **VERIFIED** (`origin/main` graph and ancestry command output): T3.2 commit `bf2da53` is included in merge commit `232db57` on `origin/main`. Local tracked files matched fetched `origin/main`; branch `feature/T3.3-jwt-login` was created at that base SHA.
- **VERIFIED** (`AuthController.java`): Authentication uses `UsernamePasswordAuthenticationToken.unauthenticated(...)` through the configured `AuthenticationManager`. On any `AuthenticationException`, the endpoint returns a single generic `401` reason, `Invalid username or password.`; no exception detail or credentials are included in the response.
- **VERIFIED** (`AuthController.java`): Token claims contain authenticated principal subject, second-precision issued-at and expiration computed from external `AppJwtProperties.expirationMinutes()`. The `JwsHeader` is explicitly created with `MacAlgorithm.HS256`; response fields are `accessToken` and `tokenType` set to `Bearer`.
- **VERIFIED** (`SecurityConfiguration.java`): The existing encoder uses the external Base64 JWT secret; existing decoder is pinned to HS256 and performs timestamp validation. The implementation adds no JWT dependency and does not add credential/token logging.
- **VERIFIED** (`AuthControllerIntegrationTest.java`; focused test output): Successful login returned JSON containing Bearer token type; the existing decoder successfully decoded the issued token (therefore verifying its signature and non-expired timestamp), test assertions verified HS256, subject, issued-at and expiration interval, and the issued token accessed `GET /api/clients` successfully.
- **VERIFIED** (`AuthControllerIntegrationTest.java`; focused test output): Invalid password and unknown username both returned HTTP 401 with the same generic detail. Response bodies did not contain the tested username/password/hash values.
- **VERIFIED** (focused Maven command output): 9 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- **VERIFIED** (full Maven command output): 331 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- **VERIFIED** (Maven output): Maven printed an HTTP 401 warning while retrieving auto-discovery metadata for a Flyway GitHub Maven registry; compilation/tests still completed successfully. Existing deprecation and dynamic Java-agent warnings were also printed.

## 9. Findings and severity

| ID | Severity | Finding / evidence |
|---|---|---|
| T33-DEV-01 | Info | **VERIFIED** (Maven output): Remote metadata retrieval from the Flyway GitHub registry logged HTTP 401 during Maven prefix discovery, but both requested test runs completed successfully. This did not block build/test execution. |
| T33-DEV-02 | Pending | **PENDING VERIFICATION**: Independent Tester verification, independent Reviewer audit, and human Tech Lead acceptance have not been performed by this Developer execution. |

- **VERIFIED**: No implementation-blocking issue was observed in the requested focused or full test runs. This is not an independent Reviewer conclusion or acceptance decision.

## 10. Decisions and reasoning

- **VERIFIED**: Kept token issuance in a minimal controller with two small DTO records; no service layer or additional abstractions were added for this single endpoint.
- **VERIFIED**: Reused existing `AuthenticationManager`, `JwtEncoder`, `AppJwtProperties`, external secret, and configured expiration instead of introducing security infrastructure or dependencies.
- **VERIFIED**: Explicitly supplies HS256 in the JWS header to address the T3.1 Reviewer follow-up T31-REV-M01; integration coverage inspects the issued algorithm and uses the configured decoder to verify the token.
- **VERIFIED**: Converts all Spring Security authentication failures to one generic 401 message, avoiding user-existence/password-mismatch differentiation. No credential or token logging was added.
- **VERIFIED**: Test-only dynamic properties use the existing dummy credential fixture in `StartupTestHarness`; the test confirms both invalid-password and unknown-user responses.
- **VERIFIED**: Preserved pre-existing unrelated worktree files and did not stage, commit, push, merge or create a PR.

## 11. Final status

**IMPLEMENTATION AND REQUESTED TEST RUNS FINISHED — T3.3 NOT ACCEPTED OR MARKED COMPLETE.** The endpoint and tests are implemented, the focused suite passed 9 tests and the full Maven suite passed 331 tests. Independent Tester verification, Reviewer audit and human Tech Lead acceptance remain outstanding. No commit, push, PR or merge was made.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Tester to independently verify the T3.3 implementation and tests before reading this Developer report, per `agent-reports/README.md`.
- **PENDING VERIFICATION**: Reviewer to independently audit implementation before reading Architect, Developer or Tester conclusions, per `agent-reports/README.md`.
- **PENDING VERIFICATION**: Human Tech Lead acceptance is not available and remains the only authority for accepting T3.3. No approval or completion is claimed.
- **VERIFIED**: Maven metadata HTTP 401 warning did not prevent either requested build/test run from passing; no dependency change was needed.
- **VERIFIED**: Pre-existing unrelated worktree modifications/untracked governance and performance report directories remain preserved.

## 13. Handoff to next agent

- **VERIFIED**: Developer report path: `agent-reports/T3.3/developer-report.md`.
- **VERIFIED**: Branch: `feature/T3.3-jwt-login`; base: fetched `origin/main` at `232db57d2b7a05464bd9fda97c049410189dc1a3`, which contains the T3.2 merge.
- **VERIFIED**: Focused test result: 9/9; full Maven result: 331/331; both requested commands reported `BUILD SUCCESS`.
- **PENDING VERIFICATION**: Tester must verify independently before consulting Developer conclusions. Reviewer must audit implementation before reading agent reports.
- **PENDING VERIFICATION**: Human Tech Lead alone decides final acceptance. This report does not claim T3.3 is accepted or complete.
