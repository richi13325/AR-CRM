# Exploración: contratos de aplicación del asistente de IA

## Alcance y fuentes

- Cambio activo: `openspec/changes/add-crm-ai-assistant-spring-ai/`.
- Fuente de verdad usada: `git status --short --untracked-files=all` y `git diff HEAD` del árbol de trabajo local.
- Alcance limitado a contratos de aplicación bajo `application/src/main/java/com/ar/crm2/application/ai/{command,exception,port/in,port/out}` y tests de comandos bajo `application/src/test/java/com/ar/crm2/application/ai/command`.
- No se ejecutaron tests.

Convención de estado: `A` = agregado en el índice/diff; `AM` = agregado y modificado en el árbol de trabajo; `??` = no trackeado.

## Inventario y contratos por grupo

Para los `record`, los accesores clave son los componentes listados en la firma canónica.

### Comandos

| Estado | Archivo | Firma / métodos clave | Responsabilidad contractual |
|---|---|---|---|
| `A` | `AnalizarChatCommand.java` | `record AnalizarChatCommand(UUID actorUsuarioId, UUID empresaId, String waConversacionId, String mensajeUsuario)` | Solicita análisis de un chat de WhatsApp. `actorUsuarioId` y `waConversacionId` son obligatorios; `empresaId` es solo indicio opcional y no autoridad de tenant. |
| `AM` | `ConfirmarAccionCommand.java` | `record ConfirmarAccionCommand(UUID actorUsuarioId, UUID accionId, int expectedVersion, UUID empresaId)` | Confirma una propuesta pendiente. Exige actor, acción, versión positiva y `empresaId` obligatorio para cross-check estricto contra el tenant del recurso. |
| `A` | `ExpirarAccionCommand.java` | `record ExpirarAccionCommand(LocalDateTime ahora, int maxPorLote)` | Define el barrido de expiración. Exige instante no nulo y lote entre 1 y 1000. |
| `??` | `ListarAccionesPendientesCommand.java` | `record ListarAccionesPendientesCommand(UUID actorUsuarioId, UUID empresaId, int limite)` | Lista propuestas `PENDING` del actor. `empresaId` es obligatorio; el contrato prohíbe inferir o auto-resolver tenant. |
| `A` | `ListarConversacionesAsistenteCommand.java` | `record ListarConversacionesAsistenteCommand(UUID actorUsuarioId, UUID empresaId, int limite)` | Lista conversaciones IA del actor. `empresaId` puede resolverse por el puerto de scope cuando es nulo; `limite` 1..200. |
| `A` | `ObtenerAccionCommand.java` | `record ObtenerAccionCommand(UUID actorUsuarioId, UUID accionId, UUID empresaId)` | Obtiene una propuesta por id con scope de actor/tenant. Exige actor y acción; `empresaId` está documentado como opcional. |
| `AM` | `ObtenerConversacionAsistenteCommand.java` | `record ObtenerConversacionAsistenteCommand(UUID actorUsuarioId, UUID aiConversacionId, UUID empresaId)` | Obtiene una conversación IA y exige `empresaId` obligatorio para cross-check estricto contra el tenant del recurso. |
| `A` | `ProponerAccionCommand.java` | `record ProponerAccionCommand(TipoAccion tipo, String payloadJson, String rationale, int ttlMinutos)` | Entrada de herramienta IA para proponer una acción. Solo transporta forma suministrada por el modelo; no permite actor/tenant/conversación desde el payload. |
| `AM` | `RechazarAccionCommand.java` | `record RechazarAccionCommand(UUID actorUsuarioId, UUID accionId, UUID empresaId)` | Rechaza una propuesta pendiente. Exige actor, acción y `empresaId` obligatorio para cross-check estricto contra el recurso. |
| `A` | `RegistrarAccionCommand.java` | `record RegistrarAccionCommand(UUID actorUsuarioId, UUID empresaId, UUID aiConversacionId, String waConversacionId, String waMensajeId, String tipoAccion, String payloadJson, String rationale, int ttlMinutos)`; `static conTipoAccion(...)` | Registra una propuesta IA en estado `PENDING`. Normaliza `waMensajeId` vacío a `null`; la factory tipada guarda `TipoAccion.name()`. |
| `A` | `RegistrarMensajeAsistenteCommand.java` | `record RegistrarMensajeAsistenteCommand(UUID actorUsuarioId, UUID aiConversacionId, UUID empresaId, String mensajeUsuario)` | Registra un mensaje de seguimiento en una conversación IA existente. Exige actor, conversación y contenido; `empresaId` está documentado como opcional. |

