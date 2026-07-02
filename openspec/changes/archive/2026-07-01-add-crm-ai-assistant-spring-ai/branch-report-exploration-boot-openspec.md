# Exploración de branch — boot/config/schema + OpenSpec/docs

**Proyecto**: `ar-crm / CRM2`  
**Cambio OpenSpec**: `add-crm-ai-assistant-spring-ai`  
**Slice**: `boot/wiring/config/schema + OpenSpec/docs`  
**Fuente de verdad usada**: `git status --short` + `git diff HEAD` sobre el alcance indicado.  
**Restricciones respetadas**: no se ejecutaron pruebas; no se modificó código productivo ni de test; solo se escribió este artefacto.

## 1. Inventario de archivos cambiados/nuevos del slice

### Wiring / boot

| Estado | Archivo | Rol en el cambio |
|---|---|---|
| `M` | `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` | Raíz de composición existente; ahora importa `AiWiringConfig` y documenta que la superficie AI vive en una configuración separada y feature-flagged. |
| `??` | `boot/src/main/java/com/ar/crm2/config/AiWiringConfig.java` | Nueva raíz de composición del grafo AI, protegida por `ai-assistant.enabled=true`. |
| `??` | `boot/src/test/java/com/ar/crm2/config/AiWiringConfigGateAnnotationTest.java` | Test por reflexión que fija el contrato del gate AI y el `@Import` desde `WiringConfig`. |
| `M` | `pom.xml` | Agrega BOM de Spring AI `2.0.0` en `dependencyManagement`. |

### Configuración

| Estado | Archivo | Rol en el cambio |
|---|---|---|
| `M` | `boot/src/main/resources/application.yml` | Agrega configuración `spring.ai.openai.*` y flags/presupuestos `ai-assistant.*`. |

### Schema

| Estado | Archivo | Rol en el cambio |
|---|---|---|
| `M` | `boot/src/main/resources/schema.sql` | Agrega DDL idempotente para tablas `ai_*` y sus índices principales. |

### OpenSpec

| Estado | Archivo | Rol en el cambio |
|---|---|---|
| `AM` | `openspec/changes/add-crm-ai-assistant-spring-ai/proposal.md` | Propuesta inicial del asistente AI. Tiene referencias que quedaron desactualizadas frente a PR7 y al schema actual. |
| `AM` | `openspec/changes/add-crm-ai-assistant-spring-ai/exploration.md` | Exploración previa de modelo tenant resource-first. Parcialmente desactualizada en `/acciones` porque todavía menciona auto-resolve como opción. |
| `AM` | `openspec/changes/add-crm-ai-assistant-spring-ai/design.md` | Diseño actual resource-first; documenta selector `empresaId` requerido para `/acciones`. Queda una deriva menor sobre el nombre de la excepción de validación MVC. |
| `AM` | `openspec/changes/add-crm-ai-assistant-spring-ai/tasks.md` | Checklist acumulado. Fases 1-4 están completas; quedan Phase 5 cross-PR verify y 6.7. |
| `??` | `openspec/changes/add-crm-ai-assistant-spring-ai/apply-progress.md` | Bitácora acumulada de slices PR4→PR7 con evidencia TDD. |
| `AM` | `openspec/changes/add-crm-ai-assistant-spring-ai/verify-report.md` | Verify report de PR7 con PASS WITH WARNINGS. |
| `AM` | `openspec/changes/add-crm-ai-assistant-spring-ai/specs/ai-assistant/spec.md` | Delta de asistente AI conversacional, resource-first y tool-only. |
| `AM` | `openspec/changes/add-crm-ai-assistant-spring-ai/specs/ai-action-proposal/spec.md` | Delta de propuestas AI y selector `empresaId` estricto. |
| `A` | `openspec/changes/add-crm-ai-assistant-spring-ai/specs/ai-memory/spec.md` | Delta de memoria AI fase 1. Contiene expectativa de `ai_memoria_hechos` que no aparece en `schema.sql`. |
| `??` | `openspec/changes/add-crm-ai-assistant-spring-ai/specs/security/spec.md` | Delta de seguridad: `ActorContext` solo identidad; tenant desde recurso o request. |
| `??` | `openspec/changes/add-crm-ai-assistant-spring-ai/specs/tablero/spec.md` | Delta de tablero: herramientas AI read-only y movimientos solo tras confirmación humana. |

### Docs

| Estado | Archivo | Rol en el cambio |
|---|---|---|
| `??` | `docs/reports/keycloak-usuario-id-sync-report.md` | Reporte estrecho de sincronización `usuario_id` en Keycloak; no representa el branch completo de AI assistant. |

### Misc

| Estado | Archivo | Rol en el cambio |
|---|---|---|
| `??` | `boot-stderr.log` | Archivo de log vacío. Ruido local; no aporta evidencia y no debería entrar al reporte final ni al commit. |

