# AI Assistant Specification

## Purpose

Define the conversational AI over CRM data exposed to authenticated users. Built on Spring AI, it anchors authentication on `ActorContext.usuarioId`, uses resource-first tenant resolution for resource-bound AI flows, and keeps mutations staged for later human confirmation.

## Requirements

### Requirement: Resource-First Tenant Resolution for AI Endpoints

The system MUST authenticate AI requests using `ActorContext.usuarioId`. `ActorContext` MUST NOT carry tenant scope. For AI endpoints that operate on or derive from a resource, the system MUST resolve tenant scope from the owned resource instead of requiring an actor-context tenant value.

#### Scenario: Chat turn uses WhatsApp conversation tenant

- GIVEN an authenticated user linked to multiple companies
- AND the requested WhatsApp conversation belongs to company `E2`
- WHEN the user posts a message to the AI endpoint
- THEN the system MUST process the turn within tenant `E2`
- AND persisted AI conversation state MUST be stored under tenant `E2`

#### Scenario: Absence of actor-context tenant does not block valid chat

- GIVEN an authenticated user with identity-only `ActorContext`
- AND the requested WhatsApp conversation belongs to a company owned by that user
- WHEN the user posts a message to the AI endpoint
- THEN the system MUST accept the request
- AND MUST derive tenant scope from the WhatsApp conversation resource

#### Scenario: Cross-tenant resource access is rejected

- GIVEN an authenticated user
- AND the requested WhatsApp conversation belongs to a company the user does not own
- WHEN the user posts a message to the AI endpoint
- THEN the system MUST reject the request with a 403-style tenant or ownership error

### Requirement: Tool-Only Data Access

The system MUST restrict AI data access to results returned by registered `@Tool` adapters. The model MUST NOT assert CRM facts in free text without a corresponding tool call in the same turn.

#### Scenario: Model answers from tool result

- GIVEN a registered `BuscarContactoTool`
- WHEN the user asks "How many contacts does Acme have?"
- THEN the model MUST invoke the tool before stating a number
- AND the response MUST cite the tool's returned id(s)

#### Scenario: No tool covers the question

- GIVEN no registered tool covers the user's question
- WHEN the model generates a response
- THEN the model MUST answer that the data cannot be retrieved
- AND MUST NOT fabricate CRM values

### Requirement: Tool Surface Is Read-Only or Propose-Only

The system MUST expose only read or propose tools to the model. Mutation use cases SHALL NOT be registered as `@Tool` adapters.

#### Scenario: Mutation use case is not a tool

- GIVEN mutation use cases such as `CreateContactoUseCase` or `MoverColumnaFichaUseCase`
- WHEN the AI assistant's tool registry is built
- THEN the registry MUST NOT include any mutation use case as an `@Tool`

#### Scenario: Read tools depend on AI-bounded-context outbound read ports, not inbound use cases

- GIVEN the four AI read tools (`BuscarClientePorTelefonoTool`, `ListarColumnasTableroTool`, `ObtenerMensajesRecientesTool`, `ObtenerResumenChatTool`)
- WHEN the AI tool registry is built
- THEN each read tool MUST depend ONLY on the trusted `AiToolContextPort` (where tenant scope is required) and ONE AI-bounded-context outbound read port (`ContactoLecturaPort`, `ColumnaLecturaPort`, `WhatsappMensajeLecturaPort`, or `FindAiResumenPort`)
- AND MUST NOT depend on any inbound use case — the AI read ports are owned by the AI bounded context (`application.ai.port.out`) and expose AI-specific shapes that the read tools map directly into tool responses
- AND MUST NOT depend on any save / update / delete AI port
- AND the tool's `@Tool` description MUST declare "Read-only" so the model never proposes the tool as a mutation entry point
- AND this contract MUST be pinned by an architecture-guard test that fails the build if a future refactor injects a forbidden dependency

#### Scenario: Propose tool depends ONLY on the inbound propose use case, not on any real mutation use case

