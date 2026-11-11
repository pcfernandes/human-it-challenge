# Independent Tester Report — T2.7 Transactional Atomicity and Rollback

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2, T2.7): T2.7 — Transactional atomicity and deletion rules.

## 2. Execution identifier and date

- **VERIFIED** (direct tasking): `T2.7-EXEC-003-TEST`.
- **VERIFIED** (execution environment): 2026-11-11.

## 3. Agent role and model

- **VERIFIED** (runtime instructions): Tester / QA engineer.
- **VERIFIED** (runtime instructions): `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Independent verification only. Inspect implementation and tests before opening Architect/Developer reports; run the focused Maven tests and full clean suite; then compare those reports with observed evidence.
- **VERIFIED** (direct tasking): No application/test source, governance, or requirements edits; no branch switch, commit, push, merge, or self-approval. Stop after reporting.
- **VERIFIED** (`implementation-plan.md`, T2.5–T2.7; `requirements-decisions.md`, Sections 4–8 and 20): Verify transactional atomicity, explicit Document deletion and DELETE version behavior while retaining T2.5 identity/ownership and T2.6 optimistic locking behavior.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (T2.5–T2.7); `requirements-decisions.md` (approved transaction, deletion, ownership, and concurrency decisions).
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/DocumentService.java`; `src/main/java/com/humanit/challenge/domain/Client.java`; `src/main/java/com/humanit/challenge/repository/ClientRepository.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`; `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`; relevant T2.5 cases in `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`; `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java`.
- **VERIFIED (after independent test execution):** `agent-reports/T2.7/architect-proposal.md`; `agent-reports/T2.7/developer-report.md`; relevant status and scope context in `agent-reports/decisions.md`.
- **VERIFIED:** Maven/Surefire output from this execution, including `target/surefire-reports` result summaries.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.7/tester-report.md` — this report only.
- **VERIFIED** (initial `git status --short` and inspected diff, before this report): The working tree already contained modifications to `src/main/java/com/humanit/challenge/service/DocumentService.java`, the two inspected test files, `agent-reports/decisions.md`, and untracked report directories. These were present before this Tester execution's report creation. No application or test source was changed by this Tester execution.

## 7. Commands executed

- **VERIFIED** (PowerShell output): The literal requested focused command `.\mvnw.cmd -B -Dtest=DocumentServiceTest,ClientAggregateUpdateHttpIntegrationTest test` was first attempted. PowerShell rejected the unquoted comma (`Missing argument in parameter list`); Maven did not start.
- **VERIFIED** (Maven output): `.\mvnw.cmd -B "-Dtest=DocumentServiceTest,ClientAggregateUpdateHttpIntegrationTest" test` — quoting the comma-bearing property allowed the equivalent focused test selection to execute.
- **VERIFIED** (Maven output): `.\mvnw.cmd -B clean test` — full clean test suite.
- **VERIFIED** (terminal output): `git status --short; git diff --stat; git branch --show-current` — initial worktree inventory and branch observation. No branch switch or Git write operation was performed.
- **VERIFIED** (terminal output): `git diff -- src/main/java/com/humanit/challenge/service/DocumentService.java src/test/java/com/humanit/challenge/service/DocumentServiceTest.java src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java` — inspected the pre-existing source/test delta.
- **VERIFIED** (terminal output): `git status --short; git diff -- src/main/java/com/humanit/challenge/service/DocumentService.java` — post-suite worktree check; the source delta remained the same.

## 8. Evidence and observed results

### Requested focused scenarios

- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest`, focused Surefire output; 9 tests): Client POST with two nested Documents using the same globally unique number returned HTTP 409 Problem Details. After `EntityManager.clear()`, the test verified both the new Client tax identifier and duplicate Document number were absent. This establishes rollback of the Client and nested rows after the uniqueness failure.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest`, focused Surefire output): Client PATCH with a valid first nested Document change followed by a stale second Document version returned HTTP 409 Problem Details. After clearing/reloading, the test asserted Client scalar fields and version, both Documents' fields and versions, and both attempted Document numbers remained unchanged/absent.
- **VERIFIED** (`DocumentServiceTest`, focused Surefire output): Stale Client-version and stale Document-version DELETE attempts each produced conflict. After clear and fetch-join reload, the owning Client version was unchanged; both the target Document and sibling remained persisted and present in the Client collection.
- **VERIFIED** (`DocumentServiceTest`, focused Surefire output): Successful Document DELETE removed the target row, retained the sibling row, reloaded the Client collection containing exactly the sibling, and confirmed the Client version increased by one from its pre-delete version.
- **VERIFIED** (`DocumentService.java`, `Client.java`, `DocumentServiceTest`): The pre-existing source delta calls `client.removeDocument(document)` before the explicit `documentRepository.delete(document)`. `Client.removeDocument` only synchronizes the in-memory collection; the repository delete remains explicit. The passing success assertions establish that this synchronization did not cascade-delete the sibling and the target row was deleted as intended.
- **VERIFIED** (focused Maven output/Surefire reports): `ClientAggregateUpdateHttpIntegrationTest` ran 9 tests and `DocumentServiceTest` ran 13 tests; total 22, with 0 failures, 0 errors, and 0 skipped.

### T2.5/T2.6 regression verification

- **VERIFIED** (`ClientServiceTest.java` and full-suite Surefire report): Existing PUT/PATCH cases assert omitted/empty collection preservation, retained Document IDs, in-place nested update, and no transfer when associating a foreign Client's Document. Unknown nested IDs remain rejected. The class's 22 tests passed in the full run.
- **VERIFIED** (`OptimisticConcurrencyIntegrationTest.java` and full-suite Surefire report): Existing race tests cover competing Client scalar writers, competing writers of one Document, and competing aggregate writers on different Documents. They assert one winner/one conflict and winner-only persisted state. All 3 tests passed in the full run. `ClientRepository.lockById` remains configured with `OPTIMISTIC_FORCE_INCREMENT`.
- **VERIFIED** (full Maven output): `clean test` completed successfully: 310 tests, 0 failures, 0 errors, and 0 skipped. This includes the focused classes and the T2.5/T2.6 regression classes.
- **VERIFIED** (Maven output): Maven logged a non-fatal HTTP 401 while checking Flyway community repository metadata; it did not stop dependency resolution, compilation, or either successful test run.

### Production-change scope check

- **VERIFIED** (observed diff): The only production-source delta in the inspected T2.7 working-tree changes is in `DocumentService.delete`: keep the `Client` returned by `lockClient` and synchronize its in-memory collection using `client.removeDocument(document)` before the existing explicit repository deletion. No other production file appeared in the initial short status.
- **VERIFIED** (`architect-proposal.md`, Sections 9–10; `developer-report.md`, Sections 4 and 6): The Architect proposed this exact one-line relationship-synchronization change; the Developer report states the change was authorized by direct Tech Lead tasking and that transaction boundaries and explicit row deletion were retained.
- **NOT AVAILABLE** (`agent-reports/decisions.md` and inspected T2.7 reports): A raw, separately persisted Tech Lead authorization message for the Developer execution was not available for independent inspection. The Developer report is an attestation, not the raw tasking artifact. Therefore this Tester confirms the observed production diff matches the proposed/attested T2.7 scope, but cannot independently authenticate the prior authorization provenance from project artifacts.
- **VERIFIED** (this execution's initial and subsequent status/diff observations): This Tester made no production or test source edits.

### Comparison with prior role reports (opened only after independent verification)

- **VERIFIED** (comparison of Architect report Sections 69–85 against inspected tests and current run): The Architect identified missing direct coverage for duplicate nested POST rollback, late-failure PATCH rollback, and complete failed Document DELETE state. The current added tests cover these gaps, and observed assertions/results match those scenarios. No substantive discrepancy found on these claims.
- **VERIFIED** (comparison of Architect proposal Sections 91–104 against source and tests): The implemented relationship helper call is paired with explicit row deletion, not used as a deletion substitute; the new tests and current behavior align with the proposal's cautions.
- **VERIFIED** (comparison of Developer report Sections 42–57 against independent results): Developer claims of 22/22 focused and 310/310 full-suite passing match this execution's test counts and outcomes. The claimed post-clear persisted-state assertions are present in the inspected test sources.
- **PENDING VERIFICATION** (Developer report Section 42): The Developer report describes an earlier focused attempt with two lazy-loading errors and a subsequent test-helper adjustment. That intermediate run was not part of this Tester execution; the final fetched-collection tests were inspected and passed independently.
- **VERIFIED** (report and initial status comparison): The Developer report lists the same three intended application/test source files as pre-existing changes in this execution's initial status. No additional production-source delta was observed. The missing raw authorization evidence noted above remains an authorization-provenance limit, not a test-result discrepancy.

## 9. Findings and severity

1. **No Critical or Major technical findings observed.** **VERIFIED** (focused and full Maven output plus persisted-state assertions): The requested rollback/deletion cases passed; T2.5 identity/ownership and T2.6 optimistic-lock regression suites passed.
2. **LOW — Authorization provenance limitation.** **VERIFIED** (observed diff and Developer report) the sole production delta matches the Architect's proposed scope and Developer's stated authorized scope. **NOT AVAILABLE** is the raw direct Tech Lead tasking as a separate artifact, so independent provenance confirmation is incomplete. No unrelated or additional production change was observed, and this Tester did not make source changes.
3. **INFO — Focused command quoting.** **VERIFIED** PowerShell rejected the unquoted comma in the literal requested command before Maven ran; the quoted equivalent ran the same two test classes successfully.
4. **INFO — Non-fatal repository metadata warning.** **VERIFIED** a Flyway repository metadata HTTP 401 appeared in the Maven output, while both requested build/test runs still completed successfully.

## 10. Decisions and reasoning

- **VERIFIED:** Behavior was checked using the requested service and HTTP integration tests, then the complete clean suite. Persisted state was reloaded after clearing the persistence context in the rollback/delete scenarios, avoiding reliance only on in-memory entity state.
- **VERIFIED:** The delete assertions test both the explicit target deletion and sibling retention, and separately verify parent Client version behavior. This is direct observable evidence against unintended cascade deletion from inverse-collection synchronization.
- **VERIFIED:** T2.5 and T2.6 behavior was considered through the relevant regression test sources and their full-suite execution, rather than inferring preservation only from a passing T2.7 test.
- **VERIFIED:** No test or production code correction was made by the Tester. The initial PowerShell syntax rejection was handled by quoting the `-Dtest` argument; the selected classes were unchanged.
- **PENDING VERIFICATION:** The Tech Lead or Reviewer may independently resolve the raw authorization-provenance gap. This report does not treat the Developer's attestation as the Tech Lead's final acceptance.

## 11. Final status

**PASS — INDEPENDENT TECHNICAL VERIFICATION RECOMMENDATION.** **VERIFIED** focused suite: 22/22 passed; **VERIFIED** full suite: 310/310 passed. The seven requested behavior checks (items 1–7) passed based on observed test evidence and source/test inspection. For item 8, no production/test source change was made by this Tester; one pre-existing production delta matching the Architect proposal and Developer's authorization attestation was observed, but its raw authorization provenance remains **PENDING VERIFICATION**. This recommendation is not Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Obtain/confirm the raw Tech Lead authorization provenance for the pre-existing `DocumentService.java` change if an independent artifact is required; the Developer report attests it was directly authorized.
2. **PENDING APPROVAL:** Reviewer audit and Human Tech Lead final acceptance remain separate workflow gates. This Tester report does not grant either.
3. **VERIFIED:** Focused suite (22 tests) and full clean suite (310 tests) passed with no failures, errors, or skips. No further test failure remains outstanding from these runs.

## 13. Handoff to next agent

- **VERIFIED:** Reviewer may audit this independent verification execution `T2.7-EXEC-003-TEST`, its source/test evidence, and the low-severity authorization-provenance limitation.
- **PENDING APPROVAL:** Human Tech Lead alone decides final T2.7 acceptance after the required review workflow; no approval or acceptance is claimed here.
- **VERIFIED:** Stop after this report. No application/test source changes, branch switch, commit, push, or merge were made by this Tester execution.
