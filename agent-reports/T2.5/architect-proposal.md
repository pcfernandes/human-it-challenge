# Architect Proposal — T2.5 Aggregate Update Semantics and Document Identity Preservation

## 1. Task ID

**VERIFIED (`implementation-plan.md`, Section 6):** T2.5 — Aggregate update semantics and Document identity preservation.

## 2. Execution identifier and date

**VERIFIED (direct tasking/environment):** `T2.5-EXEC-001-ARCH` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Architect; `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct tasking):** Analyze the approved T2.5 scope, relevant existing implementation and tests; prepare this proposal and implementation handoff only. No application/test/configuration/plan/requirements edits, Maven execution, Git mutation or self-approval. T2.5 implementation requires separate Tech Lead approval under `implementation-plan.md` Section 2.

## 5. Files inspected

- **VERIFIED:** `implementation-plan.md` (Phase 2, T2.5 and adjacent T2.3/T2.4/T2.6/T2.7 scope); `requirements-decisions.md` (update semantics, ownership, concurrency); `agent-reports/decisions.md` (D-10, D-11, D-13); `agent-reports/README.md`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/service/ClientService.java`, `DocumentService.java`; both controllers; `ClientMapper.java`, `DocumentMapper.java`; `ClientRepository.java`, `DocumentRepository.java`; `Client.java`, `Document.java`; the Client PUT/PATCH and nested-document DTOs.
- **VERIFIED:** `src/test/java/com/humanit/challenge/service/ClientServiceTest.java`, `DocumentServiceTest.java`; relevant T2.3 historical Developer/Tester reports and current T2.4 Developer/Reviewer reports.
- **NOT AVAILABLE:** No existing T2.5 Architect report or T2.5 report history was found.

## 6. Files changed

- **VERIFIED:** `agent-reports/T2.5/architect-proposal.md` — this report.
- **VERIFIED:** No application code, tests, configuration, requirements, plan, decision log or other role report was changed. No prior T2.5 Architect report existed to archive.

## 7. Commands executed

- **VERIFIED:** None. No shell, Maven or Git command was executed; project files were inspected using file/search tools.

## 8. Evidence and observed results

- **VERIFIED (`implementation-plan.md`, T2.5 lines 240–263):** Objective is to enforce Client PUT/PATCH aggregate merge and Document identity preservation: collection-preserving semantics, create/update discrimination, omission preservation, rejection of unknown/cross-Client IDs, and no implicit deletion. Cross-Client association must return HTTP 409. T2.5 Developer work belongs in the Service layer.
- **VERIFIED (`requirements-decisions.md`, Sections 4 and 20; D-11/D-13):** Client PUT/PATCH use the approved DTO contracts. Existing nested Documents carry ID and expected version; new nested Documents carry neither. PATCH is partial; omission preserves values; explicit null follows the approved field-specific rules. Mappers remain in-memory transformations; services own identity resolution, ownership and merge orchestration. Preserve accepted T2.1 behavior.
- **VERIFIED (`ClientPutRequest.java`, `ClientPatchRequest.java`):** PUT documents defaults to an empty list and rejects explicit null by validation; PATCH uses the approved `PatchValue` tri-state and rejects explicit null documents. Both contracts carry the expected Client version. Nested PUT/PATCH DTOs encode ID/version pair rules and required creation fields.
- **VERIFIED (`ClientService.java`):** Existing aggregate workflow already treats null/empty PUT and omitted/empty PATCH documents as preserve; creates entries with no ID/version; loads and updates entries with ID/version; rejects unknown nested IDs with 404, foreign ownership with 409 and stale nested Document versions with 409. It uses `ClientMapper`/`DocumentMapper` for scalar transformation and does not remove collection members during these updates.
- **VERIFIED (`DocumentService.java`, both controllers and mappers):** Dedicated Document PUT/PATCH service workflows already replace/partially modify editable fields while targeting the path-owned Document. The nested Document controller delegates to `DocumentService`; route contracts, status handling and concurrency version inputs are existing work, not new T2.5 endpoints.
- **VERIFIED (`ClientServiceTest.java`):** Current focused coverage demonstrates a nested create, PATCH scalar change, and preservation for an omitted PATCH collection. It does not directly establish the full T2.5 matrix (PUT omitted/empty preservation, existing nested identity update, unknown nested ID, cross-owner nested ID, and consistent standalone-vs-aggregate field behavior).
- **VERIFIED (`DocumentServiceTest.java`):** Current tests cover standalone PUT/PATCH field changes, description clearing, owner retention and identity by lookup, but not consistency against the Client aggregate path.
- **RECONSTRUCTED (plan plus observed implementation):** T2.5 is primarily completion and regression-proofing of service-layer semantics already partially present, not a new REST resource, DTO, mapper, repository, migration or dependency. Restrict source changes to a concrete behavior gap uncovered by focused tests; do not duplicate existing implementation.

