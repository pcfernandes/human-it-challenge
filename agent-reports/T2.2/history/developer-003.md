# Developer Report — T2.2 Implementation Part 1/3: DTO Contracts and Jakarta Bean Validation

## 1. Task ID

**VERIFIED:** T2.2 — Implementation Part 1/3 (DTO contracts and Jakarta Bean Validation), authorized by the Human Tech Lead under D-13. Part 1 only. Parts 2 (Jackson tri-state PATCH binding) and 3 (dedicated mappers, remaining tests, regression verification, Git commit/push) are **not** implemented in this execution.

## 2. Execution identifier and date

| Field | Value |
|---|---|
| Execution ID | `T2.2-EXEC-009-DEV-01` — **RECONSTRUCTED:** the sequence number is inferred from the highest recorded identifier (`T2.2-EXEC-008-REV-01`, Reviewer). No in-project record of this exact identifier was provided by the tasking. |
| Date | 2026-11-10 (Europe/Lisbon). **VERIFIED:** Maven log timestamps `2026-11-10 16:13Z` (baseline) and later runs in the same session. |
| Branch | **VERIFIED:** `git branch --show-current` = `feature/T2.2-dtos-mappers` (expected branch; not switched). |
| Base commit | **VERIFIED:** `git log --oneline -1` = `8810cd2 docs(architecture): formalize D-11 layer separation and approval`. |
| Previous primary archived | **VERIFIED:** former primary (`T2.2-EXEC-007-DEV-01`, documentation-only corrections) copied unchanged to `agent-reports/T2.2/history/developer-002.md`; SHA-256 `01CD37A0…4077B245` identical for source and archive (`Get-FileHash`). `developer-001.md` (`T2.2-EXEC-002-DEV-01`) untouched. |

## 3. Agent role and model

- **VERIFIED:** Role — Developer, senior Java and Spring Boot engineer.
- **VERIFIED:** Model — `claude-haiku-5-5` (exact model ID `opencode-go/claude-haiku-5-5`), as stated in the execution environment.

## 4. Authorized scope

**VERIFIED (Human Tech Lead tasking):**

- Implement the approved Client and Document request/response DTOs with Jakarta Bean Validation constraints and focused validation tests (Part 1 only).
- Use the Architect package structure; respect approved domain names and API contracts; `ClientResponse` embeds `DocumentResponse`; `DocumentResponse` exposes scalar `clientId`.
- Maximum lengths: Client `firstName` 100, `lastName` 100, `taxIdentifier` 50, `email` 255, `phoneNumber` 50; Document `number` 100, `description` 1000.
- Reject blank required strings, validate email format, reject negative expected versions, keep expired Documents valid, no country-specific formats, no entity/schema changes.
- Client POST `documents: null` treated as omitted; Client PUT `documents: null` invalid; omitted/empty PUT documents preserve Documents; nested Document rules as in D-13.
- PATCH: only what is achievable without tri-state infrastructure; no substitute (Optional, nullable fields, `JsonNode`, `JsonNullable`).

**VERIFIED (explicit prohibitions honoured):** No Git add/commit/push/merge; no changes to T2.1 entities, repositories, Flyway migrations, services, REST controllers, dedicated mappers, Jackson deserializer, requirements, `decisions.md`, Architect/Reviewer reports, project-level `reports/*.md`, or agent definitions.

## 5. Files inspected

