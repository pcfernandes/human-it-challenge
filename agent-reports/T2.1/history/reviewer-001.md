# Reviewer Report — T2.1 Independent Architecture and Code Audit

## 1. Task ID

**VERIFIED:** **T2.1 — Domain model, mappings and database schema** (Phase 2), audited against `implementation-plan.md` Section 6 (T2.1), `requirements-decisions.md` Sections 2, 4, 5, 6, 7, 8, 9, 12 and 20, and the Tech Lead review instructions for execution `T2.1-EXEC-004-REV-01`.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.1-EXEC-004-REV-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None — first Reviewer execution for T2.1 |
| History archive | Not required — **VERIFIED** via `glob` (`agent-reports/T2.1/**/*`) that no prior `agent-reports/T2.1/reviewer-report.md` existed prior to this execution |

## 3. Agent role and model

- **VERIFIED — Role:** Independent Senior Java/Spring Boot Code Reviewer (`reviewer`).
- **VERIFIED — Model:** `gemini-4-argon` (exact model ID `opencode-go/gemini-4-argon`).
- **VERIFIED — Independence compliance:** Source code, Flyway migrations, repository interfaces, tests, Surefire XML/TXT artifacts and Git working-tree state were inspected independently **before** reading `agent-reports/T2.1/architect-proposal.md`, `agent-reports/T2.1/developer-report.md` and `agent-reports/T2.1/tester-report.md`.

## 4. Authorized scope

**VERIFIED:** Perform an independent architecture and code review of the T2.1 domain model and persistence implementation covering:
1. `Client` and `Document` entity design.
2. JPA relationship ownership and cardinality.
3. Cascade and `orphanRemoval` behavior.
4. Immutable `Document` ownership.
5. Global uniqueness constraints (`taxIdentifier` and `Document.number`).
6. `@Version` and optimistic locking.
7. `equals`/`hashCode` with database-generated IDs.
8. Lazy-loading and serialization risks.
9. Flyway V2 schema and indexes.
10. H2/PostgreSQL SQL portability (distinguishing static SQL inspection from live PostgreSQL execution).
11. Repository query correctness and test coverage (`findByTaxIdentifier`, `findByNumber`, `findByClientId`, `findByIdAndClientId`, `lockById`, `Pageable` and deterministic sorting).
12. Test quality and coverage in `DomainPersistenceTest`.
13. Consistency with approved requirements and implementation plan.
14. Scope compliance and unrelated working-tree changes (`.opencode/agents/reviewer.md`).

**Restrictions enforced (VERIFIED):** No production code, test files, migrations, configurations, requirements, implementation plan, `agent-reports/decisions.md` or other agents' reports were modified. No Git commit, revert or push was performed.

## 5. Files inspected

**VERIFIED — Inspected independently BEFORE reading T2.1 agent reports:**

| File / Artifact | Purpose of inspection |
|---|---|
| `agent-reports/README.md` | Mandatory 13-section format, evidence markers, independence and history rules |
| `implementation-plan.md` | Phase 2 / T2.1 scope, acceptance criteria and task boundaries (T2.1–T2.8) |
| `requirements-decisions.md` | Approved domain model (§2), update/deletion semantics (§4–§5), validation/integrity (§6), transactions (§7), concurrency (§8, §20), pagination (§9), persistence (§12) |
| `agent-reports/decisions.md` | Approved decision log (`D-01` through `D-09`; Phase 1 closure and Phase 2 / T2.1 authorization) |
| `pom.xml` | Dependency baseline (Java 21, Spring Boot 3.5.16, Spring Data JPA, Flyway, H2, PostgreSQL) |
| `src/main/resources/application.yml` | Common JPA (`ddl-auto: validate`), Flyway and pagination settings |
| `src/main/resources/application-dev.yml` | H2 in-memory configuration for `dev` profile |
| `src/main/resources/application-prod.yml` | Externalized PostgreSQL datasource configuration for `prod` profile |
| `src/main/resources/db/migration/V1__baseline.sql` | Version 1 baseline migration (unmodified) |
| `src/main/resources/db/migration/V2__create_clients_and_documents.sql` | T2.1 business schema migration (`clients`, `documents`, constraints, FK cascade, index) |
| `src/main/java/com/humanit/challenge/domain/Client.java` | `Client` JPA entity mapping, lifecycle helpers, `equals`/`hashCode`/`toString` |
| `src/main/java/com/humanit/challenge/domain/Document.java` | `Document` JPA entity mapping, ownership guard, `equals`/`hashCode`/`toString` |
| `src/main/java/com/humanit/challenge/repository/ClientRepository.java` | `Client` Spring Data JPA repository and `OPTIMISTIC_FORCE_INCREMENT` locking query |
| `src/main/java/com/humanit/challenge/repository/DocumentRepository.java` | `Document` Spring Data JPA repository, client-scoped lookup and pageable query |
| `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java` | 13 `@DataJpaTest` persistence integration tests on H2 with Flyway and `ddl-auto=validate` |
| `src/test/java/com/humanit/challenge/config/SourceScanAuditTest.java` | Phase 1 source-scan guard tests |
| `src/test/java/com/humanit/challenge/config/StartupTestHarness.java` | Phase 1 startup test harness |
| `.opencode/agents/reviewer.md` | Inspected to verify the unrelated working-tree modification |
| `target/surefire-reports/*.txt` (all 13 files) & `TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` | Surefire test execution evidence, Hibernate SQL logs and Flyway migration logs |

