# Evidence Gap Register — Human IT Challenge

**Report type:** Reporting infrastructure (authorized)
**Generated:** 2026-11-09 (Europe/Lisbon)
**Author role:** Developer (reporting only — no application code or existing project documents modified)
**Method:** Evidence-only. Gaps were identified by searching the project workspace only (no external or OpenCode-internal sources). Each gap states its impact and the authorized activity required to close it. Remediation items are **recommendations only**; nothing has been executed or changed.

---

## 1. Purpose

Single registry of all evidence gaps affecting the traceability of the approved plan (`implementation-plan.md`). The task-status board (`task-status-report.md`) references these gap IDs.

## 2. Gap register

| ID | Gap | Affects | Status | Impact | Closing action (recommended, not executed) |
|---|---|---|---|---|---|
| G-01 | **Autonomous application termination is not proven.** Surefire reports support build/test results, but the T1.1 acceptance criterion "Application starts locally" and any termination expectation require standalone startup/shutdown evidence (console transcript, shutdown log, exit code) that does not exist in the project | T1.1 | **PENDING EVIDENCE** | T1.1 cannot be declared fully verified | Authorized verification run: start the app standalone (`.\mvnw.cmd spring-boot:run` or `java -jar`), capture startup + shutdown output and exit code; record in `build-and-test-evidence-report.md` |
| G-02 | No Developer task summary for T1.1 | T1.1 | **NOT AVAILABLE** | Implementation assumptions/deviations for T1.1 are unrecorded | Developer produces the T1.1 task summary |
| G-03 | No Tester verification record for T1.1 | T1.1 | **NOT AVAILABLE** | Test results exist as raw Surefire artifacts but no Tester verification/coverage statement exists | Tester records verification against T1.1 acceptance criteria |
| G-04 | No Reviewer audit for T1.1 | T1.1 | **NOT AVAILABLE** | Governance requires an independent audit before approval | Reviewer performs and records audit A-01 (`reviewer-audit-report.md`) |
| G-05 | No Tech Lead approval records for the T1.1 task scope, implementation completion, or Phase 1 | T1.1 / Phase 1 | **NOT AVAILABLE** | T1.1 cannot be marked complete under governance rules | Tech Lead records approvals in `approval-and-decision-log.md` |
| G-06 | **No version-control history.** The workspace is not a git repository; there is no commit history, so no historical traceability of who/what/when for any change | All tasks | **NOT AVAILABLE** | Cannot reconstruct historical task execution from VCS | Initialize version control if authorized; going forward, commit per approved task |
| G-07 | **Stale status statement in `requirements-decisions.md`** ("Application implementation has not started") that conflicts with existing T1.1 artifacts and the 2026-11-09 test run | Documentation accuracy | Observed; document **not modified** per task constraints | Risk of confusing traceability | Tech Lead authorizes a one-line status update to `requirements-decisions.md` |
| G-08 | No README/run instructions; T1.6 documentation checkpoint not started | T1.6 | **NO EVIDENCE OF ACTIVITY** | "Instructions to run the project" deliverable (per `requirements.md`) is unmet | Execute T1.6 when approved |
| G-09 | Approval dates for the requirements decisions and implementation plan are not recorded anywhere in the project | Governance | **NOT AVAILABLE** | Approval chronology cannot be established | Record dates in `approval-and-decision-log.md` if known to the Tech Lead |
| G-10 | No phase/task execution records (summaries, meeting notes, approval artifacts) exist for anything beyond T1.1's raw artifacts | Phases 1–6 | **NOT AVAILABLE** | Historical agent work cannot be audited or reconstructed | Maintain the five reporting files from now on as the single record |

## 3. Explicit non-claims

- No historical agent conclusion, review finding, or approval has been inferred or reconstructed in any of the five reporting files.
- The Surefire reports prove only what is stated in `build-and-test-evidence-report.md`; no additional performance, security or correctness claims are made.
- Absence of evidence inside the workspace is reported as such; nothing outside the workspace was consulted.

## 4. Maintenance rule

Every new authorized task should either close a gap ID above or add a new one, keeping this register the definitive map of what is proven versus pending.

---

**Status of this register:** Initialized on 2026-11-09 with G-01 … G-10; all remain open.
