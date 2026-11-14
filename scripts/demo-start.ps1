<#
.SYNOPSIS
  Start the Human IT challenge demo using the published Docker image.

.DESCRIPTION
  Verifies Docker, prepares .env, pulls ghcr.io/pcfernandes/human-it-challenge:latest,
  starts PostgreSQL and the application with compose.demo-image.yaml, and waits for readiness.
#>

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$Image = "ghcr.io/pcfernandes/human-it-challenge:latest"
$ComposeFile = "compose.demo-image.yaml"
$ReadinessUrl = "http://localhost:8080/v3/api-docs"
$ReadinessTimeoutSeconds = 120
$PollIntervalSeconds = 3

function Write-Info([string]$Message) { Write-Host $Message }
function Write-Fail([string]$Message) { Write-Error $Message }

function Test-Command([string]$Name) {
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

function Get-EnvValue([string]$Key) {
    if (-not (Test-Path -LiteralPath $envFile)) { return "" }
    $pattern = "^\\s*$([regex]::Escape($Key))=(.*)$"
    $line = @(Get-Content -LiteralPath $envFile) | Where-Object { $_ -match $pattern } | Select-Object -Last 1
    if ($null -eq $line) { return "" }
    $value = ($line -replace $pattern, '$1').Trim()
    if ($value.StartsWith("'") -and $value.EndsWith("'") -and $value.Length -ge 2) {
        $value = $value.Substring(1, $value.Length - 2)
    } elseif ($value.StartsWith('"') -and $value.EndsWith('"') -and $value.Length -ge 2) {
        $value = $value.Substring(1, $value.Length - 2)
    }
    return $value
}

function Set-EnvValue([string]$Key, [string]$Value) {
    $lines = @()
    if (Test-Path -LiteralPath $envFile) {
        $lines = @(Get-Content -LiteralPath $envFile)
    }
    $pattern = "^\\s*$([regex]::Escape($Key))="
    $found = $false
    $updated = foreach ($line in $lines) {
        if ($line -match $pattern) {
            if (-not $found) {
                "$Key=$Value"
                $found = $true
            }
            # Drop any duplicate keys so only the first occurrence remains.
        } else {
            $line
        }
    }
    if (-not $found) {
        $updated += "$Key=$Value"
    }
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($envFile, @($updated), $utf8NoBom)
}

function Test-Placeholder([string]$Value) {
    if ([string]::IsNullOrWhiteSpace($Value)) { return $true }
    return $Value -match "replace-with"
}

function New-RandomBase64([int]$Bytes) {
    $buffer = New-Object byte[] $Bytes
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($buffer)
    return [Convert]::ToBase64String($buffer)
}

# Locate repository root from script location.
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
Set-Location -LiteralPath $repoRoot

$envFile = Join-Path $repoRoot ".env"
$envExample = Join-Path $repoRoot ".env.example"

Write-Info "Repository root: $repoRoot"

# Verify Docker and Compose.
if (-not (Test-Command "docker")) {
    Write-Fail "Docker is not installed or not on PATH. Install Docker Desktop/Engine with the Compose plugin, then retry."
    exit 1
}

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Fail "Cannot connect to the Docker daemon. Start Docker Desktop/Engine and retry."
    exit 1
}

docker compose version *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Fail "Docker Compose is not available. Install the Compose plugin (docker compose) and retry."
    exit 1
}

# Create .env when missing.
if (-not (Test-Path -LiteralPath $envFile)) {
    if (-not (Test-Path -LiteralPath $envExample)) {
        Write-Fail ".env.example is missing; cannot create .env."
        exit 1
    }
    Copy-Item -LiteralPath $envExample -Destination $envFile
    Write-Info "Created .env from .env.example. Please review the generated values."
}

# Preserve valid values and fill missing/placeholder values.
$postgresDb = Get-EnvValue "POSTGRES_DB"
if (Test-Placeholder $postgresDb) {
    Set-EnvValue "POSTGRES_DB" "humanit"
    Write-Info "Set POSTGRES_DB to the default value 'humanit'."
}

