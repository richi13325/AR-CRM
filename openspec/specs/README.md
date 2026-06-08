# Specs — main source of truth

Active specs live under `openspec/specs/<domain>/spec.md` (e.g. `openspec/specs/tablero/spec.md`).
Delta specs created by an active change live under `openspec/changes/<change-name>/specs/<domain>/spec.md`.
On archive, deltas are merged into the matching main spec.

Suggested initial domains (detected from code):
- agenda
- columna
- contacto
- empresa
- ficha
- identity
- rol
- security
- superusuario
- tablero
- tarea
- trato
- usuario

Create the first spec with `/sdd-new <domain>` or by hand.
