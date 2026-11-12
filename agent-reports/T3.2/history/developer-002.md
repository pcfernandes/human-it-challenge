# Developer Report — T3.2 Single-user provisioning with BCrypt hashes

## 1. Task ID

- **VERIFIED** (direct Tech Lead tasking): `T3.2` — configure one in-memory account for development and production using BCrypt hashes, keep the demo user restricted to `dev & !prod`, and never configure or store plaintext passwords.

## 2. Execution identifier and date

- **VERIFIED** (this execution): `T3.2-DEV-001`.
- **VERIFIED** (execution environment): 2026-11-12.
- **NOT AVAILABLE**: No earlier `agent-reports/T3.2/developer-report.md` was present when this execution began; therefore no prior Developer report needed archiving under `agent-reports/README.md`, Section 6.

## 3. Agent role and model

- **VERIFIED** (execution environment): Senior Java/Spring Boot Developer; model `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct Tech Lead tasking): Reuse current configuration where applicable; provide one in-memory user; load the production username and BCrypt hash from external configuration; permit the demo user only under `dev & !prod`; validate credentials using `PasswordEncoder.matches()`; fail startup for missing/invalid configuration; do not log passwords, hashes or tokens.
- **VERIFIED** (direct Tech Lead tasking): No plaintext passwords in YAML or Java configuration; no startup-time application hash generation; no user database, roles, registration, refresh tokens, endpoints, new dependencies or JWT issuance. Run focused tests and the full Maven suite once; preserve unrelated worktree changes; stop after implementation and reporting; do not self-approve or mark T3.2 complete.
- **VERIFIED** (`implementation-plan.md`, Phase 3 T3.2): The task is one-user provisioning without a user database/account-management endpoints, and production must not provision the demo account.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): This report uses the mandatory thirteen-section format and evidence markers; a previous primary report must be archived before replacement.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md` — report sections, evidence markers, execution history, and handoff workflow.
- **VERIFIED**: `implementation-plan.md`, Phase 3 — approved one-user T3.2 scope and profile-isolation acceptance criteria.
- **VERIFIED**: `src/main/java/com/humanit/challenge/config/AppDemoUserProperties.java`, `DevConfigPropertiesRegistration.java`, `ConfigPropertiesRegistration.java`, and `AppJwtProperties.java` — existing development-only credentials binding and JWT configuration.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java` and `agent-reports/T3.1/architect-proposal.md` — T3.1 BCrypt encoder/security infrastructure and approved in-memory user provisioning approach.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`, `README.md`, `pom.xml`, and related profile/security tests — existing external configuration, docs, dependencies and test conventions.
- **VERIFIED**: Initial `git status --short` — pre-existing modified `agent-reports/decisions.md` and untracked governance/performance report directories were present before implementation.

## 6. Files changed

- **VERIFIED**: `src/main/java/com/humanit/challenge/config/AppDemoUserProperties.java` — retained the existing dev property prefix and changed the credential field to `passwordHash`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/config/AppProductionUserProperties.java` and `ProductionUserPropertiesRegistration.java` — added production-specific properties at `app.security.user`, registered only when `prod` is active.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SingleUserProvisioningConfiguration.java` — added profile-conditioned in-memory user provisioning, BCrypt hash-format checks and safe startup errors that do not include credential values.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java` — retained the existing BCrypt `PasswordEncoder` and added a `DaoAuthenticationProvider`/`AuthenticationManager` wired to the single active `UserDetailsService` and that encoder.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`, and `README.md` — documented the dev/prod BCrypt-hash environment variables without placing credentials or hashes in committed configuration.
- **VERIFIED**: `src/test/java/com/humanit/challenge/config/StartupTestHarness.java`, `DemoUserPropertiesTest.java`, `ProductionUserProvisioningTest.java`, `ProdProfileStartupTest.java`, `CombinedDevProdProfilesTest.java`, `JwtSecretValidationTest.java`, and `src/test/java/com/humanit/challenge/security/SecurityConfigurationIntegrationTest.java` — added or adapted tests for profile selection, hash validation, authentication matching and startup failures. A temporary test-only credential/hash fixture is generated for tests; application startup does not encode plaintext.
- **VERIFIED**: `agent-reports/T3.2/developer-report.md` — this report.
- **VERIFIED** (`git status --short`, `git diff --stat`): The pre-existing modified `agent-reports/decisions.md` and untracked `GOV-DECISIONS-RECONSTRUCTION-*` / `PERF-NPLUS1-*` report directories were preserved and not edited. No dependency file, migration, user table, login endpoint, commit, push, merge or branch switch was made.

## 7. Commands executed

- **VERIFIED**: `git status --short; git diff --stat` — captured initial unrelated worktree state before changes.
- **VERIFIED**: `git diff --check; git status --short; git diff --stat` — checked whitespace and reviewed final changed/untracked paths; `git diff --check` reported no whitespace errors (Git displayed existing line-ending conversion warnings).
- **VERIFIED**: `.\mvnw.cmd "-Dtest=DemoUserPropertiesTest,ProductionUserProvisioningTest,ProdProfileStartupTest,CombinedDevProdProfilesTest,SecurityConfigurationIntegrationTest" test` — focused provisioning/security tests passed.
- **VERIFIED**: `.\mvnw.cmd test` — full Maven test suite passed once.

