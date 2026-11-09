# Developer Report — T1.3 Environment Configuration and Secret Externalization

---

## 1. Task ID

**T1.3** — Environment configuration and secret externalization (Phase 1), against Architect Revision 4 (`agent-reports/T1.3/architect-proposal.md`, execution `T1.3-EXEC-007-ARCH-01`), authorized by the human Tech Lead for essential implementation with pragmatic scope (execution `T1.3-EXEC-008-DEV-01`).

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T1.3-EXEC-008-DEV-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Type | Implementation execution (authorized scope only) |
| Predecessor primary report | `agent-reports/T1.3/developer-report.md` (execution `T1.3-EXEC-001-DEV-01`) — archived unchanged at `agent-reports/T1.3/history/developer-001.md` before replacement, per `agent-reports/README.md` Section 6 |

## 3. Agent role and model

- **Role:** Developer (senior Java and Spring Boot developer)
- **Model:** `opencode/beam-free`

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 (`T1.3-EXEC-008-DEV-01`), implemented only:

1. Fix startup test property override precedence.
2. Fix default versus active profile assertions.
3. Change demo-user configuration to `@Profile("dev & !prod")`.
4. Update the existing combined-profile test.
5. Move JWT secret validation out of Bean Validation into a simple startup validator.
6. Preserve Base64 decoding and the 32-byte minimum.
7. Preserve pagination and expiration validation.
8. Update existing tests where necessary.

No new abstractions, infrastructure, extra edge-case tests or production deployment safeguards were added. No commit or push was performed.

## 5. Files inspected

All inspected this execution with read/glob/grep/bash tools:

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section format; evidence markers; archival convention (Section 6) |
| `agent-reports/T1.3/architect-proposal.md` (Revision 4) | VERIFIED | Authorized design amendments (Sections 10.3, 10.4, 10.8) |
| `agent-reports/T1.3/developer-report.md` (EXEC-001-DEV-01) | VERIFIED | Prior implementation state and failure root causes F-02/F-03/F-04 |
| `src/main/java/com/humanit/challenge/config/AppJwtProperties.java` | VERIFIED | Bean Validation constraints on secret to remove |
| `src/main/java/com/humanit/challenge/config/DevConfigPropertiesRegistration.java` | VERIFIED | `@Profile("dev")` to change |
| `src/main/java/com/humanit/challenge/config/validation/Base64DecodedMinBytesValidator.java` | VERIFIED | Reusable Base64/32-byte logic reference |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | VERIFIED | Property override precedence (F-02) |
| `src/test/java/com/humanit/challenge/config/DefaultDevProfileStartupTest.java` | VERIFIED | Default vs active profile assertion (F-03) |
| `src/test/java/com/humanit/challenge/config/CombinedDevProdProfilesTest.java` | VERIFIED | Combined-profile assertion to update |
| `src/test/java/com/humanit/challenge/config/JwtSecretValidationTest.java` | VERIFIED | Secret validation tests to keep green |
| `src/test/java/com/humanit/challenge/config/PaginationPropertiesValidationTest.java` | VERIFIED | Pagination/expiration fail-fast tests |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | VERIFIED | Audit test left unchanged and passing |
| `git status` / `git diff` | VERIFIED | Working-tree state before and after changes |

## 6. Files changed

By this execution:

| File | Change |
|---|---|
| `src/main/java/com/humanit/challenge/config/AppJwtProperties.java` | **Modified** — removed `@NotBlank` and `@Base64DecodedMinBytes(32)` from `secret`; secret is now a plain `String` field validated by `JwtSecretValidator`. Preserved `@NotNull @Min(1)` on `expirationMinutes`. |
| `src/main/java/com/humanit/challenge/config/JwtSecretValidator.java` | **Created** — `@Component` with constructor-injected `AppJwtProperties` and `@PostConstruct` validation: non-blank, strict RFC 4648 Base64 decode, decoded length >= 32. Messages contain the property prefix and failure kind only; never the secret value. |
| `src/main/java/com/humanit/challenge/config/DevConfigPropertiesRegistration.java` | **Modified** — `@Profile("dev")` changed to `@Profile("dev & !prod")`; javadoc updated. |
| `src/main/java/com/humanit/challenge/config/ConfigPropertiesRegistration.java` | **Modified** — javadoc reference updated to `@Profile("dev & !prod")`. |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | **Modified** — overrides are now applied via `TestPropertyValues` through an `ApplicationContextInitializer`, a high-precedence property source, so committed `application.yml` values cannot shadow invalid overrides. |
| `src/test/java/com/humanit/challenge/config/DefaultDevProfileStartupTest.java` | **Modified** — assertion changed from `getActiveProfiles()` contains exactly `"dev"` to `getDefaultProfiles()` contains `"dev"` plus `getActiveProfiles()` is empty. |
| `src/test/java/com/humanit/challenge/config/CombinedDevProdProfilesTest.java` | **Modified** — combined-profile test now asserts `AppDemoUserProperties` bean is absent under `dev,prod`. |
| `agent-reports/T1.3/history/developer-001.md` | **Created** — unchanged archival copy of the prior primary Developer report (execution `T1.3-EXEC-001-DEV-01`), before replacement, per `agent-reports/README.md` Section 6. |
| `agent-reports/T1.3/developer-report.md` | **Replaced** — this report. |

No application domain code, YAML (other than the pre-existing T1.3 files), `pom.xml`, `decisions.md`, other agents' reports or Git operations were touched by this execution. Base64 decoding and the 32-byte minimum are preserved (in `JwtSecretValidator` and in the retained `Base64DecodedMinBytesValidator`). Pagination and expiration Bean Validation are preserved unchanged.

## 7. Commands executed

| # | Command | Result |
|---|---|---|
| C1 | `Copy-Item agent-reports\T1.3\developer-report.md agent-reports\T1.3\history\developer-001.md` | VERIFIED — archival copy created before replacement |
| C2 | Code edits (files in Section 6) | VERIFIED — applied as listed |
| C3 | `.\mvnw.cmd clean test` | VERIFIED — **BUILD SUCCESS** (Section 8) |
| C4 | `git status` / `git diff --stat` | VERIFIED — working-tree changes summarized in Section 8 |

## 8. Evidence and observed results

### 8.1 Maven test results (VERIFIED — executed this execution, C3)

**`.\mvnw.cmd clean test` — BUILD SUCCESS.**

- `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0`
- `Total time: 29.731 s`
- Finished at: `2026-11-09T15:23:12Z`
- Environment: Java 21.0.12.1, Spring Boot 3.5.16, Surefire 3.5.6

Per class (Surefire summary lines, VERIFIED):

| Test class | Run | Fail | Result |
|---|---|---|---|
| `validation.Base64DecodedMinBytesValidatorTest` | 6 | 0 | PASS |
| `SourceScanAuditTest` | 1 | 0 | PASS |
| `HumanItChallengeApplicationTests` | 1 | 0 | PASS |
| `ProdProfileStartupTest` | 4 | 0 | PASS |
| `ExplicitDevProfileStartupTest` | 1 | 0 | PASS |
| `DemoUserPropertiesTest` | 2 | 0 | PASS |
| `CombinedDevProdProfilesTest` | 2 | 0 | PASS |
| `DefaultDevProfileStartupTest` | 3 | 0 | PASS |
| `JwtSecretValidationTest` | 6 | 0 | PASS |
| `PaginationPropertiesValidationTest` | 5 | 0 | PASS |
| **Total** | **31** | **0** | **BUILD SUCCESS** |

### 8.2 Key observed outcomes (VERIFIED)