**VERIFIED — Inspected AFTER forming independent audit findings:**

| File / Artifact | Purpose of inspection |
|---|---|
| `agent-reports/T2.1/architect-proposal.md` | Compare implementation against the proposed T2.1 architecture (`T2.1-EXEC-001-ARCH-01`) |
| `agent-reports/T2.1/developer-report.md` | Compare Developer claims (`T2.1-EXEC-002-DEV-01`) against observed code and test artifacts |
| `agent-reports/T2.1/tester-report.md` | Compare Tester verification (`T2.1-EXEC-003-TST-01`) against independent review findings |

## 6. Files changed

| File | Change |
|---|---|
| `agent-reports/T2.1/reviewer-report.md` | **Created** — this independent Reviewer audit report |

**VERIFIED:** No other files were created or modified by this execution.

## 7. Commands executed

**VERIFIED:** Read-only Git inspection commands permitted by `.opencode/agents/reviewer.md` were executed in `C:\Users\pedro\human-it-challenge`:

| Command | Observed result |
|---|---|
| `git status` | On branch `feature/T2.1-domain-model`. Tracked modified file: `.opencode/agents/reviewer.md`. Untracked paths: `agent-reports/T2.1/`, `src/main/java/com/humanit/challenge/domain/`, `src/main/java/com/humanit/challenge/repository/`, `src/main/resources/db/migration/V2__create_clients_and_documents.sql`, `src/test/java/com/humanit/challenge/repository/`. |
| `git diff` | Shows a 1-line diff in `.opencode/agents/reviewer.md` removing a single blank line after line 16 (`"**/agent-reports/**": allow`). No other tracked files have diffs. |
| `git log --oneline -10` | Confirms `HEAD` is `884c744 Merge pull request #4 from pcfernandes/feature/T1.6-phase1-checkpoint` (`39213d9 docs: complete Phase 1 checkpoint (T1.6)`). |

**Note on Maven execution:** Under `.opencode/agents/reviewer.md`, `bash` is restricted to read-only `git status*`, `git diff*`, `git ls-files*`, `git show*` and `git log*`. Build and test execution evidence was therefore audited directly from the workspace Surefire reports (`target/surefire-reports/*.txt` and `target/surefire-reports/TEST-*.xml`) and cross-checked against the Tester and Developer reports.

## 8. Evidence and observed results

### 8.1 Item-by-item technical review (Items 1–14 and Special Attention A–E)

#### 1. `Client` and `Document` entity design
- **VERIFIED (`Client.java`, `Document.java`):**
  - `Client` maps to table `clients` with fields `id` (`Long`, `GenerationType.IDENTITY`), `firstName` (`VARCHAR(100) NOT NULL`), `lastName` (`VARCHAR(100) NOT NULL`), `taxIdentifier` (`VARCHAR(50) NOT NULL`), `email` (`VARCHAR(255) NOT NULL`), `phoneNumber` (`VARCHAR(50) NOT NULL`), `documents` (`List<Document>`, initialized to `new ArrayList<>()`), and `version` (`Long`, `@Version`).
  - `Document` maps to table `documents` with fields `id` (`Long`, `GenerationType.IDENTITY`), `number` (`VARCHAR(100) NOT NULL`), `description` (`VARCHAR(1000)`, nullable), `expirationDate` (`LocalDate`, `DATE NOT NULL`), `client` (`Client`, `@ManyToOne(fetch = FetchType.LAZY, optional = false)`), and `version` (`Long`, `@Version`).
  - Both entities declare a `protected` no-arg constructor for JPA and a domain constructor for required scalar fields, expose no setters for `id`, `version` or `documents`, and return an unmodifiable view from `Client.getDocuments()` (`Collections.unmodifiableList(documents)`).
  - No unapproved fields (such as an invented `Document.type`) were introduced, matching `requirements-decisions.md` §2.

