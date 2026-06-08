# Runtime / Build Parity Finding

**Change**: `fix-tableros-get-all-403`
**Status**: Source tests GREEN. Runtime 403 NOT reproducible from source.
**Action**: NO production security change. Follow-up owned by the runtime/deployment reconciliation team.

## Symptom (reported)

Authenticated `GET /api/tableros/get-all` returns **403** (with `Bearer insufficient_scope` framing) while authenticated `GET /api/roles/get-all` returns **200**.

## Source-Level Investigation Result

New path-accurate security regression coverage in `SecurityConfigTest` (added by this change) exercises the real Spring Security filter chain against the real adapter paths with the real `JwtDecoder` contract (mock JWT with `aud=crm2-api`, `realm_access.roles=["USER"]`):

| Test | Path | Auth state | Expected | Actual |
|------|------|------------|----------|--------|
| `getApiTablerosGetAll_authenticated_returns200` | `GET /api/tableros/get-all` | `Authorization: Bearer valid-token` | 200 | **200** |
| `getApiRolesGetAll_authenticated_returns200` | `GET /api/roles/get-all` | `Authorization: Bearer valid-token` | 200 | **200** |
| `getApiTablerosGetAll_noToken_returns401` | `GET /api/tableros/get-all` | no token | 401 | **401** |
| `getApiRolesGetAll_noToken_returns401` | `GET /api/roles/get-all` | no token | 401 | **401** |

Result: **21/21 `SecurityConfigTest` tests pass.** The source `SecurityConfig` authorizes `/api/tableros/get-all` and `/api/roles/get-all` identically (both fall under `/api/**` → `.authenticated()`). No 403 is reproducible from source.

## Source Authorization Rule (unchanged)

`infrastructure/src/main/java/com/ar/crm2/security/SecurityConfig.java`:

```java
.requestMatchers(API_ENDPOINTS).authenticated()  // /api/** requires JWT
.requestMatchers(SUPERUSUARIO_CREATE)
    .hasRole("SUPER_USUARIO")                    // only POST /api/superusuarios/create
.anyRequest().denyAll()
```

Neither `TableroController#getAll()` nor `RolController#getAll()` has `@PreAuthorize`, `@Secured`, or `@RolesAllowed`. Both endpoints are `@GetMapping("/get-all")` plain handlers. The `KeycloakJwtAuthoritiesConverter` maps Keycloak roles only for `hasRole(...)` checks; it does not introduce any path-specific logic.

## Conclusion

The runtime 403 is **runtime/build drift**, not a source authorization defect. The change **does not** introduce a production security fix. The follow-up owner must reconcile the deployed artifact/branch/config with this source before assuming a code change is needed.

## Recommended Reconciliation Checklist (for runtime/deployment team)

1. **Verify deployed branch and commit hash**:
   - Confirm the running backend was built from a commit where `SecurityConfig` matches the checked source (no `hasRole(...)` guard added for `tableros`).
   - `git log --oneline -5` on the deployed artifact's source revision should match this repository's working branch.

2. **Verify build artifact**:
   - Rebuild from this branch and redeploy. If the 403 disappears, the deployed artifact was stale.

3. **Verify runtime configuration**:
   - `boot/src/main/resources/application.yml` — confirm `spring.security.oauth2.resourceserver.jwt.audiences: crm2-api` and the Keycloak `issuer-uri` resolve at runtime.
   - Check that no external reverse proxy / API gateway is injecting a `403` upstream of the Spring filter chain.

4. **Verify token contract**:
   - Decode a failing token (Keycloak token introspection) and confirm `aud=crm2-api` and `realm_access.roles` is present. If `aud` is missing or differs, the runtime `JwtDecoder` will reject — but that produces **401**, not 403.
   - 403 with `insufficient_scope` framing suggests an upstream layer (gateway, Keycloak resource server scope, or reverse proxy) is enforcing a `scope` claim that the token lacks. The Spring Security `hasAuthority("SCOPE_xxx")` is not configured in this source, so a `403 insufficient_scope` from Spring is not expected — search for the upstream layer.

5. **If a runtime-only source override exists** (e.g. untracked `application-prod.yml`, feature flag, or external config service), inspect it for any path-specific authorization rule.

## Out of Scope for This Change

- Project-manager, ownership, role-based, or fine-grained tablero authorization.
- Adding `hasRole(...)` or `hasAuthority("SCOPE_xxx")` to `SecurityConfig` — would break the current product contract ("any authenticated user can list tableros").
- Touching `/api/roles/get-all` production behavior — confirmed test control only, not a production change.

## Follow-Up Owner

The runtime/deployment reconciliation team owns the next step. This change closes the source-level contract; the runtime 403 is owned by the deployment/ops track.
