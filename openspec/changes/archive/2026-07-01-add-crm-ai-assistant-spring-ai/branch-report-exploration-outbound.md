# Exploración outbound: persistencia, Spring AI y bridges de lectura

## Alcance y fuente

- Proyecto: `ar-crm` / `CRM2`.
- Cambio OpenSpec: `add-crm-ai-assistant-spring-ai`.
- Slice inspeccionado: adapters outbound de IA, persistencia IA, bridges de lectura CRM/WhatsApp, mapper JWT y `infrastructure/pom.xml`.
- Fuente de verdad: working tree comparado contra `HEAD` mediante `git status --short` y lectura directa del código actual.
- No se ejecutaron tests, por instrucción explícita.

## 1. Inventario de archivos cambiados/nuevos

### Spring AI adapter y contexto de herramientas

- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/AiToolContextAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/AiToolContextHolder.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/ThreadLocalAiToolContextHolder.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/spring/GenerarEmbeddingStubAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/spring/OpenAiChatAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/spring/mapper/SpringAiChatResponseMapper.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/ai/spring/mapper/SpringAiPromptMapper.java`

### Persistencia IA: adapters, entidades, mappers y repositorios

- `AM infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/AiAccionRepositoryAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/AiConversacionRepositoryAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/AiConversacionSaveAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/AiMemoriaRepositoryAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/AiMensajeRepositoryAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/AiResumenContextoRepositoryAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/entity/AiAccionJpaEntity.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/entity/AiConversacionJpaEntity.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/entity/AiMemoriaJpaEntity.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/entity/AiMensajeJpaEntity.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/entity/AiResumenContextoJpaEntity.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/mapper/AiAccionMapper.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/mapper/AiConversacionMapper.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/mapper/AiMemoriaMapper.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/mapper/AiMensajeMapper.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/mapper/AiResumenContextoMapper.java`
- `AM infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/repository/AiAccionSpringDataRepository.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/repository/AiConversacionSpringDataRepository.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/repository/AiMemoriaSpringDataRepository.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/repository/AiMensajeSpringDataRepository.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ai/repository/AiResumenContextoSpringDataRepository.java`

### Bridges de lectura CRM sobre adapters existentes

- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ColumnaRepositoryAdapter.java`
- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/ContactoRepositoryAdapter.java`
- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/EmpresaRepositoryAdapter.java`
- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/FichaRepositoryAdapter.java`
- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/TareaRepositoryAdapter.java`
- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/TratoRepositoryAdapter.java`
- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/repository/ColumnaRepository.java`
- `M infrastructure/src/main/java/com/ar/crm2/adapter/out/persistence/repository/EmpresaRepository.java`

### Bridges de lectura WhatsApp

- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/whatsapp/WhatsappCanalNotFoundException.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/whatsapp/WhatsappConversacionLecturaAdapter.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/whatsapp/WhatsappConversacionNotFoundException.java`
- `A infrastructure/src/main/java/com/ar/crm2/adapter/out/whatsapp/WhatsappMensajeLecturaAdapter.java`

### Seguridad y dependencias

- `M infrastructure/src/main/java/com/ar/crm2/security/KeycloakJwtActorContextMapper.java`
- `M infrastructure/pom.xml`

### Tests correspondientes inspeccionados

- `adapter/out/ai/**`: tests de holder, adapter, prompt mapper, response mapper, ChatClient adapter y embedding stub.
- `adapter/out/persistence/ai/**`: tests unitarios de adapters IA e integración JPA `AiRepositoryAdaptersIT`.
- `adapter/out/persistence/*`: tests de bridges de lectura CRM y find-by-id reutilizados por IA.
- `adapter/out/whatsapp/**`: tests de bridges de lectura y excepciones.
- `security/KeycloakJwtActorContextMapperTest.java`: tests del mapeo JWT identity-only.

## 2. Detalle por clase de producción

### 2.1 Spring AI adapter y contexto

#### `AiToolContextHolder`

- Responsabilidad: interfaz de infraestructura para exponer el `AiToolContext` confiable a las invocaciones `@Tool` ejecutadas en el mismo hilo.
- Métodos públicos:
  - `AiToolContext current()` — obtiene el contexto actual o `null`.
  - `void set(AiToolContext context)` — asocia contexto al hilo actual.
  - `void clear()` — elimina la asociación del hilo actual.
- Manejo de contexto: define el contrato mínimo para evitar que actor, tenant y conversación viajen dentro del payload del modelo.

#### `ThreadLocalAiToolContextHolder`

- Responsabilidad: implementación por defecto del holder con `ThreadLocal<AiToolContext>`.
- Métodos públicos:
  - `current()` — lee `ThreadLocal.get()`.
  - `set(AiToolContext)` — escribe `ThreadLocal.set(...)`.
  - `clear()` — ejecuta `ThreadLocal.remove()`.
- Manejo de contexto: aísla contexto por hilo y exige limpieza en `finally`; el propio comentario advierte que cualquier flujo async/reactive debe revisar esta estrategia.

#### `AiToolContextAdapter`

- Implementa: `AiToolContextPort`.
- Método público:
  - `AiToolContext resolve()` — lee `holder.current()` y devuelve el contexto confiable.
- Responsabilidad: puente outbound para que casos de uso como `ProponerAccionUseCase` resuelvan actor/tenant/conversación sin aceptar esos valores desde el modelo.
- Manejo de contexto: si no hay contexto asociado lanza `IllegalStateException`; esto bloquea propuestas no acotadas.

#### `OpenAiChatAdapter`

- Implementa: `GenerarChatAsistentePort`.
- Colaboradores: `ChatClient`, `List<Object> toolObjects`, `SpringAiPromptMapper`, `SpringAiChatResponseMapper`, `String modelId`, `AiToolContextHolder`.
- Método público:
  - `RespuestaAsistente generar(ChatAsistenteRequest solicitud)`.
- Responsabilidad:
  1. Construye el prompt de usuario mediante `SpringAiPromptMapper.toUserPrompt(...)`.
  2. Crea `AiToolContext` desde `ChatAsistenteRequest` (`actorUsuarioId`, `empresaId`, `aiConversacionId`, `waConversacionId`).
  3. Asocia el contexto al hilo antes de llamar `ChatClient.prompt()`.
  4. Invoca Spring AI: `.prompt().user(userText).tools(toolObjects.toArray()).call().chatResponse()`.
  5. Mapea errores de framework/proveedor a `AiAssistantException.upstreamFailure(...)`.
  6. Limpia el holder en `finally` tanto en éxito como en falla.
  7. Traduce `ChatResponse` a `RespuestaAsistente` mediante `SpringAiChatResponseMapper`.
  8. Registra log con conversación, actor, empresa, WhatsApp conversation, modelo, latencia y tokens.
- Límite hexagonal: Spring AI queda encapsulado en infraestructura; la aplicación recibe y devuelve DTOs propios.

#### `SpringAiPromptMapper`

- Método público:
  - `String toUserPrompt(ChatAsistenteRequest solicitud)`.
- Responsabilidad: transformar el request de aplicación a texto de prompt. Incluye siempre ids de scope y agrega secciones opcionales de facts, inferences, transcript y kickoff si están presentes.
- Métodos privados relevantes: `hasText(...)`, `appendTranscript(...)`.
- Límite: no importa tipos Spring AI; no valida reglas de negocio.

#### `SpringAiChatResponseMapper`

- Método público:
  - `RespuestaAsistente toResponse(ChatResponse response, long latencyMs)`.
- Responsabilidad: mapear respuesta Spring AI a DTO de aplicación.
- Validaciones: `ChatResponse`, `result`, `output` y `output.text` no pueden ser `null`; si lo son, lanza `AiAssistantException.upstreamFailure(...)`.
- Metadata: `model`, `promptTokens` y `completionTokens` se devuelven como `null` si Spring AI no los provee; no hay fallback silencioso.

#### `GenerarEmbeddingStubAdapter`

- Implementa: `GenerarEmbeddingPort`.
- Constante pública: `EMBEDDING_DIMENSION = 32`.
- Método público:
  - `List<Double> embed(String texto)`.
- Responsabilidad: stub determinístico de embeddings para fase 1. Usa SHA-256 sobre UTF-8, tolera `null` como texto vacío y produce 32 dimensiones no constantes.
- Método privado: `sha256(byte[] input)`.

### 2.2 Persistencia IA

#### `AiAccionRepositoryAdapter`

- Implementa: `SaveAiAccionPort`, `FindAiAccionPort`, `UpdateEstadoAccionPort`, `ListPendingAiAccionesPort`.
- Métodos públicos:
  - `AiAccion save(AiAccion accion)` — dominio → `AiAccionJpaEntity` → `repository.save(...)` → dominio.
  - `Optional<AiAccion> findById(UUID id)` — busca por id string y mapea a dominio.
  - `Optional<AiAccion> findById(AiAccionId id)` — overload de conveniencia.
  - `List<AiAccion> findPendingExpired(int limite)` — query `estado=PENDING` y `expiresAt < now`, paginada con mínimo 1.
  - `List<AiAccion> listPendingByActor(UUID actorUsuarioId, UUID empresaId, int limite)` — query tenant-scoped por `(solicitadaPor, estado=PENDING, empresaId)`.
- Query crítica: `findBySolicitadaPorAndEstadoAndEmpresaId(...)` es el trust boundary SQL para `GET /api/ai/acciones`.

#### `AiConversacionRepositoryAdapter`

- Implementa: `FindAiConversacionPort`, `ListAiConversacionesPort`.
- Métodos públicos:
  - `Optional<AiConversacion> findById(UUID id)`.
  - `Optional<AiConversacion> findById(AiConversacionId id)`.
  - `List<AiConversacion> listByActor(UUID actorUsuarioId, UUID empresaId, int limite)` — lista conversaciones del actor en tenant, ordenadas por `actualizadoEn DESC`, paginada con mínimo 1.
- Nota: la escritura está separada en `AiConversacionSaveAdapter` por conflicto de firmas `findById(UUID)` con distinto tipo de retorno en los ports.

#### `AiConversacionSaveAdapter`

- Implementa: `SaveAiConversacionPort`.
- Métodos públicos:
  - `AiConversacion save(AiConversacion conversacion)`.
  - `AiConversacion findById(UUID id)` — devuelve dominio o `null` si no existe, según contrato del port.

#### `AiMemoriaRepositoryAdapter`

- Implementa: `SaveAiMemoriaPort`, `FindAiMemoriaPort`, `DeleteAiMemoriaPort`.
- Métodos públicos:
  - `AiMemoria save(AiMemoria memoria)`.
  - `List<AiMemoria> findActivasByConversacionId(UUID waConversacionId, UUID actorUsuarioId, UUID empresaId)` — delega en `findActiveMemories(...)`.
  - `void delete(AiMemoria memoria)` — borra por id string.
- Query crítica: memoria activa por actor + empresa + WhatsApp conversation, excluyendo `superseded=true` y `expirada=true`.

#### `AiMensajeRepositoryAdapter`

- Implementa: `SaveAiMensajePort`, `FindAiMensajesByConversacionPort`.
- Métodos públicos:
  - `AiMensaje save(AiMensaje mensaje)`.
  - `List<AiMensaje> findByConversacionId(UUID aiConversacionId)` — transcript ordenado por `creadoEn ASC`.

#### `AiResumenContextoRepositoryAdapter`

- Implementa: `SaveAiResumenPort`, `FindAiResumenPort`.
- Métodos públicos:
  - `AiResumenContexto save(AiResumenContexto resumen)`.
  - `Optional<AiResumenContexto> findByConversacionId(UUID aiConversacionId)` — último resumen por `actualizadoEn DESC`.

#### Entidades JPA IA

- `AiAccionJpaEntity` → tabla `ai_accion`; campos de tenant, solicitante, WhatsApp conversation/message, AI conversation, tipo, estado, payload, rationale, version, expiración, resultado/error y auditoría temporal.
- `AiConversacionJpaEntity` → tabla `ai_conversacion`; actor, empresa, WhatsApp conversation, contacto opcional, archivado y timestamps.
- `AiMemoriaJpaEntity` → tabla `ai_memoria`; actor, empresa, WhatsApp conversation/contacto, visibilidad, contenido, origen, versión, expiración, superseded/expirada.
- `AiMensajeJpaEntity` → tabla `ai_mensaje`; AI conversation, rol, contenido, modelo, tokens, latencia, `tool_call_json` y timestamp.
- `AiResumenContextoJpaEntity` → tabla `ai_resumen_contexto`; facts, inferences, source watermark, source WhatsApp message y AI conversation.
- Responsabilidad común: proyección JPA sin lógica de negocio ni tipos Spring AI.

#### Mappers IA

Todos son `final` con constructor privado y métodos estáticos `toEntity(...)` / `toDomain(...)`:

- `AiAccionMapper` — conserva audit links `aiConversacionId` y `waMensajeId`; convierte UUID/VO ↔ string; reconstituye estado, expiración, resultado y error.
- `AiConversacionMapper` — mapea `contactoId` nullable y `archivada`.
- `AiMemoriaMapper` — mapea visibilidad, origen, superseded, expirada y `supersededBy` nullable.
- `AiMensajeMapper` — mapea tokens, modelo, latencia y `toolCallJson` opaco.
- `AiResumenContextoMapper` — mapea facts/inferences, source watermark y `aiConversacionId`.

#### Repositorios Spring Data IA

- `AiAccionSpringDataRepository`
  - `findByEstadoAndExpiresAtBefore(EstadoAccion estado, LocalDateTime cutoff, Pageable pageable)`.
  - `findBySolicitadaPorAndEstado(String solicitadaPor, EstadoAccion estado, Pageable pageable)`.
  - `findBySolicitadaPorAndEstadoAndEmpresaId(String solicitadaPor, EstadoAccion estado, String empresaId, Pageable pageable)`.
- `AiConversacionSpringDataRepository`
  - `findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(...)`.
  - `findByActorUsuarioIdAndWaConversacionId(...)` — declarada para lookup por conversación WhatsApp, aunque en este slice no aparece consumida por un adapter.
- `AiMemoriaSpringDataRepository`
  - `@Query findActiveMemories(actorUsuarioId, empresaId, waConversacionId)`.
- `AiMensajeSpringDataRepository`
  - `findByAiConversacionIdOrderByCreadoEnAsc(String aiConversacionId)`.
- `AiResumenContextoSpringDataRepository`
  - `findFirstByAiConversacionIdOrderByActualizadoEnDesc(String aiConversacionId)`.

### 2.3 Bridges de lectura CRM en adapters existentes

#### `ColumnaRepositoryAdapter`

- Ahora implementa también `ColumnaLecturaPort`.
- Métodos públicos relevantes:
  - `save`, `findAll`, `findById(ColumnaId)`, `deleteById`, `existsByColumnaId`.
  - `List<Columna> findByTipoTablero(TipoTablero tipoTablero)` — devuelve `List.of()` si el tipo es `null`; si no, delega en `ColumnaRepository.findByTipoTablero(tipoTablero.name())`.
- Responsabilidad IA: permitir que herramientas lean columnas válidas por tipo de tablero. El adapter sigue siendo tenant-blind; la autorización queda en aplicación.

#### `ContactoRepositoryAdapter`

- Ahora implementa también `ContactoLecturaPort`.
- Métodos públicos relevantes:
  - `save`, `findAll`, `findById(ContactoId)`, `deleteById`, `existsTratosByContactoId`.
  - `Optional<Contacto> findByEmpresaIdAndTelefono(EmpresaId empresaId, String telefono)` — valida `empresaId` no nulo y teléfono no blank; delega en `ContactoRepository.findByEmpresaIdAndTelefono(...)`.
- Responsabilidad IA: lookup de contacto por empresa + teléfono para herramientas de cliente.

#### `EmpresaRepositoryAdapter`

- Ahora implementa `FindEmpresasByCreadorPort`.
- Métodos públicos relevantes:
  - `save`, `findAll`, `findById(EmpresaId)`, `deleteById`, `existsTratosByEmpresaId`.
  - `List<EmpresaId> findEmpresasByCreador(UsuarioId creadoPor)` — delega en `EmpresaRepository.findByCreadoPor(...)` y devuelve proyección liviana de ids.
- Responsabilidad IA: resolver empresas del actor por `Empresa.creadoPor == usuarioId` para validar alcance tenant.

#### `FichaRepositoryAdapter`

- Ahora implementa también `FichaLecturaPort`.
- Métodos públicos relevantes: `save`, `findAll`, `findById(FichaId)`, `deleteById`, `existsFichasByColumnaId`.
- Responsabilidad IA: reutiliza `findById` como lectura de ficha; tenant enforcement queda fuera del adapter.

#### `TareaRepositoryAdapter` y `TratoRepositoryAdapter`

- Cambios principalmente documentales/arquitectónicos.
- Métodos públicos relevantes:
  - `TareaRepositoryAdapter`: `save`, `findAll`, `findById(TareaId)`, `deleteById`.
  - `TratoRepositoryAdapter`: `save`, `findAll`, `findById(TratoId)`, `deleteById`.
- Responsabilidad IA: los flujos de IA reutilizan los ports existentes `FindTareaByIdPort` y `FindTratoByIdPort`; no se introduce port IA separado.

#### Repositorios CRM modificados

- `ColumnaRepository`
  - Nuevo `List<ColumnaEntity> findByTipoTablero(String tipoTablero)` para listar columnas por catálogo/tipo de tablero.
- `EmpresaRepository`
  - Nuevo `List<EmpresaEntity> findByCreadoPor(String creadoPor)` para resolver ownership por creador.

### 2.4 Bridges de lectura WhatsApp

#### `WhatsappConversacionLecturaAdapter`

- Implementa: `WhatsappConversacionLecturaPort`.
- Método público:
  - `Optional<WhatsappConversacionResumen> findById(UUID waConversacionId)`.
- Responsabilidad: leer conversación WhatsApp y canal asociado para proyectar `waConversacionId`, `canalId`, `canalEmpresaId`, `contactoId`, teléfono y nombre.
- Flujo:
  1. `ConversacionRepositoryAdapter.findById(ConversacionId.from(...))`.
  2. `CanalWhatsappRepositoryAdapter.findById(conv.getCanalId())`.
  3. Mapeo a `WhatsappConversacionResumen`.
  4. Si falta conversación o canal, retorna `Optional.empty()`.
- Límite: bridge read-only; la autorización tenant la hace el servicio de aplicación consumidor.

#### `WhatsappMensajeLecturaAdapter`

- Implementa: `WhatsappMensajeLecturaPort`.
- Método público:
  - `List<WhatsappMensajeResumen> findByConversacionId(UUID waConversacionId)`.
- Responsabilidad: leer transcript WhatsApp por conversación y proyectar mensajes a DTOs de aplicación con dirección, tipo, contenido, media URL y fecha.

#### Excepciones de bridge

- `WhatsappConversacionNotFoundException`
  - `static forId(UUID waConversacionId)`.
  - `UUID waConversacionId()`.
- `WhatsappCanalNotFoundException`
  - `static forId(UUID canalId)`.
  - `UUID canalId()`.
- Responsabilidad: control de flujo interno para hacer explícitos missing rows; el adapter los traduce a `Optional.empty()`.

### 2.5 Seguridad: `KeycloakJwtActorContextMapper`

- Métodos públicos:
  - `ActorContext map(Jwt jwt)`.
  - `ActorContext map(Authentication authentication)`.
- Métodos privados relevantes:
  - `parseOptionalUuid(Jwt, String)` — claims UUID ausentes, blank o malformados → `Optional.empty()`.
  - `extractRoles(Jwt)` — extrae strings de `realm_access.roles` e ignora no strings.
- Responsabilidad: confinar tipos Spring Security/JWT en infraestructura y construir `ActorContext` de aplicación.
- Cambio clave: no extrae `empresa_id`; `ActorContext` queda identity-only (`sub`, username, email, `usuario_id`, `super_usuario_id`, roles). El tenant se resuelve por parámetro explícito o recurso propietario en aplicación.

### 2.6 `infrastructure/pom.xml`

- Agrega dependencia:
  - `org.springframework.ai:spring-ai-starter-model-openai`.
- La versión queda gestionada por el BOM de Spring AI del proyecto.
- Nota de consistencia: la propuesta original menciona Anthropic como modelo por defecto, pero este slice agrega OpenAI starter y el adapter se llama `OpenAiChatAdapter`. Si el cambio final debe ser Anthropic, hay que alinear nombre, dependency y properties antes de cerrar la especificación.

## 3. Implementación de ports y flujo de datos

### Chat outbound hacia Spring AI

1. La aplicación arma `ChatAsistenteRequest` con scope ya resuelto por el servicio (`actorUsuarioId`, `empresaId`, `aiConversacionId`, `waConversacionId`).
2. `OpenAiChatAdapter.generar(...)` transforma ese request en prompt textual.
3. Antes de llamar a Spring AI, crea `AiToolContext` desde el request confiable y lo escribe en `AiToolContextHolder`.
4. `ChatClient` recibe el prompt y los objetos `@Tool` registrados.
5. Si Spring AI invoca herramientas sincrónicamente, esas herramientas pueden resolver scope mediante `AiToolContextAdapter.resolve()`.
6. La respuesta `ChatResponse` se mapea a `RespuestaAsistente` y vuelve a aplicación.
7. El contexto se limpia siempre en `finally` para evitar contaminación entre requests en hilos reutilizados.

### Persistencia IA

- Los casos de uso de aplicación dependen de ports granulares (`SaveAiAccionPort`, `FindAiConversacionPort`, `FindAiMemoriaPort`, etc.).
- Los adapters de infraestructura hacen conversión dominio ↔ JPA mediante mappers estáticos, y delegan a Spring Data repositories.
- La mayoría de adapters son tenant-blind y confían en que aplicación valide alcance; excepción importante: `AiAccionRepositoryAdapter.listPendingByActor(...)` también filtra por `empresaId` en SQL, reforzando el trust boundary de `GET /api/ai/acciones`.
- Las entidades JPA no incorporan lógica de negocio ni tipos Spring AI.

### Bridges CRM y WhatsApp

- CRM: se reutilizan adapters existentes cuando la firma del port coincide (`findById`). Se agregan métodos específicos para lecturas IA: columnas por tipo de tablero, contacto por empresa/teléfono y empresas por creador.
- WhatsApp: infraestructura importa el módulo `whatsapp`, carga entidades de dominio WhatsApp y las proyecta a DTOs/ports de aplicación. Esto evita que `application` importe directamente `whatsapp`.
- En ambos casos, los bridges son read-only. Las mutaciones siguen fuera de estos adapters y deben pasar por propuestas `AiAccion` + confirmación humana.

### Seguridad

- JWT validado por Spring Security entra a `KeycloakJwtActorContextMapper`.
- El mapper construye `ActorContext` identity-only.
- La resolución tenant queda desplazada a servicios de aplicación con dos fuentes: selector explícito (`empresaId`) o tenant derivado del recurso cargado.

## 4. Resumen de cobertura de tests inspeccionada

No se ejecutaron tests. La cobertura siguiente se infiere de los archivos de test leídos.

### Spring AI / contexto

- `ThreadLocalAiToolContextHolderTest`: cubre estado inicial vacío, `set`, `clear`, sobrescritura e aislamiento entre hilos.
- `AiToolContextAdapterTest`: cubre resolución del contexto, error si holder vacío, no cachear valores, implementación del port y constructor mínimo.
- `OpenAiChatAdapterTest`: cubre delegación a prompt/response mappers, registro de herramientas, errores del `ChatClient`, propagación de errores del mapper, binding del contexto antes de `ChatClient.prompt()`, limpieza en éxito y falla, construcción del contexto desde `ChatAsistenteRequest`, firma del constructor y guardas arquitectónicas de imports/helpers.
- `SpringAiPromptMapperTest`: cubre render de ids de scope, facts, inferences, transcript, kickoff, omisiones por null/blank, prompt no vacío y ausencia de Spring AI imports.
- `SpringAiChatResponseMapperTest`: cubre happy path, nulls estructurales, metadata ausente, latencia y guardas de dependencias.
- `GenerarEmbeddingStubAdapterTest`: cubre tamaño estable, determinismo, distinción de entradas, vector no cero, unicode, input vacío, baja colisión básica, stateless e implementación del port.

### Persistencia IA

- Tests unitarios de adapters: cubren save/find/list/delete, conversiones dominio ↔ JPA, resultados vacíos, límites mínimos de paginación, audit links de `AiAccion`, resumen más reciente y query tenant-scoped de acciones pendientes.
- `AiRepositoryAdaptersIT`: cubre round-trip JPA de conversación, mensaje, resumen, memoria y acción; orden fresco de conversaciones; memoria activa no superseded/no expirada; delete de memoria; acciones expiradas; wiring de repositorios Spring Data; y filtro `(actor, PENDING, empresaId)` para inbox de acciones.

### Bridges CRM existentes

- `ColumnaRepositoryAdapterAiReadPortTest`, `ContactoRepositoryAdapterAiReadPortTest`, `FichaRepositoryAdapterAiReadPortTest`: cubren `findById` a través de ports IA y miss del repositorio.
- `EmpresaRepositoryAdapterBridgeTest`: cubre `findEmpresasByCreador(...)` y resultado vacío.
- `TareaRepositoryAdapterFindByIdTest`, `TratoRepositoryAdapterFindByIdTest`: cubren reutilización de `FindTareaByIdPort` / `FindTratoByIdPort` para resultado presente y vacío.

### WhatsApp read bridges

- `WhatsappConversacionLecturaAdapterTest`: cubre proyección conversación+canal, conversación faltante y canal huérfano → `Optional.empty()`.
- `WhatsappMensajeLecturaAdapterTest`: cubre mapeo de mensajes a proyección de aplicación y lista vacía.
- `WhatsappReadBridgeExceptionsTest`: cubre factories, ids y mensajes de excepciones internas.

### Seguridad

- `KeycloakJwtActorContextMapperTest`: cubre claims completos, delegación desde `Authentication`, principal no JWT, claims UUID ausentes/blank/malformados, email ausente, roles, no roles, roles no string y `hasRole` en `ActorContext`.

## 5. Riesgos y gaps del slice

1. **Desalineación Anthropic/OpenAI**: la propuesta habla de Anthropic, pero este slice agrega `spring-ai-starter-model-openai`, `OpenAiChatAdapter` y properties OpenAI en comentarios. Debe resolverse antes del cierre del cambio.
2. **`AiRepositoryAdaptersIT` puede no ejecutarse en `mvn verify`**: `infrastructure/pom.xml` tiene `maven-failsafe-plugin` con includes explícitos y no incluye `**/AiRepositoryAdaptersIT.java`. Bajo esa configuración, la integración JPA IA puede quedar fuera del verify estándar.
3. **Cobertura directa incompleta en nuevos métodos CRM bridge**: no se encontró test directo del adapter para `ContactoRepositoryAdapter.findByEmpresaIdAndTelefono(...)` ni para `ColumnaRepositoryAdapter.findByTipoTablero(...)`; los tests de herramientas encontrados mockean los ports, no prueban estas implementaciones.
4. **Memoria activa no verifica TTL por fecha en query**: `findActiveMemories(...)` filtra `superseded=false` y `expirada=false`, pero no `expiresAt > now`. Si no hay proceso que marque `expirada`, memoria vencida podría seguir retornando.
5. **ThreadLocal depende de invocación sincrónica de tools**: está documentado y testeado, pero cualquier cambio a ejecución async/reactive de Spring AI invalidaría el mecanismo de contexto.
6. **Adapters CRM son tenant-blind**: es coherente con el diseño, pero exige que todos los consumidores de aplicación hagan validación tenant antes/después de llamar al port. Un uso directo incorrecto podría leer datos fuera de alcance.
7. **`WhatsappConversacionLecturaAdapter` oculta canal huérfano como empty**: cumple el contrato del port, pero puede dificultar observabilidad de datos huérfanos si no se registra por otra vía.
8. **Prompt actual es `.user(...)` solamente**: `SpringAiPromptMapper` serializa scope y transcript en texto de usuario; en este slice no se observa uso de `.system(...)` ni estructura explícita de mensajes históricos. Puede quedar corto frente al requisito de system prompt + history cronológico si no está cubierto en otra capa.
9. **Query declarada no consumida**: `AiConversacionSpringDataRepository.findByActorUsuarioIdAndWaConversacionId(...)` aparece declarada, pero no se observa uso desde los adapters de este slice. Puede ser preparación futura o deuda de integración.

## 6. Conclusión

El slice implementa la frontera outbound de IA con una separación hexagonal razonable: Spring AI queda encapsulado, el contexto confiable de tools no viaja por payload del modelo, la persistencia IA usa ports granulares y mappers explícitos, y los bridges CRM/WhatsApp se mantienen read-only. Los principales puntos a corregir antes de cerrar son la alineación OpenAI/Anthropic, asegurar que los IT de persistencia IA entren en `verify`, y cubrir directamente los nuevos métodos de lectura CRM agregados para herramientas.
