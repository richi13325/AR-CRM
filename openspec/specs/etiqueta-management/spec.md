# Etiqueta Management Specification

> Delta from `add-ficha-etiquetas`. On archive, the requirements below merge into
> the main `openspec/specs/etiqueta-management/spec.md`.

## Purpose

Define the rules, validations, and operations for the global Etiqueta catalog.

## Requirements

### Requirement: Etiqueta Structure

The system MUST support a global Etiqueta entity with `EtiquetaId`, `nombre`, `tipoEtiqueta`, and `color`.

#### Scenario: Valid Etiqueta
- GIVEN a valid name, type (TAREA or TRATO), and color format
- WHEN the Etiqueta is created
- THEN the system stores the Etiqueta with a generated ID

### Requirement: Name Uniqueness Per Type

The system MUST enforce that the `nombre` of an Etiqueta is unique within its `tipoEtiqueta`.

#### Scenario: Unique Name
- GIVEN an existing Etiqueta "Urgent" of type TAREA
- WHEN a user creates a new Etiqueta "Urgent" of type TRATO
- THEN the system allows the creation

#### Scenario: Duplicate Name
- GIVEN an existing Etiqueta "Urgent" of type TAREA
- WHEN a user creates a new Etiqueta "Urgent" of type TAREA
- THEN the system rejects the creation with a validation error

### Requirement: Global Catalog Operations

The system MUST allow CRUD operations and listing/filtering by `tipoEtiqueta` on the global catalog.

#### Scenario: Filter by Type
- GIVEN the catalog contains TAREA and TRATO Etiquetas
- WHEN a user requests a list filtered by type TAREA
- THEN the system returns only TAREA Etiquetas

#### Scenario: Edit Etiqueta Updates Data Universally
- GIVEN an Etiqueta associated with multiple Fichas
- WHEN a user updates the Etiqueta's name or color
- THEN the updated data is reflected universally everywhere it is referenced

### Requirement: Deletion with Confirmation

The system MUST require explicit confirmation to delete an Etiqueta that is in use.

#### Scenario: Delete In-Use Etiqueta
- GIVEN an Etiqueta associated with one or more Fichas
- WHEN a user attempts to delete the Etiqueta without confirmation
- THEN the system rejects the request
- WHEN a user confirms the deletion
- THEN the system deletes the Etiqueta and all related FichaEtiqueta relations

### Requirement: Id-aware Uniqueness on Edit

The system MUST allow an Etiqueta to keep its own name when it is edited, even if
another Etiqueta in the same `tipoEtiqueta` already has that name. Uniqueness MUST
be evaluated excluding the Etiqueta being edited.

#### Scenario: Edit keeps own name
- GIVEN an Etiqueta "Urgent" of type TAREA with id `E`
- WHEN the user edits the Etiqueta to set its name to "Urgent" (no rename)
- THEN the system accepts the edit (the existing row's own name is not a conflict)

#### Scenario: Edit rejects a name collision with another Etiqueta
- GIVEN an Etiqueta "Urgent" of type TAREA with id `E1`
- AND another Etiqueta "Critical" of type TAREA with id `E2`
- WHEN the user edits `E2` to rename it to "Urgent"
- THEN the system rejects the edit with a validation error

### Requirement: Catalog Source of Truth

The Etiqueta catalog row MUST be the single source of truth for `nombre`,
`tipoEtiqueta`, and `color`. Ficha responses MUST NOT duplicate these fields;
consumers resolve the full Etiqueta from the catalog when they need name or color.

#### Scenario: Edit reflects universally on Ficha responses
- GIVEN an Etiqueta E associated with several Fichas
- WHEN the user renames E to "VIP"
- THEN Ficha responses that include E continue to expose E's id (which is stable)
- AND any consumer that resolves E by id from the catalog sees the new name "VIP"
- AND Ficha responses do NOT carry a stale copy of the old name