## 2. Bean methods, configuración y schema

### Wiring principal

- `WiringConfig`:
  - Agrega `@Import(AiWiringConfig.class)` para incluir el grafo AI en la raíz de composición.
  - Mantiene el grafo no-AI existente.
  - Cambia la firma de `keycloakUserProvisioningAdapter(...)` a tipo fully-qualified; no hay cambio funcional aparente en ese bean.

- `AiWiringConfig`:
  - Clase `@Configuration` con `@ConditionalOnProperty(name = "ai-assistant.enabled", havingValue = "true", matchIfMissing = false)`.
  - Responsabilidad: registrar todo el grafo AI solo cuando el master kill-switch esté habilitado.

### Beans clave en `AiWiringConfig`

| Grupo | Bean methods / tipos | Responsabilidad |
|---|---|---|
| Contexto de tools | `aiToolContextHolder`, `aiToolContextAdapter` | Mantener y exponer contexto thread-local para llamadas de tools AI. |
| Embeddings fase 1 | `generarEmbeddingPort` | Stub determinístico; embeddings reales quedan diferidos. |
| Tools read/propose | `obtenerMensajesRecientesTool`, `obtenerResumenChatTool`, `buscarClientePorTelefonoTool`, `listarColumnasTableroTool`, `proponerAccionTool` | Superficie AI registrada en `OpenAiChatAdapter`: lectura o propuesta, sin ejecución directa de mutaciones. |
| Tenant scope | `actorEmpresaScopeService` | Expone `ActorEmpresaScopeService` como implementación del port Empresa-owned `ActorEmpresaScopePort`. |
| Persistencia AI | `aiAccionRepositoryAdapter`, `aiConversacionRepositoryAdapter`, `aiConversacionSaveAdapter`, `aiMensajeRepositoryAdapter`, `aiResumenContextoRepositoryAdapter`, `aiMemoriaRepositoryAdapter` | Adaptadores JPA para acciones, conversaciones, mensajes, resumen y memoria AI. |
| Read ports existentes | `aiContactoLecturaPort`, `aiFichaLecturaPort`, `aiColumnaLecturaPort` | Reutilizan adapters CRM existentes como puertos de lectura para tools AI. |
| WhatsApp read ports | `whatsappConversacionLecturaAdapter`/`Port`, `whatsappMensajeLecturaAdapter`/`Port` | Puente desde repositorios WhatsApp hacia el bounded context AI. |
| Spring AI | `chatClient`, `openAiChatAdapter` | Construye `ChatClient` y adapta llamadas al modelo con tools registradas y `modelId` desde `spring.ai.openai.chat.options.model`. |
| Use cases AI | `registrarAccion*`, `proponerAccion*`, `analizarChat*`, `registrarMensajeAsistente*`, `obtenerConversacionAsistente*`, `listarConversacionesAsistente*`, `listarAccionesPendientes*`, `obtenerAccion*`, `confirmarAccion*`, `rechazarAccion*`, `expirarAccion*` | Publican servicios de aplicación como ports-in/use cases y conectan ports-out. |

### Configuración agregada

- `pom.xml`:
  - `spring-ai.version=2.0.0`.
  - Importa `org.springframework.ai:spring-ai-bom:${spring-ai.version}`.

- `application.yml`:
  - `spring.ai.openai.base-url`: default `https://api.anthropic.com/v1/` como gateway OpenAI-compatible hacia Anthropic.
  - `spring.ai.openai.api-key`: `${ANTHROPIC_API_KEY:}`.
  - `spring.ai.openai.chat.options.model`: default `claude-haiku-4-5-20251001`.
  - `spring.ai.openai.chat.options.timeout`: default `PT30S`.
  - `temperature=0.2`, `max-tokens=2048`.
  - `ai-assistant.enabled`: default `false`.
  - `ai-assistant.phase1.memory-writes-enabled`: default `false`.
  - Budgets: `token-budget-per-conversation=12000`, `max-tool-calls-per-turn=5`, `turn-timeout-ms=25000`, `default-proposal-ttl-minutes=60`.

### Schema AI agregado en `schema.sql`

