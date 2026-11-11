# Tester Report — T2.6 Independent Verification

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2, T2.6): T2.6 — Concurrency control and version validation.

## 2. Execution identifier and date

- **VERIFIED (direct tasking):** `T2.6-EXEC-003-TEST`.
- **VERIFIED (environment):** 2026-11-11.

## 3. Agent role and model

- **VERIFIED:** Tester role; model `opencode-go/gpt-6-luna` (`gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED (direct tasking and `implementation-plan.md`, T2.6):** Independently verify Client DELETE version preconditions, aggregate Document versioning, real concurrent writers, conflict rollback, response versions, version validation and Problem Details; preserve T2.5 semantics and remain outside unrelated T2.7 redesign.
- **VERIFIED (direct tasking):** Verification only. No production or test source edits, Git mutation, governance reconstruction edits or self-approval.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`; `implementation-plan.md` (T2.5–T2.7); `requirements-decisions.md` (Concurrency Control and Section 20 requirements).
- **VERIFIED:** `src/main/java/com/humanit/challenge/controller/ClientController.java`; `service/ClientService.java`; `service/DocumentService.java`; `repository/ClientRepository.java`; `exception/GlobalExceptionHandler.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`; `service/DocumentServiceTest.java`; `service/OptimisticConcurrencyIntegrationTest.java`; `controller/ClientControllerTest.java`; `controller/DocumentControllerTest.java`; `controller/ClientAggregateUpdateHttpIntegrationTest.java`.
- **VERIFIED (after independent source inspection and test execution):** `agent-reports/T2.6/architect-proposal.md` and `agent-reports/T2.6/developer-report.md` were read for comparison.
- **NOT AVAILABLE:** No prior `agent-reports/T2.6/tester-report.md` or Tester history archive was present when checked; no archive was required.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.6/tester-report.md` — this report only.
- **VERIFIED:** No production or test source, Architect/Developer report, decision log or governance reconstruction file was changed by this Tester execution.

## 7. Commands executed

- **VERIFIED (initial attempt; PowerShell parser failure before Maven ran):** `.\mvnw.cmd -B clean -Dtest=ClientServiceTest,DocumentServiceTest,ClientControllerTest,DocumentControllerTest,ClientAggregateUpdateHttpIntegrationTest,OptimisticConcurrencyIntegrationTest test`.
- **VERIFIED (focused successful rerun):** `.\mvnw.cmd -B clean "-Dtest=ClientServiceTest,DocumentServiceTest,ClientControllerTest,DocumentControllerTest,ClientAggregateUpdateHttpIntegrationTest,OptimisticConcurrencyIntegrationTest" test` — `BUILD SUCCESS`; 63 tests, 0 failures, 0 errors, 0 skipped.
- **VERIFIED (full suite, run after focused pass):** `.\mvnw.cmd -B clean test` — `BUILD SUCCESS`; 308 tests, 0 failures, 0 errors, 0 skipped.

## 8. Evidence and observed results

### Client DELETE and version contract

- **VERIFIED** (`ClientController.java`; `ClientService.java`; `ClientAggregateUpdateHttpIntegrationTest.clientDeleteEnforcesVersionAndDoesNotDeleteOnInvalidOrStalePrecondition`): DELETE binds required `X-Client-Version`. A current version returns 204 and removes Client plus associated Documents. Missing, malformed and negative versions return 400; stale returns 409 Problem Details. The test reloads/checks both rows after the invalid and stale requests and confirms they remain.
- **VERIFIED** (`ClientControllerTest.java`; `DocumentControllerTest.java`; `GlobalExceptionHandler.java`; focused test output): Missing/malformed required version inputs map to 400 Problem Details; service-validated negative versions return 400; stale version conflicts return 409 Problem Details. The corresponding current-version endpoint paths are covered by passing controller/service/integration tests.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.postWithInitialDocumentsNeedsNoExpectedClientVersionAndUsesJpaVersion`; `ClientService.create`): Client POST with initial Documents requires no expected version, does not call the force-increment lock, and returns a Client version equal to the persisted JPA version. This matches the Option A behavior identified in the task; no exact initial version is assumed.

### Aggregate versioning and T2.5 preservation

- **VERIFIED** (`ClientService.java`; `ClientRepository.java`; `ClientServiceTest.java`; `DocumentServiceTest.java`): Non-empty PUT nested Document operations and PATCH operations that actually create/edit a nested Document acquire the existing `OPTIMISTIC_FORCE_INCREMENT` lock for the parent Client within the service transaction. Nested-only HTTP PUT/PATCH integration tests establish a higher persisted Client version and compare the response version with a database reload.
- **VERIFIED** (`ClientServiceTest.java`): Omitted and empty Documents do not force-increment the Client. An identity/version-only nested PATCH is also a no-op for aggregate versioning. Scalar-only updates retain JPA Client version behavior; real competing scalar writers are separately covered.
- **VERIFIED** (`ClientServiceTest.java`; `DocumentServiceTest.java`): Existing nested Document IDs and Client ownership are preserved through aggregate updates; the existing T2.5 collection-preservation, update/create, omitted-document and ownership regression cases remain in the focused suite and pass.

### Actual concurrency and rollback

