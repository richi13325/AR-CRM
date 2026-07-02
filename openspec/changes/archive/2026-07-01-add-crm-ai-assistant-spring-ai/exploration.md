## Exploration: PR4 AI tenant source of truth

### Current State
Tenant scope is not part of actor context. For endpoints that require an explicit tenant, `AiController` receives `empresaId` as request data and passes it to the application command. Application services depend on the Empresa-owned `ActorEmpresaScopePort`, implemented by `ActorEmpresaScopeService`, to validate explicit tenant ownership or apply the existing fallback behavior where that command allows it.

That model is NOT consistent with the real PR4 resource flow:
- `/chat` resolves a tenant first, then separately checks that the WhatsApp conversation belongs to any owned company. If the actor owns multiple companies and `empresaId` is absent/wrong, the service can authorize the chat from company B but persist `AiConversacion`, memory scope, tool context, and staged actions under company A (`AnalizarChatService`).
- `/acciones/{id}/confirmar`, `/acciones/{id}/rechazar`, and `/conversaciones/{id}` load resources that already carry their own `empresaId`, but they still depend on the pre-resolved company hint before calling `requireOwnedBy(...)`. That makes valid access depend on an external active-company value even though the resource already knows its tenant.
- `/acciones` resolves `empresaId`, but the current repository query ignores it and lists pending actions by actor only (`AiAccionRepositoryAdapter` + `AiAccionSpringDataRepository`). In a multi-company case this can surface actions from the wrong company.

The initial OpenSpec security draft expected tenant scope on actor context, but the implementation and final review corrected that model. Tenant is explicit request data or resource-derived.

### Affected Areas
- `application/src/main/java/com/ar/crm2/application/security/ActorContext.java` — carries actor identity only.
- `infrastructure/src/main/java/com/ar/crm2/security/KeycloakJwtActorContextMapper.java` — maps identity claims only for actor context.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/ai/AiController.java` — passes `actor.empresaId().orElse(null)` into every PR4 AI command.
- `application/src/main/java/com/ar/crm2/application/ai/service/AnalizarChatService.java` — mixes actor-owned fallback selection with resource-owned WhatsApp authorization; this is the main abstraction break.
- `application/src/main/java/com/ar/crm2/application/ai/service/{ConfirmarAccionService,RechazarAccionService,ObtenerConversacionAsistenteService}.java` — authorize resource access using a pre-resolved tenant hint instead of the resource tenant as source of truth.
- `application/src/main/java/com/ar/crm2/application/empresa/service/ActorEmpresaScopeService.java` — currently implements the first-owned fallback.
- `domain/src/main/java/com/ar/crm2/model/policy/EmpresaPermitidaPolicy.java` — encodes the fallback rule that is safe only when tenant must be selected without a resource anchor.
- `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/AiAccionRepositoryAdapter.java` — ignores `empresaId` in the pending-actions list query.
- `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/ai/AiControllerIT.java` and `application/src/test/java/com/ar/crm2/application/ai/service/*` — tests assume a single-company actor and do not cover the multi-company mismatch.

### Approaches
1. **Keep tenant scope in actor context as the authoritative AI tenant** — rejected because it treats a request/JWT value as the tenant source of truth for all PR4 endpoints.
   - Pros: Minimal conceptual change; keeps the current controller/service plumbing.
   - Cons: Wrong abstraction for switchable chat/company context; contradicts the current fallback implementation; can persist `/chat` state under the wrong company; makes resource endpoints depend on an external company hint; does not fix `/acciones` cross-company listing.
   - Effort: Low

2. **Use actor identity from JWT, but resource-owned tenant as the primary source of truth** — keep `usuarioId` in `ActorContext`; use resource tenant for resource endpoints; use explicit active-tenant input only where there is no resource anchor yet.
   - Pros: Matches the actual trust boundaries; prevents actor spoofing while avoiding tenant drift; smallest correct architecture for PR4; works with users moving between chats/companies.
   - Cons: Requires endpoint-specific redesign and test updates; `/acciones` needs an explicit validated tenant or an ambiguity rule.
   - Effort: Medium

3. **Remove tenant from `ActorContext` entirely and require explicit tenant on every AI request** — make active company a request-level input everywhere.
   - Pros: Makes active context explicit.
   - Cons: Unnecessary for resource endpoints that already have a tenant anchor; adds more client surface area; larger change than PR4 needs.
   - Effort: Medium/High

### Recommendation
Tenant scope does NOT belong in `ActorContext` as the PR4 AI tenant source of truth. The correct answer for this slice is: explicit request tenant where needed, or resource-derived tenant for resource-bound flows.

Recommended tenant model by endpoint:
- `POST /api/ai/chat` — **resource-owned tenant**. Resolve the WhatsApp conversation first and use `WhatsappConversacionResumen.canalEmpresaId` as the tenant anchor. If an explicit active tenant is ever accepted, it must only be a cross-check that matches the conversation and belongs to the actor.
- `POST /api/ai/acciones/{id}/confirmar` — **resource-owned tenant + actor identity**. Load `AiAccion`, require `solicitadaPor == actor.usuarioId`, and use `accion.empresaId` as the tenant anchor. Optional explicit tenant may be validated against the action, but should not drive authorization.
- `POST /api/ai/acciones/{id}/rechazar` — same as confirm.
- `GET /api/ai/conversaciones/{id}` — **resource-owned tenant + actor identity**. Load `AiConversacion`, require owner match, use `conversacion.empresaId` as the tenant anchor.
- `GET /api/ai/acciones` — **explicit active-tenant input validated against ownership**. This endpoint has no single resource id to anchor the tenant, so it needs either an explicit validated tenant selector or a hard ambiguity rule (only auto-resolve when the actor owns exactly one company).

Minimum PR4 redesign:
1. Keep `ActorContext.usuarioId` as the authenticated identity anchor.
2. Stop treating actor-context tenant scope as required/authoritative for AI commands.
3. In `/chat`, derive the persisted/tool-bound tenant from the WhatsApp conversation's owning company, not from `ActorEmpresaScopeService` fallback.
4. In resource endpoints (`confirmar`, `rechazar`, `obtener conversacion`), authorize against the loaded resource's `empresaId`.
5. In `/acciones`, require a validated active tenant when the actor can own more than one company, and make the read query actually filter by tenant.
6. Update the OpenSpec security/AI specs and tests to reflect this model before further apply work.

### Risks
- If the current model stays, `/chat` can bind `AiConversacion`, `AiMemoria`, tool context, and staged `AiAccion` to the wrong company for multi-company actors.
- `/acciones` can return pending actions from the wrong company because the current repository query ignores tenant.
- Existing draft specs expected actor-context tenant scope, but the implementation already uses explicit request tenant or resource-derived tenant; keeping that mismatch will reintroduce wrong code in later apply phases.

### Ready for Proposal
Yes — but only if the orchestrator tells the user that PR4 should switch to a resource-first tenant model. The next phase should adjust design/specs first, then apply. Do NOT keep an actor-context-tenant-driven approach as the authoritative model.
