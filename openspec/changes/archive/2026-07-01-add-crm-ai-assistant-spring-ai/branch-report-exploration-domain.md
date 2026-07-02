# Exploración de rama — Dominio del asistente IA CRM

## Alcance

- Proyecto: `ar-crm` / `CRM2`.
- Cambio OpenSpec: `add-crm-ai-assistant-spring-ai`.
- Rutas revisadas: `domain/src/main/java/com/ar/crm2/**` y `domain/src/test/java/com/ar/crm2/**`.
- Fuente usada: `git status --short` y `git diff --name-status HEAD` sobre el módulo `domain`.
- Estado observado: todos los archivos de este slice aparecen como agregados (`A`); no se observaron modificaciones a clases de dominio existentes dentro del alcance.
- Pruebas: no se ejecutaron, por instrucción explícita.

## Inventario de archivos nuevos/cambiados

### Entidades

- `domain/src/main/java/com/ar/crm2/model/entity/ia/AiAccion.java` — Propuesta de acción CRM sugerida por IA y su ciclo de aprobación/ejecución.
- `domain/src/main/java/com/ar/crm2/model/entity/ia/AiConversacion.java` — Sesión de asistente IA con alcance por usuario, empresa, conversación WhatsApp y contacto opcional.
- `domain/src/main/java/com/ar/crm2/model/entity/ia/AiMemoria.java` — Memoria atómica reutilizable y privada del asistente.
- `domain/src/main/java/com/ar/crm2/model/entity/ia/AiMensaje.java` — Turno individual de conversación IA con rol, contenido, métricas y tool call opaco.
- `domain/src/main/java/com/ar/crm2/model/entity/ia/AiResumenContexto.java` — Resumen persistente de hechos e inferencias para una conversación.

### Value Objects

- `domain/src/main/java/com/ar/crm2/model/vo/AiAccionId.java`
- `domain/src/main/java/com/ar/crm2/model/vo/AiConversacionId.java`
- `domain/src/main/java/com/ar/crm2/model/vo/AiMemoriaId.java`
- `domain/src/main/java/com/ar/crm2/model/vo/AiMensajeId.java`
- `domain/src/main/java/com/ar/crm2/model/vo/AiResumenContextoId.java`

### Enums

- `domain/src/main/java/com/ar/crm2/model/enums/EstadoAccion.java`
- `domain/src/main/java/com/ar/crm2/model/enums/OrigenMemoria.java`
- `domain/src/main/java/com/ar/crm2/model/enums/RolMensajeAi.java`
- `domain/src/main/java/com/ar/crm2/model/enums/TipoAccion.java`
- `domain/src/main/java/com/ar/crm2/model/enums/VisibilidadMemoria.java`

### Excepciones

- `domain/src/main/java/com/ar/crm2/exception/AccionExpiredException.java`
- `domain/src/main/java/com/ar/crm2/exception/AccionNotFoundException.java`
- `domain/src/main/java/com/ar/crm2/exception/AccionNotOwnedByActorException.java`
- `domain/src/main/java/com/ar/crm2/exception/AccionStateException.java`
- `domain/src/main/java/com/ar/crm2/exception/AccionStateTransitionException.java`
- `domain/src/main/java/com/ar/crm2/exception/AccionVersionMismatchException.java`
- `domain/src/main/java/com/ar/crm2/exception/ConversacionAsistenteNotOwnedByActorException.java`
- `domain/src/main/java/com/ar/crm2/exception/TenantScopeViolationException.java`

### Políticas

- `domain/src/main/java/com/ar/crm2/model/policy/EmpresaPermitidaPolicy.java`

### Tests

- `domain/src/test/java/com/ar/crm2/model/entity/AiConversacionTest.java`
- `domain/src/test/java/com/ar/crm2/model/entity/AiMemoriaTest.java`
- `domain/src/test/java/com/ar/crm2/model/entity/AiMensajeTest.java`
- `domain/src/test/java/com/ar/crm2/model/entity/AiResumenContextoTest.java`
- `domain/src/test/java/com/ar/crm2/model/entity/ia/AiAccionPolicyTest.java`
- `domain/src/test/java/com/ar/crm2/model/entity/ia/AiAccionTest.java`
- `domain/src/test/java/com/ar/crm2/model/entity/ia/AiConversacionPolicyTest.java`
- `domain/src/test/java/com/ar/crm2/model/policy/EmpresaPermitidaPolicyTest.java`

