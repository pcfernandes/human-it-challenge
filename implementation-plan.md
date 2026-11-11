# Human IT Challenge — Consolidated Implementation Plan

**Status:** Approved by the Tech Lead  
**Scope:** Java 21 + Spring Boot REST API for managing Clients and Documents  
**References:** `requirements.md`, `evaluation-notes.md`, `requirements-decisions.md`  
**Constraint:** Documentation/planning only. No requirements files changed and no application code implemented by this document.

## 1. Purpose

This plan consolidates the previously approved six-phase implementation strategy and applies the mandatory Tech Lead adjustments without changing project requirements.

### Mandatory adjustments incorporated

1. **Version validation, optimistic locking and Document ownership rules are implemented in Phase 2**, together with Client and Document CRUD. They are no longer deferred to a later phase.
2. **Every phase includes its own automated tests and an independent Reviewer audit.** Testing and review are not concentrated only in Phase 6.
3. **Phase 1 is split into small, independently approved tasks**, starting with **project bootstrap only**. No foundation, domain, persistence, security or delivery work is bundled into the bootstrap task.

## 2. Governance and Approval Rules

The workflow remains:

1. Architect proposes the current task/phase scope.
2. Tech Lead approves it.
3. Developer implements only the approved scope.
4. Tester verifies the implementation.
5. Reviewer performs an independent audit.
6. Tech Lead approves completion before the next task or phase.

Additional rules:

- No task or phase may begin without Tech Lead approval.
- No phase may be marked complete without its own tests and an independent Reviewer audit.
- Reviewer findings of Critical or Major severity must be resolved before approval.
- Agents must not implement unapproved features or make unrelated changes.

## 3. Phase Overview

| Phase | Name | Primary Outcome |
|---|---|---|
| Phase 1 | Bootstrap and Incremental Foundation | Runnable, correctly configured, minimal application baseline |
| Phase 2 | Core Client and Document CRUD, Integrity, Concurrency and Ownership | Full CRUD with validation, transactions, optimistic locking, ownership rules and collection-preserving updates |
| Phase 3 | JWT Authentication and Authorization | Protected Client/Document APIs and working login |
| Phase 4 | OpenAPI Documentation and API Contract Hardening | Complete code-first API documentation and contract consistency |
| Phase 5 | Containerization and Environment Delivery | Docker, PostgreSQL delivery and operational documentation |
| Phase 6 | End-to-End Verification and Acceptance | Final acceptance validation and release-readiness audit |

## 4. Role Responsibilities

### Developer

- Implements only the approved task scope.
- Follows the approved package structure, DTO contracts, transaction boundaries and error-handling conventions.
- Adds migration scripts and configuration required by the task.
- Documents assumptions and deviations in the task summary.

### Tester

- Designs tests for happy paths, validation failures, not-found cases, uniqueness conflicts, ownership violations, concurrency conflicts, rollback behavior and pagination.
- Keeps tests independent and deterministic.
- Reports gaps in coverage and edge cases.
- Does not modify production code unless explicitly authorized.

### Reviewer

- Reviews code, architecture, REST semantics, transactions, security, validation and error handling.
- Reports findings as Critical, Major or Minor.
- Verifies requirements traceability and test coverage.
- Performs the phase-specific audit before Tech Lead approval.

### Tech Lead

- Approves task/phase scope, implementation, tests and review outcomes.
- Decides whether to proceed, rework or split scope further.

## 5. Phase 1 — Bootstrap and Incremental Foundation

### Objective

Establish the project baseline in small, independently approved tasks. **The first task is project bootstrap only.**

### Task list

#### T1.1 — Project bootstrap only

- **In scope:** Maven project, Maven Wrapper, Java 21 baseline, Spring Boot parent, main application class, minimal `application.yml`, base package declaration, initial test skeleton.
- **Out of scope:** Domain entities, persistence, security, business endpoints, Docker, full configuration profiles.
- **Developer:** Create only the minimal runnable Spring Boot application.
- **Tester:** Verify the application starts and a basic context-load test passes.
- **Reviewer:** Confirm the bootstrap is minimal, correct and free of unapproved dependencies.
- **Acceptance criteria:**
  - Project builds successfully with the Maven Wrapper.
  - Application starts locally.
  - Java 21 baseline is enforced.
  - No domain, persistence, security or Docker code is introduced.

#### T1.2 — Technical baseline and dependency verification

