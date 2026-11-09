# Architect Proposal — T1.6 Phase 1 Sanity and Documentation Checkpoint (Revised)

## 1. Task ID

**T1.6 — Phase 1 sanity and documentation checkpoint** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 17.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.6-EXEC-002-ARCH-02` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Author | Architect role |
| Supersedes | `agent-reports/T1.6/architect-proposal.md` (`T1.6-EXEC-001-ARCH-01`) — archived to `agent-reports/T1.6/history/architect-001.md` in this execution |
| Predecessor archives | `agent-reports/T1.6/history/architect-001.md` (created in this execution) |
| Previous-execution correction reason | The previous execution inspected the repository before T1.5 was merged. T1.5 is now present and accepted (D-08). |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot architect)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/architect.md` (`edit` allow for `agent-reports/**` only; `bash: deny` — VERIFIED)

## 4. Authorized scope

**In scope (T1.6 only):**

1. Local application startup documentation.
2. Build and test command documentation.
3. Required Java and Maven versions.
4. Development profile configuration.
5. Production profile configuration.
6. Required environment variables.
7. Database and Flyway baseline documentation.
8. Phase 1 changelog entry (including T1.5 deliverables).
9. Final Phase 1 sanity-check procedure.
10. Acceptance criteria and evidence checklist.

**Out of scope (unchanged):** business entities or endpoints, Client/Document CRUD, authentication implementation, JWT enforcement, Docker infrastructure, PostgreSQL integration tests, React frontend, new application functionality, refactoring existing working code, application code changes, new tests or dependencies.

**Restrictions honored:** no application code, tests, `pom.xml`, Maven Wrapper, requirements, planning documents, existing `reports/*.md`, agent definitions or Git configuration modified; no `README.md` or `CHANGELOG.md` created yet; no commit, push or PR; `decisions.md` not modified; other agents' reports not modified; stale historical reports not modified.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format, evidence markers, history convention |
| `agent-reports/decisions.md` | VERIFIED | D-04…D-08 confirm T1.1–T1.5 accepted; D-08 records T1.5 acceptance |
| `implementation-plan.md` | VERIFIED | T1.6 definition (Section 5), Phase 1 exit criteria, Phase 4 T4.3 README requirements |
| `requirements-decisions.md` | VERIFIED | Sections 11, 12, 14, 16, 17 |
| `agent-reports/T1.5/architect-proposal.md` | VERIFIED | T1.5 design (archived original + decision record) |
| `agent-reports/T1.5/developer-report.md` | VERIFIED | T1.5 implementation and verification (41 tests) |
| `agent-reports/T1.5/tester-report.md` | VERIFIED | Independent Tester verification (PASS, 41/41) |
| `agent-reports/T1.5/reviewer-report.md` | VERIFIED | Independent Reviewer audit (PASS) |
| `agent-reports/T1.6/architect-proposal.md` (previous) | VERIFIED | Prior proposal archived to `history/architect-001.md` |
| `pom.xml` | VERIFIED | Spring Boot 3.5.16, Java 21, enforcer rules, dependencies |
| `.mvn/wrapper/maven-wrapper.properties` | VERIFIED | Wrapper 3.3.4, Maven 3.10.0 |
| `src/main/resources/application.yml` | VERIFIED | Default profile `dev`, `ddl-auto: validate`, Flyway, pagination/security defaults, logging pattern |
| `src/main/resources/application-dev.yml` | VERIFIED | H2 datasource, dev-only H2 Console, logging |
| `src/main/resources/application-prod.yml` | VERIFIED | PostgreSQL datasource from `SPRING_DATASOURCE_*`, logging |
| `src/main/resources/db/migration/V1__baseline.sql` | VERIFIED | Comment-only Flyway baseline migration |
| `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` | VERIFIED | T1.5 Problem Details infrastructure present |
| `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` | VERIFIED | T1.5 OpenAPI bean present |
| `src/test/java/com/humanit/challenge/exception/GlobalExceptionHandlerTest.java` | VERIFIED | 8 MockMvc scenarios |
| `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java` | VERIFIED | OpenAPI bean assertion |
| Root `README.md` | NOT AVAILABLE | No root README exists |
| Root `CHANGELOG*` | NOT AVAILABLE | No changelog file exists |

