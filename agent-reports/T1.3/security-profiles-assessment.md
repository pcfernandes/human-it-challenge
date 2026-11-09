# Security/Profile Assessment — T1.3 Combined Spring Profiles (F-03 follow-up)

---

## 1. Task ID

**T1.3** — Environment configuration and secret externalization (Phase 1). Sub-analysis: **combined Spring profiles** and the proposed minimal guard change `@Profile("dev")` → `@Profile("dev & !prod")` on the dev-only demo-user configuration (execution ID `T1.3-EXEC-006-ARCH-01`).

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.3-EXEC-006-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Type | Profile/security analysis (design only; no implementation) |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot software architect)
- **Model:** `opencode/beam-free`
- **Agent definition:** `.opencode/agents/architect.md` — `edit` denied except `agent-reports/**`; `bash` denied (VERIFIED)

## 4. Authorized scope

Per Tech Lead tasking of 2026-11-09 (`T1.3-EXEC-006-ARCH-01`):

- Analyze the combined-profiles problem: `@Profile("dev")` matches when both `dev` and `prod` are active, registering demo-user configuration in a potentially production-like environment.
- Evaluate the minimal change `@Profile("dev & !prod")` (written in the tasking as `@Profile(dev" & "!prod)`).
- Evaluate the five cases: no active profile (default dev), `dev`, `prod`, `dev,prod`, `prod,dev`.
- Explain whether the proposed annotation prevents demo-user registration whenever `prod` is active, remaining risks, exact files and tests needing changes, and whether future demo-user provisioning needs the same guard.
- Write `agent-reports/T1.3/security-profiles-assessment.md` in the 13-section format.
- Mark unverified assumptions clearly.

**Out of scope:** any code change, test change, dependency change, approval, commit or push. No modification of existing reports or `decisions.md`.

## 5. Files inspected

All inspected this execution with read/glob/grep tools:

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format, evidence markers, archival convention |
| `agent-reports/T1.3/architect-proposal.md` (Revision 3, `T1.3-EXEC-003-ARCH-01`) | VERIFIED | F-03 finding, T-M20, Appendix B.1/B.2 residual risk, Appendix D demo-user gating, OD-01/OD-02 |
| `agent-reports/T1.3/developer-report.md` (`T1.3-EXEC-001-DEV-01`) | VERIFIED | F-03 root cause, CombinedDevProdProfilesTest evidence, OT-02/OT-03 governance |
| `agent-reports/T1.3/security-jwt-assessment.md` (`T1.3-EXEC-005-ARCH-01`) | VERIFIED | Sister assessment; reporting style and boundary conventions |
| `src/main/java/com/humanit/challenge/config/DevConfigPropertiesRegistration.java` | VERIFIED | Current `@Profile("dev")` (line 15) — the only change target |
| `src/main/java/com/humanit/challenge/config/ConfigPropertiesRegistration.java` | VERIFIED | Committed `@Configuration` (no profile) registering pagination + JWT |
| `src/main/java/com/humanit/challenge/config/AppDemoUserProperties.java` | VERIFIED | Dev-gated record; javadoc states `@Profile("dev")` gating |
| `src/main/resources/application.yml` | VERIFIED | `spring.profiles.default: dev`; no demo-user keys |
| `src/main/resources/application-dev.yml` | VERIFIED | DEBUG logging only; no secrets |
| `src/main/resources/application-prod.yml` | VERIFIED | INFO logging; no demo-user keys; no secrets |
| `src/test/java/com/humanit/challenge/config/CombinedDevProdProfilesTest.java` | VERIFIED | T-M20: asserts demo bean registers under `dev,prod` (will become stale) |
| `src/test/java/com/humanit/challenge/config/DefaultDevProfileStartupTest.java` | VERIFIED | T-M1/T-M8R: `getActiveProfiles()` assertion form (F-03 in developer report) |
| `src/test/java/com/humanit/challenge/config/ProdProfileStartupTest.java` | VERIFIED | T-M19: active profiles exactly `[prod]` |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | VERIFIED | T-M18 absence scan (candidate for guard-form extension) |
| Grep `@Profile` across `src/main` | VERIFIED | Exactly one `@Profile` in main code: `DevConfigPropertiesRegistration` (line 15) |

