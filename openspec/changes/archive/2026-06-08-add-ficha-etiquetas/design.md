# Design: Add Ficha Etiquetas

## Technical Approach

Add `Etiqueta` as a global rich domain entity and `FichaEtiqueta` as an owned relation inside `Ficha`, mirroring the existing `Tablero` → `ColumnaTablero` pattern: the aggregate owns contextual relations while the catalog entity remains the source of truth. Application services orchestrate uniqueness, type validation, listing/filtering, and confirmed delete. Infrastructure persists `etiquetas` plus `fichas_etiquetas` with explicit mappers and Spring Data repositories.

## Architecture Decisions

| Decision | Choice | Alternatives considered | Rationale |
|---|---|---|---|
| Relation model | `FichaEtiqueta` stores `EtiquetaId` + `TipoEtiqueta`; `Ficha` owns `List<FichaEtiqueta>` | Plain `List<EtiquetaId>` or embedded full `Etiqueta` | Matches `ColumnaTablero`, keeps relation lifecycle under `Ficha`, avoids duplicating catalog data. |
| Type model | Add `TipoEtiqueta { TAREA, TRATO }`; map from `TipoFicha` in domain validation | Reuse `TipoFicha` directly | Keeps tag catalog language independent while preserving strict compatibility checks. |
| Safe delete | `DeleteEtiquetaService` requires `confirm=true` when relation count > 0, then deletes relation rows and catalog row in one transaction | Block all in-use deletes | Spec requires confirmed cascade; transaction belongs in application/infrastructure boundary. |
| Persistence | `EtiquetaEntity` plus `FichaEtiquetaEntity` child collection on `FichaEntity` with `cascade=ALL`, `orphanRemoval=true`, unique constraints | JPA `@ManyToMany` | Explicit join entity preserves aggregate ownership and mapper control. |

## Data Flow

Create/update Ficha with tags:

    REST DTO ─→ FichaCommandMapper ─→ Ficha use case
         └──────── etiquetaIds ─────→ FindEtiquetasByIdsPort
                                      └→ Ficha.withEtiquetas(...)
                                             └→ SaveFichaPort ─→ FichaMapper/JPA

Delete Etiqueta:

    EtiquetaController ─→ DeleteEtiquetaUseCase(confirm)
        ├→ FindEtiquetaByIdPort
        ├→ CountFichaEtiquetasByEtiquetaIdPort
        └→ DeleteFichaEtiquetasByEtiquetaIdPort ─→ DeleteEtiquetaByIdPort

## File Changes

| File | Action | Description |
|---|---|---|
| `domain/.../model/entity/Etiqueta.java` | Create | Rich catalog entity with name/type/color validation. |
| `domain/.../model/entity/FichaEtiqueta.java` | Create | Owned relation object for `Ficha`. |
| `domain/.../model/entity/Ficha.java` | Modify | Add immutable etiqueta relation list and factory/reconstitution/update methods. |
| `domain/.../model/vo/EtiquetaId.java` | Create | UUID value object. |
| `domain/.../model/enums/TipoEtiqueta.java` | Create | Tag type enum. |
| `domain/.../exception/*Etiqueta*.java` | Create | Duplicate/type/color/delete confirmation domain exceptions. |
| `application/.../etiqueta/**` | Create | Commands, ports, use cases, services, not-found exception. |
| `application/.../ficha/**` | Modify | Commands/use cases resolve and pass etiqueta ids for create/edit. |
| `infrastructure/.../entity/EtiquetaEntity.java` | Create | Maps `etiquetas`. |
| `infrastructure/.../entity/FichaEtiquetaEntity.java` | Create | Maps `fichas_etiquetas`. |
| `infrastructure/.../entity/FichaEntity.java` | Modify | Add one-to-many child relation. |
| `infrastructure/.../mapper/{EtiquetaMapper,FichaMapper}.java` | Create/Modify | Boundary mapping with UUID/String conversion. |
| `infrastructure/.../repository/*Etiqueta*Repository.java` | Create | Spring Data queries for uniqueness, filtering, relation delete/count. |
| `infrastructure/.../rest/EtiquetaController.java` | Create | CRUD/list/delete API. |
| `infrastructure/.../rest/dto/**Ficha**` | Modify | Add `etiquetaIds` request and `etiquetas` response. |
| `boot/.../WiringConfig.java` | Modify | Register adapters and use case beans. |
| `boot/src/main/resources/schema.sql` | Modify | Idempotent table/index/constraint creation. |

