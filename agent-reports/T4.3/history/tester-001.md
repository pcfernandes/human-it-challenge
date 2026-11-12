# Tester Report — T4.3 Real CLI HTTP Smoke Test

## 1. Task ID

- **VERIFIED** (direct Tech Lead tasking): `T4.3 — Complete real CLI HTTP smoke test`.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T4.3-TEST-EXEC-001`, 2026-11-12 (Europe/Lisbon).
- **VERIFIED** (`agent-reports/T4.3/` inspection before report creation): no earlier `tester-report.md` was present; no Tester execution archive needed to be created.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Tester; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct Tech Lead tasking): Independently inspect documentation and relevant application/security configuration, attempt a real Spring Boot dev/H2 launch with temporary credentials, perform the specified HTTP checks with `curl.exe`, clean up, and report.
- **VERIFIED** (direct Tech Lead tasking): Do not rerun Maven tests, install dependencies, modify application/test source or documentation, commit/push/open a PR/merge, or mark T4.3 complete. The requested report is the only file created by this Tester execution.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`, `README.md`, `implementation-plan.md`, and `pom.xml`.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`, `db/migration/V1__baseline.sql`, and `db/migration/V2__create_clients_and_documents.sql`.
- **VERIFIED**: `AuthController.java`, `ClientController.java`, `DocumentController.java`, `SecurityConfiguration.java`, `SingleUserProvisioningConfiguration.java`, `AppDemoUserProperties.java`, `AppJwtProperties.java`, `LoginRequest.java`, `TokenResponse.java`, and `OpenApiConfig.java` under `src/main/java/com/humanit/challenge/`.
- **VERIFIED**: `agent-reports/T4.3/developer-report.md` was read for comparison only after the independent source/document inspection and after the runtime attempt had been determined blocked. A broad search result had accidentally included short excerpts of it earlier; this process deviation is recorded in Sections 8 and 9.
- **NOT AVAILABLE**: No `target/surefire-reports/*.txt` files were found by the requested workspace pattern search.

## 6. Files changed

- **VERIFIED** (post-write file inspection): Created only `agent-reports/T4.3/tester-report.md`.
- **VERIFIED** (`git status --short` before report writing): Pre-existing modified/untracked paths included `.opencode/agents/tester.md`, `README.md`, `implementation-plan.md`, `agent-reports/T3.5-CLOSEOUT/`, `agent-reports/T4.2-GIT-CLOSEOUT/`, and `agent-reports/T4.3/`. They were not changed by this Tester execution except for creating this report. No application/test source or documentation was edited.

## 7. Commands executed

- **VERIFIED**: `git status --short; java -version; .\mvnw.cmd -version` — status listed pre-existing work; Java reported `21.0.12.1`; Maven Wrapper reported Apache Maven `3.10.0`.
- **VERIFIED**: `python --version`; `python -c "import importlib.util; ..."`; `where.exe htpasswd`; `where.exe curl.exe`; `where.exe node`; `where.exe php`; `where.exe ruby`; `where.exe openssl`; `where.exe perl`; `where.exe py`; `where.exe git`; `curl.exe --version` — Python and the checked BCrypt-capable candidate executables (`htpasswd`, Node, PHP, Ruby, OpenSSL, Perl, `py`) were not available; `curl.exe` was available (8.9.1). No package was installed.
- **VERIFIED**: `Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue` — no listener was observed on port 8080 during preflight.
- **VERIFIED**: `git diff -- README.md implementation-plan.md` — inspected only the documentation diff; it shows README modernization and canonical `/api/clients` prefixes in T2.3, T2.4, and T2.7.
- **VERIFIED**: targeted source searches and reads — checked README legacy Phase 1 wording, documented routes, required environment names, security permit/protect rules, endpoint mappings, migration contents, and the targeted implementation-plan sections.
- **VERIFIED**: `git diff --check` — `DIFF_CHECK_PASS`; Git emitted LF-to-CRLF future-touch notices for README and implementation-plan, but no whitespace errors.
- **VERIFIED**: No `mvn test`, `mvnw spring-boot:run`, application launch, `curl.exe` HTTP request, process termination, or temp-file creation command was run. Maven tests were not rerun, as instructed.
- **VERIFIED** (direct Tech Lead task context): A previous Tester attempt was blocked while accessing the external Maven cache. I did not retry that denied cache access.

## 8. Evidence and observed results

- **VERIFIED** (`java -version` output): The installed Java runtime is `21.0.12.1` LTS. **VERIFIED** (`pom.xml`): the project declares Spring Boot `3.5.16`. **NOT AVAILABLE**: An actual runtime Spring Boot version could not be observed because the application was not started. Maven Wrapper reported Maven `3.10.0`.
- **VERIFIED** (`SecurityConfiguration.java`, `SingleUserProvisioningConfiguration.java`, `AppDemoUserProperties.java`, `AppJwtProperties.java`): Development credentials bind to the requested demo-user environment names; the JWT secret is Base64 with a minimum decoded length of 32 bytes; credentials require a BCrypt hash. The code exposes a BCrypt password encoder. **NOT AVAILABLE**: No local BCrypt hash was generated. The checked local executable candidates were absent, and Maven cache access was not retried after the prior denial recorded in task context.
- **VERIFIED** (execution actions): No temporary username/password/hash/JWT secret was created, no authentication environment variables were set, and no credential or token value was printed. Because the required hash could not be produced, startup was not attempted.
- **VERIFIED** (port preflight): Port 8080 was not listening before any application launch attempt. **NOT AVAILABLE**: Application-started/listening status, Spring startup success, Flyway V1/V2 runtime completion, and startup-error status could not be tested because no application process was started.
- **NOT AVAILABLE** (no HTTP calls were made): Login `POST /auth/login` status; authenticated `GET /api/clients?page=0&size=20` status and JSON; unauthenticated GET status; and `GET /v3/api-docs` status and OpenAPI JSON. No HTTP status codes are claimed.
- **VERIFIED** (README and source/config inspection): README documents `SPRING_PROFILES_ACTIVE=dev`, the three requested development variables, `POST /auth/login` with `username`/`password`, `accessToken` use as a bearer token for paginated `GET /api/clients`, Swagger UI `/swagger-ui/index.html`, and OpenAPI `/v3/api-docs`. The controller and security mappings agree with these documented paths and access expectations.
- **VERIFIED** (`README.md`, `application*.yml`, `V1__baseline.sql`, `V2__create_clients_and_documents.sql`): README startup/configuration and H2 instructions match the inspected declarations; it accurately describes V1 as comment-only and V2 as creating the Client/Document schema. This is structural inspection, not proof migrations execute successfully.
- **VERIFIED** (`README.md`, `implementation-plan.md`, controllers): README contains no obsolete Phase 1-only/no-endpoints/no-authentication claims. T2.3/T2.4/T2.7 use `/api/clients` paths. README accurately states real PostgreSQL integration is unverified and Dockerfile/Compose are not implemented. Login/protected GET examples use the observed controller routes and request fields.
- **VERIFIED** (direct Tech Lead tasking, prior-run evidence only): The prior Maven suite is reported as `335/335` tests passing with `BUILD SUCCESS`. This was not rerun or independently verified in this execution; no Surefire text reports were available in the workspace search.
- **VERIFIED** (cleanup actions): No application process or temporary file was started/created by this execution, so there was no Tester-owned process to stop or temp file to remove. No unrelated process was terminated. The preflight port check observed no listener.
- **VERIFIED** (comparison with `agent-reports/T4.3/developer-report.md`, performed after independent inspection/blocked determination): Its runtime/startup checks are explicitly pending and it does not claim the CLI smoke test passed; this agrees with the current blocked result. Its structural documentation claims agree with the files independently inspected. No factual discrepancy was found in those compared claims.
- **VERIFIED** (tool search output): Before the full Developer report was read for comparison, an overly broad search result unexpectedly displayed short portions of that report. This was a procedural deviation from the strict read-after-independent-verification rule. Direct README, plan, configuration and controller inspections had already been made independently; no Developer runtime result was used to infer an HTTP or startup outcome. The deviation is disclosed and limits the strength of the independence claim.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| T43-TST-01 | Blocker — runtime verification incomplete | **NOT AVAILABLE**: No BCrypt hash could be generated using a checked local implementation, and the previously denied external Maven cache was not retried. The required app launch, Flyway runtime checks, and all four real HTTP tests therefore remain unexecuted. No HTTP statuses are available. |
| T43-TST-02 | Minor — process deviation | **VERIFIED**: A broad search result exposed brief Developer-report excerpts before the runtime verification stage was concluded. The incident is recorded above; application/runtime conclusions remain unclaimed and the smoke test was blocked independently by the missing local hash generator. |
| T43-TST-03 | Informational — documentation review | **VERIFIED**: Inspected README/config/controller/planning paths were consistent; `git diff --check` passed. Structural review does not substitute for the required runtime smoke test. |

## 10. Decisions and reasoning

- **VERIFIED** (direct task constraint and environment checks): Did not request/access the external Maven cache again after the prior denial; did not install packages. The local candidate check did not identify a BCrypt generation implementation, so I did not invent or substitute a non-BCrypt hash.
- **VERIFIED** (execution actions): Did not start the application without valid temporary credentials, and did not issue HTTP calls without a valid access token. This avoids reporting fabricated responses or leaving an untracked app process running.
- **VERIFIED** (`README.md`, `implementation-plan.md`, and source inspection): Documentation review can be reported as structurally passing, but overall runtime-smoke status must remain blocked/FAIL until actual startup and HTTP responses are obtained.
- **VERIFIED** (direct task constraints): The reported `335/335` Maven result is labeled previous-execution evidence and was not rerun. No T4.3 completion or acceptance approval is claimed.

## 11. Final status

**BLOCKED — OVERALL FAIL FOR THE REQUESTED REAL CLI HTTP SMOKE TEST; T4.3 NOT COMPLETE.** Documentation/source consistency checks and `git diff --check` passed. Application startup, Flyway runtime, login, protected/unprotected Client GET, and OpenAPI HTTP checks were not run because a BCrypt hash could not be generated without the denied Maven-cache route or a locally available implementation. No HTTP status codes are available.

## 12. Outstanding issues

- **PENDING VERIFICATION**: With an approved locally available BCrypt generator or otherwise authorized dependency access, generate ephemeral credentials and secret, launch the dev/H2 application, confirm port 8080 and Flyway V1/V2, run the four specified `curl.exe` requests, and clean up only that process.
- **PENDING VERIFICATION**: Reviewer should treat the absent HTTP/startup evidence as a blocker and independently audit according to the reporting workflow; no reviewer result is available in this execution.
- **VERIFIED**: No Tester-owned app process or temporary artifact remained to clean up. Existing unrelated work was preserved.
- **VERIFIED** (direct task constraints): Maven suite evidence is only the prior-run `335/335` report from task context; it was not rerun. T4.3 remains incomplete pending runtime verification, independent review, and human Tech Lead acceptance.

## 13. Handoff to next agent

- **VERIFIED**: Report path: `agent-reports/T4.3/tester-report.md`.
- **PENDING VERIFICATION**: Resolve the BCrypt-generation environment blocker without repeating the denied Maven-cache access, then collect actual startup/Flyway and HTTP evidence. Do not infer any response code from this report.
- **VERIFIED** (direct task constraints): Final status is **BLOCKED / FAIL for runtime verification**; outstanding findings are `T43-TST-01` and the disclosed process deviation `T43-TST-02`. No T4.3 completion or human approval is claimed.
