# Security Assessment — T1.3 JWT Secret Exposure (F-04)

---

## 1. Task ID

**T1.3** — Environment configuration and secret externalization (Phase 1). Sub-analysis: **F-04 — security analysis of JWT secret exposure in Spring Boot configuration binding failures** (execution ID `T1.3-EXEC-005-ARCH-01`).

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.3-EXEC-005-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Type | Security analysis / assessment (design only; no implementation) |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot software architect)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/architect.md` — `edit` denied except `agent-reports/**`; `bash` denied (VERIFIED)

## 4. Authorized scope

Per Tech Lead tasking of 2026-11-09 (`T1.3-EXEC-005-ARCH-01`):

- Analyze the exposure of rejected JWT secret values in Spring Boot configuration binding failures / startup logs.
- Write a concise assessment to `agent-reports/T1.3/security-jwt-assessment.md`.
- Do not implement code; do not modify existing reports or `decisions.md`; do not commit/push; do not approve.

**Out of scope:** any code change, test change, dependency change, or approval.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format, evidence markers, archival convention |
| `agent-reports/T1.3/architect-proposal.md` (Revision 3) | VERIFIED | F-04 finding, claims 10.3/C.3 "never the secret value"; Appendix C validation contract; BK-08 |
| `agent-reports/T1.3/developer-report.md` (EXEC-001-DEV-01) | VERIFIED | F-04 root-cause analysis; observed leak text; test assertions |
| `src/main/java/com/humanit/challenge/config/AppJwtProperties.java` | VERIFIED | Secret field annotated `@NotBlank @Base64DecodedMinBytes(32)` |
| `src/main/java/com/humanit/challenge/config/validation/Base64DecodedMinBytesValidator.java` | VERIFIED | Strict Base64, byte-count check, leak-free messages |
| `src/main/java/com/humanit/challenge/config/validation/Base64DecodedMinBytes.java` | VERIFIED | Constraint annotation; message carries `{value}` bytes only |
| `src/test/java/com/humanit/challenge/config/JwtSecretValidationTest.java` | VERIFIED | T-M5/T-M6 `doesNotContain(secret)` assertions that currently fail |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | `builder.properties(...)` → default property source; failure-message joiner |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | VERIFIED | T-M18 absence audit (candidate for extension) |
| `pom.xml` (version only) | VERIFIED | Spring Boot `3.5.16`, Java 21 |
| Web search (Spring Boot bind failure / masking) | RECONSTRUCTED | No built-in masking of rejected values in startup failure reports; actuator `/env` masks by key name only |

## 6. Files changed

**By this execution:** one file, if the write succeeds.

| File | Change |
|---|---|
| `agent-reports/T1.3/security-jwt-assessment.md` | **Created** — this assessment |

No application code, YAML, `pom.xml`, `decisions.md`, other agents' reports or Git state was touched. (Write result and read-back verification are reported in Section 8 / at the end.)

## 7. Commands executed

**None.** `bash` is denied for this agent. Evidence is drawn from inspected files and one web search (Section 5). No build or test was re-run by this execution; test outcomes are cited from the Developer report (VERIFIED there, C5/C6).

## 8. Evidence and observed results

### 8.1 The observed leak (VERIFIED — developer report F-04 + test source)

For an invalid secret, the application fails to start (fail-fast works), but the failure report contains the rejected value:

- `ConfigurationPropertiesBindException` → nested `BindException` → `FieldError` with `rejectedValue = <input>`; the `FieldError.toString()` includes `rejected value [not!valid]`.
- Spring Boot's `LoggingFailureAnalysisReporter` prints a block containing `Value: "AAAAAAAA…"` together with the property origin.
- The custom `Base64DecodedMinBytesValidator` messages themselves are leak-free (`"must be valid Base64"`, `"decoded to 24 bytes; minimum 32 required"`); the leak is framework-generated, not validator-generated.

VERIFIED leak text (developer report 8.6 F-04): `rejected value [not!valid]` / `rejected value [AAAA…32…]` and `Value: "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"`.

### 8.2 Scope of the leak (VERIFIED)

- The leak occurs **only on the invalid-secret startup-failure path** (a deployment that cannot start). The valid-secret runtime path never prints the secret (developer report 8.6 F-04).
- In `--debug`/trace mode the full stack trace (and exception `toString()`) is also emitted, increasing exposure (RECONSTRUCTED from Spring Boot logging behavior; not directly observed in this project).

### 8.3 Built-in masking (RECONSTRUCTED / web search)

- Spring Boot has **no** built-in mechanism that masks rejected values in startup bind-failure reports. The `Binder` always records the raw input as `rejectedValue`, and `ConfigurationPropertiesBindException`/`BindException` carry it.
- Spring Boot **does** mask secret-like values in the Actuator `/env` (and `/configprops`) endpoints by key name (`management.endpoint.env.keys-to-sanitize`, default includes `password`, `secret`, etc.) — but that is a runtime diagnostic endpoint, not the startup failure report, and it does not retroactively sanitize the startup log.
- `spring.main.log-startup-info=false` suppresses the normal startup info line but does **not** suppress the failure-analysis report.

### 8.4 Report persistence check

The write of this assessment is attempted in this execution. Whether it succeeded is reported at the end of the output (per mandatory reporting rule 12, no claim is made without verification).

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| SA-01 | **Major** | Spring Boot configuration binding echoes the rejected JWT secret value in `ConfigurationPropertiesBindException`/`BindException` messages and in the `LoggingFailureAnalysisReporter` startup-failure block. The custom validator does not leak; the leak is framework-generated. |
| SA-02 | Minor | Leak is confined to the invalid-secret startup-failure path; valid-secret path is clean. |
| SA-03 | Minor | `--debug`/trace mode widens exposure (stack trace + exception `toString()`). |
| SA-04 | Minor | No built-in startup-time masking exists; mitigation must be application-side. |
| SA-05 | Minor | Removing/relaxing the fail-fast would be worse; the fix must keep fail-fast and the 32-byte floor. |

Zero Critical findings.

## 10. Decisions and reasoning (alternatives, trade-offs, assumptions)

### 10.1 Exact cause of the exposure

The secret is bound as a validated `@ConfigurationProperties` record component:

```java
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record AppJwtProperties(
        @NotBlank
        @Base64DecodedMinBytes(32)   // custom constraint
        String secret,
        @NotNull @Min(1) Integer expirationMinutes) {}
```

When binding fails, Spring's `Binder` creates a `FieldError` whose `rejectedValue` is the raw input string. `ConfigurationPropertiesBindingPostProcessor.bind()` wraps the resulting `BindException` in a `ConfigurationPropertiesBindException`. That exception's message (and the derived `FailureAnalysis`) includes `rejected value [...]`. Spring Boot's `LoggingFailureAnalysisReporter` then prints it at startup. The custom validator's own message is clean, but it is embedded in a `FieldError` that also carries the raw value, and the framework renders both.

In short: **the leak is a property of Spring's bind-error reporting, not of the validator.**

### 10.2 Simplest safe mitigation

**Move JWT secret validation out of the `@ConfigurationProperties` bind path and into application code that throws a clean exception.**

Concretely:

1. Remove `@NotBlank` and `@Base64DecodedMinBytes(32)` from `AppJwtProperties.secret` (keep the field as a plain `String` so it still binds and remains in configuration metadata).
2. Add a small startup validator bean (e.g. `JwtSecretValidator`, `@Component` with constructor-injected `AppJwtProperties`) whose `@PostConstruct` performs the same checks — non-blank, strict Base64, decoded length ≥ 32 — and throws `IllegalStateException` with messages that contain the property name and the failure kind but **never** the secret value (e.g. `"app.security.jwt.secret must be valid Base64"`, `"app.security.jwt.secret decoded to 24 bytes; minimum 32 required"`).
3. Reuse the existing decoding logic (extract a static helper from `Base64DecodedMinBytesValidator`, or call `java.util.Base64.getDecoder()` directly) so the 32-byte rule is not duplicated divergently.

Why this is the simplest safe option:

- It removes the `FieldError`/`rejectedValue` from the failure path entirely: a bean-creation failure carries only our message, so no value reaches the startup log or `--debug` stack trace.
- It preserves fail-fast (bean creation runs during context refresh, before the web server starts) and the 32-decoded-byte minimum (same rule, same threshold).
- It needs no new framework extension (no `FailureAnalyzer`, no `spring.factories`, no `BindHandler`), respecting BK-08 and keeping the change small.
- It keeps `spring-boot-configuration-processor` metadata for the `app.security.jwt.secret` property.

Alternatives considered:

- **Custom `FailureAnalyzer`** (sanitize the bind-failure report): rejected as the primary fix, because it only cleans the human-readable report; the underlying exception still carries the rejected value, so `--debug`/stack traces and any code inspecting the exception still leak. It is acceptable as defense-in-depth only.
- **Keep constraints and accept framework echo** (adjust the acceptance claim): rejected — leaves a real secret leak in startup logs, which is the security problem under analysis.
- **External secret manager / encrypted property values**: out of T1.3 scope (Phase 5/ops); can be layered later without changing this validation.

### 10.3 Does Spring Boot have a built-in solution?

**No — not for startup failure reports.** (RECONSTRUCTED + web search, Section 8.3.)

- No property or flag suppresses `rejected value [...]` in bind errors. The `Binder` records the raw input unconditionally.
- Actuator `/env` (and `/configprops`) masking is key-name based and runtime-only; it does not sanitize the startup failure report.
- `spring.main.log-startup-info=false` does not suppress the failure-analysis report.
- Conclusion: the mitigation must be application-side (Section 10.2). Any future Spring Boot feature that sanitizes bind errors would let us revert to declarative validation; that is a PENDING VERIFICATION / future-work item, not something to assume today.

### 10.4 Preserving fail-fast validation and the 32 decoded-byte minimum

- **Fail-fast:** validation still runs during context refresh (`@PostConstruct` of an eagerly created bean), so an invalid/missing secret aborts startup before the application serves traffic. Behavior is equivalent to the current bind-time failure for operators; only the exception type changes (bean-creation failure instead of bind failure).
- **32 decoded bytes:** unchanged rule — strict RFC 4648 Base64, decoded length ≥ 32. The `@Max(100)`-style hard ceilings for pagination are unaffected (pagination values are not secrets and remain declarative).
- **Message contract:** messages must still contain the tokens the tests assert (`"Base64"`, `"decoded to 24 bytes; minimum 32 required"`, and the property prefix `app.security.jwt`), and must never contain the secret. This keeps the existing test intent meaningful after the change.
- **Metadata:** the property stays in the record, so `spring-boot-configuration-processor` still emits `app.security.jwt.secret` metadata (O-01).

### 10.5 Files and tests that would need changes

**Main code (small, targeted):**

- `src/main/java/com/humanit/challenge/config/AppJwtProperties.java` — remove the two constraint annotations from `secret`; keep the field and the `expirationMinutes` constraints.
- `src/main/java/com/humanit/challenge/config/validation/Base64DecodedMinBytesValidator.java` — optionally expose a reusable static `validate(String, int)` returning an error message or `null` (or keep the logic duplicated in the new bean; reuse is preferred to avoid drift). The annotation and validator remain valid for other, non-secret uses.
- **New:** `src/main/java/com/humanit/challenge/config/JwtSecretValidator.java` (or similarly named) — `@Component`, constructor-injected `AppJwtProperties`, `@PostConstruct` validation throwing `IllegalStateException` with clean messages.

**Tests (require Tech Lead authorization to edit, per developer report OT-02/OT-03 governance):**

- `JwtSecretValidationTest.java` — keep the T-M3/T-M4/T-M5/T-M6/T-M7/T-M17 intent; assertions on `failureMessages` still pass because our messages contain the same tokens and never the secret. The `doesNotContain(secret)` assertions become satisfiable. No structural rewrite expected, but message wording must be kept aligned with the new validator.
- `SourceScanAuditTest.java` — extend T-M18 to assert that `AppJwtProperties` does not carry secret-validation constraints on `secret` (e.g. no `@Base64DecodedMinBytes`/`@NotBlank` on that field), preventing regression into the leaking path. (Optional but recommended.)
- `Base64DecodedMinBytesValidatorTest.java` — unchanged (validator still exists and is unit-tested).
- Bootstrap/profile tests (`HumanItChallengeApplicationTests`, `ProdProfileStartupTest`, etc.) — unchanged, provided the supplied dummy secret remains valid (it does: 43 `A` chars = 32 bytes).

**No changes needed to:** YAML files, `pom.xml`, pagination properties/tests (except the separate F-02 harness-precedence issue, which is unrelated to this security finding), demo-user gating, or boundaries BK-01…BK-08.

### 10.6 Residual security risks

1. **Regression risk:** if a future change re-adds `@NotBlank`/`@Base64DecodedMinBytes` to `AppJwtProperties.secret`, the leak returns. Mitigation: source-scan audit extension (10.5) plus code review. Marked PENDING VERIFICATION until the audit is added.
2. **Debug/trace mode:** even with clean exception messages, `--debug` may print other sensitive context (e.g. full environment). Operational recommendation: keep debug disabled in production; do not enable `show-values` on Actuator endpoints. (Operational, not code.)
3. **Other secret-bearing properties:** any future `@ConfigurationProperties` field that validates a secret with a Bean Validation constraint will leak the same way. The pattern established here (validate secrets in application code, not in the bind path) should be documented as the standard for this codebase. PENDING VERIFICATION / governance.
4. **Process/environment exposure:** the secret is supplied via `APP_SECURITY_JWT_SECRET`; environment variables are visible to other users on the host and in process listings. Mitigation is operational (secret manager, restricted permissions, container secrets) — out of T1.3 scope but should be tracked for Phase 5 / T1.6.
5. **Log aggregation:** startup failure reports are often shipped to centralized logs. Even after the fix, ensure log pipelines do not capture raw environment dumps. Operational.
6. **Test-only secrets:** the dummy secrets in `StartupTestHarness` (`"A".repeat(43)`, etc.) are not real credentials and are acceptable in tests; they must never be used as production values (already documented).
7. **Claim adjustment:** the proposal's claims 10.3/C.3 ("failure messages name the property and failure kind only — never the secret value") become achievable with this mitigation; until it lands, the claim is not fully supported (developer report F-04). This assessment does not alter the proposal; it recommends the mitigation above.

### 10.7 Assumptions (unverified)

- A1: `@PostConstruct` validation during context refresh fails startup before traffic is served — standard Spring Boot behavior, not re-verified here (PENDING VERIFICATION).
- A2: Removing the constraints from the record does not break `spring-boot-configuration-processor` metadata generation — expected, metadata is property-driven, not constraint-driven (PENDING VERIFICATION).
- A3: The dummy secret used in tests (`VALID_SECRET_32_BYTES`) remains bindable and passes the new validator unchanged (RECONSTRUCTED from existing test outcomes).
- A4: No other code path logs `AppJwtProperties` or its secret (not exhaustively searched; recommend a grep audit by the Reviewer).
- A5: The web-search conclusion (no built-in masking) reflects Spring Boot 3.5.x behavior; a future Spring Boot release may add masking and allow reverting to declarative validation.

## 11. Final status

**ASSESSMENT DELIVERED — RECOMMENDATION: MOVE JWT SECRET VALIDATION OUT OF THE BIND PATH (APPLICATION-CODE VALIDATION), KEEPING FAIL-FAST AND THE 32-BYTE MINIMUM. NO IMPLEMENTATION PERFORMED.**

- No code changed. No approval, acceptance or "tests passed" statement is claimed.
- The mitigation is a design recommendation pending Tech Lead approval; if approved, it is a small, targeted change (Section 10.5) plus test-side updates requiring authorization.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OSA-01 | Awaiting decision | Approve the mitigation (10.2) or choose an alternative | Tech Lead |
| OSA-02 | PENDING VERIFICATION | Re-verify fail-fast + 32-byte behavior after implementation (A1–A3) | Tester (independent, before reading developer conclusions) |
| OSA-03 | Governance | Test-file edits for `JwtSecretValidationTest` / `SourceScanAuditTest` require explicit authorization (developer report OT-02/OT-03) | Tech Lead |
| OSA-04 | Follow-up | Document "validate secrets in application code, not the bind path" as a codebase standard; grep audit for other secret-bearing properties | Reviewer / Developer |
| OSA-05 | Operational (out of T1.3) | Secret handling: host/container permissions, log pipelines, Actuator `show-values` | Phase 5 / T1.6 |

## 13. Handoff to next agent

- **Tech Lead:** decide on the mitigation (10.2) and, if approved, explicitly authorize the small code change plus the test-side updates (OSA-03). No implementation has been or should be performed without that authorization.
- **Architect:** if the Tech Lead wants the proposal's claims (10.3/C.3) reworded to reflect pre-mitigation reality, issue a Revision 4 under the archival duty; otherwise the mitigation makes the existing claims achievable.
- **Developer (after authorization):** implement Section 10.5 exactly; keep `Base64DecodedMinBytesValidator` for reuse; add the source-scan assertion.
- **Tester:** re-run the suite independently before reading the Developer report; verify T-M3…T-M7/T-M17 fail-fast and no value leak, plus the new audit assertion.
- **Reviewer:** audit that the secret no longer traverses the bind-error path (no `rejected value` containing the secret in failure reports), that fail-fast and the 32-byte floor are preserved, and that BK-08 (no `spring.factories`/`EnvironmentPostProcessor`) still holds.

---

**End of assessment.** All statements are classified per `agent-reports/README.md` Section 5 (VERIFIED / RECONSTRUCTED / NOT AVAILABLE / PENDING VERIFICATION). No test results, commands, approvals or historical events were invented.
