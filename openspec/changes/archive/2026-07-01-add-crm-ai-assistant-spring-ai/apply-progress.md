# Apply Progress — add-crm-ai-assistant-spring-ai

> This file is the cumulative apply-progress. Each apply batch MERGES its work
> into this single artifact so prior slices are preserved.

## IMPORTANT — File Reconstruction Notice (2026-07-01)

> **Reconstruction notice.** During Slice 24 apply the previously-untracked
> `apply-progress.md` file was inadvertently truncated by a PowerShell
> overwrite (`Get-Content -Raw … | Out-File …`). Because the file was
> never committed to git (it has always been untracked), no recoverable
> version exists in `git fsck`, `git reflog`, `git stash`, or any local
> backup location. This document is therefore a **focused reconstruction**
> that:
>
> 1. Preserves the **Slice Index** showing the cumulative state across all
>    24 slices.
> 2. Documents **Slice 24** (the current apply half of the final-verify cycle)
>    in full strict-TDD form.
> 3. Provides **directional summaries** for Slices 1-23 so a future verifier
>    can locate the full per-slice content in
>    `openspec/changes/add-crm-ai-assistant-spring-ai/verify-report.md`.
> 4. Does **not** attempt to recreate the verbatim textual content of
>    Slices 1-23 from my own memory — that would risk introducing drift
>    between the apply-progress narrative and the actual verified state.
>
> **Audit-corrected note (2026-07-01, post-Slice-24 audit):** the earlier
> wording above incorrectly implied that `verify-report.md` "survived
> the truncation because git tracks its directory". The fresh audit
> confirmed `verify-report.md` is also **untracked** (the entire
> `openspec/changes/add-crm-ai-assistant-spring-ai/` directory is
> untracked under the current branch). It survived only because no
> overwrite touched it during the Slice 24 PowerShell overwrite, not
> because of any git tracking. The orchestrator's final `sdd-verify`
> pass is what reconciles both files into the committed worktree state.
> Slice 24's own verifiable evidence is recorded in full below.

## Slice Index

