# Tech Lead Decision Log — Human IT Challenge

**Location:** `agent-reports/decisions.md`
**Initialized:** 2026-11-09 (Europe/Lisbon)
**Maintained by:** Tech Lead (entries are appended by or on behalf of the Tech Lead only)
**Method:** Evidence-only. No approval is recorded unless evidence exists inside the project or in the Tech Lead's direct tasking. No historical approval has been invented.

---

## 1. Log rules (append-only)

1. Entries are numbered sequentially (`D-nn`) and appended in chronological order.
2. Existing entries are **never deleted or rewritten**. A correction or reversal is made by appending a new entry that explicitly supersedes the earlier one.
3. **Proposed** decisions (`P-nn`) are kept distinct from **approved** decisions (`D-nn`). A proposal is not a decision until the Tech Lead approves it.
4. Each entry states its evidence. Entries without in-project evidence or direct Tech Lead tasking are not recorded.
5. The earlier log at `reports/approval-and-decision-log.md` remains unmodified. This log continues the chronology; identifiers below align with it (D-01, D-02) to keep cross-references unambiguous.

## 2. Approved decisions

### D-01 — Requirements decisions and implementation plan approved (historical)

| Field | Value |
|---|---|
| Date of record | NOT AVAILABLE — no dated approval record exists in the project (gap G-09) |
| Decided by | Tech Lead |
| Scope | Requirements decisions (`requirements-decisions.md`) and the consolidated implementation plan (`implementation-plan.md`), including the six-phase plan and the T1.1–T6.3 task breakdown |
| Evidence | Status lines in both documents: *"Status: Approved by the Tech Lead"* (`implementation-plan.md`, line 3) and *"Requirements decisions and the implementation plan are approved by the Tech Lead"* (`requirements-decisions.md`, line 5) |
| Effect | Architecture design and phased implementation may proceed under the approved workflow |
| Cross-reference | `reports/approval-and-decision-log.md`, entry D-01 (identical evidence) |

### D-02 — Reporting-infrastructure task authorized

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Tech Lead (tasking received directly in the current session) |
| Scope | Documentation-only: create `agent-reports/README.md`, `agent-reports/decisions.md`, and reconstructed `agent-reports/T1.1/architect-proposal.md`, `developer-report.md`, `tester-report.md`, using only in-project evidence |
| Constraints | Do not modify application code, `pom.xml`, Maven Wrapper, requirements/planning documents, existing `reports/*.md`, agent definitions or Git configuration. Do not create `reviewer-report.md` yet. Record autonomous termination as PENDING VERIFICATION |
| Evidence | Tasking received directly from the Tech Lead in the current session (2026-11-09); consistent with the earlier authorization recorded in `reports/approval-and-decision-log.md`, entry D-02 |
| Effect | The five files listed above were created; nothing else was changed |

### D-03 — Reporting conventions adopted (proposed by reporting task; effective for agent-reports/ only)

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Tech Lead (via authorization of the reporting task that defines these conventions in `agent-reports/README.md`) |
| Scope | (a) Mandatory 13-section report format; (b) evidence markers VERIFIED / RECONSTRUCTED / NOT AVAILABLE / PENDING VERIFICATION; (c) execution-history convention with `history/` archival and no silent overwrites; (d) append-only decision-log rules |
| Evidence | Authorized tasking of 2026-11-09; conventions documented in `agent-reports/README.md` |
| Effect | All future agent reporting under `agent-reports/` follows these conventions |

## 3. Proposed decisions (awaiting Tech Lead — NOT approved)

The following are proposals recorded for the Tech Lead's decision. **None of them is approved.** They must not be treated as authorization to act.

### P-01 — Ratify the reconstructed T1.1 design as the reference proposal

| Field | Value |
|---|---|
| Proposal | Ratify `agent-reports/T1.1/architect-proposal.md` (reconstruction of the T1.1 bootstrap design) as the reference proposal for T1.1, since no historical Architect proposal document exists (NOT AVAILABLE) |
| Raised by | Reporting task, 2026-11-09 |
| Evidence basis | Bootstrap artifacts: `pom.xml`, `src/main/java/com/humanit/challenge/HumanItChallengeApplication.java`, `src/main/resources/application.yml`, `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java`, Maven Wrapper |
| Status | **PROPOSED** |

### P-02 — Authorize a standalone start/termination verification run for T1.1

| Field | Value |
|---|---|
| Proposal | Authorize an execution to close gap G-01: start the application standalone (`.\mvnw.cmd spring-boot:run` or `java -jar`), capture startup log, shutdown log and process exit code, and record the results in the T1.1 Tester report |
| Raised by | Reporting task, 2026-11-09 |
| Evidence basis | `reports/build-and-test-evidence-report.md` Section 4 and `reports/evidence-gap-register.md` G-01: Surefire reports prove the context-load test but **do not prove autonomous termination** |
| Status | **PROPOSED** |

### P-03 — Authorize the independent Reviewer audit of T1.1 (audit A-01)

