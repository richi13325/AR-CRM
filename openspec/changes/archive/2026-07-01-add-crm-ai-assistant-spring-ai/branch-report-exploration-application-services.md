# Exploración de rama — Servicios de aplicación AI

## Alcance y fuente

- Proyecto: `ar-crm` / `CRM2`.
- Cambio OpenSpec: `openspec/changes/add-crm-ai-assistant-spring-ai/`.
- Alcance inspeccionado: `application/src/main/java/com/ar/crm2/application/ai/service/**` y resumen de cobertura en `application/src/test/java/com/ar/crm2/application/ai/service/**`.
- Fuente de verdad utilizada: `git status --short`, `git diff HEAD` y archivos no versionados del working tree. No se utilizó `origin/main...HEAD` como fuente principal.
- No se ejecutaron tests por instrucción explícita.

## Inventario de archivos cambiados/nuevos

### Análisis de chat

- `application/src/main/java/com/ar/crm2/application/ai/service/AnalizarChatService.java` — nuevo servicio de análisis inicial sobre conversación WhatsApp.

### Propuesta, registro, confirmación, rechazo, expiración, listado y consulta de acciones

- `application/src/main/java/com/ar/crm2/application/ai/service/ProponerAccionService.java` — nuevo coordinador de propuestas originadas por tool calls de Spring AI.
- `application/src/main/java/com/ar/crm2/application/ai/service/RegistrarAccionService.java` — nuevo servicio de staging de acciones AI en estado `PENDING`.
- `application/src/main/java/com/ar/crm2/application/ai/service/ConfirmarAccionService.java` — nuevo servicio de confirmación y ejecución de acciones AI.
- `application/src/main/java/com/ar/crm2/application/ai/service/RechazarAccionService.java` — nuevo servicio de rechazo de acciones AI.
- `application/src/main/java/com/ar/crm2/application/ai/service/ExpirarAccionService.java` — nuevo servicio batch para expirar propuestas vencidas.
- `application/src/main/java/com/ar/crm2/application/ai/service/ListarAccionesPendientesService.java` — archivo no versionado; lista propuestas `PENDING` del actor por empresa explícita.
- `application/src/main/java/com/ar/crm2/application/ai/service/ObtenerAccionService.java` — nuevo servicio de consulta de una acción AI.

### Listado y consulta de conversaciones

- `application/src/main/java/com/ar/crm2/application/ai/service/ListarConversacionesAsistenteService.java` — nuevo servicio de listado de conversaciones AI del actor.
- `application/src/main/java/com/ar/crm2/application/ai/service/ObtenerConversacionAsistenteService.java` — nuevo servicio de consulta de conversación AI con mensajes.

### Registro de mensajes

- `application/src/main/java/com/ar/crm2/application/ai/service/RegistrarMensajeAsistenteService.java` — nuevo servicio de turno follow-up usuario/asistente.

### Mappers, bridges y traductores

- `application/src/main/java/com/ar/crm2/application/ai/service/ConfirmarAccionMapper.java` — mapper JSON plano → comandos CRM reales.
- `application/src/main/java/com/ar/crm2/application/ai/service/SaveAiAccionPortBridge.java` — bridge del puerto de guardado de acciones.
- `application/src/main/java/com/ar/crm2/application/ai/service/AiTenantExceptionTranslator.java` — archivo no versionado; helper package-private para traducir excepciones de tenant del puerto Empresa a excepciones públicas AI.

## Detalle por clase de producción

### `AnalizarChatService`

