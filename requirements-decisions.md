# Human IT Challenge — Requirements Decisions



**Status:** Requirements decisions and the implementation plan are approved by the Tech Lead. Implementation through T6.3 is complete: phases 1–6 (T1.1–T6.3) are implemented, independently verified and audited. T6.3 final release was accepted by the Human Tech Lead on 2026-11-13 (`agent-reports/decisions.md` D-15), with Major process findings T63-REV-01 and T63-REV-02 waived under D-14. Earlier "pending" status statements in this document are historical and superseded by the agent-reports record.

**Purpose:** Document the functional, technical and quality decisions agreed during requirements analysis.



## 1. Scope and Objectives



Build a Java and Spring Boot REST API for managing Clients and their associated Documents.



The solution must prioritize maintainability, correctness, transactional integrity, security, automated testing and clear documentation.



The original requirements are defined in `requirements.md`, and additional evaluation criteria are defined in `evaluation-notes.md`.



This document records the decisions made to resolve ambiguities and extend the original requirements.



## 2. Domain Model



### Client



Required fields:



- `id`: Long, database-generated

- `firstName`: String, required

- `lastName`: String, required

- `taxIdentifier`: String, required, globally unique

- `email`: String, required, valid email format

- `phoneNumber`: String, required

- `documents`: Collection of Documents, optional

- `version`: Optimistic locking version



### Document



Required fields:



- `id`: Long, database-generated

- `number`: String, required, globally unique

- `description`: String, optional

- `expirationDate`: LocalDate, required

- `client`: Owning Client, required

- `version`: Optimistic locking version



### Relationship



- One Client can have zero or more Documents.

- Each Document belongs to exactly one Client.

- Documents cannot be transferred between Clients.

- Deleting a Client also deletes its Documents.

- The deletion must be atomic.



## 3. REST API Endpoints



### Clients



| Method | Endpoint | Purpose |

|---|---|---|

| POST | `/api/clients` | Create Client, optionally with Documents |

| GET | `/api/clients` | List Clients with pagination |

| GET | `/api/clients/{id}` | Retrieve Client |

| PUT | `/api/clients/{id}` | Replace editable Client fields |

| PATCH | `/api/clients/{id}` | Partially update Client |

| DELETE | `/api/clients/{id}` | Delete Client and associated Documents |



### Documents



| Method | Endpoint | Purpose |

|---|---|---|

| POST | `/api/clients/{clientId}/documents` | Create Document |

| GET | `/api/clients/{clientId}/documents` | List Documents with pagination |

| GET | `/api/clients/{clientId}/documents/{documentId}` | Retrieve Document |

| PUT | `/api/clients/{clientId}/documents/{documentId}` | Replace editable Document fields |

| PATCH | `/api/clients/{clientId}/documents/{documentId}` | Partially update Document |

| DELETE | `/api/clients/{clientId}/documents/{documentId}` | Delete Document |



### Authentication



| Method | Endpoint | Purpose |

|---|---|---|

| POST | `/auth/login` | Authenticate and obtain JWT |



All Client and Document endpoints require valid JWT authentication.



## 4. Update Semantics



### PUT



PUT replaces the editable scalar fields of the targeted resource.



For Client updates:



- Omitted `documents` collection: preserve existing Documents.

- Empty `documents` collection: preserve existing Documents.

- Document with an existing ID: update that Document.

- Document without an ID: create a new Document.

- Existing Document omitted from the supplied collection: preserve it.

- Unknown Document ID: reject the request.

- Document belonging to another Client: reject the request with HTTP 409.

- Document deletion must be explicit through DELETE.



For Document updates:



- PUT replaces all editable Document fields.

- Required editable fields must be provided.

- Document ownership cannot be changed.

- The existing Document ID must be preserved.



### PATCH



PATCH modifies only the fields explicitly supplied in the request.



- Omitted fields remain unchanged.

- Existing entity identities are preserved.

- Validation and uniqueness rules still apply.

- PATCH cannot transfer Documents between Clients.

- PATCH must not implicitly delete Documents.

### Client PATCH with Nested Documents

