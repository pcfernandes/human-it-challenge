# T2.8 Architect Proposal — Pagination, Ordering and Query Behavior

## 1. Task ID

**T2.8 — Pagination, ordering and query behavior.**

## 2. Execution identifier and date

- Execution: `T2.8-EXEC-001-ARCH`
- Date: 2026-11-11
- **VERIFIED:** This is the execution identifier and task scope supplied for this run.

## 3. Agent role and model

- Role: Architect
- Model: `opencode-go/gpt-6-luna`
- **VERIFIED:** Model identity is supplied by the execution environment.

## 4. Authorized scope

- **VERIFIED** (`implementation-plan.md`, T2.8): Analyze and propose pageable list endpoints with page/size defaults 0/20, maximum size 100, ID-ascending ordering, metadata, and documentation readiness.
- **VERIFIED** (direct tasking): Analysis and proposal only; do not change application code, tests, requirements, plans, or governance files. Stop after reporting.
- **PENDING VERIFICATION:** The user reports that T2.1–T2.7 are accepted/merged and T2.7 passed 310 tests. No T2.7 test output or acceptance record was inspected for this execution; these are treated as task context, not independently verified evidence.
- No implementation authorization or Tech Lead approval is recorded or implied by this proposal.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md` — mandatory report structure, evidence markers and history convention.
- **VERIFIED:** `implementation-plan.md` — Phase 2 boundaries and T2.8 scope/acceptance criteria.
- **VERIFIED:** `requirements-decisions.md` — list routes, pagination contract, errors and OpenAPI expectations.
- **VERIFIED:** `requirements.md` — original project requirements.
- **VERIFIED:** `src/main/java/com/humanit/challenge/controller/ClientController.java` — Client list endpoint currently returns a bare `List<ClientResponse>` from `clientService.list()`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/controller/DocumentController.java` — Document list endpoint currently accepts Spring `Pageable` and directly returns Spring `Page<DocumentResponse>`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java` — Client list currently uses `clientRepository.findAll()` and maps the full result to a list.
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/DocumentService.java` — Document list already accepts `Pageable`, scopes by `clientId`, verifies Client existence and maps a repository `Page`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/repository/ClientRepository.java` — extends `JpaRepository<Client, Long>`; inherited pageable `findAll(Pageable)` is available.
- **VERIFIED:** `src/main/java/com/humanit/challenge/repository/DocumentRepository.java` — already provides `Page<Document> findByClientId(Long, Pageable)`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/config/AppPaginationProperties.java` and `src/main/resources/application.yml` — defaults 0/20 and maximum 100 are already configured; the configuration comment assigns HTTP parameter validation to T2.8.
- **VERIFIED:** `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java` — type mismatches and `ResponseStatusException` are mapped to HTTP 400 as RFC 9457 Problem Details.
- **VERIFIED:** `src/main/java/com/humanit/challenge/dto/client/ClientResponse.java` and `src/main/java/com/humanit/challenge/dto/document/DocumentResponse.java` — existing content DTO contracts to preserve.
- **VERIFIED:** `src/main/java/com/humanit/challenge/config/OpenApiConfig.java` — minimal API metadata bean; it does not define endpoint pagination documentation.
- **VERIFIED:** `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java` — existing Client list test expects a JSON array and has no query tests.
- **VERIFIED:** `src/test/java/com/humanit/challenge/controller/DocumentControllerTest.java` — existing Document list test expects Spring Page content and does not verify page/size validation or metadata contract.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java` — has a basic owner-scoped paged-list test.
- **VERIFIED:** `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java` — repository-level Document pagination and ID-ascending order are exercised when an explicitly sorted `PageRequest` is supplied.
- **VERIFIED:** `src/test/java/com/humanit/challenge/config/PaginationPropertiesValidationTest.java` — configuration defaults/limits are tested; its comments explicitly leave HTTP behavior to T2.8.
- **VERIFIED:** `pom.xml` declares Spring Data JPA and springdoc OpenAPI dependencies; no new dependency appears necessary for the proposed design.
- **VERIFIED:** `agent-reports/T2.8/` had no report files at inspection time; no earlier Architect execution was available to archive.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.8/architect-proposal.md` was created as the required Architect report.
- **VERIFIED:** No application source, tests, configuration, migration, requirements, implementation plan, or decision-log files were changed.

### Exact files proposed for implementation changes