## Clases de producción: responsabilidades, API pública e invariantes

### Entidades

#### `AiConversacion`

- Responsabilidad: representar una sesión del asistente IA con alcance de tenant y actor: `empresaId`, `actorUsuarioId`, `waConversacionId` y `contactoId` opcional.
- Factories:
  - `crear(EmpresaId, UsuarioId, String, ContactoId, LocalDateTime)` crea una conversación no archivada, genera `AiConversacionId`, trimmea `waConversacionId` y asigna timestamps iniciales.
  - `reconstitute(...)` reconstruye desde persistencia preservando estado y timestamps.
- Transiciones:
  - `archivar(LocalDateTime)` marca la conversación como archivada; es idempotente si ya estaba archivada.
- Consultas y políticas:
  - `perteneceA(UsuarioId)` y `perteneceA(EmpresaId)` validan pertenencia por actor o empresa.
  - `scopeEs(String)` valida coincidencia con la conversación WhatsApp de origen.
  - `requireOwnedBy(UsuarioId, EmpresaId)` centraliza autorización de lectura/continuación: exige actor original y empresa correcta; primero valida actor y luego tenant.
- Invariantes/validaciones:
  - `empresaId`, `actorUsuarioId` y `waConversacionId` son obligatorios.
  - `contactoId` es opcional.
  - La entidad no expone setters; los cambios devuelven una nueva instancia.

#### `AiMensaje`

- Responsabilidad: almacenar un turno de conversación IA sin reemplazar el transcript canónico de WhatsApp.
- Factories:
  - `crear(AiConversacionId, RolMensajeAi, String, String, Integer, Integer, Long, String, LocalDateTime)` genera `AiMensajeId`, trimmea `contenido` y guarda metadatos opcionales de modelo, tokens, latencia y `toolCallJson`.
  - `reconstitute(...)` reconstruye desde persistencia.
- Consultas:
  - `perteneceA(AiConversacionId)` verifica asociación con la conversación IA padre.
- Invariantes/validaciones:
  - `aiConversacionId`, `rol` y `contenido` son obligatorios.
  - `contenido` debe tener entre 1 y 16384 caracteres.
  - `promptTokens`, `completionTokens` y `latencyMs`, si se informan, no pueden ser negativos.
  - `toolCallJson` se conserva de forma opaca; el dominio no interpreta payloads de herramientas.

#### `AiMemoria`

- Responsabilidad: representar un hecho o decisión atómica reutilizable por el asistente, siempre privada al alcance `(actorUsuarioId, empresaId)` y además acotada a conversación WhatsApp o contacto.
- Factories:
  - `crear(UsuarioId, EmpresaId, String, ContactoId, VisibilidadMemoria, String, OrigenMemoria, String, LocalDateTime, LocalDateTime)` crea una memoria activa con `version=1`, `superseded=false`, `expirada=false` y `expiresAt` explícito.
  - `reconstitute(...)` reconstruye desde persistencia, incluyendo versión, reemplazo y flags.
- Transiciones:
  - `supersede(AiMemoriaId, LocalDateTime)` marca la memoria como reemplazada, registra `supersededBy` e incrementa versión; es idempotente.
  - `expirar(LocalDateTime)` marca la memoria como expirada e incrementa versión; es idempotente.
- Consultas:
  - `perteneceA(UsuarioId)` y `perteneceA(EmpresaId)` verifican pertenencia.
  - `estaExpirada(LocalDateTime)` considera el flag `expirada` o `now >= expiresAt`.
  - `estaViva(LocalDateTime)` exige no estar superseded ni expirada.
- Invariantes/validaciones:
  - `actorUsuarioId`, `empresaId`, `visibilidad`, `origenTipo`, `contenido`, `ahora` y `expiresAt` son obligatorios.
  - `contenido` debe tener entre 1 y 4000 caracteres.
  - `expiresAt` debe ser posterior a `ahora`.
  - `CONVERSACION_SCOPED` exige `waConversacionId` no blank y prohíbe `contactoId`.
  - `CONTACTO_SCOPED` exige `contactoId` y prohíbe `waConversacionId` no blank.

#### `AiResumenContexto`

