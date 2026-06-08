# Proposal: Add Ficha Etiquetas

## Intent

Introduce a global `Etiqueta` (Tag) catalog in the CRM to allow categorizing and organizing `Ficha` entities (Tareas and Tratos). This enables better filtering, visual organization, and structured metadata for Fichas without scoping tags to individual owners.

## Scope

### In Scope
- Creation of the `Etiqueta` rich domain entity (fields: `EtiquetaId`, `nombre`, `tipoEtiqueta`, `color`).
- Enforcing validation: required fields, valid color formats, and unique `nombre` per `tipoEtiqueta`.
- `Etiqueta` CRUD operations and list/filter by `tipoEtiqueta`.
- Modifying `Ficha` entity to include a `List<FichaEtiqueta>` (zero or more tags, no duplicates).
- Enforcing strict type consistency (e.g., TAREA Fichas can only have TAREA Etiquetas).
- Updating Etiqueta reflects universally across all associated Fichas.
- Transactional cascade deletion: deleting an Etiqueta in use requires user confirmation and removes all related `FichaEtiqueta` records.

### Out of Scope
- A separate manual assign/unassign API for Ficha Etiquetas (tags will be managed through the Ficha's aggregate root updates for now).
- Owner-scoped or user-specific tags (tags are global).

## Capabilities

> This section is the CONTRACT between proposal and specs phases.
> The sdd-spec agent reads this to know exactly which spec files to create or update.

### New Capabilities
- `etiqueta-management`: CRUD operations, validations, and listing/filtering of the global Etiqueta catalog.
- `ficha-etiqueta`: Association rules, uniqueness, type consistency, and transactional cascade deletion between Ficha and Etiqueta.

### Modified Capabilities
- None

## Approach

Implement `Etiqueta` as a rich domain entity in the Core layer. Define `FichaEtiqueta` as a value object or relation entity within the `Ficha` aggregate.
- **Domain Layer**: Add `Etiqueta` entity, `TipoEtiqueta` enum (`TAREA`, `TRATO`), and domain services/logic to enforce `nombre` uniqueness per `tipoEtiqueta` and color format validation. Modify `Ficha` aggregate to manage `List<FichaEtiqueta>` and validate `tipoEtiqueta` matching.
- **Application Layer**: Add CRUD use cases for `Etiqueta`. Implement cascade delete logic inside the Etiqueta deletion usecase.
- **Infrastructure Layer**: Add EF Core configurations, migrations, and repository implementations. Ensure transactional boundaries for the delete cascade.
- **Presentation Layer**: Expose endpoints for Etiqueta CRUD and querying. Update Ficha endpoints to support Etiqueta payloads implicitly during Ficha updates.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `Core/Domain/Entities` | New/Modified | Create `Etiqueta`, `FichaEtiqueta`. Modify `Ficha`. |
| `Core/Application/UseCases` | New | Add Etiqueta CRUD use cases. |
| `Infrastructure/Data` | Modified | Add DbSets, configurations, and repositories. |
| `API/Controllers` | New/Modified | Add `EtiquetasController`. Update `FichaController`. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Concurrency on Etiqueta Deletion | Medium | Ensure strict transactional boundaries when deleting an Etiqueta and its `FichaEtiqueta` relations. |
| Large Ficha Payloads | Low | Limit the number of tags per Ficha or paginate if necessary (not strictly needed right now but monitorable). |
| Type Inconsistency | Low | Enforce strict domain-level invariants in `Ficha.AddEtiqueta()` verifying `tipoEtiqueta`. |

## Rollback Plan

- Revert the PR.
- Down-migrate the database to remove the `Etiquetas` and `FichaEtiquetas` tables.
- Flush application caches if caching is introduced for tags.

## Dependencies

- EF Core (for migrations and transactional delete).

## Success Criteria

- [ ] Global `Etiqueta` records can be created, read, updated, and deleted.
- [ ] Attempting to create duplicate Etiqueta names per type fails.
- [ ] Fichas can be saved with multiple associated tags of the correct type without duplicates.
- [ ] Modifying an Etiqueta's name/color immediately reflects on all related Fichas on read.
- [ ] Deleting an Etiqueta transactionally removes its relationships from all Fichas.
