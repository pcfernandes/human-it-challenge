# Changelog

## [Unreleased]

## [Phase 1] - 2026-11-09

### Added

- Project bootstrap (Maven project, Maven Wrapper, Java 21 baseline, Spring Boot application entry point, minimal `application.yml`) — T1.1.
- Technical baseline dependencies (Spring Web, Data JPA, Validation, Security, OAuth2 resource server, Flyway, H2, PostgreSQL, springdoc-openapi, test dependencies) — T1.2.
- Environment configuration with `dev` and `prod` profiles; externalized JWT secret and dev-only demo user — T1.3.
- Persistence baseline: H2 development datasource, PostgreSQL production datasource from environment variables, Flyway enabled, `ddl-auto: validate`, dev-only H2 Console, comment-only `V1__baseline.sql` — T1.4.
- Cross-cutting scaffolding (T1.5): RFC 9457 Problem Details via `@RestControllerAdvice` (`GlobalExceptionHandler`), global validation-error mapping, minimal OpenAPI bean (`OpenApiConfig`), standard logging baseline.
- Phase 1 sanity and documentation checkpoint: root `README.md` and `CHANGELOG.md` — T1.6.

### Changed

- Not applicable (initial baseline).

### Known limitations

- PostgreSQL integration is not verified in Phase 1 (deferred to Phase 5).
- H2 Console and Swagger UI reachability under default Spring Security is not verified.
- Business endpoints, authentication enforcement, Docker and React frontend are not implemented (later phases).
- Root documentation created in T1.6.
