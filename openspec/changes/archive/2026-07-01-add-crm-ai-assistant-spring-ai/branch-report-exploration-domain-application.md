# Exploración de rama: dominio y aplicación del asistente IA

## Alcance y fuente de verdad

- Proyecto: `ar-crm` / `CRM2`.
- Cambio OpenSpec: `add-crm-ai-assistant-spring-ai`.
- Slice inspeccionado: `domain/src/main/java/com/ar/crm2/**`, `domain/src/test/java/com/ar/crm2/**`, `application/src/main/java/com/ar/crm2/application/ai/**`, cambios de `application/.../empresa/**`, `application/.../security/ActorContext.java` y tests correspondientes de `application`.
- Fuente de verdad usada: working tree (`git status --short`, `git diff --name-status HEAD` y archivos no trackeados del slice). No se usó `origin/main...HEAD` como fuente principal.
- No se ejecutaron pruebas por instrucción explícita.

## 1. Inventario de archivos cambiados/nuevos

### Dominio

- Excepciones nuevas: `AccionExpiredException`, `AccionNotFoundException`, `AccionNotOwnedByActorException`, `AccionStateException`, `AccionStateTransitionException`, `AccionVersionMismatchException`, `ConversacionAsistenteNotOwnedByActorException`, `TenantScopeViolationException`.
- Entidades IA nuevas: `model/entity/ia/AiAccion`, `AiConversacion`, `AiMemoria`, `AiMensaje`, `AiResumenContexto`.
- Enums nuevos: `EstadoAccion`, `OrigenMemoria`, `RolMensajeAi`, `TipoAccion`, `VisibilidadMemoria`.
- Política nueva: `EmpresaPermitidaPolicy`.
- Value objects nuevos: `AiAccionId`, `AiConversacionId`, `AiMemoriaId`, `AiMensajeId`, `AiResumenContextoId`.
- Tests nuevos: entidades IA, políticas de propiedad, memoria, resumen y `EmpresaPermitidaPolicy`.

### Aplicación: `application/ai`

- Commands nuevos/cambiados: `AnalizarChatCommand`, `ConfirmarAccionCommand`, `ExpirarAccionCommand`, `ListarAccionesPendientesCommand`, `ListarConversacionesAsistenteCommand`, `ObtenerAccionCommand`, `ObtenerConversacionAsistenteCommand`, `ProponerAccionCommand`, `RechazarAccionCommand`, `RegistrarAccionCommand`, `RegistrarMensajeAsistenteCommand`.
- Excepciones nuevas/cambiadas: `AccionInvalidaException`, `AiAssistantException`, `AsistenteTenantException`, `ConversacionAsistenteNoEncontradaException`.
- Ports-in nuevos: `AnalizarChatUseCase`, `ConfirmarAccionUseCase`, `ExpirarAccionUseCase`, `ListarAccionesPendientesUseCase`, `ListarConversacionesAsistenteUseCase`, `ObtenerAccionUseCase`, `ObtenerConversacionAsistenteUseCase`, `ProponerAccionUseCase`, `RechazarAccionUseCase`, `RegistrarAccionUseCase`, `RegistrarMensajeAsistenteUseCase`.
- Results nuevos: `ProponerAccionResponse`, `ResultadoAnalisisChat`, `ResultadoEjecucionAccion`.
- Ports-out nuevos: `AiToolContextPort`, `ColumnaLecturaPort`, `ContactoLecturaPort`, `DeleteAiMemoriaPort`, `FichaLecturaPort`, `FindAiAccionPort`, `FindAiConversacionPort`, `FindAiMemoriaPort`, `FindAiMensajesByConversacionPort`, `FindAiResumenPort`, `GenerarChatAsistentePort`, `GenerarEmbeddingPort`, `ListAiConversacionesPort`, `ListPendingAiAccionesPort`, `SaveAiAccionPort`, `SaveAiConversacionPort`, `SaveAiMemoriaPort`, `SaveAiMensajePort`, `SaveAiResumenPort`, `UpdateEstadoAccionPort`, `WhatsappConversacionLecturaPort`, `WhatsappMensajeLecturaPort`.
- DTOs/proyecciones de ports-out nuevos: `AiToolContext`, `ChatAsistenteRequest`, `MensajeChat`, `RespuestaAsistente`, `WhatsappConversacionResumen`, `WhatsappMensajeResumen`.
- Services/helpers nuevos/cambiados: `AnalizarChatService`, `ConfirmarAccionMapper`, `ConfirmarAccionService`, `ExpirarAccionService`, `ListarAccionesPendientesService`, `ListarConversacionesAsistenteService`, `ObtenerAccionService`, `ObtenerConversacionAsistenteService`, `ProponerAccionService`, `RechazarAccionService`, `RegistrarAccionService`, `RegistrarMensajeAsistenteService`, `SaveAiAccionPortBridge`, `AiTenantExceptionTranslator`.
- Tests nuevos/cambiados: commands estrictos, servicios IA, mapper de confirmación, helper de stubs.

