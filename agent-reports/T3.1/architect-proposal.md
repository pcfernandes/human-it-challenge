# Architect Proposal — Phase 3 Authentication & Security Design

## 1. Task ID

- **VERIFIED** (direct tasking): `T3.1` — Phase 3 authentication and security design for the Human IT challenge, including confirmation against `implementation-plan.md`, an implementation task split, and acceptance criteria.
- **VERIFIED** (tasking): The requested deliverable is `agent-reports/T3.1/architect-proposal.md`; implementation must not be performed by this execution.

## 2. Execution identifier and date

- **VERIFIED** (this execution): `T3.1-ARCH-001`.
- **VERIFIED** (execution environment): 2026-11-11.
- **VERIFIED** (`agent-reports/README.md`, Section 6): No earlier Architect primary report for T3.1 was found, so no history archive was required before creating this report.

## 3. Agent role and model

- **VERIFIED** (execution environment): Senior Java/Spring Boot Architect; model `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct tasking): Read the reporting conventions, implementation plan, and existing Spring configuration, dependencies, entities, controllers and tests; design the smallest maintainable Phase 3 solution; identify plan conflicts; define endpoints, flow, classes, dependencies, migration, task breakdown and acceptance tests.
- **VERIFIED** (role constraints): No production code or tests, no commits/pushes/merges/branch changes, preserve unrelated worktree changes, do not self-approve, and stop after reporting.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): The report must use all 13 mandatory sections, distinguish evidence using the prescribed markers, and archive an earlier report before replacing it.
- **VERIFIED** (Architect role restrictions): This role may author Architect reports but must not modify `implementation-plan.md` or other application/planning artifacts. The requested plan update is therefore documented as a blocker and proposed change only; no plan update was made.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md` — 13-section report format, evidence markers, execution-history and governance rules.
- **VERIFIED**: `implementation-plan.md`, Sections 2, 3, 5, 6, 7, 8 and 11 — approval workflow; Phase 3 objective, tasks and exits; Phase 4 OpenAPI follow-up; cross-phase test themes.
- **VERIFIED**: `pom.xml` — Spring Boot 3.5.16, Java 21, `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server`, `spring-security-test`, and springdoc are already declared.
- **VERIFIED**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml` — JWT expiry and external JWT secret configuration; default dev profile; dev datasource; production datasource and absence of demo-user settings.
- **VERIFIED**: `src/main/java/com/humanit/challenge/config/AppJwtProperties.java`, `AppDemoUserProperties.java`, `JwtSecretValidator.java`, `ConfigPropertiesRegistration.java`, `DevConfigPropertiesRegistration.java` — external JWT secret configuration/validation and dev-only demo-user property registration guarded by `dev & !prod`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` — metadata bean exists, but no security scheme is registered there.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/ClientController.java` and `DocumentController.java` — current business paths are `/api/clients/**` and `/api/clients/{clientId}/documents/**`.
- **VERIFIED**: `src/main/resources/db/migration/V1__baseline.sql` and `V2__create_clients_and_documents.sql` — current schema has no user/account table.
- **VERIFIED**: `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` — MVC exceptions use RFC 9457 Problem Details, but Spring Security filter-chain errors need their own entry-point/access-denied handling.
- **VERIFIED**: `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java` and `DocumentControllerTest.java` — controller slice tests disable servlet filters; they are not end-to-end security tests.
- **VERIFIED**: `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java`, `DemoUserPropertiesTest.java`, and `SourceScanAuditTest.java` — current metadata, dev demo-property, and profile/configuration checks.
- **VERIFIED**: `agent-reports/T1.3/security-profiles-assessment.md` — existing assessment recommends applying `dev & !prod` to future Phase 3 demo-user provisioning as well as to demo-user property registration.
- **VERIFIED**: `agent-reports/decisions.md` — the project records prior decisions; no Phase 3 approval was established by this execution. The file was inspected only and was not modified.
- **NOT AVAILABLE**: No existing `agent-reports/T3.1/**` report was found before this execution.

## 6. Files changed

- **VERIFIED**: Created `agent-reports/T3.1/architect-proposal.md` (this report).
- **VERIFIED**: No application code, tests, configuration, dependencies, migrations, requirements, implementation plan, other agent reports, decision log, or Git state was changed.
- **VERIFIED**: `implementation-plan.md` was not updated because the Architect role is explicitly prohibited from modifying planning documents. The requested update requires an authorized role/Tech Lead follow-up; Section 10 supplies a proposed replacement task breakdown for that action.

## 7. Commands executed

- **VERIFIED**: None. No shell, build, test or Git commands were executed. Workspace read/glob and report-write tools were used only.

## 8. Evidence and observed results

### 8.1 Phase 3 requirements confirmed from the plan

- **VERIFIED** (`implementation-plan.md`, Section 7): Phase 3 is named “JWT Authentication and Authorization”; its objective is to secure the API using Spring Security and signed JWTs while preserving Phase 2 behavior.
- **VERIFIED** (`implementation-plan.md`, T3.1): The plan currently calls for JWT generation/validation, a security filter chain, stateless sessions, password encoding, and a “role/authority model baseline”; its acceptance requires invalid/expired JWT rejection, stateless security and BCrypt.
- **VERIFIED** (`implementation-plan.md`, T3.2): The plan specifies `POST /auth/login`, credential validation, a JWT response and configurable development-only demo-user provisioning; valid credentials return JWT, invalid credentials return 401, and the demo account is absent in production.
- **VERIFIED** (`implementation-plan.md`, T3.3): Client and Document endpoints must require valid JWTs; missing/invalid tokens return 401 and the plan also names 403 for forbidden actions.
- **VERIFIED** (`implementation-plan.md`, T3.4 and Phase 3 exit criteria): Phase 2 tests must be re-run with authentication where applicable, security happy/failure paths must be tested, and an independent Reviewer audit is required before Phase 3 can exit.
- **VERIFIED** (`implementation-plan.md`, Section 2): Implementation requires Tech Lead approval, tests, independent review and final Tech Lead acceptance; no phase is complete based on this proposal alone.

### 8.2 Existing foundation and integration points

- **VERIFIED** (`pom.xml`): Spring Security, OAuth2 Resource Server, Spring Security test support and springdoc are already present. The minimal design does not require another dependency unless implementation verification disproves the needed encoder/decoder classes are available through the current dependency set.
- **VERIFIED** (`AppJwtProperties`, `JwtSecretValidator`, `application.yml`): A standard Base64 JWT secret is externally supplied as `APP_SECURITY_JWT_SECRET`, validated to decode to at least 32 bytes, and the expiration setting defaults to 60 minutes. The current property/validator infrastructure can be reused; no new secret should be committed.
- **VERIFIED** (`AppDemoUserProperties`, `DevConfigPropertiesRegistration`): Existing configurable demo credentials are development-only, and provisioning must use the same strict `dev & !prod` guard to avoid combined-profile registration.
- **VERIFIED** (`ClientController`, `DocumentController`): All existing Client and Document routes are under `/api/clients/**`.
- **VERIFIED** (`OpenApiConfig`): OpenAPI is initialized but no bearer scheme has been declared. T3.1 may keep Swagger/OpenAPI public; explicit JWT documentation in OpenAPI is also a Phase 4 plan item.
- **VERIFIED** (`GlobalExceptionHandler`): Problem Details are established for MVC processing. Authentication and authorization failures occur in the security filter chain and will not necessarily pass through this advice; security handlers are required to preserve the API error contract.
- **VERIFIED** (existing controller tests): `@AutoConfigureMockMvc(addFilters = false)` is used in Client/Document controller tests. Those focused tests can remain filter-isolated; separate security-enabled integration tests must exercise the actual filter chain.
- **VERIFIED** (V1/V2 migrations): There is no users table. A single fixed account held by Spring Security's in-memory `UserDetailsService` does not require persistence or a Flyway migration.
- **NOT AVAILABLE**: The current plan/configuration does not specify how a production user obtains credentials or is provisioned; production explicitly excludes the dev demo user, while the business API is intended to be secured.
- **PENDING VERIFICATION**: Confirm the exact Nimbus JWT encoder/decoder APIs and key construction work with the versions already resolved by the Spring Boot dependency set during an authorized implementation/build; no dependency tree or build was run here.

### 8.3 Design recommendation in brief

- **RECONSTRUCTED** (from observed scope and dependencies): The smallest solution is one authenticated principal, BCrypt-backed password verification, a short-lived HS256 signed access JWT, Spring Security OAuth2 Resource Server bearer validation, stateless sessions, and one catch-all authenticated policy after explicit public routes. No roles, refresh tokens, registration, recovery, OAuth2 login, user table or migration are needed for the proposed single-account scope.
- **PENDING VERIFICATION**: The production credential-provisioning decision and final login path need Tech Lead resolution before the plan can be updated or implementation begins.

## 9. Findings and severity

| ID | Severity | Evidence / finding |
|---|---|---|
| F3-01 | **Major** | **VERIFIED**: T3.1 in the approved plan requests a role/authority model baseline, while the direct tasking expressly excludes role-based authorization and calls for one authenticated user type with no roles. Remove the role-model requirement; an empty authority set or a single non-authorization marker must not be used to create role complexity. |
| F3-02 | **Major** | **VERIFIED**: The plan provisions only a development demo user and says production must not provision it; production login/account provisioning is not specified. **RECONSTRUCTED**: Without a separate production credential source, production can validate bearer tokens but no operator can obtain one through login. Tech Lead must decide the minimal production credential mechanism. |
| F3-03 | **Major** | **VERIFIED**: The plan names `/auth/login`, whereas all current business endpoints use `/api/...`. Preserve `/auth/login` to avoid silently changing the existing plan contract, or explicitly approve and update it to `/api/auth/login` for namespace consistency. |
| F3-04 | **Minor** | **VERIFIED**: OpenAPI is already initialized but has no security scheme; public Swagger/OpenAPI access needs explicit security matchers. T4.1 separately requires documenting JWT security. Do not inadvertently require a token to fetch the UI or JSON contract. |
| F3-05 | **Minor** | **VERIFIED**: MVC Problem Details handling does not establish filter-chain 401/403 responses. Add security entry-point and access-denied response handling without exposing credentials, token contents or exception details. |
| F3-06 | **Minor** | **VERIFIED**: Existing Client/Document controller tests disable filters. Relying on them alone would not verify endpoint protection; add filter-enabled security integration tests while retaining focused controller tests as appropriate. |
| F3-07 | **Minor** | **VERIFIED**: The Phase 3 plan requests HTTP 403 for forbidden actions, but role-based authorization is excluded and only one authenticated user type exists. There is no defined business action that should be forbidden to that principal. Retain a standard 403 handler, but do not invent role/permission policies or a 403 business contract; Tech Lead should clarify whether handler-only verification satisfies the legacy criterion. |
| F3-08 | **Info** | **VERIFIED**: Security and OAuth2 Resource Server dependencies are already included; adding another JWT library would duplicate existing capability unless implementation verification identifies a concrete gap. |
| F3-09 | **Info** | **VERIFIED**: No user schema exists. **RECONSTRUCTED**: For one externally configured account, a database entity and migration add lifecycle/schema complexity without a current registration, recovery or user-management use case. Recommend no migration. |

No Critical finding identified. Findings F3-01 through F3-03 require human decisions/plan reconciliation before implementation; this report does not resolve or approve them.

## 10. Decisions and reasoning

### 10.1 Proposed security architecture

1. **Authentication mechanism — PROPOSED, not approved:** Use Spring Security form-free JSON login with a `DaoAuthenticationProvider`/`AuthenticationManager` backed by one `UserDetailsService` account. Use `BCryptPasswordEncoder` for stored in-memory password representation and credential verification. Never log username/password request bodies, password hashes or bearer tokens.
2. **Provisioning — PROPOSED, subject to F3-02:** Keep the existing development demo account configurable and provision it only under `dev & !prod`; encode its configured password with BCrypt on startup. For production, use one explicitly configured deployment account sourced only from environment variables/secrets. Do not provision the dev demo identity in production. Its property name/format and production binding policy must be approved before implementation; the present plan does not define them.
3. **JWT — PROPOSED:** Issue a signed HS256 JWT containing only `sub`, `iat`, and `exp`; use the existing externalized, minimum-256-bit secret and configured 60-minute expiry. Do not include roles/authorities. Validate bearer tokens with Spring Security OAuth2 Resource Server and a Nimbus decoder; use Spring Security's Nimbus encoder for issuance if supported by the already-resolved dependency set. No custom JWT parsing filter and no additional JWT library.
4. **Stateless filter chain — PROPOSED:** `SessionCreationPolicy.STATELESS`; disable CSRF for the JSON/Bearer API chain; use the bearer-token filter for subsequent requests. Configure a 401 authentication entry point and 403 access-denied handler with safe RFC 9457-style `application/problem+json` bodies. Do not add a role hierarchy, method-security annotations, refresh tokens, logout/token-revocation store, or per-user data authorization.
5. **Authorization — PROPOSED:** Explicitly permit the login path selected by the Tech Lead and springdoc UI/OpenAPI paths (at minimum `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`). Require authentication for all other requests (`anyRequest().authenticated()`) so business endpoints and unintended auxiliary routes are not accidentally public. This also keeps H2 Console from becoming an anonymous route; do not create a browser/H2 exception as part of Phase 3 absent separate approval.
6. **Migration — PROPOSED:** None. The only account is external configuration loaded into an in-memory Spring Security user service, not a persisted domain resource. A user table/migration becomes justified only if persistent account management or multiple users is later approved.
7. **API contract — PROPOSED:** Follow the current plan literally with `POST /auth/login` unless the Tech Lead approves changing it to `/api/auth/login`. Request body: `{ "username": "…", "password": "…" }`. Success: HTTP 200 and `{ "accessToken": "<signed JWT>", "tokenType": "Bearer", "expiresIn": 3600 }` (seconds, derived from configured expiry). Invalid credentials: generic HTTP 401 response, not distinguishable between unknown username and wrong password. Validation failures: HTTP 400 Problem Details. Clients send `Authorization: Bearer <accessToken>` on protected requests. Do not return the configured password, hash, secret, or internal claims beyond the token.
8. **Public OpenAPI — PROPOSED:** Keep Swagger UI and `/v3/api-docs/**` publicly reachable. Add a bearer security scheme in Phase 4's OpenAPI task (or a narrowly scoped Phase 3 OpenAPI hook only if required by an implementation dependency); runtime public-path allowlisting belongs in Phase 3.

### 10.2 Small implementation task breakdown for the plan (proposed replacement)

The following is a **proposal for a separately authorized `implementation-plan.md` edit**, not a record that the breakdown is agreed or approved. It keeps Phase 2 unchanged and retains task-level test/reviewer gates.

#### T3.1 — Stateless security and JWT foundation

- **Scope:** Spring Security filter chain; stateless policy; JWT encoder/decoder configuration using existing external secret; BCrypt encoder; resource-server bearer validation; safe 401/403 handlers; no role model.
- **Dependencies/migration:** No new dependency expected and no migration.
- **Minimum tests:** Validly signed token accepted by decoder; wrong-key, malformed, unsupported/invalid and expired tokens rejected; session remains stateless; security error handlers return safe response shapes.
- **Acceptance:** Existing 32-byte external secret validation remains effective; signed-token acceptance and invalid/expired-token rejection verified; no server-side session is created; no secret/token/password is exposed in logs or error bodies.

#### T3.2 — One-user credential provisioning

- **Scope:** Configure one authenticated account source; use BCrypt credentials; preserve dev demo user configurability with the existing `dev & !prod` guard; define the production credential source without enabling demo provisioning in production.
- **Dependencies/migration:** Reuse current dependencies; no database entity/table or Flyway migration.
- **Minimum tests:** Dev credentials bind and authenticate; wrong password fails; produced/stored credential is BCrypt-encoded; demo account absent under `prod` and combined `dev,prod`; production starts/authenticates only with its explicitly approved external account configuration.
- **Acceptance:** No plaintext password stored in the `UserDetails` account or returned/logged; demo credentials are never provisioned when `prod` is active; production account-source decision is explicit and tested.
- **Gate:** Do not begin until F3-02 is resolved by the Tech Lead.

#### T3.3 — Login endpoint and token contract

- **Scope:** Login request/response DTOs and one login controller/service path; validate credentials through Spring Security; issue the JWT; generic invalid-credential result.
- **Contract:** Keep `/auth/login` per plan unless a path change is approved. Return HTTP 200 with Bearer token and expiry seconds; invalid credentials return 401; malformed/invalid request returns 400.
- **Minimum tests:** Successful login yields a token that validates and has expected subject/expiry; bad username and bad password both yield the same generic 401 semantics; validation errors are 400 Problem Details; response contains no password/hash/secret.

#### T3.4 — Public routes and business endpoint protection

- **Scope:** Permit login and OpenAPI/Swagger only; require a valid bearer JWT for `/api/clients/**` and nested Documents; configure stateless unauthenticated/denied responses.
- **Minimum tests:** Missing, malformed, invalid and expired token are rejected with 401 on representative Client and Document reads/writes; valid token permits each route family; Swagger UI and JSON OpenAPI paths remain anonymously reachable; no role-based 403 rule is added.
- **Acceptance:** All Client/Document endpoint methods require authentication, with original Phase 2 validation, ownership, version and response behavior unchanged.

#### T3.5 — Phase 2 regression and Phase 3 security audit

- **Scope:** Run Phase 2 functional suite, adjust its integration tests to authenticate where filters are active, preserve isolated controller test intent, and add full-filter-chain coverage. Complete independent Reviewer audit.
- **Minimum tests:** Full suite; login/security success and failure paths; Client/Document route coverage; error format; public docs; regression of CRUD, ownership, validation, optimistic locking, transactions and pagination.
- **Acceptance:** No Phase 2 behavior regression; required security tests pass; no Critical/Major Reviewer findings remain; Phase 3 completion remains pending Tech Lead approval.

### 10.3 Minimal test matrix across T3.1–T3.5

- **PENDING VERIFICATION:** JWT unit/integration coverage for valid signature, wrong signature, malformed token, expired token and expiry boundary.
- **PENDING VERIFICATION:** Login tests for valid account, unknown username, incorrect password, invalid request, generic failure equivalence, BCrypt verification and no sensitive response fields.
- **PENDING VERIFICATION:** Filter-enabled MockMvc tests for representative requests in both `/api/clients/**` and nested Documents: anonymous 401, bad/expired bearer 401, valid bearer success. Cover method classes across GET/POST/PUT/PATCH/DELETE, or use a parameterized route matrix to ensure all mappings are protected.
- **PENDING VERIFICATION:** Anonymous access to `/swagger-ui/**`, `/swagger-ui.html` and `/v3/api-docs/**`; verify login remains public.
- **PENDING VERIFICATION:** Statelessness (no `JSESSIONID`, no persisted HTTP session) and error responses with correct status/content type, no token/password/secret disclosure.
- **PENDING VERIFICATION:** Profile provisioning tests: dev account available; production and combined `dev,prod` do not contain the demo account; production configured identity behavior follows the approved provisioning contract.
- **PENDING VERIFICATION:** Full Phase 2 suite under its existing intent plus authenticated end-to-end coverage. Existing `@AutoConfigureMockMvc(addFilters = false)` slice tests should not be treated as protection tests.
- **PENDING VERIFICATION:** 403 handler response format may be tested directly or through a controlled denied test path, but do not add application roles or a forbidden business operation solely to force a 403 test. The Tech Lead should decide whether this satisfies the old plan criterion.

### 10.4 Dependency and package recommendations

- **VERIFIED**: Keep current `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server`, and `spring-security-test`; do not add JJWT or another token library by default.
- **PROPOSED**: Add a small `com.humanit.challenge.security` area for security configuration, token issue support and security error handlers; use `controller` for the login resource and a focused `dto.auth` package for request/response records. Reuse `config.AppJwtProperties` and the existing dev profile properties; avoid a general identity abstraction, custom filter, database user repository or placeholder service.
- **PENDING VERIFICATION**: Confirm exact class APIs and test-context startup with the dependency versions at implementation time; this design report did not run Maven or inspect a dependency tree.

### 10.5 Required plan reconciliation and update status

- **VERIFIED**: The Phase 3 plan currently has four tasks T3.1–T3.4; the proposed T3.1–T3.5 breakdown above is not currently present in `implementation-plan.md`.
- **VERIFIED**: The direct request asked for the plan to be updated, but the Architect role is prohibited from modifying planning documents. Consequently, the plan remains unchanged and this requested deliverable is **blocked for this role**.
- **PROPOSED**: An authorized plan editor should replace only Phase 3's task breakdown and acceptance criteria with Sections 10.2–10.3, clarify the chosen login route, remove the role/authority baseline, and explicitly resolve production account provisioning. Keep completed Phase 1/Phase 2 sections and all non-Phase-3 content unchanged. Do not label this proposal agreed until the Tech Lead approves it.
- **PENDING VERIFICATION**: The Tech Lead must approve the architecture and authorize a separate plan edit and implementation tasks. No approval was provided or implied by this report.

## 11. Final status

**ARCHITECTURE PROPOSAL DELIVERED; IMPLEMENTATION-PLAN UPDATE BLOCKED BY ROLE RESTRICTION.** The plan's Phase 3 requirements and conflicts are recorded, and a minimal architecture, proposed T3.1–T3.5 breakdown, dependencies/migration recommendation and test criteria are provided. No code, tests or plan were changed. No command/test result, approval or acceptance is claimed. Stop after this report as requested.

## 12. Outstanding issues

| ID | Status | Description / owner |
|---|---|---|
| O3-01 | **PENDING VERIFICATION** | Human Tech Lead to resolve the F3-01 conflict: remove the plan's role/authority model baseline in favor of one authenticated principal with no roles. |
| O3-02 | **PENDING VERIFICATION** | Human Tech Lead to choose the production credential provisioning source. Current plan provides only a dev demo user; production needs a way to obtain login JWTs without demo-account provisioning. |
| O3-03 | **PENDING VERIFICATION** | Human Tech Lead to confirm `/auth/login` (literal current plan) or approve `/api/auth/login` (consistent with current route prefix). |
| O3-04 | **BLOCKED** | `implementation-plan.md` update was not made because the Architect may not modify planning documents. Obtain an authorized plan editor/Tech Lead action; preserve completed phases. |
| O3-05 | **PENDING VERIFICATION** | Verify 403 criterion disposition under no-role authorization and identify whether a handler-only test is acceptable. |
| O3-06 | **PENDING VERIFICATION** | Confirm current dependencies expose compatible Nimbus JWT encoder/decoder APIs during authorized implementation. |
| O3-07 | **PENDING VERIFICATION** | Implementation, automated verification, independent Reviewer audit and human Tech Lead acceptance remain outstanding; no Phase 3 work was implemented by this execution. |

## 13. Handoff to next agent

- **VERIFIED**: Report path: `agent-reports/T3.1/architect-proposal.md`.
- **VERIFIED**: No implementation plan edit, application code, test, dependency, configuration or migration was changed.
- **PENDING VERIFICATION**: Human Tech Lead should decide O3-01 through O3-03 and the 403 criterion, then authorize the separate Phase 3 plan update. The proposal is not an approved decision.
- **PENDING VERIFICATION**: After authorization, implement only the agreed small task at a time, preserving Phase 2 behavior. Tester verifies independently before reading Developer conclusions; Reviewer audits independently before reading Developer/Tester conclusions or this proposal.
- **VERIFIED**: Final acceptance remains solely with the human Tech Lead. This execution stops after reporting.
