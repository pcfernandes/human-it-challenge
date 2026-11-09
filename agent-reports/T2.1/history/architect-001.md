# Architect Proposal — T2.1 Domain Model and Persistence Design

## 1. Task ID

**T2.1 — Domain model, mappings and database schema** (Phase 2), per `implementation-plan.md` Section 6 (T2.1) and `requirements-decisions.md` Sections 2, 5, 6, 7, 8, 9, 12, 20.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.1-EXEC-001-ARCH-01` |
| Date | 2026-11-09 (Europe/Lisbon) |
| Supersedes | None (first T2.1 Architect execution) |
| Predecessor archives | None required (no prior `agent-reports/T2.1/architect-proposal.md`) |
| Task directory | `agent-reports/T2.1/` (created by this execution) |

## 3. Agent role and model

- **Role:** Architect (senior Java and Spring Boot software architect)
- **Model:** `opencode/beam-free` (exact model ID `opencode/beam-free`)
- **Agent definition:** `.opencode/agents/architect.md` — `edit` allowed under `agent-reports/**` only; `bash: deny` (VERIFIED)

## 4. Authorized scope

Per the Tech Lead tasking of 2026-11-09 (D-09 grants authorization to proceed to Phase 2 / T2.1) and `implementation-plan.md` T2.1:

**In scope (this proposal):**
- `Client` and `Document` JPA entities: required fields, `@Version` fields, one-to-many relationship.
- Database constraints for required fields, uniqueness and foreign keys; cascade delete of Documents on Client deletion.
- Flyway migration creating the business schema.
- Repository interfaces for Client and Document.
- Package structure and mapping conventions for Phase 2.
- Design decisions, alternatives, trade-offs and acceptance criteria for T2.1.

**Out of scope (explicitly NOT proposed here):**
- REST controllers, services, DTOs, mappers, security (T2.2–T2.8, Phase 3).
- Application code, entities, repositories, migrations, configuration or tests (this is architecture only).
- Docker / containerization (Phase 5).
- Any change to requirements, planning documents, `decisions.md`, or other agents' reports.

## 5. Files inspected

| Artifact | Evidence class | Relevance |
|---|---|---|
| `agent-reports/README.md` | VERIFIED | 13-section report format, evidence markers, history convention |
| `agent-reports/decisions.md` (D-09) | VERIFIED | Phase 1 complete; authorization to proceed to Phase 2 / T2.1 granted |
| `implementation-plan.md` (Section 6, T2.1; Phase 2 exit) | VERIFIED | T2.1 scope, acceptance criteria, task split |
| `requirements-decisions.md` (Sections 2, 5, 6, 7, 8, 9, 12, 20) | VERIFIED | Domain fields, endpoints, update/deletion rules, validation, transactions, concurrency, pagination, persistence |
| `README.md` (root) | VERIFIED | Phase 1 baseline state, profile/config, deferred work |
| `pom.xml` | VERIFIED | Java 21, Spring Boot 3.5.16, JPA, Validation, Security, Flyway, H2, PostgreSQL, springdoc |
| `src/main/resources/application.yml` | VERIFIED | `ddl-auto: validate`, Flyway enabled, pagination defaults |
| `src/main/resources/application-dev.yml` | VERIFIED | H2 in-memory, dev-only H2 Console |
| `src/main/resources/application-prod.yml` | VERIFIED | PostgreSQL from `SPRING_DATASOURCE_*` |
| `src/main/resources/db/migration/V1__baseline.sql` | VERIFIED | Comment-only baseline migration (version 1) |
| `src/main/java/com/humanit/challenge/**` (glob) | VERIFIED | No entities, repositories, controllers or domain packages exist yet |
| `agent-reports/T1.4/architect-proposal.md` | VERIFIED | Established reporting style and migration conventions (V1 baseline) |
| `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` | VERIFIED | Existing Problem Details handler; 409 handler deferred |
| `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` | VERIFIED | Minimal OpenAPI bean; security scheme deferred |

## 6. Files changed

| File | Change |
|---|---|
| `agent-reports/T2.1/architect-proposal.md` | **Created** — this proposal |

No application code, YAML, `pom.xml`, tests, requirements, planning documents, `decisions.md`, or Git state were modified. The proposed implementation changes are listed in Section 10 for later authorized execution.

## 7. Commands executed

**None.** `bash` is denied for this agent. No build or test command was run; all facts are taken from inspected files. No application code was compiled.

## 8. Evidence and observed results

- **VERIFIED:** Phase 1 is complete and accepted (D-09); authorization to proceed to Phase 2 / T2.1 is granted.
- **VERIFIED:** No business entities, repositories, controllers or domain packages exist in `src/main/java` (glob returned only bootstrap/config/exception classes). T2.1 starts from a clean domain layer.
- **VERIFIED:** `V1__baseline.sql` is comment-only and recorded as schema version 1; business schema must arrive in a subsequent migration (V2).
- **VERIFIED:** Persistence baseline is H2 in `dev`, PostgreSQL in `prod`, Flyway enabled, `ddl-auto: validate` common to both profiles.
- **VERIFIED:** `GlobalExceptionHandler` provides RFC 9457 handling for 400/404/405/500 but **does not** yet handle 409 (uniqueness/optimistic-locking conflicts) — deferred per T1.5.
- **VERIFIED:** Requirements fix `Document.number` as **globally** unique (requirements-decisions.md §2 and §6) and `taxIdentifier` as **globally** unique; T2.1 acceptance criteria in `implementation-plan.md` repeat both.
- **VERIFIED:** No `document type` attribute is defined anywhere in the approved domain model (`requirements-decisions.md` §2 lists only `number`, `description`, `expirationDate`, `client`, `version` for Document).
- **RECONSTRUCTED:** Because no T2.1 implementation exists, the repository state matches the expected Phase 1 baseline; no discrepancy with the expected baseline was observed beyond the already-documented stale status line in `requirements-decisions.md` ("Application implementation has not started", gap G-07 / proposal P-04), which is out of scope for this task and is not corrected here.
- **NOT AVAILABLE:** No prior `agent-reports/T2.1/` directory or report (confirmed by glob). No history archival required.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| F-01 | Info | Domain layer is entirely absent; T2.1 introduces the first entities, repositories and business migration. |
| F-02 | Minor | `GlobalExceptionHandler` lacks a 409 (conflict) handler; uniqueness/optimistic-locking conflicts cannot yet be mapped to Problem Details. This is T2.3/T2.4/T2.6 scope, but the repository/entity design must raise standard Spring exceptions so future handlers can translate them. |
| F-03 | Minor | The approved Document model defines no "document type" attribute, while the task's design-consideration list names "Document type representation". This is an ambiguity (see Section 10.6) and must be resolved by the Tech Lead before any type field is introduced. |
| F-04 | Info | String column lengths are not specified by requirements; proposed lengths are technical defaults requiring approval (Section 10.4). |
| F-05 | Info | No Critical or Major findings. |

No Critical or Major findings.

## 10. Decisions and reasoning (alternatives, trade-offs, assumptions)

### 10.1 Proposed package structure

Base package remains `com.humanit.challenge` (VERIFIED in existing code).

Proposed additions (T2.1 scope only):
- `com.humanit.challenge.domain` — JPA entities (`Client`, `Document`).
- `com.humanit.challenge.repository` — Spring Data JPA interfaces (`ClientRepository`, `DocumentRepository`).

Deferred to later tasks (not created in T2.1):
- `com.humanit.challenge.service` (T2.3+), `com.humanit.challenge.controller` (T2.3+), `com.humanit.challenge.dto` (T2.2), `com.humanit.challenge.mapper` (T2.2).

Rationale: keep the persistence model isolated from web/DTO concerns; entities live in `domain`, repositories in `repository`; services orchestrate transactions in a later task.

### 10.2 Proposed entity model — Client

```java
@Entity
@Table(name = "clients",
       uniqueConstraints = @UniqueConstraint(name = "uk_clients_tax_identifier",
                                            columnNames = "tax_identifier"))
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "tax_identifier", nullable = false, unique = true, length = 50)
    private String taxIdentifier;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @OneToMany(mappedBy = "client",
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH},
               fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // constructors, getters/setters, equals/hashCode/toString
}
```

Notes:
- `documents` is optional (may be null/empty on input); initialized to an empty list to avoid NPEs.
- No `orphanRemoval = true`: documents are **not** deleted when removed from the collection (collection-preserving update semantics, requirements §4/§5). Deletion happens only via explicit DELETE or via cascade when the Client is deleted.
- `CascadeType.REMOVE` ensures deleting a Client deletes its Documents through JPA; the database FK also uses `ON DELETE CASCADE` as a backstop.
- `FetchType.LAZY` on both sides to avoid unintended N+1 loading.
- `@Version` type `Long`; column `BIGINT NOT NULL`.

### 10.3 Proposed entity model — Document

```java
@Entity
@Table(name = "documents",
       uniqueConstraints = @UniqueConstraint(name = "uk_documents_number",
                                            columnNames = "number"))
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "number", nullable = false, unique = true, length = 100)
    private String number;

    @Column(name = "description", length = 1000)
    private String description; // nullable (optional)

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_documents_client"))
    private Client client;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // constructors, getters/setters, equals/hashCode/toString
}
```

Notes:
- `client` is required and non-null; `optional = false` + `nullable = false` FK enforces no orphan.
- `description` nullable (optional, requirements §6).
- `expirationDate` is `java.time.LocalDate`; expired documents are accepted (no future-date constraint).
- No "document type" field (see 10.6).

### 10.4 Proposed database schema (Flyway V2)

File: `src/main/resources/db/migration/V2__create_clients_and_documents.sql`

```sql
-- V2: business schema for Clients and Documents (T2.1).

CREATE TABLE clients (
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_name      VARCHAR(100)  NOT NULL,
    last_name       VARCHAR(100)  NOT NULL,
    tax_identifier  VARCHAR(50)   NOT NULL,
    email           VARCHAR(255)  NOT NULL,
    phone_number    VARCHAR(50)   NOT NULL,
    version         BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_clients_tax_identifier UNIQUE (tax_identifier)
);

CREATE TABLE documents (
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    number          VARCHAR(100)  NOT NULL,
    description     VARCHAR(1000),
    expiration_date DATE          NOT NULL,
    client_id       BIGINT        NOT NULL,
    version         BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_documents_number UNIQUE (number),
    CONSTRAINT fk_documents_client
        FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE CASCADE
);

CREATE INDEX idx_documents_client_id ON documents (client_id);
```

Compatibility notes:
- `GENERATED BY DEFAULT AS IDENTITY` is standard SQL identity syntax supported by both H2 and PostgreSQL 10+ (VERIFIED: H2 supports identity columns; PostgreSQL supports `GENERATED ... AS IDENTITY` since 10). This avoids vendor-specific `AUTO_INCREMENT` / `SERIAL`.
- `DATE`, `VARCHAR(n)`, `BIGINT` are portable.
- `ON DELETE CASCADE` on the FK guarantees Client deletion removes Documents even outside JPA.
- Explicit index on `client_id` supports nested queries and FK lookups (PostgreSQL does not auto-index FK columns).
- Unique constraints create backing unique indexes on both databases.

Lengths (F-04) are technical defaults, not business rules; they are proposed for approval and may be adjusted.

### 10.5 Relationship cardinality, cascade and orphan rules

| Aspect | Decision |
|---|---|
| Cardinality | Client `1` — `0..*` Document; Document `*` — `1` Client |
| Ownership | Client is the aggregate root; Document cannot exist without a Client |
| JPA mapping | `Client.documents`: `@OneToMany(mappedBy="client")`; `Document.client`: `@ManyToOne(optional=false)` |
| Cascade on Client delete | `CascadeType.REMOVE` (JPA) + FK `ON DELETE CASCADE` (DB) |
| Orphan prevention | `client_id NOT NULL` FK; no `orphanRemoval` (no implicit deletion on collection change) |
| Transfer between clients | Forbidden by design (ownership immutable; no setter for `client` reassignment in aggregate updates) |
| Fetching | `LAZY` both sides |

### 10.6 Ambiguity: "Document type representation"

**Ambiguity identified:** The task's design-consideration list names "Document type representation", but the approved requirements (`requirements-decisions.md` §2) define **no** document-type attribute on Document. The approved Document fields are only `number`, `description`, `expirationDate`, `client`, `version`.

**Consequence:** Introducing a `type` field would invent a mandatory/business field, which is forbidden by the task ("Do not invent mandatory fields or business rules").

**Alternatives proposed for Tech Lead approval:**
- **Alternative A (recommended, default):** No document-type field in T2.1. Document is identified by its globally unique `number`. This matches approved requirements exactly.
- **Alternative B:** Add an optional `type` attribute later (T2.2+), represented as a Java `enum` mapped with `@Enumerated(EnumType.STRING)` and a DB `CHECK` constraint, with a Flyway migration. Requires a requirements amendment.
- **Alternative C:** Treat `number` prefix/structure as the type indicator. Rejected: pushes business semantics into a free-text field and complicates uniqueness.

**Decision required:** Confirm Alternative A for T2.1. If a type is needed, it must be added as a new approved requirement in a later task, not in T2.1.

### 10.7 Ambiguity: uniqueness scope for Document.number

**Approved policy (VERIFIED):** `Document.number` is **globally** unique (requirements-decisions.md §2, §6; implementation-plan.md T2.1 acceptance: "`Document.number` is globally unique"; T2.4 acceptance: "Duplicate `Document.number` returns HTTP 409").

The task explicitly asks to distinguish the three possible policies. They are listed below for the Tech Lead's awareness; **no silent choice is made** — the approved policy is global uniqueness and is the only one implemented unless requirements change.

| Policy | Description | Status |
|---|---|---|
| **Global (approved)** | `number` unique across all Documents and all Clients | **Implemented** (V2 `UNIQUE (number)`) |
| Per document type | `number` unique within a type | Not applicable — no type field exists (see 10.6) |
| Per client | `number` unique within a Client | Not approved; would require `UNIQUE (client_id, number)` and a requirement change |

**Decision required:** Confirm global uniqueness (Alternative: global). If per-client or per-type uniqueness is desired, requirements and the V2 migration must change before implementation.

### 10.8 Nullability and field validation

| Field | Nullable (DB) | Validation (Bean Validation) | Where enforced |
|---|---|---|---|
| Client.id | no (PK) | — | DB |
| Client.firstName | NOT NULL | `@NotBlank` | DTO (T2.2) + DB |
| Client.lastName | NOT NULL | `@NotBlank` | DTO (T2.2) + DB |
| Client.taxIdentifier | NOT NULL, UNIQUE | `@NotBlank` | DTO (T2.2) + DB |
| Client.email | NOT NULL | `@NotBlank`, `@Email` | DTO (T2.2) + DB |
| Client.phoneNumber | NOT NULL | `@NotBlank` | DTO (T2.2) + DB |
| Client.documents | n/a (collection) | optional | — |
| Client.version | NOT NULL | — | JPA/DB |
| Document.id | no (PK) | — | DB |
| Document.number | NOT NULL, UNIQUE | `@NotBlank` | DTO (T2.2) + DB |
| Document.description | nullable | optional; `@Size` optional | DTO (T2.2) |
| Document.expirationDate | NOT NULL | `@NotNull` | DTO (T2.2) + DB |
| Document.client | NOT NULL (FK) | `@NotNull` (via ownership) | DB + service |
| Document.version | NOT NULL | — | JPA/DB |

Design decision: Bean Validation annotations are applied on **DTOs** in T2.2, not on entities, to keep entities persistence-focused. DB `NOT NULL`/`UNIQUE`/FK constraints remain the authoritative backstop. `description: null` clears the value (requirements §20); this is handled in the service/mapper layer (T2.2/T2.5), not the entity.

Alternative: add `jakarta.validation` annotations to entities for defense-in-depth. Trade-off: couples persistence model to validation framework and may trigger validation during repository tests. Recommended: keep on DTOs only; DB constraints enforce integrity.

### 10.9 Repository interfaces

```java
public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByTaxIdentifier(String taxIdentifier);

    Optional<Client> findByTaxIdentifier(String taxIdentifier);

    // For pagination (inherited from JpaRepository via findAll(Pageable)).
    // Locking helper for T2.6 (OPTIMISTIC_FORCE_INCREMENT on parent during Document mutations):
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select c from Client c where c.id = :id")
    Optional<Client> lockById(@Param("id") Long id);
}

public interface DocumentRepository extends JpaRepository<Document, Long> {

    boolean existsByNumber(String number);

    Optional<Document> findByNumber(String number);

    Page<Document> findByClientId(Long clientId, Pageable pageable);

    Optional<Document> findByIdAndClientId(Long documentId, Long clientId);
}
```

Notes:
- `lockById` is declared now so the T2.6 service can force-increment the parent Client version within the same transaction; it is not used until T2.6.
- `findByIdAndClientId` supports the nested-endpoint ownership rule (accessing a Document under a non-owning Client returns 404).
- Pagination uses Spring Data `Pageable` with the approved defaults/max (T2.8); repository methods accept `Pageable`.

Duplicate handling: services perform an `existsBy...` pre-check for an early 409, but the DB unique constraint is authoritative. `DataIntegrityViolationException` thrown by the DB must be translated to HTTP 409 (Problem Details) by a handler added in T2.3/T2.4 (F-02). Repository design does not swallow these exceptions.

### 10.10 JSON serialization boundaries and DTO separation

- Entities are **persistence model only** and must never be serialized directly by controllers. Services map entities to DTOs (T2.2) before returning; controllers return DTOs.
- No Jackson annotations are added to entities in T2.1. Lazy associations (`documents`, `client`) must not be touched outside a transaction; mapping to DTOs occurs inside `@Transactional` service methods.
- `toString()` on both entities excludes the lazy `documents`/`client` associations to prevent accidental initialization and log noise.
- `equals`/`hashCode` are based on the generated `id` with null-safe handling (unsaved entities are not considered equal). This is a persistence-model concern only.

### 10.11 Flyway V2 migration strategy

- Keep `V1__baseline.sql` as the version-1 baseline (comment-only, already applied). Do **not** modify V1.
- Add a single `V2__create_clients_and_documents.sql` creating both tables (clients first, then documents with FK), plus the `client_id` index.
- Use standard, portable SQL so the same migration runs on H2 (dev/test) and PostgreSQL (prod). If a future change is vendor-specific, use Flyway's database-specific locations (`classpath:db/migration/{vendor}`) rather than hard-coding vendor assumptions in the common path (per T1.4 proposal 10.3 point 6).
- Hibernate `ddl-auto: validate` will validate the JPA mappings against V2 on startup; Flyway owns all schema changes.
- No seed/demo data in migrations; demo user provisioning is a Phase 3 concern and must not be in schema migrations.

### 10.12 H2 development and PostgreSQL compatibility

- Portable types only: `BIGINT`, `VARCHAR(n)`, `DATE`, identity columns.
- Unquoted, lowercase identifiers so both H2 and PostgreSQL fold to lowercase consistently.
- No PostgreSQL-specific types (e.g., `JSONB`, `UUID`) in V2.
- PostgreSQL integration against a real instance remains PENDING VERIFICATION until Phase 5 (OT-01 from T1.4); H2 is the automated-test database.

### 10.13 Consistency with future CRUD operations

- Field names (`firstName`, `lastName`, `taxIdentifier`, `email`, `phoneNumber`, `number`, `description`, `expirationDate`) and column names (snake_case) are fixed by this proposal; DTOs (T2.2) and mappers must use the same names to keep mapping trivial.
- `id` and `version` are never accepted on create requests (POST Client/Document require no version; POST Document requires `X-Client-Version` header only).
- Aggregate merge rules (T2.5) rely on `id` presence to distinguish existing vs new Documents; this entity design supports that (nullable generated id).
- Optimistic locking contract (T2.6) relies on `@Version` on both entities and `lockById` (OPTIMISTIC_FORCE_INCREMENT) on the parent; this proposal provides both.

### 10.14 Handling duplicate document numbers

- DB unique constraint `uk_documents_number` is the authoritative guard against races.
- Repository `existsByNumber` enables an early, user-friendly 409 before hitting the constraint.
- On constraint violation, Spring raises `DataIntegrityViolationException`, which a T2.3/T2.4 handler translates to HTTP 409 Problem Details (F-02).
- The same pattern applies to `taxIdentifier` (`uk_clients_tax_identifier`).

### 10.15 Integration with existing Problem Details handling

- Current `GlobalExceptionHandler` covers validation (400), malformed body (400), type mismatch (400), not found (404), method not allowed (405), constraint violations (400) and unexpected (500).
- T2.1 adds no handler; it only ensures entities/repositories raise standard Spring exceptions (`DataIntegrityViolationException`, `OptimisticLockingFailureException`, `EmptyResultDataAccessException`/`EntityNotFoundException`).
- T2.3/T2.4 (and T2.6) will extend the handler for: 409 conflict (uniqueness + optimistic locking), and 404 for missing resources. This is flagged as F-02 and tracked as a future task, not part of T2.1.
- `ResponseStatusException` reasons (per D-08) must remain client-safe in future controllers.

### 10.16 Trade-offs and assumptions

Trade-offs:
- **No `orphanRemoval`** preserves collection-update safety (no accidental deletion) at the cost of relying on explicit DELETE + DB cascade for Client deletion. Accepted: matches requirements §4/§5 ("no implicit deletion").
- **Bean Validation on DTOs only** keeps entities persistence-focused; DB constraints remain the backstop. Accepted.
- **Identity columns via standard SQL** keeps one migration for both databases; assumes H2 and PG both accept `GENERATED BY DEFAULT AS IDENTITY`.
- **Separate repository for Document** supports nested endpoints and document-level pagination, at the cost of two aggregate entry points. Accepted (required by endpoints).

Assumptions (PENDING VERIFICATION):
- H2 and PostgreSQL both accept `GENERATED BY DEFAULT AS IDENTITY` in Flyway migrations.
- Flyway applies V2 successfully on H2 (dev/test) after V1 comment-only baseline.
- `ddl-auto: validate` passes against the V2 schema for the proposed JPA mappings.
- `@Lock(OPTIMISTIC_FORCE_INCREMENT)` is supported by Spring Data JPA for the declared method (to be verified in T2.6).

## 11. Final status

**DELIVERED — PROPOSAL PERSISTED, PENDING TECH LEAD APPROVAL.**

This proposal is design-only. No implementation was performed, no application code, configuration, migration, tests or Git state were modified, and no approval was recorded. Approval and implementation authorization remain with the human Tech Lead.

## 12. Outstanding issues

| ID | Type | Description | Owner / next step |
|---|---|---|---|
| OT-01 | Awaiting approval | Tech Lead approval of this T2.1 proposal | Tech Lead |
| OT-02 | Awaiting authorization | Implement entities, repositories and V2 migration per Section 10 | Developer (after approval) |
| OT-03 | Awaiting decision | Confirm "Document type" policy (10.6, Alternative A recommended) | Tech Lead |
| OT-04 | Awaiting decision | Confirm global uniqueness for `Document.number` (10.7) | Tech Lead |
| OT-05 | Awaiting decision | Approve proposed string column lengths (10.4) | Tech Lead |
| OT-06 | PENDING VERIFICATION | Flyway applies V2 on H2 and PostgreSQL; `ddl-auto: validate` passes | Tester (T2.1) |
| OT-07 | PENDING VERIFICATION | Cascade deletion and no-orphan behavior | Tester (T2.1) |
| OT-08 | Deferred | 409 Problem Details handler for uniqueness/optimistic-locking conflicts | T2.3/T2.4/T2.6 |

## 13. Handoff to next agent

- **To the Tech Lead:** review and approve or reject this proposal; resolve the decisions in Sections 10.6, 10.7 and 10.4 (OT-03, OT-04, OT-05) and record approval before implementation.
- **To the Developer (after approval):** implement only the approved scope — entities in `com.humanit.challenge.domain`, repositories in `com.humanit.challenge.repository`, and `V2__create_clients_and_documents.sql`. Do not add controllers, services, DTOs, security or tests beyond repository-persistence verification.
- **To the Tester:** verify independently before reading the Developer report, using T2.1 acceptance criteria (global uniqueness of `taxIdentifier` and `Document.number`, cascade deletion, no orphans, optimistic locking on both entities) and the test-impact notes in Section 10.9.
- **To the Reviewer:** audit aggregate design, constraint correctness, cascade behavior, H2/PostgreSQL migration portability, and absence of invented fields/business rules.

---
**Status of this report:** Created 2026-11-09 from in-project artifacts only. All 13 mandatory sections present. No approval recorded.