### Puertos de entrada / casos de uso

| Estado | Archivo | Firma clave | Responsabilidad contractual |
|---|---|---|---|
| `A` | `AnalizarChatUseCase.java` | `ResultadoAnalisisChat analizar(AnalizarChatCommand command)` | Orquesta análisis inicial: resuelve tenant, carga transcript WhatsApp, memoria/resumen, invoca generación IA y persiste turnos. |
| `A` | `ConfirmarAccionUseCase.java` | `ResultadoEjecucionAccion confirmar(ConfirmarAccionCommand command)` | Único contrato IA autorizado a invocar mutaciones reales de CRM, tras validar dueño, tenant, estado, versión y expiración. |
| `A` | `ExpirarAccionUseCase.java` | `int expirar(ExpirarAccionCommand command)` | Marca como `EXPIRED` propuestas pendientes vencidas, hasta el límite del comando. |
| `??` | `ListarAccionesPendientesUseCase.java` | `List<AiAccion> listar(ListarAccionesPendientesCommand command)` | Bandeja de acciones pendientes por actor y tenant explícito; no debe exponer estados no pendientes. |
| `A` | `ListarConversacionesAsistenteUseCase.java` | `List<AiConversacion> listar(ListarConversacionesAsistenteCommand command)` | Lista conversaciones IA dentro del scope del solicitante. |
| `A` | `ObtenerAccionUseCase.java` | `AiAccion obtener(ObtenerAccionCommand command)` | Obtiene una propuesta IA concreta, acotada al actor y tenant. |
| `A` | `ObtenerConversacionAsistenteUseCase.java` | `ResultadoConversacionAsistente obtener(ObtenerConversacionAsistenteCommand command)`; `record ResultadoConversacionAsistente(AiConversacion conversacion, List<AiMensaje> mensajes)` | Devuelve conversación IA y su historial completo ordenado; rechaza accesos fuera del dueño/scope. |
| `A` | `ProponerAccionUseCase.java` | `ProponerAccionResponse proponer(ProponerAccionCommand command)` | Frontera única para herramientas IA: resuelve contexto confiable mediante `AiToolContextPort`, construye staging y delega persistencia de propuesta pendiente. |
| `A` | `RechazarAccionUseCase.java` | `AiAccion rechazar(RechazarAccionCommand command)` | Rechaza una propuesta pendiente sin tocar entidades CRM reales. |
| `A` | `RegistrarAccionUseCase.java` | `AiAccion registrar(RegistrarAccionCommand command)` | Persiste una nueva propuesta IA en `PENDING`; no ejecuta mutaciones reales. |
| `A` | `RegistrarMensajeAsistenteUseCase.java` | `ResultadoAnalisisChat registrar(RegistrarMensajeAsistenteCommand command)` | Agrega un turno de usuario en una conversación IA existente y obtiene la siguiente respuesta del asistente. |

### Resultados de puertos de entrada

| Estado | Archivo | Firma / métodos clave | Responsabilidad contractual |
|---|---|---|---|
| `A` | `ProponerAccionResponse.java` | `record ProponerAccionResponse(String accionId, String estado)` | Resultado de staging para herramientas IA; expone id de propuesta y estado resultante, esperado como `PENDING` en éxito. |
| `A` | `ResultadoAnalisisChat.java` | `record ResultadoAnalisisChat(UUID aiConversacionId, String contenidoAsistente, String modelo)` | Resultado de análisis/seguimiento. Exige conversación IA y normaliza contenido nulo a cadena vacía; no transporta acciones. |
| `A` | `ResultadoEjecucionAccion.java` | `record ResultadoEjecucionAccion(EstadoAccion estado, String resultadoEntidadId, String errorReason, int nuevaVersion)`; `ejecutada(...)`; `fallida(...)` | Resultado de confirmación: `EXECUTED` con entidad resultante o `FAILED` con razón. Exige estado y versión positiva. |

### Puertos de salida

