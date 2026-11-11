# Architect Proposal — T2.9 Client Listing N+1

## 1. Task ID

- **VERIFIED** (direct tasking): `T2.9` — propose the simplest targeted fix for the confirmed query-count growth in paginated `GET /api/clients`.

## 2. Execution identifier and date

- **VERIFIED** (this execution): `T2.9-ARCH-001`.
- **VERIFIED** (execution environment): 2026-11-11.

## 3. Agent role and model

- **VERIFIED** (execution environment): Architect; `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct tasking): Architecture proposal only. Preserve REST contracts, DTOs, pagination, ordering, and total counts; avoid collection `JOIN FETCH` with pagination; prefer minimal repository/service changes; reuse the existing diagnostic test; define measurable acceptance criteria; do not expand to unrelated endpoints.
- **VERIFIED** (role constraints and `agent-reports/README.md`): No application/test/configuration/plan/requirements changes, no Git operations, and no self-approval. Only this Architect report is authorized for writing.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): This report follows the mandatory 13-section format. No earlier `T2.9/architect-proposal.md` was present when checked, so no previous execution required archiving.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md` — mandatory report format, evidence markers, execution history rules.
- **VERIFIED**: `agent-reports/PERF-NPLUS1-001/reviewer-report.md` — earlier mapping/call-path review and options.
- **VERIFIED**: `agent-reports/PERF-NPLUS1-002/developer-report.md` — recorded runtime counts from the diagnostic test.
- **VERIFIED**: `src/test/java/com/humanit/challenge/controller/NPlusOneQueryCountTest.java` — fixture, request sizes, Hibernate statistics measurement, and existing response assertions.
- **VERIFIED**: `src/main/java/com/humanit/challenge/domain/Client.java` — lazy `documents` collection mapping.
- **VERIFIED**: `src/main/java/com/humanit/challenge/domain/Document.java` — owning `ManyToOne` relationship and foreign key.
- **VERIFIED**: `src/main/java/com/humanit/challenge/repository/ClientRepository.java` — no existing pageable fetch query for the listing path.
- **VERIFIED**: `src/main/java/com/humanit/challenge/service/ClientService.java` — `list(Pageable)` delegates to `findAll(pageable)` and maps page content within a read-only transaction.
- **VERIFIED**: `src/main/java/com/humanit/challenge/mapper/ClientMapper.java` — `toResponse` traverses `client.getDocuments()`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/ClientController.java` — page ordering is Client ID ascending and maximum page size is 100.
- **VERIFIED**: `src/main/java/com/humanit/challenge/dto/common/PageResponse.java` — response page metadata includes page, size, total elements, and total pages.
- **VERIFIED**: `pom.xml` — Spring Data JPA is present; Hibernate is used by the test diagnostic.

## 6. Files changed

- **VERIFIED**: Created only `agent-reports/T2.9/architect-proposal.md`.
- **VERIFIED**: No production source, tests, configuration, requirements, implementation plans, decision log, or other agent report was changed.

## 7. Commands executed

- **VERIFIED**: No shell or application/test commands were executed. Inspection and report creation used workspace file tools; no Git operation was performed.

## 8. Evidence and observed results

- **VERIFIED** (`agent-reports/PERF-NPLUS1-002/developer-report.md`, Section 8): The diagnostic measured prepared-statement counts of 3, 7, and 22 for Client-list pages returning 1, 5, and 20 Clients respectively. The report records successful responses and at least one Document per returned Client.
- **VERIFIED** (`NPlusOneQueryCountTest.java`): Hibernate statistics are enabled for this diagnostic test; it flushes and clears the persistence context and clears statistics before each measured GET. It currently prints counts and verifies content but does not assert a query-count threshold or pagination metadata/order.
- **VERIFIED** (`Client.java`, `ClientService.java`, `ClientMapper.java`): The pageable root query leaves the `documents` collection lazy; list mapping reads that collection once for each returned Client.
- **VERIFIED** (`ClientController.java`): Client listing explicitly orders by `id` ascending and allows page sizes up to 100.
- **VERIFIED** (`PageResponse.java`): Existing response metadata carries page, size, total element count, and total pages; those DTO fields need no change.
- **RECONSTRUCTED** (arithmetic over the 1/5/20 counts, supported by the inspected mapping path): The observations fit two page-level statements plus one additional collection-load statement per returned Client (`2 + n`). The reports do not provide a per-statement SQL trace, so the individual statement identities are inferred rather than separately proven by SQL output.
- **PENDING VERIFICATION**: Post-fix query counts and SQL shape have not been measured in this architecture-only execution.

## 9. Findings and severity

1. **Major — VERIFIED measurement / RECONSTRUCTED cause:** The diagnostic reports linear growth from 3 to 7 to 22 statements as page content grows from 1 to 5 to 20. The inspected mapping path explains this as per-Client lazy collection loading during response mapping. The runtime count trend is verified by the Developer report; the exact statement decomposition is reconstructed because no SQL trace was inspected here.
2. **VERIFIED — pagination risk to avoid:** Client list pagination is currently applied to root Clients. A collection fetch join in this pageable query can multiply root rows and complicate page boundaries and count semantics; it is expressly excluded by task requirements.
3. **VERIFIED — contract constraint:** `ClientResponse` mapping includes Documents and `PageResponse` metadata; solution must retain the embedded Document list and existing pagination behavior rather than omit collection data or change response shape.
4. **NOT AVAILABLE**: This execution did not independently run the test, inspect post-fix database execution, or establish a production database's parameter limits.

## 10. Decisions and reasoning

### Recommended targeted change

- **PROPOSED**: Add Hibernate collection batch fetching specifically to `Client.documents`, using `@org.hibernate.annotations.BatchSize(size = 100)` on that association. Leave `ClientRepository`, `ClientService.list`, the mapper, endpoint, DTOs, and pagination configuration unchanged.
- **VERIFIED basis**: `Client.documents` is the one lazy collection traversed by list mapping; maximum API page size is 100. A per-association batch size equal to that maximum aims to load documents for a full page in one collection-fetch batch, instead of one collection query per Client.
- **RECONSTRUCTED expected behavior**: For a page with up to 100 Clients and no pre-initialized collections in the request persistence context, Hibernate should batch-initialize the page's eligible `documents` collections in one batch. With the observed two page-level statements, the target is at most three prepared statements for page sizes 1, 5, 20, and 100. This is a prediction to verify, not an observed result.
- **VERIFIED scope fit**: This is a single association mapping change and does not alter repository/service logic, REST/DTO contracts, page ordering, page boundaries, or total-count calculation. The annotation is scoped to this collection role, unlike a global Hibernate batch-fetch setting.

### Alternatives and trade-offs

- **PROPOSED alternative, not preferred for the smallest change**: Keep the pageable root query, then add a separate bulk query for Documents by the page's Client IDs and assemble response DTOs from those results. This can keep query count bounded and precisely target listing, but requires repository/service/DTO-mapping coordination and careful preservation of document list semantics.
- **VERIFIED rejected approach**: Do not add collection `JOIN FETCH` to the pageable Client root query. It violates the explicit constraint and risks duplicated roots, incorrect pagination/counts, or in-memory pagination.
- **PROPOSED trade-off/risk**: `@BatchSize` is Hibernate-specific and affects lazy initialization of this collection role wherever it is used, not only the listing endpoint. It preserves lazy loading and limits the behavioral scope to `Client.documents`, but batching can load unused sibling collections that are present in the same persistence context. A batch size of 100 also needs verification against the configured database and actual Hibernate SQL behavior; if database bind limits or runtime behavior make this unsuitable, evaluate the two-step bulk-load alternative rather than adding global configuration.

### Assumptions

- **PENDING VERIFICATION**: The Hibernate version managed by the Spring Boot dependency set supports `@BatchSize` on a collection role with the expected batching semantics; confirm by compile/test during authorized implementation.
- **PENDING VERIFICATION**: The diagnostic's measured H2 behavior is representative enough to establish the local acceptance threshold; separately validate on the deployment database if required by the Tech Lead.
- **VERIFIED**: No approval for implementation or final acceptance is recorded or implied in this proposal.

## 11. Final status

**PROPOSAL COMPLETE — AWAITING TECH LEAD APPROVAL.** The recommended minimal change is association-scoped Hibernate batch fetching for `Client.documents`, at size 100, followed by reuse and strengthening of the existing diagnostic test. No implementation or test changes were made. This Architect does not self-approve.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Authorized implementation and compilation of the association-level batch annotation.
- **PENDING VERIFICATION**: Re-run `NPlusOneQueryCountTest` after the fix and confirm the statement threshold for page sizes 1, 5, 20, and ideally 100.
- **PENDING VERIFICATION**: Extend assertions in the existing diagnostic test to cover ordering, page/size metadata, total elements, total pages, and complete embedded Documents without changing API contracts.
- **PENDING VERIFICATION**: Confirm the chosen batch size against the target runtime/database and ensure the successful request's response content remains unchanged.
- **VERIFIED**: No unrelated endpoint is included in the proposed scope; no code changes, tests, Git operations, or Tech Lead approval occurred in this execution.

## 13. Handoff to next agent

- **VERIFIED**: Report path: `agent-reports/T2.9/architect-proposal.md`.
- **PROPOSED**: Wait for Tech Lead approval before implementation. After approval, make only the targeted `Client.documents` batch-fetch mapping change and update/reuse `NPlusOneQueryCountTest` to assert the acceptance criteria below.
- **PROPOSED acceptance criteria** (all require future verification):
  1. For GET `/api/clients` with non-empty Document collections, `Statistics.getPrepareStatementCount()` is **no more than 3** for returned page sizes 1, 5, 20, and 100 in the diagnostic setup. This bounds the count to the two observed page-level statements plus one batched collection load, rather than the previous 3/7/22 growth.
  2. Responses still return the requested number of Clients and every embedded Document with the existing Client/Document DTO fields.
  3. For unchanged fixtures, Client IDs remain in ascending order; page index and requested size remain correct; total elements and total pages match the fixture population and existing contract.
  4. The test itself asserts the count threshold and content/page invariants, runs successfully, and captures counts after request setup/statistics reset as it currently does. Verify 100-client behavior by using the existing diagnostic test with enough fixtures to fill a maximum-size page.
  5. The pageable root query remains free of collection `JOIN FETCH`; no unrelated endpoint or API contract is changed.
- **PENDING VERIFICATION**: If the 100-client test exceeds the threshold or batch loading is unsupported by the target DB, stop and present the observed evidence before selecting the two-step bulk-load alternative.
- **VERIFIED**: Final status is proposal only; outstanding findings are post-change query-count verification, pagination/contract assertions, and target-database batch behavior. Final acceptance belongs solely to the human Tech Lead.