#### 2. JPA relationship ownership and cardinality
- **VERIFIED (`Client.java` lines 45–48, `Document.java` lines 37–40):**
  - Cardinality is `1 Client : 0..* Document`.
  - `Document.client` is the owning side (`@ManyToOne(fetch = FetchType.LAZY, optional = false)` with `@JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_documents_client"))`).
  - `Client.documents` is the inverse side (`@OneToMany(mappedBy = "client", ...)`).
  - Having the `@ManyToOne` child own the foreign key column is idiomatic JPA and avoids extra join tables or redundant FK update statements.

#### 3 & Special Attention B. Cascade, `orphanRemoval` behavior, and relationship helpers
- **VERIFIED (`Client.java` lines 45–48, 122–136; `Document.java` lines 92–97; `DomainPersistenceTest.java` lines 85–132, 217–228):**
  - `Client.documents` cascades `{CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}` and intentionally leaves `orphanRemoval = false` (default).
  - **Are there unintended database deletes?** **No.** Because `orphanRemoval` is `false`, omitting or removing a `Document` from `Client.documents` does not trigger an implicit SQL `DELETE`. Explicit deletion of a `Client` cascades deletion to its `Document` children via both JPA `CascadeType.REMOVE` (`TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` lines 142–143: `delete from documents where id=? and version=?` followed by `delete from clients where id=? and version=?`) and the database `ON DELETE CASCADE` foreign key (`DomainPersistenceTest.databaseOnDeleteCascadeDeletesDocumentsWhenClientIsDeletedOutsideJpa`).
  - **Can documents be accidentally transferred?** **No via the entity API, with one proxy-equality nuance (Finding R-F02b).** `Document` has no public `setClient(...)`; package-private `assignClient(Client)` throws `IllegalArgumentException("Document ownership cannot be transferred")` when `this.client != null && this.client != client` (verified by `DomainPersistenceTest.associationHelpersPreventOwnershipTransfer`). However, using reference inequality (`this.client != client`) instead of `!this.client.equals(client)` can falsely reject re-associating the *same* persisted `Client` if `this.client` is an uninitialized Hibernate proxy and `client` is an unproxied or merged instance with the same ID. Additionally, `@JoinColumn(name = "client_id", ...)` omits `updatable = false` (Finding R-F03).
  - **Can collection operations cause inconsistent in-memory relationships?** **Yes, if `Client.removeDocument(Document)` is misused without deleting the `Document` (Finding R-F02a).** `Client.removeDocument(Document)` removes the element from `this.documents` in memory without clearing `document.client`. If called without deleting the `Document` row in the same transaction, the in-memory graph becomes inconsistent for the remainder of the persistence context (`client.getDocuments().contains(doc)` is `false` while `doc.getClient() == client` is `true`), and if called on a transient (unsaved) `Document`, `doc.getClient()` remains non-null so the discarded transient instance remains permanently bound to `client`. `removeDocument` is safe when paired with `documentRepository.delete(document)` in the same transaction (where removing from the initialized parent collection is required to prevent `CascadeType.PERSIST` from cancelling the deletion on flush).

#### 4. Immutable `Document` ownership
- **VERIFIED (`Document.java` lines 37–40, 84–97):**
  - `Document` has no `setClient` method; `assignClient(Client)` is package-private and prevents reassigning a `Document` to another `Client`.
  - **Minor hardening noted (Finding R-F03):** `@JoinColumn(name = "client_id", nullable = false, ...)` in `Document.java` does not set `updatable = false`. Consequently, Hibernate includes `client_id=?` in `UPDATE documents SET client_id=?,description=?,expiration_date=?,number=?,version=? WHERE id=? AND version=?` (observed in `TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` lines 126 and 177). Adding `updatable = false` will prevent Hibernate from ever including `client_id` in `UPDATE` SQL.

#### 5. Global uniqueness constraints
- **VERIFIED (`Client.java` lines 21–22, `Document.java` lines 19–20, `V2__create_clients_and_documents.sql` lines 11, 21, `DomainPersistenceTest.java` lines 56–74):**
  - `clients.tax_identifier` is constrained by `uk_clients_tax_identifier UNIQUE (tax_identifier)` in both JPA metadata and Flyway V2 DDL, without duplicate `@Column(unique = true)`.
  - `documents.number` is constrained globally across all clients by `uk_documents_number UNIQUE (number)` in both JPA metadata and Flyway V2 DDL, without duplicate `@Column(unique = true)`.
  - Both constraints are verified by negative persistence tests (`taxIdentifierIsGloballyUnique` and `documentNumberIsGloballyUniqueAcrossDifferentClients`, which confirmed SQLState `23505` / `DataIntegrityViolationException`).

