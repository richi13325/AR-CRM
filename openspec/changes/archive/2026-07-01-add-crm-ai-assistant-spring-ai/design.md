# Design: AI Assistant (Spring AI)

## Technical Approach

Keep the existing `application/ai` bounded context, manual `WiringConfig` composition, and human-confirmed action flow, but correct PR4 tenant handling to a resource-first model. `ActorContext.usuarioId` remains the authenticated identity anchor and `ActorContext` does not carry tenant scope. Explicit tenant selection enters as request data for endpoints that need it, while resource-bound AI flows MUST derive tenant authority from the resource they load before any AI state is read or written. Confirmation is a two-step lifecycle: `PENDING -> CONFIRMED` before CRM dispatch, then `CONFIRMED -> EXECUTED | FAILED` after dispatch. Replay protection for phase 1 remains state/version based; no separate `idempotency_key` is part of the confirmation endpoint contract. This aligns the design with `ai-assistant`, `ai-action-proposal`, and `security` after the PR4 correction.

## Architecture Decisions

| Decision | Alternatives considered | Rationale |
|---|---|---|
| Use `ActorContext.usuarioId` as the only identity anchor | Using actor context as both identity and tenant authority | User identity is stable across tabs/chats; tenant context is not. |
| Resource-bound endpoints derive tenant from the loaded resource: `/chat` -> `WhatsappConversacionResumen.canalEmpresaId`; confirm/reject -> `AiAccion.empresaId`; get conversation -> `AiConversacion.empresaId` | Shared actor-scoped resolver for every endpoint | Prevents persisting or authorizing AI state under the wrong company when the actor owns multiple companies. |
| `GET /api/ai/acciones` uses an explicit REQUIRED `empresaId` selector; the system MUST NOT guess, MUST NOT auto-resolve single-company actors, MUST NOT fall back to the first owned company | Reusing first-owned fallback for multi-company actors; auto-resolve for single-company actors (originally proposed, **rejected by user**) | Listing has no resource anchor, so ambiguity MUST be resolved at the request boundary. Auto-resolve was rejected because it gives a tenant-level UI affordance without explicit tenant selection — that hides ownership from the audit trail and contradicts the resource-first model used everywhere else. |
| Split trust boundaries: controller supplies identity + optional active context, services enforce tenant authorization from resource ownership, repositories filter by authoritative tenant | Letting a service-layer helper both select and authorize tenant without a clear port boundary | Makes actor identity, tenant authorization, and request context independently auditable. |
| AI services depend on the Empresa-owned {@code ActorEmpresaScopePort} (implemented by {@code ActorEmpresaScopeService}) for tenant resolution, not on a separate AI-specific resolver service | Each bounded context introduces its own thin tenant-resolver service that just delegates to Empresa and translates the domain exception | Hexagonal rule: an application service depends on a port, not on another bounded context's service. The previous AI-specific {@code AiTenantResolver} / {@code DefaultAiTenantResolver} layer added an indirection whose only job was to translate {@code TenantScopeViolationException} into {@code AsistenteTenantException} — that translation now lives at the AI service call site via a small package-private helper, so the Empresa port stays neutral and no AI-specific service is needed. |
| Keep the implemented post-confirm lifecycle `CONFIRMED -> EXECUTED | FAILED` and document replay as state/version rejection, not `idempotency_key` replay | Collapse successful confirmation into terminal `CONFIRMED`; add a new confirmation request id / `idempotency_key` contract in phase 1 | Source inspection shows the domain aggregate, application result contract, REST response, persistence enum, and targeted tests already agree on `EXECUTED` / `FAILED`. Introducing `idempotency_key` now would require a broader API + schema contract that does not exist in the current controller or command surface. |
| Remove the unsafe async-wrapping timeout from `OpenAiChatAdapter.generar(...)`; rely on the Spring AI auto-configured provider HTTP timeout (`spring.ai.openai.chat.options.timeout`) for the runtime per-turn wall-clock gate. Drop the previous `applyTurnTimeout(Supplier)` public seam that invited misuse. | Keep `CompletableFuture.supplyAsync(...).orTimeout(...)` wrapping the chat call (the previous Phase B approach); add a separate thread-local-aware context carrier; wire a custom `CallAdvisor` for the per-tool-call budget | The previous wrap jumped execution to a `ForkJoinPool` thread and broke the production `ThreadLocalAiToolContextHolder` propagation — every real `@Tool` callback would then throw `IllegalStateException` from `AiToolContextAdapter.resolve()`. Spring AI 2.0 already exposes a provider HTTP timeout that the synchronous call observes; wiring both timeouts is the smallest coherent fix and was found by the fresh audit (Slice 10 closing). |
| Pin the phase-1 tool-context carrier explicitly at the boot composition root as `ThreadLocalAiToolContextHolder`; any async/reactive tool execution remains out of scope until a future change introduces a new carrier contract. | Keep the boot factory methods typed only to `AiToolContextHolder` and rely on Javadoc/tests to imply the current carrier | The current runtime model is intentionally synchronous and already depends on real `ThreadLocal` semantics. Declaring the concrete carrier in `AiWiringConfig` makes that assumption explicit where the beans are assembled, and a focused wiring-contract test now fails if someone silently swaps the carrier without a new design change. |
| AI read tools depend on AI-bounded-context outbound read ports (`ContactoLecturaPort`, `ColumnaLecturaPort`, `WhatsappMensajeLecturaPort`, `FindAiResumenPort`) plus the trusted `AiToolContextPort`. The propose tool depends on the inbound `ProponerAccionUseCase` (the only inbound use case in the tool surface). No tool depends on a real CRM mutation use case. | Route every AI tool call through an inbound use case; collapse all read and write into a single "AI tools use case" boundary | The AI read ports are AI-bounded-context owned (`application.ai.port.out`) and expose AI-specific shapes (e.g. `findByEmpresaIdAndTelefono(EmpresaId, String)` takes the trusted tenant from `AiToolContext`; `findByConversacionId(UUID)` returns the application-owned `WhatsappMensajeResumen` projection, not the JPA entity). Read tools have no business logic to wrap, so calling an inbound use case would be unnecessary indirection. The propose tool calls `ProponerAccionUseCase` because staging has business logic (resolve trusted context, call `RegistrarAccionUseCase`, return staged id + estado). Real CRM mutation use cases (`CreateContactoUseCase`, `MoverColumnaFichaUseCase`, etc.) are NEVER tool dependencies — that is the safety boundary. The contract is pinned by `AiToolArchitectureContractTest` (8 cases, 1 per tool boundary + cross-tool invariants) and by the existing per-tool `constructor_doesNotInjectRealMutationUseCases` and `@Tool` description tests. |

