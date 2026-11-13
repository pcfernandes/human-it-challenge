# Reviewer Report — T5.2 Docker Compose and PostgreSQL

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 5, T5.2; direct tasking): `T5.2 — Docker Compose and PostgreSQL`.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T5.2-EXEC-001-REV-01`, 2026-11-12.
- **VERIFIED** (inspection of `agent-reports/T5.2/` before report creation): No earlier T5.2 Reviewer report was present; no history archive was required.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Reviewer; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Independently review `compose.yaml`, `.env.example`, `Dockerfile` and Spring configuration for correctness, security, PostgreSQL persistence, healthcheck, environment variables, Flyway integration, and preservation of local H2 development. Treat the existing-volume credential mismatch in the Tester report as an environmental caveat, not automatically a code defect. Do not modify application/configuration files or commit/push.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): Inspect implementation independently before reading the Architect, Developer and Tester reports; follow the 13-section format, evidence markers and execution-history convention.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`, `README.md`, `implementation-plan.md` (Phase 5, T5.2), `compose.yaml`, `.env.example`, `Dockerfile`, `.gitignore`, and `pom.xml`.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`, `src/main/resources/db/migration/V1__baseline.sql`, and `V2__create_clients_and_documents.sql`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/config/AppJwtProperties.java`, `AppDemoUserProperties.java`, `AppProductionUserProperties.java`, and `JwtSecretValidator.java`.
- **NOT AVAILABLE**: `.dockerignore` was not found in the repository file search.
- **VERIFIED**: Only after independently inspecting the implementation, read `agent-reports/T5.2/architect-proposal.md`, `developer-report.md`, `tester-report.md`, `agent-reports/decisions.md`, and `agent-reports/T5.1/reviewer-report.md`.

## 6. Files changed

- **VERIFIED**: Created only `agent-reports/T5.2/reviewer-report.md`, this required audit report. No application code, test, Docker/Spring configuration, migration, planning file, decision log, or other agent report was modified.

## 7. Commands executed

- **VERIFIED**: No terminal, Git, Docker, Compose, Maven, or application command was executed. Repository inspection used file read, search and listing tools only.

## 8. Evidence and observed results

- **VERIFIED** (`compose.yaml`, lines 1–18): The application is built from the root Dockerfile, waits for the PostgreSQL service to be healthy, receives the `prod` profile and datasource/security values from required Compose interpolation, and publishes port 8080.
- **VERIFIED** (`compose.yaml`, lines 20–36): PostgreSQL uses `postgres:17-alpine`, receives database initialization values from the environment, has a `pg_isready` healthcheck, and stores data in the named `postgres_data` volume mounted at `/var/lib/postgresql/data`. The database port is not published to the host.
- **VERIFIED** (`.env.example`, `.gitignore`): The sample contains explicit placeholders and instructions to replace them; `.env` is ignored by Git while `.env.example` is retained. The database password and app datasource credentials use the same Compose variables.
- **VERIFIED** (`Dockerfile`): The image uses a Java 21 build stage and JRE runtime stage, copies the wrapper/project sources needed for the build, runs the final container as `spring:spring`, exposes 8080, and contains no embedded application secrets.
- **VERIFIED** (`application.yml`, `application-dev.yml`, `application-prod.yml`): Spring defaults to `dev`; that profile uses in-memory H2. Compose explicitly sets `SPRING_PROFILES_ACTIVE=prod`, whose datasource is PostgreSQL configured from `SPRING_DATASOURCE_*`. Flyway is enabled in common configuration and Hibernate is set to `ddl-auto: validate`; the H2 console is enabled only in the dev profile.
- **VERIFIED** (`pom.xml`, lines 57–74; migration files): Flyway core, Flyway PostgreSQL support, and PostgreSQL JDBC are runtime dependencies. The repository contains V1 and V2 migrations.
- **VERIFIED** (`JwtSecretValidator.java`, lines 25–40; `.env.example`, line 8): Application startup requires the JWT secret to be standard Base64 decoding to at least 32 bytes. The example value `replace-with-a-base64-secret-of-at-least-32-bytes` is intentionally a placeholder and is not valid standard Base64; the file instructs users to replace placeholders/generate a secret.
- **VERIFIED (reported, not rerun)** (`agent-reports/T5.2/developer-report.md`, Sections 7–8): Developer reports a Compose build/start, healthy PostgreSQL, app startup in `prod`, HTTP 200 from `/v3/api-docs`, successful Flyway V1/V2 records, and volume preservation after `down`.
- **VERIFIED (reported, not rerun)** (`agent-reports/T5.2/tester-report.md`, Sections 7–8): Tester independently reports successful Compose parsing and, using an isolated fresh project/volume with process-local values, healthy PostgreSQL, `prod` startup, successful Flyway V1/V2 and HTTP 200 from `/v3/api-docs`.
- **VERIFIED** (`agent-reports/T5.2/tester-report.md`, Sections 8–9): The Tester observed authentication failure against a pre-existing volume after selecting different local credentials, preserved the volume and passed with a fresh isolated volume. This is consistent with persisted database initialization credentials and is treated here as an environmental caveat, not a Compose code finding.
- **RECONSTRUCTED** (Compose healthcheck command and standard `pg_isready` default behavior): Because the healthcheck omits a host, `pg_isready` uses the container's local Unix socket. During initialization the official PostgreSQL image can use a temporary local-only server; in a slow initialization this can make the check green before the TCP endpoint used by the Spring datasource is ready. No such race was reported in the Developer or Tester runs; this is a startup robustness risk, not an observed failure.
- **VERIFIED** (`Dockerfile`; no `.dockerignore` found): The Dockerfile's explicit `COPY` instructions do not copy root `.env` into the image. However, `.gitignore` does not exclude files from a Docker build context; absent a Docker-specific exclusion policy, local `.env` and unrelated files remain eligible for transfer to a builder. The actual use/exposure of a remote builder is **NOT AVAILABLE**.
- **VERIFIED** (`README.md`, lines 102–105): The Limitations section still states that Dockerfile/Compose have not been implemented and no container startup commands are available, which is stale given the inspected Dockerfile, Compose file and reported startup verification.
- **RECONSTRUCTED** (comparison of `developer-report.md` Sections 7–8 with `.env.example` and `JwtSecretValidator.java`): The Developer reports an app startup using `--env-file .env.example`, but the report does not establish whether shell environment overrides supplied a valid JWT secret. The literal example secret fails the application's Base64 requirement. This does not dispute the separately reported or Tester-verified stack startup with valid values, and it is not a defect in a file explicitly marked as placeholders; it means the sample alone cannot establish that startup result.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| T52-REV-01 | Minor | **RECONSTRUCTED**: The PostgreSQL healthcheck does not force TCP (`pg_isready` has no `-h`), so it can report the local temporary initialization server ready before the app's TCP datasource path is ready. This can create an intermittent first-start race. Consider probing `127.0.0.1` (or otherwise checking the same network path used by the application). No race was observed in the reported runs. |
| T52-REV-02 | Minor | **VERIFIED / PENDING VERIFICATION**: No `.dockerignore` exists, and `.env` is ignored only by Git. The Dockerfile does not bake `.env` into the image, but a build context may still transfer it to a remote builder and includes unrelated local artifacts. Add context exclusions for secrets and irrelevant files; the actual remote-builder exposure is not established. |
| T52-REV-03 | Minor | **VERIFIED**: `README.md` still says Docker/Compose are unimplemented and provides no container startup commands. This is now inaccurate and can mislead setup users; update the delivery documentation, at least as part of the planned T5.3 documentation/parity work. |

