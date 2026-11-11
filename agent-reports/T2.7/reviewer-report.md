# Independent Reviewer Report — T2.7 Transactional Atomicity and Rollback

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 2, T2.7): T2.7 — transactional atomicity and deletion rules.

## 2. Execution identifier and date

- **VERIFIED** (direct tasking): `T2.7-EXEC-004-REV`.
- **VERIFIED** (execution environment): 2026-11-11.

## 3. Agent role and model

- **VERIFIED** (runtime instructions): Senior Java and Spring Boot Reviewer.
- **VERIFIED** (runtime instructions): `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Independently review T2.7 source/test changes, transaction and rollback behavior, DELETE semantics, T2.5/T2.6 preservation, and unrelated-change scope; report only and stop.
- **VERIFIED** (direct tasking): The Tech Lead authorized the minimal T2.7 changes in this conversation. A separately persisted authorization artifact is not a blocker. No source/test/governance/requirements edits, Maven execution, Git mutation, branch switch, commit, push, merge, or self-approval is authorized.
- **VERIFIED** (`implementation-plan.md`, T2.7; `requirements-decisions.md`, Sections 5, 7, 8 and 20): Client/Document operations must be transactional and atomic; Document DELETE requires both version headers, stale versions conflict, and deleting a Document removes only that Document while incrementing the parent Client version in the same transaction.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (T2.7); `requirements-decisions.md` (deletion, transaction, concurrency and ownership decisions).
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/DocumentService.java`; `src/main/java/com/humanit/challenge/service/ClientService.java`; `src/main/java/com/humanit/challenge/domain/Client.java`; `src/main/java/com/humanit/challenge/domain/Document.java`; `src/main/java/com/humanit/challenge/repository/ClientRepository.java`; `src/main/java/com/humanit/challenge/repository/DocumentRepository.java`; `src/main/java/com/humanit/challenge/controller/DocumentController.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`; `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`; `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`; `src/test/java/com/humanit/challenge/controller/DocumentControllerTest.java`; `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`; `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`.
- **VERIFIED (after independent source/test inspection):** `agent-reports/T2.7/architect-proposal.md`; `agent-reports/T2.7/developer-report.md`; `agent-reports/T2.7/tester-report.md`.
- **NOT AVAILABLE:** Local `target/surefire-reports` artifacts were not found by the targeted path lookup; this review did not run tests or Maven.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.7/reviewer-report.md` — this execution's report only. The primary Reviewer report and Reviewer history files did not exist at audit start, so no prior report needed archival.
- **VERIFIED:** No production source, test source, configuration, planning, requirements, decision-log, or other agent-report file was changed by this Reviewer execution.

## 7. Commands executed

- **VERIFIED** (terminal output): Read-only Git inspection: `git status --short --untracked-files=all`; `git diff --stat`; `git diff -- src/main src/test`; `git diff --name-only`; `git diff --numstat -- src/main src/test`; and `git diff --check -- src/main src/test`.
- **VERIFIED** (tool use): Read-only file reads, targeted source/test searches and path globs were used. No Maven command was run.
- **INFORMATIONAL process note:** An initial broad `grep` for `T2.7` searched the workspace and returned excerpts from role reports before the source/test audit. The audit then inspected the implementation and test diffs before opening the three requested reports, but this accidental early excerpt exposure is a deviation from the requested strict independence/search discipline and is disclosed in Section 9.

## 8. Evidence and observed results

### Change scope and implementation

- **VERIFIED** (`git status` and `git diff -- src/main`): The only modified production source is `src/main/java/com/humanit/challenge/service/DocumentService.java`. Its diff is limited to retaining the `Client` returned by `lockClient` and adding `client.removeDocument(document)` immediately before the existing `documentRepository.delete(document)`; the other production diff is no change.
- **VERIFIED** (`DocumentService.java:100-112`): `delete` remains `@Transactional`; client and document versions are checked before mutation; collection synchronization and explicit row deletion happen in the same transaction; both repository flushes remain; persistence/optimistic conflicts are translated to unchecked `ResponseStatusException` conflicts.
- **VERIFIED** (`Client.java:45-48, 130-138`; `Document.java:38-45`): `Client.documents` is the inverse `mappedBy="client"` collection and has REMOVE cascade but no `orphanRemoval=true`; `Document.client` is the required owning-side foreign key and is non-updatable. The helper removes only from the in-memory collection and does not clear the owning side or delete the row. Pairing it with explicit repository deletion is therefore necessary and appropriate; no cascade mapping change was made.
- **VERIFIED** (`ClientRepository.java:20-22`; `DocumentService.java:115-128`): `lockClient` obtains the Client through `OPTIMISTIC_FORCE_INCREMENT` and checks the expected parent version. The Document DELETE path preserves this behavior and does not manually modify the version.
- **VERIFIED** (`git status --short --untracked-files=all`; source/test diffs): The changed application/test files are exactly the single production service and the two test classes listed below; no unrelated production or test-source change appears in the inspected diff. The working tree also contains a modified `agent-reports/decisions.md` and untracked governance/T2.7 role-report artifacts; they are outside the source/test diff and were not modified by this Reviewer. **NOT AVAILABLE:** This review cannot independently establish the provenance of those other worktree artifacts beyond the current status and role reports.

### Requested transactional and test audit

- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.java:120-144`): The POST rollback test sends a Client with two nested Documents sharing a globally unique number, expects HTTP 409 Problem Details, clears the persistence context, and asserts neither the Client tax identifier nor the duplicate Document number exists. **VERIFIED** (`ClientService.java:38-53`): Client creation is `@Transactional`, flushes the aggregate, and translates integrity failure to an unchecked conflict; the failed service call therefore exits the transaction exceptionally.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.java:256-306`): The PATCH late-failure test attempts a Client scalar change and an earlier nested Document change before supplying a stale version for the later nested Document. It expects 409, clears the persistence context, reloads and compares Client scalars/version and both Documents' fields/versions, and checks that neither attempted number exists. **VERIFIED** (`ClientService.java:88-112, 195-220`): PATCH mutations are within `@Transactional`; stale nested version failure propagates from `applyPatchDocuments` as an unchecked conflict, so the managed earlier changes roll back rather than partially commit.
- **VERIFIED** (`DocumentServiceTest.java:187-206, 257-281`): Successful Document DELETE asserts the target row is absent, the sibling remains, and after clearing/reloading the Client collection contains exactly the sibling; the Client version is asserted to increase by one. Failed stale Client- and Document-version DELETE attempts are followed by a fresh Client/collection load and assertions that the parent version is unchanged and both target/sibling remain. These checks cover explicit row deletion, inverse-collection consistency, parent version behavior, and rollback/no partial deletion.
- **VERIFIED** (`DocumentController.java:73-80`; `DocumentControllerTest.java:118-126, 150-191`): HTTP DELETE requires both version headers, returns 204 on success and passes both values to the service; controller tests cover missing/malformed values. The service integration tests cover stale-version conflicts. **VERIFIED** (direct task scope): A separate full HTTP-to-persistence negative-header test was expressly excluded from authorized changes; no T2.7 requirement gap is inferred from that exclusion because controller and service boundaries are covered.
- **VERIFIED** (`ClientServiceTest.java:241-316, 418-455`; `DomainPersistenceTest.java:212-245, 330-369`): Existing T2.5 regression tests assert omitted/empty Client collections preserve existing Document identities, updates retain IDs, and ownership transfer is rejected; persistence tests establish collection removal alone does not delete or transfer a row. No affected T2.5 implementation was changed.
- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java:69-135`; `ClientRepository.java:20-22`): Existing T2.6 integration tests cover competing Client and Document writers and winner-only persistence; the parent force-increment lock remains present. No T2.6 implementation or regression test was changed. **NOT AVAILABLE:** This Reviewer did not rerun these tests.

