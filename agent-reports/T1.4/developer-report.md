# Developer Report — T1.4 Persistence and Migration Baseline

## 1. Task ID

**T1.4 — Persistence and migration baseline** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 12.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.4-EXEC-002-DEV-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None (first T1.4 Developer execution; no prior `agent-reports/T1.4/developer-report.md` existed) |
| Predecessor archives | None required |
| Architect proposal | `agent-reports/T1.4/architect-proposal.md` (`T1.4-EXEC-001-ARCH-01`) |

## 3. Agent role and model

- **Role:** Developer (senior Java and Spring Boot developer)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/developer.md`

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 and `implementation-plan.md` T1.4:

**Implemented:**
1. H2 datasource for dev.
2. PostgreSQL datasource for prod using environment variables.
3. Flyway migrations enabled.
4. Hibernate `ddl-auto: validate`.
5. H2 Console enabled only in dev.
6. Existing startup tests updated where necessary.
7. Existing JWT and profile behavior kept unchanged.

**Adjustments applied:**
- No unnecessary SQL operations were added solely to demonstrate Flyway. The baseline migration is comment-only (`V1__baseline.sql`), which Flyway accepted (see Section 8).
- H2 overrides were added to existing prod-profile tests so they do not require a running PostgreSQL instance.
- PostgreSQL integration verification is left for Phase 5.

**Scope restrictions honored:**
- No business entities or tables were created.
- No Docker Compose was added.
- No new abstractions or infrastructure were introduced.
- No unrelated functionality was modified.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section report format, evidence markers, history convention |
| `agent-reports/T1.4/architect-proposal.md` | VERIFIED | Approved design and proposed YAML/migration |
| `agent-reports/decisions.md` | VERIFIED | D-06 confirms T1.3 accepted; T1.4 authorized by current tasking |
| `implementation-plan.md` (T1.4, lines 120–132) | VERIFIED | Task scope and acceptance criteria |
| `requirements-decisions.md` (Section 12, lines 549–596) | VERIFIED | H2/dev, PostgreSQL/prod, Flyway, `ddl-auto: validate` |
| `pom.xml` | VERIFIED | Flyway, H2, PostgreSQL already present from T1.2; no change made |
| `src/main/resources/application.yml` | VERIFIED | Common profile default `dev`; no datasource before change |
| `src/main/resources/application-dev.yml` | VERIFIED | No datasource before change |
| `src/main/resources/application-prod.yml` | VERIFIED | No datasource before change |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | Prod externals supplied no datasource before change |
| `src/test/java/com/humanit/challenge/config/ProdProfileStartupTest.java` | VERIFIED | Prod profile binding tests |
| `src/test/java/com/humanit/challenge/config/CombinedDevProdProfilesTest.java` | VERIFIED | Combined profile test |
| `src/test/java/com/humanit/challenge/config/JwtSecretValidationTest.java` | VERIFIED | Prod-without-secret fail-fast test |

## 6. Files changed

| File | Change |
|---|---|
| `src/main/resources/application.yml` | **Modified** — added common, environment-neutral JPA/Flyway/SQL-init settings (`ddl-auto: validate`, Flyway enabled, locations, `sql.init.mode: never`) |
| `src/main/resources/application-dev.yml` | **Modified** — added H2 in-memory datasource and dev-only H2 Console |
| `src/main/resources/application-prod.yml` | **Modified** — added PostgreSQL datasource bound to `SPRING_DATASOURCE_*` environment variables |
| `src/main/resources/db/migration/V1__baseline.sql` | **Created** — comment-only Flyway baseline migration (no SQL operations) |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | **Modified** — `supplyProdExternals` and `prodProperties()` now supply an in-memory H2 datasource override for prod-profile configuration tests; `prodProperties()` no longer carries dev-only demo-user keys (resolves latent R-M04 inconsistency) |
| `src/test/java/com/humanit/challenge/config/CombinedDevProdProfilesTest.java` | **Modified** — combined profile test now also supplies prod datasource externals so the prod profile can start against H2 |
| `agent-reports/T1.4/developer-report.md` | **Created** — this report |

No changes to `pom.xml`, main Java source, requirements, planning documents, or other agents' reports.

## 7. Commands executed

| Command | Result | Evidence |
|---|---|---|
| `.\mvnw.cmd test` | Exit code 0 (first run after changes) | `C:\Users\pedro\AppData\Local\Temp\opencode\mvn-test2.log` |
| `.\mvnw.cmd clean verify` | Exit code 0; `BUILD SUCCESS`; total time 27.846 s | `C:\Users\pedro\AppData\Local\Temp\opencode\mvn-verify.log` |

Test summary (clean verify):
- Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
- Java runtime: 21.0.12.1 (VERIFIED in log)
- Maven Enforcer `requireJavaVersion [21,22)` and `requireMavenVersion [3.10.0,)`: passed (VERIFIED)

## 8. Evidence and observed results

### 8.1 Configuration

**Common (`application.yml`):**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
  sql:
    init:
      mode: never
```
(VERIFIED by file content.)

**Development (`application-dev.yml`):**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:humanit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
```
(VERIFIED by file content.)

**Production (`application-prod.yml`):**
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
```
(VERIFIED by file content.)

### 8.2 Flyway initialization