- **In scope:** Add and version-lock approved dependencies (Spring Web, JPA, Validation, Security, Flyway, springdoc-openapi, PostgreSQL driver, test dependencies).
- **Out of scope:** Dependency usage in business code.
- **Developer:** Add dependencies and verify compatibility.
- **Tester:** Verify build and test execution remain green.
- **Reviewer:** Check dependency necessity, version consistency and unused libraries.
- **Acceptance criteria:**
  - All required dependencies are present.
  - No unnecessary dependencies are introduced.
  - Build and tests pass.

#### T1.3 — Environment configuration and secret externalization

- **In scope:** Spring profiles `dev` and `prod`, externalized credentials, JWT secret configuration, configurable demo-user settings, maximum page size and pagination defaults.
- **Out of scope:** Database schema and domain code.
- **Developer:** Implement profile-based configuration without hardcoded production secrets.
- **Tester:** Verify profile startup and configuration binding.
- **Reviewer:** Check secret handling and environment separation.
- **Acceptance criteria:**
  - Development and production settings are separated.
  - Sensitive values are externalized.
  - Demo credentials are not provisioned automatically in production.

#### T1.4 — Persistence and migration baseline

- **In scope:** H2 and PostgreSQL configuration, Flyway enablement, Hibernate `ddl-auto: validate`, H2 Console policy for development only, migration directory structure.
- **Out of scope:** Business tables and entity mappings.
- **Developer:** Configure persistence baseline and create an empty/initial migration placeholder if needed.
- **Tester:** Verify datasource and Flyway startup in `dev` (H2); PostgreSQL integration verification requires an available PostgreSQL instance and is not assumed to happen automatically during Phase 1.
- **Reviewer:** Confirm database setup does not rely on Hibernate table generation.
- **Acceptance criteria:**
  - Application starts with H2 in `dev`.
  - Flyway runs successfully.
  - Hibernate schema validation is enabled.
  - PostgreSQL integration verification requires an available PostgreSQL instance.
  - Full Docker Compose verification remains in Phase 5.

#### T1.5 — Cross-cutting application scaffolding

- **In scope:** RFC 9457 `@RestControllerAdvice` skeleton, Problem Details response model, global validation-error mapping, springdoc/OpenAPI bean configuration, standard logging baseline.
- **Out of scope:** Endpoint-specific business validation and JWT enforcement.
- **Developer:** Add reusable cross-cutting infrastructure.
- **Tester:** Verify Problem Details infrastructure using isolated exception-handler tests or test-only controllers, without introducing production business endpoints before Phase 2.
- **Reviewer:** Confirm consistency, no stack-trace leakage and no sensitive details.
- **Acceptance criteria:**
  - Problem Details format is available for all future controllers.
  - OpenAPI is initialized.
  - Error responses are consistent.
  - Problem Details infrastructure is tested without production business endpoints before Phase 2; tests may use isolated exception-handler tests or test-only controllers.

#### T1.6 — Phase 1 sanity and documentation checkpoint

- **In scope:** Local run instructions for the baseline, build/test command documentation, changelog entry for Phase 1.
- **Out of scope:** End-to-end business validation.
- **Developer:** Document baseline execution.
- **Tester:** Re-run full baseline suite.
- **Reviewer:** Audit Phase 1 outputs before approval.
- **Acceptance criteria:**
  - Documented commands execute successfully.
  - Phase 1 has its own tests and independent review record.

### Phase 1 exit criteria

- Runnable Spring Boot baseline exists.
- Build, dependency, profile, persistence and cross-cutting scaffolding tasks are approved separately.
- Phase 1 tests pass.
- Phase 1 independent Reviewer audit is complete.

## 6. Phase 2 — Core Client and Document CRUD, Integrity, Concurrency and Ownership

### Objective

Implement the complete Client and Document domain behavior, including CRUD, validation, transactional integrity, pagination, version validation, optimistic locking and Document ownership rules. These controls are mandatory in this phase and are not deferred.

### Application-layer and persistence boundaries (D-11)

- Entities are persistence-focused JPA models containing mappings, fields, relationships, constructors, getters/setters and only minimal behavior required for persistence consistency. They must not perform DTO conversion, repository orchestration, HTTP processing or application-service logic. Existing JPA relationship-consistency helpers may remain inside entities.
- Dedicated mapper classes own DTO/entity transformations. T2.2 implements the mappers alongside the request/response DTO contracts and validation; entities do not provide DTO conversion methods.
- Repositories own database access. Client and Document CRUD workflows in T2.3 and T2.4 follow Resource -> Service -> Repository separation: REST resources/controllers handle HTTP concerns and delegate application workflows to services, and services use repositories for database access.
- Introduce `ClientService` and `DocumentService` only where actual use cases justify them. Do not add placeholder services or unnecessary pass-through services. Services own business rules, ownership enforcement, orchestration and transaction boundaries when those responsibilities are required by the use case.
- Preserve the accepted T2.1 persistence behavior, including its minimal JPA relationship-consistency helpers; this clarification does not reopen or require refactoring T2.1. Preserve all approved API endpoints and contracts, acceptance criteria, ownership rules, versioning/optimistic-locking behavior and transactional/rollback contracts.

