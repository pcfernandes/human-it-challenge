# Human IT Challenge - Docker Image Demo

Quick start for running the API from the pre-built image. No Java or Maven build is required.

## Prerequisites

- Docker Engine/Desktop with the Compose plugin.
- Git (optional) or a ZIP download of the repository.
- Network access to pull `ghcr.io/pcfernandes/human-it-challenge:latest` and `postgres:17-alpine`.

## Start (recommended)

Windows PowerShell:

```powershell
.\scripts\demo-start.ps1
```

Bash:

```bash
bash scripts/demo-start.sh
```

The script verifies Docker, creates `.env` from `.env.example` when needed, fills placeholder values, prompts for credentials, pulls the image, starts PostgreSQL and the app, and waits for readiness.

## Credentials

On first run, enter the application login username and password when prompted. The password is read without echo and stored as a BCrypt hash. Existing valid `.env` values are preserved.

## Use the API

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Login: `POST /auth/login` with your username and plaintext password; use the returned access token as a `Bearer` token for protected endpoints.

## Stop

```bash
docker compose -f compose.demo-image.yaml down
```

## Reset data (optional)

```bash
docker compose -f compose.demo-image.yaml down -v
```

This removes the containers and the PostgreSQL volume.

## More details

See [README.md](README.md).
