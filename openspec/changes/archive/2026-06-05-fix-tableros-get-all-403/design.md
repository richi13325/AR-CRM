# Design: Fix `GET /api/tableros/get-all` 403

## Technical Approach

Make the authorization contract executable before changing production security. The source currently has no tablero-specific route restriction: `SecurityConfig` permits public/docs/health/forgot-password/CORS, requires `SUPER_USUARIO` only for `POST /api/superusuarios/create`, and requires authentication for all other `/api/**` paths. `TableroController#getAll()` and `RolController#getAll()` are equivalent `GET /get-all` handlers with no method-level security. Therefore the first implementation step is endpoint-accurate regression coverage in `SecurityConfigTest` for `/api/tableros/get-all` and `/api/roles/get-all`. Only if that RED test reproduces 403 should `SecurityConfig` be changed.

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Add tests in `SecurityConfigTest` first | Proves real Spring Security chain behavior, but may show source is already correct | Chosen: aligns with strict TDD and separates source defect from runtime drift |
| Change `SecurityConfig` immediately | Faster apparent fix, but risks over-fixing a non-reproducible runtime/build mismatch | Rejected unless regression test fails |
| Add role/scope/ownership checks for tablero list | More restrictive, but violates current product contract | Rejected: any authenticated user must access get-all for now |
| Use controller slice tests as security proof | Existing `TableroControllerIT` verifies JSON/controller behavior but does not prove the real security chain | Rejected as primary proof; keep it as controller contract only |

## Data Flow

Authenticated request path:

    Bearer JWT -> SecurityConfig /api/** authenticated()
        -> KeycloakJwtAuthoritiesConverter maps roles only for hasRole guards
        -> TableroController#getAll()
        -> GetAllTablerosUseCase
        -> 200 OK list response

For `/api/tableros/get-all`, no route, scope, role, or ownership decision should occur after JWT authentication in this change.

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `infrastructure/src/test/java/com/ar/crm2/security/SecurityConfigTest.java` | Modify | Add path-accurate tests for authenticated and unauthenticated `GET /api/tableros/get-all` and comparison `GET /api/roles/get-all` through the real security filter chain. Extend the dummy controller with exact mappings instead of relying only on `/api/**` fallback. |
| `infrastructure/src/main/java/com/ar/crm2/security/SecurityConfig.java` | Conditional modify | Change only if the new test fails with 403 from source. The expected minimal fix is to keep `/api/tableros/get-all` under `.authenticated()` and avoid role/scope guards. |
| `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/TableroControllerIT.java` | No planned change | Already verifies controller `GET /api/tableros/get-all` behavior; do not use it as security evidence because `@WebMvcTest` does not exercise the same full security setup. |
| `infrastructure/src/main/java/com/ar/crm2/security/KeycloakJwtAuthoritiesConverter.java` | No planned change | Converter maps Keycloak roles for route guards; no tablero path logic belongs here. |
| `boot/src/main/resources/application.yml` | No planned change | Audience/issuer config is relevant runtime context, not a path-specific authorization rule. |

## Interfaces / Contracts

No public API shape changes.

Security contract:
- `GET /api/tableros/get-all` with a valid JWT MUST NOT return 403 due to route authorization.
- `GET /api/tableros/get-all` without a JWT MUST return 401.
- `GET /api/roles/get-all` remains the parity endpoint for authenticated access.

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Security integration | Authenticated JWT can access `/api/tableros/get-all` and `/api/roles/get-all`; unauthenticated requests get 401 | Add/adjust `SecurityConfigTest` methods using `Authorization: Bearer valid-token` and exact dummy mappings |
| Controller slice | Tablero list returns 200 and body shape | Keep existing `TableroControllerIT#getAll_shouldReturn200WithListOfTableros` |
| Regression command | Infrastructure security/controller tests | `mvn -pl infrastructure -am test -Dtest=SecurityConfigTest,TableroControllerIT` |
| Full verification | Module and repository health | `mvn -pl infrastructure -am test` then `mvn verify` |

## Migration / Rollout

No migration required. If tests pass without production changes, rollout is a runtime parity action: verify the deployed artifact/branch/config matches this source before changing authorization code.

## Open Questions

- [ ] None blocking. If the source regression passes, the follow-up owner must confirm whether the runtime 403 came from stale deployment, different branch/build, or untracked external security configuration.