- GIVEN the AI propose tool (`ProponerAccionTool`)
- WHEN the AI tool registry is built
- THEN the propose tool MUST depend ONLY on the inbound `ProponerAccionUseCase`
- AND MUST NOT depend on any read port (read access is owned by the use case's internal service)
- AND MUST NOT depend on any real CRM mutation use case (`CreateContactoUseCase`, `CreateTratoUseCase`, `CreateTareaUseCase`, `MoverColumnaFichaUseCase`, etc.)
- AND the tool's `@Tool` description MUST declare the propose-only invariant (PENDING + human confirmation) so the model never proposes the tool as a direct mutation entry point
- AND this contract MUST be pinned by an architecture-guard test that fails the build if a future refactor injects a forbidden dependency

### Requirement: Conversation History and System Prompt

The system MUST send the system prompt plus the active conversation's history to the model on every turn, in chronological order.

#### Scenario: New turn includes history

- GIVEN a conversation with N prior messages
- WHEN the user sends message N+1
- THEN the model call MUST include all N prior messages and the system prompt

### Requirement: Per-Turn Safety Limits (Honest Configuration + Follow-up Enforcement)

The system MUST expose the per-conversation token budget, the max tool-call count per turn, and the per-turn wall-clock timeout as validated configuration surfaces, and MUST wire them to the Spring AI / OpenAI provider where the framework supports it. Real loop-stopping enforcement of the per-turn tool-call budget requires a custom `CallAdvisor` injected into the Spring AI `ChatClient` chain; this is a documented follow-up (see `tasks.md` 9.10 + 10.0 and `design.md` open questions) and is NOT a runtime contract in this slice.

#### Scenario: Per-turn tool-call budget is verified through the adapter test seam

- GIVEN the configured max tool calls per turn is `N`
- WHEN a caller invokes `OpenAiChatAdapter.assertToolCallBudget(N+1)`
- THEN the adapter MUST throw `AiAssistantException` naming the configured budget and the offending count so the contract is verifiable end-to-end through a single seam

#### Scenario: Per-turn timeout is verified through the adapter test seam

- GIVEN the configured per-turn wall-clock budget is `T` milliseconds
- WHEN a caller invokes `OpenAiChatAdapter.getTurnTimeoutMillis()`
- THEN the adapter MUST return `T` so the runtime timeout knob is readable from the adapter boundary

#### Scenario: Provider wall-clock timeout is the runtime gate

- GIVEN `spring.ai.openai.chat.options.timeout` is configured to `T` (ISO-8601)
- WHEN the model exceeds `T` before producing a response
- THEN Spring AI's provider HTTP client MUST abort the call, the `OpenAiChatAdapter` MUST map the failure to `AiAssistantException`, and the REST boundary MUST surface HTTP 502 Bad Gateway

### Requirement: Tool Input Validation Server-Side

The system MUST validate every `@Tool` argument against authenticated actor identity and the tenant ownership of the addressed resource. Tenant values from request context MUST NOT override resource ownership.

#### Scenario: Tool input is cross-tenant

- GIVEN an authenticated actor
- WHEN a tool receives an entity id that belongs to `E2`
- THEN the system MUST reject the call with a tenant mismatch error

#### Scenario: Hint tenant conflicts with owned resource

- GIVEN an authenticated actor with an explicit tenant hint `E1`
- AND the addressed resource belongs to `E2`
- WHEN the tool-backed AI flow resolves the resource
- THEN the system MUST treat the resource tenant as authoritative
- AND MUST reject the request if the actor is not authorized for `E2`

### Requirement: AI Conversation Retrieval Uses Resource Tenant

The system MUST authorize `GET /api/ai/conversaciones/{id}` using the stored `AiConversacion.empresaId` together with the authenticated actor identity.

#### Scenario: Owner retrieves own conversation

- GIVEN an AI conversation owned by the authenticated actor in tenant `E1`
- WHEN the actor requests that conversation by id
- THEN the system MUST return the conversation and its ordered history

#### Scenario: Conversation belongs to another tenant

- GIVEN an AI conversation stored under tenant `E2`
- WHEN an actor not authorized for `E2` requests that conversation
- THEN the system MUST reject the request with a 403-style tenant or ownership error

### Requirement: Coexistence with WhatsApp AI Surface

The system MUST NOT modify `SugerirRespuestaUseCase` or `AnthropicSugerenciaAdapter` as part of this change.

#### Scenario: WhatsApp surface untouched

- GIVEN the WhatsApp AI surface is in production
- WHEN this change ships
- THEN `SugerirRespuestaUseCase` and `AnthropicSugerenciaAdapter` MUST remain unchanged in behavior