## 6. Files changed

| File | Change |
|---|---|
| `agent-reports/T1.6/history/architect-001.md` | **Created** — archived copy of the previous primary report (`T1.6-EXEC-001-ARCH-01`), unchanged |
| `agent-reports/T1.6/architect-proposal.md` | **Replaced** — this report (`T1.6-EXEC-002-ARCH-02`) |

No application code, tests, configuration, `pom.xml`, Maven Wrapper, requirements, planning documents, existing `reports/*.md`, other agents' reports, `decisions.md`, or Git state were modified.

## 7. Commands executed

None. This execution was limited to read-only inspection of in-project artifacts. No build, test, startup or shell commands were run (the `bash` tool is denied for this agent). File edits were performed via `edit`/`write` tools on `agent-reports/**` only.

## 8. Evidence and observed results

### 8.1 Verification of T1.5 implementation (current repository)

All eight verification points required by this execution were checked:

| # | Check | Result | Evidence |
|---|---|---|---|
| 1 | T1.5 implementation present | **VERIFIED** | `GlobalExceptionHandler.java` and `OpenApiConfig.java` exist under `src/main`; logging pattern present in `application.yml` |
| 2 | `GlobalExceptionHandler.java` exists | **VERIFIED** | `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` |
| 3 | `OpenApiConfig.java` exists | **VERIFIED** | `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` |
| 4 | T1.5 tests exist | **VERIFIED** | `GlobalExceptionHandlerTest` (8), `OpenApiConfigTest` (1), `SourceScanAuditTest` (2, includes `noTestScaffoldingInProductionSource`) |
| 5 | T1.5 Tester reports 41/41 PASS | **VERIFIED** | `agent-reports/T1.5/tester-report.md` Section 7: `Tests run: 41, Failures: 0, Errors: 0, Skipped: 0`; independent `clean verify` BUILD SUCCESS |
| 6 | T1.5 Reviewer recommends PASS | **VERIFIED** | `agent-reports/T1.5/reviewer-report.md` Section 11: "REVIEW COMPLETE — PASS (reviewer recommendation)", 0 Critical, 0 Major, no blocking defects |
| 7 | T1.5 acceptance D-08 exists in `decisions.md` | **VERIFIED** | `agent-reports/decisions.md` D-08: "T1.5 formally ACCEPTED"; T1.6 authorized as next task |
| 8 | Current branch includes T1.5 changes | **PARTIALLY VERIFIED** | T1.5 source artifacts are present in the working tree (file existence VERIFIED). Branch name/merge status is taken from the task context and the Developer report's branch field (`feature/T1.5-cross-cutting`); no Git tool was available to this agent to confirm the branch or merge commit independently (PENDING VERIFICATION) |

### 8.2 Build and runtime baseline (VERIFIED)

