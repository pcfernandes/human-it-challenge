# Tester Report — T1.4 Persistence and Migration Baseline

## 1. Task ID

**T1.4 — Persistence and migration baseline** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 12.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.4-EXEC-003-TST-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None (first T1.4 Tester execution; no prior `agent-reports/T1.4/tester-report.md` existed) |
| Predecessor archives | None required |
| Architect proposal | `agent-reports/T1.4/architect-proposal.md` (`T1.4-EXEC-001-ARCH-01`) |
| Implementation inspected | Working tree on branch `feature/T1.4-database` (independent verification before reading the Developer report) |

## 3. Agent role and model

- **Role:** Tester (senior Java and Spring Boot QA engineer)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/tester.md`

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09:

- Independently inspect the actual implementation and tests.
- Verify H2 dev datasource, Flyway baseline, SQL content, Hibernate `ddl-auto`, H2 Console policy, prod external variables, prod-profile tests, JWT/profile behavior, and absence of business tables/Docker configuration.
- Run `.\mvnw.cmd clean verify` and record the actual result.
- Report meaningful verification gaps.
- Do not modify application code or tests.
- Do not require a running PostgreSQL instance.

No application code, YAML, tests, `pom.xml`, requirements, planning documents, or Git state were modified by this execution. The only file created is this report.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section report format, evidence markers, history convention |
| `agent-reports/T1.4/architect-proposal.md` | VERIFIED | Approved design and acceptance criteria |
| `implementation-plan.md` (T1.4, lines 120–132) | VERIFIED | Task scope and acceptance criteria |
| `requirements-decisions.md` (Section 12, lines 549–596) | VERIFIED | H2/dev, PostgreSQL/prod, Flyway, `ddl-auto: validate` |
| `pom.xml` | VERIFIED | Flyway, H2, PostgreSQL dependencies already present; no change in this task |
| `src/main/resources/application.yml` | VERIFIED | Common `ddl-auto: validate`, Flyway enabled, locations, `sql.init.mode: never` |
| `src/main/resources/application-dev.yml` | VERIFIED | H2 datasource and dev-only H2 Console |
| `src/main/resources/application-prod.yml` | VERIFIED | PostgreSQL datasource bound to `SPRING_DATASOURCE_*` |
| `src/main/resources/db/migration/V1__baseline.sql` | VERIFIED | Comment-only Flyway baseline migration |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | Prod H2 override for configuration tests |
| `src/test/java/com/humanit/challenge/config/ProdProfileStartupTest.java` | VERIFIED | Prod profile startup and binding tests |
| `src/test/java/com/humanit/challenge/config/CombinedDevProdProfilesTest.java` | VERIFIED | Combined profile behavior documentation |
| `src/test/java/com/humanit/challenge/config/DefaultDevProfileStartupTest.java` | VERIFIED | Default dev startup behavior |
| `src/test/java/com/humanit/challenge/config/ExplicitDevProfileStartupTest.java` | VERIFIED | Explicit dev startup behavior |
| `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java` | VERIFIED | Default-dev context load |
| `target/surefire-reports/*.txt` | VERIFIED | Actual per-class test counts |
| Git working tree (`git status`, `git diff`) | VERIFIED | Scope compliance: no business tables, no Docker configuration, no unrelated source changes |

The Developer report (`agent-reports/T1.4/developer-report.md`) was **not** opened until after the verification in Section 7 and Section 8 was complete.

## 6. Files changed

None, except this report (`agent-reports/T1.4/tester-report.md`).

No application code, tests, YAML, SQL, `pom.xml`, requirements, planning documents, or Git state were modified.

## 7. Commands executed

| Command | Result | Evidence |
|---|---|---|
| `.\mvnw.cmd clean verify` | Exit code 0; `BUILD SUCCESS`; total time `27.100 s` | Console output captured for this execution; `target/surefire-reports/` |
| `git status --short` | Modified: 5 tracked files; untracked: `agent-reports/T1.4/` and `src/main/resources/db/` only | Console output |
| `git diff --stat` | 5 files changed, 48 insertions, 5 deletions; no Dockerfiles or business entities | Console output |

Maven Enforcer checks passed: `requireJavaVersion [21,22)` and `requireMavenVersion [3.10.0,)` (VERIFIED in build output).

## 8. Evidence and observed results

### 8.1 H2 datasource in the default dev profile

- Default-dev context tests pass:
  - `HumanItChallengeApplicationTests`: 1 test, 0 failures
  - `DefaultDevProfileStartupTest`: 3 tests, 0 failures
- Flyway connects to `jdbc:h2:mem:humanit (H2 2.3)` during those contexts (VERIFIED in build log).
- H2 datasource properties are present only in `application-dev.yml` (VERIFIED by file content).

### 8.2 Flyway initializes and records `V1__baseline.sql`

Observed Flyway log lines during the dev contexts (VERIFIED):

- `org.flywaydb.core.FlywayExecutor : Database: jdbc:h2:mem:humanit (H2 2.3)`
- `o.f.core.internal.command.DbValidate : Successfully validated 1 migration`
- `o.f.core.internal.command.DbMigrate : Current version of schema "PUBLIC": 1`
- `o.f.core.internal.command.DbMigrate : Schema "PUBLIC" is up to date. No migration necessary.`

The single migration is `V1__baseline.sql`, located at `src/main/resources/db/migration/` (VERIFIED by file path and `spring.flyway.locations: classpath:db/migration`). Flyway therefore recorded schema version 1. Direct querying of `flyway_schema_history` was not performed (see Section 9, F-T02).

### 8.3 Baseline migration contains no unnecessary SQL

`V1__baseline.sql` contains only comments and no SQL statements (VERIFIED by file content). Flyway accepted it as a valid migration and recorded version 1. No tables, indexes, constraints, seed data, or vendor-specific SQL were introduced.

### 8.4 Hibernate `ddl-auto` is set to `validate`

`src/main/resources/application.yml` contains (VERIFIED):

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This is common to both profiles because it is in the shared `application.yml`. `spring.sql.init.mode: never` is also set, preventing Spring Boot from running `schema.sql`/`data.sql` alongside Flyway.

### 8.5 H2 Console is enabled only for dev, not prod

- `application-dev.yml` contains `spring.h2.console.enabled: true` and `path: /h2-console` (VERIFIED).
- `application.yml` and `application-prod.yml` do not contain H2 Console configuration (VERIFIED by file contents).
- Under an explicit `prod` profile, the H2 Console auto-configuration property is therefore absent/false by default.

### 8.6 Prod datasource uses external variables

`application-prod.yml` contains (VERIFIED):

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

No datasource URL, username, password, or PostgreSQL driver version is committed. The values come from environment variables.

### 8.7 Existing prod-profile tests work using H2 overrides

- `ProdProfileStartupTest`: 4 tests, 0 failures, 0 errors (VERIFIED).
- The prod profile starts with the in-memory H2 override supplied by `StartupTestHarness.supplyProdExternals` and `prodProperties()` (VERIFIED by harness code and passing tests).
- `CombinedDevProdProfilesTest`: 2 tests, 0 failures (VERIFIED).

### 8.8 JWT and profile behavior remain unchanged

- T1.3 JWT/profile tests continue to pass:
  - `JwtSecretValidationTest`: 6 tests, 0 failures
  - `DemoUserPropertiesTest`: (included in the 31-test run)
  - `PaginationPropertiesValidationTest`: 5 tests, 0 failures
  - `Base64DecodedMinBytesValidatorTest`: 6 tests, 0 failures
- No changes were made to JWT validation, profile activation, or demo-user gating logic (VERIFIED by `git diff`).
- Total test result: **Tests run: 31, Failures: 0, Errors: 0, Skipped: 0** (VERIFIED).

### 8.9 No business tables or Docker configuration were added

- `git status` shows only configuration, test-harness, migration, and report changes (VERIFIED).
- The only new source artifact is `src/main/resources/db/migration/V1__baseline.sql`, which is comment-only (VERIFIED).
- No `Dockerfile`, `compose.yaml`, or Docker Compose file exists in the project root (VERIFIED by file search excluding unrelated `.opencode` contents).
- No JPA entities, repositories, or business tables were introduced (VERIFIED by source tree and `git diff`).

## 9. Findings and severity

| ID | Severity | Finding | Evidence |
|---|---|---|---|
| F-T01 | Info | Flyway baseline migration is comment-only and contains no SQL operations; Flyway accepted it and recorded schema version 1. | `V1__baseline.sql`; build log |
| F-T02 | Minor (verification gap) | No automated test directly queries `flyway_schema_history` or asserts the effective H2 Console property value; Flyway version 1 and console policy are inferred from configuration and build logs. | `target/surefire-reports/`; config files |
| F-T03 | Minor (verification gap) | No automated test verifies that prod startup fails when `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, or `SPRING_DATASOURCE_PASSWORD` are missing; only the H2 override path is exercised. | `ProdProfileStartupTest`; `StartupTestHarness` |
| F-T04 | Info | Under combined `dev,prod` activation, the H2 Console remains enabled because the dev profile file loads; this is documented combined-profile behavior and not a standalone prod activation. | `CombinedDevProdProfilesTest`; config files |
| F-T05 | Info | Maven build succeeds with 31 tests, 0 failures, 0 errors, 0 skipped; Java 21 and Maven version enforcements pass. | `.\mvnw.cmd clean verify` output |

No Critical or Major findings. No blocking defects found.

## 10. Decisions and reasoning

1. **PASS recommendation.** The implementation satisfies the T1.4 acceptance criteria that are verifiable in Phase 1: H2 dev startup, Flyway initialization/version 1, comment-only baseline migration, `ddl-auto: validate`, dev-only H2 Console configuration, externalized prod datasource variables, passing prod-profile tests through H2 overrides, unchanged JWT/profile behavior, and no business tables or Docker configuration.
2. **PostgreSQL remains PENDING VERIFICATION.** Real PostgreSQL integration is intentionally deferred to Phase 5 (T5.2), as stated in `implementation-plan.md` and `requirements-decisions.md`. No running PostgreSQL instance was required or used.
3. **Comment-only migration accepted.** Although the Architect proposal suggested `SELECT 1;`, the Developer's comment-only migration is sufficient and better matches the requirement that the baseline contain no unnecessary SQL. Flyway accepted it and recorded version 1, proving the migration pipeline.
4. **Verification gaps are non-blocking.** F-T02 and F-T03 are coverage/documentation gaps, not implementation defects. They can be addressed by the Reviewer/Tech Lead or in later phases without changing the T1.4 implementation.
5. **No code or test changes made.** This execution was verification-only, per the Tester role rules.

## 11. Final status

**PASS — no blocking defects.**

All 31 automated tests pass via `.\mvnw.cmd clean verify` (`BUILD SUCCESS`, exit code 0). The persistence and migration baseline is correctly configured for H2 in dev and externalized PostgreSQL in prod, with Flyway owning schema changes and Hibernate set to `validate`.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | PENDING VERIFICATION | PostgreSQL integration against a real PostgreSQL instance | Phase 5 (T5.2) |
| OT-02 | Verification gap | Direct assertion of `flyway_schema_history` version 1 and H2 Console property policy | Reviewer / future test hardening |
| OT-03 | Verification gap | Prod fail-fast behavior when datasource variables are missing | Reviewer / future test hardening |
| OT-04 | Deferred | Business tables and entity mappings | T2.1 |

## 13. Handoff to next agent

- **To the Reviewer:** audit migration ownership (Flyway, not Hibernate), profile separation, secret/credential handling, absence of Hibernate table generation, scope compliance (no entities, no Docker, no new abstractions), and the verification gaps F-T02/F-T03.
- **To the Tech Lead:** T1.4 passes automated verification on H2; PostgreSQL integration remains deferred to Phase 5 per scope. No blocking defects found.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts and verified command output only. All 13 mandatory sections present.
