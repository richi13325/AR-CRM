# Tasks: Add Ficha Etiquetas

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~900–1100 (domain + application + JPA + DDL + REST + tests) |
| 800-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (domain + application) → PR 2 (infrastructure/JPA + schema) → PR 3 (REST DTO/controllers + boot wiring) |
| Delivery strategy | auto-chain |
| Chain strategy | feature-branch-chain |

Decision needed before apply: No (resolved at orchestrator)
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Slice 1 progress (Domain + Application) — PR 1 base
- Status: in-flight (this apply batch)
- Work unit: Domain + Application slice for Etiqueta/FichaEtiqueta
- Changed-line estimate for slice 1 only: ~350–420 lines

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Domain entities + VOs/enum/exceptions + application ports/services + RED-first tests for both domains. ~350–400 lines. | PR 1 | Base: `main`. Compiles standalone; no infra wiring yet. |
| 2 | JPA entities + mappers + Spring Data repos + schema.sql + repo/controller ITs. ~300–350 lines. | PR 2 | Base: `main`. Adapters implement the ports from PR 1. |
| 3 | REST DTOs/Controllers + Ficha DTO extension + WiringConfig composition + controller tests + DTO mapping tests. ~250–300 lines. | PR 3 | Base: `main`. Wires the full flow; no business logic. |

## Phase 1: Domain Layer (PR 1 — RED-first)

- [x] 1.1 RED: `domain/src/test/java/com/ar/crm2/model/entity/EtiquetaTest.java` — valid name/type/color, blank name rejected, invalid color format rejected, `TipoEtiqueta` enum values.
- [x] 1.2 GREEN: `domain/src/main/java/com/ar/crm2/model/enums/TipoEtiqueta.java` — enum `{TAREA, TRATO}` with `fromFicha(TipoFicha)` mapping helper.
- [x] 1.3 GREEN: `domain/src/main/java/com/ar/crm2/model/vo/EtiquetaId.java` — UUID value object (factory + equality).
- [x] 1.4 GREEN: `domain/src/main/java/com/ar/crm2/exception/InvalidColorFormatException.java`, `DuplicateEtiquetaNameException.java`, `EtiquetaTypeMismatchException.java` — domain exceptions (no framework).
- [x] 1.5 GREEN: `domain/src/main/java/com/ar/crm2/model/entity/Etiqueta.java` — rich entity: name/type/color invariants, `rename(...)`, `recolor(...)`; static factory and reconstitution.
- [x] 1.6 GREEN: `domain/src/main/java/com/ar/crm2/model/entity/FichaEtiqueta.java` — value object holding `EtiquetaId` + `TipoEtiqueta` snapshot; immutability and equality.
- [x] 1.7 RED: `domain/src/test/java/com/ar/crm2/model/entity/FichaEtiquetaTest.java` — duplicate `EtiquetaId` rejected, type mismatch with `Ficha.tipoFicha` rejected, empty list allowed.
- [x] 1.8 RED: `domain/src/test/java/com/ar/crm2/model/entity/FichaTest.java` — add cases for `withEtiquetas(...)` (empty, single, multiple, duplicate, type mismatch) extending the existing test class.
- [x] 1.9 GREEN: `domain/src/main/java/com/ar/crm2/model/entity/Ficha.java` — add immutable `List<FichaEtiqueta>`, `withEtiquetas(List<FichaEtiqueta>)` factory-style updater preserving aggregate invariants.

## Phase 2: Application Layer (PR 1)