### Aplicación: `empresa` y seguridad

- Empresa: `ActorEmpresaScopePort` nuevo, `FindEmpresasByCreadorPort` nuevo, `ActorEmpresaScopeService` nuevo.
- Seguridad: `ActorContext` modificado para quedar como identidad autenticada sin `empresaId`/tenant scope.
- Tests nuevos: `ActorEmpresaScopeServiceTest`.

## 2. Contratos de producción por clase

### Dominio: entidades, estado e invariantes

| Clase | API/fábricas/transiciones | Invariantes y responsabilidad |
|---|---|---|
| `AiAccion` | `crear(...)`, `reconstitute(...)`, `confirmar`, `rechazar`, `expirar`, `marcarEjecutada`, `marcarFallida`, `estaExpirada`, `perteneceA(UsuarioId/EmpresaId)`, `esTerminal`, `requireOwnedBy`, `requirePending`, `requireVersion`, `requireNotExpired`, `requireConfirmable` | Agregado de propuesta IA. Crea siempre `PENDING`, `version=1`, `expiresAt=ahora+ttl`. Transiciones: `PENDING -> CONFIRMED/REJECTED/EXPIRED`, `CONFIRMED -> EXECUTED/FAILED`; terminales: `REJECTED`, `EXPIRED`, `EXECUTED`, `FAILED`. No interpreta `payloadJson`; valida actor, empresa, estado, versión y expiración. |
| `AiConversacion` | `crear(...)`, `reconstitute(...)`, `archivar`, `perteneceA(UsuarioId/EmpresaId)`, `scopeEs`, `requireOwnedBy` | Conversación IA por actor, empresa, WhatsApp conversation y contacto opcional. Arranca no archivada; `archivar` es idempotente. Autoriza lectura/seguimiento por actor iniciador y empresa. |
| `AiMemoria` | `crear(...)`, `reconstitute(...)`, `supersede`, `expirar`, `perteneceA`, `estaExpirada`, `estaViva` | Memoria privada por actor+empresa, scoped a conversación WhatsApp o contacto, nunca global. `CONVERSACION_SCOPED` exige `waConversacionId` y prohíbe `contactoId`; `CONTACTO_SCOPED` exige `contactoId` y prohíbe `waConversacionId`. Supersede/expire son idempotentes. |
| `AiMensaje` | `crear(...)`, `reconstitute(...)`, `perteneceA(AiConversacionId)` | Turno IA con rol, contenido, metadatos de modelo/tokens/latencia y `toolCallJson` opaco. Rechaza contenido vacío y métricas negativas. No reemplaza el transcript de WhatsApp. |
| `AiResumenContexto` | `crear(...)`, `reconstitute(...)`, `reemplazarCon`, `perteneceA`, `esStale` | Resumen persistente de contexto por actor+empresa+WA conversation. `sourceWatermark` no puede retroceder; el transcript WhatsApp sigue siendo fuente autoritativa. |
| `EstadoAccion` | enum `PENDING`, `CONFIRMED`, `REJECTED`, `EXPIRED`, `EXECUTED`, `FAILED` | Define la máquina de estados de `AiAccion`. Agrega estados post-ejecución además de los cuatro estados mínimos del spec. |
| `TipoAccion` | enum `CREATE_CONTACTO`, `CREATE_TRATO`, `CREATE_TAREA`, `MOVE_KANBAN_FICHA` | Discriminador de payload IA; la validación del esquema vive en aplicación. |
| `RolMensajeAi` | enum `USER`, `ASSISTANT`, `SYSTEM`, `TOOL` | Rol de mensaje usado por historial/modelo/UI. |
| `OrigenMemoria` | enum `CHAT_ANALYSIS`, `FOLLOW_UP`, `MANUAL`, `PROPOSAL` | Procedencia de una memoria para ponderación/provenance. |
| `VisibilidadMemoria` | enum `CONVERSACION_SCOPED`, `CONTACTO_SCOPED` | Determina qué scope de memoria es obligatorio. |
| `EmpresaPermitidaPolicy` | `seleccionarEmpresaPermitida(List<EmpresaId>, UUID requestedId)` | Valida tenant explícito contra empresas del actor; si `requestedId == null`, retorna la primera empresa propia. |
| `AiAccionId`, `AiConversacionId`, `AiMemoriaId`, `AiMensajeId`, `AiResumenContextoId` | records con `from(UUID)` y `create()` | VOs de identidad; rechazan UUID nulo y encapsulan creación aleatoria. |

