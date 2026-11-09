# Architect Proposal — T1.1 Project Bootstrap Only

**Report type:** Architect proposal (RECONSTRUCTED — see section 3)
**Generated:** 2026-11-09 (Europe/Lisbon)
**Method:** Evidence-only. No historical Architect proposal document was found anywhere in the project (**NOT AVAILABLE**). This document reconstructs the T1.1 design **as embodied in the existing bootstrap artifacts**, so that the Tech Lead has a reference proposal to ratify or reject. It does not claim that a historical Architect agent authored or approved it.

---

## 1. Task ID

**T1.1 — Project bootstrap only** (per `implementation-plan.md`, Section 5).

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution identifier | `T1.1-EXEC-001` — identifier assigned during this reconstruction for reference purposes; any original identifier is **NOT AVAILABLE** |
| Historical execution date | **RECONSTRUCTED** as 2026-11-09 from artifact timestamps: source files written 10:41 (`pom.xml` 10:41:46, `HumanItChallengeApplication.java` 10:41:47), compiled classes 10:55:09, Surefire run 10:55:10–10:55:14 |
| This document authored | 2026-11-09 (Europe/Lisbon) |

## 3. Agent role and model

| Field | Value |
|---|---|
| Proposal role | Architect (per `.opencode/agents/architect.md`: design and analysis only; file edits and shell execution denied) |
| Historical Architect agent identity and model | **NOT AVAILABLE** — no record of which agent or model produced the T1.1 design exists in the project |
| This document authored by | Developer role (reporting only), model `opencode/glm-5.3-flash-free` via OpenCode, under the reporting task authorized on 2026-11-09 |

## 4. Authorized scope

Per `implementation-plan.md`, T1.1:

- **In scope:** Maven project, Maven Wrapper, Java 21 baseline, Spring Boot parent, main application class, minimal `application.yml`, base package declaration, initial test skeleton.
- **Out of scope:** Domain entities, persistence, security, business endpoints, Docker, full configuration profiles.

This document is a design proposal only. Per the Architect agent definition, the Architect does not implement code and waits for Tech Lead approval.

## 5. Files inspected

| File | Purpose in inspection |
|---|---|
| `requirements.md` | Original requirements |
| `evaluation-notes.md` | Evaluation criteria |
| `requirements-decisions.md` | Approved requirements decisions (Sections 16, 18, 19) |
| `implementation-plan.md` | Approved plan; T1.1 task definition |
| `pom.xml` | Build design actually implemented (**VERIFIED** in project) |
| `src/main/java/com/humanit/challenge/HumanItChallengeApplication.java` | Main application class (**VERIFIED**) |
| `src/main/resources/application.yml` | Minimal configuration (**VERIFIED**) |
| `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java` | Test skeleton (**VERIFIED**) |
| `.mvn/wrapper/maven-wrapper.properties`, `mvnw`, `mvnw.cmd` | Wrapper baseline (**VERIFIED**) |
| `target/surefire-reports/` | Test execution evidence (**VERIFIED**) |
| `reports/*.md` (5 files) | Prior reporting pass |
| `.opencode/agents/architect.md`, `developer.md`, `tester.md`, `reviewer.md` | Role definitions |

## 6. Files changed

**None.** This is a design document. No file was created or modified in the application tree.

## 7. Commands executed

**None.** Read-only file inspections only, consistent with the Architect role definition (`bash: deny`). Historical commands used to produce the bootstrap are **NOT AVAILABLE**.

## 8. Evidence and observed results

The design recorded below is read directly from the artifacts (**VERIFIED** facts):

| Design element | Recorded value | Source artifact |
|---|---|---|
| Build tool | Maven with Maven Wrapper (wrapper 3.3.4, pinned distribution Maven 3.10.0, `distributionType=bin`) | `pom.xml`, `.mvn/wrapper/maven-wrapper.properties` |
| Spring Boot parent | `spring-boot-starter-parent` **3.5.16** | `pom.xml` |
| Java baseline | `java.version` **21**, enforced by `maven-enforcer-plugin` rule `requireJavaVersion [21,22)` plus `requireMavenVersion [3.10.0,)` | `pom.xml` |
| Dependencies | `spring-boot-starter` and `spring-boot-starter-test` (test scope) only — no web, JPA, security, Flyway or other dependencies | `pom.xml` |
| Build plugins | `spring-boot-maven-plugin`, `maven-enforcer-plugin` (execution `enforce-baseline`) | `pom.xml` |
| Base package | `com.humanit.challenge` | All Java sources |
| Main class | `HumanItChallengeApplication` with standard `@SpringBootApplication` and `main` delegating to `SpringApplication.run` | Application source |
| Configuration | `application.yml` containing only `spring.application.name: human-it-challenge` | `application.yml` |
| Test skeleton | Single `@SpringBootTest` class `HumanItChallengeApplicationTests` with one `contextLoads()` test | Test source |
| Test run result | `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` (2026-11-09 10:55, Java 21.0.12.1, Windows 11, Spring Boot v3.5.16 banner) | `target/surefire-reports/` |

