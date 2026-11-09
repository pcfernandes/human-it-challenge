# Developer Report — T2.1 Focused Persistence Polish

## 1. Task ID

**VERIFIED:** T2.1 — Focused persistence polish, restricted to reviewer findings R-F01, R-F02 and R-F03, under the Tech Lead's direct authorization in execution `T2.1-EXEC-005-DEV-02`.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.1-EXEC-005-DEV-02` |
| Date | 2026-11-09 |
| Predecessor | `T2.1-EXEC-002-DEV-01` |
| History archive | `agent-reports/T2.1/history/developer-001.md` |

**VERIFIED:** The prior primary Developer report identified execution `T2.1-EXEC-002-DEV-01`; it was archived unchanged before this primary report was written. No earlier execution history files were present.

## 3. Agent role and model

- **VERIFIED:** Role — Senior Java/Spring Boot Developer.
- **VERIFIED:** Model — `gpt-6-luna` (exact model ID `opencode-go/gpt-6-luna`).

## 4. Authorized scope

**VERIFIED:** The human Tech Lead authorized corrections for R-F01, R-F02 and R-F03 only. Scope implemented:

- Add behavioral repository tests for all seven specified `ClientRepository` and `DocumentRepository` methods, including missing values, owner filtering, deterministic ascending-ID pagination, and committed optimistic force increment.
- Harden `Document.assignClient` persistent-identity checks, null rejection, ownership preservation and lazy-proxy behavior; clarify service-layer use and test `Client.removeDocument` edge cases without changing its collection-only semantics.
- Mark `Document.client`'s join column non-updatable and add regression coverage for persisted ownership.

**VERIFIED:** No repository query signatures, Flyway migrations, requirements, implementation plan, decision log, other agents' reports, `.opencode/agents/reviewer.md`, or unrelated application features were changed. No commit or push was performed.

## 5. Files inspected

**VERIFIED:** Read for this execution: `agent-reports/README.md`, `agent-reports/T2.1/reviewer-report.md`, predecessor `agent-reports/T2.1/developer-report.md`, `requirements-decisions.md`, `implementation-plan.md`, `Client.java`, `Document.java`, `ClientRepository.java`, `DocumentRepository.java`, `DomainPersistenceTest.java`, and `V2__create_clients_and_documents.sql`.

**VERIFIED:** Verification artifacts inspected: `target/surefire-reports/com.humanit.challenge.repository.DomainPersistenceTest.txt`, `target/surefire-reports/TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml`, Surefire `.txt` summaries, and the working-tree status. The history directory was checked before archiving; it contained no existing developer history files.

## 6. Files changed

| File | Change |
|---|---|
| `src/main/java/com/humanit/challenge/domain/Document.java` | Reject null owners; compare persisted owner IDs without initializing a proxy; preserve the current same-identity association; set `@JoinColumn(updatable = false)`. |
| `src/main/java/com/humanit/challenge/domain/Client.java` | Document that service-layer deletion pairs collection removal with explicit repository deletion in the same transaction; collection-preserving updates must not call removal. |
| `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java` | Add repository behavioral tests, commit-bound lock verification, removal edge cases, and persisted ownership regression checks. |
| `src/test/java/com/humanit/challenge/domain/DocumentAssociationTest.java` | Add direct null-owner assignment rejection test. |
| `agent-reports/T2.1/history/developer-001.md` | Archive the previous Developer report unchanged. |
| `agent-reports/T2.1/developer-report.md` | Replace the primary Developer report with this execution record. |

**VERIFIED:** No Flyway migration file was changed. Existing `V2__create_clients_and_documents.sql` was read and remains outside the changed-file list.

## 7. Commands executed

**VERIFIED:** Commands run from the repository root:

