# Tester Report — T3.2 Independent credential provisioning verification

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 3, T3.2; direct tasking): `T3.2` — independently verify one-user credential provisioning, profile isolation, BCrypt handling, safe startup/error behavior, and scope boundaries. Do not modify production code, approve, or mark the task complete.

## 2. Execution identifier and date

- **VERIFIED** (this execution): `T3.2-TEST-001`.
- **VERIFIED** (execution environment): 2026-11-12.
- **VERIFIED** (`agent-reports/T3.2/` glob inspection): No prior primary tester report or tester history file existed; there was no tester report to archive before creating this primary report.

## 3. Agent role and model

- **VERIFIED** (execution environment): Senior Java/Spring Boot QA Tester; model `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct tasking and `implementation-plan.md`, T3.2): Independently inspect T3.2 plan and implementation before reading Developer conclusions; run focused T3.2 tests and the full Maven suite once; add tests only for material gaps; do not modify production code; preserve unrelated worktree changes; do not commit, push, merge, switch branches, approve, or mark T3.2 complete.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): Use the mandatory thirteen report sections and evidence markers; archive any previous tester report before replacement. No previous tester report existed.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`; `implementation-plan.md` Phase 3/T3.2; `pom.xml`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SingleUserProvisioningConfiguration.java`, `SecurityConfiguration.java`; `src/main/java/com/humanit/challenge/config/AppDemoUserProperties.java`, `AppProductionUserProperties.java`, `DevConfigPropertiesRegistration.java`, `ProductionUserPropertiesRegistration.java`, and `ConfigPropertiesRegistration.java`.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, and `application-prod.yml`.
- **VERIFIED**: `src/test/java/com/humanit/challenge/config/ProductionUserProvisioningTest.java`, `DemoUserPropertiesTest.java`, `ProdProfileStartupTest.java`, `CombinedDevProdProfilesTest.java`, `DefaultDevProfileStartupTest.java`, `ExplicitDevProfileStartupTest.java`, `SourceScanAuditTest.java`, and `StartupTestHarness.java`; `src/test/java/com/humanit/challenge/security/SecurityConfigurationIntegrationTest.java`.
- **VERIFIED**: Worktree status/diff summary and source searches for user provisioning, profile/credential configuration, roles/authorities, login mappings, persistence, and JWT encoder usage. After independent inspection and verification were complete, `agent-reports/T3.2/developer-report.md` was read to compare claims and test counts.

## 6. Files changed

- **VERIFIED**: Only `agent-reports/T3.2/tester-report.md` was created by this Tester execution. No production source, test, configuration, dependency, or other agent report was modified; no archive was required.
- **VERIFIED** (`git status --short` following verification): Existing modified and untracked worktree paths remain present, including `agent-reports/decisions.md`, governance/performance report directories, and T3.2 implementation/test paths. They were not changed by this Tester execution.

## 7. Commands executed

- **VERIFIED**: `git status --short; git diff --stat; git diff -- src/main src/test pom.xml` — inspected the available worktree state and implementation/test diff without changing files.
- **VERIFIED**: `.\mvnw.cmd -Dtest=ProductionUserProvisioningTest,DemoUserPropertiesTest,ProdProfileStartupTest,CombinedDevProdProfilesTest,DefaultDevProfileStartupTest,ExplicitDevProfileStartupTest,SecurityConfigurationIntegrationTest test` — initial invocation was rejected by PowerShell parsing the unquoted comma list; Maven did not run in that attempt.
- **VERIFIED**: `.\mvnw.cmd '-Dtest=ProductionUserProvisioningTest,DemoUserPropertiesTest,ProdProfileStartupTest,CombinedDevProdProfilesTest,DefaultDevProfileStartupTest,ExplicitDevProfileStartupTest,SecurityConfigurationIntegrationTest' test` — focused test execution.
- **VERIFIED**: `.\mvnw.cmd test` — full Maven test suite, run once.
- **VERIFIED**: `git diff --check; if ($?) { git status --short }` — no whitespace errors were reported; Git emitted line-ending conversion warnings for existing modified files.

## 8. Evidence and observed results

- **VERIFIED** (`SingleUserProvisioningConfiguration.java`): The demo account is created only by a `UserDetailsService` bean guarded with `@Profile("dev & !prod")`; the production account bean is guarded with `@Profile("prod")`. Each factory builds one `InMemoryUserDetailsManager` containing one user, and the builder adds no authorities.
- **VERIFIED** (`DevConfigPropertiesRegistration.java`, `ProductionUserPropertiesRegistration.java`, property records, and YAML): Demo credentials are profile-isolated; production username/hash bind from `app.security.user` external configuration. The profile YAML contains no user plaintext password or stored user hash. The production database password is an environment placeholder; the development database password is blank. Neither is a user credential.
- **VERIFIED** (`SingleUserProvisioningConfiguration.java`): Usernames must contain text; hashes must match a BCrypt-format pattern. Startup errors name only the relevant property prefix and do not interpolate credentials. The BCrypt `PasswordEncoder` and `DaoAuthenticationProvider` are wired for password matching; no application-startup plaintext-to-hash generation is present.
- **VERIFIED** (`DemoUserPropertiesTest.java`, `ProductionUserProvisioningTest.java`, `ProdProfileStartupTest.java`, `CombinedDevProdProfilesTest.java`, and `SecurityConfigurationIntegrationTest.java`): Tests cover dev and prod credential inputs, successful BCrypt matching, incorrect-password rejection, missing and malformed credential startup failures, no demo user in prod or combined `dev,prod`, and safe error/unauthorized bodies. Production and combined-profile tests resolve the production user source; the demo user is unavailable in those contexts.
- **VERIFIED** (source search and `git diff`): No login controller or auth `@PostMapping`, role/permission annotations, user entity/account repository, or JWT issuance call was added for T3.2. `JwtEncoder` infrastructure is present in `SecurityConfiguration.java`, but its bean was not part of the T3.2 diff; it is not an issuance endpoint. Existing Client/Document persistence is unrelated to account provisioning.
- **VERIFIED** (focused Maven output): `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`. Expected startup-failure cases logged their exception stack traces; the observed error text contained the configuration property path and no supplied hash/password values.
- **VERIFIED** (full Maven output): `Tests run: 329, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`.
- **VERIFIED** (comparison with `agent-reports/T3.2/developer-report.md`): Developer records 19 focused tests, while this execution records 23. The Developer’s documented focused command selected five classes; this Tester selected seven, adding `DefaultDevProfileStartupTest` and `ExplicitDevProfileStartupTest`. This is a test-selection/count difference, not a contradictory result. Both report the full suite as 329 tests with no failures/errors/skips.
- **NOT AVAILABLE**: No live production host or secrets manager was supplied; external binding was tested using test-provided values, not a deployed runtime.

## 9. Findings and severity

| ID | Severity | Finding / evidence |
|---|---|---|
| T32-TEST-01 | Minor | **VERIFIED** (`AppDemoUserProperties.java`, lines 9–11): Its Javadoc still describes `DevConfigPropertiesRegistration` as `@Profile("dev")`-gated, while the implementation uses the stricter `dev & !prod` expression. This is documentation drift only; the actual registration and provisioning guards are correct. |
| T32-TEST-02 | Info | **VERIFIED** (focused Maven outputs and Developer report): Focused counts differ (23 versus 19) because this execution selected two additional profile-startup test classes. The full-suite count/results agree. |
| T32-TEST-03 | Info | **NOT AVAILABLE**: A real production deployment/secrets-manager startup was unavailable for verification. |

No test failure or material behavioral gap was observed. Existing tests cover the identified T3.2 behaviors; no additional tests were added. This report does not assess unrelated pre-existing edits as authored by the T3.2 Developer because an independent pre-change baseline for every worktree path is not available.

## 10. Decisions and reasoning

- **VERIFIED**: The focused suite was expanded to include both default-dev and explicitly activated-dev startup tests in addition to the direct provisioning, prod, combined-profile, and authentication tests. It completed successfully.
- **VERIFIED**: No new test was necessary: the existing tests plus direct source inspection exercise the material positive, negative, profile-boundary, and safe-failure behaviors requested for T3.2.
- **VERIFIED**: No production code or existing test was changed by this Tester execution. The minor Javadoc mismatch is recorded for a separately authorized documentation correction rather than edited here.
- **VERIFIED**: `AuthenticationManager`/BCrypt verification is present, but the scope prohibition on login endpoint/JWT issuance is respected: no T3.2 login controller or token creation flow was found. Existing JWT encoder infrastructure is unchanged in the T3.2 diff.

## 11. Final status

**PASS — independent T3.2 verification; one Minor documentation finding.** Focused tests (23/23) and full suite (329/329) passed. This is a Tester result only; T3.2 is not approved or marked complete.

## 12. Outstanding issues

- **VERIFIED**: Correct the stale profile expression in `AppDemoUserProperties` Javadoc if a documentation-only correction is authorized.
- **NOT AVAILABLE**: Live production environment/secrets-manager integration and startup verification remain outstanding.
- **PENDING VERIFICATION**: The independent Reviewer audit and human Tech Lead acceptance remain outstanding. The Reviewer must complete their independent audit before reading this or other agent conclusions, in accordance with `agent-reports/README.md`.

## 13. Handoff to next agent

- **VERIFIED**: Tester report path: `agent-reports/T3.2/tester-report.md`.
- **VERIFIED**: Focused test result: 23 run, 0 failures, 0 errors, 0 skipped. Full Maven result: 329 run, 0 failures, 0 errors, 0 skipped.
- **VERIFIED**: One Minor finding is the stale `@Profile("dev")` Javadoc in `AppDemoUserProperties.java`; actual profile guards pass behavioral verification.
- **PENDING VERIFICATION**: Reviewer to independently audit without first reading agent conclusions; human Tech Lead alone decides final acceptance. Do not treat this PASS as approval or task completion.
