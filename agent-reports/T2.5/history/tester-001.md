# Tester Report — T2.5 Independent Functional Verification

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Section 6): T2.5 — Aggregate update semantics and Document identity preservation.

## 2. Execution identifier and date

**VERIFIED** (direct tasking/environment): `T2.5-EXEC-003-TEST` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Tester; `opencode-go/gpt-6-luna`.

## 4. Authorized scope

**VERIFIED** (direct tasking and `implementation-plan.md`, Section 6): Independently verify Client aggregate PUT/PATCH merge rules, Document identity preservation, unknown-ID and cross-Client association rejection, standalone/aggregate consistency, and absence of implicit deletion or unexpected creation. Prefer read-only verification; do not modify production code, add redundant tests, expand into T2.6/T2.7, commit/push, or self-approve.

## 5. Files inspected

- **VERIFIED:** `implementation-plan.md` (T2.5 and task boundaries), `requirements-decisions.md` (PUT/PATCH, ownership and endpoint consistency), `agent-reports/decisions.md`, and `agent-reports/README.md`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`, `DocumentService.java`, `mapper/ClientMapper.java`, `mapper/DocumentMapper.java`, `domain/Client.java`, `domain/Document.java`, `controller/ClientController.java`, `controller/DocumentController.java`, `exception/GlobalExceptionHandler.java`, and relevant Client/Document request DTOs.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`, `DocumentServiceTest.java`, `controller/ClientControllerTest.java`, `DocumentControllerTest.java`, and `exception/GlobalExceptionHandlerTest.java`.
- **VERIFIED:** After completing independent inspection and the focused test run, read `agent-reports/T2.5/developer-report.md` to compare claims. The Architect proposal was not read.
- **VERIFIED:** The initial `agent-reports/T2.5/**` listing showed no existing Tester primary report or Tester history file; there was no earlier Tester report to archive.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.5/tester-report.md` — this report.
- **VERIFIED:** The focused Maven run generated/updated build outputs under `target/`, including the two named Surefire summaries. No production code, test source, configuration, dependency, migration, or Git data was changed.

## 7. Commands executed

- **VERIFIED:** `.\mvnw.cmd -Dtest=ClientServiceTest,DocumentServiceTest test` — PowerShell rejected the unquoted comma in the property before Maven ran; this was a shell parsing error, not a test failure.
- **VERIFIED:** `.\mvnw.cmd '-Dtest=ClientServiceTest,DocumentServiceTest' test` — BUILD SUCCESS; 26 tests, 0 failures, 0 errors, 0 skipped. Surefire summaries: `target/surefire-reports/com.humanit.challenge.service.ClientServiceTest.txt` (14 tests) and `target/surefire-reports/com.humanit.challenge.service.DocumentServiceTest.txt` (12 tests).
- **VERIFIED:** No full Maven suite was run in this execution; the focused integration-backed suites sufficed for the scoped checks. Developer full-suite claims are treated as not independently rerun here.

## 8. Evidence and observed results

- **VERIFIED** (`ClientServiceTest`, focused run and corresponding Surefire summary): Client PUT with either omitted `documents` or an empty collection preserves the existing Document IDs and row count.
- **VERIFIED** (`ClientServiceTest`, focused run): Client PUT updates an existing Document in place (same ID), creates an entry without ID/version with a generated ID and the target Client owner, and preserves an existing Document omitted from the supplied collection. Persisted row count is exactly the expected three.
- **VERIFIED** (`ClientServiceTest`, focused run): Client PATCH with omitted or empty `documents` preserves the collection. A nested PATCH changes the supplied Document description while keeping its ID, owner, number and expiration date; the other omitted Document remains unchanged and no additional row appears.
- **VERIFIED** (`ClientServiceTest`, focused run): Unknown nested Document ID raises `ResponseStatusException` with status 404; existing Document row/count and collection remain intact, with no creation.
- **VERIFIED** (`ClientServiceTest`, focused run): Associating a Document belonging to another Client raises `ResponseStatusException` with status 409; persisted ownership and the original and target Client collections remain unchanged.
- **VERIFIED** (`DocumentServiceTest`, focused run): Standalone Document PUT and PATCH retain Document identity and owner. PUT replaces editable values; PATCH changes the supplied value and preserves omitted values.
- **VERIFIED** (`DocumentServiceTest`, focused run): The persistence-backed standalone/aggregate comparison exercises both PUT and PATCH and confirms matching field-update behavior, preserved identities and Client ownership; the update operations do not change the expected Client-scoped Document count.
- **VERIFIED** (implementation inspection): ClientService returns 404 for unknown nested IDs and 409 for cross-Client associations. `GlobalExceptionHandler` maps `ResponseStatusException` to its status and Problem Details; existing MockMvc controller/handler tests cover generic response-status mapping. The focused T2.5 service tests themselves do not exercise aggregate update through MockMvc.
- **VERIFIED** (implementation inspection): Client scalar PATCH mapping applies only supplied values. **NOT AVAILABLE** (focused regression coverage): no persistence-backed T2.5 test directly asserts that an omitted Client scalar remains unchanged after Client PATCH; the aggregate PATCH test directly asserts nested Document partial-field behavior instead.
- **VERIFIED** (scope): Focused H2/JPA-backed test cases cover no implicit deletion or unexpected creation in the specified aggregate paths. Concurrency/rollback contract claims are not made; those remain T2.6/T2.7 scope.
- **VERIFIED** (independent comparison with `agent-reports/T2.5/developer-report.md`): Its claims about the scoped test behaviors and the focused outcome (26 passed) agree with my source inspection and independent run. Its note that aggregate JSON binding / business-endpoint Problem Details behavior was not newly checked is consistent with this execution.
- **PENDING VERIFICATION:** Developer-reported full clean suite result (285 tests) was not independently repeated in this execution.

## 9. Findings and severity

- **Minor — Client scalar PATCH partial-update regression assertion missing.** The implementation inspection supports omission-preserving behavior, but the scoped persistence-backed tests do not directly verify an omitted Client scalar remains unchanged. Existing controller tests mock the service and do not establish this service behavior.
- **Minor — Aggregate failure HTTP response path not exercised end-to-end.** Aggregate tests assert service `ResponseStatusException` statuses (404/409), and generic HTTP Problem Details translation is tested separately. No focused MockMvc request was run through the actual aggregate update service to verify the final HTTP response for unknown/cross-Client Document IDs.
- **No Critical or Major functional defect observed** in the verified T2.5 scenarios.

## 10. Decisions and reasoning

- **VERIFIED** (focused test run and implementation evidence): The essential collection, identity, ownership, unknown-ID and standalone/aggregate field semantics passed persistence-backed focused tests; no production change or redundant test was warranted.
- **VERIFIED** (scope boundary): The focused test run was sufficient for the requested T2.5 scenarios; no full suite was needed to validate a failure or compatibility issue. No T2.6/T2.7 behavior is inferred.
- **RECONSTRUCTED** (from focused tests plus source inspection): The aggregate service statuses should be translated to matching HTTP statuses by the common exception handler. This is not equivalent to observing those exact aggregate requests over MockMvc, so the API-path coverage gap remains a Minor finding.
- **VERIFIED:** Developer claims checked after independent verification were confirmed for focused test result and the tested behavior. The Developer's 285-test full-suite result was not independently verified here; no contrary evidence was found.
- **NOT AVAILABLE:** Human Tech Lead final acceptance of T2.5; this report makes no approval claim.

## 11. Final status

**PASS WITH FINDINGS** — requested T2.5 behavior was verified in focused tests and implementation inspection, with two Minor coverage gaps documented. This is a Tester result, not final acceptance.

**Ready for Reviewer: YES.**

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Reviewer to independently audit the T2.5 implementation and verification evidence.
2. **PENDING VERIFICATION:** Human Tech Lead to determine final T2.5 acceptance after review; no approval is claimed.
3. **PENDING VERIFICATION:** Consider directly testing omitted Client scalar preservation and actual aggregate endpoint 404/409 mapping if additional T2.5 coverage is requested.
4. **VERIFIED (scope boundary):** T2.6 concurrency/version behavior and T2.7 transaction-wide rollback remain outside this execution.

## 13. Handoff to next agent

- **VERIFIED:** Reviewer may begin an independent audit. The findings above identify test-coverage limitations, not observed production defects.
- **VERIFIED:** Human Tech Lead retains final acceptance authority. No self-approval or task acceptance is recorded.