## Data Flow

### Sequence 1 — `POST /api/ai/chat`

```text
User -> AiController (actor.usuarioId, optional activeEmpresaId)
     -> WhatsappConversacionLecturaPort.findById(waConversacionId)
     -> tenant = conversacion.canalEmpresaId
     -> ActorEmpresaScopeService validates actor owns tenant
     -> Find/Create AiConversacion(empresaId = tenant)
     -> GenerarChatAsistentePort with AiToolContext(empresaId = tenant)
     -> Save ai_conversaciones / ai_mensajes / ai_resumen under tenant
```

### Sequence 2 — resource endpoints

```text
User -> AiController (actor.usuarioId, optional activeEmpresaId)
     -> load AiAccion or AiConversacion by id
     -> tenant = resource.empresaId
     -> require owner + actor ownership of tenant
     -> confirm / reject / return conversation
     -> persist only with the same resource tenant
```

### Sequence 3 — `GET /api/ai/acciones` (PR7 strict selector)

```text
User -> AiController (actor.usuarioId, request.empresaId REQUIRED, request.limite optional)
     -> ListarAccionesPendientesRequest DTO @Valid @NotNull empresaId
        -> missing empresaId -> BindException -> GlobalExceptionHandler -> HTTP 400
     -> AiRestCommandMapper.toListarAccionesPendientesCommand(actor, request)
        -> ListarAccionesPendientesCommand(actor, empresaId, limite)
        -> canonical constructor rejects null empresaId (defense in depth)
     -> ListarAccionesPendientesService
        -> AiTenantExceptionTranslator.resolveForSelector(port, actor, empresaId)
        -> ActorEmpresaScopePort.resolver(actor, empresaId)
            -> actor owns empresaId -> EmpresaId
            -> actor does NOT own empresaId -> TenantScopeViolationException
               -> AsistenteTenantException.tenantSelectorRechazado(actor, empresaId)
               -> GlobalExceptionHandler -> HTTP 403
     -> ListPendingAiAccionesPort.listPendingByActor(actor, empresaId, limite)
        -> AiAccionSpringDataRepository.findBySolicitadaPorAndEstadoAndEmpresaId(
               actor, PENDING, empresaId, pageable)  [SQL trust boundary]
     -> AccionPendienteResponse[] to caller (HTTP 200)
```