### Dominio: excepciones

| Clase | Factories/API | Uso esperado |
|---|---|---|
| `AccionExpiredException` | `expired(accionId, expiresAt)` | Rechaza operar una acción expirada; conflicto. |
| `AccionNotFoundException` | `forId(UUID)` | Acción IA inexistente; not found. |
| `AccionNotOwnedByActorException` | `notRequester(actorUsuarioId, accionId)` | Actor no solicitante o tenant incorrecto en `AiAccion.requireOwnedBy`. |
| `AccionStateException` | `invalidState(accionId, operacion, estadoActual)` | Estado incompatible en guards de aplicación/dominio. |
| `AccionStateTransitionException` | `transicionNoPermitida`, `estadoTerminal` | Transición inválida de la máquina de estados. |
| `AccionVersionMismatchException` | `mismatch(accionId, expected, actual)` | Optimistic-lock mismatch. |
| `ConversacionAsistenteNotOwnedByActorException` | `notOwner`, `tenantMismatch` | Actor/tenant no autorizado para conversación IA. |
| `TenantScopeViolationException` | constructor público | Rechazo neutral de tenant desde política/Empresa. |

### Aplicación: commands y results

| Clase | API/invariantes |
|---|---|
| `AnalizarChatCommand` | Record. Requiere `actorUsuarioId` y `waConversacionId`; `empresaId` es solo hint y no autoridad. |
| `ConfirmarAccionCommand` | Record. Requiere actor, acción, `expectedVersion > 0` y `empresaId` obligatorio como cross-check estricto. |
| `ExpirarAccionCommand` | Record. Requiere `ahora`; `maxPorLote` entre 1 y 1000. |
| `ListarAccionesPendientesCommand` | Record. Requiere actor, `empresaId` y `limite` 1..200; bloquea auto-resolución del selector. |
| `ListarConversacionesAsistenteCommand` | Record. Requiere actor y `limite` 1..200; `empresaId` opcional. |
| `ObtenerAccionCommand` | Record. Requiere actor y acción; `empresaId` opcional. |
| `ObtenerConversacionAsistenteCommand` | Record. Requiere actor, conversación IA y `empresaId` obligatorio como cross-check estricto. |
| `ProponerAccionCommand` | Record. Requiere `TipoAccion`, payload JSON, rationale y TTL positivo. No trae actor/tenant: salen del contexto confiable. |
| `RechazarAccionCommand` | Record. Requiere actor, acción y `empresaId` obligatorio como cross-check estricto. |
| `RegistrarAccionCommand` | Record. Requiere actor, empresa, conversación IA, WA conversation, tipo, payload, rationale y TTL positivo. Normaliza `waMensajeId` blank a `null`. Factory `conTipoAccion(...)`. |
| `RegistrarMensajeAsistenteCommand` | Record. Requiere actor, conversación IA y mensaje; `empresaId` opcional. |
| `ProponerAccionResponse` | Record con `accionId` y `estado`, usado por tool propose-only. |
| `ResultadoAnalisisChat` | Record con `aiConversacionId`, `contenidoAsistente`, `modelo`; normaliza contenido nulo a `""`. |
| `ResultadoEjecucionAccion` | Record con estado, resultado/error y nueva versión. Factories `ejecutada` y `fallida`. |

### Aplicación: excepciones

