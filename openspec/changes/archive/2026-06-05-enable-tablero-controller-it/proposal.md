# Proposal: Enable TableroControllerIT in Maven/CI

**Affected layers**: infrastructure, boot

## Intent

Make `TableroControllerIT` run reliably in `mvn verify` / CI for this first slice, closing the current gap where the class exists but is not executed and fails when forced.

## Scope

### In Scope
- Wire Maven test execution so `TableroControllerIT` is included in CI verification.
- Fix the `@WebMvcTest` slice for `TableroControllerIT` by providing the missing `KeycloakJwtActorContextMapper` dependency.
- Preserve current `TableroControllerIT` security assertions against the real `/api/tableros/get-all` endpoint.

### Out of Scope
- Enabling `ColumnaControllerIT`, `TableroRepositoryIT`, or `ColumnaRepositoryIT`.
- Renaming or standardizing all orphan `*IT` classes.
- Broad security or controller refactors beyond what `TableroControllerIT` needs to boot.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- None — this change makes existing `tablero` and `security` requirements executable in CI without changing product behavior.

## Approach

Use a targeted Maven integration-test configuration in `infrastructure` so only `TableroControllerIT` is executed in this slice, then align its MVC test wiring with the existing working pattern used by `UsuarioControllerMvcTest` by mocking/providing `KeycloakJwtActorContextMapper`.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `infrastructure/pom.xml` | Modified | Add targeted IT execution for `TableroControllerIT` during `verify`. |
| `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/TableroControllerIT.java` | Modified | Add missing test-slice dependency wiring. |
| `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/UsuarioControllerMvcTest.java` | Reference | Reuse the existing MVC security mocking pattern. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Build config accidentally pulls in other orphan `*IT` classes | Med | Use explicit include targeting `TableroControllerIT` only. |
| Test still fails due additional hidden MVC/security beans | Med | Mirror the proven `UsuarioControllerMvcTest` slice setup and verify locally with module-scoped `mvn verify`. |

## Rollback Plan

Revert the Maven include and `TableroControllerIT` wiring changes, returning CI to the previous state where this test is not part of verification.

## Dependencies

- Existing Spring MVC test security pattern in `UsuarioControllerMvcTest`.

## Success Criteria

- [ ] `TableroControllerIT` runs during `mvn -pl infrastructure -am verify`.
- [ ] The test passes with the required MVC/security beans present.
- [ ] No additional orphan `*IT` classes are pulled into scope by this slice.
