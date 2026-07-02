# Reporte final de rama — Asistente IA

**Proyecto**: `ar-crm / CRM2`  
**Rama revisada**: `feauture/IA`  
**Cambio OpenSpec**: `add-crm-ai-assistant-spring-ai`  
**Fuente de verdad**: `git status --short --untracked-files=all`, reportes parciales `branch-report-exploration-*.md`, `apply-progress.md` y `verify-report.md`.  
**Nota**: este reporte consolida el branch de asistente IA. No reemplaza ni valida el reporte estrecho `docs/reports/keycloak-usuario-id-sync-report.md`, que pertenece a otro cambio.

## Resumen ejecutivo

La rama introduce un asistente IA para CRM2 sobre una arquitectura hexagonal: dominio Spring-free, casos de uso de aplicación, adaptadores REST/tools/persistencia, integración Spring AI y schema `ai_*`. El diseño mantiene una separación fuerte entre **identidad autenticada** y **autoridad de tenant**: `ActorContext` identifica al usuario, mientras el `empresaId` se deriva del recurso o se exige como selector explícito.

El cambio está avanzado y cubierto por múltiples suites enfocadas, con estado OpenSpec **PASS WITH WARNINGS** para PR7. Aun así, no está listo para cerrar sin revisar los gaps documentales y de integración: drift de `ai_memoria_hechos`, estados `EXECUTED/FAILED` no reflejados en todos los specs, algunos archivos críticos no trackeados, y suite completa roja por deuda preexistente fuera del slice IA.

## Qué agrega la rama

| Área | Cambio principal |
|---|---|
| Dominio | Nuevas entidades IA: `AiAccion`, `AiConversacion`, `AiMensaje`, `AiMemoria`, `AiResumenContexto`; VOs, enums, excepciones y política `EmpresaPermitidaPolicy`. |
| Aplicación | Commands, results, ports-in, ports-out y services para analizar chats, registrar mensajes, proponer acciones, confirmar/rechazar/expirar acciones y listar conversaciones/propuestas. |
| Seguridad / tenant | `ActorContext` queda como identidad; `ActorEmpresaScopePort` valida empresas del actor; los flujos sensibles usan resource-first o selector explícito. |
| Inbound | `AiController`, DTOs REST, mapper REST y tools Spring AI read-only/propose-only. |
| Outbound | `OpenAiChatAdapter`, contexto de tools por `ThreadLocal`, persistencia JPA `ai_*`, bridges CRM/WhatsApp y mapper JWT identity-only. |
| Boot/config | `AiWiringConfig` gateado por `ai-assistant.enabled=false` por defecto, importado desde `WiringConfig`. |
| Schema | Tablas `ai_accion`, `ai_conversacion`, `ai_mensaje`, `ai_resumen_contexto`, `ai_memoria` con índices y checks. |
| OpenSpec | Proposal/design/specs/tasks/apply-progress/verify-report para el cambio `add-crm-ai-assistant-spring-ai`. |

## Arquitectura y contratos clave

### 1. Dominio Spring-free

- `domain` no depende de Spring, JPA, Spring AI ni WhatsApp.
- `AiAccion` modela el ciclo de vida de propuestas IA:
  - `PENDING -> CONFIRMED | REJECTED | EXPIRED`
  - `CONFIRMED -> EXECUTED | FAILED`
- El payload de acción queda opaco para dominio; la forma JSON se interpreta en aplicación.
- `AiConversacion`, `AiMemoria` y `AiResumenContexto` encapsulan ownership, scope y lifecycle.

### 2. Aplicación hexagonal

- REST, scheduler y tools entran por ports-in/use cases.
- Persistencia, generación IA, lectura WhatsApp, lectura CRM y contexto de tools salen por ports-out.
- DTOs como `ChatAsistenteRequest`, `RespuestaAsistente`, `AiToolContext` y `WhatsappConversacionResumen` son application-owned: evitan filtrar tipos Spring AI, JPA o del módulo WhatsApp.

### 3. Tenant resource-first y selector explícito

- `ActorContext` ya no transporta `empresaId`; solo identidad (`subject`, `username`, `email`, `usuarioId`, `superUsuarioId`, roles).
- `/chat` deriva tenant desde la conversación WhatsApp (`canalEmpresaId`).
- Confirmar/rechazar/obtener conversación usan cross-check estricto entre `empresaId` seleccionado y tenant del recurso persistido.
- `GET /api/ai/acciones` exige `empresaId`; no auto-resuelve ni toma la primera empresa.
- `ActorEmpresaScopePort` + `ActorEmpresaScopeService` + `EmpresaPermitidaPolicy` centralizan la validación de empresas poseídas por el actor.

### 4. Tools sin identidad provista por el modelo

- Las tools no aceptan actor, tenant ni conversación desde el payload del modelo.
- `AiToolContextPort` resuelve el contexto confiable.
- Tools de lectura consultan ports de aplicación; `ProponerAccionTool` solo stagea propuestas `PENDING` mediante `ProponerAccionUseCase`.
- La única ejecución real de mutaciones CRM está en `ConfirmarAccionService`.

## Inventario por capa

