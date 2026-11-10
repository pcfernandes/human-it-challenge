# Architect Proposal — T2.2 DTOs, Bean Validation and Mappers

## 1. Task ID

**VERIFIED:** T2.2 — DTOs, Bean Validation and dedicated Client/Document mappers, scoped to the approved requirements and implementation-plan task.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.2-EXEC-004-ARCH-01` |
| Date | 2026-11-10 |
| Previous primary execution | **VERIFIED:** `D-11-CLOSE-001-ARCH-01`, currently in `agent-reports/T2.2/architect-proposal.md` before this replacement. |
| Archive | **VERIFIED:** Previous primary preserved unchanged as `agent-reports/T2.2/history/architect-002.md`; `architect-001.md` already exists, so 002 is the next available archive number. |

## 3. Agent role and model

- **VERIFIED:** Role — Architect, senior Java and Spring Boot software architect.
- **VERIFIED:** Model — `gpt-6-luna` (exact model ID `opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct Human Tech Lead tasking):** Prepare the T2.2 technical architecture proposal based on the named requirements, plan, approved D-11/D-12 decisions, T2.1 proposal, entities, repositories and persistence tests; write this report; do not implement code until the Human Tech Lead approves the proposal.

**VERIFIED (scope constraints):** This execution may author only its own Architect report and required history archive. It must not modify source, tests, migrations, configuration, requirements, implementation plan, decisions log, agent instructions, or other agents' reports. No service, REST resource/controller, or Git staging/commit/push work is proposed or performed.

**VERIFIED (direct tasking):** The supplied context states that T2.1 is accepted and closed, D-11 and D-12 are approved, and the development branch is `feature/T2.2-dtos-mappers`. **PENDING VERIFICATION:** This execution did not inspect Git branch state because shell/Git commands are outside the Architect's permitted scope.

## 5. Files inspected

| Artifact | Evidence / relevance |
|---|---|
| `agent-reports/README.md` | **VERIFIED:** Mandatory 13-section format, evidence markers, archive convention, and reporting boundaries. |
| `requirements-decisions.md` | **VERIFIED:** Client/Document field requirements, POST/PUT/PATCH behavior, nested collection and ownership rules, concurrency/version contract, validation, and testing requirements. |
| `implementation-plan.md` | **VERIFIED:** T2.2 scope/acceptance criteria, D-11 boundaries, later-task ownership of aggregate merge workflows, CRUD resources/services, concurrency, and transaction behavior. |
| `agent-reports/decisions.md` | **VERIFIED:** D-10 records T2.1 accepted and closed; D-11 records entity/mapper/repository/service/resource boundaries; D-12 records acceptance of the D-11 review. |
| `agent-reports/T2.1/architect-proposal.md` | **VERIFIED:** Prior T2.1 architecture/acceptance context; used only to preserve accepted persistence behavior. |
| `src/main/java/com/humanit/challenge/domain/Client.java` | **VERIFIED:** Scalar fields/accessors, lazy Documents collection, `@Version`, and relationship-consistency methods. |
| `src/main/java/com/humanit/challenge/domain/Document.java` | **VERIFIED:** Scalar fields/accessors, lazy owner, `@Version`, immutable `client_id` mapping and ownership guard. |
| `src/main/java/com/humanit/challenge/repository/ClientRepository.java` | **VERIFIED:** Repository API includes tax-identifier queries and an `OPTIMISTIC_FORCE_INCREMENT` lock query. |
| `src/main/java/com/humanit/challenge/repository/DocumentRepository.java` | **VERIFIED:** Repository API includes number queries and owner-filtered page/identity lookups. |
| `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java` | **VERIFIED:** Persistence tests cover schema/constraints, lazy loading, versions, collection preservation, ownership, and cascade behavior. |
| `src/test/java/com/humanit/challenge/domain/DocumentAssociationTest.java` | **VERIFIED:** Association test covers rejection of a null owner assignment. |
| `agent-reports/T2.2/architect-proposal.md` | **VERIFIED:** Previous primary report, archived unchanged before replacement. |
| `agent-reports/T2.2/history/architect-001.md` | **VERIFIED:** Existing first Architect archive; therefore the next archive is `architect-002.md`. |

## 6. Files changed