### Task list

#### T2.1 — Domain model, mappings and database schema

- **In scope:** `Client` and `Document` JPA entities, required fields, `@Version` fields, one-to-many relationship, database constraints for required fields, uniqueness and foreign keys, cascade delete. Flyway migration for schema.
- **Out of scope:** REST controllers and security.
- **Developer:** Implement persistence-focused entities, repositories and migration. Keep entity behavior limited to persistence consistency; existing relationship-consistency helpers may remain. Do not add DTO conversion, repository orchestration, HTTP concerns or application-service logic to entities. Preserve the accepted T2.1 persistence behavior.
- **Tester:** Verify repository persistence and cascade deletion.
- **Reviewer:** Check aggregate design, constraint correctness and cascade behavior.
- **Acceptance criteria:**
  - `taxIdentifier` is globally unique.
  - `Document.number` is globally unique.
  - Deleting a Client deletes associated Documents.
  - No orphan Documents are permitted.
  - Both entities use JPA optimistic locking.

#### T2.2 — DTOs, validation and mappers

- **In scope:** Request and response DTOs, Bean Validation, mapper layer, validation messages.
- **Out of scope:** Security DTOs.
- **Developer:** Implement DTO contracts, validation and dedicated mapper classes for DTO/entity transformations. Keep mapping outside the entities.
- **Tester:** Verify field validation and null/blank rejection scenarios.
- **Reviewer:** Confirm DTO separation and validation completeness.
- **Acceptance criteria:**
  - **PATCH tri-state binding:** A project-owned Jackson-integrated representation distinguishes an omitted property (`ABSENT`), explicit JSON `null` (`PRESENT_NULL`) and a supplied non-null value (`PRESENT_VALUE`). Binding verification distinguishes all three cases; no external JSON-nullable dependency is introduced.
  - **Response DTOs and mapping boundary:** `ClientResponse` contains nested `DocumentResponse` entries. Each `DocumentResponse` contains scalar `clientId` and no nested `Client`; DTOs form no recursive graph. Mappers perform DTO/entity transformation without repository access. Any future use case producing `ClientResponse` intentionally loads the required lazy Documents association.
  - **Nested Client PUT:** Each supplied Document requires `number` and `expirationDate`. Existing Documents require both ID and expected version; new Documents provide neither ID nor version. Omitted or empty `documents` preserves existing Documents; explicit `documents: null` is invalid. Omission does not delete Documents, and updates cannot transfer ownership.
  - **Client POST collection exception:** `documents: null` is treated as an omitted collection for Client POST only. It does not apply to Client PUT or PATCH, where explicit `documents: null` is invalid.
  - **Validation:** Negative expected versions are rejected. Apply `@Size` maxima: Client `firstName` 100, `lastName` 100, `taxIdentifier` 50, `email` 255 and `phoneNumber` 50; Document `number` 100 and `description` 1000. Required-field and email-format validation remain enforced; `description` remains optional, and expired Documents are accepted.
  - **Nested Client PATCH:** Existing nested Documents require ID and expected version and support partial scalar updates (omitted values remain unchanged). New nested Documents have neither ID nor version and require creation fields `number` and `expirationDate`. Explicit null for required fields is invalid; explicit null for optional `description` clears it. Client PATCH `documents: null` is invalid; omitted or empty `documents` preserves existing Documents. PATCH does not implicitly delete Documents or transfer ownership.
  - **T2.2 architecture and scope:** T2.2 uses DTOs, Bean Validation, Jackson binding and dedicated mappers only. Mappers contain no repository access, ownership resolution, transaction orchestration, HTTP behavior or business workflows. No services or REST resources are introduced in T2.2. Preserve the accepted T2.1 behavior and persistence contracts; ownership resolution and intentional loading of lazy associations belong to future use cases.

#### T2.3 — Client CRUD endpoints

