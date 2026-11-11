# Developer Report — T2.6 Versioning and Optimistic Concurrency

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2, T2.6): T2.6 — Concurrency control and version validation.

## 2. Execution identifier and date

- **VERIFIED (direct tasking/environment):** `T2.6-EXEC-002-DEV`.
- **VERIFIED (environment):** 2026-11-11.

## 3. Agent role and model

- **VERIFIED:** Developer role; model `opencode-go/gpt-6-luna` (`gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED (direct Human Tech Lead tasking):** Implement and test T2.6 against the approved Architect proposal.
- **VERIFIED (direct Human Tech Lead tasking):** Option A applies. Documents created during initial Client POST do not require `OPTIMISTIC_FORCE_INCREMENT`; initial Client version remains JPA-managed. Subsequent Document create/update/delete operations against an existing Client participate in parent optimistic concurrency through `OPTIMISTIC_FORCE_INCREMENT`. No expected-version input was added to Client POST.
- **VERIFIED (`implementation-plan.md`, T2.6/T2.7):** Scope includes version preconditions, aggregate Document force-increment, conflict mapping/rollback and concurrency verification. General T2.7 transaction redesign, unrelated work, Git commit/merge/rebase/push, and self-approval were excluded.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (T2.6 and T2.7); `requirements-decisions.md`; `agent-reports/decisions.md`; `agent-reports/T2.6/architect-proposal.md`.
- **VERIFIED:** T2.5 `architect-proposal.md`, `developer-report.md`, `tester-report.md`, and `reviewer-report.md`.
- **VERIFIED:** `ClientController.java`, `ClientService.java`, `DocumentService.java`, `ClientRepository.java`, `GlobalExceptionHandler.java`, `Client.java`, `Document.java`, request/response DTOs, and both mappers.
- **VERIFIED:** `ClientServiceTest.java`, `DocumentServiceTest.java`, `ClientControllerTest.java`, `DocumentControllerTest.java`, `ClientAggregateUpdateHttpIntegrationTest.java`, `DomainPersistenceTest.java`, and `CommitTimeOptimisticLockIntegrationTest.java`.
- **VERIFIED (Git pre-flight):** Current branch and status were checked. T2.5 implementation is present in current history (`c383fe2`, with later T2.5 report commits) and its aggregate regression tests are in the checked-out source.
- **VERIFIED (Git pre-flight):** The current branch is `feature/T2.5-aggregate-updates`, not `main`. No branch switch was attempted. Initial pending work included modified `agent-reports/decisions.md` and untracked `agent-reports/GOV-DECISIONS-RECONSTRUCTION-001/`, `agent-reports/GOV-DECISIONS-RECONSTRUCTION-002/`, and `agent-reports/T2.6/architect-proposal.md`; these were preserved and not edited.
- **VERIFIED:** No prior `agent-reports/T2.6/developer-report.md` or Developer history archive existed before this execution; no archive was required.

## 6. Files changed

- **VERIFIED — T2.6 production changes:**
  - `src/main/java/com/humanit/challenge/controller/ClientController.java`
  - `src/main/java/com/humanit/challenge/service/ClientService.java`
- **VERIFIED — T2.6 test changes:**
  - `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java`
  - `src/test/java/com/humanit/challenge/controller/DocumentControllerTest.java`
  - `src/test/java/com/humanit/challenge/controller/ClientAggregateUpdateHttpIntegrationTest.java`
  - `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`
  - `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`
  - `src/test/java/com/humanit/challenge/service/OptimisticConcurrencyIntegrationTest.java` (new)
- **VERIFIED — reporting change:** `agent-reports/T2.6/developer-report.md` (this report).
- **VERIFIED — excluded pre-existing work:** This execution did not change `agent-reports/decisions.md`, either `GOV-DECISIONS-RECONSTRUCTION-*` directory, or the pre-existing `agent-reports/T2.6/architect-proposal.md`. No DTO, entity, repository, mapper, exception-handler, migration, dependency, requirements or implementation-plan change was made.

## 7. Commands executed

- **VERIFIED:** `git branch --show-current`; `git status --short`; `git log --oneline -5`; `git diff --name-status`; `git diff --cached --name-status`; `git ls-files --others --exclude-standard` — inspected branch/history and pre-existing changes before implementation.
- **VERIFIED (intermediate exploratory commands):** Focused Maven runs exposed implementation issues during development. The initial non-clean run stopped on a missing static test assertion import; a later clean compile caught the unsupported `PatchValue.isPresent()` call; subsequent clean tests showed that aggregate force-increment was not being applied when the Client was first loaded without the repository lock, and that the HTTP response could carry the pre-commit version. These findings were corrected. An unquoted PowerShell `-Dspring.jpa.show-sql=true` attempt was parsed as an invalid Maven lifecycle phase; a quoted retry ran for diagnosis. These are not final test results.
- **VERIFIED (final focused command):** `.\mvnw.cmd -B clean "-Dtest=ClientServiceTest,DocumentServiceTest,ClientControllerTest,DocumentControllerTest,ClientAggregateUpdateHttpIntegrationTest,OptimisticConcurrencyIntegrationTest" test` — BUILD SUCCESS; 63 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED (required full-suite command):** `.\mvnw.cmd -B clean test` — BUILD SUCCESS; 308 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED:** `git status --short`; `git diff --check`; `git diff --stat`; `git diff --name-only`; `git diff --` on the T2.6 tracked implementation/test files — worktree and tracked diff were reviewed. Git emitted line-ending conversion notices; `git diff --check` reported no whitespace errors.
- **VERIFIED:** No commit, merge, rebase, push or branch switch was performed.

## 8. Evidence and observed results

### Production behavior

- **VERIFIED** (`ClientController.java`; `ClientService.java`; focused HTTP/service tests): Client DELETE now binds required `X-Client-Version`. Missing and malformed headers map to HTTP 400 Problem Details; negative versions are rejected by the service with 400; stale versions return 409 Problem Details; a matching current version returns 204. Tests assert invalid/stale requests leave both Client and associated Document rows present, valid deletion removes both, and deleting an unknown Client with a syntactically valid version preserves 404.
- **VERIFIED** (`ClientService.java`; `ClientRepository.java`; `ClientServiceTest.java`): For a non-empty Client PUT nested Document collection, ClientService loads through existing `lockById`; for PATCH it does so when a nested Document is created or an existing nested request supplies editable values. The lock is requested once per aggregate operation, before regular Client loading. Omitted/empty nested collections and PATCH entries containing only nested ID/version do not request force-increment. No `@Version` value is manually changed. Existing nested ID, owner and expected Document version checks remain in the merge workflow.
- **VERIFIED** (`DocumentService.java`; existing and extended Document service tests): Standalone Document service paths retain their existing `lockById` behavior. Added stale Client and stale Document delete assertions prove no Document is removed by a losing delete.
- **VERIFIED** (HTTP tests; `GlobalExceptionHandler.java` unchanged): Missing, malformed and negative applicable input versions are covered through controller/service tests; stale versions are covered through Client PUT/PATCH, Client DELETE, standalone Document operations, and aggregate nested Document behavior. Existing Spring Problem Details handling passed endpoint assertions; no exception-handler change was needed.

### Version contracts and Option A

- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.postWithInitialDocumentsNeedsNoExpectedClientVersionAndUsesJpaVersion`): Client POST with one initial Document and no expected Client version succeeds. The returned Client version matches the persisted JPA version, and the initial Document is persisted. The assertion does not require an exact initial numeric version.
- **VERIFIED** (`ClientService.create` and the same regression test): Initial Client creation does not call `ClientRepository.lockById`; Option A is respected and no explicit force-increment is requested solely for initial Documents.
- **VERIFIED** (existing DTO validation tests, controller tests and focused tests): Client PUT/PATCH require non-negative body Client versions; Document POST requires `X-Client-Version`; Document PUT/PATCH require body Document versions and `X-Client-Version`; Client DELETE requires `X-Client-Version`; Document DELETE requires both `X-Client-Version` and `X-Document-Version`. Tests cover missing, malformed, negative and stale inputs. Existing nested Document DTO tests cover ID plus version for existing entries and omission of both for new entries.