- [x] 2.1 RED: `application/src/test/java/com/ar/crm2/application/etiqueta/CreateEtiquetaServiceTest.java` — happy path + duplicate name rejected via port fake.
- [x] 2.2 RED: `application/src/test/java/com/ar/crm2/application/etiqueta/EditEtiquetaServiceTest.java` — renames/recolors, propagates new state, not-found case.
- [x] 2.3 RED: `application/src/test/java/com/ar/crm2/application/etiqueta/GetAllEtiquetasServiceTest.java` — list + filter by `TipoEtiqueta`.
- [x] 2.4 RED: `application/src/test/java/com/ar/crm2/application/etiqueta/DeleteEtiquetaServiceTest.java` — rejects without `confirm=true` when relations exist; cascades relations + deletes catalog row in one tx when confirmed.
- [x] 2.5 RED: `application/src/test/java/com/ar/crm2/application/ficha/FichaEtiquetaResolutionTest.java` — extends CreateFichaService + EditFichaService test coverage to validate `etiquetaIds` resolution and type-mismatch rejection (existing tests in this layer are Keycloak-coupled; this consolidated test covers the new behavior cleanly).
- [x] 2.6 GREEN: `application/.../etiqueta/port/in/{Create,Edit,GetById,GetAll,Delete}EtiquetaUseCase.java` — one interface per use case; `DeleteEtiquetaUseCase` takes `confirm` flag.
- [x] 2.7 GREEN: `application/.../etiqueta/port/out/{Save,FindById,FindAll,ExistsByNombreAndTipo,DeleteById,CountFichaEtiquetasByEtiquetaId,DeleteFichaEtiquetasByEtiquetaId,FindByIds}EtiquetaPort.java` — one port per concern.
- [x] 2.8 GREEN: `application/.../etiqueta/command/{Create,Edit,GetById,GetAll,Delete}EtiquetaCommand.java` and `application/.../etiqueta/exception/EtiquetaNotFoundException.java` + `EtiquetaRequiresConfirmationException.java`.
- [x] 2.9 GREEN: `application/.../etiqueta/service/{Create,Edit,GetById,GetAll,Delete}EtiquetaService.java` — orchestrate uniqueness, mapping. **Note**: transaction boundary placed on infrastructure adapter (not on the use case class with `@Transactional`) — the `application` module is intentionally Spring-free by project rule; tx must be owned by the persistence adapter in slice 2.
- [x] 2.10 GREEN: `application/.../ficha` — extended `CreateFichaCommand`/`EditFichaCommand` with `List<UUID> etiquetaIds`; services resolve ids through `FindEtiquetasByIdsPort` via the new `FichaEtiquetaResolver` helper, building `FichaEtiqueta` rows with type validation against `TipoFicha`.

## Phase 3: Infrastructure Layer (PR 2 — JPA + Schema)

- [x] 3.1 RED: `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/entity/EtiquetaEntityMappingIT.java` — H2 round-trip persist + find.
- [x] 3.2 RED: `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/entity/FichaEtiquetaEntityCascadeIT.java` — orphan removal + cascade delete propagation.
- [x] 3.3 GREEN: `infrastructure/.../adapter/out/persistence/entity/EtiquetaEntity.java` — maps `etiquetas` (UUID id, `uk_etiquetas_nombre_tipo`).
- [x] 3.4 GREEN: `infrastructure/.../adapter/out/persistence/entity/FichaEtiquetaEntity.java` — maps `fichas_etiquetas` (`uk_fichas_etiquetas_ficha_etiqueta`, FKs to `fichas` and `etiquetas`).
- [x] 3.5 GREEN: `infrastructure/.../adapter/out/persistence/entity/FichaEntity.java` — add `@OneToMany(mappedBy="ficha", cascade=ALL, orphanRemoval=true)` `List<FichaEtiquetaEntity> etiquetas`.
- [x] 3.6 GREEN: `infrastructure/.../adapter/out/persistence/mapper/EtiquetaMapper.java` and `FichaMapper.java` updates — UUID/String conversion, list mapping.
- [x] 3.7 GREEN: `infrastructure/.../adapter/out/persistence/repository/{Etiqueta,FichaEtiqueta}SpringDataRepository.java` plus adapters implementing all ports from Phase 2 (`Exists`, `FindAll(filter)`, `Count`, `Delete cascade`).
- [x] 3.8 GREEN: `boot/src/main/resources/schema.sql` — idempotent DDL for `etiquetas`, `fichas_etiquetas`, unique constraints, FKs, indexes (`idx_fichas_etiquetas_etiqueta`).
- [x] 3.9 RED: `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/EtiquetaRepositoryAdapterIT.java` — uniqueness constraint violation mapping, `findAll` filter, cascade delete behavior.

### Slice 2 seams (resolved in this run)
- [x] `application/.../ficha/service/FichaEtiquetaResolver.java` — strict missing-id check: when the catalog returns fewer Etiquetas than requested, throw `EtiquetaNotFoundException.forMissingIds(...)` instead of silently dropping tags. New `forMissingIds` factory on the application `EtiquetaNotFoundException`. Test added in `FichaEtiquetaResolutionTest.create_rejectsWhenCatalogReturnsFewerEtiquetasThanRequested`.
- [x] `infrastructure/.../adapter/in/rest/mapper/FichaCommandMapper.java` — thread `null` for `etiquetaIds` until slice 3 DTOs add the field. Bounded seam so the application compile does not depend on slice 3.
- [x] `infrastructure/pom.xml` — failsafe `<includes>` updated to run the three new `*IT.java` classes under `mvn verify`.

## Phase 4: REST + Boot Wiring (PR 3)