Flyway ran successfully in the H2/dev contexts. Observed log lines (VERIFIED):
- `org.flywaydb.core.FlywayExecutor : Database: jdbc:h2:mem:humanit (H2 2.3)`
- `o.f.core.internal.command.DbValidate : Successfully validated 1 migration (execution time ...)`
- `o.f.core.internal.command.DbMigrate : Current version of schema "PUBLIC": 1`
- `o.f.core.internal.command.DbMigrate : Schema "PUBLIC" is up to date. No migration necessary.`

The comment-only `V1__baseline.sql` was accepted as a Flyway migration and recorded as version 1 in `flyway_schema_history`; no SQL operation was executed. (VERIFIED by log.)

### 8.3 H2 Console policy

The H2 Console is configured only in `application-dev.yml` (`spring.h2.console.enabled: true`). It is absent from `application.yml` and `application-prod.yml`, so it is not exposed under `prod`. (VERIFIED by file contents and `SourceScanAuditTest` passing.)

### 8.4 Profile behavior

- Default-dev context (`HumanItChallengeApplicationTests`, `DefaultDevProfileStartupTest`) starts with H2 and Flyway; passes.
- Explicit dev context (`ExplicitDevProfileStartupTest`) passes.
- Prod context (`ProdProfileStartupTest`) starts against the in-memory H2 override; passes (4 tests).
- Combined `dev,prod` context (`CombinedDevProdProfilesTest`) passes (2 tests).

(VERIFIED by clean verify: 31 tests, 0 failures.)

### 8.5 JWT and profile behavior

No changes were made to JWT validation, profile activation, or demo-user gating logic. The existing T1.3 tests continue to pass, including `JwtSecretValidationTest` (31 tests total, 0 failures). (VERIFIED.)

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-D01 | Info | Comment-only baseline migration is supported by the installed Flyway version (3.5.x-managed); no SQL placeholder operation was needed. |
| F-D02 | Info | `prodProperties()` was aligned with its Javadoc during this change (no demo-user keys), resolving the latent R-M04 harness inconsistency. |
| F-D03 | Info | PostgreSQL integration against a real PostgreSQL instance was not exercised; it is deferred to Phase 5 per scope. |

No Critical or Major findings.

## 10. Decisions and reasoning

1. **Comment-only baseline migration chosen.** Per the Tech Lead adjustment, `V1__baseline.sql` contains only comments. Flyway accepted it and recorded schema version 1, proving the migration pipeline works without introducing SQL that does not correspond to a real schema change. Business schema remains T2.1 scope.
2. **Common JPA/Flyway settings in `application.yml`.** `ddl-auto: validate` ensures Hibernate never creates or alters tables; Flyway owns schema changes. `sql.init.mode: never` prevents Spring Boot from also running `schema.sql`/`data.sql`.
3. **H2 for dev, PostgreSQL for prod.** Dev uses an in-memory H2 with `DB_CLOSE_DELAY=-1` so the Flyway-created schema persists across the test JVM. Prod reads `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` and uses the PostgreSQL driver; no credentials are committed.
4. **H2 Console dev-only.** Enabled only in `application-dev.yml`; absent from prod and common config.
5. **H2 overrides in prod-profile tests.** `StartupTestHarness.supplyProdExternals` and `prodProperties()` supply an in-memory H2 datasource (and `SPRING_DATASOURCE_*` so the prod YAML placeholders resolve) plus the H2 driver override, keeping configuration verification deterministic without PostgreSQL. This matches the adjustment and preserves fail-fast production behavior.
6. **No `pom.xml` change.** T1.2 already provides `flyway-core`, `flyway-database-postgresql`, `h2`, and `postgresql`.
7. **No Java configuration.** Spring Boot auto-configures datasource, JPA, Flyway, and the H2 Console from properties, per the approved design.

## 11. Final status

**IMPLEMENTED — VERIFIED on H2/dev.** All 31 tests pass (`.\mvnw.cmd clean verify`, exit code 0, `BUILD SUCCESS`). Flyway initializes and records the comment-only baseline migration as version 1. Hibernate schema validation is enabled in both profiles. H2 Console is dev-only. Prod-profile configuration tests pass against an in-memory H2 override.

PostgreSQL integration verification against a real PostgreSQL instance remains **PENDING VERIFICATION** (Phase 5 / T5.2), as scoped.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | PENDING VERIFICATION | PostgreSQL integration verification against a real PostgreSQL instance | Phase 5 (T5.2) |
| OT-02 | Deferred | Business tables and entity mappings | T2.1 |
| OT-03 | Deferred | Docker Compose delivery | Phase 5 |
| OT-04 | Info | `open-in-view` default warning in logs (pre-existing, unrelated to T1.4) | Not in scope |

## 13. Handoff to next agent

- **To the Tester:** verify independently before reading this report, using the criteria in `architect-proposal.md` Section 10.8; confirm Flyway applies `V1__baseline.sql` as version 1, `ddl-auto: validate` is effective in both profiles, H2 Console is dev-only, and prod fail-fast behavior for missing datasource variables.
- **To the Reviewer:** audit migration ownership (Flyway, not Hibernate), profile separation, secret/credential handling, absence of Hibernate table generation, and scope compliance (no entities, no Docker, no new abstractions).
- **To the Tech Lead:** T1.4 implementation is complete and verified on H2; PostgreSQL integration remains deferred to Phase 5 per scope.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts and verified command output only. All 13 mandatory sections present.