| Field | Value |
|---|---|
| Proposal | Authorize the Reviewer role to perform audit A-01 (`reports/reviewer-audit-report.md`) on the T1.1 execution and, once performed, create `agent-reports/T1.1/reviewer-report.md` |
| Raised by | Reporting task, 2026-11-09 |
| Evidence basis | `implementation-plan.md` Section 2 (independent audit required before approval) and gap G-04: no Reviewer audit for T1.1 exists |
| Status | **PROPOSED** |

### P-04 — Authorize correction of the stale status line in `requirements-decisions.md`

| Field | Value |
|---|---|
| Proposal | Authorize a one-line status update to `requirements-decisions.md` ("Application implementation has not started"), which conflicts with existing T1.1 artifacts and the 2026-11-09 test run |
| Raised by | Reporting task, 2026-11-09 |
| Evidence basis | `reports/evidence-gap-register.md` G-07 and `reports/task-status-report.md` Section 4 (documented, not corrected, per task constraints) |
| Status | **PROPOSED** |

## 4. Missing historical decisions (explicitly not reconstructed)

The following approvals are required by the governance model but **no record exists in the project**. They are listed only to state their absence; none is assumed, inferred or reconstructed:

| Required decision | Record |
|---|---|
| Tech Lead approval of the T1.1 task scope itself | **NOT AVAILABLE** |
| Tech Lead approval of T1.1 implementation completion | **NOT AVAILABLE** |
| Tech Lead acceptance of T1.1 verification results | **NOT AVAILABLE** |
| Tech Lead approval of Phase 1 completion | **NOT AVAILABLE** |
| Any approval for tasks T1.2 onward | **NOT AVAILABLE** (no evidence of activity) |

## 5. Entry template (append below this line)

```text
### D-<nn> — <Decision title>

| Field | Value |
|---|---|
| Date | YYYY-MM-DD |
| Decided by | Tech Lead |
| Scope | <task/phase> |
| Evidence | <artifact or direct tasking> |
| Effect | <what may start/complete as a result> |
```

### D-04 — T1.1 formally ACCEPTED

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly via tasking in the current session) |
| Scope | T1.1 — Project bootstrap only (implementation, independent verification and final review) |
| Evidence | - Maven `clean verify`: **BUILD SUCCESS**, exit code 0 (`.\mvnw.cmd clean verify`, 2026-11-09T12:10:20Z) — `agent-reports/T1.1/tester-report.md` Sections 7–8.1, execution `T1.1-EXEC-002-TST-01` <br> - Tests: **1 passed (run), 0 failures, 0 errors, 0 skipped** (`contextLoads`) — tester report Section 8.1, corroborated by `target/surefire-reports/com.humanit.challenge.HumanItChallengeApplicationTests.txt` (Reviewer report Section 8.2) <br> - **Spring Boot standalone startup VERIFIED**: `Started HumanItChallengeApplication in 0.995 seconds` (PID 6344), corroborated at `target/standalone-run.log` line 47 — tester report Sections 8.2–8.4, Reviewer report Section 8.2 <br> - **Java 21 baseline VERIFIED**: runtime Java 21.0.12.1; Maven Enforcer `requireJavaVersion [21,22)` fail-fast rule present and passed — tester report Section 8.4, Reviewer report Section 8.2 <br> - Reviewer final review (`T1.1-EXEC-003-RVW-02`, `agent-reports/T1.1/reviewer-report.md` Section 11): **"FINAL REVIEW COMPLETE — READY FOR TECH LEAD ACCEPTANCE"**; **CRITICAL findings: none; MAJOR technical findings: none** (Section 9) |
| Minor findings | Minor findings accepted as **non-blocking**: Tester T-F02, T-F03, T-F04; Reviewer F-03 (evidence precision), F-04 (JDK-21 pin), F-05 (G-06), F-06 (reporting hygiene), F-07 (G-07). The Reviewer's single MAJOR finding F-02 is governance-only (approval records pending) and is resolved by this very decision |
| Cross-reference | Resolves in this log's Section 4 the rows *"Tech Lead approval of T1.1 implementation completion"* and *"Tech Lead acceptance of T1.1 verification results"* (gap G-05). The row *"Tech Lead approval of the T1.1 task scope itself"* and the Phase 1 row remain NOT AVAILABLE — this decision records acceptance, not a separate scope-approval record. Proposals P-01 … P-04 remain PROPOSED and are not affected by this decision |
| Effect | T1.1 is formally accepted. Per the tasking of 2026-11-09, T1.2 remains **not started** and still requires the Tech Lead's explicit authorization before it may begin |