### Force-increment, concurrency, rollback and response version

- **VERIFIED** (`OptimisticConcurrencyIntegrationTest`, real H2/JPA transactions): Deterministically coordinated concurrent transactions with the same expected Client version and changes to different Documents under that Client produced exactly one successful writer and one optimistic conflict. Persistence reload confirmed the losing Document change was not committed and the parent Client version advanced.
- **VERIFIED** (`OptimisticConcurrencyIntegrationTest`): Same-Document concurrent updates and scalar-only Client concurrent updates also yielded exactly one winner and one optimistic conflict; persisted state contained only the winner's change.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.staleNestedDocumentVersionRollsBackEarlierAggregateChanges`): A real HTTP PUT attempted a valid first nested Document update and Client scalar change before encountering a stale version on a later nested Document. It returned HTTP 409 Problem Details. After clearing/reloading persistence, the earlier Client scalar and first Document values were unchanged, the second Document remained unchanged, and the attempted stale replacement number did not exist.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.nestedOnlyPutAndPatchReturnPersistedClientVersion`): Both nested-only PUT and PATCH responses carry a Client version greater than the preceding persisted version and equal the Client version reloaded after request transaction completion.
- **VERIFIED (observed provider behavior and fix):** The initial aggregate version test showed Hibernate defers `OPTIMISTIC_FORCE_INCREMENT` until transaction completion; a response mapped inside the service transaction was therefore stale. The controller now rereads Client after the service transaction returns for non-empty nested PUT/PATCH requests. HTTP tests confirm the returned version matches persistence. This adds a post-commit read but does not redesign `ClientResponse`, manually increment a version, or change the transaction boundary.
- **VERIFIED (focused/full Maven output):** The final focused suite passed 63/63 tests and the required full suite passed 308/308 tests.