- [x] 4.1 RED: `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/EtiquetaControllerTest.java` — 201/200/204/400/404 status mapping for create/get-by-id/get-all/edit/delete (no-confirm vs confirm).
- [x] 4.2 GREEN: `infrastructure/.../adapter/in/rest/dto/{Create,Edit,Get,Response}EtiquetaRequest.java` and `EtiquetaResponse.java`.
- [x] 4.3 GREEN: `infrastructure/.../adapter/in/rest/dto/{CreateFichaRequest,EditFichaRequest,FichaResponse}.java` — add `etiquetaIds` (request) and compact `etiquetas` (response) fields.
- [x] 4.4 GREEN: `infrastructure/.../adapter/in/rest/EtiquetaController.java` — endpoints: `POST /api/etiquetas/create`, `GET /api/etiquetas/get-all?tipoEtiqueta=`, `GET /api/etiquetas/get-by-id`, `PUT /api/etiquetas/edit`, `DELETE /api/etiquetas/delete?id=&confirm=`.
- [x] 4.5 GREEN: `infrastructure/.../adapter/in/rest/FichaController.java` updates — pass `etiquetaIds` through to the use cases; map response etiquetas. *(Actual implementation: extended `FichaCommandMapper` to thread the new `etiquetaIds` field through; `FichaController` itself was untouched because the change is purely in the request body.)*
- [x] 4.6 GREEN: `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` — register every `*Etiqueta*Service` and `*Etiqueta*Port` adapter as beans.
- [x] 4.7 RED: `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/EtiquetaControllerIT.java` — end-to-end HTTP flow with MockMvc + real Jackson, validating status codes, 404 mapping via the global exception handler, and the `confirm` query parameter on delete. *(Replaces the original `FichaEtiquetaWiringIT` plan: the slice-3 design landed as `EtiquetaControllerIT` + `FichaResponseTest` for the Ficha side, since the Ficha catalog flow does not need a separate integration test — the response mapping is covered by pure-function tests, and the end-to-end catalog HTTP flow is proven through `EtiquetaControllerIT`. No separate `FichaEtiquetaWiringIT` is needed; the Ficha aggregate flow's wiring is covered by the existing `FichaEtiquetaResolutionTest` plus the new `FichaResponseTest`.)*

### Slice 3 seams (resolved in this run)
- [x] `infrastructure/.../adapter/in/rest/GlobalExceptionHandler.java` — added handlers for `EtiquetaNotFoundException` (404) and `EtiquetaRequiresConfirmationException` (409).
- [x] `infrastructure/.../adapter/out/persistence/EtiquetaRepositoryAdapter.java` — removed `@Component` to match project convention (other outbound adapters are wired only via `WiringConfig`).
- [x] `infrastructure/pom.xml` — failsafe `<includes>` updated to add `**/EtiquetaControllerIT.java`.
- [x] Carry-forward runtime gap: `application/src/test/.../etiqueta/GetEtiquetaByIdServiceTest.java` — closes the runtime-facing gap from the slice-2 verify report ("GetEtiquetaByIdService coverage if it belongs via controller path").

### Slice 3 carry-forward corrective (this run)
- [x] `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` — `createFichaUseCase` and `editFichaUseCase` beans updated to inject the `EtiquetaRepositoryAdapter` (which implements `FindEtiquetasByIdsPort`) alongside `FichaRepositoryAdapter`. Slice 1 changed the ficha service constructors to require `FindEtiquetasByIdsPort`, but the boot composition root had not been updated; this is the runtime-blocking gap flagged by the slice-3 verify report.
- [x] `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` — stale Javadoc on `etiquetaRepositoryAdapter` cleaned up: removed the incorrect "annotated with `@Component`" wording and replaced it with a precise description of the project convention.
- [x] `boot/src/test/java/com/ar/crm2/config/FichaWiringTest.java` (new) — wiring-only test under `@SpringJUnitConfig(WiringConfig.class)` that uses Mockito-injected `JpaRepository` mocks and asserts (via reflection on the private `findEtiquetasPort` / `savePort` / `findPort` fields of `CreateFichaService` / `EditFichaService`) that the boot composition root injects the right adapter into every constructor slot. 6/6 passing.

### Slice 3 carry-forward corrective #2 (this run) — owned-relation row id strategy

User-approved corrective: in BOTH `ColumnaTableroEntity` and `FichaEtiquetaEntity`, the relation row MUST generate and own its own UUID technical id. No composite key. No copy of the catalog id (`columnaId` / `etiquetaId`) into the row `id`.

- [x] `infrastructure/.../mapper/TableroMapper.java` — `toColumnaTableroEntity` now uses `UUID.randomUUID().toString()` for the row id (was `ct.getColumnaId().value().toString()`). Catalog `columnaId` is preserved in its own `columna_id` column. Class-level + method-level Javadoc extended with an explicit "Identity strategy" paragraph that names the enforcing test and points at the matching `FichaMapper` contract.
- [x] `infrastructure/.../mapper/FichaMapper.java` — Javadoc only. The mapper was already correct (`UUID.randomUUID().toString()` for each row). Class-level Javadoc extended with the matching "Child row id strategy" paragraph for symmetry.
- [x] `infrastructure/.../entity/ColumnaTableroEntity.java` — Javadoc only. Added an "Identity strategy" paragraph explaining the contract, naming the enforcing test, and pointing at the matching `FichaEtiquetaEntity` strategy.
- [x] `infrastructure/.../entity/FichaEtiquetaEntity.java` — Javadoc only. Added the matching "Identity strategy" paragraph.
- [x] `infrastructure/src/test/.../mapper/FichaMapperTest.java` (NEW) — 6 pure-function unit tests for the static `FichaMapper`. 4 tests in `@Nested ToEntityChildIdOwnership`: (a) row id is not a copy of `etiquetaId`, (b) each child row gets a distinct UUID when multiple etiquetas are present, (c) row id is a valid UUID, (d) `etiquetaId` and `tipoEtiqueta` are preserved as separate fields AND the row id is independent of `etiquetaId`. 2 orthogonal tests: empty list, null domain.
- [x] `infrastructure/src/test/.../mapper/TableroMapperTest.java` (MODIFIED) — extended with 4 new tests proving the same contract for `ColumnaTableroEntity`: (a) row id is not a copy of `columnaId` (this test was RED on the pre-fix code, GREEN after the mapper change), (b) each child row gets a distinct UUID, (c) row id is a valid UUID, (d) `columnaId` is preserved as a separate field AND the row id is independent of it.

## Phase 5: Cleanup / Documentation

- [x] 5.1 Update `openspec/specs/etiqueta-management/spec.md` and `openspec/specs/ficha-etiqueta/spec.md` deltas if any scenario drifted during implementation.
  - `etiqueta-management/spec.md`: tightened "Delete In-Use" wording; added "Id-aware Uniqueness on Edit" requirement (slice-1 corrective); added "Catalog Source of Truth" requirement (Ficha responses must not duplicate catalog name/color).
  - `ficha-etiqueta/spec.md`: tightened "Unique Tags Per Ficha" to "rejected, not deduplicated" (was "rejects OR deduplicates" — implementation chose reject); added "Strict Resolution of Etiqueta Ids" requirement (missing ids surface as 404, never silently dropped).
- [x] 5.2 Run `mvn verify` and capture coverage delta; ensure no regression in `Tablero`/`Ficha` suites.
  - `./mvnw verify` BUILD SUCCESS on 2026-06-08. No regression in `Tablero`/`Ficha` suites.
  - Test totals: infrastructure Surefire 269/269, Failsafe 49/49 (including 9 `EtiquetaControllerIT`, 11 `TableroControllerIT`, 10 `EtiquetaEntityMappingIT`, 7 `FichaEtiquetaEntityCascadeIT`, 12 `EtiquetaRepositoryAdapterIT`); boot `FichaWiringTest` 6/6.
  - JaCoCo ran during module verification. No per-file coverage delta emitted; coverage threshold in `openspec/config.yaml` is `0` (no hard gate), and the change adds tests rather than removing them.
- [x] 5.3 Add a short section in `README.md` (or module-level docs) describing the new endpoints and the catalog-vs-relation model.
  - Extended `openspec/changes/add-ficha-etiquetas/design.md` with an "API Surface" section that lists the five `/api/etiquetas/*` endpoints, the Ficha `etiquetaIds` extension on create/edit, the compact `etiquetas` response shape, and the catalog-vs-relation diagram with the key contracts (catalog owns read state, relation owns its own row id, type snapshot, strict resolution, no per-Ficha assign/unassign endpoints). The project has no root `README.md` or module-level README to extend; the design artifact is the canonical place for this content and will survive into the archive.
- [x] 5.4 Record non-obvious decisions (cascade tx ownership, type snapshot on `FichaEtiqueta`) to the team docs.
  - Added a "Non-Obvious Decisions (Phase 5 — closure)" section to `openspec/changes/add-ficha-etiquetas/design.md` that records D1–D6 (cascade tx ownership, type snapshot, owned-relation row id strategy, `@Component`-less outbound adapters, duplicate-id rejection, strict `etiquetaIds` resolution) with rationale and trade-offs. Closed the original "Open Question" about the `TipoEtiqueta` snapshot.