- Responsabilidad: persistir un resumen compacto de hechos e inferencias para evitar reenviar todo el historial en turnos posteriores.
- Factories:
  - `crear(UsuarioId, EmpresaId, String, ContactoId, String, String, String, long, AiConversacionId, LocalDateTime)` genera `AiResumenContextoId`, trimmea `waConversacionId`, `facts` e `inferences`, y registra watermark/fuente.
  - `reconstitute(...)` reconstruye desde persistencia.
- Transiciones:
  - `reemplazarCon(String, String, String, long, LocalDateTime)` reemplaza facts/inferences, actualiza `sourceWaMensajeId`, preserva identidad y actualiza timestamp.
- Consultas:
  - `perteneceA(UsuarioId)` y `perteneceA(EmpresaId)` verifican pertenencia.
  - `esStale(long)` indica si el watermark actual es estrictamente mayor que el registrado.
- Invariantes/validaciones:
  - `actorUsuarioId`, `empresaId`, `waConversacionId`, `aiConversacionId`, `facts` e `inferences` son obligatorios.
  - `facts` e `inferences` deben tener entre 1 y 16384 caracteres.
  - `sourceWatermark` no puede ser negativo.
  - `reemplazarCon` rechaza un watermark menor al actual.
  - La documentación declara que `wa_mensaje` conserva autoridad si contradice el resumen.

#### `AiAccion`

- Responsabilidad: ser el límite seguro para mutaciones CRM sugeridas por IA. El dominio permite proponer, confirmar/rechazar, expirar y registrar resultado, pero no ejecuta mutaciones reales.
- Factories:
  - `crear(EmpresaId, UsuarioId, String, String, AiConversacionId, String, String, String, int, LocalDateTime)` crea una propuesta `PENDING`, genera `AiAccionId`, `version=1`, `expiresAt=ahora+ttl`, timestamps y preserva `payloadJson` de forma opaca.
  - `reconstitute(...)` reconstruye desde persistencia.
- Máquina de estados:
  - `confirmar(LocalDateTime)`: `PENDING -> CONFIRMED`, incrementa versión.
  - `rechazar(LocalDateTime)`: `PENDING -> REJECTED`, incrementa versión.
  - `expirar(LocalDateTime)`: `PENDING -> EXPIRED`, incrementa versión; idempotente si ya está `EXPIRED`.
  - `marcarEjecutada(String, LocalDateTime)`: `CONFIRMED -> EXECUTED`, registra `resultadoEntidadId`.
  - `marcarFallida(String, LocalDateTime)`: `CONFIRMED -> FAILED`, registra `errorReason`.
- Consultas y políticas:
  - `estaExpirada(LocalDateTime)` aplica solo a propuestas `PENDING`.
  - `perteneceA(UsuarioId)` y `perteneceA(EmpresaId)` verifican pertenencia.
  - `esTerminal()` reconoce `REJECTED`, `EXPIRED`, `EXECUTED` y `FAILED`.
  - `requireOwnedBy(UsuarioId, EmpresaId)` exige solicitante original y empresa correcta.
  - `requirePending(String)` exige estado `PENDING` para una operación.
  - `requireVersion(int)` aplica control optimista.
  - `requireNotExpired(LocalDateTime)` rechaza propuestas pendientes vencidas.
  - `requireConfirmable(int, LocalDateTime)` compone estado pendiente, versión y vigencia, dejando ownership/tenant como paso separado.
- Invariantes/validaciones:
  - `empresaId`, `solicitadaPor`, `waConversacionId`, `aiConversacionId`, `tipoAccion`, `payloadJson`, `rationale` y `ttl` positivo son obligatorios.
  - `tipoAccion`: 1-50 caracteres.
  - `payloadJson`: 1-16384 caracteres.
  - `rationale`: 1-2000 caracteres.
  - `waMensajeId` puede ser `null`, pero si se informa no puede ser blank.
  - Las transiciones inválidas lanzan excepciones de dominio y no mutan la instancia original.

### Value Objects

Todos los IDs nuevos siguen el mismo patrón: `record` con `UUID value`, constructor compacto con `DomainAssert.notNull`, factory `from(UUID)` y factory `create()` basada en `UUID.randomUUID()`.

- `AiAccionId`: identidad de una propuesta de acción IA durante todo su ciclo.
- `AiConversacionId`: identidad de una sesión del asistente.
- `AiMemoriaId`: identidad de una memoria atómica y posible referencia `supersededBy`.
- `AiMensajeId`: identidad de un turno individual.
- `AiResumenContextoId`: identidad del resumen persistente.

### Enums

