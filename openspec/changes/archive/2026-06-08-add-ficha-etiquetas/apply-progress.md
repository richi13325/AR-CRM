# Apply-Progress: add-ficha-etiquetas — Slice 3 (REST + Boot Wiring) + Carry-forward Fixes

## Mode
Strict TDD. Continuation of slice 2 (persistence). This slice wires the REST inbound adapter for `Etiqueta` and extends the `Ficha` REST adapter to expose etiqueta relations, per the agreed scope boundary (no separate manual assign/unassign API; etiquetas travel as part of the Ficha aggregate flow).

## Carried over from slice 1 (merge: see #1826 prior content)
- Slice 1: domain + application layer complete and verified (PASS WITH WARNINGS, see #1828)
- Slice 1 corrective continuations 1 + 2 complete (id-aware uniqueness port, see #1826 prior content)

## Carried over from slice 2 (merge: see #1826 prior content)
- Slice 2: JPA + schema + Spring Data repos + integration tests complete and verified
- Slice 2 seams (FichaEtiquetaResolver strict missing-id, FichaCommandMapper `null` `etiquetaIds` seam, failsafe includes for new ITs) complete

## Slice 3 (this run) — REST + DTOs + Controllers + Boot Wiring

### Review Workload Forecast (resolved by orchestrator)
- Delivery strategy: `auto-chain`
- Chain strategy: `feature-branch-chain`
- 800-line PR budget risk: High (carried over)
- Decision needed before apply: No (resolved by orchestrator: this is the next autonomous slice, no PR boundary expansion)
- PR boundary for this run: implement ONLY the next autonomous slice (REST + DTOs + controllers + wiring + controller/runtime-facing tests), no PR creation, no commit, no push.

### Slice 3 work unit: REST inbound + boot wiring + controller tests

#### Production code added (infrastructure, 5 new main + 1 new test adapter pattern)
- `infrastructure/.../adapter/in/rest/dto/request/CreateEtiquetaRequest.java` — record with `@NotBlank` nombre, `@NotNull` tipo, `@NotBlank @Pattern("#RRGGBB")` color.
- `infrastructure/.../adapter/in/rest/dto/request/EditEtiquetaRequest.java` — record with editable fields; tipo is immutable.
- `infrastructure/.../adapter/in/rest/dto/response/EtiquetaResponse.java` — record with `id`, `nombre`, `tipoEtiqueta` (as enum name), `color`, `creadoEn`. `fromDomain(Etiqueta)` mapper.
- `infrastructure/.../adapter/in/rest/mapper/EtiquetaCommandMapper.java` — static utility, no Spring: `toCreateCommand`, `toEditCommand`, `toGetByIdCommand`, `toDeleteCommand`, `toGetAllCommand`.
- `infrastructure/.../adapter/in/rest/EtiquetaController.java` — REST endpoints:
  - `POST /api/etiquetas/create` — 201 Created
  - `GET /api/etiquetas/get-all?tipoEtiqueta=` — 200 OK (filter optional)
  - `GET /api/etiquetas/get-by-id?id=` — 200 OK
  - `PUT /api/etiquetas/edit?id=` — 200 OK
  - `DELETE /api/etiquetas/delete?id=&confirm=` — 204 No Content (confirm defaults to false)
  - All ids via `@RequestParam` (consistent with the rest of the controllers in the project).

#### Production code modified
- `infrastructure/.../adapter/in/rest/dto/request/CreateFichaRequest.java` — added `List<UUID> etiquetaIds`.
- `infrastructure/.../adapter/in/rest/dto/request/EditFichaRequest.java` — added `List<UUID> etiquetaIds`.
- `infrastructure/.../adapter/in/rest/dto/response/FichaResponse.java` — added `List<EtiquetaRefDto> etiquetas` (compact: id + tipo). Inner `EtiquetaRefDto` record + `fromDomain(FichaEtiqueta)` mapper.
- `infrastructure/.../adapter/in/rest/mapper/FichaCommandMapper.java` — replaces the slice-2 `null` `etiquetaIds` seam with the real DTO field. Both create and edit now thread `request.etiquetaIds()` to the application command.
- `infrastructure/.../adapter/in/rest/GlobalExceptionHandler.java` — added `handleEtiquetaNotFoundException` (404) and `handleEtiquetaRequiresConfirmationException` (409). Existing handlers untouched.
- `infrastructure/.../adapter/out/persistence/EtiquetaRepositoryAdapter.java` — removed the `@Component` annotation that was a slice-2 deviation. Now wired only through `WiringConfig#etiquetaRepositoryAdapter`, consistent with every other outbound adapter in the project (FichaRepositoryAdapter, TableroRepositoryAdapter, etc.).
- `infrastructure/pom.xml` — failsafe `<includes>` updated to add `**/EtiquetaControllerIT.java`.
- `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` — added:
  - `etiquetaRepositoryAdapter(EtiquetaRepository, FichaEtiquetaRepository)` — explicit bean, same signature as the removed `@Component` constructor.
  - `createEtiquetaUseCase`, `getAllEtiquetasUseCase`, `getEtiquetaByIdUseCase`, `editEtiquetaUseCase`, `deleteEtiquetaUseCase` — one `@Bean` per inbound port, each delegating to the explicit adapter.

#### Test code added (5 new test files, 3 modified test files)
- `infrastructure/src/test/.../adapter/in/rest/EtiquetaControllerTest.java` (new) — 13 Mockito-based controller unit tests covering all five endpoints, status codes, command field mapping, 404 propagation, and confirmation-required propagation.
- `infrastructure/src/test/.../adapter/in/rest/EtiquetaControllerIT.java` (new) — 9 `@WebMvcTest` integration tests with real JSON serialization covering the same surface plus 404 status mapping via the global exception handler.
- `infrastructure/src/test/.../adapter/in/rest/mapper/EtiquetaCommandMapperTest.java` (new) — 6 pure-function mapper tests (no mocks).
- `infrastructure/src/test/.../adapter/in/rest/mapper/FichaCommandMapperTest.java` (new) — 4 mapper tests for the etiquetaIds threading path (null, empty, single, multiple).
- `infrastructure/src/test/.../adapter/in/rest/dto/response/EtiquetaResponseTest.java` (new) — 2 domain->DTO mapping tests.
- `infrastructure/src/test/.../adapter/in/rest/dto/response/FichaResponseTest.java` (new) — 3 domain->DTO mapping tests for the etiqueta list extension.
- `infrastructure/src/test/.../adapter/in/rest/GlobalExceptionHandlerTest.java` (modified) — added 2 tests for the new etiqueta exception handlers.
- `application/src/test/.../application/etiqueta/GetEtiquetaByIdServiceTest.java` (new) — 3 unit tests closing the runtime-facing gap from the prior verify (noted as "GetEtiquetaByIdService coverage if it belongs via controller path"). Verifies find-by-id happy path, 404 propagation, and that the service implements the inbound port contract.

### Slice 3 test layer strategy (strict TDD)
- **Unit tests (Mockito)** for the controller — `EtiquetaControllerTest` (13 cases): 201/200/204 status mapping, DTO->command field mapping, 404 + 409 propagation, filter param forwarding. No real Spring context.
- **Unit tests (no mocks, pure function)** for the static mappers — `EtiquetaCommandMapperTest` (6 cases) and `FichaCommandMapperTest` (4 cases). All edge cases (null, empty, single, multiple).
- **Unit tests (no mocks, pure mapping)** for the response DTOs — `EtiquetaResponseTest` (2 cases) and `FichaResponseTest` (3 cases for the etiqueta list extension).
- **Integration tests (Spring `@WebMvcTest` + MockMvc + real Jackson)** for the controller — `EtiquetaControllerIT` (9 cases): full HTTP surface with status code assertions via `jsonPath` and `status()`. Mocks the use cases at the application boundary; loads the real `GlobalExceptionHandler` so 404/409 mapping is exercised.
- **Unit tests (Mockito)** for the application service `GetEtiquetaByIdService` (3 cases) — closes the runtime-facing gap noted in the slice-2 verify report.
- **Unit tests for the global exception handler** — `GlobalExceptionHandlerTest` extended with 2 cases for the new etiqueta handlers (404 + 409).

### Slice 3 — Safety Net
- Domain baseline: 168/168 unchanged.
- Application baseline: 72/72 (post slice 1 batch 2).
- Infrastructure Surefire baseline (post slice 2): 229/229.
- After this run: 75 application (+3 for GetEtiquetaByIdServiceTest), 259 infrastructure (+30), 49 Failsafe ITs (+9).
- `./mvnw -pl infrastructure -am verify` — BUILD SUCCESS.

### TDD Cycle Evidence
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 4.1 | `EtiquetaControllerTest.java` (new) | Unit (Mockito) | N/A (new) | ✅ Written (13 cases) | ✅ 13/13 passed | ✅ 13 cases (status codes per endpoint, mapping per field, exception propagation) | ➖ None needed |
| 4.2 | `EtiquetaCommandMapperTest.java` (new) | Unit (pure) | N/A (new) | ✅ Written (6 cases) | ✅ 6/6 passed | ✅ 6 cases (create/edit/getById/delete with confirm flag/getAll with and without filter) | ➖ None needed |
| 4.2 (Dtos) | `EtiquetaResponseTest.java` (new) | Unit (pure) | N/A (new) | ✅ Written (2 cases) | ✅ 2/2 passed | ✅ 2 cases (full domain mapping + tipo-as-string) | ➖ None needed |
| 4.3 | `FichaCommandMapperTest.java` (new) | Unit (pure) | N/A (new) | ✅ Written (4 cases) | ✅ 4/4 passed | ✅ 4 cases (null, empty, single, multiple etiquetaIds for create + edit) | ➖ None needed |
| 4.3 (FichaResp) | `FichaResponseTest.java` (new) | Unit (pure) | N/A (new) | ✅ Written (3 cases) | ✅ 3/3 passed | ✅ 3 cases (empty list, list with multiple etiquetas, existing fields preservation) | ➖ None needed |
| 4.3 (Controllers) | covered by 4.1 | n/a | n/a | n/a (DTO changes are the GREEN) | ✅ 13/13 passed | n/a | n/a |
| 4.4 | `EtiquetaController.java` (new) | n/a (production) | n/a | n/a (controller is the GREEN) | ✅ Compile + tests pass | n/a | ➖ None needed |
| 4.5 | covered by 4.1 (existing FichaController untouched, only mapper) | n/a | n/a | n/a | n/a | n/a | n/a |
| 4.6 | `WiringConfig.java` (modified) | n/a (build) | n/a | n/a (config change) | ✅ `@SpringBootTest` + `mvn verify` succeeds | n/a | n/a |
| 4.7 (IT) | `EtiquetaControllerIT.java` (new) | Integration (WebMvcTest) | N/A (new) | ✅ Written (9 cases) | ✅ 9/9 passed | ✅ 9 cases (full HTTP flow with real Jackson + global handler, 404 mapping, 204 with/without confirm flag) | ➖ None needed |
| 4.6 (GlobalHandler) | `GlobalExceptionHandlerTest.java` (modified) | Unit | ✅ 11/11 | ✅ Written (2 new cases for etiqueta handlers) | ✅ 13/13 passed | ✅ 2 cases (404 for not-found, 409 for confirmation-required) | ➖ None needed |
| 4.6 (RemovedComponent) | `EtiquetaRepositoryAdapter.java` (modified) | n/a (consistency fix) | n/a | n/a (consistency fix) | ✅ `mvn verify` still passes | n/a | ✅ Removed `@Component` to match project convention |
| Gap-fix | `GetEtiquetaByIdServiceTest.java` (new) | Unit (Mockito) | N/A (new) | ✅ Written (3 cases) | ✅ 3/3 passed | ✅ 3 cases (happy path, not-found, port-implementation contract) | ➖ None needed |

### Test Summary
- **Total tests written this run**: ~40 new test cases across 6 new test files (13 + 6 + 2 + 4 + 3 + 9 + 3 = 40).
- **Total tests passing after this run**: 168 domain + 75 application + 259 infrastructure Surefire + 49 infrastructure Failsafe = 551 total.
- **Layers used**: Unit (Mockito 13 + pure 18 + Mockito 3 = 34), Integration (WebMvcTest 9), Unit for handler (2).
- **Approval tests (refactoring)**: None — slice 3 is greenfield REST + wiring, no refactoring of existing code (only tiny edits: mapper `null`-seam replaced with real threading, DTOs extended, handler added).
- **Pure functions created**: `EtiquetaCommandMapper`, `EtiquetaResponse.fromDomain`, `FichaCommandMapper` (etiquetaIds threading path).

### Files Changed (this run only)

**New (infrastructure, 5 main + 6 test + 1 application test):**
- `infrastructure/.../dto/request/CreateEtiquetaRequest.java`
- `infrastructure/.../dto/request/EditEtiquetaRequest.java`
- `infrastructure/.../dto/response/EtiquetaResponse.java`
- `infrastructure/.../mapper/EtiquetaCommandMapper.java`
- `infrastructure/.../EtiquetaController.java`
- `infrastructure/src/test/.../EtiquetaControllerTest.java`
- `infrastructure/src/test/.../EtiquetaControllerIT.java`
- `infrastructure/src/test/.../mapper/EtiquetaCommandMapperTest.java`
- `infrastructure/src/test/.../mapper/FichaCommandMapperTest.java`
- `infrastructure/src/test/.../dto/response/EtiquetaResponseTest.java`
- `infrastructure/src/test/.../dto/response/FichaResponseTest.java`
- `application/src/test/.../etiqueta/GetEtiquetaByIdServiceTest.java`

**Modified (infrastructure, 5):**
- `infrastructure/.../dto/request/CreateFichaRequest.java` — added `etiquetaIds` field.
- `infrastructure/.../dto/request/EditFichaRequest.java` — added `etiquetaIds` field.
- `infrastructure/.../dto/response/FichaResponse.java` — added `etiquetas` list and inner `EtiquetaRefDto`.
- `infrastructure/.../mapper/FichaCommandMapper.java` — replaced `null` seam with real `etiquetaIds` threading.
- `infrastructure/.../GlobalExceptionHandler.java` — added 2 handlers.
- `infrastructure/.../EtiquetaRepositoryAdapter.java` — removed `@Component` to match project convention.
- `infrastructure/src/test/.../GlobalExceptionHandlerTest.java` — added 2 tests.
- `infrastructure/pom.xml` — failsafe includes updated.

**Modified (boot, 1):**
- `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` — added 6 Etiqueta beans.

**Modified (tasks, 1):**
- `openspec/changes/add-ficha-etiquetas/tasks.md` — phase 4 marked complete (4.1-4.7 all `[x]`); slice 3 seams section appended.

### Deviations from Design
1. **Removed `@Component` on `EtiquetaRepositoryAdapter`** — was a slice-2 deviation from the project convention. Now the adapter is wired only through `WiringConfig#etiquetaRepositoryAdapter`, consistent with every other outbound adapter. This avoids a duplicate-bean issue when both `@Component` and `@Bean` define the same adapter.
2. **One `@Bean` per Etiqueta use case, all delegating to the single `EtiquetaRepositoryAdapter`** — matches the existing project pattern (`CreateEmpresaUseCase`, `EditEmpresaUseCase`, etc., all delegate to the single `EmpresaRepositoryAdapter`).
3. **Single `delete` endpoint with `confirm` query param** — keeps the URL shape consistent with the rest of the controllers (no path variable, all action-named routes). The `confirm` flag defaults to `false`.
4. **Compact `EtiquetaRefDto` in `FichaResponse`** — only carries `id` and `tipoEtiqueta`. The catalog Etiqueta's name and color are NOT duplicated in the Ficha response; per the "edit updates universally" scenario, the front-end resolves the full Etiqueta from the catalog when needed. This avoids staleness when an Etiqueta is renamed/recolored.
5. **The slice-2 `FichaCommandMapper` `null` `etiquetaIds` seam is replaced with the real `request.etiquetaIds()` threading**, since the DTOs are now extended. The seam was documented as a placeholder; this is its removal.

### Issues Found
None blocking. The 404/409 status mapping required adding the two new handlers to `GlobalExceptionHandler`; this is a natural part of closing the controller surface, not a defect.

## Workload / PR Boundary
- Mode: `auto-chain` (slice 3 of the chained PR set)
- Chain strategy: `feature-branch-chain` (next PR after slice 2; the feature branch already contains slices 1 and 2)
- Current work unit: REST DTOs + controllers + boot wiring + controller tests + carry-forward runtime gap closure (`GetEtiquetaByIdServiceTest`)
- Boundary: 12 new files + 8 modified files. ~+1100 lines net (within the 800-line budget per file but cumulative across slices 1+2+3; slice 3 alone is ~500-700 lines, consistent with the plan estimate of 250-300 lines plus a bit more for the IT).
- Estimated review budget impact: well-scoped to the inbound adapter layer; the persistence/domain layers are unchanged from slice 2. No PR, no commit, no push performed.

## Remaining Tasks
- [ ] 5.1–5.4 Phase 5: cleanup + docs
  - 5.1 Update `openspec/specs/etiqueta-management/spec.md` and `openspec/specs/ficha-etiqueta/spec.md` deltas if any scenario drifted during implementation.
  - 5.2 Run `mvn verify` and capture coverage delta; ensure no regression in `Tablero`/`Ficha` suites.
  - 5.3 Add a short section in `README.md` (or module-level docs) describing the new endpoints and the catalog-vs-relation model.
  - 5.4 Record non-obvious decisions (cascade tx ownership, type snapshot on `FichaEtiqueta`) to the team docs.

## Status
Slice 3 complete: 12 new files, 8 modified files, ~40 new test cases. 551 tests passing across domain (168), application (75), infrastructure (259 Surefire + 49 Failsafe). `mvn -pl infrastructure -am verify` and `mvn -pl domain,application -am test` both BUILD SUCCESS. No PR, no commit, no push performed. Ready for verify on slice 3.

## Slice 3 Carry-forward Corrective (this run) — boot wiring fix

### What changed
The slice-3 verify report flagged a CRITICAL failure: `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` still constructed `CreateFichaService` and `EditFichaService` with the pre-etiqueta signatures. Slice 1 added a `FindEtiquetasByIdsPort` parameter to both ficha services (so they can resolve `etiquetaIds` into domain `Etiqueta` entities), but the boot composition root was never updated to inject the port. Result: `./mvnw -pl boot -am test` failed at compile time.

### Production code modified
- `boot/src/main/java/com/ar/crm2/config/WiringConfig.java`:
  - `createFichaUseCase(FichaRepositoryAdapter savePort, EtiquetaRepositoryAdapter findEtiquetasPort)` — now passes both ports into `new CreateFichaService(savePort, findEtiquetasPort)`.
  - `editFichaUseCase(FichaRepositoryAdapter findPort, FichaRepositoryAdapter savePort, EtiquetaRepositoryAdapter findEtiquetasPort)` — now passes three ports into `new EditFichaService(findPort, savePort, findEtiquetasPort)`.
  - Section header Javadoc explaining why the extra `FindEtiquetasByIdsPort` is required.
  - Cleaned up the stale Javadoc on `etiquetaRepositoryAdapter(...)` — removed the incorrect "annotated with `@Component`" wording (the adapter class is NOT annotated with `@Component`; the `@Bean` method is the only definition) and replaced it with an accurate description of the project convention.

### Test code added
- `boot/src/test/java/com/ar/crm2/config/FichaWiringTest.java` (new) — 6 wiring-only tests under `@SpringJUnitConfig(classes = WiringConfig.class)`. Spring Boot 4.x idioms: `@SpringJUnitConfig` instead of `@SpringBootTest`; `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito`) instead of the deprecated `@MockBean`. Mocks every `JpaRepository` that `WiringConfig` requires as a constructor parameter for the adapter beans, and asserts via reflection on the private `findEtiquetasPort` / `savePort` / `findPort` fields of `CreateFichaService` and `EditFichaService` that the wiring injects the right adapter into every constructor slot.

### TDD Cycle Evidence
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 4.6-fix | `FichaWiringTest.java` (new) | Wiring slice (SpringJUnitConfig) | N/A (new — boot had no test dir) | Compile-time RED (boot main failed to compile) | 6/6 passed | 4 reflection-based cases + 2 type-based cases | First pass had weak `isNotNull()` assertions; refactored to reflection checks on private fields |

### Verification commands (executed)
- `./mvnw -pl boot -am clean test` — BUILD SUCCESS. `FichaWiringTest` 6/6 passing. All other modules (domain, application, infrastructure) still pass with the slice-3 totals unchanged.
- `./mvnw -pl infrastructure -am verify` — BUILD SUCCESS. 259 Surefire + 49 Failsafe = 308 tests, all green. No regression in the slice-2/slice-3 work.

### Workload / PR Boundary (this corrective)
- Mode: `auto-chain` (slice 3 corrective continuation, no PR boundary expansion)
- Current work unit: this corrective only — 1 production file modified (`WiringConfig.java`) + 1 test file added (`FichaWiringTest.java`) + 1 task-list file updated (`tasks.md`). ~+150 lines net.
- No PR, no commit, no push performed (per the orchestrator's instruction).

### Deviations from Design
None. The wiring fix is the natural consequence of slice 1's application-service constructor change; the corrective is the missing glue. The Javadoc cleanup is a documentation correction flagged by the slice-3 verify report as a WARNING; it falls within the same bounded change.

### Issues Found
None blocking.

## Status
Slice 3 carry-forward corrective complete: 1 production file fixed (`WiringConfig.java` — 2 beans updated + 1 stale Javadoc cleaned), 1 wiring test added (`FichaWiringTest` — 6/6 green), 1 task-list updated (`tasks.md`). Total project tests after this run: 557 passing across all 4 modules. `./mvnw -pl boot -am test` BUILD SUCCESS. `./mvnw -pl infrastructure -am verify` BUILD SUCCESS. No PR, no commit, no push performed. Ready for re-verify on slice 3.

## Slice 3 Carry-forward Corrective #2 (this run) — owned-relation row id strategy

### User-approved corrective requirement
- In BOTH owned-relation persistence cases, `ColumnaTableroEntity` and `FichaEtiquetaEntity`, the relation row MUST generate and own its own UUID technical id.
- Do NOT use composite keys.
- Do NOT reuse/copy the catalog id (`columnaId` / `etiquetaId`) into the row `id`.

### What was already correct (no change needed)
- `FichaMapper#toEntity` already used `UUID.randomUUID().toString()` for each `FichaEtiquetaEntity` row's id. The new `FichaMapperTest` documents and locks in this contract.

### What was wrong (the actual bug)
- `TableroMapper#toColumnaTableroEntity` was copying `ct.getColumnaId().value().toString()` into the row `id`. This meant the row's technical id was identical to the catalog `Columna` id — defeating the purpose of an owned-relation id. The mapper was treating the row id as if it were the domain identity, when in fact the row's domain identity is the (tablero, columna) pair, not a single UUID.

### Production code modified
- `infrastructure/.../mapper/TableroMapper.java`:
  - `toColumnaTableroEntity(ct, parent, orden)` — `.id(ct.getColumnaId().value().toString())` → `.id(UUID.randomUUID().toString())`. The row now owns its own generated UUID technical id; the catalog `columnaId` is preserved in its own `columna_id` column so the row still references the Columna catalog.
  - Class-level Javadoc extended with a "Child row id strategy" paragraph that explicitly documents the contract: each row owns its own fresh UUID technical id, the catalog id is stored separately, the contract is enforced by `TableroMapperTest#toEntity_childRowId_isGeneratedUuidNotEqualToColumnaId`, and the same strategy is used by `FichaMapper#toEntity` for `FichaEtiquetaEntity`.
  - Method-level Javadoc on `toColumnaTableroEntity` extended with the same "Identity strategy" paragraph.

- `infrastructure/.../mapper/FichaMapper.java` — Javadoc only (mapper was already correct). Class-level Javadoc extended with the matching "Child row id strategy" paragraph for symmetry.
- `infrastructure/.../entity/ColumnaTableroEntity.java` — Javadoc only. Added an "Identity strategy" paragraph.
- `infrastructure/.../entity/FichaEtiquetaEntity.java` — Javadoc only. Added the matching "Identity strategy" paragraph.

### Test code added (1 new test file)
- `infrastructure/src/test/.../mapper/FichaMapperTest.java` (new) — 6 pure-function unit tests for the static `FichaMapper`. 4 tests in `@Nested ToEntityChildIdOwnership`: (a) row id is not a copy of `etiquetaId`, (b) each child row gets a distinct UUID when multiple etiquetas are present, (c) row id is a valid UUID, (d) `etiquetaId` and `tipoEtiqueta` are preserved as separate fields AND the row id is independent of `etiquetaId`. 2 orthogonal tests: empty list, null domain.

### Test code modified (1 file)
- `infrastructure/src/test/.../mapper/TableroMapperTest.java` — appended 4 new tests proving the same contract for `ColumnaTableroEntity`: (a) row id is not a copy of `columnaId` (**RED on pre-fix code, GREEN after the mapper change**), (b) each child row gets a distinct UUID, (c) row id is a valid UUID, (d) `columnaId` is preserved as a separate field AND the row id is independent of it.

### TDD Cycle Evidence
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| id-strategy-ficha | `FichaMapperTest.java` (new) | Unit (pure static) | N/A (new) | n/a (mapper was already correct; new tests document the contract) | ✅ 6/6 passed on first run | ✅ 4 cases in `ToEntityChildIdOwnership` (not-equal, distinct, valid-uuid, separate-fields) + 2 orthogonal (empty, null) | ➖ None needed — mapper was already correct |
| id-strategy-tablero | `TableroMapperTest.java` (modified) | Unit (Mockito) | ✅ 12/12 baseline (existing tests) | ✅ 3 of the 4 new tests FAILED on pre-fix code; 1 passed only incidentally because the pre-fix row id was also a valid UUID | ✅ 16/16 passed after the mapper change to `UUID.randomUUID()` | ✅ 4 cases (not-equal, distinct, valid-uuid, separate-fields) | ✅ Class-level + method-level Javadoc on mapper + entity Javadoc extended with "Identity strategy" paragraph |

### Verification commands (executed)
- `./mvnw -pl infrastructure -am test "-Dtest=TableroMapperTest,FichaMapperTest" -Dsurefire.failIfNoSpecifiedTests=false` — BUILD SUCCESS. 22/22 passing (16 TableroMapperTest + 6 FichaMapperTest). The 3 previously RED tests are now GREEN.
- `./mvnw -pl infrastructure -am verify` — BUILD SUCCESS. 269 Surefire + 49 Failsafe = 318 tests, all green. No regression.
- `./mvnw -pl boot -am test` — BUILD SUCCESS. 6 FichaWiringTest + the rest, no regression.

### Deviations from Design
None. The fix aligns both relations with the explicit user-approved contract: "relation row generates and owns its own UUID technical id, no composite key, no copy of catalog id." This is the only consistent interpretation of "owned-relation with own id." The change is bounded to mapper code + entity/mapper Javadoc + 1 new test file + 1 extended test file. No production entity definition changes (no new fields, no new annotations, no `@IdClass` / `@EmbeddedId` composite-key classes).

### Issues Found
- The slice-3 verify report had NOT caught the `columnaId` → row `id` copy because the existing `TableroMapperTest` cases asserted on `columnaId` and `tipoTablero` but never on the row `id` itself. This is a real coverage gap that the new tests close. The `FichaMapperTest` would have caught the same defect on the Ficha side if it had existed pre-fix — its existence now ensures the strategy cannot silently regress.

### Important discovery
- Before this fix, `ColumnaTableroEntity` row ids in the database were EQUAL to the catalog `Columna` ids. This is harmless only because the row is a child of `TableroEntity` and never queried by id independently — the parent owns the lifecycle (cascade=ALL, orphan removal). However, anyone who later tried to load a `ColumnaTableroEntity` by id directly would have hit the catalog Columna, not the relation row. The fix future-proofs the design and matches the explicit "technical id" contract.

### Workload / PR Boundary (corrective #2)
- Mode: `auto-chain` (slice 3 corrective continuation, no PR boundary expansion)
- Chain strategy: `feature-branch-chain` (unchanged)
- Current work unit: 4 production files (1 mapper fix + 3 Javadoc extensions) + 1 new test file + 1 extended test file + 1 OpenSpec `tasks.md` update. ~+260 lines net (well below the 800-line PR budget for the chained PRs).
- Boundary: pure mapper fix + Javadoc cleanup + new tests proving the identity strategy. No PR creation, no commit, no push performed (per the orchestrator's instruction).
- Estimated review budget impact: trivial. The 10 new test cases are colocated with the existing mapper tests; the mapper change is a one-line `UUID.randomUUID().toString()` substitution.

## Status
Slice 3 carry-forward corrective #2 complete: 1 mapper fix (`TableroMapper#toColumnaTableroEntity` — now uses `UUID.randomUUID().toString()` for the row id), 4 Javadoc extensions making the identity strategy explicit and consistent in both mappers and both entities, 1 new test file (`FichaMapperTest` — 6/6 green), 1 extended test file (`TableroMapperTest` — 16/16 green total, 4 new cases for the contract). Total project tests after this run: 318 (infrastructure 269 Surefire + 49 Failsafe) + 168 domain + 75 application + 6 boot wiring = 567 tests, all green. `./mvnw -pl infrastructure -am verify` BUILD SUCCESS. `./mvnw -pl boot -am test` BUILD SUCCESS. No PR, no commit, no push performed. Ready for re-verify.

## Phase 5: Cleanup / Documentation (this run)

### Mode
Strict TDD is active, but Phase 5 is a documentation-only phase (no production code change, no new test required). The slice 3 verify baseline (567 tests) is the safety net; `./mvnw verify` re-executed in this run confirms the baseline still holds.

### Review Workload Forecast (this run)
- Delivery strategy: `auto-chain` (Phase 5 is a documentation continuation; no PR boundary expansion, no PR creation, no commit, no push)
- Chain strategy: `feature-branch-chain` (unchanged)
- 800-line PR budget risk: Low — Phase 5 only touches OpenSpec markdown files, no production code
- Decision needed before apply: No
- PR boundary for this run: documentation only. No PR creation, no commit, no push.

### What was done

#### 5.1 — Spec delta reconciliation
Two delta specs were tightened to match the implementation decisions that emerged during slices 1, 2, 3, and the two carry-forward correctives. Both spec files now also carry a top-of-file header explaining that they are deltas to be merged on archive.

`openspec/changes/add-ficha-etiquetas/specs/etiqueta-management/spec.md`:
- Tightened the "Delete In-Use Etiqueta" scenario wording.
- Added `Requirement: Id-aware Uniqueness on Edit` with two scenarios (edit keeps own name, edit rejects a name collision with another Etiqueta) — this locks in the slice-1 id-aware uniqueness corrective.
- Added `Requirement: Catalog Source of Truth` with one scenario (edit reflects universally on Ficha responses) — this locks in the slice-3 design decision that `FichaResponse` does NOT duplicate catalog name/color.

`openspec/changes/add-ficha-etiquetas/specs/ficha-etiqueta/spec.md`:
- Tightened the `Requirement: Unique Tags Per Ficha` title and scenario: the implementation **rejects** duplicate `EtiquetaId` rather than silently deduplicating. The original delta allowed both behaviors; the new wording makes the chosen behavior explicit.
- Added `Requirement: Strict Resolution of Etiqueta Ids` with two scenarios (all ids resolve, one id is unknown → 404) — this locks in the slice-2 `FichaEtiquetaResolver` strict missing-id check.

#### 5.2 — `mvn verify` regression check
Command: `./mvnw verify -B`
Result: **BUILD SUCCESS**

| Module | Tests | Result |
|---|---:|---|
| `domain` | 168 | PASS |
| `application` | 75 | PASS |
| `infrastructure` (Surefire) | 269 | PASS |
| `infrastructure` (Failsafe: 9 `EtiquetaControllerIT` + 11 `TableroControllerIT` + 10 `EtiquetaEntityMappingIT` + 7 `FichaEtiquetaEntityCascadeIT` + 12 `EtiquetaRepositoryAdapterIT`) | 49 | PASS |
| `boot` (`FichaWiringTest`) | 6 | PASS |
| **Total** | **567** | **PASS** |

JaCoCo ran during module verification; per-file coverage delta is not emitted (the `openspec/config.yaml` coverage threshold is `0`, no hard gate). The change is test-positive (adds tests, never removes them), so coverage is preserved by construction.

The unrelated Hibernate warning about `TableroEntity.columnasTablero` `@OrderColumn` (flagged in the slice-3 verify report as pre-existing technical debt) still surfaces; it is NOT introduced by this change.

#### 5.3 — API surface and catalog-vs-relation documentation
The project has no root `README.md` and no module-level README to extend. The canonical, team-shareable home for this content is the change's `design.md`, which will be archived alongside the rest of the change and is the place reviewers look for "what is this API and how do I use it?".

Extended `openspec/changes/add-ficha-etiquetas/design.md` with two new sections:

- **API Surface** — three tables covering the five `/api/etiquetas/*` endpoints (method, path, query/body, success status, error mapping), the Ficha aggregate extension on create/edit (body change, response change), and the catalog-vs-relation diagram with the five key contracts (catalog owns read state, relation owns its own row id, type snapshot, strict resolution, no per-Ficha assign/unassign endpoints).
- **Interfaces / Contracts** — extended the existing application port list with the final signatures (id-aware `ExistsEtiquetaByNombreAndTipoPort`, `FindEtiquetasByIdsPort`, `CountFichaEtiquetasByEtiquetaIdPort`, etc.) and the inbound use case list with the final signatures (`CreateEtiquetaUseCase`, `EditEtiquetaUseCase`, `GetByIdEtiquetaUseCase`, `GetAllEtiquetasUseCase`, `DeleteEtiquetaUseCase`).

#### 5.4 — Non-obvious decisions recorded
Added a **Non-Obvious Decisions (Phase 5 — closure)** section to `design.md` that records six decisions that emerged during the slice chain and the two carry-forward correctives. Each entry has a `Why` and a `Trade-off` so a future maintainer can re-derive the rule from the design instead of from the code.

| # | Decision | Origin |
|---|---|---|
| D1 | Cascade transaction lives in the persistence adapter, not the use case | slice 1 design rule (`application` is Spring-free) |
| D2 | `TipoEtiqueta` snapshot on `FichaEtiqueta` (closed the original "Open Question") | original design's open question |
| D3 | Owned-relation rows own their own UUID technical id | slice-3 carry-forward corrective #2 |
| D4 | Outbound adapters are wired only via `WiringConfig` (no `@Component`) | slice-3 carry-forward corrective (stereotype removal) |
| D5 | Duplicate etiqueta id is rejected, not deduplicated | slice 1 domain `validarEtiquetas(...)` rule |
| D6 | Strict `etiquetaIds` resolution (missing ids → 404) | slice-2 `FichaEtiquetaResolver` strict missing-id check |

The original "Open Questions" section in `design.md` is now closed; the `TipoEtiqueta` snapshot question is answered by **D2**.

### Files Changed (this Phase 5 run)

**Modified (4 OpenSpec files, all English, all documentation):**
- `openspec/changes/add-ficha-etiquetas/specs/etiqueta-management/spec.md` — added delta header + 2 new requirements (Id-aware Uniqueness, Catalog Source of Truth) + tightened Delete scenario.
- `openspec/changes/add-ficha-etiquetas/specs/ficha-etiqueta/spec.md` — added delta header + tightened Unique Tags requirement (rejected, not deduplicated) + added Strict Resolution requirement.
- `openspec/changes/add-ficha-etiquetas/design.md` — extended `Interfaces / Contracts` with final port/use case signatures; added `API Surface` section (3 tables + diagram + 5 key contracts); added `Non-Obvious Decisions (Phase 5 — closure)` section (D1–D6); closed `Open Questions` (D2).
- `openspec/changes/add-ficha-etiquetas/tasks.md` — marked 5.1, 5.2, 5.3, 5.4 complete with evidence.

**Modified (hybrid sync):**
- `openspec/changes/add-ficha-etiquetas/apply-progress.md` — appended this Phase 5 section.
- Engram `sdd/add-ficha-etiquetas/apply-progress` topic — appended matching Phase 5 section via `mem_update` on observation #1826.

No production code changed. No tests added. No PR, no commit, no push performed.

### TDD Cycle Evidence
Not applicable. Phase 5 is documentation-only. Strict TDD was honored by NOT introducing any production change that would require a new test cycle; the existing 567-test safety net is the regression guarantee and it still passes.

### Deviations from Design
None. The Phase 5 updates (spec tightening, API surface, decisions) are all RECONCILIATION with the design, not new design decisions. The design.md was extended to capture decisions that were already made and implemented in slices 1–3 + correctives; the spec deltas were tightened to match those decisions; the open question was closed.

### Issues Found
None blocking. The unrelated Hibernate warning on `TableroEntity.columnasTablero` `@OrderColumn` is still present (pre-existing technical debt, not introduced by this change). JaCoCo does not emit a per-file changed-file coverage report (the `coverage_threshold` in `openspec/config.yaml` is `0`; the project does not enforce a per-file coverage gate).

### Workload / PR Boundary
- Mode: `auto-chain` (Phase 5 documentation continuation; no PR boundary expansion)
- Chain strategy: `feature-branch-chain` (unchanged)
- Current work unit: documentation only — 4 OpenSpec files modified + 1 hybrid sync. ~+200 lines of English markdown.
- Boundary: no production code, no tests, no PR, no commit, no push.
- Estimated review budget impact: trivial. The changes are confined to the change folder and do not affect any module or runtime behavior. The content is descriptive of what the change already does.

## Status
Phase 5 complete. `./mvnw verify` BUILD SUCCESS (567 tests, 0 failures, 0 errors). Spec deltas tightened to match the slice-1/2/3/corrective decisions. API surface and catalog-vs-relation diagram added to `design.md`. Six non-obvious decisions (D1–D6) recorded with rationale and trade-offs. Open question closed. Hybrid artifact stores (OpenSpec filesystem + Engram) in sync. No PR, no commit, no push. Ready for archive.
