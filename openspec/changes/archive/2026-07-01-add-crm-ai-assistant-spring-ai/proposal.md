# Proposal: AI Assistant (Spring AI)

## Intent

Add a tenant-scoped AI assistant to the CRM that answers questions over Contacts, Fichas, Tableros and Conversations, and proposes — but never executes — mutations. Built on Spring AI with Anthropic as the default model. Mutations require explicit human confirmation routed through the application layer, never triggered by the model. Coexists with the existing WhatsApp-scoped `SugerirRespuestaUseCase` (kept intact in phase 1).

## Quick path

1. New bounded context `ai-assistant` in `application/src/main/java/com/ar/crm2/application/ai/` (commands, ports in/out, services, exceptions).
2. New domain entities and VOs under `domain/src/main/java/com/ar/crm2/model/{entity,vo,enums}/ia/`.
3. New infrastructure adapters: REST `AiController` (`adapter/in/ai/`), Spring AI client adapter (`adapter/out/ai/`), `@Tool` adapters (`adapter/in/tool/ai/`).
4. New persistence via additive DDL in `boot/src/main/resources/schema.sql`: `ai_*` tables, with phase-1 memory limited to the existing `ai_memoria` read model and mutations guarded off by config.
5. Manual wiring in `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` (no component scanning, follows project convention).
6. Confirmation flow: `ProponerAccionTool` stages an `AiAccion` in PENDING state and returns its id/status to the model; the user confirms later via REST/UI through `ConfirmarAccionUseCase` — never via another AI tool call.

## Scope

### In Scope
- AI REST endpoint per user, authenticated via `ActorContext` actor identity. Explicit tenant comes from request parameters where needed; resource-bound endpoints derive tenant from the addressed resource.
- Spring AI `ChatClient` adapter with system prompt, conversation history, and tool-calling loop.
- `@Tool` adapters for read-only CRM queries: Contacto, Ficha, Tablero, Columna, Conversacion.
- `ProponerAccionTool` -> `RegistrarPropuestaAccionUseCase` (PENDING lifecycle).
- `ConfirmarAccionUseCase` / `RechazarAccionUseCase` dispatching to existing real-entity use cases.
- Additive `ai_*` DDL; phase 1 reads active `ai_memoria` rows only, rejects memory mutations, and does not ship embeddings.
- Per-conversation token budget, max tool calls per turn, hard timeouts.
- Phase 1 keeps WhatsApp `SugerirRespuestaUseCase` untouched.

### Out of Scope
- Embeddings / vector recall (phase 2).
- Cross-tenant or company-wide memory; contact-scoped retrieval and fact-table extraction stay deferred beyond the current phase-1 conversation-scoped read API.
- Replacing or modifying `SugerirRespuestaUseCase` and `AnthropicSugerenciaAdapter`.
- Model-driven auto-execution of any mutation (forbidden in all phases).
- AI SSE streaming of responses (deferred to a later change).

## Capabilities

> Contract with the `sdd-spec` phase. New capabilities become `openspec/specs/<name>/spec.md`; modified capabilities need delta specs.

### New Capabilities
- `ai-assistant`: conversational AI over CRM data with tool calling, system prompt, and history.
- `ai-memory`: private AI memory backed by `ai_memoria`; phase 1 exposes actor+tenant+WhatsApp-conversation reads only, while contact-scoped retrieval and fact extraction remain deferred.
- `ai-action-proposal`: staged, human-confirmed action lifecycle (`AiAccion`: `PENDING -> CONFIRMED/REJECTED/EXPIRED`, then `CONFIRMED -> EXECUTED/FAILED` after dispatch).

### Modified Capabilities
- `security`: `ActorContext` carries actor identity only. AI tenant scope is request-explicit or resource-derived, then validated through the Empresa-owned `ActorEmpresaScopePort` where an explicit tenant is required.
- `tablero`: AI tools may read Tableros/Fichas/Columnas; mutations via `MoverColumnaFicha` only after confirmation.

## Approach