| Path | Change |
|---|---|
| `agent-reports/T2.2/history/architect-002.md` | **VERIFIED:** Added unchanged copy of the previous primary Architect report before replacement. |
| `agent-reports/T2.2/architect-proposal.md` | **VERIFIED:** Replaced the previous primary with this T2.2 DTO/validation/mapper proposal. |

**VERIFIED:** No application source, test, migration, configuration, requirements, plan, decision log, agent instruction, or other role report was changed.

## 7. Commands executed

**VERIFIED:** None. No shell command, build, test, or Git command was run. File reads/searches and authorized report-file operations only.

## 8. Evidence and observed results

### Requirements and approved constraints

- **VERIFIED (`requirements-decisions.md`, Sections 2, 4, 6 and 8/20):** Client required values are `firstName`, `lastName`, `taxIdentifier`, `email`, and `phoneNumber`; Client `documents` are optional. Document requires `number`, `expirationDate`, and an owning Client; `description` is optional. Email format is required; expired documents are allowed; uniqueness is a database constraint with HTTP 409 behavior.
- **VERIFIED (`requirements-decisions.md`, Sections 4 and 20):** Client PUT/PATCH require expected Client `version`; Document PUT/PATCH require expected Document `version` plus `X-Client-Version`; nested existing Documents being changed require ID/version, while new nested Documents have neither. POST Client has no version. POST Document's expected Client version is an HTTP header, not a body DTO field.
- **VERIFIED (`requirements-decisions.md`, Sections 4 and 20):** Omitted/empty Client update `documents` preserve existing Documents; `documents: null` is invalid; no implicit deletion or ownership transfer is permitted. PATCH omission preserves a field; required fields reject explicit null; `description: null` clears it. Existing nested IDs are updates, unknown IDs are rejected, and association of a foreign Client's Document returns 409.
- **VERIFIED (`implementation-plan.md`, D-11 and T2.2/T2.5):** Dedicated mappers own DTO/entity transformations. T2.2 does DTO contracts, validation and mapping; aggregate merge/identity/ownership workflows belong to later service use cases, not mapping code. No placeholder services are permitted, and T2.1 behavior must be preserved.
- **VERIFIED (`agent-reports/decisions.md`, D-10/D-11/D-12):** T2.1 is accepted/closed and is not reopened; entities remain persistence-focused; DTO conversion belongs in dedicated mappers; repositories own database access; services own actual workflows and ownership enforcement.

### Existing persistence model

- **VERIFIED (`Client.java`):** Client has generated `id`, five scalar values, `List<Document>` initialized empty and mapped `LAZY`, and JPA `@Version`. `getDocuments()` returns an unmodifiable view. `addDocument` delegates ownership assignment and prevents duplicate collection membership. `removeDocument` only removes from the in-memory collection and explicitly does not delete a row.
- **VERIFIED (`Document.java`):** Document has generated `id`, `number`, optional `description`, `LocalDate expirationDate`, lazy `client`, and JPA `@Version`. The `client_id` join column is `updatable=false`; owner assignment is guarded and transfer is rejected.
- **VERIFIED (entities):** `Client` construction accepts its required scalar fields; `Document` construction accepts number/description/date; neither entity has DTO conversion methods. `Document.client` has no public setter.
- **VERIFIED (`ClientRepository.java`, `DocumentRepository.java`):** Repositories provide persistence-oriented uniqueness lookups, owner-scoped document lookups and paging; this confirms mappers need no repository dependency.
- **VERIFIED (`DomainPersistenceTest.java`):** Existing tests assert collection removal is not deletion, persisted ownership cannot transfer, lazy associations remain unloaded for certain repository operations, and versions/optimistic locking are active. T2.2 mapping must preserve these behaviors.
- **NOT AVAILABLE:** No exact response JSON shape or approved rule for including a Client's nested Documents in `ClientResponse` was found in the inspected requirements. No exact Jackson representation/library for absent-vs-null fields is specified.
- **NOT AVAILABLE:** No DTO, mapper or Bean Validation implementation currently exists in the inspected source inventory; this task's file search was limited to the named classes and repository/persistence tests rather than a complete source-tree audit.

## 9. Findings and severity