| Estado | Archivo | Firma clave | Responsabilidad contractual |
|---|---|---|---|
| `A` | `AiToolContextPort.java` | `AiToolContext resolve()` | Resuelve contexto confiable para herramientas IA desde infraestructura; debe fallar si no hay scope. |
| `A` | `ColumnaLecturaPort.java` | `Optional<Columna> findById(ColumnaId id)`; `List<Columna> findByTipoTablero(TipoTablero tipoTablero)` | Lee columnas/catálogos válidos para propuestas de movimiento Kanban. |
| `A` | `ContactoLecturaPort.java` | `Optional<Contacto> findById(ContactoId id)`; `Optional<Contacto> findByEmpresaIdAndTelefono(EmpresaId empresaId, String telefono)` | Lee contactos para herramientas IA usando tenant confiable. |
| `A` | `DeleteAiMemoriaPort.java` | `void delete(AiMemoria memoria)` | Elimina memoria IA para olvido o reemplazo controlado. |
| `A` | `FichaLecturaPort.java` | `Optional<Ficha> findById(FichaId id)` | Lee fichas para validación/consulta de herramientas IA. |
| `A` | `FindAiAccionPort.java` | `Optional<AiAccion> findById(UUID id)` | Busca una propuesta IA por id. |
| `A` | `FindAiConversacionPort.java` | `Optional<AiConversacion> findById(UUID id)` | Busca una conversación IA por id. |
| `A` | `FindAiMemoriaPort.java` | `List<AiMemoria> findActivasByConversacionId(UUID waConversacionId, UUID actorUsuarioId, UUID empresaId)` | Carga memorias IA activas en scope de conversación/actor/tenant. |
| `A` | `FindAiMensajesByConversacionPort.java` | `List<AiMensaje> findByConversacionId(UUID aiConversacionId)` | Carga mensajes IA por conversación. |
| `A` | `FindAiResumenPort.java` | `Optional<AiResumenContexto> findByConversacionId(UUID aiConversacionId)` | Carga el resumen de contexto vigente de una conversación IA. |
| `A` | `GenerarChatAsistentePort.java` | `RespuestaAsistente generar(ChatAsistenteRequest solicitud)` | Frontera hacia proveedor IA; mantiene tipos Spring AI/OpenAI fuera de aplicación. |
| `A` | `GenerarEmbeddingPort.java` | `List<Double> embed(String texto)` | Frontera para embeddings de memoria IA; permite reemplazar implementación placeholder por proveedor real. |
| `A` | `ListAiConversacionesPort.java` | `List<AiConversacion> listByActor(UUID actorUsuarioId, UUID empresaId, int limite)` | Lista conversaciones IA del actor dentro de tenant. |
| `??` | `ListPendingAiAccionesPort.java` | `List<AiAccion> listPendingByActor(UUID actorUsuarioId, UUID empresaId, int limite)` | Lista propuestas `PENDING` del actor para una empresa explícita. |
| `A` | `SaveAiAccionPort.java` | `AiAccion save(AiAccion accion)` | Persiste propuestas IA nuevas o actualizadas. |
| `A` | `SaveAiConversacionPort.java` | `AiConversacion save(AiConversacion conversacion)`; `AiConversacion findById(UUID id)` | Persiste y recupera conversaciones IA. |
| `A` | `SaveAiMemoriaPort.java` | `AiMemoria save(AiMemoria memoria)` | Persiste memoria IA. |
| `A` | `SaveAiMensajePort.java` | `AiMensaje save(AiMensaje mensaje)` | Persiste mensajes IA. |
| `A` | `SaveAiResumenPort.java` | `AiResumenContexto save(AiResumenContexto resumen)` | Persiste resúmenes de contexto IA. |
| `A` | `UpdateEstadoAccionPort.java` | `List<AiAccion> findPendingExpired(int limite)`; `AiAccion save(AiAccion accion)` | Soporta el barrido de expiración: encuentra pendientes vencidas y guarda transición. |
| `A` | `WhatsappConversacionLecturaPort.java` | `Optional<WhatsappConversacionResumen> findById(UUID waConversacionId)` | Lee resumen de conversación WhatsApp sin acoplar aplicación al módulo WhatsApp. |
| `A` | `WhatsappMensajeLecturaPort.java` | `List<WhatsappMensajeResumen> findByConversacionId(UUID waConversacionId)` | Lee transcript WhatsApp ordenado por conversación. |

### DTOs/proyecciones de puertos de salida

