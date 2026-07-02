# Tablero Specification

> Delta from `add-crm-ai-assistant-spring-ai`. On archive, the requirements below merge into the main `openspec/specs/tablero/spec.md`.

## Purpose

Allow the AI assistant to read Tableros, Fichas, and Columnas through dedicated tools while forbidding direct mutation. The only AI-mediated mutation of tablero state is `MoverColumnaFicha`, and it MUST run only after explicit human confirmation.

## ADDED Requirements

### Requirement: AI Read Tools Over Tableros, Fichas, Columnas

The system MUST expose `BuscarTableroTool`, `BuscarFichaTool`, and `BuscarColumnaTool` to the AI assistant. Each tool MUST scope results to the authenticated actor's authorized company context, and for resource lookups the addressed resource's owning company MUST be authoritative.

#### Scenario: AI reads tablero from an authorized company

- GIVEN an authenticated actor authorized for company `E`
- WHEN the AI assistant invokes `BuscarTableroTool`
- THEN the system MUST return only tableros owned by `E`

#### Scenario: AI read tool is read-only

- GIVEN the AI assistant's tool registry
- WHEN the registry is built
- THEN the registry MUST NOT include any `@Tool` that mutates tablero, ficha, or columna state

### Requirement: Column Moves Require Human Confirmation

The system MUST execute `MoverColumnaFicha` only after an authenticated user confirms an `AiAccion` staged for that move. Direct invocation by the AI MUST NOT be exposed.

#### Scenario: Confirmed action moves the ficha

- GIVEN an `AiAccion` of type `MOVER_COLUMNA_FICHA` in `estado = PENDING`
- WHEN the user confirms the action via REST
- THEN `ConfirmarAccionUseCase` MUST invoke `MoverColumnaFichaUseCase`
- AND MUST persist `EXECUTED` when the move succeeds

#### Scenario: Unconfirmed action never moves the ficha

- GIVEN an `AiAccion` of type `MOVER_COLUMNA_FICHA` in `estado = PENDING`
- WHEN no confirmation request has been received
- THEN the ficha MUST remain in its current column
- AND the action MUST stay `PENDING` until expiry or explicit rejection

### Requirement: Tenant Match on AI-Mediated Ficha Moves

The system MUST verify that the ficha, the source column, and the destination column share the same tenant, and that the confirmer is authorized for that tenant, before dispatching `MoverColumnaFicha`.

#### Scenario: Cross-tenant ficha move rejected

- GIVEN a ficha owned by `empresaId = E1`
- WHEN the AI assistant proposes a move and a user not authorized for `E1` confirms
- THEN the system MUST reject the confirmation
- AND MUST NOT move the ficha
