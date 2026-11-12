# Developer Report — T4.3 README and Technical Decisions

## 1. Task ID

- **VERIFIED** (direct Tech Lead tasking and `agent-reports/T4.3/architect-proposal.md`): `T4.3 — README and Technical Decisions`.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T4.3-DEV-001`, 2026-11-12.
- **VERIFIED** (`agent-reports/T4.3/` pattern inspection before writing): `developer-report.md` and a `history/` archive were not present, so no previous Developer report required archiving.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Developer; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct Tech Lead authorization): Implement the Architect's T4.3 documentation proposal only.
- **VERIFIED** (direct tasking): Change only `README.md`, `implementation-plan.md`, and the new `agent-reports/T4.3/developer-report.md`. Do not change requirements, application/test code, Docker assets, or other reports; do not run the full Maven suite; do not commit, push, create a PR, merge, switch branches, modify the stash, or mark T4.3 complete.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`, `agent-reports/T4.3/architect-proposal.md`, `README.md`, `implementation-plan.md`, and `requirements-decisions.md`.
- **VERIFIED**: `pom.xml`, `.mvn/wrapper/maven-wrapper.properties`, `mvnw.cmd`, `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`, `src/main/resources/db/migration/V1__baseline.sql`, and `V2__create_clients_and_documents.sql`.
- **VERIFIED**: `AuthController.java`, `ClientController.java`, `DocumentController.java`, `SecurityConfiguration.java`, `SecurityProblemHandlers.java`, `SingleUserProvisioningConfiguration.java`, `GlobalExceptionHandler.java`, `OpenApiConfig.java`, `LoginRequest.java`, `TokenResponse.java`, `AppDemoUserProperties.java`, `AppJwtProperties.java`, `Client.java`, `Document.java`, `ClientService.java`, `DocumentService.java`, and `ClientRepository.java` at their corresponding paths under `src/main/java/com/humanit/challenge/`.

## 6. Files changed

- **VERIFIED**: `README.md` — replaced obsolete Phase 1 status with current purpose, prerequisites, H2 startup, credential environment variables, V1/V2 migrations, login and bearer usage, endpoints, OpenAPI URLs, pagination/update/version behavior, error format, build/test commands, design decisions and limitations.
- **VERIFIED**: `implementation-plan.md` — aligned T2.3 Client routes, T2.4 nested Document routes, and T2.7 DELETE routes with `/api/clients`.
- **VERIFIED**: `agent-reports/T4.3/developer-report.md` — created this report under the mandatory report convention. No earlier Developer report existed to archive.
- **VERIFIED**: No application code, tests, `pom.xml`, `requirements-decisions.md`, Architect/other agent reports, Docker assets, or Git/stash metadata were changed by this execution.

## 7. Commands executed

- **VERIFIED**: `git status --short; git diff -- README.md implementation-plan.md; git stash list` — inspected pre-existing work, allowed-file diff, and stash state. The output showed `stash@{0}: On feature/T3.3-jwt-login: T3.4 preserve pre-existing work`; no stash operation was performed.
- **VERIFIED**: `git diff --check` — executed for documentation changes. It reported no whitespace errors. Git emitted line-ending notices that LF will be replaced by CRLF for `README.md` and `implementation-plan.md` on a future Git touch.
- **VERIFIED** (PowerShell command output): `rg -n -F ' /clients' implementation-plan.md; if ($LASTEXITCODE -eq 1) { Write-Output 'No bare /clients route references in implementation-plan.md' }` — could not run because `rg` is not installed/available in this environment. The requested T2.3/T2.4/T2.7 sections were instead read directly and their changed paths were inspected in `git diff`.
- **VERIFIED**: No Maven command, application startup, test command, Git write operation, or stash operation was run.
- **VERIFIED**: File reads, file-pattern inspection and content search were used to compare documentation with project sources; they did not execute the documented runtime/build examples.

## 8. Evidence and observed results