### Role-report comparison and verification limits

- **VERIFIED** (comparison of inspected diffs with `architect-proposal.md`, `developer-report.md`, and `tester-report.md`): The production edit and three added/extended scenarios match the Architect's narrow proposed scope and the Developer's description; no conflicting implementation claim was identified. The explicit repository delete remains, consistent with the Architect's warning that collection removal alone is not row deletion.
- **VERIFIED** (`tester-report.md`, Sections 8 and 11): Tester reports focused 22/22 and full-suite 310/310 passing. **NOT AVAILABLE** (local Surefire artifacts absent; no Maven run permitted): This Reviewer did not independently reproduce those test results, so the counts are attributed to the Tester report, not represented as this review's test execution.
- **VERIFIED** (direct tasking): The Tester report's qualification that a separately persisted raw authorization artifact was unavailable does not block this review: the current Tech Lead task directly authorizes the minimal T2.7 changes and explicitly resolves that concern.

## 9. Findings and severity

1. **Informational — Independence/search-process deviation.** **VERIFIED** (this execution's tool history): The initial broad `grep` returned role-report excerpts before source/test inspection, contrary to the instruction to avoid broad searches exposing those reports. The complete source/test diff inspection was performed before opening the Architect, Developer and Tester report files, and conclusions above are supported by directly inspected code/tests; nevertheless, the early excerpts are a process limitation and strict report-blind independence cannot be claimed.
2. **No Critical, Major, or Minor code findings observed.** **VERIFIED** (direct source/test inspection and diffs): The one production change is narrowly scoped, synchronizes the inverse collection while retaining explicit repository deletion and transactional optimistic locking, and the added tests assert persisted rollback/delete state. This is a static audit conclusion, not a claim that the tests were rerun by this Reviewer.

## 10. Decisions and reasoning

- **VERIFIED:** Retaining the existing service transaction and force-increment lock is correct: `client.removeDocument(document)` only synchronizes the inverse collection; the explicit repository delete removes only the selected row; exception propagation remains within the existing service transaction.
- **VERIFIED:** No change to T2.5 identity/ownership rules or T2.6 optimistic locking was observed. Existing regression source remains present and unmodified.
- **VERIFIED:** The Tester-reported counts are acknowledged as reported evidence but not as Reviewer-executed results; Maven execution was prohibited, and the targeted local Surefire report path contained no files.
- **VERIFIED:** No Tech Lead acceptance is recorded or implied. The direct tasking authorizes the scope but is not final acceptance.

## 11. Final status

**PASS WITH FINDINGS.** **VERIFIED** by source/test diff inspection: no Critical, Major, or Minor implementation defect was identified; the requested transaction, collection, explicit deletion, rollback-test, T2.5 and T2.6 checks are supported by inspected code and assertions. The recommendation carries one **Informational** finding for accidental early report-excerpt exposure during broad search. Tester-reported focused/full-suite counts are not independently rerun by this Reviewer. This is a Reviewer recommendation only, not Human Tech Lead acceptance.

## 12. Outstanding issues

1. **VERIFIED:** The informational independence/search-process deviation is disclosed above; no code correction is indicated by this audit.
2. **NOT AVAILABLE:** Reviewer-side test execution evidence; tests were not run, and targeted local Surefire report files were not found. Tester-reported results remain attributed to the Tester report.
3. **PENDING APPROVAL:** Human Tech Lead final acceptance remains required. No approval or acceptance is claimed.

## 13. Handoff to next agent

- **VERIFIED:** The Reviewer recommends **PASS WITH FINDINGS** to the Human Tech Lead, with no Critical/Major/Minor code findings and one Informational process finding.
- **PENDING APPROVAL:** The Human Tech Lead alone decides final T2.7 acceptance. The Reviewer stops after reporting; no source/test correction, Maven execution, Git mutation, or self-approval was performed.
