# Tester Report

## 1. Task ID

T7.1 — Independent Demo Package Validation

## 2. Execution identifier and date

Execution: T7.1-TEST-001
Date: 2026-11-13

## 3. Agent role and model

Tester role. Model: opencode-go/beam-free.

## 4. Authorized scope

Independent, read-only/static validation of the T7.1 demo package before reading `agent-reports/T7.1/developer-report.md`. No application code, tests, configuration or demo deliverables were modified. No Maven builds, Docker containers or Postman requests were run. Only static validation commands (JSON parsing, script syntax parsing, Compose configuration rendering) were executed. After independent verification, `agent-reports/T7.1/developer-report.md` was read and compared.

## 5. Files inspected

- `DEMO.md` (VERIFIED)
- `compose.demo-image.yaml` (VERIFIED)
- `scripts/demo-start.ps1` (VERIFIED)
- `scripts/demo-start.sh` (VERIFIED)
- `postman/HumanIT-Demo.postman_collection.json` (VERIFIED)
- `postman/HumanIT-Local.postman_environment.json` (VERIFIED)
- `Dockerfile` (VERIFIED)
- `compose.yaml` (VERIFIED)
- `.env.example` (VERIFIED)
- `.gitignore` (VERIFIED)
- `.github/workflows/docker-publish.yml` (VERIFIED)
- `src/main/java/com/humanit/challenge/controller/AuthController.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/controller/ClientController.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/controller/DocumentController.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/auth/LoginRequest.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/auth/TokenResponse.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/client/ClientCreateRequest.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/client/ClientPutRequest.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/client/ClientPatchRequest.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/client/ClientResponse.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/document/DocumentCreateRequest.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/document/DocumentPutRequest.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/document/DocumentPatchRequest.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/dto/document/DocumentResponse.java` (VERIFIED)
- `src/main/resources/application.yml` (VERIFIED)
- `src/main/resources/application-dev.yml` (VERIFIED)
- `src/main/resources/application-prod.yml` (VERIFIED)
- `src/main/java/com/humanit/challenge/config/AppJwtProperties.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/config/JwtSecretValidator.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/security/SingleUserProvisioningConfiguration.java` (VERIFIED)
- `src/main/java/com/humanit/challenge/service/ClientService.java` (VERIFIED)
- `agent-reports/T7.1/developer-report.md` (read only after independent verification; VERIFIED)

## 6. Files changed

Created only `agent-reports/T7.1/tester-report.md` (this report). No application code, tests, configuration or demo deliverables were modified.

## 7. Commands executed

- `Get-Content ... | ConvertFrom-Json` on both Postman files — VERIFIED: both parse successfully; collection has 4 top-level items; environment has 8 values.
- `[System.Management.Automation.Language.Parser]::ParseFile` on `scripts/demo-start.ps1` — VERIFIED: parse OK.
- `bash -n scripts/demo-start.sh` (Git Bash) — VERIFIED: syntax OK (exit 0).
- `docker compose --env-file .env.example -f compose.demo-image.yaml config` — VERIFIED: renders successfully; app image `ghcr.io/pcfernandes/human-it-challenge:latest`; postgres has no published ports; named volume `postgres_data` present; `SPRING_PROFILES_ACTIVE=prod`.
- `Select-String` for `replace-with` across non-target/non-git files — VERIFIED: only `.env.example`, Postman environment placeholders, and scripts' placeholder detection logic.
- `git status --short` / `git log --oneline -5` — VERIFIED: demo deliverables are untracked; no tracked files modified.

## 8. Evidence and observed results

### 8.1 Fresh-machine setup instructions and prerequisites

- DEMO.md sections 1–3 list Docker Engine/Desktop with Compose plugin, optional Git/ZIP, PowerShell or POSIX shell, and network access to pull `ghcr.io/pcfernandes/human-it-challenge:latest` and `postgres:17-alpine` — VERIFIED.
- Docker verification commands (`docker --version`, `docker compose version`, `docker info`) are documented — VERIFIED.
- `.env.example` contains exactly six variables: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `APP_SECURITY_JWT_SECRET`, `APP_SECURITY_USER_USERNAME`, `APP_SECURITY_USER_PASSWORD_HASH` — VERIFIED.
- DEMO.md documents generation commands for database password, JWT secret (32 random bytes, Base64) and BCrypt hash, plus single-quoting for `$` — VERIFIED.

### 8.2 Docker image-only startup, without local Java or Maven

- `compose.demo-image.yaml` uses `image: ghcr.io/pcfernandes/human-it-challenge:latest` and has no `build` section — VERIFIED.
- DEMO.md states no Java/Maven/Gradle/source compilation is required for consumers — VERIFIED.
- `Dockerfile` performs the build inside the image (maintainer-side) using `eclipse-temurin:21-jdk-alpine` and `./mvnw -DskipTests package`, runtime uses `eclipse-temurin:21-jre-alpine` — VERIFIED.

### 8.3 Environment variables, secure JWT secret format and credential handling