Hexagonal-compliant per `MASTER_RULES.md`, `APPLICATION_RULES.md`, `INFRASTRUCTURE_RULES.md`. The current module layout is `boot -> infrastructure -> application -> domain`, with `whatsapp` as a sibling module that `application` cannot import. New `ai-assistant` context lives in `application/ai/*` and depends only on `domain`. All CRM cross-module reads (e.g. WhatsApp entities if ever needed) go through new out-ports implemented in `infrastructure`.

`@Tool` adapters are input adapters living in `infrastructure/adapter/in/tool/ai/`. They call application use cases via injected `UseCase` ports-in, never JPA repositories directly, never entities. The Spring AI `ChatClient` is an output adapter in `infrastructure/adapter/out/ai/`. The `AiController` is the REST entry point. Domain entities (`AiConversacion`, `AiMensaje`, `AiMemoria`, `AiAccion`, `Hecho`) live in `domain/.../ia/` and are framework-free.

Per Engram observation #2252, confirmation is a two-step user-driven flow: the model never confirms its own proposals. `ProponerAccionTool` creates `AiAccion(PENDING)` and returns `{id, status, accionTipo, payloadResumen}` to the model so the model can present it. The user later sends a REST/UI request to `ConfirmarAccionUseCase`, which loads the staged `AiAccion`, verifies tenant + requester + state, transitions the action to `CONFIRMED`, dispatches to the real use case (`CreateContactoUseCase`, `CreateTratoUseCase`, `CreateTareaUseCase`, `MoverColumnaFichaUseCase`), then persists the final post-dispatch outcome as `EXECUTED` or `FAILED`. Rejection stays `PENDING -> REJECTED`, expiry stays `PENDING -> EXPIRED`. Replay protection in phase 1 is state/version based; there is no separate `idempotency_key` contract at the confirmation endpoint. `AiAccion` lifecycle is owned by application use cases — domain entity holds no Spring AI types.

## Affected Areas

| Area | Layer | Impact |
|------|-------|--------|
| `domain/src/main/java/com/ar/crm2/model/entity/ia/` | domain | New: `AiConversacion`, `AiMensaje`, `AiMemoria`, `AiAccion`, `Hecho`. |
| `domain/src/main/java/com/ar/crm2/model/vo/ia/` | domain | New: `AiConversacionId`, `AiMensajeId`, `AiMemoriaId`, `AiAccionId`, `HechoId`, `TipoHecho`. |
| `domain/src/main/java/com/ar/crm2/model/enums/` | domain | New: `TipoAccionAi`, `EstadoAccionAi`, `RolMensajeAi`. |
| `application/src/main/java/com/ar/crm2/application/ai/{command,port/in,port/out,service,exception}/` | application | New: full per-entity folders per `APPLICATION_RULES.md`. |
| `application/src/main/java/com/ar/crm2/application/security/ActorContext.java` | application | Used as actor identity only; no authoritative AI tenant lives in `ActorContext`. |
| `infrastructure/src/main/java/com/ar/crm2/adapter/in/ai/` | infrastructure | New: `AiController`, DTOs, exception handler. |
| `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/` | infrastructure | New: `BuscarContactoTool`, `BuscarFichaTool`, `BuscarTableroTool`, `ProponerAccionTool`, etc. |
| `infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/` | infrastructure | New: `SpringAiClientAdapter`, JPA repos, mappers. |
| `infrastructure/src/main/java/com/ar/crm2/adapter/in/ai/dto/` | infrastructure | New: REST request/response DTOs. |
| `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` | boot | Modified: manual wiring for AI beans. |
| `boot/src/main/resources/schema.sql` | boot | Modified: additive `ai_*` DDL. |
| `infrastructure/pom.xml` | infrastructure | Modified: add `spring-ai-starter-model-anthropic`. |
| `boot/src/main/resources/application.yml` | boot | Modified: `spring.ai.anthropic.api-key`, model name. |
| `infrastructure/src/test/java/com/ar/crm2/adapter/in/ai/` | infrastructure | New: `AiControllerIT` (parallel to `TableroControllerIT`). |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Model bypasses confirmation and tries to mutate directly | Med | Only `ProponerAccionTool` may stage mutations; LLM-facing tool surface is read-only + propose. Real mutation use cases are never exposed as tools. |
| Tenant leak via AI memory or tool calls | High | Resource-bound flows derive tenant from the resource; explicit-tenant flows pass `empresaId` as request data and validate it through `ActorEmpresaScopePort`; `ai_*` tables carry explicit `empresa_id` + `actor_usuario_id`; tool inputs validated server-side. |
| Ambiguous explicit tenant selection | High | Keep actor context identity-only; require explicit `empresaId` where the endpoint is not resource-bound and validate/resolve it through the Empresa-owned `ActorEmpresaScopePort`. |
| `application` accidentally imports `whatsapp` | Med | New out-ports for any cross-module reads; explicit review gate in PR. |
| Token cost / runaway tool-call loops | Med | Per-conversation token budget, max tool calls per turn (default 5), hard timeout per turn, conversation TTL. |
| LLM hallucinates CRM data | High | Tools return rich typed results; system prompt forbids free-text claims about data not returned by tools; responses include tool-call provenance. |
| Confirmation replay / double-execution | Med | `AiAccion` transitions out of `PENDING` exactly once; confirmation persists `EXECUTED` or `FAILED` after dispatch; replays are rejected by the non-PENDING state and optimistic version checks. |
| Unbounded memory growth | Low | TTL + per-user cap; compaction deferred to phase 2; phase 1 has no writes. |
| `@Tool` adapter breaks layer rules | Med | Tools live in `infrastructure/adapter/in/tool/ai/`, call application use cases only, never JPA. |
| Two AI surfaces confuse users | Low | Keep `SugerirRespuestaUseCase` intact in phase 1; document separation; future change may consolidate. |
| Spring AI version drift / API breaks | Med | Pin Anthropic model + SDK version; integration test against stub adapter (`FakeSpringAiClientAdapter` for tests). |
| Test coverage gaps on confirmation flow | Med | RED-first: tests for `RegistrarPropuestaAccionUseCase` and `ConfirmarAccionUseCase` precede implementation per `openspec/config.yaml` rules. |

