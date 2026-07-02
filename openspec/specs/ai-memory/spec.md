# AI Memory Specification

## Purpose

Define the phase-1 AI memory contract. The current implementation reads active rows from `ai_memoria` by actor + tenant + WhatsApp conversation, rejects memory mutations while the phase-1 flag is off, and defers contact-scoped retrieval, fact extraction, embeddings, and compaction to later phases.

## Requirements

### Requirement: Active Conversation-Scoped Reads

The system MUST read phase-1 AI memory from `ai_memoria` using the tuple `(actor_usuario_id, empresa_id, wa_conversacion_id)`. The read path MUST return only active conversation-scoped rows and MUST NOT leak rows across tenants or actors.

#### Scenario: Matching active conversation memory is returned

- GIVEN an active `ai_memoria` row owned by `empresa_id = E`, `actor_usuario_id = U`, `wa_conversacion_id = W`
- WHEN an AI request reads memory for `(U, E, W)`
- THEN the system returns that row

#### Scenario: Cross-tenant read is blocked

- GIVEN an AI request from `empresa_id = E1`
- WHEN the request reads memory for `wa_conversacion_id = W` owned by `E2`
- THEN the system MUST return an empty result
- AND MUST NOT raise a not-found or error

#### Scenario: Contact-scoped row is ignored by the conversation read API

- GIVEN a `CONTACTO_SCOPED` memory row without `wa_conversacion_id`
- WHEN the phase-1 conversation read API loads memory for a WhatsApp conversation
- THEN the system MUST NOT return that row

### Requirement: Phase 1 Writes Disabled

The system MUST reject phase-1 memory mutations to `ai_memoria` while `ai-assistant.phase1.memory-writes-enabled = false`. The schema MUST exist and reads against it MUST still succeed.

#### Scenario: Read against empty memory schema

- GIVEN `ai-assistant.phase1.memory-writes-enabled = false`
- WHEN the AI assistant reads memory for a WhatsApp conversation with no rows
- THEN the system returns an empty result without error

#### Scenario: Write attempt rejected

- GIVEN `ai-assistant.phase1.memory-writes-enabled = false`
- WHEN the AI assistant attempts to persist a memory row
- THEN the system MUST reject the attempt
- AND MUST NOT persist the row

### Requirement: Memory TTL

The system MUST enforce a configurable TTL on every memory row and MUST treat rows past their `expires_at` as expired, even when `expirada = false`.

#### Scenario: Expired memory is filtered

- GIVEN a memory row with `expires_at` in the past and `expirada = false`
- WHEN the AI assistant reads conversation-scoped memory
- THEN the system MUST NOT return the expired row

### Requirement: Memory Is Source-Scoped to Conversation History

The system MUST treat `ai_mensajes` as the authoritative conversation history. Memory rows SHALL be derived from messages and MUST NOT duplicate or override message content.

#### Scenario: Memory does not shadow a message

- GIVEN a message `M` in `ai_mensajes` and a memory row that conflicts with `M`
- WHEN the AI assistant composes a response
- THEN the system MUST prefer `M` over the memory row
