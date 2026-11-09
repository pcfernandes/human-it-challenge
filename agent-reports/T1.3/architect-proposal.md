# Architect Proposal — T1.3 Environment Configuration and Secret Externalization (Revision 4)

---

## 1. Task ID

**T1.3** — Environment configuration and secret externalization (Phase 1).

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.3-EXEC-007-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | `T1.3-EXEC-003-ARCH-01` (Revision 3). Archived unchanged **before** replacement at `agent-reports/T1.3/history/architect-002.md` per `agent-reports/README.md` Section 6; archival verified by read-back within this execution (Section 8.3). |
| Predecessor archives | `agent-reports/T1.3/history/architect-001.md` (Revision 2, `T1.3-EXEC-002-ARCH-01`); `agent-reports/T1.3/history/architect-002.md` (Revision 3, `T1.3-EXEC-003-ARCH-01`). |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot software architect)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/architect.md` — `edit` allow for `agent-reports/**` only; `bash: deny` (VERIFIED this execution, Section 5)

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 (`T1.3-EXEC-007-ARCH-01`): **T1.3 architecture Revision 4** — minimal revision of Revision 3 incorporating four proposed amendments (JWT secret handling, profile isolation, test corrections, preserved residual risk) while preserving all unrelated Revision 3 decisions and boundaries.

**Amendments applied (this revision):**

1. **JWT secret** (mandatory amendment 1):
   - Keep the externalized Base64 secret (`APP_SECURITY_JWT_SECRET`).
   - Require at least 32 decoded bytes.
   - Fail startup for missing, blank or invalid secrets.
   - Avoid exposing rejected secret values in startup diagnostics → move secret validation **out of** the Bean Validation bind path into application code (see Section 10.3, Appendix C).
   - Preserve existing pagination and expiration validation.
2. **Profile isolation** (mandatory amendment 2):
   - Use `@Profile("dev & !prod")` for demo-user configuration (`DevConfigPropertiesRegistration`).
   - Ensure dev-only behavior remains available (cases 1–2).
   - Ensure `prod`, `dev,prod` and `prod,dev` exclude demo-user config (cases 3–5).
   - Require the same protection for future demo-user provisioning (Phase 3, T3.2) via a shared constant.
3. **Test corrections** (mandatory amendment 3):
   - Use high-precedence property overrides in startup tests (`TestPropertyValues` / `@DynamicPropertySource` at run level).
   - Distinguish default profiles from active profiles (`getDefaultProfiles()` vs `getActiveProfiles()`).
   - Update combined-profile assertions (demo bean **absent** under combined profiles).
   - Add security regression tests (no rejected secret value in failure reports; guard-form source-scan audit).
4. **Preserve the previously accepted residual risk** (mandatory amendment 4):
   - Missing explicit prod activation still defaults to dev (RK-01 / F-02).
   - Operational mitigation remains required in T1.6 / Phase 5 (M1–M5 unchanged).
5. **Preserve all unrelated Revision 3 decisions and boundaries** (mandatory amendment 5): O-01/O-02/OD-01…OD-04, BK-01…BK-08, RK-02/RK-03, pagination 0/20/100, no startup logging, no datasource/Flyway, no JWT code, no HTTP pagination.

**Out of scope (explicitly not authorized):**

- Implementation of anything proposed here (waits for explicit Tech Lead implementation approval).
- Application code, `pom.xml`, `decisions.md`, requirements/planning documents, other agents' reports, Git operations.
- This revision does **not** claim approval; it is **PROPOSED / PENDING TECH LEAD APPROVAL**.

## 5. Files inspected

All inspected during **this** execution with the read/glob/grep tools:

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format; evidence markers; execution-history convention (Section 6: archive before replace, zero-padded numbering) |
| `agent-reports/T1.3/architect-proposal.md` (pre-replacement, Revision 3) | VERIFIED | Revision 3, 541 lines, execution `T1.3-EXEC-003-ARCH-01` — predecessor archived by this execution; source of carried-forward verified facts |
| `agent-reports/T1.3/developer-report.md` (`T1.3-EXEC-001-DEV-01`) | VERIFIED | F-02 (harness precedence), F-03 (assertion form), F-04 (secret exposure), F-05, F-06; build failure evidence (31 tests, 8 failures) |
| `agent-reports/T1.3/security-jwt-assessment.md` (`T1.3-EXEC-005-ARCH-01`) | VERIFIED | SA-01…SA-05; mitigation: move JWT secret validation out of the bind path (Section 10.2 of that assessment) |
| `agent-reports/T1.3/security-profiles-assessment.md` (`T1.3-EXEC-006-ARCH-01`) | VERIFIED | F-SP-01…F-SP-07; recommendation: `@Profile("dev & !prod")` on `DevConfigPropertiesRegistration` |
| `agent-reports/T1.3/history/` (directory) | VERIFIED | Contained `architect-001.md` (Revision 2) at execution start; `architect-002.md` (Revision 3) created by this execution before replacement |
| `agent-reports/decisions.md` | VERIFIED (read-only) | D-01…D-05 approved; **no T1.3 approval recorded**; not modified by this execution |
| `.opencode/agents/architect.md` | VERIFIED | `edit` allow for `agent-reports/**` only; `bash: deny` |
| `implementation-plan.md` (lines 108–118) | VERIFIED | T1.3 scope unchanged |
| `requirements-decisions.md` (Section 12, lines 549–615) | VERIFIED | Dev: H2, profile `dev`; Prod: PostgreSQL, profile `prod`, externalized credentials |
| Source files referenced (config records, validators, registration classes, tests) | VERIFIED via developer/security reports | Structural facts cited from those reports' VERIFIED evidence; no re-inspection required for this design revision |

## 6. Files changed

| File | Change |
|---|---|
| `agent-reports/T1.3/history/architect-002.md` | **Created** — unchanged archival copy of Revision 3 (`T1.3-EXEC-003-ARCH-01`), written before the primary was replaced, per `agent-reports/README.md` Section 6; persistence verified by read-back (Section 8.3) |
| `agent-reports/T1.3/architect-proposal.md` | **Replaced** by this Revision 4 (execution `T1.3-EXEC-007-ARCH-01`); persistence verified by read-back (Section 8.6) |

No other file was touched. No application code, no `pom.xml`, no `decisions.md`, no requirements/planning documents, no other agent's reports, no Git operations.

## 7. Commands executed

**None.** `bash` is denied for this agent (VERIFIED in `.opencode/agents/architect.md`), and this execution is design-only. All build/test evidence is cited from existing reports, not re-executed.

## 8. Evidence and observed results

### 8.1 Revision 3 as the predecessor (VERIFIED)

`agent-reports/T1.3/architect-proposal.md` at execution start was Revision 3 (541 lines, execution `T1.3-EXEC-003-ARCH-01`), the version archived by this execution. Its full content was read in this execution (Section 5) and reproduced unchanged in `agent-reports/T1.3/history/architect-002.md`.

### 8.2 Inputs consumed (VERIFIED)

- `agent-reports/T1.3/developer-report.md` (EXEC-001-DEV-01) — build failure `Tests run: 31, Failures: 8`, root causes F-02 (harness precedence), F-03 (assertion form), F-04 (framework-echoed secret value).
- `agent-reports/T1.3/security-jwt-assessment.md` (EXEC-005-ARCH-01) — mitigation: move JWT secret validation out of the bind path; keep fail-fast and 32-byte floor; no built-in Spring Boot masking.
- `agent-reports/T1.3/security-profiles-assessment.md` (EXEC-006-ARCH-01) — `@Profile("dev & !prod")` on `DevConfigPropertiesRegistration`; five-case table; same guard for future provisioning.

### 8.3 Archival verification (VERIFIED — read-back)

`agent-reports/T1.3/history/architect-002.md` was written before the primary report was replaced and read back in full within this execution. Content matches the pre-replacement Revision 3 primary as read at execution start: 541 lines, unchanged text, original header/execution ID `T1.3-EXEC-003-ARCH-01` and date retained (README Section 6 requirement). No silent overwrite occurred.

### 8.4 Corroboration limitation (PENDING VERIFICATION)

All design claims here remain PENDING VERIFICATION until implemented and tested under an approved execution — in particular: application-code (post-bind) secret validation fail-fast timing, `@Profile("dev & !prod")` expression parsing under Spring Boot 3.5.x default-profile semantics, high-precedence override behavior in the test harness, and the security regression assertions.

### 8.5 Report persistence check (per Tech Lead instruction)

The write of this Revision 4 to `agent-reports/T1.3/architect-proposal.md` is attempted in this execution; success and read-back verification are reported at the end of the output. No permission bypass is used; `edit`/`write` are allowed for `agent-reports/**` by the agent definition.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-01 | Major (resolved by Revision 3) | Revision 2's explicit-profile / `EnvironmentPostProcessor` guard superseded by OD-01/OD-02. Unchanged by Revision 4. |
| F-02 | **Major — accepted residual risk (PRESERVED per mandatory amendment 4)** | With `spring.profiles.default: dev`, a production deployment that omits `SPRING_PROFILES_ACTIVE=prod` **silently starts as dev** (R1 demo user with known credentials in Phase 3; R2 in-memory H2 instead of PostgreSQL from T1.4; R3 DEBUG logging; R4 dev-valued divergent properties). No technical guard exists by explicit Tech Lead decision (OD-02). Mitigations M1–M5 are operational only (Section 10.2, Appendix B.2). **This risk is preserved, not reduced, by Revision 4** — the `@Profile("dev & !prod")` guard (amendment 2) closes only the *combined-profile* demo-user exposure, not the *missing-profile* case (F-SP-02). |
| F-03 | Minor (partially addressed by amendment 2) | Previously: `SPRING_PROFILES_ACTIVE=dev,prod` loaded both files and the `@Profile("dev")` demo-user configuration registered. After amendment 2, the demo-user bean **no longer registers** under `dev,prod` / `prod,dev` (cases 4–5 flip to "absent"). The combined-profile *file-loading* half remains (both profile files still load; dev settings may override prod's) — F-SP-03 residual. T-M20 is updated to assert absence of the demo bean (Section 10.8, Appendix F). |
| F-04 | Minor (addressed by amendment 1, pending implementation verification) | Framework-level echo of the rejected secret value in `ConfigurationPropertiesBindException` / `LoggingFailureAnalysisReporter` (developer report F-04). Revision 4 removes the secret from the bind-error path by validating in application code (`JwtSecretValidator`, `@PostConstruct`) instead of Bean Validation on the record component, so no `FieldError.rejectedValue` carries the secret. The custom validator messages stay leak-free. PENDING VERIFICATION (security regression tests, Appendix F). |
| F-05 | Minor (identifier collision, resolved in Revision 3) | OD numbering collision between Revision 2 and Tech Lead identifiers. Unchanged. |
| F-06 | Minor (edge case, PENDING VERIFICATION) | Empty `SPRING_PROFILES_ACTIVE=""` semantics. Unchanged (OT-05). |
| F-07 | Minor (new, from amendment 3) | Test-harness property-source precedence: overrides injected via `SpringApplicationBuilder.properties()` land in `defaultProperties` and are shadowed by committed `application.yml` values, making T-M10…T-M14 ineffective. Amendment 3 requires high-precedence overrides (`TestPropertyValues` / run-level `@DynamicPropertySource`). PENDING VERIFICATION. |
| F-08 | Minor (new, from amendment 3) | Default-vs-active profile confusion: `spring.profiles.default` activates `dev` for config loading and profile-condition evaluation but does not report it via `getActiveProfiles()` (which returns `[]` for a default-dev start). Tests must distinguish `getDefaultProfiles()` from `getActiveProfiles()` and use functional evidence (dev settings visible, log line). PENDING VERIFICATION. |
| F-09 | Minor (new, from amendment 2) | Profile-name fragility: the guard keys on the exact string `"prod"`; variants (`production`, `prd`, …) are not covered, and the string is duplicated across code/tests/docs. Mitigation: shared constant `Profiles.DEV_AND_NOT_PROD`. PENDING VERIFICATION. |
| F-10 | Minor (new, from amendment 1) | Other secret-bearing `@ConfigurationProperties` fields validated via Bean Validation would leak the same way; the pattern "validate secrets in application code, not the bind path" should be documented as a codebase standard. PENDING VERIFICATION / governance. |

Zero Critical findings. F-02 (Major) remains open by design — it is the residual risk the Tech Lead's OD-02 decision explicitly accepted and mandatory amendment 4 requires preserved; it is not a defect of this revision.

## 10. Decisions and reasoning (alternatives, trade-offs, assumptions)

Concrete artifacts (exact files, snippets, test matrix, acceptance criteria) are in Appendices A–I. Unchanged decisions from Revision 3 (O-01, O-02, OD-01…OD-04, BK-01…BK-08, RK-02, RK-03) are preserved verbatim in spirit and are not re-litigated here.

### 10.1 Changes 1–3 — Native default dev profile; guard machinery removed (PRESERVED)

Unchanged from Revision 3 Section 10.1: `spring.profiles.default: dev` in common `application.yml`; production must set `SPRING_PROFILES_ACTIVE=prod`; no `EnvironmentPostProcessor`, no `META-INF/spring.factories`, no no-profile fail-fast. OD-01/OD-02 respected.

### 10.2 Change 5 — Residual risk of accidental dev startup in production (PRESERVED, mandatory amendment 4)

Unchanged from Revision 3 Section 10.2. RK-01 remains a Major accepted residual risk. Mitigations M1–M5 unchanged. **Revision 4 does not introduce any technical safeguard** (OD-02 honored). The `@Profile("dev & !prod")` guard (amendment 2) closes only F-03's combined-profile demo-user exposure, not RK-01 (F-SP-02). Any future code-level safeguard remains a new proposal requiring Tech Lead approval.

### 10.3 Amendment 1 — JWT secret: keep fail-fast, move validation out of the bind path

**Decision:** the secret stays externalized Base64 (`APP_SECURITY_JWT_SECRET`; relaxed binding → `app.security.jwt.secret`) with a ≥32 decoded-byte floor and fail-fast for missing/blank/invalid — but validation is performed in **application code**, not by Bean Validation on the record component, so that no rejected value reaches startup diagnostics.

**Why (security-jwt-assessment SA-01):** Spring's `Binder` records the raw input as `FieldError.rejectedValue`, and `ConfigurationPropertiesBindException` / `LoggingFailureAnalysisReporter` render it (`rejected value [not!valid]`, `Value: "AAAA…"`). The custom validator's own messages are leak-free; the leak is framework-generated and cannot be suppressed by configuration (no built-in masking for startup failure reports; Actuator `/env` masking is key-name based and runtime-only).

**Concretely:**

1. `AppJwtProperties.secret` becomes a plain `String` field — **remove** `@NotBlank` and `@Base64DecodedMinBytes(32)` from it. The field still binds and remains in configuration metadata (O-01).
2. `app.security.jwt.expiration-minutes` keeps `@NotNull` + `@Min(1)` on the record (non-secret, declarative validation preserved — mandatory amendment 1 "preserve existing pagination and expiration validation").
3. Pagination properties and their constraints (`@Max(100)`, `@Min(1)`, `@ValidPaginationInvariants`) are **unchanged** — pagination values are not secrets and remain declarative (mandatory amendment 1).
4. Add `JwtSecretValidator` — `@Component`, constructor-injected `AppJwtProperties`, `@PostConstruct` performing: non-blank check, strict RFC 4648 Base64 decode, decoded length ≥ 32. On failure it throws `IllegalStateException` whose message contains the property name and failure kind but **never** the secret value (e.g. `"app.security.jwt.secret must be valid Base64"`, `"app.security.jwt.secret decoded to 24 bytes; minimum 32 required"`).
5. Reuse the decoding logic: extract a static helper from `Base64DecodedMinBytesValidator` (or call `Base64.getDecoder()` directly) so the 32-byte rule is not duplicated divergently. The annotation and validator remain valid for other, non-secret uses.
6. Fail-fast is preserved: `@PostConstruct` runs during context refresh, before the web server starts, so an invalid/missing secret aborts startup. Only the exception type changes (bean-creation failure instead of bind failure).

**Message contract (preserved for tests):** messages must still contain the tokens the tests assert (`"Base64"`, `"decoded to 24 bytes; minimum 32 required"`, and the property prefix `app.security.jwt`), and must never contain the secret. This keeps the existing test intent meaningful after the change.

**Alternatives considered (security-jwt-assessment Section 10.2):**

- *Custom `FailureAnalyzer`* — rejected as primary: only cleans the human-readable report; the underlying exception still carries the rejected value, so `--debug`/stack traces still leak. Acceptable as defense-in-depth only.
- *Keep constraints and accept framework echo* — rejected: leaves a real secret leak in startup logs.
- *External secret manager / encrypted property values* — out of T1.3 scope (Phase 5/ops); can be layered later.

**Boundaries respected:** BK-08 (no `spring.factories`, no `EnvironmentPostProcessor`); no new framework extension; metadata generation unaffected (property-driven, not constraint-driven — PENDING VERIFICATION).

### 10.4 Amendment 2 — Profile isolation: `@Profile("dev & !prod")`

**Decision:** change `DevConfigPropertiesRegistration` from `@Profile("dev")` to `@Profile("dev & !prod")`. This is the minimal, infrastructure-free change recommended by the security-profiles assessment (F-SP-01…F-SP-07) and closes F-03's combined-profile demo-user exposure.

**Case evaluation (security-profiles assessment Section 8.2):**

| # | Active profiles | `dev` active? | `prod` active? | `dev & !prod` | Demo-user bean registers? |
|---|---|---|---|---|---|
| 1 | (none → default `dev`) | yes (default) | no | true | **YES** (dev behavior preserved) |
| 2 | `dev` | yes | no | true | **YES** |
| 3 | `prod` | no | yes | false | **NO** |
| 4 | `dev,prod` | yes | yes | false | **NO** (was YES before) |
| 5 | `prod,dev` | yes | yes | false | **NO** (was YES before) |

- Dev-only behavior remains available (cases 1–2) — OD-02 and the dev experience are intact.
- `prod`, `dev,prod`, `prod,dev` exclude demo-user config (cases 3–5).
- The guard is evaluated at bean-registration time against the active profile set, so combined prod activation excludes the dev-gated configuration.

**Caveat (F-SP-02, F-02/RK-01 preserved):** the guard only protects the *demo-user properties bean*. It cannot distinguish "dev by default on a dev laptop" from "dev by default in production" — if `prod` is simply absent, `dev & !prod` is true and the app still runs as dev. RK-01 remains governed by operational mitigations M1–M5 (Section 10.2). The annotation fixes the *blended* profile case, not the *missing* profile case.

**Future provisioning (security-profiles assessment Section 10.4):** the Phase 3 (T3.2) provisioning bean must use the **same** guard (`@Profile("dev & !prod")`, or a shared constant) rather than `@Profile("dev")`. Otherwise it would register under `dev,prod` and either fail at injection time (missing `AppDemoUserProperties`) or provision a demo user with known credentials in a blended environment — recreating F-03. Recommendation: introduce a shared constant `Profiles.DEV_AND_NOT_PROD = "dev & !prod"` in a small `config`-level holder so the properties registration and the future provisioning bean cannot drift, and extend the source-scan audit to assert both usages. (Design recommendation; implementation out of scope for this execution.)

**Alternatives considered (security-profiles assessment Section 10.2):**

- *Keep `@Profile("dev")`* — leaves F-03 open; rejected.
- *`@Profile("!prod")`* — too broad: registers under *any* non-prod profile (including unknown/overlay profiles such as a future `test` profile). Not minimal-safe. Rejected.
- *Reintroduce `EnvironmentPostProcessor`/fail-fast guard* — rejected by OD-01; contradicts the native default-profile design.
- *Code-level conditional (`@ConditionalOnProperty` or an `if` check)* — more code/surface, duplicates what the profile expression already expresses. Rejected in favor of the idiomatic annotation.
- *Doing nothing and documenting* — leaves the combined-profile demo-user exposure in place. Rejected.

**Trade-offs:** profile-expression syntax (`&`, `!`) is less commonly known than a plain profile name; must be documented in code comments and the proposal so future readers do not "simplify" it back to `@Profile("dev")`. Test assertions and proposal wording must change (F-SP-06, F-SP-07) — addressed by amendment 3.

**Residuals preserved:** RK-01 (F-SP-02), combined-profile file loading (F-SP-03), profile-name variants (F-SP-04).

### 10.5 Change 8 — Base64 JWT secret validation using decoded bytes (PRESERVED, amended by 10.3)

Unchanged in substance from Revision 3 Section 10.5, with the amendment that validation is performed in application code rather than via Bean Validation on the record component. The 32-decoded-byte rule, strict RFC 4648 Base64, no hex/MIME tolerance, no `String.length()` inspection, and the phase boundary (no encoder/decoder/filter/`SecretKey`/algorithm/`/auth/login`) are all retained. Operational guidance (`openssl rand -base64 64`) retained.

### 10.6 Change 9 — Pagination defaults and the maximum of 100 (PRESERVED)

Unchanged from Revision 3 Section 10.6: 0/20/100, `@Min(1)`, `@Max(100)` hard ceiling, `@ValidPaginationInvariants` cross-field, code-owned ceiling, HTTP enforcement in T2.8. Mandatory amendment 1 explicitly preserves pagination validation.

### 10.7 Change 10 — No optional startup INFO logging (PRESERVED)

Unchanged from Revision 3 Section 10.7 (OD-04 deferred, BK-08).

### 10.8 Amendment 3 — Test corrections

**Decision:** the test suite and assertions are corrected to match the amended design and to be effective. Four corrections:

**A. High-precedence property overrides in startup tests (addresses F-07).** Overrides must be registered through a source with higher precedence than committed `application.yml` (e.g. `TestPropertyValues.applyTo(...)` or run-level `@DynamicPropertySource`), so that invalid values actually reach the binder and fail startup. The current `SpringApplicationBuilder.properties(...)` → `defaultProperties` (lowest precedence) is ineffective for keys committed in `application.yml`. Only then do T-M10…T-M14 become executable. This is a test-code change requiring Tech Lead authorization (developer report OT-02).

**B. Distinguish default profiles from active profiles (addresses F-08).** For a no-`@ActiveProfiles` start, `getActiveProfiles()` returns `[]` even though `dev` is applied via `spring.profiles.default`. Tests must not assert `getActiveProfiles() == [dev]`. Instead:
- Assert `getDefaultProfiles()` contains `dev` (or, functionally) that `application-dev.yml` settings are visible and the default-dev log line is emitted; and
- For explicit activation, assert `getActiveProfiles()` exactly (T-M19: `[prod]`).
The OD-02 proof survives in functional form (dev file loads, defaults bind, prod test passes). T-M1/T-M8R wording is adjusted accordingly (see Appendix F).

**C. Update combined-profile assertions (addresses F-03 / F-SP-06).** `CombinedDevProdProfilesTest` is updated so that:
- `documentsF03DevGatedDemoUserBeanRegisters` → asserts `context.getBeansOfType(AppDemoUserProperties.class)` is **empty** under `dev,prod`; rename method/test to reflect "demo bean does NOT register under combined profiles".
- `documentsF03BothProfileFilesLoad` → kept (both files still load).
- Optionally add a guard-form assertion to `SourceScanAuditTest` (no `@Profile("dev")` without `& !prod` on `DevConfigPropertiesRegistration`).

**D. Add security regression tests (addresses F-04 / SA-01…SA-05 and amendment 1):**
- No rejected secret value in failure reports: for malformed/short/blank secrets, the startup failure report (and `--debug` stack trace where feasible) must not contain the supplied secret string. The custom `JwtSecretValidator` messages are leak-free by construction; the test asserts `doesNotContain(secret)` on the captured failure output.
- Guard-form source-scan audit: `AppJwtProperties` must not carry `@NotBlank`/`@Base64DecodedMinBytes` on `secret` (prevents regression into the leaking path); `DevConfigPropertiesRegistration` must carry `@Profile("dev & !prod")` (or the shared constant) and no bare `@Profile("dev")`.
- Functional fail-fast preserved: missing/blank/malformed/short secrets still abort startup in dev and prod (T-M3…T-M7, T-M17) with the property named in the failure.
- 32-byte boundary accepted (T-M7) unchanged.

All test edits require Tech Lead authorization (developer report OT-02/OT-03; security assessments OSA-03/OSP-03).

### 10.9 Preserved design elements (compatibility)

| Element | Status in Revision 4 |
|---|---|
| `application.yml` / `application-dev.yml` / `application-prod.yml` triplet | Retained; common file owns `spring.profiles.default: dev` |
| Immutable `@ConfigurationProperties` records, constructor-bound, `@Validated` | Retained (pagination, jwt secret field now plain `String`, demo-user) |
| Pagination validation (0/20/100, `@Max(100)`, cross-field invariant) | **Preserved** (amendment 1) |
| Expiration validation (`@NotNull` + `@Min(1)`, default 60) | **Preserved** (amendment 1) |
| Secrets externalized, fail-fast | Retained — amended to application-code validation (10.3) |
| Production demo-user protection | Retained — amended to `@Profile("dev & !prod")` (10.4) |
| Base64 decoded-bytes secret validation (≥32 bytes) | Retained — amended validation location (10.3/10.5) |
| Independent configuration tests | Retained — corrected per amendment 3 (10.8) |
| Strict separation from T1.4 | Retained — zero datasource/Flyway/`ddl-auto` |
| `spring-boot-configuration-processor` (O-01) | Retained — only permitted `pom.xml` change |
| `expiration-minutes` default 60 (O-02) | Retained |
| Native default dev profile (OD-02) | Retained |
| No `EnvironmentPostProcessor`/`spring.factories` (OD-01) | Retained (BK-08) |
| No optional startup INFO logging (OD-04) | Retained (BK-08) |
| Residual risk RK-01 preserved (amendment 4) | Retained (Section 10.2) |

### 10.10 Assumptions

1. The T1.1/T1.2 baseline is the starting state; the T1.3 implementation exists in the working tree (developer report VERIFIED) but is not modified by this execution.
2. `requirements-decisions.md` and `implementation-plan.md` remain authoritative; no requirement-level decision is reopened.
3. Spring Boot 3.5.x parses `&` and `!` inside a single `@Profile("...")` string as a profile expression (PENDING VERIFICATION, A1 in security-profiles assessment; natural fit: rewritten T-M20).
4. Active profiles are treated as a set, so `prod,dev` ≡ `dev,prod` (RECONSTRUCTED; standard Spring behavior).
5. `@PostConstruct` validation during context refresh fails startup before traffic is served (PENDING VERIFICATION, A1 in security-jwt assessment).
6. Removing the constraints from the record does not break `spring-boot-configuration-processor` metadata generation (PENDING VERIFICATION, A2 in security-jwt assessment).
7. High-precedence overrides in the test harness are feasible without changing production code (PENDING VERIFICATION, F-07).
8. `spring.profiles.default` semantics (honored only when no active profiles set; ignored when `SPRING_PROFILES_ACTIVE` set) — PENDING VERIFICATION (carried from Revision 3).

## 11. Final status

**DELIVERED — REVISED PROPOSAL (REVISION 4) PERSISTED, PENDING TECH LEAD APPROVAL.**

- Revision 4 persisted at `agent-reports/T1.3/architect-proposal.md` (verification reported at end of output); Revision 3 archived unchanged at `agent-reports/T1.3/history/architect-002.md` (VERIFIED, Section 8.3); Revision 2 remains at `agent-reports/T1.3/history/architect-001.md`.
- All four amendments are addressed (Sections 10.3, 10.4, 10.8; Appendices A–I); all unrelated Revision 3 decisions and boundaries are preserved (Section 10.9).
- Residual risk RK-01 / F-02 is preserved, not reduced (Section 10.2, mandatory amendment 4).
- **Implementation is NOT authorized and NOT performed.** No application code, YAML, `pom.xml` or `decisions.md` change; no Git operations; only the two report files in Section 6 were written.
- This proposal is **PROPOSED / PENDING TECH LEAD APPROVAL** — only the human Tech Lead can approve it and authorize T1.3 implementation.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Awaiting approval | Explicit Tech Lead approval of Revision 4 (in particular the mitigation in 10.3 and the guard in 10.4) before any implementation begins | Tech Lead |
| OT-02 | Awaiting authorization | Apply amendment 1: remove constraints from `AppJwtProperties.secret`; add `JwtSecretValidator`; extract reusable decode helper; add source-scan audit assertion | Developer (after authorization) |
| OT-03 | Awaiting authorization | Apply amendment 2: change `@Profile("dev")` → `@Profile("dev & !prod")` on `DevConfigPropertiesRegistration`; optionally introduce shared `Profiles.DEV_AND_NOT_PROD` constant | Developer (after authorization) |
| OT-04 | Awaiting authorization | Apply amendment 3: high-precedence overrides in startup tests; distinguish default vs active profiles; rewrite combined-profile assertions; add security regression tests | Developer (test edits need authorization, OT-02/OT-03 governance) |
| OT-05 | PENDING VERIFICATION | Verify application-code secret validation fail-fast timing and leak-free reports (amendment 1, security regression tests) | Tester (independent, before reading developer conclusions) |
| OT-06 | PENDING VERIFICATION | Verify `@Profile("dev & !prod")` expression parsing, default-profile matching, and the five-case table (amendment 2) | Tester (independent) |
| OT-07 | PENDING VERIFICATION (edge) | Empty `SPRING_PROFILES_ACTIVE=""` semantics (F-06) | Tester / optional |
| OT-08 | Deferred (accepted) | README (T1.6): prod requires `SPRING_PROFILES_ACTIVE=prod` + profile-check runbook step; dev requires JWT secret + demo-user env vars (F-04) | Developer (T1.6) |
| OT-09 | Forward boundary | Phase 5 `compose.yaml` must set `SPRING_PROFILES_ACTIVE: prod` explicitly (mitigation M-1, RK-01) | Developer (Phase 5), Reviewer check |
| OT-10 | Forward boundary (T3.2) | Phase 3 provisioning must use the identical guard / shared constant | Developer (Phase 3), Reviewer check |
| OT-11 | Standing governance | `decisions.md` has no T1.3 approval recorded; only the human Tech Lead can create one | Tech Lead |
| OT-12 | Governance | Document "validate secrets in application code, not the bind path" as a codebase standard; grep audit for other secret-bearing properties | Reviewer / Developer |

## 13. Open decisions (summary)

**None new for T1.3 beyond those carried.** Revision 4 resolves the amendment-level items into concrete design changes (10.3, 10.4, 10.8) but does not by itself approve them — approval remains with the human Tech Lead. The only substantive judgment calls left to the Tech Lead are:

1. Approval of the mitigation in 10.3 (moving JWT secret validation out of the bind path) — or selection of an alternative (custom `FailureAnalyzer`, accept framework echo).
2. Approval of the guard `@Profile("dev & !prod")` in 10.4 — or selection of an alternative (`!prod` alone, reintroduced guard, code-level conditional, or doing nothing).
3. Authorization of the test-side edits in amendment 3 (OT-04) and the security regression tests.
4. Continued acceptance of the preserved residual risk RK-01 (F-02) — operational mitigation remains required in T1.6 / Phase 5.

No new open decisions are raised beyond these. Approval status is **PENDING**; nothing is recorded in `decisions.md`.

---

# Appendices — concrete proposal artifacts

*(The appendices add detail; the thirteen mandatory sections above were not reordered or omitted.)*

## Appendix A — Configuration architecture and exact proposed files (amended)

### A.1 File layout (exact list for an approved T1.3 implementation)

```text
pom.xml                                                          (modify: add spring-boot-configuration-processor — O-01, the ONLY pom change)
src/main/resources/
  application.yml                                                (modify: common env-neutral defaults + DEFAULT DEV PROFILE)
      has:
        spring.application.name
        spring:
          profiles:
            default: dev        # the ONLY profile key committed anywhere; applies when
                                # no active profile is set; production MUST set
                                # SPRING_PROFILES_ACTIVE=prod
      common app.* key values:
        app.pagination.default-page: 0
        app.pagination.default-page-size: 20
        app.pagination.max-page-size: 100
        app.security.jwt.expiration-minutes: 60
      content clarifying comments:
        # No profile is active? 'dev' applies automatically (spring.profiles.default).
        # PRODUCTION MUST START WITH: SPRING_PROFILES_ACTIVE=prod
        #   (verify the startup log line shows 'prod' active)
        # Secrets are NEVER committed. Supply by environment:
        #   dev/prod: APP_SECURITY_JWT_SECRET            (required, Base64)
        #   dev only: APP_SECURITY_DEMO_USER_USERNAME
        #             APP_SECURITY_DEMO_USER_PASSWORD
        # Datasource settings arrive in T1.4 — do not add them here.
  application-dev.yml                                            (create)
    logging.level.com.humanit.challenge: DEBUG
    # (No datasource here — T1.4. No secrets here — env only.)
  application-prod.yml                                           (create)
    logging.level.root: WARN
    logging.level.com.humanit.challenge: INFO
    # (No datasource here — T1.4. No secrets here — env only.
    #  Demo-user settings are deliberately ABSENT in prod.)
src/main/java/com/humanit/challenge/config/
  ConfigPropertiesRegistration.java                              (create)
      @Configuration(proxyBeanMethods = false)
      @EnableConfigurationProperties({AppPaginationProperties.class, AppJwtProperties.class})
  DevConfigPropertiesRegistration.java                           (create)
      @Configuration(proxyBeanMethods = false)
      @Profile("dev & !prod")                                   # AMENDED (was @Profile("dev"))
      @EnableConfigurationProperties(AppDemoUserProperties.class)
  Profiles.java                                                  (create, optional but recommended)
      public final class Profiles { public static final String DEV_AND_NOT_PROD = "dev & !prod"; private Profiles() {} }
  AppPaginationProperties.java                                   (create record — Appendix E; unchanged)
  AppJwtProperties.java                                          (create record — Appendix C; secret field now PLAIN String, no constraints)
  AppDemoUserProperties.java                                     (create record — dev-profile-gated registration)
  JwtSecretValidator.java                                        (create — AMENDED; @Component, @PostConstruct validation)
  validation/
    Base64DecodedMinBytes.java                                   (create constraint annotation; retained for non-secret uses)
    Base64DecodedMinBytesValidator.java                          (create; extract reusable static decode helper)
    ValidPaginationInvariants.java                               (create class-level constraint; unchanged)
    ValidPaginationInvariantsValidator.java                      (create; unchanged)
src/test/java/com/humanit/challenge/config/                      (create — Appendix F; amended matrix)
src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java
                                                                  (modify: NO @ActiveProfiles; @DynamicPropertySource secret/demo values)
```

**Deliberately ABSENT (mandatory change 3 / BK-08):** `src/main/resources/META-INF/spring.factories` — not created; any `EnvironmentPostProcessor`/bootstrap package — not created. Audit item T-M18 enforces both absences. **No** custom startup INFO logger.

**No** other files are modified: no entity/repository/controller code, no datasource/Flyway config, no `README.md` (T1.6), no security filter chain (Phase 3).

### A.2 Property tree summary (unchanged)

```yaml
spring:
  profiles:
    default: dev             # committed (common); native fallback when nothing active
app:
  pagination:
    default-page: 0          # committed default (common)
    default-page-size: 20    # committed default (common)
    max-page-size: 100       # committed default (common); overrides >100 FAIL startup
  security:
    jwt:
      expiration-minutes: 60 # committed default (common)
      secret:                # NOT COMMITTED ANYWHERE — env-only, both profiles
    demo-user:               # DEV-PROFILE REGISTRATION ONLY (dev & !prod)
      username: <env>        # APP_SECURITY_DEMO_USER_USERNAME
      password: <env>        # APP_SECURITY_DEMO_USER_PASSWORD
```

## Appendix B — Profile startup behavior (amended)

### B.1 Behavior table (amended)

| Scenario | Behavior (Revision 4) | Notes |
|---|---|---|
| No profile set (local `mvnw spring-boot:run`, tests without `@ActiveProfiles`) | **Default `dev` applies natively**; `application-dev.yml` loads; demo-user bean registrable; JWT secret required | OD-02; proven functionally (T-M1/T-M8R amended) |
| `SPRING_PROFILES_ACTIVE=prod` | prod file loads; demo-user bean **not** registered; secrets required | Mandatory change 2; default ignored (PENDING VERIFICATION T-M19) |
| `SPRING_PROFILES_ACTIVE=dev` | Same as default (explicit) | Equivalent to no-profile start |
| `SPRING_PROFILES_ACTIVE=dev,prod` | **Both files load**; demo-user bean **does NOT register** (guard `dev & !prod`) | F-03 amended; T-M20 updated to assert absence |
| `SPRING_PROFILES_ACTIVE=prod,dev` | Same as `dev,prod` (profiles are a set) | F-03 amended |
| `SPRING_PROFILES_ACTIVE=qa` | Only common file loads; no profile file for `qa`; app starts if secret provided | Operational exposure, RK-01 |
| Empty `SPRING_PROFILES_ACTIVE=""` | Expected: treated as unset → default dev (PENDING VERIFICATION) | F-06 / OT-07 |
| Test without `@ActiveProfiles` | **Succeeds** with default dev; `getActiveProfiles()` is `[]` but `getDefaultProfiles()` contains `dev` | Amendment 3 (F-08) |
| Any committed `spring.profiles.active` in YAML | **None** — forbidden; audit T-M18 | Active profiles come from deployment env only |
| `spring.profiles.default` committed | **Exactly once**, in `application.yml` | Audit T-M18 |

Equivalence clause: `SPRING_PROFILES_ACTIVE`, `--spring.profiles.active` and `-Dspring.profiles.active` are the documented, equivalent production activation mechanisms; the recommendation is the environment variable for all deployment tooling.

### B.2 Residual-risk register (preserved, mandatory amendment 4)

| Risk | Consequence | Mitigations | Residual |
|---|---|---|---|
| RK-01 production start without `SPRING_PROFILES_ACTIVE=prod` (F-02) | R1 demo user with known credentials (Phase 3, but only if provisioning bean is mis-guarded — guard now `dev & !prod`); R2 in-memory H2 instead of PostgreSQL (T1.4); R3 DEBUG logging; R4 dev-valued divergent properties | M1 compose/prod tooling sets prod explicitly; M2 startup-log profile check in runbook; M3 property fail-fast aborts unconfigured starts; M4 demo keys dev-gated + env-only; M5 deployment checklist | **Accepted by Tech Lead decision (OD-02)**; severity Major; any technical safeguard = new proposal. **Preserved by Revision 4** (amendment 4). |
| RK-02 T1.4 adds datasource config on top of default-dev | Dev auto-config must keep working; prod datasource must never silently load under dev default | T1.4 proposal review against this profile contract | Standard task boundary |
| RK-03 empty-active-profile edge (F-06) | Ambiguous behavior if tooling exports an empty variable | Confirm by test (OT-07) or document | Low |
| RK-04 combined-profile file loading (F-SP-03) | Both profile files load under `dev,prod`; dev settings may override prod's | Documentation; T1.4 review | Low (demo bean now excluded) |
| RK-05 profile-name variants (F-SP-04) | `production`, `prd`, etc. not covered by the `prod` guard | Shared constant + audit; documentation | Low |

## Appendix C — Secret handling and validation (amended by amendment 1)

### C.1 Representation contract (unchanged — OD-03 approved)

`app.security.jwt.secret` = **standard Base64 (RFC 4648)** of ≥ 32 random bytes (256 bits); recommended generation 64 bytes (88 Base64 chars) for HS512 headroom. A 32-character Base64 string decodes to exactly 24 bytes → below the 256-bit floor → **rejected** even though character count is 32. The validator never inspects `String.length()`; it decodes and counts bytes (demonstrated by unit test T-M9).

### C.2 Registration and validation (AMENDED — validation moved out of the bind path)

```java
// AppJwtProperties (record, constructor-bound, validated)
// AMENDED: secret is now a PLAIN field — no @NotBlank / @Base64DecodedMinBytes.
//          expiration-minutes constraints PRESERVED (amendment 1).
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record AppJwtProperties(
        String secret,                                    // plain; validated by JwtSecretValidator
        @NotNull
        @Min(1)
        Integer expirationMinutes                        // committed default 60 (O-02); PRESERVED
) {}

// NEW: application-code validation (amendment 1) — keeps fail-fast, removes secret from bind-error path.
@Component
public class JwtSecretValidator {
    private final AppJwtProperties props;
    public JwtSecretValidator(AppJwtProperties props) { this.props = props; }

    @PostConstruct
    void validate() {
        String secret = props.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret must not be blank");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);  // strict RFC 4648
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("app.security.jwt.secret must be valid Base64");
        }
        if (decoded.length < 32) {
            throw new IllegalStateException(
                    "app.security.jwt.secret decoded to " + decoded.length + " bytes; minimum 32 required");
        }
    }
}
```

`Base64DecodedMinBytesValidator` (retained for non-secret uses) exposes a reusable static decode helper (e.g. `static String validate(String value, int minBytes)` returning an error message or `null`) so the 32-byte rule is not duplicated divergently.

**Why application code (security-jwt-assessment Section 10.2):** removes the `FieldError`/`rejectedValue` from the failure path entirely — a bean-creation failure carries only our message, so no value reaches the startup log or `--debug` stack trace. Preserves fail-fast (bean creation runs during context refresh, before the web server starts) and the 32-decoded-byte minimum. Needs no new framework extension (no `FailureAnalyzer`, no `spring.factories`, no `BindHandler`), respecting BK-08. Keeps `spring-boot-configuration-processor` metadata for `app.security.jwt.secret`.

**Message contract (preserved):** messages must still contain the tokens the tests assert (`"Base64"`, `"decoded to 24 bytes; minimum 32 required"`, and the property prefix `app.security.jwt`), and must never contain the secret. This keeps the existing test intent meaningful after the change.

### C.3 Externalization and failure posture (amendment 1)

- Dev **and** prod: `export APP_SECURITY_JWT_SECRET=<base64>`; no committed value in either profile.
- Absent / blank / non-Base64 / decoded < 32 bytes → **startup failure in both profiles** (application-code fail-fast during context refresh; behavior equivalent to the previous bind-time failure for operators, only the exception type changes). PENDING VERIFICATION (T-M3…T-M7, T-M17).
- Prod emphasis (mandatory change 6): the externalized-credentials requirement of `requirements-decisions.md` Section 12 is honored; `application-prod.yml` contains no secret and no credential material.
- **No rejected secret value in startup diagnostics** (security regression tests, Appendix F) — the secret no longer traverses the bind-error path.

### C.4 What T1.3 does **not** do (unchanged)

No encoder, decoder, filter, `SecretKey` bean, algorithm selection or `/auth/login` — Phase 3 (T3.1) owns those, with nimbus already on the classpath. A Phase 3 RSA/ECDSA choice redesigns the property set there with approval.

## Appendix D — Demo-user safety in production (amended by amendment 2)

- `AppDemoUserProperties` (`@Validated` record: `@NotBlank username`, `@NotBlank password`) is registered **only** by a `@Profile("dev & !prod")` configuration class (amendment 2) — in prod, or under combined `dev,prod` / `prod,dev`, the bean does not exist, independent of env vars present.
- Credential **values** env-only in dev (`APP_SECURITY_DEMO_USER_USERNAME` / `..._PASSWORD`); nothing in committed YAML; zero demo-user keys in `application-prod.yml` and the common file (Reviewer grep audit).
- Phase 3 (T3.2) implements provisioning as a dev-only bean and stores a BCrypt hash at provision time; T1.3 provides only the externalized, dev-scoped plumbing. **The provisioning bean must use the same guard** (`@Profile("dev & !prod")` or the shared `Profiles.DEV_AND_NOT_PROD` constant) — otherwise it would register under `dev,prod` and either fail at injection time (missing `AppDemoUserProperties`) or provision a demo user with known credentials in a blended environment, recreating F-03.
- **Boundary of the protection (honest statement, preserved):** the gating is profile-based and dev is now the *default* profile — under RK-01 (accidental dev start, where `prod` is simply absent) the bean still registers. The protection is real for correctly-activated prod deployments and is exactly as strong as mitigations M1–M5 elsewhere; this boundary is the accepted risk of OD-02, not a hidden weakness. Revision 4 closes only the *combined-profile* exposure, not the *missing-profile* case.

## Appendix E — Pagination invariants (PRESERVED, amendment 1)

Unchanged from Revision 3 Appendix E. The ceiling remains code-owned; `@Max(100)` binds at bind time; cross-field invariant enforced; HTTP enforcement remains T2.8. Mandatory amendment 1 explicitly preserves pagination validation.

## Appendix F — Test matrix (amended by amendment 3)

All tests are expected to be executable via `.\mvnw.cmd test` after an approved implementation; all are **PENDING VERIFICATION**. Tests are designed and run by the Tester **before** reading the Developer report (independence, `agent-reports/README.md` Section 8). Amendments from Revision 3: high-precedence overrides (A), default-vs-active profile distinction (B), combined-profile assertions updated (C), security regression tests (D).

| ID | Test | Setup | Expected outcome |
|---|---|---|---|
| T-M1 | Context loads with **no** `@ActiveProfiles` (default dev) | no profile annotation + `@DynamicPropertySource` JWT secret (valid ≥32-byte Base64) + demo-user values | Context loads; `getDefaultProfiles()` contains `dev`; dev settings visible; `getActiveProfiles()` is `[]` (amendment 3B) |
| T-M2 | Defaults bound | same context as T-M1 | `default-page 0`, `default-page-size 20`, `max-page-size 100`, `expiration-minutes 60` |
| T-M3 | No secret → fail | dev (default), NO secret source | startup **fails** (property named, no secret value in message) |
| T-M4 | Blank secret → fail | secret `""` | startup fails |
| T-M5 | Malformed Base64 → fail | secret `not!valid` | startup fails ("must be valid Base64") |
| T-M6 | <32 decoded bytes → fail | Base64 of a 24-byte array (32 chars) | startup fails; message reports decoded length |
| T-M7 | 32-byte boundary accepted | Base64 of exactly 32 bytes (43 chars) | binds successfully |
| T-M8R | Default-dev semantics (replaces removed fail-fast T-M8) | context with no active profiles | default profiles contain `dev`; `application-dev.yml` settings visible; `getActiveProfiles()` is `[]` (amendment 3B) |
| T-M9 | Chars ≠ bytes demonstration | unit test of `Base64DecodedMinBytesValidator`: 32-char → invalid (24 bytes); 43-char → valid (32 bytes); 88-char (64 bytes) → valid | as stated |
| T-M10 | `max-page-size=150` override → fail | override via **high-precedence** source (amendment 3A) | startup fails (`@Max(100)`), never "silently increased" |
| T-M11 | `default-page-size=0` → fail | high-precedence override | startup fails (`@Min(1)`) |
| T-M12 | `default-page=-1` → fail | high-precedence override | startup fails (`@Min(0)`) |
| T-M13 | `default-page-size > max-page-size` → fail | high-precedence override | startup fails (cross-field invariant) |
| T-M14 | `expiration-minutes=0` → fail | high-precedence override | startup fails (`@Min(1)`); default 60 confirmed in T-M2 |
| T-M15 | Demo-user dev-only binding | dev/default: properties present → record binds; **prod:** no `AppDemoUserProperties` bean exists regardless of env presence | as stated |
| T-M16 | Prod happy path (config-only) | `@ActiveProfiles("prod")` + env-style secret | context loads; jwt + pagination bound; no demo-user bean |
| T-M17 | Prod without secret → fail | prod, no overrides | startup fails |
| T-M18 | Source-scan audit (inverted from Revision 2) | static scan | `spring.profiles.default` **exactly once**, in `application.yml` only; **zero** `spring.factories`; **zero** `EnvironmentPostProcessor`; no committed `spring.profiles.active`; **no** `@NotBlank`/`@Base64DecodedMinBytes` on `AppJwtProperties.secret`; `DevConfigPropertiesRegistration` carries `@Profile("dev & !prod")` (or shared constant) and no bare `@Profile("dev")` (amendment 3D) |
| T-M19 | Explicit prod suppresses default | `@ActiveProfiles("prod")` | active profiles exactly `[prod]`; no `dev` leakage; `application-dev.yml` settings absent |
| T-M20 | `dev,prod` combined activation (amended) | `@ActiveProfiles({"dev","prod"})` | both files load; demo bean **absent** (amendment 2); documents F-03/RK-04; renamed test reflects absence (amendment 3C) |
| T-M21 | Empty `SPRING_PROFILES_ACTIVE=""` edge (optional) | environment simulation | behavior recorded; expected default dev (F-06/OT-07) |
| T-M22 | Security regression — no secret leak (NEW, amendment 3D) | malformed/short/blank secret supplied; capture startup failure report (+ `--debug` where feasible) | failure output does **not** contain the supplied secret string; messages contain only property name + failure kind (+ decoded length) |
| T-M23 | Security regression — guard form (NEW, amendment 3D) | static scan / context under `dev,prod` | `DevConfigPropertiesRegistration` excluded; no bare `@Profile("dev")` on demo-user-related beans; provisioning (Phase 3) uses the same guard |
| T-M24 | Security regression — fail-fast preserved (NEW, amendment 3D) | missing/blank/malformed/short secret in dev and prod | startup aborts; property named in failure (T-M3…T-M7, T-M17 re-asserted) |
| T-M25 | Security regression — 32-byte boundary (NEW, amendment 3D) | 43-char (32 bytes) secret | binds successfully (T-M7 re-asserted) |

Bootstrap `HumanItChallengeApplicationTests` (updated): **no** `@ActiveProfiles`; gains dynamic JWT-secret/demo-user properties → exercises the native default-dev path end-to-end (T-M1 equivalent at the bootstrap level); default-profile distinction (amendment 3B) applies.

## Appendix G — Acceptance criteria and implementation boundaries (amended)

### G.1 Acceptance criteria (proposed, for Tech Lead approval to adopt)

1. `dev` and `prod` settings are separated across `application-dev.yml` / `application-prod.yml` per Appendix A.
2. `application.yml` (common) sets `spring.profiles.default: dev`; with no active profile the application starts as `dev` (T-M1/T-M8R, functional proof + default-profile distinction).
3. Production deployment documentation and comments state: production **must** start with explicit `SPRING_PROFILES_ACTIVE=prod`; `--spring.profiles.active=prod` / `-D…` are documented equivalents (T1.6/Phase 5 own the deployment tooling).
4. **No** `EnvironmentPostProcessor`, **no** `META-INF/spring.factories`, and **no** no-profile fail-fast requirement exists anywhere in code, config or tests (T-M18).
5. The residual accidental-dev-startup risk is documented (this proposal Section 10.2 / Appendix B.2) and carried into README (T1.6) and Phase 5 tooling (OT-08/OT-09). **Preserved** (amendment 4).
6. The JWT secret is externalized (no committed value in dev or prod), validated on **decoded byte length ≥ 32**, and absent/blank/malformed/too-short values fail startup; `app.security.jwt.expiration-minutes` defaults to 60. **Validation is performed in application code, not the bind path, so no rejected secret value appears in startup diagnostics** (amendment 1; T-M22).
7. No JWT encoder/decoder/filter/algorithm code exists in the delivered T1.3 code.
8. Pagination defaults bind to 0/20/100; `defaultPageSize` has `@Min(1)`; overrides raising `maxPageSize` above 100 fail startup (verified via high-precedence overrides, amendment 3A); no HTTP pagination logic (T2.8 boundary kept).
9. Demo-user properties are dev-profile-gated using `@Profile("dev & !prod")`, env-sourced, absent from prod binding and from combined `dev,prod` / `prod,dev` activation; no demo user is provisionable in prod or under combined profiles from T1.3 artifacts (amendment 2).
10. `spring-boot-configuration-processor` is added (O-01) and produces metadata for the `app.*` tree.
11. Configuration records are immutable Java records with constructor binding.
12. All independent configuration tests (Appendix F, including security regression tests T-M22…T-M25) pass via Maven, before any Developer-report reading.
13. Zero datasource/Flyway/Hibernate `ddl-auto` settings are introduced (T1.4 boundary).
14. (NEW) Demo-user provisioning in Phase 3 (T3.2) uses the same guard / shared constant (Appendix D).
15. (NEW) "Validate secrets in application code, not the bind path" is documented as a codebase standard (OT-12).

### G.2 Implementation boundaries (hard) (preserved)

| Boundary | Rule |
|---|---|
| BK-01 | No JPA entities, repositories, controllers, services, DTOs |
| BK-02 | No datasource, H2/PostgreSQL connection, Flyway, `ddl-auto` configuration |
| BK-03 | No JWT token creation/validation code, no `SecurityFilterChain` change, no `/auth/login` |
| BK-04 | No `Pageable`/controller/HTTP pagination enforcement |
| BK-05 | `pom.xml` touched **only** for the O-01 annotation processor |
| BK-06 | No README changes (T1.6); interim friction documented to the Tech Lead instead |
| BK-07 | No `decisions.md`, other-agent reports, requirements/plan files, or Git operations |
| BK-08 | **No** custom startup INFO logging (OD-04 deferred); no `META-INF/spring.factories`; no `EnvironmentPostProcessor`/bootstrap package (OD-01) |
| RK-01 | Risk: accidental dev startup in production (documented, accepted, **preserved** — Section 10.2, Appendix B.2) |
| RK-02 | Risk: T1.4 must respect the default-profile contract while adding datasource config |
| RK-03 | Edge: empty `SPRING_PROFILES_ACTIVE` semantics (OT-07) |
| RK-04 | Risk: combined-profile file loading (F-SP-03) — demo bean now excluded; file loading remains |
| RK-05 | Risk: profile-name variants (F-SP-04) — guard keys on exact `"prod"` |

## Appendix H — Report-persistence and archival traceability

| Item | Status |
|---|---|
| Archival of predecessor (Revision 2) | `agent-reports/T1.3/history/architect-001.md` — unchanged copy of `T1.3-EXEC-002-ARCH-01` (VERIFIED in Revision 3) |
| Archival of predecessor (Revision 3) | `agent-reports/T1.3/history/architect-002.md` — unchanged copy of `T1.3-EXEC-003-ARCH-01`; written **before** replacement; full read-back VERIFIED (Section 8.3) |
| Primary report replacement | `agent-reports/T1.3/architect-proposal.md` replaced by Revision 4 (`T1.3-EXEC-007-ARCH-01`); full read-back VERIFIED (Section 8.5) |
| Write mechanism | `write`/`edit` tools only (no shell; none available); permitted by the agent definition for `agent-reports/**` — no blocker, no bypass, no permission change needed |
| Files authored | **Only** the two files in Section 6 (own report + own archive); agent-author rule respected: no `decisions.md`, no other agent's reports |
| Approval status | **No approval recorded or claimed** — only the human Tech Lead can approve; nothing was appended to `decisions.md` |

## Appendix I — Handoff summary

- **Tech Lead:** approve/reject Revision 4 (Sections 10–13, Appendices A–I) — in particular (1) the mitigation in 10.3 (application-code secret validation), (2) the guard in 10.4 (`@Profile("dev & !prod")`), (3) authorization of the test-side edits in amendment 3, and (4) continued acceptance of the preserved residual risk RK-01 (F-02, amendment 4). Then explicitly authorize T1.3 implementation.
- **Developer (after explicit approval):** implement Appendix A exactly; apply amendments 1–3 in order; add O-01 processor only; respect boundaries BK-01…BK-08 and RK-01…RK-05; do not create `spring.factories` or any bootstrap guard; use the shared `Profiles` constant for future provisioning alignment.
- **Tester:** execute Appendix F independently **before** reading the Developer report; pay specific attention to T-M1/T-M8R (default-dev proof with default-vs-active distinction), T-M19 (prod suppression), T-M18 (absence + guard-form scan), T-M20 (combined-profile absence assertion), T-M22…T-M25 (security regression), and OT-07 (empty-variable edge).
- **Reviewer:** audit the profile mechanism (single `spring.profiles.default` in the common file; `@Profile("dev & !prod")` exactly, not `!prod` alone; no bare `@Profile("dev")` on demo-user-related beans), secret validation location (no constraints on `AppJwtProperties.secret`; `JwtSecretValidator` present; no rejected value in failure reports), pagination ceiling enforcement, demo-user gating boundary, test override precedence, and boundary compliance including BK-08 and RK-01…RK-05.

---

**End of revised proposal (Revision 4).** Final status and outstanding findings are consolidated in Sections 11–12. **PENDING TECH LEAD APPROVAL — not approved.**
