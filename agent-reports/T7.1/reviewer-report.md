# Reviewer Report — T7.1 Independent Final Demo Package Review

## 1. Task ID

T7.1 — Human IT Docker Image Demo Package.

## 2. Execution identifier and date

- Execution: T7.1-REV-001
- Date: 2026-11-13
- No prior `agent-reports/T7.1/reviewer-report.md` existed; no history archive was required.

## 3. Agent role and model

- Role: Reviewer.
- Model: opencode-go/beam-free.
- Independent inspection of repository files was completed before reading `agent-reports/T7.1/developer-report.md` and `agent-reports/T7.1/tester-report.md`.

## 4. Authorized scope

Read-only audit of the T7.1 demo package: `DEMO.md`, `compose.demo-image.yaml`, helper scripts, Postman files, publishing workflow, Dockerfile, `.env.example`, application configuration, and authentication/Client/Document controllers and DTOs. No application code, tests, configuration, requirements, planning files, scripts, other agents' reports, or Git state were modified. No Maven, Docker container, or Postman execution was performed by this Reviewer.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`; `DEMO.md`; `compose.demo-image.yaml`; `compose.yaml`; `Dockerfile`; `.env.example`; `.gitignore`.
- **VERIFIED**: `scripts/demo-start.ps1`; `scripts/demo-start.sh`.
- **VERIFIED**: `postman/HumanIT-Demo.postman_collection.json`; `postman/HumanIT-Local.postman_environment.json`.
- **VERIFIED**: `.github/workflows/docker-publish.yml`.
- **VERIFIED**: `src/main/resources/application.yml`; `application-dev.yml`; `application-prod.yml`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/AuthController.java`; `ClientController.java`; `DocumentController.java`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/dto/auth/LoginRequest.java`; `TokenResponse.java`; Client and Document request/response DTOs; `PatchValue` and `PatchValueDeserializer.java`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java`; `config/AppJwtProperties.java`; `config/JwtSecretValidator.java`.
- **VERIFIED**: `agent-reports/T7.1/developer-report.md` and `agent-reports/T7.1/tester-report.md` (read only after independent inspection).

## 6. Files changed

- **VERIFIED**: Created only `agent-reports/T7.1/reviewer-report.md`.
- **VERIFIED**: No other file was changed. No archive was required.

## 7. Commands executed

- **VERIFIED** (read-only Git): `git status --short`; `git log --oneline -10`.
- **VERIFIED**: No Maven, Docker, Compose, application, Postman, commit or push command was executed by this Reviewer.
- **PENDING VERIFICATION**: JSON, script syntax, and Compose rendering were not rerun by this Reviewer; the results recorded in `agent-reports/T7.1/tester-report.md` (T7.1-TEST-001) and `agent-reports/T7.1/developer-report.md` (T7.1-DEV-001) were used as supporting evidence and cross-checked against repository files.

## 8. Evidence and observed results

### 8.1 Fresh-machine startup instructions

- **VERIFIED** (`DEMO.md` sections 1–10): Prerequisites, repository acquisition, `.env` creation, secure value generation, startup, readiness, Swagger/OpenAPI, Postman, stop/restart/reset, troubleshooting, and maintainer publishing are documented.
- **VERIFIED** (`DEMO.md`): States that no Java, Maven, Gradle, or local source compilation is required by consumers.
- **VERIFIED** (`.env.example`): Contains exactly the six variables used by Compose and the application.
- **VERIFIED** (`scripts/demo-start.ps1`, `scripts/demo-start.sh`): Both check Docker daemon and Compose plugin availability, create `.env` from `.env.example`, fill missing/placeholder values, pull the image, start Compose, and poll readiness for 120 seconds.

### 8.2 Docker image-only startup and no local build

- **VERIFIED** (`compose.demo-image.yaml`): Uses `image: ghcr.io/pcfernandes/human-it-challenge:latest` and has no `build` section.
- **VERIFIED** (`Dockerfile`): Build occurs only inside the image using `eclipse-temurin:21-jdk-alpine` and `./mvnw -DskipTests package`; runtime uses `eclipse-temurin:21-jre-alpine`.
- **VERIFIED** (`git status --short`): The only changes are untracked demo deliverables; no tracked application source or configuration was modified.

### 8.3 Environment variables and secure credential handling

- **VERIFIED** (`compose.demo-image.yaml`): All six `.env` variables are mapped to app/postgres services with `:?` fail-fast guards; `SPRING_PROFILES_ACTIVE: prod`.
- **VERIFIED** (`JwtSecretValidator.java`, `AppJwtProperties.java`): JWT secret is externalized, standard Base64, minimum 32 decoded bytes; rejected values are not included in diagnostics.
- **VERIFIED** (scripts): Generated DB passwords use 24 random bytes and JWT secrets use 32 random bytes via CSPRNG; entered BCrypt hashes are wrapped in single quotes when written to `.env`.
- **VERIFIED** (`.gitignore`): `.env`, `.env.*` (except `.env.example`), keys and local profile files are ignored.
- **VERIFIED** (scripts): Generated/stored password, JWT secret, and password hash values are not printed.

### 8.4 PostgreSQL persistence and network exposure

- **VERIFIED** (`compose.demo-image.yaml`): PostgreSQL has no `ports` mapping; only app publishes `8080:8080`.
- **VERIFIED** (`compose.demo-image.yaml`): Named volume `postgres_data` is mounted at `/var/lib/postgresql/data`.
- **VERIFIED** (`compose.demo-image.yaml`): App depends on postgres `service_healthy`; healthcheck uses `pg_isready`.

### 8.5 Postman endpoint, DTO, JWT and ID capture accuracy

- **VERIFIED** (controllers/DTOs vs Postman): `/auth/login` with `{ username, password }`; `TokenResponse.accessToken` captured into `token`; invalid login expects 401.
- **VERIFIED** (controllers/DTOs vs Postman): Client endpoints, pagination (`page`, `size`), invalid size 400, missing client 404, duplicate tax identifier 409, PUT/PATCH bodies and status codes match DTOs and controllers.
- **VERIFIED** (controllers/DTOs vs Postman): Document endpoints, `X-Client-Version`/`X-Document-Version` headers, create/get/update/patch/list/delete flows, and 201/200/204 expectations match controllers.
- **VERIFIED** (Postman environment): Defines `baseUrl`, `username`, `password`, `token`, `clientId`, `clientVersion`, `documentId`, `documentVersion`; collection captures `id` and `version` from responses.

### 8.6 GHCR workflow and image tags

- **VERIFIED** (`.github/workflows/docker-publish.yml`): Triggers on push to `main` and `workflow_dispatch`; permissions `contents: read`, `packages: write`; image `ghcr.io/${{ github.repository }}`; tags `latest` on default branch and `sha-<full commit SHA>`; builds `./Dockerfile` with `push: true`.
- **VERIFIED** (`DEMO.md` section 10): Documents the same triggers, tags, and publication behavior.

### 8.7 No application code or security behavior modified

- **VERIFIED** (`git status --short`): Only untracked demo-package files and `agent-reports/T7.1/` are present; no tracked file is modified.
- **VERIFIED** (file inspection): Security behavior remains externalized-secret JWT/BCrypt with prod profile and no committed secrets.

## 9. Findings and severity

| ID | Severity | Finding and evidence |
|---|---|---|
| T7.1-REV-01 | Minor | **PowerShell single-line `.env` handling.** `Set-EnvValue` in `scripts/demo-start.ps1` (lines 40–61) assigns `Get-Content` output to `$lines`; if `.env` has one line, `$lines` is a string and `foreach ($line in $lines)` iterates characters, corrupting the file. Typical multi-line `.env` from `.env.example` is unaffected. |
| T7.1-REV-02 | Minor | **PowerShell output encoding.** `Set-Content` in `scripts/demo-start.ps1` (line 60) uses the host default encoding rather than explicit UTF-8. Values are ASCII, so no functional impact. |
| T7.1-REV-03 | Minor | **Inconsistent duplicate-key handling.** `set_env` in `scripts/demo-start.sh` (lines 38–57) replaces all matching key lines, while PowerShell removes duplicates. |
| T7.1-REV-04 | Minor | **No helper-script credential format validation.** Scripts do not validate a user-supplied JWT secret (Base64, >= 32 decoded bytes) or BCrypt hash format; invalid values are detected at application startup. |
| T7.1-REV-05 | Minor | **Sequential Postman execution requirement.** The collection is order-dependent and uses unquoted template variables such as `"version": {{clientVersion}}`; out-of-order or empty-variable runs produce invalid requests. DEMO.md should state that the collection must be run sequentially in listed order, preferably via the Collection Runner. |
| T7.1-REV-06 | Minor | **No automated test gate before image publication.** `.github/workflows/docker-publish.yml` builds/pushes without running tests, and `Dockerfile` uses `-DskipTests`. The published image is therefore not gated by the project test suite. |
| T7.1-REV-07 | Minor | **Documentation wording mismatch.** `DEMO.md` section 3 states all six variables are mandatory, while helper scripts auto-fill `POSTGRES_DB`/`POSTGRES_USER` defaults when placeholders are present. |

No Critical or Major findings were identified.

Additional observations (not findings): Bash script executable bit cannot be set on Windows, and DEMO.md correctly instructs `bash scripts/demo-start.sh`; Compose relies on automatic `.env` loading from the repository root, as assumed by DEMO.md.

## 10. Decisions and reasoning

- The seven reported Minor findings are confirmed. None affects the normal multi-line `.env`, published-image-only startup path, PostgreSQL persistence/exposure, or Postman endpoint/DTO correctness.
- T7.1-REV-01 is the most actionable script robustness issue and should be corrected, but it is an edge-case defect rather than a blocker for the documented flow.
- T7.1-REV-06 is a CI quality-gate gap, not a runtime defect in the demo package; it should be addressed before relying on automated publication, but it does not invalidate the manually validated deliverables.
- Runtime end-to-end execution of the published image and Postman collection remains unverified by task constraints and is recorded as pending.
- GHCR package visibility and the first workflow execution are maintainer actions outside this Reviewer's scope.

## 11. Final status

**ACCEPT WITH CONDITIONS** — demo package is structurally valid, internally consistent, and compliant with the image-only startup requirement. Conditions are maintainer/runtime verification items and the Minor robustness/documentation fixes above.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Runtime startup and end-to-end execution of the published image and full Postman collection (task constraints prohibited running them).
- **PENDING VERIFICATION**: GHCR package must be made Public by a maintainer.
- **PENDING VERIFICATION**: GitHub Actions workflow has not been executed yet.
- **Minor**: T7.1-REV-01 through T7.1-REV-07 remain open.
- **NOT AVAILABLE**: No Human Tech Lead approval for T7.1 is recorded in `agent-reports/decisions.md`.

## 13. Handoff to next agent

Human Tech Lead: disposition the seven Minor findings and perform/authorize the pending maintainer actions. Exact actions required before approval:
1. Fix `Set-EnvValue` in `scripts/demo-start.ps1` to normalize `Get-Content` output to an array before iterating.
2. Write `.env` with explicit UTF-8 encoding in the PowerShell script.
3. Harmonize duplicate-key handling between `scripts/demo-start.ps1` and `scripts/demo-start.sh`.
4. Optionally add JWT secret and BCrypt hash format validation to both helper scripts.
5. Add an explicit sequential-execution note to `DEMO.md` and/or the Postman collection description.
6. Add a test step (or `mvn verify`/`mvn test`) to `.github/workflows/docker-publish.yml` before image build/push, or otherwise document the test gate.
7. Clarify in `DEMO.md` that `POSTGRES_DB`/`POSTGRES_USER` default when placeholders are present.
8. Make the GHCR package public and run the publishing workflow.
9. Perform runtime end-to-end verification of the published image and Postman collection when authorized.

No approval is recorded or implied by this report.
Report path: `agent-reports/T7.1/reviewer-report.md`.
Final status: ACCEPT WITH CONDITIONS.
Outstanding findings: 7 Minor (T7.1-REV-01 through T7.1-REV-07); runtime/GHCR/workflow verification pending.