- `EstadoAccion`: `PENDING`, `CONFIRMED`, `REJECTED`, `EXPIRED`, `EXECUTED`, `FAILED`. Documenta el grafo permitido de `AiAccion`.
- `OrigenMemoria`: `CHAT_ANALYSIS`, `FOLLOW_UP`, `MANUAL`, `PROPOSAL`. Sirve para procedencia y ponderación de memoria.
- `RolMensajeAi`: `USER`, `ASSISTANT`, `SYSTEM`, `TOOL`. Distingue la interpretación de cada turno.
- `TipoAccion`: `CREATE_CONTACTO`, `CREATE_TRATO`, `CREATE_TAREA`, `MOVE_KANBAN_FICHA`. Define discriminadores esperados para propuestas; la validación de payload queda fuera del dominio.
- `VisibilidadMemoria`: `CONVERSACION_SCOPED`, `CONTACTO_SCOPED`. Define qué clave de alcance debe existir en `AiMemoria`.

### Excepciones

- `AccionExpiredException`
  - Constructor público `AccionExpiredException(String)`.
  - Factory `expired(String accionId, String expiresAt)`.
  - Uso: propuesta pendiente vencida; mapeable a 409.
- `AccionNotFoundException`
  - Constructor público `AccionNotFoundException(String)`.
  - Factory `forId(UUID)`.
  - Uso: acción IA inexistente; mapeable a 404.
- `AccionNotOwnedByActorException`
  - Constructor público `AccionNotOwnedByActorException(String)`.
  - Factory `notRequester(String actorUsuarioId, String accionId)`.
  - Uso: actor o empresa no autorizados para operar una acción; mapeable a 403.
- `AccionStateException`
  - Constructor público `AccionStateException(String)`.
  - Factory `invalidState(String accionId, String operacion, String estadoActual)`.
  - Uso: operación de aplicación incompatible con el estado actual; mapeable a 409.
- `AccionStateTransitionException`
  - Constructores públicos con `String` y con `String, Throwable`.
  - Factories `transicionNoPermitida(String estadoActual, String accion)` y `estadoTerminal(String estadoActual)`.
  - Uso: transiciones inválidas de la máquina de estados de `AiAccion`; mapeable a 409.
- `AccionVersionMismatchException`
  - Constructor público `AccionVersionMismatchException(String)`.
  - Factory `mismatch(String accionId, int expected, int actual)`.
  - Uso: conflicto de versión optimista; mapeable a 409.
- `ConversacionAsistenteNotOwnedByActorException`
  - Constructor público `ConversacionAsistenteNotOwnedByActorException(String)`.
  - Factories `notOwner(String actorUsuarioId, String aiConversacionId)` y `tenantMismatch(String actorUsuarioId, String aiConversacionId)`.
  - Uso: actor o empresa no autorizados para leer o continuar una conversación; mapeable a 403.
- `TenantScopeViolationException`
  - Constructor público `TenantScopeViolationException(String)`.
  - Uso: empresa solicitada fuera del set permitido o actor sin empresas; mapeable a 403.

### Política

#### `EmpresaPermitidaPolicy`

- Responsabilidad: seleccionar la empresa que un actor puede usar en operaciones IA, sin consultar puertos ni infraestructura.
- API pública:
  - `seleccionarEmpresaPermitida(List<EmpresaId> owned, UUID requestedId)`.
- Reglas:
  - Si `requestedId` existe, debe pertenecer a `owned`; si no, lanza `TenantScopeViolationException`.
  - Si `requestedId` es `null`, retorna la primera empresa de `owned`.
  - Si no hay empresas, lanza `TenantScopeViolationException`.
- Decisión explícita documentada: fase 1 no soporta multi-empresa real; el fallback a la primera empresa es intencional.

## Cómo el modelo soporta el asistente IA