- **VERIFIED** (`OptimisticConcurrencyIntegrationTest`, H2/JPA with separate `TransactionTemplate` transactions and coordinated concurrent workers): Two writers using the same Client version to update different Documents produce exactly one success and one optimistic conflict; reload assertions show only one Document update persisted and the parent version advanced.
- **VERIFIED** (`OptimisticConcurrencyIntegrationTest`): Same-Document writers and scalar-only Client writers likewise produce exactly one winner and one conflict. Persisted state reflects only the winning writer.
- **VERIFIED** (`ClientAggregateUpdateHttpIntegrationTest.staleNestedDocumentVersionRollsBackEarlierAggregateChanges`): An HTTP aggregate PUT applies attempted scalar and first-Document changes before a later nested Document's stale version yields 409 Problem Details. After `EntityManager.clear()` and database reload, the Client scalar and earlier Document fields remain unchanged, the other Document remains unchanged, and the stale replacement number is absent.
- **VERIFIED (scope):** The observed concurrent integration tests use H2; PostgreSQL execution is **NOT AVAILABLE** in this task run and is not claimed.

### Response reread and concrete query finding

- **VERIFIED** (`ClientController.java`, update/patch methods; `ClientAggregateUpdateHttpIntegrationTest.nestedOnlyPutAndPatchReturnPersistedClientVersion`): For the tested nested mutations, the controller calls the service after the mutation service transaction returns and the HTTP response version matches the persisted Client version. The reread is justified for force-incremented mutations because JPA may apply that version increment at transaction completion.
- **VERIFIED — MINOR:** The PATCH controller rereads on any present, non-empty nested Documents list (`ClientController.java`, condition at lines 64–66), while `ClientService.patch` only requests parent force-increment for nested entries with actual create/edit values (`ClientService.java`, `nestedDocumentsSupplied` / `hasNestedPatchValues`, lines 92–95 and 224–229). The existing `nestedPatchWithIdentityAndVersionOnlyDoesNotForceIncrementClient` test proves this input can be a no-op, but the controller still calls `getById`. That creates an unnecessary post-transaction Client/Document read for an identity/version-only PATCH. This is a concrete query inefficiency, not a persisted-version correctness defect; it is not shown to corrupt response fields.
- **VERIFIED:** No inconsistent response field or other concrete response-version correctness defect was observed in the inspected code and passing HTTP tests. No additional correctness risk is asserted from untested concurrent interleavings.

### Independence-process caveat

- **VERIFIED (execution history):** Before source inspection and test execution were complete, a broad content search for T2.6 terms returned matching excerpts from both the Architect proposal and Developer report. Those conclusions were therefore exposed earlier than the required strict independence sequence. I then inspected the approved contract and implementation, executed both required Maven runs, formed the findings above, and only afterward opened the two complete reports for comparison. This process deviation qualifies the independence claim; the findings above are supported by the cited source and test output, but the Reviewer should audit them independently.

## 9. Findings and severity

1. **MINOR — unnecessary post-transaction read for no-op nested PATCH. VERIFIED** (`ClientController.java`; `ClientService.java`; `ClientServiceTest.java`): Identity/version-only nested PATCH does not mutate a Document or force-increment Client version, but the controller performs `getById` solely because the nested list is non-empty. This adds an avoidable read of the Client and its Documents. Response correctness is unaffected in the demonstrated scenario. No Critical or Major implementation finding was observed.
2. **PROCESS NOTE — independence sequence was qualified. VERIFIED** (execution history in Section 8): Broad search output exposed Architect/Developer report excerpts before independent source review and Maven verification were complete. Full reports were only read after verification. This is disclosed for the Reviewer; it is not a product-code defect.

## 10. Decisions and reasoning

- **VERIFIED:** Focused verification met the requested gate, so the full suite was also executed. Both successful runs are recorded in Section 7.
- **VERIFIED:** Response reread after nested mutations is necessary for correctness on the exercised force-increment paths. The no-op identity/version-only PATCH condition is broader than the service's actual mutation condition and is reported as Minor rather than as a response correctness failure.
- **VERIFIED:** After independent verification, comparison with `developer-report.md` found the 63/308 passing test counts and the described DELETE, force-increment, concurrency, rollback, Option A and response-version results consistent with observed artifacts. The report does not identify the Minor no-op PATCH reread; this is an additional Tester finding, not a contradiction to its stated response-version tests. The Architect proposal's pre-implementation major gaps are addressed in the current inspected implementation and test evidence; no current-code discrepancy was identified.
- **VERIFIED:** No T2.7 transaction redesign or unrelated scope was evaluated or performed. No approval or acceptance is claimed.

## 11. Final status

**VERIFIED — TESTS PASS; READY FOR REVIEWER: YES, WITH QUALIFICATION.** Focused suite: 63 passed, 0 failed. Full suite: 308 passed, 0 failed. No Critical/Major product finding; one Minor unnecessary-read finding remains. The early report-excerpt exposure is disclosed as an independence-process limitation. This report is not Reviewer approval or Human Tech Lead acceptance.

## 12. Outstanding issues

1. **VERIFIED — MINOR:** Consider narrowing the Client PATCH post-transaction reread condition to actual nested mutations; the current identity/version-only no-op request causes an avoidable read.
2. **PENDING VERIFICATION:** Reviewer should independently audit the implementation and this report, accounting for the report-excerpt exposure documented in Section 8.
3. **NOT AVAILABLE:** PostgreSQL-specific concurrency verification was not run; current integration evidence is H2-only.
4. **PENDING APPROVAL:** Only the Human Tech Lead may make final acceptance decision.

## 13. Handoff to next agent

- **VERIFIED:** `agent-reports/T2.6/tester-report.md` records the independent source/test evidence, test commands/results, the Minor query finding and the independence-process caveat.
- **PENDING VERIFICATION:** Reviewer may proceed with an independent audit; do not treat this Tester report as approval.
- **PENDING APPROVAL:** Human Tech Lead retains final acceptance authority. No source edits, Git operations or self-approval were performed; stop after reporting.
