# Architect Proposal — T4.3 README and Technical Decisions

## 1. Task ID

- **VERIFIED** (direct tasking and `implementation-plan.md`, Phase 4, T4.3): `T4.3 — README and Technical Decisions`.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T4.3-ARCH-001`, 2026-11-12.
- **VERIFIED** (`agent-reports/T4.3/` file-pattern inspection): No existing T4.3 Architect primary report or history archive was found; no archival copy was required before creating this report.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Architect; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Documentation analysis and proposal only. T4.3 requires concise README documentation of purpose, technology, run/build/test, authentication, Swagger, design decisions, assumptions and limitations; align legacy `/clients` planning references with `/api/clients`.
- **VERIFIED** (direct tasking): No application/test implementation, no Git writes/commit/push/PR, no branch switching or stash operations, and do not mark T4.3 complete.
- **VERIFIED** (`agent-reports/README.md`, Sections 3–6): This report follows the required 13-section format, evidence markers and execution-history convention. Architect reporting is the only file-writing scope used.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`; `implementation-plan.md` (T2.3, T2.4, T2.7, T4.3 and T5); `requirements-decisions.md` (API, update, concurrency, security, persistence, Docker, OpenAPI and README requirements); current `README.md`; `pom.xml`; `.mvn/wrapper/maven-wrapper.properties`.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`, `db/migration/V1__baseline.sql`, `db/migration/V2__create_clients_and_documents.sql`.
- **VERIFIED**: `SecurityConfiguration.java`, `AuthController.java`, `ClientController.java`, `DocumentController.java`, `OpenApiConfig.java`; `AppJwtProperties.java`, `AppDemoUserProperties.java`, `AppProductionUserProperties.java`; login, Client, Document and page response/request DTOs.
- **VERIFIED**: `agent-reports/T4.2/reviewer-report.md`; source/test file listings relevant to tests; file-pattern searches for Dockerfile and Compose configuration.

## 6. Files changed

- **VERIFIED**: Created only `agent-reports/T4.3/architect-proposal.md`, this report. No application code, tests, configuration, README, implementation plan, requirements, decision log, Git metadata or other agent reports were changed.
- **VERIFIED**: No earlier T4.3 Architect report existed to archive.

## 7. Commands executed

- **VERIFIED**: No shell commands or Maven commands were executed. This is an analysis-only execution and the Architect is prohibited from executing shell commands.
- **VERIFIED**: Read/glob/content-search tools were used to inspect project files. Proposed commands below are checked structurally against the wrapper, POM and configuration; they have **not** been executed in this report.

## 8. Evidence and observed results

- **VERIFIED** (`pom.xml`, `.mvn/wrapper/maven-wrapper.properties`, current `README.md`): Build baseline is Java 21; Maven Enforcer requires Maven `[3.10.0,)`; wrapper 3.3.4 downloads Maven 3.10.0. `mvnw.cmd` exists. The README's prerequisite versions are accurate, but testing/run instructions require fresh execution by the Tester.
- **VERIFIED** (`src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`): `dev` is the default profile and uses in-memory H2. PostgreSQL is configured under `prod` through `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Flyway is enabled at `classpath:db/migration`; Hibernate uses `ddl-auto: validate`; pagination defaults are page 0, size 20, maximum 100; JWT expiry defaults to 60 minutes.
- **VERIFIED** (`V1__baseline.sql`, `V2__create_clients_and_documents.sql`): V1 is comment-only; V2 creates Client and Document tables, uniqueness constraints, Client foreign key with cascade delete, and a Client index. Current README incorrectly says business tables are not yet present and describes V1 as the only migration.
- **VERIFIED** (controllers): Current business routes are rooted at `/api/clients`; nested Document routes are `/api/clients/{clientId}/documents`; login is `POST /auth/login`. The current README incorrectly says there are no business endpoints/controllers and no authentication enforcement.
- **VERIFIED** (`SecurityConfiguration.java`, `AuthController.java`, user properties): Login and Swagger/OpenAPI paths are permitted; other routes require authentication. JWT is stateless, HS256 and signed using the externally supplied Base64 secret; BCrypt is configured. A single configured account is used, with dev credentials restricted to `dev & !prod` and production credentials configured separately. Approved plan decisions also state no user database, registration, roles or refresh-token flow (`implementation-plan.md`, Phase 3).
- **VERIFIED** (`OpenApiConfig.java`, controller mappings): OpenAPI includes a JWT bearer scheme and Problem Details schema; Swagger UI and OpenAPI paths are public per the security route policy. The T4.2 Reviewer independently confirmed the current `/api/clients` route contract and OpenAPI assertions (`agent-reports/T4.2/reviewer-report.md`, Section 8).
- **VERIFIED** (`ClientController.java`, `DocumentController.java`): API uses zero-based pagination, defaults 0/20, maximum size 100, ascending ID ordering and page metadata. Client PUT preserves existing Documents for omitted/empty collections; explicit `documents:null` is invalid. PATCH changes supplied values only, distinguishes omitted/null semantics, and does not implicitly delete Documents. Required expected versions and `X-Client-Version`/`X-Document-Version` headers apply as documented by the controllers and approved decisions; conflicts return 409 and invalid/missing version inputs return 400.
- **VERIFIED** (Docker/Compose file-pattern searches and `implementation-plan.md`, Phase 5): No repository Dockerfile or Compose YAML was found. Docker delivery is planned for Phase 5, not currently supported by files observed. README must not present Docker commands as runnable.
- **VERIFIED** (`implementation-plan.md`, T2.3, T2.4 and T2.7; corroborated by `agent-reports/T4.2/reviewer-report.md`, Sections 5 and 9): Those planning sections retain legacy `/clients` route strings; the current route contract is `/api/clients`. This is the deferred Minor documentation carryover.
- **VERIFIED** (`src/test/java` listing and `pom.xml`): Maven tests are configured through Spring Boot Test/Spring Security Test and test sources include controller, persistence, concurrency, security/authentication, pagination/query-count, validation and OpenAPI test classes. **NOT AVAILABLE**: This Architect did not execute tests and makes no current pass/count claim.
- **VERIFIED** (`implementation-plan.md`, T2.9 verification note): H2 acceptance tests were reported as passed for the N+1 task, with PostgreSQL verification deferred. A README may describe PostgreSQL configuration, but must not claim the real PostgreSQL integration path was verified.
- **VERIFIED** (`APP*Properties` source and current `README.md`): Dev requires `APP_SECURITY_JWT_SECRET`, `APP_SECURITY_DEMO_USER_USERNAME`, and `APP_SECURITY_DEMO_USER_PASSWORD_HASH`; prod additionally requires active `prod`, PostgreSQL datasource variables, `APP_SECURITY_USER_USERNAME`, and `APP_SECURITY_USER_PASSWORD_HASH`. The JWT secret must be standard Base64 decoding to at least 32 bytes. No credential value should be included in documentation.
- **NOT AVAILABLE** (repository source/file inspection): No production/demo credential values, valid sample BCrypt hash, or in-repository BCrypt-hash-generation utility was found. README examples should use explicit placeholders and state that the supplied hash must be generated/provisioned securely; do not provide pretend credentials.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| T43-ARCH-01 | Major — materially stale README | **VERIFIED** (`README.md` Sections “Purpose and Current Scope,” “Database and Flyway Baseline,” “OpenAPI Configuration,” and “Known Limitations”): README repeatedly describes a Phase 1-only baseline with no business controllers/endpoints, no authentication enforcement, no business schema and no bearer OpenAPI scheme. Source and V2 migration show these statements are now out of date. Replace rather than append competing current-state claims. |
| T43-ARCH-02 | Major — missing evaluator guidance | **VERIFIED** (`README.md`): No current API login/authorized-request examples or endpoint inventory are present. Add concise working-shape examples based on current DTO/controller contracts and the canonical `/api/clients` paths. |
| T43-ARCH-03 | Minor — legacy plan routes | **VERIFIED** (`implementation-plan.md`, T2.3, T2.4, T2.7; T4.2 Reviewer): Planning paths use `/clients` instead of the implemented `/api/clients`. Align these references without changing endpoint behavior or requirements.
| T43-ARCH-04 | Minor — unsupported delivery claims risk | **VERIFIED** (Docker/Compose file searches): The README correctly states no Docker files exist, but current implementation plan expects Docker later. Keep the README explicit that Docker/Compose is not available in this repository state; do not add speculative commands.
| T43-ARCH-05 | Minor — evidence qualification needed | **VERIFIED** (`implementation-plan.md`, T2.9): PostgreSQL connection configuration exists, while real PostgreSQL verification was deferred. Document this boundary and do not imply config presence proves integration.

