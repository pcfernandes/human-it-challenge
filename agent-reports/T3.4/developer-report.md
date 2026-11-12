# Developer Report — T3.4 Phase 3 Contract Regression

## 1. Task ID

- **VERIFIED** (direct tasking): `T3.4` — Phase 3 contract regression.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T3.4-DEV-001`, 2026-11-12.
- **VERIFIED** (`agent-reports/README.md`, Section 6; repository search before writing): No earlier `agent-reports/T3.4/developer-report.md` existed, so no prior report required archival.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Developer; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking and `implementation-plan.md`, Sections 7/T3.4 and Phase 3 exit criteria): Inspect existing Phase 2 and security tests; add/adjust only tests needed to establish Client/Document CRUD behavior with JWT, 401 responses for missing/invalid/expired tokens, preserve existing safe 403 behavior where applicable, and preserve existing REST responses. No production refactoring/features, T3.2 credential changes, default credentials, commit/push/PR/merge, or T3.4 completion approval were authorized.
- **VERIFIED** (direct tasking): Work from `feature/T3.4-security-regression` based on updated `origin/main`, preserve unrelated local changes, and stop if branch creation is unsafe.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`, `implementation-plan.md` (T3.4 and Phase 3 exit criteria), `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java`, `DocumentControllerTest.java`, `ClientAggregateUpdateHttpIntegrationTest.java`, `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java`, `SecurityConfigurationIntegrationTest.java`, and `SecurityProblemHandlersTest.java`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/ClientController.java` and `DocumentController.java`; source test inventory was searched for MockMvc/security token usage.
- **VERIFIED**: Initial Git status, branch/log state, `origin/main` ancestry/merge evidence, and the pre-existing worktree diff were inspected before branch creation.

## 6. Files changed

- **VERIFIED**: `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java` — added one authenticated end-to-end Client and Document CRUD regression test.
- **VERIFIED**: `src/test/java/com/humanit/challenge/security/SecurityConfigurationIntegrationTest.java` — moved invalid-signature and expired-token checks to the nested Document route and added an unauthenticated nested Document route assertion.
- **VERIFIED**: `agent-reports/T3.4/developer-report.md` — this execution report.
- **VERIFIED**: No production source, configuration, dependency, T3.2 credential provisioning, or default-credential files were changed.

## 7. Commands executed

- **VERIFIED**: Inspected `git status --short --branch`, current branch, recent log, local diff, and candidate branch existence. Fetched remote refs with `git fetch origin`.
- **VERIFIED**: Checked `origin/main` with `git show`, compared it with `HEAD`, and verified `f934cb8` (T3.3) is an ancestor of `origin/main` using `git merge-base --is-ancestor f934cb8 origin/main`.
- **VERIFIED**: Because pre-existing local work included a modified T3.3 report that differed from the updated base, preserved the entire tracked/untracked worktree in `git stash push --include-untracked -m "T3.4 preserve pre-existing work"`, then created/switched to `feature/T3.4-security-regression` from `origin/main` using `git switch -c feature/T3.4-security-regression origin/main`.
- **VERIFIED**: `.\mvnw.cmd test "-Dtest=AuthControllerIntegrationTest,SecurityConfigurationIntegrationTest"` — focused security tests.
- **VERIFIED**: `.\mvnw.cmd test` — full Maven suite, run once after the changes.
- **VERIFIED**: Checked final `git status --short --branch`, `git rev-parse HEAD`, `git rev-parse origin/main`, `git diff --check`, diff summary/file list, stash list, and confirmed no existing T3.4 Developer report before creating this one.

## 8. Evidence and observed results

- **VERIFIED** (`git fetch`, `git show origin/main`, and ancestry check): Updated `origin/main` is `ce6a453cdab6054263931237beff7ece9b73a680`, whose commit is the merge of PR #16 for T3.3; T3.3 commit `f934cb8` is an ancestor. The feature branch was created at that exact `origin/main` SHA.
- **VERIFIED** (`git stash` output and `git stash list -1`): Pre-existing modified/untracked work was preserved in `stash@{0}` with message `T3.4 preserve pre-existing work`. It was not discarded or reapplied over the updated T3.3 report. The stash remains available; unrelated changes therefore remain preserved but are not currently in this branch's working tree.
- **VERIFIED** (inspected Phase 2 tests): Client/Document controller tests and `ClientAggregateUpdateHttpIntegrationTest` use `@AutoConfigureMockMvc(addFilters = false)`. **RECONSTRUCTED** from these test annotations: those existing functional tests verify business/REST behavior without exercising the security filter chain. Existing security integration coverage accepted a valid token on the Client list and rejected missing/invalid/expired tokens on protected routes, but did not exercise authenticated full Client/Document CRUD through the filters.
- **VERIFIED** (`AuthControllerIntegrationTest`): The added test obtains a token through the existing login endpoint/test harness and exercises authenticated Client POST, list, get, PUT, PATCH and DELETE plus Document POST, list, get, PUT, PATCH and DELETE. It checks established status codes, response fields, version headers/body and Problem Details on the post-delete not-found responses.
- **VERIFIED** (`SecurityConfigurationIntegrationTest`): Missing bearer authentication is asserted as 401 for both Client list and nested Document list. Invalid-signature and expired JWT are asserted as 401 on the nested Document route; the invalid-token response is checked for safe Problem Details and absence of token/secret values. Existing valid Client JWT and public OpenAPI checks remain.
- **VERIFIED** (`SecurityProblemHandlersTest` and `implementation-plan.md`, Section 7): The existing safe 403 handler test continues to assert a generic 403 Problem Details response without exception content. No business operation currently defines a forbidden case for the single-user/no-role model; no artificial authorization rule or 403 endpoint was introduced.
- **VERIFIED** (focused Maven output): Command `.\mvnw.cmd test "-Dtest=AuthControllerIntegrationTest,SecurityConfigurationIntegrationTest"` succeeded — 9 tests run, 0 failures, 0 errors, 0 skipped (AuthControllerIntegrationTest: 3; SecurityConfigurationIntegrationTest: 6).
- **VERIFIED** (full Maven output): Command `.\mvnw.cmd test` succeeded — 332 tests run, 0 failures, 0 errors, 0 skipped. No second full-suite run was performed.
- **VERIFIED** (`git diff --check` and final Git status): No whitespace errors; only the two listed test files were modified before this report. Current branch HEAD equals `origin/main` at `ce6a453`; no commit, push, PR, or merge was performed.
- **VERIFIED** (Maven focused output): Maven printed an HTTP 401 metadata-transfer warning for the Flyway community DB support repository; test execution continued and succeeded. **NOT AVAILABLE**: Whether this external metadata warning is transient or requires repository credentials.
- **NOT AVAILABLE**: Independent Tester verification, Reviewer audit, and Human Tech Lead final acceptance have not been performed/recorded by this Developer execution.

## 9. Findings and severity

| ID | Severity | Finding / disposition |
|---|---|---|
| T34-DEV-01 | Info | **VERIFIED / RECONSTRUCTED**: Existing Phase 2 endpoint tests disabled security filters; added one authenticated integration test to bridge the material test gap without changing application behavior. |
| T34-DEV-02 | Info | **VERIFIED**: Existing 403 safe-handler test is present. No actual forbidden business action is defined for this one-user/no-role API, so no artificial 403 route was added. |
| T34-DEV-03 | Info | **VERIFIED**: Unrelated pre-existing local changes were preserved in Git stash `stash@{0}` because a changed T3.3 report conflicted with the updated base. They were not lost, but are not checked out on this feature branch. |
| T34-DEV-04 | Info | **VERIFIED**: Focused and full Maven test suites passed. Maven logged an external Flyway metadata HTTP 401 warning, but it did not fail either run. |

## 10. Decisions and reasoning

- **VERIFIED**: Kept changes test-only. Reused `StartupTestHarness`, the existing public login flow, and the application-issued HS256 token rather than inventing credentials or adding test dependencies.
- **VERIFIED**: Used one lifecycle integration scenario to cover all Client and nested Document CRUD methods under the real security filter chain, while retaining existing Phase 2 functional tests for detailed non-security contract behavior.
- **VERIFIED**: Expanded route checks for missing, invalid and expired JWTs to include the nested Document API, since existing unauthenticated coverage already exercised the Client route.
- **VERIFIED**: Left the existing 403-handler test intact. The approved plan explicitly says there are no roles/permissions and artificial authorization rules must not be added solely to trigger 403.
- **VERIFIED**: Preserved prior local changes by stashing rather than attempting to overwrite a T3.3 report changed both locally and on the updated base. No reset, clean, rebase, force-push, commit, push, PR, or merge was run.
- **VERIFIED**: Did not change T3.2 provisioning, production configuration, or credentials; the new CRUD test authenticates using the existing test harness login credentials.

## 11. Final status

**IMPLEMENTED AND TESTED — pending independent Tester verification, Reviewer audit, and Human Tech Lead acceptance.** The requested regression tests were added/adjusted and both test runs passed. This report does not approve or mark T3.4 complete.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Tester must independently verify the implementation and Phase 3 contract before reading Developer conclusions; Reviewer must independently audit before reading other agent reports.
- **PENDING APPROVAL**: Human Tech Lead acceptance remains outstanding; Phase 3 exit criteria and T3.4 are not declared complete here.
- **VERIFIED**: Pre-existing unrelated work is preserved in `stash@{0}`. Reapply it only when safe, accounting for its T3.3 report conflict with updated `origin/main`.
- **NOT AVAILABLE**: Cause/remediation for the non-fatal external Flyway metadata HTTP 401 warning is not established.

## 13. Handoff to next agent

- **VERIFIED**: Test changes are in `src/test/java/com/humanit/challenge/security/AuthControllerIntegrationTest.java` and `SecurityConfigurationIntegrationTest.java`; report is `agent-reports/T3.4/developer-report.md`.
- **VERIFIED**: Branch is `feature/T3.4-security-regression`, based at `ce6a453cdab6054263931237beff7ece9b73a680`; no Git close-out operation was performed.
- **VERIFIED**: Prior unrelated work remains intact in `stash@{0}`; do not discard the stash.
- **PENDING VERIFICATION**: Tester should complete independent verification before consulting this report. Reviewer should complete the independent audit before consulting Architect, Developer, or Tester conclusions. Human Tech Lead alone decides acceptance.
