#!/usr/bin/env bash
set -euo pipefail

IMAGE="ghcr.io/pcfernandes/human-it-challenge:latest"
COMPOSE_FILE="compose.demo-image.yaml"
READINESS_URL="http://localhost:8080/v3/api-docs"
READINESS_TIMEOUT=120
POLL_INTERVAL=3

log() { printf '%s\n' "$*"; }
fail() { printf 'ERROR: %s\n' "$*" >&2; }

command_exists() { command -v "$1" >/dev/null 2>&1; }

get_env() {
    local key="$1"
    if [[ ! -f "$ENV_FILE" ]]; then
        printf ''
        return
    fi
    local line
    line="$(grep -E "^[[:space:]]*${key}=" "$ENV_FILE" | tail -n 1 || true)"
    if [[ -z "$line" ]]; then
        printf ''
        return
    fi
    local value="${line#*=}"
    value="${value#"${value%%[![:space:]]*}"}"
    value="${value%"${value##*[![:space:]]}"}"
    if [[ "$value" == \'*\' && ${#value} -ge 2 ]]; then
        value="${value:1:${#value}-2}"
    elif [[ "$value" == \"*\" && ${#value} -ge 2 ]]; then
        value="${value:1:${#value}-2}"
    fi
    printf '%s' "$value"
}

set_env() {
    local key="$1"
    local value="$2"
    if [[ ! -f "$ENV_FILE" ]]; then
        printf '%s=%s\n' "$key" "$value" > "$ENV_FILE"
        return
    fi
    if grep -qE "^[[:space:]]*${key}=" "$ENV_FILE"; then
        key="$key" value="$value" awk '
            BEGIN { found = 0 }
            $0 ~ "^" ENVIRON["key"] "=" {
                if (!found) {
                    print ENVIRON["key"] "=" ENVIRON["value"]
                    found = 1
                }
                next
            }
            { print }
        ' "$ENV_FILE" > "$ENV_FILE.tmp"
        mv "$ENV_FILE.tmp" "$ENV_FILE"
    else
        printf '%s=%s\n' "$key" "$value" >> "$ENV_FILE"
    fi
}

is_placeholder() {
    local value="$1"
    [[ -z "$value" || "$value" == *replace-with* ]]
}

random_base64() {
    local bytes="$1"
    if command_exists openssl; then
        openssl rand -base64 "$bytes" | tr -d '\n'
    else
        head -c "$bytes" /dev/urandom | base64 | tr -d '\n'
    fi
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"
ENV_FILE="$REPO_ROOT/.env"
ENV_EXAMPLE="$REPO_ROOT/.env.example"

log "Repository root: $REPO_ROOT"

if ! command_exists docker; then
    fail "Docker is not installed or not on PATH. Install Docker Desktop/Engine with the Compose plugin, then retry."
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    fail "Cannot connect to the Docker daemon. Start Docker Desktop/Engine and retry."
    exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
    fail "Docker Compose is not available. Install the Compose plugin (docker compose) and retry."
    exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
    if [[ ! -f "$ENV_EXAMPLE" ]]; then
        fail ".env.example is missing; cannot create .env."
        exit 1
    fi
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    log "Created .env from .env.example. Please review the generated values."
fi

postgres_db="$(get_env POSTGRES_DB)"
if is_placeholder "$postgres_db"; then
    set_env POSTGRES_DB "humanit"
    log "Set POSTGRES_DB to the default value 'humanit'."
fi

postgres_user="$(get_env POSTGRES_USER)"
if is_placeholder "$postgres_user"; then
    set_env POSTGRES_USER "humanit"
    log "Set POSTGRES_USER to the default value 'humanit'."
fi

postgres_password="$(get_env POSTGRES_PASSWORD)"
if is_placeholder "$postgres_password"; then
    generated="$(random_base64 24)"
    set_env POSTGRES_PASSWORD "$generated"
    log "Generated a PostgreSQL password and stored it in .env (value not shown)."
fi

jwt_secret="$(get_env APP_SECURITY_JWT_SECRET)"
if is_placeholder "$jwt_secret"; then
    generated="$(random_base64 32)"
    set_env APP_SECURITY_JWT_SECRET "$generated"
    log "Generated a JWT secret and stored it in .env (value not shown)."
fi

app_username="$(get_env APP_SECURITY_USER_USERNAME)"
if is_placeholder "$app_username"; then
    read -r -p "Enter the application login username: " entered
    if [[ -z "$entered" ]]; then
        fail "Application username is required."
        exit 1
    fi
    set_env APP_SECURITY_USER_USERNAME "$entered"
    log "Stored the application username in .env."
fi

app_password_hash="$(get_env APP_SECURITY_USER_PASSWORD_HASH)"
if is_placeholder "$app_password_hash"; then
    log "A BCrypt password hash is required."
    log "Example: python3 -c \"import bcrypt; print(bcrypt.hashpw(b'your-password', bcrypt.gensalt()).decode())\""
    read -r -s -p "Enter the BCrypt password hash: " entered_hash
    printf '\n'
    if [[ -z "$entered_hash" ]]; then
        fail "Password hash is required."
        exit 1
    fi
    set_env APP_SECURITY_USER_PASSWORD_HASH "'$entered_hash'"
    log "Stored the password hash in .env (value not shown)."
fi

log "Pulling $IMAGE ..."
docker pull "$IMAGE"

log "Starting the demo stack..."
docker compose -f "$COMPOSE_FILE" up -d

log "Waiting for application readiness (timeout: ${READINESS_TIMEOUT} seconds)..."
ready=0
deadline=$(( $(date +%s) + READINESS_TIMEOUT ))
while [[ $(date +%s) -lt $deadline ]]; do
    if command_exists curl; then
        if curl -fsS "$READINESS_URL" >/dev/null 2>&1; then
            ready=1
            break
        fi
    elif command_exists wget; then
        if wget -qO- "$READINESS_URL" >/dev/null 2>&1; then
            ready=1
            break
        fi
    else
        fail "Neither curl nor wget is available to check readiness."
        exit 1
    fi
    sleep "$POLL_INTERVAL"
done

if [[ "$ready" -ne 1 ]]; then
    fail "The application did not become ready within ${READINESS_TIMEOUT} seconds."
    log "Troubleshooting:"
    log "  docker compose -f $COMPOSE_FILE ps"
    log "  docker compose -f $COMPOSE_FILE logs --tail=100 app"
    exit 1
fi

log ""
log "Human IT demo is ready."
log "Application: http://localhost:8080"
log "Swagger UI:  http://localhost:8080/swagger-ui/index.html"
log "OpenAPI JSON: http://localhost:8080/v3/api-docs"
log ""
log "Stop with:    docker compose -f $COMPOSE_FILE down"
log "Reset data:   docker compose -f $COMPOSE_FILE down -v"