1. `src/main/java/com/humanit/challenge/controller/ClientController.java` — accept only `page` and `size`; apply configured defaults; reject invalid values; create a `PageRequest` sorted by `id` ascending; wrap the result in the minimal paginated response. Add small OpenAPI parameter/operation descriptions.
2. `src/main/java/com/humanit/challenge/controller/DocumentController.java` — apply the same query contract and response wrapper to the existing owner-scoped list endpoint. Preserve the existing Client-not-found behavior. Add small OpenAPI parameter/operation descriptions.
3. `src/main/java/com/humanit/challenge/service/ClientService.java` — replace unpaged `list()` with a pageable method returning `Page<ClientResponse>` using inherited `clientRepository.findAll(pageable)` and existing mapping.
4. `src/main/java/com/humanit/challenge/dto/common/PageResponse.java` — add the minimal generic API envelope with exactly `content`, `page`, `size`, `totalElements`, and `totalPages`, created from a Spring `Page<T>`.
5. `src/test/java/com/humanit/challenge/controller/ClientControllerTest.java` — replace array assertions with the paginated contract and cover Client request binding, defaults and invalid parameters.
6. `src/test/java/com/humanit/challenge/controller/DocumentControllerTest.java` — replace raw Spring Page assertions with the paginated contract and cover Document request binding, defaults and invalid parameters.
7. `src/test/java/com/humanit/challenge/service/ClientServiceTest.java` — cover actual Client page content, totals and deterministic ordered paging against persistence.
8. `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java` — extend the existing paged-list coverage for ordering, metadata/boundary behavior and retain owner scoping.

- **VERIFIED:** No `ClientRepository`, `DocumentRepository`, `DocumentService`, `ClientResponse`, `DocumentResponse`, `OpenApiConfig`, application YAML, or `pom.xml` change is required by this design. Existing repository pageable methods and configuration values suffice.
- **PENDING VERIFICATION:** If implementation chooses one end-to-end MockMvc integration test rather than extending the service integration tests listed above, the exact final test-file set should be confirmed by the Developer and Tester. No extra abstraction or test is proposed solely for organization.

## 7. Commands executed

- **VERIFIED:** None. No shell commands were executed; inspection used read-only file tools.

## 8. Evidence and observed results

### Observed current state

- **VERIFIED** (`ClientController.java`, `ClientService.java`): Client collection listing is unpaged and returns a JSON array.
- **VERIFIED** (`DocumentController.java`, `DocumentService.java`, `DocumentRepository.java`): Document listing uses Spring `Pageable`/`Page`, but the controller exposes Spring's Page representation directly. Its pageable argument is not bounded in the controller and accepts the framework's pageable/sort surface rather than only the required `page` and `size` parameters.
- **VERIFIED** (`AppPaginationProperties.java`, `application.yml`): Defaults and maximum-size configuration already exist, but the configuration explicitly says HTTP request validation is T2.8's responsibility.
- **VERIFIED** (`GlobalExceptionHandler.java`): Invalid typed parameters and request-status errors can use the existing Problem Details path; no pagination-specific error handler is needed.
- **VERIFIED** (existing tests listed in Section 5): Document persistence tests cover a manually supplied ascending-ID `PageRequest`; current controller tests do not cover both endpoints' agreed pagination contract, and the Client list test still asserts the old array shape.
- **RECONSTRUCTED:** Because `ClientRepository` extends `JpaRepository`, `findAll(Pageable)` is inherited and can supply paged Client retrieval without adding a repository method. This follows from the inspected interface declaration and Spring Data repository contract.

### Minimal proposed request/response contract

- **VERIFIED** (`requirements-decisions.md`, Pagination section; `implementation-plan.md`, T2.8): Zero-based `page`, default 0; `size`, default 20; maximum size 100; ID ascending; page metadata; oversize request returns 400.
- **PROPOSED:** Accept `page` and `size` only, with defaults resolved from the existing `AppPaginationProperties`. Construct `PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))` in each list controller. Services continue to receive Spring `Pageable`; the response envelope converts the returned `Page<T>`.
- **PROPOSED:** Do not accept caller-supplied `sort` or other dynamic filters. A single fixed sort makes ordering deterministic and keeps the request API intentionally small.

Example default request:

```http
GET /api/clients
GET /api/clients/42/documents
```

Example explicit request:

```http
GET /api/clients?page=1&size=2
GET /api/clients/42/documents?page=1&size=2
```

Example successful response (Client content objects retain all existing `ClientResponse` fields):

