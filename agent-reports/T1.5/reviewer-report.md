# Reviewer Report — T1.5 Cross-cutting Application Scaffolding

## 1. Task ID

**T1.5 — Cross-cutting application scaffolding** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 11.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.5-EXEC-004-REV-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Reviewer model | `opencode/beam-free` |
| Architect proposal reviewed | `agent-reports/T1.5/architect-proposal.md` (`T1.5-EXEC-001-ARCH-01`) |
| Developer report reviewed | `agent-reports/T1.5/developer-report.md` (`T1.5-EXEC-002-DEV-01`) |
| Tester report reviewed | `agent-reports/T1.5/tester-report.md` (`T1.5-EXEC-003-TST-01`) |
| Independent inspection completed before reading Developer/Tester reports | Yes (see Section 5) |

## 3. Agent role and model

- **Role:** Reviewer (senior Java and Spring Boot code reviewer)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/reviewer.md`

## 4. Authorized scope

Independent audit of T1.5 only. No application code, tests, configuration, `pom.xml`, requirements, planning documents, Git state, other agents' reports or `decisions.md` were modified. Final acceptance is not recorded on behalf of the Tech Lead.

Scope audited:
1. RFC 9457 response consistency.
2. Correct HTTP status preservation.
3. Validation error handling.
4. Sanitization of unexpected exceptions.
5. `ResponseStatusException` reason exposure.
6. OpenAPI configuration.
7. Logging and sensitive-data handling.
8. Test coverage and isolation.
9. Absence of production test endpoints.
10. Scope compliance and unnecessary complexity.

## 5. Files inspected

Independent inspection (before reading Developer/Tester reports):
- `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` (166 lines)
- `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` (25 lines)
- `src/main/resources/application.yml`
- `src/test/java/com/humanit/challenge/exception/GlobalExceptionHandlerTest.java`
- `src/test/java/com/humanit/challenge/exception/TestErrorController.java`
- `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java`
- `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java`

Planning/requirements/context (read before independent inspection):
- `agent-reports/README.md`
- `implementation-plan.md`
- `requirements-decisions.md`

Post-inspection (reports read afterward):
- `agent-reports/T1.5/architect-proposal.md`
- `agent-reports/T1.5/developer-report.md`
- `agent-reports/T1.5/tester-report.md`
- `agent-reports/decisions.md`

Git evidence (VERIFIED):
- `git status --short`
- `git diff HEAD -- src/main/resources/application.yml src/test/.../SourceScanAuditTest.java`
- `git ls-files --others --exclude-standard`

## 6. Files changed

None by this execution, except the creation of this report (`agent-reports/T1.5/reviewer-report.md`). No application code, tests, configuration, `pom.xml`, requirements, planning documents or Git state were modified.

## 7. Commands executed

| Command | Result | Evidence |
|---|---|---|
| `git status --short` | 2 modified + 7 untracked files, all T1.5-scoped | VERIFIED |
| `git diff HEAD -- src/main/resources/application.yml src/test/.../SourceScanAuditTest.java` | Only logging pattern added; only source-scan assertion added | VERIFIED |
| `git ls-files --others --exclude-standard` | 7 untracked T1.5 files (incl. new main/test sources and agent reports) | VERIFIED |

No build/test command was executed by this Reviewer run (`bash` is restricted to read-only Git commands). Build/test outcomes are taken from the Tester report (`T1.5-EXEC-003-TST-01`) and corroborated by source inspection, and are marked accordingly in Section 8.

## 8. Evidence and observed results

### 8.1 RFC 9457 response consistency

VERIFIED by source inspection of `GlobalExceptionHandler`:
- Extends `ResponseEntityExceptionHandler`; overrides framework handlers for validation, malformed body, type mismatch, not found and method not allowed.
- Uses `org.springframework.http.ProblemDetail` via `ProblemDetail.forStatusAndDetail(status, detail)`.
- Sets `title`, `status` (via factory), `detail` and `instance` (request path via `extractPath`).
- `type` is left at Spring's default `about:blank` (no `setType` call).
- Content type is `application/problem+json` (`MediaType.APPLICATION_PROBLEM_JSON`) on every path.
- Validation failures add an `errors` array of `{field, message}`; malformed body/type mismatch have no `errors`.
- Catch-all returns a fixed detail `"An unexpected error occurred."`.

### 8.2 Correct HTTP status preservation

VERIFIED by source inspection and Tester report:
- `handleMethodArgumentNotValid` → 400 (PASS, `invalidBodyReturns400ProblemDetails`).
- `handleHttpMessageNotReadable` → 400 (PASS, `malformedJsonReturns400`).
- `handleTypeMismatch` → 400 (PASS, `typeMismatchReturns400`).
- `handleNoResourceFoundException` → 404 (PASS, `unknownRouteReturns404`).
- `handleHttpRequestMethodNotSupported` → 405 (PASS, `methodNotAllowedReturns405`).
- `ConstraintViolationException` → 400 (PASS, `constraintViolationReturns400`).
- `ResponseStatusException` → preserved status (PASS, `responseStatusPreservesStatus`, 404).
- Catch-all `Exception` → 500 (PASS, `unexpectedExceptionReturns500Sanitized`).
- No 4xx is collapsed to 500 because the advice extends `ResponseEntityExceptionHandler` and the catch-all only matches otherwise-unhandled exceptions.

### 8.3 Validation error handling

VERIFIED by source inspection:
- `MethodArgumentNotValidException`: one `{field, message}` entry per `FieldError`; `rejectedValue` is not included (sanitization).
- `ConstraintViolationException`: one entry per violation with `field = propertyPath`.
- Malformed JSON and type mismatch use generic details; type mismatch names only the parameter name.
- Coverage gap (Minor): only `FieldError`s are mapped; global/object-level errors are not represented (see R-F03).

### 8.4 Sanitization of unexpected exceptions

VERIFIED by source inspection and Tester report (`unexpectedExceptionReturns500Sanitized`, PASS):
- Catch-all returns fixed detail `"An unexpected error occurred."`; body contains neither `"secret internal message"`, `"RuntimeException"` nor a stack trace.
- The exception (with stack trace) is logged server-side only via SLF4J at `ERROR` level.
- No request bodies, headers or rejected values are logged or echoed.

### 8.5 `ResponseStatusException` reason exposure

VERIFIED by source inspection (lines 140–155):
- The handler reads `ex.getReason()` and, when non-blank, copies it verbatim into the Problem Details `detail`.
- The response status is taken from `ex.getStatusCode()`.
- Current sole caller: `TestErrorController.throwResponseStatus()` supplies `"Resource not found"` (safe). A project-wide search found no other `ResponseStatusException` construction in `src` (VERIFIED by Tester report Section 8.10).
- No concrete sensitive leak exists in the current code. The exposure channel is caller-dependent (future-caller risk). See R-F01.

### 8.6 OpenAPI configuration

VERIFIED by source inspection and `OpenApiConfigTest` (PASS):
- `OpenApiConfig` defines a single `@Bean OpenAPI` with `Info` (title `"Human IT Challenge API"`, version `"0.0.1-SNAPSHOT"`, description).
- No security scheme or groups are registered (deferred by design to Phase 3/4).
- The bean overrides springdoc's auto-configured `OpenAPI`; removing it falls back to the default.

### 8.7 Logging and sensitive-data handling

VERIFIED by source inspection and git diff:
- `application.yml` now contains `logging.pattern.console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n"`.
- Profile levels unchanged (`DEBUG` dev, `WARN`/`INFO` prod) in `application-dev.yml`/`application-prod.yml`.
- Application context loads successfully (`HumanItChallengeApplicationTests` PASS).
- No JWT secret, datasource credentials or demo-user credentials are logged; the handler logs only the exception type and stack trace for unexpected errors.

