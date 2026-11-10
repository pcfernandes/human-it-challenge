# Architect Approval Record — T2.2 DTOs, Validation and Mappers

## 1. Task ID

**VERIFIED:** T2.2 — Formalize Human Tech Lead approval of the five pending architecture decisions; implementation is expressly not authorized by this task.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.2-EXEC-005-ARCH-01` |
| Date | 2026-11-10 |
| Previous primary execution | **VERIFIED:** `T2.2-EXEC-004-ARCH-01`, preserved unchanged as `agent-reports/T2.2/history/architect-003.md`. |
| Earlier history | **VERIFIED:** `architect-001.md` and `architect-002.md` already existed; this is the next available sequential Architect archive. |

## 3. Agent role and model

- **VERIFIED:** Role — Architect, senior Java and Spring Boot software architect.
- **VERIFIED:** Model — `gpt-6-luna` (exact model ID `opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct Human Tech Lead tasking):** Append decision D-13 recording approval of all five T2.2 design decisions; archive and update this Architect report under the mandatory history convention; read back and verify the decision and report updates.

**VERIFIED (explicit boundary):** This is documentation/decision-recording work only. The task explicitly authorizes no implementation. Modify only `agent-reports/decisions.md`, this Architect report, and its required history archive. Do not modify requirements, the implementation plan, Java source, tests, migrations, configuration, other agents' reports or instructions. Do not stage, commit or push.

## 5. Files inspected

| Artifact | Evidence / relevance |
|---|---|
| `agent-reports/README.md` | **VERIFIED:** Mandatory 13-section report format, evidence markers, append-only decision log and report history rules. |
| `agent-reports/decisions.md` | **VERIFIED:** Existing approved D-10 (T2.1), D-11 (application-layer boundaries) and D-12 (D-11 documentation closure); D-13 is next sequential decision. |
| `agent-reports/T2.2/architect-proposal.md` | **VERIFIED:** Prior T2.2 proposal with five design questions previously marked approval-required; archived unchanged before replacement. |
| `agent-reports/T2.2/history/architect-001.md`, `architect-002.md` | **VERIFIED:** Earlier Architect executions, establishing `architect-003.md` as the next archive. |
| `requirements-decisions.md` | **VERIFIED:** Existing request, PATCH, nested collection, validation, concurrency and ownership contracts. |
| `implementation-plan.md` | **VERIFIED:** T2.2 DTO/validation/mapper scope, later service workflow boundaries and D-11 constraints. |
| `src/main/java/com/humanit/challenge/domain/Client.java` | **VERIFIED:** Client scalar mappings, lazy Documents collection, version and relationship-consistency methods. |
| `src/main/java/com/humanit/challenge/domain/Document.java` | **VERIFIED:** Document scalar mappings, lazy owner, version and immutable ownership mapping. |
| `src/main/resources/db/migration/V2__create_clients_and_documents.sql` | **VERIFIED:** Existing persistence lengths: Client first/last 100, tax identifier 50, email 255, phone 50; Document number 100 and description 1000. |
| `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java` | **VERIFIED:** Existing persistence tests exercise lazy relationships, collection preservation, ownership, constraints and optimistic versions. |

## 6. Files changed

| Path | Change |
|---|---|
| `agent-reports/decisions.md` | **VERIFIED after read-back:** Appended D-13; prior D-10, D-11 and D-12 entries were not edited. |
| `agent-reports/T2.2/history/architect-003.md` | **VERIFIED after read-back:** Preserved the former primary report unchanged before replacing it. |
| `agent-reports/T2.2/architect-proposal.md` | **VERIFIED:** Replaced with this approval record reflecting all five decisions as approved. |

**VERIFIED:** No source code, test, migration, configuration, requirements, implementation plan, other role report or agent instruction was modified.

## 7. Commands executed

**VERIFIED:** None. No shell, build, test or Git command was executed. Authorized file reads, searches and report/decision-file operations only.

## 8. Evidence and observed results