```json
{
  "content": [
    {
      "id": 3,
      "firstName": "Ada",
      "lastName": "Lovelace",
      "taxIdentifier": "TAX-3",
      "email": "ada@example.com",
      "phoneNumber": "123",
      "version": 0,
      "documents": []
    }
  ],
  "page": 1,
  "size": 2,
  "totalElements": 3,
  "totalPages": 2
}
```

- **PROPOSED:** Document responses use the same envelope and preserve each existing `DocumentResponse` item (`id`, `clientId`, `number`, `description`, `expirationDate`, `version`). Metadata `page` is zero-based; `size` is the requested/active page size; `totalElements` is the count in the full selected collection; `totalPages` is supplied by Spring `Page`.

### Invalid, empty and out-of-range behavior

- **PROPOSED:** A negative page, size less than 1, or size greater than 100 returns HTTP 400 with the current `application/problem+json` response. Oversize is rejected, not silently clamped. A non-integer or integer-overflow parameter likewise produces HTTP 400 through existing type-mismatch handling. The exact human-readable `detail` may follow existing error wording; status and Problem Details content type are the contract.
- **PROPOSED:** A syntactically valid page beyond the last page returns HTTP 200, empty `content`, and the actual total metadata (`totalElements` and `totalPages`); do not turn it into 404. An empty collection returns HTTP 200, empty `content`, `totalElements: 0`, and `totalPages: 0`.
- **PROPOSED:** For `GET /api/clients/{clientId}/documents`, preserve existing 404 if the Client does not exist, including when the requested page is out of range. A valid existing Client with no Documents gets a 200 empty page.
- **RECONSTRUCTED:** `GlobalExceptionHandler` already maps `ResponseStatusException` to RFC 9457 and type mismatch to 400, so controller-side bounds checks can use the current error handling rather than introduce pagination-specific errors.

### Minimal OpenAPI changes

- **PROPOSED:** Add concise endpoint/parameter documentation to the two list methods (e.g., Springdoc `@Operation` and parameter `@Parameter`/`@Schema` metadata): zero-based page default 0 and minimum 0; size default 20, minimum 1, maximum 100; fixed ID-ascending order; HTTP 400 for invalid parameters; and the paginated response fields. Let Springdoc infer query parameter names and the generic envelope/content DTO schemas.
- **VERIFIED:** `OpenApiConfig` already registers the global OpenAPI metadata bean. Changing it or adding dependencies is unnecessary for these endpoint-level docs. Full API-wide OpenAPI hardening remains a Phase 4 task.

### Minimal test plan

- **PROPOSED:** Keep the test scope to the two existing controller tests and two existing service/persistence tests listed in Section 6; do not add broad pagination framework tests.
- **PROPOSED:** In **both** `ClientControllerTest` and `DocumentControllerTest`, assert (a) omitted parameters use page 0 and size 20, (b) explicit `page`/`size` are passed through, (c) size 100 is accepted and size 101 is rejected with HTTP 400 Problem Details, and (d) negative page, zero/negative size, and malformed numeric parameters return HTTP 400 without calling the service.
- **PROPOSED:** In `ClientServiceTest` and `DocumentServiceTest`, use persisted records with deliberately non-ID insertion/fixture order to verify `id ASC`; assert page content and `totalElements`/`totalPages` for explicit page/size; assert empty collections return empty content with zero totals; and assert a valid out-of-range page returns empty content with the actual totals. Retain the Document owner-scope and missing-Client 404 coverage.
- **PROPOSED:** Existing DTO fields should be asserted inside `content` to ensure Client/Document response DTOs are preserved. No standalone mapper test is needed because pagination changes neither mapper.

## 9. Findings and severity

1. **Major — Client list does not meet the pagination acceptance criteria yet.** **VERIFIED** (`ClientController.java`, `ClientService.java`): It returns the complete collection in a bare array and accepts no pagination.
2. **Major — Document query/response contract is incomplete for T2.8.** **VERIFIED** (`DocumentController.java`): It does accept Spring `Pageable`, but returns framework `Page` JSON directly, has no explicit 100-size HTTP 400 rule, and exposes pageable sorting behavior beyond the required fixed deterministic ordering.
3. **Minor — Pagination test evidence is incomplete at API level.** **VERIFIED** (existing test files): Client has only old bare-list expectations; Document controller test checks only content item paths; property and repository tests do not verify end-to-end HTTP metadata/bounds for both endpoints.
4. **No additional dependency or migration risk identified.** **VERIFIED** (`pom.xml`, repositories, pagination configuration): The required Spring Data and springdoc infrastructure already exists, and pagination is query behavior, not schema behavior.