### 8.8 Test coverage and isolation

VERIFIED via Tester report (independent `.\mvnw.cmd clean verify`, BUILD SUCCESS, 41 tests, 0 failures, 0 errors, 0 skipped; Java 21.0.12.1; Maven Enforcer passed):
- `GlobalExceptionHandlerTest`: 8 scenarios (validation, malformed JSON, type mismatch, method not allowed, unknown route, unexpected exception, `ResponseStatusException`, constraint violation).
- `OpenApiConfigTest`: 1 (bean + metadata).
- `SourceScanAuditTest`: 2 (profile mechanism + no test scaffolding in production source).
- `HumanItChallengeApplicationTests`: 1.
- Tests are isolated (`@WebMvcTest` with `addFilters = false`); no persistence or business endpoints involved.

Coverage gaps (non-blocking): no explicit assertion for `type == about:blank` or absence of `rejectedValue`; no assertion for `ResponseStatusException` detail. See R-F05.

### 8.9 Absence of production test endpoints

VERIFIED by `SourceScanAuditTest` (2 tests, PASS) and git status:
- No path under `src/main` contains `__test`.
- No `.java` file under `src/main/java` contains `TestErrorController` or `__test`.
- `TestErrorController` resides only under `src/test`.

### 8.10 Scope compliance and unnecessary complexity

