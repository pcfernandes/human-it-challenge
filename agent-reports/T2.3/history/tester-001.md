# Tester Report — T2.3 Part 1: ClientService

## 1. Task ID
T2.3

## 2. Execution identifier and date
T2.3-EXEC-002-TEST-01 — 2026-11-10

## 3. Agent role and model
Tester (Java/Spring Boot). Model: beam-free.

## 4. Authorized scope
Independent verification of `ClientService` functional behavior (CRUD, 404/409 handling, version enforcement, ID/ownership preservation, uniqueness, nested document handling, transactions). No production or test code changes. No Git mutations.

## 5. Files inspected
- `src/main/java/com/humanit/challenge/service/ClientService.java`
- `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`
- `domain/Client.java`, `domain/Document.java`
- `repository/ClientRepository.java`, `repository/DocumentRepository.java`
- `mapper/ClientMapper.java`, `mapper/DocumentMapper.java`
- `dto/client/*`, `dto/patch/PatchValue.java`
- `implementation-plan.md` (T2.3, T2.5, T2.6, T2.7)
- `agent-reports/T2.3/developer-report.md` (read only after verification)

## 6. Files changed
None.

## 7. Commands executed
- `.\mvnw.cmd -B -q clean test` (single run; Surefire XML evidence).

## 8. Evidence and observed results
- Surefire: 247 tests total, 0 failures, 0 errors, 0 skipped (XML aggregates; TXT per-class reports undercount nested tests). **VERIFIED**.
- `ClientServiceTest`: 5 tests PASS (create/retrieve, update happy path, missing client, version conflict, nested document). **VERIFIED**.
- Exit code: not printed under `-q`; build completed and all Surefire reports were generated with 0 failures (exit code 0 inferred). **RECONSTRUCTED**.
- Developer report claims 249 tests; observed count is 247. **VERIFIED** discrepancy of 2 tests (documentation only).
- CRUD uses `ClientRepository` (`saveAndFlush`, `findById`, `findAll`, `delete`) and `DocumentRepository` (`findById`, `findByNumber`) correctly. **VERIFIED**.
- Missing client paths (`getById`, `loadClient`) throw `ResponseStatusException` 404. **VERIFIED**.
- `enforceVersion` rejects null/mismatched expected version with 409. **VERIFIED**.
- ID and ownership are preserved: mappers do not overwrite IDs; `Document.client_id` is `updatable=false`; `assignClient` rejects ownership transfer. **VERIFIED**.
- Tax identifier and document number uniqueness are pre-checked plus DB constraints. **VERIFIED**.
- Nested document handling: new (no id/version) created; existing requires id+version and ownership/version checks; unknown id 404; cross-owner 409. **VERIFIED**.
- Omitted/empty `documents` preserves existing documents (PUT/PATCH). **VERIFIED**.
- `@Transactional` present on create/update/patch/delete; read-only on get/list. **VERIFIED**.
- `OPTIMISTIC_FORCE_INCREMENT` (`ClientRepository.lockById`) is not used by `ClientService`; parent version increment on document-only changes is deferred to T2.6. **VERIFIED**.

## 9. Findings and severity
- **F-01 (Major, blocking):** Alias methods `put`, `get`, `findAll`, `deleteById` are not `@Transactional` and delegate through `this` to transactional methods, so Spring's transactional advice is bypassed when aliases are invoked; transaction boundaries/atomicity are lost. **VERIFIED**.
- **F-02 (Minor):** `create()` catches any `DataIntegrityViolationException` and reports "Tax identifier already in use", masking document-number (and other) constraint violations. **VERIFIED**.
- **F-03 (Minor):** Duplicate document numbers within a single create/update/patch request are not detected before flush (only persisted-row checks), so they surface as a generic conflict. **VERIFIED**.
- **F-04 (Minor):** `delete()` does not call `saveAndFlush`/flush; constraint or optimistic-lock failures may escape the try block and not be mapped to 409 (transaction still rolls back). **VERIFIED**.
- **F-05 (Info):** Test-count discrepancy: 247 observed vs 249 reported by Developer. Not a code defect. **VERIFIED**.

## 10. Decisions and reasoning
- Primary transactional methods behave as specified and all 5 `ClientServiceTest` cases pass.
- Transaction correctness is impaired only through the non-transactional aliases (F-01); recommend annotating aliases or removing them.
- Uniqueness/status-code findings are minor robustness issues; DB constraints remain the backstop.
- `OPTIMISTIC_FORCE_INCREMENT` is out of T2.3 Part 1 scope (T2.6) and is not treated as a defect here.

## 11. Final status
PASS WITH FINDINGS (247 tests, 0 failures, 0 errors, 0 skipped).

## 12. Outstanding issues
- Fix F-01 before acceptance (alias transaction boundaries).
- Address F-02/F-03/F-04 for correct conflict reporting.
- T2.6 concurrency force-increment and T2.3 controllers remain pending.

## 13. Handoff to next agent
Reviewer: audit `ClientService` independently first, then read this report and the Developer report; verify F-01 alias transaction behavior and conflict mapping. Ready for Reviewer: YES.
Blocking defects: F-01 (Major); F-02–F-04 non-blocking.
No Git mutations performed.
No approval recorded.