- **In scope:** `POST /clients`, `GET /clients`, `GET /clients/{id}`, `PUT /clients/{id}`, `PATCH /clients/{id}`, `DELETE /clients/{id}`.
- **Out of scope:** JWT enforcement (Phase 3).
- **Developer:** Implement the Client REST resource/controller and the use-case-justified service/repository workflow with Resource -> Service -> Repository separation. The resource handles HTTP concerns and delegates application workflows to services; services own applicable business rules, orchestration, ownership enforcement and transaction boundaries. Do not introduce a placeholder or pass-through `ClientService`; add it only when justified by actual Client use cases.
- **Tester:** Verify happy-path endpoint tests for each Client endpoint.
- **Reviewer:** Check REST semantics, status codes and layer separation.
- **Acceptance criteria:**
  - Client CRUD operations work end to end.
  - Duplicate `taxIdentifier` returns HTTP 409.
  - Missing resources return HTTP 404.
  - Validation failures return HTTP 400 with Problem Details.
  - Version validation, optimistic-locking and conflict behavior for these endpoints are verified in T2.6 and transactional header behavior in T2.7.

#### T2.4 — Document CRUD endpoints

- **In scope:** `POST /clients/{clientId}/documents`, `GET /clients/{clientId}/documents`, `GET /clients/{clientId}/documents/{documentId}`, `PUT /clients/{clientId}/documents/{documentId}`, `PATCH /clients/{clientId}/documents/{documentId}`, `DELETE /clients/{clientId}/documents/{documentId}`.
- **Out of scope:** Document transfer between Clients.
- **Developer:** Implement the nested Document REST resource/controller and the use-case-justified service/repository workflow with Resource -> Service -> Repository separation. The resource handles HTTP concerns and delegates application workflows to services; services own applicable business rules, ownership enforcement, orchestration and transaction boundaries. Do not introduce a placeholder or pass-through `DocumentService`; add it only when justified by actual Document use cases.
- **Tester:** Verify happy-path endpoint tests for each Document endpoint.
- **Reviewer:** Check ownership enforcement and status codes.
- **Acceptance criteria:**
  - Document CRUD operations work under an existing Client.
  - Duplicate `Document.number` returns HTTP 409.
  - Document not found under that Client returns HTTP 404.
  - Validation failures return HTTP 400 with Problem Details.
  - Accessing a Document through a nested endpoint under a Client ID that does not own it returns HTTP 404.
  - Version validation, optimistic-locking and conflict behavior for these endpoints are verified in T2.6 and transactional header behavior in T2.7.

#### T2.5 — Aggregate update semantics and Document identity preservation

- **In scope:** Client PUT collection-preserving rules, Client PATCH partial update, Document PUT/PATCH field replacement, omitted/empty collection behavior, existing/new/omitted Document handling, unknown Document ID rejection, cross-Client Document rejection.
- **Business rules:**
  - Omitted `documents`: preserve existing Documents.
  - Empty `documents`: preserve existing Documents.
  - Document with existing ID: update.
  - Document without ID: create.
  - Existing Document omitted from supplied collection: preserve.
  - Unknown Document ID: reject.
  - Document belonging to another Client: reject with HTTP 409.
  - No implicit Document deletion on update.
  - Document deletion is only through DELETE.
- **Developer:** Implement the aggregate merge logic in the Service layer.
- **Tester:** Verify omitted, empty, update, create, unknown ID and cross-owner scenarios.
- **Reviewer:** Confirm no unexpected Document creation or identity loss.
- **Acceptance criteria:**
  - Existing Document identities are preserved.
  - No orphan or unexpected Documents are created.
  - A Document belonging to another Client returns HTTP 409.
  - Attempting to associate an existing Document owned by another Client during a Client aggregate update returns HTTP 409.
  - Unknown Document IDs remain rejected according to the existing rules.
  - Consistent behavior whether updated through Client or Document endpoints.

#### T2.6 — Concurrency control and version validation

- **In scope:** Required expected Client and Document `version` on applicable update requests, JPA `@Version` enforcement, incrementing the parent Client version using `OPTIMISTIC_FORCE_INCREMENT` whenever a Document is created, updated or deleted within the same transaction, HTTP 409 on stale version, full transaction rollback on conflict, equivalent concurrency rules across endpoints.
- **Approved contract:** POST Client requires no version. POST Document requires the `X-Client-Version` header. PUT/PATCH Client require the expected Client `version` in the request DTO. PUT/PATCH Document require the expected Document `version` in the request DTO plus the `X-Client-Version` header. PUT/PATCH Client with nested Documents requires the expected Client `version` and the expected `version` for every existing nested Document being updated; new nested Documents have no version. DELETE Client requires the `X-Client-Version` header. DELETE Document requires both the `X-Client-Version` and `X-Document-Version` headers. Missing, malformed or invalid required versions return HTTP 400. Stale versions return HTTP 409 Problem Details. Every Document create, update or delete increments the parent Client version using JPA `OPTIMISTIC_FORCE_INCREMENT` within the same transaction. Conflicts roll back the entire transaction.
- **Developer:** Implement version validation and conflict mapping.
- **Tester:** Verify stale-version conflicts for Client and Document updates, including nested aggregate updates.
- **Reviewer:** Audit partial persistence prevention and conflict response consistency.
- **Acceptance criteria:**
  - Applicable updates require the expected Client and Document versions.
  - Creating, updating or deleting a Document increments the parent Client version via OPTIMISTIC_FORCE_INCREMENT within the same transaction.
  - Missing, malformed or invalid required versions return HTTP 400.
  - Stale versions return HTTP 409 Problem Details with full transaction rollback.
  - Conflicts do not result in partial persistence.
  - Aggregate updates account for concurrent Document changes.