## Rollback Plan

1. Revert PR.
2. Down-migrate: drop `ai_*` tables (`ai_conversacion`, `ai_mensaje`, `ai_resumen_contexto`, `ai_memoria`, `ai_accion`).
3. Remove `spring-ai-starter-model-anthropic` from `infrastructure/pom.xml`.
4. Remove AI bean wiring entries from `boot/.../config/WiringConfig.java`.
5. Confirm `SugerirRespuestaUseCase` and `AnthropicSugerenciaAdapter` still work (no shared code paths with new AI context).
6. Remove `spring.ai.anthropic.*` keys from `application.yml`.
7. Feature-flag fallback: keep `ai-assistant.enabled=false` default in `application.yml` so a misconfigured deploy cannot enable AI accidentally.

## Dependencies

- `spring-ai-starter-model-anthropic` (or compatible starter) on `infrastructure`.
- Anthropic API key provisioned via env var / secret manager, bound to `spring.ai.anthropic.api-key`.
- PostgreSQL `ai_*` schema (additive DDL in `boot/src/main/resources/schema.sql`).
- Existing application ports (`CreateContactoUseCase`, `CreateTratoUseCase`, `CreateTareaUseCase`, `MoverColumnaFichaUseCase`) for confirmation dispatch.
- Empresa-owned `ActorEmpresaScopePort` for validating explicit tenant scope where a resource does not provide the tenant.

## Success Criteria

- [ ] AI endpoint answers CRM questions using only data returned by tools; no free-text CRM claims.
- [ ] All AI requests and tool calls tenant-scoped via explicit request tenant or resource-derived tenant, with `ActorEmpresaScopePort` validation where explicit tenant is required; no cross-tenant reads/writes.
- [ ] Mutations execute only after user confirmation of a staged `AiAccion`; double-confirm is impossible.
- [ ] WhatsApp `SugerirRespuestaUseCase` and `AnthropicSugerenciaAdapter` unchanged and green.
- [ ] `mvn -pl domain,application -am test` and `mvn -pl infrastructure -am verify` green; coverage non-decreasing.
- [ ] `ai_*` tables exist; `ai_memoria` schema is present, phase-1 reads stay conversation-scoped, and memory mutations are disabled by default.
- [ ] `AiControllerIT` integration test covers happy path + confirmation + tenant-leak attempt + replay rejection.
- [ ] `spring.ai.anthropic.api-key` absent in test profile; tests run against `FakeSpringAiClientAdapter`.