- Compose maps all six `.env` variables into the app and postgres services with `:?` fail-fast guards — VERIFIED.
- `SPRING_PROFILES_ACTIVE: prod` is set in Compose — VERIFIED.
- `JwtSecretValidator` requires non-blank standard Base64 decoding to at least 32 bytes and never includes the secret value in failure messages — VERIFIED.
- Helper scripts generate 24-byte (DB password) and 32-byte (JWT secret) Base64 values using CSPRNG (`New-RandomBase64` / `openssl rand -base64` or `/dev/urandom`) — VERIFIED.
- Scripts wrap the entered BCrypt hash in single quotes when writing `.env` — VERIFIED.
- `.gitignore` ignores `.env`, `.env.*` (except `.env.example`), keys and local profile files — VERIFIED.

### 8.4 No passwords or secrets printed or committed

- Scripts print `(value not shown)` for generated/stored password, JWT secret and password hash — VERIFIED.
- No `.env` file is present in the working tree — VERIFIED (NOT AVAILABLE by design; `.gitignore`).
- Secret scan for `replace-with` found only `.env.example` placeholders, Postman environment placeholders, and script placeholder-detection logic — VERIFIED.
- No committed application secret values found in inspected source/configuration — VERIFIED.

### 8.5 PowerShell and Bash script behavior, error handling and startup timeout

- Both scripts parse successfully — VERIFIED.
- Both check Docker daemon and Compose plugin availability and exit non-zero on failure — VERIFIED.
- Both create `.env` from `.env.example` when missing and fail if the example is missing — VERIFIED.
- Both fill missing/placeholder values (DB name/user defaults, generated DB password/JWT secret, prompted username/hash) — VERIFIED.
- Both pull the image before `docker compose up -d` and check the pull exit code (PowerShell checks; Bash uses `set -e`) — VERIFIED.
- Both wait for readiness on `http://localhost:8080/v3/api-docs` with a 120-second timeout and 3-second polling — VERIFIED.
- Bash readiness uses `curl` then `wget`, failing if neither is available — VERIFIED.
- PowerShell readiness uses `Invoke-WebRequest` with a 5-second request timeout — VERIFIED.

### 8.6 Compose configuration, PostgreSQL persistence and network exposure

- `docker compose config` renders successfully — VERIFIED.
- Postgres service has no `ports` mapping; only app publishes `8080:8080` — VERIFIED.
- Named volume `postgres_data` mounted at `/var/lib/postgresql/data` — VERIFIED.
- Postgres healthcheck uses `pg_isready` with `start_period: 5s`, `interval: 5s`, `timeout: 3s`, `retries: 10` — VERIFIED.
- App `depends_on` postgres with `condition: service_healthy` — VERIFIED.

### 8.7 Postman authentication, JWT capture, IDs, payloads, status codes and sequential execution

- Collection-level bearer auth uses `{{token}}` — VERIFIED.
- `Login` POST `/auth/login` with `{ username, password }` expects 200 and stores `json.accessToken` into `token` — VERIFIED against `TokenResponse.accessToken`.
- `Login with invalid credentials` expects 401 — VERIFIED against `AuthController` `ResponseStatusException(UNAUTHORIZED)`.
- `Create client` POST `/api/clients` payload fields (`firstName`, `lastName`, `taxIdentifier`, `email`, `phoneNumber`, `documents[].number/description/expirationDate`) match DTOs; expects 201 and captures `id`, `version`, `documents[0].id/version` — VERIFIED.
- `Get client`, `Update client (PUT)` (expects 200, captures version), `Patch client (PATCH)` (expects 200) — VERIFIED against controller/DTOs.
- `List clients` GET `/api/clients?page=0&size=20` expects 200; invalid size 101 expects 400; missing client 999999 expects 404; duplicate tax identifier expects 409 — VERIFIED against pagination validation and service exceptions.
- Documents flow (create with `X-Client-Version`, get, PUT, PATCH, list) expects 201/200 and captures `documentId`/`documentVersion`; client version refresh requests are included — VERIFIED.
- Cleanup deletes document (204) with `X-Client-Version` and `X-Document-Version`, refreshes client version, then deletes client (204) with `X-Client-Version` — VERIFIED.
- Environment file defines `baseUrl`, `username`, `password`, `token`, `clientId`, `clientVersion`, `documentId`, `documentVersion` — VERIFIED.
- Both Postman files parse as valid JSON — VERIFIED.

### 8.8 GitHub Actions workflow syntax, GHCR permissions and image tags

- Workflow triggers on push to `main` and `workflow_dispatch`; includes `if: github.event_name != 'pull_request'` — VERIFIED.
- Permissions: `contents: read`, `packages: write` — VERIFIED.
- Image: `ghcr.io/${{ github.repository }}` — VERIFIED.
- Tags: `latest` on default branch and `sha-<full commit sha>` — VERIFIED (matches DEMO.md).
- Workflow builds `./Dockerfile` with `push: true` — VERIFIED.

### 8.9 JSON and script syntax

- Both Postman JSON files parse successfully — VERIFIED.
- PowerShell script parses successfully — VERIFIED.
- Bash script passes `bash -n` — VERIFIED.
- Compose files render via `docker compose config` — VERIFIED.

### 8.10 DEMO.md commands match actual implementation

