# Human IT Challenge — Clients and Documents REST API

## Purpose and current capabilities

This Java 21 / Spring Boot application provides a REST API for managing Clients and their associated Documents. It includes authenticated Client CRUD, nested Document CRUD, input validation, Flyway-managed persistence, pagination, transactional updates, optimistic locking, and RFC 9457 Problem Details responses.

## Technology and prerequisites

- Java 21 JDK (verify with `java -version`; set `JAVA_HOME` to that JDK for the wrapper).
- Maven Wrapper 3.3.4, configured to download Maven 3.10.0. A separate Maven installation is not required. The first wrapper run needs network access.
- Spring Boot 3.5.16, H2 for local development, and PostgreSQL configured for production.
- PowerShell examples below assume the repository root as the current directory.

## Local development with H2

The default profile is `dev`, which uses an in-memory H2 database. Set these required environment variables before starting:

| Variable | Value |
|---|---|
| `APP_SECURITY_JWT_SECRET` | Standard Base64 secret decoding to at least 32 bytes. Generate a fresh value; do not commit it. |
| `APP_SECURITY_DEMO_USER_USERNAME` | A username you choose. |
| `APP_SECURITY_DEMO_USER_PASSWORD_HASH` | A BCrypt hash for the password you choose, provisioned securely. This is not the plaintext password. |

### Generate a secure Base64 JWT secret

This PowerShell example generates 32 random bytes (256 bits) and stores only the Base64 value in the current process environment:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

### Start the application

Replace both quoted placeholders with your own username and securely generated BCrypt hash. Do not use these placeholders as credentials or commit real credentials. The matching plaintext password is required only for the login request below.

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:APP_SECURITY_DEMO_USER_USERNAME = "<your-username>"
$env:APP_SECURITY_DEMO_USER_PASSWORD_HASH = "<BCrypt-hash-for-your-password>"
.\mvnw.cmd spring-boot:run
```

The application listens on port 8080 by default. No database setup is needed for the in-memory H2 profile.

## Database and migrations

Flyway runs migrations from `classpath:db/migration`; Hibernate uses `ddl-auto: validate` and does not create or alter the schema. `V1__baseline.sql` is a comment-only baseline migration. `V2__create_clients_and_documents.sql` creates the Client and Document tables, uniqueness constraints, Client foreign key with cascade deletion, and Client index. Spring SQL initialization is disabled.

The `prod` profile is configured for PostgreSQL using `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`, together with `APP_SECURITY_JWT_SECRET`, `APP_SECURITY_USER_USERNAME`, and `APP_SECURITY_USER_PASSWORD_HASH`. Supply production values through a secure environment/secrets mechanism. PostgreSQL configuration exists, but integration against a real PostgreSQL instance has not yet been verified.

## Authentication and API usage

`POST /auth/login` is public. Supply the configured username and its corresponding plaintext password; the application verifies it against the configured BCrypt hash and returns an access token. The following PowerShell example uses placeholders only:

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/login' `
  -ContentType 'application/json' `
  -Body (@{ username = '<your-username>'; password = '<your-password>' } | ConvertTo-Json)
```

Use the returned token as a bearer token for protected endpoints. This performs an authenticated, paginated Client list request:

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/clients?page=0&size=20' `
  -Headers @{ Authorization = "Bearer $($login.accessToken)" }
```

The placeholder password must match the password used to produce the configured BCrypt hash. Do not put real credentials or tokens in source control.

### Endpoint overview

- Login: `POST /auth/login`.
- Client CRUD: `POST`, `GET` (collection and `/{id}`), `PUT`, `PATCH`, and `DELETE` under `/api/clients`.
- Nested Document CRUD: `POST`, `GET` (collection and `/{documentId}`), `PUT`, `PATCH`, and `DELETE` under `/api/clients/{clientId}/documents`.
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`.
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`.

Login and Swagger/OpenAPI paths are public; Client and Document operations require a valid JWT bearer token.

## API behavior and technical decisions

- Client and Document list endpoints use zero-based `page` and `size` parameters (defaults `0` and `20`), sort by ID ascending, return pagination metadata, and reject page sizes above 100.
- `PUT` updates all editable scalar fields. For a Client, omitted or empty `documents` preserves existing Documents; explicit `documents: null` is invalid. `PATCH` changes only supplied values; omitted values remain unchanged. Neither update implicitly deletes Documents, and Document ownership cannot be transferred.
- Client and Document entities use version-based optimistic locking. Client PUT/PATCH requests supply the expected Client version in the body. Document create/update/delete operations require the expected `X-Client-Version`; Document PUT/PATCH also supply the Document version in the body, while Document DELETE requires `X-Document-Version` as well. Client DELETE requires `X-Client-Version`. Stale versions return HTTP 409; invalid or missing required versions return HTTP 400. Document changes increment the parent Client version.
- Related Client/Document changes and deletion are transactional so a failed operation does not leave a partial aggregate update. Deleting a Client deletes its owned Documents; deleting a Document removes only that Document.
- Authentication is stateless JWT bearer authentication using HS256. Password verification uses BCrypt, and signing material and account settings are externalized.
- Client/Document application errors use RFC 9457 Problem Details (`application/problem+json`), including validation, not-found, conflict, authentication, and unexpected-error responses. Responses do not expose secrets, stack traces, or internal SQL details.

## Build and tests

Run from the repository root in PowerShell:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd test
```

`clean verify` builds and runs verification, including tests and configured Maven Enforcer checks. `test` runs the test phase. Both use the checked-in wrapper and do not require a global Maven installation.

## Limitations

- A Dockerfile and Docker Compose configuration have not yet been implemented; no container startup commands are available.
- PostgreSQL configuration exists, but integration against a real PostgreSQL instance is not yet verified.
- The application has no registration endpoint, roles/permissions, or refresh-token flow; it supports one configured account at a time.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