## Open Product Questions (block spec)

- Q1 — Tenant scoping: resolved. Actor context carries actor identity only; endpoints either pass an explicit `empresaId` request parameter or derive tenant from the addressed resource, with Empresa-owned scope validation when explicit tenant is required.
- Q2 — Conversation TTL and max turns per conversation in phase 1.
- Q3 — Who may confirm an `AiAccion` beyond the original requester? Same `usuarioId` only, or any user in the same `empresaId` with the right role?
- Q4 — Token budget per request and per user/day; cost guardrails.
- Q5 — Streaming (SSE) responses in phase 1, or full response only?
- Q6 — Single conversation per user/contact vs multi-conversation in phase 1?
- Q7 — Memory retention policy: TTL, max facts per user, deletion triggers, GDPR/right-to-erasure path.
- Q8 — Rate limiting: per user, per tenant, per IP, or all three?
- Q9 — Audit log retention for proposals, confirmations, rejections, expirations.
- Q10 — Observability minimum in phase 1: which logs/metrics/traces are mandatory?

---

## Appendix A — Phase Roadmap (PR4 continuation)

- **Phase 1 (this change)** — Read-only Q&A + propose-only actions, schema for memory, no embeddings, no streaming. `ai-assistant.enabled=true` opt-in. WhatsApp surface untouched.
- **Phase 2 (later change)** — Persistent memory writes, retrieval over recent messages, basic fact extraction, conversation compaction, observability hardening.
- **Phase 3 (later change)** — Embeddings + vector recall, streaming SSE, optional multi-tenant memory sharing, optional migration of WhatsApp `SugerirRespuestaUseCase` onto the same assistant.

## Appendix B — Proposed Package Layout

```
domain/src/main/java/com/ar/crm2/model/
  entity/ia/  AiConversacion, AiMensaje, AiMemoria, AiAccion, Hecho
  vo/ia/      AiConversacionId, AiMensajeId, AiMemoriaId, AiAccionId, HechoId
  enums/      TipoAccionAi, EstadoAccionAi, RolMensajeAi, TipoHecho

application/src/main/java/com/ar/crm2/application/ai/
  command/    EnviarMensajeAiCommand, ConfirmarAccionCommand, RechazarAccionCommand
  port/in/    EnviarMensajeAiUseCase, ConfirmarAccionUseCase, RechazarAccionUseCase,
              RegistrarPropuestaAccionUseCase, GetConversacionAiUseCase, ListarAccionesPendientesUseCase
  port/out/   SaveConversacionAiPort, FindConversacionAiPort, SaveMensajeAiPort,
              FindAccionAiPort, SaveAccionAiPort, SpringAiGateway
  service/    EnviarMensajeAiService, ConfirmarAccionService, RechazarAccionService,
              RegistrarPropuestaAccionService, GetConversacionAiService, ListarAccionesPendientesService
  exception/  AiAccionNoConfirmableException, AiAccionNoEncontradaException, AiTenantMismatchException

infrastructure/src/main/java/com/ar/crm2/adapter/
  in/ai/              AiController, dto/* (EnviarMensajeRequest, AiMensajeResponse, AccionPendienteResponse)
  in/tool/ai/         BuscarContactoTool, BuscarFichaTool, BuscarTableroTool, ProponerAccionTool, ...
  out/ai/             SpringAiClientAdapter, JpaAiConversacionRepository, JpaAiMensajeRepository,
                      JpaAiAccionRepository, JpaAiMemoriaRepository, Ai*Mapper, FakeSpringAiClientAdapter (test)

boot/src/main/java/com/ar/crm2/config/
  WiringConfig.java   (add @Bean methods for every AI use case + adapter)
boot/src/main/resources/
  schema.sql          (additive ai_* DDL)
  application.yml     (spring.ai.anthropic.* + ai-assistant.enabled flag)
```