- `docker compose -f compose.demo-image.yaml up -d` matches Compose file — VERIFIED.
- `.\scripts\demo-start.ps1` and `bash scripts/demo-start.sh` match scripts — VERIFIED.
- Readiness/Swagger/OpenAPI paths (`/v3/api-docs`, `/swagger-ui/index.html`) match `SecurityConfiguration` public matchers and controllers — VERIFIED.
- Stop/restart/reset commands (`down`, `up -d`, `down -v`) match Compose volume configuration — VERIFIED.
- Image name and tag in DEMO.md match workflow and Compose image — VERIFIED.

## 9. Findings and severity

### Critical

None.

### Major

None.

### Minor

1. `scripts/demo-start.ps1` — `Set-EnvValue` builds `$lines` via `Get-Content`. If `.env` contains a single line, `Get-Content` returns a string and `foreach ($line in $lines)` iterates characters, corrupting the file. Typical multi-line `.env` (from `.env.example`) is unaffected. Exact reference: `scripts/demo-start.ps1`, lines 40–61.
2. `scripts/demo-start.ps1` — `Set-Content -LiteralPath $envFile -Value $updated` uses the default encoding for the host PowerShell session; not explicitly UTF-8. BCrypt/Base64 values are ASCII, so no functional impact. Exact reference: `scripts/demo-start.ps1`, line 60.
3. `scripts/demo-start.sh` — `set_env` replaces every matching key line (duplicates retained) instead of only the first; PowerShell removes duplicates. Inconsistent duplicate-key handling between scripts. Exact reference: `scripts/demo-start.sh`, lines 38–57.
4. `scripts/demo-start.ps1` / `scripts/demo-start.sh` — helper scripts do not validate a user-supplied JWT secret format (Base64, >= 32 decoded bytes) or BCrypt hash format; invalid values are only detected at application startup. Exact references: `scripts/demo-start.ps1`, lines 132–137 and 150–163; `scripts/demo-start.sh`, lines 124–129 and 142–154.
5. `postman/HumanIT-Demo.postman_collection.json` — collection is order-dependent and uses unquoted template variables such as `"version": {{clientVersion}}`; running requests out of order or with empty variables yields invalid JSON/requests. This is expected for a demo collection but should be documented as sequential-only. Exact reference: `postman/HumanIT-Demo.postman_collection.json`, requests "Update client (PUT)", "Patch client (PATCH)", "Create document", "Update document (PUT)", "Patch document (PATCH)", "Delete document", "Delete client".
6. `.github/workflows/docker-publish.yml` — the workflow builds/pushes the image without running tests first, and `Dockerfile` uses `-DskipTests`. This does not affect demo syntax but means CI image publication is not gated by tests. Exact references: `.github/workflows/docker-publish.yml`, lines 41–49; `Dockerfile`, line 9.
7. `DEMO.md` — states all six variables are mandatory, while helper scripts auto-fill `POSTGRES_DB`/`POSTGRES_USER` defaults when placeholders are present. Minor documentation/behavior mismatch. Exact reference: `DEMO.md`, section 3; `scripts/demo-start.ps1`, lines 113–124; `scripts/demo-start.sh`, lines 105–115.

### Informational

- Bash script executable bit cannot be set on Windows; DEMO.md correctly instructs `bash scripts/demo-start.sh` — VERIFIED.
- Compose demo file relies on automatic `.env` loading in the current working directory; running with `-f` from another directory may not load `.env`. DEMO.md assumes repository root. Exact reference: `compose.demo-image.yaml`.

## 10. Decisions and reasoning

- Independent verification was completed before reading `agent-reports/T7.1/developer-report.md`, as required.
- Read-only/static checks were used; no Maven, Docker containers or Postman requests were executed. `docker compose config` was executed as a non-runtime configuration render.
- Postman payloads and status codes were cross-checked against controllers and DTOs rather than executed.
- After reading the Developer report, no material discrepancy was found in its verification claims: Compose rendering, script parsing, JSON parsing and image-only startup claims were reproduced and confirmed. The Developer report did not mention the minor robustness items listed in Section 9; these are additional findings, not contradictions.
- The Developer report's "Whitespace check: none found" was not independently reproduced (NOT AVAILABLE for this execution).

## 11. Final status

Completed. The demo package is structurally valid and internally consistent. No Critical or Major findings. Runtime end-to-end behavior of the published image remains PENDING VERIFICATION by task constraints.

## 12. Outstanding issues

- Runtime startup and end-to-end execution of the published image and Postman collection — PENDING VERIFICATION (not permitted by task constraints).
- GHCR package visibility must be set to Public by a maintainer — PENDING VERIFICATION.
- GitHub Actions workflow has not been executed yet — PENDING VERIFICATION.
- Autonomous application termination and standalone start evidence — PENDING VERIFICATION (carried from prior tasks; see `reports/evidence-gap-register.md`).

## 13. Handoff to next agent

Reviewer: audit scope compliance (no application code changes), security (no secrets printed/committed), and the minor robustness findings above. Tech Lead: decide whether the minor findings require correction before acceptance. No approval is recorded by this Tester report.