| Estado | Archivo | Firma | Responsabilidad contractual |
|---|---|---|---|
| `A` | `AiToolContext.java` | `record AiToolContext(UUID actorUsuarioId, UUID empresaId, UUID aiConversacionId, String waConversacionId)` | Transporta scope confiable para herramientas IA; exige todos los campos y evita confiar en payload del modelo para actor/tenant. |
| `A` | `ChatAsistenteRequest.java` | `record ChatAsistenteRequest(UUID aiConversacionId, UUID actorUsuarioId, UUID empresaId, String waConversacionId, List<MensajeChat> historial, List<MensajeChat> memoria, List<WhatsappMensajeResumen> transcript, String resumenFacts, String resumenInferences, String kickoffUsuario)` | Solicitud application-owned al proveedor IA; exige ids/scope, copia listas defensivamente y usa transcript persistido como fuente. |
| `A` | `MensajeChat.java` | `record MensajeChat(String rol, String contenido, String toolCallJson)` | Mensaje del historial IA; exige rol y contenido. |
| `A` | `RespuestaAsistente.java` | `record RespuestaAsistente(String contenido, String modelo, Integer promptTokens, Integer completionTokens, Long latencyMs)` | Respuesta del proveedor IA; solo texto/metadatos, no acciones. Normaliza contenido nulo a cadena vacía. |
| `A` | `WhatsappConversacionResumen.java` | `record WhatsappConversacionResumen(UUID waConversacionId, UUID canalId, UUID canalEmpresaId, UUID contactoId, String numeroTelefono, String nombreContacto)` | Proyección application-owned de conversación WhatsApp; `canalEmpresaId` es la autoridad de tenant. |
| `A` | `WhatsappMensajeResumen.java` | `record WhatsappMensajeResumen(UUID waMensajeId, UUID waConversacionId, String direccion, String tipo, String contenido, String mediaUrl, LocalDateTime creadoEn)` | Proyección application-owned de mensaje WhatsApp para construir transcripts. |

### Excepciones

| Estado | Archivo | Métodos clave | Responsabilidad contractual |
|---|---|---|---|
| `A` | `AccionInvalidaException.java` | `forInvalidInput(String reason)`; `forTenantMismatch(String actorUsuarioId, String empresaId)` | Error de comando/propuesta inválida; documenta mapeo 400 para input y 403 para tenant. |
| `A` | `AiAssistantException.java` | `upstreamFailure(String)`; `upstreamFailure(String, Throwable)`; `invalidAssistantOutput(String)` | Error genérico del proveedor o salida IA inválida; documenta mapeo 502. |
| `AM` | `AsistenteTenantException.java` | `chatNoPerteneceAlActor(...)`; `empresaNoEncontradaParaActor(...)`; `accionNoPerteneceALaEmpresaSeleccionada(...)`; `conversacionNoPerteneceALaEmpresaSeleccionada(...)`; `empresaNoPoseidaPorActor(...)`; `tenantSelectorRechazado(...)`; `from(TenantScopeViolationException, UUID, UUID)` | Excepción application-owned para violaciones de tenant/scope. Cubre resource-first cross-check, selector explícito PR7 y traducción desde excepción de dominio. |
| `A` | `ConversacionAsistenteNoEncontradaException.java` | `forId(String id)` | Conversación IA inexistente o inaccesible; documenta mapeo 404. |

## Cómo estos contratos definen la frontera del asistente IA

- **Entrada controlada por casos de uso.** REST, scheduler y herramientas IA deben entrar por puertos `port.in`; los comandos concentran validaciones mínimas y semántica de tenant antes de llegar a servicios de aplicación.
- **Tenant resource-first para recursos existentes.** `AnalizarChatCommand` toma `empresaId` solo como indicio; la autoridad viene del recurso WhatsApp. `ConfirmarAccionCommand`, `RechazarAccionCommand` y `ObtenerConversacionAsistenteCommand` exigen `empresaId` para cross-check estricto contra el recurso persistido.
- **Selector explícito donde no hay recurso direccionado.** `ListarAccionesPendientesCommand` requiere `empresaId` y documenta que no se debe inferir una empresa por defecto.
- **Herramientas IA sin identidad model-supplied.** `ProponerAccionCommand` no contiene actor/tenant/conversación; `ProponerAccionUseCase` debe obtenerlos desde `AiToolContextPort`. Esto evita que el modelo pueda inventar scope.
- **Separación entre proponer y ejecutar.** `ProponerAccionUseCase` y `RegistrarAccionUseCase` solo dejan propuestas en `PENDING`; `ConfirmarAccionUseCase` es el único contrato que puede despachar mutaciones reales CRM, con controles de dueño, tenant, versión, estado y expiración.
- **Dependencias de orquestación explicitadas por puertos de salida.** La aplicación depende de abstracciones para IA (`GenerarChatAsistentePort`, `GenerarEmbeddingPort`), persistencia IA (`Find*`, `Save*`, `UpdateEstadoAccionPort`, `List*`), lectura WhatsApp vía proyecciones, catálogos CRM para herramientas y contexto confiable de tool-calling.
- **Tipos application-owned en la frontera.** `ChatAsistenteRequest`, `RespuestaAsistente`, `MensajeChat` y las proyecciones WhatsApp evitan filtrar Spring AI, OpenAI o entidades del módulo WhatsApp al núcleo de aplicación.