- Spring Boot parent: `3.5.16` (`pom.xml`).
- Java baseline: `java.version=21`; Maven Enforcer `requireJavaVersion [21,22)` and `requireMavenVersion [3.10.0,)` (`pom.xml`).
- Maven entry point: wrapper `3.3.4` pinned to Apache Maven `3.10.0` (`.mvn/wrapper/maven-wrapper.properties`).
- Last verified full test run (T1.5): `Tests run: 41, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (`agent-reports/T1.5/tester-report.md`, Section 7). This supersedes the historical 31-test figure from T1.3/T1.4.
- Standalone startup evidence (T1.1): `Started HumanItChallengeApplication in 0.995 seconds` (PID 6344), Maven exit code `0` (`agent-reports/T1.1/tester-report.md`, Section 8.2).

### 8.3 Configuration baseline (VERIFIED)

- Default profile is `dev` (`spring.profiles.default: dev` in `application.yml`); production requires `SPRING_PROFILES_ACTIVE=prod`.
- Development datasource: H2 in-memory `jdbc:h2:mem:humanit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`, user `sa`, empty password, driver `org.h2.Driver` (`application-dev.yml`).
- H2 Console: enabled only in `application-dev.yml`, path `/h2-console`.
- Production datasource: `${SPRING_DATASOURCE_URL}`, `${SPRING_DATASOURCE_USERNAME}`, `${SPRING_DATASOURCE_PASSWORD}`, driver `org.postgresql.Driver` (`application-prod.yml`).
- Common persistence: `spring.jpa.hibernate.ddl-auto: validate`, `spring.flyway.enabled: true`, `locations: classpath:db/migration`, `spring.sql.init.mode: never`.
- Flyway baseline: `V1__baseline.sql` is comment-only and was recorded as schema version 1 in reported runs.
- JWT secret: externalized via `APP_SECURITY_JWT_SECRET` (required, standard Base64, at least 32 decoded bytes); expiration default 60 minutes (`application.yml`).
- Demo user: configured only under `dev & !prod`; requires `APP_SECURITY_DEMO_USER_USERNAME` and `APP_SECURITY_DEMO_USER_PASSWORD` in development.
- Pagination defaults: `default-page: 0`, `default-page-size: 20`, `max-page-size: 100`.
- Logging baseline: common console pattern in `application.yml`; dev `DEBUG`, prod `root: WARN` / `com.humanit.challenge: INFO`. T1.5 added the common `logging.pattern.console` property.

### 8.4 Security behavior relevant to documentation (VERIFIED)

- Spring Security is on the classpath (`spring-boot-starter-security`), but no `SecurityFilterChain` exists in `src/main/java` (VERIFIED by source inspection).
- Under Spring Boot's default security configuration, endpoints are authenticated by default; therefore H2 Console and Swagger UI reachability cannot be claimed as public without verification (F-R04, `agent-reports/T1.4/reviewer-report.md`).
- The H2 Console remains enabled under combined `dev,prod` activation because the `dev` profile is active (F-R03, Minor, non-blocking).
- Swagger UI is provided by `springdoc-openapi-starter-webmvc-ui` (dependency present); no business endpoints exist yet.

### 8.5 Documentation gaps (VERIFIED)

- No root `README.md` exists.
- No changelog file exists.

## 9. Findings and severity

| ID | Severity | Finding | Evidence |
|---|---|---|---|
| F-01 | Major (documentation gap) | No local run, build, profile or environment documentation exists; T1.6 cannot be completed without creating it | Root `README.md` NOT AVAILABLE |
| F-02 | Minor | No changelog file or Phase 1 changelog entry exists | Root `CHANGELOG*` NOT AVAILABLE |
| F-03 | Resolved | Previous finding stated T1.5 cross-cutting scaffolding was not present. Current repository inspection confirms it is present and accepted (D-08): `GlobalExceptionHandler`, `OpenApiConfig`, logging pattern, tests. | Section 8.1, D-08 |
| F-04 | Minor (accuracy) | H2 Console and Swagger UI are configured/available as dependencies, but their public reachability under default Spring Security is unverified | F-R04, `agent-reports/T1.4/reviewer-report.md` Section 9 |
| F-05 | Info | PostgreSQL integration verification remains pending and must be documented as deferred, not as verified | OT-01, `agent-reports/T1.4/developer-report.md` Section 12 |

No Critical findings. F-01 is the only blocking documentation gap for T1.6.

## 10. Decisions and reasoning

### 10.1 Current Phase 1 deliverables (VERIFIED)

- **T1.1 — Project bootstrap:** accepted (D-04). Minimal Spring Boot application, Java 21 baseline, Maven Wrapper, context-load test, standalone startup and autonomous termination verified.
- **T1.2 — Technical baseline and dependency verification:** accepted (D-05). Approved dependency set including Spring Web, Data JPA, Validation, Security, OAuth2 resource server, Flyway, H2, PostgreSQL, springdoc-openapi, test dependencies.
- **T1.3 — Environment configuration and secret externalization:** accepted (D-06). `dev`/`prod` profiles, externalized JWT secret, dev-only demo user with `@Profile("dev & !prod")`, pagination/expiration validation, fail-fast secret validation.
- **T1.4 — Persistence and migration baseline:** accepted (D-07). H2 dev datasource, PostgreSQL prod datasource from environment variables, Flyway enabled, comment-only `V1__baseline.sql`, `ddl-auto: validate`, dev-only H2 Console, test-harness H2 override.
- **T1.5 — Cross-cutting application scaffolding:** accepted (D-08). RFC 9457 `@RestControllerAdvice` (`GlobalExceptionHandler`), global validation-error mapping, minimal OpenAPI bean (`OpenApiConfig`), standard logging baseline. 41/41 tests pass, 0 Critical/Major findings, no blocking defects. Deferred items (409 handlers, 401/403 mappings, problem-type URIs, bearer scheme, structured logging) remain assigned to later phases.

### 10.2 Java and Maven prerequisites (VERIFIED)

Document the following as hard prerequisites:

- **Java:** JDK 21 (LTS). Enforced at build time by Maven Enforcer `requireJavaVersion [21,22)`; runtime evidence Java 21.0.12.1.
- **Maven:** 3.10.0 or later, enforced by `requireMavenVersion [3.10.0,)`. Global Maven installation is not required; use the committed Maven Wrapper (`mvnw.cmd` on Windows).
- **Wrapper:** Apache Maven Wrapper 3.3.4, pinned to `apache-maven-3.10.0-bin.zip`.
- **Spring Boot:** 3.5.16 (managed by the Spring Boot parent POM).
- **Operating system note:** commands below use Windows PowerShell syntax (`$env:VAR = "value"`). They can be adapted to other shells, but the verified baseline is Windows PowerShell.

### 10.3 Environment variables

**Development (required):**

| Variable | Required | Purpose | Notes |
|---|---|---|---|
| `APP_SECURITY_JWT_SECRET` | Yes | JWT signing secret | Standard Base64, at least 32 decoded bytes. Never commit. |
| `APP_SECURITY_DEMO_USER_USERNAME` | Yes | Development demo user username | Required because dev demo-user properties are validated; missing value fails startup. |
| `APP_SECURITY_DEMO_USER_PASSWORD` | Yes | Development demo user password | Required for the same reason. |

**Production (required):**

| Variable | Required | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Yes | Must be `prod`. |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL. |
| `SPRING_DATASOURCE_USERNAME` | Yes | PostgreSQL username. |
| `SPRING_DATASOURCE_PASSWORD` | Yes | PostgreSQL password. |
| `APP_SECURITY_JWT_SECRET` | Yes | JWT signing secret (Base64, at least 32 decoded bytes). |

### 10.4 Windows PowerShell command to generate a development JWT secret (>= 32 decoded bytes)

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

- Produces 32 random bytes (256 bits) and Base64-encodes them (44 characters). This satisfies the "at least 32 decoded bytes" rule.
- The value is generated locally and is never committed. For production, generate an independent secret using the same method on the target host or a secrets manager.
- Verification (optional, does not leak the secret): `[Convert]::FromBase64String($env:APP_SECURITY_JWT_SECRET).Length` should print `32` or more.

### 10.5 Local development startup procedure (PowerShell, from project root)

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:APP_SECURITY_DEMO_USER_USERNAME = "demo"
$env:APP_SECURITY_DEMO_USER_PASSWORD = "<demo-password>"
.\mvnw.cmd spring-boot:run
```