- **VERIFIED** (`pom.xml`, `.mvn/wrapper/maven-wrapper.properties`, `mvnw.cmd`): Java 21 is enforced; Maven 3.10.0 is the wrapper distribution; wrapper version is 3.3.4; the Spring Boot Maven plugin and Maven Enforcer configuration are present. The documented `spring-boot:run`, `clean verify`, and `test` wrapper command forms are consistent with those artifacts. These commands were checked against source but not run.
- **VERIFIED** (`application.yml`, `application-dev.yml`, `application-prod.yml`, `AppJwtProperties.java`, `AppDemoUserProperties.java`, `SingleUserProvisioningConfiguration.java`): `dev` is the default H2 profile. The documented development variables are `APP_SECURITY_JWT_SECRET`, `APP_SECURITY_DEMO_USER_USERNAME`, and `APP_SECURITY_DEMO_USER_PASSWORD_HASH`; the JWT secret is standard Base64 with at least 32 decoded bytes, and the configured password value must be BCrypt. `prod` uses PostgreSQL environment variables and a separately configured account. No credential values were added to the README.
- **VERIFIED** (`V1__baseline.sql`, `V2__create_clients_and_documents.sql`, `application.yml`): V1 is comment-only; V2 creates the Clients and Documents schema and constraints. Flyway uses `classpath:db/migration`; Hibernate is set to `ddl-auto: validate`; SQL initialization is disabled.
- **VERIFIED** (`AuthController.java`, `LoginRequest.java`, `TokenResponse.java`, `SecurityConfiguration.java`): Login is `POST /auth/login`, accepts `username` and `password`, returns `accessToken` and `tokenType`, and the authenticated route policy requires bearer authentication outside the permitted login and Swagger/OpenAPI paths. The documented protected GET uses the controller's `/api/clients` mapping and supported `page` and `size` parameters.
- **VERIFIED** (`ClientController.java`, `DocumentController.java`, `OpenApiConfig.java`): Client CRUD is mapped under `/api/clients`; Document CRUD is mapped under `/api/clients/{clientId}/documents`; Swagger UI and OpenAPI JSON paths are `/swagger-ui/index.html` and `/v3/api-docs` and are permitted without authentication. Both list controllers use zero-based pages, defaults 0/20, ascending ID order and a maximum size of 100.
- **VERIFIED** (`ClientController.java`, `DocumentController.java`, service/domain/repository files, and `requirements-decisions.md`): The README's summary reflects implemented PUT/PATCH collection semantics, expected version inputs, optimistic conflicts, parent Client version changes for Document mutations, and transaction boundaries. The README does not reproduce every endpoint-specific version rule.
- **VERIFIED** (`GlobalExceptionHandler.java`, `SecurityProblemHandlers.java`, `OpenApiConfig.java`): Application and security error handling uses RFC 9457 Problem Details with `application/problem+json`; inspected handlers avoid returning stack traces, secret values, or SQL details.
- **VERIFIED** (`implementation-plan.md`, exact T2.3/T2.4/T2.7 diff): Only route paths in the specified task sections changed; task descriptions and acceptance criteria were otherwise preserved.
- **VERIFIED** (`git diff --check` output): No whitespace errors were reported. The displayed LF-to-CRLF notices are line-ending warnings, not whitespace errors.
- **PENDING VERIFICATION**: No PowerShell example, wrapper build/test command, app startup, login, protected GET or full Maven suite was executed in this Developer stage. Tester execution remains necessary to validate runtime behavior and actual setup.
- **NOT AVAILABLE** (`README.md` and inspected project files): No working credential, sample BCrypt hash or repository-provided BCrypt-generation utility was included. The README uses explicit placeholders and requires user-provisioned values.
- **VERIFIED** (initial `git status --short` output): Untracked directories `agent-reports/T3.5-CLOSEOUT/`, `agent-reports/T4.2-GIT-CLOSEOUT/`, and `agent-reports/T4.3/` were present before the documentation edits. They were not modified except for creating this authorized report in the existing T4.3 directory. The existing stash was left untouched.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| T43-DEV-01 | Minor — verification pending | **PENDING VERIFICATION**: README PowerShell setup, login, protected GET, wrapper build and test instructions need to be followed independently by the Tester. Structural inspection is not proof of successful execution. |
| T43-DEV-02 | Minor — environment prerequisite | **NOT AVAILABLE**: The repository does not provide a credential or BCrypt-hash generation workflow. Users/Testers must supply their own valid username, corresponding password and BCrypt hash as the README instructs. |
| T43-DEV-03 | Informational — known limitation | **VERIFIED** (`application-prod.yml`, `implementation-plan.md` T2.9 and Docker artifact search referenced in `agent-reports/T4.3/architect-proposal.md`): PostgreSQL is configured but real PostgreSQL integration is not verified; Dockerfile/Compose are not yet implemented. README now states both limitations. |

## 10. Decisions and reasoning

- **VERIFIED** (authorized scope and `agent-reports/T4.3/architect-proposal.md`): Replaced outdated README statements instead of appending a conflicting status note. Kept the update limited to the approved documentation files and did not edit `requirements-decisions.md`.
- **VERIFIED** (`README.md`, source/configuration cross-check): Used PowerShell examples with placeholders for the username, plaintext login password and BCrypt hash; JWT generation creates random bytes locally. No working credentials or secret were invented or exposed.
- **VERIFIED** (`implementation-plan.md` diff): Updated only T2.3, T2.4 and T2.7 route strings to canonical `/api/clients` paths, preserving their task descriptions and acceptance criteria.
- **VERIFIED** (direct task constraints): Did not run the Maven suite or application and did not modify code/tests, Docker files, requirements, the stash or Git history. Documentation command claims are source-checked, not runtime-verified.

## 11. Final status

**DOCUMENTATION IMPLEMENTED — READY FOR TESTER VERIFICATION — T4.3 NOT COMPLETE.** **VERIFIED**: The authorized README and route-reference updates were made, and `git diff --check` reported no whitespace errors. **PENDING VERIFICATION**: Runtime setup and all documented commands remain for independent Tester verification. No final acceptance is claimed.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Tester should follow the documented PowerShell H2 startup, authenticate with Tester-owned credentials and a valid BCrypt hash, then perform the documented bearer-authenticated `GET /api/clients`.
- **PENDING VERIFICATION**: Tester should validate the documented wrapper commands and run the authorized test/build checks independently. No Maven suite was run in this Developer stage.
- **VERIFIED**: Dockerfile and Compose are not implemented; the README does not provide container commands.
- **VERIFIED**: PostgreSQL configuration exists, but a real PostgreSQL integration has not been verified.
- **VERIFIED** (`implementation-plan.md`, Phase 3 decisions): Registration, roles/permissions and refresh tokens are not implemented.
- **PENDING VERIFICATION**: Independent Reviewer audit and human Tech Lead acceptance remain required; this report does not mark T4.3 complete.

## 13. Handoff to next agent

- **VERIFIED**: Documentation changes are ready for the Tester to independently verify. Test the documented setup without reading this report as a prerequisite, using Tester-owned credentials and a valid BCrypt hash.
- **PENDING VERIFICATION**: Report observed command and endpoint results independently; resolve any mismatch between the README instructions and runtime behavior through authorized follow-up.
- **VERIFIED**: Report path: `agent-reports/T4.3/developer-report.md`. Final status: documentation implemented; ready for Tester verification; T4.3 remains open. Outstanding items: runtime/build/test verification, PostgreSQL verification, Docker/Compose delivery, independent review and Tech Lead acceptance.
