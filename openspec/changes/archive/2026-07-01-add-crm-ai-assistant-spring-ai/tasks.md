# Tasks: AI Assistant (Spring AI) — Resource-First Tenant Correction

## Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~800–1100 (3 PRs, `feature-branch-chain`) |
| 800-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR5 → PR6 → PR7 |
| Delivery strategy | auto-forecast |
| Chain strategy | feature-branch-chain (target: `feauture/IA`) |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
800-line budget risk: High

### Work Units

| Unit | Goal | PR | Base |
|------|------|----|------|
| 5 | `/chat` persistence | PR5 | `feauture/IA` |
| 6 | confirm/reject/get | PR6 | PR5 branch |
| 7 | `/acciones` selector | PR7 | PR6 branch |

> **Naming**: `findBySolicitadaPorAndEstadoAndEmpresaId(actor, estado, empresaId, pageable)` — PR7.

## Phase 1: Batch 1 — Spec Alignment ✅ DONE

- [x] 1.1 `design.md` rewritten to resource-first tenant model.
- [x] 1.2 `specs/security/spec.md` — `usuarioId` identity anchor; `empresaId` optional hint.
- [x] 1.3 `specs/ai-assistant/spec.md` — `/chat`, `/conversaciones/{id}` resource-first.
- [x] 1.4 `specs/ai-action-proposal/spec.md` — `AiAccion.empresaId` authoritative.
- [x] 1.5 `specs/tablero/spec.md` — AI tools use owned-resource tenant.
- [x] 1.6 `tasks.md` rewritten to point apply at Batch 2/3/4.

## Phase 2: Batch 2 — `/chat` Persistence (PR5)

- [x] 2.1 RED `application/src/test/.../service/AnalizarChatServiceTest` — actor owns E1+E2, WA conv E2, hint E1 must NOT override; rows carry `empresaId=E2`.
- [x] 2.2 GREEN `application/ai/service/AnalizarChatService.java` — load WA conv FIRST, `empresaId = conv.canalEmpresaId`, validate ownership, persist under that tenant.
- [x] 2.3 REFACTOR `AnalizarChatCommand` Javadoc — `empresaId` hint only.
- [x] 2.4 GREEN `infrastructure/adapter/in/rest/ai/AiController.java#chat` — drop `actor.empresaId()` forwarding.
- [x] 2.5 RED `infrastructure/src/test/.../AiControllerIT` — multi-company `/chat` IT; PR5 verify.

## Phase 3: Batch 3 — Resource Endpoints (PR6)