- **VERIFIED**: No Critical or Major defect was identified in the reviewed Compose wiring, Spring profile configuration, PostgreSQL persistence declaration, environment-variable mapping, or Flyway integration.

## 10. Decisions and reasoning

- **VERIFIED**: Compose uses the correct service DNS name (`postgres`) in the JDBC URL, aligns Spring datasource credentials with PostgreSQL initialization variables, gates initial app start on database health, and uses a named volume for database persistence.
- **VERIFIED**: `SPRING_PROFILES_ACTIVE=prod` is scoped to the Compose app service; the existing application default remains H2-backed `dev`, and no configuration change removed that local path.
- **VERIFIED (reported, not rerun)**: Developer and Tester runtime evidence is consistent on successful fresh PostgreSQL startup, Flyway migration and endpoint response. The Tester’s existing-volume credential mismatch is not treated as a code defect, consistent with direct tasking.
- **VERIFIED**: The Developer's report does not prove that the literal `.env.example` placeholder secret was the value used at startup; the placeholder is deliberately invalid and must be replaced. No contradiction is inferred about successful startup using valid supplied environment values.
- **RECONSTRUCTED**: The three Minor findings are hardening/documentation improvements rather than blockers to the T5.2 acceptance criteria. The healthcheck race was not observed, the Dockerfile does not copy `.env`, and the README correction can align with T5.3 documentation work.
- **VERIFIED**: No tests or container checks were run by this Reviewer; runtime claims above are explicitly attributed to the Developer/Tester reports. No Tech Lead approval or final acceptance is claimed.

## 11. Final status

**REVIEW COMPLETE — NO CRITICAL OR MAJOR FINDINGS; THREE MINOR FINDINGS.** **VERIFIED**: The inspected implementation aligns with T5.2's Compose, PostgreSQL, production-profile and Flyway scope, while preserving the H2-backed default development profile. The reported fresh-volume runtime checks support the stated acceptance behavior but were not rerun by this Reviewer. This is a Reviewer recommendation only; final acceptance remains with the human Tech Lead.

## 12. Outstanding issues

- **Minor — RECONSTRUCTED**: Consider making the PostgreSQL readiness probe explicitly test TCP to avoid a temporary-local-server startup race.
- **Minor — PENDING VERIFICATION**: Add a Docker build-context exclusion policy for `.env` and irrelevant files, particularly if remote builders are used; no remote-builder exposure was verified.
- **Minor — VERIFIED**: README's Docker/Compose limitations are stale; update the setup/limitations documentation in the delivery documentation task.
- **PENDING VERIFICATION**: Human Tech Lead acceptance is not recorded by this audit.
- **VERIFIED**: The Tester-reported credentials mismatch with an existing PostgreSQL volume is environmental and is not listed as a code defect.

## 13. Handoff to next agent

- **VERIFIED**: This independent review is recorded at `agent-reports/T5.2/reviewer-report.md`.
- **VERIFIED**: No Critical or Major finding was identified. The three Minor findings are the local-socket readiness probe, missing Docker context exclusions, and stale Docker/Compose README wording.
- **PENDING VERIFICATION**: The human Tech Lead retains sole authority for final acceptance; this report does not grant approval.
