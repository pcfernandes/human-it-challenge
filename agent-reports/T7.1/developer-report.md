# Developer Report

## 1. Task ID

T7.1 - Human IT Docker Image Demo Package

## 2. Execution identifier and date

Execution: T7.1-DEV-001
Date: 2026-11-13

## 3. Agent role and model

Developer role. Model: opencode-go/beam-free.

## 4. Authorized scope

Documentation and delivery packaging only. Created a Docker-image-based demo package:
DEMO.md, compose.demo-image.yaml, scripts/demo-start.ps1, scripts/demo-start.sh,
postman/HumanIT-Demo.postman_collection.json, postman/HumanIT-Local.postman_environment.json,
and .github/workflows/docker-publish.yml.
No application code, tests, security behavior, or existing configuration were modified.
No Maven build or Docker container runtime verification was performed; only static validation
(compose config, script syntax, JSON parsing) was performed.

## 5. Files inspected

- README.md (VERIFIED)
- Dockerfile (VERIFIED)
- compose.yaml (VERIFIED)
- .env.example (VERIFIED)
- src/main/resources/application.yml (VERIFIED)
- src/main/resources/application-dev.yml (VERIFIED)
- src/main/resources/application-prod.yml (VERIFIED)
- src/main/java/com/humanit/challenge/controller/AuthController.java (VERIFIED)
- src/main/java/com/humanit/challenge/controller/ClientController.java (VERIFIED)
- src/main/java/com/humanit/challenge/controller/DocumentController.java (VERIFIED)
- src/main/java/com/humanit/challenge/dto/** (VERIFIED)
- Existing .github directory contents (VERIFIED: no workflows existed)
- agent-reports/README.md (VERIFIED)

## 6. Files changed

Created:
- DEMO.md
- compose.demo-image.yaml
- scripts/demo-start.ps1
- scripts/demo-start.sh
- postman/HumanIT-Demo.postman_collection.json
- postman/HumanIT-Local.postman_environment.json
- .github/workflows/docker-publish.yml
- agent-reports/T7.1/developer-report.md

No existing files were modified.

## 7. Commands executed

- PowerShell parser validation: `System.Management.Automation.Language.Parser::ParseFile` on `scripts/demo-start.ps1` (VERIFIED: parse OK).
- Bash syntax validation: `bash -n scripts/demo-start.sh` (VERIFIED: syntax OK).
- JSON validation: `ConvertFrom-Json` on both Postman files (VERIFIED: both OK).
- Compose validation: `docker compose --env-file .env.example -f compose.demo-image.yaml config` (VERIFIED: config renders successfully; postgres has no published ports).
- Whitespace check: PowerShell scan for trailing spaces in created files (VERIFIED: none found).
- Git status and `git diff --check` (VERIFIED: exit 0; new files untracked).

Docker daemon was started locally to perform Compose validation, but no containers were run.

## 8. Evidence and observed results

- Compose renders the published image `ghcr.io/pcfernandes/human-it-challenge:latest` with no build section (VERIFIED).
- PostgreSQL service has no `ports` mapping, so it is not publicly exposed (VERIFIED).
- Named volume `postgres_data` is preserved (VERIFIED).
- `SPRING_PROFILES_ACTIVE` is set to `prod` (VERIFIED).
- Postman JSON files parse successfully (VERIFIED).
- Scripts parse successfully (VERIFIED).
- Environment variable names match `.env.example` and application configuration (VERIFIED).

## 9. Findings and severity

No Critical or Major findings.
Minor/Info:
- Docker daemon was not running initially; it was started to validate Compose configuration. This is an environment condition, not a deliverable defect.
- Bash script executable bit cannot be set on Windows; evaluators can run it with `bash scripts/demo-start.sh`.
- The Postman collection assumes the demo data created by earlier requests exists and is order-dependent; this is expected for a demo collection.

## 10. Decisions and reasoning

- Reused the existing Compose structure and environment variable conventions from `compose.yaml` and `.env.example`.
- Used the production Spring profile as required.
- Documented one recommended startup path (published image via Compose), while helper scripts automate the same path.
- Did not modify README or existing Compose configuration to avoid introducing additional startup alternatives or changing accepted behavior.

## 11. Final status

Completed. All requested deliverables created and statically validated.

## 12. Outstanding issues

- Runtime end-to-end verification of the published image was not performed (PENDING VERIFICATION), as required by task constraints.
- GHCR package visibility must be set to public by a maintainer (PENDING VERIFICATION).
- The GitHub Actions workflow has not been run yet (PENDING VERIFICATION).

## 13. Handoff to next agent

Tester/Reviewer: independently validate JSON, scripts, Compose configuration, and documentation. Do not read this report before independent verification. Reviewer should audit scope compliance and security (no secrets printed, no local builds).