### D-05 — T1.2 formally ACCEPTED

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly via tasking in the current session) |
| Scope | T1.2 — Technical baseline and dependency verification only (implementation, independent verification and final review) |
| Evidence | - T1.2 architect proposal approved via direct tasking, quoted in `agent-reports/T1.2/developer-report.md` Section 1 ("Human Tech Lead approves the T1.2 architecture proposal, subject to the instructions below") — closes Reviewer finding R-F05 <br> - Developer `T1.2-EXEC-001-DEV-01` completed: only `pom.xml` modified; `.\mvnw.cmd clean verify` BUILD SUCCESS, exit 0 (2026-11-09T12:34:57Z); dependency tree captured — developer-report.md Sections 6–8, §11 <br> - Tester `T1.2-EXEC-001-TST-01`: **PASS** — independent `clean verify` BUILD SUCCESS (12:42:00Z), `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`, Surefire-corroborated; dependency set/scopes audited — tester-report.md Sections 8.1–8.2, §11 <br> - Reviewer `T1.2-EXEC-001-RVW-01`: **"FINAL REVIEW COMPLETE — READY FOR TECH LEAD ACCEPTANCE"**; all three T1.2 acceptance criteria met; **CRITICAL findings: none; MAJOR findings: none** — reviewer-report.md Sections 8.1, 9, 11 |
| Minor findings | Zero Critical and zero Major findings across all three T1.2 reports (each §9). Minor findings accepted as **non-blocking**: Developer F-D01…F-D05; Tester T-F01…T-F05 and T-STIND-D01; Reviewer R-F01…R-F06 |
| Cross-reference | Supersedes, for T1.2 specifically, Section 4's row "Any approval for tasks T1.2 onward — NOT AVAILABLE" (recorded when no T1.2 evidence existed; the row remains standing for T1.3+ per the append-only rule). Closes R-F05 (decision-log hygiene). Proposals P-01…P-04 remain PROPOSED and are unaffected |
| Effect | T1.2 is formally accepted and **closed**. Next authorized activity: **Git/GitHub setup** (named directly by the Tech Lead in tasking; its in-project task ID was not verified by the recording execution). **T1.3 is NOT yet authorized** and may not begin without explicit Tech Lead authorization |

### D-06 — T1.3 formally ACCEPTED

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly via tasking in the current session) |
| Scope | T1.3 — Environment configuration and secret externalization (Phase 1) only (implementation, independent verification and final review) |
| Evidence | - T1.3 architect proposal approved via direct tasking (Architect Revision 4, execution `T1.3-EXEC-007-ARCH-01`), referenced in `agent-reports/T1.3/developer-report.md` Section 1 and `agent-reports/T1.3/reviewer-report.md` Section 1 <br> - Developer `T1.3-EXEC-008-DEV-01` completed: 8 authorized items implemented (JWT secret validation moved out of Bean Validation into `JwtSecretValidator`; `@Profile("dev & !prod")` guard; high-precedence test overrides; default-vs-active profile distinction; combined-profile absence assertion); `.\mvnw.cmd clean test` BUILD SUCCESS, exit 0 (2026-11-09T15:23:12Z); Tests run: 31, Failures: 0, Errors: 0, Skipped: 0 — developer-report.md Sections 6–8, §11 <br> - Tester `T1.3-EXEC-009-TST-01`: **PASS** — independent `.\mvnw.cmd clean test` BUILD SUCCESS; 31/0/0/0; verified JWT secret validation (missing/blank/malformed/too-short fail; 32-byte boundary accepted; no value leak), dev/prod profile behavior (default dev, prod isolation, combined absence, both files load), pagination validation (0/20/100 defaults; invalid overrides fail) — tester-report.md Sections 8.1–8.4, §11 <br> - Reviewer `T1.3-EXEC-010-REV-01`: **"REVIEW COMPLETE — NO BLOCKING ISSUES (0 Critical, 0 Major)"**; implementation matches Architect Revision 4 amendments 1–4; all T1.3 acceptance criteria met; scope compliance confirmed; residual risk RK-01 preserved — reviewer-report.md Sections 8.2–8.4, 9, 11 |
| Minor findings | Zero Critical and zero Major findings across all three T1.3 reports (Developer §9; Tester §9; Reviewer §9). Minor findings accepted as **non-blocking**: Tester F-T01…F-T05; Reviewer R-M01…R-M08 (R-M01…R-M05 correspond to F-T01…F-T05; R-M06…R-M08 are additional documentation/coverage/maintainability gaps). The Developer's claim of "no findings" is not fully supported; the Tester and Reviewer independently identified the gaps listed above, which are documentation, test-coverage, harness-consistency and maintainability issues rather than runtime or correctness defects |
| Cross-reference | Supersedes, for T1.3 specifically, Section 4's row "Any approval for tasks T1.2 onward — NOT AVAILABLE" (recorded when no T1.3 evidence existed; the row remains standing for T1.4+ per the append-only rule). Closes the Tester's outstanding item 2 (Tech Lead acceptance of T1.3 pending) and the Reviewer's outstanding item 4 (Tech Lead acceptance of T1.3 pending). Residual risk RK-01 / F-02 / F-SP-02 remains accepted under OD-02 and is preserved (not reduced) by this decision. Proposals P-01…P-04 remain PROPOSED and are unaffected |
| Effect | T1.3 is formally accepted and **closed**. **T1.4 is NOT yet authorized** and may not begin without explicit Tech Lead authorization |

---