## 9. Findings and severity

| ID | Finding | Severity |
|---|---|---|
| A-F01 | No historical Architect proposal document exists for T1.1; this proposal is a reconstruction from artifacts | Minor (documentation gap; gap G-10) |
| A-F02 | The implemented design matches the T1.1 authorized scope exactly: no out-of-scope dependency or code is present | Informational (positive) |
| A-F03 | The design's approval by the Tech Lead is **NOT AVAILABLE**; ratification is outstanding | Major (governance; gaps G-05, per `decisions.md` Section 4) |

## 10. Decisions and reasoning

### 10.1 Design decisions (facts VERIFIED from artifacts; rationale RECONSTRUCTED)

| Decision | Rationale (reconstructed from the approved plan's constraints) |
|---|---|
| Maven + Wrapper rather than Gradle | `requirements.md` allows either; the approved technical baseline (`requirements-decisions.md`, Section 16) specifies "Maven with Maven Wrapper" |
| Spring Boot 3.5.16 parent | Satisfies the approved "Spring Boot 3.x" baseline; parent-managed versions avoid early version locking, which `requirements-decisions.md` Section 19 reserves for later design verification |
| Java 21 with enforcer rule | T1.1 acceptance criterion "Java 21 baseline is enforced"; the enforcer plugin makes the baseline fail fast rather than merely declared |
| Minimal dependency set | T1.1 is "project bootstrap only"; no domain, persistence, security or Docker scope. Dependency additions belong to T1.2 |
| Standard `@SpringBootApplication` main class | The minimal runnable Spring Boot application required by the task |
| `application.yml` with application name only | Full profiles and externalization belong to T1.3 |
| Single `contextLoads` test skeleton | T1.1 requires an "initial test skeleton"; the Tester duty for T1.1 is "verify the application starts and a basic context-load test passes" |

### 10.2 Alternatives considered (analytical context — **RECONSTRUCTED**; no historical record of deliberation exists)

- **Gradle instead of Maven:** permitted by `requirements.md` but rejected against the approved baseline, which names Maven and the Maven Wrapper.
- **Declaring full dependency set at bootstrap:** rejected because T1.1 explicitly excludes it and T1.2 exists for version-locked dependency addition with verification.
- **Profiles and externalized configuration at bootstrap:** rejected; explicitly T1.3 scope.

### 10.3 Trade-offs and assumptions

- **Trade-off:** the enforcer plugin adds build-time checks in exchange for guaranteed baseline compliance.
- **Trade-off:** parent-managed dependency versions defer exact version pinning to T1.2, accepting a temporary unknown in exchange for a minimal, verifiable bootstrap.
- **Assumption (RECONSTRUCTED):** the bootstrap targets local execution on JDK 21; the Surefire environment records Java 21.0.12.1 on Windows 11.
- **Assumption (RECONSTRUCTED):** H2 remains the development database per the approved baseline; nothing database-related is introduced at T1.1.

## 11. Final status

**RECONSTRUCTED — submitted for Tech Lead ratification.** The bootstrap design recorded here is **VERIFIED** to exist in the project and to match the T1.1 scope, but:

- The historical proposal and its Tech Lead approval are **NOT AVAILABLE** (`decisions.md`, Section 4).
- Ratification of this reconstruction is **PENDING** (`decisions.md`, proposal P-01).

This document is not evidence that the historical Architect produced or the Tech Lead approved any proposal.

## 12. Outstanding issues

- Tech Lead ratification of this reconstructed proposal (P-01) — **PENDING**.
- Historical proposal and approval records — **NOT AVAILABLE** (gaps G-05, G-09, G-10).
- Standalone start and autonomous termination — **PENDING VERIFICATION** (gap G-01); owned by the Tester/Developer reports.

## 13. Handoff to next agent

- **To the Tech Lead:** review and ratify or reject this reconstructed proposal (P-01 in `agent-reports/decisions.md`).
- **To the Developer:** no action until ratification; the Developer report for T1.1 (`developer-report.md`) records the implemented state.
- **To the Tester:** verification state and pending items are in `tester-report.md`.

---

**Status of this report:** Reconstructed 2026-11-09 from in-project artifacts only. All 13 mandatory sections present.