## 10. Decisions and reasoning

### Recommended implementation design

1. **PROPOSED — Request binding:** Bind just `page` and `size` as integer request parameters. Use the existing configured defaults and maximum. This prevents Spring's default pageable handling from silently normalizing or capping values and avoids accidental exposure of dynamic sorting.
2. **PROPOSED — Validation and ordering:** In each list controller, reject `page < 0`, `size < 1`, or `size > maxPageSize` with `ResponseStatusException(BAD_REQUEST, safe detail)`. Build a `PageRequest` with `id ASC`. Keep the controller responsible for HTTP query validation; keep service responsible for use-case retrieval/mapping.
3. **PROPOSED — Services/repositories:** Change only Client service list to accept `Pageable` and return `Page<ClientResponse>`. Use inherited `findAll(pageable)`. Leave Document service's existing pageable query and repository method intact.
4. **PROPOSED — Response contract:** Add a generic `PageResponse<T>` record with exactly the five agreed fields. Convert the `Page<T>` in the controller. Do not expose Spring's Page implementation-specific properties or alter existing resource response DTOs.
5. **PROPOSED — Boundaries preserved:** No changes to CRUD endpoints, ownership checks, transactions, version behavior, entity mappings, or persistence schema.

### Alternatives and trade-offs

- **RECONSTRUCTED:** Passing Spring `Pageable` directly is the smallest Java signature but Spring's default resolver can clamp page size and exposes `sort`; that conflicts with the explicit “above 100 returns 400” requirement and the no-advanced-sorting objective. Therefore explicit `page`/`size` binding followed by internal `PageRequest` is preferred.
- **RECONSTRUCTED:** Returning `Page<T>` directly is minimal but leaks the Spring Data serialization shape and does not provide the requested minimal five-field response. A small generic response record is the narrowest contract boundary that satisfies the requested shape.
- **PROPOSED:** Repeating a few lines of bounds validation in the two controllers is preferable to creating a generic pagination framework or extra abstraction for only two endpoints. If code review establishes meaningful shared logic, a small helper can be considered without widening scope.

### Decisions requiring Tech Lead approval

- **PROPOSED — Approval requested:** Confirm invalid values are: `page < 0`, `size < 1`, `size > 100`, malformed/non-integer values; all return HTTP 400 Problem Details. This fills the task's requested “invalid page/size” behavior beyond the explicitly listed oversize case.
- **PROPOSED — Approval requested:** Confirm out-of-range pages and empty result sets return HTTP 200 with empty content and metadata, with zero total pages for a genuinely empty collection.
- **PROPOSED — Approval requested:** Confirm query parameters are only `page` and `size` and ordering is fixed at ID ascending (no client-provided sort).
- **PROPOSED — Approval requested:** Confirm the minimal response uses `size` for the requested page size and Spring `Page`'s `totalPages` semantics.
- **VERIFIED:** Requirements already approve page 0 / size 20 / max 100 / ascending ID / oversize 400; this proposal does not reopen those values.

## 11. Final status

**PROPOSAL COMPLETE — AWAITING HUMAN TECH LEAD REVIEW/APPROVAL.** **VERIFIED:** This is an architecture recommendation only; no implementation authorization, acceptance, or self-approval is claimed.

## 12. Outstanding issues

- **PENDING VERIFICATION:** Tech Lead approval of the detailed invalid-value, boundary-page and envelope semantics in Section 10.
- **PENDING VERIFICATION:** Developer confirmation that all eight proposed files in Section 6 match the approved implementation choices; no code changes have been made by this Architect.
- **PENDING VERIFICATION:** Independent tests and Reviewer audit are required after authorized implementation under the established workflow.
- **NOT AVAILABLE:** Independently inspected evidence supporting the user-provided T2.1–T2.7 acceptance and “310 tests passed” context was not included in this architecture inspection.

## 13. Handoff to next agent

- **VERIFIED:** The required report has been written to `agent-reports/T2.8/architect-proposal.md`.
- **PROPOSED:** Human Tech Lead reviews/approves or revises the decisions in Section 10 before any implementation starts. The Architect stops here as instructed.
- **PROPOSED:** After approval, the Developer should implement only the accepted scope in Section 6; the Tester should independently verify both endpoint contracts before reading Developer conclusions; the Reviewer should audit independently before reading Developer and Tester reports.
- **VERIFIED:** No approval or test execution is claimed in this report.