## 6. Files changed

**By this execution:** one file, if the write succeeds.

| File | Change |
|---|---|
| `agent-reports/T1.3/security-profiles-assessment.md` | **Created** — this assessment |

No application code, YAML, `pom.xml`, `decisions.md`, other agents' reports, Git state or any existing report was touched. Write result and read-back verification are reported in Section 8 and at the end.

## 7. Commands executed

**None.** `bash` is denied for this agent. Evidence is drawn from file reads and one grep (Section 5). No build or test was re-run by this execution; test outcomes are cited from the Developer report (VERIFIED there, C5/C6).

## 8. Evidence and observed results

### 8.1 Current state (VERIFIED)

- The only profile-gated bean registration in main code is `DevConfigPropertiesRegistration` carrying `@Profile("dev")` (`src/main/.../DevConfigPropertiesRegistration.java`, line 15, VERIFIED).
- Under the native default-profile mechanism (`spring.profiles.default: dev`), a no-profile start activates `dev` (Developer report: log line "No active profile set, falling back to 1 default profile: dev"; dev YAML settings visible). So `@Profile("dev")` is satisfied by the **default** as well as by an explicit `dev` activation.
- `CombinedDevProdProfilesTest` (VERIFIED) proves the F-03 behavior today: with `@ActiveProfiles({"dev","prod"})`, both profile files load **and** `AppDemoUserProperties` registers (`context.getBean(AppDemoUserProperties.class)` isNotNull; active profiles `containsExactly("dev","prod")`).
- Architect Revision 3 documents F-03 as an accepted, unguarded behavior and T-M20 as a documentation (not pass/fail) test.

### 8.2 Case evaluation of the proposed `@Profile("dev & !prod")`

Spring's `@Profile` accepts a single string that may be a profile **expression** using `!` (NOT), `&` (AND), `|` (OR) and parentheses (Spring `org.springframework.core.env.Profiles`). The expression `dev & !prod` is true iff `dev` is active and `prod` is not. Profile activation order does not matter because active profiles are a set.

| # | Active profiles (as Spring sees them) | `dev` active? | `prod` active? | `dev & !prod` | Demo-user bean registers? |
|---|---|---|---|---|---|
| 1 | (none → default `dev`) | yes (default) | no | true | **YES** (dev behavior preserved) |
| 2 | `dev` | yes | no | true | **YES** |
| 3 | `prod` | no | yes | false | **NO** |
| 4 | `dev,prod` | yes | yes | false | **NO** (was YES before the change) |
| 5 | `prod,dev` | yes | yes | false | **NO** (was YES before the change) |

### 8.3 Does the proposed annotation prevent registration whenever `prod` is active?

**Yes — for the demo-user properties bean, in all cases where `prod` is among the active profiles**, including the combined `dev,prod` / `prod,dev` cases that today register it (cases 3, 4, 5). The guard is evaluated at bean-registration time against the active profile set, so an explicit or combined prod activation excludes the dev-gated configuration. This directly closes the F-03 combined-profile demo-user exposure with a one-line, infrastructure-free change, and it does not disturb cases 1–2 (default dev and explicit dev still register the bean, preserving OD-02 and the dev experience).

**Caveat (important, and the core of the remaining risk in Section 9):** the guard only protects the *demo-user properties bean*. It does not make a production deployment that forgot `SPRING_PROFILES_ACTIVE=prod` safe — in that case `prod` is simply not active, so `dev & !prod` is true and the app still runs as dev (RK-01). The annotation fixes the *blended* profile case, not the *missing* profile case.

### 8.4 Report persistence check