- Existing nested Documents require `id` and the expected `version`.
- Existing nested Documents use partial update semantics: omitted fields remain unchanged.
- New nested Documents have no `id` or `version` and require `number` and `expirationDate`.
- Omitted Documents remain unchanged.
- An empty `documents` array preserves existing Documents.
- `documents: null` returns HTTP 400.
- Client PATCH requires the expected Client `version`.
- Every changed Document increments its own version and the parent Client `version` according to the approved optimistic-locking contract.
- All changes are atomic, with full rollback on failure.
- No implicit Document deletion or ownership transfer is allowed.



**Important:** Client PUT uses explicitly documented collection-preserving semantics rather than unconditional replacement of the entire aggregate.



### Consistency


Updating a Document through the Client endpoint or its dedicated Document endpoint must enforce equivalent validation, uniqueness, identity and concurrency rules.


Accessing a Document through a nested endpoint under a Client ID that does not own it returns HTTP 404. Attempting to associate an existing Document owned by another Client during a Client aggregate update returns HTTP 409.




## 5. Deletion Rules



- Deleting a Document removes only that Document.

- Deleting a Client removes the Client and all associated Documents.

- No orphan Documents are permitted.

- Deletion operations must be transactional.

- Documents must never be deleted implicitly because they were omitted from an update request.



## 6. Validation and Integrity



### Client Validation



- `firstName`: required, nonblank

- `lastName`: required, nonblank

- `taxIdentifier`: required, nonblank, globally unique

- `email`: required, valid email format

- `phoneNumber`: required, nonblank

- `documents`: optional



No country-specific tax identifier or phone number format is required.



### Document Validation



- `number`: required, nonblank, globally unique

- `description`: optional

- `expirationDate`: required, ISO 8601 date (`YYYY-MM-DD`)

- Expired Documents are accepted.



### Database Integrity



- Enforce uniqueness through database constraints.

- Enforce required fields and foreign-key relationships.

- Validate business rules in the application layer.

- Return HTTP 409 Conflict for uniqueness violations.



## 7. Transaction Management



Client and Document persistence must be atomic.



Transactions must cover:



- Creating a Client with Documents.

- Updating a Client and its Documents.

- Deleting a Client and its Documents.

- Individual Document modifications where appropriate.



If any operation within a transaction fails, all changes in that transaction must be rolled back.



Use Spring transaction management, with transaction boundaries defined at the Service layer.



## 8. Concurrency Control



Use JPA optimistic locking through `@Version` on Client and Document entities.



Requirements:


- POST Client requires no version.

- POST Document requires the `X-Client-Version` header.

- PUT/PATCH Client require the expected Client `version` in the request DTO.

- PUT/PATCH Document require the expected Document `version` in the request DTO plus the `X-Client-Version` header.

- PUT/PATCH Client with nested Documents require the expected Client `version` and the expected `version` for every existing nested Document being updated; new nested Documents have no version.

- DELETE Client requires the `X-Client-Version` header.

- DELETE Document requires both the `X-Client-Version` and `X-Document-Version` headers.

- Missing, malformed or invalid required versions return HTTP 400.

- Stale versions return HTTP 409 with RFC 9457 Problem Details.

- Conflicts roll back the entire transaction.

- Every Document create, update or delete increments the parent Client `version` using JPA `OPTIMISTIC_FORCE_INCREMENT` within the same transaction.

- Conflicts must not result in partial persistence.

- Updates through different endpoints must follow equivalent concurrency rules.

- Aggregate updates must account for concurrent changes to associated Documents.




The approved concurrency contract is defined in Section 20. It requires JPA `@Version` on both Client and Document entities and `OPTIMISTIC_FORCE_INCREMENT` on the parent Client for every Document create, update or delete, within the same transaction. Update requests must supply the expected Client and Document versions; stale versions return HTTP 409 with full transaction rollback.



## 9. Pagination



Client and Document list endpoints must support pagination.



Default parameters:



- `page=0`

- `size=20`



Requirements:



- Zero-based pagination.

- Include pagination metadata in responses.

- Maximum allowed page size is 100; requests exceeding it return HTTP 400.

- Use deterministic ordering by resource ID in ascending order.

- Document pagination behavior in OpenAPI.



The maximum page size is 100 and sorting is by ID in ascending order.



## 10. Authentication and Security



Implement Spring Security with JWT authentication.



### Login



`POST /auth/login`



- Accept username and password.

- Validate credentials.