#### 6 & Special Attention C. `@Version` and optimistic locking
- **VERIFIED (`Client.java` lines 50–52, `Document.java` lines 42–44, `ClientRepository.java` lines 20–22, `V2__create_clients_and_documents.sql` lines 10, 20, `DomainPersistenceTest.java` lines 134–172):**
  - **Is `@Version` correctly configured?** **Yes.** Both `Client` and `Document` map `private Long version` annotated with `@jakarta.persistence.Version` and `@Column(name = "version", nullable = false)`, backed by `version BIGINT NOT NULL DEFAULT 0` in `V2__create_clients_and_documents.sql`. Hibernate initializes `version` to `0L` on `INSERT` and generates `WHERE id=? AND version=?` on `UPDATE` and `DELETE` (`TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` lines 126, 136, 142–143, 175, 177).
  - **Are parent-version increments achievable?** **Yes.** `ClientRepository.lockById(@Param("id") Long id)` is annotated with `@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)` and `@Query("select c from Client c where c.id = :id")`. In JPA/Hibernate, acquiring `LockModeType.OPTIMISTIC_FORCE_INCREMENT` on a managed `Client` schedules a forced version increment (`UPDATE clients SET version = ? WHERE id = ? AND version = ?`) at flush/commit time even when no scalar fields on `Client` changed, enabling the T2.6 requirement that every `Document` create, update or delete increments the parent `Client.version` in the same transaction.
  - **Are concurrency assumptions justified?** **Yes, with one test-coverage gap (Finding R-F01).** Entity-level version increment and stale-update rejection (`OptimisticLockException`) are verified for both `Client` and `Document` in `DomainPersistenceTest` (lines 134–172). However, `ClientRepository.lockById(id)` itself is not yet executed in `DomainPersistenceTest` (Finding R-F01), and the HTTP-level expected-version comparison (`X-Client-Version`, `X-Document-Version`, DTO `version` fields, and `409 Conflict` Problem Details mapping) belongs to T2.6.

#### 7. `equals`/`hashCode` with generated IDs
- **VERIFIED (`Client.java` lines 138–153, `Document.java` lines 99–114, `DomainPersistenceTest.java` lines 174–215):**
  - Both entities implement `equals(Object)` using `this == other`, `instanceof` pattern matching (`other instanceof Client that` / `other instanceof Document that`), and `id != null && id.equals(that.getId())`.
  - Using `that.getId()` instead of direct field access (`that.id`) is proxy-safe when `other` is a Hibernate proxy.
  - Two distinct transient (unsaved, `id == null`) instances are never considered equal (`id != null` is `false`), while the same transient instance equals itself (`this == other` is `true`).
  - `hashCode()` returns a constant per class (`Client.class.hashCode()` / `Document.class.hashCode()`), guaranteeing that an entity's hash code remains stable across the `null -> generated ID` persistence transition so `Set`/`Map` collections are not corrupted.

#### 8. Lazy-loading and serialization risks
- **VERIFIED (`Client.java` lines 45–48, 155–166; `Document.java` lines 37–40, 116–125; `DomainPersistenceTest.java` lines 174–215):**
  - Both `@OneToMany` (`Client.documents`) and `@ManyToOne` (`Document.client`) explicitly declare `fetch = FetchType.LAZY`.
  - `toString()`, `equals()` and `hashCode()` on both entities exclude `documents` and `client`. `DomainPersistenceTest` verifies via `entityManagerFactory.getPersistenceUnitUtil().isLoaded(...)` that neither `Client.documents` nor `Document.client` is initialized when `equals`, `hashCode` and `toString` are invoked on a freshly loaded entity.
  - No Jackson annotations are placed on the JPA entities, preserving strict separation between persistence entities and the REST DTO layer planned for T2.2.
  - Note: `spring.jpa.open-in-view` is not yet explicitly disabled in `application.yml` (already tracked as a deferred hardening item in `agent-reports/decisions.md` D-09).