The write of this assessment is attempted in this execution. Success/failure and read-back verification are reported at the end of the output (per mandatory reporting rule 12; no claim is made without verification).

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-SP-01 | Major (inherited, partially addressed) | Today, combined `dev,prod` activation registers the dev-gated demo-user bean (F-03). The proposed guard removes this specific exposure (cases 4–5), but F-03's broader "both profile files load and dev settings apply" half remains. |
| F-SP-02 | **Major** (remaining risk) | **RK-01 is unaffected.** If `prod` is not active (forgotten profile in a production deployment), `dev & !prod` evaluates true and the application still starts as default dev: DEBUG logging, dev-valued divergent properties, and (from T1.4 onward) an in-memory H2 datasource. The annotation cannot distinguish "dev by default in a dev laptop" from "dev by default in production" — that distinction is operational (M1–M5, Section 10.2 of Revision 3). |
| F-SP-03 | Minor | Combined activation still loads **both** profile files; dev settings (e.g. `logging.level.com.humanit.challenge: DEBUG`) may still be present/override prod's INFO, and T1.4 datasource behavior under combined profiles is unaddressed. Only the demo-user bean is now protected. |
| F-SP-04 | Minor | Profile-name fragility: the guard keys on the exact string `"prod"`. A deployment using `production`, `prd`, `prod-eu`, etc. is not covered. There is no centralized profile constant; the string is duplicated in code, tests and docs. |
| F-SP-05 | Minor | Consistency risk across phases: Phase 3 (T3.2) provisioning must use the **same** guard. If the provisioning bean keeps `@Profile("dev")`, it could register (or fail on a missing properties bean) under combined profiles even though the properties bean is now excluded. |
| F-SP-06 | Minor | Test impact: `CombinedDevProdProfilesTest.documentsF03DevGatedDemoUserBeanRegisters` asserts the bean **is present** under `dev,prod` and will fail after the change; it must be rewritten to assert absence (and possibly renamed). `SourceScanAuditTest` does not yet assert the guard form, so a regression to `@Profile("dev")` would not be caught. Both edits require Tech Lead authorization (Developer report OT-02/OT-03). |
| F-SP-07 | Minor | Documentation staleness: Architect Revision 3's F-03, T-M20 and Appendix B.1 describe the demo bean registering under combined profiles. Adopting the guard makes those statements stale; a Revision 4 (under the archival duty) would be needed to keep the proposal consistent. No existing report is modified by this execution. |

Zero Critical findings. F-SP-02 is a Major inherited risk, not a defect of the proposed annotation itself.

## 10. Decisions and reasoning (alternatives, trade-offs, assumptions)

### 10.1 Recommendation

Adopt `@Profile("dev & !prod")` on `DevConfigPropertiesRegistration` as the minimal, targeted change. It:
- closes the F-03 combined-profile demo-user exposure with a single annotation (cases 4–5 flip from "registers" to "does not register");
- preserves default-dev and explicit-dev behavior (cases 1–2 unchanged, so OD-02 and the dev experience are intact);
- adds no new infrastructure, respecting BK-08 (no `spring.factories`, no `EnvironmentPostProcessor`, no custom guard);
- is idiomatic Spring and keeps the protection co-located with the bean it guards.

### 10.2 Alternatives considered

- **Keep `@Profile("dev")`** — leaves F-03 open; rejected because the task is precisely to close that exposure.
- **`@Profile("!prod")`** — too broad: registers under *any* non-prod profile (including unknown or overlay profiles such as a future `test` profile), so a demo user could be provisioned outside the intended dev profile. Not minimal-safe.
- **Reintroduce an `EnvironmentPostProcessor`/fail-fast guard** — rejected by OD-01; contradicts the native default-profile design and re-adds bootstrap machinery.
- **Code-level conditional (`@ConditionalOnProperty` or an `if` check)** — more code, more surface, duplicates what the profile expression already expresses; rejected in favor of the idiomatic annotation.
- **Doing nothing and documenting** — leaves the combined-profile demo-user exposure in place; rejected.

### 10.3 Trade-offs

- **Benefit:** F-03's demo-user half is closed with no runtime cost and no new files.
- **Cost:** the profile-expression syntax (`&`, `!`) is less commonly known than a plain profile name; it must be documented in code comments and the proposal so future readers do not "simplify" it back to `@Profile("dev")`.
- **Cost:** test assertions and proposal wording must change (F-SP-06, F-SP-07), which requires authorized edits.
- **Not addressed:** RK-01 (F-SP-02) and the combined-profile file-loading half (F-SP-03). Those remain as documented residual risks and operational mitigations (M1–M5, Revision 3 Section 10.2).

