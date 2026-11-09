# Architect Proposal — T1.5 Cross-cutting Application Scaffolding

## 1. Task ID

**T1.5 — Cross-cutting application scaffolding** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 11.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.5-EXEC-001-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None (first T1.5 Architect execution) |
| Predecessor archives | None required (no prior `agent-reports/T1.5/architect-proposal.md`) |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot software architect)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/architect.md` — `edit` allow for `agent-reports/**` only; `bash: deny` (VERIFIED)

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 and `implementation-plan.md` T1.5:

**In scope:**
1. RFC 9457 Problem Details infrastructure.
2. Global `@RestControllerAdvice`.
3. Consistent validation-error mapping.
4. Springdoc/OpenAPI bean configuration.
5. Standard logging baseline.
6. Focused infrastructure tests.

**Out of scope:**
- Business controllers and endpoints.
- Client or document entities.
- Business-specific validation.
- JWT authentication or enforcement.
- New persistence functionality.
- Docker or deployment infrastructure.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format, evidence markers, history convention |
| `agent-reports/decisions.md` (D-07) | VERIFIED | T1.4 accepted; T1.5 not yet authorized |
| `implementation-plan.md` (T1.5, lines 134–146) | VERIFIED | Task scope and acceptance criteria |
| `requirements-decisions.md` (Sections 11, 14, 16, 19) | VERIFIED | RFC 9457, springdoc code-first, error taxonomy, technical baseline |
| `pom.xml` | VERIFIED | Spring Web, Validation, Security, springdoc 2.10.0 already present; no new dependency needed |
| `src/main/resources/application.yml` | VERIFIED | Common config; logging levels per profile |
| `src/main/resources/application-dev.yml` | VERIFIED | `logging.level.com.humanit.challenge: DEBUG` |
| `src/main/resources/application-prod.yml` | VERIFIED | `logging.level.root: WARN`, `logging.level.com.humanit.challenge: INFO` |
| `src/main/java/com/humanit/challenge/HumanItChallengeApplication.java` | VERIFIED | Base package `com.humanit.challenge` |
| `src/main/java/com/humanit/challenge/config/*` | VERIFIED | Existing config pattern (records, `@Profile`, validators) |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | Test harness conventions |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | VERIFIED | Source-scan audit pattern (no test endpoints in production source) |
| `agent-reports/T1.4/architect-proposal.md` | VERIFIED | Prior task proposal style and boundaries |
| `agent-reports/T1.4/reviewer-report.md` | VERIFIED | Minor gaps F-R01…F-R04 (non-blocking) |

## 6. Files changed

| File | Change |
|---|---|
| `agent-reports/T1.5/architect-proposal.md` | **Created** — this proposal |

No application code, YAML, `pom.xml`, tests, requirements, planning documents, or Git state were modified. The proposed implementation changes are listed in Section 10.1 for later authorized execution.

## 7. Commands executed

**None.** `bash` is denied for this agent. No build or test command was run; all facts are taken from inspected files.

## 8. Evidence and observed results

- **VERIFIED:** Spring Boot 3.5.16 (Spring Framework 6.2.x) provides native `org.springframework.http.ProblemDetail` (RFC 9457) and `ErrorResponse`/`ErrorResponseException` abstractions, plus `MediaType.APPLICATION_PROBLEM_JSON`. No extra dependency is required.
- **VERIFIED:** `spring-boot-starter-validation` provides `jakarta.validation` and Spring's `MethodArgumentNotValidException`/`ConstraintViolationException` mapping hooks.
- **VERIFIED:** `springdoc-openapi-starter-webmvc-ui` 2.10.0 is on the classpath; springdoc auto-configures an `OpenAPI` bean when none is provided, so defining one is an override, not a new dependency.
- **VERIFIED:** No `@RestControllerAdvice`, Problem Details, OpenAPI bean or logback configuration exists today (`reports/task-status-report.md` line 58: "NO EVIDENCE OF ACTIVITY").
- **VERIFIED:** Logging levels already differ by profile (`DEBUG` dev, `WARN`/`INFO` prod); a common console pattern is absent.
- **RECONSTRUCTED:** With Spring Security on the classpath but no `SecurityFilterChain` bean, the default security posture applies; infra tests will disable filters to isolate the handler, as is standard for `@WebMvcTest` slices.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-01 | Minor | No cross-cutting error infrastructure exists; every future controller would otherwise improvise error formats. |
| F-02 | Minor | Validation failures have no structured field-error representation today. |
| F-03 | Info | Springdoc is initialized but has no application-level `OpenAPI` metadata bean. |
| F-04 | Info | No common logging pattern; console output relies on Spring Boot defaults. |
| F-05 | Info | No custom exception hierarchy exists and none is needed (see analysis B). |

No Critical or Major findings.

## 10. Decisions and reasoning (alternatives, trade-offs, assumptions)

### Analysis A — Existing dependencies and infrastructure

All required building blocks are already present from T1.2/T1.3/T1.4; **no `pom.xml` change is proposed**.

| Capability | Source | Notes |
|---|---|---|
| Web MVC, `@RestControllerAdvice`, `ProblemDetail`, `MediaType.APPLICATION_PROBLEM_JSON` | `spring-boot-starter-web` (Spring 6.2) | Native RFC 9457 support |
| Bean Validation, `MethodArgumentNotValidException` | `spring-boot-starter-validation` | Jakarta Validation 3.x |
| `ConstraintViolationException` (method-level) | Hibernate Validator (transitive) | For `@Validated` controller params |
| springdoc `OpenAPI`/`Info` | `springdoc-openapi-starter-webmvc-ui` 2.10.0 | Bean override of auto-config |
| SLF4J logging | `spring-boot-starter` | Logback default backend |
| Security exceptions (optional mapping) | `spring-boot-starter-security` | Mapping only; no enforcement added |

The existing package convention is `com.humanit.challenge.<area>` (`config`, `config.validation`). Cross-cutting web/error code will live under `com.humanit.challenge.exception` and `com.humanit.challenge.config`, matching that convention. No new top-level package is introduced.

### Analysis B — Proposed exception-handler design

**Decision: do not introduce a custom exception hierarchy.** The approved error taxonomy (requirements-decisions.md Section 11) maps HTTP statuses to *situations*, not to exception types. Spring and the future business layer can throw standard exceptions (`ResponseStatusException`, `DataIntegrityViolationException`, `OptimisticLockingFailureException`, `EntityNotFoundException`-style `ResponseStatusException`, etc.), and the advice maps them to statuses. A custom hierarchy would add indirection without value at this stage and would have to be revisited in Phase 2 anyway. This is explicitly consistent with the task rule "Do not introduce custom exception hierarchies unless clearly justified."

Design:

- `@RestControllerAdvice` (base package `com.humanit.challenge`) producing `application/problem+json`.
- One `@ExceptionHandler` per situation, each returning `ResponseEntity<ProblemDetail>` with an explicit status, so the status code is unambiguous regardless of annotation processing.
- A small private helper builds a `ProblemDetail` with `title`, `detail`, `instance` (request path) and optional `properties.errors`.
- Sanitization: `detail` is either a fixed, safe message (for 5xx, malformed body, security failures) or a structured validation description (field + message, where messages are our own validation messages, never user input). Exception messages from unexpected exceptions are **never** copied to the response; they are logged server-side only.
- `type` defaults to `about:blank` (Spring default). No custom type URIs are introduced in T1.5; Phase 4 may refine them. This keeps the baseline minimal.

### Analysis C — HTTP error response structure

All error responses follow RFC 9457 with these fields:

| Field | Source | Notes |
|---|---|---|
| `type` | `about:blank` (default) | Phase 4 may replace with stable problem URIs |
| `title` | Short, fixed, human-readable | e.g. "Validation failed", "Bad request" |
| `status` | HTTP status code | Mirrors response status |
| `detail` | Safe message | Generic for 5xx/security; specific for validation |
| `instance` | Request URI | `HttpServletRequest.getRequestURI()` |
| `errors` (property) | List of `{field, message}` | Only for 400 validation failures |

Content type is `application/problem+json` (`MediaType.APPLICATION_PROBLEM_JSON`). No stack trace, exception class name, SQL state, or internal message appears in any 4xx/5xx body. `rejectedValue` is deliberately omitted from validation errors to avoid leaking submitted (possibly sensitive) input.

### Analysis D — Bean Validation error representation

Two validation entry points are covered:

1. `MethodArgumentNotValidException` (invalid `@RequestBody`): iterate `BindingResult.getFieldErrors()`, build `errors` list of `{field, message}`. Duplicate field entries are preserved (one per constraint) so callers see all failures.
2. `ConstraintViolationException` (method-level `@Validated` parameters): iterate violations, build `errors` list of `{field = propertyPath, message}`.

Both map to HTTP 400. `HttpMessageNotReadableException` (malformed JSON / type mismatch at parse time) maps to 400 with a generic detail ("Malformed or unreadable request body.") and no `errors` list. `MethodArgumentTypeMismatchException` maps to 400 with a generic detail naming only the parameter name (not the rejected value).

### Analysis E — Safe handling of unexpected exceptions

- A catch-all `@ExceptionHandler(Exception.class)` returns HTTP 500 with a fixed detail ("An unexpected error occurred.") and no exception details.
- The exception (including stack trace) is logged at `ERROR` level server-side via SLF4J. This is operational diagnostics, not response exposure.
- `DataIntegrityViolationException` and `OptimisticLockingFailureException` are mapped to 409 with a generic detail ("The request conflicts with the current state of the resource."), because these are predictable future outcomes (uniqueness/locking) and should not surface as 500. This prepares the handler for Phase 2 without adding business logic.
- Security exceptions are mapped for completeness and predictability: `AuthenticationException` → 401, `AccessDeniedException` → 403, both with generic details. These mappings only take effect for exceptions thrown within the controller layer; filter-level security failures are handled by Spring Security's own entry points in Phase 3.

No internal message, class name, or SQL detail is ever copied to a response.

### Analysis F — Springdoc/OpenAPI configuration

**Decision: minimal `OpenAPI` bean only.** Define `com.humanit.challenge.config.OpenApiConfig` with a single `@Bean OpenAPI` carrying `Info` (title, version, description). This satisfies "OpenAPI is initialized" and gives springdoc stable metadata without pulling in business/endpoint documentation, which is Phase 4 (T4.1).

- No security scheme is registered here. JWT is Phase 3; adding a bearer scheme now would be speculative and would have to be re-verified against the actual security filter chain. Phase 4 (T4.1) owns "Authentication is represented in OpenAPI."
- No `GroupedOpenApi` beans: there is only one API group at this stage.
- No changes to `springdoc.*` properties are proposed; defaults (`/v3/api-docs`, `/swagger-ui.html`) remain. Production visibility hardening stays deferred (T1.2 reviewer R-F03 / T4.x), as previously decided.
- The bean overrides springdoc's auto-configured `OpenAPI`; if the bean is removed, springdoc falls back to its default, so the change is low-risk and reversible.

### Analysis G — Logging configuration and sensitive data

Baseline approach (minimal, no new framework):

- Keep profile-specific levels as they are (`DEBUG` dev, `WARN`/`INFO` prod). No change to those values.
- Add a single common console pattern in `application.yml` so that local and container logs have a consistent, readable format: timestamp, level, thread, logger, message. This is a property change, not a new logging framework.
- No `logback-spring.xml` is introduced in T1.5. Spring Boot's default Logback configuration plus the common pattern property is sufficient and avoids duplicating profile logic in XML. If structured/JSON logging or file appenders are required (Phase 5 containerization), that is a later, separately approved change.
- Sensitive-data rules:
  - JWT secret, datasource credentials and demo-user credentials are never logged. The existing `JwtSecretValidator` already follows this (messages name the property and failure kind only).
  - The global handler logs only the exception type and stack trace for unexpected errors; it never logs request bodies, headers (which may carry tokens) or validation rejected values.
  - No MDC/correlation-ID infrastructure is introduced (out of scope: "Avoid excessive logging infrastructure").

### Analysis H — Test scenarios and acceptance criteria

Focused infrastructure tests only (no business endpoints):

1. **Validation failure (400):** POST a body failing `@NotBlank`/`@Email` to a test-only endpoint → response status 400, content type `application/problem+json`, body `status=400`, `title="Validation failed"`, `errors` list contains `{field, message}` for each failing field.
2. **Malformed JSON (400):** POST invalid JSON → 400, generic detail, no `errors`.
3. **Type mismatch (400):** GET with a non-numeric path variable where a number is expected → 400, generic detail naming only the parameter.
4. **Method not allowed (405):** wrong HTTP method → 405 Problem Details.
5. **Not found (404):** unknown path handled by the advice → 404 Problem Details (where Spring routes `NoResourceFoundException` through the dispatcher).
6. **Unexpected exception (500):** test-only endpoint throws `RuntimeException` → 500, fixed detail, and the response body does not contain the exception message, class name or stack trace.
7. **Security mapping (401/403):** controller-layer `AuthenticationException`/`AccessDeniedException` → 401/403 Problem Details (unit-level or MockMvc with a throwing test controller).
8. **Conflict mapping (409):** test-only endpoint throws `DataIntegrityViolationException` / `OptimisticLockingFailureException` → 409 Problem Details.
9. **OpenAPI bean:** `@SpringBootTest` context contains a non-null `OpenAPI` bean; `/v3/api-docs` group is registered (bean-level assertion preferred over HTTP to avoid security-filter complications).
10. **No test endpoints in production source:** extend the source-scan audit pattern (as in `SourceScanAuditTest`) to assert no `@RestController` test scaffolding or `__test` paths exist under `src/main`.

Acceptance criteria mapping (T1.5):

| Acceptance criterion | Covered by |
|---|---|
| Problem Details format available for all future controllers | `GlobalExceptionHandler` + helper, tests 1–8 |
| OpenAPI initialized | `OpenApiConfig` bean + test 9 |
| Error responses consistent | Single advice + helper + tests 1–8 |
| Problem Details tested without production business endpoints | Test-only controller in `src/test`, tests 1–8 |
| No stack-trace leakage / no sensitive details | Sanitization rules + test 6 |
| Predictable HTTP status codes | Explicit status per handler + tests |

### Analysis I — Exact files to create or modify

**Create:**
- `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` — `@RestControllerAdvice`, handlers for validation, malformed body, type mismatch, not found, method not allowed, conflict, security, catch-all.
- `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` — `@Configuration` with `@Bean OpenAPI`.
- `src/test/java/com/humanit/challenge/exception/GlobalExceptionHandlerTest.java` — MockMvc-based scenarios 1–8.
- `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java` — context/OpenAPI bean assertion.
- `src/test/java/com/humanit/challenge/exception/TestErrorController.java` — `@RestController` under `src/test`, mapped to a test-only path (e.g. `/__test/errors`), used by the MockMvc tests. Must never be under `src/main`.

**Modify:**
- `src/main/resources/application.yml` — add a common `logging.pattern.console` (levels remain in profile files).

**No change:**
- `pom.xml` (all dependencies already present).
- `application-dev.yml` / `application-prod.yml` levels (unchanged).
- No entity, repository, business controller, security filter chain, Flyway migration, or Docker file.

### Analysis J — Potential risks and unnecessary complexity

| Risk | Mitigation |
|---|---|
| Catch-all `Exception` handler could convert Spring Security filter exceptions into 500 if they ever reach the dispatcher | Security failures normally occur in filters before the controller; mappings are included for controller-layer completeness only. Phase 3 will add proper entry points. |
| `NoResourceFoundException` handling may duplicate Spring's default error page behavior | Handler is explicit and returns Problem Details; tests verify format. If Spring does not route it through the dispatcher in some configurations, the default error controller still returns a Problem Details body under Boot 3.5. |
| OpenAPI bean could drift from springdoc's expectations | Bean is a plain `OpenAPI` override; springdoc merges it. Reversible by deleting the bean. |
| Validation `errors` list could grow large | Acceptable for T1.5; pagination of errors is not required. |
| Adding conflict/security mappings "early" might look like business logic | They are pure status mapping with generic details; no business rules or persistence calls are introduced. |
| Test-only controller accidentally shipped in production | Placed under `src/test`; source-scan audit test asserts absence from `src/main`. |
| Logging pattern change could affect existing profile tests | Those tests read `logging.level.*` properties, not the pattern; no impact expected (PENDING VERIFICATION). |

Risks are acceptable and low. No Critical or Major risks identified.

### Proposed implementation (developer-facing, after approval)

1. Create `GlobalExceptionHandler` with the handlers and a private `buildProblem(...)` helper; use `ProblemDetail.forStatusAndDetail(...)` or `ProblemDetail.forStatus(...)` plus `setProperty`.
2. Create `OpenApiConfig` with the `OpenAPI` bean.
3. Add `logging.pattern.console` to common `application.yml`.
4. Add test-only controller and MockMvc tests in `src/test`.
5. Add OpenAPI bean test and source-scan assertion.
6. Run `.\mvnw.cmd clean test` and `.\mvnw.cmd clean verify`; record results.

### Test strategy

- Use `@WebMvcTest` with `addFilters = false` for handler slices, plus a nested/static test-only controller in `src/test`.
- Use `@SpringBootTest` with `StartupTestHarness.supplyDevExternals` for the OpenAPI bean test.
- Assert content type `application/problem+json`, status, and JSON fields via `jsonPath`.
- Assert the 500 response does not contain the exception message/class/stack trace.
- Keep tests deterministic and free of PostgreSQL/H2-specific behavior (no persistence involved).
- Tester verifies independently before reading the Developer report (per reporting rules).

### Acceptance criteria mapping

(See Analysis H table above.)

### Design decisions requiring Tech Lead approval

1. **Catch-all `Exception` → 500 plus conflict/security mappings (409/401/403) in the same advice.** Alternative: handle only validation/malformed/not-found and let Spring's default error handling cover the rest. Proposed mappings improve predictability for Phase 2/3 but add early surface area.
2. **Validation errors as `errors: [{field, message}]` with no `rejectedValue`.** Alternative: include `rejectedValue`. Omitted to avoid leaking submitted (possibly sensitive) input; Phase 4 can revisit.
3. **No custom exception hierarchy.** Alternative: introduce `ApiException` base class. Rejected as unnecessary complexity; can be added in Phase 2 if business code needs it.
4. **OpenAPI bean with `Info` only (no security scheme, no groups).** Alternative: register a bearer security scheme now. Deferred to Phase 3/4 to avoid speculative configuration.
5. **Logging via common `logging.pattern.console` property only; no `logback-spring.xml`.** Alternative: introduce a Logback config file. Proposed property-only approach is smaller and reuses profile levels.
6. **`type` defaults to `about:blank`.** Alternative: mint problem-type URIs now. Deferred to Phase 4 (T4.2 contract consistency).

## 11. Final status

**DELIVERED — PROPOSAL PERSISTED, PENDING TECH LEAD APPROVAL.**

This proposal is design-only. No implementation was performed, no application code or configuration was modified, and no Git operations were executed. Approval and implementation authorization remain with the human Tech Lead.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Awaiting approval | Explicit Tech Lead approval of this T1.5 proposal before implementation | Tech Lead |
| OT-02 | Awaiting authorization | Create `GlobalExceptionHandler` and `OpenApiConfig` | Developer (after authorization) |
| OT-03 | Awaiting authorization | Add common logging pattern to `application.yml` | Developer (after authorization) |
| OT-04 | Awaiting authorization | Add test-only controller and infrastructure tests | Developer (after authorization) |
| OT-05 | PENDING VERIFICATION | Verify RFC 9457 responses and sanitization via tests | Tester |
| OT-06 | PENDING VERIFICATION | Verify OpenAPI bean and source-scan (no test endpoints in main) | Tester |
| OT-07 | Deferred | Problem-type URIs, security scheme in OpenAPI, structured logging | Phase 3/4/5 |

## 13. Handoff to next agent

- **To the Tech Lead:** review and approve or reject this proposal; authorize implementation only after approval.
- **To the Developer:** implement only the approved scope in Analysis I; do not add entities, business endpoints, JWT enforcement, persistence or Docker changes.
- **To the Tester:** verify independently before reading the Developer report, using the scenarios in Analysis H.
- **To the Reviewer:** audit sanitization (no stack traces/secrets), status-code predictability, scope compliance and absence of test endpoints in production source.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts only. All 13 mandatory sections present.