#### 9 & 10 & Special Attention E. Flyway V2 schema, indexes, and H2/PostgreSQL SQL portability
- **VERIFIED (`V2__create_clients_and_documents.sql`, `DomainPersistenceTest.java` lines 46–54, `TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` lines 91–98):**
  - `V1__baseline.sql` is untouched.
  - `V2__create_clients_and_documents.sql` creates `clients` and `documents` with column names, lengths, types and nullability matching the JPA entity mappings.
  - Includes named constraints `uk_clients_tax_identifier`, `uk_documents_number`, and `fk_documents_client FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE CASCADE`.
  - Explicitly creates `CREATE INDEX idx_documents_client_id ON documents (client_id);`, which is essential for PostgreSQL performance on nested document queries and cascading deletes because PostgreSQL does not auto-index foreign key columns.
  - **Distinction between static SQL portability and verified PostgreSQL execution (Special Attention E):**
    - **VERIFIED (H2 execution):** Flyway migration V1 -> V2 and Hibernate `ddl-auto: validate` were executed and passed against H2 2.3.232 (`TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` lines 91–114).
    - **VERIFIED (Static SQL portability inspection):** V2 uses standard SQL:2003 `BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY`, `VARCHAR(n)`, `DATE`, `BIGINT NOT NULL DEFAULT 0`, standard `UNIQUE`/`FOREIGN KEY ... ON DELETE CASCADE` constraints, and `CREATE INDEX`, with unquoted lowercase identifiers. By static inspection, this DDL is portable across H2 2.x and PostgreSQL 10+.
    - **PENDING VERIFICATION (Live PostgreSQL execution):** No live PostgreSQL database was run during T2.1. Real PostgreSQL execution is **not** claimed and remains deferred until a PostgreSQL instance / Docker Compose environment is exercised (Phase 5 / T5.2, per `implementation-plan.md` §5 T1.4 and `agent-reports/decisions.md` D-07, D-09).

#### 11 & Special Attention A. Repository query correctness and coverage
- **VERIFIED (`ClientRepository.java`, `DocumentRepository.java`, `DomainPersistenceTest.java`):**
  - Query correctness by source inspection and Spring Data bootstrap validation (`TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` lines 87–88: `Finished Spring Data repository scanning in 418 ms. Found 2 JPA repository interfaces`):
    - `ClientRepository.existsByTaxIdentifier(String taxIdentifier)` — valid derived query matching `Client.taxIdentifier`.
    - `ClientRepository.findByTaxIdentifier(String taxIdentifier)` — valid derived query returning `Optional<Client>`.
    - `ClientRepository.lockById(@Param("id") Long id)` — valid JPQL `select c from Client c where c.id = :id` with `@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)`.
    - `DocumentRepository.existsByNumber(String number)` — valid derived query matching `Document.number`.
    - `DocumentRepository.findByNumber(String number)` — valid derived query returning `Optional<Document>`.
    - `DocumentRepository.findByClientId(Long clientId, Pageable pageable)` — valid derived query traversing `Document.client.id` with `Pageable` pagination support.
    - `DocumentRepository.findByIdAndClientId(Long documentId, Long clientId)` — valid derived query matching `Document.id` and `Document.client.id`, returning `Optional<Document>`.
  - **Repository query test coverage gap (Finding R-F01):**
    - A `grep` across `src/test/` confirms that **none of the 7 custom methods declared on `ClientRepository` and `DocumentRepository` are called in `DomainPersistenceTest`**.
    - Specifically, `findByTaxIdentifier`, `existsByTaxIdentifier`, `findByNumber`, `existsByNumber`, `findByClientId(clientId, pageable)`, `findByIdAndClientId(documentId, clientId)` and `lockById(id)` have zero direct behavioral test invocations.
    - Furthermore, `DocumentRepository.findByClientId(Long clientId, Pageable pageable)` does not embed a fixed `OrderByIdAsc` in the method name; deterministic ascending-ID sorting (required by `requirements-decisions.md` §9 and §20) relies on the caller passing `Sort.by(Sort.Direction.ASC, "id")` in `Pageable` (just like `ClientRepository.findAll(Pageable)`).

#### 12. Test quality and coverage
- **VERIFIED (`DomainPersistenceTest.java`, `target/surefire-reports/*.txt`):**
  - All 54 tests across the 13 test classes pass (`0 failures, 0 errors, 0 skipped`), including all 13 tests in `DomainPersistenceTest`.
  - `DomainPersistenceTest` is clean, deterministic, uses `@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true"})`, properly flushes and clears the `EntityManager` before reloading to avoid false positives from the first-level cache, and tests both JPA cascade delete and direct SQL `ON DELETE CASCADE`.
  - Coverage gaps are limited to the unexercised custom repository query methods (Finding R-F01) and optional per-column `NOT NULL` insert checks (Finding R-F05).

