# Developer Report — T7.1 Minimal Final Corrections

## 1. Task ID

T7.1 — Human IT Docker Image Demo Package (minimal final corrections).

## 2. Execution identifier and date

- Execution: T7.1-DEV-002
- Date: 2026-11-14
- Prior primary report `agent-reports/T7.1/developer-report.md` (T7.1-DEV-001) archived unchanged to `agent-reports/T7.1/history/developer-001.md`.

## 3. Agent role and model

- Role: Developer.
- Model: opencode-go/beam-free.

## 4. Authorized scope

Apply only the corrections requested for T7.1:
- REV-01: Fix PowerShell `Set-EnvValue` single-line `.env` handling.
- REV-02: Write `.env` explicitly as UTF-8 without BOM.
- REV-03: Make Bash duplicate-key handling consistent with PowerShell.
- REV-05: Clearly document sequential Postman execution in `DEMO.md`.
- REV-06: Add a Maven test gate before publishing the Docker image in GitHub Actions (Java 21, existing Maven wrapper; publish only if tests pass).
- REV-07: Clarify which environment variables are automatically generated or defaulted by startup scripts.
- REV-04: Do not implement additional JWT/BCrypt format validation; record as accepted Minor limitation requiring no code change.

Constraints honored: no Java application code, tests, Dockerfile or Compose changes; no features or dependencies added; no Maven or Docker containers run locally; no commit/push/merge.

## 5. Files inspected

- **VERIFIED**: `agent-reports/T7.1/reviewer-report.md` (findings REV-01..REV-07).
- **VERIFIED**: `agent-reports/T7.1/tester-report.md`.
- **VERIFIED**: `DEMO.md`.
- **VERIFIED**: `scripts/demo-start.ps1`.
- **VERIFIED**: `scripts/demo-start.sh`.
- **VERIFIED**: `.github/workflows/docker-publish.yml`.
- **VERIFIED**: `agent-reports/README.md` (reporting conventions).

## 6. Files changed

- **MODIFIED**: `scripts/demo-start.ps1` — `Set-EnvValue` now normalizes `Get-Content` output to an array via `@(...)` (single-line safe); writes `.env` with explicit UTF-8 without BOM via `[System.IO.File]::WriteAllLines` and `System.Text.UTF8Encoding($false)`. `Get-EnvValue` also uses `@(...)` for consistency.
- **MODIFIED**: `scripts/demo-start.sh` — `set_env` now keeps only the first matching key and drops subsequent duplicates, matching PowerShell.
- **MODIFIED**: `DEMO.md` — added helper-script default/auto-generation clarification (REV-07) and sequential Postman execution note (REV-05).
- **MODIFIED**: `.github/workflows/docker-publish.yml` — added `actions/setup-java@v4` (Temurin, Java 21, Maven cache) and `./mvnw -B test` gate before Docker Buildx/login/build/push (REV-06).
- **CREATED**: `agent-reports/T7.1/history/developer-001.md` — archived previous Developer report (unchanged).
- **CREATED**: `agent-reports/T7.1/developer-fixes-report.md` — this report.
- **NOT MODIFIED**: Java application code, tests, `Dockerfile`, Compose files, Postman files, `.env.example`.

## 7. Commands executed

- **VERIFIED**: `powershell ... Parser::ParseFile` on `scripts/demo-start.ps1` — parse OK.
- **VERIFIED**: `bash -n scripts/demo-start.sh` (Git Bash) — exit 0.
- **VERIFIED**: Functional test of `Set-EnvValue` logic on a single-line `.env` — replaced value correctly; no BOM (first byte `70`, not `239`).
- **VERIFIED**: Functional test of duplicate-key removal in PowerShell and Bash — duplicates dropped, first occurrence retained.
- **NOT AVAILABLE**: YAML parser (no Python/Node available) — workflow inspected visually; indentation and structure are consistent.
- **VERIFIED**: `git diff --check` — no whitespace errors reported for tracked files (changes are untracked; reviewed manually).
- **NOT RUN**: Maven, Docker, Compose (per task constraints).

## 8. Evidence and observed results

### 8.1 REV-01 — PowerShell single-line `.env`
- **VERIFIED**: `Set-EnvValue` now uses `$lines = @(Get-Content -LiteralPath $envFile)`, so a one-line file yields a one-element array instead of a character-enumerated string.
- **VERIFIED**: Functional test replaced `FOO=bar` (single line) with `FOO=baz` without corruption.

