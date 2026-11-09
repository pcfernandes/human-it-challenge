# Developer Report — T1.2 Technical Baseline and Dependency Verification

**Report type:** Developer implementation report (first Developer execution for T1.2; no prior `developer-report.md` existed, so no `history/` archival was required — VERIFIED, `agent-reports/T1.2/` contained only `architect-proposal.md` before this execution)
**Generated:** 2026-11-09 (Europe/Lisbon)
**Method:** Evidence-only. Every statement is backed by an in-project artifact inspected during this execution (marker **VERIFIED**) or is explicitly marked otherwise. No test results, commands, approvals or historical events are invented.

---

## 1. Task ID

**T1.2 — Technical baseline and dependency verification** (per `implementation-plan.md` §5).

Implemented per the Tech Lead-approved Architect proposal `agent-reports/T1.2/architect-proposal.md` (decisions D-1…D-4, dependency set in its Section 10.3). Approval evidence: direct Tech Lead tasking received 2026-11-09 — *"Human Tech Lead approves the T1.2 architecture proposal, subject to the instructions below."* Prerequisite T1.1 formally accepted (D-04 in `agent-reports/decisions.md`) — **VERIFIED**.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution identifier | `T1.2-EXEC-001-DEV-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| History note | First Developer execution for T1.2; primary report created new — **VERIFIED** |
| Total/last duration | Build run finished 2026-11-09T12:34:57Z; report read-back verified 2026-11-09 |

## 3. Agent role and model

| Field | Value |
|---|---|
| Role | Developer (implements the approved scope only) |
| Model | `opencode/glm-5.3-flash-free` via OpenCode |
| Constraints honored | No business logic, no controllers/entities/repositories, no application configuration changes, no test classes added, no other agent's report touched; stopped after implementation, verification and reporting; no self-approval |

## 4. Authorized scope

In scope (**VERIFIED**, `implementation-plan.md` §5, T1.2): *"Add and version-lock approved dependencies (Spring Web, JPA, Validation, Security, Flyway, springdoc-openapi, PostgreSQL driver, test dependencies)."* Out of scope: *"Dependency usage in business code."*

Additional Tech Lead tasking instructions (2026-11-09, received directly): Spring Boot BOM-managed versions wherever possible; springdoc-openapi for Boot 3.x; Flyway PostgreSQL support; Spring Security test support *if required*; minimum-necessary configuration only if `contextLoads` fails; no temporary hacks; `.\mvnw.cmd clean verify` verification; report and read-back.

As in the proposal, the **only** file modified for implementation was `pom.xml`. No `application.yml` (main), no Java source, no migration files were touched — **VERIFIED** by this report's Section 6 (the "test resources" note in Section 8.1 refers to Maven's normal skip logging: `src/test/resources` does not exist and was not created).

## 5. Files inspected

| File / source | Purpose | Evidence class |
|---|---|---|
| `requirements.md`, `implementation-plan.md`, `requirements-decisions.md` | Approved requirements, task definition, technical baseline (§16), test strategy (§15), persistence/DB decisions (§12) | VERIFIED |
| `agent-reports/T1.2/architect-proposal.md` | Approved design: dependency table §10.3, decisions D-1…D-4, implementation steps §10.6, risks R-01…R-03 | VERIFIED |
| `agent-reports/README.md` | Mandatory 13-section format, evidence markers, history convention | VERIFIED |
| `agent-reports/decisions.md` | T1.1 acceptance (D-04); proposals P-01…P-04 untouched | VERIFIED |
| `pom.xml` (before edit) | T1.1 baseline: parent 3.5.16, Java 21 property, starter + starter-test, enforcer `[21,22)` / `[3.10.0,)` | VERIFIED |
| `src/main/resources/application.yml`, `src/main/java/com/humanit/challenge/HumanItChallengeApplication.java`, `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java` | Confirmed unchanged by this execution; `contextLoads` is the regression gate | VERIFIED |
| `target/dependency-tree.txt` | Produced by this execution; version-consistency evidence (Section 8.2) | VERIFIED (artifact created by this execution, inspected) |
| `target/surefire-reports/com.humanit.challenge.HumanItChallengeApplicationTests.txt` | Test result artifact of this execution's `clean verify` run | VERIFIED (artifact created by this execution, present post-build) |

## 6. Files changed

| File | Action | Detail |
|---|---|---|
| `pom.xml` | **Modified** | (a) Added property `springdoc-openapi.version` = `2.10.0` — the only explicit version in the project and the only dependency not managed by the parent BOM; (b) added compile-scope starters: web, data-jpa, validation, security, oauth2-resource-server (all without `<version>` — BOM-managed); (c) added `org.flywaydb:flyway-core` (compile, BOM-managed) and `org.flywaydb:flyway-database-postgresql` (runtime, BOM-managed); (d) added `com.h2database:h2` and `org.postgresql:postgresql` (runtime, BOM-managed); (e) added `org.springdoc:springdoc-openapi-starter-webmvc-ui` referencing the property; (f) added `org.springframework.security:spring-security-test` (test, BOM-managed). Parent, `java.version`, enforcer rules and plugins untouched. |

Deviations from the approved proposal (documented, within its intent):

1. **Item #11 artifact name.** The proposal's table named `org.springframework.boot:spring-boot-starter-security-test`. That starter does not exist in the Spring Boot 3.x dependency model; the correct, BOM-managed test artifact is `org.springframework.security:spring-security-test` (provides `@WithMockUser`, `SecurityMockMvcRequestPostProcessors`, etc.). Added under that name with the same test scope and the same purpose; its BOM-managed resolution (6.5.11, test scope) is confirmed in the dependency tree — **VERIFIED** (Section 8.2).
2. **One cosmetic edit-repair.** An intermediate edit sequence briefly left the `<properties>` block malformed (a placeholder property and an incomplete `</dependencies>` block) — this was fixed **within the same execution, before any build was run**; the file was never in a state presented as final, and the only version built/verified is the final POM. Noted for transparency; final file content VERIFIED by read-back before the build.
3. **Produced artifacts.** `target/dependency-tree.txt` (captured per proposal step 8). A transient `[WARNING] Could not transfer metadata /.meta/prefixes.txt from/to flyway-community-db-support (https://maven.pkg.github.com/flyway/flyway-community-db-support): HTTP Status: 401` appeared in both runs from an auto-discovered GitHub-package registry probe; it is informational only (metadata probe, not a dependency) and both runs succeeded — **VERIFIED**.

Nothing else was added, changed or removed. Main configuration (R-01 contingency) was **not needed and not applied** — see Section 8.1.

## 7. Commands executed

| Command | Result |
|---|---|
| `.\mvnw.cmd clean verify` | **BUILD SUCCESS**, exit code 0, 2026-11-09T12:34:57Z, total time 30.693 s |
| `.\mvnw.cmd dependency:tree -DoutputFile=target\dependency-tree.txt` | **BUILD SUCCESS**, tree written to `target/dependency-tree.txt` |
| `pom.xml` read-back before build; report files read-back after writing | Persistence/content verification (this section and Section 8) |

## 8. Evidence and observed results

### 8.1 Build and test (`clean verify`) — VERIFIED by the captured build output

- **BUILD SUCCESS** — `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` (`contextLoads`, runtime Java 21.0.12.1, Spring Boot v3.5.16 banner).
- Enforcer `enforce-baseline`: `RequireJavaVersion passed`, `RequireMavenVersion passed` — Java 21 / Maven baselines intact.
- Full stack wired exactly as the proposal predicted (R-01): embedded H2 auto-configured (`HikariPool-1 - Added connection url=jdbc:h2:mem:…`), Spring Data JPA bootstrap (`Found 0 JPA repository interfaces`), Flyway 11.7.2 executed against H2 (`Successfully validated 0 migrations`, `Schema "PUBLIC" is up to date. No migration necessary.`), Hibernate 6.6.53.Final started, springdoc initialized (`/v3/api-docs`, `/swagger-ui.html` enabled by default), embedded Tomcat in the autowired stack.
- **No auto-configuration failure occurred.** Per the Tech Lead's contingency instruction, the minimum necessary configuration is therefore **zero**; no `src/test/resources/application.yml`, no hack, no workaround was needed or created — recorded explicitly so the contingency's trigger is evidenced as not met.
- Two known, expected log artifacts, both non-fatal and informational: Flyway `WARN No migrations found. Are your locations set up correctly?` (zero-migration state until T1.4 creates the migration structure — **VERIFIED** as expected from the empty default location) and springdoc's endpoint-enabled warnings. These are deferred to T1.3/T1.4/T4.x by the plan's task boundaries; they are not defects of this task.

### 8.2 Dependency tree (`target/dependency-tree.txt`) — VERIFIED

Every added dependency resolves at its Spring Boot 3.5.16 BOM version; the only non-BOM version is the explicitly pinned springdoc:

| Dependency (declared) | Resolved version | Scope | Version authority |
|---|---|---|---|
| `spring-boot-starter-web` | (starter) 3.5.16; brings Spring 6.2.19, Tomcat 10.1.55 | compile | Boot BOM |
| `spring-boot-starter-data-jpa` | 3.5.16; brings Hibernate 6.6.53.Final | compile | Boot BOM |
| `spring-boot-starter-validation` | (starter, BOM-managed) | compile | Boot BOM |
| `spring-boot-starter-security` | (starter, BOM-managed; Spring Security 6.5.11) | compile | Boot BOM |
| `spring-boot-starter-oauth2-resource-server` | 3.5.16; brings `spring-security-oauth2-jose` (Nimbus) 6.5.11 | compile | Boot BOM |
| `flyway-core` | 11.7.2 | compile | Boot BOM |
| `flyway-database-postgresql` | 11.7.2 | runtime | Boot BOM (**VERIFIED** — closes the proposal's PENDING assumption that the BOM manages this module; version matches `flyway-core`, closing risk R-03) |
| `h2` | 2.3.232 | runtime | Boot BOM |
| `postgresql` | 42.7.11 | runtime | Boot BOM |
| `springdoc-openapi-starter-webmvc-ui` | 2.10.0 (property `springdoc-openapi.version`) | compile | Explicit pin (D-1/D-3) — resolves cleanly under Boot 3.5.16, closing risk R-02 |
| `spring-boot-starter-test` | (pre-existing from T1.1; JUnit 5, Mockito, AssertJ, MockMvc support) | test | Boot BOM |
| `spring-security-test` | 6.5.11 | test | Boot BOM |

No unapproved transitive additions beyond starter internals were observed in the reviewed tree sections.

### 8.3 Requirements traceability — VERIFIED

| Acceptance criterion (implementation-plan §5, T1.2, verbatim) | Evidence in this execution |
|---|---|
| "All required dependencies are present." | Every §16 baseline item is mapped to a declared dependency (proposal §10.2 table) and confirmed at its resolved version in Section 8.2 |
| "No unnecessary dependencies are introduced." | Set identical to approved proposal §10.3 (one artifact-name correction, Section 6); the proposal's NOT-added list (jjwt, jdbc starter, starter-flyway, actuator, devtools, Lombok, caching/messaging, Docker tooling) was respected |
| "Build and tests pass." | Section 8.1: BUILD SUCCESS; `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` |

Tech Lead tasking traceability: BOM-managed versions (Section 8.2, all but springdoc) ✔; springdoc-openapi for Boot 3.x (2.10.0, 2.x line) ✔; Flyway PostgreSQL support (`flyway-database-postgresql` 11.7.2) ✔; Spring Security test support "if required" → included (Section 10, decision DEV-D2) ✔; no business logic/controllers/entities/repositories (Section 6 — only `pom.xml` changed) ✔; project compiles and tests pass ✔; report read-back performed ✔.

## 9. Findings and severity

**Critical findings: none. MAJOR findings: none.**

Minor (informational, non-blocking):

| ID | Severity | Finding |
|---|---|---|
| F-D01 | Minor | Flyway `WARN No migrations found` at startup — expected consequence of an empty migration location; the T1.4 scope ("migration directory structure") closes it. Kept in the log for the Tester/Reviewer to see as expected behavior, not an error. |
| F-D02 | Minor | springdoc endpoints (/v3/api-docs, /swagger-ui.html) enabled by default in all profiles — visible only after T1.3 (profiles) exists to disable in production; unrelated to dependency presence itself. |
| F-D03 | Minor | Proposal item #11 named a nonexistent starter (`spring-boot-starter-security-test`); implemented as the real BOM-managed artifact `spring-security-test`. Recorded as a documented deviation (Section 6) so the Reviewer can audit against it. |
| F-D04 | Minor | Transient Maven WARNING in both runs (auto-discovered `flyway-community-db-support` GitHub registry probe, HTTP 401). No dependency resolution impact; both builds succeeded. |
| F-D05 | Minor | `flyway-database-postgresql`, `postgres-driver` and `spring-security-test` have no compile-time consumer until Phase 5 / Phase 3 respectively — intentional per the approved proposal (R-03 prevention and D-2) and the plan's test-strategy requirements (§15), not unused-library noise. |

## 10. Decisions and reasoning

- **DEV-D1 — Follow the approved set exactly; one assumption verified.** Implementation used the proposal's decision D-1 (BOM as single version authority, single explicit pin `springdoc-openapi.version=2.10.0`). The proposal's PENDING assumption that the Boot BOM manages `flyway-database-postgresql` was **verified at build time** (tree shows 11.7.2, identical to `flyway-core`); no manual version was needed.
- **DEV-D2 — Include Spring Security test support now.** The Tech Lead's instruction was "include… if required". Interpretation: the requirement-driven decision of the proposal (§10.3 item 11, recommended), given `requirements-decisions.md` §15 explicitly puts authentication tests in scope of the approved test strategy and the plan's Phase 3 and T1.5 test themes will need `@WithMockUser`/MockMvc-security utilities; including it now avoids re-opening `pom.xml` in a later approval round while remaining a zero-impact, test-scoped addition. The Reviewer may challenge this as borderline-unused-until-Phase-3; the artifact is inert at runtime (test scope).
- **DEV-D3 — No auto-configuration remedy applied.** R-01's matrix (H2 auto-detection, Flyway zero-migration tolerance, Security default chain) materialized exactly as designed, the context started, and the contingency path was never triggered. Applying any configuration now would have been unapproved scope (T1.3/T1.4 territory) even though the tasking permitted a minimal fix on failure.
- **DEV-D4 — Proposal steps 1–10 followed except where recorded.** Step 6 (item #11) and step 8 (tree captured to file) were executed with the documented deviations. Step 10's contingency (stop on build failure) did not apply.

No architectural decision was changed and no unapproved feature was implemented.

## 11. Final status

**IMPLEMENTED AND VERIFIED (by this execution) — awaiting independent Tester verification, Reviewer audit and Tech Lead acceptance.** All thirteen mandatory sections are present. This report does not approve its own implementation and does not infer any approval beyond the Tech Lead's pre-approved scope instruction.

## 12. Outstanding issues

1. **Independent verification pending:** the Tester must re-run `.\mvnw.cmd clean verify` and inspect `pom.xml`/`dependency:tree` **before** reading this report (per `agent-reports/README.md` §3); afterwards compare with Sections 8.1–8.2.
2. **Independent review pending:** the Reviewer must audit dependency necessity, version consistency and unused libraries against the approved proposal §10.2–10.3 (including documented deviation F-D03) — **before** reading this report.
3. **Tech Lead acceptance of T1.2** not recorded — required before T1.3 may begin (governance; D-04 pattern).
4. Deferred-by-design items (not defects): Flyway empty-location warning → T1.4; springdoc production-profile visibility → T1.3/T4; JWT implementation → Phase 3; Docker → Phase 5. Proposals P-01…P-04 in `agent-reports/decisions.md` remain PROPOSED and untouched.
5. Transient artifacts: `target/dependency-tree.txt` and Surefire files live under `target/` and are cleaned by `clean`; the evidence summarized here is the record of record — re-runs should regenerate equivalent outputs.

## 13. Handoff to next agent

- **To the Tester:** independently run `.\mvnw.cmd clean verify` and `.\mvnw.cmd dependency:tree`; expect BUILD SUCCESS, `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`, and the resolved versions in Section 8.2; verify the R-01 behavior matrix in Section 8.1 appears in your own run before reading this report's conclusions.
- **To the Reviewer:** audit `pom.xml` against the approved dependency table (Sections 10.2–10.3 of the proposal); rule on deviation F-D03 (`spring-security-test` substituted for a nonexistent starter and inclusion under DEV-D2) and on any dependency outside the approved table; confirm no unapproved transitive introductions.
- **To the Tech Lead:** after the Tester and Reviewer results are recorded, decide acceptance of T1.2 and authorize T1.3.

---

**Read-back verification:** this file was re-read after writing within the same execution and its persistence confirmed (13 sections present; content matches what was authored). — **VERIFIED**

**Status of this report:** Authored 2026-11-09 (Europe/Lisbon) by the Developer role. Scope: implementation + verification + reporting only; no self-approval; T1.3 not started.
