# Reviewer Report — T1.4 Persistence and Migration Baseline

## 1. Task ID

**T1.4 — Persistence and migration baseline** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 12.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.4-EXEC-005-REV-02` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None found at inspection time — no prior `agent-reports/T1.4/reviewer-report.md` existed in the task directory |
| Predecessor archives | None required (no existing primary reviewer report to archive) |
| Architect proposal | `agent-reports/T1.4/architect-proposal.md` (`T1.4-EXEC-001-ARCH-01`) |
| Developer report | `agent-reports/T1.4/developer-report.md` (`T1.4-EXEC-002-DEV-01`) |
| Tester report | `agent-reports/T1.4/tester-report.md` (`T1.4-EXEC-003-TST-01`) |

## 3. Agent role and model

- **Role:** Reviewer (senior Java and Spring Boot code reviewer)
- **Model:** `opencode/beam-free`
- **Audit method:** Independent source inspection first (Sections 5 and 8.1–8.3), then review of the Developer and Tester reports (Sections 8.4–8.5). No application code, tests, YAML, SQL, `pom.xml`, requirements, planning documents, Git state, or other agents' reports were modified.

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09:

- Audit the T1.4 persistence and migration baseline implementation.
- Verify H2 configuration in dev, PostgreSQL configuration in prod, externalized database credentials, Flyway baseline migration, Hibernate `ddl-auto: validate`, H2 Console profile separation, test harness correctness, JWT/profile regression risk, scope compliance and unnecessary complexity.
- Evaluate Tester findings **F-T02** and **F-T03**.
- Do not modify application code or tests; do not approve T1.4 on behalf of the Tech Lead.

## 5. Files inspected

Independent inspection was completed **before** reading the Developer and Tester reports.

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section report format, evidence markers, history convention |
| `agent-reports/T1.4/architect-proposal.md` | VERIFIED | Approved design, proposed YAML and migration |
| `implementation-plan.md` (T1.4, lines 120–132) | VERIFIED | Task scope and acceptance criteria |
| `requirements-decisions.md` (Section 12, lines 549–596) | VERIFIED | H2/dev, PostgreSQL/prod, Flyway, `ddl-auto: validate` |
| `pom.xml` | VERIFIED | Flyway, H2, PostgreSQL already present from T1.2; no T1.4 dependency change |
| `src/main/resources/application.yml` | VERIFIED | Common `ddl-auto: validate`, Flyway enabled, locations, `sql.init.mode: never` |
| `src/main/resources/application-dev.yml` | VERIFIED | H2 datasource and dev-only H2 Console |
| `src/main/resources/application-prod.yml` | VERIFIED | PostgreSQL datasource bound to `SPRING_DATASOURCE_*` |
| `src/main/resources/db/migration/V1__baseline.sql` | VERIFIED | Comment-only Flyway baseline migration |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | Prod H2 override for configuration tests |
| `src/test/java/com/humanit/challenge/config/ProdProfileStartupTest.java` | VERIFIED | Prod profile startup and binding tests |
| `src/test/java/com/humanit/challenge/config/CombinedDevProdProfilesTest.java` | VERIFIED | Combined profile behavior documentation |
| `src/test/java/com/humanit/challenge/config/DefaultDevProfileStartupTest.java` | VERIFIED | Default-dev startup behavior |
| `src/test/java/com/humanit/challenge/config/ExplicitDevProfileStartupTest.java` | VERIFIED | Explicit-dev startup behavior |
| `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java` | VERIFIED | Default-dev context load |
| `src/test/java/com/humanit/challenge/config/JwtSecretValidationTest.java` | VERIFIED | JWT secret fail-fast behavior |
| `src/main/java/com/humanit/challenge/config/DevConfigPropertiesRegistration.java` | VERIFIED | `dev & !prod` guard for demo-user properties |
| `src/main/java/com/humanit/challenge/config/ConfigPropertiesRegistration.java` | VERIFIED | Environment-neutral property registration |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | VERIFIED | Profile mechanism and guard audit |
| `agent-reports/T1.4/developer-report.md` | VERIFIED (read after independent inspection) | Developer claims and scope |
| `agent-reports/T1.4/tester-report.md` | VERIFIED (read after independent inspection) | Tester verification and findings F-T01…F-T05 |

No Git or Maven command was executed by this reviewer; command results cited below are taken from the Developer and Tester reports and are marked as reported, not independently reproduced.

## 6. Files changed

None in application code, tests, YAML, SQL, `pom.xml`, requirements, planning documents, Git state, or other agents' reports.

- `agent-reports/T1.4/reviewer-report.md` — **Created** (this report).

## 7. Commands executed

None. The `bash` tool was unavailable for this execution, and no Git or Maven command was run. All evidence comes from direct file reads and from command results reported by the Developer and Tester (treated as reported, not independently reproduced).

## 8. Evidence and observed results

### 8.1 Independent source observations (VERIFIED from files)

**H2 configuration in dev (`application-dev.yml`):**
- `spring.datasource.url: jdbc:h2:mem:humanit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- `username: sa`, `password:` (empty), `driver-class-name: org.h2.Driver`
- `spring.h2.console.enabled: true`, `path: /h2-console`
- VERIFIED by file content.