- Return a signed JWT on successful authentication.

- Return HTTP 401 for invalid credentials.



### Development User



- Automatically provision one demonstration user in the development environment.

- Credentials must be configurable.

- Passwords must be stored using a secure hash, such as BCrypt.

- No public user registration API is required.



### Security Requirements



- Protect Client and Document endpoints using JWT.

- Do not hardcode secrets or production credentials.

- Externalize sensitive configuration.

- Separate development and production settings.

- Do not automatically provision demonstration credentials in production.

- Use appropriate HTTP 401 and 403 responses.



## 11. Error Handling



Use RFC 9457 Problem Details (`application/problem+json`).



Implement centralized exception handling using `@RestControllerAdvice`.



Handle at least:



| HTTP Status | Situation |

|---|---|

| 400 | Invalid request or validation failure |

| 401 | Missing or invalid authentication |

| 403 | Authenticated but forbidden |

| 404 | Client or Document not found |

| 409 | Uniqueness or optimistic locking conflict |

| 500 | Unexpected server error |



Requirements:



- Consistent error response format.

- Structured field validation errors.

- Clear, meaningful error messages.

- No exposure of stack traces, secrets or sensitive internal details.



## 12. Persistence and Environments



### Development and Testing



- H2 in-memory database.

- Spring profile: `dev`.

- Flyway migrations.

- Hibernate schema validation.



### Production



- PostgreSQL.

- Spring profile: `prod`.

- Flyway migrations.

- Hibernate schema validation.

- Externalized connection credentials.



### Database Migration



- Use Flyway for versioned schema migrations.

- Do not rely on Hibernate to generate production tables.

- Use `ddl-auto: validate`.

- Ensure migration compatibility with H2 and PostgreSQL, using database-specific migrations where necessary.



## 13. Docker



Provide:



- `Dockerfile` for the Spring Boot application.

- `compose.yaml` for the application and PostgreSQL.

- Appropriate environment-variable configuration.

- Startup instructions in README.



The project must also support local execution using Java and Maven without Docker.



## 14. API Documentation



Use Swagger/OpenAPI through `springdoc-openapi`.



Approach: **code-first**.



Document:



- All endpoints.

- Request and response DTOs.

- HTTP status codes.

- Validation rules.

- JWT authentication.

- Pagination.

- PUT and PATCH semantics.

- Document ownership and deletion rules.

- Optimistic locking conflicts.

- RFC 9457 error responses.



## 15. Testing Strategy



Use:



- JUnit 5

- Mockito

- MockMvc

- Spring Boot Test

- H2 integration tests



### Test Layers



- Controller unit tests use mocked dependencies to verify endpoint happy paths and request/response mapping.

- Integration tests verify persistence, transactions, concurrency, authentication and database constraints.



### Minimum Coverage



At least one successful automated endpoint test for every implemented API endpoint.



### Additional Required Scenarios



- Request validation failures.

- Resource not found.

- Duplicate TaxIdentifier.

- Duplicate Document Number.

- Client and Document transaction rollback.

- Client deletion cascade.

- Document identity preservation.

- Document collection omission during updates.

- Empty Document collection during updates.

- Document ownership violations.

- JWT authentication success and failure.

- Protected endpoint access.

- Optimistic locking conflicts.

- Pagination behavior.

- Consistent Problem Details responses.



Tests must be independent, deterministic and executable through the build tool.



## 16. Technical Baseline



The implementation will use:



- Java 21

- Spring Boot 3.x

- Maven with Maven Wrapper

- Spring Web

- Spring Data JPA

- Bean Validation

- Spring Security

- JWT

- H2

- PostgreSQL

- Flyway

- springdoc-openapi

- JUnit 5, Mockito and MockMvc

- Docker and Docker Compose



Exact dependency versions must be selected and verified during architecture design.



## 17. Documentation and Deliverables



Provide:



- Complete source code.

- Maven Wrapper and build configuration.

- Database migration scripts.

- Development and production configuration.

- Dockerfile and Docker Compose configuration.

- Automated tests.

- Swagger/OpenAPI documentation.

- README with setup and execution instructions.



### README Must Explain



- Project purpose.

- Technology choices.

- How to build and run locally.

- How to run with Docker.

- How to execute tests.

- How to authenticate.