## Appendix C — Confirmation Flow (per Engram #2252)

```
User  ->  AiController  ->  EnviarMensajeAiUseCase
                                  |
                                  v
                          ChatClient (Spring AI)
                                  |
                                  v
                          Model picks tool
                                  |
              +-------------------+-------------------+
              v                                       v
   read-only Tool (Buscar*)                  ProponerAccionTool
              |                                       |
              v                                       v
       Spring AI response                   RegistrarPropuestaAccionUseCase
                                                      |
                                                      v
                                              AiAccion(PENDING) in DB
                                                      |
                                                      v
                                  returns {id, status, payloadResumen} to model
                                                      |
                                                      v
                                          Model presents to user

User  ->  AiController  ->  ConfirmarAccionUseCase
                                  |
                                  v
                          Load AiAccion by id
                                  |
                  verify tenant + state == PENDING
                                  |
                                  v
                  dispatch to real use case
                  (CreateContacto / CreateTrato / CreateTarea / MoverColumnaFicha)
                                  |
                                  v
                  AiAccion -> EXECUTED (or FAILED after a confirmed dispatch; REJECTED/EXPIRED stay on the pre-dispatch branch)
```

## Appendix D — Persistence Sketch (`ai_*`)

- `ai_conversaciones` — `id (uuid pk)`, `empresa_id`, `actor_usuario_id`, `contacto_id (nullable)`, `estado`, `creada_en`, `actualizada_en`, `tokens_estimados`.
- `ai_mensajes` — `id`, `conversacion_id fk`, `rol (USER/ASSISTANT/SYSTEM/TOOL)`, `contenido`, `herramienta (nullable)`, `creado_en`.
- `ai_acciones` — `id`, `conversacion_id fk`, `tipo_accion`, `payload_json`, `estado (PENDING/CONFIRMED/REJECTED/EXPIRED/EXECUTED/FAILED)`, `propuesto_por_usuario_id`, `confirmado_por_usuario_id (nullable)`, `entidad_destino_id (nullable)`, `creada_en`, `expirada_en (nullable)`.
- `ai_memoria` — `id`, `actor_usuario_id`, `empresa_id`, `wa_conversacion_id (nullable)`, `contacto_id (nullable)`, `visibilidad`, `contenido`, `origen_tipo`, `origen_id`, `version`, `creado_en`, `actualizado_en`, `expires_at`, `superseded_by`, `superseded`, `expirada`. Phase 1: reads only; save/delete are rejected unless `ai-assistant.phase1.memory-writes-enabled=true`.

Indexes: `(actor_usuario_id, empresa_id, wa_conversacion_id, superseded, expirada)` and `(expires_at)`.

## Appendix E — Code Skeleton Hints (illustrative, not exhaustive)

```java
// application/ai/port/in/RegistrarPropuestaAccionUseCase.java
public interface RegistrarPropuestaAccionUseCase {
    AiAccionId registrar(RegistrarPropuestaAccionCommand cmd);
}

// application/ai/port/in/ConfirmarAccionUseCase.java
public interface ConfirmarAccionUseCase {
    AccionConfirmadaResult confirmar(ConfirmarAccionCommand cmd);
}

// infrastructure/adapter/in/tool/ai/ProponerAccionTool.java
@Component
class ProponerAccionTool {
  private final RegistrarPropuestaAccionUseCase registrar;
  @Tool(name = "proponerAccion", description = "Stages a CRM mutation for human confirmation. NEVER executes directly.")
  public ProponerAccionResult proponer(ProponerAccionArgs args, ToolContext ctx) {
    var actor = actorContext.from(ctx);
    return new ProponerAccionResult(
        registrar.registrar(new RegistrarPropuestaAccionCommand(actor, args)));
  }
}

// infrastructure/adapter/out/ai/SpringAiClientAdapter.java
class SpringAiClientAdapter implements SpringAiGateway {
  private final ChatClient client;
  public AiMensaje responder(AiConversacion conv, List<MensajeConversacional> history, List<Object> tools) {
    return client.prompt(systemPrompt).messages(history).tools(tools).call().entity(AiMensaje.class);
  }
}
```