| Clase | API/factories | Responsabilidad |
|---|---|---|
| `AccionInvalidaException` | `forInvalidInput`, `forTenantMismatch` | Errores de payload/comando para staging o dispatch. |
| `AiAssistantException` | `upstreamFailure`, `invalidAssistantOutput` | Fallas del proveedor IA o salida inválida. |
| `AsistenteTenantException` | `chatNoPerteneceAlActor`, `empresaNoEncontradaParaActor`, `accionNoPerteneceALaEmpresaSeleccionada`, `conversacionNoPerteneceALaEmpresaSeleccionada`, `empresaNoPoseidaPorActor`, `tenantSelectorRechazado`, `from(...)` | Excepción pública de tenant del bounded context IA; traduce `TenantScopeViolationException` y separa cross-check de selector. |
| `ConversacionAsistenteNoEncontradaException` | `forId(String)` | Conversación IA inexistente/inaccesible. |

### Aplicación: ports-in

| Port | Método | Responsabilidad |
|---|---|---|
| `AnalizarChatUseCase` | `analizar(AnalizarChatCommand)` | Analizar chat WhatsApp con IA, persistir turnos/resumen. |
| `ConfirmarAccionUseCase` | `confirmar(ConfirmarAccionCommand)` | Único camino IA que ejecuta mutaciones CRM reales. |
| `ExpirarAccionUseCase` | `expirar(ExpirarAccionCommand)` | Expirar propuestas vencidas. |
| `ListarAccionesPendientesUseCase` | `listar(ListarAccionesPendientesCommand)` | Inbox de propuestas `PENDING` por actor+tenant explícito. |
| `ListarConversacionesAsistenteUseCase` | `listar(ListarConversacionesAsistenteCommand)` | Listar conversaciones IA del actor. |
| `ObtenerAccionUseCase` | `obtener(ObtenerAccionCommand)` | Obtener acción IA por id, scoped al actor/tenant. |
| `ObtenerConversacionAsistenteUseCase` | `obtener(ObtenerConversacionAsistenteCommand)` | Obtener conversación IA con historial; result interno `ResultadoConversacionAsistente`. |
| `ProponerAccionUseCase` | `proponer(ProponerAccionCommand)` | Staging propose-only desde herramienta IA; no muta CRM. |
| `RechazarAccionUseCase` | `rechazar(RechazarAccionCommand)` | Rechazar propuesta sin tocar CRM. |
| `RegistrarAccionUseCase` | `registrar(RegistrarAccionCommand)` | Persistir `AiAccion(PENDING)`. |
| `RegistrarMensajeAsistenteUseCase` | `registrar(RegistrarMensajeAsistenteCommand)` | Follow-up de conversación IA. |

### Aplicación: ports-out, DTOs y proyecciones