### D-07 — T1.4 formally ACCEPTED

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly via tasking in the current session) |
| Scope | T1.4 — Persistence and migration baseline (Phase 1) only (implementation, independent verification and final review) |
| Evidence | - T1.4 architect proposal approved via direct tasking (execution `T1.4-EXEC-001-ARCH-01`), referenced in `agent-reports/T1.4/developer-report.md` Section 1 and `agent-reports/T1.4/reviewer-report.md` Section 1 <br> - Developer `T1.4-EXEC-002-DEV-01` completed: H2 dev datasource, PostgreSQL prod datasource from `SPRING_DATASOURCE_*`, Flyway enabled with comment-only `V1__baseline.sql`, `ddl-auto: validate`, dev-only H2 Console, test-harness H2 override; `.\mvnw.cmd clean verify` BUILD SUCCESS, exit 0 (2026-11-09); Tests run: 31, Failures: 0, Errors: 0, Skipped: 0 — developer-report.md Sections 6–8, §11 <br> - Tester `T1.4-EXEC-003-TST-01`: **PASS** — independent `.\mvnw.cmd clean verify` BUILD SUCCESS; 31/0/0/0; Flyway applies `V1__baseline.sql` as version 1 on H2; `ddl-auto: validate` common to both profiles; H2 Console dev-only; prod external variables; JWT/profile behavior unchanged; no business tables or Docker configuration — tester-report.md Sections 8.1–8.9, §11 <br> - Reviewer `T1.4-EXEC-005-REV-02`: **"PASS (reviewer recommendation) — no Critical or Major findings; no blocking defects"**; migration ownership (Flyway, not Hibernate) confirmed; profile separation and secret/credential handling correct; scope compliance confirmed — reviewer-report.md Sections 8.1, 9, 10, 11 |
| Minor findings | Zero Critical and zero Major findings across all three T1.4 reports (Developer §9; Tester §9; Reviewer §9). Minor findings accepted as **non-blocking**: F-R01 through F-R04 (verification/consistency gaps, including direct assertion of `flyway_schema_history` version 1, prod fail-fast behavior when datasource variables are missing, combined-profile H2 Console exposure, and H2 Console reachability pending Phase 3 security configuration) |
| Cross-reference | Supersedes, for T1.4 specifically, Section 4's row "Any approval for tasks T1.2 onward — NOT AVAILABLE" (recorded when no T1.4 evidence existed; the row remains standing for T1.5+ per the append-only rule). Closes the Tester's outstanding items (Tech Lead acceptance of T1.4 pending) and the Reviewer's outstanding item (Tech Lead acceptance of T1.4 pending). PostgreSQL integration verification deferred to Phase 5 (OT-01). H2 Console accessibility to be addressed in Phase 3 (T3.1/T3.3). Proposals P-01 … P-04 remain PROPOSED and are unaffected |
| Effect | T1.4 is formally accepted and **closed**. 31/31 automated tests passed. Developer, Tester and Reviewer completed. No Critical or Major findings. F-R01 through F-R04 accepted as non-blocking. PostgreSQL integration verification deferred to Phase 5. H2 Console accessibility to be addressed in Phase 3. No additional remediation required for T1.4. **T1.5 is NOT yet authorized** and may not begin without explicit Tech Lead authorization |

---
**Status of this log:** Initialized 2026-11-09 with D-01 … D-03 (approved) and P-01 … P-04 (proposed). All historical approvals other than D-01 are NOT AVAILABLE. Append new entries below Section 5's template. **Updated 2026-11-09:** D-04 appended — T1.1 formally accepted by the human Tech Lead (see entry D-04). **Updated 2026-11-09:** D-05 appended — T1.2 formally accepted by the human Tech Lead (see entry D-05). **Updated 2026-11-09:** D-06 appended — T1.3 formally accepted by the human Tech Lead (see entry D-06). **Updated 2026-11-09:** D-07 appended — T1.4 formally accepted by the human Tech Lead (see entry D-07). **Updated 2026-11-09:** D-08 appended — T1.5 formally accepted by the human Tech Lead (see entry D-08). **Updated 2026-11-09:** D-09 appended — T1.6 formally accepted by the human Tech Lead; Phase 1 COMPLETE (see entry D-09).

---

