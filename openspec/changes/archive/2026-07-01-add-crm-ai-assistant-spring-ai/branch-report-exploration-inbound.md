# Exploración de rama — Infraestructura inbound REST + AI tools

## Alcance y fuente

- Proyecto: `ar-crm` / `CRM2`.
- Cambio OpenSpec activo: `openspec/changes/add-crm-ai-assistant-spring-ai/`.
- Slice explorado: REST inbound AI, DTOs REST AI, manejo global de excepciones AI, herramientas Spring AI y sus tests correspondientes.
- Fuente de verdad utilizada: working tree (`git diff HEAD` y `git status --short`). No se usó `origin/main...HEAD` como fuente principal.
- No se ejecutaron tests.

## Inventario de archivos del slice

### REST controllers

- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/ai/AiController.java` — nuevo/untracked.

### REST mappers

- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/ai/AiRestCommandMapper.java` — nuevo/untracked.

### REST DTOs

- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/AnalizarChatRequest.java` — nuevo/untracked.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/ListarAccionesPendientesRequest.java` — nuevo/untracked.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/AiMensajeResponse.java` — nuevo/untracked.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/ConversacionAsistenteResponse.java` — nuevo/untracked.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/ConversacionSummaryResponse.java` — nuevo/untracked.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/AccionPendienteResponse.java` — nuevo/untracked.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/AccionEjecutadaResponse.java` — nuevo/untracked.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/dto/ai/AccionRechazadaResponse.java` — nuevo/untracked.

### Exception handling

- `infrastructure/src/main/java/com/ar/crm2/adapter/in/rest/GlobalExceptionHandler.java` — modificado.

### AI tools

- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/BuscarClientePorTelefonoTool.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/ListarColumnasTableroTool.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/ObtenerMensajesRecientesTool.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/ObtenerResumenChatTool.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/ProponerAccionTool.java` — nuevo.

### AI tool mappers

- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/BuscarClientePorTelefonoToolMapper.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/ListarColumnasTableroToolMapper.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/ObtenerMensajesRecientesToolMapper.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/ProponerAccionToolMapper.java` — nuevo.

### AI tool DTOs

- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/BuscarClientePorTelefonoRequest.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/BuscarClientePorTelefonoResponse.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/ListarColumnasTableroRequest.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/ListarColumnasTableroResponse.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/ObtenerMensajesRecientesRequest.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/ObtenerMensajesRecientesResponse.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/ObtenerResumenChatResponse.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/ProponerAccionRequest.java` — nuevo.
- `infrastructure/src/main/java/com/ar/crm2/adapter/in/tool/ai/dto/ProponerAccionResponse.java` — nuevo.

### Tests del slice

- REST: `AiControllerIT.java`, `AiRestCommandMapperTest.java`, `ListarAccionesPendientesRequestTest.java` — nuevos/untracked.
- Exception handling: `GlobalExceptionHandlerTest.java` — modificado.
- Tools: `BuscarClientePorTelefonoToolTest.java`, `ListarColumnasTableroToolTest.java`, `ObtenerMensajesRecientesToolTest.java`, `ObtenerResumenChatToolTest.java`, `ProponerAccionToolTest.java`, `ProponerAccionToolMapperTest.java` — nuevos.

## REST inbound: clases de producción

### `AiController`

Controlador `@RestController` bajo `/api/ai`. Es un adaptador fino: extrae `ActorContext` desde `ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE`, usa `AiRestCommandMapper` para construir comandos y delega en puertos de entrada de aplicación. No accede a repositorios.

- `POST /api/ai/chat`
  - Request: `@Valid @RequestBody AnalizarChatRequest`.
  - Validación REST: `waConversacionId` obligatorio y máximo 200; `mensajeUsuario` opcional, máximo 16384.
  - Mapeo: `AiRestCommandMapper.toCommand(actor, request)` produce `AnalizarChatCommand(actorUsuarioId, null, waConversacionId, mensajeUsuario)`.
  - Use case: `AnalizarChatUseCase.analizar(command)`.
  - Response: `AiMensajeResponse.fromDomain(ResultadoAnalisisChat)`.
  - Seguridad/tenant: el endpoint no acepta `empresaId`; el tenant debe derivarse en aplicación desde la conversación WhatsApp.

- `POST /api/ai/acciones/{id}/confirmar`
  - Request: path `id`, query `expectedVersion`, query `empresaId`.
  - Mapeo: `ConfirmarAccionCommand(actorUsuarioId, accionId, expectedVersion, empresaId)`.
  - Use case: `ConfirmarAccionUseCase.confirmar(command)`.
  - Response: `AccionEjecutadaResponse.fromDomain(ResultadoEjecucionAccion)`.
  - Seguridad/tenant: `empresaId` es selector/cross-check obligatorio; la aplicación carga la acción, compara contra el tenant del recurso, verifica ownership, estado, versión y expiración.

- `POST /api/ai/acciones/{id}/rechazar`
  - Request: path `id`, query `empresaId`.
  - Mapeo: `RechazarAccionCommand(actorUsuarioId, accionId, empresaId)`.
  - Use case: `RechazarAccionUseCase.rechazar(command)`.
  - Response: `AccionRechazadaResponse.fromDomain(AiAccion)`.
  - Seguridad/tenant: mismo patrón de cross-check por `empresaId`; no ejecuta mutaciones CRM reales, solo transición de propuesta.

- `GET /api/ai/conversaciones/{id}`
  - Request: path `id`, query `empresaId`.
  - Mapeo: `ObtenerConversacionAsistenteCommand(actorUsuarioId, aiConversacionId, empresaId)`.
  - Use case: `ObtenerConversacionAsistenteUseCase.obtener(command)`.
  - Response: `ConversacionAsistenteResponse.fromDomain(result)` con conversación y mensajes ordenados.
  - Seguridad/tenant: la aplicación usa el tenant del recurso como autoridad y trata `empresaId` como cross-check obligatorio.

- `GET /api/ai/acciones`
  - Request: query-bound `@Valid ListarAccionesPendientesRequest`.
  - Validación REST: `empresaId` obligatorio; `limite` opcional entre 1 y 200.
  - Mapeo: `ListarAccionesPendientesCommand(actorUsuarioId, empresaId, limite)`; `limite` default `50` cuando viene `null`.
  - Use case: `ListarAccionesPendientesUseCase.listar(command)`.
  - Response: lista de `AccionPendienteResponse.fromDomain(AiAccion)`.
  - Seguridad/tenant: selector explícito obligatorio. No debe auto-resolver empresa, ni siquiera para actores de una sola empresa.

### `AiRestCommandMapper`

Métodos públicos estáticos:

- `toCommand(ActorContext, AnalizarChatRequest)` — construye `AnalizarChatCommand` con `empresaId = null` por contrato resource-first.
- `toCommand(ActorContext, UUID accionId, int expectedVersion, UUID empresaId)` — construye `ConfirmarAccionCommand`.
- `toRechazarAccionCommand(ActorContext, UUID accionId, UUID empresaId)` — construye `RechazarAccionCommand`.
- `toObtenerConversacionCommand(ActorContext, UUID aiConversacionId, UUID empresaId)` — construye `ObtenerConversacionAsistenteCommand`.
- `toListarAccionesPendientesCommand(ActorContext, ListarAccionesPendientesRequest)` — aplica default `limite = 50` y construye `ListarAccionesPendientesCommand`.

El mapper toma `actor.usuarioId()` como fuente de identidad. Si el actor no tiene `usuarioId`, los constructores de comandos rechazan el valor con `IllegalArgumentException`.

### REST DTOs

- `AnalizarChatRequest` — record de entrada para `/chat`; valida `waConversacionId` con `@NotBlank @Size(max = 200)` y `mensajeUsuario` con `@Size(max = 16384)`.
- `ListarAccionesPendientesRequest` — record de query params para `/acciones`; valida `empresaId` con `@NotNull` y `limite` con `@Min(1) @Max(200)`.
- `AiMensajeResponse` — `fromDomain(ResultadoAnalisisChat)` proyecta `aiConversacionId`, texto del asistente y modelo.
- `ConversacionAsistenteResponse` — `fromDomain(ResultadoConversacionAsistente)` proyecta `AiConversacion` y `AiMensaje` a nested records JSON-friendly; timestamps se serializan vía `toString()`.
- `ConversacionSummaryResponse` — `fromDomain(AiConversacion)` proyecta una conversación para listados; en este slice no aparece usado por `AiController`.
- `AccionPendienteResponse` — `fromDomain(AiAccion)` expone id, empresa, conversación AI, tipo, rationale, versión, expiración, estado y creación.
- `AccionEjecutadaResponse` — `fromDomain(ResultadoEjecucionAccion)` expone estado, entidad resultante, error y nueva versión.
- `AccionRechazadaResponse` — `fromDomain(AiAccion)` expone id, empresa, conversación AI, estado, versión y actualización.

## Exception handling relevante a AI

`GlobalExceptionHandler` agrega handlers específicos para excepciones del bounded context AI antes de caer en errores genéricos:

- `AccionNotFoundException` → HTTP 404.
- `AccionStateException` → HTTP 409.
- `AccionStateTransitionException` → HTTP 409.
- `AccionVersionMismatchException` → HTTP 409.
- `AccionExpiredException` → HTTP 409.
- `AccionNotOwnedByActorException` → HTTP 403.
- `ConversacionAsistenteNotOwnedByActorException` → HTTP 403.
- `ConversacionAsistenteNoEncontradaException` → HTTP 404.
- `AsistenteTenantException` → HTTP 403.

La respuesta mantiene el formato uniforme `{"error": "..."}`.

## AI tools: clases de producción

### `BuscarClientePorTelefonoTool`

- Callback público: `buscarClientePorTelefono(BuscarClientePorTelefonoRequest)` anotado con `@Tool(name = "buscarClientePorTelefono")`.
- Request: solo `telefono`; el constructor rechaza null/blank y `@ToolParam` describe el parámetro.
- Orquestación: resuelve `AiToolContext` desde `AiToolContextPort`, convierte `ctx.empresaId()` a `EmpresaId`, llama `ContactoLecturaPort.findByEmpresaIdAndTelefono(...)` y delega respuesta en `BuscarClientePorTelefonoToolMapper.toResponse(...)`.
- Seguridad/tenant: el modelo no puede enviar `empresaId`; el lookup queda acotado al tenant confiable del contexto.
- Response: `BuscarClientePorTelefonoResponse.hit(...)` o `miss()`; `encontrado=false` deja el resto de campos en `null`.

### `BuscarClientePorTelefonoToolMapper`

- Método público: `toResponse(Contacto)`.
- Mapea `Contacto` a wire DTO o `miss()` si no hay contacto. No valida reglas de negocio.

### `ListarColumnasTableroTool`

- Callback público: `listarColumnasTablero(ListarColumnasTableroRequest)` anotado con `@Tool(name = "listarColumnasTablero")`.
- Request: `tipoTablero`; constructor rechaza null/blank; `tipoTableroAsEnum()` usa `TipoTablero.valueOf(...)`.
- Orquestación: llama `ColumnaLecturaPort.findByTipoTablero(...)` y mapea con `ListarColumnasTableroToolMapper.toResponseList(...)`.
- Seguridad/tenant: no usa `AiToolContextPort`; esto es coherente solo si las columnas son catálogo global por tipo de tablero.
- Response: lista de columnas, nunca `null`.

### `ListarColumnasTableroToolMapper`

- Método público: `toResponseList(List<Columna>)`.
- Devuelve lista vacía si el input es null/vacío; mapea id, nombre, color, tipo de tablero y tipo de columna.

### `ObtenerMensajesRecientesTool`

- Callback público: `obtenerMensajesRecientes(ObtenerMensajesRecientesRequest)` anotado con `@Tool(name = "obtenerMensajesRecientes")`.
- Request: solo `limit`; constructor restringe rango 1..50. El modelo no puede enviar `waConversacionId`.
- Orquestación: resuelve `AiToolContext`, convierte `ctx.waConversacionId()` a `UUID`, llama `WhatsappMensajeLecturaPort.findByConversacionId(...)`, recorta los últimos N mensajes con `ObtenerMensajesRecientesToolMapper.takeLastN(...)` y mapea a DTOs.
- Seguridad/tenant: la conversación WhatsApp viene del contexto confiable; si no hay contexto, envuelve `IllegalStateException` en `AiAssistantException` y no llama al port de lectura.
- Response: lista cronológica ascendente de mensajes recientes.

### `ObtenerMensajesRecientesToolMapper`

- Métodos públicos:
  - `toResponseList(List<WhatsappMensajeResumen>)` — proyección a DTOs.
  - `takeLastN(List<WhatsappMensajeResumen>, int)` — recorte defensivo de últimos N preservando orden.
  - `limitOf(ObtenerMensajesRecientesRequest)` — lee el límite validado.

### `ObtenerResumenChatTool`

- Callback público: `obtenerResumenChat()` anotado con `@Tool(name = "obtenerResumenChat")` y sin parámetros.
- Request: no existe; el modelo no puede enviar `aiConversacionId`.
- Orquestación: resuelve `AiToolContext`, llama `FindAiResumenPort.findByConversacionId(ctx.aiConversacionId())` y proyecta `AiResumenContexto`.
- Seguridad/tenant: el id de conversación AI viene del contexto confiable; si falta contexto, lanza `AiAssistantException`.
- Response: `ObtenerResumenChatResponse` con facts/inferences/sourceWatermark/actualizadoEn o placeholder con campos `null`.

### `ProponerAccionTool`

- Callback público: `proponerAccion(ProponerAccionRequest)` anotado con `@Tool(name = "proponerAccion")`.
- Request: `tipoAccion`, `payloadJson`, `rationale`, `ttlMinutos`; constructor rechaza campos obligatorios vacíos y TTL no positivo; cada componente tiene `@ToolParam`.
- Orquestación: `ProponerAccionToolMapper.toCommand(request)` → `ProponerAccionUseCase.proponer(command)` → `ProponerAccionToolMapper.toResponse(result)`.
- Seguridad/tenant: el tool no recibe actor/tenant/conversación; esos datos se resuelven dentro del use case mediante contexto confiable. No inyecta use cases de mutación real (`CreateContacto`, `CreateTrato`, `CreateTarea`, `MoverColumnaFicha`).
- Response: id de acción y estado, esperado `PENDING`.

### `ProponerAccionToolMapper`

- Métodos públicos:
  - `toCommand(ProponerAccionRequest)` — convierte `tipoAccion` con `TipoAccion.valueOf(...)` y pasa payload/rationale/TTL al comando de aplicación.
  - `toResponse(application.ai.port.in.result.ProponerAccionResponse)` — proyecta id y estado; rechaza result `null` con `IllegalStateException`.

### Tool DTOs

- `BuscarClientePorTelefonoRequest` — `telefono` obligatorio; no incluye tenant.
- `BuscarClientePorTelefonoResponse` — `hit(...)`/`miss()` para respuesta explícita de encontrado/no encontrado.
- `ListarColumnasTableroRequest` — `tipoTablero` obligatorio; conversión a enum en método público `tipoTableroAsEnum()`.
- `ListarColumnasTableroResponse` — columna de catálogo para el modelo.
- `ObtenerMensajesRecientesRequest` — `limit` obligatorio y acotado a 1..50; no incluye conversación.
- `ObtenerMensajesRecientesResponse` — proyección mínima de mensaje WhatsApp.
- `ObtenerResumenChatResponse` — resumen nullable de conversación AI.
- `ProponerAccionRequest` — discriminador, payload JSON, rationale y TTL; valida campos obligatorios.
- `ProponerAccionResponse` — id y estado de propuesta staged.

## Cómo los adaptadores llaman a aplicación

- REST inbound sigue el patrón hexagonal esperado: `AiController` depende de puertos de entrada (`AnalizarChatUseCase`, `ConfirmarAccionUseCase`, `RechazarAccionUseCase`, `ObtenerConversacionAsistenteUseCase`, `ListarAccionesPendientesUseCase`) y usa `AiRestCommandMapper` para convertir HTTP → comandos de aplicación.
- `ProponerAccionTool` también sigue el patrón de entrada: tool → mapper → `ProponerAccionUseCase`; el use case conserva la responsabilidad de resolver scope confiable y stagear la acción.
- Los tools de lectura (`BuscarClientePorTelefonoTool`, `ListarColumnasTableroTool`, `ObtenerMensajesRecientesTool`, `ObtenerResumenChatTool`) llaman directamente puertos de aplicación de lectura (`ContactoLecturaPort`, `ColumnaLecturaPort`, `WhatsappMensajeLecturaPort`, `FindAiResumenPort`) más `AiToolContextPort` cuando necesitan scope. No llaman repositorios directamente, pero sí usan puertos de salida desde un adaptador de entrada.
- La frontera de confianza para actor/tenant en tools se desplaza a `AiToolContextPort`: el modelo solo provee datos de búsqueda o propuesta; los IDs de actor, tenant, conversación AI y conversación WhatsApp no vienen del payload del modelo.

## Resumen de tests por comportamiento

- `AiRestCommandMapperTest`: cubre identidad desde `ActorContext`, `/chat` con `empresaId = null`, propagación de path/query/body a comandos, límite default `50`, y defensa en profundidad cuando faltan `usuarioId`/`empresaId` o el límite excede rango.
- `AiControllerIT`: WebMvc slice con use cases mockeados. Cubre happy paths de chat, confirmar, rechazar, obtener conversación y listar acciones; verifica forwarding de comandos; valida que `/chat` no envía tenant hint; cubre 403/404/409 para tenant mismatch, missing resources, replay y version mismatch; cubre `/acciones` sin `empresaId`, límite fuera de rango, selector no autorizado y default de límite.
- `ListarAccionesPendientesRequestTest`: Bean Validation del selector obligatorio y rango de límite; acepta `limite = null` y bordes 1/200.
- `GlobalExceptionHandlerTest`: prueba directa de handlers AI nuevos: 404 para acción/conversación no encontrada, 409 para estado/transición/versión/expiración, 403 para ownership/tenant.
- Tool tests: cubren happy paths, respuestas vacías seguras, resolución de contexto confiable, ausencia de campos scope en DTOs model-facing, anotaciones `@Tool`/`@ToolParam`, constructores sin use cases de mutación real, y que no se retengan DTOs/proyecciones/contexto como campos.
- `ProponerAccionToolMapperTest`: cubre conversión de `tipoAccion` a enum, preservación de payload/rationale/TTL, respuesta staged, error con tipo desconocido y error con resultado null.

## Riesgos y gaps identificados

1. `AiAssistantException` declara que debe mapear a HTTP 502, y los tools la lanzan cuando falta `AiToolContext`, pero `GlobalExceptionHandler` no tiene handler específico para esa excepción. Resultado probable: 500 genérico en vez de 502 controlado.
2. El diseño espera que los errores de binding/validación de query DTO en `GET /api/ai/acciones` produzcan 400 con cuerpo `{"error": ...}`. `GlobalExceptionHandler` solo maneja `MethodArgumentNotValidException`; no se observa handler para `BindException`/`HandlerMethodValidationException`. Riesgo de respuesta no uniforme o no cubierta según la excepción real emitida por Spring MVC.
3. Hay una divergencia de arquitectura respecto de la propuesta: los tools de lectura llaman puertos de salida de aplicación directamente desde adaptadores de entrada. No acceden a repositorios, pero sí omiten un use case de lectura que concentre validación/authorización. Conviene confirmar si esta excepción de arquitectura fue aceptada.
4. `ListarColumnasTableroTool` no usa `AiToolContextPort` ni actor/tenant. Es seguro si `Columna` es catálogo global; si las columnas pasan a ser tenant-owned, incumpliría el requisito de scoping por empresa.
5. `ConversacionSummaryResponse` está nuevo, pero no se usa en `AiController` dentro de este slice. Puede ser preparación para un endpoint futuro o código sobrante.
6. `ProponerAccionRequest` deja que el modelo elija `ttlMinutos` y solo valida que sea positivo en este adapter. Se debe verificar fuera de este slice que el use case aplique un máximo razonable.
7. `BuscarClientePorTelefonoTool` registra el teléfono completo en logs. Puede ser aceptable para depuración, pero es PII; conviene evaluar enmascaramiento.

## Conclusión

El slice REST está alineado con el flujo de comandos y con la separación actor-identidad / tenant-resource-first. El flujo propose-only está protegido en `ProponerAccionTool`, pero los tools de lectura introducen una decisión arquitectónica relevante: usan puertos de lectura directamente en vez de use cases. Antes de cerrar el cambio, conviene resolver los gaps de exception mapping (`AiAssistantException` y validación de query DTO) y confirmar explícitamente la excepción arquitectónica para tools read-only.
