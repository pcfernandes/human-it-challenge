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
    line="$(grep -E "^[[:space:]]*${key}=" "$ENV_FILE" | head -n 1 || true)"
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
            {
                if ($0 ~ "^[[:space:]]*" ENVIRON["key"] "=") {
                    if (!found) {
                        print ENVIRON["key"] "=" ENVIRON["value"]
                        found = 1
                    }
                    # Drop any duplicate keys so only the first occurrence remains.
                    next
                }
                print
            }
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

find_maven_jar() {
    local artifact_path="$1"
    local pattern="$2"
    local base="$HOME/.m2/repository/$artifact_path"
    if [[ ! -d "$base" ]]; then
        return
    fi
    find "$base" -type f -name "$pattern" ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | sort -r | head -n 1
}

test_bcrypt_hash() {
    local value="$1"
    local re='^\$2[aby]\$(0[4-9]|[12][0-9]|3[01])\$[./A-Za-z0-9]{53}$'
    [[ "$value" =~ $re ]]
}

new_bcrypt_hash() {
    local password="$1"
    if ! command_exists jshell; then
        fail "JDK (jshell) is required to generate a BCrypt hash automatically. Install JDK 21 or provide APP_SECURITY_USER_PASSWORD_HASH manually."
        exit 1
    fi
    local crypto_jar commons_jar classpath
    crypto_jar="$(find_maven_jar "org/springframework/security/spring-security-crypto" "spring-security-crypto-*.jar")"
    if [[ -z "$crypto_jar" ]]; then
        fail "spring-security-crypto was not found in the local Maven repository. Run 'mvnw dependency:resolve' or install the project dependencies first."
        exit 1
    fi
    commons_jar="$(find_maven_jar "commons-logging/commons-logging" "commons-logging-*.jar")"
    classpath="$crypto_jar"
    if [[ -n "$commons_jar" ]]; then
        classpath="$crypto_jar:$commons_jar"
    fi

    local tmp_code tmp_out
    tmp_code="$(mktemp)"
    tmp_out="$(mktemp)"
    printf '%s\n' \
        'import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;' \
        'String password = System.getenv("HUMANIT_PASSWORD");' \
        'System.out.println(new BCryptPasswordEncoder().encode(password));' \
        '/exit' > "$tmp_code"

    local had_password=0
    if [[ ${HUMANIT_PASSWORD+set} == set ]]; then
        had_password=1
    fi
    local previous_password="${HUMANIT_PASSWORD-}"
    export HUMANIT_PASSWORD="$password"

    HUMANIT_PASSWORD="$password" jshell --class-path "$classpath" "$tmp_code" >"$tmp_out" 2>/dev/null
    local status=$?

    if [[ $had_password -eq 1 ]]; then
        export HUMANIT_PASSWORD="$previous_password"
    else
        unset HUMANIT_PASSWORD
    fi

    rm -f "$tmp_code"
    local hash
    hash="$(tail -n 1 "$tmp_out" | tr -d '\r')"
    hash="${hash#"${hash%%[![:space:]]*}"}"
    hash="${hash%"${hash##*[![:space:]]}"}"
    rm -f "$tmp_out"

    if [[ $status -ne 0 ]] || ! test_bcrypt_hash "$hash"; then
        fail "Failed to generate a valid BCrypt password hash."
        exit 1
    fi
    printf '%s' "$hash"
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
    log "Enter the application login password. It is read securely and hashed automatically with BCrypt."
    read -r -s -p "Enter the application login password: " entered_password
    printf '\n'
    if [[ -z "$entered_password" ]]; then
        fail "Application password is required."
        exit 1
    fi
    generated_hash="$(new_bcrypt_hash "$entered_password")"
    entered_password=""
    set_env APP_SECURITY_USER_PASSWORD_HASH "'$generated_hash'"
    log "Stored the BCrypt password hash in .env (value not shown)."
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