### D-08 — T1.5 formally ACCEPTED

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly via tasking in the current session) |
| Scope | T1.5 — Cross-cutting application scaffolding (Phase 1) only (implementation, independent verification and final review) |
| Evidence | - T1.5 architect proposal approved via direct tasking (execution `T1.5-EXEC-001-ARCH-01`), referenced in `agent-reports/T1.5/developer-report.md` Section 1 and `agent-reports/T1.5/reviewer-report.md` Section 1 <br> - Developer `T1.5-EXEC-002-DEV-01` completed: `GlobalExceptionHandler` (RFC 9457 Problem Details, `@RestControllerAdvice`), `OpenApiConfig` (minimal `OpenAPI` bean with `Info`), common logging pattern in `application.yml`; `.\mvnw.cmd clean verify` BUILD SUCCESS, exit 0 (2026-11-09); Tests run: 41, Failures: 0, Errors: 0, Skipped: 0 — developer-report.md Sections 6–8, §11 <br> - Tester `T1.5-EXEC-003-TST-01`: **PASS** — independent `.\mvnw.cmd clean verify` BUILD SUCCESS; 41/0/0/0; verified RFC 9457 response format, `application/problem+json` content type, 400/405/404/500 status preservation, `ResponseStatusException` status preservation, sanitization (no stack traces, secrets or rejected values), OpenAPI bean metadata, logging baseline, absence of test controllers in production source, no out-of-scope changes — tester-report.md Sections 8.1–8.14, §11 <br> - Reviewer `T1.5-EXEC-004-REV-01`: **"REVIEW COMPLETE — PASS (reviewer recommendation)"**; no Critical or Major findings; no blocking defects; 7 Minor/Info findings (R-F01…R-F07), all non-blocking; implementation satisfies all T1.5 acceptance criteria — reviewer-report.md Sections 8.1–8.10, 9, 11 |
| Minor findings | Zero Critical and zero Major findings across all three T1.5 reports (Developer §9; Tester §9; Reviewer §9). Minor findings accepted as **non-blocking**: R-F01 through R-F05 (Reviewer). R-F01 — `ResponseStatusException` reason propagated verbatim into Problem Details `detail`; no concrete leak in current code (sole caller is safe, no other callers exist); channel is only as safe as future callers. R-F02 — non-standard HTTP status body/response status mismatch (robustness edge case). R-F03 — validation maps only `FieldError`s; global/object-level constraint violations not represented. R-F04 — deferred items (409/401/403 handlers, problem-type URIs, bearer OpenAPI scheme, structured logging) not implemented; acceptable for scaffolding. R-F05 — test coverage gaps (no explicit assertion for `type == about:blank`, absence of `rejectedValue`, or `ResponseStatusException` detail behavior). Additional Info findings R-F06 (governance: D-08 not yet recorded) and R-F07 (process/audit note: developer's first-run compilation failure not independently inspected by Tester) are also accepted as non-blocking. Tester T-F01…T-F04 and Developer F-D01…F-D03 are likewise accepted as non-blocking. |
| Cross-reference | Supersedes, for T1.5 specifically, Section 4's row "Any approval for tasks T1.2 onward — NOT AVAILABLE" (recorded when no T1.5 evidence existed; the row remains standing for T1.6+ per the append-only rule). Closes the Tester's outstanding item OT-06 (Tech Lead final acceptance of T1.5 pending) and the Reviewer's outstanding item OT-08 (record T1.5 authorization/completion approval and final Tech Lead acceptance). R-F06 (governance: no D-08 entry) is resolved by this very decision. Resolves the Tester's OT-04 (hardening: document/enforce that `ResponseStatusException` reasons must be client-safe) by recording the Tech Lead's requirement that `ResponseStatusException` reasons must be client-safe in future business controllers. R-F02 and R-F03 may be addressed when relevant functionality (business DTOs, non-standard status codes) is introduced. Deferred work — 409 conflict handlers, 401/403 security mappings and filter-level handling, problem-type URIs, bearer security scheme in OpenAPI, structured logging — remains assigned to future phases (Phase 2/3/4/5). Proposals P-01 … P-04 remain PROPOSED and are unaffected |
| Effect | T1.5 is formally accepted and **closed**. 41/41 automated tests passed. Developer, Tester and Reviewer completed. No Critical or Major findings. No blocking defects. R-F01 through R-F05 accepted as non-blocking. `ResponseStatusException` reasons must be client-safe in future business controllers (recorded as a standing requirement). R-F02 and R-F03 may be addressed when relevant functionality is introduced. Deferred 409, 401/403 and OpenAPI security work remains assigned to future phases. **T1.6 is authorized** as the next task. |

---

### D-09 — T1.6 formally ACCEPTED

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly via tasking in the current session) |
| Scope | T1.6 — Phase 1 sanity and documentation checkpoint (Phase 1) only (implementation, independent verification and final review) |
| Evidence | - T1.6 architect proposal approved via direct tasking (execution `T1.6-EXEC-002-ARCH-02`), referenced in `agent-reports/T1.6/developer-report.md` Section 1 and `agent-reports/T1.6/reviewer-report.md` Section 1 <br> - Developer `T1.6-EXEC-003-DEV-01` completed: created root `README.md` and `CHANGELOG.md`; `.\mvnw.cmd clean verify` BUILD SUCCESS, exit 0 (2026-11-09T16:48:37Z); Tests run: 41, Failures: 0, Errors: 0, Skipped: 0 — developer-report.md Sections 6–8, §11 <br> - Tester `T1.6-EXEC-004-TST-01`: **PASS** — independent `.\mvnw.cmd clean verify` BUILD SUCCESS (41/0/0/0), `.\mvnw.cmd test` BUILD SUCCESS (41/0/0/0), `.\mvnw.cmd -DskipTests package` BUILD SUCCESS; development startup succeeded (`Started HumanItChallengeApplication in 4.711 seconds`, correct `dev` profile, H2 datasource, Flyway baseline applied to version 1, no secret leakage); fail-fast startup aborted before web server with `app.security.jwt.secret must not be blank`; no secret leaked; README accuracy and CHANGELOG coverage verified — tester-report.md Sections 7–11 <br> - Reviewer `T1.6-EXEC-006-REV-02`: **"REVIEW COMPLETE — PASS (reviewer recommendation)"**; 0 Critical, 0 Major, 0 Minor findings; all Phase 1 exit criteria met; documentation/report-only scope confirmed — reviewer-report.md Sections 8–11 |
| Minor findings | Zero Critical, zero Major and zero Minor findings across all three T1.6 reports (Developer §9; Tester §9; Reviewer §9). Informational observations Info-01…Info-05 are accepted as non-blocking and remain tracked |
| Cross-reference | Supersedes, for T1.6 specifically, Section 4's row "Any approval for tasks T1.2 onward — NOT AVAILABLE" (recorded when no T1.6 evidence existed; the row remains standing for tasks beyond Phase 1 per the append-only rule). Resolves the outstanding Phase 1 completion row in Section 4 ("Tech Lead approval of Phase 1 completion — NOT AVAILABLE") by recording Phase 1 completion. Closes the Reviewer's outstanding item (Tech Lead final acceptance of T1.6 pending) and the Tester's outstanding items. Deferred work — PostgreSQL integration, H2 Console/Swagger UI reachability, 409/401/403 handlers, problem-type URIs, bearer OpenAPI scheme, structured logging, global/object-level constraint violation mapping, `spring.jpa.open-in-view` hardening, stale `reports/task-status-report.md` — remains tracked for later phases. Proposals P-01 … P-04 remain PROPOSED and are unaffected |
| Effect | T1.6 is formally accepted and **closed**. Phase 1 is **COMPLETE**. 41/41 automated tests passed. Developer, Tester and Reviewer completed. No Critical or Major blockers. Approved documentation deliverables (`README.md`, `CHANGELOG.md`) recorded. Deferred work remains tracked. **Authorization to proceed to Phase 2 / T2.1** is granted. |

