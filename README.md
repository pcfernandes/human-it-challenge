# Human IT Challenge — REST API for Managing Clients and Documents

## Purpose and Current Scope

This repository is a Java 21 / Spring Boot REST API for managing **Clients** and their associated **Documents**. It prioritizes maintainability, correctness, transactional integrity, security, automated testing and clear documentation.

**Current state (Phase 1 baseline only):** the application boots, is correctly configured for `dev` and `prod` profiles, has a persistence and Flyway baseline, and ships cross-cutting scaffolding (RFC 9457 Problem Details, minimal OpenAPI bean, standard logging). There are **no business endpoints, entities, controllers or repositories** yet — those arrive in Phase 2. There is no authentication enforcement, no Docker configuration and no React frontend.

## Technology Baseline

| Component | Version / value |
|---|---|
| Java | 21 (LTS) — enforced at build time by Maven Enforcer `requireJavaVersion [21,22)` |
| Spring Boot | 3.5.16 (parent POM) |
| Maven | 3.10.0 or later — enforced by `requireMavenVersion [3.10.0,)` |
| Maven Wrapper | 3.3.4, pinned to Apache Maven 3.10.0 |
| Database (dev) | H2 in-memory |
| Database (prod) | PostgreSQL (from environment variables) |
| Schema management | Flyway (enabled), Hibernate `ddl-auto: validate` |

## Prerequisites

- **JDK 21** (LTS). Verify with `java -version`.
- **Maven is not required globally** — use the committed Maven Wrapper (`mvnw.cmd` on Windows).
- **Windows PowerShell** is the verified shell for the commands in this document. Other shells require adaptation.
- Network access on first run so the wrapper can download Maven 3.10.0.

## Configuration and Environment Variables

### Development (required)

| Variable | Required | Purpose |
|---|---|---|
| `APP_SECURITY_JWT_SECRET` | Yes | JWT signing secret. Standard Base64, at least 32 decoded bytes. Never commit. |
| `APP_SECURITY_DEMO_USER_USERNAME` | Yes | Development demo user username. The dev demo-user properties are validated; missing value fails startup. |
| `APP_SECURITY_DEMO_USER_PASSWORD` | Yes | Development demo user password. Required for the same reason. |

### Production (required)

| Variable | Required | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Yes | Must be `prod`. |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL. |
| `SPRING_DATASOURCE_USERNAME` | Yes | PostgreSQL username. |
| `SPRING_DATASOURCE_PASSWORD` | Yes | PostgreSQL password. |
| `APP_SECURITY_JWT_SECRET` | Yes | JWT signing secret (Base64, at least 32 decoded bytes). |

Demo-user settings are intentionally absent in `prod`; no demonstration credentials are provisioned in production.

## Generating a JWT Secret (PowerShell)

Generates 32 random bytes (256 bits) and Base64-encodes them. The value is generated locally and is never committed.

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Optional, non-leaking verification (prints `32` or more):

```powershell
[Convert]::FromBase64String($env:APP_SECURITY_JWT_SECRET).Length
```

For production, generate an independent secret on the target host or via a secrets manager using the same method.

## Local Development Startup (PowerShell, from project root)

The development profile is selected **explicitly** so that a `SPRING_PROFILES_ACTIVE=prod` value inherited from the environment cannot accidentally activate production configuration.

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:APP_SECURITY_DEMO_USER_USERNAME = "demo"
$env:APP_SECURITY_DEMO_USER_PASSWORD = "<demo-password>"
.\mvnw.cmd spring-boot:run
```

- If any required variable is missing or invalid, startup fails fast before the web server starts; the failure names the property but never leaks the secret value.
- The application starts on the default Spring Boot port (8080) unless overridden by standard Spring Boot configuration.
- No manual database setup is required (H2 in-memory).

## Production Configuration Example (PowerShell)

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://<host>:5432/<database>"
$env:SPRING_DATASOURCE_USERNAME = "<username>"
$env:SPRING_DATASOURCE_PASSWORD = "<password>"
$env:APP_SECURITY_JWT_SECRET = "<base64-secret-with-at-least-32-decoded-bytes>"
.\mvnw.cmd spring-boot:run
```

H2 Console is not configured in `prod`. PostgreSQL integration against a real PostgreSQL instance is **not verified in Phase 1**; the production datasource configuration is validated only through configuration inspection and the H2-overridden test harness. Configuration presence must not be read as verified integration.

## H2 and PostgreSQL Profile Differences