## 9. Findings and severity

1. **VERIFIED — Resolved during implementation:** Client DELETE lacked the approved expected-version precondition; binding, service validation and stale checks are now in place and tested.
2. **VERIFIED — Resolved during implementation:** Aggregate nested Document updates did not previously request parent force-increment. Initializing the Client through `lockById` for nested mutation requests now supplies the force-increment within the existing transaction; real competing transactions verify conflict and rollback.
3. **VERIFIED — Resolved during implementation:** Hibernate's commit-time force-increment caused a stale HTTP response version. Post-transaction reread for non-empty nested aggregate requests produces the persisted version; PUT/PATCH HTTP regression tests assert it.
4. **VERIFIED:** No unresolved Critical or Major issue was observed in the implemented T2.6 scope. No T2.7 general atomicity claim is made.
5. **PENDING VERIFICATION:** Concurrency and persistence evidence uses H2; PostgreSQL integration remains outside this execution and is not claimed.

## 10. Decisions and reasoning

- **VERIFIED (approved proposal and direct Tech Lead authorization):** Reused the existing `ClientRepository.lockById` rather than adding locks, dependencies, repository APIs or manual version mutation. The lock is obtained as the Client is loaded when nested mutation input is present; this was necessary because a real concurrent test exposed that requesting it only after `findById` did not establish the expected aggregate concurrency in this provider.
- **VERIFIED (test evidence):** Omitted and empty collections, plus identity/version-only PATCH entries, take the ordinary load path and do not request a force-increment. Client scalar-only behavior therefore remains unchanged.
- **VERIFIED (observed Hibernate behavior and approved response contract):** The response correction is a controller-side reload after the proxied service transaction completes. A post-commit reread was selected over manual `@Version` editing, response DTO redesign, new transaction propagation or transaction-boundary redesign.
- **VERIFIED (direct Human Tech Lead Option A):** No force increment was added to Client POST with initial Documents. No expected-version field/header was added to Client POST.
- **VERIFIED — Deviation from the Architect proposal's expected file footprint:** `ClientController.java` gained post-transaction rereads for aggregate PUT/PATCH in addition to the required DELETE header. The change is justified by the observed response-version mismatch and is covered by persisted-version HTTP assertions. There were no requirements, DTO, schema, dependency or exception-handler deviations.

## 11. Final status

**VERIFIED — IMPLEMENTATION COMPLETE: YES.** Final focused tests: 63 passed, 0 failed. Final `.\mvnw.cmd -B clean test`: 308 passed, 0 failed, 0 errors, 0 skipped. Client DELETE, nested force-increment, concurrency, conflict rollback, approved version validation and response-version checks are implemented and covered. **Ready for independent Tester: YES.** This report is not Reviewer approval or Human Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Independent Tester verification and Reviewer audit remain to be performed. The Tester should verify independently before reading this Developer report; the Reviewer should audit independently before reading role reports.
2. **PENDING APPROVAL:** Human Tech Lead retains final acceptance authority. No self-approval or final acceptance is claimed.
3. **VERIFIED (scope boundary):** H2 was used for concurrency/persistence tests. PostgreSQL verification was not run; it remains outside T2.6 and is not represented as passed.
4. **VERIFIED (worktree preservation):** Pre-existing `agent-reports/decisions.md`, `GOV-DECISIONS-RECONSTRUCTION-001/`, `GOV-DECISIONS-RECONSTRUCTION-002/`, and the Architect proposal were left unchanged. The active branch remains `feature/T2.5-aggregate-updates`; no branch switch or Git mutation was performed.

## 13. Handoff to next agent

- **VERIFIED:** T2.6 implementation and final test commands completed; source/test changes are listed in Section 6.
- **PENDING VERIFICATION:** Independent Tester may now verify T2.6 and its evidence without first reading this report. Reviewer must perform independent audit before reading this or other role conclusions.
- **PENDING APPROVAL:** Only the Human Tech Lead may accept T2.6. No commit or push was made; stop after this report.