| Tabla | Columnas principales | Constraints / índices |
|---|---|---|
| `ai_accion` | `id`, `empresa_id`, `solicitada_por`, `wa_conversacion_id`, `wa_mensaje_id`, `ai_conversacion_id`, `tipo_accion`, `estado`, `payload_json`, `rationale`, `version`, `expires_at`, `resultado_entidad_id`, `error_reason`, `creado_en`, `actualizado_en` | PK `pk_ai_accion`; check `estado IN ('PENDING','CONFIRMED','REJECTED','EXPIRED','EXECUTED','FAILED')`; índices por `(solicitada_por, estado)`, `(expires_at, estado)`, `(empresa_id, wa_conversacion_id)`, `ai_conversacion_id`. |
| `ai_conversacion` | `id`, `empresa_id`, `actor_usuario_id`, `wa_conversacion_id`, `contacto_id`, `archivada`, `creado_en`, `actualizado_en` | PK; índices `(actor_usuario_id, wa_conversacion_id)` y `(empresa_id, actualizado_en)`. |
| `ai_mensaje` | `id`, `ai_conversacion_id`, `rol`, `contenido`, `modelo`, `prompt_tokens`, `completion_tokens`, `latency_ms`, `tool_call_json`, `creado_en` | PK; check `rol IN ('USER','ASSISTANT','SYSTEM','TOOL')`; índice `(ai_conversacion_id, creado_en)`. |
| `ai_resumen_contexto` | `id`, `actor_usuario_id`, `empresa_id`, `wa_conversacion_id`, `contacto_id`, `facts`, `inferences`, `source_wa_mensaje_id`, `source_watermark`, `ai_conversacion_id`, `creado_en`, `actualizado_en` | PK; índice `(ai_conversacion_id, actualizado_en)`. |
| `ai_memoria` | `id`, `actor_usuario_id`, `empresa_id`, `wa_conversacion_id`, `contacto_id`, `visibilidad`, `contenido`, `origen_tipo`, `origen_id`, `version`, `creado_en`, `actualizado_en`, `expires_at`, `superseded_by`, `superseded`, `expirada` | PK; check `visibilidad IN ('CONVERSACION_SCOPED','CONTACTO_SCOPED')`; índices `(actor_usuario_id, empresa_id, wa_conversacion_id, superseded, expirada)` y `expires_at`. |

Observación: el DDL no crea `ai_memoria_hechos`, aunque `application.yml`, `proposal.md` y `specs/ai-memory/spec.md` lo mencionan.

## 3. Resumen de artefactos OpenSpec

- `proposal.md`: define intención de asistente AI tenant-scoped, tools read-only/propose-only, confirmación humana y DDL `ai_*`. Está desactualizado en algunos detalles: menciona starter/config Anthropic directo, nombres plurales de tablas y `ai_memoria_hechos`.
- `exploration.md`: documenta la corrección resource-first de tenant. Sigue siendo útil para `/chat`, confirm/reject y conversaciones, pero quedó desactualizado en `/acciones` porque presenta auto-resolve de single-company como opción; PR7 lo rechazó.
- `design.md`: artefacto más alineado al estado actual. Registra `ActorContext.usuarioId` como identidad, tenant desde recurso, `/acciones` con `empresaId` requerido, `ActorEmpresaScopePort` Empresa-owned y `AiRestCommandMapper`. Drift menor: la secuencia aún nombra `BindException`; el verify observó `MethodArgumentNotValidException`.
- `specs/ai-assistant/spec.md`: exige resolución tenant resource-first, acceso solo vía tools, tool surface read-only/propose-only, historial, límites por turno, validación server-side y coexistencia con `SugerirRespuestaUseCase`.
- `specs/ai-action-proposal/spec.md`: define ciclo `PENDING → CONFIRMED/REJECTED/EXPIRED`, confirmación solo REST/UI, verificación de actor + tenant de recurso, listado `/acciones` estrictamente tenant-scoped con `empresaId` requerido.
- `specs/ai-memory/spec.md`: define memoria privada por `empresa_id + actor_usuario_id + contacto_id`, writes deshabilitados en fase 1, TTL y hechos ligados a memoria. Gap: exige presencia de `ai_memoria_hechos`, no presente en DDL del slice.
- `specs/security/spec.md`: agrega contrato de `ActorContext` identity-only y autorización de AI desde recursos propios.
- `specs/tablero/spec.md`: tools AI de lectura sobre tableros/fichas/columnas; `MoverColumnaFicha` solo después de `AiAccion` confirmada.
- `tasks.md`: fases 1-4 completas; quedan `5.1`-`5.4` cross-PR verify y `6.7` focused tests/compile. Contiene la decisión PR7 bloqueada: no auto-resolve, `empresaId` obligatorio.
- `apply-progress.md`: bitácora acumulada PR4→PR7 con evidencia Strict TDD. La sección Slice 6 es la referencia actual para PR7; se observan remanentes históricos en secciones previas, por lo que debe leerse por slice y no como estado lineal único.
- `verify-report.md`: PR7 pasa con advertencias. Evidencia verde enfocada: aplicación `10/10`, infraestructura enfocada `47/47`, H2 PR7 `2/2`, package boot con `-DskipTests` exitoso. Full `verify` sigue rojo por deuda fuera de PR7 (`SecurityConfigTest`, `FichaWiringTest`).

## 4. Docs incorrectos o stale