#### 13. Consistency with approved requirements
- **VERIFIED:** All 5 acceptance criteria for T2.1 in `implementation-plan.md` Section 6 (lines 180–185) are met:
  1. `taxIdentifier` is globally unique — **VERIFIED**.
  2. `Document.number` is globally unique — **VERIFIED**.
  3. Deleting a Client deletes associated Documents — **VERIFIED**.
  4. No orphan Documents are permitted — **VERIFIED**.
  5. Both entities use JPA optimistic locking — **VERIFIED**.

#### 14 & Special Attention D. Scope compliance and unrelated changes
- **VERIFIED (`git status`, `git diff`):**
  - All new T2.1 files are strictly within T2.1 scope (`domain/`, `repository/`, `V2__create_clients_and_documents.sql`, `DomainPersistenceTest.java`, and `agent-reports/T2.1/`). No controllers, services, DTOs, mappers, security classes or Docker files were introduced.
  - **Out-of-scope modification flagged (Finding R-F04):** `.opencode/agents/reviewer.md` has an unstaged 1-line whitespace modification (removal of a blank line at line 17). Per instructions, this file was not modified or reverted by the Reviewer and **must be excluded from the T2.1 commit**.

### 8.2 Comparison with Architect, Developer and Tester reports

- **Comparison with Architect proposal (`T2.1-EXEC-001-ARCH-01`):**
  - **VERIFIED:** The Developer implementation matches the Architect proposal's package layout, entity structure, V2 SQL schema, and repository method signatures, incorporating the Tech Lead adjustment to omit redundant `@Column(unique = true)` when `@Table(uniqueConstraints = ...)` is already present.
- **Comparison with Developer report (`T2.1-EXEC-002-DEV-01`):**
  - **VERIFIED:** The Developer's reported files, test counts (54 total, 13 in `DomainPersistenceTest`), and H2 verification claims match the workspace artifacts.
  - **CHALLENGED / SUPPLEMENTED:** The Developer report (§9) listed only `Info` items and no `Minor` findings. Our independent audit identified 4 `Minor` findings (`R-F01` through `R-F04`), including the lack of behavioral test coverage for the 7 custom repository methods (`R-F01`), in-memory relationship helper edge cases (`R-F02`), missing `updatable = false` on `Document.client` `@JoinColumn` (`R-F03`), and the out-of-scope working-tree edit to `.opencode/agents/reviewer.md` (`R-F04`).
- **Comparison with Tester report (`T2.1-EXEC-003-TST-01`):**
  - **VERIFIED:** The Tester's findings `T-F01` (custom repository queries and `Pageable` behavior not directly exercised in `DomainPersistenceTest`), `T-F02` (unrelated modification to `.opencode/agents/reviewer.md`), `T-F03` (PostgreSQL compatibility unverified against a live instance), and `T-F04` (`NOT NULL` constraints validated via DDL/schema rather than per-column null-insert tests) corroborate our independent audit.
  - **SUPPLEMENTED:** Beyond the Tester's `T-F01` (which focused on derived `findBy...` queries and `Pageable`), our independent review also highlights that `ClientRepository.lockById(id)` (`@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)`) is not yet exercised in `DomainPersistenceTest`, and identifies the entity design nuances in `R-F02` (`removeDocument` in-memory bidirectional state and `assignClient` proxy equality) and `R-F03` (`@JoinColumn(..., updatable = false)`).

## 9. Findings and severity