| Artifact | Evidence / relevance |
|---|---|
| `agent-reports/README.md` | **VERIFIED:** 13-section format, evidence markers, history convention. |
| `agent-reports/decisions.md` | **VERIFIED:** D-10 (T2.1 closed), D-11 (layering), D-12, D-13 (five approved T2.2 decisions, full text read via `git diff` after the Read tool truncated the line). |
| `requirements-decisions.md` | **VERIFIED:** Sections 2, 4, 6, 8, 9, 11, 20 (field rules, PUT/PATCH semantics, validation, versioning). Status line 5 and line 962 updated under the prior documentation task. |
| `implementation-plan.md` | **VERIFIED:** T2.2 acceptance criteria (lines ~195–209) and Phase 2 D-11 boundaries. |
| `agent-reports/T2.2/architect-proposal.md` | **VERIFIED:** D-13 approval record. Contains **no package structure** (see Finding F-P1-01). |
| `agent-reports/T2.2/history/architect-003.md` | **VERIFIED:** Superseded proposal that *does* define packages (`com.humanit.challenge.dto.client`, `com.humanit.challenge.dto.document`, mappers in `com.humanit.challenge.mapper`), labelled PROPOSED there. Used as the package reference; see F-P1-01. |
| `agent-reports/T2.2/reviewer-report.md` | **VERIFIED:** `T2.2-EXEC-008-REV-01` — PASS WITH FINDINGS, 0 Critical / 0 Major, RF2-1 Minor (documentation tracking, non-blocking). Read for context; Reviewer's conclusions not reused as evidence. |
| `agent-reports/T2.2/developer-report.md` | **VERIFIED:** Former primary (documentation corrections) — archived before replacement. |
| `src/main/java/com/humanit/challenge/domain/Client.java` | **VERIFIED:** Column lengths; lazy `documents`; `@Version`; `addDocument`/`removeDocument` helpers (not modified). |
| `src/main/java/com/humanit/challenge/domain/Document.java` | **VERIFIED:** Column lengths (`number` 100, `description` 1000); required `expiration_date`; lazy `client`; `assignClient` guard (not modified). |
| `src/main/resources/db/migration/V2__create_clients_and_documents.sql` | **VERIFIED:** `VARCHAR` lengths match the approved `@Size` values. |
| `src/main/java/com/humanit/challenge/repository/ClientRepository.java`, `DocumentRepository.java` | **VERIFIED:** Existing repositories; not modified; no DTO code depends on them. |
| `src/test/java/com/humanit/challenge/repository/DomainPersistenceTest.java`, `domain/DocumentAssociationTest.java`, `config/SourceScanAuditTest.java` | **VERIFIED:** Existing tests; not modified; scanned only for conventions and for the source-scan audit scope (scans `src/main` only). |
| `pom.xml` | **VERIFIED:** Spring Boot 3.5.16 parent, Java 21, `spring-boot-starter-validation` (Jakarta Validation/Hibernate Validator), `spring-boot-starter-web` (Jackson), `spring-boot-starter-test`. No dependency change was made. |
| `src/main/resources/*.yml` | **VERIFIED (content search):** No Jackson customization exists (no `default-property-inclusion`, no `Nulls` configuration). |

**VERIFIED (read-only Git checks):** `git branch --show-current`, `git status --short`, `git diff --stat`, `git log --oneline -1`, `git diff --check`, `git check-ignore`.

## 6. Files changed

### 6.1 Created — application code (Part 1 DTOs)

| Path | Purpose |
|---|---|
| `src/main/java/com/humanit/challenge/dto/document/DocumentCreateRequest.java` | Body of `POST /clients/{clientId}/documents` (no version; the `X-Client-Version` header is not part of the body). |
| `src/main/java/com/humanit/challenge/dto/document/DocumentPutRequest.java` | Body of `PUT /clients/{clientId}/documents/{documentId}` (expected Document `version` in body). |
| `src/main/java/com/humanit/challenge/dto/document/DocumentResponse.java` | Response record; scalar `clientId`, no Client reference. |
| `src/main/java/com/humanit/challenge/dto/client/ClientCreateRequest.java` | Body of `POST /clients`; `documents` optional, `documents: null` normalized to empty. |
| `src/main/java/com/humanit/challenge/dto/client/ClientPutRequest.java` | Body of `PUT /clients/{id}`; `version` required; `documents` required non-null (see §10). |
| `src/main/java/com/humanit/challenge/dto/client/ClientDocumentPutRequest.java` | Nested Document entry for Client PUT; identity-pair rule. |
| `src/main/java/com/humanit/challenge/dto/client/ClientResponse.java` | Response record embedding `List<DocumentResponse>`. |