VERIFIED by git status/diff:
- Modified: `src/main/resources/application.yml`, `src/test/.../SourceScanAuditTest.java`.
- New: `GlobalExceptionHandler.java`, `OpenApiConfig.java`, `GlobalExceptionHandlerTest.java`, `OpenApiConfigTest.java`, `TestErrorController.java` (plus agent reports).
- No changes to `pom.xml`, entities, repositories, business controllers, security configuration, Flyway migrations, Docker files or profile levels.
- Implementation is minimal and aligned with T1.5 scope. No unnecessary complexity introduced.

## 9. Findings and severity

| ID | Severity | Finding | Blocking? |
|---|---|---|---|
| R-F01 | Minor | `ResponseStatusException` reason is propagated verbatim into the Problem Details `detail`. No concrete leak in current code (sole caller is safe and no other callers exist), but the channel is only as safe as future callers. | No |
| R-F02 | Minor | For a `ResponseStatusException` carrying a non-standard HTTP status, `HttpStatus.resolve(...)` returns `null` and the handler falls back to `INTERNAL_SERVER_ERROR` for the body `status`, while the HTTP response keeps the original status code — an inconsistent body/response status. | No |
| R-F03 | Minor | Validation handling maps only `FieldError`s; global/object-level constraint violations are not represented, so a class-level constraint would produce a 400 with an empty `errors` list. | No |
| R-F04 | Minor | Deferred items (409 conflict handlers, 401/403 mappings, problem-type URIs, bearer OpenAPI scheme, structured logging) are not implemented. Acceptable for scaffolding, but must be in place before Phase 2/3 business code triggers them. | No |
| R-F05 | Minor | Test coverage gaps: no explicit assertion for `type == about:blank`, absence of `rejectedValue`, or `ResponseStatusException` detail behavior. Code inspection confirms these properties. | No |
| R-F06 | Info | Governance: no `D-08` entry records T1.5 authorization/completion approval in `decisions.md` (T1.5 was marked "NOT yet authorized" in D-07). Final acceptance remains with the Tech Lead. | No |
| R-F07 | Info | Process/audit note: the developer's first-run compilation failure was not independently inspected by the Tester; the final `clean verify` is VERIFIED. | No |

Critical findings: none.
Major findings: none.
Blocking defects: none.

## 10. Decisions and reasoning

