# Ficha Etiqueta Specification

> Delta from `add-ficha-etiquetas`. On archive, the requirements below merge into
> the main `openspec/specs/ficha-etiqueta/spec.md`.

## Purpose

Define the association rules, type consistency, and constraints between a Ficha and its assigned Etiquetas.

## Requirements

### Requirement: Ficha Etiqueta Association

A Ficha MAY own zero or more `FichaEtiqueta` relations.

#### Scenario: Empty Tags
- GIVEN a new or existing Ficha
- WHEN the Ficha is saved without any tags
- THEN the system saves the Ficha successfully

### Requirement: Unique Tags Per Ficha (rejected, not deduplicated)

A Ficha MUST NOT contain the same Etiqueta twice. The system MUST reject the
save with a validation error rather than silently deduplicating, so the caller
is forced to make the intent explicit.

#### Scenario: Duplicate Tag Assignment
- GIVEN a Ficha with Etiqueta A
- WHEN a user attempts to save the Ficha with Etiqueta A and Etiqueta A again
- THEN the system rejects the save with a validation error listing the duplicated EtiquetaId

### Requirement: Strict Type Consistency

A Ficha MUST only be associated with Etiquetas of its own type.

#### Scenario: Valid Type Assignment
- GIVEN a Ficha of type TAREA and an Etiqueta of type TAREA
- WHEN the user assigns the Etiqueta to the Ficha
- THEN the system accepts the assignment

#### Scenario: Invalid Type Assignment
- GIVEN a Ficha of type TRATO and an Etiqueta of type TAREA
- WHEN the user assigns the Etiqueta to the Ficha
- THEN the system rejects the assignment with a validation error

### Requirement: Transactional Cascade Deletion

The system MUST transactionally remove relation rows when an Etiqueta is deleted.

#### Scenario: Cascade Delete
- GIVEN an Etiqueta associated with a Ficha
- WHEN the Etiqueta is successfully deleted from the catalog
- THEN the system transactionally removes the FichaEtiqueta relation from the Ficha

### Requirement: Strict Resolution of Etiqueta Ids

When a Ficha create or edit request references `etiquetaIds`, the system MUST
resolve every requested id through the catalog and MUST reject the request if
any id is unknown. The system MUST NOT silently drop unknown ids.

#### Scenario: All ids resolve
- GIVEN a Ficha of type TAREA and three existing Etiqueta ids of type TAREA
- WHEN the user creates the Ficha with those three `etiquetaIds`
- THEN the system saves the Ficha with the three relations

#### Scenario: One id is unknown
- GIVEN a Ficha of type TAREA and two existing Etiqueta ids of type TAREA
- AND a third id that does not exist in the catalog
- WHEN the user creates the Ficha with those three `etiquetaIds`
- THEN the system rejects the request with a 404 listing the unknown id(s)