#### T2.7 — Transactional atomicity and deletion rules

- **In scope:** Service-layer `@Transactional` boundaries for Client-with-Documents creation, aggregate update, Client deletion, Document modification and Document deletion; version-header validation on DELETE requests.
- **Developer:** Define transaction boundaries and rollback paths.
- **Tester:** Verify rollback on failure and cascade deletion.
- **Reviewer:** Verify transaction correctness and no partial commits.
- **Acceptance criteria:**
  - Client creation with Documents is atomic.
  - Client update with Documents is atomic.
  - Client deletion with Documents is atomic.
  - DELETE `/clients/{id}` requires the `X-Client-Version` header.
  - DELETE `/clients/{clientId}/documents/{documentId}` requires both `X-Client-Version` and `X-Document-Version` headers.
  - Missing or invalid version headers on DELETE return HTTP 400.
  - Stale versions on DELETE return HTTP 409.
  - Deletion and version checks are atomic.
  - Any failed operation inside a transaction rolls back.
  - Deleting a Document removes only that Document.

#### T2.8 — Pagination, ordering and query behavior

- **In scope:** Zero-based pagination, page/size defaults (`page=0`, `size=20`), maximum page size of 100, HTTP 400 for size above 100, deterministic ordering by ID ascending, pagination metadata in responses.
- **Developer:** Implement `Pageable` support and metadata mapping.
- **Tester:** Verify page size limits, metadata, deterministic ordering and boundary pages.
- **Reviewer:** Audit pagination behavior and documentation readiness.
- **Acceptance criteria:**
  - List endpoints support pagination.
  - Default page is `0` and default size is `20`.
  - Maximum page size is 100.
  - Page size above 100 returns HTTP 400.
  - Ordering is deterministic by ID ascending.

#### T2.9 — Optimize Client Listing — Fix N+1

- **Status:** COMPLETED (accepted by the Tech Lead; PostgreSQL verification deferred).
- **In scope:** Reduce SQL query growth for paginated `GET /api/clients` while preserving existing REST, DTO, Client ordering and pagination behavior.
- **Implementation:** Keep the existing pageable `ClientRepository.findAll(pageable)` query. Collect IDs from the returned page, then fetch its Documents with one bulk `IN` query. Group Documents by owning Client ID and map each Client response with an explicit Document list. Skip the Documents query when the Client page is empty. Keep the existing `ClientMapper.toResponse(Client)` path for other CRUD operations.
- **Exclusions:** No `@BatchSize`, collection `JOIN FETCH` on the pageable Client query, schema changes, DTO/API changes or unrelated endpoint optimization.
- **Acceptance criteria:**
  - Non-empty Client listing requests for page sizes 1, 5, 20 and 100 execute no more than 3 SQL statements each, with exactly one bulk Documents query per page and no per-Client Document loads.
  - Empty/out-of-range pages issue no bulk Documents query and retain the existing response envelope and pagination metadata.
  - Responses preserve Client ID-ascending ordering, requested page boundaries and page metadata, and contain the complete, correctly owned nested Document values in compatible order.
  - Regression tests cover non-empty sizes 1/5/20/100, empty pages, response content, ordering and pagination; focused and full Maven test suites pass.
  - Existing CRUD mapping behavior remains unchanged; no `@BatchSize`, collection fetch join or schema change is introduced.
- **Developer:** Implement the two-step page-then-bulk-fetch flow in the service/repository boundary and explicit-list mapping path; record actual query counts and test results.
- **Tester:** Independently verify query behavior and response/pagination contracts.
- **Reviewer:** Independently audit the implementation and approved design boundaries.
- **Verification note:** PostgreSQL verification is deferred by the Tech Lead decision recorded for T2.9; the reported H2 acceptance tests passed.