| ID | Severity | Finding | Why it matters & Evidence | Suggested improvement |
|---|---|---|---|---|
| **R-F01** | **Minor** | Custom repository methods in `ClientRepository` and `DocumentRepository` have no direct behavioral tests in `DomainPersistenceTest`. | **VERIFIED (`ClientRepository.java`, `DocumentRepository.java`, `DomainPersistenceTest.java`):** None of the 7 custom methods (`existsByTaxIdentifier`, `findByTaxIdentifier`, `lockById`, `existsByNumber`, `findByNumber`, `findByClientId(Long, Pageable)`, `findByIdAndClientId(Long, Long)`) are invoked in `src/test/`. While Spring Data validates query derivation and JPQL syntax at context startup, behavioral properties — such as `lockById` forcing a parent `Client.version` increment on flush, `findByIdAndClientId` returning empty when a `Document` belongs to a different `Client`, and `findByClientId` returning deterministically ordered pages when passed `PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))` — are not yet asserted by automated tests. | Add focused test methods in `DomainPersistenceTest` (either in an optional T2.1 polish pass or alongside T2.3/T2.4/T2.6/T2.8 service/repository tests) exercising all 7 custom repository methods, including `lockById` version increment and `findByClientId` with `Sort.by(Sort.Direction.ASC, "id")`. |
| **R-F02** | **Minor** | Relationship helper nuances in `Client.removeDocument(Document)` (in-memory bidirectional state) and `Document.assignClient(Client)` (reference `!=` vs `equals` with lazy proxies). | **VERIFIED (`Client.java` lines 130–136, `Document.java` lines 92–97):** <br>1. `Client.removeDocument(Document)` removes the element from `Client.documents` in memory without clearing `document.client` or deleting the row. If called without `documentRepository.delete(document)` in the same transaction, `client.getDocuments()` and `document.getClient()` become inconsistent in memory for the remainder of the persistence context (and a transient `Document` removed before flush retains a non-null `client` reference). <br>2. `Document.assignClient(Client)` checks `if (this.client != null && this.client != client)` using `!=`. Because `Document.client` is `FetchType.LAZY`, `this.client` on a loaded `Document` can be a `HibernateProxy` while `client` is an unproxied/merged instance of the *same* `Client` (same non-null `id`), causing `this.client != client` to evaluate to `true`. | 1. Ensure service-layer code in T2.4/T2.5/T2.7 only calls `client.removeDocument(doc)` when paired with `documentRepository.delete(doc)` in the same transaction (never during collection-preserving updates). <br>2. In `Document.assignClient(Client)`, require non-null `client` (`Objects.requireNonNull(client, "client must not be null")`) and check `if (this.client != null && !this.client.equals(client))` (which preserves reference identity for transient clients where `id == null` and uses ID equality for persisted/proxied clients). |
| **R-F03** | **Minor** | `Document.client` `@JoinColumn` omits `updatable = false`. | **VERIFIED (`Document.java` lines 38–39, `TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml` lines 126, 177):** Because `updatable = false` is not set on `@JoinColumn(name = "client_id", nullable = false, ...)`, Hibernate includes `client_id=?` in every generated `UPDATE documents SET client_id=?, ...` statement even though `Document` ownership is immutable by domain rule. | Add `updatable = false` to `@JoinColumn(name = "client_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_documents_client"))` in `Document.java`. |
| **R-F04** | **Minor** | Unrelated working-tree modification to `.opencode/agents/reviewer.md`. | **VERIFIED (`git status`, `git diff`):** `.opencode/agents/reviewer.md` has a 1-line unstaged change (blank line deletion) outside T2.1 scope. Modifying agent definitions is outside task scope (`agent-reports/README.md` §8). | Exclude `.opencode/agents/reviewer.md` from the T2.1 commit (e.g., restore it via `git restore .opencode/agents/reviewer.md` before staging T2.1 files). |
| **R-F05** | **Info** | Database `NOT NULL` constraints on scalar columns are verified via DDL inspection and Hibernate `ddl-auto: validate`, not via individual null-insert SQL tests. | **VERIFIED (`Client.java`, `Document.java`, `V2__create_clients_and_documents.sql`, `DomainPersistenceTest.java`):** All required scalar columns are marked `nullable = false` in JPA and `NOT NULL` in V2 DDL, and Hibernate schema validation passed on H2. DTO-level `@NotBlank`/`@NotNull` validation is scheduled for T2.2. | No action required for T2.1; T2.2 will add Bean Validation tests for all required request fields. |
| **R-F06** | **Info** | PostgreSQL migration compatibility is verified by static SQL inspection only; live PostgreSQL execution remains `PENDING VERIFICATION`. | **VERIFIED (`V2__create_clients_and_documents.sql`):** V2 uses standard ANSI SQL portable to both H2 and PostgreSQL, but only H2 was executed during T2.1 tests. Live PostgreSQL verification is deferred to Phase 5 (T5.2) per `implementation-plan.md` §5 T1.4 and `agent-reports/decisions.md` D-07/D-09. | Keep tracked as `PENDING VERIFICATION` until Phase 5 (`compose.yaml` + PostgreSQL container verification). |
| **R-F07** | **Info** | Tech Lead approval of T2.1 scope/proposal and final T2.1 acceptance are not yet recorded in `agent-reports/decisions.md`. | **VERIFIED (`agent-reports/decisions.md`):** The decision log currently ends at `D-09` (Phase 1 complete; authorization to proceed to Phase 2 / T2.1). | Human Tech Lead to append `D-10` to `agent-reports/decisions.md` upon accepting T2.1. |

**Severity summary:**
- **Critical:** 0
- **Major:** 0
- **Minor:** 4 (`R-F01`, `R-F02`, `R-F03`, `R-F04`)
- **Info:** 3 (`R-F05`, `R-F06`, `R-F07`)

