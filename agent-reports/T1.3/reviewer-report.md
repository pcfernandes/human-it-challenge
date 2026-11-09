# Reviewer Report — T1.3 Environment Configuration and Secret Externalization

---

## 1. Task ID

**T1.3** — Environment configuration and secret externalization (Phase 1), against Architect Revision 4 (`agent-reports/T1.3/architect-proposal.md`, execution `T1.3-EXEC-007-ARCH-01`), Developer execution `T1.3-EXEC-008-DEV-01`, Tester execution `T1.3-EXEC-009-TST-01`.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.3-EXEC-010-REV-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Type | Independent final code review (audit) |
| History note | No prior `agent-reports/T1.3/reviewer-report.md` existed at execution start — **VERIFIED** via `git status --short` / `git ls-files --others --exclude-standard`; therefore no archival copy was required under `agent-reports/README.md` Section 6. |

## 3. Agent role and model

- **Role:** Reviewer (senior Java and Spring Boot code reviewer)
- **Model:** `opencode/beam-free`
- **Constraints honored:** No application code, tests, `pom.xml`, Maven Wrapper, requirements/planning documents, other agents' reports or `decisions.md` were modified. No Git operations beyond read-only inspection. No approval of T1.3 is claimed.

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 (`T1.3-EXEC-010-REV-01`): perform an independent final review of the T1.3 implementation, including:

1. Correctness and Spring Boot best practices.
2. JWT secret validation and basic security.
3. Dev/prod profile separation.
4. Pagination configuration.
5. Test quality and meaningful coverage.
6. Compliance with approved T1.3 scope.
7. Unnecessary complexity.

Independence duty honored — **VERIFIED**:
1. Read `agent-reports/README.md` and `agent-reports/T1.3/architect-proposal.md` (Revision 4) first.
2. Inspected the actual implementation via read-only Git inspection (`git status --short`, `git diff HEAD --stat`, `git diff HEAD`, `git ls-files --others --exclude-standard`) and direct file reads, **before** reading `developer-report.md` and `tester-report.md`.
3. Read `developer-report.md` and `tester-report.md` only after the independent inspection was complete, then compared findings (Section 8.5).

## 5. Files inspected

All inspected this execution with read/glob/grep tools and one delegated read-only Git inspection:

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format, evidence markers, archival convention |
| `agent-reports/T1.3/architect-proposal.md` (Revision 4) | VERIFIED | Authorized design, amendments 1–4, Appendices A–F, test matrix T-M1…T-M21 |
| `agent-reports/T1.3/security-jwt-assessment.md` | VERIFIED | SA-01…SA-05; mitigation adopted |
| `agent-reports/T1.3/security-profiles-assessment.md` | VERIFIED | F-SP-01…F-SP-07; `@Profile("dev & !prod")` recommendation |
| `agent-reports/decisions.md` | VERIFIED (read-only) | No T1.3 approval recorded (Section 4 of log) |
| `implementation-plan.md` (T1.3, lines 108–118) | VERIFIED | Approved T1.3 scope and acceptance criteria |
| `requirements-decisions.md` (Section 12) | VERIFIED | Dev `dev`/H2, prod `prod`/PostgreSQL, externalized credentials |
| `pom.xml` (working tree) | VERIFIED | Only change: `spring-boot-configuration-processor` (optional) |
| `src/main/resources/application.yml` (working tree) | VERIFIED | `spring.profiles.default: dev`, pagination 0/20/100, JWT expiration 60 |
| `src/main/resources/application-dev.yml`, `application-prod.yml` | VERIFIED | Dev DEBUG, prod WARN/INFO; no secrets, no demo-user in prod |
| `src/main/java/.../config/*` (all config records, registration, `JwtSecretValidator`, validators) | VERIFIED | Implementation under review |
| `src/test/java/.../config/*` and `HumanItChallengeApplicationTests` | VERIFIED | 31 tests |
| Git working tree (`git status --short`, `git diff HEAD`, `git ls-files --others --exclude-standard`) | VERIFIED | 27 changed files; +1044/-3 lines; 9 untracked files incl. `JwtSecretValidator.java` and 8 report files |
| `agent-reports/T1.3/developer-report.md` | VERIFIED (read post-inspection) | Comparison in Section 8.5 |
| `agent-reports/T1.3/tester-report.md` | VERIFIED (read post-inspection) | Comparison in Section 8.5 |

