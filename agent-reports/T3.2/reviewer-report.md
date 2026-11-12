# Reviewer Report — T3.2 Independent final security review

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 3, T3.2; direct tasking): `T3.2` — independently audit one-user credential provisioning, profile isolation, externalized credentials, BCrypt verification, safe startup failure, scope boundaries, and Phase 2/T3.1 regression risk.

## 2. Execution identifier and date

- **VERIFIED** (this execution): `T3.2-REV-001`.
- **VERIFIED** (execution environment): 2026-11-12.
- **VERIFIED** (`agent-reports/T3.2/` listing): No prior Reviewer primary report or Reviewer history file was present; there was no earlier report to archive before creating this primary report.

## 3. Agent role and model

- **VERIFIED** (execution environment): Senior Java/Spring Boot Code Reviewer; model `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct Tech Lead tasking and `implementation-plan.md`, Phase 3/T3.2): Independently inspect implementation, configuration and tests before reading Developer or Tester conclusions; review one-user-per-environment behavior, `dev & !prod` demo-user isolation, production credential externalization, plaintext-password absence, BCrypt hash validation/matching, safe startup failures, out-of-scope features/dependencies, and Phase 2/T3.1 regressions.
- **VERIFIED** (direct Tech Lead tasking): No application or test changes, commits, pushes, merges, branch switches, self-approval, or task completion marking. Preserve unrelated worktree changes.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): Use the thirteen mandatory report sections, evidence markers, and execution-history rules. Independent source inspection was completed before opening the Developer and Tester reports.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md` and `implementation-plan.md` (Phase 3 T3.1/T3.2/T3.5 and Phase 2 exit criteria).
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SingleUserProvisioningConfiguration.java`, `SecurityConfiguration.java`, and `src/main/java/com/humanit/challenge/config/AppDemoUserProperties.java`, `AppProductionUserProperties.java`, `DevConfigPropertiesRegistration.java`, `ProductionUserPropertiesRegistration.java`, `ConfigPropertiesRegistration.java`, and `JwtSecretValidator.java`.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`, and `pom.xml`.
- **VERIFIED**: T3.2-relevant tests `DemoUserPropertiesTest.java`, `ProductionUserProvisioningTest.java`, `ProdProfileStartupTest.java`, `CombinedDevProdProfilesTest.java`, `DefaultDevProfileStartupTest.java`, `ExplicitDevProfileStartupTest.java`, `SecurityConfigurationIntegrationTest.java`, `SourceScanAuditTest.java`, and `StartupTestHarness.java`.
- **VERIFIED** (after independent inspection): `agent-reports/T3.2/developer-report.md` and `agent-reports/T3.2/tester-report.md` were read for claim comparison.
- **VERIFIED**: Relevant `target/surefire-reports/*.txt` records were inspected for per-class outcomes. These records do not provide an unambiguous aggregate report proving the full-suite total of 329.

## 6. Files changed

- **VERIFIED**: Only `agent-reports/T3.2/reviewer-report.md` was created by this Reviewer execution. No production code, tests, configuration, requirements, plans, or other agents' reports were changed. No archive was required because no prior Reviewer report existed.
- **VERIFIED** (`git status --short`): Existing modified/untracked worktree paths, including unrelated governance/performance reports and T3.2 source/config/test files, were present. This execution did not alter them.

## 7. Commands executed

- **VERIFIED**: `git status --short` — read-only worktree inspection; no Git state was changed.
- **VERIFIED**: `git diff -- src/main/java/com/humanit/challenge/security/SecurityConfiguration.java pom.xml src/main/resources/application.yml src/main/resources/application-dev.yml src/main/resources/application-prod.yml` — inspected relevant tracked T3.2 changes without changing files.
- **VERIFIED**: File read/search/list operations inspected the artifacts listed in Section 5.
- **VERIFIED**: No Maven command was run by the Reviewer; no code, tests, or Git state were modified.

## 8. Evidence and observed results