### D-10 — T2.1 ACCEPTED WITH DOCUMENTED MINOR FINDINGS

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly in the tasking for execution `T2.1-EXEC-009-ARCH-03`) |
| Scope | T2.1 — Domain model and persistence implementation, independent verification, and final review |
| Evidence | - Human Tech Lead explicitly approved T2.1 acceptance with documented Minor findings in the direct tasking for this execution. <br> - Tester `T2.1-EXEC-006-TST-02`: **PASS WITH FINDINGS**; report records 61 tests passed, 0 failures, 0 errors, 0 skipped, and confirms R-F01, R-F02 and R-F03 resolved (`agent-reports/T2.1/tester-report.md`, Sections 8–11). <br> - Reviewer `T2.1-EXEC-007-REV-02`: **PASS WITH FINDINGS**; R-F01, R-F02 and R-F03 resolved, with optional coverage and scope-control findings documented; no Critical or Major findings (`agent-reports/T2.1/reviewer-report.md`, Sections 9–12). <br> - Developer `T2.1-EXEC-005-DEV-02`: clean verification reported 61 tests passed, 0 failures, 0 errors, 0 skipped (`agent-reports/T2.1/developer-report.md`, Sections 7–11). |
| Minor findings / accepted limitations | Optional pagination boundary/sorting tests; optional same-transient-Client regression test; PostgreSQL validation deferred to T5.2; exclude `.opencode/agents/reviewer.md` from the implementation commit. |
| Cross-reference | Resolves the Tech Lead acceptance pending statements in the Developer, Tester and Reviewer reports for T2.1. The previous Architect execution `T2.1-EXEC-008-ARCH-02` was blocked and, per direct tasking, changed no files and did not record D-10; this entry is the first D-10 record. |
| Effect | T2.1 is **ACCEPTED WITH DOCUMENTED MINOR FINDINGS** and closed. R-F01, R-F02 and R-F03 are resolved. No outstanding Critical or Major findings are accepted as blockers. The accepted limitations above remain documented; `.opencode/agents/reviewer.md` must be excluded from the implementation commit. This decision does not authorize an unrelated implementation change or commit. |

**Updated 2026-11-09:** D-10 appended — T2.1 accepted with documented Minor findings by the human Tech Lead (see entry D-10).

### D-11 — Application-layer separation for Client and Document workflows

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval received directly via tasking in the current session) |
| Scope | Architecture for Client and Document functionality from T2.2 onward; does not reopen or alter accepted T2.1. |
| Approved rules | 1. Entities are persistence-focused JPA models: mappings, fields, relationships, constructors, getters/setters, and only minimal behavior required for persistence consistency. <br> 2. Entities must not contain application-service logic, repository orchestration, transaction boundaries, HTTP concerns, or DTO conversion methods. <br> 3. DTO-to-entity and entity-to-DTO transformations belong to dedicated mapper classes. <br> 4. Repositories own database access. <br> 5. Services own actual use cases, business rules, ownership enforcement, orchestration, and transaction boundaries. <br> 6. Do not introduce placeholder services or unnecessary pass-through abstractions. Introduce `ClientService` and `DocumentService` when their actual use cases require them. <br> 7. REST resources/controllers handle HTTP concerns and delegate application workflows to services. <br> 8. Preserve existing T2.1 persistence behavior, ownership invariants, optimistic locking, and tests. |
| Evidence | Direct human Tech Lead tasking in the current session (2026-11-09), which explicitly states these rules are approved and requests D-11 be recorded. |
| Effect | D-11 is the approved architecture constraint for upcoming Client/Document application work, including T2.2, T2.3 and T2.4. It does not change any approved REST contract or acceptance criterion and does not authorize refactoring or reopening T2.1. The requested implementation-plan and agent-instruction edits were not made in this Architect execution; see `agent-reports/T2.2/architect-proposal.md` for the role-scope limitation and handoff. |

