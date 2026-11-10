# Tester Report — T2.1 Independent Verification After Polish

## 1. Task ID

**VERIFIED:** T2.1 — independent verification after polish, specifically corrections for Reviewer findings R-F01, R-F02 and R-F03.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.1-EXEC-006-TST-02` |
| Date | 2026-11-09 |
| Predecessor | `T2.1-EXEC-003-TST-01` |
| Previous primary report archive | `agent-reports/T2.1/history/tester-001.md` |

**VERIFIED:** The prior primary Tester report was moved unchanged to the role-specific first history slot before this report was created. The pre-existing history directory contained `developer-001.md` and no Tester archive.

## 3. Agent role and model

- **VERIFIED:** Role — Independent Senior Java/Spring Boot QA Engineer.
- **VERIFIED:** Model — `gpt-6-luna` (exact model ID `opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED:** The human tasking authorizes independent verification of corrections for R-F01, R-F02 and R-F03, regression runs, migration/schema checks, and inspection of Git scope. Production code, test source, other agents' reports, decisions, and `.opencode/agents/reviewer.md` were not to be modified. No commit, push or T2.1 approval was authorized.

## 5. Files inspected

**VERIFIED — before reading any agent conclusions:**

- `agent-reports/README.md`
- `implementation-plan.md`
- `requirements-decisions.md`
- `pom.xml`
- `src/main/java/com/humanit/challenge/domain/Client.java`
- `src/main/java/com/humanit/challenge/domain/Document.java`
- `src/main/java/com/humanit/challenge/repository/ClientRepository.java`
- `src/main/java/com/humanit/challenge/repository/DocumentRepository.java`
- `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`
- `src/test/java/com/humanit/challenge/domain/DocumentAssociationTest.java`
- `src/main/resources/db/migration/V1__baseline.sql`
- `src/main/resources/db/migration/V2__create_clients_and_documents.sql`
- `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`
- Git status/diff and direct Maven test output.

**VERIFIED — only after source inspection and both requested test runs completed:** `agent-reports/T2.1/developer-report.md`, `agent-reports/T2.1/reviewer-report.md`, and predecessor `agent-reports/T2.1/tester-report.md` were read for comparison.

## 6. Files changed

- **VERIFIED:** No production source, test source, migration, requirements, planning file, decision log, Developer report, Reviewer report or `.opencode/agents/reviewer.md` was changed by this execution.
- **VERIFIED:** `agent-reports/T2.1/tester-report.md` was replaced by this execution's report after preserving the predecessor unchanged at `agent-reports/T2.1/history/tester-001.md`.
- **VERIFIED:** Maven generated build output under `target/`; no tracked or untracked changes outside those already observed were reported after the test runs.

## 7. Commands executed

All commands ran from `C:\Users\pedro\human-it-challenge`.

| Command | Observed result |
|---|---|
| `git status --short; git diff --` | **VERIFIED:** `.opencode/agents/reviewer.md` is modified; T2.1 implementation/report paths are untracked. Diff is removal of a blank line from `.opencode/agents/reviewer.md`. |
| `.\mvnw.cmd clean verify` | **VERIFIED:** `BUILD SUCCESS`; 61 tests, 0 failures, 0 errors, 0 skipped. `DomainPersistenceTest`: 19; `DocumentAssociationTest`: 1. |
| `.\mvnw.cmd -Dtest=DomainPersistenceTest,DocumentAssociationTest test` (initial PowerShell invocation, without quoting the comma argument) | **VERIFIED:** PowerShell parser rejected the command before Maven ran (`Missing argument in parameter list`). This is a shell parsing issue, not a test result. |
| `.\mvnw.cmd "-Dtest=DomainPersistenceTest,DocumentAssociationTest" test` (corrected PowerShell invocation) | **VERIFIED:** `BUILD SUCCESS`; 20 tests, 0 failures, 0 errors, 0 skipped (19 persistence + 1 association). |
| `git status --short; git diff --` | **VERIFIED:** Same unrelated `.opencode/agents/reviewer.md` blank-line deletion and T2.1 untracked paths remained; no test execution created an additional source change. |

**VERIFIED:** The focused command used a quoted `-Dtest` argument so PowerShell passed the comma-separated test list to Maven.

## 8. Evidence and observed results

### R-F01 — Seven custom repository methods

