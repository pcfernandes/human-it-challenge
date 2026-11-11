# Reviewer Report — T2.5 Aggregate Update Semantics and Document Identity Preservation

## 1. Task ID

**VERIFIED** (`implementation-plan.md`, Phase 2, T2.5): T2.5 — Aggregate update semantics and Document identity preservation.

## 2. Execution identifier and date

**VERIFIED** (direct tasking/environment): `T2.5-EXEC-004-REV` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Reviewer; `opencode-go/gpt-6-luna` (`gpt-6-luna`).

## 4. Authorized scope

**VERIFIED** (direct tasking and `implementation-plan.md`, T2.5): Independently audit Client aggregate PUT/PATCH semantics, Document identity/ownership preservation, unknown and cross-Client nested Document IDs, endpoint consistency, test coverage, and task scope. Read-only review only; no production/test changes, no T2.6/T2.7 expansion, no Git mutation and no self-approval.

## 5. Files inspected

- **VERIFIED:** `implementation-plan.md` (T2.5 and adjacent task boundaries); `requirements-decisions.md` (PUT/PATCH, identity, ownership, endpoint consistency); `agent-reports/decisions.md` (D-10 through D-17, especially D-11, D-13 and D-17); `agent-reports/README.md` (reporting conventions).
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`, `DocumentService.java`; `controller/ClientController.java`, `DocumentController.java`; `exception/GlobalExceptionHandler.java`; `mapper/ClientMapper.java`, `DocumentMapper.java`; `domain/Client.java`, `Document.java`; relevant Client and Document request/response DTOs, `PatchValue` and its deserializer/validator; `repository/ClientRepository.java`, `DocumentRepository.java`.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`, `DocumentServiceTest.java`, `controller/ClientControllerTest.java`, `DocumentControllerTest.java`, `mapper/ClientMapperTest.java`, `dto/client/ClientPatchRequestValidationTest.java`, and related test-source search results.
- **VERIFIED:** Only after independent source/test inspection, read `agent-reports/T2.5/architect-proposal.md`, `developer-report.md` and `tester-report.md` for claim comparison.
- **NOT AVAILABLE:** No prior `agent-reports/T2.5/reviewer-report.md` or Reviewer history file existed at report creation; no archive was required.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.5/reviewer-report.md` — this review report only.
- **VERIFIED:** No application source, test source, configuration, planning/requirements file, decision log or other agent report was changed. No earlier Reviewer report was overwritten.

## 7. Commands executed

- **VERIFIED:** None. No shell, Git or Maven command was run. The review is based on direct source/test/report inspection; execution results below are attributed to the inspected Tester/Developer reports and were not rerun by this Reviewer.

## 8. Evidence and observed results

- **VERIFIED** (`ClientService.java`, `applyPutDocuments` and `applyPatchDocuments`; `ClientPatchRequest.java`; `ClientMapper.java`): Client PUT returns without changing the Documents collection for null/empty supplied lists. PATCH only enters nested processing for a present, non-null, non-empty collection. The Client mapper changes scalar fields only when their `PatchValue` is `PRESENT_VALUE`; omitted values therefore remain untouched. No merge path removes an existing Document.
- **VERIFIED** (`ClientService.java`): Entries with no ID/version are mapped to new Documents and associated to the target Client. Entries with identity are looked up by ID; missing IDs produce 404, cross-Client ownership produces 409, matching ownership/version permits scalar update of the loaded entity. Identity and owner are not assigned from request data by `DocumentMapper`.
- **VERIFIED** (`Client.java`, `Document.java`, `DocumentMapper.java`): Entity relationship helpers prevent ownership transfer; update mappers alter editable scalar fields only. Existing Document IDs and client association are not replaced by PUT/PATCH mapping.
- **VERIFIED** (`DocumentService.java`, `DocumentController.java`): Standalone updates target a Document through `findByIdAndClientId`, so a Document under a non-owning Client route is not found. Standalone PUT replaces editable fields and PATCH applies supplied fields; neither mapper assigns an ID or owner.
- **VERIFIED** (`ClientServiceTest.java`): Focused persistence-backed test cases cover Client PUT omitted/empty collection preservation; updating an existing nested Document while creating a new one and preserving an omitted one; PATCH omitted/empty collection preservation; nested partial field update and ID/owner preservation; unknown nested ID status 404 with no extra row; and cross-Client association status 409 without transfer.
- **VERIFIED** (`DocumentServiceTest.java`): Persistence-backed tests exercise standalone and aggregate PUT/PATCH field behavior, ID and owner preservation, and expected collection size. This supports endpoint consistency for editable field semantics.
- **VERIFIED** (`ClientControllerTest.java`, `DocumentControllerTest.java`, `GlobalExceptionHandler.java`): Controllers delegate update calls to their services. Generic `ResponseStatusException` to Problem Details/status translation is tested; however, controller tests do not send an aggregate PUT/PATCH request that triggers the unknown-ID or cross-owner branch.
- **VERIFIED** (test-source inspection): No Client service regression test directly asserts that omitted Client scalar properties remain unchanged after a PATCH. The existing nested Client PATCH cases focus their assertions on the Document collection/fields. The mapper implementation supports the expected omission behavior, but direct persistence-backed regression coverage is absent.
- **VERIFIED** (`agent-reports/T2.5/tester-report.md`, Sections 7–9): Tester reports an independent focused run of 26 tests (0 failures/errors/skips), and explicitly records the two coverage gaps above. **PENDING VERIFICATION:** This Reviewer did not rerun Maven or independently establish that run result.
- **VERIFIED** (`agent-reports/T2.5/developer-report.md`, Sections 6–8): Developer reports only the two focused service test classes and its report changed, and reports focused and full test success. **PENDING VERIFICATION:** This Reviewer did not run Git diff/status or Maven to independently establish the changed-file set or reported 285-test full-suite result.
- **VERIFIED** (`agent-reports/decisions.md`, D-17): T2.5 implementation authorization was limited to focused tests and service fixes only if tests exposed a concrete defect; T2.5 final acceptance remains pending.

## 9. Findings and severity

### Critical

- **None observed.**

### Major

- **None observed.**

### Minor

1. **Client scalar PATCH omission lacks a direct service regression assertion.** `ClientMapper.updateFromPatch` applies only supplied values, so inspected implementation is consistent with the omission contract. Nevertheless, no persistence-backed test proves that omitted Client scalar fields retain their old values after an actual service PATCH. Add a focused test that changes one scalar while asserting the other Client scalars remain unchanged. This is a regression-coverage gap, not an observed product defect.
2. **Aggregate update 404/409 is not asserted through the HTTP endpoint.** Service tests assert `ResponseStatusException` status 404 for an unknown nested ID and 409 for a foreign-owned nested ID; controller delegation and generic exception translation are separately covered/inspected. No aggregate PUT/PATCH MockMvc test verifies those exact requests return the expected HTTP status and Problem Details. An endpoint-level test would close this evidence gap. No mismatch was observed in the service/controller/handler path.

**Neither Minor finding independently blocks T2.5 acceptance:** the first is contradicted as a functional defect by straightforward mapper logic but remains useful regression coverage; for the second, service statuses are tested and the shared HTTP status mapping is covered separately, while the controller directly delegates. A targeted endpoint test is recommended, but no correction is required before acceptance on current evidence. Human Tech Lead retains the acceptance decision.

## 10. Decisions and reasoning

- **VERIFIED (T2.5 plan and requirements decisions):** Contract requires collection-preserving Client PUT/PATCH, nested existing/new discrimination, preservation of omitted Documents, rejection of unknown IDs and foreign-owner associations, no implicit deletion, and equivalent standalone/aggregate Document field semantics.
- **VERIFIED (source and tests inspected):** The current implementation meets the reviewed merge/identity behaviors. Existing entities are updated in place; new entries are associated with the requested Client; existing omitted entries remain in the managed collection; there is no update-time delete path. Test assertions check IDs, owner IDs, values and persisted counts for key scenarios.
- **VERIFIED (scope assessment):** No new service abstraction, endpoint, DTO, mapper, repository, migration or dependency is warranted by the observed work. The implementation keeps orchestration in `ClientService`, transformations in the dedicated mappers, and HTTP handling in controllers. This aligns with D-11 and D-17 and shows no overengineering in the reviewed T2.5 scope.
- **VERIFIED (scope boundary):** This review does not assess or claim completion of T2.6 version/concurrency guarantees or T2.7 transaction-wide rollback guarantees. Existing version checks visible in code are not treated as evidence that those later task acceptance criteria are complete.
- **VERIFIED (claim comparison):** Architect proposal claims about existing collection-preserving branches, unknown-ID/ownership status rules, and the need for focused tests match inspected source and test files. Its statement that implementation authorization was unavailable was accurate for its proposal snapshot but is superseded by later D-17 and the Developer report's attestation of direct authorization.
- **VERIFIED (claim comparison):** Developer's claim that no concrete production defect was identified and no production change was needed is supported by the inspected service/mapping logic and focused test cases. Its reported test outcomes are not represented as independently rerun by this Reviewer. Its note that endpoint-level binding/status coverage was not added is confirmed.
- **VERIFIED (claim comparison):** Tester’s two Minor findings are confirmed by test-source inspection. Its description of the service-level 404/409 assertions and generic Problem Details mapping coverage is also confirmed. No finding is disputed. The Tester’s reported focused test result remains a report artifact, not a Reviewer-run result.
- **RECONSTRUCTED (cross-layer behavior):** Given the service exceptions, direct controller delegation and shared exception advice, aggregate failures are expected to surface as HTTP 404/409 Problem Details. The exact aggregate request/response path was not exercised, so this inference is not elevated to an end-to-end VERIFIED result.
- **VERIFIED (approval status):** No Tech Lead final T2.5 acceptance is recorded in the inspected task reports or D-17. This report is a Reviewer recommendation only.

## 11. Final status

**PASS WITH FINDINGS — Reviewer recommendation.** T2.5 aggregate merge, identity and ownership behavior is supported by direct implementation inspection and focused persistence-backed regression coverage. Two Minor test-coverage gaps remain; no Critical or Major finding and no concrete production defect was observed. Both gaps can be deferred without weakening the implemented contract, though adding the tests would improve regression assurance. This is not Human Tech Lead acceptance.

### Acceptance criteria assessment

- **VERIFIED:** Client PUT omitted/empty collection preservation; existing/new/omitted Document handling; no implicit deletion or unexpected creation in covered cases.
- **VERIFIED:** Existing Document identity and ownership are preserved through aggregate and standalone PUT/PATCH updates in the inspected implementations and persistence-backed tests.
- **VERIFIED at service level:** Unknown nested ID returns 404; cross-Client association returns 409; tests also assert no transfer/creation. **PENDING VERIFICATION:** Exact aggregate HTTP responses for those failures are not directly tested.
- **VERIFIED:** Standalone and aggregate Document editable-field behavior is compared by persistence-backed tests.
- **VERIFIED by code inspection, with Minor test gap:** Client scalar PATCH omission is correctly implemented; a direct regression assertion is absent.
- **NOT AVAILABLE:** Human Tech Lead final acceptance.

## 12. Outstanding issues

1. **Minor, optional before or after acceptance:** Add a persistence-backed Client PATCH test asserting omitted scalar values remain unchanged.
2. **Minor, optional before or after acceptance:** Add aggregate endpoint-level 404/409 MockMvc coverage asserting status and Problem Details for unknown and cross-Client nested Document IDs.
3. **PENDING VERIFICATION:** Human Tech Lead to decide T2.5 acceptance. Reviewer makes no approval claim.
4. **VERIFIED (scope boundary):** Keep T2.6 concurrency/version behavior and T2.7 transactional atomicity/rollback work outside this review decision.

## 13. Handoff to next agent

- **VERIFIED:** Human Tech Lead: review this PASS WITH FINDINGS recommendation and decide whether to accept T2.5 with the two Minor coverage observations or request the optional test additions.
- **VERIFIED:** No production correction is indicated by this audit. If additional tests are requested, keep them within T2.5 scope and do not expand into T2.6/T2.7.
- **VERIFIED:** No self-approval or Tech Lead acceptance is recorded.