- **VERIFIED (direct Human Tech Lead tasking):** The Human Tech Lead explicitly approved all five pending T2.2 design decisions and explicitly stated that no implementation is authorized by this task.
- **VERIFIED (`agent-reports/decisions.md`, before append):** D-10, D-11 and D-12 were present; D-13 was the next sequential decision number.
- **VERIFIED (`Client.java`, `Document.java`, V2 migration):** Existing entity annotations and migration define the field lengths reflected in approved `@Size` rules. T2.2 is to change request validation only, not persistence schema or entity constraints.
- **VERIFIED (`requirements-decisions.md` and `implementation-plan.md`):** Existing required-field, email, null, expired-document, collection-preservation, ownership and later service-use-case contracts remain relevant and are not replaced by this decision.
- **VERIFIED after read-back:** D-13 appears exactly once as a numbered decision. D-10, D-11 and D-12 remain present with their prior text unchanged; only the new D-13 text was appended.
- **VERIFIED after read-back:** The current Architect report records all five decisions as approved. The former primary is preserved at `history/architect-003.md`.
- **VERIFIED:** No tests/builds were run or claimed. No implementation was performed or authorized.

## 9. Findings and severity

| Finding | Severity | Evidence / disposition |
|---|---|---|
| Jackson tri-state binding must correctly retain omitted versus explicit-null state through JSON deserialization | **Implementation risk** | **VERIFIED (approved semantics):** Binding and validation behavior needs focused implementation tests; this is not a remaining approval question. |
| Client response embeds nested Documents while persistence collection is lazy | **Integration risk** | **VERIFIED (approved response contract and T2.1 mappings):** Later response-producing use cases must intentionally load the collection; mappers must not query repositories. |
| Nested Client PUT requires cross-field ID/version shape and required Document values | **Implementation risk** | **VERIFIED (approved semantics):** DTO Bean Validation can validate shape/value rules, while ownership resolution, unknown IDs, version checking and aggregate orchestration belong to later service use cases. |
| Persistence migration/entity lengths are the validation boundary reference | **VERIFIED; no schema change** | **VERIFIED (V2 migration/entities):** Apply `@Size` to request DTO fields using approved limits; do not alter T2.1 schema or entity constraints in T2.2. |
| No implementation authorization | **Scope blocker for coding only** | **VERIFIED (direct tasking):** This task closes the architecture-decision record only. Any implementation requires separate authorization. |

**VERIFIED:** No blocker remains to this documentation task. This execution does not assess implementation quality because no implementation was performed.

## 10. Decisions and reasoning

**VERIFIED:** The five proposals in the previous T2.2 Architect report are now approved by the Human Tech Lead and recorded in D-13. They are design constraints for a separately authorized implementation; their approval is not implementation authorization.

### 1. PATCH representation

**VERIFIED — approved:** Use a project-owned tri-state wrapper integrated with Jackson. The wrapper distinguishes `ABSENT` (omitted property), `PRESENT_NULL` (explicit JSON null) and `PRESENT_VALUE` (non-null value). Do not add an external JSON-nullable dependency. Omission preserves existing state; explicit null is rejected for required fields; explicit null clears optional Document `description`; Client PATCH `documents: null` is invalid.

**Reasoning:** A conventional nullable property cannot safely carry the approved omission-versus-null semantics by itself. A local wrapper avoids an external dependency while making the distinction explicit. The Jackson integration and each state require binding/validation tests.

### 2. Response contract

**VERIFIED — approved:** `ClientResponse` includes nested `DocumentResponse` entries. `DocumentResponse` includes scalar `clientId` and must not embed a `Client` object. Avoid recursive DTO graphs. A future service/repository use case must intentionally load the collection needed for `ClientResponse`; mappers must not query repositories.

**Reasoning:** Nested output is an explicit public contract, but keeping the reverse association scalar-only avoids recursive response graphs. Intentional loading at the use-case boundary respects the lazy T2.1 mapping and D-11 mapper/repository separation.

### 3. Nested Client PUT

**VERIFIED — approved:** Every supplied nested Document item must contain valid `number` and `expirationDate`, whether it creates or updates a Document. Existing nested Documents require both ID and expected version. New nested Documents must supply neither ID nor version. Ownership resolution, unknown-ID handling, version checking and aggregate merge orchestration remain responsibilities of later service use cases. Omitted nested Documents are not deleted; omitted or empty `documents` preserves existing Documents; explicit `documents: null` is invalid.

