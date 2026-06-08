# Proposal: Fix `GET /api/tableros/get-all` 403

**Affected Layers**: infrastructure

## Intent

Ensure `GET /api/tableros/get-all` is available to any authenticated user, matching the confirmed product rule and the observed behavior of `GET /api/roles/get-all`.

## Scope

### In Scope
- Add security regression coverage for the real endpoint paths `/api/tableros/get-all` and `/api/roles/get-all`.
- Reproduce the reported 403 through the real Spring Security chain before changing authorization code.
- If reproduced from source, adjust infrastructure-layer security wiring so authenticated JWTs can access `/api/tableros/get-all`.

### Out of Scope
- Project-manager, ownership, role-based, or fine-grained tablero authorization.
- Changes to business rules for other tablero endpoints beyond what is required to resolve this regression.

## Capabilities

### New Capabilities
- `tablero`: Authenticated users can list tableros through `GET /api/tableros/get-all`.
- `security`: API security coverage must verify real route behavior for authenticated access and reject path drift.

### Modified Capabilities
- None.

## Approach

Follow the exploration recommendation: make the failing contract executable first in `SecurityConfigTest`, using the real adapter paths instead of `/api/tableros`. Keep `TableroControllerIT` focused on controller behavior; only change security code if the new regression test fails. If tests pass, treat the 403 as runtime/build parity drift, not a source authorization rule.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `infrastructure/src/test/java/com/ar/crm2/security/SecurityConfigTest.java` | Modified | Add endpoint-accurate auth regression coverage |
| `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/TableroControllerIT.java` | Modified | Keep controller contract aligned with security expectations if needed |
| `infrastructure/src/main/java/com/ar/crm2/security/SecurityConfig.java` | Modified | Only if source tests reproduce the 403 |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Failure is runtime drift, not source | High | Use tests to separate code defects from deployment mismatch |
| Over-fixing auth with new restrictions | Medium | Keep scope limited to authenticated access only |

## Rollback Plan

Revert the security-path test updates and any related `SecurityConfig` change, then restore the previous route matrix while retaining investigation notes in the change history.

## Dependencies

- `openspec/changes/fix-tableros-get-all-403/exploration.md`

## Success Criteria

- [ ] Automated security coverage exercises `GET /api/tableros/get-all` and `GET /api/roles/get-all` through the real security chain.
- [ ] Authenticated JWT requests to `GET /api/tableros/get-all` no longer return 403 from source-controlled behavior.
- [ ] No new role-based or ownership-based restriction is introduced in this change.
