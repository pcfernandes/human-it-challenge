# Architect Proposal — T2.9 Client Listing N+1 (Revised: Two-Step Bulk Fetch)

## 1. Task ID

- **VERIFIED** (direct tasking): `T2.9` — revise the architecture proposal for query-count growth in paginated `GET /api/clients` using the Tech Lead's Two-Step Bulk Fetching decision.

## 2. Execution identifier and date

- **VERIFIED** (this execution): `T2.9-ARCH-002`.
- **VERIFIED** (execution environment): 2026-11-11.
- **VERIFIED** (`agent-reports/README.md`, Section 6): Prior execution `T2.9-ARCH-001` was archived unchanged as `agent-reports/T2.9/history/architect-001.md` before this primary report was replaced.

## 3. Agent role and model

- **VERIFIED** (execution environment): Architect; `opencode-go/gpt-6-luna`.

## 4. Authorized scope

- **VERIFIED** (direct tasking): Revise the proposal to use Two-Step Bulk Fetching: page Clients, collect IDs, query Documents by those IDs once, group by `clientId`, map responses using explicit Document lists, and skip the Document query for empty pages.
- **VERIFIED** (direct tasking): Preserve REST contracts, DTOs, Client ordering and pagination; do not use collection `JOIN FETCH` on the paginated query, do not use `@BatchSize`, make no schema changes, keep implementation minimal, define query-count and regression-test criteria, and describe required `implementation-plan.md` updates.
- **VERIFIED** (direct tasking and role constraints): Architecture/report changes only. Do not modify `implementation-plan.md`, source, tests, configuration, requirements or other agent reports; perform no shell commands or Git operations; do not self-approve.
- **VERIFIED** (`agent-reports/README.md`, Sections 4–6): The report uses the required 13 sections and the prior primary was preserved in the prescribed history location.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md` — mandatory 13-section format, evidence markers and report-history convention.
- **VERIFIED**: Previous `agent-reports/T2.9/architect-proposal.md` — prior proposal and execution identifier, archived unchanged as `agent-reports/T2.9/history/architect-001.md`.
- **VERIFIED**: `implementation-plan.md`, Sections 2, 5, 6 and 11 — approval workflow, Phase 2 task list and pagination requirements; no T2.9 entry is present in the inspected task list.
- **VERIFIED**: `agent-reports/PERF-NPLUS1-002/developer-report.md`, Section 8 — historical H2 query-count measurements.
- **VERIFIED**: `src/main/java/com/humanit/challenge/service/ClientService.java` — listing currently calls `clientRepository.findAll(pageable).map(ClientMapper::toResponse)` in a read-only transaction; the same mapper is used in other CRUD paths.
- **VERIFIED**: `src/main/java/com/humanit/challenge/repository/ClientRepository.java` and `DocumentRepository.java` — existing pageable Client repository and Document repository; no bulk-by-client-ID method is present.
- **VERIFIED**: `src/main/java/com/humanit/challenge/domain/Client.java` and `Document.java` — lazy Client-to-Documents collection and Document's owning Client reference.
- **VERIFIED**: `src/main/java/com/humanit/challenge/mapper/ClientMapper.java` and `DocumentMapper.java` — current Client response mapping traverses the entity collection; Document DTO conversion is available as an in-memory mapper.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/ClientController.java`, `dto/common/PageResponse.java`, `dto/client/ClientResponse.java`, and `dto/document/DocumentResponse.java` — current ID-ascending pagination, page envelope and nested DTO fields.
- **VERIFIED**: `src/test/java/com/humanit/challenge/controller/NPlusOneQueryCountTest.java` — current 20-client diagnostic, measured page sizes 1/5/20, and content assertions.
- **VERIFIED**: `src/test/java/com/humanit/challenge/service/ClientServiceTest.java` and `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java` — existing service/controller tests for ordered pagination, page metadata, boundaries and response shape.

## 6. Files changed