## Interfaces / Contracts

Application ports follow existing package conventions:

```java
interface SaveEtiquetaPort { Etiqueta save(Etiqueta etiqueta); }
interface FindAllEtiquetasPort { List<Etiqueta> findAll(Optional<TipoEtiqueta> tipo); }
interface ExistsEtiquetaByNombreAndTipoPort { boolean exists(String nombre, TipoEtiqueta tipo, EtiquetaId excludeId); }
interface DeleteFichaEtiquetasByEtiquetaIdPort { void deleteByEtiquetaId(EtiquetaId id); }
interface FindEtiquetasByIdsPort { List<Etiqueta> findByIds(List<EtiquetaId> ids); }
interface CountFichaEtiquetasByEtiquetaIdPort { long countByEtiquetaId(EtiquetaId id); }
interface DeleteEtiquetaByIdPort { void deleteById(EtiquetaId id); }
interface FindEtiquetaByIdPort { Optional<Etiqueta> findById(EtiquetaId id); }
```

Inbound use cases (one port per concern, one service implementation per port):

```java
interface CreateEtiquetaUseCase { Etiqueta create(CreateEtiquetaCommand cmd); }
interface EditEtiquetaUseCase   { Etiqueta edit(EditEtiquetaCommand cmd); }
interface GetByIdEtiquetaUseCase { Etiqueta getById(GetEtiquetaByIdCommand cmd); }
interface GetAllEtiquetasUseCase { List<Etiqueta> getAll(GetAllEtiquetasCommand cmd); }
interface DeleteEtiquetaUseCase { void delete(DeleteEtiquetaCommand cmd); } // cmd carries boolean confirm
```

## API Surface

All endpoints are under the same JWT-protected resource server. The Etiqueta
catalog is global (not per-Ficha) — there is no per-Ficha `assign`/`unassign`
endpoint. Etiqueta relations are written as part of the Ficha aggregate flow.

### Etiqueta catalog

| Method | Path | Query / Body | Success | Error mapping |
|---|---|---|---|---|
| `POST`   | `/api/etiquetas/create`        | body: `CreateEtiquetaRequest` (`nombre`, `tipoEtiqueta`, `color` `#RRGGBB`) | `201 Created` + `EtiquetaResponse` | `400` validation, `409` duplicate name |
| `GET`    | `/api/etiquetas/get-all`       | query: `tipoEtiqueta=TAREA\|TRATO` (optional) | `200 OK` + `List<EtiquetaResponse>` | — |
| `GET`    | `/api/etiquetas/get-by-id`     | query: `id=UUID` | `200 OK` + `EtiquetaResponse` | `404` not found |
| `PUT`    | `/api/etiquetas/edit`          | query: `id=UUID`; body: `EditEtiquetaRequest` (`nombre`, `color`; `tipoEtiqueta` is immutable) | `200 OK` + `EtiquetaResponse` | `400` validation, `404` not found, `409` duplicate name |
| `DELETE` | `/api/etiquetas/delete`        | query: `id=UUID&confirm=boolean` (`confirm` defaults to `false`) | `204 No Content` | `404` not found, `409` requires confirmation when in use |

All ids travel as `@RequestParam` for consistency with the rest of the
controllers in the project.

### Ficha aggregate (etiqueta extension)

| Method | Path | Body change | Response change |
|---|---|---|---|
| `POST` `/api/fichas/create` | body now accepts `etiquetaIds: List<UUID>` (optional) | `201` + `FichaResponse` now includes a compact `etiquetas: List<{id, tipoEtiqueta}>` (no duplicated name/color) |
| `PUT`  `/api/fichas/edit`   | body now accepts `etiquetaIds: List<UUID>` (optional) | `200` + same response shape |

`etiquetaIds` is **strict**: every id MUST resolve through the catalog, and the
request is rejected with `404` if any id is unknown. The system does not
silently drop unknown ids.

### Catalog-vs-relation model (read this before extending the API)