### 8.2 REV-02 — UTF-8 without BOM
- **VERIFIED**: `Set-Content` (default/BOM-prone) replaced by `[System.IO.File]::WriteAllLines($envFile, @($updated), (New-Object System.Text.UTF8Encoding($false)))`.
- **VERIFIED**: Functional test first byte was `70` (`F`), not `239` (UTF-8 BOM).

### 8.3 REV-03 — Bash duplicate-key handling
- **VERIFIED**: `set_env` awk now uses a `found` flag; only the first matching key is rewritten and subsequent matching lines are dropped, matching PowerShell.
- **VERIFIED**: Functional test `KEY=old1 / KEY=old2 / OTHER=x` became `KEY=new / OTHER=x`.

### 8.4 REV-05 — Sequential Postman execution
- **VERIFIED**: `DEMO.md` section 7 step 7 now states requests must run sequentially in listed order, explains variable dependencies and unquoted template variables, and recommends the Collection Runner.

### 8.5 REV-06 — Maven test gate in GitHub Actions
- **VERIFIED**: Workflow now sets up Java 21 (Temurin) with Maven cache and runs `./mvnw -B test` before Docker Buildx/login/build/push. A test failure fails the job, so publication only occurs if tests pass.
- **VERIFIED**: Uses the existing Maven wrapper (`./mvnw`), no new dependencies.

### 8.6 REV-07 — Auto-generated/defaulted variables
- **VERIFIED**: `DEMO.md` section 3 now clarifies that `POSTGRES_DB`/`POSTGRES_USER` default to `humanit`, `POSTGRES_PASSWORD` and `APP_SECURITY_JWT_SECRET` are generated, and username/password hash are prompted, when placeholders are present.

### 8.7 REV-04 — Accepted limitation (no code change)
- **VERIFIED**: No JWT/BCrypt format validation was added to helper scripts or application. Application already validates (`JwtSecretValidator` requires standard Base64 decoding to at least 32 bytes; BCrypt hash format enforced at authentication). Helper scripts retain startup-time detection.

## 9. Findings and severity

| ID | Severity | Status | Notes |
|---|---|---|---|
| REV-01 | Minor | Resolved | PowerShell single-line `.env` handling fixed. |
| REV-02 | Minor | Resolved | Explicit UTF-8 without BOM. |
| REV-03 | Minor | Resolved | Bash duplicate-key handling harmonized. |
| REV-04 | Minor | Accepted (no code change) | Application already validates JWT secret and BCrypt hash; helper scripts rely on startup-time detection. |
| REV-05 | Minor | Resolved | Sequential Postman execution documented. |
| REV-06 | Minor | Resolved | Maven test gate added before image publication. |
| REV-07 | Minor | Resolved | Auto-generated/defaulted variables clarified. |

No Critical or Major findings. No new findings introduced.

## 10. Decisions and reasoning

- Minimal targeted changes only; no Java, tests, Dockerfile or Compose modifications.
- PowerShell write uses .NET `UTF8Encoding($false)` because Windows PowerShell 5.1's `Set-Content -Encoding UTF8` emits a BOM; this guarantees no BOM across PowerShell versions.
- Bash duplicate handling changed to drop duplicates (first occurrence retained) to match PowerShell; no change to append-when-missing behavior.
- Test gate uses `./mvnw -B test` (not `verify`/Docker) to avoid integration/Docker dependencies in CI while still gating publication on the unit test suite; Java 21 matches the Dockerfile toolchain.
- REV-04 intentionally left unimplemented per Tech Lead instruction; the application's existing validation is the source of truth.

## 11. Final status

Completed. All requested corrections applied (REV-01, REV-02, REV-03, REV-05, REV-06, REV-07). REV-04 recorded as accepted Minor limitation requiring no code change.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Runtime end-to-end startup of published image and Postman collection (not permitted by task constraints).
- **PENDING VERIFICATION**: GitHub Actions workflow execution with the new test gate (not yet run).
- **PENDING VERIFICATION**: GHCR package visibility (maintainer action).
- **ACCEPTED LIMITATION**: REV-04 — helper scripts do not pre-validate JWT secret/BCrypt hash format; invalid values surface at application startup (application already validates).

## 13. Handoff to next agent

Tester/Reviewer: re-run static validation (PowerShell parse, Bash `bash -n`, YAML inspection, Compose render) and confirm the corrections. Tech Lead: disposition accepted limitation REV-04 and pending maintainer/runtime actions. No approval is recorded or implied by this report.

Report path: `agent-reports/T7.1/developer-fixes-report.md`.
Final status: Completed.
Outstanding findings: none new; REV-04 accepted Minor limitation; runtime/CI/GHCR verification pending.