- **VERIFIED**: Replaced `agent-reports/T2.9/architect-proposal.md` with this revised proposal and copied the prior primary report unchanged to `agent-reports/T2.9/history/architect-001.md`.
- **VERIFIED**: No other files were changed. In particular, `implementation-plan.md`, production source, tests, configuration, requirements, decisions and other agent reports remain outside this change.

## 7. Commands executed

- **VERIFIED**: No shell, build, test or Git commands were executed. Files were inspected and reports maintained using workspace file tools only.

## 8. Evidence and observed results

- **VERIFIED** (`agent-reports/PERF-NPLUS1-002/developer-report.md`, Section 8): Existing H2 diagnostic results were 3, 7 and 22 prepared statements for `GET /api/clients` requests returning 1, 5 and 20 Clients respectively, each with at least one Document. Those are historical results, not post-fix measurements.
- **VERIFIED** (`NPlusOneQueryCountTest.java`): The diagnostic currently seeds 20 Clients, measures only page sizes 1/5/20, clears the persistence context and Hibernate statistics before each request, asserts content sizes and presence of Documents, and prints rather than asserts counts.
- **VERIFIED** (`ClientService.java`, `ClientMapper.java`): Listing maps each page Client with the existing `ClientMapper.toResponse(Client)`, which reads the lazy entity collection; other CRUD methods also use this existing mapper.
- **VERIFIED** (`ClientRepository.java`, `DocumentRepository.java`): The pageable Client fetch is supported by existing `JpaRepository` methods, while the Document repository currently has no bulk query for a collection of Client IDs.
- **VERIFIED** (`ClientController.java`, `ClientServiceTest.java`, `ClientControllerTest.java`): Listing requests sort Clients by ID ascending; tests already exercise page metadata, Client ordering and empty/out-of-range response envelopes.
- **VERIFIED** (`Client.java`, `ClientResponse.java`): Client Documents are lazy and the response contract embeds `List<DocumentResponse>`; no schema or DTO change is needed for the proposed assembly path.
- **VERIFIED** (direct tasking): The Tech Lead decision for this task is Two-Step Bulk Fetching instead of `@BatchSize`. This report records that selected design and does not imply implementation or final-acceptance approval.
- **RECONSTRUCTED** (historical counts and inspected mapping): The prior statement-count pattern is consistent with two page-level statements plus one additional lazy-collection load per returned Client (`2 + n`); no per-statement SQL trace was inspected, so this decomposition remains an inference.
- **PENDING VERIFICATION**: Bulk-fetch query shape, post-change query counts, nested Document ordering parity and target-database behavior require authorized implementation and tests.

## 9. Findings and severity

1. **Major — VERIFIED measurements / RECONSTRUCTED cause:** The reported counts grow from 3 to 7 to 22 for pages of 1, 5 and 20 Clients. The existing lazy collection mapping explains the growth, although the exact SQL statement identities were not proven by an inspected SQL trace.
2. **VERIFIED — required pagination safeguard:** Keep the pageable Client root query and apply the existing `Pageable`; do not fetch-join the collection in that query. Client ordering and page metadata must continue to be derived from the existing `Page<Client>`.
3. **VERIFIED — DTO/mapping safeguard:** Preserve the nested `ClientResponse.documents` shape. Add an explicit-list mapping path for listing while leaving the existing entity-collection mapper in place for other CRUD operations.
4. **PENDING VERIFICATION — ordering:** The Client list's ascending ID ordering is explicit. The current `Client.documents` list has no observed `@OrderBy`/`@OrderColumn`; verify the Document order returned by the bulk query against existing response behavior and preserve the observed order. Do not claim a new nested ordering contract without evidence.
5. **NOT AVAILABLE**: No post-fix run, query trace, target-database parameter-limit check, implementation-plan update, or implementation authorization was performed in this architecture/report-only execution.

## 10. Decisions and reasoning

### Revised design — Two-Step Bulk Fetching