- Método clave: `analizar(AnalizarChatCommand)`.
- Puertos usados: `ActorEmpresaScopePort`, `WhatsappConversacionLecturaPort`, `WhatsappMensajeLecturaPort`, `FindAiConversacionPort`, `FindAiResumenPort`, `FindAiMemoriaPort`, `FindAiMensajesByConversacionPort`, `SaveAiConversacionPort`, `SaveAiMensajePort`, `SaveAiResumenPort`, `GenerarChatAsistentePort`.
- Flujo: carga primero la conversación WhatsApp, deriva el tenant desde `canalEmpresaId`, valida que el actor posea ese tenant, calcula un `AiConversacionId` determinístico por `(actor, waConversacion)`, busca o crea la conversación AI, carga transcript WhatsApp, historial AI, memoria y resumen, llama al puerto de generación, persiste turno de usuario opcional, turno de asistente y refresca resumen.
- Seguridad/tenant: patrón resource-first; `empresaId` del comando es solo hint/cross-check y no puede sobrescribir el tenant del recurso. Rechaza con `AsistenteTenantException` antes de crear filas `ai_*` si la conversación WhatsApp no pertenece al actor.
- Llamadas de dominio: `AiConversacion.reconstitute`, `AiMensaje.crear`, `AiResumenContexto.crear`, `AiResumenContexto.reemplazarCon`, VOs `EmpresaId`, `UsuarioId`, `AiConversacionId`.
- Persistencia/interacciones: lectura WhatsApp y AI; guardado de conversación AI, mensajes AI y resumen AI; generación externa vía `GenerarChatAsistentePort`.

### `ProponerAccionService`

- Método clave: `proponer(ProponerAccionCommand)`.
- Puertos/colaboradores usados: `AiToolContextPort`, `RegistrarAccionUseCase`.
- Flujo: resuelve contexto confiable del tool call, construye `RegistrarAccionCommand` con actor, empresa, conversación AI y conversación WhatsApp del contexto, delega el staging en `RegistrarAccionUseCase` y retorna `ProponerAccionResponse` con id/estado.
- Seguridad/tenant: el modelo no provee actor, tenant ni conversación; esos datos salen del contexto confiable. No inyecta ni invoca use cases reales de mutación CRM.
- Llamadas de dominio: no crea entidades directamente; proyecta el `AiAccion` retornado por staging.
- Persistencia/interacciones: persistencia indirecta mediante `RegistrarAccionUseCase`.

### `RegistrarAccionService`

- Método clave: `registrar(RegistrarAccionCommand)`.
- Puertos usados: `SaveAiAccionPort`.
- Flujo: crea una propuesta con `AiAccion.crear(...)` usando el scope confiable recibido y guarda la entidad.
- Seguridad/tenant: no ejecuta mutaciones CRM; solo registra propuestas `PENDING`. Depende de que el comando provenga de un flujo con contexto confiable.
- Llamadas de dominio: `AiAccion.crear`, `EmpresaId.from`, `UsuarioId.from`, `AiConversacionId.from`.
- Persistencia/interacciones: escritura única mediante `SaveAiAccionPort.save`.

### `ConfirmarAccionService`

- Método clave: `confirmar(ConfirmarAccionCommand)`.
- Puertos/colaboradores usados: `ActorEmpresaScopePort`, `FindAiAccionPort`, `SaveAiAccionPortBridge`, `CreateContactoUseCase`, `CreateTratoUseCase`, `CreateTareaUseCase`, `MoverColumnaFichaUseCase`.
- Flujo: carga la acción por id, deriva el tenant del recurso, compara estrictamente contra `command.empresaId()`, valida que el actor posea el tenant, delega ownership/version/estado/expiración al agregado, confirma, despacha la mutación CRM según `TipoAccion`, marca `EXECUTED` con id resultante o `FAILED` con razón truncada.
- Seguridad/tenant: patrón resource-first con cross-check obligatorio de empresa seleccionada; valida ownership del actor vía Empresa-owned port y luego `AiAccion.requireOwnedBy`.
- Llamadas de dominio: `requireOwnedBy`, `requireConfirmable`, `expirar`, `confirmar`, `marcarEjecutada`, `marcarFallida`; `TipoAccion.valueOf` para dispatch.
- Persistencia/interacciones: lectura por `FindAiAccionPort`; guardado de transiciones `EXPIRED`, `EXECUTED` o `FAILED` vía bridge; ejecución real solo por los cuatro use cases CRM permitidos.

### `RechazarAccionService`

- Método clave: `rechazar(RechazarAccionCommand)`.
- Puertos/colaboradores usados: `ActorEmpresaScopePort`, `FindAiAccionPort`, `SaveAiAccionPortBridge`.
- Flujo: carga la acción, deriva tenant del recurso, valida cross-check de `empresaId`, valida ownership del actor, exige ownership y estado `PENDING`, guarda la acción rechazada.
- Seguridad/tenant: resource-first; rechazo controlado si la empresa seleccionada no coincide con el recurso o si el actor no posee el tenant.
- Llamadas de dominio: `requireOwnedBy`, `requirePending`, `rechazar`.
- Persistencia/interacciones: lectura de acción y escritura de transición a `REJECTED`; no toca CRM.

