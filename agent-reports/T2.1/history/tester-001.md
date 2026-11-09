# Tester Report — T2.1 Independent Persistence Verification

## 1. Task ID

**VERIFIED:** T2.1 — Domain model, mappings and database schema, verified against `implementation-plan.md` §6, `requirements-decisions.md` §§2, 5, 6, 8, 9, 12 and 20, and the direct task instructions for this execution.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.1-EXEC-003-TST-01` |
| Date | 2026-11-09 |
| Primary report status before this execution | **VERIFIED:** `agent-reports/T2.1/` contained no `tester-report.md`; no Tester history archive was present, so no prior Tester report required archiving. |

## 3. Agent role and model

- **VERIFIED — Role:** Independent Senior Java/Spring Boot QA Engineer.
- **VERIFIED — Model:** `gpt-6-luna` (exact model ID `opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED:** Independently assess T2.1 persistence mappings, field constraints, uniqueness, foreign-key integrity, cascade and collection-removal behavior, optimistic locking, equality/hash behavior, lazy-loading side effects, Flyway/H2 migration and schema validation, repository query declarations/pagination support, test results, and working-tree scope. No production or test source was modified by this Tester execution. PostgreSQL compatibility was not to be inferred without testing against a real PostgreSQL instance.

## 5. Files inspected

**VERIFIED:** Requirements, reporting conventions, configuration and implementation inspected independently before reading Developer conclusions:

- `agent-reports/README.md`
- `implementation-plan.md`
- `requirements-decisions.md`
- `pom.xml`
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/db/migration/V1__baseline.sql`
- `src/main/resources/db/migration/V2__create_clients_and_documents.sql`
- `src/main/java/com/humanit/challenge/domain/Client.java`
- `src/main/java/com/humanit/challenge/domain/Document.java`
- `src/main/java/com/humanit/challenge/repository/ClientRepository.java`
- `src/main/java/com/humanit/challenge/repository/DocumentRepository.java`
- `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`
- All 13 Surefire `.txt` reports and the Surefire report directory contents after running the full verification.
- Current `git status`, tracked diff for `.opencode/agents/reviewer.md`, and `git diff --check` output.

**VERIFIED:** Only after independent source inspection and both required test executions completed, I read `agent-reports/T2.1/developer-report.md`, `agent-reports/T2.1/architect-proposal.md`, and `agent-reports/decisions.md` to compare conclusions and authorization evidence.

## 6. Files changed

- **VERIFIED:** This execution created only `agent-reports/T2.1/tester-report.md`. No application source, test source, migration, requirements, plan, configuration, Developer/Architect report, or decision-log file was modified by this Tester.
- **VERIFIED:** Maven generated build output under `target/`; these are build artifacts, not source changes. The working-tree listing showed the T2.1 implementation paths as untracked and `.opencode/agents/reviewer.md` as modified; the latter is discussed in §9.

## 7. Commands executed

All commands were executed in `C:\Users\pedro\human-it-challenge`:

| Command | Observed result |
|---|---|
| `git status --short; git diff --stat; git diff --name-status; git log -10 --oneline` | **VERIFIED:** T2.1 source/migration/test paths and `agent-reports/T2.1/` were untracked; `.opencode/agents/reviewer.md` was modified. Recent history was inspected; no commit was created by this execution. |
| `.\mvnw.cmd clean verify` | **VERIFIED:** `BUILD SUCCESS`; Maven aggregate reported 54 tests, 0 failures, 0 errors, 0 skipped. |
| `.\mvnw.cmd -Dtest=DomainPersistenceTest test` | **VERIFIED:** Focused persistence suite `BUILD SUCCESS`; 13 tests, 0 failures, 0 errors, 0 skipped. |
| `git diff -- .opencode/agents/reviewer.md; git status --short; git diff --check` | **VERIFIED:** The tracked out-of-scope diff removes one blank line in `.opencode/agents/reviewer.md`; `git diff --check` printed no issue. Working-tree status otherwise showed the T2.1 paths and reports directory described above. |

**VERIFIED:** Maven emitted an HTTP 401 warning while resolving Flyway community database-support metadata during the focused run; dependencies were available, and Maven completed successfully. Database uniqueness violations and failed-context logs in the output were deliberate negative test cases; Surefire records no failed test.

## 8. Evidence and observed results

### 8.1 Full and focused test execution

- **VERIFIED:** `.\mvnw.cmd clean verify` completed with `BUILD SUCCESS`; Maven printed `Tests run: 54, Failures: 0, Errors: 0, Skipped: 0`.
- **VERIFIED:** `target/surefire-reports/com.humanit.challenge.repository.DomainPersistenceTest.txt` records 13 run, 0 failures, 0 errors and 0 skipped after the full run. The focused run independently printed the same count and succeeded.
- **VERIFIED:** The remaining Surefire reports plus the persistence report total 54 tests: Combined profiles 2; default dev 3; demo-user properties 2; explicit dev 1; JWT validation 6; OpenAPI 1; pagination properties 5; prod profile 4; source scan 2; Base64 validator 6; exception handler 8; application context 1; persistence 13.
- **VERIFIED:** Neither the full run nor the focused run had failing tests. No reproduction procedure for a test failure applies to these executions.

### 8.2 Entity mappings, field constraints and database integrity

- **VERIFIED — source:** `Client` maps to `clients`, has a generated `Long` ID, required `firstName`, `lastName`, `taxIdentifier`, `email`, `phoneNumber`, and `version` columns; its `documents` collection is initialized and mapped one-to-many with lazy fetch.
- **VERIFIED — source/schema:** Client required scalar fields are `nullable=false` in `Client.java` and `NOT NULL` in V2. `tax_identifier` has the named unique constraint in both mapping and migration. The test `taxIdentifierIsGloballyUnique` inserts duplicate values and observes `DataIntegrityViolationException`.
- **VERIFIED — source/schema:** `Document` maps to `documents`, has generated `Long` ID, required `number`, `expirationDate`, `client`, and `version`; optional `description` is nullable. `number` has a named table-level unique constraint; test `documentNumberIsGloballyUniqueAcrossDifferentClients` attempts the same number under two distinct Clients and observes a constraint violation.
- **VERIFIED — source/schema:** The Document ownership association is `@ManyToOne(fetch = LAZY, optional = false)` with a non-null `client_id` and named FK. V2 declares the FK to `clients(id)` and `ON DELETE CASCADE`; `databaseRejectsDocumentWithoutAnExistingClient` confirms a nonexistent Client ID is rejected by H2.
- **VERIFIED — limitation:** Required/nullability enforcement was inspected in annotations and migration DDL and consistent with the successful H2 schema validation. The suite does not separately attempt a null SQL insert for each required field. Email format and nonblank validation are application/DTO-layer requirements deferred beyond T2.1, not entity annotations.
- **VERIFIED — source/schema:** `description` is the sole optional Document scalar; Client documents may be empty. `LocalDate` maps to SQL `DATE`. No expiration-in-the-future constraint is added, so the persistence mapping does not preclude expired Documents.

### 8.3 Cascades, collection removal and optimistic locking

- **VERIFIED:** `Client.documents` includes `CascadeType.REMOVE` and does not set `orphanRemoval=true`; the DB FK independently specifies delete cascade. `deletingClientThroughJpaDeletesAssociatedDocuments` and `databaseOnDeleteCascadeDeletesDocumentsWhenClientIsDeletedOutsideJpa` both pass.
- **VERIFIED:** `Client.removeDocument` only removes from the in-memory collection; it does not null the owning side. `removingDocumentFromCollectionDoesNotDeleteOrTransferIt` flushes and reloads and confirms the row remains persisted with its original owner.
- **VERIFIED:** Both entity mappings use `@Version` and non-null version columns. Passing tests verify version increments following entity updates and stale-update rejection for Client and Document. These tests establish entity-level optimistic locking; they do not test T2.6 service/header/transaction contract, which is outside this task.

### 8.4 Equality, hash stability and lazy associations

- **VERIFIED:** Both entities compare by non-null generated ID, return false for distinct unsaved entities, and use a constant class hash code that does not change when an ID is assigned. The two passing equality tests check unsaved inequality and hash equality across persistence.
- **VERIFIED:** `Client.toString()` omits `documents`; `Document.toString()` omits `client`. The persistence tests check each association is not loaded before and remains not loaded after equals/hashCode/toString checks, using `PersistenceUnitUtil.isLoaded`.

### 8.5 Migration sequence and Hibernate validation

- **VERIFIED:** V1 is comment-only. On an empty H2 schema, focused test logs show Flyway applied migration 1 (`baseline`) followed by migration 2 (`create clients and documents`) and reached v2. The test asserts version 2 and the presence of both business tables.
- **VERIFIED:** `application.yml` sets `spring.jpa.hibernate.ddl-auto: validate`; `DomainPersistenceTest` explicitly pins the same setting and enables Flyway. The test Spring context starts against the migrated embedded H2 database, which is evidence that Hibernate validation passed for these mappings/schema.
- **NOT AVAILABLE:** This execution did not run Flyway or Hibernate validation against a real PostgreSQL instance. PostgreSQL compatibility is not verified or claimed.

### 8.6 Repositories, pagination and change scope

- **VERIFIED — source:** `ClientRepository` declares `existsByTaxIdentifier`, `findByTaxIdentifier`, `lockById` with `OPTIMISTIC_FORCE_INCREMENT`, and inherits `findAll(Pageable)` from `JpaRepository`. `DocumentRepository` declares `existsByNumber`, `findByNumber`, `findByClientId(Long, Pageable)`, and `findByIdAndClientId`.
- **VERIFIED — limitation:** Spring Data repository parsing and application context initialization succeeded, and persistence operations exercised the base repositories. The T2.1 persistence suite does not execute each declared derived query or assert a page's content, totals, boundaries, or sort order. `Pageable` is present in the source signature, but runtime pagination behavior is not independently established here; pagination behavior is assigned to T2.8 by the implementation plan.
- **VERIFIED:** Current status reports the expected T2.1 domain, repository, V2 migration, persistence test and task-report paths, plus a tracked change outside T2.1: `.opencode/agents/reviewer.md`. Its exact diff is deletion of one blank line. No authorization for that unrelated file change was found in the inspected T2.1 decision record.

## 9. Findings and severity

| ID | Severity | Finding | Evidence / consequence |
|---|---|---|---|
| T-F01 | Minor | Repository derived-query and pageable behavior is not directly exercised by the new persistence suite. | **VERIFIED:** Source signatures and repository context startup are present; no test asserts `findBy...` results or page slicing/sorting. Add behavioral repository/page tests in the appropriate authorized task if required. |
| T-F02 | Minor | The current worktree contains an unexplained out-of-scope edit to `.opencode/agents/reviewer.md`. | **VERIFIED:** Current diff removes a blank line and is outside T2.1. **NOT AVAILABLE:** No T2.1 authorization for this edit was found. **RECONSTRUCTED:** Developer report says the edit was already present before implementation; this execution had no pre-implementation snapshot to independently confirm that timing, so attribution remains unresolved. |
| T-F03 | Info | PostgreSQL compatibility is unverified. | **VERIFIED:** H2 only was used in the executed tests; a real PostgreSQL instance was not available/used in this execution. |
| T-F04 | Info | Database `NOT NULL` constraints are source/schema-validated but not each behaviorally exercised with explicit null inserts. | **VERIFIED:** Mapping and migration declare required fields; H2 schema validation passes. The database null-rejection behavior is not separately asserted by this test class. |

**VERIFIED:** No persistence test failed, and no Critical or Major implementation defect was observed in the checks performed. Findings T-F01 and T-F02 remain outstanding; this is a Tester verification verdict, not Tech Lead acceptance.

## 10. Decisions and reasoning

- **VERIFIED:** The table-level unique constraints and cross-Client duplicate test support the approved global uniqueness requirements for Client tax identifiers and Document numbers.
- **VERIFIED:** No orphan-removal behavior is configured, and both JPA/database cascade routes are covered by tests; collection removal is separately tested as non-deleting.
- **VERIFIED:** Tests and source inspection support generated-ID equality/hash stability and no lazy association initialization for the tested loaded entities.
- **VERIFIED:** H2 migration execution and `ddl-auto=validate` are demonstrated. **PENDING VERIFICATION:** PostgreSQL migration and validation must be tested against a real PostgreSQL instance before asserting compatibility.
- **VERIFIED:** After independent inspection and both test runs completed, the Developer report was compared. Developer-reported aggregate result (54 passed) and persistence class result (13 passed) agree with this execution. Developer's description of persistence behavior aligns with inspected source/tests. The Developer report additionally says the `.opencode/agents/reviewer.md` modification predated implementation; **NOT AVAILABLE:** this Tester had no prior working-tree snapshot to independently verify that timing. No other result discrepancy was found.
- **RECONSTRUCTED:** Current T2.1 source changes are confined to the expected domain, repository, migration and persistence-test paths, with task reports under `agent-reports/T2.1`; this follows the current `git status` because the files are untracked relative to HEAD. This does not establish the historical author or approval of every untracked artifact.

## 11. Final status

**PASS WITH MINOR FINDINGS — independent H2 verification passed; repository paging behavior and out-of-scope diff attribution remain open.**

**VERIFIED:** Full `clean verify` passed (54/54, no failures/errors/skips); focused persistence test passed (13/13, no failures/errors/skips). Entity mappings, H2 migration sequence, schema validation, uniqueness, FK rejection, cascade, collection-removal behavior, optimistic locking and lazy-loading/equality behavior are supported by source inspection and the named tests above.

**VERIFIED:** This report does not approve T2.1. Only the human Tech Lead may make final acceptance; no T2.1 acceptance decision was found in the decision log at this verification point.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Exercise derived repository queries and `Pageable` behavior, including deterministic sorting, in authorized repository tests; endpoint pagination acceptance remains assigned to T2.8.
2. **PENDING VERIFICATION:** Establish the origin/authorization status of the `.opencode/agents/reviewer.md` blank-line deletion; do not infer it was caused by T2.1 or authorized based only on current status.
3. **PENDING VERIFICATION:** Test the migration and `ddl-auto=validate` against a real PostgreSQL instance before making a PostgreSQL compatibility claim.
4. **PENDING VERIFICATION:** Add direct null-insert checks if explicit behavioral coverage of each database `NOT NULL` constraint is required.
5. **PENDING VERIFICATION:** Independent Reviewer audit and human Tech Lead acceptance remain outstanding.

## 13. Handoff to next agent

- **To the Reviewer:** Audit `Client`, `Document`, both repositories, V2 and `DomainPersistenceTest` independently before reading other agent conclusions; assess query coverage and the unresolved out-of-scope working-tree difference.
- **To the Tech Lead:** Review this PASS-with-findings result, decide how to handle T-F01/T-F02, and make the final acceptance decision. No acceptance is claimed here.

---
**Report status:** Created for `T2.1-EXEC-003-TST-01`. The primary report was absent before creation; no prior Tester report was overwritten or archived.
