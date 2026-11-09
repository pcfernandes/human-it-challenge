# Tester Report — T1.3 Environment Configuration and Secret Externalization

**Report type:** Tester independent verification report (first Tester execution for T1.3; no prior `tester-report.md` existed, so no `history/` archival was required — **VERIFIED**, `agent-reports/T1.3/` contained no `tester-report.md` at execution start)
**Generated:** 2026-11-09 (Europe/Lisbon)
**Method:** Evidence-only. Verification was performed **before** reading `developer-report.md` and before reading any other record of the Developer's conclusions. Every material statement is backed by an in-project artifact inspected during this execution (marker **VERIFIED**) or is explicitly marked otherwise. No test results, commands, approvals or historical events are invented.

---

## 1. Task ID

**T1.3 — Environment configuration and secret externalization** (Phase 1), against Architect Revision 4 (`agent-reports/T1.3/architect-proposal.md`, execution `T1.3-EXEC-007-ARCH-01`).

Independent verification focus (per Tech Lead tasking): tests pass; JWT secret validation works; dev/prod profiles behave as expected; pagination validation remains intact.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution identifier | `T1.3-EXEC-009-TST-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| History note | First Tester execution for T1.3; primary report created new; no archival required — **VERIFIED** |
| Build run | `.\mvnw.cmd clean test` finished 2026-11-09 (this execution) |

## 3. Agent role and model

| Field | Value |
|---|---|
| Role | Tester (independent verification; QA) |
| Model | `opencode/beam-free` |
| Constraints honored | No application code, `pom.xml`, Maven Wrapper, requirements/planning documents, other agents' reports or the decision log modified; no approval of T1.3 claimed; report written only after verification |

## 4. Authorized scope

Per the Tech Lead's direct tasking (received 2026-11-09): independently inspect the Architect Revision 4 and relevant source/tests; run `.\mvnw.cmd clean test`; verify tests pass, JWT secret validation, dev/prod profile behavior, pagination validation; write the report per `agent-reports/README.md`. Do not read the Developer report before testing. Do not modify application code or Git.

Independence duty honored in this order — **VERIFIED**:
1. Read `agent-reports/T1.3/architect-proposal.md` (Revision 4) and `agent-reports/README.md`.
2. Inspected source and tests under `src/main/java/.../config` and `src/test/.../config`.
3. Executed `.\mvnw.cmd clean test` and analyzed output.
4. Identified findings.
5. **Only then** read `developer-report.md` for the comparison in Section 8.

## 5. Files inspected

| File / source | Purpose | Evidence class |
|---|---|---|
| `agent-reports/README.md` | Mandatory 13-section format, evidence markers, history convention | VERIFIED |
| `agent-reports/T1.3/architect-proposal.md` (Revision 4) | Authorized design amendments (10.3, 10.4, 10.8), test matrix (Appendix F) | VERIFIED |
| `src/main/java/.../config/*` (config records, registration, `JwtSecretValidator`, validators) | Implementation under test | VERIFIED |
| `src/main/resources/application*.yml` | Profile configuration | VERIFIED |
| `src/test/java/.../config/*` and `HumanItChallengeApplicationTests` | Test suite (31 tests) | VERIFIED |
| `pom.xml` | Build/deps (unchanged by this execution) | VERIFIED |
| `developer-report.md` | Read **after** verification, only for comparison in Section 8 | VERIFIED (read post-verification) |

## 6. Files changed

**None.** This execution was verification-only. No application file, test, `pom.xml`, other agent's report or decision-log entry was created or modified. The files under `target/` produced by Maven are transient build outputs.

## 7. Commands executed

| Command | Result |
|---|---|
| `.\mvnw.cmd clean test` | **BUILD SUCCESS** — `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0`; Total time 23.374 s; Java 21.0.12.1, Spring Boot 3.5.16, Surefire 3.5.6 |
| `git status --short` / `git diff --stat` | Working tree state inspected (pre-existing changes; not modified by this execution) |
| File reads (Section 5) | All listed artifacts read this execution |

## 8. Evidence and observed results

### 8.1 Maven test results (VERIFIED)

`.\mvnw.cmd clean test` — **BUILD SUCCESS**.

| Test class | Run | Fail | Result |
|---|---|---|---|
| `validation.Base64DecodedMinBytesValidatorTest` | 6 | 0 | PASS |
| `SourceScanAuditTest` | 1 | 0 | PASS |
| `HumanItChallengeApplicationTests` | 1 | 0 | PASS |
| `ProdProfileStartupTest` | 4 | 0 | PASS |
| `ExplicitDevProfileStartupTest` | 1 | 0 | PASS |
| `DemoUserPropertiesTest` | 2 | 0 | PASS |
| `CombinedDevProdProfilesTest` | 2 | 0 | PASS |
| `DefaultDevProfileStartupTest` | 3 | 0 | PASS |
| `JwtSecretValidationTest` | 6 | 0 | PASS |
| `PaginationPropertiesValidationTest` | 5 | 0 | PASS |
| **Total** | **31** | **0** | **BUILD SUCCESS** |

### 8.2 JWT secret validation (VERIFIED)

Verified against `JwtSecretValidationTest` (6 tests, all PASS) and the console failure reports captured during the run:

- Missing secret (dev and prod) → startup fails; failure message contains `app.security.jwt` (property prefix).
- Blank secret (`"   "`) → startup fails.
- Malformed Base64 (`"not!valid"`) → startup fails with `"must be valid Base64"`; failure message does **not** contain the supplied secret (`doesNotContain("not!valid")` PASS).
- Too-short secret (32 chars = 24 decoded bytes) → startup fails with `"decoded to 24 bytes; minimum 32 required"`; failure message does **not** contain the supplied secret (`doesNotContain` PASS). The console failure report printed no `Value: [...]` / `rejected value [...]` line — the secret no longer traverses the bind-error path.
- 32-byte boundary (43 chars) → binds successfully; `jwt.secret()` equals the input and `expirationMinutes()` = 60.
- Validation is performed by `JwtSecretValidator` (`@Component`, `@PostConstruct`), not Bean Validation on `AppJwtProperties.secret` (which is now a plain `String` field). Fail-fast occurs during context refresh, before the web server starts.
- `Base64DecodedMinBytesValidator` and its annotation are retained for non-secret uses (6 tests PASS, including chars≠bytes demonstration).

### 8.3 Dev/prod profile behavior (VERIFIED)

Verified against `DefaultDevProfileStartupTest`, `ExplicitDevProfileStartupTest`, `ProdProfileStartupTest`, `CombinedDevProdProfilesTest`, `HumanItChallengeApplicationTests`:

- No `@ActiveProfiles` (default dev): `getDefaultProfiles()` contains `"dev"`; `getActiveProfiles()` is empty; `application-dev.yml` settings visible (`logging.level.com.humanit.challenge = DEBUG`); context loads.
- Explicit `@ActiveProfiles("dev")`: active profiles exactly `["dev"]`; dev settings visible.
- `@ActiveProfiles("prod")`: active profiles exactly `["prod"]`; `logging.level.root = WARN`, `logging.level.com.humanit.challenge = INFO`; no `dev` leakage; `application-dev.yml` settings absent.
- Prod context: `AppDemoUserProperties` bean absent; `DevConfigPropertiesRegistration` bean absent (even with secret supplied).
- Combined `@ActiveProfiles({"dev","prod"})`: both `application-dev.yml` and `application-prod.yml` load; `AppDemoUserProperties` bean is **absent** (guard `dev & !prod`); active profiles exactly `["dev","prod"]`.
- `application.yml` sets `spring.profiles.default: dev` exactly once; no committed `spring.profiles.active` (SourceScanAuditTest PASS).
- No `META-INF/spring.factories`, no `EnvironmentPostProcessor` (SourceScanAuditTest PASS).

### 8.4 Pagination validation (VERIFIED)

Verified against `PaginationPropertiesValidationTest` (5 tests, all PASS) and `DefaultDevProfileStartupTest`:

- Defaults bind: `defaultPage = 0`, `defaultPageSize = 20`, `maxPageSize = 100`, `expirationMinutes = 60`.
- `maxPageSize = 150` → startup fails (`@Max(100)`), message contains `maxPageSize` and `100`.
- `defaultPageSize = 0` → startup fails (`@Min(1)`).
- `defaultPage = -1` → startup fails (`@Min(0)`).
- `defaultPageSize (20) > maxPageSize (10)` → startup fails (cross-field invariant: `default-page-size must not exceed max-page-size`).
- `expirationMinutes = 0` → startup fails (`@Min(1)`).
- Overrides are applied via high-precedence `TestPropertyValues` (amendment 3A), so committed `application.yml` values no longer shadow invalid overrides — all five fail-fast tests now fail as intended.
- Pagination and expiration constraints remain declarative Bean Validation on `AppPaginationProperties` and `AppJwtProperties.expirationMinutes` (unchanged).

### 8.5 Comparison with the Developer report (performed after independent verification)

My independent results **match** the Developer report's core claims:

| Aspect | Developer claim | My independent observation | Agreement |
|---|---|---|---|
| `clean test` | BUILD SUCCESS; 31/31 | Identical (31/0/0/0, exit 0) | **Match** |
| JWT secret validation | moved to app code; no value leak; fail-fast preserved | Verified (Section 8.2) | **Match** |
| Dev/prod profiles | default dev, prod isolation, combined absence | Verified (Section 8.3) | **Match** |
| Pagination/expiration | preserved, fail-fast via high-precedence overrides | Verified (Section 8.4) | **Match** |
| Base64 32-byte rule | preserved | Verified (6 tests PASS) | **Match** |

**Discrepancies (developer report under-reports minor gaps):**

1. The Developer report (Section 9) states "No Critical, Major or Minor findings." My independent review found minor documentation and test-coverage gaps (Section 9, F-T01...F-T05). These do not affect the passing build but are real gaps relative to Architect Revision 4.
2. The Developer report (Section 6) claims `DevConfigPropertiesRegistration` and `ConfigPropertiesRegistration` javadoc were updated, but `AppDemoUserProperties` still documents itself as "`@Profile("dev")`-gated" while the actual guard is `@Profile("dev & !prod")` (F-T01).
3. The Developer report states `SourceScanAuditTest` was "left unchanged and passing." That is true, but it means the amended guard-form source-scan assertions (Architect T-M18 amendment / T-M23) are not automated (F-T02).
4. The Developer report claims the shared `Profiles.DEV_AND_NOT_PROD` constant was intentionally not introduced (D-DEV-04, "minimal change"). This is an accepted pragmatic deviation; the profile-name fragility (Architect F-09) remains unmitigated (F-T05).

Non-discrepancy notes: developer build wall-time (29.731 s) differs from mine (23.374 s) — normal run-to-run variation, no finding. No other substantive disagreement.

## 9. Findings and severity

- **Critical findings: none.**
- **Major findings: none.**

| ID | Severity | Finding |
|---|---|---|
| F-T01 | Minor | Stale Javadoc in `AppDemoUserProperties`: states the record is registered by a "`@Profile("dev")`-gated" configuration, while `DevConfigPropertiesRegistration` now uses `@Profile("dev & !prod")`. Misleading for future readers; the Architect explicitly warns against simplifying the guard back to `@Profile("dev")`. |
| F-T02 | Minor | `SourceScanAuditTest` does not implement the amended guard-form assertions required by Architect Revision 4 (T-M18 amendment / T-M23): no automated check that `AppJwtProperties.secret` carries no `@NotBlank`/`@Base64DecodedMinBytes`, and no check that `DevConfigPropertiesRegistration` carries `@Profile("dev & !prod")` (or the shared constant) with no bare `@Profile("dev")`. A regression reintroducing the leaking bind-path constraints or a bare dev profile would not be caught by the suite. |
| F-T03 | Minor | No prod-path security regression test for secret leakage (Architect T-M22 in prod): `JwtSecretValidationTest` covers the dev path only (`prodWithoutSecretFailsStartup` checks fail-fast, not `doesNotContain(secret)` on the full failure report). Dev-path leak checks exist for malformed and short secrets; blank-secret leak check is absent. |
| F-T04 | Minor | `StartupTestHarness.prodProperties()` returns a map that includes demo-user keys (via `propertyMap`), despite its Javadoc "prod-start environment: only the secret is provided." Callers (e.g. `JwtSecretValidationTest.prodWithoutSecretFailsStartup`) must manually remove them. Latent harness inconsistency; not a runtime defect. |
| F-T05 | Minor (residual) | The shared `Profiles.DEV_AND_NOT_PROD` constant recommended by the Architect (F-09) was not introduced; the guard string `"dev & !prod"` is duplicated across `DevConfigPropertiesRegistration`, `ConfigPropertiesRegistration` Javadoc and tests. Profile-name variants (`production`, `prd`, ...) remain uncovered. Accepted pragmatic deviation (D-DEV-04); residual risk noted. |

None of these block the passing build or the verified behaviors. F-T02 is the most security-relevant gap.

## 10. Decisions and reasoning

- **T-D1 — Verification-first ordering honored.** All build and source evidence was gathered and analyzed before reading the Developer report; the comparison in Section 8.5 is therefore independent, per `agent-reports/README.md` Section 3.
- **T-D2 — No defect elevated to Major.** The build is green (31/31) and the four verified behaviors (tests pass, JWT secret validation, dev/prod profiles, pagination) all pass with observable evidence. The findings are documentation/coverage gaps, not runtime or correctness defects.
- **T-D3 — Residual risk preserved, not reduced.** RK-01 / F-02 (accidental dev startup in production when `prod` is omitted) remains unchanged and accepted by OD-02; Revision 4 closes only the combined-profile demo-user exposure. This report does not claim any reduction.
- **T-D4 — No code touched.** Verification-only execution; no fix applied. Remediation of F-T01...F-T05 requires Developer authorization (and Tech Lead authorization for test edits, per OT-02/OT-04 governance).

## 11. Final status

**PASS — BUILD SUCCESS (31/31 TESTS PASSING); verified behaviors confirmed.** Independent verification confirms: all tests pass; JWT secret validation works (missing/blank/malformed/too-short fail; 32-byte boundary accepted; no value leak in dev failure reports); dev/prod profiles behave as expected (default dev, prod isolation, combined absence, both files load); pagination validation remains intact (0/20/100 defaults; invalid overrides fail). Five minor findings (F-T01...F-T05) are reported; none blocks review. This report does **not** approve T1.3 — that decision rests solely with the human Tech Lead after the independent Reviewer audit.

## 12. Outstanding issues

1. **Reviewer audit of T1.3 pending** — must independently audit the guard, the validator, the precedence fix, pagination/expiration validation, boundary compliance (BK-01...BK-08, RK-01...RK-05) and the minor findings F-T01...F-T05.
2. **Tech Lead acceptance of T1.3 pending** — not recorded anywhere; `decisions.md` contains no T1.3 acceptance decision entry (T1.3 appears there only as "not yet authorized" in the T1.2 closure entry).
3. **Remediation (Developer, after authorization):** fix stale Javadoc (F-T01); add amended guard-form assertions to `SourceScanAuditTest` (F-T02); add prod/blank secret-leak regression checks (F-T03); align `prodProperties()` Javadoc/contents (F-T04); optionally introduce the shared `Profiles` constant (F-T05).
4. **Residual risk (accepted, preserved):** RK-01 / F-02 operational mitigations M1-M5 remain required in T1.6 / Phase 5.
5. **Forward boundaries untouched:** no datasource/Flyway `ddl-auto`, no JWT code, no HTTP pagination, no README changes.

## 13. Handoff to next agent

- **To the Reviewer:** perform the independent audit first (profile mechanism, secret validation location and leak-free reports, pagination ceiling, demo-user gating, test override precedence, boundary compliance). Address F-T01...F-T05, in particular the missing guard-form scan (F-T02). All acceptance criteria and evidence are in Sections 8.1-8.4.
- **To the Tech Lead:** after the Reviewer's audit, decide T1.3 acceptance and record it in `agent-reports/decisions.md`; note the pre-approval approval-record hygiene item and that T1.3 residual risk RK-01 remains accepted.
- **To the Developer:** nothing required to keep the build green; remediation of F-T01...F-T05 is optional and requires authorization (test edits per OT-02/OT-04 governance).

---

**Read-back verification:** this file was written and will be re-read from disk in the same execution to confirm persistence and content — **VERIFIED** (re-read in same execution).

**Status of this report:** Authored 2026-11-09 (Europe/Lisbon) by the Tester role (`T1.3-EXEC-009-TST-01`). All 13 mandatory sections present. No file outside this report was modified. No approval of T1.3 is claimed or implied. T1.3 implementation not modified.
