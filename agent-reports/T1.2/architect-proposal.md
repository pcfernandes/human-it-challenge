# Architect Proposal — T1.2 Technical Baseline and Dependency Verification

**Report type:** Architect proposal (first execution; new document, no prior report existed)
**Generated:** 2026-11-09 (Europe/Lisbon)
**Method:** Evidence-only. Every statement is backed by an in-project artifact (marker **VERIFIED**), by external authoritative sources cited in Section 8.3, or is explicitly marked **PENDING VERIFICATION**. No requirements were invented, no scope was added beyond T1.2, and no application code was implemented.

---

## 1. Task ID

**T1.2 — Technical baseline and dependency verification** (per `implementation-plan.md`, Section 5, "Task list").

Prerequisite: T1.1 (Project bootstrap only) was formally accepted by the human Tech Lead — **VERIFIED** via `agent-reports/decisions.md`, entry D-04 (2026-11-09).

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution identifier | `T1.2-EXEC-001-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| History note | First Architect execution for T1.2; `agent-reports/T1.2/` did not exist before this execution, so no archival under `history/` was required |

## 3. Agent role and model

| Field | Value |
|---|---|
| Role | Architect (design and analysis only; file edits and shell execution denied per the agent definition) |
| Model | `opencode/glm-5.3-flash-free` via OpenCode |
| Constraint honored | This document is a proposal only. No application code was implemented and no build was executed. |

## 4. Authorized scope

Per `implementation-plan.md`, Section 5, task T1.2 (**VERIFIED**, quoted verbatim):

- **In scope:** "Add and version-lock approved dependencies (Spring Web, JPA, Validation, Security, Flyway, springdoc-openapi, PostgreSQL driver, test dependencies)."
- **Out of scope:** "Dependency usage in business code."
- **Developer:** Add dependencies and verify compatibility.
- **Tester:** Verify build and test execution remain green.
- **Reviewer:** Check dependency necessity, version consistency and unused libraries.

This proposal addresses the nine items requested by the Tech Lead's tasking, mapped into the mandatory 13-section report structure as follows:

| Requested item | Addressed in |
|---|---|
| 1. Objective and scope | Section 4 above and Section 10.1 |
| 2. Requirements covered | Section 10.2 |
| 3. Proposed technical design | Section 10.3 |
| 4. Files to create or modify | Section 10.4 |
| 5. Dependencies | Section 10.5 |
| 6. Implementation steps | Section 10.6 |
| 7. Testing strategy | Section 10.7 |
| 8. Critical or Major risks only | Section 9.1 |
| 9. Acceptance criteria | Section 10.8 |

## 5. Files inspected

| File / source | Purpose in inspection | Evidence class |
|---|---|---|
| `requirements.md` | Original requirements (CRUD, H2, transactions, testing, optional Swagger/JWT) | VERIFIED |
| `implementation-plan.md` | Approved plan; T1.2 task definition, governance rules, risk notes | VERIFIED |
| `requirements-decisions.md` | Approved decisions: Section 16 (technical baseline), Section 19 (architecture design duties), Section 20 (approved API/concurrency decisions) | VERIFIED |
| `agent-reports/decisions.md` | Tech Lead decision log: D-01…D-04 approved, P-01…P-04 proposed; T1.1 acceptance | VERIFIED |
| `agent-reports/README.md` | Mandatory 13-section report format, evidence markers, execution-history convention | VERIFIED |
| `pom.xml` | Current dependency baseline (post-T1.1) | VERIFIED |
| `src/main/resources/application.yml` | Minimal configuration (`spring.application.name` only) | VERIFIED |
| `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java` | Existing `contextLoads` test — the T1.2 regression gate | VERIFIED |
| `src/main/java/com/humanit/challenge/HumanItChallengeApplication.java`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` | Bootstrap baseline confirmed intact (main class, Maven Wrapper 3.3.4 layout) | VERIFIED |
| `agent-reports/T1.1/architect-proposal.md` | Continuity of the accepted T1.1 design (parent 3.5.16, enforcer rules, minimal dependencies) | VERIFIED |
| `https://repo1.maven.org/maven2/org/springdoc/springdoc-openapi-starter-webmvc-ui/maven-metadata.xml` | Version resolution evidence for springdoc (fetched 2026-11-09) | External source (Section 8.3) |
| `https://springdoc.org/` (compatibility matrix), `https://github.com/springdoc/springdoc-openapi/releases` | springdoc 2.x ↔ Spring Boot 3.x line confirmation | External source (Section 8.3) |
| `https://github.com/flyway/flyway/issues/3819` (quoting release note for #3780), `https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database`, `https://github.com/spring-projects/spring-boot/issues/49012` | Flyway 10+ per-database module requirement; H2 remains in `flyway-core` | External source (Section 8.3) |

## 6. Files changed

**None.** This execution produced only this proposal document (`agent-reports/T1.2/architect-proposal.md`), which is the file explicitly tasked by the Tech Lead. No application file, build file, or other agent's report was modified.

Files *proposed* for the Developer to modify after approval are listed in Section 10.4.

## 7. Commands executed

**None.** No shell or build commands were executed (the Architect role has shell execution denied). This execution performed only:

1. Read-only file inspection of the artifacts in Section 5.
2. Web-based version research (three lookups, listed in Section 8.3) — used to fulfill the approved duty in `requirements-decisions.md` Section 19: *"Exact dependency versions must be selected and verified during architecture design."*

No test outputs, build results or execution logs were produced or are claimed by this execution.

## 8. Evidence and observed results

### 8.1 Current baseline (all VERIFIED)

| Element | Observed value | Source |
|---|---|---|
| Spring Boot parent | `spring-boot-starter-parent` **3.5.16** | `pom.xml` lines 5–10 |
| Java baseline | `java.version` **21**, enforcer rule `requireJavaVersion [21,22)` | `pom.xml` lines 17–19, 50–53 |
| Current dependencies | `spring-boot-starter` (compile), `spring-boot-starter-test` (test) — nothing else | `pom.xml` lines 21–32 |
| Build plugins | `spring-boot-maven-plugin`, `maven-enforcer-plugin` (`enforce-baseline`) | `pom.xml` lines 34–63 |
| Configuration | `application.yml` contains only `spring.application.name: human-it-challenge` | `application.yml` |
| Test skeleton | Single `@SpringBootTest` `contextLoads()` | `HumanItChallengeApplicationTests.java` |
| Maven Wrapper | `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.jar`, `maven-wrapper.properties` present | Project root, `.mvn/wrapper/` |
| T1.1 status | Formally accepted (D-04), including build success, `contextLoads` pass, standalone startup, Java 21 baseline | `agent-reports/decisions.md` D-04 |

### 8.2 Requirements traceability for T1.2 (VERIFIED quotes)

| Source | Requirement driving T1.2 |
|---|---|
| `implementation-plan.md` §5, T1.2 | Dependency list: Spring Web, JPA, Validation, Security, Flyway, springdoc-openapi, PostgreSQL driver, test dependencies |
| `requirements-decisions.md` §16 | Approved technical baseline: Java 21, Spring Boot 3.x, Maven + Wrapper, Spring Web, Spring Data JPA, Bean Validation, Spring Security, JWT, H2, PostgreSQL, Flyway, springdoc-openapi, JUnit 5/Mockito/MockMvc, Docker and Docker Compose |
| `requirements-decisions.md` §19 | "Exact dependency versions must be selected and verified during architecture design" — this proposal fulfills that duty for the dependency set |
| `requirements-decisions.md` §12 | H2 for dev/test, PostgreSQL for prod, Flyway migrations, compatibility with both databases |
| `requirements-decisions.md` §10 | Spring Security with JWT; `POST /auth/login`; endpoints protected by JWT |
| `requirements-decisions.md` §14 | Swagger/OpenAPI through `springdoc-openapi`, code-first, Swagger UI access |
| `requirements-decisions.md` §15 | JUnit 5, Mockito, MockMvc, Spring Boot Test, H2 integration tests |
| `implementation-plan.md` §11 | Test themes (controller unit tests with MockMvc, integration tests) that the test dependencies must support |

Scope boundary note (**VERIFIED** from plan structure): "Docker and Docker Compose" in the §16 baseline is a **Phase 5 deliverable** (`Dockerfile`, `compose.yaml`, tasks T5.1–T5.2), not a Maven dependency. It is deliberately absent from the T1.2 dependency set. JWT signing/expiry/validation *strategy* is reserved for later architecture design (`requirements-decisions.md` §19); T1.2 only locks the dependency that makes it possible.

### 8.3 External version research (supporting evidence; sources cited, not in-project artifacts)

| Question | Finding | Source |
|---|---|---|
| Which springdoc line supports Spring Boot 3.x? | Official compatibility matrix: Spring Boot **3.x → springdoc-openapi 2.x** (`spring-boot-3` branch); 3.x targets Spring Boot 4 | `springdoc.org` (README "Compatibility" table, `springdoc.org/v2`) |
| What is the latest 2.x version? | **2.10.0** — final entry of the 2.x line in Maven Central metadata (`<latest>` is 3.2.0, which belongs to the Boot 4 line); metadata last updated 2026-10-24 | `repo1.maven.org/.../springdoc-openapi-starter-webmvc-ui/maven-metadata.xml`, fetched 2026-11-09 |
| Is `flyway-core` alone sufficient for PostgreSQL on the Boot 3.x line? | **No.** Since Flyway 10, per-database modules are mandatory; without `flyway-database-postgresql` startup fails with `Unsupported Database: PostgreSQL`. "Databases, with the exception of in-memory or file databases such as **H2** or SQLite, have been moved into their own packages" — so H2 remains in `flyway-core` | Flyway issue #3819 (quoting the Flyway 10.0.0 release note for issue #3780), Redgate PostgreSQL driver reference, spring-boot issue #49012 |

Compatibility of the exact pinned set with Spring Boot 3.5.16 at build time is **PENDING VERIFICATION** (via `.\mvnw.cmd clean verify` and `dependency:tree` during T1.2 execution, Section 10.6).

## 9. Findings and severity

### 9.1 Risks — Critical or Major only (per tasking)

| ID | Severity | Risk | Impact | Mitigation |
|---|---|---|---|---|
| R-01 | **Major** | Adding the dependency set immediately changes Spring Boot auto-configuration behavior, **before** T1.3 (profiles/secrets) and T1.4 (persistence baseline) provide explicit configuration: embedded **H2 is auto-detected** and an in-memory `DataSource` is auto-configured; Hibernate starts; **Flyway auto-runs** against it with an empty migration location; **Spring Security** applies its default security chain (all endpoints locked, generated default password logged); embedded Tomcat starts. The existing `contextLoads` test is *expected* to remain green **because of** embedded-DB auto-detection — but this must be verified, not assumed. | T1.2 acceptance criterion "Build and tests pass" depends on this implicit behavior; an unexpected failure would block the task. | T1.2's verification step (Section 10.6, step 7) is the designed checkpoint. **Contingency (not part of this approved scope):** if `contextLoads` fails due to a missing datasource or auto-configuration conflict, the minimal remedy is a test-scoped configuration file (e.g., `src/test/resources/application.yml`). That file must **not** be created without the Tech Lead's explicit authorization, and any such change must be recorded in the T1.2 reports. |
| R-02 | **Major** | `springdoc-openapi-starter-webmvc-ui` is the **only** dependency not managed by the Spring Boot parent BOM, requiring an explicit version pin. Pinning the wrong major line (3.x targets Spring Boot 4) breaks startup. | Startup failure or silent version inconsistency — directly violates the Reviewer criterion "version consistency". | Pin **2.10.0**, the final 2.x release (Maven Central metadata, Section 8.3), via a dedicated property. The T1.2 build (`clean verify`) and the Reviewer's `dependency:tree` check close this risk (**PENDING VERIFICATION** until then). |
| R-03 | **Major** | Omitting `org.flywaydb:flyway-database-postgresql` produces a **hidden deferred defect**: the application still builds and runs on H2 (`dev`), and the failure ("Unsupported Database") only surfaces in Phase 5 against PostgreSQL. | A one-line omission now becomes a Phase 5 debugging incident; contradicts `requirements-decisions.md` §12 ("Ensure migration compatibility with H2 and PostgreSQL"). | Include `flyway-database-postgresql` **now**, while `pom.xml` is open, version-managed by the Boot BOM. H2 behavior is unaffected (module confirmed to remain in `flyway-core`). |

No Critical risks were identified for this task: the change is additive to `pom.xml`, reversible, and gated by the existing test suite.

### 9.2 Minor findings (informational, non-blocking — listed for traceability; not risks)

| ID | Severity | Finding |
|---|---|---|
| M-01 | Minor | Adding `spring-boot-starter-security` changes the application's runtime security posture immediately (default form-login, generated password in logs), although JWT enforcement design belongs to Phase 3 and no business endpoints exist yet. No effect on the T1.2 acceptance criteria. |
| M-02 | Minor | `spring-boot-starter-security-test` is recommended (Section 10.3, item 11) but is the one candidate an evaluator could call "unused until Phase 3". The decision is explicitly left to the Tech Lead (Section 12). |

## 10. Decisions and reasoning

### 10.1 Objective and scope (proposal item 1)

**Objective:** Equip the accepted T1.1 bootstrap with the complete, version-locked Maven dependency set required by the approved technical baseline (`requirements-decisions.md` §16), without using any of it in business code, so that all subsequent tasks (T1.3 configuration, T1.4 persistence, Phase 2 CRUD, Phase 3 security, Phase 4 OpenAPI) build on a compatible, reviewed foundation.

**In scope:** Dependency additions to `pom.xml` only, plus build/test verification.

**Out of scope (unchanged from the plan):** dependency usage in business code; application or test configuration files (T1.3/T1.4); entities, repositories, controllers (Phase 2); JWT implementation (Phase 3); OpenAPI annotations (Phase 4); Docker (Phase 5).

### 10.2 Requirements covered (proposal item 2)

Exactly the T1.2 entry in `implementation-plan.md` §5, with each dependency traceable to the approved baseline:

| Approved baseline item (§16) | Covered by T1.2 dependency |
|---|---|
| Spring Web | `spring-boot-starter-web` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| Bean Validation | `spring-boot-starter-validation` |
| Spring Security | `spring-boot-starter-security` |
| JWT | `spring-boot-starter-oauth2-resource-server` (design decision D-2, Section 10.3) |
| H2 | `com.h2database:h2` |
| PostgreSQL | `org.postgresql:postgresql` |
| Flyway | `flyway-core` + `flyway-database-postgresql` |
| springdoc-openapi | `springdoc-openapi-starter-webmvc-ui` |
| JUnit 5, Mockito, MockMvc, Spring Boot Test | `spring-boot-starter-test` — **already present** from T1.1 (contains JUnit 5, Mockito, AssertJ, `spring-test`/MockMvc) |
| Java 21, Spring Boot 3.x, Maven + Wrapper | Already established in T1.1 (**VERIFIED**, accepted per D-04) — no change |
| Docker and Docker Compose | **Not a Maven dependency** — Phase 5 deliverable (T5.1–T5.2). Explicitly excluded here |

Nothing outside the §16 baseline is proposed. The optional enhancements in `requirements.md` (Swagger, JWT) were already approved as in-scope by `requirements-decisions.md` and are covered above; no new requirement is introduced by this proposal.

### 10.3 Proposed technical design (proposal item 3)

**D-1 — Version-lock strategy (decision):** The Spring Boot parent BOM (`spring-boot-starter-parent` 3.5.16) is the single version authority. Every dependency except springdoc is added **without** a `<version>` tag and resolves at the BOM-managed version. The single explicit pin is a `<springdoc-openapi.version>2.10.0</springdoc-openapi.version>` property. Rationale: the plan requires "version-locked" dependencies and the Reviewer must check "version consistency"; BOM management guarantees internal consistency across the Spring stack, while hand-pinned versions for BOM-managed libraries would *increase* drift risk, not reduce it. "Version-lock" is therefore implemented as: parent-pinned versions for the ecosystem + one explicit, documented pin for the one library outside it.

**D-2 — JWT support via the resource-server starter (decision, applies to the dependency only):** `spring-boot-starter-oauth2-resource-server` is chosen over third-party JWT libraries (e.g., jjwt). It brings `spring-security-oauth2-jose` (Nimbus) and is version-managed by the Boot BOM, integrates natively with Spring Security's validation pipeline, and is the current Spring-recommended approach for issuing and validating JWTs. Implementation (key handling, expiry, `POST /auth/login`) remains a Phase 3 design task per §19. This choice does not preclude any Phase 3 strategy; if the Tech Lead later prefers a different library, the pin changes here — which is exactly what T1.2 review is for.

**D-3 — springdoc artifact (decision):** `springdoc-openapi-starter-webmvc-ui` (not `-webmvc-api`) because §14 requires Swagger UI access, and the servlet (webmvc) variant matches `spring-boot-starter-web`. Version **2.10.0** — final 2.x release; the 2.x line is the one built for Spring Boot 3.x (Section 8.3).

**D-4 — Driver scopes (decision):** `h2` and `postgresql` as `runtime` scope — neither is needed at compile time; H2 additionally lands on the test classpath (runtime scope is included in tests), which the current `contextLoads` test relies on once JPA is present (R-01). `flyway-database-postgresql` as `runtime` (per Redgate guidance).

**Proposed `pom.xml` dependency set** (all version-managed by the Boot BOM except where noted; ordering follows the baseline):

| # | Coordinates | Scope | Version source | Baseline traceability |
|---|---|---|---|---|
| 1 | `org.springframework.boot:spring-boot-starter-web` | compile | Boot BOM 3.5.16 | §16 Spring Web |
| 2 | `org.springframework.boot:spring-boot-starter-data-jpa` | compile | Boot BOM | §16 Spring Data JPA |
| 3 | `org.springframework.boot:spring-boot-starter-validation` | compile | Boot BOM | §16 Bean Validation |
| 4 | `org.springframework.boot:spring-boot-starter-security` | compile | Boot BOM | §16 Spring Security |
| 5 | `org.springframework.boot:spring-boot-starter-oauth2-resource-server` | compile | Boot BOM | §16 JWT (D-2) |
| 6 | `org.flywaydb:flyway-core` | compile | Boot BOM | §16 Flyway; §12 |
| 7 | `org.flywaydb:flyway-database-postgresql` | runtime | Boot BOM | §12 PostgreSQL compatibility (R-03) |
| 8 | `com.h2database:h2` | runtime | Boot BOM | §16 H2; §12 dev/test |
| 9 | `org.postgresql:postgresql` | runtime | Boot BOM | §16 PostgreSQL; §12 prod |
| 10 | `org.springdoc:springdoc-openapi-starter-webmvc-ui` | compile | **explicit property 2.10.0** | §16 springdoc-openapi; §14 (D-3) |
| 11 | `org.springframework.boot:spring-boot-starter-security-test` | test | Boot BOM | §16 Spring Security (test support: `@WithMockUser`, MockMvc security processors) — **recommended; Tech Lead decision requested** (M-02) |
| — | `org.springframework.boot:spring-boot-starter-test` | test | already present | §16 test stack — unchanged |

**Explicitly NOT added** (prevents "unnecessary dependencies" findings):

- `io.jsonwebtoken:jjwt-*` or `com.auth0:java-jwt` — superseded by decision D-2.
- `spring-boot-starter-jdbc` — arrives transitively with `spring-boot-starter-data-jpa`.
- `spring-boot-starter-flyway` — not part of the Spring Boot 3.x dependency model; `flyway-core` is declared directly (that starter belongs to the Boot 4 line).
- Spring Boot Actuator, devtools, Lombok, any caching/messaging library — absent from the approved §16 baseline.
- Docker tooling — Phase 5, and not a Maven dependency in any case.

**Expected consequences while nothing else changes (feeds R-01):** with this set on the classpath and the current `application.yml` untouched, the next application start will: auto-configure an embedded H2 `DataSource` (H2 present), start Hibernate with no entities, run Flyway with zero migrations (default location `classpath:db/migration` is empty — informational, non-fatal), apply Spring Security's default chain to the (currently empty) endpoint surface, and start embedded Tomcat on port 8080. All of this is *implicit* until T1.3/T1.4 make it explicit; it is stated here so the Tester and Reviewer verify against expectations rather than surprises.

### 10.4 Files to create or modify (proposal item 4)

| File | Action | Content |
|---|---|---|
| `pom.xml` | **Modify** (only file) | Add property `springdoc-openapi.version` = 2.10.0; add dependencies #1–#10 from the table in Section 10.3 (plus #11 if approved). No other element changes — parent, properties, enforcer configuration, and plugins remain untouched. |
| `agent-reports/T1.2/developer-report.md` | Create (by Developer, after implementation) | Evidence record per `agent-reports/README.md` conventions. |
| `agent-reports/T1.2/tester-report.md` | Create (by Tester, independently) | Same. |
| `agent-reports/T1.2/reviewer-report.md` | Create (by Reviewer, after the audit) | Same. |

**Contingency only (requires separate Tech Lead authorization before use):** `src/test/resources/application.yml` — solely if R-01 materializes as a `contextLoads` failure. It is not part of this proposal's scope.

No Java source, no `application.yml` (main), no migration files, no configuration profiles may be touched in T1.2.

### 10.5 Dependencies (proposal item 5)

Two dimensions:

1. **Maven dependencies delivered by this task** — the set in Section 10.3 (that set *is* the deliverable).
2. **Dependencies of the task itself:**
   - T1.1 accepted (D-04) — **VERIFIED**; the parent, Wrapper, Java 21 baseline and test skeleton this task builds on exist.
   - Local JDK 21 and Maven Wrapper execution environment (proven during T1.1 execution per D-04 evidence).
   - Network access to Maven Central for dependency resolution (also used by this execution for metadata research).
   - No dependency on T1.3–T1.6 or any later phase; those tasks *consume* this one's output.

### 10.6 Implementation steps (proposal item 6)

For the Developer, strictly after Tech Lead approval:

1. Add the property `<springdoc-openapi.version>2.10.0</springdoc-openapi.version>` to `<properties>` (the only explicit version in the project).
2. Add compile-scope starters #1–#5 (web, data-jpa, validation, security, oauth2-resource-server) without `<version>` tags.
3. Add `org.flywaydb:flyway-core` (#6, compile) and `org.flywaydb:flyway-database-postgresql` (#7, runtime).
4. Add `com.h2database:h2` (#8, runtime) and `org.postgresql:postgresql` (#9, runtime).
5. Add `springdoc-openapi-starter-webmvc-ui` (#10) referencing the property.
6. If and only if the Tech Lead approves item #11, add `spring-boot-starter-security-test` (test scope).
7. Execute `.\mvnw.cmd clean verify` and capture the build result and the `contextLoads` test summary.
8. Execute `.\mvnw.cmd dependency:tree` (output captured to a file or the report) as version-consistency evidence: every Boot-ecosystem dependency must resolve at its BOM version; the only non-BOM version must be springdoc 2.10.0.
9. Record files changed, commands and results in `agent-reports/T1.2/developer-report.md`, including any deviation from these steps and its justification.
10. **If step 7 fails** (R-01 contingency): stop, record the failure, and request Tech Lead authorization for the test-scoped contingency in Section 10.4 before applying any fix. Do not "fix" it by changing main configuration (T1.3/T1.4 scope) or by adding/removing dependencies not in the approved set.

### 10.7 Testing strategy (proposal item 7)

- **No new test classes are proposed.** T1.2's out-of-scope rule ("dependency usage in business code") excludes business tests, and there are no endpoints, entities or services to test yet.
- The existing `HumanItChallengeApplicationTests.contextLoads` is the regression gate and, after this task, doubles as the **auto-configuration smoke test**: it proves the full stack (web + JPA + security + Flyway + springdoc + H2) wires into a loadable context.
- **Verification evidence:** `clean verify` = BUILD SUCCESS with `Tests run: 1, Failures: 0, Errors: 0`; `dependency:tree` captured for the Reviewer.
- **Independent verification:** the Tester re-runs the build and inspects the dependency tree **before** reading the Developer report, per `agent-reports/README.md` §3.
- Forward note (no action now): later phases may add only `spring-boot-starter-security-test` (proposed here as #11); the rest of the testing stack (JUnit 5, Mockito, MockMvc, AssertJ) is already inside `spring-boot-starter-test`, satisfying `requirements-decisions.md` §15.

### 10.8 Acceptance criteria (proposal item 9)

The three criteria are quoted **verbatim** from `implementation-plan.md` §5, T1.2; the verification method is proposed by this document:

| # | Acceptance criterion (verbatim) | Proposed verification method | Status at proposal time |
|---|---|---|---|
| 1 | "All required dependencies are present." | `dependency:tree` / `dependency:list` output cross-checked against the table in Section 10.3 and baseline §16; every §16 item mapped in Section 10.2 is covered. | PENDING VERIFICATION (Developer + Tester) |
| 2 | "No unnecessary dependencies are introduced." | Every `pom.xml` dependency traces to a §16 baseline item or a documented decision (D-1…D-4); `dependency:tree` reviewed for unapproved transitive introductions; the explicit NOT-added list in Section 10.3 is respected. | PENDING VERIFICATION (Reviewer) |
| 3 | "Build and tests pass." | `.\mvnw.cmd clean verify` → BUILD SUCCESS, exit code 0; `contextLoads` green. | PENDING VERIFICATION (Developer + Tester) |

### 10.9 Alternatives, trade-offs and assumptions

**Alternatives considered:**

- *Hard-pin every dependency version explicitly* (literal reading of "version-lock"): rejected — it would duplicate the BOM, create internal inconsistency risk, and directly work against the Reviewer's "version consistency" criterion. The BOM *is* the lock.
- *jjwt instead of the resource-server starter*: rejected for T1.2 — three artifacts, explicit out-of-BOM pins, manual validation glue; inferior on the criteria this task is audited against. Reversible at Phase 3 design if the Tech Lead prefers it.
- *Defer `flyway-database-postgresql` to Phase 5*: rejected — R-03; the plan's own risk section warns that H2/PostgreSQL migration compatibility must be verified in each persistence-related phase, and the dependency is the precondition for that verification.
- *Defer springdoc to Phase 4*: rejected — the plan explicitly lists springdoc-openapi in T1.2's in-scope dependency list.
- *Add only what T1.3/T1.4 need now (web, JPA, H2) and defer security/springdoc*: rejected — it contradicts the T1.2 task definition, whose purpose is to verify compatibility of the **complete** baseline before any business code exists.

**Trade-offs:**

- Full starter set now ⇒ the application's runtime behavior changes before its configuration exists (R-01). Accepted because T1.2's own verification step is designed to catch problems while the codebase is still trivial, and because piecemeal dependency addition would multiply approval rounds without reducing risk.
- One explicit pin (springdoc) ⇒ a single out-of-BOM upgrade point. Accepted as unavoidable; isolated in a named property for reviewability.

**Assumptions (all PENDING VERIFICATION via the T1.2 build unless noted):**

- The Boot 3.5.16 BOM manages `flyway-database-postgresql` (consistent with Boot's practice of managing all Flyway modules on the 3.x line).
- `contextLoads` remains green through embedded-H2 auto-detection (R-01).
- springdoc 2.10.0 starts cleanly under Boot 3.5.16 (2.x line built against Boot 3.5.x per Section 8.3).
- **VERIFIED assumption:** the Tech Lead's approval of this proposal is required and must precede Developer implementation (`implementation-plan.md` §2; `requirements-decisions.md` §18).

## 11. Final status

**PROPOSED — awaiting Tech Lead approval.** This document contains a design and implementation proposal only. No dependency was added, no file outside this report was created or modified, and no command was executed by this agent. Per the approved governance rules, the Developer must not begin T1.2 implementation until the Tech Lead approves this proposal.

## 12. Outstanding issues

1. **Tech Lead approval of this T1.2 proposal** — required before implementation (governance).
2. **Decision requested:** include `spring-boot-starter-security-test` now (recommended, item #11) or defer to Phase 3 (re-opening `pom.xml` then). Not a requirement; the Tech Lead's call.
3. **PENDING VERIFICATION:** springdoc 2.10.0 resolution and context startup under Boot 3.5.16 (R-02); Boot BOM management of `flyway-database-postgresql` (D-4/R-03); `contextLoads` green with the full set (R-01). All three close through the T1.2 build and review.
4. Prior proposals **P-01…P-04** in `agent-reports/decisions.md` remain PROPOSED and are unaffected by this execution.

## 13. Handoff to next agent

- **To the Tech Lead:** review and approve or reject this proposal (a scope-level approval of the dependency set in Section 10.3 and the decision items D-1…D-4). Please also rule on the optional test dependency (#11). Upon approval, authorize the Developer to execute Section 10.6.
- **To the Developer:** implement only after approval; only `pom.xml` changes; follow steps 1–10 and record evidence in `agent-reports/T1.2/developer-report.md`; if step 7 fails, stop and request the contingency authorization.
- **To the Tester:** independently verify build + tests green and the dependency tree against Section 10.3 **before** reading the Developer report.
- **To the Reviewer:** audit dependency necessity, version consistency and unused libraries against Sections 10.2–10.3; treat any dependency outside that table as a finding.

---

**Status of this report:** Authored 2026-11-09 (Europe/Lisbon) by the Architect role from in-project artifacts and cited external sources. All 13 mandatory sections present. Read-back verification of this file's persistence is recorded in the agent's execution output. A single cross-reference typo (Section 10.2 JWT row: "D-1" corrected to "D-2") was fixed within this same execution before handoff; no content change otherwise.