- How to access Swagger UI.

- Important design decisions.

- Assumptions and limitations.

- PUT versus PATCH semantics.

- Transaction and concurrency strategies.



## 18. Development Workflow



Development will proceed incrementally using four OpenCode agents.



| Agent | Responsibility |

|---|---|

| Architect | Requirements interpretation, architecture and technical design |

| Developer | Implementation |

| Tester | Automated testing and verification |

| Reviewer | Independent code review |



Workflow:



1\. Architect proposes a design for the current phase.

2\. Tech Lead reviews and approves it.

3\. Developer implements only the approved scope.

4\. Tester verifies the implementation.

5\. Reviewer identifies defects and improvements.

6\. Tech Lead approves completion before the next phase.



Agents must not implement unapproved features or make unrelated changes.



The accepted development tooling for this project is OpenCode with Ollama and Beam Cloud. This is a development tooling choice and does not affect the API requirements.



## 19. Items for Architecture Design



The decisions in Section 20 and all other previously approved requirements are already approved and must be followed; they are not open for redesign here. The items below are technical details that still require implementation-level design by the Architect, followed by Tech Lead approval. These are design tasks, not authorization to implement code.



The Architect must propose and justify:



- Exact Spring Boot and dependency versions.

- Package structure and separation of responsibilities.

- DTO design and entity mapping.

- Request/response contracts.

- How Client PUT handles nested Document updates.

- How PATCH distinguishes omitted fields from explicit null values.

- Optimistic locking behavior across the Client aggregate.

- JWT signing, expiry and validation strategy.

- Error code taxonomy.

- Pagination limits and sorting.

- Database migration compatibility.

- Test organization and execution.

- Implementation phases and acceptance criteria.



These are design tasks, not authorization to implement code.







## 20. Approved API and Concurrency Decisions

- Client and Document use JPA @Version.
- POST Client requires no version.
- POST Document requires the X-Client-Version header.
- PUT/PATCH Client require the expected Client version in the request DTO.
- PUT/PATCH Document require the expected Document version in the request DTO plus the X-Client-Version header.
- PUT/PATCH Client with nested Documents require the expected Client version and the expected version for every existing nested Document being updated. New nested Documents have no version.
- Client PATCH with nested Documents requires id and expected version for existing nested Documents; new nested Documents have no id or version and require number and expirationDate.
- Client PATCH nested Documents use partial update semantics; omitted fields remain unchanged.
- Omitted Documents remain unchanged on Client PATCH; an empty documents array preserves existing Documents; documents:null returns HTTP 400.
- No implicit Document deletion or ownership transfer is allowed on Client PATCH.
- Every changed nested Document in a Client PATCH increments its own version and the parent Client version according to the approved optimistic-locking contract.
- DELETE Client requires the X-Client-Version header.
- DELETE Document requires both the X-Client-Version and X-Document-Version headers.
- Document mutations increment the parent Client version using OPTIMISTIC_FORCE_INCREMENT in the same transaction.
- Updates validate expected Client and Document versions.
- Missing, malformed or invalid required versions return HTTP 400.
- Stale versions return HTTP 409 Problem Details with full transaction rollback.
- Conflicts roll back the entire transaction.
- Deletion and concurrency checks must be atomic.
- PATCH distinguishes omitted fields from explicit null.
- Required fields reject null.
- description:null clears the value.
- documents:null is invalid.
- Omitted or empty documents collections preserve existing Documents.
- Unknown nested Document IDs are rejected.
- Accessing a Document through a nested endpoint under a Client ID that does not own it returns HTTP 404.
- Attempting to associate an existing Document owned by another Client during a Client aggregate update returns HTTP 409.
- Foreign Client Document IDs return 409.
- Separate create/update/patch/response DTOs.
- Pagination maximum size 100 and ascending ID ordering.

---

**Status:** Requirements decisions and the implementation plan are approved by the Tech Lead. Implementation through T6.3 is complete: phases 1–6 (T1.1–T6.3) are implemented, independently verified and audited. T6.3 final release was accepted by the Human Tech Lead on 2026-11-13 (`agent-reports/decisions.md` D-15), with Major process findings T63-REV-01 and T63-REV-02 waived under D-14. Earlier "pending" status statements in this document are historical and superseded by the agent-reports record.