| Clase | API/invariantes | Responsabilidad |
|---|---|---|
| `AiToolContextPort` | `resolve()` | Obtiene contexto confiable actor/tenant/conversación para tools; debe fallar si falta contexto. |
| `ColumnaLecturaPort` | `findById`, `findByTipoTablero` | Lectura de columnas para herramientas/propuestas Kanban. |
| `ContactoLecturaPort` | `findById`, `findByEmpresaIdAndTelefono` | Lectura de contactos bajo tenant confiable. |
| `DeleteAiMemoriaPort` | `delete(AiMemoria)` | Borrado/forget de memoria IA. |
| `FichaLecturaPort` | `findById` | Lectura de ficha. |
| `FindAiAccionPort` | `findById(UUID)` | Lookup de acción IA. |
| `FindAiConversacionPort` | `findById(UUID)` | Lookup de conversación IA. |
| `FindAiMemoriaPort` | `findActivasByConversacionId(UUID waConversacionId, UUID actorUsuarioId, UUID empresaId)` | Carga memorias activas scoped a WA conversation/actor/tenant. |
| `FindAiMensajesByConversacionPort` | `findByConversacionId(UUID)` | Carga historial IA ordenado por conversación. |
| `FindAiResumenPort` | `findByConversacionId(UUID)` | Carga resumen vigente. |
| `GenerarChatAsistentePort` | `generar(ChatAsistenteRequest)` | Frontera hacia proveedor IA; no filtra tipos Spring AI a aplicación. |
| `GenerarEmbeddingPort` | `embed(String)` | Contrato futuro para embeddings; fase 1 puede usar placeholder. |
| `ListAiConversacionesPort` | `listByActor(UUID actor, UUID empresa, int limite)` | Listado de conversaciones por actor+tenant. |
| `ListPendingAiAccionesPort` | `listPendingByActor(UUID actor, UUID empresa, int limite)` | Listado PENDING filtrado por actor+tenant. |
| `SaveAiAccionPort` | `save(AiAccion)` | Persistencia de acciones IA. |
| `SaveAiConversacionPort` | `save(AiConversacion)`, `findById(UUID)` | Persistencia y lookup directo de conversación. |
| `SaveAiMemoriaPort` | `save(AiMemoria)` | Persistencia de memoria IA. |
| `SaveAiMensajePort` | `save(AiMensaje)` | Persistencia de mensaje IA. |
| `SaveAiResumenPort` | `save(AiResumenContexto)` | Persistencia de resumen. |
| `UpdateEstadoAccionPort` | `findPendingExpired(int)`, `save(AiAccion)` | Sweep de expiración. |
| `WhatsappConversacionLecturaPort` | `findById(UUID)` | Lectura de conversación WhatsApp como proyección application-owned. |
| `WhatsappMensajeLecturaPort` | `findByConversacionId(UUID)` | Lectura de transcript WhatsApp persistido. |
| `AiToolContext` | Record; requiere actor, empresa, conversación IA y WA conversation | DTO de contexto confiable para tool calls. |
| `ChatAsistenteRequest` | Record; requiere ids principales; copia listas defensivamente | Request application-owned para generación IA con historial, memoria, transcript y resumen. |
| `MensajeChat` | Record; requiere rol y contenido | Mensaje del port de generación IA. |
| `RespuestaAsistente` | Record; normaliza contenido nulo | Respuesta del proveedor IA sin acciones; el staging ocurre por tool. |
| `WhatsappConversacionResumen` | Record; requiere WA conversation, canal y `canalEmpresaId` | Proyección que trae el tenant autoritativo del recurso WhatsApp. |
| `WhatsappMensajeResumen` | Record; requiere mensaje, conversación, dirección y tipo | Proyección de transcript WhatsApp. |

### Aplicación: services y helpers

| Clase | Método/funciones clave | Orquestación |
|---|---|---|
| `AnalizarChatService` | `analizar` | Carga conversación WhatsApp primero, deriva tenant desde `canalEmpresaId`, valida empresas del actor, crea/reusa `AiConversacion` determinística por actor+WA conversation, carga transcript/historial/memoria/resumen, llama `GenerarChatAsistentePort`, persiste turnos y refresca resumen. No stagea acciones. |
| `ConfirmarAccionMapper` | `toCreateContacto`, `toCreateTrato`, `toCreateTarea`, `toMoverFicha`, helpers de extracción | Mapea `AiAccion.payloadJson` a comandos CRM existentes. Mantiene parsing JSON plano dentro de aplicación sin Jackson. |
| `ConfirmarAccionService` | `confirmar` | Carga acción, cross-check obligatorio `command.empresaId == accion.empresaId`, valida actor dueño del tenant, aplica guards del agregado, confirma, despacha a `CreateContacto/Trato/Tarea` o `MoverColumnaFicha`, guarda `EXECUTED` o `FAILED`; si expirada, persiste `EXPIRED` y relanza. |
| `ExpirarAccionService` | `expirar` | Busca pendientes vencidas y aplica `AiAccion.expirar`; ignora transiciones inválidas ya terminales. |
| `ListarAccionesPendientesService` | `listar` | Resuelve selector explícito con `resolveForSelector`, lista propuestas `PENDING` por actor+empresa+limite. |
| `ListarConversacionesAsistenteService` | `listar` | Resuelve tenant con `resolve` y delega a `ListAiConversacionesPort`. |
| `ObtenerAccionService` | `obtener` | Resuelve tenant, carga acción y delega ownership a `AiAccion.requireOwnedBy`. |
| `ObtenerConversacionAsistenteService` | `obtener` | Carga conversación IA primero, cross-check obligatorio con `empresaId`, valida actor dueño del tenant, aplica `requireOwnedBy` y carga mensajes. |
| `ProponerAccionService` | `proponer` | Resuelve contexto confiable por `AiToolContextPort`, arma `RegistrarAccionCommand`, delega staging y devuelve id/estado. No depende de mutaciones CRM. |
| `RechazarAccionService` | `rechazar` | Carga acción, cross-check obligatorio con `empresaId`, valida actor dueño del tenant, exige `PENDING` y persiste `REJECTED`. |
| `RegistrarAccionService` | `registrar` | Construye `AiAccion.crear` y guarda `PENDING`; no ejecuta mutaciones. |
| `RegistrarMensajeAsistenteService` | `registrar` | Resuelve tenant, carga conversación IA, valida ownership, arma request con transcript/historial/memoria/resumen, llama IA, persiste turno usuario+asistente y refresca resumen si existe. |
| `SaveAiAccionPortBridge` | `save` | Wrapper fino sobre `SaveAiAccionPort`; sin lógica de negocio. |
| `AiTenantExceptionTranslator` | `resolve`, `resolveForSelector`, `assertActorOwnsTenant` | Helper package-private que traduce `TenantScopeViolationException` del puerto Empresa a `AsistenteTenantException`. |

