## Exploration: Investigate and plan how to ensure `TableroControllerIT` actually runs in Maven/CI

### Current State
The repository is a Maven multi-module build using the Spring Boot parent defaults. There is no explicit `maven-failsafe-plugin` or `maven-surefire-plugin` configuration in `pom.xml` or `infrastructure/pom.xml`, so the default `mvn test` / `mvn verify` flow only runs surefire-compatible test names. The infrastructure module currently contains four `*IT.java` classes (`TableroControllerIT`, `ColumnaControllerIT`, `TableroRepositoryIT`, `ColumnaRepositoryIT`) and none of them appear in `infrastructure/target/surefire-reports` after `./mvnw.cmd -pl infrastructure -am test`. `TableroRepositoryIT` can execute when targeted explicitly, but `TableroControllerIT` currently fails when forced to run because its `@WebMvcTest` slice does not provide `KeycloakJwtActorContextMapper`, which is required by `ActorContextRequestAttributeFilter`.

### Affected Areas
- `pom.xml` — parent build currently relies on default surefire behavior and has no failsafe lifecycle wiring.
- `infrastructure/pom.xml` — infrastructure module owns the orphan `*IT` classes and is the likely place for module-specific failsafe configuration if not done in the parent.
- `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/TableroControllerIT.java` — orphan test under `*IT` suffix; when executed explicitly, it fails to boot its test slice because `KeycloakJwtActorContextMapper` is missing.
- `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/ColumnaControllerIT.java` — same `*IT` naming pattern and same `@WebMvcTest` style, so it is likely affected by the same execution/wiring issue.
- `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/TableroRepositoryIT.java` — orphan `@DataJpaTest` that does execute successfully when targeted, showing the suffix problem is real beyond `TableroControllerIT`.
- `infrastructure/src/test/java/com/ar/crm2/adapter/out/persistence/ColumnaRepositoryIT.java` — another orphan repository integration test under the same naming convention.
- `infrastructure/src/test/java/com/ar/crm2/adapter/in/rest/UsuarioControllerMvcTest.java` — existing working MVC slice pattern; it already mocks `KeycloakJwtActorContextMapper`, which is the missing dependency in `TableroControllerIT`.
- `infrastructure/src/main/java/com/ar/crm2/security/ActorContextRequestAttributeFilter.java` — constructor requires `KeycloakJwtActorContextMapper`, which explains the current `TableroControllerIT` boot failure.
- `openspec/config.yaml` — project verification convention says `mvn verify`, but today that command does not include `*IT` coverage.

### Approaches
1. **Configure Failsafe and keep `*IT` naming** — Add `maven-failsafe-plugin` so `integration-test` / `verify` executes `**/*IT.java` as intended.
   - Pros: Matches Maven conventions; fixes all four existing orphan `*IT` classes at once; preserves the existing semantic distinction between `*Test` and `*IT`.
   - Cons: `TableroControllerIT` is not execution-ready today, so enabling failsafe alone will make CI fail until the MVC slice wiring is fixed; may also surface the same issue in `ColumnaControllerIT`.
   - Effort: Medium

2. **Rename `TableroControllerIT` to a surefire-compatible `*Test` class** — Move this one test into the normal `test` phase instead of adding failsafe.
   - Pros: Smaller build-config change; `mvn test` and `mvn verify` would pick it up without a new plugin.
   - Cons: Renaming only `TableroControllerIT` leaves the other three `*IT` classes orphaned; the class still fails when actually executed, so renaming does not solve the hidden boot issue; weakens naming consistency because repository `*IT` tests still look like integration tests but remain unrun.
   - Effort: Medium

3. **Adopt failsafe and repair the broken controller IT slices** — Treat this as a small testing-convention cleanup: enable failsafe, then make `TableroControllerIT` (and likely `ColumnaControllerIT`) use the working MVC security pattern already proven elsewhere.
   - Pros: Solves the real CI gap and the real test-health gap; aligns with the existing presence of multiple `*IT` classes; gives `mvn verify` honest integration coverage.
   - Cons: Slightly broader than just renaming one file; requires updating failing test wiring before CI can stay green.
   - Effort: Medium

### Recommendation
Recommend **Approach 3**: configure `maven-failsafe-plugin` and keep the `*IT` convention, but do not stop there — first make `TableroControllerIT` execution-ready using the same MVC slice security pattern already used by `UsuarioControllerMvcTest` (mock or provide `KeycloakJwtActorContextMapper`, and then validate request auth behavior as needed). This is the only approach that fixes the full repository convention instead of papering over a single file, and it avoids leaving `TableroRepositoryIT` / `ColumnaRepositoryIT` orphaned.

### Risks
- Enabling failsafe without fixing controller test wiring will immediately turn the current hidden problem into a red CI build.
- `ColumnaControllerIT` likely has the same missing-bean problem as `TableroControllerIT`, so the blast radius is probably two controller IT classes, not one.
- If build owners expect `mvn test` alone to run every check, moving `*IT` coverage to failsafe means team/docs must standardize on `mvn verify` in CI and local verification.

### Ready for Proposal
Yes — tell the user the repo has a broader `*IT` convention problem, not just a `TableroControllerIT` naming problem. The proposal should target failsafe-based integration-test execution plus the minimum test-slice fixes required for the controller IT classes to boot successfully.