**PostgreSQL configuration in prod (`application-prod.yml`):**
- `spring.datasource.url: ${SPRING_DATASOURCE_URL}`
- `username: ${SPRING_DATASOURCE_USERNAME}`
- `password: ${SPRING_DATASOURCE_PASSWORD}`
- `driver-class-name: org.postgresql.Driver`
- No URL, username, password, or driver version is committed. Values come from environment variables.
- VERIFIED by file content.

**Common configuration (`application.yml`):**
- `spring.jpa.hibernate.ddl-auto: validate`
- `spring.flyway.enabled: true`, `locations: classpath:db/migration`
- `spring.sql.init.mode: never`
- `spring.profiles.default: dev`
- VERIFIED by file content.

**Flyway baseline migration (`V1__baseline.sql`):**
- Contains comments only; no SQL statements, tables, indexes, constraints, seed data, or vendor-specific SQL.
- VERIFIED by file content.

**Test harness (`StartupTestHarness`):**
- `supplyProdExternals` and `prodProperties()` supply an in-memory H2 override (`jdbc:h2:mem:humanit-prod-test;…`, user `sa`, empty password, driver `org.h2.Driver`) plus `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` placeholders, so prod-profile configuration tests do not require PostgreSQL.
- `prodProperties()` no longer carries dev-only demo-user keys.
- VERIFIED by file content.

**Profile / JWT regression surface:**
- `DevConfigPropertiesRegistration` uses `@Profile("dev & !prod")`, so demo-user properties are not registered under prod or combined `dev,prod`.
- `SourceScanAuditTest` enforces `spring.profiles.default: dev` exactly once in `application.yml`, no committed `spring.profiles.active`, no `META-INF/spring.factories`, and no `EnvironmentPostProcessor`.
- No changes to JWT validation, profile activation, or demo-user gating logic are present in the T1.4 diff surface inspected (application.yml/dev/prod, migration, harness, combined test).
- VERIFIED by file content.

### 8.2 Reported verification results (NOT independently reproduced)

- Developer reports `.\mvnw.cmd test` exit code 0 and `.\mvnw.cmd clean verify` exit code 0 with `BUILD SUCCESS`, total time 27.846 s, Tests run: 31, Failures: 0, Errors: 0, Skipped: 0, Java 21.0.12.1. Reported in `developer-report.md` Section 7.
- Tester reports `.\mvnw.cmd clean verify` exit code 0, `BUILD SUCCESS`, total time 27.100 s, Tests run: 31, Failures: 0, Errors: 0, Skipped: 0, Maven Enforcer checks passed. Reported in `tester-report.md` Section 7.
- Tester reports per-class counts: `HumanItChallengeApplicationTests` 1/0, `DefaultDevProfileStartupTest` 3/0, `ProdProfileStartupTest` 4/0, `CombinedDevProdProfilesTest` 2/0, `JwtSecretValidationTest` 6/0, `PaginationPropertiesValidationTest` 5/0, `Base64DecodedMinBytesValidatorTest` 6/0. Reported in `tester-report.md` Section 8.
- Tester reports Flyway log lines showing database `jdbc:h2:mem:humanit (H2 2.3)`, `Successfully validated 1 migration`, `Current version of schema "PUBLIC": 1`, `Schema "PUBLIC" is up to date`. Reported in `tester-report.md` Section 8.2.
- Tester reports `git status --short` showing 5 modified tracked files and untracked `agent-reports/T1.4/` and `src/main/resources/db/` only; `git diff --stat` showing 5 files changed, 48 insertions, 5 deletions; no Dockerfiles or business entities. Reported in `tester-report.md` Section 7.

These command results are reported evidence, not independently reproduced in this review (bash unavailable).

### 8.3 Reported (Developer) deviations from the Architect proposal

- The Architect proposed `V1__baseline.sql` containing `SELECT 1;`. The Developer instead made it comment-only, stating Flyway accepted it and recorded schema version 1. The Tester accepted this as sufficient and better aligned with "no unnecessary SQL." Reported in `developer-report.md` Sections 4/10 and `tester-report.md` Section 10.3.
- The Architect proposed extending `StartupTestHarness` with an H2 override; the Developer implemented exactly that via `supplyProdExternals` and `prodProperties()`. VERIFIED in source.

### 8.4 Review of Developer report claims