- The implementation correctly extends `ResponseEntityExceptionHandler`, preserving framework HTTP semantics (400/404/405) instead of collapsing them into 500. This matches the Tech Lead adjustment A recorded by the Developer.
- Sanitization is sound for the unexpected path: fixed detail, server-side-only logging, no rejected values, no stack traces in the body. This satisfies the T1.5 acceptance criterion "no stack-trace leakage and no sensitive details".
- The deferral of 409/401/403 handlers and problem-type URIs deviates from the architect's original proposal but is consistent with the absence of business/security code that could trigger them and with the Developer's recorded adjustments. It is accepted as in-scope for T1.5 scaffolding; the deferred work is tracked in Section 12 (OT-01…OT-03).
- On `ResponseStatusException` (R-F01): there is no concrete leak today. The risk is a future-caller contract issue, not an observed defect. The smallest reasonable mitigation is to keep the current propagation (standard Spring behavior, which preserves meaningful messages such as "Client not found") and (a) document that reasons must be client-safe, and (b) add a regression test locking the current safe caller. A heavier mitigation (stripping all reasons to the status reason phrase) would discard useful, safe messages and is not justified by a concrete leak. If the Tech Lead prefers defense-in-depth, the handler could log the supplied reason at `DEBUG` and return the status reason phrase; this is optional and non-blocking.
- R-F02 is a robustness edge case (non-standard status codes) and does not affect any current caller. Recommended fix is to use `ProblemDetail.forStatusAndDetail(statusCode, detail)` (or `statusCode.value()`) instead of the resolved `HttpStatus` enum, so the body status always mirrors the response status.
- R-F03 is forward-looking (no business DTOs yet). Recommended fix before Phase 2: map `getAllErrors()` and derive a field name when present, otherwise a global marker.
- R-F04 must be closed before Phase 2 (409) and Phase 3 (401/403) business code is introduced; otherwise those cases would surface as 500.
- No custom exception hierarchy was introduced, matching the approved decision (architect Analysis B). No unnecessary complexity or out-of-scope changes were found.

## 11. Final status

**REVIEW COMPLETE — PASS (reviewer recommendation).** No Critical or Major findings and no blocking defects. Implementation satisfies the T1.5 acceptance criteria (Problem Details format available for future controllers, OpenAPI initialized, consistent error responses, tested without production business endpoints, no stack-trace/sensitive-detail leakage, predictable HTTP status codes). Final acceptance remains with the human Tech Lead; no approval is recorded by this report.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Deferred | 409 conflict handlers (uniqueness/optimistic locking) | Phase 2 (T2.3/T2.4/T2.6) |
| OT-02 | Deferred | 401/403 mappings and filter-level handling | Phase 3 (T3.1/T3.3) |
| OT-03 | Deferred | Problem-type URIs, bearer security scheme in OpenAPI, structured logging | Phase 4/5 |
| OT-04 | Hardening (optional) | Document/enforce that `ResponseStatusException` reasons must be client-safe; optionally log the reason server-side and return the status reason phrase (R-F01) | Tech Lead decision |
| OT-05 | Hardening (optional) | Make `ResponseStatusException` body status mirror non-standard status codes (R-F02) | Developer (Phase 2/3) |
| OT-06 | Hardening (optional) | Map global/object-level validation errors (R-F03) | Developer (Phase 2) |
| OT-07 | Test coverage gap | Add explicit assertions for `type == about:blank`, absence of `rejectedValue`, and `ResponseStatusException` detail (R-F05) | Tester/Reviewer |
| OT-08 | Governance | Record T1.5 authorization/completion approval (D-08) and final Tech Lead acceptance | Tech Lead |

## 13. Handoff to next agent

- **To the Tech Lead:** T1.5 implementation is complete and independently verified (41/41 tests pass, BUILD SUCCESS). Review found 0 Critical, 0 Major and no blocking defects (7 Minor/Info findings, all non-blocking). Final acceptance decision and any optional hardening (OT-04…OT-06) remain with the Tech Lead. No approval is recorded by this report.
- **To the Developer (future phases):** Implement OT-01, OT-02 and OT-03 before business/security code is introduced; consider OT-05 and OT-06 alongside Phase 2 DTOs.
- **To the Tester:** Consider OT-07 if stricter verification of RFC 9457 details is desired.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts, independent source inspection and verified command output only. All 13 mandatory sections present.
