# Architect Proposal — T1.3 Environment Configuration and Secret Externalization (Revision 3)

---

## 1. Task ID

**T1.3** — Environment configuration and secret externalization (Phase 1).

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.3-EXEC-003-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | `T1.3-EXEC-002-ARCH-01` (Revision 2 — explicit-profile / `EnvironmentPostProcessor` design). Archived unchanged **before** replacement at `agent-reports/T1.3/history/architect-001.md` per `agent-reports/README.md` Section 6; archival verified by full read-back within this execution (Section 8.3). |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot software architect)
- **Model:** `opencode/glm-5.3-flash-free`
- **Agent definition:** `.opencode/agents/architect.md` — permissions `edit`: deny on all paths except `agent-reports/**`, `bash: deny` (VERIFIED this execution, Section 5)

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 (`T1.3-EXEC-003-ARCH-01`): **T1.3 final architecture simplification** — replace the explicit-profile / `EnvironmentPostProcessor` design with Spring Boot's native default dev profile.

**In scope (this execution — architecture proposal revision only):**

1. Configure `spring.profiles.default: dev` in `application.yml`.
2. Production must use explicit `SPRING_PROFILES_ACTIVE=prod`.
3. Remove the `EnvironmentPostProcessor`, `META-INF/spring.factories` and **all** no-profile fail-fast requirements from the proposal.
4. Update the profile startup tests accordingly.
5. Document the residual risk of accidental dev startup in a production deployment.
6. Keep production secrets externalized and fail-fast.
7. Keep production demo-user protections.
8. Keep Base64 JWT secret validation using decoded bytes.
9. Keep pagination defaults (0/20) and the maximum of 100.
10. Do **not** introduce optional startup INFO logging.

**Approved decisions incorporated (received via direct Tech Lead tasking):**

| ID | Decision | Effect on this revision |
|---|---|---|
| O-01 | `spring-boot-configuration-processor` approved | Retained as the only permitted `pom.xml` change |
| O-02 | JWT `expiration-minutes` default 60 approved | Retained unchanged |
| OD-01 | `EnvironmentPostProcessor` **rejected** | Guard machinery removed entirely (Section 10.1) |
| OD-02 | Default dev profile **approved** | `spring.profiles.default: dev` in common `application.yml` (Section 10.1) |
| OD-03 | Base64-only **approved** | No hex alternative; Revision 2's "can be reopened" caveat closed (Section 10.5) |
| OD-04 | Optional startup logging **deferred** | Custom startup INFO logger excluded; boundary BK-08 (Section 10.7) |

**Out of scope (explicitly not authorized):**

- Implementation of anything proposed here (waits for explicit Tech Lead implementation approval).
- Application code, `pom.xml`, `decisions.md`, requirements/planning documents, other agents' reports, Git operations.

## 5. Files inspected