- **VERIFIED (`ClientRepository.java`, `DomainPersistenceTest.java`, passing Surefire output):** `existsByTaxIdentifier` and `findByTaxIdentifier` return expected existing/missing results. The test flushes/clears and checks the stored tax identifier and Client version are unchanged by lookup operations.
- **VERIFIED (`ClientRepository.java`, `DomainPersistenceTest.java`, test output):** `lockById` returns a present Client and an empty result for a missing ID. It is called inside a `TransactionTemplate` with test-level transaction disabled; after transaction completion, a fresh lookup confirms the Client version increased and its tax identifier and email did not change.
- **VERIFIED (`DocumentRepository.java`, `DomainPersistenceTest.java`, test output):** `existsByNumber` and `findByNumber` cover existing and missing values. `findByIdAndClientId` covers the correct owner, a different Client owner, and missing ID. `findByClientId` returns documents only for the requested Client and an empty page for a missing owner.
- **VERIFIED (`DomainPersistenceTest.java`, Hibernate SQL printed during test):** `findByClientId` is run with `Sort.by(ASC, "id")` for page 0 and page 1. Expected page content is two then one document, with total count 3 on both pages. Hibernate emitted `order by d1_0.id`; page output follows the generated ascending IDs. This verifies the method with the caller-provided ascending sort, not a repository-level default sort when no sort is supplied.
- **VERIFIED:** The repository class now has 19 passing tests; all seven specified methods were behaviorally invoked in the inspected test source. The requested persisted values/version checks passed.

### R-F02 — Relationship helper behavior

- **VERIFIED (`DocumentAssociationTest.java`, test output):** `Document.assignClient(null)` throws `NullPointerException` with `client must not be null`.
- **VERIFIED (`DomainPersistenceTest.java`, source and test output):** An already assigned Document rejects another Client identity; the test includes distinct transient Client objects. Same persisted Client identity is accepted when a loaded Document's client is an uninitialized Hibernate proxy and the detached persisted Client has the same ID. The original proxy remains uninitialized after association.
- **VERIFIED (`Document.java`):** When there is no current owner, a non-null transient Client can be associated. For an already owned Document, two different transient identities are rejected rather than treated as equal based on null IDs; reusing the same object is allowed by the reference-identity guard.
- **VERIFIED (`Client.java`, `DomainPersistenceTest.java`):** A persisted Document removed from the in-memory collection retains its owning-side Client and database row after flush/reload. Removing null or an absent Document returns false; removing an associated transient Document is safe and does not insert a row. No `orphanRemoval` attribute is set. The mapping's cascade types retain explicit parent-delete cascade; tests separately demonstrate expected JPA and database cascade deletion when deleting the Client.
- **VERIFIED:** Source documentation explains that service-layer deletion pairs collection removal with explicit `DocumentRepository.delete(document)` in the same transaction, and collection-preserving updates must not call removal.

### R-F03 — Immutable ownership

- **VERIFIED (`Document.java`, `DomainPersistenceTest.java`):** The `Document.client` field's `@JoinColumn` sets `updatable = false`. A transfer attempt to another Client is rejected.
- **VERIFIED (Hibernate SQL output from both test executions):** Scalar Document changes generated an `UPDATE documents SET description=?,expiration_date=?,number=?,version=? WHERE id=? AND version=?`; `client_id` is absent from the update list.
- **VERIFIED (`DomainPersistenceTest.java`, test output):** After the scalar update and persistence-context clear, a SQL query confirms the database `client_id` remains the original owner and differs from the other Client ID.

### Regression, migration, schema validation and test count

- **VERIFIED (`.\mvnw.cmd clean verify` output):** Full clean verify succeeded with 61 tests and zero failures/errors/skips.
- **VERIFIED (corrected focused command output):** Targeted persistence and association run succeeded with 20 tests and zero failures/errors/skips.
- **VERIFIED (`DomainPersistenceTest.java`, Flyway output):** The persistence test sets `spring.jpa.hibernate.ddl-auto=validate` and enables Flyway. The focused run applied V1 then V2 on an empty H2 schema and reached version 2; the Hibernate persistence context initialized successfully. The clean full run also validated two migrations and reported schema at version 2. Flyway V2 asserts the two business tables exist.
- **VERIFIED (`application.yml`):** Common Hibernate configuration is `ddl-auto: validate`.
- **NOT AVAILABLE:** Neither requested command was run against PostgreSQL. No live PostgreSQL migration or Hibernate validation claim is made.

### Git scope

- **VERIFIED (`git status --short`, `git diff --`):** `.opencode/agents/reviewer.md` has an unrelated tracked blank-line deletion and must be excluded from any T2.1 commit. This execution did not edit or restore it. Domain/repository/migration/test and task-report paths appear untracked relative to the current Git `HEAD`.

## 9. Findings and severity