| Finding | Severity | Evidence / impact |
|---|---|---|
| PATCH needs a tri-state representation for omitted, explicit null and supplied value | **Major design risk; approval required** | **VERIFIED (`requirements-decisions.md`, Section 20):** PATCH omission and explicit null have different outcomes. A conventional nullable Java field cannot reliably distinguish absent JSON property from JSON `null` after ordinary binding. Selecting the representation is a T2.2 design decision. |
| Client nested Document update validation is conditional on whether an item is new or existing | **Major design risk; approval required** | **VERIFIED (`requirements-decisions.md`, Sections 4/20):** existing items need ID/version while new items need neither and must provide required creation fields. Plain field annotations alone do not express all cross-field combinations; nested DTOs need class-level validation or equivalent. |
| Client PUT collection behavior is intentionally not ordinary full aggregate replacement | **Major contract risk if implemented naively** | **VERIFIED (`requirements-decisions.md`, Sections 4/20):** omitted and empty collections preserve existing rows; explicit null is invalid; omitted documents are not deleted. Do not replace the entity collection or call `removeDocument` as a mapping side effect. |
| Lazy relationship serialization/mapping can issue hidden SQL or fail outside a persistence context | **Major integration risk** | **VERIFIED (`Client.java`, `Document.java`, `DomainPersistenceTest.java`):** both relationship directions are LAZY and persistence tests explicitly observe unloaded state. Mappers must not fetch, and response callers must supply a graph loaded for the selected DTO contract. |
| Database uniqueness and optimistic-lock checks are not DTO-local validations | **Informational boundary risk** | **VERIFIED (`requirements-decisions.md`, Sections 6/8):** uniqueness and stale-version outcomes require persistence/use-case coordination and the specified 409 behavior. A mapper/Bean Validation annotation cannot replace DB constraints, service checks or transaction handling. |
| String maximum lengths and negative expected versions are not explicitly stated as request-validation rules | **Minor contract ambiguity** | **VERIFIED (entities):** JPA columns specify lengths; **VERIFIED (`requirements-decisions.md`):** required version fields and invalid-version handling are specified, but exact negative-version and per-field maximum-length DTO behavior is not. Avoid silently expanding validation rules without Tech Lead confirmation. |

## 10. Decisions and reasoning

All items below are **proposals**, not additional approved decisions. Existing approved contracts listed in Section 8 are constraints and are not reopened.

### Proposed package and types

**PROPOSED:** Place transport models under `com.humanit.challenge.dto.client` and `com.humanit.challenge.dto.document`, with dedicated mappers under `com.humanit.challenge.mapper`. Keep the package names independent of the `domain` package. Suggested types:

| Purpose | Proposed DTO | Required contract |
|---|---|---|
| Create Client | `ClientCreateRequest` | Required nonblank names/tax ID/phone, required valid email; optional `List<DocumentCreateRequest>`; no Client ID or version. Nested creation uses the same owner-free, ID/version-free Document creation contract. |
| Full Client update (PUT) | `ClientPutRequest` | Required expected Client version and all editable Client scalars; optional/presence-aware `documents`; nested existing/new items represented by `ClientDocumentPutRequest`. |
| Partial Client update (PATCH) | `ClientPatchRequest` | Required expected Client version; each editable field represented as absent/null/value; optional/presence-aware nested `documents` using `ClientDocumentPatchRequest`. |
| Client response | `ClientResponse` | Generated ID, editable scalar values, current version; nested Documents only if the Tech Lead approves that response contract. |
| Create Document | `DocumentCreateRequest` | Required number and expiration date; optional description; no owner, ID or version. Parent ID and `X-Client-Version` belong to the later HTTP/use-case boundary. |
| Full Document update (PUT) | `DocumentPutRequest` | Required expected Document version, number and expiration date; optional description; no ID or owner field. |
| Partial Document update (PATCH) | `DocumentPatchRequest` | Required expected Document version; number/date/description represented as absent/null/value, with explicit null rejected for required fields and clearing optional description. |
| Document response | `DocumentResponse` | Generated ID, number, description, expiration date and current version; no nested `Client` object. Whether to include scalar `clientId` requires contract approval. |