### Empresa y seguridad

| Clase | API/invariantes | Responsabilidad |
|---|---|---|
| `ActorEmpresaScopePort` | `resolver(UUID actor, UUID empresaId)`, `empresasDelActor(UUID actor)` | Puerto inbound neutral para validar/obtener empresas del actor; expone excepción neutral de dominio. |
| `FindEmpresasByCreadorPort` | `findEmpresasByCreador(UsuarioId)` | Puerto outbound granular para cargar empresas donde `Empresa.creadoPor == actor`. |
| `ActorEmpresaScopeService` | `resolver`, `empresasDelActor` | Implementa el puerto Empresa; coordina `FindEmpresasByCreadorPort` + `EmpresaPermitidaPolicy`. |
| `ActorContext` | `hasRole`, `isSuperUsuario` | Record de identidad autenticada: subject, username, email, `usuarioId`, `superUsuarioId`, roles. No contiene tenant scope. |

## 3. Cómo encajan las piezas

1. `ActorContext` solo identifica al actor autenticado. La autoridad de tenant no viaja en el actor; se deriva del recurso o de un selector explícito.
2. `AnalizarChatService` usa `WhatsappConversacionLecturaPort` para obtener `canalEmpresaId`; ese tenant del recurso se vuelve autoritativo para `AiConversacion`, `AiMensaje`, `AiResumenContexto`, memoria y tools.
3. `ActorEmpresaScopePort` es el punto común para validar que el actor posee una empresa. IA no depende de `ActorEmpresaScopeService` directamente; depende del puerto y traduce errores con `AiTenantExceptionTranslator` cuando necesita excepción pública IA.
4. Las entidades de dominio encapsulan invariantes: `AiAccion` controla la máquina de estados y guards; `AiConversacion`/`AiMemoria`/`AiResumenContexto` controlan ownership, scope y freshness; los VOs encapsulan identidad.
5. Los commands de aplicación fijan invariantes de frontera: campos requeridos, límites de paginación/TTL y `empresaId` obligatorio donde el flujo PR6/PR7 exige cross-check/selector.
6. Los ports-in exponen casos de uso; los services coordinan puertos y delegan decisiones de negocio a dominio. La capa de aplicación no importa Spring AI, JPA ni WhatsApp domain.
7. Los ports-out aíslan persistencia, proveedor IA, lectura WhatsApp y tool context. Los DTOs/proyecciones son application-owned para evitar fugas de infraestructura/framework.
8. La ejecución de mutaciones CRM queda concentrada en `ConfirmarAccionService`; `ProponerAccionService` y `RegistrarAccionService` solo stagean `AiAccion(PENDING)`.

## 4. Resumen de cobertura de tests observada

- Dominio:
  - Creación, reconstitución, equality por id e invariantes de `AiConversacion`, `AiMensaje`, `AiMemoria`, `AiResumenContexto`.
  - Máquina de estados de `AiAccion`: confirmación, rechazo, expiración idempotente, ejecución/falla post-confirmación y guards de estado/versión/expiración/ownership.
  - `EmpresaPermitidaPolicy`: selector explícito válido/inválido, actor sin empresas y fallback a primera empresa cuando no hay selector.