- No `SPRING_PROFILES_ACTIVE` is required; the configured default profile is `dev`.
- If any required variable is missing or invalid, startup fails fast before the web server starts; the failure names the property but never leaks the secret value.
- The application starts on the default Spring Boot port (8080) unless overridden by standard Spring Boot configuration.

### 10.6 Production profile requirements (PowerShell example)

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://<host>:5432/<database>"
$env:SPRING_DATASOURCE_USERNAME = "<username>"
$env:SPRING_DATASOURCE_PASSWORD = "<password>"
$env:APP_SECURITY_JWT_SECRET = "<base64-secret-with-at-least-32-decoded-bytes>"
.\mvnw.cmd spring-boot:run
```

- Demo-user settings are intentionally absent in `prod`; no demonstration credentials are provisioned in production.
- H2 Console is not configured in `prod`.
- PostgreSQL integration against a real PostgreSQL instance is **PENDING VERIFICATION** (deferred to Phase 5 / T5.2); the configuration is validated only through the H2-overridden test harness in Phase 1. Configuration presence must not be presented as verified integration.

### 10.7 Database and Flyway initialization (PROPOSED)

Document the following facts, all verified from configuration:

- **Development:** H2 in-memory database (`jdbc:h2:mem:humanit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`). No manual database setup is required.
- **Production:** PostgreSQL, configured entirely from `SPRING_DATASOURCE_*`.
- **Flyway:** enabled in both profiles; migrations are loaded from `classpath:db/migration`.
- **Baseline migration:** `V1__baseline.sql` is comment-only and is recorded as schema version 1. It intentionally creates no tables; business schema arrives in Phase 2 (T2.1).
- **Hibernate:** `ddl-auto: validate` in common configuration. Hibernate validates the schema but never creates or alters tables; Flyway owns schema changes.
- **SQL initialization:** `spring.sql.init.mode: never`, so Spring Boot does not run `schema.sql`/`data.sql`.
- **H2 Console:** configured only in `dev`, path `/h2-console`. It is also active under combined `dev,prod` because the `dev` profile is active.
- **Reachability caveat:** because Spring Security is present and no `SecurityFilterChain` is configured yet, H2 Console and Swagger UI access may require authentication and may be affected by default frame/CSRF settings. Do not document them as publicly accessible in Phase 1. Phase 3 (T3.1/T3.3) will address security configuration.

### 10.8 Build and test commands (PowerShell, from project root)

```powershell
# Full build and test suite (recommended baseline check)
.\mvnw.cmd clean verify