## 10. Decisions and reasoning

1. **Verdict classification (`PASS WITH FINDINGS`):**
   - Per `implementation-plan.md` Section 2 ("Reviewer findings of Critical or Major severity must be resolved before approval"), only Critical or Major findings block task approval.
   - All five T2.1 acceptance criteria (`implementation-plan.md` Section 6, lines 180–185) are fully implemented and verified by automated H2 persistence tests (`54/54` total tests passing, `13/13` in `DomainPersistenceTest`).
   - None of `R-F01` through `R-F04` constitutes a runtime defect or requirement violation in T2.1's delivered scope; therefore, the audit verdict is **PASS WITH FINDINGS** (0 Critical, 0 Major, 4 Minor, 3 Info).
2. **Concrete required fixes vs. non-blocking recommendations:**
   - **Required before committing T2.1 (`R-F04`):** Do **not** stage or commit the unrelated modification to `.opencode/agents/reviewer.md`.
   - **Recommended non-blocking technical refinements (`R-F01`, `R-F02`, `R-F03`):** The human Tech Lead may either:
     - **(Option 1 — Accept T2.1 now and carry forward):** Accept T2.1 as complete without code rework, addressing `R-F01` (repository query behavioral tests), `R-F02` (`assignClient` proxy-safe equality and `removeDocument` usage discipline), and `R-F03` (`@JoinColumn(..., updatable = false)`) during the upcoming Phase 2 service/repository tasks (T2.3–T2.8), **or**
     - **(Option 2 — Optional minor polish in T2.1):** Authorize a small Developer/Tester pass in T2.1 to add `updatable = false` on `Document.client`, use `!this.client.equals(client)` in `Document.assignClient`, and add behavioral tests in `DomainPersistenceTest` for the 7 custom repository methods.

## 11. Final status

**PASS WITH FINDINGS (Reviewer Verdict) — 0 Critical, 0 Major, 4 Minor, 3 Info. Ready for Human Tech Lead decision.**

**VERIFIED:** This Reviewer report does not grant final acceptance of T2.1. Per `agent-reports/README.md` Section 8 and Mandatory Rule 10, only the human Tech Lead may approve final acceptance.

## 12. Outstanding issues

1. **ACTION REQUIRED AT GIT STAGE/COMMIT (`R-F04`):** Exclude the unrelated 1-line modification in `.opencode/agents/reviewer.md` from the T2.1 commit.
2. **OPEN MINOR FINDING (`R-F01`):** Custom repository methods (`existsByTaxIdentifier`, `findByTaxIdentifier`, `lockById`, `existsByNumber`, `findByNumber`, `findByClientId`, `findByIdAndClientId`) and `Pageable` deterministic sorting (`Sort.by(Sort.Direction.ASC, "id")`) lack direct execution tests in `DomainPersistenceTest`.
3. **OPEN MINOR FINDING (`R-F02`):** `Document.assignClient(Client)` uses `!=` instead of `!this.client.equals(client)` (relevant when `this.client` is a lazy Hibernate proxy), and `Client.removeDocument(Document)` leaves `document.client` pointing to `this` in memory if called without deleting the `Document`.
4. **OPEN MINOR FINDING (`R-F03`):** `@JoinColumn(name = "client_id", ...)` in `Document.java` omits `updatable = false`.
5. **PENDING VERIFICATION (`R-F06`):** Live PostgreSQL verification of `V2__create_clients_and_documents.sql` and `ddl-auto: validate` remains deferred to Phase 5 (T5.2).
6. **PENDING TECH LEAD DECISION (`R-F07`):** Formal Tech Lead acceptance of T2.1 and recording of `D-10` in `agent-reports/decisions.md`.

## 13. Handoff to next agent

- **To the Human Tech Lead:**
  - Review this **PASS WITH FINDINGS** audit report (`0 Critical, 0 Major, 4 Minor, 3 Info`).
  - Decide whether to **(a)** formally accept T2.1 now (recording `D-10` in `agent-reports/decisions.md`), exclude `.opencode/agents/reviewer.md` from the T2.1 commit, and carry `R-F01`–`R-F03` into T2.3–T2.8, or **(b)** authorize a focused T2.1 minor-polish pass for `R-F01`–`R-F03` before acceptance.
  - Authorize **T2.2 — DTOs, validation and mappers** only after T2.1 is formally accepted.

---
**Report status:** Created on 2026-11-09 for execution `T2.1-EXEC-004-REV-01`. Verified persisted at `agent-reports/T2.1/reviewer-report.md`.