## 6. Files changed

**By this execution: one report file only.**

| File | Change |
|---|---|
| `agent-reports/T1.3/reviewer-report.md` | **Created** — this report |

No application code, test, `pom.xml`, YAML, `decisions.md`, other agent's report or Git state was modified. No commit, stage, push or PR was performed.

## 7. Commands executed

| # | Command | Result |
|---|---|---|
| C1 | `git status --short` (delegated, read-only) | VERIFIED — working-tree state summarized in Section 8.1 |
| C2 | `git diff HEAD --stat` (delegated, read-only) | VERIFIED — 27 files, +1044/-3 lines |
| C3 | `git diff HEAD` (delegated, read-only) | VERIFIED — full diff captured and inspected (1267 lines) |
| C4 | `git ls-files --others --exclude-standard` (delegated, read-only) | VERIFIED — 9 untracked files |
| C5 | File reads (Section 5) | VERIFIED — all listed artifacts read this execution |
| C6 | `.\mvnw.cmd clean test` | **NOT RUN by this execution** — cited from Developer report (31/31, BUILD SUCCESS, 2026-11-09T15:23:12Z) and Tester report (31/31, BUILD SUCCESS, 23.374 s). Both independently corroborate the same counts. Build evidence is therefore RECONSTRUCTED from those two in-project artifacts, not re-executed here. |

## 8. Evidence and observed results

### 8.1 Git working-tree state (VERIFIED)

`git status --short` (delegated, read-only):

- Modified (staged/tracked): `.opencode/agents/architect.md`, `.opencode/agents/reviewer.md`, `.opencode/agents/tester.md`, `pom.xml`, `src/main/resources/application.yml`, `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java`.
- Added (staged): 22 new main/test source and resource files (config records, validators, registration classes, profile YAMLs, test classes, `StartupTestHarness`).
- Untracked: `agent-reports/T1.3/` (8 report files) and `src/main/java/com/humanit/challenge/config/JwtSecretValidator.java`.

`git diff HEAD --stat`: 27 files changed, 1044 insertions, 3 deletions. `git ls-files --others --exclude-standard` confirms 9 untracked entries. No `reviewer-report.md` existed before this execution (VERIFIED).

### 8.2 Implementation observations (VERIFIED, from direct file reads)

**JWT secret validation (amendment 1):**
- `AppJwtProperties.secret` is now a plain `String` field with no `@NotBlank`/`@Base64DecodedMinBytes` (VERIFIED, `AppJwtProperties.java` lines 26–28). `expirationMinutes` retains `@NotNull @Min(1)` (VERIFIED, line 28).
- `JwtSecretValidator` is a `@Component` with constructor-injected `AppJwtProperties` and `@PostConstruct` validation: non-blank, strict RFC 4648 Base64 decode, decoded length ≥ 32. Messages contain the property prefix and failure kind only; never the secret value (VERIFIED, `JwtSecretValidator.java` lines 25–41).
- `Base64DecodedMinBytes` annotation and validator are retained and still unit-tested (6 tests) (VERIFIED).
- No other main-code path logs or prints `AppJwtProperties` or its secret (VERIFIED by grep, Section 5).

**Profile separation (amendment 2):**
- `DevConfigPropertiesRegistration` carries `@Profile("dev & !prod")` (VERIFIED, line 16). `ConfigPropertiesRegistration` is not profile-gated and registers pagination + JWT in all profiles (VERIFIED).
- `application.yml` sets `spring.profiles.default: dev` exactly once (VERIFIED). `application-dev.yml` (DEBUG) and `application-prod.yml` (WARN/INFO) contain no secrets and no demo-user keys (VERIFIED).
- `AppDemoUserProperties` is registered only by the dev-gated configuration (VERIFIED, `DevConfigPropertiesRegistration`).