## Cobertura de tests de comandos inspeccionada

Tests presentes bajo `application/src/test/java/com/ar/crm2/application/ai/command/**`:

- `ConfirmarAccionCommandTest`: cubre rechazo de `empresaId` nulo, aceptación de `empresaId` válido y preservación de rechazo para `actorUsuarioId` nulo.
- `RechazarAccionCommandTest`: cubre rechazo de `empresaId` nulo, aceptación de `empresaId` válido y preservación de rechazo para `accionId` nulo.
- `ObtenerConversacionAsistenteCommandTest`: cubre rechazo de `empresaId` nulo, aceptación de `empresaId` válido y preservación de rechazo para `aiConversacionId` nulo.
- `ListarAccionesPendientesCommandTest`: cubre rechazo de `empresaId` nulo, aceptación de selector válido, rechazo de `actorUsuarioId` nulo y límite inferior inválido.

No hay tests de comandos en este paquete para `AnalizarChatCommand`, `ExpirarAccionCommand`, `ListarConversacionesAsistenteCommand`, `ObtenerAccionCommand`, `ProponerAccionCommand`, `RegistrarAccionCommand` ni `RegistrarMensajeAsistenteCommand` dentro del slice inspeccionado.

## Riesgos y gaps del slice

- **Inconsistencia potencial de tipos para WhatsApp conversation id.** Algunos contratos usan `String waConversacionId` (`AnalizarChatCommand`, `RegistrarAccionCommand`, `AiToolContext`, `ChatAsistenteRequest`) mientras los puertos/proyecciones WhatsApp usan `UUID`. Conviene verificar el adapter inbound/outbound para evitar conversiones implícitas frágiles.
- **Semántica de tenant no uniforme por comando.** Algunos flujos exigen `empresaId` obligatorio y otros lo documentan como opcional/resoluble. Puede ser correcto por tipo de operación, pero requiere trazabilidad en specs/tests para evitar regresiones de autorización.
- **`GenerarChatAsistentePort` tiene documentación ambigua.** Su Javadoc menciona “assistant reply and any staged action proposals”, pero `RespuestaAsistente` declara explícitamente que no transporta acciones. La intención parece ser que el staging ocurra solo por tool-calling; conviene alinear el comentario del puerto.
- **`FindAiMemoriaPort` mezcla nombres de conversación.** El método se llama `findActivasByConversacionId`, el parámetro es `waConversacionId` y el Javadoc habla de conversación IA o contacto. Esto puede confundir implementaciones y tests.
- **Algunos puertos no son estrictamente single-method.** `SaveAiConversacionPort` y `UpdateEstadoAccionPort` combinan lectura/guardado. Puede ser aceptable por caso de uso, pero se desvía de la convención comentada en otros contratos.
- **Mapeo 400/403 de `AccionInvalidaException` puede ser frágil.** El comentario dice que el handler distingue por factory method name, pero esa información no queda en el tipo en runtime salvo que el handler use mensaje u otro criterio externo.
- **Cobertura de tests de comandos parcial.** Los tests nuevos protegen invariantes PR6/PR7, pero no cubren todos los comandos ni todos los rangos (`expectedVersion <= 0`, `limite > 200`, blanks en strings, TTL inválido) en el paquete de comandos.
- **Archivos no trackeados relevantes.** `ListarAccionesPendientesCommand`, `ListarAccionesPendientesUseCase`, `ListPendingAiAccionesPort` y los cuatro tests de comandos aparecen como `??`; deben incluirse explícitamente en la entrega para no perder el contrato PR7.

## Conclusión

El slice define una frontera de aplicación coherente para el asistente IA: comandos con invariantes de scope, puertos de entrada para orquestación, puertos de salida para IA/persistencia/lectura, y resultados que separan conversación, staging y ejecución. La decisión arquitectónica más importante es que el modelo puede proponer forma de acción, pero no identidad ni tenant, y nunca ejecuta mutaciones CRM sin confirmación explícita del usuario por `ConfirmarAccionUseCase`.