## File Changes

| File | Action | Description |
|---|---|---|
| `application/src/main/java/com/ar/crm2/application/security/ActorContext.java` | Modify | Keep `usuarioId` as the identity anchor and remove tenant scope from the actor context. |
| `infrastructure/src/main/java/com/ar/crm2/security/KeycloakJwtActorContextMapper.java` | Modify | Stop mapping `empresa_id` into `ActorContext`; tenant input is handled by endpoint-specific request parameters or resource ownership. |
| `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/ai/AiController.java` | Modify | Pass actor identity separately; receive explicit `empresaId` request parameters for non-chat endpoints that require tenant selection. |
| `application/src/main/java/com/ar/crm2/application/ai/command/{AnalizarChatCommand,ConfirmarAccionCommand,RechazarAccionCommand,ObtenerConversacionAsistenteCommand,ListarAccionesPendientesCommand}.java` | Modify | Reflect identity-first commands and endpoint-specific tenant inputs. |
| `application/src/main/java/com/ar/crm2/application/ai/service/{AnalizarChatService,ConfirmarAccionService,RechazarAccionService,ObtenerConversacionAsistenteService,ListarAccionesPendientesService}.java` | Modify | Resolve tenant from resource ownership or explicit list selection. |
| `application/src/main/java/com/ar/crm2/application/empresa/service/ActorEmpresaScopeService.java` and `domain/src/main/java/com/ar/crm2/model/policy/EmpresaPermitidaPolicy.java` | Modify | Support ownership validation plus the documented ambiguity rule instead of actor-wide fallback for resource flows. |
| `application/src/main/java/com/ar/crm2/application/empresa/port/in/ActorEmpresaScopePort.java` | Create | Empresa-owned inbound port (interface) that AI services depend on; implemented by `ActorEmpresaScopeService`. |
| `application/src/main/java/com/ar/crm2/application/ai/port/in/AiTenantResolver.java` + `application/src/main/java/com/ar/crm2/application/ai/service/DefaultAiTenantResolver.java` | Delete | Replaced by the Empresa-owned port + a small package-private `AiTenantExceptionTranslator` helper that translates `TenantScopeViolationException` into `AsistenteTenantException` at the AI service call site. The previous AI-specific resolver layer was redundant indirection. |
| `application/src/main/java/com/ar/crm2/application/ai/service/{AnalizarChatService,ConfirmarAccionService,RechazarAccionService,ObtenerConversacionAsistenteService,ObtenerAccionService,ListarAccionesPendientesService,ListarConversacionesAsistenteService,RegistrarMensajeAsistenteService}.java` | Modify | Depend on `ActorEmpresaScopePort` (not on `ActorEmpresaScopeService` directly and not on `AiTenantResolver`). Services that call `resolver(...)` use `AiTenantExceptionTranslator.resolve(...)` to keep the AI-public exception type. |
| `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/ai/AiRestCommandMapper.java` | Create | REST-side mapper that builds application commands from the AI REST controller's inbound payload (path variables, query parameters, JSON body, and the trusted `ActorContext`). The controller does NOT instantiate commands directly — every command is built through this mapper. |
| `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/{AiAccionRepositoryAdapter.java,repository/AiAccionSpringDataRepository.java}` | Modify | Enforce pending-action reads by actor and authoritative tenant. |
| `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/ai/AiControllerIT.java` and `application/src/test/java/com/ar/crm2/application/ai/service/*` | Modify | Add multi-company mismatch and ambiguity coverage per endpoint. |