## 9. Findings and severity

- **VERIFIED — No concrete contract incompatibility identified** in the inspected service/DTO design. Existing code visibly implements the main ownership and merge branches; no defect is asserted solely from missing test coverage.
- **PENDING VERIFICATION — Coverage gap:** T2.5 acceptance scenarios listed above require direct deterministic tests, especially identity preservation, collection preservation on PUT, unknown ID and cross-owner 409. This is a verification requirement, not a finding that those paths are necessarily broken.
- **VERIFIED — Scope boundary:** Expected-version validation, parent force-increment guarantees, conflict rollback and transaction-wide atomicity remain assigned to T2.6/T2.7; do not expand T2.5 into those tasks or reopen accepted T2.1–T2.4 decisions without new concrete incompatibility.

## 10. Decisions and reasoning

### Exact objective and requirements

- **VERIFIED (plan):** Implement/enforce aggregate merge semantics and preserve Document database identity across Client PUT/PATCH and standalone Document PUT/PATCH.
- **VERIFIED (requirements and approved decisions):**
  - Client PUT replaces editable Client scalar fields, but its documents collection is preserving: omitted or empty means preserve all existing Documents.
  - Client PATCH modifies only supplied scalar fields; omitted or empty documents likewise preserves existing Documents. Existing nested Documents update partially; new entries are created under the current Client.
  - Existing nested Document identity is resolved by ID; a new entry has no ID/version. Existing Documents omitted from the request remain unchanged.
  - Unknown nested Document IDs are rejected under the existing not-found rule (HTTP 404). An existing Document owned by a different Client is rejected with HTTP 409. No ownership transfer or implicit deletion; deletion is through DELETE only.
  - Standalone Document PUT replaces editable fields; standalone PATCH changes supplied fields only. In either path the existing Document ID/owner remains unchanged. Field validation and uniqueness still apply.

### Technical approach and trade-offs

- **VERIFIED (D-11):** Keep aggregate orchestration in `ClientService`; keep REST/controller HTTP concerns, repositories as persistence access, and mappers as repository-free DTO/entity conversion. Reuse the existing services, DTOs, mappers, repositories, entity relationship helper, and Problem Details handling. Do not add abstractions or dependencies.
- **RECONSTRUCTED (observed code and approved contract):** Prefer small focused fixes only where a new test exposes mismatch. The observed service already has explicit branch logic, so a rewrite or generic merge framework would add risk without evidenced benefit.
- **VERIFIED (T2.5 scope):** No new route or HTTP contract is required. Existing routes are Client PUT/PATCH and nested Document PUT/PATCH. Existing validation failures use HTTP 400 Problem Details; unknown IDs use 404; cross-owner association uses 409 Problem Details. Do not alter existing endpoint prefixes/statuses as part of this task.

### Expected files and minimal test strategy