| Command | Observed result |
|---|---|
| `git status --short` | Before edits, showed `.opencode/agents/reviewer.md` modified and T2.1 implementation/report paths untracked. This unrelated reviewer-agent edit was not changed. |
| `.\mvnw.cmd -Dtest=DomainPersistenceTest test` | Initial run: 19 tests, 1 failure. The proxy test observed that the association reference was replaced (persistence-util loaded state), not that the Hibernate proxy had initialized. The test was refined to inspect the original proxy directly, and same-identity assignment now preserves it. |
| `.\mvnw.cmd -Dtest=DomainPersistenceTest test` | Rerun: 19 tests, 0 failures, 0 errors, 0 skipped. |
| `.\mvnw.cmd clean verify` | **BUILD SUCCESS**; aggregate 61 tests, 0 failures, 0 errors, 0 skipped. `DomainPersistenceTest`: 19 tests; `DocumentAssociationTest`: 1 test. |
| `git diff --check` | No output; no whitespace errors reported. |
| `git status --short` | Confirmed expected source/test paths and the pre-existing `.opencode/agents/reviewer.md` modification. |

## 8. Evidence and observed results

### R-F01 — Repository behavior

- **VERIFIED (`DomainPersistenceTest.java`; Surefire report):** Tests invoke `ClientRepository.existsByTaxIdentifier` and `findByTaxIdentifier` with existing and missing values, checking that lookup does not change the Client's version or tax identifier.
- **VERIFIED (`DomainPersistenceTest.java`):** `lockById` is exercised in a programmatic transaction outside the test-managed transaction. A re-read after transaction completion confirms the Client version increased; the Client's tax identifier and email remain unchanged. Missing ID returns empty.
- **VERIFIED (`DomainPersistenceTest.java`):** `DocumentRepository.existsByNumber`, `findByNumber`, `findByClientId`, and `findByIdAndClientId` are exercised with present/missing values, correct and incorrect Client IDs, and a missing owner/document. Pages use `Sort.by(ASC, "id")`; assertions check page contents, page boundaries, total count, and owner separation.
- **VERIFIED (`com.humanit.challenge.repository.DomainPersistenceTest.txt`):** The class reports 19 tests, 0 failures, 0 errors and 0 skipped.

### R-F02 — Relationship helpers

- **VERIFIED (`Document.java`, `DocumentAssociationTest.java`, `DomainPersistenceTest.java`):** Owner assignment rejects null. If a Document already has an owner, same-reference assignment is a no-op; another object with the same non-null persistent ID is accepted without replacing the current association; a different or transient identity is rejected. The integration test checks that the original Hibernate proxy remains uninitialized.
- **VERIFIED (`Client.java`, `DomainPersistenceTest.java`):** `removeDocument` continues to remove only from the in-memory collection. Its Javadoc now directs service-layer deletion to pair it with `DocumentRepository.delete(document)` in the same transaction and forbids use for collection-preserving updates. Tests cover persisted and transient removal, null/absent removal, retained owning-side reference, and no unintended row creation/deletion. No `orphanRemoval` was introduced.

### R-F03 — Immutable database ownership

- **VERIFIED (`Document.java`, `DomainPersistenceTest.java`):** `Document.client` is mapped with `@JoinColumn(updatable = false)`. Regression checks confirm a different Client cannot be assigned after persistence, verify the mapping flag, update a scalar field, and query the stored `client_id` to ensure it remains the original owner.
- **VERIFIED (`TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml`):** Hibernate's scalar Document `UPDATE` statements omit `client_id`; the emitted statement updates only `description`, `expiration_date`, `number`, and `version` (plus the ID/version predicate).

### Build and schema verification

- **VERIFIED (`.\mvnw.cmd clean verify` output and Surefire summaries):** 61 total tests passed with 0 failures, 0 errors and 0 skipped; `BUILD SUCCESS`.
- **VERIFIED (`DomainPersistenceTest.java`, `target/surefire-reports/TEST-com.humanit.challenge.repository.DomainPersistenceTest.xml`):** The persistence test enables Flyway and sets `spring.jpa.hibernate.ddl-auto=validate`. Its test asserts Flyway applied version 2 and that both business tables exist; the test context started and the class passed. The existing V2 migration was not modified.
- **VERIFIED (`git diff --check`):** No whitespace errors were reported.
- **NOT AVAILABLE:** This execution did not run against PostgreSQL; real PostgreSQL migration/schema validation remains unverified.
- **VERIFIED (`git status --short`):** `.opencode/agents/reviewer.md` remains an unrelated pre-existing working-tree modification and was not altered by this execution.
- **PENDING VERIFICATION:** Independent Tester verification, independent Reviewer audit, and human Tech Lead acceptance have not occurred as part of this Developer execution.