### Dominio

| Grupo | Archivos clave |
|---|---|
| Entidades IA | `domain/src/main/java/com/ar/crm2/model/entity/ia/*` |
| VOs IA | `domain/src/main/java/com/ar/crm2/model/vo/Ai*Id.java` |
| Enums | `EstadoAccion`, `TipoAccion`, `RolMensajeAi`, `OrigenMemoria`, `VisibilidadMemoria` |
| Excepciones | `Accion*Exception`, `ConversacionAsistenteNotOwnedByActorException`, `TenantScopeViolationException` |
| Política | `EmpresaPermitidaPolicy` |

### Aplicación

| Grupo | Responsabilidad |
|---|---|
| Commands | Validan invariantes de frontera: actor, recurso, límites, TTL, `empresaId` obligatorio u opcional según flujo. |
| Ports-in | Exponen use cases de análisis, propuesta, confirmación, rechazo, expiración, listado y consulta. |
| Ports-out | Aíslan persistencia IA, generación IA, embeddings, contexto de tools, lectura CRM y lectura WhatsApp. |
| Services | Coordinan puertos y delegan reglas a dominio; concentran mutación CRM real solo en confirmación. |
| Empresa/security | `ActorEmpresaScopePort`, `FindEmpresasByCreadorPort`, `ActorEmpresaScopeService`, `ActorContext`. |

### Inbound / outbound

| Grupo | Archivos representativos |
|---|---|
| REST AI | `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/ai/AiController.java`, `AiRestCommandMapper.java`, `dto/ai/*` |
| Exception handling | `GlobalExceptionHandler.java` con handlers IA. |
| Tools | `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/*` |
| Spring AI | `OpenAiChatAdapter`, `SpringAiPromptMapper`, `SpringAiChatResponseMapper`, `GenerarEmbeddingStubAdapter` |
| Tool context | `AiToolContextAdapter`, `ThreadLocalAiToolContextHolder`, `AiToolContextHolder` |
| Persistencia IA | `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/**` |
| Bridges CRM | adapters de `Columna`, `Contacto`, `Empresa`, `Ficha`, `Tarea`, `Trato` |
| Bridges WhatsApp | `WhatsappConversacionLecturaAdapter`, `WhatsappMensajeLecturaAdapter` |
| Seguridad JWT | `KeycloakJwtActorContextMapper` |

### Boot, configuración y schema

| Archivo | Rol |
|---|---|
| `boot/src/main/java/com/ar/crm2/config/AiWiringConfig.java` | Registra el grafo AI completo bajo `@ConditionalOnProperty(ai-assistant.enabled=true)`. |
| `boot/src/main/java/com/ar/crm2/config/WiringConfig.java` | Importa `AiWiringConfig`. |
| `boot/src/main/resources/application.yml` | Configura `ai-assistant.*` y `spring.ai.openai.*` hacia gateway OpenAI-compatible. |
| `pom.xml` | Agrega BOM Spring AI `2.0.0`. |
| `infrastructure/pom.xml` | Agrega dependencia Spring AI OpenAI starter. |
| `boot/src/main/resources/schema.sql` | Crea DDL `ai_*`. |

## Schema `ai_*`

| Tabla | Propósito | Notas |
|---|---|---|
| `ai_accion` | Propuestas IA y resultado de confirmación. | Check de estados incluye `PENDING`, `CONFIRMED`, `REJECTED`, `EXPIRED`, `EXECUTED`, `FAILED`. |
| `ai_conversacion` | Conversaciones IA por actor/empresa/conversación WhatsApp. | Índices por actor + WA conversation y empresa + actualización. |
| `ai_mensaje` | Historial IA. | Roles `USER`, `ASSISTANT`, `SYSTEM`, `TOOL`. |
| `ai_resumen_contexto` | Resúmenes persistentes de contexto. | Asociado a conversación IA. |
| `ai_memoria` | Memoria privada por actor/empresa y scope. | Scopes `CONVERSACION_SCOPED` y `CONTACTO_SCOPED`. |

**Drift importante**: `ai_memoria_hechos` aparece en algunos artefactos/specs, pero no existe en `schema.sql`.

## Evidencia de tests y verificación

| Evidencia | Resultado reportado |
|---|---|
| Application focused PR7 | `10/10` verde. |
| Infrastructure focused PR7 | `47/47` verde. |
| H2 query tenant-scoped | `2/2` verde, con query filtrada por `solicitada_por`, `estado`, `empresa_id`. |
| Boot package | `BUILD SUCCESS` con `-DskipTests`. |
| Suites acumuladas del slice | `171/171` application + `102/102` infrastructure focal según `apply-progress.md`. |
| OpenSpec verify PR7 | `PASS WITH WARNINGS`. |

### Rojo conocido fuera del slice

| Suite | Estado | Causa reportada |
|---|---|---|
| `./mvnw.cmd verify` | Rojo | 31 errores en `SecurityConfigTest`, principalmente configuración sin `WaApiKeyFilter`. |
| `FichaWiringTest` | Rojo | `NotaTratoRepository` faltante/no descubierto. |
| Expiración IT | Rojo puntual | Caso `aiAccion_findPendingExpired_shouldReturnOnlyPENDINGPastExpiry` pendiente. |