## Interfaces / Contracts

```java
record AnalizarChatCommand(UUID actorUsuarioId, UUID activeEmpresaId, String waConversacionId, String mensajeUsuario) {}
record ListarAccionesPendientesCommand(UUID actorUsuarioId, UUID empresaId, int limite) {}
```

Resource-bound services MUST load the target resource first and derive `empresaId` from that resource. Only `GET /api/ai/acciones` consumes a request-selected tenant.

## Testing Strategy

| Layer | What to Test | Approach |
|---|---|---|
| Unit | Resource-first tenant derivation, actor-owns-tenant checks, ambiguous list rejection | JUnit + Mockito in `application`/`domain`. |
| Integration | `/chat` persists under `canalEmpresaId`; confirm/reject/get ignore mismatching active context; `/acciones` filters by actor + tenant | `AiControllerIT` plus repository adapter tests in `infrastructure`. |
| E2E | Not available in this repo | Rely on `mvn -pl infrastructure -am verify` integration evidence. |

## Migration / Rollout

No migration required. Existing `ai_*` rows already carry `empresa_id`; this PR4 correction changes tenant resolution and authorization behavior only.

## Open Questions

- [x] Resolved (PR7): The user's earlier "auto-resolve single-company actor" suggestion was REJECTED. The selector is REQUIRED across the board, including single-company actors. The DTO's `@NotNull empresaId` enforces this at the REST boundary; the application command's canonical constructor enforces it as defense in depth; the persistence layer enforces it via the `findBySolicitadaPorAndEstadoAndEmpresaId` SQL trust boundary.
- [x] Resolved (Slice 10): The fresh-audit timeout-overclaim is fixed by **removing** the unsafe `CompletableFuture.supplyAsync(...).orTimeout(...)` wrap and letting Spring AI's provider HTTP timeout own the wall-clock gate. The per-turn wall-clock budget is still surfaced through `OpenAiChatAdapter.getTurnTimeoutMillis()` and `application.yml` `ai-assistant.turn-timeout-ms` as a verifiable configuration surface. The `AiAssistantException -> HTTP 502` mapping is now registered in `GlobalExceptionHandler`, closing audit #3.
- [ ] Open (Slice 10 follow-up): real per-tool-call budget enforcement requires a custom `CallAdvisor` injected into the Spring AI `ChatClient` chain (Spring AI 2.0 does not expose a built-in knob). The `ToolCallBudgetPolicy` is currently a verified configuration surface (`assertToolCallBudget(int)` seam); wiring it into a custom `CallAdvisor` is tracked as a follow-up.
- [x] Resolved (Slice 14 — read-tools ports-vs-usecase decision/alignment): the AI read tools (`BuscarClientePorTelefonoTool`, `ListarColumnasTableroTool`, `ObtenerMensajesRecientesTool`, `ObtenerResumenChatTool`) intentionally depend on **AI-bounded-context outbound read ports** (e.g. `ContactoLecturaPort`, `WhatsappMensajeLecturaPort`) plus the trusted `AiToolContextPort` — they do NOT wrap an inbound use case. The AI read ports are owned by the AI bounded context (`application.ai.port.out`) and expose AI-specific shapes (e.g. `findByEmpresaIdAndTelefono(EmpresaId, String)` takes the trusted tenant resolved from `AiToolContext`; `findByConversacionId(UUID)` returns the application-owned `WhatsappMensajeResumen` projection). Read tools have no business logic to wrap, so calling an inbound use case would be unnecessary indirection. The propose tool (`ProponerAccionTool`) is the **only** tool that calls an inbound use case (`ProponerAccionUseCase`) because staging has business logic to coordinate. **No tool depends on a real CRM mutation use case** — that is the safety boundary and is pinned by `AiToolArchitectureContractTest` (8 cases) plus the per-tool `constructor_doesNotInjectRealMutationUseCases` and `@Tool` description tests. The architecture-decision table above documents the contract; the spec requirement `Tool Surface Is Read-Only or Propose-Only` is updated to spell out the inbound-port vs outbound-port distinction.
