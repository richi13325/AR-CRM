# Tablero Specification

## Purpose

Defines the requirements for accessing and listing tableros, ensuring proper authorization rules are enforced for authenticated users.

## Requirements

### Requirement: Authenticated Access to Tableros List

The system MUST allow any authenticated user to retrieve the list of tableros.

#### Scenario: Authenticated user requests tableros list

- GIVEN an authenticated user with a valid JWT
- WHEN the user sends a `GET` request to `/api/tableros/get-all`
- THEN the system MUST return a 200 OK status
- AND the system MUST return the list of tableros

#### Scenario: Unauthenticated user requests tableros list

- GIVEN an unauthenticated user without a valid JWT
- WHEN the user sends a `GET` request to `/api/tableros/get-all`
- THEN the system MUST return a 401 Unauthorized status