### `ExpirarAccionService`

- Método clave: `expirar(ExpirarAccionCommand)`.
- Puertos usados: `UpdateEstadoAccionPort`.
- Flujo: obtiene propuestas `PENDING` vencidas por lote, intenta `expirar` cada una, guarda las expiradas y cuenta transiciones exitosas; omite transiciones inválidas ya terminales.
- Seguridad/tenant: no opera por actor/tenant; es un flujo batch de mantenimiento limitado al puerto que filtra propuestas vencidas.
- Llamadas de dominio: `AiAccion.expirar`.
- Persistencia/interacciones: `findPendingExpired(maxPorLote)` y `save(expirada)`.

### `ListarAccionesPendientesService`

- Método clave: `listar(ListarAccionesPendientesCommand)`.
- Puertos usados: `ActorEmpresaScopePort`, `ListPendingAiAccionesPort`.
- Flujo: resuelve la empresa seleccionada con `resolveForSelector`, luego lista acciones pendientes por `(actor, empresa, límite)`.
- Seguridad/tenant: `empresaId` es requerido por contrato PR7; no auto-resuelve empresas únicas ni multiempresa. Si el actor no posee la empresa seleccionada, traduce a `AsistenteTenantException`.
- Llamadas de dominio: no modifica agregados; retorna `AiAccion` leídos por puerto.
- Persistencia/interacciones: lectura por `ListPendingAiAccionesPort`; el filtrado `PENDING` queda en la query del puerto.

### `ObtenerAccionService`

- Método clave: `obtener(ObtenerAccionCommand)`.
- Puertos usados: `ActorEmpresaScopePort`, `FindAiAccionPort`.
- Flujo: resuelve tenant para el actor, carga la acción y aplica ownership del agregado antes de retornar.
- Seguridad/tenant: depende del puerto Empresa para resolver scope y de `AiAccion.requireOwnedBy` para actor/tenant del recurso.
- Llamadas de dominio: `requireOwnedBy`, `UsuarioId.from`.
- Persistencia/interacciones: lectura por `FindAiAccionPort`; sin escritura.

### `ListarConversacionesAsistenteService`

- Método clave: `listar(ListarConversacionesAsistenteCommand)`.
- Puertos usados: `ActorEmpresaScopePort`, `ListAiConversacionesPort`.
- Flujo: resuelve tenant para el actor y delega el listado por actor/empresa/límite.
- Seguridad/tenant: traduce violaciones de scope vía `AiTenantExceptionTranslator.resolve`.
- Llamadas de dominio: no invoca comportamiento de agregados; retorna `AiConversacion` leídas.
- Persistencia/interacciones: lectura por `ListAiConversacionesPort`.

### `ObtenerConversacionAsistenteService`

- Método clave: `obtener(ObtenerConversacionAsistenteCommand)`.
- Puertos usados: `ActorEmpresaScopePort`, `FindAiConversacionPort`, `FindAiMensajesByConversacionPort`.
- Flujo: carga la conversación AI por id, deriva tenant del recurso, compara contra `command.empresaId()`, valida ownership del actor sobre el tenant del recurso, exige ownership del agregado, carga mensajes y retorna conversación + historial.
- Seguridad/tenant: resource-first con cross-check obligatorio de empresa seleccionada y validación por Empresa-owned port.
- Llamadas de dominio: `AiConversacion.requireOwnedBy`, `UsuarioId.from`.
- Persistencia/interacciones: lectura de conversación y mensajes; sin escritura.

### `RegistrarMensajeAsistenteService`

