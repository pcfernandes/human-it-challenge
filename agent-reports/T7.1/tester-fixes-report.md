# Tester Report — T7.1 Final Fix Verification

## 1. Task ID

T7.1 — Human IT Docker Image Demo Package (final fix verification).

## 2. Execution identifier and date

- Execution: T7.1-TEST-002
- Date: 2026-11-14
- Prior primary report `agent-reports/T7.1/tester-report.md` (T7.1-TEST-001) archived unchanged to `agent-reports/T7.1/history/tester-001.md`.

## 3. Agent role and model

- Role: Tester.
- Model: opencode-go/beam-free.
- Independent verification was completed before reading `agent-reports/T7.1/developer-fixes-report.md` and `agent-reports/T7.1/developer-report.md` conclusions, per reporting rules.

## 4. Authorized scope

Static verification of the T7.1 minimal final corrections only (REV-01, REV-02, REV-03, REV-05, REV-06, REV-07) and confirmation that REV-04 is recorded as an accepted limitation. No Java application code, tests, Dockerfile, Compose files, Postman files or `.env.example` were modified. No Maven builds, Docker containers or Postman requests were run; only non-runtime checks (PowerShell parse, Bash syntax, Compose config render, JSON parse, file encoding/whitespace) were executed. No commit, push or merge was performed.

## 5. Files inspected

- **VERIFIED**: `scripts/demo-start.ps1`
- **VERIFIED**: `scripts/demo-start.sh`
- **VERIFIED**: `DEMO.md`
- **VERIFIED**: `.github/workflows/docker-publish.yml`
- **VERIFIED**: `compose.demo-image.yaml`
- **VERIFIED**: `compose.yaml`
- **VERIFIED**: `Dockerfile`
- **VERIFIED**: `.env.example`
- **VERIFIED**: `postman/HumanIT-Demo.postman_collection.json`
- **VERIFIED**: `postman/HumanIT-Local.postman_environment.json`
- **VERIFIED**: `src/main/java/com/humanit/challenge/config/JwtSecretValidator.java`
- **VERIFIED**: `agent-reports/T7.1/reviewer-report.md` (read after independent inspection)
- **VERIFIED**: `agent-reports/T7.1/tester-report.md` (read after independent inspection)
- **VERIFIED**: `agent-reports/T7.1/developer-fixes-report.md` (read after independent verification)
- **VERIFIED**: `agent-reports/T7.1/developer-report.md` (read after independent verification)
- **VERIFIED**: `agent-reports/README.md`

## 6. Files changed

- **CREATED**: `agent-reports/T7.1/history/tester-001.md` — archived previous Tester report unchanged.
- **CREATED**: `agent-reports/T7.1/tester-fixes-report.md` — this report.
- **NOT MODIFIED**: all implementation, Docker, Compose, Postman, Java and configuration files; `scripts/*`, `DEMO.md` and `.github/workflows/docker-publish.yml` were inspected only.

## 7. Commands executed

- **VERIFIED**: `[System.Management.Automation.Language.Parser]::ParseFile` on `scripts/demo-start.ps1` — parse OK (no errors).
- **VERIFIED**: `bash -n scripts/demo-start.sh` — exit 0 (syntax OK).
- **VERIFIED**: `docker compose --env-file .env.example -f compose.demo-image.yaml config` — renders successfully; app image `ghcr.io/pcfernandes/human-it-challenge:latest`; postgres has no published ports; named volume `postgres_data` present; `SPRING_PROFILES_ACTIVE=prod`.
- **VERIFIED**: JSON parse of both Postman files via `ConvertFrom-Json` — both valid.
- **VERIFIED**: Functional test of the PowerShell `Set-EnvValue` transform on a single-line `.env` (`FOO=bar`) — replaced with `FOO=baz` without character corruption; first byte `70` (`F`), not `239` (no UTF-8 BOM).
- **VERIFIED**: Functional test of duplicate-key removal in PowerShell (`KEY=old1 / KEY=old2 / OTHER=x`) — result `KEY=new / OTHER=x` (first occurrence retained, duplicate dropped).
- **VERIFIED**: Functional test of duplicate-key removal in Bash using the `set_env` awk logic — result `KEY=new / OTHER=x`.
- **VERIFIED**: UTF-8 BOM check on `scripts/demo-start.ps1`, `scripts/demo-start.sh`, `DEMO.md`, `.github/workflows/docker-publish.yml`, `.env.example` — all no-BOM.
- **VERIFIED**: Trailing-whitespace and line-ending check on the four modified files — no trailing whitespace; LF line endings.
- **VERIFIED**: `git status --short` / `git ls-files -s mvnw` — no tracked files modified; `mvnw` tracked as mode `100644`.
- **VERIFIED**: `git diff --check` — no whitespace errors reported for tracked files.
- **NOT RUN**: Maven, Docker containers, Postman (per task constraints).