## 9. Findings and severity

| Finding | Severity | Developer disposition |
|---|---|---|
| R-F01 | Minor | **VERIFIED:** Behavioral repository coverage added for all seven methods, with existing/missing lookups, ownership filtering, sorted pagination, committed force increment and side-effect checks. Independent confirmation remains pending. |
| R-F02 | Minor | **VERIFIED:** Proxy-safe persistent identity and null-owner guard added; collection removal remains non-deleting and documented for explicit service-layer deletion. Focused regression/edge tests added. |
| R-F03 | Minor | **VERIFIED:** Join column is non-updatable; persisted ownership regression and emitted SQL were checked. |
| R-F04 | Minor (pre-existing, out of scope) | **VERIFIED:** The unrelated `.opencode/agents/reviewer.md` modification remains present and untouched; exclude it from any later T2.1 staging/commit. |
| R-F06 | Info | **NOT AVAILABLE:** Live PostgreSQL integration was not performed. |

**PENDING VERIFICATION:** These are Developer implementation dispositions, not independent review findings or approval. No Critical/Major classification is claimed on behalf of another role.

## 10. Decisions and reasoning

- **VERIFIED:** Persistent identity uses the existing owner's and requested owner's `getId()` values, with a same-reference fast path. The method leaves an existing association intact for matching persistent IDs; a test confirms the Hibernate proxy itself is not initialized. This avoids relationship traversal/lazy loading while preserving immutable ownership.
- **VERIFIED:** `Client.removeDocument` semantics were not changed. Because collection removal does not delete a row or clear the owning side, service code must pair removal with explicit repository deletion in the same transaction; collection-preserving updates must leave the collection alone. `orphanRemoval` remains absent.
- **VERIFIED:** `updatable = false` aligns Hibernate's generated updates with the domain rule that ownership is immutable. A scalar update's observed SQL and a database owner check protect against regressions.
- **VERIFIED:** Repository pagination behavior is tested with the approved caller-provided ascending-ID sort; the repository contract/signatures remain unchanged.
- **VERIFIED:** Flyway V2 was not edited, as the authorization explicitly prohibited migration changes.

## 11. Final status

**IMPLEMENTATION COMPLETE — R-F01, R-F02 AND R-F03 CORRECTIONS IMPLEMENTED; CLEAN VERIFICATION PASSED; INDEPENDENT TESTER/REVIEWER WORK AND TECH LEAD ACCEPTANCE PENDING.**

**VERIFIED:** `.\mvnw.cmd clean verify` succeeded with 61 tests, 0 failures, 0 errors and 0 skipped. This does not constitute T2.1 approval or final acceptance.

## 12. Outstanding issues

1. **PENDING VERIFICATION:** Tester must independently verify the implementation before consulting Developer conclusions, then compare evidence according to `agent-reports/README.md`.
2. **PENDING VERIFICATION:** Reviewer must complete an independent audit before reading Architect, Developer or Tester reports.
3. **PENDING VERIFICATION:** Only the human Tech Lead may approve T2.1; no approval is claimed here.
4. **NOT AVAILABLE:** Live PostgreSQL migration and Hibernate-validation evidence was not produced in this execution.
5. **VERIFIED:** The unrelated pre-existing `.opencode/agents/reviewer.md` modification was left untouched; it remains excluded from this authorized change set.

## 13. Handoff to next agent

- **To the Tester:** Perform independent verification of the repository behavior, owner/proxy helpers, explicit-delete semantics and non-updatable ownership before reading this report; after that independent pass, compare findings against the recorded execution evidence.
- **To the Reviewer:** Audit independently before reading Architect, Developer or Tester reports, as required by the workflow.
- **To the Tech Lead:** Review the independent Tester and Reviewer outcomes and make any acceptance decision. This report does not approve T2.1.

---
**Report status:** Written for `T2.1-EXEC-005-DEV-02`; predecessor `T2.1-EXEC-002-DEV-01` archived at `agent-reports/T2.1/history/developer-001.md`. Primary report path: `agent-reports/T2.1/developer-report.md`.