- Aplicación IA:
  - Commands con `empresaId` obligatorio para confirmar/rechazar/obtener conversación/listar acciones, límites de paginación y validación de actor/id nulos.
  - `AnalizarChatService`: rechazo de chat ajeno sin persistir, persistencia de respuesta, envío de transcript al port IA, id determinístico y tenant resource-first en actores multiempresa.
  - `ConfirmarAccionService`: rechazo por actor, tenant, estado, inexistencia, versión y expiración; dispatch a `CreateContacto`, `CreateTrato`, `CreateTarea`, `MoverColumnaFicha`; failure marca `FAILED`.
  - `RechazarAccionService` y `ObtenerConversacionAsistenteService`: cross-check de empresa seleccionada contra recurso y validación de actor dueño del tenant.
  - `ListarAccionesPendientesService`: selector obligatorio, no auto-resolución multiempresa, actor sin empresa/no dueño y delegación a port filtrado.
  - `ProponerAccionService`: usa contexto confiable, stagea proposal y no inyecta mutaciones reales.
  - `RegistrarAccionService`, `RegistrarMensajeAsistenteService`, `ExpirarAccionService`, `ListarConversacionesAsistenteService`, `ObtenerAccionService` cubren flujos happy path y rechazos principales.
  - `ConfirmarAccionMapperTest`: parsing de payloads hacia comandos CRM y errores de campos faltantes/UUID inválido.
- Empresa:
  - `ActorEmpresaScopeService`: resolución explícita, fallback sin selector, actor sin empresas, actor nulo y lista completa.

## 5. Riesgos y brechas del slice

1. **Semántica de tenant aún mixta en algunos flujos.** `ConfirmarAccionService`, `RechazarAccionService` y `ObtenerConversacionAsistenteService` ya siguen resource-first con cross-check obligatorio. En cambio, `RegistrarMensajeAsistenteService`, `ObtenerAccionService` y `ListarConversacionesAsistenteService` resuelven tenant antes o aparte del recurso usando `ActorEmpresaScopePort.resolve`, que conserva fallback a primera empresa cuando `empresaId` es `null`. Esto puede chocar con el modelo resource-first para actores multiempresa si esos endpoints permiten omitir `empresaId`.
2. **Fallback de `EmpresaPermitidaPolicy` sigue vivo.** La política y el servicio Empresa documentan y prueban `requestedId == null -> primera empresa`. Esto está protegido por tests, pero debe quedar acotado a flujos donde sea intencional; PR7 exige explícitamente no auto-resolver para `GET /api/ai/acciones`.
3. **Estados de `AiAccion` exceden el spec de acción.** El spec enumera `PENDING`, `CONFIRMED`, `REJECTED`, `EXPIRED`; el dominio agrega `EXECUTED` y `FAILED`, y `ConfirmarAccionService` persiste directamente `EXECUTED/FAILED` después del dispatch. Conviene alinear spec/terminología antes de archivar.
4. **Parsing JSON en `ConfirmarAccionMapper` es frágil.** La extracción por substring funciona para JSON plano controlado, pero no soporta escapes, objetos anidados ni casos complejos. Si los payloads provienen de modelo/tool, el contrato debe quedar muy restringido o moverse a un mapper robusto en adapter.
5. **Contrato de memoria necesita precisión.** `FindAiMemoriaPort.findActivasByConversacionId(UUID waConversacionId, ...)` usa un nombre de conversación genérico pero el parámetro representa WhatsApp conversation id; la entidad guarda `waConversacionId` como `String`. Verificar que los adapters mantengan el mismo significado.
6. **Solo inspección estática.** No se ejecutaron tests ni compilación; este reporte no prueba que el slice compile ni que la wiring/infrastructure coincida con estos contratos.

## 6. Siguiente recomendado

- Antes de verificación completa, revisar los tres flujos con `empresaId` opcional (`RegistrarMensajeAsistenteService`, `ObtenerAccionService`, `ListarConversacionesAsistenteService`) y decidir si deben ser resource-first/selector explícito o si el fallback a primera empresa es intencional y documentado.
- Alinear `ai-action-proposal/spec.md` con `EXECUTED/FAILED` o ajustar el dominio/servicio al lenguaje de `CONFIRMED` si esos estados no deben formar parte del contrato público.
- Ejecutar verificación enfocada del módulo `domain,application` cuando el orquestador autorice pruebas.