```
Etiqueta (catalog, source of truth)
   ├── id (UUID, generated)
   ├── nombre           ── renamed/recolored here
   ├── tipoEtiqueta     ── TAREA | TRATO (immutable per Etiqueta)
   └── color            ── #RRGGBB

Ficha (aggregate root)
   └── etiquetas: List<FichaEtiqueta>   ── owned relation
            ├── id (UUID, generated)    ── own technical id, not the catalog id
            ├── etiquetaId  ── FK to Etiqueta
            └── tipoEtiqueta ── snapshot of Etiqueta.tipoEtiqueta at write time
```

Key contracts:

- **Catalog owns the read state.** Ficha responses do NOT carry `nombre` or
  `color`. Consumers resolve the full Etiqueta from the catalog when needed.
  This guarantees renames/recolors are reflected everywhere immediately.
- **Relation owns its own row id.** `FichaEtiquetaEntity.id` is a fresh
  `UUID.randomUUID()` per write, never the catalog `etiquetaId`. The same
  rule applies to `ColumnaTableroEntity.id` for symmetry.
- **Type snapshot is required.** `FichaEtiqueta.tipoEtiqueta` is stored at
  write time so the aggregate can reconstitute itself without re-reading
  the catalog. This mirrors the existing `ColumnaTablero` pattern.
- **Strict resolution.** The Ficha `etiquetaIds` payload is resolved through
  `FindEtiquetasByIdsPort`; missing ids surface as `404` via
  `EtiquetaNotFoundException.forMissingIds(...)`.
- **No per-Ficha assign/unassign endpoints.** Etiquetas travel with the
  Ficha aggregate. This is the agreed slice-3 scope boundary.

## Testing Strategy

| Layer | What to Test | Approach |
|---|---|---|
| Domain | color/name validation, duplicate relation rejection, type mismatch | JUnit tests for `Etiqueta`, `FichaEtiqueta`, `Ficha`. |
| Application | CRUD uniqueness, filter, confirmed safe delete, Ficha tag resolution | Service tests with fake/mock ports. |
| Infrastructure | JPA mapping, unique indexes, cascade/orphan delete, controller status mapping | Mapper tests, repository ITs, controller tests. |
| E2E | Not available | Covered through `mvn verify` integration suite. |

## Migration / Rollout

No data backfill required. Add idempotent DDL for `etiquetas` and `fichas_etiquetas`, including `uk_etiquetas_nombre_tipo`, `uk_fichas_etiquetas_ficha_etiqueta`, FKs to `fichas` and `etiquetas`, and indexes by type and etiqueta id.

## Review Slicing Strategy

Implementation likely exceeds 800 lines. Slice PRs as: (1) domain + application ports/services/tests, (2) JPA persistence + schema/tests, (3) REST DTO/controllers + boot wiring/tests.

## Non-Obvious Decisions (Phase 5 — closure)

These decisions emerged during the slice 1 → 3 chain and from the two
carry-forward correctives. Recording them so future maintainers do not have
to re-derive them from the code.

### D1 — Cascade transaction lives in the persistence adapter, not the use case

`DeleteEtiquetaService` orchestrates the two-step dance
(count relations → delete relations → delete catalog row) but does NOT carry
a `@Transactional` annotation. The single transaction that wraps the cascade
lives on the `EtiquetaRepositoryAdapter` method that performs both
`deleteFichaEtiquetasByEtiquetaId(...)` and `deleteById(...)`.

**Why**: the `application` module is intentionally Spring-free by project rule
(`MASTER_RULES.md` — `domain` and `application` MUST be testable without
lifting Spring). Owning the transaction at the adapter keeps the application
module framework-agnostic while still guaranteeing atomic cascade delete.

**Trade-off**: the catalog and the relation rows are deleted by the SAME
adapter call, not by composing two port calls inside a service-level
transaction. This is acceptable because both deletes are part of the same
aggregate-scoped write and there is no reasonable read of intermediate state
that a service-level transaction would protect.

### D2 — `TipoEtiqueta` snapshot on `FichaEtiqueta` (closed)

`FichaEtiqueta` stores `TipoEtiqueta` at write time, not by reference to
`Etiqueta.tipoEtiqueta`. This mirrors the existing `ColumnaTablero` pattern.

**Why**: the aggregate (`Ficha`) must be able to reconstitute itself
(structural validation in `validarEtiquetas(...)`) without re-reading the
catalog at construction time. Storing the type snapshot on the relation row
keeps the aggregate self-validating.