### Phase 2 exit criteria

- Full Client and Document CRUD works.
- Validation, uniqueness, ownership, version validation, optimistic locking and transaction rules are implemented and tested.
- Collection update semantics are enforced and tested.
- Phase 2 tests pass.
- Phase 2 independent Reviewer audit is complete.

## 7. Phase 3 — JWT Authentication and Authorization

### Objective

Secure the API using Spring Security and signed JWTs while preserving Phase 2 behavior. Phase 3 uses one authenticated user type with no roles or permissions. JWTs are stateless, signed with HS256, and use BCrypt for password encoding. Production credentials come from environment variables; the development demo user is available only with the `dev & !prod` profile condition. There is no user database, registration or refresh-token flow. HTTP 403 uses a standard safe handler; no artificial authorization rules are added solely to produce forbidden responses.

### Approved Phase 3 decisions

- Login endpoint: `POST /auth/login`.
- Production credentials are supplied through environment variables; the development demo user is not provisioned in production and is restricted to `dev & !prod`.
- There is one authenticated user type and no roles or permissions.
- Use stateless JWT bearer authentication with HS256, the existing external JWT secret and existing token-expiration configuration; use BCrypt for password encoding.
- Keep the existing Spring Security dependencies and JWT property/validation infrastructure. Do not add a user database, registration, refresh tokens, another JWT library or additional infrastructure.
- Return safe Problem Details for authentication (401) and access-denied (403) failures. Do not add artificial authorization rules; a standard 403 handler is sufficient where no forbidden business action is defined.

### Task list

#### T3.1 — Security baseline and JWT infrastructure

- **In scope:** Spring Security filter chain; stateless sessions; disable CSRF for the Bearer-token API; HS256 JWT encoder/decoder using `AppJwtProperties` and the externally supplied secret; existing token-expiration configuration; BCrypt password encoder; OAuth2 Resource Server JWT validation; safe 401/403 Problem Details handlers; permit `/auth/login` and Swagger/OpenAPI routes; require authentication for all other routes. No roles or permissions.
- **Out of scope:** Login controller/token issuance, account provisioning, user database, registration, refresh tokens and artificial authorization rules.
- **Developer:** Implement only the security configuration and JWT infrastructure using existing dependencies and `AppJwtProperties`/`JwtSecretValidator`.
- **Tester:** Verify valid signed JWT acceptance; invalid-signature and expired-token rejection; unauthenticated protected-route 401; safe security error bodies; and stateless behavior.
- **Reviewer:** Audit security configuration, token validation, secret handling and route policy.
- **Acceptance criteria:**
  - Valid HS256 JWTs are accepted and invalid-signature or expired JWTs are rejected.
  - Protected routes reject requests without a bearer JWT using HTTP 401; login and Swagger/OpenAPI paths are permitted.
  - No server-side session is created and CSRF is disabled for the API.
  - BCrypt password encoder is configured.
  - Authentication and access-denied error bodies contain no secrets or token contents.

#### T3.2 — One-user credential provisioning

- **In scope:** One authenticated account source; production credentials from environment variables; configurable demo-user credentials only with `dev & !prod`; BCrypt-backed credential verification.
- **Out of scope:** User database, registration and account-management endpoints.
- **Developer:** Configure the approved account sources without provisioning the demo user in production.
- **Tester:** Verify credential configuration, BCrypt verification, invalid credentials, and absence of demo-user provisioning in production and combined `dev,prod` profiles.
- **Reviewer:** Check credential externalization and profile isolation.
- **Acceptance criteria:**
  - Exactly one authenticated user type is supported; no roles or permissions are introduced.
  - Production credentials are externalized and development demo credentials are available only when `dev & !prod` applies.
  - No account is persisted to a database.

#### T3.3 — Login API and token contract

- **In scope:** `POST /auth/login`, credential validation and signed JWT response using the configured HS256 encoder and expiration.
- **Out of scope:** Registration, refresh tokens and additional user types.
- **Developer:** Implement the login endpoint and token issuance for the single authenticated user type.
- **Tester:** Verify valid credentials and generic HTTP 401 for invalid credentials.
- **Reviewer:** Check login contract and sensitive-data handling.
- **Acceptance criteria:**
  - Login is available at `POST /auth/login` and valid credentials return a signed JWT.
  - Invalid credentials return HTTP 401 without distinguishing account existence from password mismatch.
  - JWTs use HS256 and the configured expiration; no password, hash or secret is returned.

#### T3.4 — Public routes and endpoint protection