- **RECONSTRUCTED (observed package layout and plan):** Likely production file changes, only if tests reveal a defect: `src/main/java/com/humanit/challenge/service/ClientService.java` and/or `DocumentService.java`. No DTO/entity/mapper/controller/repository/migration/config changes are expected absent a specific incompatibility.
- **RECONSTRUCTED (observed test layout):** Add focused persistence-backed tests in `src/test/java/com/humanit/challenge/service/ClientServiceTest.java` and, for standalone-vs-aggregate consistency, `DocumentServiceTest.java`. Add controller tests only if the affected HTTP mapping itself needs verification; no blanket endpoint retesting is needed for this task.
- **VERIFIED (plan acceptance scenarios) / RECOMMENDED minimal test set:**
  1. Client PUT with omitted documents and with `[]` leaves every existing Document present with the same ID and fields.
  2. Client PUT updates a supplied existing Document in place (same ID), creates a new entry without ID/version, and preserves existing entries omitted from the request.
  3. Client PATCH with omitted/empty documents preserves the collection; existing nested Document partial update preserves ID and unsupplied fields; new nested Document is associated with that Client.
  4. Unknown nested ID yields 404; an ID owned by another Client yields 409; verify neither path transfers or creates a Document.
  5. Compare standalone Document PUT/PATCH and aggregate nested Document updates for editable-field behavior, identity preservation and owner preservation, without duplicating concurrency/rollback assertions assigned to T2.6/T2.7.
- **PENDING VERIFICATION:** The authorized Tester independently selects/exercises tests and reports actual outcomes; no tests were run in this Architect execution.

### Acceptance criteria (from the plan)

- Existing Document identities are preserved; no orphan or unexpected Documents are created.
- A Document owned by another Client, including one associated through a Client aggregate update, returns HTTP 409.
- Unknown Document IDs remain rejected according to the established rule (observed service behavior: HTTP 404).
- Client collection omission/emptiness and existing/new/omitted entry handling follow the preserving rules; no update implicitly deletes a Document.
- PUT/PATCH behavior is consistent whether the Document is updated through the Client aggregate or its dedicated nested endpoint.

### Dependencies and unresolved decisions

- **VERIFIED:** Depends on accepted T2.1 persistence/identity constraints, T2.2 DTO and tri-state contracts, T2.3 Client service workflow, and T2.4 Document service/controller. D-11/D-13 and the approved requirement decisions govern boundaries and semantics.
- **VERIFIED:** No new design decision is required by the plan; HTTP 409 for cross-Client association and 404 for unknown IDs are already decided.
- **NOT AVAILABLE:** No Tech Lead approval of this proposal or authorization to implement T2.5 is recorded in the inspected decision log or supplied direct tasking. This proposal is not approval.
- **PENDING VERIFICATION:** Current T2.5 acceptance coverage and final compatibility must be confirmed by the Developer/Tester/Reviewer workflow after authorization.

## 11. Final status

**PROPOSAL COMPLETE — AWAITING TECH LEAD APPROVAL.** Scope and handoff are defined. Ready for Developer to begin now: **NO**; implementation must wait for explicit Human Tech Lead approval. No self-approval or test result is claimed.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Directly test the full T2.5 matrix, particularly existing Document ID retention, PUT collection-preservation cases, unknown ID and cross-owner behavior, and endpoint-path consistency.
2. **PENDING VERIFICATION:** If those tests reveal a defect, make only the smallest Service-layer change within T2.5 scope.
3. **VERIFIED:** Keep expected-version/force-increment and transaction-wide rollback criteria within T2.6/T2.7 respectively.
4. **NOT AVAILABLE:** Human Tech Lead approval to proceed with T2.5 implementation.

## 13. Handoff to next agent

- **VERIFIED:** Human Tech Lead: review/approve or revise this scope before implementation.
- **PENDING VERIFICATION:** After explicit approval, Developer: inspect current services and tests, implement only evidenced service-layer gaps, add the focused persistence-backed scenarios above, avoid unrelated API/schema changes, and report files/results. Tester must verify independently before reading Developer conclusions; Reviewer must audit independently before reading Developer/Tester reports.
- **VERIFIED:** Do not rerun, refactor or reopen accepted T2.1–T2.4 work unless a concrete T2.5 incompatibility is demonstrated. Final acceptance remains exclusively with the Human Tech Lead.