**Trade-off**: rename/recolor of an Etiqueta is reflected everywhere
immediately (catalog is the source of truth for `nombre` and `color`),
BUT the `tipoEtiqueta` snapshot does not auto-refresh. This is correct
semantics: the type of an Etiqueta is immutable (`EditEtiquetaRequest` does
not accept `tipoEtiqueta`), so the snapshot cannot drift.

### D3 — Owned-relation rows own their own UUID technical id (from corrective #2)

`FichaEtiquetaEntity.id` and `ColumnaTableroEntity.id` are each a fresh
`UUID.randomUUID()` generated at write time. The catalog `etiquetaId` /
`columnaId` is stored in a SEPARATE column (the FK) and is never copied into
the row's primary id. No composite keys are used.

**Why**: the row's domain identity is the parent-plus-child pair
(`(Ficha, Etiqueta)` for `FichaEtiquetaEntity`, `(Tablero, Columna)` for
`ColumnaTableroEntity`), not a single UUID. Forcing the row id to be the
catalog id conflates the two identities and breaks anyone who later tries to
load the row by id independently.

**Enforcement**: `FichaMapperTest` and `TableroMapperTest` lock this contract
in CI. The `FichaEtiquetaEntity` and `ColumnaTableroEntity` Javadoc paragraphs
("Identity strategy") and the mapper Javadoc paragraphs ("Child row id
strategy") name the enforcing test, so the rule is discoverable from each
file.

### D4 — Outbound adapters are wired only via `WiringConfig` (no `@Component`)

`EtiquetaRepositoryAdapter` and every other outbound adapter in the project
is exposed as a `@Bean` method on `WiringConfig`. None of them carry
`@Component`, `@Repository`, or any other stereotype annotation.

**Why**: keeps the entire dependency graph in one auditable place
(`WiringConfig`). The original slice-2 carry had a stray `@Component` on
`EtiquetaRepositoryAdapter`; the slice-3 corrective removed it for symmetry
with the other adapters (`FichaRepositoryAdapter`, `TableroRepositoryAdapter`,
`EmpresaRepositoryAdapter`, etc.).

**Enforcement**: `Boot only composes; no @Service / @Repository outside the
composition root` is already in `BOOT_RULES.md`. The wiring test
`FichaWiringTest` (boot, 6 tests) reads back the constructed services via
reflection to assert that the boot composition root injects the right
adapter into every constructor slot.

### D5 — Duplicate etiqueta id is rejected, not deduplicated

`Ficha.validarEtiquetas(...)` throws on duplicate `EtiquetaId` rather than
silently deduplicating the list. The spec delta in
`openspec/changes/add-ficha-etiquetas/specs/ficha-etiqueta/spec.md`
explicitly tightens the original "rejects OR deduplicates" scenario to
"rejects".

**Why**: silent deduplication hides caller intent. A client that sends the
same id twice is either buggy (forbid it loudly) or racing (the second copy
will produce a unique-constraint violation on the database anyway, with a
worse error). Failing fast at the domain boundary gives a clear, actionable
error.

**Trade-off**: a well-meaning caller must pre-deduplicate. The cost is
trivial in the front-end; the benefit is a guarantee that the relation list
written to the database is exactly the list the client sent.

### D6 — Strict `etiquetaIds` resolution

`FichaEtiquetaResolver` resolves every requested id through
`FindEtiquetasByIdsPort` and throws `EtiquetaNotFoundException.forMissingIds(...)`
when the catalog returns fewer Etiquetas than the request asked for. The
resolver does NOT silently drop unknown ids.

**Why**: matches the project rule of "fail loudly on bad input". A typo in
the front-end or a stale id would otherwise silently produce a Ficha saved
with fewer tags than the user intended — which is worse than a 404.

**Trade-off**: callers must pre-validate the ids. The cost is one extra
`/api/etiquetas/get-all` lookup in the front-end; the benefit is a
defense-in-depth guarantee at the application boundary.

## Open Questions

None. All questions raised during the design and apply phases are now closed
in the Non-Obvious Decisions section above (D1–D6).

Originally:

- [x] ~~Should `Ficha` duplicate `TipoEtiqueta` in `FichaEtiqueta`, or resolve type solely from loaded `Etiqueta` before constructing relations?~~ — Closed by **D2**: snapshot the type on the relation row, mirroring the `ColumnaTablero` pattern. The type is immutable, so the snapshot cannot drift.