- **In scope:** Permit `/auth/login` and Swagger/OpenAPI routes; require a valid JWT for all other routes, including Client and Document APIs.
- **Developer:** Verify and adjust route access policy without introducing role/permission rules.
- **Tester:** Verify public paths, authenticated access and missing/invalid/expired bearer-token rejection.
- **Reviewer:** Audit route coverage, standard 401/403 handling and Problem Details safety.
- **Acceptance criteria:**
  - Login and Swagger/OpenAPI routes are accessible without a JWT.
  - All other routes require a valid JWT.
  - Missing/invalid/expired tokens return HTTP 401; a standard safe HTTP 403 handler exists without artificial authorization rules.

#### T3.5 — Phase 2 regression and Phase 3 security audit

- **In scope:** Re-run Phase 2 behavior under the security filter chain, adjust tests to authenticate where appropriate, and perform the independent Phase 3 Reviewer audit.
- **Developer:** Resolve only approved security integration or regression issues.
- **Tester:** Verify Phase 2 regression and the complete Phase 3 security contract.
- **Reviewer:** Independently audit the Phase 3 implementation and regression evidence.
- **Acceptance criteria:**
  - Phase 2 functional behavior remains unchanged.
  - Phase 3 security tests pass and the independent Reviewer audit is complete before Tech Lead acceptance.

### Phase 3 exit criteria

- Login works and returns JWT.
- Business endpoints are protected.
- Security tests pass.
- Phase 3 independent Reviewer audit is complete.
- Phase 3 remains incomplete until the human Tech Lead grants acceptance.

## 8. Phase 4 — OpenAPI Documentation and API Contract Hardening

### Objective

Complete code-first Swagger/OpenAPI documentation and perform an API contract audit.

### Task list

#### T4.1 — OpenAPI coverage

- **In scope:** Document all endpoints, DTOs, status codes, validation rules, JWT auth, pagination, PUT/PATCH semantics, ownership rules, optimistic-locking conflicts and Problem Details responses.
- **Developer:** Add OpenAPI annotations.
- **Tester:** Verify Swagger UI and generated documentation endpoints.
- **Reviewer:** Check documentation accuracy.
- **Acceptance criteria:**
  - All endpoints are documented.
  - Authentication is represented in OpenAPI.
  - Request and response schemas are present.

#### T4.2 — Error and contract consistency audit

- **In scope:** Verify equivalent validation, uniqueness, identity and concurrency rules across Client and Document endpoints; ensure PUT/PATCH semantics are consistently documented and enforced.
- **Developer:** Fix inconsistencies found during audit.
- **Tester:** Add regression tests for inconsistent paths.
- **Reviewer:** Perform independent contract audit.
- **Acceptance criteria:**
  - Document updates behave consistently across endpoints.
  - No unexpected Document creation or deletion occurs.
  - RFC 9457 responses are consistent.

#### T4.3 — README technical decisions

- **In scope:** README sections covering purpose, technology choices, build/run, tests, authentication, Swagger access, design decisions, assumptions, PUT versus PATCH, transaction and concurrency strategy.
- **Developer:** Write README within code repository.
- **Tester:** Follow README instructions to run project.
- **Reviewer:** Check clarity and completeness.
- **Acceptance criteria:**
  - README setup instructions work.
  - Design decisions and assumptions are documented.
  - Phase 4 tests and review are complete.

### Phase 4 exit criteria

- OpenAPI documentation is complete and accurate.
- Contract consistency issues are resolved.
- README is validated.
- Phase 4 independent Reviewer audit is complete.

## 9. Phase 5 — Containerization and Environment Delivery

### Objective

Provide Docker and Docker Compose support while preserving local Java/Maven execution.

### Task list

#### T5.1 — Application Dockerfile

- **In scope:** Dockerfile for the Spring Boot application using the approved build baseline.
- **Developer:** Create and verify Docker image build.
- **Tester:** Run container smoke test.
- **Reviewer:** Audit image configuration and secrets.
- **Acceptance criteria:**
  - Application image builds successfully.
  - Container starts in development configuration.
  - No secrets are baked into the image.

#### T5.2 — Docker Compose and PostgreSQL

- **In scope:** `compose.yaml` for application and PostgreSQL, environment-variable configuration, migration execution.
- **Developer:** Create Compose configuration.
- **Tester:** Start stack and verify endpoints.
- **Reviewer:** Audit environment configuration.
- **Acceptance criteria:**
  - Application and PostgreSQL start via Compose.
  - Production profile works against PostgreSQL.
  - Flyway migrations run successfully.

#### T5.3 — Local execution parity