$postgresUser = Get-EnvValue "POSTGRES_USER"
if (Test-Placeholder $postgresUser) {
    Set-EnvValue "POSTGRES_USER" "humanit"
    Write-Info "Set POSTGRES_USER to the default value 'humanit'."
}

$postgresPassword = Get-EnvValue "POSTGRES_PASSWORD"
if (Test-Placeholder $postgresPassword) {
    $generated = New-RandomBase64 24
    Set-EnvValue "POSTGRES_PASSWORD" $generated
    Write-Info "Generated a PostgreSQL password and stored it in .env (value not shown)."
}

$jwtSecret = Get-EnvValue "APP_SECURITY_JWT_SECRET"
if (Test-Placeholder $jwtSecret) {
    $generated = New-RandomBase64 32
    Set-EnvValue "APP_SECURITY_JWT_SECRET" $generated
    Write-Info "Generated a JWT secret and stored it in .env (value not shown)."
}

$appUsername = Get-EnvValue "APP_SECURITY_USER_USERNAME"
if (Test-Placeholder $appUsername) {
    $entered = Read-Host "Enter the application login username"
    if ([string]::IsNullOrWhiteSpace($entered)) {
        Write-Fail "Application username is required."
        exit 1
    }
    Set-EnvValue "APP_SECURITY_USER_USERNAME" $entered
    Write-Info "Stored the application username in .env."
}

$appPasswordHash = Get-EnvValue "APP_SECURITY_USER_PASSWORD_HASH"
if (Test-Placeholder $appPasswordHash) {
    Write-Info "A BCrypt password hash is required. Generate one with Python (bcrypt) or another BCrypt tool."
    Write-Info "Example: python -c ""import bcrypt; print(bcrypt.hashpw(b'your-password', bcrypt.gensalt()).decode())"""
    $secureHash = Read-Host "Enter the BCrypt password hash" -AsSecureString
    $plainHash = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureHash))
    if ([string]::IsNullOrWhiteSpace($plainHash)) {
        Write-Fail "Password hash is required."
        exit 1
    }
    Set-EnvValue "APP_SECURITY_USER_PASSWORD_HASH" "'$plainHash'"
    Write-Info "Stored the password hash in .env (value not shown)."
}

# Pull the published application image.
Write-Info "Pulling $Image ..."
docker pull $Image
if ($LASTEXITCODE -ne 0) {
    Write-Fail "Failed to pull $Image."
    exit 1
}

# Start PostgreSQL and the application.
Write-Info "Starting the demo stack..."
docker compose -f $ComposeFile up -d
if ($LASTEXITCODE -ne 0) {
    Write-Fail "Failed to start the demo stack."
    Write-Info "Troubleshooting:"
    Write-Info "  docker compose -f $ComposeFile ps"
    Write-Info "  docker compose -f $ComposeFile logs --tail=100"
    exit 1
}

# Wait for readiness with a bounded timeout.
Write-Info "Waiting for application readiness (timeout: $ReadinessTimeoutSeconds seconds)..."
$deadline = (Get-Date).AddSeconds($ReadinessTimeoutSeconds)
$ready = $false
while ((Get-Date) -lt $deadline) {
    try {
        $response = Invoke-WebRequest -Uri $ReadinessUrl -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            $ready = $true
            break
        }
    } catch {
        # Application is not ready yet.
    }
    Start-Sleep -Seconds $PollIntervalSeconds
}

if (-not $ready) {
    Write-Fail "The application did not become ready within $ReadinessTimeoutSeconds seconds."
    Write-Info "Troubleshooting:"
    Write-Info "  docker compose -f $ComposeFile ps"
    Write-Info "  docker compose -f $ComposeFile logs --tail=100 app"
    exit 1
}

Write-Info ""
Write-Info "Human IT demo is ready."
Write-Info "Application: http://localhost:8080"
Write-Info "Swagger UI:  http://localhost:8080/swagger-ui/index.html"
Write-Info "OpenAPI JSON: http://localhost:8080/v3/api-docs"
Write-Info ""
Write-Info "Stop with:    docker compose -f $ComposeFile down"
Write-Info "Reset data:   docker compose -f $ComposeFile down -v"