## 10. Decisions and reasoning

- **VERIFIED** (source and plan artifacts): Recommend a small documentation-only update to exactly two existing project files: `README.md` and `implementation-plan.md`. Do not edit `requirements-decisions.md`; its Section 3 endpoint table already uses canonical `/api/clients` routes, and T4.3 does not authorize requirements changes.
- **VERIFIED** (`README.md`, T4.3 scope): Replace stale Phase 1 statements with concise evaluator-focused sections in this order:
  1. Purpose and current capabilities.
  2. Technology and prerequisites.
  3. Local setup/startup and environment configuration.
  4. Database and Flyway migrations.
  5. Authentication and API examples.
  6. Endpoints and Swagger/OpenAPI.
  7. PUT/PATCH, transactions and concurrency decisions.
  8. Build/tests.
  9. Limitations and unverified deployment paths.
  Keep logging internals and duplicated long environment tables out of the minimal README unless required for an executable local startup.
- **VERIFIED** (`README.md`, existing headings): Exact README areas to replace/update are `Purpose and Current Scope`; `Technology Baseline` and `Prerequisites`; consolidate/update `Configuration and Environment Variables`, `Generating a JWT Secret`, `Local Development Startup` and `Production Configuration Example`; replace `H2 and PostgreSQL Profile Differences` and `Database and Flyway Baseline`; replace `RFC 9457 Problem Details Infrastructure` and `OpenAPI Configuration and Limitations` with current behavior; update `Build and Test Commands` and `Known Limitations and Deferred Work`. Remove or shorten `Logging Baseline` if retaining it does not help evaluator setup. In `implementation-plan.md`, update route strings only in T2.3, T2.4 and T2.7.
- **VERIFIED** (`pom.xml`, `.mvn/wrapper/maven-wrapper.properties`, config files): Essential local command shape (PowerShell, from repo root) is:
  ```powershell
  java -version
  $bytes = New-Object byte[] 32
  [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
  $env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
  $env:SPRING_PROFILES_ACTIVE = "dev"
  $env:APP_SECURITY_DEMO_USER_USERNAME = "<your-username>"
  $env:APP_SECURITY_DEMO_USER_PASSWORD_HASH = "<valid-BCrypt-hash-from-secure-provisioning>"
  .\mvnw.cmd spring-boot:run
  ```
  Label the last two values as user-supplied placeholders, not ready-to-use credentials. Explicit `dev` avoids accidentally inheriting production profile selection. No global Maven installation is needed; first wrapper use may require network access.
