## Exploration: Diagnose and plan a fix for `GET /api/tableros/get-all` returning 403 Bearer insufficient_scope while other authenticated endpoints such as `/api/roles/get-all` return 200

### Current State
Current source does not contain a tablero-specific authorization rule. `SecurityConfig` permits public docs/health, requires `SUPER_USUARIO` only for `POST /api/superusuarios/create`, and requires plain authentication for every other `/api/**` route. `TableroController#getAll()` and `RolController#getAll()` are both simple `@GetMapping("/get-all")` handlers with no `@PreAuthorize`, `@Secured`, or `@RolesAllowed` annotations. The JWT authorities converter only maps Keycloak roles for route guards and does not introduce any tablero path logic. This means the checked source says both endpoints should behave the same for the same authenticated token.

### Affected Areas
- `infrastructure/src/main/java/com/ar/crm2/security/SecurityConfig.java` — defines the only checked route guard; currently `/api/**` is `.authenticated()`.
- `infrastructure/src/main/java/com/ar/crm2/security/KeycloakJwtAuthoritiesConverter.java` — proves role mapping exists only for `hasRole(...)` checks, not for tablero-specific scopes.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/TableroController.java` — real failing endpoint mapping is `GET /api/tableros/get-all`.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/RolController.java` — comparison endpoint; same structure, currently succeeds at runtime.
- `infrastructure/src/test/java/com/ar/crm2/security/SecurityConfigTest.java` — security tests cover `/api/tableros`, not the real `/api/tableros/get-all` adapter path.
- `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/TableroControllerIT.java` — exercises `/api/tableros/get-all` but without the real security filter chain, so it cannot catch this 403.
- `boot/src/main/resources/application.yml` — resource server runtime config (`issuer-uri`, `audiences: crm2-api`) confirms audience-based JWT validation, but nothing path-specific.
- `Keycloak/README.md` — smoke-test docs still reference `/api/tableros`, showing test/docs drift from the controller contract.

### Approaches
1. **Source-first reproducibility fix** — Add focused security coverage for the real adapter paths, then reproduce and fix only if the failure appears in code.
   - Pros: Safest path; turns the reported runtime bug into a testable contract; likely reveals whether the problem is code or runtime drift.
   - Cons: May prove the source is already correct and shift the fix to deployment/runtime reconciliation.
   - Effort: Medium

2. **Runtime parity / stale artifact investigation** — Assume the checked source is correct and treat the 403 as a stale build, external config, or branch mismatch.
   - Pros: Likely explanation given the current code comparison; fastest if the running app is outdated.
   - Cons: Weak without adding regression tests; does not protect against future path-specific auth regressions.
   - Effort: Low

### Recommendation
Start with Approach 1. The strongest finding is that current source shows no authorization difference between `/api/tableros/get-all` and `/api/roles/get-all`, so the first fix should be to add a security regression test that hits the real endpoints under `SecurityConfig` with the same JWT shape used in runtime verification. If that test passes for both endpoints, the orchestrator should treat the 403 as runtime drift (stale backend, different branch/build, or untracked external config) and reconcile the deployed artifact before changing application code.

### Risks
- The bug may be non-reproducible from source because the running backend is stale or differs from the checked workspace.
- Current tests are split in a way that hides this class of issue: security tests use generic `/api/tableros` paths, while controller tests bypass the real JWT security chain.

### Ready for Proposal
Yes — propose a change that first adds endpoint-accurate security regression coverage for `GET /api/tableros/get-all` and `GET /api/roles/get-all`, then only changes application security code if the failure reproduces in source; otherwise the follow-up action is runtime/deployment parity verification.
