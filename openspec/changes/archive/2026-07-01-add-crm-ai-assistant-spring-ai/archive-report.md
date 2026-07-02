# Archive Report — add-crm-ai-assistant-spring-ai

**Change**: `add-crm-ai-assistant-spring-ai`
**Archived**: `openspec/changes/archive/2026-07-01-add-crm-ai-assistant-spring-ai/`
**Archive date**: 2026-07-01
**Artifact store mode**: `openspec`

---

## Executive Summary

Change `add-crm-ai-assistant-spring-ai` has completed the full SDD cycle: propose → spec → design → tasks → apply (25 slices) → verify → archive. Final verify passed after Slice 24/25 (AI memory time-stable fix + audit cleanup). All 152 change-critical AI tests green, root `./mvnw.cmd verify` BUILD SUCCESS.

---

## Verification Closure

| Gate | Result | Evidence |
|------|--------|----------|
| `./mvnw.cmd verify` | ✅ BUILD SUCCESS | Domain 267/267, Application 235/235, Infrastructure surefire 494/494, Infrastructure failsafe 49/49, Boot 11/11 |
| Change-critical AI suite (11 classes) | ✅ 152/152 | `AiControllerIT`, `AiRepositoryAdaptersIT`, `AiAccionRepositoryAdapterTest`, `AiMemoriaRepositoryAdapterTest`, `GlobalExceptionHandlerTest`, `KeycloakJwtActorContextMapperTest`, `OpenAiChatAdapterTest`, `SpringAiPromptMapperTest`, `AiToolContextAdapterTest`, `ThreadLocalAiToolContextHolderTest`, `AiToolArchitectureContractTest` |
| Domain + application full suite | ✅ Domain 267/267, Application 235/235 | |
| `SugerirRespuestaUseCase` / `AnthropicSugerenciaAdapter` unchanged | ✅ Confirmed | No paths changed in `whatsapp` or `AnthropicSugerenciaAdapter.java` |

---

## Specs Synced to Main

| Domain | Action | Details |
|--------|--------|---------|
| `ai-assistant` | Created (new) | 158-line spec — resource-first tenanting, tool-only access, read-only/propose-only tool surface, conversation history, per-turn safety limits, tool input validation, conversation retrieval, coexistence |
| `ai-memory` | Created (new) | 67-line spec — active conversation-scoped reads, phase-1 writes disabled, TTL, memory does not shadow messages |
| `ai-action-proposal` | Created (new) | 148-line spec — lifecycle states, user-driven confirmation, tenant verification on confirm/reject, replay protection, pending-list tenant-scoped, domain holds no Spring AI types, expiry |
| `security` | Updated (ADDED) | 2 requirements added: `ActorContext` identity-only, resource-bound AI commands authorize from owned resources |
| `tablero` | Updated (ADDED) | 3 requirements added: AI read tools, column moves require human confirmation, tenant match on AI-mediated ficha moves |

---

## Archive Contents

| Artifact | Status |
|----------|--------|
| `proposal.md` | ✅ Present |
| `specs/` (5 delta specs) | ✅ Present |
| `design.md` | ✅ Present |
| `tasks.md` | ✅ Present (96 tasks, 91 checked, 5 reconciled at archive) |
| `apply-progress.md` | ✅ Present (Slice 1–25, reconstructed after Slice 24 truncation) |
| `verify-report.md` | ✅ Present (9 sections covering all verify passes) |
| `exploration.md` | ✅ Present |
| `branch-report-*.md` (7 files) | ✅ Present |

---

## Tasks Reconciled at Archive

Items `5.1`–`5.4` and `6.7` were unchecked in `tasks.md` at the time of final verify. They have been checked at archive with evidence:

| Item | Evidence |
|------|----------|
| `5.1` domain+application test | Final verify: Domain 267/267, Application 235/235 |
| `5.2` AI tests | Final verify: 152/152 AI suite green |
| `5.3` WhatsApp coexistence | Final verify: no paths changed under `whatsapp` or `AnthropicSugerenciaAdapter.java` |
| `5.4` verify-report entries | `verify-report.md` has 9 sections; Slices 1-23 directional summary preserved via verify-report references |
| `6.7` focused tests / compile | Final verify: `./mvnw.cmd verify` BUILD SUCCESS |

Remaining unchecked items:
- `9.10` — explicitly deferred follow-up (`CallAdvisor` for per-tool-call loop enforcement); documented in `design.md` and `tasks.md`

---

## Artifact Hygiene Notes

1. **Untracked directory**: The entire `openspec/changes/add-crm-ai-assistant-spring-ai/` directory was always untracked under the current branch. The archive preserves the complete artifact state at archive time; git history does not contain these files.
2. **`apply-progress.md` truncation**: During Slice 24, the file was inadvertently truncated by a PowerShell overwrite. Slices 1-23 content was reconstructed directionally from `verify-report.md` sections. The reconstruction notice at the top of `apply-progress.md` documents this honestly.
3. **`tasks.md` checkboxes**: Were stale at final verify time because verify scope was restricted to `verify-report.md`. Reconciled at archive.
4. **`verify-report.md` structure**: Contains 9 `## Verification Report` sections (all titled identically) with slice disambiguation via the `**Scope**: ...` line. Per-slice → §Scope mapping for Slices 1-18 is directional/lost; Slices 19-25 are recoverable from the §Scope index in `apply-progress.md`.

---

## SDD Cycle Complete

The change `add-crm-ai-assistant-spring-ai` has been fully planned (proposal), specified (delta specs), designed (design doc), tasked (96 tasks), implemented (25 slices), verified (final pass: 152/152 AI suite, BUILD SUCCESS), and archived.

**New capabilities delivered**:
- `ai-assistant`: tenant-scoped conversational AI over CRM data via Spring AI, resource-first tenanting, tool-calling loop, system prompt, conversation history.
- `ai-memory`: phase-1 conversation-scoped memory reads from `ai_memoria`, writes disabled by default, TTL enforcement.
- `ai-action-proposal`: staged PENDING → CONFIRMED → EXECUTED/FAILED lifecycle for AI-proposed CRM mutations; human-confirmation-only; no model self-confirmation.

Ready for the next change.