## 8. Evidence and observed results

- **VERIFIED** (`SingleUserProvisioningConfiguration.java`): Development provisioning uses the existing `app.security.demo-user` properties and is guarded with `@Profile("dev & !prod")`. Production provisioning binds `app.security.user` and is guarded with `@Profile("prod")`. Both paths construct one `InMemoryUserDetailsManager` user with the supplied BCrypt hash and no authorities.
- **VERIFIED** (`SecurityConfiguration.java`): The existing `BCryptPasswordEncoder` bean is injected into the configured `DaoAuthenticationProvider`; authentication uses that provider's `PasswordEncoder.matches()` behavior. No application startup hash generation was added.
- **VERIFIED** (`application*.yml`, `README.md`): Dev uses `APP_SECURITY_DEMO_USER_USERNAME` and `APP_SECURITY_DEMO_USER_PASSWORD_HASH`; production uses `APP_SECURITY_USER_USERNAME` and `APP_SECURITY_USER_PASSWORD_HASH`. The checked-in YAML contains no credential values or plaintext passwords.
- **VERIFIED** (`DemoUserPropertiesTest.java`, `SecurityConfigurationIntegrationTest.java`): Tests exercise the dev user's BCrypt hash, successful and unsuccessful password matching, successful authentication, and rejection of an incorrect password.
- **VERIFIED** (`ProdProfileStartupTest.java`): Production profile tests load the external production user/hash and verify that the demo user is unavailable.
- **VERIFIED** (`CombinedDevProdProfilesTest.java`): Combined `dev,prod` activation does not register/use the dev demo user; the production user source remains available.
- **VERIFIED** (`DemoUserPropertiesTest.java`, `ProductionUserProvisioningTest.java`): Missing and malformed dev/production credential settings fail startup; asserted exception messages identify the relevant property prefix and omit credential/hash values.
- **VERIFIED** (focused Maven command output): `Tests run: 19, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`.
- **VERIFIED** (full Maven command output): `Tests run: 329, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`.
- **NOT AVAILABLE**: No real deployment/secrets manager was available or used; production binding was verified through test-supplied external properties, not through a live production host.

## 9. Findings and severity

| ID | Severity | Finding / evidence |
|---|---|---|
| T32-DEV-01 | Info | **VERIFIED**: Deployment operators must provide the external production username and BCrypt hash; omission or malformed configuration is designed to fail startup. No real deployment credentials were supplied or inspected. |
| T32-DEV-02 | Pending | **PENDING VERIFICATION**: Independent Tester verification, independent Reviewer audit, and human Tech Lead acceptance have not been performed by this Developer execution. |

No implementation-blocking finding was observed in the focused or full Maven runs. This is not a Reviewer conclusion or Tech Lead acceptance.

## 10. Decisions and reasoning

- **VERIFIED**: Reused the existing development demo-user property prefix, preserving the approved `dev & !prod` gate while replacing the old raw-password field with a BCrypt hash field.
- **VERIFIED**: Added a narrowly scoped production credential record/registration because the prior configuration only provided dev demo-user credentials; binding production values separately prevents accidentally treating the dev demo account as the production user.
- **VERIFIED**: Validated username presence and BCrypt hash shape before constructing the in-memory user. Startup failure text contains property names only, not configured credential values.
- **VERIFIED**: Used Spring Security's existing BCrypt encoder with a DAO provider/AuthenticationManager rather than adding dependencies or implementing a custom password comparator. No roles, permission grants, persistence, token issuance or login endpoint were introduced.
- **VERIFIED**: Updated README instructions to use only hash-valued credential environment variables. It directs operators to provide hashes from a secure provisioning process rather than storing or encoding a plaintext password in application configuration.
- **VERIFIED**: Test fixture generation is restricted to test code; the application uses the externally supplied hash as-is and does not generate a hash from plaintext during application startup.

## 11. Final status

**VERIFIED — IMPLEMENTATION AND REQUESTED TEST RUNS FINISHED; T3.2 NOT ACCEPTED OR MARKED COMPLETE.** The provisioning changes are implemented, focused tests and the full Maven suite passed, and this report is written. Independent verification/review and human Tech Lead acceptance remain outstanding; this execution stops here as requested.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Tester to independently verify the T3.2 requirements and tests before reading Developer conclusions, per `agent-reports/README.md`.
- **PENDING VERIFICATION**: Reviewer to independently audit the code and design before reading the Architect, Developer or Tester reports, per `agent-reports/README.md`.
- **PENDING VERIFICATION**: Human Tech Lead acceptance is not available and remains the only authority for accepting T3.2. No completion approval is claimed.
- **NOT AVAILABLE**: Live production secrets-manager/environment integration and real-host startup were not verified.

## 13. Handoff to next agent

- **VERIFIED**: Developer report path: `agent-reports/T3.2/developer-report.md`.
- **VERIFIED**: Test evidence recorded in Section 8: focused provisioning/security tests passed (19 tests); full Maven suite passed (329 tests).
- **PENDING VERIFICATION**: Tester must perform independent verification before consulting this report. Reviewer must complete an independent audit before consulting the Architect, Developer or Tester conclusions.
- **PENDING VERIFICATION**: Human Tech Lead must decide acceptance after test and review evidence. Do not treat these implementation/test results as approval or task completion.