- Claim "No changes to `pom.xml`, main Java source, requirements, planning documents, or other agents' reports" — VERIFIED by the inspected file set and the Tester's reported `git diff` scope.
- Claim "Flyway initializes and records the comment-only baseline migration as version 1" — RECONSTRUCTED/REPORTED. The build log lines reported by the Tester support Flyway validation and migration to version 1, but this reviewer did not directly query `flyway_schema_history` or run the build. Direct verification is PENDING (see F-R01/F-T02).
- Claim "H2 Console is dev-only" — VERIFIED by file contents (`application-dev.yml` only). See F-R03 for the combined-profile nuance.
- Claim "`prodProperties()` resolves latent R-M04 inconsistency" — VERIFIED in source: no demo-user keys are added.
- Claim "PostgreSQL integration remains PENDING VERIFICATION" — ACCEPTED; consistent with `implementation-plan.md` and `requirements-decisions.md` (Phase 5).

### 8.5 Review of Tester report claims and findings

- The Tester's independent-inspection claim (Developer report not opened until after verification) is consistent with the report's stated method; this reviewer cannot independently prove execution ordering, but the report is internally consistent. RECONSTRUCTED.
- The Tester's finding **F-T05** (build succeeds, 31 tests, 0 failures/errors/skipped, Java 21 and Maven enforcements pass) is reported evidence; not independently reproduced here. Treated as REPORTED.
- The Tester's finding **F-T04** (H2 Console remains enabled under combined `dev,prod`) is VERIFIED by source: `application-dev.yml` enables it and combined activation includes the dev profile. The Tester correctly classifies it as documented combined-profile behavior. See F-R03.
- The Tester's findings **F-T02** and **F-T03** are evaluated in Section 9 (F-R01, F-R02).

## 9. Findings and severity

| ID | Severity | Finding | Evidence |
|---|---|---|---|
| F-R01 (evaluates F-T02) | Minor (non-blocking) | Verification gap: no automated test directly queries `flyway_schema_history` or asserts the effective H2 Console property value at runtime. Flyway version 1 and console policy are inferred from configuration and reported build logs. | `tester-report.md` F-T02; `src/test/.../config/*Profile*Test.java`; config files. Review policy: minor coverage gaps do not block approval. |
| F-R02 (evaluates F-T03) | Minor (non-blocking) | Verification gap: no automated test verifies that prod startup fails when `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, or `SPRING_DATASOURCE_PASSWORD` are missing. Only the H2 override path is exercised. The fail-fast behavior follows from unresolved Spring placeholders, but it is not covered by tests. | `tester-report.md` F-T03; `ProdProfileStartupTest.java`; `StartupTestHarness.supplyProdExternals` / `prodProperties()`. |
| F-R03 | Minor (non-blocking) | H2 Console policy inconsistency under combined activation: the Architect proposal states that "production and combined profiles do not expose it," but the H2 Console is enabled whenever the `dev` profile is active, including combined `dev,prod`. `CombinedDevProdProfilesTest` documents demo-user non-registration under combined profiles but does not assert H2 Console state. Combined activation is not a supported production deployment mode, and standalone `prod` correctly has no H2 Console configuration. | `application-dev.yml` lines 12–15; `CombinedDevProdProfilesTest.java`; `architect-proposal.md` Section 10.3 item 4. |
| F-R04 | Minor (non-blocking) | H2 Console usability is not addressed in T1.4: no security configuration permits `/h2-console` or relaxes frame options/CSRF, so the console may be unreachable until Phase 3 (T3.1/T3.3) security work. This is outside the T1.4 scope of "profile separation," but it means the Architect's local-run instruction (`http://localhost:8080/h2-console`) is not yet usable end-to-end. | `pom.xml` (Spring Security present); absence of a `SecurityFilterChain` in `src/main/java`; `application-dev.yml`. |
| F-R05 | Info | The implemented baseline migration is comment-only rather than the Architect's proposed `SELECT 1;`. This is a scope-neutral improvement (no unnecessary SQL) and Flyway recorded schema version 1 per reported logs. No action required. | `V1__baseline.sql`; `developer-report.md` Section 10.1; `tester-report.md` Section 10.3. |
| F-R06 | Info | Production driver class name is hard-coded to `org.postgresql.Driver` while prod-profile tests override it to `org.h2.Driver`. This is intentional and required for the H2 override; it assumes the production datasource is PostgreSQL, consistent with scope. | `application-prod.yml`; `StartupTestHarness`. |
| F-R07 | Info | Developer notes a pre-existing `open-in-view` default warning in logs (OT-04). Unrelated to T1.4 and not a defect in this task's scope. | `developer-report.md` Section 12. |

No Critical or Major findings. No blocking defects found.

## 10. Decisions and reasoning