### D-12 — D-11 architecture documentation review accepted

| Field | Value |
|---|---|
| Date | 2026-11-09 |
| Decided by | Human Tech Lead (explicit approval and F-01 acceptance received directly via tasking in the current session) |
| Scope | Formal closure of the D-11 architecture documentation review only. |
| Evidence | - Human Tech Lead tasking explicitly states that D-11 is approved and accepted with documented Minor finding F-01; it explicitly accepts F-01 as a documentation clarification only, not a reopening of T2.1. <br> - Reviewer `T2.2-EXEC-003-REV-01`: **PASS WITH FINDINGS**; 0 Critical, 0 Major, 1 Minor (`agent-reports/T2.2/reviewer-report.md`, Sections 9–11). <br> - Reviewer finding F-01 states the implementation-plan.md T2.1 Developer instruction was clarified after T2.1 acceptance; the report records that the wording does not change T2.1 implementation, acceptance criteria or behavior. <br> - `implementation-plan.md` records the D-11 boundaries and explicitly says the clarification does not reopen or require refactoring T2.1 (Phase 2 boundaries and T2.1 Developer instruction). |
| Finding disposition | F-01 is explicitly accepted by the Human Tech Lead. No corrective action is required for F-01. |
| Cross-reference | D-11 remains authoritative for future implementation. T2.1 remains accepted and closed under D-10; this decision does not reopen T2.1. |
| Effect | The D-11 architecture documentation review is accepted and closed with the documented Minor finding F-01. This decision authorizes documentation closure only; it does not authorize implementation work or Git mutations. |

### D-13 — T2.2 DTO, validation and mapper design decisions approved

| Field | Value |
|---|---|
| Date | 2026-11-10 |
| Decided by | Human Tech Lead (explicit approval of all five pending T2.2 design decisions received directly via tasking in the current session) |
| Scope | T2.2 — Request/response DTO contracts, Bean Validation and dedicated Client/Document mappers only. This decision formalizes the design approvals; it does not authorize implementation. |
| Evidence | Direct Human Tech Lead tasking explicitly approves all five decisions stated below, states that T2.1 is accepted and closed under D-10, confirms D-11/D-12 approval, and explicitly says no implementation is authorized by this task. Existing persistence lengths are evidenced by `Client.java`, `Document.java` and `src/main/resources/db/migration/V2__create_clients_and_documents.sql`. |
| Approved decisions | **1. PATCH representation:** Use a project-owned tri-state wrapper integrated with Jackson; distinguish `ABSENT` (property omitted), `PRESENT_NULL` (explicit JSON null) and `PRESENT_VALUE` (non-null value). Do not introduce an external JSON-nullable dependency. Omission preserves existing value; explicit null is rejected for required fields; explicit null clears optional Document `description`; Client PATCH `documents: null` is invalid. <br><br> **2. Response contract:** `ClientResponse` includes nested `DocumentResponse` entries. `DocumentResponse` includes scalar `clientId`, not a nested `Client`; avoid recursive DTO graphs. Future service/repository use cases intentionally load the collection needed for `ClientResponse`; mappers do not query repositories. <br><br> **3. Nested Client PUT:** Every supplied nested Document requires valid `number` and `expirationDate` for create or update. Existing nested Documents require ID and expected version; new nested Documents supply neither ID nor version. Ownership resolution, unknown-ID handling, version checking and aggregate merge orchestration belong to later service use cases. Omitted nested Documents are not deleted; omitted or empty `documents` preserves existing Documents; explicit `documents: null` is invalid. <br><br> **4. Client POST null collection:** `ClientCreateRequest documents: null` is treated as omitted; creating a Client without Documents is valid. This applies only to POST, not Client PUT/PATCH, where explicit `documents: null` is invalid. <br><br> **5. Validation:** Negative expected-version values are invalid requests. Apply Bean Validation `@Size` limits matching existing persistence column lengths: Client `firstName` 100, `lastName` 100, `taxIdentifier` 50, `email` 255, `phoneNumber` 50; Document `number` 100 and `description` 1000. Retain existing required-field, email-format and nullable-field rules. Add no country-specific tax/phone formats. Expired Documents remain valid. Do not modify database schema or entity constraints in T2.2. |
| Compatibility | Compatible with D-10: T2.1 remains accepted/closed; no reopening or change to its persistence behavior. Compatible with D-11: dedicated mappers transform DTOs/entities, mappers do not access repositories, and later services own use-case workflows. Compatible with D-12: the D-11 documentation review closure remains accepted and unchanged. D-13 adds the T2.2 design specifics without revising those decisions. |
| Effect | The five architecture questions in `agent-reports/T2.2/architect-proposal.md` are resolved and approved for T2.2 design. The T2.2 scope remains DTOs, validation and mappers; no endpoint/service/repository workflow, migration, schema or entity-constraint change is authorized by this decision-recording task. Implementation remains pending separate explicit Human Tech Lead authorization. |

