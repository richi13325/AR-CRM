## Exploration: Reconcile runtime/deployment/config drift for `GET /api/tableros/get-all` returning `403 insufficient_scope`

### Current State
Source-level behavior is already proven correct for this contract. `SecurityConfig` authenticates all `/api/**` routes and only applies `hasRole("SUPER_USUARIO")` to `POST /api/superusuarios/create`; there is no tablero-specific or scope-based route rule. `TableroController#getAll()` and `RolController#getAll()` are plain `GET /get-all` handlers with no method-security annotations, and their application services both delegate directly to `findAll()` with no business authorization branch. `SecurityConfigTest` now covers the real adapter paths and passes for authenticated `GET /api/tableros/get-all` and `GET /api/roles/get-all`. Runtime config in `boot/src/main/resources/application.yml` validates JWT issuer/audience (`crm2-api`), but an audience or issuer mismatch would fail as `401`, not `403 insufficient_scope`. The repo-local runtime stack (`docker-compose.yml`) contains Postgres and Keycloak only, with no reverse proxy or API gateway, so the live `403 insufficient_scope` most likely comes from deployed artifact drift, external runtime config, or an upstream auth layer outside this repository.

### Affected Areas
- `infrastructure/src/main/java/com/ar/crm2/security/SecurityConfig.java` — proves `/api/tableros/get-all` is only under `.authenticated()` and has no scope guard.
- `infrastructure/src/main/java/com/ar/crm2/security/KeycloakJwtAuthoritiesConverter.java` — maps Keycloak roles to `ROLE_*`; it does not enforce `SCOPE_*` authorities.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/TableroController.java` — real failing endpoint mapping.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/RolController.java` — parity endpoint that succeeds at runtime.
- `application/src/main/java/com/ar/crm2/application/tablero/service/GetAllTablerosService.java` — confirms no app-layer authorization on tablero list.
- `application/src/main/java/com/ar/crm2/application/rol/service/GetAllRolesService.java` — parity proof: same direct `findAll()` pattern as tableros.
- `infrastructure/src/test/java/com/ar/crm2/security/SecurityConfigTest.java` — executable proof that source-level security allows both real paths with a valid JWT.
- `boot/src/main/resources/application.yml` — resource-server issuer and audience contract; useful for distinguishing `401` decoder failures from `403` authorization failures.
- `docker-compose.yml` — local runtime baseline; shows no in-repo gateway/proxy layer that could emit `insufficient_scope`.
- `Keycloak/README.md` and `Keycloak/realm-export.json` — document audience mappers, seeded `richi13335@gmail.com` user, and stale-volume drift risk in Keycloak.
- `openspec/changes/archive/2026-06-05-fix-tableros-get-all-403/runtime-parity-finding.md` — archived evidence that the 403 is not reproducible from source.

### Approaches
1. **Artifact and runtime parity audit first** — Verify the live backend is running the expected build and config before changing any application code.
   - Pros: Best match to the evidence; directly targets stale jar, wrong branch, wrong env vars, or runtime-only config overrides.
   - Cons: Requires deployment/log access and may involve systems outside this repo.
   - Effort: Medium

2. **Upstream authorization trace** — Trace the failing request end-to-end to identify where `insufficient_scope` is produced.
   - Pros: Best way to prove whether the 403 comes from Spring Security, Keycloak policy, ingress/gateway, or another middleware hop.
   - Cons: Harder if observability is limited; may require temporary logging or infrastructure access.
   - Effort: Medium

3. **Keycloak/runtime baseline reset for parity reproduction** — Recreate the documented local stack, re-import the realm, mint a token for `richi13335@gmail.com`, and compare decoded claims plus endpoint outcomes against the live environment.
   - Pros: Uses repo-controlled artifacts to establish a known-good baseline; likely exposes realm or token drift quickly.
   - Cons: Helps diagnosis, but does not by itself fix a remote deployment mismatch.
   - Effort: Low

### Recommendation
Start with Approach 1, then use Approach 2 only if the deployed artifact and env config appear correct. The strongest signal is that source tests, controller wiring, and Keycloak audience contract all align, while `403 insufficient_scope` points to a layer that is enforcing scopes outside the checked source. The proposal should therefore focus on runtime reconciliation: fingerprint the deployed commit/jar/config, confirm issuer/audience and any runtime overrides, decode the failing `richi13335@gmail.com` token without exposing secrets, and identify whether the 403 is emitted before the request reaches the Spring controller layer.

### Risks
- The live environment may contain untracked config (`application-prod.yml`, secret-store values, gateway policy, ingress annotations) that is not represented in this repository.
- A stale Keycloak database/import can drift from `Keycloak/realm-export.json`, especially because `--import-realm` is ignored when the volume already exists.
- If logs do not include request correlation or security decision traces, the team may misattribute an upstream 403 to application code again.

### Ready for Proposal
Yes — propose a runtime/deployment reconciliation change, not an application security code fix. The orchestrator should tell the user that the next phase must plan build fingerprinting, token-claim verification, and upstream auth-path tracing around the live deployment, while explicitly preserving the current product rule: any authenticated user may call `GET /api/tableros/get-all`.