**Pagination configuration:**
- `AppPaginationProperties`: `defaultPage @Min(0)`, `defaultPageSize @Min(1)`, `maxPageSize @Min(1) @Max(100)`, plus class-level `@ValidPaginationInvariants` (default page size ≤ max page size) (VERIFIED).
- Defaults committed in `application.yml`: 0 / 20 / 100, `expiration-minutes: 60` (VERIFIED).

**Test harness and assertions (amendment 3):**
- `StartupTestHarness` applies overrides via `TestPropertyValues` through an `ApplicationContextInitializer` (high-precedence) (VERIFIED, lines 1154–1172).
- `DefaultDevProfileStartupTest` asserts `getDefaultProfiles()` contains `dev` and `getActiveProfiles()` is empty (VERIFIED).
- `CombinedDevProdProfilesTest` asserts `AppDemoUserProperties` bean is absent under `dev,prod` while both profile files load (VERIFIED).
- `JwtSecretValidationTest` (6 tests) asserts fail-fast for missing/blank/malformed/too-short secrets and no value leak for malformed and short secrets; 32-byte boundary accepted (VERIFIED).
- `PaginationPropertiesValidationTest` (5 tests) covers max 100, min 1, min 0, cross-field invariant, expiration min 1 (VERIFIED).
- `SourceScanAuditTest` (1 test) verifies `spring.profiles.default: dev` exactly once, no committed `spring.profiles.active`, no `META-INF/spring.factories`, no `EnvironmentPostProcessor` (VERIFIED).
- `Base64DecodedMinBytesValidatorTest` (6 tests) verifies chars≠bytes (VERIFIED).

**Build/test evidence (RECONSTRUCTED from Developer and Tester reports):**
- Developer: `.\mvnw.cmd clean test` — BUILD SUCCESS, `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0`, total 29.731 s, Java 21.0.12.1, Spring Boot 3.5.16 (VERIFIED in `developer-report.md` Section 8.1).
- Tester: same command — BUILD SUCCESS, 31/0/0/0, total 23.374 s (VERIFIED in `tester-report.md` Section 8.1).
- Both reports list identical per-class counts (10 classes, 31 tests). I did not re-execute the build (C6).

### 8.3 Compliance with approved T1.3 scope (VERIFIED)

Cross-checked against `implementation-plan.md` T1.3 (lines 108–118) and Architect Revision 4:

- In scope implemented: Spring profiles `dev`/`prod` (default dev), externalized credentials (JWT secret env-only; demo-user env-only), JWT secret configuration (Base64, ≥32 decoded bytes, fail-fast), configurable demo-user settings (dev-gated), maximum page size and pagination defaults (0/20/100, code-owned ceiling).
- Out of scope respected: no datasource/Flyway/`ddl-auto` anywhere; no domain entities/repositories/controllers; no JWT encoder/decoder/filter/algorithm; no HTTP pagination enforcement (config only); no README changes; no custom startup INFO logging; no `EnvironmentPostProcessor`/`spring.factories`. Verified by file tree, `SourceScanAuditTest`, and grep.
- The only `pom.xml` change is `spring-boot-configuration-processor` (approved O-01) (VERIFIED, diff).

### 8.4 Unnecessary complexity assessment (VERIFIED)

- No new infrastructure: no `EnvironmentPostProcessor`, no custom `FailureAnalyzer`, no `BindHandler`, no bootstrap package. The JWT validation moved into a single `@Component` with `@PostConstruct`, as recommended by the security assessment. Minimal change.
- `Base64DecodedMinBytes` annotation/validator retained though no longer applied in main code. This is a deliberate retention (reusable for non-secret uses, still unit-tested) and matches Architect Revision 4. It is the only element that could be seen as surplus; it is justified and low-risk.
- No shared `Profiles` constant was introduced (deliberate pragmatic deviation, D-DEV-04). This keeps the change minimal but leaves the guard string duplicated (see R-M05).

