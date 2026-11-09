# Architect Proposal — T1.4 Persistence and Migration Baseline

## 1. Task ID

**T1.4 — Persistence and migration baseline** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 12.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.4-EXEC-001-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None (first T1.4 Architect execution) |
| Predecessor archives | None required (no prior `agent-reports/T1.4/architect-proposal.md`) |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot software architect)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/architect.md` — `edit` allow for `agent-reports/**` only; `bash: deny` (VERIFIED)

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 and `implementation-plan.md` T1.4:

**In scope:**
- PostgreSQL configuration for development/runtime production use and H2 for development/tests.
- Flyway enablement and migration directory structure.
- Hibernate `ddl-auto: validate`.
- H2 Console enabled only in the `dev` profile.
- Clear separation of database configuration by environment.
- Simple local database startup instructions.

**Out of scope:**
- Business tables and entity mappings (T2.1).
- Docker Compose and container delivery (Phase 5).
- Production infrastructure hardening, pooling tuning, or secret-management changes.
- New application Java code or `pom.xml` changes (dependencies already added in T1.2).

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `implementation-plan.md` (T1.4, lines 120–132) | VERIFIED | Task scope, acceptance criteria, PostgreSQL verification boundary |
| `requirements-decisions.md` (Section 12, lines 549–596) | VERIFIED | H2/dev, PostgreSQL/prod, Flyway, `ddl-auto: validate`, migration compatibility |
| `agent-reports/README.md` | VERIFIED | 13-section report format and evidence markers |
| `agent-reports/decisions.md` (D-06) | VERIFIED | T1.3 accepted; T1.4 not yet authorized |
| `pom.xml` | VERIFIED | Flyway core, `flyway-database-postgresql`, H2, PostgreSQL already present from T1.2 |
| `src/main/resources/application.yml` | VERIFIED | Common profile default `dev`; no datasource yet (placeholder comment) |
| `src/main/resources/application-dev.yml` | VERIFIED | No datasource yet (placeholder comment) |
| `src/main/resources/application-prod.yml` | VERIFIED | No datasource yet (placeholder comment) |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | Prod startup tests supply only JWT/demo externals; no datasource |
| `src/test/java/com/humanit/challenge/config/ProdProfileStartupTest.java` | VERIFIED | Prod profile binding tests will need a datasource after T1.4 |
| `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java` | VERIFIED | Default-dev context-load test; will exercise Flyway + H2 after T1.4 |

## 6. Files changed

| File | Change |
|---|---|
| `agent-reports/T1.4/architect-proposal.md` | **Created** — this proposal |

No application code, YAML, `pom.xml`, tests, requirements, planning documents, or Git state were modified. The proposed implementation changes are listed in Section 10.1 for later authorized execution.

## 7. Commands executed

**None.** `bash` is denied for this agent. No build or test command was run; all facts are taken from inspected files.

## 8. Evidence and observed results

- **VERIFIED:** T1.2 already provides all required database dependencies (`flyway-core`, `flyway-database-postgresql`, `h2`, `postgresql`) in `pom.xml`.
- **VERIFIED:** T1.3 established `spring.profiles.default: dev` in common `application.yml` and separate `application-dev.yml` / `application-prod.yml` files.
- **VERIFIED:** No datasource, Flyway, JPA, or H2 console configuration exists yet; profile files explicitly defer it to T1.4.
- **VERIFIED:** No migration directory or SQL migration exists yet.
- **VERIFIED:** `ProdProfileStartupTest` activates `prod` and supplies only JWT externals via `StartupTestHarness.supplyProdExternals`; it has no datasource override today.
- **RECONSTRUCTED:** Adding required production datasource properties will make prod-profile startup tests fail unless the test harness supplies an H2 override, because no PostgreSQL instance is assumed in automated tests.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-01 | Minor | Database configuration is entirely absent; profile files contain only deferred-work comments. |
| F-02 | Minor | Production datasource must be required in `prod`, but automated prod-profile tests need a deterministic H2 override to remain green without PostgreSQL. |
| F-03 | Minor | A Flyway baseline migration is needed so the migration pipeline is exercised in both H2 and PostgreSQL. |
| F-04 | Info | No `pom.xml` change is required; T1.2 already locked the needed dependencies. |

No Critical or Major findings.

## 10. Decisions and reasoning (alternatives, trade-offs, assumptions)

### 10.1 Proposed files to create or modify

**Modify:**
- `src/main/resources/application.yml` — common, environment-neutral JPA/Flyway settings.
- `src/main/resources/application-dev.yml` — H2 datasource and dev-only H2 Console.
- `src/main/resources/application-prod.yml` — PostgreSQL datasource from environment variables.

**Create:**
- `src/main/resources/db/migration/V1__baseline.sql` — portable Flyway baseline placeholder.

**No change:**
- `pom.xml` (dependencies already present from T1.2).

### 10.2 Proposed configuration

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
Keep the existing `spring.application.name`, `spring.profiles.default: dev`, and `app.*` settings unchanged.

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

**Production (`application-prod.yml`):**
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

**Baseline migration (`V1__baseline.sql`):**
```sql
-- Flyway baseline migration (T1.4).
-- No application tables are created here; business schema arrives in T2.1.
-- This proves the migration pipeline works in both H2 (dev/test) and PostgreSQL (prod).
SELECT 1;
```

### 10.3 Decisions and rationale

1. **H2 for dev/tests, PostgreSQL for prod.** Matches `requirements-decisions.md` Section 12 and keeps the default dev experience dependency-free.
2. **Hibernate validates only.** `ddl-auto: validate` prevents Hibernate from creating or altering tables; Flyway owns schema changes.
3. **Flyway owns initialization.** `spring.sql.init.mode: never` avoids Spring Boot running `schema.sql`/`data.sql` alongside Flyway.
4. **H2 Console is dev-only.** It is enabled only in `application-dev.yml`; production and combined profiles do not expose it.
5. **Required production credentials.** Production datasource values come from `SPRING_DATASOURCE_*` environment variables and are not committed.
6. **Portable baseline migration.** `V1__baseline.sql` uses `SELECT 1`, which runs on both H2 and PostgreSQL. Future migrations should remain portable; if a change is database-specific, use Flyway database-specific locations (for example `classpath:db/migration/{vendor}`) rather than hard-coding vendor assumptions in the common path.
7. **No `pom.xml` change.** T1.2 already added all required drivers and Flyway modules.
8. **No Java configuration.** Spring Boot auto-configures datasource, JPA, Flyway, and the H2 Console from properties.

### 10.4 Alternatives considered

- **Create business tables now.** Rejected: explicitly out of scope for T1.4; schema belongs to T2.1.
- **Use Hibernate `ddl-auto: update` or `create`.** Rejected: violates the approved `ddl-auto: validate` requirement and hides migration defects.
- **Commit a development PostgreSQL URL or credentials.** Rejected: conflicts with externalized secret/credential handling approved in T1.3.
- **Add Spring Boot Docker Compose support now.** Rejected: container delivery is Phase 5 scope; local PostgreSQL instructions are sufficient for T1.4.
- **Leave the migration directory empty.** Rejected: an explicit baseline migration proves Flyway execution in both supported databases.

### 10.5 Trade-offs and assumptions

- **Trade-off:** Requiring a production datasource makes `ProdProfileStartupTest` depend on a datasource override in tests. This is accepted because it keeps production fail-fast while preserving deterministic automated tests.
- **Trade-off:** The H2 Console improves local debugging but is limited to dev to avoid exposing database tooling in production.
- **Assumption (PENDING VERIFICATION):** Spring Boot 3.5.x auto-configures Flyway with the proposed properties and applies `V1__baseline.sql` successfully on H2.
- **Assumption (PENDING VERIFICATION):** Overriding `spring.datasource.*` through `@DynamicPropertySource` in `ProdProfileStartupTest` is sufficient to start the prod profile against H2 for configuration verification.
- **Assumption (PENDING VERIFICATION):** PostgreSQL integration is verified manually or in Phase 5, not during routine Phase 1 test execution.

### 10.6 Test impact and expected verification

The Tester should verify at least:
- Default-dev and explicit-dev context startup with Flyway enabled and H2.
- Production profile binding with a test datasource override.
- `ddl-auto: validate` is effective in both profiles.
- H2 Console is available only under `dev`.
- Flyway applies `V1__baseline.sql` and records it in `flyway_schema_history`.
- Missing production datasource variables fail startup in prod.

Recommended test-harness update:
- Extend `StartupTestHarness.supplyProdExternals` (or add a focused helper) to supply an in-memory H2 datasource override for prod-profile configuration tests:
  - `spring.datasource.url=jdbc:h2:mem:humanit-prod-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - `spring.datasource.driver-class-name=org.h2.Driver`
  - `spring.datasource.username=sa`
  - `spring.datasource.password=`

### 10.7 Local database startup instructions

**Development (H2, default profile):**
1. No database setup is required.
2. Set `APP_SECURITY_JWT_SECRET` and, for dev, `APP_SECURITY_DEMO_USER_USERNAME` / `APP_SECURITY_DEMO_USER_PASSWORD`.
3. Run `.\mvnw.cmd spring-boot:run`.
4. H2 Console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:humanit`).

**Production (PostgreSQL):**
1. Start PostgreSQL locally, for example:
   ```text
   docker run --name humanit-postgres -e POSTGRES_DB=humanit -e POSTGRES_USER=humanit -e POSTGRES_PASSWORD=humanit -p 5432:5432 -d postgres:16
   ```
2. Set environment variables:
   - PowerShell:
     ```powershell
     $env:SPRING_PROFILES_ACTIVE="prod"
     $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/humanit"
     $env:SPRING_DATASOURCE_USERNAME="humanit"
     $env:SPRING_DATASOURCE_PASSWORD="humanit"
     $env:APP_SECURITY_JWT_SECRET="<standard Base64, at least 32 decoded bytes>"
     ```
3. Run `.\mvnw.cmd spring-boot:run`.
4. Flyway applies `db/migration` automatically on startup.

Full Docker Compose delivery remains Phase 5 scope.

### 10.8 Basic acceptance criteria

- Application starts with H2 in `dev` (default profile).
- Flyway runs successfully in `dev`.
- Hibernate schema validation is enabled in both profiles.
- H2 Console is available only in `dev`.
- Production profile uses PostgreSQL and requires externalized credentials.
- No Hibernate table generation is used.
- Migration directory and baseline migration exist.
- PostgreSQL integration verification requires an available PostgreSQL instance and is not assumed to occur automatically during Phase 1.
- Full Docker Compose verification remains in Phase 5.

### 10.9 Dependencies on previous tasks

- **T1.1:** Provides the runnable Spring Boot baseline, default `dev` profile behavior, and package structure.
- **T1.2:** Provides all required database dependencies (`flyway-core`, `flyway-database-postgresql`, `h2`, `postgresql`).
- **T1.3:** Provides profile separation and externalized configuration; T1.4 replaces the deferred-datasource comments in those profile files with concrete settings.

## 11. Final status

**DELIVERED — PROPOSAL PERSISTED, PENDING TECH LEAD APPROVAL.**

This proposal is design-only. No implementation was performed, no application code or configuration was modified, and no Git operations were executed. Approval and implementation authorization remain with the human Tech Lead.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Awaiting approval | Explicit Tech Lead approval of this T1.4 proposal before implementation | Tech Lead |
| OT-02 | Awaiting authorization | Apply the proposed YAML and migration changes | Developer (after authorization) |
| OT-03 | Awaiting authorization | Update `StartupTestHarness` for prod-profile datasource override | Developer (after authorization) |
| OT-04 | PENDING VERIFICATION | Verify Flyway runs on H2 and PostgreSQL with the baseline migration | Tester |
| OT-05 | PENDING VERIFICATION | Verify `ddl-auto: validate` and H2 Console profile policy | Tester |
| OT-06 | Deferred | Full PostgreSQL and Docker Compose verification | Phase 5 |

## 13. Handoff to next agent

- **To the Tech Lead:** review and approve or reject this proposal; authorize implementation only after approval.
- **To the Developer:** implement only the approved scope in Section 10.1; do not add entities, security, or Docker changes.
- **To the Tester:** verify independently before reading the Developer report, using the criteria in Section 10.8 and test impact in Section 10.6.
- **To the Reviewer:** audit migration ownership, profile separation, secret/credential handling, and absence of Hibernate table generation.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts only. All 13 mandatory sections present.