### 6.2 Created — tests

| Path | Purpose |
|---|---|
| `src/test/java/com/humanit/challenge/dto/DtoValidationSupport.java` | Test helper: plain Bean Validation `Validator`, violation lookup by path and constraint. No Spring context. |
| `src/test/java/com/humanit/challenge/dto/document/DocumentRequestValidationTest.java` | Document create/put validation (see §8.3). |
| `src/test/java/com/humanit/challenge/dto/client/ClientRequestValidationTest.java` | Client create validation, email, lengths, POST documents rules. |
| `src/test/java/com/humanit/challenge/dto/client/ClientPutRequestValidationTest.java` | Client PUT version, documents collection, nested identity and version rules. |
| `src/test/java/com/humanit/challenge/dto/client/ClientRequestBindingTest.java` | Standard Jackson binding (no tri-state) of the `documents` collection for PUT/POST. |
| `src/test/java/com/humanit/challenge/dto/client/ClientResponseShapeTest.java` | Structural test: no recursive DTO graph (`DocumentResponse.clientId` is scalar). |

### 6.3 Archive and report

| Path | Change |
|---|---|
| `agent-reports/T2.2/history/developer-002.md` | **VERIFIED:** Created — unchanged copy of the former primary (`T2.2-EXEC-007-DEV-01`). |
| `agent-reports/T2.2/developer-report.md` | **VERIFIED:** Replaced with this report. |

### 6.4 Not changed (VERIFIED)

No file under `src/main/java/.../domain`, `repository`, `config`, `exception`, `src/main/resources`, `db/migration`, `pom.xml`, the Maven Wrapper, `requirements-decisions.md`, `implementation-plan.md`, `agent-reports/decisions.md`, the Architect or Reviewer reports, `reports/*.md`, or agent definitions was modified by this execution. Pre-existing uncommitted changes (`architect-proposal.md`, `reviewer-report.md`, `decisions.md`, `implementation-plan.md`, `requirements-decisions.md`, and the untracked history archives `architect-002.md`, `architect-003.md`, `developer-001.md`, `reviewer-001.md`, `reviewer-002.md`) were preserved byte-for-byte; this execution did not touch them.

## 7. Commands executed