### 10.4 Does future demo-user provisioning need the same guard?

**Yes.** The Phase 3 (T3.2) provisioning bean must use the same guard (`@Profile("dev & !prod")`, or a shared constant) rather than `@Profile("dev")`. Reasoning:

- The guard on `DevConfigPropertiesRegistration` only prevents the *properties* bean from being created under combined profiles. It does not, by itself, stop a provisioning component that is independently annotated `@Profile("dev")`.
- If the provisioning bean keeps `@Profile("dev")`, it would register under `dev,prod` and either fail at injection time (missing `AppDemoUserProperties`) or — worse — provision a demo user with known credentials in a blended environment, recreating exactly the exposure F-03 describes.
- The architect proposal's optional `@ConditionalOnBean(AppDemoUserProperties.class)` belt-and-braces only helps if the provisioning bean is annotated consistently; otherwise the conditional is never evaluated against the excluded bean.
- Recommendation: define a single shared constant (e.g. `Profiles.DEV_AND_NOT_PROD = "dev & !prod"`) in a small `config`-level holder so the properties registration and the future provisioning bean cannot drift apart, and extend the source-scan audit to assert both usages. (This is a design recommendation; implementation is out of scope for this execution.)

### 10.5 Assumptions (unverified — marked explicitly)

- A1 (PENDING VERIFICATION): Spring Boot 3.5.x parses `&` and `!` inside a single `@Profile("...")` string as a profile expression. This is standard Spring `Profiles` behavior and is used in many codebases, but **no test in this project exercises the expression form today**. Recommend verifying with a minimal context test before relying on it (natural fit: rewrite T-M20 to assert the bean is absent under `dev,prod`).
- A2 (RECONSTRUCTED): active profiles are treated as a set, so case 5 (`prod,dev`) is equivalent to case 4 (`dev,prod`). Standard Spring behavior; not re-verified here.
- A3 (PENDING VERIFICATION): `@Profile` on a `@Configuration` class that also carries `@EnableConfigurationProperties` is evaluated before the properties bean is registered. Standard Spring component scanning behavior; not re-verified here.
- A4 (PENDING VERIFICATION): a no-profile start activates `dev` as a *default* profile such that `@Profile("dev & !prod")` matches. The Developer report confirms default-dev functional behavior (log line, dev settings visible), but the exact `getActiveProfiles()` semantics are the subject of F-03 in that report; default-profile matching for expressions is assumed to follow the same rule. Recommend verifying in the rewritten T-M20.
- A5 (RECONSTRUCTED): no other main-code `@Profile` exists (grep VERIFIED, Section 5), so the change touches exactly one registration class.

## 11. Final status

**ASSESSMENT DELIVERED — RECOMMENDATION: ADOPT `@Profile("dev & !prod")` ON `DevConfigPropertiesRegistration`, WITH THE SAME GUARD ON FUTURE (T3.2) DEMO-USER PROVISIONING. NO IMPLEMENTATION PERFORMED.**

- No code, test, YAML, `pom.xml`, `decisions.md` or report was modified by this execution except the new assessment file.
- The annotation closes the F-03 combined-profile demo-user exposure (cases 4–5) while preserving default-dev and explicit-dev behavior (cases 1–2) and prod exclusion (case 3).
- RK-01 (accidental dev startup in production) remains a Major residual risk and is not addressed by this change; it stays governed by operational mitigations M1–M5 and the T1.6/Phase 5 work.
- Approval, implementation authorization and any test-file edits remain with the human Tech Lead.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OSP-01 | Awaiting decision | Approve or reject the `@Profile("dev & !prod")` change and the shared-constant recommendation (10.4) | Tech Lead |
| OSP-02 | Awaiting authorization | Edit `DevConfigPropertiesRegistration.java` (one annotation) once approved | Developer (after authorization) |
| OSP-03 | Awaiting authorization | Rewrite `CombinedDevProdProfilesTest` to assert demo bean is **absent** under `dev,prod` (and rename/repurpose as needed); optionally add guard-form assertion to `SourceScanAuditTest` | Developer (test edits need authorization, per OT-02/OT-03) |
| OSP-04 | PENDING VERIFICATION | Verify A1–A4 (expression parsing, default-profile matching) with a minimal context test | Tester (independent, before reading developer conclusions) |
| OSP-05 | Documentation | If approved, update Architect Revision 3 (F-03, T-M20, Appendix B.1) in a Revision 4 under the archival duty; do not edit the current report | Architect (only if Tech Lead requests) |
| OSP-06 | Forward boundary (T3.2) | Ensure Phase 3 provisioning uses the identical guard / shared constant | Developer (Phase 3), Reviewer check |
| OSP-07 | Standing governance | No T1.3 approval is recorded in `decisions.md`; only the human Tech Lead can create one | Tech Lead |