## 8. Evidence and observed results

### 8.1 REV-01 — PowerShell single-line `.env` handling

- **VERIFIED** (`scripts/demo-start.ps1`, `Set-EnvValue`): input is normalized to an array via `$lines = @(Get-Content -LiteralPath $envFile)`, so a one-line file yields a one-element array instead of a character-enumerated string. `Get-EnvValue` also uses `@(Get-Content ...)` for consistency.
- **VERIFIED** (functional test): single-line file `FOO=bar` was transformed to `FOO=baz` without corruption.

### 8.2 REV-02 — UTF-8 without BOM

- **VERIFIED** (`scripts/demo-start.ps1`, `Set-EnvValue`): `Set-Content` replaced by `[System.IO.File]::WriteAllLines($envFile, @($updated), (New-Object System.Text.UTF8Encoding($false)))`.
- **VERIFIED** (functional test): first byte of the written file was `70` (`F`), not `239` (UTF-8 BOM).

### 8.3 REV-03 — Bash duplicate-key handling

- **VERIFIED** (`scripts/demo-start.sh`, `set_env`): awk uses a `found` flag; only the first matching key is rewritten and subsequent matching lines are dropped via `next`, matching PowerShell behavior.
- **VERIFIED** (functional test): `KEY=old1 / KEY=old2 / OTHER=x` became `KEY=new / OTHER=x`.

### 8.4 REV-05 — Sequential Postman execution

- **VERIFIED** (`DEMO.md`, section 7, step 7): explicitly states requests must run "sequentially in the order listed in the collection"; explains dependency on captured variables (`token`, `clientId`, `clientVersion`, `documentId`, `documentVersion`) and unquoted template variables such as `"version": {{clientVersion}}`; recommends the Postman Collection Runner; Cleanup runs last.

### 8.5 REV-06 — Maven test gate in GitHub Actions

- **VERIFIED** (`.github/workflows/docker-publish.yml`): `actions/setup-java@v4` with Temurin, Java `21`, Maven cache is present; `./mvnw -B test` step runs before `docker/setup-buildx-action`, login, metadata and build/push. A test failure fails the job, so publication only occurs if tests pass. Uses the existing Maven wrapper and no new dependencies.
- **VERIFIED** (YAML structure): `on` triggers (push to `main`, `workflow_dispatch`), `permissions` (`contents: read`, `packages: write`), `jobs.build-and-push`, and ordered steps are well-formed; image `ghcr.io/${{ github.repository }}`; tags `latest` on default branch and `sha-<full commit SHA>`; `push: true`.

### 8.6 REV-07 — Auto-generated/defaulted variables

- **VERIFIED** (`DEMO.md`, section 3): clarifies that `POSTGRES_DB` defaults to `humanit`, `POSTGRES_USER` defaults to `humanit`, `POSTGRES_PASSWORD` and `APP_SECURITY_JWT_SECRET` are generated, and username/password hash are prompted when placeholders are present. The variable table and manual generation commands remain documented.

### 8.7 REV-04 — Accepted limitation (no code change)

- **VERIFIED** (no change): helper scripts do not add JWT/BCrypt format validation. Application-side validation remains the source of truth: `JwtSecretValidator` requires non-blank standard Base64 decoding to at least 32 bytes; BCrypt hash format is enforced at authentication by Spring Security. Invalid values surface at application startup.
- **VERIFIED** (documentation): `agent-reports/T7.1/developer-fixes-report.md` records REV-04 as "Accepted (no code change)" in its findings table and outstanding issues; reviewer report `agent-reports/T7.1/reviewer-report.md` lists T7.1-REV-04 as the corresponding Minor finding. No helper-script pre-validation was added.