**Reasoning:** PUT entries are complete for their required editable fields, while item identity/version discriminate create versus update. Keeping identity resolution and aggregate orchestration out of DTOs/mappers follows D-11 and the planned T2.5 service responsibility.

### 4. Client POST null collection

**VERIFIED — approved:** `ClientCreateRequest.documents: null` is treated as omitted; creating a Client without Documents is valid. This rule applies only to Client POST, not Client PUT or PATCH; explicit `documents: null` remains invalid there.

**Reasoning:** This establishes the approved optional-create collection behavior without weakening the distinct update contract.

### 5. Validation

**VERIFIED — approved:** Negative expected-version values are invalid requests. Apply Bean Validation `@Size` limits matching current persistence column lengths: Client `firstName` 100, `lastName` 100, `taxIdentifier` 50, `email` 255, `phoneNumber` 50; Document `number` 100 and `description` 1000. Retain required-field, email-format and nullable-field rules. Do not add country-specific tax/phone formats. Expired Documents remain valid. Do not modify database schema or entity constraints in T2.2.

**Reasoning:** DTO size checks align requests with the existing storage limits without changing T2.1 persistence. Existing generic required/email/null semantics remain; formats and date-future restrictions are not expanded.

### Compatibility and scope

- **VERIFIED:** D-13 is compatible with D-10: T2.1 remains accepted and closed; its entities, migration and persistence behavior are not reopened or changed.
- **VERIFIED:** D-13 is compatible with D-11: DTO-to-entity mapping stays in dedicated mappers, mappers do not access repositories, and later services own use-case workflows and orchestration.
- **VERIFIED:** D-13 is compatible with D-12: documentation review closure remains accepted; this decision neither revises D-12 nor requires corrective work for its accepted finding.
- **VERIFIED:** T2.2 remains limited to request/response DTOs, Bean Validation and dedicated mapping. No security DTOs, endpoint/controller work, service/repository use-case implementation, migration, schema change or entity-constraint change is authorized here.
- **VERIFIED:** Implementation remains pending separate Human Tech Lead authorization. D-13 formalizes design approval only.

## 11. Final status

**DOCUMENTATION COMPLETE — D-13 RECORDED; FIVE T2.2 DESIGN DECISIONS APPROVED.** The prior Architect report was archived, the current report now reflects the approved semantics, and the decision/report read-back checks passed. **No implementation is authorized or performed by this task.**

## 12. Outstanding issues

1. **VERIFIED:** There are no outstanding approval questions among the five T2.2 decisions recorded in D-13.
2. **PENDING VERIFICATION:** Jackson wrapper implementation must demonstrate correct absent/null/value binding and validation in authorized tests.
3. **PENDING VERIFICATION:** Future response-producing use cases must load Client Documents intentionally, without repository access from mappers.
4. **PENDING VERIFICATION:** Future service use cases must enforce ownership, unknown-ID, expected-version and atomic aggregate-merge rules for nested PUT as required by the approved contracts.
5. **VERIFIED:** No database schema or entity constraint change is in T2.2; current persistence limits are evidenced by the T2.1 entity mappings and V2 migration.
6. **VERIFIED:** Separate implementation authorization is still required. No implementation task has been authorized by this decision-recording task.
7. **VERIFIED:** No test/build execution was required or run for this documentation-only execution.

## 13. Handoff to next agent

- **VERIFIED:** D-13 is the authoritative record of the five approved T2.2 design decisions; D-10, D-11 and D-12 remain unchanged and authoritative for their respective scopes.
- **VERIFIED:** This report supersedes the previous primary proposal for decision status, with the prior report preserved unchanged in `agent-reports/T2.2/history/architect-003.md`.
- **VERIFIED:** Do not begin implementation based on this decision-recording task alone. Wait for the Human Tech Lead's separate implementation authorization, then implement only the approved T2.2 scope.
- **VERIFIED:** Any future Tester must verify independently before reading Developer conclusions. Any future Reviewer must audit independently before reading Developer/Tester reports or this Architect proposal.
- **VERIFIED:** No next implementation step is proposed or authorized here.

---
**Report status:** Written for `T2.2-EXEC-005-ARCH-01`. Previous primary archived at `agent-reports/T2.2/history/architect-003.md`; current primary report: `agent-reports/T2.2/architect-proposal.md`.
