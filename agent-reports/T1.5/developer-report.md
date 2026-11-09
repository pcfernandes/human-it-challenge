# Developer Report — T1.5 Cross-cutting Application Scaffolding

## 1. Task ID

**T1.5 — Cross-cutting application scaffolding** (Phase 1), per `implementation-plan.md` Section 5 and `requirements-decisions.md` Section 11.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.5-EXEC-002-DEV-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None (first T1.5 Developer execution; no prior `agent-reports/T1.5/developer-report.md` existed) |
| Predecessor archives | None required |
| Architect proposal | `agent-reports/T1.5/architect-proposal.md` (`T1.5-EXEC-001-ARCH-01`) |
| Branch | `feature/T1.5-cross-cutting` |

## 3. Agent role and model

- **Role:** Developer (senior Java and Spring Boot developer)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/developer.md`

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 and `implementation-plan.md` T1.5:

**Implemented:**
1. RFC 9457 Problem Details infrastructure (`GlobalExceptionHandler`).
2. Global `@RestControllerAdvice` producing `application/problem+json`.
3. Consistent validation-error responses (field + message, no rejected values).
4. Minimal OpenAPI bean with `Info` metadata (`OpenApiConfig`).
5. Standard logging baseline (common console pattern in `application.yml`).
6. Focused infrastructure tests (MockMvc handler tests, OpenAPI bean test, source-scan audit extension).

**Tech Lead adjustments applied:**
- A. Extended `ResponseEntityExceptionHandler` and overrode framework handlers so 400/404/405 keep their status codes; `ResponseStatusException` status codes preserved and details sanitized.
- B. Handlers kept minimal: no speculative database-conflict handlers (no business tables exist yet), no custom exception hierarchy, no security enforcement (deferred to Phase 3).
- C. No stack traces, internal exception messages, SQL details, secrets or rejected values leaked; unexpected errors return a generic HTTP 500 detail.

**Scope restrictions honored:**
- No business controllers or endpoints.
- No entities, repositories or persistence functionality.
- No JWT authentication/enforcement changes.
- No Docker or deployment changes.
- No `pom.xml` change.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format, evidence markers, history convention |
| `agent-reports/T1.5/architect-proposal.md` | VERIFIED | Approved design and proposed file list |
| `agent-reports/decisions.md` | VERIFIED | D-07 confirms T1.4 accepted; T1.5 authorized by current tasking |
| `implementation-plan.md` (T1.5, lines 134–146) | VERIFIED | Task scope and acceptance criteria |
| `requirements-decisions.md` (Sections 11, 14, 16, 19) | VERIFIED | RFC 9457, springdoc code-first, error taxonomy, technical baseline |
| `pom.xml` | VERIFIED | Spring Web, Validation, Security, springdoc 2.10.0 already present; no change |
| `src/main/resources/application.yml` | VERIFIED | Common config; logging pattern added |
| `src/main/resources/application-dev.yml` | VERIFIED | Logging levels unchanged |
| `src/main/resources/application-prod.yml` | VERIFIED | Logging levels unchanged |
| `src/main/java/com/humanit/challenge/HumanItChallengeApplication.java` | VERIFIED | Base package `com.humanit.challenge` |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | VERIFIED | Source-scan audit pattern extended |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | Test harness conventions for OpenAPI test |
| `src/test/java/com/humanit/challenge/exception/*` (new) | VERIFIED | Test-only controller + handler tests |

## 6. Files changed

| File | Change |
|---|---|
| `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` | **Created** — `@RestControllerAdvice` extending `ResponseEntityExceptionHandler`; handlers for validation, malformed body, type mismatch, not found, method not allowed, constraint violation, `ResponseStatusException`, and catch-all 500. |
| `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` | **Created** — `@Configuration` with `@Bean OpenAPI` carrying `Info` (title, version, description). |
| `src/main/resources/application.yml` | **Modified** — added common `logging.pattern.console`. |
| `src/test/java/com/humanit/challenge/exception/TestErrorController.java` | **Created** — test-only `@RestController` under `src/test`, mapped to `/__test/errors`. |
| `src/test/java/com/humanit/challenge/exception/GlobalExceptionHandlerTest.java` | **Created** — 8 MockMvc scenarios. |
| `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java` | **Created** — context/OpenAPI bean assertion. |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | **Modified** — added `noTestScaffoldingInProductionSource` assertion. |
| `agent-reports/T1.5/developer-report.md` | **Created** — this report. |

No changes to `pom.xml`, main Java other than the two new files, requirements, planning documents, profile YAML levels, or other agents' reports.

## 7. Commands executed

| Command | Result | Evidence |
|---|---|---|
| `.\mvnw.cmd clean verify` (first run) | Compilation error — `handleMethodArgumentTypeMismatch` does not override (Spring uses `handleTypeMismatch`) | `C:\Users\pedro\AppData\Local\Temp\opencode\mvn-verify-t15.log` |
| `.\mvnw.cmd clean verify` (final run) | Exit code 0; `BUILD SUCCESS`; total time 28.700 s | `C:\Users\pedro\AppData\Local\Temp\opencode\mvn-verify-t15b.log` |

Test summary (final clean verify):
- Tests run: **41**, Failures: 0, Errors: 0, Skipped: 0.
- Java runtime: 21.0.12.1 (VERIFIED in log).
- Maven Enforcer `requireJavaVersion [21,22)` and `requireMavenVersion [3.10.0,)`: passed (VERIFIED).

Per-class results (VERIFIED in log):
- `CombinedDevProdProfilesTest`: 2
- `DefaultDevProfileStartupTest`: 3
- `DemoUserPropertiesTest`: 2
- `ExplicitDevProfileStartupTest`: 1
- `JwtSecretValidationTest`: 6
- `OpenApiConfigTest`: 1
- `PaginationPropertiesValidationTest`: 5
- `ProdProfileStartupTest`: 4
- `SourceScanAuditTest`: 2 (was 1; +1 new assertion)
- `Base64DecodedMinBytesValidatorTest`: 6
- `GlobalExceptionHandlerTest`: 8
- `HumanItChallengeApplicationTests`: 1

## 8. Evidence and observed results

### 8.1 Problem Details responses

`GlobalExceptionHandler` extends `ResponseEntityExceptionHandler` and overrides the framework handlers (`handleMethodArgumentNotValid`, `handleHttpMessageNotReadable`, `handleTypeMismatch`, `handleNoResourceFoundException`, `handleHttpRequestMethodNotSupported`) plus `@ExceptionHandler` for `ConstraintViolationException`, `ResponseStatusException` and a catch-all `Exception`. Every response is serialized as `application/problem+json` with `type` defaulting to `about:blank`, `title`, `status`, `detail`, `instance` (request path) and, for validation failures, an `errors` array of `{field, message}`.

Verified by `GlobalExceptionHandlerTest` (8 tests, all passing):
- Invalid request body → 400 Problem Details with `errors` array.
- Malformed JSON → 400 Problem Details, generic detail, no `errors`.
- Type mismatch → 400 Problem Details, detail names only the parameter.
- Unsupported HTTP method → 405 Problem Details.
- Unknown route → 404 Problem Details.
- `ResponseStatusException` → preserved HTTP status (404) Problem Details.
- Unexpected exception → 500 with generic detail; body contains neither the exception message (`"secret internal message"`), the class name (`RuntimeException`) nor a stack trace.
- Constraint violation (method-level) → 400 Problem Details.

### 8.2 OpenAPI bean

`OpenApiConfig` defines a single `@Bean OpenAPI` with `Info` (title "Human IT Challenge API", version "0.0.1-SNAPSHOT", description). Verified by `OpenApiConfigTest` (1 test, passing): the bean is non-null and all three metadata fields are non-blank.

### 8.3 Logging baseline

A common console pattern was added to `application.yml`:
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n"
```
Profile-specific levels remain unchanged (`DEBUG` dev, `WARN`/`INFO` prod). Verified by source inspection and the application context loading successfully in `HumanItChallengeApplicationTests`.

### 8.4 Source-scan audit

`SourceScanAuditTest` now also asserts that no test scaffolding (`TestErrorController`, `__test`) exists under `src/main`. Passing (2 tests).

### 8.5 Sensitive-data handling

- `detail` for unexpected exceptions is the fixed string `"An unexpected error occurred."`; the exception (with stack trace) is logged server-side only via SLF4J at `ERROR` level.
- Validation errors expose only field names and safe constraint messages; `rejectedValue` is omitted.
- Type-mismatch detail names only the parameter name, never the rejected value.
- `ResponseStatusException` detail uses the developer-provided reason (or the status reason phrase) and never the exception's `toString`/stack trace.
- Malformed-body detail is generic.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-D01 | Info | `ResponseEntityExceptionHandler` uses `handleTypeMismatch` (not `handleMethodArgumentTypeMismatch`); the first build failed to compile until corrected. Resolved in the final build. |
| F-D02 | Info | `NoResourceFoundException` handling relies on Spring Boot 3.4+ routing unknown routes through the dispatcher; verified by the unknown-route test passing under Boot 3.5.16. |
| F-D03 | Info | `@WebMvcTest` slice loads the security auto-configuration; filters are disabled via `addFilters = false` to isolate the handler. No security enforcement was added (deferred to Phase 3). |

No Critical or Major findings.

## 10. Decisions and reasoning

1. **Extend `ResponseEntityExceptionHandler` instead of a plain `@RestControllerAdvice`.** Per Tech Lead adjustment A, this preserves framework HTTP semantics (400 validation, 400 malformed body, 400 type mismatch, 404, 405) instead of collapsing them into 500. Each override returns an explicit status and `application/problem+json`.
2. **No conflict (409) handlers.** Per adjustment B, `DataIntegrityViolationException`/`OptimisticLockingFailureException` mappings are speculative while no business tables exist; they will be added in Phase 2 when uniqueness/locking actually occur. The error taxonomy (409) is still honored by the requirements and will be implemented then.
3. **No security exception mappings (401/403).** Security enforcement is deferred to Phase 3; mapping those exceptions now would be speculative and would have to be re-verified against the real filter chain.
4. **No custom exception hierarchy.** Standard Spring exceptions (`ResponseStatusException`, etc.) are mapped directly; a base `ApiException` is unnecessary at this stage and can be added in Phase 2 if business code needs it.
5. **Validation errors as `errors: [{field, message}]` without `rejectedValue`.** Per the architect's decision and adjustment C, submitted (possibly sensitive) input is not echoed back.
6. **`type` defaults to `about:blank`.** Problem-type URIs are deferred to Phase 4 (T4.2).
7. **OpenAPI bean with `Info` only.** No security scheme or groups; JWT/bearer documentation is Phase 3/4. The bean overrides springdoc's auto-configured `OpenAPI`; removing it falls back to the default, so the change is reversible.
8. **Logging via a common `logging.pattern.console` property only.** No `logback-spring.xml`; profile levels remain in profile files. Structured/JSON logging is deferred to Phase 5.
9. **Catch-all `Exception` → 500 with a fixed detail**, with the exception (including stack trace) logged server-side only. This is the single path that converts unknown failures to 500, and it never copies exception messages to the response.
10. **Test-only controller placed under `src/test`.** `TestErrorController` is never compiled into production classes; `SourceScanAuditTest` asserts its absence from `src/main`.

## 11. Final status

**IMPLEMENTED — VERIFIED.** `.\mvnw.cmd clean verify` exits 0 with `BUILD SUCCESS`; 41 tests pass (0 failures, 0 errors, 0 skipped). All Tech Lead test scenarios are covered and passing. No Critical or Major findings.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Deferred | Database-conflict (409) handlers for uniqueness/optimistic-locking | Phase 2 (T2.3/T2.4/T2.6) |
| OT-02 | Deferred | Security exception mappings (401/403) and filter-level handling | Phase 3 (T3.1/T3.3) |
| OT-03 | Deferred | Problem-type URIs, bearer security scheme in OpenAPI, structured logging | Phase 4/5 |
| OT-04 | Info | `open-in-view` default warning in logs (pre-existing, unrelated to T1.5) | Not in scope |

## 13. Handoff to next agent

- **To the Tester:** verify independently before reading this report, using the scenarios in `architect-proposal.md` Section 10 (Analysis H); confirm 400/405/404/500 Problem Details, `ResponseStatusException` status preservation, sanitization (no stack traces/secrets/rejected values), the OpenAPI bean metadata, and the absence of test controllers in production source.
- **To the Reviewer:** audit HTTP status preservation (no 4xx collapsed to 500), sanitization, scope compliance (no business endpoints, entities, persistence, security enforcement, Docker), and the test-only controller placement.
- **To the Tech Lead:** T1.5 implementation is complete and verified; 41/41 tests pass. Deferred items (409 conflict handlers, security mappings, problem-type URIs) are intentionally scoped to later phases per the Tech Lead adjustments.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts and verified command output only. All 13 mandatory sections present.