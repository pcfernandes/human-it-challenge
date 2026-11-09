Objective

Develop a RESTful API using Java + Spring Framework to manage Clients and their associated Documents, implementing full CRUD operations.

The solution must be developed using the following stack: OpenCode → Ollama → Qwen3, with agents used throughout the implementation process.



Requirements

Create a Java + Spring API that supports CRUD (Create, Read, Update, Delete) for Clients.

Client Entity must include the following fields:

Id

FirstName

LastName

Tax Identifier

Email

PhoneNumber

Documents (one-to-many relationship)

Document Entity must include:

Id

Number

Description

Expiration Date

Database:

Use an H2 in-memory database (you can use other)

Ensure Clients and Documents are persisted in a single transaction (atomicity).

The database schema should be created automatically, or at least provide a script to create it (Liquibase or create in your application.yaml)

Build Tool:

You may use Gradle or Maven to build and manage the project.

Testing:

Implement at least one unit test (happy path) for each API endpoint.

APIs:

Implement POST, GET, PUT, DELETE endpoints for Clients.

Additionally, create a Login API (for JWT purposes if implemented).

Nice to Have (Optional Enhancements)

Document the APIs using Swagger.

Use Docker to containerize and run the service.

Add JWT Authentication:

Implement a sign-in API to obtain a JWT token.

Secure the other APIs with this token.

Deliverables

Source code for the API.

Instructions to run the project.

Tests for each API (unit-tested, at least the happy path).

Any additional scripts or configuration files (e.g., Dockerfile, database scripts).



