# Architect Proposal — T4.1 OpenAPI Coverage

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Section 8, task list): `T4.1` — OpenAPI coverage. This report proposes the T4.1 design requested as the Phase 4 kickoff; it does not authorize implementation.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T4.1-ARCH-001`, 2026-11-12.
- **VERIFIED** (`agent-reports/README.md`, Section 6; search of `agent-reports/T4.1/`): No prior T4.1 Architect primary report or history archive was found, so no archival copy was required.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Architect; model `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking): Planning only; inspect the reporting conventions, Phase 4 plan, relevant approved requirements and existing project structure, and author only this Architect report.
- **VERIFIED** (direct tasking): Do not edit application code, tests, migrations, configuration, requirements or implementation plans; do not execute shell commands; do not claim approval or completion.
- **VERIFIED** (`implementation-plan.md`, Sections 2 and 8): T4.1 scope is documenting endpoints, DTOs, status codes, validation, JWT authentication, pagination, PUT/PATCH behavior, ownership, optimistic-lock conflicts and Problem Details. T4.2 owns the later error/contract consistency audit and fixes; T4.3 owns README technical decisions.
- **VERIFIED** (`implementation-plan.md`, Sections 2 and 8): Work must be approved by the human Tech Lead before implementation, and phase completion requires its tests, independent Reviewer audit and Tech Lead approval.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md` (mandatory format, evidence markers and history convention).
- **VERIFIED**: `implementation-plan.md` (governance, T3.5/Phase 3 exit criteria, T4.1–T4.3 and Phase 4 exit criteria).
- **VERIFIED**: `requirements-decisions.md` (Sections 3–4, 8–11, 14–18 and 20: API, update, validation, concurrency, pagination, security, Problem Details and documentation contracts).
- **VERIFIED**: `README.md` (current project state and documented OpenAPI limitations).
- **VERIFIED**: `pom.xml`; `src/main/java/com/humanit/challenge/config/OpenApiConfig.java`.
- **VERIFIED**: `src/main/java/com/humanit/challenge/controller/ClientController.java`, `DocumentController.java`, `AuthController.java`.
- **VERIFIED**: Request/response DTOs under `src/main/java/com/humanit/challenge/dto/client/`, `dto/document/`, `dto/auth/`, `dto/common/` and `dto/patch/` (including `ClientCreateRequest`, `ClientPutRequest`, `ClientPatchRequest`, `DocumentCreateRequest`, `DocumentPutRequest`, `DocumentPatchRequest`, `ClientResponse`, `DocumentResponse`, `PageResponse`, `LoginRequest` and `TokenResponse`).
- **VERIFIED**: `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java`, `src/main/java/com/humanit/challenge/security/SecurityConfiguration.java`, `SecurityProblemHandlers.java`, and `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java`.
- **VERIFIED**: T3.4 reports under `agent-reports/T3.4/` were inspected to establish their recorded status. `agent-reports/T3.5/` and `agent-reports/T4.1/` searches returned no files before this report was created.
- **VERIFIED**: The source-tree structure was inspected through file listing; its relevant package boundaries include `controller`, `dto`, `service`, `repository`, `domain`, `config`, `exception`, `mapper` and `security`.

## 6. Files changed

- **VERIFIED**: Only `agent-reports/T4.1/architect-proposal.md` is authored by this execution. No prior T4.1 Architect report existed to archive.
- **VERIFIED** (authorized scope): No application code, tests, migrations, configuration, requirements, implementation plans, other agent reports or decision log were changed.

## 7. Commands executed

- **VERIFIED**: None. No shell commands were executed; inspection used workspace file-reading and file-listing tools only.

## 8. Evidence and observed results

- **VERIFIED** (`pom.xml`): Springdoc WebMVC UI dependency is present at the configured springdoc version `2.10.0`; Java baseline is 21.
- **VERIFIED** (`OpenApiConfig.java`): Existing OpenAPI configuration supplies only title/version/description metadata and explicitly has no security scheme or groups.
- **VERIFIED** (`ClientController.java`, `DocumentController.java`, `AuthController.java`): Controller route bases currently resolve to `/api/clients`, `/api/clients/{clientId}/documents`, and `/auth`; these expose six Client CRUD operations, six nested Document CRUD operations and `POST /auth/login`.
- **VERIFIED** (controller source): Pagination has `page`/`size` parameters, defaults supplied by configuration, ascending ID ordering, and size bounds enforced at runtime. Existing pagination method annotations document some—but not all—of these operations.
- **VERIFIED** (controller source): Required concurrency headers currently include `X-Client-Version` on Document create/update/delete and Client delete, and `X-Document-Version` on Document delete. Update bodies carry versions as specified in the approved plan.
- **VERIFIED** (DTO source and approved plan): Request/response DTOs are separate; Client responses contain nested Document response entries, and PageResponse has content/page/size/totalElements/totalPages. Validation bounds and special PATCH binding are represented in DTO constraints and the approved contracts.
- **VERIFIED** (`GlobalExceptionHandler.java`, `SecurityProblemHandlers.java`): Application and security failures use Spring `ProblemDetail`; observed handlers cover validation/malformed input, not found, method not allowed, conflicts, unexpected errors, authentication and access denial. Controller/service behavior and documented response status must be checked together before describing endpoint-specific applicability.
- **VERIFIED** (`SecurityConfiguration.java`): `/auth/login`, Swagger UI and `/v3/api-docs` paths are configured as public; remaining requests require authentication and use stateless JWT resource-server handling.
- **VERIFIED** (`OpenApiConfigTest.java`): Existing OpenAPI test checks that the OpenAPI bean and its metadata exist; it does not establish generated-document endpoint/operation/schema coverage.
- **VERIFIED** (`README.md`): README states that the OpenAPI bean is minimal and authentication is not represented in OpenAPI yet; T4.1 is the planned documentation task.
- **VERIFIED** (`agent-reports/T3.4/reviewer-report.md`, Sections 8, 12; `agent-reports/T3.4/tester-report.md`, Sections 11–12): T3.4 evidence records Phase 3 as still awaiting human Tech Lead acceptance; direct Swagger UI route coverage was noted as an optional Minor evidence gap. **NOT AVAILABLE** (search of `agent-reports/T3.5/`): No T3.5 report was found. This does not prove whether work occurred elsewhere.
- **NOT AVAILABLE**: No generated OpenAPI JSON, Swagger UI runtime verification, T3.5 report, or Phase 3 acceptance record was inspected/available in the inspected task-report evidence.
- **PENDING VERIFICATION**: Generated OpenAPI operation schemas, response content types, authorization requirements, and successful unauthenticated Swagger UI access must be verified by the authorized Developer/Tester workflow; this Architect execution ran no application or tests.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| T41-ARCH-01 | Blocker / governance | **VERIFIED** (`implementation-plan.md`, Phase 3 exit criteria; T3.4 Reviewer report): Phase 3 acceptance is not evidenced, and **NOT AVAILABLE**: no T3.5 report was found. T4.1 implementation must not start until the human Tech Lead confirms the preceding phase/task gate is satisfied or explicitly authorizes sequencing. |
| T41-ARCH-02 | Major / scope risk | **VERIFIED** (`ClientPatchRequest`, `DocumentPatchRequest`, `PatchValue` source and approved requirements): PATCH uses project-owned `PatchValue` to bind omitted, explicit-null and non-null values. The OpenAPI schema must expose the wire-level JSON shape and its real absent/null rules, not an implementation wrapper shape; verify generated schema. |
| T41-ARCH-03 | Major / contract accuracy risk | **VERIFIED** (controller annotations and `GlobalExceptionHandler`): Existing OpenAPI annotations are partial, and Problem Details structures/status applicability vary by handler and operation. Document only observed/approved contracts; T4.1 should record mismatches for T4.2 rather than silently alter runtime behavior. |
| T41-ARCH-04 | Minor / evidence gap | **VERIFIED** (`OpenApiConfigTest.java`, T3.4 Reviewer report): Current test checks bean metadata and existing T3.4 evidence did not directly request a Swagger UI route. T4.1 acceptance should validate generated docs and the actual public UI/docs paths without assuming runtime reachability from configuration alone. |

## 10. Decisions and reasoning

- **VERIFIED** (plan and inspected package layout): Keep the current code-first design. Put endpoint-specific summaries/descriptions, parameters, response statuses and request/response media schemas at controller operation boundaries; describe DTO constraints and property semantics on DTO schemas. This follows the existing Controller/DTO separation and avoids a parallel handwritten API specification.
- **VERIFIED** (existing `OpenApiConfig.java`, security configuration): Extend the centralized OpenAPI configuration only for API-wide metadata/components, including an HTTP Bearer JWT security scheme. Apply security requirements to protected Client/Document operations and explicitly leave login public; do not attach auth globally in a way that contradicts the public login contract.
- **VERIFIED** (approved API/Concurrency requirements): Represent standard RFC 9457 Problem Details responses and applicable 400/401/403/404/409/500 statuses per operation, with concise explanations. Document required version headers and request-body versions where applicable; delete operations return no response body on success.
- **VERIFIED** (approved pagination/update contracts): Document zero-based `page`, default page 0 and size 20, maximum size 100, ascending ID ordering and PageResponse metadata. Distinguish Client POST's special null collection normalization from PUT/PATCH null rejection; explain collection-preserving PUT and partial PATCH semantics, including nested Document identity and ownership constraints.
- **VERIFIED** (DTO source and approved requirements): Preserve the documented wire representation for PATCH: omitted values remain unchanged; explicit null is rejected for required values, optional Document `description: null` clears it, and Client `documents: null` is invalid on PUT/PATCH. Because generic `PatchValue` could be inferred inaccurately, treat generated schema inspection as a required acceptance check rather than trusting annotations alone.
- **RECONSTRUCTED** (plan sequence and available T3.4/T3.5 records): T4.1 is the requested Phase 4 kickoff target, but its implementation handoff remains gated by Phase 3 completion. The immediately preceding plan item is T3.5, and no evidence of its execution or Phase 3 acceptance was found here; human Tech Lead clarification/acceptance is needed before proceeding.
- **VERIFIED** (scope boundary in `implementation-plan.md`): Keep T4.1 documentation-focused. Do not change endpoint behavior or resolve contract inconsistencies under T4.1; record concrete discrepancies for T4.2. Do not pull README editing or phase-wide audit into this task.
- **RECONSTRUCTED** (trade-off from the available structure): Reuse springdoc annotations and OpenAPI model components rather than add a separate documentation stack. This minimizes duplication and keeps API contract documentation close to runtime contracts, at the cost of requiring generated-spec tests to catch annotation/schema inference errors.
- **PENDING VERIFICATION**: Whether every currently implemented endpoint's actual status, body and media type exactly matches the approved contract; discrepancies should be confirmed through source review and tests during T4.1/T4.2, not presumed from the plan alone.

## 11. Final status

**PROPOSAL READY — T4.1 scope and acceptance are defined; implementation is blocked pending the Phase 3/T3.5 governance gate and human Tech Lead authorization.** This is an architecture proposal only, not approval, implementation authorization, phase acceptance or completion.

## 12. Outstanding issues

- **PENDING VERIFICATION**: Human Tech Lead to confirm T3.5 completion and Phase 3 acceptance, or explicitly authorize T4.1 sequencing. Evidence available to this execution does not establish that gate.
- **PENDING APPROVAL**: Human Tech Lead approval of this T4.1 proposal is required before a Developer begins implementation.
- **PENDING VERIFICATION**: Confirm OpenAPI generated JSON accurately represents all 13 operations, request/response schemas, field constraints, status codes, Problem Details, bearer authentication, pagination, version headers and PUT/PATCH/ownership semantics.
- **PENDING VERIFICATION**: Confirm unauthenticated access to generated docs and Swagger UI in the running secured application, not only by inspecting route configuration.
- **PENDING VERIFICATION**: Determine how OpenAPI accurately expresses PatchValue's flattened wire values and presence/null rules without changing application behavior or introducing an external nullable abstraction.
- **VERIFIED**: T4.2 remains the plan's later contract consistency audit/fix task; T4.3 remains the README technical decisions task. They are out of scope for this T4.1 proposal.
- **NOT AVAILABLE**: Any human Tech Lead authorization to bypass the preceding phase gate or any Phase 3 acceptance decision.

## 13. Handoff to next agent

- **VERIFIED**: Proposed Phase 4 kickoff task is `T4.1 — OpenAPI coverage`; its minimal scope is code-first documentation of the existing 13 API operations and approved contracts, with generated-spec and Swagger/UI verification. Do not include behavior fixes, the T4.2 audit or T4.3 README work.
- **VERIFIED**: Exact next task in plan order before Phase 4 implementation is `T3.5 — Phase 2 regression and Phase 3 security audit`; **NOT AVAILABLE**: evidence that T3.5 or human Phase 3 acceptance has occurred. Human Tech Lead must resolve this gate before T4.1 implementation proceeds.
- **PENDING APPROVAL**: After the gate is resolved and the human Tech Lead approves T4.1, Developer should implement only the approved documentation scope; Tester verifies generated schemas/docs and public route reachability independently; Reviewer audits accuracy afterward.
- **VERIFIED**: No files other than this Architect report were changed, and no commands or tests were run.