- Conversaciones: `AiConversacion` fija el scope del asistente por actor, empresa, WhatsApp y contacto opcional. `requireOwnedBy` evita que otro actor o tenant lea o continúe la sesión.
- Mensajes: `AiMensaje` captura los turnos `USER`, `ASSISTANT`, `SYSTEM` y `TOOL`, con métricas de modelo. El transcript WhatsApp sigue siendo la fuente canónica; los tool calls son JSON opaco.
- Memoria: `AiMemoria` permite persistir hechos atómicos reutilizables, privados por `(actorUsuarioId, empresaId)` y acotados a conversación o contacto. El ciclo `supersede/expirar` evita reutilizar memoria obsoleta.
- Resúmenes: `AiResumenContexto` conserva facts/inferences comprimidos con `sourceWatermark` para saber cuándo regenerar contexto. La autoridad final queda en `wa_mensaje`.
- Acciones: `AiAccion` transforma sugerencias IA en propuestas auditables. Solo una propuesta `PENDING`, vigente, con versión correcta y perteneciente al solicitante puede confirmarse; la ejecución real queda fuera del dominio.
- Tenant scope: las entidades principales conservan `empresaId` y actor/solicitante, y exponen queries/políticas de pertenencia. Las excepciones nuevas modelan explícitamente 403/409/404 esperables en capas superiores.
- Política de empresa permitida: `EmpresaPermitidaPolicy` encapsula la selección de empresa desde el set ya resuelto por aplicación, separando autorización de tenant de infraestructura y evitando reglas duplicadas en servicios.

## Cobertura de tests por comportamiento

- `AiAccionTest`: creación válida, validaciones de campos obligatorios, opacidad de payload, linkage de auditoría con conversación/mensaje WhatsApp, versión inicial, transiciones válidas e inválidas, terminalidad y expiración.
- `AiAccionPolicyTest`: guards de ownership/tenant, estado pendiente, versión esperada, vigencia y composición `requireConfirmable`, incluyendo orden de validación.
- `AiConversacionTest`: creación con/sin contacto, estado inicial no archivado, pertenencia por actor/empresa, scope WhatsApp, archivado idempotente y reconstitución.
- `AiConversacionPolicyTest`: autorización de conversación por actor y empresa, orden de validación actor antes que empresa, y contacto opcional sin impacto en ownership.
- `AiMensajeTest`: validación de rol/contenido/conversación, tokens y latencia no negativos, persistencia opaca de `toolCallJson`, pertenencia a conversación y reconstitución.
- `AiMemoriaTest`: reglas de visibilidad conversación/contacto, expiración por fecha o flag, vida útil, supersession, versionado, ownership y reconstitución.
- `AiResumenContextoTest`: creación, watermark no negativo, reemplazo con watermark monotónico, detección de stale, ownership y reconstitución.
- `EmpresaPermitidaPolicyTest`: selección con empresa explícita propia, rechazo de empresa ajena, fallback a primera empresa cuando no se solicita una explícita, rechazo cuando no hay empresas y constructor privado de utility class.

## Riesgos y gaps del slice de dominio

- Validación de `ahora` inconsistente: varias factories/transiciones usan `LocalDateTime` como timestamp, pero no todas validan null antes de asignar o usar el valor. Esto puede producir `NullPointerException` o timestamps nulos en vez de una excepción de dominio uniforme.
- `EmpresaPermitidaPolicy.seleccionarEmpresaPermitida` no valida `owned == null`; una lista nula produciría NPE fuera del lenguaje de dominio.
- `AiAccion` guarda `tipoAccion` como `String` aunque existe `TipoAccion`; esto preserva flexibilidad, pero exige que la aplicación valide pertenencia al enum y payload schema para evitar drift.
- Los métodos `reconstitute` confían en persistencia y no revalidan todos los invariantes. Es coherente con el comentario del código, pero aumenta la responsabilidad de mappers/adapters.
- Normalización de scope no es homogénea: `AiConversacion` y `AiResumenContexto` trimmean `waConversacionId`; `AiMemoria` valida scope pero conserva el valor recibido.
- La política de empresa sin `requestedId` depende del orden de `owned.get(0)`. La aplicación debe garantizar orden determinista o limitar efectivamente a una empresa en fase 1.
- La cobertura actual se centra en entidades/políticas. No hay tests directos para los value objects, enums, factories de excepciones, límites máximos de longitud, `waMensajeId` blank, `completionTokens` negativo ni nulidad de timestamps/listas.

## Conclusión

El slice de dominio introduce un modelo rico y Spring-free para conversaciones, mensajes, memoria, resúmenes y acciones del asistente IA. La decisión central es correcta: el dominio modela seguridad, pertenencia, vigencia, versionado y lifecycle; la interpretación de payloads IA y la ejecución de mutaciones CRM quedan delegadas a la aplicación. Antes de integrar capas superiores conviene cerrar los gaps de validación/normalización para evitar que errores técnicos escapen al lenguaje de dominio.
