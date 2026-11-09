# Tester Report — T1.5 Cross-cutting Application Scaffolding

## 1. Task ID

**T1.5 — Cross-cutting application scaffolding** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 11.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.5-EXEC-003-TST-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Model | `opencode/beam-free` |
| Predecessor archives | None required (no prior `agent-reports/T1.5/tester-report.md` existed) |
| Architect proposal | `agent-reports/T1.5/architect-proposal.md` (`T1.5-EXEC-001-ARCH-01`) |
| Developer report read | After independent verification only (`agent-reports/T1.5/developer-report.md`, `T1.5-EXEC-002-DEV-01`) |

## 3. Agent role and model

- **Role:** Tester (senior Java and Spring Boot QA engineer)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/tester.md`

## 4. Authorized scope

Independent verification of the T1.5 implementation only. No application code, tests, configuration, `pom.xml`, requirements, planning documents or Git state were modified. No staging, commit or push was performed. Final acceptance is not recorded on behalf of the Tech Lead.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format, evidence markers, history convention |
| `agent-reports/T1.5/architect-proposal.md` | VERIFIED | Approved design, expected files, acceptance criteria |
| `agent-reports/decisions.md` (D-07) | VERIFIED | T1.4 accepted; T1.5 authorized by current tasking |
| `implementation-plan.md` (T1.5, lines 134–146) | VERIFIED | Task scope and acceptance criteria |
| `requirements-decisions.md` (Sections 11, 14, 16, 19) | VERIFIED | RFC 9457, springdoc code-first, error taxonomy, technical baseline |
| `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` | VERIFIED | Handler implementation (166 lines) |
| `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` | VERIFIED | OpenAPI bean |
| `src/main/resources/application.yml` | VERIFIED | Common logging pattern |
| `src/test/java/com/humanit/challenge/exception/GlobalExceptionHandlerTest.java` | VERIFIED | 8 MockMvc scenarios |
| `src/test/java/com/humanit/challenge/exception/TestErrorController.java` | VERIFIED | Test-only controller |
| `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java` | VERIFIED | OpenAPI bean assertion |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | VERIFIED | Source-scan audit (modified) |
| `agent-reports/T1.5/developer-report.md` | VERIFIED | Read only after independent build verification |
| Git status/diff output | VERIFIED | Staged, unstaged and untracked changes |

## 6. Files changed

None. No application code, tests, configuration or Git state were modified by this execution. The only file created by this execution is `agent-reports/T1.5/tester-report.md`. Build artifacts under `target/` are generated output and are not source changes.

## 7. Commands executed

| Command | Result | Evidence |
|---|---|---|
| `git status --short` | 2 modified + 7 untracked files (all T1.5-scoped) | VERIFIED |
| `git diff -- src/main/resources/application.yml` | Only logging pattern added | VERIFIED |
| `git diff -- src/test/.../SourceScanAuditTest.java` | Only `noTestScaffoldingInProductionSource` added | VERIFIED |
| `git diff --cached --stat` | No staged changes | VERIFIED |
| `git ls-files --others --exclude-standard` | 7 untracked T1.5 files | VERIFIED |
| `.\mvnw.cmd clean verify` | Exit code 0; `BUILD SUCCESS`; finished 2026-11-09T16:26:42Z | `C:\Users\pedro\AppData\Local\Temp\opencode\mvn-verify.log` |

Test summary (independent run):

- Tests run: **41**, Failures: 0, Errors: 0, Skipped: 0.
- Java runtime: 21.0.12.1 (VERIFIED in log).
- Maven Enforcer `requireJavaVersion [21,22)` and `requireMavenVersion [3.10.0,)`: passed.

Per-class results (VERIFIED in log):

| Test class | Tests |
|---|---|
| `CombinedDevProdProfilesTest` | 2 |
| `DefaultDevProfileStartupTest` | 3 |
| `DemoUserPropertiesTest` | 2 |
| `ExplicitDevProfileStartupTest` | 1 |
| `JwtSecretValidationTest` | 6 |
| `OpenApiConfigTest` | 1 |
| `PaginationPropertiesValidationTest` | 5 |
| `ProdProfileStartupTest` | 4 |
| `SourceScanAuditTest` | 2 |
| `Base64DecodedMinBytesValidatorTest` | 6 |
| `GlobalExceptionHandlerTest` | 8 |
| `HumanItChallengeApplicationTests` | 1 |
| **Total** | **41** |

## 8. Evidence and observed results

### 8.1 RFC 9457 Problem Details response format

VERIFIED by source inspection of `GlobalExceptionHandler`:
- Extends `ResponseEntityExceptionHandler`.
- Uses `org.springframework.http.ProblemDetail` via `ProblemDetail.forStatusAndDetail(status, detail)`.
- Sets `title`, `status` (via the factory), `detail`, and `instance` (request URI).
- `type` is left at Spring's default `about:blank` (VERIFIED by use of `forStatusAndDetail`; no `setType` call overrides it).

### 8.2 `application/problem+json` content type

VERIFIED by source inspection: every handler returns `ResponseEntity...contentType(MediaType.APPLICATION_PROBLEM_JSON)`. Tests assert `content().contentType(MediaType.APPLICATION_PROBLEM_JSON)` for all 8 scenarios and pass.

### 8.3 Validation errors map to HTTP 400

VERIFIED by `GlobalExceptionHandlerTest.invalidBodyReturns400ProblemDetails` (PASS): POST invalid body → 400, content type `application/problem+json`, `$.status=400`, `$.title="Validation failed"`, `$.errors` is an array. Code builds one `{field, message}` entry per `FieldError` without `rejectedValue`.

### 8.4 Malformed JSON maps to HTTP 400

VERIFIED by `malformedJsonReturns400` (PASS): POST `{bad json` → 400, `$.detail="Malformed or unreadable request body."`, `$.errors` does not exist.

### 8.5 Type mismatch maps to HTTP 400

VERIFIED by `typeMismatchReturns400` (PASS): GET `/__test/errors/type-mismatch/abc` → 400, `$.detail` contains `id`. Code uses only the parameter name (`matm.getName()`), never the rejected value.

### 8.6 Unsupported method maps to HTTP 405

VERIFIED by `methodNotAllowedReturns405` (PASS): GET on a POST-only endpoint → 405, content type `application/problem+json`, `$.status=405`.

### 8.7 Unknown route maps to HTTP 404

VERIFIED by `unknownRouteReturns404` (PASS): GET `/__test/does-not-exist` → 404, content type `application/problem+json`, `$.status=404`. Handler overrides `handleNoResourceFoundException`.

### 8.8 `ResponseStatusException` preserves HTTP status

VERIFIED by `responseStatusPreservesStatus` (PASS): test-only controller throws `ResponseStatusException(NOT_FOUND, "Resource not found")` → response status 404 and `$.status=404`. Code uses `ex.getStatusCode()` for the response status and resolves a `HttpStatus` for the body.

### 8.9 Unexpected exceptions map to HTTP 500

VERIFIED by `unexpectedExceptionReturns500Sanitized` (PASS): GET `/__test/errors/throw-runtime` → 500, `$.detail="An unexpected error occurred."`, and the body contains neither `"secret internal message"` nor `"RuntimeException"`. The exception (with stack trace) is logged server-side only via SLF4J at `ERROR` level (observed in the build log).

### 8.10 No stack traces, internal messages, secrets or rejected values

VERIFIED by combined evidence:
- Catch-all handler returns only the fixed detail `"An unexpected error occurred."` (code + test).
- Validation handler omits `rejectedValue` (code inspection; no `rejectedValue` property is added).
- Type-mismatch handler names only the parameter name (code + test).
- Malformed-body handler returns a generic detail (code + test).
- No production code path logs request bodies, headers or validation rejected values (code inspection of `GlobalExceptionHandler`).
- `ResponseStatusException` detail behavior is assessed in Section 9 (T-F01): the handler propagates `ex.getReason()` verbatim. The only current caller (`TestErrorController`) supplies the safe reason `"Resource not found"`. A project-wide search found no other `ResponseStatusException` construction in `src`. Therefore no concrete sensitive leak exists in the current code; the channel is only as safe as future callers (non-blocking observation).

### 8.11 OpenAPI bean initialization and metadata

VERIFIED by `OpenApiConfigTest` (PASS): `@SpringBootTest` context contains a non-null `OpenAPI` bean; `Info`, title, version and description are all non-blank. `OpenApiConfig` defines a single `@Bean OpenAPI` with title `"Human IT Challenge API"`, version `"0.0.1-SNAPSHOT"` and a description. No security scheme or groups are registered (deferred by design).

### 8.12 Logging baseline and profile levels

VERIFIED by source inspection and git diff:
- `application.yml` now contains `logging.pattern.console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n"`.
- `application-dev.yml` and `application-prod.yml` were not modified; profile levels remain `DEBUG` (dev) and `WARN`/`INFO` (prod) per their existing files.
- Application context loads successfully (`HumanItChallengeApplicationTests` PASS).

### 8.13 No test controllers in production source

VERIFIED by `SourceScanAuditTest` (2 tests, PASS), including the new `noTestScaffoldingInProductionSource`:
- No path under `src/main` contains `__test`.
- No `.java` file under `src/main/java` contains `TestErrorController` or `__test`.
- Git status confirms `TestErrorController.java` resides only under `src/test`.

### 8.14 No business endpoints or out-of-scope changes

VERIFIED by Git inspection:
- Modified files: `src/main/resources/application.yml`, `src/test/.../SourceScanAuditTest.java`.
- New files: `GlobalExceptionHandler.java`, `OpenApiConfig.java`, `GlobalExceptionHandlerTest.java`, `OpenApiConfigTest.java`, `TestErrorController.java`, plus agent reports.
- No changes to `pom.xml`, entities, repositories, business controllers, security configuration, Flyway migrations, Docker files or profile levels.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| T-F01 | Info (non-blocking) | `GlobalExceptionHandler.handleResponseStatus` propagates `ex.getReason()` verbatim into the Problem Details `detail` (lines 140–155). No concrete sensitive leak exists in current code: the sole caller (`TestErrorController`) supplies `"Resource not found"`, and a project-wide search found no other `ResponseStatusException` construction. The exposure channel is only as safe as future callers. See Section 8.10. |
| T-F02 | Info (non-blocking) | Tests do not explicitly assert `type == about:blank` or the absence of `rejectedValue`. Code inspection confirms the default type and the absence of `rejectedValue`; this is a coverage gap, not a defect. |
| T-F03 | Info (non-blocking) | Deferred scope items (409 conflict handlers, 401/403 mappings, problem-type URIs, bearer OpenAPI scheme, structured logging) are intentionally not implemented per the developer's recorded Tech Lead adjustments. No business code exercises the 409/401/403 taxonomy yet, so this is acceptable for T1.5. |
| T-F04 | Info (non-blocking) | The developer report's first-run compilation failure and its intermediate log were not independently inspected by the tester; only the final `clean verify` was rerun and verified. The final build result is VERIFIED. |

No Critical or Major findings. No blocking defects identified.

## 10. Decisions and reasoning

- Independent verification was completed before reading `developer-report.md`, per the Tester independence duty.
- The implementation matches the architect proposal's core design: a `@RestControllerAdvice` extending `ResponseEntityExceptionHandler`, `application/problem+json`, validation errors without `rejectedValue`, a minimal OpenAPI bean and a common console logging pattern.
- The developer's deferral of 409/401/403 handlers and problem-type URIs deviates from the architect's original proposal but is recorded as a Tech Lead adjustment in the developer report and is consistent with the absence of business code that could trigger those cases. This is accepted as in-scope for T1.5.
- Comparison with the developer report shows agreement on all verified outcomes (41 tests, 0 failures/errors/skipped, BUILD SUCCESS, sanitization of the 500 path, OpenAPI metadata, source-scan result). Two minor documentation/accuracy notes are recorded as T-F01 and T-F04; neither changes the verification outcome.
- Security assessment: no concrete leak exists in the current code. The only potential exposure channel is the propagation of caller-supplied `ResponseStatusException` reasons (T-F01), which is a caller-contract concern rather than an observed leak. It is therefore recorded as Info and is not blocking.

## 11. Final status

**PASS (tester recommendation) — implementation verified; no blocking defects.**

All 13 requested verification points are satisfied by code inspection and/or passing tests. `.\mvnw.cmd clean verify` completed with BUILD SUCCESS, 41 tests, 0 failures, 0 errors and 0 skipped. Final acceptance remains with the human Tech Lead.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Deferred | 409 conflict handlers (uniqueness/optimistic locking) | Phase 2 (T2.3/T2.4/T2.6) |
| OT-02 | Deferred | 401/403 mappings and filter-level handling | Phase 3 (T3.1/T3.3) |
| OT-03 | Deferred | Problem-type URIs, bearer security scheme in OpenAPI, structured logging | Phase 4/5 |
| OT-04 | Hardening (optional) | Document or enforce that `ResponseStatusException` reasons must be client-safe, or return the status reason phrase and log the original reason server-side | Tech Lead decision |
| OT-05 | Test coverage gap | Add explicit assertions for `type == about:blank` and absence of `rejectedValue` if stricter verification is desired | Tester/Reviewer |
| OT-06 | Governance | Tech Lead final acceptance of T1.5 not yet recorded | Tech Lead |

## 13. Handoff to next agent

- **To the Reviewer:** audit the sanitization of the `ResponseStatusException` reason channel (T-F01), HTTP status preservation, scope compliance (no business endpoints, entities, persistence, security enforcement, Docker) and the test-only controller placement.
- **To the Tech Lead:** T1.5 implementation is complete and independently verified (41/41 tests pass, BUILD SUCCESS). No Critical or Major findings and no blocking defects were identified. Final acceptance decision remains with the Tech Lead; no approval is recorded by this report.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts and verified command output only. All 13 mandatory sections present.
