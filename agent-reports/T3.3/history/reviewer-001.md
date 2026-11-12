# Reviewer Report — T3.3 Independent Login and JWT Security Review

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 3, T3.3; direct Tech Lead tasking): `T3.3` — independently review the public login endpoint, credential authentication, JWT contract and security/regression scope.

## 2. Execution identifier and date

- **VERIFIED** (this execution): `T3.3-REV-001`.
- **VERIFIED** (execution environment): 2026-11-12.
- **VERIFIED** (`agent-reports/T3.3/` workspace listing): No prior `reviewer-report.md` or Reviewer history archive was present; no archive was required before creating this primary report.

## 3. Agent role and model

- **VERIFIED** (execution environment): Senior Java/Spring Boot Code Reviewer; model `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct Tech Lead tasking and `implementation-plan.md`, Phase 3, T3.3): Review public access to `POST /auth/login`, use of the existing `AuthenticationManager`, explicit HS256 signing, claims and externally configured expiration/secret, generic invalid-credential 401 behavior, sensitive-data handling, use of issued JWTs on protected endpoints, dependencies/abstractions, Phase 2/T3.1/T3.2 regression risk, and exclusion of registration, roles and refresh tokens.
- **VERIFIED** (direct Tech Lead tasking and Reviewer role constraints): No code/test/configuration changes, Maven execution, Git state changes, approval or completion claim. Preserve unrelated worktree state and author only this Reviewer report (and any required Reviewer history archive).
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): Independently inspect implementation before reading Developer or Tester reports; report using the thirteen mandatory sections, evidence markers and history convention.

## 5. Files inspected

### Independent implementation inspection, completed before reading Developer/Tester reports

- **VERIFIED**: `agent-reports/README.md` and `implementation-plan.md` (Phase 3, T3.1–T3.3) — report conventions, approved task scope and acceptance criteria.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/AuthController.java`; `src/main/java/com/humanit/challenge/dto/auth/LoginRequest.java`; `src/main/java/com/humanit/challenge/dto/auth/TokenResponse.java`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java`, `SingleUserProvisioningConfiguration.java`, `SecurityProblemHandlers.java`; `src/main/java/com/humanit/challenge/config/AppJwtProperties.java`, `JwtSecretValidator.java`; `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java`.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`; `pom.xml`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java`, `SecurityConfigurationIntegrationTest.java`, `SecurityProblemHandlersTest.java`.
- **VERIFIED**: Corresponding available Surefire text summaries under `target/surefire-reports/` for the three security test classes.

### Read only after independent implementation inspection

- **VERIFIED**: `agent-reports/T3.3/developer-report.md` and `agent-reports/T3.3/tester-report.md` — compared reported implementation/test evidence and execution outcomes.
- **VERIFIED**: `agent-reports/T3.1/reviewer-report.md` and `agent-reports/T3.2/reviewer-report.md` — checked relevant prior security review context, including T3.1 finding `T31-REV-M01`.

## 6. Files changed

- **VERIFIED**: Created only `agent-reports/T3.3/reviewer-report.md` (this report). The primary report and Reviewer history archive were absent before this execution, so no prior report required archival.
- **VERIFIED**: No production code, test code, configuration, planning file, other agent report, decision log or Git state was changed.

## 7. Commands executed

- **VERIFIED**: None. Workspace read, glob and content-search tools were used; no shell/Git command or Maven command was executed. In particular, the Reviewer did not rerun tests.

## 8. Evidence and observed results

### 8.1 Login contract and token behavior

- **VERIFIED** (`SecurityConfiguration.java`, lines 47–50; `AuthController.java`, lines 27–45): `/auth/login` is explicitly permitted by the filter chain and the controller exposes `POST /auth/login`. The successful anonymous POST in `AuthControllerIntegrationTest.java` returns HTTP 200, confirming the login route is publicly accessible.
- **VERIFIED** (`AuthController.java`, lines 33–52; `SecurityConfiguration.java`, lines 79–84): The controller delegates username/password authentication to the configured `AuthenticationManager` using an unauthenticated `UsernamePasswordAuthenticationToken`; the manager is backed by the existing DAO provider and configured password encoder. No separate credential-comparison implementation was added.
- **VERIFIED** (`AuthController.java`, lines 54–63): JWT claims use `authentication.getName()` as subject, a second-precision `Instant.now()` as issued-at, and expiration computed from that issued-at plus `AppJwtProperties.expirationMinutes()`. The JWS header explicitly selects `MacAlgorithm.HS256`; the response record exposes only `accessToken` and `tokenType`, with `Bearer` as the token type.
- **VERIFIED** (`SecurityConfiguration.java`, lines 59–71; `AppJwtProperties.java`; `JwtSecretValidator.java`; `application.yml`): Issuance reuses the existing `JwtEncoder`, whose key is derived from the externally supplied Base64 secret. The decoder is pinned to HS256 and validates timestamps. Expiration remains the existing `app.security.jwt.expiration-minutes` property (with the common YAML default and normal external Spring property override), rather than a new controller constant. Secret validation requires a nonblank Base64 secret with at least 32 decoded bytes.
- **VERIFIED** (`AuthControllerIntegrationTest.java`, lines 51–75; available Surefire summary): The test decodes a login token with the configured decoder and asserts HS256, expected subject, issued-at presence, configured expiration interval and that expiration is in the future. It also uses the token against protected `GET /api/clients`, which returns HTTP 200. This addresses prior T3.1 finding `T31-REV-M01`: issuance now supplies a server-created HS256 header.
- **VERIFIED** (`AuthController.java`, lines 31, 47–52; `GlobalExceptionHandler.java`, lines 142–157; `AuthControllerIntegrationTest.java`, lines 77–99): Authentication exceptions are mapped to one generic `401` reason (`Invalid username or password.`). Both an incorrect password and unknown username are asserted to return that same response and not to reflect the tested username, password or BCrypt hash. The login controller does not log credentials, hashes or tokens. The access token is intentionally returned only as the successful login result.
- **VERIFIED** (`LoginRequest.java`, `TokenResponse.java`, inspected routes and `pom.xml`): The endpoint has a minimal validated request and response DTO. No registration/account-management route, role/permission policy, refresh-token flow, JWT library dependency or extra service/abstraction is introduced by T3.3. Existing Spring Security/OAuth2 Resource Server dependencies are reused.

### 8.2 Regressions and test evidence

- **VERIFIED** (`SecurityConfigurationIntegrationTest.java`, `SecurityProblemHandlersTest.java`, and their available Surefire summaries): Existing security checks cover valid token acceptance, wrong-signature and expired-token rejection, anonymous protected-route 401, stateless/no-session behavior, public OpenAPI access, BCrypt credential handling and safe 403 response handling. These are relevant T3.1/T3.2 regression checks, and the inspected security configuration retains the established stateless policy and route defaults.
- **VERIFIED** (three available Surefire text summaries): `AuthControllerIntegrationTest` reports 2 tests, `SecurityConfigurationIntegrationTest` reports 6, and `SecurityProblemHandlersTest` reports 1; each reports zero failures, errors and skips. These add to the reported focused 9-test selection.
- **VERIFIED** (`agent-reports/T3.3/tester-report.md`, Sections 7–8 and 11; `developer-report.md`, Sections 7–8): Tester and Developer both record the focused security run as **9 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS** and the full suite as **331 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS**. Their reported counts and outcomes agree. The focused per-class Surefire summaries independently corroborate the 9-test count and zero failures/errors/skips.
- **NOT AVAILABLE**: No aggregate full-suite console output was inspected or reproduced by this Reviewer. The 331/331 full-suite result is verified as a consistent Developer/Tester report claim, not as a Reviewer-run test or an independently inspected aggregate output. The available Surefire directory contains per-class summaries, not an aggregate total.
- **VERIFIED** (Developer and Tester reports): Both reports disclose a Maven Flyway remote metadata HTTP 401 warning but state the requested tests still completed successfully. This warning is not a login/security defect based on the inspected evidence.

### 8.3 Finding

- **VERIFIED** (`AuthController.java`, lines 50–52; `GlobalExceptionHandler.java`, lines 142–157; `AuthControllerIntegrationTest.java`, lines 77–99): The controller's invalid-credential `ResponseStatusException` is handled by the MVC advice as a `401` `ResponseEntity` with Problem Details; unlike the security filter's separate `SecurityProblemHandlers.authenticationEntryPoint()`, this MVC response path does not set a `WWW-Authenticate` header. The invalid-login test checks the status/body but does not assert a challenge header. RFC 9110 §15.5.2 specifies that a 401 response includes at least one `WWW-Authenticate` challenge. This is a Minor protocol/interop issue; it does not expose credentials or bypass authentication.

## 9. Findings and severity

| ID | Severity | Finding / recommendation |
|---|---|---|
| T33-REV-M01 | **Minor** | **VERIFIED** (`AuthController.java`, `GlobalExceptionHandler.java`, and `AuthControllerIntegrationTest.java`): Invalid login produces the required generic 401 body, but its MVC exception-handling path does not add `WWW-Authenticate`; the filter-level handler's Bearer challenge does not run for this permitted login route. HTTP 401 responses are expected by RFC 9110 §15.5.2 to carry an authentication challenge. Add an appropriate documented `WWW-Authenticate` challenge to this login failure response and assert it in the invalid-credential integration test. Keep the body generic and do not distinguish unknown user from password mismatch. |

- **VERIFIED**: No Critical or Major finding was identified. No credential, hash or signing secret leakage, token validation bypass, or Phase 2/T3.1/T3.2 regression was observed in the inspected code and available test evidence.

## 10. Decisions and reasoning

- **VERIFIED**: The implementation meets the principal T3.3 functional/security contract: public login, existing AuthenticationManager, explicit HS256, correct subject/iat/configured expiration, generic invalid-credential 401, protected-endpoint token acceptance, and no additional account/authorization flows or dependency.
- **VERIFIED**: The implementation explicitly addresses the prior T3.1 Minor follow-up for issuance algorithm selection by supplying HS256 in the endpoint-created JWS header. The integration test checks the decoded algorithm and verifies the issued signature using the configured decoder.
- **VERIFIED**: Developer and Tester reports agree on the 9/9 focused and 331/331 full-suite outcomes; per-class Surefire evidence corroborates the focused count. The full-suite count remains a report-based result because this Reviewer did not run Maven and no aggregate result was available to inspect.
- **VERIFIED**: The missing login 401 challenge is limited to protocol interoperability and is classified Minor; it does not change the required generic body or indicate a security bypass. The audit therefore has PASS status with one Minor finding and no Critical/Major findings.
- **PENDING VERIFICATION**: The human Tech Lead alone decides final T3.3 acceptance. This report does not approve or mark the task complete.

## 11. Final status

**PASS — independent T3.3 review completed; no Critical or Major findings.** One Minor HTTP 401 challenge-header finding is outstanding (`T33-REV-M01`). Tester and Developer report 9/9 focused and 331/331 full-suite passes; focused Surefire class summaries support 9/9, while the Reviewer did not independently execute Maven or verify an aggregate full-suite output. T3.3 is not approved or marked complete by this report.

## 12. Outstanding issues

- **VERIFIED — Minor, T33-REV-M01**: Add an appropriate `WWW-Authenticate` challenge to invalid-login 401 responses and assert it in the integration test.
- **VERIFIED**: No Critical or Major implementation finding or regression was observed.
- **NOT AVAILABLE**: Reviewer-run test execution and independently inspectable aggregate output for the claimed 331-test full-suite result; Maven was not run by the Reviewer.
- **PENDING VERIFICATION**: Human Tech Lead acceptance remains outstanding and is the only authority for final T3.3 acceptance/completion.

## 13. Handoff to next agent

- **VERIFIED**: Reviewer report path: `agent-reports/T3.3/reviewer-report.md`.
- **VERIFIED**: Final review status: **PASS**, one Minor finding (`T33-REV-M01`), no Critical or Major findings; reported focused result 9/9 and full-suite result 331/331.
- **PENDING VERIFICATION**: Address or explicitly disposition the 401 challenge-header finding through separately authorized work before final acceptance.
- **PENDING VERIFICATION**: The human Tech Lead alone decides whether to accept T3.3. This report does not claim approval or completion.