Estos rojos están documentados como deuda preexistente o fuera del PR7 focal; no deben confundirse con fallo directo del selector estricto de IA.

## Riesgos y gaps que no conviene esconder

1. **Archivos críticos no trackeados**: varios contratos PR7 siguen como `??`, incluyendo `ListarAccionesPendientesCommand`, `ListarAccionesPendientesUseCase`, `ListPendingAiAccionesPort`, `AiTenantExceptionTranslator`, `ListarAccionesPendientesService`, `ActorEmpresaScopePort`, tests REST y `AiWiringConfig`. Si no se agregan, el branch queda incompleto.
2. **Drift `ai_memoria_hechos`**: referenciada en proposal/spec/config, pero sin DDL real.
3. **Plurales vs singular en docs**: algunos artefactos hablan de `ai_conversaciones`, `ai_mensajes`, `ai_memorias`, `ai_acciones`; el DDL usa singular.
4. **Estados de acción**: specs mencionan principalmente `PENDING`, `CONFIRMED`, `REJECTED`, `EXPIRED`; el código/schema agregan `EXECUTED` y `FAILED`.
5. **OpenAI-compatible vs Anthropic directo**: proposal original menciona Anthropic starter/config directa, pero la implementación usa Spring AI OpenAI starter/gateway compatible hacia Anthropic.
6. **Tools de lectura saltan use cases**: llaman ports directamente. Puede ser aceptable por ser read-only, pero debe quedar como decisión consciente.
7. **`ThreadLocalAiToolContextHolder`**: correcto para flujo síncrono, riesgoso si Spring AI pasa a ejecución async/reactive.
8. **Parsing JSON manual en `ConfirmarAccionMapper`**: funciona para JSON plano controlado, pero no es robusto ante escapes/objetos anidados.
9. **Tenant opcional en algunos servicios**: `RegistrarMensajeAsistenteService`, `ObtenerAccionService` y `ListarConversacionesAsistenteService` aún pueden resolver tenant antes/aparte del recurso usando fallback; revisar si debe alinearse con resource-first estricto.
10. **Handlers faltantes o ambiguos**: `AiAssistantException` debería mapearse a 502; validaciones como `BindException`/`HandlerMethodValidationException` deben producir cuerpo uniforme si se prometió esa UX.
11. **PII en logs**: revisar si `BuscarClientePorTelefonoTool` registra teléfonos completos.
12. **JaCoCo por archivo modificado**: no se emite como evidencia; queda como limitación de cobertura.

## Limpieza recomendada antes de commit o archive

- Eliminar `boot-stderr.log` si sigue vacío.
- No incluir `docs/reports/keycloak-usuario-id-sync-report.md` en este cambio, salvo que se mueva/aclare como reporte histórico de otro scope.
- Agregar explícitamente los archivos `??` que son parte del contrato PR7.
- Alinear OpenSpec con el código real:
  - `ai_memoria_hechos`: diferida a fase 2 o implementada en DDL.
  - nombres de tablas en singular.
  - estados `EXECUTED`/`FAILED` documentados o removidos.
  - proveedor/config Spring AI como gateway OpenAI-compatible.
  - excepción MVC real (`MethodArgumentNotValidException`) si aplica.
- Ejecutar o cerrar `tasks.md` fase 5 (`5.1`-`5.4`) y `6.7` antes de archivar el cambio.
- Documentar explícitamente que `ai-assistant.enabled=false` es el kill-switch/default de reversión.

## Archivos fuente relevantes

### Reportes parciales consolidados

- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-domain.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-domain-application.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-application-contracts.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-application-services.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-application-empresa-security.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-inbound.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-outbound.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/branch-report-exploration-boot-openspec.md`

### OpenSpec

- `openspec/changes/add-crm-ai-assistant-spring-ai/proposal.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/design.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/tasks.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/apply-progress.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/verify-report.md`
- `openspec/changes/add-crm-ai-assistant-spring-ai/specs/**/spec.md`

### Código principal

- `domain/src/main/java/com/ar/crm2/model/entity/ia/**`
- `application/src/main/java/com/ar/crm2/application/ai/**`
- `application/src/main/java/com/ar/crm2/application/empresa/**`
- `application/src/main/java/com/ar/crm2/application/security/ActorContext.java`
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/ai/**`
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/**`
- `infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/**`
- `infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/**`
- `boot/src/main/java/com/ar/crm2/config/AiWiringConfig.java`
- `boot/src/main/resources/application.yml`
- `boot/src/main/resources/schema.sql`

## Veredicto

El branch tiene una arquitectura coherente y una separación sana entre IA, dominio CRM, seguridad y persistencia. El mayor valor técnico está en que el modelo **puede proponer**, pero no puede inventar identidad/tenant ni ejecutar mutaciones reales sin confirmación humana.

Antes de cerrar, el foco debe estar en higiene de entrega: agregar los archivos no trackeados correctos, limpiar ruido, alinear los specs con el código real y dejar explícita la deuda de verify global para que no contamine el resultado del cambio IA.