**PROPOSED:** Use separate nested `ClientDocumentPutRequest` and `ClientDocumentPatchRequest` types rather than one loosely constrained nested DTO. PUT nested entries carry optional `id` and conditional `version`; ID absent means create, ID present means update and requires version. PATCH existing entries require both ID and version; new entries forbid both and require number and expiration date. Existing IDs are matched and ownership checked by the later service workflow, not by the mapper.

**PROPOSED:** The Client response embeds `DocumentResponse` entries only if the approved Client GET/create response is intended to include the aggregate. `DocumentResponse` must not embed Client, preventing recursive DTO graphs. If nested Documents are not part of the Client response contract, prefer a flat Client response; T2.2 must not infer a public JSON contract merely from the JPA relationship.

### Validation strategy

1. **VERIFIED requirement / PROPOSED enforcement:** Use Jakarta Bean Validation constraints on separate DTOs, rather than validation groups where distinct request classes already represent POST, PUT and PATCH. Add `@Valid` for each nested list entry and class-level constraints for conditional ID/version combinations.
2. **VERIFIED requirement / PROPOSED enforcement:** Client create and PUT require non-null, nonblank first/last name, tax identifier and phone; email is non-null and nonblank and must pass email-format validation. Document create and PUT require a nonblank number and non-null expiration date. `description` remains nullable/optional. Expiration dates in the past remain valid; do not add a future-date constraint.
3. **VERIFIED requirement / PROPOSED enforcement:** POST has no body version on Client or Document. PUT/PATCH request DTOs carry the required expected version fields. Document POST's required `X-Client-Version` and later DELETE headers are HTTP-level inputs, not fields in these DTOs. T2.2 does not add security/login DTOs.
4. **PROPOSED:** Validate expected version DTO values as non-null and non-negative; JPA owns the persisted version and the mapper must never set it. Negative values are treated as malformed/invalid 400 candidates. **APPROVAL REQUIRED:** Requirements specify missing/malformed/invalid versions as 400 but do not define whether negative values count as invalid.
5. **VERIFIED requirement / PROPOSED enforcement:** For PATCH, validate the supplied inner value only when present. Explicit null on a required field is invalid; explicit null on `description` means clear; omitted field means unchanged. For PATCH `documents`, omitted means unchanged, empty means preserve, explicit null is invalid. For Client PUT, preserve the approved omitted/empty collection behavior and reject explicit null as recorded by the requirements.
6. **PROPOSED:** Do not attach repository-backed uniqueness validators to the DTOs. They would couple validation/mapping to database access and still be race-prone; service/persistence constraints and later exception handling enforce uniqueness and 409 semantics.
7. **APPROVAL REQUIRED:** Requirements do not declare API maximum lengths. Existing entity columns give documented persistence lengths (Client first/last 100, tax 50, email 255, phone 50; Document number 100 and description 1000). Decide whether DTOs should reject longer values with `@Size` at the request boundary or leave this to persistence/business error handling. Do not add country-specific phone/tax formats.
8. **PROPOSED:** Standard Jackson date binding to `LocalDate` should accept ISO `YYYY-MM-DD`; unparsable JSON dates are request-binding errors handled by the later HTTP boundary. T2.2 should test the declared DTO type/validation and, if JSON representation is decided, its deserialization independently.

### PATCH representation and mapper architecture

**PROPOSED — preferred representation:** Define a small presence-aware value abstraction (`PatchValue<T>` or equivalent) with three states: `ABSENT`, `PRESENT_NULL`, and `PRESENT_VALUE`. Bind each PATCH property so ordinary omitted JSON fields become absent and explicit JSON `null` becomes present-null. A plain `Optional<T>` or nullable field is insufficient because it conflates states. Apply a custom Jackson deserializer/creator or select a compatible existing JSON-nullable type; verify the representation through JSON binding tests.

**APPROVAL REQUIRED:** Choose a dependency-free project-owned presence wrapper with Jackson binding, or approve a dependency such as a JSON nullable wrapper. The currently inspected project artifacts do not establish a JSON-nullable dependency or an approved binding convention. Do not silently add a library or weaken the null/omission contract.

**PROPOSED — mapper methods/responsibilities:**