- **VERIFIED** (`pom.xml`, current README): The essential Windows build/test commands to document are `.\mvnw.cmd clean verify` and `.\mvnw.cmd test`. They are structurally consistent with the checked-in wrapper and Maven Enforcer rules; they remain **PENDING VERIFICATION** by the Tester because no command was run in this execution.
- **VERIFIED** (`application*.yml`, Flyway migrations): Document dev as H2/no manual database provisioning; migration directory `classpath:db/migration`; V1 baseline followed by V2 business schema; Flyway owns DDL and Hibernate validates. Describe prod's PostgreSQL environment requirements, with a clear real-PostgreSQL verification caveat.
- **VERIFIED** (controllers, security and OpenAPI config): List endpoint families, not every response schema: Client CRUD at `/api/clients`; nested Document CRUD at `/api/clients/{clientId}/documents`; login at `/auth/login`; query `page`/`size` defaults and limit. Swagger UI URL is `http://localhost:8080/swagger-ui/index.html`; OpenAPI JSON is `http://localhost:8080/v3/api-docs`. Both paths are permitted without bearer token by the filter chain.
- **VERIFIED** (`AuthController.java`, `TokenResponse.java`, controllers): Basic usage can show login yielding `accessToken`/`tokenType`, followed by a bearer-authenticated page request. A PowerShell shape is:
  ```powershell
  $login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/login' `
    -ContentType 'application/json' `
    -Body (@{ username = $env:APP_SECURITY_DEMO_USER_USERNAME; password = '<your-password>' } | ConvertTo-Json)
  Invoke-RestMethod -Uri 'http://localhost:8080/api/clients?page=0&size=20' `
    -Headers @{ Authorization = "Bearer $($login.accessToken)" }
  ```
  State that `<your-password>` must correspond to the configured BCrypt hash; it is a placeholder and must not be committed. For mutation examples, keep the README short and note that Client/Document responses include versions, and required version headers/body fields must match the latest returned version.
- **VERIFIED** (controller source and approved `requirements-decisions.md`): Document technical decisions worth retaining: one stateless authenticated user, HS256/BCrypt, externally supplied secret/credentials; Client-Document ownership and cascade deletion; collection-preserving Client PUT; PATCH omitted-vs-explicit-null behavior; transactional atomicity and optimistic locking, including parent version increment when a Document changes; RFC 9457 Problem Details; page cap/order. Summarize, do not reproduce the full requirements contract.
- **VERIFIED** (repo file searches and approved plan): Disclose no Dockerfile/Compose files; no user database, registration, role/permission model or refresh-token flow; PostgreSQL integration remains unverified/deferred. Avoid listing superseded Phase 1 gaps as current limitations.
- **VERIFIED** (T2.3, T2.4, T2.7 plan locations): Update only their route strings to `/api/clients/{id}`, `/api/clients/{clientId}/documents`, and their nested paths where applicable. Preserve task semantics and acceptance criteria. This aligns the planning records with controller mappings, not a runtime/API change.
- **RECONSTRUCTED** (direct scope and audited artifacts): Alternatives considered: (a) append a status correction while leaving old detailed sections, which risks contradiction; (b) replace obsolete README sections with the proposed compact structure, recommended because it is clearer and avoids conflicting operational claims. Do not expand scope to a new docs site, Docker work, code changes, tests, or a `CHANGELOG.md` entry.
- **VERIFIED** (T4.3 acceptance criteria and inspected files) — proposed acceptance criteria:
  1. README prerequisites accurately state Java 21, Maven 3.10+ minimum and wrapper 3.3.4/Maven 3.10.0 distribution.
  2. A user with their own valid BCrypt hash can follow local H2 startup steps without invented credentials; Flyway V1/V2 and Hibernate validation are described accurately.
  3. README includes accurate login, bearer-request and Swagger/OpenAPI instructions, uses `/api/clients`, and summarizes the implemented PUT/PATCH and concurrency behavior.
  4. README documents actual build/test wrapper commands and makes no unsupported Docker or verified-PostgreSQL claim; limitations are current and explicit.
  5. The legacy route strings in implementation-plan T2.3/T2.4/T2.7 align with `/api/clients` without contract changes.
  6. Tester follows the README startup and test instructions, records observed results, and performs a protected read smoke check; no source/test code or extra suite is added.
- **VERIFIED** (task constraints and artifacts) — minimal verification approach: Tester executes `java -version`, ` .\mvnw.cmd clean verify` and follows the documented `dev` startup and login/protected-read example with Tester-owned credentials. Do not run Docker checks because no Docker artifacts exist. This Architect ran none of these commands; all execution outcomes remain pending.
- **PENDING VERIFICATION**: The exact startup and request examples need a Tester to follow them in a fresh local environment, including a valid user-owned BCrypt hash. Architect did not execute them.

## 11. Final status

**PROPOSAL READY — T4.3 NOT COMPLETE.** **VERIFIED**: The existing README contains materially stale statements and lacks current evaluator-facing API examples; Docker files are absent; legacy `/clients` references remain in three implementation-plan tasks. **VERIFIED**: This execution only wrote its Architect report. No README, planning or application changes were made. **PENDING VERIFICATION**: No commands or test runs were executed, and T4.3 implementation, independent test/review and Tech Lead acceptance remain outstanding.

## 12. Outstanding issues

- **VERIFIED**: Developer must update `README.md` and the route references in `implementation-plan.md` T2.3/T2.4/T2.7 within approved documentation-only scope.
- **PENDING VERIFICATION**: Tester must execute the README startup/build/test instructions and exercise the documented login and protected read example. Do not mark command success until actually observed.
- **PENDING VERIFICATION**: Secure provisioning method for a usable BCrypt hash is not present in the inspected repository; documentation should require a valid externally produced hash without inventing a hash or credential.
- **VERIFIED**: Docker instructions must remain absent/non-runnable until Dockerfile and Compose artifacts are delivered in the planned delivery phase.
- **PENDING VERIFICATION**: Real PostgreSQL integration remains deferred; README claims must remain limited to configuration and explicitly state the verification gap.
- **PENDING VERIFICATION**: This report is a proposal and does not constitute human Tech Lead approval or final T4.3 acceptance.

## 13. Handoff to next agent

- **VERIFIED**: Recommend Developer execution after Tech Lead approval: update the README structure/content and correct the specified planning route strings only. Preserve the documented distinction between implementation, tested evidence and unverified production/PostgreSQL behavior.
- **VERIFIED**: Handoff to Developer and then Tester for README-following verification. Tester should use current code, the actual H2 dev profile and supplied test credentials; no Docker test is applicable because repository Docker files are absent.
- **VERIFIED**: Report path: `agent-reports/T4.3/architect-proposal.md`. Final status: proposal ready; T4.3 remains open. Outstanding findings: stale README, absent API examples, legacy plan paths, no Docker delivery, PostgreSQL integration deferred/unverified, and setup example needs Tester verification.