---

### D-14 — Waiver of Major process findings T63-REV-01 and T63-REV-02 for final release

| Field | Value |
|---|---|
| Date | 2026-11-13 |
| Decided by | Human Tech Lead (explicit waiver received directly via tasking in the current session) |
| Scope | T6.3 — Final release readiness audit; governance/process findings T63-REV-01 (Tester report-order qualification) and T63-REV-02 (carried-forward T6.2 Tester report-order finding) |
| Evidence | - `agent-reports/T6.3/reviewer-report.md`, Sections 9, 10, 11 and 12 (findings T63-REV-01 and T63-REV-02 recorded as Major governance/process findings; no Critical or technical blocker identified) <br> - `agent-reports/T6.3/tester-report.md`, finding T63-TST-01 (disclosed early exposure to Architect report excerpts) <br> - `agent-reports/T6.2/tester-report.md` finding T62-TST-01 and `agent-reports/T6.2/reviewer-report.md` (carried-forward process finding) <br> - `agent-reports/T6.3/maven-clean-verify.log`: `BUILD SUCCESS`, exit code 0, 338 tests, 0 failures, 0 errors, 0 skipped (2026-11-13T16:08:51Z) <br> - Direct Human Tech Lead tasking of 2026-11-13 (this session) |
| Rationale | The deviations concern report-reading order, not application defects. Independent final Reviewer verification was completed. Final Maven `clean verify` passed with 338 tests, 0 failures, 0 errors, 0 skipped. No Critical or Major technical blockers were identified. Previous Docker/PostgreSQL/Flyway verification remains valid. No additional tests or implementation changes are required. |
| Effect | Findings T63-REV-01 and T63-REV-02 are waived for final release. The original findings in `agent-reports/T6.3/reviewer-report.md` and `agent-reports/T6.2/reviewer-report.md` / `tester-report.md` are preserved unchanged and are not erased or downgraded. This waiver does not invalidate the test results or the independent Reviewer audit; it records the Human Tech Lead's explicit disposition of two Major governance/process findings. No application code, test, configuration or other report is changed by this entry. |
| Cross-reference | `reports/approval-and-decision-log.md` (D-14, this session). Original findings preserved in `agent-reports/T6.3/reviewer-report.md` (T63-REV-01, T63-REV-02) and `agent-reports/T6.2/reviewer-report.md` / `tester-report.md`. |

---

### D-15 — T6.3 final release accepted by the Human Tech Lead

| Field | Value |
|---|---|
| Date | 2026-11-13 |
| Decided by | Human Tech Lead (explicit final release acceptance received directly via tasking in the current session) |
| Scope | T6.3 — Final release readiness audit; final delivery of the Human IT challenge application and documentation |
| Evidence | - `agent-reports/T6.3/reviewer-report.md` (execution `T6.3-REV-001`, 2026-11-13): independent final audit; 0 Critical findings; 2 Major governance/process findings (waived under D-14); 4 Minor findings; technical and configuration readiness verified; no technical blocker identified <br> - `agent-reports/T6.3/tester-report.md` (execution `T6.3-TST-001`, 2026-11-13): full-suite verification and configuration/delivery checks <br> - `agent-reports/T6.3/maven-clean-verify.log`: `BUILD SUCCESS`, exit code 0, 338 tests run, 0 failures, 0 errors, 0 skipped, finished 2026-11-13T16:08:51Z, Java 21.0.12.1, Spring Boot 3.5.16, Flyway V1/V2 applied on H2 <br> - `agent-reports/T6.3/architect-proposal.md` (execution `T6.3-ARCH-001`, 2026-11-13): final release-readiness audit proposal <br> - Prior scoped runtime evidence: `agent-reports/T5.2/tester-report.md` and `agent-reports/T5.3/tester-report.md` (Docker/PostgreSQL/Flyway) <br> - Git delivery evidence: `HEAD`, local `main` and `origin/main` at `ce997e7` (merge of T6.2); tracked worktree clean; T6.2 merged into `main` <br> - Direct Human Tech Lead tasking of 2026-11-13 (this session) authorizing the final documentation closeout and recording this acceptance |
| Decisions recorded | D-14 (waiver of T63-REV-01 and T63-REV-02) precedes this acceptance. |
| Effect | T6.3 is accepted and the Human IT challenge delivery is released. This records the Human Tech Lead's final acceptance decision, distinct from any agent recommendation (Architect/Tester/Reviewer reports recommend or condition acceptance; only the Human Tech Lead approves it). No further implementation, test, Maven, Docker or container verification is required by this decision. Documentation closeout performed under the authorized T6.3 task scope is recorded in `agent-reports/T6.3/developer-report.md`. |
| Cross-reference | `reports/approval-and-decision-log.md` (D-15, this session); `agent-reports/T6.3/reviewer-report.md`; `agent-reports/T6.3/tester-report.md`; `agent-reports/T6.3/maven-clean-verify.log`. |