1. **PASS recommendation.** The implementation satisfies the T1.4 acceptance criteria verifiable in Phase 1: H2 dev startup, Flyway initialization and recorded baseline version 1 (reported), comment-only baseline migration, `ddl-auto: validate` in common configuration, dev-only H2 Console configuration, externalized prod datasource variables, prod-profile configuration tests passing through H2 overrides (reported), unchanged JWT/profile behavior, and no business tables or Docker configuration. PENDING items (real PostgreSQL integration) are explicitly deferred to Phase 5 per scope.
2. **F-T02 and F-T03 are valid but non-blocking.** Both are coverage/verification gaps, not implementation defects. Per review policy, minor coverage gaps do not block approval. They are suitable for optional test hardening in a later phase or by the Tech Lead's discretion; no application code change is required for T1.4 acceptance.
3. **F-R03 (combined-profile H2 Console) is a documentation/consistency gap, not a production exposure.** Standalone `prod` does not configure the H2 Console, so production is not exposed. The Architect's claim that combined profiles "do not expose it" is inaccurate because combined activation includes `dev`. The finding is Minor because combined `dev,prod` is not a supported production deployment mode and is already documented as residual-risk behavior for demo-user gating. If the Tech Lead wants strict combined-profile hardening, the H2 Console would need to be gated independently of the dev profile (for example, via a separate profile or conditional configuration), but that is beyond T1.4 scope.
4. **F-R04 is out of T1.4 scope but worth noting.** H2 Console usability depends on Phase 3 security configuration. The console property is correctly separated by profile, which is what T1.4 requires.
5. **Migration ownership is correct.** Flyway owns schema changes (`flyway.enabled: true`, `locations: classpath:db/migration`, `V1__baseline.sql`), and Hibernate is set to `validate` only. `spring.sql.init.mode: never` prevents Spring Boot from also running `schema.sql`/`data.sql`. No Hibernate table generation is used. VERIFIED by configuration.
6. **Secret/credential handling is correct.** Production datasource values use `${SPRING_DATASOURCE_*}` placeholders with no defaults, so missing variables fail startup. No credentials are committed in any profile file. Dev uses an empty H2 password (acceptable for local H2). VERIFIED by configuration.
7. **Scope compliance is good.** No business entities, repositories, tables, Dockerfiles, Compose files, new Java abstractions, or `pom.xml` changes were introduced. The only source artifacts changed are environment-neutral/common YAML, profile YAML, a comment-only migration, and test-harness/test adjustments required by the prod datasource. VERIFIED by inspected files and reported `git diff` scope.
8. **Unnecessary complexity assessment.** The implementation is minimal and relies on Spring Boot auto-configuration from properties, as approved. No Java configuration classes were added. The H2 override in the test harness is the smallest change needed to keep prod-profile tests deterministic without PostgreSQL. No overengineering identified.

## 11. Final status

**PASS (reviewer recommendation) — no Critical or Major findings; no blocking defects.**

T1.4 is ready for Tech Lead acceptance consideration, subject to the Minor verification/consistency gaps above (F-R01–F-R04) and the explicitly deferred PostgreSQL integration verification (Phase 5). Final acceptance remains the Tech Lead's decision.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | PENDING VERIFICATION | PostgreSQL integration against a real PostgreSQL instance | Phase 5 (T5.2) |
| OT-02 | Verification gap (Minor) | Direct assertion of `flyway_schema_history` version 1 and effective H2 Console property | Optional test hardening (Tech Lead discretion) |
| OT-03 | Verification gap (Minor) | Prod fail-fast behavior when datasource variables are missing | Optional test hardening (Tech Lead discretion) |
| OT-04 | Consistency gap (Minor) | H2 Console remains enabled under combined `dev,prod` despite Architect claim | Optional profile hardening or documentation correction |
| OT-05 | Usability (Minor) | H2 Console reachability depends on Phase 3 security configuration | Phase 3 (T3.1/T3.3) |
| OT-06 | Deferred | Business tables and entity mappings | T2.1 |

## 13. Handoff to next agent

- **To the Tech Lead:** T1.4 passes this independent review with no Critical or Major findings and no blocking defects. Minor gaps F-R01–F-R04 and deferred PostgreSQL integration (Phase 5) are documented; none blocks approval under the stated review policy. Final acceptance decision remains with the Tech Lead.
- **To the Developer:** No code changes required from this review. If the Tech Lead requests hardening for F-R01–F-R03, add focused tests only; no production code change is expected.
- **To the Tester:** If authorized, optionally add tests for `flyway_schema_history` version 1, effective H2 Console property under dev vs. prod, and prod fail-fast when datasource variables are missing. Keep tests deterministic and free of PostgreSQL.
- **To the Architect:** Correct the combined-profile H2 Console claim in Section 10.3 item 4 or accept combined activation as a dev-inclusive mode.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts and reported command output only. All 13 mandatory sections present.
