# Security Specification

> Delta from `add-crm-ai-assistant-spring-ai`. On archive, the requirements below merge into the main `openspec/specs/security/spec.md`.

## Purpose

Extend the security context so the AI assistant can anchor authentication on actor identity and authorize tenant access from owned resources. Existing endpoint coverage behavior remains unchanged.

## ADDED Requirements

### Requirement: ActorContext Exposes Authenticated Identity Only

The system MUST expose authenticated actor identity through `ActorContext.usuarioId`. Actor context MUST NOT carry tenant scope; explicit tenant selection is request data at the REST boundary, while resource-bound AI flows derive tenant authority from the addressed resource.

#### Scenario: Authenticated actor context omits tenant scope

- GIVEN a JWT for an authenticated CRM user
- WHEN the request enters the application layer
- THEN `ActorContext.usuarioId` MUST identify the actor
- AND actor context MUST NOT expose a tenant field

#### Scenario: Token without empresaId still builds actor context

- GIVEN a JWT missing the `empresa_id` claim
- WHEN the request is assembled into an `ActorContext`
- THEN the system MUST still preserve authenticated actor identity
- AND resource-bound AI flows MUST remain eligible to authorize from owned resources

### Requirement: Resource-Bound AI Commands Authorize from Owned Resources

The system MUST authorize AI commands that address or derive from a resource by using the owned resource's tenant together with authenticated actor identity. The system MUST NOT require tenant scope from `ActorContext`.

#### Scenario: Resource-bound AI command without tenant hint

- GIVEN an authenticated actor context
- AND the AI command targets a resource owned by one of the actor's companies
- WHEN the AI use case authorizes the request
- THEN the use case MUST derive tenant scope from the owned resource

#### Scenario: Cross-tenant resource access is rejected

- GIVEN an authenticated actor
- AND the addressed AI resource belongs to a company the actor does not own
- WHEN the AI use case authorizes the request
- THEN the system MUST reject the request with a tenant or ownership error