| Aspect | `dev` | `prod` |
|---|---|---|
| Datasource | H2 in-memory (`jdbc:h2:mem:humanit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`), user `sa`, empty password | PostgreSQL from `SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD` |
| Driver | `org.h2.Driver` | `org.postgresql.Driver` |
| H2 Console | Enabled, path `/h2-console` | Not configured |
| Demo user | Configured under `dev & !prod` (requires username/password env vars) | Absent |
| Logging | `com.humanit.challenge: DEBUG` | root `WARN`, `com.humanit.challenge: INFO` |

Common to both: `spring.jpa.hibernate.ddl-auto: validate`, Flyway enabled (`classpath:db/migration`), `spring.sql.init.mode: never`, pagination defaults (`default-page: 0`, `default-page-size: 20`, `max-page-size: 100`), JWT expiration default 60 minutes.

## Database and Flyway Baseline

- **Development:** H2 in-memory database. No manual database setup is required.
- **Production:** PostgreSQL, configured entirely from `SPRING_DATASOURCE_*`.
- **Flyway:** enabled in both profiles; migrations are loaded from `classpath:db/migration`.
- **Baseline migration:** `V1__baseline.sql` is comment-only and is recorded as schema version 1. It intentionally creates no tables; business schema arrives in Phase 2.
- **Hibernate:** `ddl-auto: validate` in common configuration. Hibernate validates the schema but never creates or alters tables; Flyway owns schema changes.
- **SQL initialization:** `spring.sql.init.mode: never` — Spring Boot does not run `schema.sql`/`data.sql`.
- **H2 Console:** configured only in `dev`, path `/h2-console`. It is also active under combined `dev,prod` activation because the `dev` profile is active.

**Reachability caveat:** Spring Security is on the classpath and no `SecurityFilterChain` is configured yet, so H2 Console and Swagger UI access may require authentication and may be affected by default frame/CSRF settings. Do not treat them as publicly accessible in Phase 1. Security configuration is addressed in Phase 3.

## RFC 9457 Problem Details Infrastructure

A `@RestControllerAdvice` (`GlobalExceptionHandler`) provides RFC 9457 Problem Details (`application/problem+json`) responses for framework and unexpected errors: validation failures (400), malformed body (400), type mismatch (400), not found (404), method not allowed (405), constraint violations (400), `ResponseStatusException` (status preserved) and a catch-all 500. No stack traces, exception class names, SQL details, secrets or rejected values are copied to a response body; unexpected exceptions are logged server-side only.

Deferred (not yet implemented): 409 conflict handlers, 401/403 security mappings, problem-type URIs, bearer OpenAPI security scheme, structured logging.

## OpenAPI Configuration and Limitations

`OpenApiConfig` registers a minimal `OpenAPI` bean with application title/version/description. The `springdoc-openapi-starter-webmvc-ui` dependency is present, so the OpenAPI JSON and Swagger UI endpoints are available from the dependency, but **there are no business endpoints yet**, and **reachability under default Spring Security is not verified** in Phase 1. Authentication is not yet represented in OpenAPI (deferred to Phase 3/4).

## Logging Baseline

A common console logging pattern is configured in `application.yml`:

```
%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n
```

Log levels: `com.humanit.challenge: DEBUG` in dev; root `WARN` and `com.humanit.challenge: INFO` in prod.

## Build and Test Commands (PowerShell, from project root)

```powershell
# Full build and test suite (recommended baseline check)
.\mvnw.cmd clean verify

# Tests only
.\mvnw.cmd test

# Package without running tests (use only when explicitly intended)
.\mvnw.cmd -DskipTests package
```

- The Maven Wrapper downloads Maven 3.10.0 automatically on first use.
- `clean verify` runs compilation, tests, packaging and the Maven Enforcer baseline checks.
- No global Maven installation is required.
- Test counts drift between runs; record the actual count from your own run. The last verified full run (T1.5) reported 41 tests.

## Known Limitations and Deferred Work

- No business endpoints, entities, repositories or controllers exist yet (Phase 2).
- Authentication is not enforced; JWT infrastructure is only configured/externalized (Phase 3).
- H2 Console and Swagger UI reachability under default Spring Security is not verified (Phase 3).
- PostgreSQL integration against a real PostgreSQL instance is not verified (Phase 5).
- No Dockerfile or Compose configuration exists yet (Phase 5).
- No React frontend exists.
- Deferred from T1.5: 409 conflict handlers, 401/403 mappings, problem-type URIs, bearer OpenAPI security scheme, structured logging, global/object-level constraint violation mapping.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
