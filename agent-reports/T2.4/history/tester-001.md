# Tester Report — T2.4 Part 1: DocumentService

## 1. Task ID

**VERIFIED (direct tasking):** T2.4 Part 1 — DocumentService verification.

## 2. Execution identifier and date

**VERIFIED (direct tasking):** `T2.4-EXEC-002-TEST-SERVICE` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Tester; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct tasking):** Independently inspect DocumentService, its tests and repository contracts; verify CRUD/transactions, Client ownership, versions, forced parent-version increments, uniqueness, PATCH semantics, HTTP error status behavior and essential test coverage. Run only the focused Maven command recorded in Section 7. Do not change application code, reopen T2.2/T2.3, mutate Git or self-approve.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`, `requirements.md`, `requirements-decisions.md`, `implementation-plan.md`, and `agent-reports/decisions.md` (D-11 and D-13).
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/DocumentService.java`, `repository/ClientRepository.java`, `repository/DocumentRepository.java`, `mapper/DocumentMapper.java`, Document request DTOs, `dto/patch/PatchValueDeserializer.java`, `exception/GlobalExceptionHandler.java`, and `src/main/resources/db/migration/V2__create_clients_and_documents.sql`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`.
- **VERIFIED (after independent inspection and test):** `agent-reports/T2.4/developer-report.md` was then read for comparison.

## 6. Files changed

- **VERIFIED:** Only `agent-reports/T2.4/tester-report.md` was created. No application/test code or Git state was changed by this execution.

## 7. Commands executed

- **VERIFIED:** Executed .\mvnw.cmd -B -Dtest=DocumentServiceTest test — BUILD SUCCESS; 7 tests, 0 failures, 0 errors, 0 skipped. Maven emitted a metadata HTTP 401 warning for Flyway community DB support, but the build and focused test completed successfully.
- **VERIFIED:** No Git commands or additional test commands were run.

## 8. Evidence and observed results

- **VERIFIED (`DocumentService.java:35-111`):** Create, get, list, PUT, PATCH and delete have service-layer transaction boundaries; reads use `readOnly=true`. Writes flush persistence operations inside the transactional service workflow. Tests disable the test-managed transaction (`DocumentServiceTest.java:29-35`), so service invocations exercise their own transactions.
- **VERIFIED (`DocumentService.java:126-129`; `DocumentRepository.java:19`):** Nested Document lookup scopes by both Document ID and Client ID. Wrong-owner nested access therefore resolves as 404; the focused test asserts this (`DocumentServiceTest.java:139-155`). Listing checks Client existence and queries by Client ID (`DocumentService.java:54-60`); the test confirms another Client's Document is excluded (`DocumentServiceTest.java:62-77`).
- **VERIFIED (`DocumentService.java:114-138`; `ClientRepository.java:20-22`):** Mutations require the expected Client version, acquire `OPTIMISTIC_FORCE_INCREMENT`, and compare the loaded version; PUT/PATCH/delete also check the Document version. Null expected versions produce 400; stale values produce 409. Request DTOs constrain negative body versions with `@PositiveOrZero` (`DocumentPutRequest.java:16-20`, `DocumentPatchRequest.java:25-37`); `GlobalExceptionHandler.java:61-77,140-155` preserves 400/404/409 as Problem Details.
- **VERIFIED (focused test output and Hibernate SQL):** The executed test run showed parent Client version `UPDATE`s for create, PUT, PATCH and delete. This is consistent with the `OPTIMISTIC_FORCE_INCREMENT` repository lock and flush sequence. **VERIFIED (`DocumentServiceTest.java:48-155`):** tests exercise create, PUT/PATCH, delete and stale Client/Document conflicts, but do not directly assert the parent version increments after each mutation.
- **VERIFIED (`DocumentService.java:140-146`; `DocumentRepository.java:15`; migration `V2__create_clients_and_documents.sql:14-23`):** Number lookup is global (not Client-scoped); the database has a global unique constraint; update checks exclude the current Document; database integrity and optimistic locking failures in mutation flushes map to 409. The test covers a duplicate on create (`DocumentServiceTest.java:128-137`).
- **VERIFIED (D-13; `DocumentPatchRequest.java:21-23,31-37`; `PatchValueDeserializer.java:17-24,66-70`; `DocumentMapper.java:97-103,183-197`):** PATCH distinguishes absent, explicit null and value; absent fields remain unchanged, nullable description can be cleared, and required fields reject explicit null through DTO validation. The service test asserts description clearing and editable-field/owner behavior (`DocumentServiceTest.java:79-98`); it does not directly assert absent-field preservation or invalid explicit-null HTTP binding.
- **VERIFIED (`DocumentServiceTest.java:48-155`):** Seven focused integration-style service tests cover create/get, scoped paged list, PUT/PATCH, delete, missing resources, duplicate number, stale versions and wrong-owner access. **NOT AVAILABLE:** No `DocumentServiceTest` assertion exercises missing-version 400 behavior, explicit parent-version increments, conflict rollback, or all duplicate-number update/PATCH cases.
- **VERIFIED (process deviation):** A broad search performed before the focused run returned snippets from the Developer report. The source/contracts and test implementation were independently inspected and the requested test was run before the full Developer report was opened, but the strict instruction to avoid seeing any Developer conclusions before verification was not fully preserved. This is disclosed; the verification was based on the primary artifacts and execution output.
- **VERIFIED (comparison):** After verification, the Developer report's central implementation and seven-test claims matched the inspected code and focused test result. Its 264-test full-run claim was not rerun or independently verified here; this execution ran only the requested focused command.

## 9. Findings and severity

- **F-01 — Minor, direct assertion gaps:** `DocumentServiceTest.java:48-155` does not explicitly assert expected-version missing/invalid 400 outcomes, parent Client version increments per mutation, absent-field PATCH preservation, or rollback on conflict. Service/repository source and SQL output support the behavior observed, but those essential contract edges lack direct regression assertions. No application defect was observed in the inspected paths.
- **F-02 — Minor, execution-sequencing deviation:** The broad search exposed Developer-report snippets before verification finished. Subsequent verification used direct source, contract and test evidence; the full report was read only after the focused test. Independent sequence was therefore imperfect, not entirely maintained.
- **VERIFIED:** No blocking implementation defect was identified in the inspected DocumentService scope. No T2.2/T2.3 behavior was reopened.

## 10. Decisions and reasoning

- **VERIFIED (D-11):** Repository access and transactional use-case orchestration reside in the service; DTO/entity transformation delegates to the dedicated mapper.
- **VERIFIED (D-13):** PATCH tri-state semantics align with the approved DTO, deserializer and mapper behavior. Ownership remains fixed to the nested Client path.
- **VERIFIED:** Focused tests cover the essential service flows and principal 404/409 scenarios. Findings are coverage gaps, not evidence that the inspected implementation returns incorrect behavior. HTTP-level controller binding and validation were not part of this Part 1 service run.
- **VERIFIED:** Status is **PASS WITH FINDINGS** because the focused suite passes and no blocker was found, with the test gaps and disclosed process deviation retained for Reviewer visibility.

## 11. Final status

**PASS WITH FINDINGS.** Focused test: **BUILD SUCCESS, 7 tests passed, 0 failures/errors/skips**. This is a Tester result only; no Tech Lead approval or acceptance is claimed.

## 12. Outstanding issues

1. **VERIFIED (F-01):** Consider adding direct service tests for missing versions, force-incremented Client versions, PATCH omission preservation and rollback on conflict when authorized.
2. **VERIFIED (F-02):** Reviewer should be aware of the disclosed sequencing deviation and perform its audit independently.
3. **PENDING VERIFICATION:** Controller-level HTTP binding/status/Problem Details and final Tech Lead acceptance are outside this service-only execution.

## 13. Handoff to next agent

- **VERIFIED:** Ready for Reviewer: **YES**. The Reviewer should independently inspect source/contracts before reading agent conclusions, and assess F-01/F-02.
- **PENDING VERIFICATION:** Final acceptance belongs only to the Human Tech Lead.