- Método clave: `registrar(RegistrarMensajeAsistenteCommand)`.
- Puertos usados: `ActorEmpresaScopePort`, `WhatsappMensajeLecturaPort`, `FindAiConversacionPort`, `FindAiMensajesByConversacionPort`, `FindAiResumenPort`, `FindAiMemoriaPort`, `SaveAiMensajePort`, `SaveAiResumenPort`, `GenerarChatAsistentePort`.
- Flujo: resuelve tenant del actor, carga conversación AI, valida ownership, carga transcript WhatsApp persistido, historial AI, resumen y memoria, genera respuesta, persiste turno de usuario y asistente, refresca resumen si existe y devuelve el mismo `aiConversacionId`.
- Seguridad/tenant: usa Empresa-owned port para resolver scope y `AiConversacion.requireOwnedBy` para impedir acceso a conversación ajena o de otra empresa.
- Llamadas de dominio: `AiConversacion.requireOwnedBy`, `AiMensaje.crear`, `AiResumenContexto.reemplazarCon`.
- Persistencia/interacciones: lectura de conversación, mensajes, resumen, memoria y transcript; escritura de mensajes y resumen; generación externa vía AI port.

### `ConfirmarAccionMapper`

- Funciones clave: `toCreateContacto(AiAccion)`, `toCreateTrato(AiAccion)`, `toCreateTarea(AiAccion)`, `toMoverFicha(AiAccion)`; helpers de extracción `stringField`, `uuidField`, `intField`, `decimalField`, `dateField`, `dateTimeField`, `enumField`.
- Puertos usados: ninguno.
- Flujo: extrae campos de un JSON plano almacenado en `AiAccion.payloadJson` y construye comandos CRM reales (`CreateContactoCommand`, `CreateTratoCommand`, `CreateTareaCommand`, `MoverColumnaFichaCommand`).
- Seguridad/tenant: no decide ownership ni tenant; transforma forma JSON. Los errores de forma/tipo se expresan como `AccionInvalidaException`.
- Llamadas de dominio: no modifica entidades; lee datos de `AiAccion` y construye comandos de aplicación.
- Persistencia/interacciones: ninguna.

### `SaveAiAccionPortBridge`

- Método clave: `save(AiAccion)`.
- Puertos usados: `SaveAiAccionPort`.
- Flujo: delegación directa al puerto de persistencia para mantener limpia la firma de servicios que coordinan transiciones.
- Seguridad/tenant: sin decisiones propias.
- Llamadas de dominio: ninguna.
- Persistencia/interacciones: escritura vía `SaveAiAccionPort.save`.

### `AiTenantExceptionTranslator`

- Funciones clave: `resolve(ActorEmpresaScopePort, UUID, UUID)`, `resolveForSelector(ActorEmpresaScopePort, UUID, UUID)`, `assertActorOwnsTenant(ActorEmpresaScopePort, UUID, EmpresaId)`.
- Puertos usados: `ActorEmpresaScopePort`.
- Flujo: invoca el puerto Empresa y traduce `TenantScopeViolationException` a `AsistenteTenantException` con mensajes públicos del bounded context AI.
- Seguridad/tenant: centraliza la traducción de errores para resolución general, selector obligatorio PR7 y validación resource-first PR6.
- Llamadas de dominio: usa `EmpresaId` como VO; no altera agregados.
- Persistencia/interacciones: ninguna.

## Coordinación entre servicios de aplicación, dominio y puertos

Los servicios de aplicación mantienen una separación consistente: reciben comandos ya validados en lo escalar, coordinan puertos de entrada/salida, resuelven o validan tenant antes de tocar estado sensible y delegan invariantes de negocio a entidades de dominio. Las transiciones de `AiAccion` y `AiConversacion` no se implementan en infraestructura ni en controladores: se ejecutan mediante métodos del agregado (`requireOwnedBy`, `requireConfirmable`, `confirmar`, `rechazar`, `expirar`, `marcarEjecutada`, `marcarFallida`) y luego se persisten por puertos.

La generación AI y la lectura de WhatsApp se tratan como dependencias externas detrás de puertos outbound. El único punto que puede ejecutar mutaciones CRM reales es `ConfirmarAccionService`, y esa capacidad queda explícita por constructor. Los servicios de análisis, follow-up, propuesta y registro solo persisten conversación/mensajes/resúmenes o propuestas pendientes.

## Resumen de cobertura de tests inspeccionada