- `ClientMapper`: construct a new Client from `ClientCreateRequest`; map an already loaded Client to `ClientResponse`; update only editable scalar values on an existing managed Client for PUT/PATCH. Never replace an existing entity or its collection on update.
- `DocumentMapper`: construct a new Document from create/nested-create request; map a loaded Document to `DocumentResponse`; update editable scalar values on the supplied existing managed Document for PUT/PATCH. Never set its ID, version or Client association.
- Use mapping methods with explicit target arguments (conceptually `updateFrom(request, managedTarget)`) to preserve Java object identity and JPA persistence-context identity. Mapper does not look up the target by ID; the caller/use case resolves it.
- The JPA provider alone assigns generated IDs and increments `@Version`. Mapping must ignore request IDs/version as mutation targets; version values are expected-state inputs for a later use-case concurrency check, not values to copy onto entities.
- Client aggregate orchestration later resolves nested IDs, rejects unknown/foreign IDs, checks per-document expected versions, and applies the rules atomically in a service. The mapper only maps scalar values onto the exact target the service supplies and creates new nested entities. This preserves the D-11 separation and avoids repository/business workflows in mappers.
- For new aggregate members, mapping may use the existing `Client.addDocument` relationship helper to assemble a new graph and preserve the owning-side invariant. It must not invoke `removeDocument`, infer deletion from omission, assign an existing Document to a different Client, or access repositories. Any ownership guard failure is not translated into HTTP behavior in the mapper.

**PROPOSED — lazy loading/serialization:** Mappers perform no repository calls or hidden fetches. If `ClientResponse` includes nested Documents, its caller must obtain a Client with the collection intentionally loaded; if the response is flat, the mapper must not touch `getDocuments()`. `DocumentResponse` contains no Client entity/reference, eliminating recursion and avoiding traversal of the LAZY `Document.client`. Later service/repository work determines the fetch plan; do not solve fetching inside a mapper.

### T2.2 boundaries and compatibility

- **VERIFIED constraint / PROPOSED application:** Entities remain persistence-focused; no DTO methods or validation annotations are added to entities. Repositories remain the only database-access abstraction. Mappers contain transformation only, with no repositories, HTTP status handling, transactions, ownership-resolution workflow or business orchestration.
- **VERIFIED constraint:** Do not add services, REST resources/controllers, auth DTOs, migrations or endpoint workflows in T2.2. D-11 says services are introduced only when an actual later use case requires them.
- **VERIFIED constraint:** Preserve T2.1 mapping, lazy relationships, generated identity, version ownership, explicit deletion behavior, immutable Document ownership, database constraints and accepted tests. No refactor of T2.1 behavior is part of this proposal.

### Testing strategy for the approved implementation

**PROPOSED:** Add focused tests in DTO and mapper test packages, independent of endpoint/service integration:

- Validator tests for each create/PUT/ PATCH DTO: required null/blank fields, email malformed values, optional description, valid/expired dates, invalid missing/negative version as approved, and no accidental country-specific formats.
- Nested validation tests for `@Valid`, existing nested ID/version pairs, new nested item constraints, forbidden combinations (ID without version, version without ID), required values on new nested entries, and supplied field null behavior.
- JSON binding tests for PATCH absent versus explicit null versus explicit value, and for `documents` absent/empty/null. Include ISO LocalDate success and invalid date binding if the JSON representation is selected in T2.2.
- Mapper unit tests for create mapping, response mapping, scalar PUT/PATCH updates, omitted fields untouched, description clear, and explicit invalid null rejected before mapper invocation.
- Identity tests assert update uses the same target instance and leaves generated ID, version, and preexisting document identities unchanged. For mapper contract, don't set entity version from the request.
- Nested mapping tests cover creating a new Document through Client with correct bidirectional association; mapping an existing target changes only scalar properties; no implicit deletion; no owner reassignment; existing `Client.addDocument` transfer protection remains effective.
- Lazy/recursion tests ensure `DocumentResponse` has no recursively nested Client and that a flat mapper does not traverse lazy associations. If approved Client response nests documents, test the loaded-graph precondition and nested output.

**VERIFIED requirement:** T2.2 acceptance calls for required-field and email validation, expired Document acceptance, optional description, mapper separation and completeness. **PENDING VERIFICATION:** These proposed tests have not been implemented or run; this report is architecture only.

### Alternatives, trade-offs and assumptions