## 13. Handoff to next agent

- **Tech Lead:** decide OSP-01 (adopt guard or not), then explicitly authorize OSP-02/OSP-03 if approved. Note that this assessment improves but does not resolve RK-01 (F-SP-02); confirm that residual risk remains acceptable.
- **Architect (only if requested):** issue Revision 4 of `architect-proposal.md` under the archival duty to reconcile F-03/T-M20 wording with the new guard; otherwise the current proposal stands with F-03 documented as pre-change behavior.
- **Developer (after authorization):** apply the one-line annotation change; optionally introduce the shared profile constant (10.4); keep `Base64DecodedMinBytesValidator` and all other code untouched.
- **Tester:** re-run the suite independently **before** reading the Developer report; verify the rewritten combined-profile test asserts absence of the demo bean under `dev,prod`, that default-dev and explicit-dev still register it, and that prod still excludes it; confirm A1–A4.
- **Reviewer:** audit that the guard is exactly `dev & !prod` (not `!prod` alone), that no other `@Profile("dev")` remains on demo-user-related beans, that the shared constant (if introduced) is used consistently, and that BK-08 still holds (no `spring.factories`/`EnvironmentPostProcessor`).

---

**End of assessment.** All statements are classified per `agent-reports/README.md` Section 5 (VERIFIED / RECONSTRUCTED / NOT AVAILABLE / PENDING VERIFICATION). No test results, commands, approvals or historical events were invented. No implementation was performed.

## Appendix A — Exact change set if approved

**Main code (one line):**

```java
// src/main/java/com/humanit/challenge/config/DevConfigPropertiesRegistration.java
@Configuration(proxyBeanMethods = false)
@Profile("dev & !prod")          // was: @Profile("dev")
@EnableConfigurationProperties(AppDemoUserProperties.class)
public class DevConfigPropertiesRegistration { }
```

**Recommended (optional, Phase 3 alignment):**

```java
// new: src/main/java/com/humanit/challenge/config/Profiles.java  (or nested constant)
public final class Profiles {
    public static final String DEV_AND_NOT_PROD = "dev & !prod";
    private Profiles() {}
}
// then: @Profile(Profiles.DEV_AND_NOT_PROD)
```

**Tests to update (authorization required):**

- `CombinedDevProdProfilesTest.documentsF03DevGatedDemoUserBeanRegisters` → assert `context.getBeansOfType(AppDemoUserProperties.class)` is empty under `dev,prod`; rename test/method to reflect "demo bean does NOT register under combined profiles".
- `CombinedDevProdProfilesTest.documentsF03BothProfileFilesLoad` → keep (both files still load).
- `SourceScanAuditTest` → optionally assert no `@Profile("dev")` (without `& !prod`) on `DevConfigPropertiesRegistration` (regression guard).

**No changes needed to:** YAML files, `pom.xml`, pagination/JWT records, `Base64DecodedMinBytesValidator`, other tests, boundaries BK-01…BK-08.

## Appendix B — What this change does NOT fix

- Accidental dev startup in production when `prod` is absent (RK-01) — operational mitigations only.
- Combined-profile file loading and dev settings overriding prod settings (F-SP-03).
- Profile-name variants (`production`, `prd`, …) (F-SP-04).
- The F-04 JWT secret value exposure in bind-failure reports (separate assessment, `security-jwt-assessment.md`).
- F-02 test-harness property-source precedence issue (separate, test-side).