1. **VERIFIED** (direct tasking): Use the existing `ClientRepository` pageable fetch first, retaining its incoming `Pageable` and ID-ascending sort.
2. **VERIFIED** (direct tasking): Collect the IDs from the returned page's content in page order. If the page content is empty, do not call the Documents repository.
3. **VERIFIED** (direct tasking): For a non-empty page, run one Document repository query with an `IN` predicate over those Client IDs. The implementation may use a derived repository method or an explicit JPQL query; it must remain one bulk query for the page and must not issue a query per Client.
4. **VERIFIED** (direct tasking): Group the returned Documents by their owning `clientId`, retaining a consistent per-Client Document order compatible with the existing response behavior.
5. **VERIFIED** (direct tasking): Map each Client in the original page content order to `ClientResponse` using the explicit Document list for that Client (or an empty list when it owns none). Preserve the page metadata from the original Client `Page`.
6. **VERIFIED** (direct tasking): Leave the existing `ClientMapper.toResponse(Client)` path available and unchanged for other CRUD operations. A narrowly scoped overload/helper accepting an explicit `List<Document>` is the minimal mapping extension; repository access and grouping remain in the service/repository boundary, not in the mapper.

### Minimal change boundaries and trade-offs

- **VERIFIED** (observed source and revised design): Expected production scope is a bulk-by-Client-ID method in `DocumentRepository`, list-only orchestration in `ClientService`, and an explicit-Document-list mapping overload in `ClientMapper`. Keep the existing Client pageable repository method, REST controller, DTOs, entity mappings and schema unchanged.
- **VERIFIED** (direct tasking): Do not add collection `JOIN FETCH` to the pageable Client query, do not add `@BatchSize`, and do not make database schema changes.
- **RECONSTRUCTED** (existing 1/5/20 measurements and two-step design): With one Client content query, one count query when Spring Data executes it, and one bulk Document query for a non-empty Client page, expected statement count is at most three regardless of page size (subject to DB/runtime behavior). This is an acceptance target, not a measured result.
- **VERIFIED** (direct tasking): Empty page content skips the Document query. The Client page query and Spring Data's optional count behavior remain unchanged; query counts for empty results are bounded by the existing root-page/count behavior rather than requiring an exact count.
- **PENDING VERIFICATION**: A page has at most 100 Client IDs under the existing endpoint limit. Confirm the target database/runtime accepts the IN bind count and that the provider issues one query; do not solve any limit by changing the API page-size contract or schema.
- **PENDING VERIFICATION**: Since the current association has no observed explicit ordering mapping, use a deterministic bulk-query/grouping order only if it matches existing response behavior; verify child order in regression fixtures before acceptance.

### Query-count and regression-test criteria

- **PENDING VERIFICATION — query-count criterion**: Strengthen/reuse `NPlusOneQueryCountTest` to seed enough Clients to fill a page of 100 and assert at most **3 prepared statements** for non-empty `GET /api/clients` pages of sizes 1, 5, 20 and 100, each with Documents. Reset statistics after fixture setup as the existing diagnostic does. This represents the two existing page-level statements plus one bulk Documents query; investigate any excess rather than weakening the bound.
- **PENDING VERIFICATION — single/skip query criterion**: Verify that one non-empty page invokes/issues exactly one bulk Documents query, independent of page size, and an empty/out-of-range Client page issues no Documents query. Use a focused repository/service interaction assertion and/or SQL inspection in addition to aggregate statement counts, since a total count alone does not prove the query predicate or absence of per-Client loads.
- **PENDING VERIFICATION — content/contracts**: Assert complete embedded Document DTO values and `clientId` ownership, including Clients with multiple Documents and Clients with none. Keep the Client response fields and PageResponse DTO unchanged.
- **PENDING VERIFICATION — pagination/order**: Assert Client IDs remain ascending, returned content matches requested page boundaries, `page` and `size` remain correct, `totalElements`/`totalPages` remain unchanged, and empty/out-of-range pages retain the existing envelope. Compare nested Document ordering with the pre-change behavior for stable fixtures.
- **PENDING VERIFICATION — no regressions**: Verify no collection join fetch on the pageable Client query, no `@BatchSize`, no schema change, and no unintended extra query during response serialization. Run existing Client CRUD/service/controller tests in addition to the strengthened diagnostic.

