# Human IT Challenge - Docker Image Demo

This guide starts the Human IT challenge API from the pre-built Docker image published to GitHub Container Registry. You do not need Java, Maven, Gradle, or local source compilation.

## 1. Prerequisites

- Docker Engine/Desktop with the Docker Compose plugin.
- Git (optional, for cloning) or a ZIP download of the repository.
- A terminal: PowerShell on Windows, or a POSIX shell on macOS/Linux.
- Network access to pull `ghcr.io/pcfernandes/human-it-challenge:latest` and `postgres:17-alpine`.

Verify Docker:

```powershell
docker --version
docker compose version
docker info
```

## 2. Get the repository

Clone:

```bash
git clone https://github.com/pcfernandes/human-it-challenge.git
cd human-it-challenge
```

Or download the repository ZIP and extract it, then open a terminal in the extracted folder.

The commands below assume the repository root as the current directory.

## 3. Configure environment variables

Copy the example file:

```powershell
Copy-Item .env.example .env
```

```bash
cp .env.example .env
```

All six variables are mandatory in `.env` for Compose. However, the helper scripts may auto-fill some of them when they still contain placeholder values from `.env.example`:

- `POSTGRES_DB` defaults to `humanit`.
- `POSTGRES_USER` defaults to `humanit`.
- `POSTGRES_PASSWORD` is generated as 24 random bytes, Base64-encoded.
- `APP_SECURITY_JWT_SECRET` is generated as 32 random bytes, Base64-encoded.
- `APP_SECURITY_USER_USERNAME` is prompted for.
- `APP_SECURITY_USER_PASSWORD_HASH` is prompted for (BCrypt hash).

All six variables are mandatory:

| Variable | Purpose |
|---|---|
| `POSTGRES_DB` | PostgreSQL database name. |
| `POSTGRES_USER` | PostgreSQL username. |
| `POSTGRES_PASSWORD` | PostgreSQL password. |
| `APP_SECURITY_JWT_SECRET` | Base64 secret that decodes to at least 32 bytes. Used to sign JWTs. |
| `APP_SECURITY_USER_USERNAME` | Application login username. |
| `APP_SECURITY_USER_PASSWORD_HASH` | BCrypt hash of the application login password. This is not the plaintext password. |

### Generate secure values

Generate a database password:

```powershell
$bytes = New-Object byte[] 24
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

```bash
openssl rand -base64 24
```

Generate a JWT secret (32 random bytes, Base64-encoded):

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

```bash
openssl rand -base64 32
```

Choose `APP_SECURITY_USER_USERNAME` yourself. Generate a BCrypt hash for your chosen password. Examples:

```bash
python3 -c "import bcrypt; print(bcrypt.hashpw(b'your-password', bcrypt.gensalt()).decode())"
```

If the BCrypt hash contains `$`, single-quote it in `.env` so Compose treats it literally:

```text
APP_SECURITY_USER_PASSWORD_HASH='$2a$10$...'
```

The helper scripts can create `.env`, generate the database password and JWT secret, prompt for the missing username, and prompt for the application password securely and hash it automatically with BCrypt.

## 4. Start the demo

Recommended command:

```bash
docker compose -f compose.demo-image.yaml up -d
```

This starts PostgreSQL and the application from the published image. No source build is triggered. PostgreSQL is not published to the host.

Alternatively, use the helper script:

```powershell
.\scripts\demo-start.ps1
```

```bash
bash scripts/demo-start.sh
```

The script checks Docker, prepares `.env`, pulls the image, starts the stack, and waits for readiness.

## 5. Verify readiness

Wait for the API to respond:

```bash
curl -s http://localhost:8080/v3/api-docs | head -c 200
```

Or open `http://localhost:8080/v3/api-docs` in a browser. A JSON OpenAPI document indicates readiness.

## 6. Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Login and Swagger/OpenAPI paths are public. Client and Document operations require a bearer token.

## 7. Postman

1. Open Postman.
2. Import `postman/HumanIT-Demo.postman_collection.json`.
3. Import `postman/HumanIT-Local.postman_environment.json`.
4. Select the `HumanIT Local` environment.
5. Replace `username` and `password` with your configured credentials.
6. Run `Authentication > Login`. The returned JWT is stored automatically in `token`.
7. Run the remaining requests **sequentially in the order listed in the collection**. The collection is order-dependent: later requests rely on variables (`token`, `clientId`, `clientVersion`, `documentId`, `documentVersion`) captured by earlier requests, and use unquoted template variables such as `"version": {{clientVersion}}`. Running requests out of order or with empty variables produces invalid requests. Prefer the Postman Collection Runner over manual single-request execution.
8. Run `Cleanup` last to remove created data.

## 8. Stop, restart, and reset

Stop and remove containers and network:

```bash
docker compose -f compose.demo-image.yaml down
```

Restart:

```bash
docker compose -f compose.demo-image.yaml up -d
```

Reset all data, including the database volume:

```bash
docker compose -f compose.demo-image.yaml down -v
```

PostgreSQL data is stored in the named `postgres_data` volume and survives `down`. Changing `POSTGRES_USER` or `POSTGRES_PASSWORD` after the volume exists does not update the initialized database and can cause authentication failures; use `down -v` for a clean reset.

## 9. Troubleshooting

- Docker daemon not running: start Docker Desktop/Engine, then retry.
- Port 8080 already in use: stop the conflicting process or container, then retry.
- Missing or placeholder `.env` values: the helper script reports the missing variable; edit `.env` and rerun.
- Database authentication failures after changing credentials: run `docker compose -f compose.demo-image.yaml down -v` and start again. This deletes database data.
- Image pull failures: ensure the package is public or run `docker login ghcr.io`.
- Inspect status and logs:

```bash
docker compose -f compose.demo-image.yaml ps
docker compose -f compose.demo-image.yaml logs --tail=100 app
```

## 10. Publishing the Docker image (maintainers)

The workflow `.github/workflows/docker-publish.yml` builds the existing `Dockerfile` and publishes to GHCR:

- Triggers on pushes to `main` and manual `workflow_dispatch`.
- Publishes `latest` from `main`.
- Publishes an immutable `sha-<commit>` tag.
- Does not run on pull requests.

### Make the GHCR package public

1. Open the repository on GitHub.
2. Go to `Packages` and select `human-it-challenge`.
3. Open `Package settings`.
4. Under `Danger Zone`, choose `Change visibility`.
5. Select `Public` and confirm.

After the package is public, evaluators can pull it without authentication.