- **PROPOSED alternative — one generic DTO with validation groups:** Rejected in favor of separate create/PUT/PATCH/response types because requiredness and null semantics differ materially, and the approved requirements explicitly call for separate DTO classes. Separate types cost some repetition but make contracts visible and reduce conditional annotation complexity.
- **PROPOSED alternative — map PATCH DTO directly into a fresh entity:** Rejected because it loses identity, can overwrite omitted fields with null, may detach/replace nested instances, and risks ownership reassignment. In-place updates on supplied managed targets are preferred.
- **PROPOSED alternative — mapper matches nested IDs against repositories:** Rejected because it violates D-11, creates hidden persistence/workflow behavior and makes mapper tests depend on a database. Resolve IDs in the later service, then map into the resolved managed instance.
- **PROPOSED assumption:** The `ClientResponse`/`DocumentResponse` JSON fields and nested response shape remain unapproved until the Tech Lead resolves the response-contract question below. The table above is a candidate design, not a claim that the public response shape is already specified.

### Decisions requiring Human Tech Lead approval before implementation

1. **PATCH JSON presence mechanism:** Approve project-owned tri-state Jackson binding, or authorize a specific JSON-nullable dependency/type. This is necessary to preserve required omitted-vs-null semantics.
2. **Response contract:** Confirm whether `ClientResponse` includes nested Document responses and whether `DocumentResponse` includes `clientId`; otherwise approve flat response DTOs. This affects lazy fetching and public JSON shape.
3. **Client PUT nested fields:** Confirm that every nested PUT Document item (whether ID-bearing update or new item) must provide all required editable fields (`number`, `expirationDate`), with optional `description`, rather than treating nested PUT items as patches. The main contract states Client PUT nested Documents update/create but does not fully define each nested item's scalar requiredness.
4. **Client create explicit `documents: null`:** Confirm whether it is rejected or treated as omitted. Client `documents` is optional; the explicit null rejection is clear for Client PATCH/update semantics but is not unambiguously specified for POST Client.
5. **Version and string constraints:** Confirm negative versions are invalid 400 and approve whether DTOs enforce entity column lengths with `@Size`. No country-specific formats should be introduced.

**VERIFIED:** Approval of this proposal is a prerequisite to implementation. No approval is claimed in this report.

## 11. Final status

**PROPOSAL COMPLETE — AWAITING HUMAN TECH LEAD REVIEW.** DTO families, validation direction, mapping responsibilities, test strategy and explicit design questions are presented. No application implementation or tests were changed or run.

## 12. Outstanding issues

1. **PENDING VERIFICATION / approval required:** Decide the PATCH absent/null/value representation and JSON binding mechanism before implementation.
2. **PENDING VERIFICATION / approval required:** Decide Client and Document response payload shape, including whether Client embeds Documents and whether Document exposes `clientId`.
3. **PENDING VERIFICATION / approval required:** Confirm required scalar completeness for nested Client PUT items.
4. **PENDING VERIFICATION / approval required:** Resolve explicit null behavior for `ClientCreateRequest.documents`.
5. **PENDING VERIFICATION / approval required:** Confirm negative-version validation and API length constraints.
6. **VERIFIED:** D-11/D-12 and accepted T2.1 boundaries remain unchanged; no code implementation is authorized by this proposal alone.
7. **PENDING VERIFICATION:** No tests/builds were run for this documentation-only architecture execution.

## 13. Handoff to next agent

- **VERIFIED:** Human Tech Lead review/approval is required before Developer implementation starts. The Developer should implement only the approved DTO, validation and mapper scope and preserve T2.1 behavior.
- **PENDING VERIFICATION:** Resolve the five approval questions in Section 10. If the Tech Lead leaves any open, record the decision or split the implementation before coding rather than inventing contract behavior.
- **VERIFIED:** Keep repository access, ownership resolution, aggregate merging, transactions and HTTP workflow outside mappers; no service/resource/controller work is included in T2.2.
- **VERIFIED:** A future Tester should independently inspect requirements and implementation and run verification before reading Developer conclusions. A future Reviewer should independently audit before reading Developer/Tester reports or this proposal.

---
**Report status:** Written for `T2.2-EXEC-004-ARCH-01`. Previous primary preserved at `agent-reports/T2.2/history/architect-002.md`; current primary report path: `agent-reports/T2.2/architect-proposal.md`.