### `implementation-plan.md` update requirements (not performed here)

- **VERIFIED** (direct tasking): This execution must not modify `implementation-plan.md`; the following are requirements for a separately authorized planning update.
- **VERIFIED** (`implementation-plan.md`, Sections 2 and 6): The plan uses explicit approval gates and currently lists T2.8 as the pagination task; no T2.9 task entry is present in the inspected task list.
- **PENDING VERIFICATION / REQUIRED UPDATE**: Add a T2.9 entry (or otherwise formally record T2.9 within the appropriate phase) stating the focused Client-list query-count objective and the approved two-step sequence: pageable Client fetch, collect IDs, one Documents `IN` query for non-empty pages, group by Client ID and explicit-list response mapping; skip the Documents query for empty pages.
- **PENDING VERIFICATION / REQUIRED UPDATE**: Record exclusions: preserve REST/DTO/schema/pagination/order contracts; no collection join fetch, no `@BatchSize`, no schema changes, no unrelated endpoint optimization; preserve the existing mapper for other CRUD operations.
- **PENDING VERIFICATION / REQUIRED UPDATE**: Add measurable acceptance criteria for the <=3 statement threshold at sizes 1/5/20/100, exactly one bulk Documents query for non-empty pages, zero bulk Documents queries for empty pages, complete nested response content, Client ordering/page metadata/boundaries and regression coverage.
- **PENDING VERIFICATION / REQUIRED UPDATE**: Keep governance gates explicit: plan change is separate from this report-only task, implementation starts only when authorized, and T2.9 completion still requires tests, an independent Reviewer audit and human Tech Lead acceptance under the plan's workflow.

## 11. Final status

**REVISED ARCHITECTURE PROPOSAL COMPLETE — TWO-STEP BULK FETCH DESIGN RECORDED.** **VERIFIED** (direct tasking): the Tech Lead selected Two-Step Bulk Fetching instead of `@BatchSize`; the proposal follows that decision. The previous report is archived unchanged. No implementation plan, source or test changes were made, no verification commands were run, and no implementation or final acceptance is claimed.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Human Tech Lead authorization for any implementation and for a separate update to the approved `implementation-plan.md`; the direct design decision is not treated as authorization to make those changes in this execution.
- **PENDING VERIFICATION**: Confirm bulk query method/SQL shape, query counts at 1/5/20/100, exact single-query behavior, and empty-page query skipping after implementation.
- **PENDING VERIFICATION**: Verify complete response payload, Client ordering, pagination metadata/boundaries and nested Document order parity.
- **PENDING VERIFICATION**: Confirm target-database handling of at most 100 Client IDs in the `IN` predicate.
- **VERIFIED**: No code, test, plan, configuration, requirements or decision-log changes; no shell/test/Git commands; no self-approval.

## 13. Handoff to next agent

- **VERIFIED**: Latest report: `agent-reports/T2.9/architect-proposal.md`; prior execution preserved at `agent-reports/T2.9/history/architect-001.md`.
- **VERIFIED**: The plan-update requirements are documented above; `implementation-plan.md` was intentionally not changed.
- **PENDING VERIFICATION**: After the Tech Lead authorizes the planning/implementation steps, update the plan as authorized, implement only the approved two-step list path, and strengthen/reuse the existing diagnostic plus focused empty-page and mapping/order regression tests.
- **PENDING VERIFICATION**: Tester performs independent verification before reading Developer conclusions; Reviewer audits independently before reading the Developer/Tester conclusions or this proposal, consistent with `agent-reports/README.md`.
- **VERIFIED**: Final acceptance remains solely with the human Tech Lead. This execution stops after reporting.