- Property override precedence: all five override fail-fast tests (T-M10…T-M14) now fail startup as intended; invalid values are no longer shadowed by committed `application.yml`.
- Default vs active profile: default-dev start reports `dev` via `getDefaultProfiles()` and an empty active-profile set; dev settings remain visible.
- Demo-user guard: under combined `dev,prod` activation the `AppDemoUserProperties` bean is absent; both profile files still load.
- JWT secret validation moved to application code: missing/blank/malformed/too-short secrets still abort startup with the property named in the failure; the no-value-leak assertions pass (no rejected secret value in failure messages).
- 32-byte boundary accepted; 24-byte (32-char) input rejected with the byte-count message.
- Pagination defaults 0/20/100 and expiration default 60 bind; pagination and expiration Bean Validation preserved.
- Base64 decoding and the 32-byte minimum preserved.

### 8.3 Report persistence check (VERIFIED)

This report was written to `agent-reports/T1.3/developer-report.md` after archiving the prior primary report to `agent-reports/T1.3/history/developer-001.md`. The file was read back in full after writing; persistence succeeded without permission error or bypass.

## 9. Findings and severity

No Critical, Major or Minor findings. The suite is green (`Tests run: 31, Failures: 0, Errors: 0, Skipped: 0`). The previously accepted residual risk RK-01 / F-02 (accidental dev startup in production when `prod` is omitted) remains unchanged and accepted by the Tech Lead's decision OD-02; Revision 4 closes only the combined-profile demo-user exposure, not the missing-profile case.

## 10. Decisions and reasoning

- **D-DEV-01 — Validation moved, not removed.** JWT secret validation left Bean Validation and moved to `JwtSecretValidator` (`@PostConstruct`) so that no `FieldError.rejectedValue` carries the secret into startup diagnostics, while preserving fail-fast before the web server starts and the 32-decoded-byte floor. Expiration and pagination constraints remain declarative Bean Validation.
- **D-DEV-02 — High-precedence overrides via `TestPropertyValues`.** Overrides are applied through an `ApplicationContextInitializer` using `TestPropertyValues`, which adds a high-precedence property source; this makes T-M10…T-M14 executable without changing production code.
- **D-DEV-03 — Default vs active profile distinction.** `spring.profiles.default` activates dev for config loading but does not populate `getActiveProfiles()`; the assertion now uses `getDefaultProfiles()` plus an empty active set.
- **D-DEV-04 — Minimal profile guard.** Changed `@Profile("dev")` to `@Profile("dev & !prod")` on `DevConfigPropertiesRegistration` only; no shared constant or additional infrastructure was introduced, keeping the change minimal.
- **D-DEV-05 — Combined-profile test updated.** The demo-user bean is now asserted absent under `dev,prod`, while both profile files loading is still documented.

## 11. Final status

**IMPLEMENTED — BUILD SUCCESS (31/31 TESTS PASSING).**

All eight authorized items are implemented. The Maven suite is green. No approval, acceptance or sign-off is claimed; only the human Tech Lead can approve.

## 12. Outstanding issues

None blocking. The accepted residual risk RK-01 / F-02 (missing `SPRING_PROFILES_ACTIVE=prod` in production) remains governed by operational mitigations M1–M5 and is out of scope for this task. Runbook/deployment items (T1.6 / Phase 5) remain forward boundaries.

## 13. Handoff to next agent

- **Tester:** independently verify `.\mvnw.cmd clean test` and the amended assertions (T-M1/T-M8R default vs active, T-M10…T-M14 override precedence, T-M20 combined-profile absence, secret no-value-leak) before reading this report.
- **Reviewer:** audit against Architect Revision 4; verify the guard, the validator and the precedence fix; check that pagination and expiration validation remain intact.
- **Tech Lead:** no further authorization required from this execution; approval remains the Tech Lead's decision.

---
**End of report.** All statements are classified per `agent-reports/README.md` Section 5 (VERIFIED / RECONSTRUCTED / NOT AVAILABLE / PENDING VERIFICATION).