### 8.8 No unintended changes

- **VERIFIED** (`git status --short`): only untracked demo deliverables (`.github/`, `DEMO.md`, `agent-reports/T7.1/`, `compose.demo-image.yaml`, `postman/`, `scripts/`) are present; no tracked file (including Java, `Dockerfile`, `compose.yaml`, Postman baseline) is modified.
- **VERIFIED**: `Dockerfile` still performs maintainer-side build with `./mvnw -DskipTests package` and runtime uses `eclipse-temurin:21-jre-alpine`; `compose.demo-image.yaml` renders with no `build` section and postgres unpublished.
- **VERIFIED**: Postman files parse as valid JSON; collection/environment contents unchanged by the fixes.

## 9. Findings and severity

| ID | Severity | Status | Notes |
|---|---|---|---|
| REV-01 | Minor | Resolved / VERIFIED | PowerShell single-line `.env` handling fixed. |
| REV-02 | Minor | Resolved / VERIFIED | Explicit UTF-8 without BOM. |
| REV-03 | Minor | Resolved / VERIFIED | Bash duplicate-key handling harmonized. |
| REV-04 | Minor | Accepted (no code change) / VERIFIED | Application already validates JWT secret and BCrypt hash; helper scripts rely on startup-time detection. |
| REV-05 | Minor | Resolved / VERIFIED | Sequential Postman execution documented. |
| REV-06 | Minor | Resolved / VERIFIED | Maven test gate added before image publication. |
| REV-07 | Minor | Resolved / VERIFIED | Auto-generated/defaulted variables clarified. |

No Critical or Major findings introduced by the fixes.

Additional observation (non-blocking):
- **OBS-01 (Minor, CI portability)**: `.github/workflows/docker-publish.yml` invokes `./mvnw -B test`, but `mvnw` is tracked in git with mode `100644` (non-executable). On a Linux runner the wrapper may require `chmod +x mvnw` (as the `Dockerfile` already does) or invocation via `bash ./mvnw`. This is a pre-existing condition not introduced by the fixes and does not affect the demo package or runtime behavior; it is recorded as a recommendation only, not a blocker for final acceptance.

## 10. Decisions and reasoning

- Independent verification reproduced and confirmed each developer claim: PowerShell parse, Bash syntax, Compose render, JSON parse, functional `.env` transform, BOM absence, duplicate-key removal and workflow structure.
- REV-04 is confirmed as an accepted Minor limitation: no helper-script validation was added and the application's existing validation is the source of truth, matching the developer report and reviewer finding T7.1-REV-04.
- The `mvnw` executable-mode observation (OBS-01) is reported for completeness but classified non-blocking because it is pre-existing, outside the authorized fix scope, and does not affect demo startup/runtime; remediation would require a workflow change outside this task's authorized scope.
- No Java, Dockerfile, Compose or Postman changes were made or required.

## 11. Final status

PASS. All requested corrections (REV-01, REV-02, REV-03, REV-05, REV-06, REV-07) are independently verified as correctly applied. REV-04 is confirmed documented as an accepted Minor limitation requiring no code change. No blocking findings remain.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Runtime end-to-end startup of the published image and full Postman collection (not permitted by task constraints).
- **PENDING VERIFICATION**: GitHub Actions workflow execution with the new test gate (not yet run).
- **PENDING VERIFICATION**: GHCR package visibility set to Public (maintainer action).
- **ACCEPTED LIMITATION**: REV-04 — helper scripts do not pre-validate JWT secret/BCrypt hash format; invalid values surface at application startup.
- **OBS-01 (non-blocking)**: CI wrapper executability recommendation (see Section 9).

## 13. Handoff to next agent

Tech Lead: disposition REV-04 (accepted Minor limitation) and the pending maintainer/runtime actions; decide whether OBS-01 warrants a separate workflow hardening task. No approval is recorded or implied by this report.

Report path: `agent-reports/T7.1/tester-fixes-report.md`.
Final status: PASS.
Outstanding findings: none blocking; REV-04 accepted Minor limitation; runtime/CI/GHCR verification pending; OBS-01 non-blocking CI portability note.