- `AnalizarChatServiceTest`: cubre rechazo de chat ajeno sin persistencia, persistencia de conversación/mensaje en chat propio, transcript WhatsApp como source-of-truth, id determinístico reutilizable, tenant resource-first frente a hints multiempresa y rechazo de recurso no poseído.
- `RegistrarMensajeAsistenteServiceTest`: cubre conversación inexistente, conversación ajena o de otra empresa sin persistir ni invocar AI, happy path con turnos usuario/asistente y forwarding del transcript WhatsApp al puerto AI.
- `ProponerAccionServiceTest`: cubre staging con contexto confiable, ausencia de staging si falla el resolver, propagación de fallas del staging y guard por reflexión para impedir dependencias a use cases reales de mutación.
- `RegistrarAccionServiceTest`: cubre creación `PENDING` con versión inicial, ownership por `solicitadaPor`, payload opaco, audit linkage con `aiConversacionId`/`waMensajeId`, ausencia de dependencias a mutaciones CRM e invariantes del comando.
- `ConfirmarAccionServiceTest`: cubre ownership/tenant, cross-check de empresa seleccionada, actor sin tenant del recurso, estados no confirmables, not found, versión, expiración con marcado `EXPIRED`, dispatch de los cuatro tipos de mutación y marcado `FAILED` ante falla de mutación.
- `RechazarAccionServiceTest`: cubre transición `PENDING → REJECTED`, rechazo de acción ajena/inexistente/no pendiente, cross-check de empresa y ownership del tenant del recurso sin tocar CRM.
- `ExpirarAccionServiceTest`: cubre expiración batch de pendientes vencidas y lista vacía.
- `ListarAccionesPendientesServiceTest`: cubre delegación con actor/empresa/límite, lista vacía, `empresaId` requerido, actor sin empresa o con empresa no poseída y no auto-resolución multiempresa.
- `ObtenerAccionServiceTest`: cubre not found, acción de otro actor y retorno de acción propia.
- `ListarConversacionesAsistenteServiceTest`: cubre delegación al puerto con actor, empresa resuelta y límite.
- `ObtenerConversacionAsistenteServiceTest`: cubre not found, conversación ajena sin mensajes, happy path con mensajes, cross-check de empresa seleccionada y actor sin tenant del recurso.
- `ConfirmarAccionMapperTest`: cubre mapeos happy path para contacto/trato/tarea/mover ficha, opcionales ausentes, campos obligatorios faltantes y UUID inválido.

## Riesgos y gaps del slice

- `AiTenantExceptionTranslator.java`, `ListarAccionesPendientesService.java` y `ListarAccionesPendientesServiceTest.java` están no versionados (`??`). Si no se agregan al cambio, el comportamiento PR7 y la traducción de tenant quedan incompletos.
- `ConfirmarAccionMapper` usa extracción manual por substring y solo es robusto para JSON plano y simple; no maneja estructuras anidadas ni escaping complejo. Es aceptable como implementación temporal documentada, pero es un punto de fragilidad.
- `RegistrarMensajeAsistenteService` no aplica el mismo cross-check resource-first explícito que `ObtenerConversacionAsistenteService`; resuelve el tenant seleccionado y luego deja que `AiConversacion.requireOwnedBy` rechace mismatches. Está cubierto por tests, pero puede producir semántica de excepción distinta frente a otros endpoints resource-bound.
- `AnalizarChatService` calcula el id determinístico con `String.getBytes()` sin charset explícito; conviene fijar `UTF_8` para evitar dependencia del charset por defecto.
- `ExpirarAccionService` confía en que `UpdateEstadoAccionPort.findPendingExpired` aplique correctamente filtros de estado, vencimiento y límite; el servicio no revalida esos criterios.
- Esta exploración no ejecutó tests ni compilación por instrucción explícita; la cobertura descrita es una lectura estática de los tests existentes.

## Conclusión

El slice de servicios de aplicación implementa una frontera hexagonal coherente para el asistente AI: los servicios coordinan comandos, puertos outbound e invariantes de dominio; la ejecución real de mutaciones CRM queda concentrada en confirmación; y los flujos sensibles aplican resolución de tenant mediante el puerto Empresa y checks del agregado. El principal seguimiento recomendado es asegurar que los archivos no versionados se incorporen al cambio y revisar los gaps de parsing JSON y consistencia resource-first en follow-up.