| Finding | Severity | Independent disposition |
|---|---|---|
| R-F01 — repository behavior | Resolved for this verification | **VERIFIED:** All seven requested methods have behavioral coverage for existing/missing values; owner filtering, page content/counts, ascending-ID ordering when supplied and post-commit force increment are supported by source/tests/output. The tests do not query a page beyond the last non-empty page and do not reverse the insertion order to make a sorting regression more discriminating; that is a residual test-strength limitation, not a failing observed behavior. |
| R-F02 — relationship helpers | Resolved for this verification | **VERIFIED:** Null, persistent and transient identity handling, proxy non-initialization, rejected transfers, collection-only removal, transient removal safety, absent orphan removal and expected cascade routes are supported by tests/source. |
| R-F03 — immutable ownership | Resolved for this verification | **VERIFIED:** Non-updatable join mapping, absence of `client_id` from generated scalar UPDATE, persisted owner preservation and transfer rejection are supported by tests/source/output. |
| `.opencode/agents/reviewer.md` | Minor / out of scope | **VERIFIED:** One blank-line deletion remains in the working tree; exclude this file from a T2.1 commit. Its historical author and timing were not independently established here. |
| PostgreSQL execution | Info / not available | **NOT AVAILABLE:** No live PostgreSQL was exercised. H2 results do not establish live PostgreSQL compatibility. |

## 10. Decisions and reasoning

- **VERIFIED:** Corrections for R-F01, R-F02 and R-F03 are independently supported by implementation inspection and passing H2 behavioral tests. The suite exercises post-commit force increment rather than relying only on source annotations.
- **VERIFIED:** Page 0 and the partial page 1 establish the tested pagination split and totals; `order by d1_0.id` is visible in Hibernate's SQL. A beyond-last page and reverse-insertion sort sensitivity are not covered, so those are disclosed rather than inferred.
- **VERIFIED:** Current `.opencode/agents/reviewer.md` diff is unrelated to authorized scope and remains untouched. The request explicitly says it must be excluded from the T2.1 commit.
- **VERIFIED:** Full regression evidence is H2-only. No PostgreSQL claim is made.
- **VERIFIED — comparison after independent verification:** The Developer report's 61-test clean build, 19 persistence tests, R-F01–R-F03 implementation descriptions, H2/V1-to-V2/schema validation claims and generated SQL description agree with independent output/source inspection. The Reviewer report and predecessor Tester report are from before polish and their open R-F01, R-F02 and R-F03 statements are stale for the current source: this execution directly verifies those three corrections. The Tester predecessor had recorded 54 full-suite tests and 13 persistence tests; this run has 61 and 19 respectively, matching the later Developer report and expanded tests. No contradictory result was found. The possible stronger coverage for page-beyond-end/reverse-insertion remains a limitation despite the Developer's broad pagination-boundary coverage description.
- **NOT AVAILABLE:** No historical snapshot in this execution establishes who introduced the `.opencode/agents/reviewer.md` edit or when; the Developer report's statement that it predated implementation is not independently verifiable from my current Git inspection.
- **VERIFIED:** This report does not accept or approve T2.1; final acceptance belongs only to the human Tech Lead.

## 11. Final status

**PASS WITH FINDINGS** — Independent verification of R-F01, R-F02 and R-F03 passed; both requested Maven test runs passed. Residual findings: exclude `.opencode/agents/reviewer.md` from any T2.1 commit; page-beyond-end and reverse-insertion sort sensitivity are not directly tested; live PostgreSQL execution is not available.

**VERIFIED:** `clean verify` passed 61/61; focused persistence/association tests passed 20/20. No Critical or Major regression was observed in the authorized checks. This is a Tester result only, not T2.1 approval.

## 12. Outstanding issues

1. **VERIFIED:** Exclude `.opencode/agents/reviewer.md` from any T2.1 staging or commit; do not modify it under this task.
2. **PENDING VERIFICATION:** If stronger pageable boundary/sort regression coverage is required, add a beyond-last page case and seed records so insertion order differs from requested ID order in a separately authorized test change.
3. **PENDING VERIFICATION:** Run migrations and `ddl-auto=validate` against a live PostgreSQL instance before asserting PostgreSQL compatibility.
4. **PENDING VERIFICATION:** Independent Reviewer audit of this post-polish execution and human Tech Lead acceptance remain outstanding.

## 13. Handoff to next agent

- **To the Reviewer:** Independently audit current source/tests and this execution's scope; review residual pagination test-strength and Git-scope notes. Do not treat this report as acceptance.
- **To the Tech Lead:** Consider this `PASS WITH FINDINGS` report alongside the Reviewer audit, ensure `.opencode/agents/reviewer.md` is excluded from any T2.1 commit, and make any final acceptance decision. No T2.1 approval is claimed.

---
**Report status:** Written for `T2.1-EXEC-006-TST-02`; predecessor `T2.1-EXEC-003-TST-01` archived at `agent-reports/T2.1/history/tester-001.md`.