### 8.5 Comparison with Developer and Tester conclusions (performed after independent inspection)

**Developer report (`T1.3-EXEC-008-DEV-01`):**
- Claims: BUILD SUCCESS 31/31; all 8 authorized items implemented; no findings; residual risk RK-01 preserved. **My observation: Match on build and on implementation of the eight authorized items.** The "no findings" claim is **not fully supported**: the Tester (Section 9) and I (Section 9) identify documentation and test-coverage gaps. These are not runtime defects, but they are real gaps relative to Architect Revision 4 and the Tester's F-T01…F-T05.
- Discrepancy D-1: Developer Section 6 claims `DevConfigPropertiesRegistration` and `ConfigPropertiesRegistration` javadoc were updated, but `AppDemoUserProperties` still documents itself as "`@Profile("dev")`-gated" while the actual guard is `@Profile("dev & !prod")` (R-M01 / tester F-T01).
- Discrepancy D-2: Developer Section 6 states `SourceScanAuditTest` was "left unchanged and passing." That is true, but it means the amended guard-form source-scan assertions (Architect T-M18 amendment / T-M23) are not automated (R-M02 / tester F-T02).
- Discrepancy D-3: Developer D-DEV-04 states the shared `Profiles.DEV_AND_NOT_PROD` constant was intentionally not introduced. Accepted pragmatic deviation; profile-name fragility (Architect F-09) remains unmitigated (R-M05 / tester F-T05).

**Tester report (`T1.3-EXEC-009-TST-01`):**
- Claims: PASS, BUILD SUCCESS 31/31; verified behaviors (JWT, profiles, pagination) all pass; five minor findings F-T01…F-T05. **My observation: Full agreement.** My independent code inspection confirms the same implementation facts and the same five gaps. I adopt F-T01…F-T05 and add R-M06…R-M08 (Section 9).
- No substantive disagreement with the Tester. Minor note: the Tester's F-T03 (no prod-path secret-leak regression test) is confirmed; the dev-path `doesNotContain` checks exist only for malformed and short secrets, not for the blank case (R-M03).

## 9. Findings and severity