- `docs/reports/keycloak-usuario-id-sync-report.md` es un reporte válido para un cambio estrecho de Keycloak/`usuario_id`, pero es incorrecto como reporte final de branch para `feauture/IA`:
  - No cubre Spring AI, `AiWiringConfig`, feature flag, schema `ai_*`, OpenSpec PR4→PR7 ni selector `empresaId` estricto.
  - Declara pendientes de un OpenSpec `keycloak-usuario-id-sync`, no del cambio actual `add-crm-ai-assistant-spring-ai`.
  - Debería reemplazarse por un reporte final de branch o moverse/archivarse como reporte histórico de otro cambio.
- No existe todavía un reporte final de branch consolidado para este slice/branch; este archivo es solo la exploración del slice boot/OpenSpec/docs.

## 5. Tests cubiertos por comportamiento

No se ejecutaron pruebas durante esta exploración. La cobertura observada proviene de archivos y reportes.

- `AiWiringConfigGateAnnotationTest`:
  - Verifica por reflexión que `AiWiringConfig` está protegido por `@ConditionalOnProperty(ai-assistant.enabled=true, matchIfMissing=false)`.
  - Verifica que `WiringConfig` importa `AiWiringConfig`, de modo que el grafo AI entra al contexto solo cuando el flag habilita la configuración.
- Evidencia OpenSpec/verify relacionada:
  - `/chat`: tenant derivado de la conversación WhatsApp, no de hints externos.
  - Confirm/reject/get conversation: tenant derivado del recurso AI y `empresaId` como cross-check obligatorio.
  - `/acciones`: DTO requiere `empresaId`, mapper construye command, service valida ownership vía `ActorEmpresaScopePort`, repositorio filtra por actor + `PENDING` + tenant.
  - Persistencia: tests unitarios y H2 prueban que el listado de acciones pendientes excluye otros tenants.
  - Validación REST: ausencia de `empresaId` y límites inválidos producen 400; selector no poseído produce 403 controlado.

## 6. Riesgos y gaps del slice

1. **Drift de memoria AI**: `ai-memory/spec.md` y comentarios de `application.yml` mencionan `ai_memoria_hechos`, pero `schema.sql` no la crea.
2. **Drift de nombres de tablas**: `proposal.md` usa nombres plurales (`ai_conversaciones`, `ai_mensajes`, `ai_memorias`, `ai_acciones`), mientras el DDL usa singular (`ai_conversacion`, `ai_mensaje`, `ai_memoria`, `ai_accion`).
3. **Drift de estados de acción**: `specs/ai-action-proposal/spec.md` enumera `PENDING`, `CONFIRMED`, `REJECTED`, `EXPIRED`; `schema.sql` permite además `EXECUTED` y `FAILED`. Debe confirmarse si esos estados son intencionales y actualizar el spec o el DDL.
4. **Drift de proveedor/configuración AI**: `proposal.md` habla de starter/config Anthropic directo; `application.yml` configura `spring.ai.openai.*` con gateway OpenAI-compatible hacia Anthropic y el root `pom.xml` solo agrega BOM Spring AI.
5. **Integridad referencial no declarada**: las tablas AI agregadas no declaran FKs entre sí ni hacia tablas CRM/WhatsApp. Puede ser intencional por estilo incremental, pero debe revisarse antes de archive si se quiere protección relacional.
6. **Gate del controller no confirmado en este slice**: este slice confirma `AiWiringConfig` gated y `WiringConfig @Import`; no inspeccionó el `AiController` fuera del alcance. Si el controller no está condicionado por flag o depende de beans ausentes, el arranque con `ai-assistant.enabled=false` debe seguir cubierto por verify.
7. **Deuda de suite completa**: `verify-report.md` documenta fallos fuera de PR7 en `SecurityConfigTest`, `FichaWiringTest` y un IT de expiración de acciones. No bloquean este slice, pero sí deben quedar claros en el reporte final.
8. **Artefactos no trackeados**: `docs/reports/keycloak-usuario-id-sync-report.md`, `boot-stderr.log` y nuevos OpenSpec `security/tablero/apply-progress` están sin trackear; revisar antes de commit para evitar subir ruido o reportes equivocados.
9. **`boot-stderr.log` está vacío**: debe eliminarse o ignorarse antes del commit final.

## 7. Recomendación

Antes del reporte final de branch o archive:

- Reemplazar `docs/reports/keycloak-usuario-id-sync-report.md` por un reporte final de branch que cubra el alcance real del asistente AI.
- Alinear `proposal.md`, `exploration.md`, `application.yml` y `specs/ai-memory/spec.md` con el schema real o agregar la tabla `ai_memoria_hechos` si el contrato sigue vigente.
- Resolver o documentar explícitamente el drift de estados `EXECUTED/FAILED` en `ai_accion`.
- Ejecutar el cross-PR verify pendiente y separar claramente la deuda preexistente de los resultados del cambio AI.
