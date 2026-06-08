## Exploration: add-ficha-etiquetas

### Current State
`Ficha` is currently a small rich entity that stores only card identity, owning `columnaId`, `tipoFicha`, the exclusive `tratoId`/`tareaId` pair, and `actualizadoEn`. It is persisted as a standalone aggregate through `FichaRepositoryAdapter` and exposed directly by `FichaController` CRUD endpoints. The codebase already separates a catalog entity from a contextual relation in the `Columna` + `ColumnaTablero` model: catalog data lives independently, while the relation stores only contextual fields plus foreign ids and enforces compatibility rules in domain/application.

### Affected Areas
- `domain/src/main/java/com/ar/crm2/model/entity/Ficha.java` — must gain 0..n etiqueta assignment state and domain behavior.
- `domain/src/main/java/com/ar/crm2/model/enums/TipoFicha.java` — current compatibility source for TAREA/TRATO semantics.
- `domain/src/main/java/com/ar/crm2/model/entity/ColumnaTablero.java` — reference pattern for contextual relation modeling.
- `domain/src/main/java/com/ar/crm2/model/entity/Columna.java` — reference pattern for catalog entity uniqueness by type.
- `application/src/main/java/com/ar/crm2/application/ficha/service/*.java` — create/edit/get flows must load/persist etiquetas consistently.
- `application/src/main/java/com/ar/crm2/application/ficha/port/out/*.java` — likely need richer read/save ports or new assignment/query ports.
- `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/entity/FichaEntity.java` — must persist the relation.
- `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/mapper/FichaMapper.java` — must map relation ids/domain objects.
- `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/FichaRepositoryAdapter.java` — likely needs repository orchestration beyond a flat single-table save.
- `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/repository/FichaRepository.java` — may need fetch joins / existence queries.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/FichaController.java` — DTO/API surface will expand for etiquetas.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/request/*.java` and `dto/response/FichaResponse.java` — request/response contracts must include etiqueta ids or expanded etiqueta data.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/GlobalExceptionHandler.java` — should map new not-found/conflict exceptions if introduced.
- `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` — must wire new ports, adapters, repositories, and use cases.
- `boot/src/main/resources/schema.sql` and JPA tables — local schema compatibility may need additive DDL and junction constraints.

### Approaches
1. **Etiqueta catalog + explicit FichaEtiqueta relation** — model `Etiqueta` as its own rich entity and store assignment through a dedicated intermediate relation (`FichaEtiqueta`) owned by `Ficha` in the domain, similar in spirit to `ColumnaTablero`.
   - Pros: matches current architecture, keeps `Etiqueta` reusable across many fichas, makes compatibility/duplicate assignment rules explicit, avoids raw `@ManyToMany`, supports future relation metadata if needed.
   - Cons: more files across all layers, mapper/repository complexity increases because `Ficha` is no longer a single-table save.
   - Effort: High

2. **Direct JPA many-to-many with thin domain exposure** — keep `Etiqueta` separate but let infrastructure own the join table and hydrate a simple etiqueta collection into `Ficha`.
   - Pros: fewer persistence classes initially, quicker CRUD wiring.
   - Cons: fights the project’s rich-domain style, hides business invariants in adapters/services, makes duplicate/type checks easier to leak into infrastructure, harder to evolve safely.
   - Effort: Medium

### Recommendation
Use **Approach 1**. Add a rich catalog entity `Etiqueta` (`EtiquetaId`, `nombre`, `tipoEtiqueta`, `color`) plus a contextual relation `FichaEtiqueta` owned by `Ficha`, with no separate relation id. `Ficha` should expose behaviors like assign/unassign/replace etiquetas and enforce: no null relation, no duplicate `EtiquetaId`, and `Ficha.tipoFicha` must match every assigned `Etiqueta.tipoEtiqueta`. In application, creation/edit flows should resolve etiqueta ids from an `Etiqueta` repository/port before invoking `Ficha` behavior, exactly like `AsignarColumnaTableroService` resolves `Columna` before creating `ColumnaTablero`. Persist with explicit JPA entities (`EtiquetaEntity` + `FichaEtiquetaEntity`) and composite uniqueness (`ficha_id`, `etiqueta_id`) rather than raw `@ManyToMany`.

Key invariants/persistence/API/testing implications:
- `Etiqueta.nombre` must be unique within `tipoEtiqueta`; enforce in domain/application and back it with a DB unique constraint.
- `Ficha` must always model etiquetas as 0..n, never nullable; empty collection is valid.
- Assignment must reject mismatched types (`Ficha.TAREA` ↔ `Etiqueta.TAREA`, `Ficha.TRATO` ↔ `Etiqueta.TRATO` only).
- Duplicate assignment of the same etiqueta to one ficha must be idempotent or rejected consistently; current style favors explicit rejection via domain exception.
- Create/edit ficha APIs will likely need `etiquetaIds` in the body; get endpoints should return either ids only or expanded etiqueta DTOs. Expanded DTOs are more useful and align with current controller responses.
- Because runtime uses `ddl-auto=update` plus `schema.sql`, additive tables may be created automatically, but explicit compatibility DDL may still be needed if local environments require indexes/constraints not reliably added by Hibernate.
- Tests should span: domain invariant tests (`Etiqueta`, `FichaEtiqueta`, `Ficha`), application orchestration tests (duplicate name, type mismatch, assignment replacement), repository mapper tests, controller tests, and at least one JPA integration test for unique constraints and relation persistence.

### Risks
- The existing `Ficha` persistence path assumes a flat entity; introducing child relations will ripple through mapper, repository adapter, DTOs, and tests.
- If create/edit ficha endpoints accept etiqueta ids without loading catalog etiquetas first, type consistency can be violated outside the domain.
- Relying only on application-level uniqueness for `nombre + tipoEtiqueta` is race-prone; DB constraint is required.
- This change likely exceeds the 400-line review budget and should be forecast as chained implementation slices.

### Ready for Proposal
Yes — enough code evidence exists to propose a catalog `Etiqueta` plus explicit `FichaEtiqueta` relation, likely split later into domain/application and infrastructure/API review slices.