All inspected during **this** execution with the read/glob tools:

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section report format; evidence markers; execution-history convention (Section 6: archive before replace, zero-padded numbering) |
| `agent-reports/T1.3/architect-proposal.md` (pre-replacement) | VERIFIED | Revision 2, 485 lines, execution `T1.3-EXEC-002-ARCH-01` — the predecessor archived by this execution; source of carried-forward verified facts |
| `agent-reports/T1.3/` (directory) | VERIFIED | Contained **only** `architect-proposal.md` at execution start; no `history/` existed yet |
| `agent-reports/decisions.md` | VERIFIED | D-01…D-05 approved; no T1.3 approval recorded; Section 4 rows for T1.3+ NOT AVAILABLE. **Not modified by this execution** (not this agent's file) |
| `src/main/resources/application.yml` | VERIFIED | Still bootstrap-minimal: `spring.application.name: human-it-challenge` only |
| Glob `src/main/resources/**` | VERIFIED | Only `application.yml` exists — no `application-dev.yml`, no `application-prod.yml`, no `META-INF/spring.factories` |
| `.opencode/agents/architect.md` | VERIFIED | `edit` allow for `agent-reports/**` only; `bash: deny`; mandatory reporting rules incl. archival duty |
| `implementation-plan.md` (lines 108–118) | VERIFIED | T1.3 scope: profiles, externalized credentials, JWT secret config, demo-user settings, max page size + pagination defaults; acceptance criteria; out of scope: database schema and domain code |
| `requirements-decisions.md` (Section 12, lines 549–615) | VERIFIED | Dev: H2, Spring profile `dev`; Prod: PostgreSQL, Spring profile `prod`, externalized connection credentials |
| `pom.xml`, `requirements.md` | NOT re-inspected this execution | Dependency facts cited from Revision 2 Section 5 (VERIFIED there): Spring Boot 3.5.16, Java 21, starters web/data-jpa/validation/security/oauth2-resource-server, H2+PostgreSQL drivers, Flyway, springdoc; **no** `spring-boot-configuration-processor` yet |

## 6. Files changed

| File | Change |
|---|---|
| `agent-reports/T1.3/history/architect-001.md` | **Created** — unchanged archival copy of Revision 2 (`T1.3-EXEC-002-ARCH-01`), written before the primary was replaced, per `agent-reports/README.md` Section 6; persistence verified by full read-back (Section 8.3) |
| `agent-reports/T1.3/architect-proposal.md` | **Replaced** by this Revision 3 (execution `T1.3-EXEC-003-ARCH-01`); persistence verified by full read-back (Section 8.6) |

No other file was touched. No application code, no `pom.xml`, no `decisions.md`, no requirements/planning documents, no other agent's reports, no Git operations.

## 7. Commands executed

**None.** `bash` is denied for this agent (VERIFIED in `.opencode/agents/architect.md`), and this execution is design-only; no command execution was required or attempted. All prior build/test evidence is cited from existing reports, not re-executed.

## 8. Evidence and observed results

### 8.1 Current configuration state (VERIFIED)

`src/main/resources/application.yml` contains only `spring.application.name: human-it-challenge`. No profile mechanism, no secrets, no pagination configuration exists anywhere yet. No `application-dev.yml` / `application-prod.yml` / `META-INF/spring.factories` exists (glob VERIFIED). The T1.1/T1.2 baseline is the starting state; no T1.3 implementation exists.

### 8.2 Tech Lead tasking for this execution (VERIFIED — direct tasking)

The ten mandatory changes and the six approved decision references (Section 4) were received directly via Tech Lead tasking in the current session. They are recorded here as tasking evidence, **not** as self-recorded approvals in `decisions.md` (append-only log, not this agent's file). Note on identifiers: these OD references use the **Tech Lead's** numbering and supersede Revision 2's own open-decision numbering OD-01…OD-04 (finding F-05, Section 9).

### 8.3 Archival verification (VERIFIED — read-back)

`agent-reports/T1.3/history/architect-001.md` was written before the primary report was replaced and read back in full within this execution. Content matches the pre-replacement Revision 2 primary as read at execution start: 485 lines, unchanged text, original header/execution ID `T1.3-EXEC-002-ARCH-01` and date retained (README Section 6 requirement). No silent overwrite occurred.

### 8.4 Carried-forward dependency facts (VERIFIED in Revision 2, `pom.xml` inspected there)

- `spring-boot-starter-validation` present → Bean Validation can enforce constraints on `@ConfigurationProperties` records without a new dependency.
- `spring-boot-starter-oauth2-resource-server` present → `com.nimbusds:nimbus-jose-jwt` transitively available for **future** Phase 3 work only; T1.3 must not use it.
- `spring-boot-configuration-processor` absent → O-01 addition is a Developer-executed `pom.xml` change under the approved scope.

### 8.5 Corroboration limitation (PENDING VERIFICATION)

Everything proposed here is a design claim until implemented and tested under an approved execution. In particular: native default-profile activation semantics (`spring.profiles.default` honored only when no active profiles are set; not declarable in profile-specific files), all secret-validation failure paths, the pagination-ceiling bind failure, and the demo-user profile gating are **PENDING VERIFICATION** via the test matrix (Appendix F).

### 8.6 Report persistence check (per Tech Lead instruction)

The write of this Revision 3 to `agent-reports/T1.3/architect-proposal.md` **succeeded** (no permission error returned) and this exact file was **read back in full** within this execution. **VERIFIED: the revised report is persisted at the target path.** No permission bypass was used; `edit` is allowed for `agent-reports/**` by the agent definition, so no blocker exists. Only the two files in Section 6 were written.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-01 | Major (resolved by this revision) | Revision 2 mandated explicit profile activation with no default and a no-profile fail-fast `EnvironmentPostProcessor` guard. Superseded by Tech Lead decisions OD-01 (mechanism rejected) and OD-02 (default dev profile approved). Revision 3 removes all guard machinery and all no-profile fail-fast requirements (Sections 10.1, 10.8; Appendices A, B, F, G). |
| F-02 | **Major — accepted residual risk (documented per mandatory change 5)** | With `spring.profiles.default: dev`, a production deployment that omits `SPRING_PROFILES_ACTIVE=prod` **silently starts as dev**: `application-dev.yml` loads, DEBUG logging activates, the dev-gated demo-user bean (Phase 3 provisioning) becomes registrable with known credentials, and — from T1.4 onward — an in-memory H2 datasource replaces PostgreSQL. No technical guard exists by explicit Tech Lead decision. Mitigations are operational only (Section 10.2, Appendix B.2, risk RK-01). This is the deliberate, accepted price of OD-02; it is recorded, not hidden. |
| F-03 | Minor (behavior documented, no guard) | `SPRING_PROFILES_ACTIVE=dev,prod` (both) is not rejected by any mechanism: both profile files load and the `@Profile("dev")` demo-user configuration registers. Revision 2 fail-fasted this case; Revision 3 cannot. Documented as behavior test T-M20 and residual-risk row (Appendix B.1). |
| F-04 | Minor (deferral, accepted) | README (T1.6) must document: production requires `SPRING_PROFILES_ACTIVE=prod` plus the profile-check runbook step; dev requires `APP_SECURITY_JWT_SECRET` and demo-user variables. Interim friction until T1.6 (OT-04). |
| F-05 | Minor (identifier collision, resolved in this report) | Revision 2's open decisions OD-01…OD-04 (guard mechanism, contract strictness, secret representation, startup logging) collide with the Tech Lead's approved-decision identifiers OD-01…OD-04 (EPP rejected; default dev approved; Base64-only approved; logging deferred). This report uses the **Tech Lead's meanings** exclusively; Revision 2 numbering is marked "Rev-2 OD" where referenced. |
| F-06 | Minor (edge case, PENDING VERIFICATION) | `SPRING_PROFILES_ACTIVE` set but **empty** (`""`) is an untested edge of the default-profile mechanism. Expected per Spring Boot semantics: treated like unset → default dev applies. To be confirmed (or declared out of scope) by the Tester after implementation (OT-05). |

Zero Critical findings. One Major finding (F-02) remains open by design — it is the residual risk the Tech Lead's OD-02 decision explicitly accepted and mandatory change 5 requires documented; it is not a defect of this revision.

## 10. Decisions and reasoning (alternatives, trade-offs, assumptions)

Concrete artifacts (exact files, snippets, test matrix, acceptance criteria) are in Appendices A–I.

### 10.1 Changes 1–3 — Native default dev profile; guard machinery removed

**Decision:** the common `application.yml` sets `spring.profiles.default: dev` (mandatory change 1). When no active profile is configured, Spring Boot natively activates `dev` and loads `application-dev.yml` — **no custom bootstrap code**. Production **must** set `SPRING_PROFILES_ACTIVE=prod` (mandatory change 2) via environment variable, or the documented equivalents `--spring.profiles.active=prod` / `-Dspring.profiles.active=prod` / container env; while any active profile is set, `spring.profiles.default` is ignored by Spring Boot, so an explicit prod activation cannot be diluted by the default (PENDING VERIFICATION, T-M19).

**Removed from the design and forbidden in implementation:**

- `config/bootstrap/RequiredProfileEnvironmentPostProcessor` and the whole `bootstrap/` package (OD-01).
- `src/main/resources/META-INF/spring.factories` (OD-01).
- All no-profile fail-fast requirements, tests and acceptance criteria (Revision 2 Sections 10.1, Appendix B rows, T-M8, G.1 criterion 3).

**Why this is acceptable:** Revision 2's guard existed solely to prevent a silent dev-like start. OD-02 accepts that cost explicitly in exchange for standard Spring Boot behavior: zero custom bootstrap classes, one less `META-INF` registration, one less framework extension to maintain and audit. The only fail-fast retained is at the **property** level (Section 10.3) — which is exactly what mandatory change 6 requires.

**Alternatives considered:**

- *Keep the `EnvironmentPostProcessor` guard alongside the default:* rejected — OD-01 rejects the mechanism outright; a guard plus a default would also be contradictory (the guard would fire precisely when the default is meant to apply).
- *Default `prod`:* rejected — a forgotten profile must never default toward the production database and prod secrets handling; if a default exists, dev is the only safe direction (and is the Tech Lead's decision).
- *No default + startup failure (Revision 2):* rejected by OD-02.
- *`spring.profiles.default` in a profile-specific file:* not valid — Spring Boot does not allow `spring.profiles.default` in profile-specific documents; the common `application.yml` is the only correct committed location (PENDING VERIFICATION with implementation).

**Framework behavior note:** Spring Boot logs the active profile(s) at startup with its native INFO line ("The following 1 profile is active: …"). This is **framework behavior, not** the custom startup logging deferred by OD-04 (Section 10.7); it remains the primary operational verification signal (mitigation M-2, Appendix B.2).

### 10.2 Change 5 — Residual risk of accidental dev startup in production (documented)

**Risk RK-01 (F-02):** a production deployment launched without `SPRING_PROFILES_ACTIVE=prod` starts as **dev** silently.

| Consequence | Impact | Applies from |
|---|---|---|
| R1 — demo user provisionable with **known credentials** in a production-facing deployment (dev-gated Phase 3 bean registers) | Highest | Phase 3 (T3.2) |
| R2 — in-memory **H2** datasource instead of PostgreSQL → silent data loss on restart, dev schema dialect | High | T1.4 |
| R3 — DEBUG logging for `com.humanit.challenge` → log noise, higher accidental sensitive-detail exposure | Medium | T1.3/T1.5 |
| R4 — any future dev/prod-divergent property resolves to its dev value | Medium | as the property set grows |

**Operational mitigations (no technical guard exists by Tech Lead decision):**

- M1 — Phase 5 `compose.yaml`/Docker definitions set `SPRING_PROFILES_ACTIVE: prod` explicitly; reviewed by the Reviewer for exactly that line (OT-07).
- M2 — Runbook/README step: operator verifies the native startup log line shows `prod` active, e.g. "The following 1 profile is active: prod" (T1.6; framework logging, not OD-04 logging).
- M3 — Property-level fail-fast (Section 10.3): an **unconfigured** accidental dev start still aborts on the missing JWT secret. Partial mitigation only — dev also requires the secret, so an operator who supplied it and forgot the profile gets a dev start.
- M4 — Demo-user values stay env-only and dev-gated; no demo-user key exists in prod or common YAML (Reviewer grep audit retained, Appendix D).
- M5 — Production deployment checklist includes the profile check (documentation; T1.6 / Phase 5).

Any future code-level safeguard (e.g., a prod-only self-check) is a **new proposal** requiring Tech Lead approval — deliberately not reintroduced here.

### 10.3 Change 6 — Production secrets externalized and fail-fast (retained)

- `app.security.jwt.secret` is **env-only in both profiles** (`APP_SECURITY_JWT_SECRET`; relaxed binding → `app.security.jwt.secret`). No committed value anywhere.
- Absent, blank, malformed Base64, or decoded < 32 bytes → **context startup failure in both profiles** via Bean Validation on the constructor-bound record. This property-level fail-fast is the only fail-fast in the T1.3 design (profile-level fail-fast removed per mandatory change 3).
- `app.security.jwt.expiration-minutes`: `int`, committed default **60** (O-02), `@NotNull` + `@Min(1)`.
- Failure messages name the property and failure kind only — never the secret value or content (decoded **length** is the sole reported quantity, e.g. "decoded to 24 bytes; minimum 32").

### 10.4 Change 7 — Production demo-user protections (retained)

- `AppDemoUserProperties` (`@Validated` record: `@NotBlank username`, `@NotBlank password`) is bound **only** via `@EnableConfigurationProperties` inside a `@Profile("dev")` `@Configuration` — in prod the bean does not exist, independent of which env vars are present.
- Credential values env-only in dev (`APP_SECURITY_DEMO_USER_USERNAME` / `..._PASSWORD`); nothing in committed YAML; prod and common YAML contain **no** demo-user keys (Reviewer grep audit retained).
- Actual provisioning (dev-only bean, BCrypt hash at provision time) is Phase 3 (T3.2); T1.3 provides only the externalized, dev-scoped property plumbing.
- **Residual-risk nuance (honest boundary of the protection):** the gating is profile-based, and the profile can now be dev *by default* — under an accidental dev start (F-02/R1) or a `dev,prod` combined activation (F-03) the dev-gated bean registers. The protection is therefore only as strong as the operational mitigations M1–M5; this is exactly the accepted risk of OD-02, recorded here so the Reviewer and Tech Lead see the protection's real boundary, not an overstated one.

### 10.5 Change 8 — Base64 JWT secret validation using decoded bytes (retained; OD-03 approved)

- Single representation: **standard Base64 (RFC 4648)** — no MIME tolerance, no whitespace, no hex (OD-03 closes Revision 2's "can be reopened" caveat).
- Validation **decodes and counts bytes**; `String.length()` is never inspected:
  1. `@NotBlank` — missing/blank → startup failure.
  2. `Base64.getDecoder()` (strict) — malformed input → validation failure → startup failure.
  3. `@Base64DecodedMinBytes(32)` — decoded length ≥ **32 bytes (256 bits)**, the RFC 7518 HMAC-SHA-256 floor. A 32-character Base64 string decodes to 24 bytes and is **rejected**; the minimal valid form is 43 chars encoding exactly 32 bytes; the recommended generation target is 64 random bytes (512 bits, 88 Base64 chars) for HS512 headroom.
- Phase boundary unchanged: no encoder, decoder, filter, `SecretKey` bean, algorithm choice or `/auth/login` in T1.3; nimbus dependency already on the classpath for Phase 3; an RSA/ECDSA choice in Phase 3 redesigns the property set there with approval.
- Operational guidance (README, T1.6): `openssl rand -base64 64`; `export APP_SECURITY_JWT_SECRET=...`.

### 10.6 Change 9 — Pagination defaults and the maximum of 100 (retained)

| Property | Type | Committed default | Constraints |
|---|---|---|---|
| `app.pagination.default-page` | `Integer` | `0` | `@NotNull`, `@Min(0)` |
| `app.pagination.default-page-size` | `Integer` | `20` | `@NotNull`, `@Min(1)` |
| `app.pagination.max-page-size` | `Integer` | `100` | `@NotNull`, `@Min(1)`, `@Max(100)` (hard ceiling) |

- `@Max(100)` binds at `@ConfigurationProperties` bind time: any environment override above 100 (`APP_PAGINATION_MAX_PAGE_SIZE=150` etc.) **fails startup** — the increase is loud and impossible, never silent.
- Class-level cross-field constraint `@ValidPaginationInvariants` enforces `defaultPageSize ≤ maxPageSize`.
- Trade-off unchanged: the value stays metadata-visible (O-01 processor), the ceiling is **code-owned**; raising it requires a code change, a proposal and Tech Lead approval.
- HTTP enforcement (400 on `size > 100`, defaults for absent params, ID-ascending ordering) stays in **T2.8**; T1.3 only establishes the validated configuration.

### 10.7 Change 10 — No optional startup INFO logging (OD-04 deferred)

- The T1.3 implementation must **not** include the custom "effective property values" startup logger proposed as Rev-2 OD-04. Recorded as boundary **BK-08** (Appendix G.2).
- Native framework startup logging is unaffected — including the active-profile INFO line relied on by mitigation M-2. If the observability idea is revisited later, it is a separate Tech Lead decision (OT-08).

### 10.8 Change 4 — Profile startup tests updated

- **Removed:** Revision 2's T-M8 ("no active profile → context fails") and the no-profile fail-fast acceptance criterion — the guard no longer exists, so a no-profile context now **succeeds** via the default dev profile.
- **Replaced by T-M8R:** a context with no `@ActiveProfiles` starts with `Environment.getActiveProfiles()` containing exactly `dev` and not `prod`, with `application-dev.yml` settings visible — proving OD-02.
- **New T-M19:** explicit `@ActiveProfiles("prod")` yields active profiles exactly `[prod]` — the default does not leak `dev` in.
- **New T-M20 (behavior documentation, not pass/fail of a guard):** `@ActiveProfiles({"dev","prod"})` loads both files and registers the dev-gated demo-user bean — documenting F-03 and the residual risk.
- **T-M18 inverted:** source scan requires `spring.profiles.default` **exactly once**, in `application.yml` only; zero `META-INF/spring.factories`; zero `EnvironmentPostProcessor` classes; no committed `spring.profiles.active` anywhere.
- **Bootstrap test:** `HumanItChallengeApplicationTests` keeps **no** `@ActiveProfiles` annotation (previously Revision 2 planned `@ActiveProfiles("dev")`) so it exercises the native default-dev path end-to-end, and gains `@DynamicPropertySource`-supplied JWT secret + demo-user values (mandatory in dev too). All tests remain **PENDING VERIFICATION** (Appendix F).

### 10.9 Preserved design elements (compatibility against the tasking list)

| Element | Status in Revision 3 |
|---|---|
| `application.yml` / `application-dev.yml` / `application-prod.yml` triplet | Retained; common file now additionally owns `spring.profiles.default: dev` |
| Immutable `@ConfigurationProperties` records, constructor-bound, `@Validated` | Retained (pagination, jwt, demo-user) |
| Property validation | Retained unchanged (byte-count constraint, hard ceiling, cross-field invariant) |
| Secrets externalized, fail-fast (prod emphasis) | Retained — change 6 |
| Production demo-user protection | Retained — change 7, with honest boundary note (10.4) |
| Base64 decoded-bytes secret validation | Retained — change 8, OD-03 approved |
| Pagination 0/20/100 + `@Min(1)` | Retained — change 9 |
| Independent configuration tests | Retained — matrix updated per change 4 |
| Strict separation from T1.4 | Retained — zero datasource/Flyway/`ddl-auto` settings proposed |
| `spring-boot-configuration-processor` (O-01) | Retained — the only permitted `pom.xml` change |
| `expiration-minutes` default 60 (O-02) | Retained |

### 10.10 Assumptions

1. The T1.1/T1.2 baseline is the starting state; no T1.3 implementation exists (VERIFIED 8.1).
2. `requirements-decisions.md` and `implementation-plan.md` remain authoritative; no requirement-level decision is reopened here (the profile mechanism is an architecture decision within T1.3's "Spring profiles dev and prod" scope).
3. The Tech Lead's ten mandatory changes and six decision references are tasking evidence, **not** a self-recorded approval of this proposal; approval comes only from the human Tech Lead.
4. Spring Boot 3.5.x constructor-bound records validate JSR-380 constraints on record components (PENDING VERIFICATION, T-M1/T-M3…T-M7; fallback: programmatic post-bind check with identical behavior).
5. `spring.profiles.default` semantics: honored only when no active profiles are set; not declarable in profile-specific files; ignored (not merged) when `SPRING_PROFILES_ACTIVE` is set (PENDING VERIFICATION, T-M1/T-M8R/T-M19; edge case F-06/OT-05).

## 11. Final status

**DELIVERED — REVISED PROPOSAL (REVISION 3) PERSISTED, AWAITING EXPLICIT TECH LEAD IMPLEMENTATION APPROVAL.**

- Revision 3 persisted at `agent-reports/T1.3/architect-proposal.md` (VERIFIED by read-back, Section 8.6); Revision 2 archived unchanged at `agent-reports/T1.3/history/architect-001.md` (VERIFIED, Section 8.3).
- All ten mandatory changes are addressed (Sections 10.1–10.8; Appendices A–I); all six approved decisions are incorporated (Section 4).
- **Implementation is NOT authorized and NOT performed.** No application code, YAML, `pom.xml` or `decisions.md` change; no Git operations; nothing beyond the two report files in Section 6 was written.
- This proposal is **PROPOSED** — only the human Tech Lead can approve it and authorize T1.3 implementation.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Awaiting approval | Explicit Tech Lead approval of this Revision 3 proposal before any implementation begins | Tech Lead |
| OT-02 | PENDING VERIFICATION | Record-validation behavior on `@ConfigurationProperties` records (A4) | Developer + Tester after approval |
| OT-03 | PENDING VERIFICATION | All failure paths (secret absence/blank/malformed/short, pagination overrides) and default-profile semantics (T-M1, T-M8R, T-M19) are design claims until implemented and tested | Tester after implementation |
| OT-04 | Deferred (accepted) | README (T1.6): prod requires `SPRING_PROFILES_ACTIVE=prod` + profile-check runbook step (M-2); dev requires JWT secret + demo-user env vars (F-04) | Developer (T1.6) |
| OT-05 | PENDING VERIFICATION (edge) | Empty `SPRING_PROFILES_ACTIVE=""` semantics (F-06): confirm via test or declare out of scope | Tester after implementation |
| OT-06 | Standing governance | `decisions.md` rows for T1.3 approvals remain NOT AVAILABLE until real approvals occur (append-only log; not this agent's file) | Tech Lead |
| OT-07 | Forward boundary | Phase 5 `compose.yaml` must set `SPRING_PROFILES_ACTIVE: prod` explicitly (mitigation M-1, RK-01) | Developer (Phase 5), Reviewer check |
| OT-08 | Deferred decision | OD-04 optional startup logging may be revisited by the Tech Lead in a separate decision | Tech Lead |

## 13. Open decisions (summary)

**None open for T1.3.** The Tech Lead's tasking resolved all previously open items: OD-01 (EnvironmentPostProcessor) rejected; OD-02 (default dev profile) approved; OD-03 (Base64-only) approved; OD-04 (optional startup logging) deferred — deferred, not decided, and excluded from the T1.3 scope. No new open decisions are raised by this revision; the only substantive judgment call left to the Tech Lead is **approval of the residual risk F-02/RK-01 as documented** (Section 10.2).

---

# Appendices — concrete proposal artifacts

*(The appendices add detail; the thirteen mandatory sections above were not reordered or omitted.)*

## Appendix A — Configuration architecture and exact proposed files

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
      @Profile("dev")
      @EnableConfigurationProperties(AppDemoUserProperties.class)
  AppPaginationProperties.java                                   (create record — Appendix E)
  AppJwtProperties.java                                          (create record — Appendix C)
  AppDemoUserProperties.java                                     (create record — dev-profile-gated registration)
  validation/
    Base64DecodedMinBytes.java                                   (create constraint annotation)
    Base64DecodedMinBytesValidator.java                          (create)
    ValidPaginationInvariants.java                               (create class-level constraint)
    ValidPaginationInvariantsValidator.java                      (create)
src/test/java/com/humanit/challenge/config/                      (create — Appendix F)
  (test classes per matrix)
src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java
                                                                  (modify: NO @ActiveProfiles — proves the default-dev
                                                                   path; gains @DynamicPropertySource secret/demo values)
```

**Deliberately ABSENT (mandatory change 3):** `src/main/resources/META-INF/spring.factories` — not created; any `EnvironmentPostProcessor`/bootstrap package — not created. Audit item T-M18 enforces both absences.

**No** other files are modified: no entity/repository/controller code, no datasource/Flyway config, no `README.md` (T1.6), no security filter chain (Phase 3).

### A.2 Property tree summary

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
    demo-user:               # DEV-PROFILE REGISTRATION ONLY
      username: <env>        # APP_SECURITY_DEMO_USER_USERNAME
      password: <env>        # APP_SECURITY_DEMO_USER_PASSWORD
```

## Appendix B — Profile startup behavior (Changes 1–3, 5)

### B.1 Behavior table

| Scenario | Behavior (Revision 3 — no guard exists) | Notes |
|---|---|---|
| No profile set (local `mvnw spring-boot:run`, tests without `@ActiveProfiles`) | **Default `dev` applies natively**; `application-dev.yml` loads; demo-user bean registrable; JWT secret required | OD-02; proven by T-M1/T-M8R |
| `SPRING_PROFILES_ACTIVE=prod` | prod file loads; demo-user bean **not** registered; secrets required | Mandatory change 2; default ignored (PENDING VERIFICATION T-M19) |
| `SPRING_PROFILES_ACTIVE=dev` | Same as default (explicit) | Equivalent to no-profile start |
| `SPRING_PROFILES_ACTIVE=dev,prod` | **Both files load; `@Profile("dev")` demo bean registers** — ambiguity NOT rejected (no guard) | F-03, documented by T-M20 |
| `SPRING_PROFILES_ACTIVE=qa` | Only common file loads; no profile file for `qa`; app starts if secret provided | Behavior change vs Revision 2 (was fail-fast); operational exposure, RK-01 |
| Empty `SPRING_PROFILES_ACTIVE=""` | Expected: treated as unset → default dev (PENDING VERIFICATION) | F-06 / OT-05 |
| Test without `@ActiveProfiles` | **Succeeds** with default dev (previously failed under Revision 2) | Test update, change 4 |
| Any committed `spring.profiles.active` in YAML | **None** — forbidden; audit T-M18 | Active profiles come from deployment env only |
| `spring.profiles.default` committed | **Exactly once**, in `application.yml` (not in profile-specific files — not permitted there) | Audit T-M18 |

Equivalence clause: `SPRING_PROFILES_ACTIVE`, `--spring.profiles.active` and `-Dspring.profiles.active` are the documented, equivalent production activation mechanisms; the recommendation is the environment variable for all deployment tooling.

### B.2 Residual-risk register (mandatory change 5)

| Risk | Consequence | Mitigations | Residual |
|---|---|---|---|
| RK-01 production start without `SPRING_PROFILES_ACTIVE=prod` (F-02) | R1 demo user with known credentials (Phase 3); R2 in-memory H2 instead of PostgreSQL (T1.4); R3 DEBUG logging; R4 dev-valued divergent properties | M1 compose/prod tooling sets prod explicitly; M2 startup-log profile check in runbook; M3 property fail-fast aborts unconfigured starts; M4 demo keys dev-gated + env-only; M5 deployment checklist | **Accepted by Tech Lead decision (OD-02)**; severity Major; any technical safeguard = new proposal |
| RK-02 T1.4 adds datasource config on top of default-dev | Dev auto-config must keep working; prod datasource must never silently load under dev default | T1.4 proposal review against this profile contract | Standard task boundary |
| RK-03 empty-active-profile edge (F-06) | Ambiguous behavior if tooling exports an empty variable | Confirm by test (OT-05) or document | Low |

## Appendix C — Secret handling and validation (Change 6, 8)

### C.1 Representation contract (single, unambiguous — OD-03 approved)

`app.security.jwt.secret` = **standard Base64 (RFC 4648)** of ≥ 32 random bytes (256 bits); recommended generation 64 bytes (88 Base64 chars) for HS512 headroom. A 32-character Base64 string decodes to exactly 24 bytes → below the 256-bit floor → **rejected** even though character count is 32. The validator never inspects `String.length()`; it decodes and counts bytes (demonstrated by unit test T-M9).

### C.2 Registration and validation snippet (proposed shape — implementation only after approval)

```java
// AppJwtProperties (record, constructor-bound, validated)
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record AppJwtProperties(
        @NotBlank
        @Base64DecodedMinBytes(32)           // custom constraint: decode + byte-length
        String secret,
        @NotNull
        @Min(1)
        Integer expirationMinutes            // committed default 60 (O-02)
) {}
```

`Base64DecodedMinBytesValidator.isValid(value, ctx)`:
1. `value` null/blank → invalid.
2. decode with `Base64.getDecoder()` (strict, no MIME/URL-safe); `IllegalArgumentException` → invalid ("secret is not valid Base64").
3. `decoded.length < minBytes` → invalid ("decoded to N bytes; minimum M required").
4. No message ever exposes the secret value or decoded content — length only.

### C.3 Externalization and failure posture (change 6)

- Dev **and** prod: `export APP_SECURITY_JWT_SECRET=<base64>`; no committed value in either profile.
- Absent / blank / non-Base64 / decoded < 32 bytes → **startup failure in both profiles** (property-level fail-fast; the only fail-fast retained). PENDING VERIFICATION (T-M3…T-M7).
- Prod emphasis per mandatory change 6: the externalized-credentials requirement of `requirements-decisions.md` Section 12 is honored; `application-prod.yml` contains no secret and no credential material.

### C.4 What T1.3 does **not** do

No encoder, decoder, filter, `SecretKey` bean, algorithm selection or `/auth/login` — Phase 3 (T3.1) owns those, with nimbus already on the classpath. A Phase 3 RSA/ECDSA choice redesigns the property set there with approval.

## Appendix D — Demo-user safety in production (Change 7)

- `AppDemoUserProperties` (`@Validated` record: `@NotBlank username`, `@NotBlank password`) is registered **only** by a `@Profile("dev")` configuration class — in prod the bean does not exist, independent of env vars present.
- Credential **values** env-only in dev (`APP_SECURITY_DEMO_USER_USERNAME` / `..._PASSWORD`); nothing in committed YAML; zero demo-user keys in `application-prod.yml` and the common file (Reviewer grep audit).
- Phase 3 (T3.2) implements provisioning as a dev-only bean and stores a BCrypt hash at provision time; T1.3 provides only the externalized, dev-scoped plumbing.
- **Boundary of the protection (honest statement):** the gating is profile-based and dev is now the *default* profile — under RK-01 (accidental dev start) or a `dev,prod` activation (F-03) the bean registers. The protection is real for correctly-activated prod deployments and is exactly as strong as mitigations M1–M5 elsewhere; this boundary is the accepted risk of OD-02, not a hidden weakness.

## Appendix E — Pagination invariants (Change 9)

```java
@Validated
@ValidPaginationInvariants                    // class-level: defaultPageSize <= maxPageSize
@ConfigurationProperties(prefix = "app.pagination")
public record AppPaginationProperties(
        @NotNull @Min(0)               Integer defaultPage,      // 0
        @NotNull @Min(1)               Integer defaultPageSize,  // 20
        @NotNull @Min(1) @Max(100)     Integer maxPageSize       // 100 — hard ceiling
) {}
```

| Invariant | Enforced by | When violated |
|---|---|---|
| `defaultPageSize ≥ 1` | `@Min(1)` | startup failure |
| `maxPageSize == 100` as committed default | common `application.yml` | — (single source) |
| `maxPageSize ≤ 100` even if overridden | `@Max(100)` at bind time | **startup failure — increase is loud, never silent** |
| `defaultPageSize ≤ maxPageSize` | `@ValidPaginationInvariants` | startup failure |
| `defaultPage ≥ 0` | `@Min(0)` | startup failure |

The ceiling is **code-owned**: raising it legitimately requires a code change, a proposal and Tech Lead approval. HTTP enforcement remains T2.8.

## Appendix F — Test matrix (Change 4; independent configuration tests)

All tests are expected to be executable via `.\mvnw.cmd test` after an approved implementation; all are **PENDING VERIFICATION**. Tests are designed and run by the Tester **before** reading the Developer report (independence, `agent-reports/README.md` Section 8).

| ID | Test | Setup | Expected outcome |
|---|---|---|---|
| T-M1 | Context loads with **no** `@ActiveProfiles` (default dev) | no profile annotation + `@DynamicPropertySource` JWT secret (valid ≥32-byte Base64) + demo-user values | Context loads; `getActiveProfiles()` contains `dev`, not `prod` — proves OD-02 |
| T-M2 | Defaults bound | same context as T-M1 | `default-page 0`, `default-page-size 20`, `max-page-size 100`, `expiration-minutes 60` |
| T-M3 | No secret → fail | dev (default), NO secret source | startup **fails** (property named, no secret value in message) |
| T-M4 | Blank secret → fail | secret `""` | startup fails |
| T-M5 | Malformed Base64 → fail | secret `not!valid` | startup fails ("not valid Base64") |
| T-M6 | <32 decoded bytes → fail | Base64 of a 24-byte array (32 chars) | startup fails; message reports decoded length |
| T-M7 | 32-byte boundary accepted | Base64 of exactly 32 bytes (43 chars) | binds successfully |
| T-M8R | Default-dev semantics (replaces removed fail-fast T-M8) | context with no active profiles | starts with profile set exactly `[dev]`; `application-dev.yml` settings visible |
| T-M9 | Chars ≠ bytes demonstration | unit test of `Base64DecodedMinBytesValidator`: 32-char → invalid (24 bytes); 43-char → valid (32 bytes); 88-char (64 bytes) → valid | as stated |
| T-M10 | `max-page-size=150` override → fail | `app.pagination.max-page-size=150` | startup fails (`@Max(100)`), never "silently increased" |
| T-M11 | `default-page-size=0` → fail | `app.pagination.default-page-size=0` | startup fails (`@Min(1)`) |
| T-M12 | `default-page=-1` → fail | negative page | startup fails (`@Min(0)`) |
| T-M13 | `default-page-size > max-page-size` → fail | e.g. size 120 vs max blocked at 100; also a hypothetical smaller max (10 vs 20) | startup fails (cross-field invariant) |
| T-M14 | `expiration-minutes=0` → fail | | startup fails (`@Min(1)`); default 60 confirmed in T-M2 |
| T-M15 | Demo-user dev-only binding | dev/default: properties present → record binds; **prod:** no `AppDemoUserProperties` bean exists regardless of env presence | as stated |
| T-M16 | Prod happy path (config-only) | `@ActiveProfiles("prod")` + env-style secret | context loads; jwt + pagination bound; no demo-user bean |
| T-M17 | Prod without secret → fail | prod, no overrides | startup fails |
| T-M18 | Source-scan audit (inverted from Revision 2) | static scan | `spring.profiles.default` **exactly once**, in `application.yml` only; **zero** `spring.factories`; **zero** `EnvironmentPostProcessor`; no committed `spring.profiles.active` |
| T-M19 | Explicit prod suppresses default | `@ActiveProfiles("prod")` | active profiles exactly `[prod]`; no `dev` leakage; `application-dev.yml` settings absent |
| T-M20 | `dev,prod` combined activation (behavior documentation) | `@ActiveProfiles({"dev","prod"})` | both files load; demo bean registers — documents F-03/RK-01; **no failure expected** (no guard exists) |
| T-M21 | Empty `SPRING_PROFILES_ACTIVE=""` edge (optional, if feasible in test harness) | environment simulation | behavior recorded; expected default dev (F-06/OT-05) |

Bootstrap `HumanItChallengeApplicationTests` (updated): **no** `@ActiveProfiles`; gains dynamic JWT-secret/demo-user properties → exercises the native default-dev path end-to-end (T-M1 equivalent at the bootstrap level).

## Appendix G — Acceptance criteria and implementation boundaries

### G.1 Acceptance criteria (proposed, for Tech Lead approval to adopt)

1. `dev` and `prod` settings are separated across `application-dev.yml` / `application-prod.yml` per Appendix A.
2. `application.yml` (common) sets `spring.profiles.default: dev`; with no active profile the application starts as `dev` (T-M1/T-M8R).
3. Production deployment documentation and comments state: production **must** start with explicit `SPRING_PROFILES_ACTIVE=prod`; `--spring.profiles.active=prod` / `-D…` are documented equivalents (T1.6/Phase 5 own the deployment tooling).
4. **No** `EnvironmentPostProcessor`, **no** `META-INF/spring.factories`, and **no** no-profile fail-fast requirement exists anywhere in code, config or tests (T-M18).
5. The residual accidental-dev-startup risk is documented (this proposal Section 10.2 / Appendix B.2) and carried into README (T1.6) and Phase 5 tooling (OT-04/OT-07).
6. The JWT secret is externalized (no committed value in dev or prod), validated on **decoded byte length ≥ 32**, and absent/blank/malformed/too-short values fail startup; `app.security.jwt.expiration-minutes` defaults to 60.
7. No JWT encoder/decoder/filter/algorithm code exists in the delivered T1.3 code.
8. Pagination defaults bind to 0/20/100; `defaultPageSize` has `@Min(1)`; overrides raising `maxPageSize` above 100 fail startup; no HTTP pagination logic (T2.8 boundary kept).
9. Demo-user properties are dev-profile-gated, env-sourced, absent from prod binding; no demo user is provisionable in prod from T1.3 artifacts.
10. `spring-boot-configuration-processor` is added (O-01) and produces metadata for the `app.*` tree.
11. Configuration records are immutable Java records with constructor binding.
12. All independent configuration tests (Appendix F) pass via Maven, before any Developer-report reading.
13. Zero datasource/Flyway/Hibernate `ddl-auto` settings are introduced (T1.4 boundary).

### G.2 Implementation boundaries (hard)

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
| RK-01 | Risk: accidental dev startup in production (documented, accepted — Section 10.2, Appendix B.2) |
| RK-02 | Risk: T1.4 must respect the default-profile contract while adding datasource config |
| RK-03 | Edge: empty `SPRING_PROFILES_ACTIVE` semantics (OT-05) |

## Appendix H — Report-persistence and archival traceability

| Item | Status |
|---|---|
| Archival of predecessor | `agent-reports/T1.3/history/architect-001.md` created **before** replacement; unchanged copy of Revision 2 (`T1.3-EXEC-002-ARCH-01`); full read-back VERIFIED (Section 8.3) |
| Primary report replacement | `agent-reports/T1.3/architect-proposal.md` replaced by Revision 3 (`T1.3-EXEC-003-ARCH-01`); full read-back VERIFIED (Section 8.6) |
| Write mechanism | `write`/`edit` tools only (no shell; none available); permitted by the agent definition for `agent-reports/**` — no blocker, no bypass, no permission change needed |
| Files authored | **Only** the two files in Section 6 (own report + own archive); agent-author rule respected: no `decisions.md`, no other agent's reports |
| Approval status | **No approval recorded or claimed** — only the human Tech Lead can approve; nothing was appended to `decisions.md` |

## Appendix I — Handoff summary

- **Tech Lead:** approve/reject Revision 3 (Sections 10–13, Appendices A–I) — in particular, confirm acceptance of the documented residual risk RK-01 (F-02); then explicitly authorize T1.3 implementation.
- **Developer (after explicit approval):** implement Appendix A exactly; add the O-01 processor only; respect boundaries BK-01…BK-08; do not create `spring.factories` or any bootstrap guard.
- **Tester:** execute Appendix F independently **before** reading the Developer report; pay specific attention to T-M1/T-M8R (default-dev proof), T-M19 (prod suppression), T-M18 (absence scan), T-M20 (combined-profile behavior) and OT-05 (empty-variable edge).
- **Reviewer:** audit the profile mechanism (single `spring.profiles.default` in the common file; no guard remnants), secret byte-length logic, pagination ceiling enforcement, demo-user gating boundary (Section 10.4), and boundary compliance including BK-08.

---

**End of revised proposal (Revision 3).** Final status and outstanding findings are consolidated in Sections 11–13.