**Critical findings: none.**
**Major findings: none** (the only Major-classified risk is RK-01 / F-02 / F-SP-02 — accidental dev startup in production when `prod` is omitted — which is an **accepted residual risk** under the Tech Lead's decision OD-02, preserved by mandatory amendment 4, and governed by operational mitigations M1–M5. It is not a defect of this implementation and Revision 4 explicitly does not reduce it.)

**Minor findings (non-blocking; consistent with the review policy that minor documentation issues and additional tests do not block approval):**

| ID | Severity | Finding | Evidence |
|---|---|---|---|
| R-M01 | Minor | Stale Javadoc in `AppDemoUserProperties`: states the record is registered by a "`@Profile("dev")`-gated" configuration, while `DevConfigPropertiesRegistration` now uses `@Profile("dev & !prod")`. Misleading for future readers; the Architect explicitly warns against simplifying the guard back to `@Profile("dev")`. | `AppDemoUserProperties.java` lines 12–13 vs. `DevConfigPropertiesRegistration.java` line 16 (VERIFIED). Tester F-T01. |
| R-M02 | Minor | `SourceScanAuditTest` does not implement the amended guard-form assertions required by Architect Revision 4 (T-M18 amendment / security assessments OSA-03/OSP-03): no automated check that `AppJwtProperties.secret` carries no `@NotBlank`/`@Base64DecodedMinBytes`, and no check that `DevConfigPropertiesRegistration` carries `@Profile("dev & !prod")` (or the shared constant) with no bare `@Profile("dev")`. A regression reintroducing the leaking bind-path constraints or a bare dev profile would not be caught by the suite. | `SourceScanAuditTest.java` (VERIFIED, 93 lines) vs. Architect Appendix F T-M18 amendment and security-jwt-assessment Section 10.5. Tester F-T02 (most security-relevant minor). |
| R-M03 | Minor | No prod-path security regression test for secret leakage: `JwtSecretValidationTest` covers the dev path for malformed/short secrets (`doesNotContain(secret)`), but the prod path (`prodWithoutSecretFailsStartup`) checks fail-fast only, and the blank-secret case has no `doesNotContain` assertion. | `JwtSecretValidationTest.java` lines 29–79 (VERIFIED). Tester F-T03. |
| R-M04 | Minor | `StartupTestHarness.prodProperties()` returns a map that includes demo-user keys (via `propertyMap`), despite its Javadoc "prod-start environment: only the secret is provided." Callers must manually remove them. Latent harness inconsistency; not a runtime defect. | `StartupTestHarness.java` lines 62, 95–100, 114–116 (VERIFIED). Tester F-T04. |
| R-M05 | Minor | The shared `Profiles.DEV_AND_NOT_PROD` constant recommended by the Architect (F-09) was not introduced; the guard string `"dev & !prod"` is duplicated across `DevConfigPropertiesRegistration`, `ConfigPropertiesRegistration` Javadoc and tests. Profile-name variants (`production`, `prd`, …) remain uncovered. Accepted pragmatic deviation (D-DEV-04); residual risk noted. | `DevConfigPropertiesRegistration.java` line 16; `ConfigPropertiesRegistration.java` line 15 (VERIFIED). Tester F-T05. |
| R-M06 | Minor | `CombinedDevProdProfilesTest` class-level Javadoc is stale: it states "with no technical guard … the dev-gated demo-user configuration registers," while the second test method now asserts the bean is absent under `dev,prod`. | `CombinedDevProdProfilesTest.java` lines 18–21 vs. lines 49–54 (VERIFIED). |
| R-M07 | Minor | Base64 decoding logic is duplicated: `JwtSecretValidator` calls `Base64.getDecoder().decode(...)` directly instead of reusing a helper extracted from `Base64DecodedMinBytesValidator`, as recommended by the security assessment (Section 10.2, item 3) to avoid divergent rules. Both currently implement the same strict RFC 4648 / ≥32-byte rule, so no behavioral defect today; drift risk for future changes. | `JwtSecretValidator.java` lines 32–33 vs. `Base64DecodedMinBytesValidator.java` lines 29–30 (VERIFIED). |
| R-M08 | Minor | Demo-user credentials (`AppDemoUserProperties.username`/`password`) are validated via Bean Validation (`@NotBlank`) on secret-bearing fields. Under a bind failure these values could be echoed by the framework in the same way as the JWT secret (Architect F-10 / security-jwt-assessment residual risk 3). This is pre-existing by design and out of the T1.3 JWT scope; the Architect recommends documenting "validate secrets in application code, not the bind path" as a codebase standard and auditing other secret-bearing properties. Not a regression introduced by this execution. | `AppDemoUserProperties.java` lines 19–21; `security-jwt-assessment.md` Section 10.6 item 3 (VERIFIED). |

**Observations (not findings):**
- `Base64DecodedMinBytes` is no longer applied in main code (only its own unit test). Retained deliberately per Architect Revision 4 for non-secret uses; low risk, justified.
- `spring-boot-configuration-processor` metadata generation for `app.security.jwt.secret` after removing its constraints is PENDING VERIFICATION (Architect assumption A2). No evidence of breakage; default metadata is property-driven.
- `JwtSecretValidator` fail-fast timing via `@PostConstruct` during context refresh is PENDING VERIFICATION (Architect assumption A1), but is standard Spring Boot behavior and corroborated by the passing fail-fast tests.

**Zero Critical and zero Major findings.** R-M02 is the most security-relevant minor and should be remediated before final acceptance, but under the review policy it does not block approval (it is a test-coverage gap, not a runtime defect).

## 10. Decisions and reasoning

- **D-RVW-01 — No blocking defects.** The implementation matches Architect Revision 4 amendments 1–4: JWT secret validation moved out of the bind path with fail-fast and the 32-byte floor preserved; `@Profile("dev & !prod")` guard applied; high-precedence test overrides; default-vs-active profile distinction; combined-profile absence assertion. All 31 tests pass (corroborated by two independent executions). No Critical or Major runtime/security defect was found.
- **D-RVW-02 — Residual risk preserved, not reduced.** RK-01 / F-02 / F-SP-02 (accidental dev startup in production when `prod` is omitted) remains a Major accepted risk under OD-02. Revision 4 closes only the combined-profile demo-user exposure (F-03), not the missing-profile case. This review does not claim any reduction. Operational mitigations M1–M5 remain required in T1.6 / Phase 5.
- **D-RVW-03 — Minor findings are non-blocking.** R-M01…R-M08 are documentation, test-coverage, harness-consistency and maintainability gaps. Per the review policy, minor documentation issues and additional tests do not block approval. R-M02 (guard-form source-scan) is the most security-relevant and is recommended for remediation before final acceptance, but it is a regression-test gap rather than a runtime defect.
- **D-RVW-04 — Scope compliance confirmed.** All T1.3 in-scope items are implemented; all explicitly out-of-scope items (datasource, Flyway, domain code, JWT code, HTTP pagination, README) are absent. No unnecessary complexity was introduced beyond the deliberately retained, reusable Base64 validator.
- **D-RVW-05 — Independence preserved.** This audit was completed from the actual code and Git state before reading the Developer and Tester reports; disagreements are documented with concrete evidence in Section 8.5.

## 11. Final status

**REVIEW COMPLETE — NO BLOCKING ISSUES (0 Critical, 0 Major).** The T1.3 implementation is correct, compliant with the approved scope, and consistent with Architect Revision 4. Eight minor findings (R-M01…R-M08) are reported; none blocks approval under the review policy. The implementation is ready for Tech Lead acceptance, subject to optional remediation of the minor findings (in particular R-M02) and continued acceptance of the preserved residual risk RK-01.

This report does **not** approve T1.3 — final acceptance is the sole decision of the human Tech Lead.

## 12. Outstanding issues

1. **Minor remediation (optional, Developer, after authorization):** fix stale Javadoc (R-M01, R-M06); add amended guard-form assertions to `SourceScanAuditTest` (R-M02); add prod/blank secret-leak regression checks (R-M03); align `prodProperties()` Javadoc/contents (R-M04); optionally introduce the shared `Profiles` constant (R-M05); optionally reuse the Base64 decode helper (R-M07); consider application-code validation for demo-user credentials (R-M08).
2. **Residual risk (accepted, preserved):** RK-01 / F-02 / F-SP-02 — operational mitigations M1–M5 remain required in T1.6 / Phase 5.
3. **Pending verification:** `@PostConstruct` fail-fast timing (A1) and configuration-processor metadata after removing constraints (A2); empty `SPRING_PROFILES_ACTIVE=""` semantics (F-06/OT-07).
4. **Tech Lead acceptance of T1.3 pending** — not recorded anywhere; `agent-reports/decisions.md` contains no T1.3 acceptance entry.
5. **Forward boundaries untouched:** no datasource/Flyway `ddl-auto`, no JWT code, no HTTP pagination, no README changes.

## 13. Handoff to next agent

- **To the Tech Lead:** decide T1.3 acceptance and record it in `agent-reports/decisions.md`; accept or request remediation of R-M01…R-M08 (R-M02 recommended before final acceptance); confirm RK-01 residual risk remains accepted.
- **To the Developer:** no change required to keep the build green; remediation of R-M01…R-M08 is optional and requires authorization (test edits per OT-02/OT-04 governance).
- **To the Tester:** nothing further required; the independent verification (31/31) stands. Re-verify only if remediation R-M02/R-M03 is applied.

---

**Status of this report:** Authored 2026-11-09 (Europe/Lisbon) by the Reviewer role (`T1.3-EXEC-010-REV-01`). All 13 mandatory sections present. No file outside this report was modified. No approval of T1.3 is claimed or implied. T1.3 implementation not modified.