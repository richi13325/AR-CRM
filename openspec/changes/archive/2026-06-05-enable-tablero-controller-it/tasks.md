# Tasks: Enable TableroControllerIT in Maven/CI

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 30-50 |
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
| 1 | Wire `maven-failsafe-plugin` in `infrastructure/pom.xml` (TableroControllerIT only) + fix MVC slice wiring | PR 1 | Single PR; combines pom edit + test edit. Strictly out of scope: `ColumnaControllerIT`, `TableroRepositoryIT`, `ColumnaRepositoryIT`. |

## Phase 1: RED — Capture Pre-Fix Failure

- [x] 1.1 Run `mvn -pl infrastructure -am test`; confirm `TableroControllerIT` is absent from surefire reports.
- [x] 1.2 Force-run the class (e.g., `mvn -pl infrastructure -am -Dtest=TableroControllerIT test`) and capture `NoSuchBeanDefinitionException` for `KeycloakJwtActorContextMapper`.
- [x] 1.3 Record the exact failure message in the change description for traceability.

## Phase 2: GREEN — Add Failsafe + Fix MVC Slice

- [x] 2.1 Add `maven-failsafe-plugin` to `infrastructure/pom.xml` bound to `integration-test` and `verify`; restrict `<include>` to `**/TableroControllerIT.java` only.
- [x] 2.2 In `TableroControllerIT.java`, add `@MockitoBean KeycloakJwtActorContextMapper actorContextMapper` (mirror `UsuarioControllerMvcTest`).
- [x] 2.3 Add class-level `@WithMockUser` so Spring Security permits the slice to dispatch.
- [x] 2.4 Add `@BeforeEach` stubbing: `when(actorContextMapper.map(any(Authentication.class))).thenReturn(validActorContext)` so `create` reads a non-null context.
- [x] 2.5 Run `mvn -pl infrastructure -am verify`; confirm failsafe picks `TableroControllerIT` and all 11 test methods pass.
- [x] 2.6 Confirm failsafe report contains only `TableroControllerIT`; no other `*IT` class is executed.

## Phase 3: REFACTOR — Scope & Harden

- [x] 3.1 Run `mvn verify` from repo root; confirm the slice is the only IT contribution from infrastructure.
- [x] 3.2 Run `mvn -pl infrastructure -am test`; confirm surefire flow stays green (no regression in `TableroControllerTest`).
- [x] 3.3 Inline-comment the failsafe `<include>` to document the deliberate scope restriction.

## Phase 4: Verification & Docs

- [x] 4.1 Update `design.md` Data Flow / File Changes if any line numbers shift.
- [ ] 4.2 Note failsafe scope (`TableroControllerIT` only) in the PR description to signal scope discipline.