| # | Command | Purpose | Result |
|---|---|---|---|
| 1 | `git branch --show-current`; `git status --short`; `git diff --stat` | Phase 1 read-only checks | **VERIFIED:** branch `feature/T2.2-dtos-mappers`; six modified doc files + six untracked history archives; no `src/` changes before this execution. |
| 2 | `.\mvnw.cmd -B test` (before any change) | Baseline | **VERIFIED:** `Tests run: 61, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. |
| 3 | `Copy-Item` of developer report to `history/developer-002.md`; `Get-FileHash` on both | Archive before replacement | **VERIFIED:** identical SHA-256. |
| 4 | `.\mvnw.cmd -B test` (after DTO and test creation) | First full run | **FAILED — compilation (defect in my code):** `Valid` import removed while still used in type arguments. **Fixed** by restoring `import jakarta.validation.Valid;`. |
| 5 | `.\mvnw.cmd -B test` (after fix) | Full run | **VERIFIED:** `Tests run: 144, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. |
| 6 | Removed an apparently unused `NotNull` import, then `.\mvnw.cmd -B -q test-compile` | Cleanup | **FAILED — compilation (defect in my cleanup):** `NotNull` was used at `ClientRequestValidationTest` line 204. **Fixed** by restoring the import. |
| 7 | `.\mvnw.cmd -B clean test` (final) | Final verification | **VERIFIED:** `Tests run: 144, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, Maven exit code `0`. |
| 8 | `git status --short`; `git diff --check`; `git diff --stat`; `git diff --stat -- src`; `git check-ignore -q target` | Phase 7 read-only checks | **VERIFIED:** see §8.6. |
| 9 | `Get-ChildItem`/`Select-String` scan of new files for trailing whitespace and tabs | Hygiene | **VERIFIED:** 0 trailing-whitespace matches, 0 tab matches. |

**VERIFIED:** No `git add`, `git commit`, `git push`, `git merge`, `git checkout`, `git reset` or `git restore` was run. No branch was switched. Stack traces printed in the Maven log come from the pre-existing intentional startup-failure tests (`JwtSecretValidationTest` and others); they are not failures.

## 8. Evidence and observed results

### 8.1 Build and test results (final run, command 7)

**VERIFIED:** `Tests run: 144, Failures: 0, Errors: 0, Skipped: 0` — `BUILD SUCCESS`, Maven exit code `0`.

- Baseline (T2.1 and earlier, unchanged): **61** tests passed (command 2).
- New Part 1 DTO tests: **83** tests (144 − 61), all passed. Surefire per-class counts observed in command 5 (the run before the final import fix; the test code is identical in the final run):
  - `ClientPutRequestValidationTest`: 20 (`ScalarAndVersionFields` 4, `DocumentsCollection` 13, `NestedIdentityRuleInIsolation` 3)
  - `ClientRequestValidationTest`: 33 (`RequiredAndBlankFields` 17, `EmailFormat` 6, `MaximumLengths` 4, `NoCountrySpecificFormats` 1, `CreateDocuments` 5)
  - `ClientRequestBindingTest`: 8
  - `ClientResponseShapeTest`: 2
  - `DocumentRequestValidationTest`: 20 (`CreateRequest` 12, `PutRequest` 8)
  - Sum: 20 + 33 + 8 + 2 + 20 = 83, consistent with 144 − 61.

### 8.2 Skipped / failed / deferred tests

- **Failed:** none in the final run. Two transient failures were compilation errors in my own code (commands 4 and 6), both fixed before the final run.
- **Skipped:** 0.
- **Deferred (not written):** tests for PATCH binding and PATCH validation (Part 2); mapper tests (Part 3).

### 8.3 Validation coverage (observed through the passing tests)

- Missing required fields: Client create/put scalars; Document create/put `number`, `expirationDate`; nested Document `number` and `expirationDate` in Client PUT.
- Blank required strings: `firstName`, `lastName`, `taxIdentifier`, `phoneNumber`, `email` (null, empty and whitespace); Document `number` (null, empty, whitespace).
- Invalid email: `not-an-email`, `ana@`, `@example.com`, `ana example.com`.
- Maximum lengths: exact boundary accepted and boundary+1 rejected for Client `firstName`, `lastName`, `taxIdentifier`, `phoneNumber`; Document `number` 100/101 and `description` 1000/1001; Client `email` 255 has no Size violation and 256 has one.
- Negative expected versions: `version` −1 rejected (Client PUT, Document PUT, nested Client PUT); version 0 accepted; missing version rejected.
- Expired Documents: accepted for Document create/put and for nested Client PUT (`2001-01-01`).
- No country-specific format: arbitrary tax identifier and phone strings accepted.
- Client POST: omitted documents, `documents: null` (via setter and via JSON), empty list accepted; supplied invalid Document rejected with path `documents[0].number`; null element rejected.
- Client PUT: omitted (JSON and default) accepted; empty accepted; explicit `null` (JSON and setter) rejected with `@NotNull` on `documents`; existing nested Document (id + version) accepted; new nested Document (neither) accepted; mixed accepted; id-only and version-only rejected (`documents[0].identityPairConsistent`); nested negative version rejected; nested blank number / missing date rejected for both existing and new entries; null nested element rejected.
- Standard Jackson binding: `documents` omitted / `[]` / `null` for PUT; `documents` omitted / `null` for POST; supplied nested entries bind `id`, `version`, `number`, `expirationDate` (ISO `YYYY-MM-DD`).

### 8.4 Response shape (observed)

**VERIFIED (`ClientResponseShapeTest`):** `ClientResponse.documents` has generic type `List<DocumentResponse>`; `DocumentResponse` components contain `clientId:java.lang.Long` and no `ClientResponse` or `domain.Client` component.

### 8.5 Evidence markers

- **VERIFIED:** All statements in §8.1–§8.4 come from the Maven output of commands 2, 5 and 7 and from the source files listed in §6.
- **VERIFIED:** The identity-pair `@AssertTrue` getter (`isIdentityPairConsistent`) is annotated `@JsonIgnore`, so serialization of request objects does not emit a synthetic property.
- **PENDING VERIFICATION:** Behaviour of the PUT `documents` explicit-null rule under any future Jackson configuration change (Part 2 must not introduce `Nulls.SKIP`-style defaults for this property). The current test `putWithExplicitNullDocumentsIsRejected` is the regression guard.
- **NOT AVAILABLE:** Independent Tester or Reviewer verification of Part 1 — no such report exists yet; none was read or relied on.

### 8.6 Final Git state (read-only, command 8)

- **VERIFIED:** `git diff --check` exit code `0`; only CRLF working-copy warnings were emitted (informational, pre-existing condition for tracked documentation files). Note: `git diff --check` does not cover untracked files; the untracked new files were checked separately by the scan in command 9.
- **VERIFIED:** `git diff --stat` (tracked): the same six documentation files and line counts as before this execution (301 insertions, 158 deletions; no `src/` entries).
- **VERIFIED:** `git diff --stat -- src` returns no tracked changes; all Part 1 code is untracked (`?? src/main/java/com/humanit/challenge/dto/`, `?? src/test/java/com/humanit/challenge/dto/`).
- **VERIFIED:** `target/` is git-ignored (`git check-ignore` exit `0`), so build output does not appear in status.

## 9. Findings and severity

| ID | Severity | Finding | Disposition |
|---|---|---|---|
| **F-P1-01** | **Minor (needs confirmation)** | The current Architect proposal (`T2.2-EXEC-005-ARCH-01`, D-13) does **not** define a package structure. The task instructed use of "the package structure defined in the Architect proposal". The only package definition found is in the superseded `history/architect-003.md` (PROPOSED there): `com.humanit.challenge.dto.client`, `com.humanit.challenge.dto.document`, mappers in `com.humanit.challenge.mapper`. I used the `dto.client` / `dto.document` packages from that record. | **Open — Tech Lead / Architect to confirm** the packages. Relocation is a mechanical rename if needed. |
| **F-P1-02** | **Deferred (planned)** | **All PATCH DTOs are deferred to Part 2** (Option B): `ClientPatchRequest`, `ClientDocumentPatchRequest`, `DocumentPatchRequest` were **not** created. Reason: omitted-versus-null semantics for `name`/`description`/`documents` in PATCH cannot be expressed correctly with plain nullable fields, and no `PatchValue<T>` declaration was introduced (no substitute, no partial PATCH type). | Part 2 creates the PATCH DTOs with the approved tri-state wrapper. |
| **F-P1-03** | **Info (design note for reviewers)** | Client PUT `documents: null` rejection relies on (a) a field initialized to an empty list, so an **omitted** property keeps the empty default (preserve), and (b) standard Jackson calling the setter with `null` for an explicit JSON null, which `@NotNull` rejects. This is not the PATCH tri-state and does not use `Optional`/`JsonNullable`/`JsonNode`, but it depends on default Jackson setter/field behaviour. | Guarded by `putWithExplicitNullDocumentsIsRejected` and `putWithOmittedDocumentsPropertyIsValidAndKeepsEmptyDefault`. **Part 2 must preserve it.** |
| **F-P1-04** | **Info** | Client POST `documents: null` is normalized to an empty list in the setter (POST only). PUT deliberately keeps `null` so it can be rejected. | Intended; covered by tests. |
| **F-P1-05** | **Info (boundary, by design)** | Nested Document IDs are not positivity-checked and unknown IDs are not detected in DTO validation. Ownership, unknown-ID, version comparison and aggregate merge belong to later service use cases (D-11/D-13). | Not a Part 1 gap. Later service tests must cover these. |
| **F-P1-06** | **Info** | Spring Boot defaults do not fail on unknown JSON properties. Unexpected fields (for example `id` inside a POST nested Document) are therefore silently ignored at the binding level. Not changed in Part 1. | Track for the REST layer (T2.3/T2.4) or Part 3 review. |
| **F-P1-07** | **Info** | `@Email` (Hibernate) is used together with `@NotBlank` and `@Size(max = 255)`. The 255-character boundary test checks only that no `Size` violation occurs; it does not assert full email validity for that long local part. | Acceptable; limits are checked as approved. |
| **F-P1-08** | **Minor (process, transparent)** | Two of my own compilation mistakes occurred during the session (missing `Valid` import; then an incorrect "unused" `NotNull` removal). Both were fixed and the final run is clean. No defect remains in the delivered code. | Closed. Recorded for transparency. |
| **RF2-1 (carried)** | Minor, non-blocking | Project-level tracking artifacts (`reports/evidence-gap-register.md` G-07, `reports/task-status-report.md`) still describe a stale status. **Not touched** by this execution (outside Part 1 scope). | Tech Lead disposition, as stated by the Reviewer. |

**Critical:** none identified. **Major:** none identified.

## 10. Decisions and reasoning

1. **Request/response types.** Requests are mutable classes with a no-arg constructor and getters/setters (required for Jackson and for Bean Validation getters). Responses are immutable Java records, which fit read-only output and avoid setter-based mutation.
2. **Separate create / PUT / nested types.** `DocumentCreateRequest` (no id/version), `DocumentPutRequest` (expected version), `ClientDocumentPutRequest` (optional id/version pair), `ClientCreateRequest`, `ClientPutRequest`. This follows D-13 "separate DTO types" and avoids validation groups.
3. **Nested Client PUT identity rule.** Implemented as `@AssertTrue isIdentityPairConsistent()`: valid when both `id` and `version` are null or both are non-null. Chosen over a custom class-level constraint for simplicity; the cascade reports the path `documents[i].identityPairConsistent`. No DB, repository or ownership check is performed (D-11/D-13).
4. **Expected version.** `@NotNull @PositiveOrZero` on every expected version field (Client PUT, Document PUT, nested existing Document). Version 0 is accepted because JPA versions start at 0 (T2.1 schema default `0`).
5. **No invented rules.** No positivity rule on IDs, no date-in-future rule, no format validation beyond `@Email` (D-13 forbids country-specific formats; expired Documents must be accepted).
6. **Client POST null normalization** (setter) versus **Client PUT null rejection** (field default plus `@NotNull`) — this is the minimal way to satisfy the two opposite D-13 contracts without any tri-state infrastructure. See F-P1-03 for the dependency.
7. **Alternatives considered and rejected:** `Optional<List<…>>` (prohibited for the PUT/PATCH tri-state substitute by the task), `JsonNullable` (excluded by D-13), a single generic nested DTO with validation groups (rejected by D-13 separate-DTO intent and by the Architect proposal), and placing constraints on entities (prohibited by D-11 and D-13).
8. **No mappers, services or controllers** were created (Part 3 / later tasks).

## 11. Final status

**PART 1 — COMPLETE WITHIN ITS SCOPE, SUBJECT TO VERIFICATION AND HUMAN ACCEPTANCE.**

- Implemented: 7 Part 1 DTO types (5 request classes, 2 response records; see §12 for the exact list) — see §6.1 and §12.
- Build and tests: `mvn` (via `.\mvnw.cmd`) — **144 / 0 / 0 / 0, BUILD SUCCESS** (VERIFIED).
- PATCH DTOs: **deferred to Part 2** by design (F-P1-02).
- **Not claimed:** full T2.2 completion, T2.2 acceptance, Tech Lead approval of any phase gate, Tester verification, Reviewer audit.

## 12. Outstanding issues

1. **F-P1-01 (Minor, needs decision):** Confirm the DTO package names (`com.humanit.challenge.dto.client`, `com.humanit.challenge.dto.document`) and the mapper package (`com.humanit.challenge.mapper`, Part 3) because the current Architect proposal does not state them.
2. **F-P1-02 (planned):** PATCH DTOs not created — `ClientPatchRequest`, `ClientDocumentPatchRequest`, `DocumentPatchRequest`. Part 2.
3. **F-P1-03 (must preserve):** The Client PUT `documents` null-rejection relies on default Jackson behavior; Part 2 must not alter it.
4. **F-P1-06 (tracking):** Unknown JSON properties are ignored by default at binding; decide at the REST layer.
5. **RF2-1 (carried, Minor):** project-level tracking artifacts still record stale status; not touched.
6. **Not yet performed:** Tester independent verification and Reviewer independent audit of Part 1 (NOT AVAILABLE; to be performed by those roles, in order, without reading this report first).
7. **Git:** nothing committed, staged or pushed. All Part 1 files are untracked and await the Part 3 consolidated commit under explicit authorization.

### 12.1 Exact implemented type list

| Type | Kind | Package |
|---|---|---|
| `ClientCreateRequest` | class (request) | `dto.client` |
| `ClientPutRequest` | class (request) | `dto.client` |
| `ClientDocumentPutRequest` | class (nested request) | `dto.client` |
| `ClientResponse` | record (response) | `dto.client` |
| `DocumentCreateRequest` | class (request) | `dto.document` |
| `DocumentPutRequest` | class (request) | `dto.document` |
| `DocumentResponse` | record (response) | `dto.document` |

**Not created in Part 1 (deferred):** `ClientPatchRequest`, `ClientDocumentPatchRequest`, `DocumentPatchRequest`, `PatchValue<T>` and its Jackson binding.

## 13. Handoff to next agent

**Handoff to Part 2 (Jackson tri-state PATCH binding):**

- Create the project-owned tri-state type (`ABSENT`, `PRESENT_NULL`, `PRESENT_VALUE`) with Jackson binding and no external JSON-nullable dependency, with binding tests for all three states.
- Create `ClientPatchRequest`, `ClientDocumentPatchRequest` and `DocumentPatchRequest` on top of it. Required fields reject `PRESENT_NULL`; `description` `PRESENT_NULL` clears; Client PATCH `documents` `PRESENT_NULL` is invalid; omitted or empty `documents` preserves; nested existing/new identity rules as in the PUT pair rule.
- Keep the Part 1 behavior: Client PUT `documents: null` must still be rejected and Client POST `documents: null` must still be accepted (tests `putWithExplicitNullDocumentsIsRejected`, `createWithExplicitNullDocumentsIsTreatedAsOmitted`). Do not add `Nulls.SKIP`-style configuration that would change that.
- Resolve F-P1-01 (packages) before adding PATCH types.

**Handoff to Tester (independent, before reading this report):** verify the 144-test suite and the Part 1 validation rules in §8.3 directly from the source; the Tester has not been given this report's conclusions.

**Handoff to Reviewer (independent, before reading this report or the Tester report):** audit `src/main/java/com/humanit/challenge/dto/**` for the rules in D-13 and the nested identity and POST/PUT null semantics.

**Handoff to Part 3:** dedicated mappers (`com.humanit.challenge.mapper`, pending F-P1-01), remaining tests, regression verification and one consolidated Git commit/push — only under separate explicit authorization.

---
**Report status:** Written for `T2.2-EXEC-009-DEV-01` (Part 1). Previous primary archived unchanged at `agent-reports/T2.2/history/developer-002.md`; earlier history at `agent-reports/T2.2/history/developer-001.md`. Current primary: `agent-reports/T2.2/developer-report.md`.
**Outstanding findings:** F-P1-01 (Minor, package confirmation), F-P1-02 (deferred PATCH, planned), F-P1-03 (preserve PUT null behavior), F-P1-05/06/07 (Info), RF2-1 (Minor, carried, not touched).