- **In scope:** Verify that local Java/Maven execution remains supported and documented.
- **Developer:** Keep profiles and documentation aligned.
- **Tester:** Run both local and containerized paths.
- **Reviewer:** Audit delivery completeness.
- **Acceptance criteria:**
  - Local execution works without Docker.
  - Containerized execution works.
  - Phase 5 tests and review are complete.

### Phase 5 exit criteria

- Dockerfile and Compose configuration are functional.
- PostgreSQL path works.
- Local execution remains supported.
- Phase 5 independent Reviewer audit is complete.

## 10. Phase 6 — End-to-End Verification and Acceptance

### Objective

Perform final acceptance validation across all previously delivered phases. Phase 6 is not the only phase with tests; it provides consolidated end-to-end and audit verification.

### Task list

#### T6.1 — Consolidated test suite

- **In scope:** Run unit, integration, security, pagination, transaction, concurrency and documentation smoke tests.
- **Developer:** Fix final defects.
- **Tester:** Execute full suite and coverage review.
- **Reviewer:** Audit test completeness.
- **Acceptance criteria:**
  - All tests pass via Maven.
  - Coverage includes every implemented endpoint happy path.
  - All required additional scenarios are represented.

#### T6.2 — Acceptance scenario validation

- **In scope:** Validate the required scenarios: request validation failures, not found, duplicate tax identifier, duplicate document number, transaction rollback, cascade deletion, identity preservation, collection omission, empty collection, ownership violations, JWT success/failure, protected endpoints, optimistic locking conflicts, pagination and Problem Details responses.
- **Developer:** Resolve final business defects.
- **Tester:** Execute acceptance scenarios.
- **Reviewer:** Verify requirement traceability.
- **Acceptance criteria:**
  - All required scenarios pass.
  - No Critical or Major findings remain.

#### T6.3 — Final release readiness audit

- **In scope:** Final code review, configuration audit, migration compatibility check, security review, delivery review and README validation.
- **Developer:** Apply final approved changes only.
- **Tester:** Re-run full suite after final changes.
- **Reviewer:** Perform final independent audit.
- **Acceptance criteria:**
  - Phase 6 tests pass.
  - Final independent Reviewer audit is complete.
  - Tech Lead approves release readiness.

### Phase 6 exit criteria

- Consolidated verification passes.
- All required acceptance scenarios pass.
- Final audit is complete.
- Tech Lead grants final approval.

## 11. Required Test Themes Across Phases

Each phase must include tests appropriate to its scope. The following themes must appear in at least one phase:

- Controller unit tests with mocked dependencies for endpoint happy paths.
- Integration tests for persistence, transactions, concurrency, authentication and database constraints.
- Happy-path endpoint tests.
- Validation failures.
- Resource not found.
- Duplicate tax identifier.
- Duplicate document number.
- Transaction rollback.
- Client deletion cascade.
- Document identity preservation.
- Document collection omission.
- Empty Document collection.
- Document ownership violations.
- JWT authentication success and failure.
- Protected endpoint access.
- Optimistic locking conflicts.
- Pagination behavior.
- Consistent Problem Details responses.

## 12. Risk and Notes

- Moving optimistic locking and ownership into Phase 2 increases Phase 2 complexity. The task split is intentionally granular to keep each approval small.
- Phase 2 endpoints are initially verified before JWT enforcement; Phase 3 must not alter their business behavior.
- Collection-preserving PUT semantics must be explicitly tested to prevent accidental Document deletion.
- Version conflict handling must prevent partial aggregate writes.
- H2 and PostgreSQL migration compatibility must be verified in each persistence-related phase.

## 13. Change Summary

This consolidated plan updates the previously approved six-phase strategy by:

- Placing version validation, optimistic locking and Document ownership controls in Phase 2.
- Requiring every phase to include tests and an independent Reviewer audit.
- Splitting Phase 1 into independently approved tasks, beginning with project bootstrap only.
- Resolving Phase 2 task boundaries so T2.3/T2.4 CRUD acceptance criteria do not depend on T2.6/T2.7 concurrency and transactional work; version, conflict and transactional criteria remain in T2.6 and T2.7.
- Moving the cross-Client aggregate association HTTP 409 acceptance criterion from T2.4 to T2.5 while retaining the wrong-Client nested resource HTTP 404 rule in T2.4.
- Clarifying that PostgreSQL integration verification requires an available PostgreSQL instance while full Docker Compose verification remains in Phase 5.
- Recording approved D-11 application-layer and persistence boundaries for T2.2, T2.3 and T2.4 without changing API endpoints or unrelated acceptance criteria.