- [x] 3.1 RED `ConfirmarAccionServiceTest` — derive tenant from `accion.empresaId`, reject when actor lacks ownership.
- [x] 3.2 GREEN `application/ai/service/ConfirmarAccionService.java` — drop `aiTenantScopeAdapter.resolver(...)` pre-step; use `accion.getEmpresaId()` after `findById`.
- [x] 3.3 RED+GREEN `RechazarAccionService` same pattern.
- [x] 3.4 RED+GREEN `ObtenerConversacionAsistenteService` same pattern (`conversacion.getEmpresaId()`).
- [x] 3.5 REFACTOR confirm/reject/get-conversation command Javadocs — `empresaId` is REQUIRED at the command boundary (strict cross-check against the resource's tenant); never optional / hint-only.
- [x] 3.6 RED `AiControllerIT` — hint E1 + resource E2 → 403; PR6 verify.
- [x] 3.7 RED `ConfirmarAccionCommandTest`, `RechazarAccionCommandTest`, `ObtenerConversacionAsistenteCommandTest` — constructor rejects `null empresaId` with message `"empresaId is required"` (mandatory boundary); triangulation with valid non-null case + regression of prior invariants.
- [x] 3.8 GREEN `ConfirmarAccionCommand`, `RechazarAccionCommand`, `ObtenerConversacionAsistenteCommand` — add `if (empresaId == null) throw new IllegalArgumentException("empresaId is required")` to canonical constructor.
- [x] 3.9 GREEN `ConfirmarAccionService`, `RechazarAccionService`, `ObtenerConversacionAsistenteService` — drop the `command.empresaId() != null` skip guard before the cross-check (constructor guarantees non-null, so the cross-check is always evaluated); update the inline comment to reflect the no-skip invariant.

## Phase 4: Batch 4 — `/acciones` Selector (PR7)

> <b>User-approved PR7 semantics (locked at slice start):</b> the
> `empresaId` selector is REQUIRED. The system MUST NOT guess, MUST
> NOT auto-resolve single-company actors, and MUST NOT fall back to
> the first owned company. If `empresaId` is missing/null, the DTO
> validation produces HTTP 400; if supplied and not owned by the
> actor, the application service raises `AsistenteTenantException`
> (HTTP 403). The earlier "null + 1 owned → auto-resolve" branch
> was REJECTED by the user.

- [x] 4.1 RED `ListarAccionesPendientesServiceTest` — explicit filters only; null empresaId rejected at command boundary; actor not owning supplier empresa → `AsistenteTenantException`; empty list passes through.
- [x] 4.2 GREEN `application/ai/service/ListarAccionesPendientesService.java` — drop auto-resolve single-company; explicit `empresaId` validated through `ActorEmpresaScopePort` via `AiTenantExceptionTranslator.resolveForSelector(...)`.
- [x] 4.3 GREEN `application/ai/exception/AsistenteTenantException.java` — add `empresaNoPoseidaPorActor(actor, empresaId)` + `tenantSelectorRechazado(actor, empresaId)` factories (replaces the originally proposed `tenantAmbiguo(actor)`).
- [x] 4.4 RED `infrastructure/src/test/.../ai/AiAccionRepositoryAdapterTest` + `AiRepositoryAdaptersIT` — new query filters by `(solicitadaPor, estado, empresaId)`.
- [x] 4.5 GREEN `infrastructure/adapter/out/persistence/ai/repository/AiAccionSpringDataRepository.java` — add `findBySolicitadaPorAndEstadoAndEmpresaId` (kept the actor-only `findBySolicitadaPorAndEstado` for future owner-dashboard use).
- [x] 4.6 GREEN `infrastructure/adapter/out/persistence/ai/AiAccionRepositoryAdapter.java#listPendingByActor` — new query; the SQL trust boundary enforces tenant scoping even if the application service is bypassed.
- [x] 4.7 RED `AiControllerIT#listarAcciones` — 5 new cases: missing empresaId → 400, limite out of range → 400, non-owned empresa → 403, default-lomite → 200, happy path → 200.
- [x] 4.8 GREEN `infrastructure/adapter/in/rest/dto/ai/ListarAccionesPendientesRequest.java` — new `@NotNull empresaId` + `@Min(1)/@Max(200) limite` record DTO; layered validation at the REST boundary.
- [x] 4.9 GREEN `infrastructure/adapter/in/rest/ai/AiRestCommandMapper.java` — new `toListarAccionesPendientesCommand(actor, request)` mapper method (replaces the old `toCommand(actor, empresaId, limite)` overload).
- [x] 4.10 GREEN `application/ai/command/ListarAccionesPendientesCommand.java` — canonical constructor rejects null `empresaId` with `IllegalArgumentException("empresaId is required")` (defense in depth).
- [x] 4.11 GREEN `application/ai/service/AiTenantExceptionTranslator.java` — new `resolveForSelector(port, actor, empresaId)` helper that translates to `tenantSelectorRechazado(...)` instead of the chat-shaped message.
- [x] 4.12 GREEN `infrastructure/adapter/in/rest/ai/AiController.java#listarAcciones` — accepts `@Valid ListarAccionesPendientesRequest request`; project style rule (no `var`) applied to controller + mapper at the same time (all endpoints now use explicit concrete types).

## Phase 5: Cross-PR Verify

- [x] 5.1 `mvn -pl domain,application -am test` green (147/147 baseline). ✅ Final verify: Domain 267/267, Application 235/235.
- [x] 5.2 AI tests + `ActorEmpresaScopeServiceTest` + `KeycloakJwtActorContextMapperTest` green. ✅ Final verify: 152/152 AI suite green.
- [x] 5.3 `SugerirRespuestaUseCase` + `AnthropicSugerenciaAdapter` untouched. ✅ Final verify confirmed no changes.
- [x] 5.4 Append verify-report entries for PR5/PR6/PR7. ✅ verify-report.md records all slices; Slices 1-23 directional summary preserved via verify-report.md sections.

## Phase 6: Post-Verify Architecture Corrections (Slice 3)

- [x] 6.1 Add `infrastructure/.../rest/ai/AiRestCommandMapper.java` translating REST DTO/path/query/actor → command. `/chat` always carries `empresaId=null`; non-chat endpoints forward the `empresaId` query parameter.
- [x] 6.2 Refactor `AiController` so no endpoint instantiates commands inline — every command is built through `AiRestCommandMapper`.
- [x] 6.3 Add `application/.../empresa/port/in/ActorEmpresaScopePort.java` and make `ActorEmpresaScopeService implements ActorEmpresaScopePort`. Do NOT introduce a new thin AI resolver service.
- [x] 6.4 Add `application/.../ai/service/AiTenantExceptionTranslator.java` (package-private helper) translating `TenantScopeViolationException` → `AsistenteTenantException` at the AI service call site.
- [x] 6.5 Update all 8 AI services to depend on `ActorEmpresaScopePort` instead of `AiTenantResolver`. Delete `AiTenantResolver` and `DefaultAiTenantResolver`.
- [x] 6.6 Update `boot/.../config/AiWiringConfig.java`: drop the `aiTenantResolver` `@Bean`; change every AI service `@Bean` to inject `ActorEmpresaScopePort`. Update AI service tests to instantiate `ActorEmpresaScopeService` directly as the port dependency. Update `openspec/.../{design,apply-progress}.md`.
- [x] 6.7 Run focused tests / compile and verify the suite is green. ✅ Final verify: `./mvnw.cmd verify` BUILD SUCCESS; focused AI suite 152/152 green.

## Phase 7: Verify Blocker Contract Reconciliation

- [x] 7.1 Inspect the lifecycle source of truth (`AiAccion`, `EstadoAccion`, `ConfirmarAccionService`, `ResultadoEjecucionAccion`, `AccionEjecutadaResponse`, targeted tests) and confirm the coherent implemented contract is `PENDING -> CONFIRMED | REJECTED | EXPIRED`, then `CONFIRMED -> EXECUTED | FAILED` after dispatch.
- [x] 7.2 Reconcile OpenSpec artifacts to the accepted phase-1 replay contract: confirmation has no `idempotency_key` parameter or persistence field, and replay protection is the existing non-PENDING state + optimistic version guard rather than original-result replay.

## Phase 8: Verify Blocker Memory Contract Reconciliation

- [x] 8.1 Reconcile OpenSpec/config wording to the accepted phase-1 memory contract: singular `ai_memoria`, no phase-1 `ai_memoria_hechos`, and conversation-scoped reads only.
- [x] 8.2 RED+GREEN `AiMemoriaRepositoryAdapterTest` — reject memory save/delete when `ai-assistant.phase1.memory-writes-enabled=false`.
- [x] 8.3 RED+GREEN `AiRepositoryAdaptersIT` — phase-1 conversation reads exclude rows past `expires_at` and ignore contact-scoped rows.
- [x] 8.4 RED+GREEN `AiRepositoryAdaptersIT` + repository query — phase-1 conversation reads explicitly require `visibilidad = CONVERSACION_SCOPED`, excluding malformed legacy contact-scoped rows even when they carry the same `wa_conversacion_id`.

## Phase 9: Verify Blocker AI Tool Safety

- [x] 9.1 RED+GREEN `SpringAiPromptMapperTest` — every prompt includes the explicit `NO_FABRICATION_DIRECTIVE` (tool-only / no-fabrication contract).
- [x] 9.2 RED+GREEN `SpringAiPromptMapperTest` — when every data source is empty, the prompt surfaces the stronger `NO_DATA_AVAILABLE` safe-fallback directive so the model refuses rather than guesses.
- [x] 9.3 RED+GREEN `OpenAiChatAdapterTest` — the no-fabrication directive reaches the `ChatClient.user(...)` call verbatim (end-to-end triangulation).
- [x] 9.4 RED `ConfiguredToolCallBudgetPolicyTest` + `ConfiguredTurnTimeoutPolicyTest` — per-turn safety-limit policy collaborators exist with strict assertion contracts.
- [x] 9.5 GREEN `OpenAiChatAdapter` accepts the two policies + exposes `assertToolCallBudget(int)` and `getTurnTimeoutMillis()` seams (the previous Phase B `applyTurnTimeout(Supplier)` seam was removed in Phase 10 — see audit #1 closing).
- [x] 9.6 GREEN `OpenAiChatAdapter.generar(...)` runs `ChatClient.call()` synchronously on the request thread. The per-turn wall-clock budget is owned by Spring AI's auto-configured provider HTTP timeout (`spring.ai.openai.chat.options.timeout`); the `TurnTimeoutPolicy` bean is a verified configuration surface. The previous `CompletableFuture.supplyAsync(...).orTimeout(...)` wrapping was removed in Phase 10 because it broke the production thread-local tool context.
- [x] 9.7 GREEN `AiWiringConfig` wires both policies from `ai-assistant.max-tool-calls-per-turn` and `ai-assistant.turn-timeout-ms` (env-overridable via `AI_MAX_TOOL_CALLS` / `AI_TURN_TIMEOUT_MS`).
- [x] 9.8 GREEN Boot compile sanity (`./mvnw.cmd -pl boot -am -DskipTests package`) and targeted infra AI suite.
- [x] 9.9 OpenSpec continuity — `apply-progress.md` slice appended with strict-TDD evidence; `tasks.md` Phase 9 checklist updated.
- [ ] 9.10 Follow-up (NOT in this slice): real per-tool-call enforcement requires a custom `CallAdvisor` injected into the Spring AI `ChatClient` chain. The current implementation exposes the policy as a deterministic test seam; the loop is still governed by Spring AI defaults. See `design.md` open questions for the deferred plan.

## Phase 10: Audit #1/#2/#3 Closing Slice — AI Tool-Safety Honesty Correction

> **Mandate (orchestrator directive).** A fresh narrow audit found that
> the previous implementation:
> 1. wrapped `ChatClient.call()` in `CompletableFuture.supplyAsync(...).orTimeout(...)`,
>    which moved execution off the request thread and broke production
>    `ThreadLocalAiToolContextHolder` propagation (real `@Tool` callbacks
>    would hit `IllegalStateException` from `AiToolContextAdapter.resolve()`);
> 2. exposed the per-turn tool-call budget as a `assertToolCallBudget(int)`
>    seam but never enforced loop-stopping at runtime, while `application.yml`
>    and the spec overclaimed that "exceeding this stops the tool-calling
>    loop";
> 3. documented `AiAssistantException` as mapping to HTTP 502 but the
>    `GlobalExceptionHandler` did NOT register a dedicated handler for
>    it.
>
> The orchestrator's directive: make the **smallest coherent fix** that
> turns this narrow audit green. **Resolution chosen**: remove the unsafe
> async wrapping (the provider's HTTP timeout is the wall-clock gate);
> keep the test seams as verified configuration surfaces with honest
> Javadoc / spec wording; add the missing `AiAssistantException -> 502`
> REST mapping.

- [x] 10.1 RED `OpenAiChatAdapterTest#generar_runsChatClientCallOnCallingThread_withRealThreadLocalHolder` — proves the production-shaped (real) `ThreadLocalAiToolContextHolder` is observable during `chatClient.call()` and the call runs on the calling thread. (Audit #1 closing proof.)
- [x] 10.2 RED `OpenAiChatAdapterTest#generar_returnsResponseDirectly_withoutAsyncWrapping` — triangulation: the response mapper is invoked exactly once and `generar(...)` returns the mapper's result synchronously (no future / no `.get()` unwrapping).
- [x] 10.3 RED `OpenAiChatAdapterTest#adapter_doesNotWrapChatClientInCompletableFuture` — static guard forbidding `CompletableFuture.supplyAsync(`, `.orTimeout(`, `.completeOnTimeout(` in production code (comments / strings stripped).
- [x] 10.4 RED `OpenAiChatAdapterTest#adapter_doesNotDeclareApplyTurnTimeout` — the previous public seam that invited misuse is gone.
- [x] 10.5 RED `OpenAiChatAdapterTest#adapter_doesNotCatchTimeoutOrExecutionExceptionInGenerar` — `generar(...)` declares NO `TimeoutException` / `ExecutionException` thrown types, and `applyTurnTimeout` is not declared (defence in depth).
- [x] 10.6 GREEN `OpenAiChatAdapter#generar(...)` — `CompletableFuture.supplyAsync` + `.orTimeout` wrapping REMOVED. The chat call now runs synchronously on the request thread; the thread-local context bound before `chatClient.call()` is therefore observable to any `@Tool` callback Spring AI invokes on the same thread. The runtime per-turn wall-clock budget is owned by Spring AI's auto-configured provider HTTP timeout (`spring.ai.openai.chat.options.timeout`).
- [x] 10.7 GREEN `OpenAiChatAdapter` — `applyTurnTimeout(Supplier)` method REMOVED (it invited misuse). `assertToolCallBudget(int)` and `getTurnTimeoutMillis()` stay as deterministic test seams. Javadoc rewritten to honest contract language.
- [x] 10.8 GREEN `OpenAiChatAdapterTest` — previous `applyTurnTimeout_*` tests REMOVED (they exercised removed code); existing tool-binding/cleanup tests now implicitly verify the synchronous propagation path because the real-holder test stands in for them at the audit level.
- [x] 10.9 RED `GlobalExceptionHandlerTest#handleAiAssistantException_upstreamFailure_shouldReturn502` — `AiAssistantException.upstreamFailure(...)` maps to HTTP 502 + the upstream failure reason in the body.
- [x] 10.10 RED `GlobalExceptionHandlerTest#handleAiAssistantException_invalidAssistantOutput_shouldReturn502` — the other factory (`invalidAssistantOutput(...)`) maps to 502 with the same boundary contract.
- [x] 10.11 RED `GlobalExceptionHandlerTest#handleAiAssistantException_preservesCause` — the cause is NOT stripped / rewrapped; observability hooks can introspect the original failure.
- [x] 10.12 GREEN `GlobalExceptionHandler#handleAiAssistantException` — new `@ExceptionHandler(AiAssistantException.class) -> 502 Bad Gateway`. Audit #3 closing.
- [x] 10.13 RED `AiControllerIT#chat_shouldReturn502_whenUseCaseThrowsAiAssistantException` — IT asserts the end-to-end 502 mapping on `POST /api/ai/chat`.
- [x] 10.14 GREEN `boot/src/main/resources/application.yml` — rewritten yaml comments so they no longer overclaim "exceeding this stops the tool-calling loop" / "the controller returns a 502 on expiry". The properties stay (validated configuration surface); the comments now explain what they DO pin vs what is owned by Spring AI's provider HTTP timeout.
- [x] 10.15 GREEN `openspec/changes/.../specs/ai-assistant/spec.md` — the "Per-Turn Safety Limits" scenarios rewritten from "MUST stop the loop" / "MUST abort the turn" overclaims to the honest contract: test-seam + provider-HTTP-timeout + 502 mapping. The follow-up `CallAdvisor` is now explicitly documented.
- [x] 10.16 GREEN `openspec/changes/.../design.md` — new architecture-decision row "Remove the unsafe async-wrapping timeout; rely on the Spring AI provider HTTP timeout for the runtime wall-clock gate". Open questions row updated.
- [x] 10.17 GREEN `openspec/changes/.../apply-progress.md` — Slice 10 section appended with strict-TDD evidence table and an explicit "Implementation reality vs spec" note. Earlier Phase 9 lines clarified to reflect the synchronous shape (the previous "wraps with CompletableFuture.orTimeout" line is replaced with "runs synchronously; provider HTTP timeout is the wall-clock gate").
- [x] 10.18 Boot compile sanity (`./mvnw.cmd -pl boot -am -DskipTests package`) and the orchestrator's two targeted verification commands — green.

## Phase 11: Verify Blocker AI Tool Architecture Alignment (Slice 14)

> **Mandate (orchestrator directive).** The full SDD verify report
> flagged that "several read-only AI tools call application read ports
> directly instead of inbound use cases" and asked for the smallest
> coherent fix: either refactor the tools to use use cases or make the
> exception an explicit, coherent architecture decision.
>
> **Resolution chosen**: the read tools stay on outbound read ports
> (the AI owns those ports and the read tools have no business logic
> to wrap). The decision is documented in `design.md` (new architecture
> row + open-question resolution), tightened in `specs/ai-assistant/spec.md`
> (new read-vs-propose scenarios), and pinned by a new meta-test
> `AiToolArchitectureContractTest` (8 cases) that fails the build if
> a future refactor injects a forbidden dependency. `ProponerAccionTool`
> stays on the inbound use case — no mixing with the read-only exception
> language.

- [x] 11.1 RED-first meta-test `AiToolArchitectureContractTest` (8 cases) — pins the read-tool contract (4 tools depend on read ports + trusted context, no inbound use case, no mutation use case, no write port) and the propose-tool contract (depends ONLY on `ProponerAccionUseCase`, no read port, no mutation use case); cross-tool invariants (exactly one `@Tool` method per tool; read tools describe "Read-only" verbatim; propose tool describes "PENDING" + "confirm" verbatim).
- [x] 11.2 GREEN — the architecture contract is satisfied by the current tool surface (no production code change); 8/8 meta-test cases pass.
- [x] 11.3 GREEN `design.md` — new architecture-decision row documents the read-tools-ports vs propose-tool-usecase decision with rationale; Open Questions section gets a `[x] Resolved (Slice 14)` row.
- [x] 11.4 GREEN `specs/ai-assistant/spec.md` — the "Tool Surface Is Read-Only or Propose-Only" requirement gains two new scenarios spelling out the inbound-port vs outbound-port distinction and pinning the architecture-guard contract.
- [x] 11.5 Orchestrator verification commands — all three green:
  - Command 1 (5 tool tests + ProponerAccionToolMapperTest + new meta-test): **48/48 passing** (the new meta-test is included in the command-1 enumeration as the architecture-guard file).
  - Command 2 (AiToolContextAdapterTest + ThreadLocalAiToolContextHolderTest + OpenAiChatAdapterTest): **34/34 passing**.
  - Command 3 (`./mvnw.cmd -pl boot -am -DskipTests package`): **BUILD SUCCESS** across all 6 modules.

## Phase 12: Verify Blocker JSON Parser Robustness

> **Mandate (orchestrator directive).** The full SDD verification
> flagged `ConfirmarAccionMapper` as fragile because it uses manual
> substring-based JSON parsing. The work-unit is to replace the fragile
> parser with the smallest robust approach that fits project
> dependencies. The orchestrator said: prefer Jackson if already in the
> module dependency graph; do not add a new dependency unless
> absolutely necessary.
>
> **Resolution chosen**: Jackson is NOT in the `application` module's
> dependency graph (the module depends on `domain` only, plus
> JUnit/Mockito at test scope). The smallest coherent fix is a
> dependency-free, recursive-descent JSON parser that handles the full
> RFC 8259 escape set, arbitrary whitespace, and rejects non-string
> values for fields that must be strings. Errors are translated to the
> existing `AccionInvalidaException` so no parser-level exception type
> leaks to callers.

- [x] 12.1 RED-first `ConfirmarAccionMapperTest` (10 new robustness cases: escaped quotes, escapes, Unicode, whitespace, extra fields, invalid JSON, nested objects/arrays as string fields, null literal on optional fields, quoted numbers for UUIDs). 5 of 10 failed against the substring parser as expected (escaped quotes, escapes, Unicode, nested object/array rejection).
- [x] 12.2 GREEN — new `application/ai/json/` subpackage with a sealed `JsonValue` hierarchy (`JsonObject`, `JsonArray`, `JsonString`, `JsonNumber`, `JsonBoolean`, `JsonNull`) and a recursive-descent `JsonParser` (no new dependencies). `ConfirmarAccionMapper` now parses once per call and extracts typed values via `JsonObject` lookups; `JsonParseException` is caught at the boundary and re-thrown as `AccionInvalidaException.forInvalidInput(...)`. 22/22 `ConfirmarAccionMapperTest` passing.
- [x] 12.3 TRIANGULATE — direct parser unit tests (`JsonParserTest`, 39 cases across 6 nested classes: primitives, strings + escape sequences, objects + arrays, whitespace, rejection, round-trip) plus 4 additional mapper tests for the other discriminators (`toCreateTrato`, `toCreateTarea`, `toMoverFicha`). All 61/61 application tests for the parser + mapper pass.
- [x] 12.4 REFACTOR — verified implementation is clean (no dead imports, no unused types, defensive-copy on `JsonObject` preserves insertion order, Javadoc references the actual contract behavior).
- [x] 12.5 Orchestrator verification commands — all three green:
  - Command 1 (`-pl application -am "-Dtest=ConfirmarAccionMapperTest,ConfirmarAccionServiceTest"`): **34/34 passing** (22 mapper + 12 service).
  - Command 2 (`-pl domain,application -am "-Dtest=AiAccionTest,ConfirmarAccionMapperTest,ConfirmarAccionServiceTest"`): **60/60 passing** (26 AiAccionTest + 22 mapper + 12 service).
  - Command 3 (`./mvnw.cmd -pl boot -am -DskipTests package`): **BUILD SUCCESS** across all 6 modules.
- [x] 12.6 RED-first `ConfirmarAccionMapperTest` — 9 new tests proving required `null` fields produce `AccionInvalidaException` at the mapper boundary (not `IllegalArgumentException` from the constructor). 8 of 9 fail against the leaky mapper as expected: 3 throw the wrong exception type (`nombre`/`titulo`/`trato-nombre` constructor leaks IAE), 5 throw nothing because the constructor doesn't validate `estadoRelacion`/`descripcion`/`tipo`/`prioridad`/`tipoContrato` (the silent-null leak path). The 9th is a greenward optional-null regression test (`correo:null` still maps to null).
- [x] 12.7 GREEN `ConfirmarAccionMapper` — added `requiredStringField(JsonObject, String)` + `requiredEnumField(JsonObject, String, Class<E>)` helpers (symmetric to `uuidField`); updated `toCreateContacto`/`toCreateTrato`/`toCreateTarea` to use them for required fields. `toMoverFicha` unchanged (only UUID fields, already protected). Class Javadoc extended with a "Required vs optional fields" section documenting the contract.
- [x] 12.8 GREEN `ConfirmarAccionServiceTest.CreateTareaPayload.build()` — added the previously-missing `"descripcion":"Llamar al cliente mañana"` field. The fixture was relying on the old silent-null leak to make the happy-path service test pass; the mapper fix correctly rejects the incomplete payload, which surfaced the stale fixture during the GREEN-mapper TRIANGULATE step. The fixture correction is a one-line addition that now acts as a happy-path regression pin for the new required-null contract.
- [x] 12.9 Orchestrator verification commands — all three green + full domain+application suite green:
  - Command 1 (`-pl application -am "-Dtest=ConfirmarAccionMapperTest,ConfirmarAccionServiceTest,JsonParserTest"`): **82/82 passing** (31 mapper + 12 service + 39 parser).
  - Command 2 (`-pl domain,application -am "-Dtest=AiAccionTest,ConfirmarAccionMapperTest,ConfirmarAccionServiceTest,JsonParserTest"`): **108/108 passing** (26 AiAccionTest + 82 application tests).
  - Command 3 (`./mvnw.cmd -pl boot -am -DskipTests package`): **BUILD SUCCESS** across all 6 modules.
  - Full `mvn -pl domain,application -am test`: **233/233 passing** — no regressions elsewhere in the application module.
- [x] 12.10 RED `ConfirmarAccionServiceTest` — malformed confirmation payloads (invalid JSON syntax + required-null field) propagate `AccionInvalidaException` and MUST NOT be persisted as `FAILED` action results.
- [x] 12.11 GREEN `ConfirmarAccionService` — narrow `FAILED` lifecycle bookkeeping to real CRM mutation use-case failures only; payload-mapping validation errors must escape before any `FAILED` persistence.

## Phase 13: ThreadLocal Async Future-Risk Closure

- [x] 13.1 RED `boot/src/test/java/com/ar/crm2/config/AiToolContextThreadModelWiringTest.java` — composition-root contract test pins the concrete `ThreadLocalAiToolContextHolder` carrier for `aiToolContextHolder()`, `aiToolContextAdapter(...)`, and `openAiChatAdapter(...)`.
- [x] 13.2 GREEN `boot/src/main/java/com/ar/crm2/config/AiWiringConfig.java` — bean factory signatures now declare `ThreadLocalAiToolContextHolder` explicitly while downstream runtime collaborators keep depending on the `AiToolContextHolder` abstraction.
- [x] 13.3 REFACTOR `design.md` + `apply-progress.md` — document that phase 1 intentionally stays synchronous/thread-local and any future async/reactive tool execution requires a separate design change rather than a silent carrier swap.