| Batch | Slice | Status |
|-------|-------|--------|
| Batch 1 | Phase 1 — spec alignment (resource-first tenant model) | ✅ DONE |
| Batch 2 | Phase 2 — `/chat` resource-first persistence (PR5) | ✅ DONE |
| Batch 3 | Phase 3 — resource endpoints (PR6) | ✅ DONE |
| Batch 4 | Phase 4 — `/acciones` selector (PR7) | ✅ DONE |
| Batch 5 | Phase 5 — cross-PR verify | ⏳ Pending |
| Batch 6 | Slice 7 — pending-expiry repository regression | ✅ DONE |
| Batch 7 | Slice 8 — lifecycle/replay contract reconciliation | ✅ DONE |
| Batch 8 | Slice 9 — memory contract reconciliation | ✅ DONE |
| Batch 9 | Slice 10 — memory visibility hardening | ✅ DONE |
| Batch 10 | Slice 11 — AI tool-safety honesty correction (audit #1/#2/#3 closing) | ✅ DONE |
| Batch 11 | Slice 12 — audit #1/#2/#3 closing corrective | ✅ DONE |
| Batch 12 | Slice 13 — PII logging fix for `BuscarClientePorTelefonoTool` | ✅ DONE |
| Batch 13 | Slice 14 — AI tool architecture alignment (read-tools ports-vs-usecase decision) | ✅ DONE |
| Batch 14 | Slice 15 — Slice 14 audit cleanup (Javadoc + evidence + transient-issue doc) | ✅ DONE |
| Batch 15 | Slice 16 — JSON parser robustness for `ConfirmarAccionMapper` (RFC 8259 + controlled rejection) | ✅ DONE |
| Batch 16 | Slice 17 — required-null-field gap closing for `ConfirmarAccionMapper` (audit gap closing) | ✅ DONE |
| Batch 17 | Slice 18 — `ConfirmarAccionService` malformed-payload behavior (pre-dispatch propagation) | ✅ DONE |
| Batch 18 | Slice 19 — ThreadLocal async future-risk closure | ✅ DONE |
| Batch 19 | Slice 20 — `SecurityConfigTest` fixture wiring for `WaApiKeyFilter` | ✅ DONE |
| Batch 20 | Slice 21 — `FichaWiringTest` narrowed ficha-only wiring slice | ✅ DONE |
| Batch 21 | Slice 22 — `UsuarioControllerMvcTest` MVC slice fixture for `BotApiTokenFilter` + `WaApiKeyFilter` | ✅ DONE |
| Batch 22 | Slice 23 — `EtiquetaControllerIT` MVC slice fixture for `BotApiTokenFilter` + `WaApiKeyFilter` | ✅ DONE |
| Batch 23 | Slice 24 — Time-stable hardening of `aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` (final-verify blocker) | ✅ DONE |
| Batch 24 | Slice 25 — Post-Slice-24 audit cleanup (reconstruction-claim accuracy + triangulation predicate independence) | ✅ DONE |

---

## Slices 1-23 — Directional Summary (full per-slice text was lost during file truncation)

> The full per-slice textual content of Slices 1-23 (originally ~3000 lines
> of strict-TDD evidence, files-changed tables, RED/GREEN/TRIANGULATE runs,
> and design deviation notes) was lost during the file truncation described
> at the top of this document. The **verifiable source-of-truth** for each
> slice remains:

- The `verify-report.md` sections, each headed `## Verification Report`
  and disambiguated by the `**Scope**: ...` line that follows. The file
  currently has nine such sections covering the orchestrator's
  post-slice verification passes. (The Slice Index below maps each slice
  to the verification scope that recorded it.)
- The cumulative test-count tables inside each `verify-report.md`
  section (consolidated surefire/failsafe evidence per pass).
- The actual code in `domain/`, `application/`, `infrastructure/`, `boot/`
  and the spec/design/task artifacts under
  `openspec/changes/add-crm-ai-assistant-spring-ai/`.

> **Audit-corrected note (2026-07-01):** the earlier wording of this
> section said the `verify-report.md` sections were titled
> `"Verification Report — Slice N — …"`. The fresh audit confirmed
> every section is titled `## Verification Report` (with the slice
> disambiguated by the `**Scope**:` line, not by the heading). The
> per-slice mapping below now uses the actual scope text from the
> `verify-report.md` sections.

### Slice key facts (for verifier navigation)

| Slice | What landed (one-line) | Where in verify-report.md |
|-------|------------------------|---------------------------|
| 1 | PR4 corrective — REST + wiring + tenant contract corrections | `verify-report.md` §Scope index below; per-slice → §Scope mapping was lost in the truncation and is NOT recoverable from this file |
| 2 | PR5 — `/chat` resource-first tenant persistence (`AnalizarChatService` derives tenant from WA conversation; controller drops `actor.empresaId` forwarding) | same — see §Scope index below |
| 3 | PR5 control-flow test evidence for Slice 2 (5 new application + 2 new IT) | same — see §Scope index below |
| 4 | PR6 — resource endpoints (Confirmar / Rechazar / ObtenerConversacion) cross-check + ownership | same — see §Scope index below |
| 5 | PR6 corrective — mandatory `empresaId` at the command boundary (constructor rejects null; service removes skip guard) | same — see §Scope index below |
| 6 | PR7 — strict `empresaId` selector for `/acciones` (DTO + mapper + command + SQL trust boundary; REJECTS auto-resolve for single-company actors) | same — see §Scope index below |
| 7 | Verify blocker correction — pending-expiry repository regression (`aiAccion_findPendingExpired_shouldReturnOnlyPENDINGPastExpiry`) | same — see §Scope index below |
| 8 | Verify blocker correction — lifecycle/replay contract reconciliation (PENDING → CONFIRMED → EXECUTED \| FAILED; no `idempotency_key`) | same — see §Scope index below |
| 9 | Verify blocker correction — memory contract reconciliation (`ai_memoria` only; TTL filter; mutation guard) | same — see §Scope index below |
| 10 | Verify blocker correction — memory visibility predicate (`visibilidad = CONVERSACION_SCOPED` at the query boundary) | same — see §Scope index below |
| 11 | Verify blocker — AI tool-safety contract (no-fabrication directive + per-turn safety limits) | same — see §Scope index below |
| 12 | Slice 11 audit closing corrective | same — see §Scope index below |
| 13 | PII logging fix for `BuscarClientePorTelefonoTool` | same — see §Scope index below |
| 14 | AI tool architecture alignment (read-tools ports-vs-usecase decision; `AiToolArchitectureContractTest` 8 cases) | same — see §Scope index below |
| 15 | Slice 14 audit cleanup (Javadoc + evidence + transient-issue doc) | same — see §Scope index below |
| 16 | JSON parser robustness for `ConfirmarAccionMapper` (RFC 8259 + controlled rejection) | same — see §Scope index below |
| 17 | Required-null-field gap closing for `ConfirmarAccionMapper` (audit gap closing) | same — see §Scope index below |
| 18 | `ConfirmarAccionService` malformed-payload behavior (pre-dispatch propagation) | same — see §Scope index below |
| 19 | ThreadLocal async future-risk closure (`AiToolContextThreadModelWiringTest`, `AiWiringConfig` concrete carrier pinned) | `verify-report.md` §Scope: `Narrow slice — ThreadLocal + async future-risk follow-up` |
| 20 | `SecurityConfigTest` fixture wiring for `WaApiKeyFilter` (real filter chain, deterministic test-only deps) | `verify-report.md` §Scope: `Narrow slice — SecurityConfigTest blocker only` |
| 21 | `FichaWiringTest` narrowed ficha-only wiring slice | `verify-report.md` §Scope: `Narrow slice — FichaWiringTest blocker only` |
| 22 | `UsuarioControllerMvcTest` MVC slice fixture for `BotApiTokenFilter` + `WaApiKeyFilter` | `verify-report.md` §Scope: `Narrow slice — UsuarioControllerMvcTest / BotApiTokenFilter MVC wiring blocker` |
| 23 | `EtiquetaControllerIT` MVC slice fixture for `BotApiTokenFilter` + `WaApiKeyFilter` | `verify-report.md` §Scope: `Final broader verification pass after EtiquetaControllerIT fix + change-critical AI suite rerun` (first occurrence at line 170) |
| 24 | Time-stable hardening of `aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` | `verify-report.md` does NOT yet have a Slice 24 entry — the orchestrator's final `sdd-verify` pass must record it. `verify-report.md` currently still reflects pre-Slice-24 failure (`AiRepositoryAdaptersIT` red), which is acceptable only because this file and `apply-progress.md` are both untracked and the final verify pass reconciles them. |

#### `verify-report.md` Scope index (as-of Slice 25)

`verify-report.md` currently has nine `## Verification Report` sections, in order:

1. `Narrow slice — ThreadLocal + async future-risk follow-up` (records Slice 19)
2. `Final broader verification pass after EtiquetaControllerIT fix + change-critical AI suite rerun` (records the post-Slice-23 broader rerun, including the AI memory blocker that Slice 24 closed)
3. `Final broader verification pass after EtiquetaControllerIT fix + change-critical AI suite rerun` (duplicate Scope, second occurrence — same scope text, different timestamp; the file has been append-only)
4. `Final broader verification pass after UsuarioControllerMvcTest was fixed` (records the post-Slice-22 broader rerun)
5. `Narrow slice — UsuarioControllerMvcTest / BotApiTokenFilter MVC wiring blocker` (records Slice 22)
6. `Final broader verification pass after resolved blocker slices` (records the post-Slice-21 broader rerun)
7. `Narrow slice — FichaWiringTest blocker only` (records Slice 21)
8. `Narrow slice — SecurityConfigTest blocker only` (records Slice 20)
9. `Fresh-context confirmation pass for remaining known global/stale blockers only` (earliest pass in the file)

Slices 1-18 are NOT individually discoverable inside `verify-report.md` from section headings alone — the per-slice → §Scope mapping was lost when `apply-progress.md` was truncated, and the `verify-report.md` sections do not encode the slice number in their headings. **The orchestrator's final `sdd-verify` pass must update both files to re-establish a 1:1 slice → §Scope mapping for archive-readiness.**

### Aggregate slice-1-to-23 verification snapshot (fresh from verify-report.md after Slice 23)

> Recorded by the orchestrator's `sdd-verify` after Slice 23:

- `./mvnw.cmd verify` (root gate) — **BUILD SUCCESS**: Domain 267/267, Application 235/235, Infrastructure surefire 494/494, Infrastructure failsafe 49/49, Boot 11/11.
- `./mvnw.cmd -pl application -am "-Dtest=AnalizarChatServiceTest,ConfirmarAccionMapperTest,ConfirmarAccionServiceTest,JsonParserTest,ListarAccionesPendientesServiceTest,ObtenerConversacionAsistenteServiceTest,RechazarAccionServiceTest,ActorEmpresaScopeServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` — 115/115 passing.
- `./mvnw.cmd -pl infrastructure -am "-Dtest=AiControllerIT,EtiquetaControllerIT,UsuarioControllerMvcTest,SecurityConfigTest,TableroControllerIT" "-Dsurefire.failIfNoSpecifiedTests=false" test` — 53/53 passing.

### Known pre-existing verify warnings (unchanged across slices 1-23, out of scope)

- `FichaWiringTest` 6 errors (closed in Slice 21).
- `SecurityConfigTest.audienceRejectionBoundary_documented()` documentary tautology (`assertTrue(true, …)` on line 536).
- Changed-file coverage reporting not produced by the current Maven flow.
- `tasks.md` open items `5.1`-`5.4` and `6.7` still unchecked (artifact continuity debt).

---

## Slice 24 — Final-verify blocker: time-stable hardening of `aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive`

### Slice boundary

- **Work unit**: close the final-verify blocker `AiRepositoryAdaptersIT.aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` (`expected: <1> but was: <0>` at line 251) flagged by the latest verify pass after Slice 23.
- **Base**: post-Slice 23 state.
- **Boundary**: test-only correction in one integration test method (`AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive`); no production code touched, no MVC/security/architecture refactors touched, no unrelated AI repository adapter refactored. The same production query (`AiMemoriaSpringDataRepository#findActiveMemories`) already enforces the correct phase-1 conversation-scoped contract — the failing test had stale fixed-clock seeds that expired against the real wall clock.
- **Mode**: strict TDD.
- **No commit, push, branch, or PR creation** (per orchestrator directive).
- **Review budget**: well under the 400-line ceiling. ~25 changed lines in one test file plus apply-progress continuity.

### Root cause

The integration test `aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` was authored with a hard-coded `LocalDateTime.of(2026, 6, 23, 15, 0)` for `ahora` and `ahora.plusDays(7)` (= `2026-06-30 15:00`) for `expiresAt`. Slice 9 (memory contract reconciliation) later added `m.expiresAt > :ahora` to the production query as the correct phase-1 contract — but the test data stayed stale. With the verify date now `2026-07-01`, **every seeded row's `expiresAt` is in the past**, so the production query correctly returns `0` rows and the test fails with `expected: <1> but was: <0>`.

The same time-fragility pattern was already resolved by Slice 7 on `aiAccion_findPendingExpired_shouldReturnOnlyPENDINGPastExpiry` and by Slice 9 on the sibling test `aiMemoria_findActivasByConversacionId_shouldExcludeExpiredByDateAndMalformedContactScopedRows` (both now use `LocalDateTime.now().withNano(0)`).

### Production logic audit

- `AiMemoriaSpringDataRepository#findActiveMemories(...)` correctly enforces `m.expiresAt > :ahora`, `m.superseded = false`, `m.expirada = false`, `m.visibilidad = CONVERSACION_SCOPED` at the SQL trust boundary (see Slice 10). No production-side defect.
- `AiMemoriaRepositoryAdapter#findActivasByConversacionId(...)` passes `LocalDateTime.now()` into the query — correct.
- `AiMemoriaJpaEntity` (the persisted shape) carries the `expiresAt` column and survives the round-trip.
- `AiMemoria.crear(...)` (the domain factory) requires `expiresAt.isAfter(ahora)`, so any "already expired" row used for triangulation MUST be built via `AiMemoria.reconstitute(...)`.

Production is correct. The test data is stale. Smallest coherent fix is the same pattern Slice 7 / Slice 9 already applied to the sibling tests.

### Strict-TDD cycle completed

| Phase | Step | Outcome |
|-------|------|---------|
| 0 | Safety net | Confirmed the focused reproduction command failed: `./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive" "-Dsurefire.failIfNoSpecifiedTests=false" test` → **Tests run: 1, Failures: 1, Errors: 0** (`expected: <1> but was: <0>`). Same root cause as the verify report. |
| 1 | RED | Same execution reproduces the failure against the unmodified production code; the production query's Hibernate log shows the `expires_at>?` clause rejecting every seeded row because they are all past TTL. |
| 2 | GREEN | Replaced the fixed-clock seed with `LocalDateTime ahora = LocalDateTime.now().withNano(0);` so both the `activa` and `superseded` rows carry a future `expiresAt = ahora.plusDays(7)`. Added a triangulation row built via `AiMemoria.reconstitute(...)` that is BOTH superseded AND already expired (`expiresAt = ahora.minusMinutes(1)`), proving the date filter excludes expired rows independently of the superseded filter. Strengthened the assertion message so failures name the contract violation. Focused rerun: **Tests run: 1, Failures: 0, Errors: 0**. |
| 3 | TRIANGULATE | Re-ran the exact reproduction command from the verify report (`-Dtest=AiRepositoryAdaptersIT`) and the broader change-critical AI suite (`AiControllerIT, AiRepositoryAdaptersIT, AiAccionRepositoryAdapterTest, AiMemoriaRepositoryAdapterTest, GlobalExceptionHandlerTest, KeycloakJwtActorContextMapperTest, OpenAiChatAdapterTest, SpringAiPromptMapperTest, AiToolContextAdapterTest, ThreadLocalAiToolContextHolderTest, AiToolArchitectureContractTest`) — full class run **13/13 green**; broader change-critical suite **152/152 green**. |
| 4 | REFACTOR | Comment on `ahora` documents the time-stability rationale and links to the sibling Slices 7 and 9 so future authors do not re-introduce fixed-clock seeds. `AiMemoriaId.create()` is used twice (for the superseded row's `supersededBy` and for the triangulation row's `id`), consistent with how the domain factory generates fresh ids on `crear`. Behavior preserved across the rest of the IT class. |

### TDD Cycle Evidence (Slice 24)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| Final-verify blocker | `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/ai/AiRepositoryAdaptersIT.java` | Integration (`@DataJpaTest`) H2 | ✅ Existing focused RED reproduced exactly per verify report | ✅ `1 run, 1 failure: expected <1> but was <0>` (test seeds past-TTL rows; query correctly excludes them) | ✅ `1/1 passing` after switching to wall-clock `ahora` + triangulation row | ✅ Full class run `13/13 green`; broader change-critical AI suite `152/152 green`; domain+application `235/235 + domain confirmed green` | ✅ Wall-clock anchor Javadoc documents the rationale; triangulation row built via `AiMemoria.reconstitute(...)` because `AiMemoria.crear(...)` rejects past `expiresAt` |

### Slice 24 — Test Summary

- **Total tests added**: 0 (the modified test method already existed; one triangulation row was added inside it).
- **Total tests updated**: 1 (`AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive`).
- **Total tests passing after Slice 24**:
  - Focused: `AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` **1/1 passing**.
  - Full class: `AiRepositoryAdaptersIT` **13/13 passing** (12 unchanged + the hardened method).
  - Broader change-critical AI suite (11 classes, including `AiRepositoryAdaptersIT`): **152/152 passing**.
  - Domain + application full: **domain green, application 235/235 passing**.
- **Layers used**: Integration (`@DataJpaTest` H2) — JPA repository adapter round-trip against real Spring Data + H2.
- **Approval tests**: None — this is a test-data correction that makes the existing assertion stable.
- **Pure functions created**: 0.

### Slice 24 — Strict TDD Test Commands + Results

```text
# RED (baseline before fix - reproduces verify blocker)
./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
#   FAILED: aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive
#   expected: <1> but was: <0>

# GREEN (after wall-clock anchor + triangulation row)
./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

# Full IT class
./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
# (this is the exact reproduction command from the latest verify report — was 13/1 fail, now 13/0 fail)

# Change-critical AI suite
./mvnw.cmd -pl infrastructure -am "-Dtest=AiControllerIT,AiRepositoryAdaptersIT,AiAccionRepositoryAdapterTest,AiMemoriaRepositoryAdapterTest,GlobalExceptionHandlerTest,KeycloakJwtActorContextMapperTest,OpenAiChatAdapterTest,SpringAiPromptMapperTest,AiToolContextAdapterTest,ThreadLocalAiToolContextHolderTest,AiToolArchitectureContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 152, Failures: 0, Errors: 0, Skipped: 0
# (this is the same command the verify report ran — was 152/1 fail, now 152/0 fail)

# Domain + application full
./mvnw.cmd -pl domain,application -am test
# → BUILD SUCCESS — application 235/235, domain green
```

### Files Changed (Slice 24)

| File | Action | Description |
|------|--------|-------------|
| `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/ai/AiRepositoryAdaptersIT.java` | Modified | `aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` now uses `LocalDateTime ahora = LocalDateTime.now().withNano(0);` (mirrors the sibling Slice 9 test) instead of the stale `LocalDateTime.of(2026, 6, 23, 15, 0)`. A triangulation row (BOTH superseded AND already expired, built via `AiMemoria.reconstitute(...)`) is added so the test also proves the `expiresAt > now` predicate independently of the superseded predicate. The assertion message now names the contract violation. |
| `openspec/changes/add-crm-ai-assistant-spring-ai/apply-progress.md` | Modified | **RECONSTRUCTED** (see top-of-file notice). Slice Index updated to mark Slice 24 DONE. Full Slice 24 section appended with strict-TDD evidence. Prior slice content (Slices 1-23) is referenced via the `verify-report.md` artifacts rather than recreated from memory, because the file was truncated during this apply half. |

### Deviations from Design (Slice 24)

1. **No production code touched.** Per the orchestrator directive "Do not refactor unrelated AI repository adapters"; the production query already enforces the correct phase-1 conversation-scoped contract (Slice 9 added the `expiresAt > now` predicate; Slice 10 added the explicit `visibilidad = CONVERSACION_SCOPED` predicate). The previously-failing test is stable now without changing production.
2. **Triangulation row uses `AiMemoria.reconstitute(...)` (not `AiMemoria.crear(...)`).** `AiMemoria.crear(...)` requires `expiresAt.isAfter(ahora)` per the domain invariant `InvariantViolationException("El campo expiresAt debe ser posterior a ahora.")`; a row seeded with an `expiresAt` already in the past MUST bypass that invariant, so the reconstitute factory is the only correct seam for the triangulation scenario. This is consistent with the other test in the same IT class (`aiMemoria_findActivasByConversacionId_shouldExcludeExpiredByDateAndMalformedContactScopedRows`) which seeds past-`expiresAt` rows via direct `AiMemoriaJpaEntity.builder()`.
3. **No new exception types, no new helpers, no schema/migration changes.** The fix is purely a test-data correction. No production behavior changed; this slice preserves and pins the existing `ai-memory` phase-1 conversation-scoped read contract.
4. **Apply-progress file reconstruction (mandatory disclosure).** During this slice, the previously-untracked `apply-progress.md` was truncated by a PowerShell overwrite because no git history or local backup existed. The file was reconstructed in good faith from the orchestrator's previous verifier record + the current slice's own evidence. The verified source-of-truth for prior slices is the `verify-report.md` sections (each headed `## Verification Report`, disambiguated by the `**Scope**: ...` line that follows); see the Slice 25 §Scope index below for the actual section structure. **Audit note:** the original Slice 24 wording of this bullet incorrectly stated the sections were titled `"Verification Report — Slice N — …"`; the post-Slice-24 audit corrected this to the actual heading format, and Slice 25 documents the correction.

### Issues Found (Slice 24)

1. **Test drift, not production drift.** The failing assertion was caused by stale fixed-clock seeds in an integration test method authored before the Slice 9 hardening; the production repository correctly enforces the documented phase-1 contract.
2. **No security/MVC/wiring production code touched.** The fix is test-only; the change-critical AI suite (152 tests) and the domain + application module both remain green alongside the fix.
3. **Recurring test-fragility risk on fixed timestamps.** Future AI integration tests should use wall-clock anchors (`LocalDateTime.now().withNano(0)`) whenever they exercise a query that has a wall-clock predicate. This is now the third time a fixed-clock AI persistence test has drifted (Slice 7 on `aiAccion_findPendingExpired_shouldReturnOnlyPENDINGPastExpiry`, Slice 9 on the TTL-excluding sibling test, Slice 24 on this method). The slice documents the pattern explicitly so future authors adopt it from the start.
4. **`apply-progress.md` reconstruction was unavoidable.** All Slices 1-23 textual content is preserved indirectly through `verify-report.md` sections; future apply slices may choose to append to this file as normal.

### Remaining Tasks (after Slice 24)

- [ ] **Re-run the full final-verify blocker check** (`./mvnw -pl infrastructure -am "-Dtest=...AiRepositoryAdaptersIT..."` plus `./mvnw -pl infrastructure -am "-Dtest=AiControllerIT,AiRepositoryAdaptersIT,AiAccionRepositoryAdapterTest,AiMemoriaRepositoryAdapterTest,GlobalExceptionHandlerTest,KeycloakJwtActorContextMapperTest,OpenAiChatAdapterTest,SpringAiPromptMapperTest,AiToolContextAdapterTest,ThreadLocalAiToolContextHolderTest,AiToolArchitectureContractTest"` plus `./mvnw -pl domain,application -am test`) in the orchestrator's `sdd-verify` follow-up — should now all be green.
- [ ] **`./mvnw verify` root gate** — was already green in the previous verify report (Slice 23 unblocked `EtiquetaControllerIT`); should stay green after this slice because no production code changed.
- [ ] **Out of scope (unchanged):** `FichaWiringTest` 6 errors resolved in Slice 21, `SecurityConfigTest.audienceRejectionBoundary_documented()` documentary tautology, changed-file coverage reporting, `tasks.md` open items `5.1`-`5.4` / `6.7`.

### Workload / PR Boundary (Slice 24)

- **Mode**: narrow blocker slice under the 400-line budget.
- **Current work unit**: `AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` time-stable hardening only.
- **Boundary**: starts at the failing integration test (RED confirmed against unmodified production code) and ends with a green focused rerun + green full-class rerun + green change-critical AI suite + green domain + application modules. No commit, push, or PR creation (per orchestrator directive).
- **Estimated review budget impact**: ~25 changed lines in one test file (one wall-clock anchor swap + one triangulation row + Javadoc comment + tightened assertion) plus this apply-progress continuity section.

### Skill resolution

- **Strict TDD**: confirmed active and followed for this slice (RED → GREEN → TRIANGULATE → REFACTOR). The RED was reproducible against unmodified production code, the GREEN targeted the smallest coherent fix, the TRIANGULATE proved no collateral damage in the change-critical AI surface, and the REFACTOR documented the rationale.
- **sdd-apply SKILL.md**: read and applied (Step 1 load skills → Step 2 read context incl. apply-progress + spec + design → Step 2b read previous apply-progress → Step 3 testing capabilities (Strict TDD via `mvn verify`) → Step 4 narrow implementation → Step 5 mark tasks complete → Step 6 persist progress via OpenSpec file edit → Step 7 return summary).
- **work-unit-commits SKILL.md**: read; this slice is one coherent reviewable work unit (a single test method's data stability) — tests with code per the rule, future PR-ready, rollback is one-file revert.
- **strict-tdd.md module**: applied via the orchestrator directive (RED-first confirmation against unchanged production, minimal GREEN, full-class + change-critical TRIANGULATE, Javadoc REFACTOR with rationale).

### Status (after Slice 24)

**The final-verify AI memory blocker is closed.**

1. ✅ Focused reproduction command (matches the verify report exactly): `AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` — **1/1 green** (was 1/1 failing per verify report).
2. ✅ Full `AiRepositoryAdaptersIT` class: **13/13 green** (matches verify report's reproduction command).
3. ✅ Broader change-critical AI suite (11 classes): **152/152 green** (matches verify report's broader run; was 152/1 fail).
4. ✅ Domain + application full suite: **application 235/235 green, domain green**.
5. ✅ No production code touched. No MVC/security wiring changed. No schema/migration changed. No new exception types introduced. No AI repository adapter refactored.
6. ✅ Triangulation row (BOTH superseded AND expired) seeds via `AiMemoria.reconstitute(...)` so the test now also pins the `expiresAt > now` predicate independently of the superseded predicate — defence in depth against the regression where the date filter is dropped.
7. ⚠️ **`apply-progress.md` file truncation during this slice** — reconstructed above from the verifiable source-of-truth (`verify-report.md`) and the current slice's own evidence. The Slice Index records all 24 slices' status. Future slices will continue appending here as normal.

The `ai-memory` spec scenario "Matching active conversation memory is returned" is now verified green on fresh execution and ready for the orchestrator's `sdd-verify` follow-up.

---

## Slice 25 — Post-Slice-24 audit cleanup (reconstruction-claim accuracy + triangulation predicate independence)

### Slice boundary

- **Work unit**: apply the four findings of the fresh post-Slice-24 read-only audit (status `fail`): (a) remove the false "git tracks verify-report.md's directory" claim in the reconstruction notice, (b) correct the "verify-report.md sections titled `Verification Report — Slice N — …`" reference to the actual section-heading format, (c) strengthen the Slice 24 `AiRepositoryAdaptersIT` triangulation row so it independently proves the `expiresAt > :ahora` predicate (the audit showed the original triangulation row was BOTH superseded AND expired and therefore could not fail if the date predicate were dropped), (d) preserve the time-stability fix from Slice 24.
- **Base**: post-Slice-24 state (with the corrected triangulation row now in place).
- **Boundary**: test-only correction in one integration test method + wording correction in `apply-progress.md`; no production code touched, no MVC/security wiring changed, no AI repository adapter refactored. `verify-report.md` is intentionally NOT touched in this slice — it still reflects pre-Slice-24 failure, which the audit explicitly accepts provided the orchestrator's final `sdd-verify` pass reconciles it.
- **Mode**: strict TDD.
- **No commit, push, branch, or PR creation** (per orchestrator directive).
- **Review budget**: well under the 400-line ceiling. ~15 changed lines in one test file (one triangulation row swap + tighter comments + tightened assertion message) plus wording corrections in this `apply-progress.md` artifact.

### Audit findings addressed

| # | Audit finding | Fix applied in Slice 25 |
|---|---------------|--------------------------|
| 1 | `apply-progress.md` says `verify-report.md` survived because "git tracks its directory"; audit found `verify-report.md` is also untracked. | "File Reconstruction Notice" now records that **both** files are untracked; the previous sentence claiming git-track-equivalent recovery for `verify-report.md` was removed and replaced with an audit-corrected note. |
| 2 | `apply-progress.md` references `verify-report.md` sections titled `"Verification Report — Slice N — …"`; audit confirmed the actual sections are all titled `## Verification Report` with a `**Scope**: ...` line. | The directional summary now uses the actual heading format; the `verify-report.md` §Scope index below lists the nine actual sections in order, with the per-slice → §Scope mapping for the slices that ARE recoverable (19-23) and an honest "not recoverable" note for the rest. |
| 3 | Slice 24 triangulation row is BOTH expired-by-date AND `superseded=true`; therefore it does NOT independently prove the `expiresAt > now` predicate if the date predicate were removed (the `superseded=false` predicate would still exclude it). | The triangulation row was rewritten to match the spec scenario "Expired memory is filtered" exactly: `superseded=false AND expirada=false AND expiresAt < ahora`. With this row, removing the date predicate from `AiMemoriaSpringDataRepository#findActiveMemories` would cause the assertion to fail with `expected: <1> but was: <2>`. |
| 4 | Test diff scope looked test-only but files are untracked so final verify must inspect source independently. | Out-of-scope for the apply half; the orchestrator's `sdd-verify` pass must independently inspect the source tree to confirm the production query is unchanged. The Slice 25 diff in `git diff --stat` is test-only (one test method) plus this `apply-progress.md` artifact. |

### Production logic audit (re-confirmed for Slice 25)

- `AiMemoriaSpringDataRepository#findActiveMemories(...)` still enforces `m.expiresAt > :ahora`, `m.superseded = false`, `m.expirada = false`, `m.visibilidad = CONVERSACION_SCOPED` at the SQL trust boundary (Slice 9 hardening + Slice 10 visibility predicate). **Unchanged in this slice.**
- `AiMemoriaRepositoryAdapter#findActivasByConversacionId(...)` still passes `LocalDateTime.now()` into the query — unchanged.
- `AiMemoria.crear(...)` domain invariant (`expiresAt.isAfter(ahora)`) is unchanged; the new triangulation row bypasses it via `AiMemoria.reconstitute(...)` exactly like the original Slice 24 row did.

### Strict-TDD cycle completed (Slice 25)

| Phase | Step | Outcome |
|-------|------|---------|
| 0 | Safety net | Pre-change focused rerun: `./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive"` → **Tests run: 1, Failures: 0, Errors: 0**. Pre-change full class: **Tests run: 13, Failures: 0, Errors: 0**. Slice 24's time-stability fix still holds. |
| 1 | UNDERSTAND | The audit pointed out that the Slice 24 triangulation row's `superseded=true` flag made the date predicate a redundant exclusion. The spec scenario "Expired memory is filtered" specifies `expirada = false` AND `expires_at` past — that is exactly what the new triangulation row must express. |
| 2 | RED | The new triangulation row was constructed so that, **if the `expiresAt > :ahora` predicate were dropped from `AiMemoriaSpringDataRepository#findActiveMemories`**, the new row (matching actor/empresa/waConv, CONVERSACION_SCOPED, `superseded=false`, `expirada=false`, but past `expiresAt`) would be returned and the assertion `assertEquals(1, result.size())` would fail with `expected: <1> but was: <2>`. This is a "characterization" strengthening: the production code already implements the date predicate, so the test passes today; the strengthened data makes the assertion actually fail under the regression scenario the audit flagged. |
| 3 | GREEN | Post-change focused rerun: **Tests run: 1, Failures: 0, Errors: 0**. The new triangulation row passes the date predicate (it is excluded) and the assertion holds. Post-change full class: **Tests run: 13, Failures: 0, Errors: 0**. |
| 4 | TRIANGULATE | Broader change-critical AI suite (`AiControllerIT, AiRepositoryAdaptersIT, AiAccionRepositoryAdapterTest, AiMemoriaRepositoryAdapterTest, GlobalExceptionHandlerTest, KeycloakJwtActorContextMapperTest, OpenAiChatAdapterTest, SpringAiPromptMapperTest, AiToolContextAdapterTest, ThreadLocalAiToolContextHolderTest, AiToolArchitectureContractTest`) → **Tests run: 152, Failures: 0, Errors: 0**. Domain + application full suite → **Domain 267/267, Application 235/235 green**. No collateral damage; the test-only narrowing of the triangulation row did not regress any neighbour. |
| 5 | REFACTOR | The comment block on the new triangulation row documents (a) which spec scenario it matches ("Expired memory is filtered" in `openspec/changes/add-crm-ai-assistant-spring-ai/specs/ai-memory/spec.md`), (b) why `AiMemoria.reconstitute(...)` is used instead of `AiMemoria.crear(...)` (the `crear` invariant rejects past `expiresAt`), and (c) exactly which predicates would need to be dropped from the production query for the assertion to fail. The assertion message now names the missing predicate so future failure logs point at the regression directly. The apply-progress wording corrections remove the false claim that git tracks `verify-report.md`'s directory and replace the bogus `## Verification Report — Slice N — …` references with the actual `## Verification Report` heading format plus the §Scope index. |

### TDD Cycle Evidence (Slice 25)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| Audit #3 — triangulation predicate independence | `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/ai/AiRepositoryAdaptersIT.java` | Integration (`@DataJpaTest`) H2 | ✅ Pre-change focused `1/1` green; pre-change full class `13/13` green (Slice 24 fix intact) | ✅ Strengthened (by reasoning): new triangulation row is now `superseded=false, expirada=false, past expiresAt` — would fail with `expected 1, was 2` if `expiresAt > :ahora` is dropped from `AiMemoriaSpringDataRepository#findActiveMemories` | ✅ Post-change focused `1/1` green; post-change full class `13/13` green | ✅ Broader change-critical AI suite `152/152` green; domain `267/267` green; application `235/235` green | ✅ Comment block documents spec scenario + reconstitute rationale + failure mode; assertion message names the missing predicate; apply-progress wording corrections remove false claims |
| Audit #1/#2 — apply-progress wording | `openspec/changes/add-crm-ai-assistant-spring-ai/apply-progress.md` | n/a (artifact) | n/a (artifact) | n/a (artifact) | n/a (artifact) | n/a (artifact) | ✅ "File Reconstruction Notice" + "Slices 1-23 Directional Summary" + "Slice key facts" + "Deviations from Design" all corrected to match the actual git status and the actual `verify-report.md` section structure. The §Scope index now lists the nine actual `verify-report.md` sections in order. |

### Slice 25 — Test Summary

- **Total tests added**: 0 (the modified test method already existed; one triangulation row was swapped inside it).
- **Total tests updated**: 1 (`AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive`).
- **Total tests passing after Slice 25**:
  - Focused: `AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` **1/1 passing**.
  - Full class: `AiRepositoryAdaptersIT` **13/13 passing** (12 unchanged + the hardened method).
  - Broader change-critical AI suite (11 classes, including `AiRepositoryAdaptersIT`): **152/152 passing**.
  - Domain + application full: **Domain 267/267, Application 235/235 passing**.
- **Layers used**: Integration (`@DataJpaTest` H2) — JPA repository adapter round-trip against real Spring Data + H2.
- **Approval tests**: None — this is a test-data strengthening that proves an existing contract independently.
- **Pure functions created**: 0.

### Slice 25 — Strict TDD Test Commands + Results

```text
# Pre-change safety net (Slice 24 fix still holds)
./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

# Post-change GREEN (focused)
./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
# Hibernate log confirms production query still applies all four predicates
# (visibilidad='CONVERSACION_SCOPED' AND superseded=false AND expirada=false AND expires_at>?)

# Post-change GREEN (full IT class)
./mvnw.cmd -pl infrastructure -am "-Dtest=AiRepositoryAdaptersIT" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 13, Failures: 0, Errors: 0, Skipped: 0

# Post-change GREEN (broader change-critical AI suite)
./mvnw.cmd -pl infrastructure -am "-Dtest=AiControllerIT,AiRepositoryAdaptersIT,AiAccionRepositoryAdapterTest,AiMemoriaRepositoryAdapterTest,GlobalExceptionHandlerTest,KeycloakJwtActorContextMapperTest,OpenAiChatAdapterTest,SpringAiPromptMapperTest,AiToolContextAdapterTest,ThreadLocalAiToolContextHolderTest,AiToolArchitectureContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
# → Tests run: 152, Failures: 0, Errors: 0, Skipped: 0

# Post-change GREEN (domain + application full)
./mvnw.cmd -pl domain,application -am test
# → Tests run: 267 + 235, Failures: 0, Errors: 0, Skipped: 0; BUILD SUCCESS
```

### Files Changed (Slice 25)

| File | Action | Description |
|------|--------|-------------|
| `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/ai/AiRepositoryAdaptersIT.java` | Modified | In `aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive`, replaced the Slice 24 triangulation row (which was BOTH `superseded=true` AND `expirada=true`) with a row matching the spec scenario "Expired memory is filtered" exactly: `superseded=false, expirada=false, expiresAt = ahora.minusMinutes(1)`. The new row is built via `AiMemoria.reconstitute(...)` because the `AiMemoria.crear` invariant rejects past `expiresAt`. The comment block now documents (a) which spec scenario the row matches, (b) why `reconstitute` is the correct seam, and (c) exactly which predicate would need to be dropped from `AiMemoriaSpringDataRepository#findActiveMemories` for the assertion to fail. The assertion message now names the missing predicate so future failure logs point at the regression directly. |
| `openspec/changes/add-crm-ai-assistant-spring-ai/apply-progress.md` | Modified | "File Reconstruction Notice" corrected to record that **both** `apply-progress.md` and `verify-report.md` are untracked (audit finding #1). "Slices 1-23 Directional Summary" corrected to use the actual `## Verification Report` heading format with `**Scope**: ...` disambiguation (audit finding #2). "Slice key facts" table right-column replaced with the actual §Scope index for the slices that ARE recoverable (19-23) and an honest "not recoverable" note for the rest. "Deviations from Design" + this Slice 25 section record the corrections explicitly so a future verifier can audit the audit. Slice Index updated with Slice 25 entry. |

### Deviations from Design (Slice 25)

1. **No production code touched.** Per the orchestrator directive and the project convention, the production query `AiMemoriaSpringDataRepository#findActiveMemories` is unchanged in this slice. The strengthened triangulation row only exercises an existing predicate, it does not introduce a new contract.
2. **`verify-report.md` is intentionally NOT touched in this slice.** The audit explicitly accepts the pre-Slice-24 failure state in `verify-report.md` as long as the orchestrator's final `sdd-verify` pass reconciles it. The §Scope index in this `apply-progress.md` is the authoritative pointer for both files until that final pass runs.
3. **No new exception types, no new helpers, no schema/migration changes.** The fix is a one-row data swap in a single integration test method plus wording corrections in this artifact. No production behavior changed; this slice strengthens the existing assertion that the production `ai-memory` phase-1 read query enforces `expiresAt > :ahora` independently of the superseded predicate.

### Issues Found (Slice 25)

1. **Triangulation-row independence was overstated in Slice 24 wording.** The original triangulation row's `superseded=true` flag meant the assertion could not fail if the date predicate were dropped — only if BOTH predicates were dropped. Slice 25 fixes this by aligning the triangulation row with the spec scenario.
2. **`verify-report.md` is also untracked.** Both `apply-progress.md` and `verify-report.md` (and the rest of `openspec/changes/add-crm-ai-assistant-spring-ai/`) are untracked under the current branch. The final `sdd-verify` pass must either commit them or otherwise reconcile the artifact state for archive-readiness.
3. **`verify-report.md` has nine sections, not one-per-slice.** Per-slice → §Scope mapping for slices 1-18 was lost when `apply-progress.md` was truncated and was NOT preserved in `verify-report.md`'s section structure. Slices 19-23 are recoverable from the existing scopes (see §Scope index). The orchestrator's final `sdd-verify` pass must either restore the missing mapping or accept the slices 1-18 mapping as "lost but not blocking".
4. **Recurring test-fragility risk on fixed timestamps.** Slice 25 preserves Slice 24's wall-clock anchor; future AI integration tests should follow the same pattern. This is now the fourth time the pattern has surfaced (Slice 7, Slice 9, Slice 24, Slice 25 reaffirmation).

### Remaining Tasks (after Slice 25)

- [ ] **Orchestrator `sdd-verify` follow-up** — must independently inspect the source tree (since both OpenSpec files are untracked) and confirm the production query is unchanged, then update `verify-report.md` to record Slice 24 and Slice 25 closures and reconcile the per-slice → §Scope mapping for slices 1-18 if archive-readiness requires it.
- [ ] **`./mvnw verify` root gate** — was already green in the previous verify report (Slice 23 unblocked `EtiquetaControllerIT`); should stay green after Slice 25 because no production code changed.
- [ ] **Out of scope (unchanged):** `FichaWiringTest` 6 errors resolved in Slice 21, `SecurityConfigTest.audienceRejectionBoundary_documented()` documentary tautology, changed-file coverage reporting, `tasks.md` open items `5.1`-`5.4` / `6.7`.

### Workload / PR Boundary (Slice 25)

- **Mode**: narrow audit-cleanup slice under the 400-line budget.
- **Current work unit**: `AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` triangulation-row predicate independence + `apply-progress.md` reconstruction-claim accuracy.
- **Boundary**: starts at the audit's four findings and ends with a green focused rerun + green full-class rerun + green change-critical AI suite + green domain + application modules + corrected wording in `apply-progress.md`. No commit, push, or PR creation (per orchestrator directive).
- **Estimated review budget impact**: ~15 changed lines in one test file (one triangulation row swap + tighter comments + tightened assertion message) plus ~50 lines of wording corrections + new Slice 25 section in `apply-progress.md`.

### Skill resolution (Slice 25)

- **Strict TDD**: confirmed active and followed for this slice (RED → GREEN → TRIANGULATE → REFACTOR). The RED is shown by reasoning (the strengthened assertion would fail if the date predicate were dropped from the production query); the GREEN is shown by fresh execution; the TRIANGULATE is shown by the change-critical AI suite + domain + application reruns; the REFACTOR is shown by the comment block + assertion message + apply-progress wording corrections.
- **sdd-apply SKILL.md**: read and applied (Step 1 load skills → Step 2 read context incl. apply-progress + spec + design + verify-report → Step 2b read previous apply-progress → Step 3 testing capabilities (Strict TDD via `mvn verify`) → Step 4 narrow implementation → Step 5 mark tasks complete → Step 6 persist progress via OpenSpec file edit → Step 7 return summary).
- **work-unit-commits SKILL.md**: read; this slice is one coherent reviewable work unit (audit cleanup for a single test method + artifact accuracy) — tests with code per the rule, future PR-ready, rollback is one-file revert per file.
- **strict-tdd.md module**: applied via the orchestrator directive (RED-by-reasoning against unchanged production, GREEN by fresh execution, full-class + change-critical TRIANGULATE, Javadoc REFACTOR with rationale).

### Status (after Slice 25)

**The post-Slice-24 audit is closed.**

1. ✅ Audit finding #1 (`apply-progress.md` false claim about git tracking `verify-report.md`): fixed in the "File Reconstruction Notice" + "Slice 25 — Deviations from Design".
2. ✅ Audit finding #2 (`apply-progress.md` incorrect `## Verification Report — Slice N — …` heading reference): fixed in the "Slices 1-23 Directional Summary" + "Slice key facts" + the new §Scope index.
3. ✅ Audit finding #3 (Slice 24 triangulation row not independently proving the date predicate): fixed in `AiRepositoryAdaptersIT#aiMemoria_saveAndFindActivasByConversacionId_shouldReturnOnlyActive` — the new triangulation row matches the spec scenario "Expired memory is filtered" (`superseded=false, expirada=false, expiresAt < ahora`), so removing the `expiresAt > :ahora` predicate from `AiMemoriaSpringDataRepository#findActiveMemories` would cause the assertion to fail with `expected: <1> but was: <2>`.
4. ⚠️ Audit finding #4 (source must be inspected independently because files are untracked): out of scope for the apply half — this is the orchestrator's `sdd-verify` responsibility and is recorded as the first remaining task.
5. ✅ No production code touched. No MVC/security wiring changed. No schema/migration changed. No new exception types introduced. No AI repository adapter refactored. Slice 24's time-stability fix is preserved verbatim (only the triangulation row changed).
6. ✅ Focused rerun `1/1` green; full class `13/13` green; change-critical AI suite `152/152` green; domain `267/267` green; application `235/235` green.

The `ai-memory` spec scenario "Expired memory is filtered" is now independently pinned by the strengthened triangulation row, ready for the orchestrator's `sdd-verify` follow-up.
