# Agent Reports — Human IT Challenge

**Created:** 2026-11-09 (Europe/Lisbon)
**Created by:** Developer role (reporting only), under a documentation-only task authorized by the Tech Lead
**Method:** Evidence-only. No application code, `pom.xml`, Maven Wrapper, requirements, implementation plan, existing `reports/*.md` files or agent definitions were modified. No historical agent conclusions, commands, test outputs or Tech Lead approvals have been invented.

---

## 1. Purpose

`agent-reports/` is the per-task, per-agent reporting infrastructure for the approved development workflow. Each task directory holds one report per agent role, so that every task has a traceable record of what was proposed, implemented, verified and audited.

This directory complements, and does not replace, the project-level files in `reports/`:

| Directory | Level | Contents |
|---|---|---|
| `reports/` (existing) | Project-wide | Task status board, build/test evidence, Reviewer audit registry, approval log, evidence-gap register |
| `agent-reports/` (this directory) | Per task, per role | Architect proposals, Developer reports, Tester reports, Reviewer reports and the Tech Lead decision log |

Both locations follow the same evidence-only method: every statement is backed by an artifact inside the project, and unavailable evidence is marked explicitly instead of being invented.

## 2. Directory layout

```text
agent-reports/
  README.md                          <- this file
  decisions.md                       <- append-only Tech Lead decision log
  T1.1/
    architect-proposal.md            <- primary Architect report (latest execution)
    developer-report.md              <- primary Developer report (latest execution)
    tester-report.md                 <- primary Tester report (latest execution)
    reviewer-report.md               <- primary Reviewer report (created only when an audit is performed)
    history/                         <- archived previous executions (created on first replacement)
  T1.2/
    ...
```

A `reviewer-report.md` exists only after the Reviewer role has actually performed an audit. It must not be pre-created or reconstructed.

## 3. Reporting workflow

The workflow is defined in `implementation-plan.md` (Section 2, Governance and Approval Rules) and `requirements-decisions.md` (Section 18). The reporting duties of each role are:

### Architect

- Proposes the architecture and implementation approach for the current task.
- Records alternatives, trade-offs and assumptions.
- Waits for the human Tech Lead's approval before any implementation starts.

### Developer

- Implements only the approved scope.
- Records files changed, commands executed and their results.
- Identifies incomplete work and known limitations.

### Tester

- Independently examines the requirements and the implementation.
- Runs verification **before** reading the Developer's conclusions.
- Compares results with the Developer report afterward.
- Records evidence, failures and disagreements.

### Reviewer

- Independently audits the code and architecture **first**.
- Only then reads the Developer and Tester reports.
- Challenges unsupported claims and identifies risks.
- Never modifies application code.

### Tech Lead

- Approves or rejects proposals.
- Authorizes implementation and corrections.
- Makes final acceptance decisions.

## 4. Mandatory report format

Every agent report must include the following sections, in this order:

1. **Task ID**
2. **Execution identifier and date**
3. **Agent role and model**
4. **Authorized scope**
5. **Files inspected**
6. **Files changed**
7. **Commands executed**
8. **Evidence and observed results**
9. **Findings and severity**
10. **Decisions and reasoning** (for Architect proposals this includes alternatives, trade-offs and assumptions)
11. **Final status**
12. **Outstanding issues**
13. **Handoff to next agent**

A report may add sections, but may not omit any of the thirteen above. If a section has no content (for example, "Files changed" in a review report), it states `None` with a one-line justification.

## 5. Evidence markers

Every material statement in a report must be classifiable into exactly one of:

| Marker | Meaning |
|---|---|
| **VERIFIED** | Directly supported by an artifact inspected inside the project (file content, timestamp, report output). The supporting artifact is named. |
| **RECONSTRUCTED** | Inferred from in-project artifacts because the original record is missing. The inference and its basis are stated. Reconstruction never extends to inventing agent conclusions, commands, test outputs or approvals. |
| **NOT AVAILABLE** | Searched for inside the project and not found. |
| **PENDING VERIFICATION** | Requires a future, authorized verification activity to establish. |

Special rule for T1.1: the existing Maven Surefire reports support the `contextLoads` test result, but **do not prove autonomous application termination**. Autonomous termination is therefore recorded as **PENDING VERIFICATION** (see `reports/build-and-test-evidence-report.md`, Section 4, and gap G-01 in `reports/evidence-gap-register.md`).

## 6. Execution history convention

Multiple executions of the same task by the same role must be preserved, never overwritten silently.

Convention:

- The **primary report** (for example `agent-reports/T1.2/developer-report.md`) always represents the **latest** execution.
- Before replacing a primary report, the existing file **must** be copied unchanged into the task's `history/` subdirectory with the role name and a zero-padded, monotonically increasing execution number:

  ```text
  agent-reports/T1.2/developer-report.md          <- latest execution (e.g. EXEC-003)
  agent-reports/T1.2/history/developer-001.md     <- archived execution 1
  agent-reports/T1.2/history/developer-002.md     <- archived execution 2
  ```

- Each archived copy retains its original execution identifier, date and content.
- **Never silently overwrite an existing report.** Replacing a primary report without an archival copy is a violation of this convention.
- The Reviewer's audit always refers to a specific execution identifier, so findings remain traceable even after later executions supersede the primary report.

## 7. Decision log

`decisions.md` is an **append-only, chronological Tech Lead decision log**:

- Entries are numbered sequentially and are never deleted or rewritten; corrections are made by appending a new entry that supersedes an earlier one.
- **Proposed** decisions and **approved** decisions are distinguished. A proposal only becomes approved when the Tech Lead records the approval.
- **No approval is recorded unless evidence exists** inside the project or in the Tech Lead's direct tasking.
- The historical log started in `reports/approval-and-decision-log.md` remains unmodified; `decisions.md` continues the chronology and cross-references it.

## 8. Governance rules

- No task begins without Tech Lead approval; no phase completes without its own tests, an independent Reviewer audit and Tech Lead approval (`implementation-plan.md`, Section 2).
- Reviewer findings of Critical or Major severity must be resolved before approval.
- The Tester verifies independently before reading the Developer report; the Reviewer audits independently before reading both.
- Agents must not implement unapproved features or make unrelated changes.
- Application source code, `pom.xml`, the Maven Wrapper, existing requirements and planning documents, existing `reports/*.md` files, existing agent definitions and Git configuration are not modified by reporting work.

## 9. Current state (2026-11-09)

| Item | Status |
|---|---|
| T1.1 Architect proposal | Reconstructed from bootstrap artifacts; awaiting Tech Lead ratification |
| T1.1 Developer report | Reconstructed from bootstrap artifacts; historical record NOT AVAILABLE |
| T1.1 Tester report | Reconstructed from Surefire evidence; standalone start and autonomous termination PENDING VERIFICATION |
| T1.1 Reviewer report | Not created — requires an actual Reviewer audit first |
| T1.1 completion approval | NOT AVAILABLE — not recorded anywhere in the project |
| T1.2 onwards | No evidence of activity |

Open evidence gaps are tracked in `reports/evidence-gap-register.md` (G-01 … G-10).

---

**Status of this file:** Initialized on 2026-11-09. Maintain per the conventions above.
