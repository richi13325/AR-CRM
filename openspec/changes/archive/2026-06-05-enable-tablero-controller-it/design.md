# Design: Enable TableroControllerIT in Maven/CI

## Technical Approach

Enable the existing `TableroControllerIT` as a narrowly scoped integration verification target. The infrastructure module will own the Maven integration-test configuration and will include only `TableroControllerIT`, avoiding a broad `**/*IT` include that would pull `ColumnaControllerIT`, `TableroRepositoryIT`, or `ColumnaRepositoryIT` into this change. The MVC slice will follow the working `UsuarioControllerMvcTest` pattern by providing the missing `KeycloakJwtActorContextMapper` bean, plus authenticated request context for controller methods that depend on `ActorContext`.

## Architecture Decisions

| Decision | Choice | Alternatives considered | Rationale |
|---|---|---|---|
| Integration test execution | Configure `maven-failsafe-plugin` only in `infrastructure/pom.xml` with include `**/TableroControllerIT.java`. | Parent-level failsafe config or broad `**/*IT.java`. | The orphan IT files live in infrastructure, and this change must not run other `*IT` classes. |
| MVC slice dependency | Add `@MockitoBean KeycloakJwtActorContextMapper` to `TableroControllerIT`. | Import real mapper or disable security filters. | Existing `UsuarioControllerMvcTest` proves the mock pattern; disabling filters would weaken security coverage. |
| Authentication in slice | Use class-level `@WithMockUser` and default mapper stubbing to return a valid `ActorContext`. | Leave requests unauthenticated or use full JWT resource-server setup. | `TableroController.create` reads the request `actorContext`; a WebMvc slice should stay narrow and not boot full security infrastructure. |

## Data Flow

    mvn verify
      -> infrastructure failsafe execution
      -> TableroControllerIT only
      -> @WebMvcTest(TableroController, GlobalExceptionHandler)
      -> Security filter -> mocked KeycloakJwtActorContextMapper
      -> request actorContext -> TableroController -> mocked use cases

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `infrastructure/pom.xml` | Modify | Add `maven-failsafe-plugin` execution for `integration-test` and `verify`, with an explicit include for `**/TableroControllerIT.java` only. |
| `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/TableroControllerIT.java` | Modify | Import `KeycloakJwtActorContextMapper`, `ActorContext`, `Authentication`, `@WithMockUser`, and `@BeforeEach`; add a mapper mock and default actor-context stubbing so the MVC slice boots and authenticated requests reach the controller, remove the unused `ObjectMapper` field that breaks the slice, and align request payloads with the current enum/value-object contracts. |
| `openspec/changes/enable-tablero-controller-it/design.md` | Create | This design artifact. |

## Interfaces / Contracts

No product API or application/domain contracts change.

Maven contract:

```xml
<includes>
    <include>**/TableroControllerIT.java</include>
</includes>
```

Verification commands:

```bash
mvn -pl infrastructure -am -DskipITs=false verify
mvn verify
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Existing controller unit behavior | Leave current `TableroControllerTest` unchanged. |
| Integration | `TableroControllerIT` boots and executes through Maven verify | Run `mvn -pl infrastructure -am verify`; confirm failsafe report contains only `TableroControllerIT` among IT classes. |
| E2E | Not applicable | No E2E environment is involved. |

## Migration / Rollout

No migration required. Rollback by removing the infrastructure failsafe plugin block and reverting the `TableroControllerIT` slice wiring.

## Open Questions

None.
