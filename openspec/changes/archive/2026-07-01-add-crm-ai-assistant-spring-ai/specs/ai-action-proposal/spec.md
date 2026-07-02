# AI Action Proposal Specification

## Purpose

Define the lifecycle for AI-proposed CRM mutations. The model stages an action via a propose-only tool; a human later confirms or rejects it through a REST-driven application use case. The model MUST NEVER confirm its own proposals.

## Requirements

### Requirement: Action Lifecycle States

The system MUST model `AiAccion` with states `PENDING`, `CONFIRMED`, `REJECTED`, `EXPIRED`, `EXECUTED`, `FAILED`. Transitions out of `PENDING` MUST be single-shot per action id. Only `CONFIRMED` MAY progress further, and it MUST progress only to `EXECUTED` or `FAILED` after the real CRM mutation dispatch finishes.

#### Scenario: Proposed action starts in PENDING

- GIVEN `ProponerAccionTool` receives a valid propose call
- WHEN the tool returns
- THEN the system MUST persist an `AiAccion` with `estado = PENDING`
- AND the tool MUST return `{id, status, accionTipo, payloadResumen}` to the model

#### Scenario: State transition is single-shot

- GIVEN an `AiAccion` with `estado = PENDING`
- WHEN `ConfirmarAccionUseCase` or `RechazarAccionUseCase` runs successfully
- THEN the system MUST move the action to `CONFIRMED` or `REJECTED`
- AND a second transition MUST be rejected as `AiAccionNoConfirmableException`

#### Scenario: Confirmed action records dispatch outcome

- GIVEN an `AiAccion` that has been moved to `CONFIRMED`
- WHEN the underlying CRM mutation use case succeeds or fails
- THEN the system MUST persist `EXECUTED` on success or `FAILED` on failure
- AND the persisted row MUST keep the dispatch outcome observable for audit

### Requirement: Confirmation Is User-Driven, Not Model-Driven

The system MUST accept confirmation or rejection only via REST/UI calls to `ConfirmarAccionUseCase` or `RechazarAccionUseCase`. The model MUST NOT trigger confirmation through any `@Tool` call.

#### Scenario: Confirm arrives via REST

- GIVEN an `AiAccion` with `estado = PENDING`
- WHEN an authenticated user posts to the confirmation REST endpoint
- THEN the system MUST dispatch to the underlying mutation use case
- AND MUST persist a final confirmation outcome of `EXECUTED` or `FAILED`

#### Scenario: Tool cannot confirm

- GIVEN the AI assistant's tool registry
- WHEN the registry is built
- THEN the registry MUST NOT include any `@Tool` that mutates `AiAccion.estado` away from `PENDING`

### Requirement: Requester Identity and Resource Tenant Verification on Confirm or Reject

The system MUST load the target `AiAccion` and MUST verify both requester identity and the action resource tenant before dispatching. The action's stored `empresa_id` is the authoritative tenant source for confirm or reject flows; tenant scope MUST NOT be read from `ActorContext`.

#### Scenario: Requester confirms own pending action

- GIVEN an `AiAccion` in `PENDING`
- AND the action was requested by the authenticated actor
- AND the action belongs to tenant `E1`
- WHEN the actor confirms the action
- THEN the system MUST dispatch the underlying mutation use case
- AND MUST persist `EXECUTED` when the dispatch succeeds

#### Scenario: Tenant mismatch on confirm

- GIVEN an `AiAccion` owned by `empresa_id = E1`
- WHEN an actor not authorized for tenant `E1` attempts confirmation
- THEN the system MUST reject the call with `AiTenantMismatchException`
- AND the action MUST remain `PENDING`

#### Scenario: Identity-only actor context does not block confirm

- GIVEN an `AiAccion` in `PENDING`
- AND the authenticated actor is the allowed requester
- AND `ActorContext` carries actor identity only
- WHEN the actor confirms the action
- THEN the system MUST authorize the request from the action resource and actor identity

#### Scenario: Already confirmed

- GIVEN an `AiAccion` with `estado = CONFIRMED`, `EXECUTED`, `FAILED`, `REJECTED`, or `EXPIRED`
- WHEN a user attempts confirmation again
- THEN the system MUST reject with `AiAccionNoConfirmableException`

### Requirement: Replay Protection for Confirmation

The system MUST protect confirmation replay through the action lifecycle state and optimistic version checks. Phase 1 confirmation MUST NOT require a separate `idempotency_key` request or persistence field.

#### Scenario: Replay after successful confirmation is rejected

- GIVEN a confirmation request already moved the action out of `PENDING`
- WHEN the client retries the confirmation call for the same action id
- THEN the system MUST reject the call as non-confirmable
- AND MUST NOT invoke the underlying mutation use case a second time

### Requirement: Pending Action Listing Is Tenant-Scoped

The system MUST scope `GET /api/ai/acciones` to a single tenant and MUST filter pending actions by both requester identity and tenant. The `empresaId` selector is REQUIRED at the REST boundary; the system MUST NOT guess, MUST NOT auto-resolve single-company actors, and MUST NOT fall back to the first owned company.

#### Scenario: Tenant-scoped pending list

- GIVEN an authenticated actor requesting pending AI actions for tenant `E1`
- WHEN pending actions are listed
- THEN the response MUST include only actions requested by that actor in tenant `E1`

#### Scenario: Missing selector is a controlled 400

- GIVEN an authenticated actor of any tenant profile
- WHEN the actor requests `GET /api/ai/acciones` without supplying an `empresaId` selector
- THEN the system MUST reject the request with HTTP 400 via the `ListarAccionesPendientesRequest` DTO's `@NotNull empresaId` validation
- AND MUST NOT silently return an empty list
- AND MUST NOT auto-resolve to the actor's first (or only) owned tenant

#### Scenario: Supplied selector is not owned by actor

- GIVEN an authenticated actor
- AND the actor does not own the supplied `empresaId`
- WHEN the actor requests pending AI actions
- THEN the system MUST reject the request with HTTP 403 via `AsistenteTenantException.tenantSelectorRechazado(actor, empresaId)`
- AND MUST keep the inbox unreachable for that selector

#### Scenario: Supplied selector is owned by the actor

- GIVEN an authenticated actor who owns the supplied `empresaId`
- WHEN the actor requests pending AI actions with the explicit selector
- THEN the system MUST return the PENDING proposals for that actor in that tenant, capped by the page size
- AND MUST NOT include proposals from any other tenant

### Requirement: Domain AiAccion Holds No Spring AI Types

The system MUST keep `AiAccion` in the domain layer framework-free. `AiAccion` MUST NOT depend on Spring AI, ChatClient, or `@Tool` types.

#### Scenario: Domain compiles without Spring AI

- GIVEN the `domain` module classpath
- WHEN `AiAccion` is compiled
- THEN no Spring AI class SHALL appear in the domain module's imports

### Requirement: Expiry

The system SHOULD expire `PENDING` actions after a configurable `ttl_minutes` and transition them to `EXPIRED`.

#### Scenario: Stale action expires

- GIVEN an `AiAccion` with `estado = PENDING` and `expira_en` in the past
- WHEN a user attempts confirmation
- THEN the system MUST reject with `AiAccionNoConfirmableException`
- AND the action MUST be transitioned to `EXPIRED`