# Tests only
.\mvnw.cmd test

# Package without running tests (use only when explicitly intended)
.\mvnw.cmd -DskipTests package
```

- The Maven Wrapper downloads Maven 3.10.0 automatically on first use.
- `clean verify` runs compilation, tests, packaging and the Maven Enforcer baseline checks.
- No global Maven installation is required.
- Test counts drift between runs; record the actual count from the latest run rather than hard-coding a number. The last verified full run reported 41 tests (T1.5).

### 10.9 Proposed documentation files (EXACT)

Create:

1. `README.md` (repository root) — primary local setup and execution documentation.
2. `CHANGELOG.md` (repository root) — Phase 1 changelog entry.

Modify:

- None. Existing `reports/*.md` files are stale in places but are not modified by this task, per governance rules. If the Tech Lead wants the stale status report corrected, that is a separate authorized documentation task.

### 10.10 Proposed `README.md` structure (concise, hiring-challenge appropriate)

1. Project purpose and scope (manage Clients and Documents; Phase 1 baseline only).
2. Technology baseline (Java 21, Spring Boot 3.5.16, Maven 3.10.0 via Wrapper).
3. Prerequisites.
4. Configuration and environment variables (dev and prod tables).
5. Local development startup (PowerShell), including JWT secret generation.
6. Production profile startup (PowerShell).
7. Database and Flyway baseline.
8. H2 Console (dev only; reachability caveat).
9. OpenAPI/Swagger (dependency present; no business endpoints yet; reachability caveat).
10. Build and test commands.
11. Expected startup behavior.
12. Known limitations and deferred work.
13. Changelog reference (`CHANGELOG.md`).

Sections describing features not yet implemented (authentication, Swagger endpoint usage, Docker, PostgreSQL integration) must be marked "not available in Phase 1" rather than documented as working.

### 10.11 Proposed `CHANGELOG.md` structure

```markdown
# Changelog

## [Unreleased]

## [Phase 1] - 2026-11-09

### Added
- Project bootstrap (Maven project, Maven Wrapper, Java 21 baseline, Spring Boot application entry point).
- Technical baseline dependencies (Spring Web, Data JPA, Validation, Security, OAuth2 resource server, Flyway, H2, PostgreSQL, springdoc-openapi, test dependencies).
- Environment configuration with `dev` and `prod` profiles; externalized JWT secret and dev-only demo user.
- Persistence baseline: H2 development datasource, PostgreSQL production datasource from environment variables, Flyway enabled, `ddl-auto: validate`, dev-only H2 Console, comment-only `V1__baseline.sql`.
- Cross-cutting scaffolding (T1.5): RFC 9457 Problem Details via `@RestControllerAdvice` (`GlobalExceptionHandler`), global validation-error mapping, minimal OpenAPI bean (`OpenApiConfig`), standard logging baseline.
- Phase 1 sanity and documentation checkpoint: root `README.md` and `CHANGELOG.md`.

### Changed
- Not applicable (initial baseline).

### Known limitations
- PostgreSQL integration is not verified in Phase 1 (deferred to Phase 5).
- H2 Console and Swagger UI reachability under default Spring Security is not verified.
- Business endpoints, authentication enforcement, Docker and React frontend are not implemented (later phases).
- Root documentation created in T1.6.
```

### 10.12 Phase 1 sanity-check procedure (PROPOSED)

The Tester and Reviewer should execute, in order:

1. **Build baseline:**
   ```powershell
   .\mvnw.cmd clean verify
   ```
   Expected: `BUILD SUCCESS`, exit code `0`, Maven Enforcer Java and Maven rules passed, no compilation errors.

2. **Test baseline:**
   ```powershell
   .\mvnw.cmd test
   ```
   Expected: all tests pass; record actual counts (do not assume a historical figure).

3. **Development startup:**
   ```powershell
   $bytes = New-Object byte[] 32
   [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
   $env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
   $env:APP_SECURITY_DEMO_USER_USERNAME = "demo"
   $env:APP_SECURITY_DEMO_USER_PASSWORD = "<demo-password>"
   .\mvnw.cmd spring-boot:run
   ```
   Expected: application starts and stays running on port 8080; log contains `Started HumanItChallengeApplication`; Flyway validates/applies the baseline migration; no secret value appears in logs.

4. **Fail-fast check (negative):** run the development startup command without `APP_SECURITY_JWT_SECRET`.
   Expected: startup aborts before the web server starts with a message naming the property; no secret value leaked.

5. **Configuration inspection (no execution):** verify production profile values are only `${SPRING_DATASOURCE_*}` placeholders and that H2 Console is absent from `application-prod.yml`.

6. **Documentation check:** follow the new `README.md` instructions from a clean shell and confirm every documented command succeeds.

7. **Changelog check:** confirm `CHANGELOG.md` contains the Phase 1 entry and lists deferred items honestly.

### 10.13 Acceptance criteria mapping

| T1.6 acceptance criterion | Proposed evidence |
|---|---|
| Documented commands execute successfully | Sanity procedure steps 1–6 executed and recorded by Tester; command output captured. |
| Phase 1 has its own tests and independent review record | Existing T1.1–T1.5 test suites plus independent Tester/Reviewer reports; T1.6 Tester and Reviewer reports created after this proposal is approved. |
| Local run instructions are accurate | Tester follows `README.md` from a clean shell and records results. |
| Build and test commands are documented | `README.md` build section and sanity procedure steps 1–2. |
| Required Java and Maven versions are stated | `README.md` prerequisites and `pom.xml` enforcer rules. |
| Dev and prod profiles are distinguished | Environment variable tables and startup procedures. |
| Database and Flyway baseline is documented | `README.md` database section and `V1__baseline.sql`. |
| Phase 1 changelog entry exists | `CHANGELOG.md`. |
| Known limitations are documented honestly | `README.md` limitations section and `CHANGELOG.md` known limitations. |

### 10.14 Risks and limitations

- **Staleness risk:** documentation can drift as later phases add endpoints, security and Docker. Mitigation: update `README.md`/`CHANGELOG.md` in the phase that introduces each feature; Phase 4 (T4.3) explicitly requires README validation.
- **Security reachability:** H2 Console and Swagger UI may not be usable until Phase 3 security configuration exists. Documentation must avoid promising public access.
- **Secret handling:** examples must use placeholders only; no real credentials may be committed. Reviewer should scan documentation for secret-like values.
- **Test counts drift:** the latest verified figure is 41 tests; documentation should refer to the latest run rather than hard-coding a count, or clearly label it as the last verified run.
- **Windows-only verification:** commands are verified for PowerShell. Other shells require adaptation and are not part of the Phase 1 baseline evidence.
- **No PostgreSQL verification:** production datasource behavior is validated only through configuration inspection and H2-overridden tests in Phase 1.

### 10.15 Decisions requiring Tech Lead approval

1. **Approve creation of `README.md` and `CHANGELOG.md`** at the repository root, and their proposed section structures.
2. **Approve the H2 Console/Swagger UI wording** as "configured but not verified publicly accessible under default Spring Security."
3. **Approve documenting production startup commands** that are configuration-based and not executed in Phase 1, clearly marked as not verified against a live PostgreSQL instance.
4. **Approve whether the stale `reports/task-status-report.md`** should be corrected in a separate authorized documentation task (this proposal does not modify it).
5. **No permission blocker occurred during this execution.** The report was persisted successfully under the current configuration; no permission change is required for this artifact.

## 11. Final status

**PROPOSED — awaiting Tech Lead approval.** No application, test, configuration or documentation files were changed by this execution; only this report and its archived predecessor were written. Implementation of documentation must not begin until the Tech Lead approves this proposal and authorizes the Developer task.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OI-01 | PENDING VERIFICATION | PostgreSQL integration against a real PostgreSQL instance | Phase 5 (T5.2) |
| OI-02 | Verification gap | H2 Console and Swagger UI reachability under default Spring Security | Phase 3 (T3.1/T3.3) |
| OI-03 | Documentation hygiene | `reports/task-status-report.md` is stale for T1.2–T1.6 | Separate authorized documentation task |
| OI-04 | Resolved | Previous T1.5 ambiguity resolved: T1.5 is implemented and accepted (D-08) | None |
| OI-05 | Resolved | This proposal was persisted to `agent-reports/T1.6/architect-proposal.md` during this execution; no permission blocker occurred | None |

## 13. Handoff to next agent

- **To the human Tech Lead:** review and approve or reject this proposal. If approved, authorize the Developer to create `README.md` and `CHANGELOG.md` using the structures in Sections 10.10 and 10.11. T1.5 status is now confirmed (implemented and accepted).
- **To the Developer (after approval):** create only the two documentation files; do not modify application code, tests, `pom.xml`, configuration, requirements, planning documents or existing reports.
- **To the Tester (after implementation):** execute the sanity procedure in Section 10.12 and record actual command output and test counts.
- **To the Reviewer (after implementation):** audit documentation accuracy, absence of secrets, correctness of commands on Windows PowerShell, and honesty of limitation statements.
- **To the Tech Lead:** this report was persisted successfully at `agent-reports/T1.6/architect-proposal.md`. No permission blocker or permission change is required for this artifact.

---

**Status of this proposal:** Drafted and persisted 2026-11-09 from in-project artifacts only. All statements are classified per `agent-reports/README.md` Section 5 (VERIFIED / RECONSTRUCTED / NOT AVAILABLE / PENDING VERIFICATION). No application code, tests, configuration or documentation files were modified; only this report and its archived predecessor were written. No approval is recorded by this report.