- **VERIFIED** (`SingleUserProvisioningConfiguration.java`, lines 22–32): The development `UserDetailsService` exists only under `@Profile("dev & !prod")`; the production service exists under `@Profile("prod")`. In `dev,prod`, only the production bean is eligible. Each bean builds a single-user `InMemoryUserDetailsManager`; the `User` builder is not given any roles or authorities.
- **VERIFIED** (`DevConfigPropertiesRegistration.java`, `ProductionUserPropertiesRegistration.java`, and property records): Demo and production property records are independently profile-gated. The production record binds `app.security.user`; the demo record binds `app.security.demo-user`.
- **VERIFIED** (`application.yml`, `application-dev.yml`, `application-prod.yml`): YAML documents the corresponding environment-variable names without storing user usernames, user hashes, or plaintext user passwords. The blank development datasource password and production datasource environment placeholders are database settings, not user credentials. No production demo-user configuration is present.
- **VERIFIED** (`SingleUserProvisioningConfiguration.java`, lines 20, 34–47): Username presence and BCrypt hash structure are checked before user creation. Failure messages identify only the property prefix and do not interpolate the configured username or hash. The regex admits standard BCrypt version prefixes and work factors 04–31.
- **VERIFIED** (`SecurityConfiguration.java`, lines 74–84; `SecurityConfigurationIntegrationTest.java`, lines 103–115): The `BCryptPasswordEncoder` is wired through a `DaoAuthenticationProvider` and `AuthenticationManager`; tests cover matching the correct password and rejecting an incorrect password.
- **VERIFIED** (`ProductionUserProvisioningTest.java`, `DemoUserPropertiesTest.java`): Missing and malformed configuration causes startup failure in the tested profiles, with assertions that error messages do not reveal supplied hash/password fixture values.
- **VERIFIED** (`ProdProfileStartupTest.java`, `CombinedDevProdProfilesTest.java`): Tests assert the demo account is unavailable under prod and combined `dev,prod`; production credential loading is separately exercised.
- **VERIFIED** (`pom.xml` and inspected T3.2 implementation): No T3.2-added dependency is visible in the current dependency list. No account entity/repository, account database, role/permission model, login endpoint, or JWT issuance flow is present. `JwtEncoder` infrastructure remains in `SecurityConfiguration` as existing T3.1 infrastructure; its existence alone is not token issuance.
- **VERIFIED** (`git diff` for `SecurityConfiguration.java` and current file): The tracked security-configuration change adds the DAO provider/`AuthenticationManager` only; stateless sessions, route protection, JWT decoding/encoding infrastructure and the existing BCrypt bean are unchanged. This is consistent with no T3.1 filter-chain regression.
- **VERIFIED** (`agent-reports/T3.2/tester-report.md`, Sections 8 and 11): The Tester records a focused result of 23/23 and full-suite result of 329/329. The 23 focused count is consistent with the recorded selected classes and their available Surefire per-class records. **NOT AVAILABLE**: The original aggregate Maven output for 329 tests was not present as an independently inspectable artifact; Reviewer did not rerun Maven. The Developer report separately records 19 focused tests because its focused command selected fewer test classes; this is a selection/count difference, not a conflicting test failure.
- **VERIFIED** (`agent-reports/T3.2/tester-report.md`, finding T32-TEST-01; `AppDemoUserProperties.java`, lines 9–11): The Javadoc says the registration class is guarded by `@Profile("dev")`, but the actual annotation is `@Profile("dev & !prod")` in `DevConfigPropertiesRegistration.java` and the provisioning bean uses the same stricter expression. This is documentation drift; inspected behavior and tests support the stricter guard.
- **NOT AVAILABLE**: No live production host or secrets manager was available for an end-to-end deployment binding/startup audit. Test-supplied external property values exercise the binding path instead.

## 9. Findings and severity

| ID | Severity | Finding / evidence |
|---|---|---|
| T32-TEST-01 | Minor | **VERIFIED** (`AppDemoUserProperties.java`, lines 9–11): Javadoc documents `DevConfigPropertiesRegistration` as guarded by `@Profile("dev")`, but the actual configuration and user bean use `@Profile("dev & !prod")`. This is misleading documentation, although profile behavior is correct. Update the Javadoc if separately authorized. |

- **VERIFIED**: No Critical or Major security finding was identified in the inspected T3.2 changes.
- **VERIFIED**: The BCrypt-format regex permits work factors from 04 through 31. The requirements do not prescribe a narrower factor; the reviewer did not elevate this to a finding. Deployment should use a measured, policy-approved BCrypt work factor, rather than arbitrary extremes.

## 10. Decisions and reasoning

- **VERIFIED**: The profile arrangement meets the strict demo-user boundary: dev-only/default-dev selects the demo source, prod selects the production source, and combined dev/prod selects production only. The tests specifically exercise prod and combined-profile cases.
- **VERIFIED**: Production credentials are supplied as externally bound username/hash values; no user plaintext or committed user hash was found in inspected YAML/configuration. Startup failures are generic to the property path and avoid credential interpolation.
- **VERIFIED**: BCrypt matching is delegated to Spring Security's encoder/provider rather than custom comparison logic. Tests cover success and wrong-password rejection.
- **VERIFIED**: Scope boundaries are respected in the inspected implementation: no account persistence, registration, role model, login controller, or JWT issuance flow was introduced, and no dependency was added for provisioning.
- **VERIFIED**: The Tester’s focused 23/23 and full 329/329 results are recorded as Tester-reported evidence; the Reviewer did not execute those commands. Per-class records corroborate the focused class outcomes, but do not independently establish the aggregate full-suite count. No direct disagreement with the Tester’s identified Javadoc finding was found.
- **PENDING VERIFICATION**: Tech Lead acceptance remains a human decision and is not implied by this review.

## 11. Final status

**PASS — no Critical or Major findings; one Minor documentation finding.** The inspected implementation satisfies the reviewed T3.2 security and scope criteria. This is a Reviewer recommendation only; T3.2 is not accepted or marked complete by this report.

## 12. Outstanding issues

- **VERIFIED**: `T32-TEST-01` remains as a Minor stale-Javadoc finding; correction requires separately authorized work.
- **NOT AVAILABLE**: Live production secrets-manager/environment startup was not verified.
- **PENDING VERIFICATION**: The aggregate full-suite count of 329 is recorded in the Tester report, but its original aggregate output was not independently available to this Reviewer.
- **PENDING VERIFICATION**: Human Tech Lead acceptance remains outstanding and is the only authority for final acceptance or task completion.

## 13. Handoff to next agent

- **VERIFIED**: Reviewer report path: `agent-reports/T3.2/reviewer-report.md`.
- **VERIFIED**: Review outcome: PASS recommendation; no Critical/Major findings; one Minor stale-Javadoc finding (`T32-TEST-01`).
- **PENDING VERIFICATION**: Human Tech Lead to consider this review and Tester evidence, determine whether the Minor documentation finding needs authorized correction, and make the final T3.2 acceptance decision. Do not treat this report as approval or completion.
