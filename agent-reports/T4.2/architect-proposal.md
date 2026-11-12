# Architect Proposal — T4.2 Error and API Contract Consistency

## 1. Task ID

- **VERIFIED** (`implementation-plan.md`, Phase 4, T4.2): Task is `T4.2 — Error and contract consistency audit`.

## 2. Execution identifier and date

- **VERIFIED** (execution environment): `T4.2-ARCH-001`, 2026-11-12.
- **NOT AVAILABLE** (workspace search of `agent-reports/T4.2/` before this report): No prior Architect primary report or history archive was found; no archive was required.

## 3. Agent role and model

- **VERIFIED** (execution instructions): Architect; `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

- **VERIFIED** (direct tasking and `implementation-plan.md`, T4.2): Analyze actual HTTP behavior, API contracts, validation, OpenAPI descriptions, and minimal correction/test proposals for the seven requested checks.
- **VERIFIED** (direct tasking): Analysis and proposal only. No production/test changes, Git writes, branch/stash operations, or T4.2 completion claim are authorized.
- **VERIFIED** (`agent-reports/README.md`, Sections 3–6): This report is subject to the 13-section format, evidence markers, and report-history convention. Developer implementation requires human Tech Lead approval.

## 5. Files inspected

- **VERIFIED**: `agent-reports/README.md`; `implementation-plan.md` (T4.2); `requirements-decisions.md` (Sections 3, 4, 6, 8, 10, 11, 14, and 20).
- **VERIFIED**: T4.1 evidence: `agent-reports/T4.1/developer-report.md`, `tester-report.md`, and `reviewer-report.md`.
- **VERIFIED**: API source: `src/main/java/com/humanit/challenge/controller/AuthController.java`, `ClientController.java`, `DocumentController.java`; `dto/auth/LoginRequest.java`; client/document request DTOs including `ClientPutRequest`, `ClientPatchRequest`, `ClientDocumentPutRequest`, `ClientDocumentPatchRequest`, `DocumentPutRequest`, `DocumentPatchRequest`, and `DocumentCreateRequest`.
- **VERIFIED**: Error/security/OpenAPI source: `src/main/java/com/humanit/challenge/exception/GlobalExceptionHandler.java`; `security/SecurityProblemHandlers.java`, `SecurityConfiguration.java`; `config/OpenApiConfig.java`.
- **VERIFIED**: Behavior and contract tests inspected: `src/test/java/com/humanit/challenge/config/OpenApiConfigTest.java`; controller tests for Clients and Documents; `ClientAggregateUpdateHttpIntegrationTest.java`; `GlobalExceptionHandlerTest.java`; authentication and security integration tests.
- **NOT AVAILABLE**: Generated `/v3/api-docs` JSON captured or directly inspected during this Architect execution. Shell execution is prohibited by the Architect role; the T4.1 Tester and Reviewer also recorded the direct `LoginRequest` schema assertion/output as unavailable.

## 6. Files changed

- **VERIFIED**: Only `agent-reports/T4.2/architect-proposal.md` was created by this execution, as the required Architect report.
- **VERIFIED**: No application code, tests, configuration, requirements, implementation plan, decisions log, or other agent report was changed.

## 7. Commands executed

- **VERIFIED**: None. Files were inspected with read/search tools. No shell, build, test, or Git command was executed.

## 8. Evidence and observed results

- **VERIFIED** (`requirements-decisions.md`, Section 3; `ClientController.java`, `DocumentController.java`; `OpenApiConfigTest.java`): Approved endpoint table lists `/clients` and nested `/clients/{clientId}/documents`; controller mappings and generated-OpenAPI test assertions use `/api/clients` and nested `/api/clients/...`. Runtime mappings and OpenAPI are consistent with each other, but disagree with the approved path table. T4.1 reports independently record this same discrepancy.
- **VERIFIED** (`LoginRequest.java`; `OpenApiConfig.java`): Login `username` and `password` have `@NotBlank`; `OpenApiConfig` corrects `minLength` for selected nonblank schemas but does not list `LoginRequest`. The customizer comment explains the known springdoc `@NotBlank` to `minLength=0` issue. **PENDING VERIFICATION**: this execution did not inspect the actual generated LoginRequest property values, so the current generated defect is not confirmed here.
- **VERIFIED** (`AuthController.java`; `GlobalExceptionHandler.java`; T4.1 Reviewer report): Login declares 200, 400 and 401, but does not declare 500. Validation, invalid credentials, and unexpected exceptions are handled as 400, 401 and sanitized 500 Problem Details respectively. The 500 omission is an OpenAPI documentation gap, not a demonstrated runtime defect.
- **VERIFIED** (`GlobalExceptionHandler.java`; `SecurityProblemHandlers.java`; relevant tests): MVC validation/malformed inputs and `ResponseStatusException` use `application/problem+json`; validation responses add structured `errors`; optimistic-lock failures map to 409. The global unexpected-error handler returns a sanitized 500 Problem Detail. Security handlers return Problem Details for 401 and 403 and the 401 path sets `WWW-Authenticate: Bearer`.
- **VERIFIED** (`AuthController.java`; `SecurityConfiguration.java`; `AuthControllerIntegrationTest.java`; `SecurityConfigurationIntegrationTest.java`): Login credential validation is 400; invalid login credentials and missing/invalid/expired JWT on protected routes are 401; unexpected exceptions reach 500 handling. A 403 handler is configured and independently tested, but the current filter chain has no role/authority restriction beyond `.authenticated()`, so no current Client/Document route-specific condition was found that produces 403. The OpenAPI annotations document 403 for protected operations.
- **VERIFIED** (`requirements-decisions.md`, Sections 4, 8, 11, and 20; controllers, DTOs, services and tests): PUT/PATCH behavior, expected-version locations, and required headers are represented in the source contracts. Client PUT preserves omitted/empty Documents and rejects explicit null; PATCH changes only supplied values, has explicit null behavior, and does not imply deletion. Document PUT requires editable fields; Document PATCH is partial. Required version omissions/malformed/negative values return 400; stale versions and uniqueness conflicts return 409.
- **VERIFIED** (`ClientService.java`, `DocumentService.java`; `ClientAggregateUpdateHttpIntegrationTest.java`; controller tests): Nested Document access under a non-owning Client returns 404; associating a foreign Document through Client aggregate PUT/PATCH returns 409; unknown nested IDs return 404; duplicate Document numbers return 409. Aggregate rollback and optimistic-lock outcomes are covered by existing tests.
- **VERIFIED** (`ClientController.java`, `DocumentController.java`; DTO annotations; test sources): Required version headers are declared on Document POST/PUT/PATCH, Client DELETE, and Document DELETE (both Client and Document headers); update bodies carry the Client/Document versions required by the approved contract. Tests cover missing/malformed version inputs and conflict/not-found cases. The OpenAPI focused test directly asserts only the two Document DELETE header names, leaving broader generated-contract regression coverage incomplete.
- **VERIFIED** (`agent-reports/T4.1/developer-report.md`, `.tester-report.md`, `.reviewer-report.md`): The prior T4.1 focused OpenAPI test was reported as 3/3 passing, but the Tester explicitly left generated LoginRequest nonblank output pending; no T4.2 test result is claimed here.

## 9. Findings and severity

| ID | Severity | Finding |
|---|---|---|
| T42-ARCH-01 | Minor — confirmed contract discrepancy; decision required | **VERIFIED**: Approved requirements say `/clients`; current HTTP routes and OpenAPI use `/api/clients`. OpenAPI accurately reflects current runtime, but the implemented public contract does not match the approved endpoint table. A Tech Lead decision is required before choosing a potentially breaking route correction versus formally approving the `/api` prefix and authorizing the corresponding requirements correction. |
| T42-ARCH-02 | Minor — likely schema documentation defect; pending direct verification | **VERIFIED**: `LoginRequest` uses `@NotBlank` and is absent from the explicit `markNonBlankStrings` customizer list. **PENDING VERIFICATION**: Generated `username`/`password` `minLength` values were not observed in this execution. If they are not at least 1, the schema fails to express validation; do not report this as a confirmed defect until a generated-schema assertion/output establishes it. |
| T42-ARCH-03 | Minor — confirmed OpenAPI documentation gap | **VERIFIED**: Login may return the centralized sanitized 500 Problem Details on unexpected failures, but `AuthController` documents only 200/400/401. Add a 500 Problem Details response to the login operation. Runtime behavior should remain unchanged. |
| T42-ARCH-04 | Informational — generated-contract test coverage gap | **VERIFIED**: Existing tests and controller annotations cover core behavior, but `OpenApiConfigTest` does not directly assert LoginRequest nonblank constraints, login 500 content, every required version header, or all generated status/media types. Add assertions only for the confirmed/minimally changed contract items, not a broad all-endpoint snapshot suite. |

- **VERIFIED**: No confirmed defect was found in the inspected PUT/PATCH validation and semantics, version handling, stale-version/uniqueness statuses, ownership outcomes, or Problem Details runtime mapping. Existing source/tests align with the approved behavior in those areas.
- **VERIFIED**: No 400/401/403/500 runtime change is proposed. **PENDING VERIFICATION**: Whether protected-operation 403 responses are reachable under the current authorization rules; no role-based rule was observed, but the shared handler and documented response are present.

## 10. Decisions and reasoning

- **VERIFIED** (`requirements-decisions.md`, Sections 3 and 18): The path table is part of approved requirements, whereas changing that approval is not within this Architect execution. Because current route mappings and OpenAPI agree, simply changing OpenAPI would make documentation false. Recommendation: treat the approved `/clients` contract as authoritative unless the Tech Lead explicitly decides `/api` was intentional. If `/clients` is confirmed, change only the two controller base mappings and adjust affected HTTP/OpenAPI test route references; do not add aliases or redesign routing. If `/api` is approved instead, the Tech Lead must authorize a requirements correction outside this implementation-only proposal. **No path choice is approved by this report.**
- **VERIFIED** (`OpenApiConfig.java`, `LoginRequest.java`, T4.1 Tester/Reviewer reports): Resolve the generated schema with a focused direct `/v3/api-docs` assertion. If `minLength` is missing or zero, extend the existing customizer for `LoginRequest.username` and `.password`, then pin each to `minLength: 1`. If generated output already satisfies the contract, do not add a redundant runtime/customizer change.
- **VERIFIED** (`AuthController.java`, `GlobalExceptionHandler.java`, `OpenApiConfig.java`): Add only an `@ApiResponse` for login 500 whose description includes `(Problem Details)`, allowing the existing customizer to attach the `application/problem+json` schema. No exception handling change is warranted.
- **VERIFIED** (current source and tests): Preserve existing 400/401/409/404 behavior, Problem Details representation, PUT/PATCH logic, version enforcement, and ownership mappings. Tests already cover basic happy paths and several error paths; do not duplicate them without a specific regression target.
- **Trade-off — path compatibility**: Aligning runtime with approved `/clients` may break existing clients of `/api/clients`; retaining `/api` preserves deployed behavior but requires the Tech Lead to amend the approved requirement. Dual routes would increase public API surface and generated documentation, so it is not proposed as the default.
- **Acceptance criteria for Developer work** (all **PENDING VERIFICATION** until authorized and tested): (1) exactly one Tech Lead-approved canonical Client/Document route prefix is used consistently by mappings, OpenAPI and route tests; (2) generated LoginRequest schema has non-empty constraints equivalent to both `@NotBlank` fields, if needed; (3) login OpenAPI declares 500 as `application/problem+json` ProblemDetail; (4) existing version, PUT/PATCH, ownership and error behavior remains unchanged; (5) focused regression tests pass.
- **Essential tests only**: (a) generated OpenAPI assertion for canonical Client and nested Document paths after the path decision; (b) generated LoginRequest username/password nonblank schema assertions; (c) generated login 500 Problem Details media/schema assertion; (d) one existing-or-focused happy-path and basic error-path check for the selected Client path prefix. Reuse existing authentication, version, PUT/PATCH and ownership tests; do not add a broad status matrix.
- **Out of scope**: Changing exception formats/status logic, redesigning authentication/authorization, making 403 reachable through new roles, changing optimistic-lock semantics or headers, changing ownership status policy, broad OpenAPI snapshot/coverage expansion, aliases or new API features, README/requirements/implementation-plan edits, and T4.3 work.
- **Estimate**: Approximately 2–4 Developer hours for the schema/docs/test changes and focused verification once authorized. A route-prefix migration could take an additional 1–2 hours depending on the number of affected route references; the path decision is the main estimate uncertainty.

## 11. Final status

**PROPOSAL COMPLETE — T4.2 NOT COMPLETE.** **VERIFIED**: One confirmed requirements/runtime path discrepancy and one confirmed login 500 documentation gap. **PENDING VERIFICATION**: Generated LoginRequest minimum length. No implementation or tests were run or changed. Developer execution is pending Tech Lead approval, with the canonical route-prefix decision required before resolving T42-ARCH-01.

## 12. Outstanding issues

- **VERIFIED / decision required**: Tech Lead must decide whether approved `/clients` paths remain authoritative or `/api/clients` is the intended public API and requirements need authorized correction. No approval is recorded here.
- **PENDING VERIFICATION**: Inspect/assert generated `LoginRequest` `username` and `password` schema `minLength` values; apply the customizer change only if the generated schema omits the required nonblank constraint.
- **VERIFIED**: Login 500 Problem Details is omitted from OpenAPI and is a minimal documentation correction.
- **PENDING VERIFICATION**: Developer and Tester must verify focused generated-schema/path tests after explicit approval; this Architect report does not provide test evidence or mark task completion.
- **VERIFIED**: No report-writing blocker was encountered. No production/tests/configuration/requirements/planning/decision/Git changes were made.

## 13. Handoff to next agent

- **VERIFIED**: Handoff to the human Tech Lead for proposal review and explicit canonical route-prefix decision/implementation approval.
- **PENDING VERIFICATION**: After approval, Developer should implement only the essential changes in Section 10 and report exact files/results. The Tester must verify independently before reading Developer conclusions, in accordance with `agent-reports/README.md`.
- **VERIFIED**: The Reviewer must audit independently before reading Developer and Tester reports or this proposal, per the independence rule. Only the human Tech Lead may approve final acceptance.
- **VERIFIED**: Report path: `agent-reports/T4.2/architect-proposal.md`. Outstanding findings are T42-ARCH-01 path decision, T42-ARCH-02 generated LoginRequest verification, and T42-ARCH-03 login 500 documentation gap. T4.2 remains open.
