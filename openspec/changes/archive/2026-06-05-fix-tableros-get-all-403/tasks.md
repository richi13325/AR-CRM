# Tasks: Fix `GET /api/tableros/get-all` 403

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 80–150 (1 test file expanded; conditional 1–3 prod lines) |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Endpoint-accurate security regression coverage + conditional minimal fix | PR 1 | Tests drive the change; conditional prod touch only on RED |

## Phase 1: Foundation — Test Scaffolding (infrastructure)

- [x] 1.1 In `SecurityConfigTest.TestSecurityConfig.DummyApiController`, add exact `@GetMapping("/api/tableros/get-all")` and `@GetMapping("/api/roles/get-all")` methods. Return type `ResponseEntity<Void>` (was `HttpStatus` — see REFACTOR note in apply-progress; the original `HttpStatus` return triggered 406 on authenticated requests with no `Accept` header, which would have hidden the security contract).
- [x] 1.2 Keep the existing `@RequestMapping("/api/**")` fallback so unrelated paths still resolve in the slice.
- [x] 1.3 Verify dummy mappings compile by re-running `mvn -pl infrastructure -am test-compile`. Compilation succeeded (no output = clean compile).

## Phase 2: RED — Endpoint-Accurate Security Regression Tests

- [x] 2.1 Add `@Nested AuthenticatedRequests` to `SecurityConfigTest` with `getApiTablerosGetAll_authenticated_returns200` using `Authorization: Bearer valid-token` and asserting `status().isOk()`.
- [x] 2.2 Add `getApiRolesGetAll_authenticated_returns200` in the same nested class as parity coverage.
- [x] 2.3 In the existing `UnauthenticatedRequests` block, add `getApiTablerosGetAll_noToken_returns401`.
- [x] 2.4 Add `getApiRolesGetAll_noToken_returns401` next to it.
- [x] 2.5 Run `mvn -pl infrastructure -am test -Dtest=SecurityConfigTest` and record whether the new tests RED (403) or already pass. This decides the GREEN path.
  - **Result**: tests are RED on first run (status 406 — content negotiation on the `HttpStatus` return type), then GREEN after the `ResponseEntity<Void>` return-type fix in Phase 1.1. The new tests are path-accurate regression coverage: they exercise the real security filter chain against the real adapter paths. With a valid JWT, both `/api/tableros/get-all` and `/api/roles/get-all` return 200 — the source already authorizes these paths. Without a JWT, both return 401. The 403 reported at runtime is **NOT reproducible from source**.

## Phase 3: GREEN — Conditional Production Fix

- [ ] 3.1 IF 2.5 reproduced 403: in `SecurityConfig`, keep `/api/tableros/get-all` under `.requestMatchers(API_ENDPOINTS).authenticated()`. Do NOT modify `/api/roles/get-all`; it is test control only.
  - **N/A** — 2.5 did NOT reproduce 403 from source. SecurityConfig is already correct: `/api/**` is `.authenticated()`, no role/scope/ownership guard for `tableros` or `roles`.
- [x] 3.2 IF 2.5 did NOT reproduce 403: skip `SecurityConfig` change. Record the parity finding in the change folder and verify the deployed artifact/branch matches this source.
  - **Action taken**: `SecurityConfig.java` was NOT modified. The runtime/build parity finding is recorded in `openspec/changes/fix-tableros-get-all-403/runtime-parity-finding.md`.
- [x] 3.3 Re-run `mvn -pl infrastructure -am test -Dtest=SecurityConfigTest` — must pass.
  - **Result**: 21/21 passing (8 UnauthenticatedRequests + 5 PublicPaths + 1 ConverterChain + 2 AuthenticatedRequests + 2 DenyAllFallback + 3 SuperUsuarioBootstrap). All new tests assert clean `200`/`401`.

## Phase 4: REFACTOR — Scope Guard

- [ ] 4.1 If `SecurityConfig` was modified, confirm no new restriction was added for tableros (still `.authenticated()`, no role/scope/ownership) and `/api/roles/get-all` remains untouched.
  - **N/A** — `SecurityConfig` was not modified. The contract remains: `/api/tableros/get-all` and `/api/roles/get-all` both fall under `/api/**` → `.authenticated()`. No new role/scope/ownership guard was introduced.
- [ ] 4.2 If `SecurityConfig` was modified, update its `Route policy` javadoc to record the regression-test guarantee.
  - **N/A** — `SecurityConfig` was not modified. The regression-test guarantee is now encoded in `SecurityConfigTest.AuthenticatedRequests` (clean `200` assertions) and `SecurityConfigTest.UnauthenticatedRequests` (clean `401` assertions).

## Phase 5: Verification

- [x] 5.1 Run `mvn -pl infrastructure -am test` to confirm no regression in other infrastructure tests.
  - **Result**: 205/205 passing across domain, application, and infrastructure modules.
- [x] 5.2 Run `mvn verify` for full module health.
  - **Result**: BUILD SUCCESS for all 5 modules (domain, application, parent, infrastructure, boot). Spring Boot jar repackaged successfully.
- [ ] 5.3 Confirm `TableroControllerIT#getAll_shouldReturn200WithListOfTableros` still passes (controller slice contract preserved).
  - **Pre-existing issue (NOT caused by this change)**: `TableroControllerIT.java` ends with the `*IT.java` suffix which is the failsafe (integration test phase) pattern, not the surefire (unit test phase) pattern. The project does not configure the failsafe plugin — neither `infrastructure/pom.xml` nor the parent `pom.xml` declares `maven-failsafe-plugin`. As a result, `TableroControllerIT` is currently **orphaned**: it exists in source but is never executed by `mvn test` or `mvn verify`. The full 205-test surefire run does not include any `*IT` class. This is unrelated to the security change. The `TableroControllerIT` test code itself was last modified on `bdca7fd` (feature/keycloak-frontend-login-support branch) and is not part of this change's blast radius. Recommended follow-up: either rename to `TableroControllerTest` so surefire picks it up, or configure the failsafe plugin to run `*IT` tests.
- [x] 5.4 Confirm new tests assert clean `200`/`401` (not the `201 || 406` fallback used by the SUPER_USUARIO test).
  - **Result**: `getApiTablerosGetAll_authenticated_returns200` and `getApiRolesGetAll_authenticated_returns200` both assert `status().isOk()` (clean 200). The unauthenticated tests assert `status().isUnauthorized()` (clean 401). No `201 || 406` fallback assertions are used.
