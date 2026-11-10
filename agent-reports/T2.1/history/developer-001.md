# Developer Report — T2.1 Domain Model and Persistence

## 1. Task ID

**VERIFIED:** T2.1 — Implement domain model and persistence, as authorized by the direct Tech Lead tasking for this execution.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.1-EXEC-002-DEV-01` |
| Date | 2026-11-09 |
| Supersedes | None; no prior Developer primary report was present when this execution began |
| History archive | Not required; no previous primary report to archive |

**VERIFIED:** The task execution identifier is from the Tech Lead tasking. The project date is 2026-11-09; Maven output recorded the final build finish at `2026-11-09T22:27:05` local environment time.

## 3. Agent role and model

- **VERIFIED — Role:** Developer — senior Java and Spring Boot developer.
- **VERIFIED — Model:** `gpt-6-luna` (exact model ID `opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED:** The Tech Lead directly approved the T2.1 Architect proposal, with the task-specific technical adjustments, and authorized this implementation. The approved scope implemented here was:

- `Client` and `Document` JPA entities, including generated IDs, approved fields and lengths, lazy relationship, required ownership, and `@Version` fields.
- `ClientRepository` and `DocumentRepository` with the approved proposal Section 10.9 methods.
- Flyway migration `V2__create_clients_and_documents.sql`, without modifying V1.
- Focused H2 persistence tests for migration/schema validation, uniqueness, foreign-key integrity, cascade and collection-removal semantics, optimistic locking, and entity equality behavior.

**VERIFIED:** No controllers, services, DTOs, mappers, security changes, Docker files, React files, requirements changes, plan changes, or decision-log changes were made. No commit or push was performed.

## 5. Files inspected

**VERIFIED:** The following artifacts were read for implementation context and scope:

- `agent-reports/README.md`
- `agent-reports/T2.1/architect-proposal.md`
- `agent-reports/decisions.md`
- `implementation-plan.md`
- `requirements-decisions.md`
- `pom.xml`
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/V1__baseline.sql`
- `src/main/resources/application-dev.yml`
- `src/main/java/com/humanit/challenge/HumanItChallengeApplication.java`
- `src/test/java/com/humanit/challenge/config/StartupTestHarness.java`
- `src/test/java/com/humanit/challenge/HumanItChallengeApplicationTests.java`
- `src/test/java/com/humanit/challenge/config/DefaultDevProfileStartupTest.java`
- Existing source/test file listings and the generated Surefire report for the new persistence test.

## 6. Files changed

**VERIFIED:** Files created by this execution:

| File | Change |
|---|---|
| `src/main/java/com/humanit/challenge/domain/Client.java` | JPA Client entity, named tax-identifier uniqueness, lazy documents association, cascade operations, version, association helpers, ID-based equality and association-excluding `toString()` |
| `src/main/java/com/humanit/challenge/domain/Document.java` | JPA Document entity, named number uniqueness, lazy required owning Client association, version, immutable ownership assignment and safe equality/string representation |
| `src/main/java/com/humanit/challenge/repository/ClientRepository.java` | `JpaRepository` plus approved uniqueness lookup and optimistic-force-increment locking query |
| `src/main/java/com/humanit/challenge/repository/DocumentRepository.java` | `JpaRepository` plus approved number/client-scoped queries and pagination query |
| `src/main/resources/db/migration/V2__create_clients_and_documents.sql` | Creates both tables, required columns, unique constraints, cascading FK and `client_id` index |
| `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java` | 13 focused persistence tests |
| `agent-reports/T2.1/developer-report.md` | This execution report |

**VERIFIED:** `V1__baseline.sql`, `pom.xml`, YAML configuration, requirements, plan and `agent-reports/decisions.md` were not changed. A `git status --short` captured before implementation already showed a modified `.opencode/agents/reviewer.md`; this execution did not modify that file. The final status also shows that file as modified and outside this task's changes.

## 7. Commands executed

**VERIFIED:** Commands run in `C:\Users\pedro\human-it-challenge`:

| Command | Actual result |
|---|---|
| `git status --short` | Before implementation, showed `.opencode/agents/reviewer.md` modified and the existing `agent-reports/T2.1/` directory untracked. Neither was altered by this task except creation of this Developer report within that directory. |
| `.\mvnw.cmd -Dtest=DomainPersistenceTest test` | First targeted run failed: 12 tests, 2 assertion failures and 1 error. The test-harness issues were the exception-root assertion and an H2 query against Flyway's quoted table identifier; they were corrected. |
| `.\mvnw.cmd -Dtest=DomainPersistenceTest test` | Rerun after those corrections passed: 12 tests, 0 failures, 0 errors, 0 skipped. One further equality/lazy-association test was then added. |
| `.\mvnw.cmd clean verify` | **BUILD SUCCESS**, completed at `2026-11-09T22:27:05`; 54 tests run, 0 failures, 0 errors, 0 skipped. The new persistence class ran 13 tests, all passing. |
| `git diff --check` | No output; no whitespace errors reported. |
| `git status --short` | Confirmed the expected new T2.1 domain, repository, migration and test paths, the T2.1 reports directory, and the unrelated existing `.opencode/agents/reviewer.md` modification. |

## 8. Evidence and observed results

- **VERIFIED:** `target/surefire-reports/com.humanit.challenge.repository.DomainPersistenceTest.txt` records `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0` for the final clean verification.
- **VERIFIED:** The final Maven output records aggregate `Tests run: 54, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS` for `.\mvnw.cmd clean verify`.
- **VERIFIED:** The persistence test explicitly sets `spring.jpa.hibernate.ddl-auto=validate` and enables Flyway. Its successful context start demonstrates Hibernate validation against the Flyway-managed H2 schema for this test run.
- **VERIFIED:** Flyway logs validated two migrations, applied V1 and V2 on H2, and reported schema version `v2`; the test asserts migration version 2 and the presence of both business tables.
- **VERIFIED:** Database tests demonstrate global uniqueness of `tax_identifier` and `number` (the duplicate Document number is placed under a different Client), reject a Document with a nonexistent `client_id`, and verify client deletion through JPA as well as database `ON DELETE CASCADE` for direct SQL deletion.
- **VERIFIED:** Collection-removal test removes a Document from `Client.documents`, flushes and reloads, then confirms the Document still exists and remains owned by its original Client. No `orphanRemoval` is configured.
- **VERIFIED:** Tests verify version increments and stale-update failures for both entities. Equality tests cover unsaved distinct objects, generated-ID equality/hash stability, and ensure equality/string conversion does not initialize the lazy association.
- **VERIFIED:** Source mappings define named uniqueness through `@Table(uniqueConstraints = ...)`; the unique columns do not use `@Column(unique = true)`. No `Document.type` field is present.
- **NOT AVAILABLE:** No real PostgreSQL instance was used in this execution. The PostgreSQL migration compatibility claim remains unverified; only H2 was tested.
- **PENDING VERIFICATION:** Independent Tester verification and independent Reviewer audit have not yet occurred in this Developer execution. No final Tech Lead acceptance of T2.1 is claimed.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-D01 | Info | The first targeted test run exposed test-related issues; the test assertions/query were corrected and the rerun plus final full clean verification passed. No unresolved implementation defect was identified from those failures. |
| F-D02 | Info | PostgreSQL integration has not been performed against a real PostgreSQL instance; compatibility remains unverified and is not claimed. |
| F-D03 | Info | Maven emitted a warning during dependency metadata lookup (HTTP 401 for the Flyway community database-support metadata source); the dependencies needed for this build were available and the full verification completed successfully. |
| F-D04 | Info | The final working tree reports a modified `.opencode/agents/reviewer.md` outside this execution's scope; it was observed before implementation and left untouched. |

**VERIFIED:** No Critical or Major findings were observed during this implementation and its H2 verification. This is not an independent code-review conclusion.

## 10. Decisions and reasoning

- **VERIFIED:** Both uniqueness guarantees are represented by named table-level unique constraints and matching named migration constraints, without duplicate `@Column(unique = true)` annotations, per Tech Lead adjustment A.
- **VERIFIED:** Entity equality returns true for matching non-null IDs only; distinct unsaved entities compare unequal. Constant per-class hash codes remain unchanged when generated IDs are assigned. Equality uses the ID and `toString()` omits both associations to avoid lazy-loading side effects.
- **VERIFIED:** `Document.client` is the owning `@ManyToOne` side. `Client.addDocument` sets ownership and refuses reassignment to a different Client; `removeDocument` only removes the in-memory collection entry and does not clear the owning Client or delete a row. This and the absence of `orphanRemoval` preserve explicit-delete-only semantics for Documents.
- **VERIFIED:** Client deletion combines JPA `CascadeType.REMOVE` with database `ON DELETE CASCADE`, as instructed. Tests cover JPA deletion and database-side cascade independently.
- **VERIFIED:** The V2 schema uses approved lengths and types, named unique constraints, a required FK with the approved constraint name and an index on `documents.client_id`. V1 remains unchanged.
- **VERIFIED:** Repositories implement all Section 10.9 methods, including the parent `OPTIMISTIC_FORCE_INCREMENT` query and client-scoped document queries.
- **VERIFIED:** The test class pins schema validation to `validate` instead of relying on JPA schema generation; Flyway owns schema creation in this test.

## 11. Final status

**IMPLEMENTATION COMPLETE — H2 PERSISTENCE VERIFICATION PASSED; INDEPENDENT VERIFICATION, REVIEW AND TECH LEAD ACCEPTANCE PENDING.**

**VERIFIED:** All 54 tests passed in `.\mvnw.cmd clean verify`. **PENDING VERIFICATION:** This result is the Developer's run only and is not a Tester report, Reviewer audit, or Tech Lead acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Tester to perform independent T2.1 verification before reading the Developer conclusions, following the workflow in `agent-reports/README.md`.
2. **PENDING VERIFICATION:** Reviewer to perform the independent audit before reading the Architect, Developer or Tester reports.
3. **PENDING VERIFICATION:** Human Tech Lead to decide final T2.1 acceptance after the required verification and review workflow.
4. **NOT AVAILABLE:** PostgreSQL integration evidence is absent; real PostgreSQL verification remains outside this execution.

## 13. Handoff to next agent

- **To the Tester:** Independently inspect and verify T2.1 against the authorized requirements and implementation. Do not consult this report before completing independent verification. H2 migration, `ddl-auto: validate`, both uniqueness constraints, FK rejection, JPA/database cascade, no orphan deletion and optimistic locking have dedicated persistence tests to examine.
- **To the Reviewer:** Independently audit the implementation before reading the Architect, Developer or Tester reports, as required by the reporting workflow.
- **To the Tech Lead:** Final acceptance remains with the human Tech Lead; this report makes no acceptance decision.

---
**Report status:** Created for `T2.1-EXEC-002-DEV-01`. No prior Developer report existed to archive. No other agent's report or the decision log was modified.
