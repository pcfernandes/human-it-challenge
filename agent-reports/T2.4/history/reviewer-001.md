# Reviewer Report — T2.4 Part 1: DocumentService

## 1. Task ID

**VERIFIED (direct tasking):** T2.4 Part 1 — Complete DocumentService review.

## 2. Execution identifier and date

**VERIFIED (direct tasking/environment):** `T2.4-EXEC-003-REV-SERVICE-RETRY` — 2026-11-11.

## 3. Agent role and model

**VERIFIED:** Reviewer; `gpt-6-luna` (`opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED (direct tasking):** Independently review DocumentService transaction/version handling, parent `OPTIMISTIC_FORCE_INCREMENT`, ownership and uniqueness, PATCH tri-state semantics, and 400/404/409 behavior; read Developer and Tester reports only after inspecting implementation; write this report. No application/test changes, Maven, Git mutation or self-approval.

## 5. Files inspected

- **VERIFIED:** `agent-reports/README.md`, `requirements-decisions.md`, `implementation-plan.md`, and `agent-reports/decisions.md` (D-11, D-13).
- **VERIFIED (independent inspection before reports):** `src/main/java/com/humanit/challenge/service/DocumentService.java`, `repository/ClientRepository.java`, `repository/DocumentRepository.java`, `domain/Client.java`, `domain/Document.java`, `mapper/DocumentMapper.java`, Document request DTOs, `dto/patch/PatchValue.java`, `PatchValueDeserializer.java`, `PatchRequiredValidator.java`, `PatchValueExtractor.java`, `exception/GlobalExceptionHandler.java`, `service/ClientService.java`, and `controller/ClientController.java`.
- **VERIFIED (independent inspection before reports):** `src/test/java/com/humanit/challenge/service/DocumentServiceTest.java`, `dto/document/DocumentPatchRequestValidationTest.java`, and `mapper/DocumentMapperTest.java`.
- **VERIFIED (after independent inspection):** `agent-reports/T2.4/developer-report.md` and `agent-reports/T2.4/tester-report.md`.
- **VERIFIED:** `src/main/java/com/humanit/challenge/controller/DocumentController.java` was not found; T2.4 implementation plan places the nested controller in scope.

## 6. Files changed

- **VERIFIED:** Only this Reviewer report was created. No prior T2.4 Reviewer primary report appeared in the task-directory listing, so no history archive was required.

## 7. Commands executed

- **VERIFIED:** None. No shell/Git command and no Maven command was run. Source/report inspection used project-relative file paths.

## 8. Evidence and observed results

- **VERIFIED (`DocumentService.java:35-111`; `requirements-decisions.md:384-392`):** Mutating service methods are transactional; reads are read-only. Document versions are compared for update/delete, and explicit flushes are used within the service’s conflict-catching blocks.
- **VERIFIED (`ClientRepository.java:20-22`; `DocumentService.java:38,66,84,102`):** Each mutation loads the path Client using `OPTIMISTIC_FORCE_INCREMENT`; expected Client version is compared before the Document operation. **RECONSTRUCTED:** A competing Client-version change detected only when the force-increment is finalized at transaction completion can escape the service’s `try/catch` blocks: those blocks end before the transactional proxy commits the method (`DocumentService.java:44-47,75-78,95-98,109-111`). `GlobalExceptionHandler.java:140-155` maps `ResponseStatusException`; otherwise its catch-all at lines 157-165 emits 500. This conflicts with the required stale-version 409 and needs provider-level concurrency verification.
- **VERIFIED (`DocumentService.java:126-129`; `DocumentRepository.java:19`):** Get/update/PATCH/delete resolve Documents by both document and client IDs; foreign-owner nested access is therefore returned as 404 rather than exposing/transferring ownership. `list` checks Client existence and scopes repository results (`DocumentService.java:54-60`).
- **VERIFIED (`DocumentService.java:39,69,87-89,140-146`; `Document.java:19-21`; `DocumentRepository.java:15`):** Number uniqueness is global; update checks exclude the current row. The database unique constraint provides race protection, and write/flush integrity errors inside the catches map to 409.
- **VERIFIED (D-13; `DocumentPatchRequest.java:21-37`; `PatchValueDeserializer.java:17-24,66-70`; `DocumentMapper.java:97-103,183-197`):** PATCH distinguishes absent, explicit null and value; absent fields preserve current state, required scalar nulls are rejected by DTO validation, and description null clears it. No PATCH semantic defect found.
- **VERIFIED (`DocumentService.java:114-149`; `DocumentPutRequest.java:16-30`; `DocumentPatchRequest.java:25-37`):** Null expected Client/Document versions produce 400; stale versions produce 409; missing Client or path-scoped Document produces 404. PUT/PATCH DTO body versions have `@PositiveOrZero`, but `lockClient` and `enforceDocumentVersion` do not reject negative `Long` values. **RECONSTRUCTED:** A negative version supplied via a version header (including delete headers) reaches the equality check and is reported as 409, although requirements classify invalid versions as 400 (`requirements-decisions.md:382-386`). HTTP header validation cannot yet be confirmed because `DocumentController` is absent.
- **VERIFIED (`DocumentServiceTest.java:48-155`; `DocumentPatchRequestValidationTest.java:32-129`):** Existing tests cover key CRUD, scoped ownership, duplicate-create 409, stale version 409 and description clearing; direct service assertions for missing/negative version statuses, parent-version increment, rollback on conflict, PATCH omission preservation and duplicate update/PATCH are absent. No tests were run in this review.
- **VERIFIED (comparison):** Developer report Sections 8-9 claims no blocking issue, and Tester report Sections 8-9 similarly found none. Both correctly describe scoped ownership, uniqueness checks, PATCH behavior and null/stale status handling. **DISAGREEMENT:** Those conclusions do not account for transaction-completion optimistic-lock failure mapping (F-01) or negative header-version status handling (F-02). Tester independently noted missing direct assertions for several contract edges; those coverage gaps remain.
- **NOT AVAILABLE:** Runtime evidence demonstrating the exact exception type/timing for a concurrent parent force-increment conflict; this review did not run a concurrency test or Maven.

## 9. Findings and severity

- **F-01 — Major: parent optimistic-lock conflicts may escape 409 mapping.** `OPTIMISTIC_FORCE_INCREMENT` is requested at `ClientRepository.java:20-22`, while service catches end inside the method at `DocumentService.java:44-47,75-78,95-98,109-111`. If the provider finalizes the forced version update during transaction completion, that exception occurs after those catches. The current exception handler has no explicit optimistic-lock mapping (`GlobalExceptionHandler.java:140-165`), so this can become 500 instead of the required 409. Add a transaction-boundary-safe mapping (for example, a focused exception translation layer that catches the commit failure) and an integration test with concurrent mutations to prove 409 plus full rollback. **RECONSTRUCTED; PENDING VERIFICATION** of actual provider timing and outward status. This is blocking until addressed or disproven by authorized evidence.
- **F-02 — Minor: negative header versions are not rejected as 400 by the service.** `lockClient` checks only null (`DocumentService.java:114-122`) and `enforceDocumentVersion` checks only null/equality (`131-137`); negative values therefore classify as stale 409. DTO validation covers update body versions, but expected Client and DELETE versions are headers. Ensure controller header validation and/or service checks reject values below zero as 400, with tests for create/update/delete header cases. **VERIFIED** code behavior; endpoint behavior **PENDING VERIFICATION** until DocumentController exists.
- **F-03 — Minor: missing direct regression assertions.** Current service tests do not directly assert parent version increments for all mutation types, rollback after conflict, invalid/missing version responses or omitted-field PATCH preservation. Add focused persistence-backed assertions with the authorized follow-up. **VERIFIED** from inspected tests; not a claim that these behaviors are all incorrect.

## 10. Decisions and reasoning

- **VERIFIED (D-11):** Service/repository/mapper responsibilities and transaction ownership follow the approved separation; no D-11 violation was found.
- **VERIFIED (D-13):** DTO/mapper PATCH semantics match the approved tri-state contract; no change to accepted T2.2/T2.3 behavior is proposed.
- **RECONSTRUCTED:** The parent force-increment commit-failure path is a blocking risk because its transaction interceptor boundary lies beyond the service-local catches and no global optimistic-lock mapping is present. A focused concurrency test is needed to establish provider-specific details.
- **VERIFIED:** No Tech Lead acceptance or approval is claimed. A Reviewer recommendation is not final acceptance.

## 11. Final status

**FAIL** — one Major finding (F-01) remains unresolved; two Minor findings are also outstanding. This is an audit result, not a Tech Lead decision.

## 12. Outstanding issues

1. **F-01:** Demonstrate and correctly map concurrent parent force-increment failure to HTTP 409, retaining full transaction rollback.
2. **F-02:** Ensure negative expected-version header values return 400, including DELETE versions.
3. **F-03:** Add direct tests for force increments, rollback, version validation and PATCH omission.
4. **VERIFIED:** DocumentController is not present; controller binding and endpoint status behavior remain unreviewed.

## 13. Handoff to next agent

- **VERIFIED:** Ready for DocumentController: **NO** until F-01 is resolved or disproven with a concurrency/commit-boundary test and the invalid-header-version contract is explicitly enforced.
- **PENDING VERIFICATION:** After service correction/verification, review DocumentController mappings, `@Valid`/header validation, response statuses and Problem Details. Final acceptance belongs only to the Human Tech Lead.
