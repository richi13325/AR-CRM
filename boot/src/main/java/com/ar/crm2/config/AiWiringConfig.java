package com.ar.crm2.config;

import com.ar.crm2.adapter.in.tool.ai.BuscarClientePorTelefonoTool;
import com.ar.crm2.adapter.in.tool.ai.ListarColumnasTableroTool;
import com.ar.crm2.adapter.in.tool.ai.ObtenerMensajesRecientesTool;
import com.ar.crm2.adapter.in.tool.ai.ObtenerResumenChatTool;
import com.ar.crm2.adapter.in.tool.ai.ProponerAccionTool;
import com.ar.crm2.adapter.out.ai.ThreadLocalAiToolContextHolder;
import com.ar.crm2.adapter.out.ai.spring.OpenAiChatAdapter;
import com.ar.crm2.adapter.out.ai.spring.mapper.SpringAiChatResponseMapper;
import com.ar.crm2.adapter.out.ai.spring.mapper.SpringAiPromptMapper;
import com.ar.crm2.adapter.out.persistence.ai.AiAccionRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiAccionSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiConversacionSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMemoriaSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMensajeSpringDataRepository;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiResumenContextoSpringDataRepository;
import com.ar.crm2.adapter.out.whatsapp.WhatsappConversacionLecturaAdapter;
import com.ar.crm2.adapter.out.whatsapp.WhatsappMensajeLecturaAdapter;
import com.ar.crm2.application.ai.port.in.AnalizarChatUseCase;
import com.ar.crm2.application.ai.port.in.ConfirmarAccionUseCase;
import com.ar.crm2.application.ai.port.in.ExpirarAccionUseCase;
import com.ar.crm2.application.ai.port.in.ListarAccionesPendientesUseCase;
import com.ar.crm2.application.ai.port.in.ListarConversacionesAsistenteUseCase;
import com.ar.crm2.application.ai.port.in.ObtenerAccionUseCase;
import com.ar.crm2.application.ai.port.in.ObtenerConversacionAsistenteUseCase;
import com.ar.crm2.application.ai.port.in.ProponerAccionUseCase;
import com.ar.crm2.application.ai.port.in.RechazarAccionUseCase;
import com.ar.crm2.application.ai.port.in.RegistrarAccionUseCase;
import com.ar.crm2.application.ai.port.in.RegistrarMensajeAsistenteUseCase;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.ColumnaLecturaPort;
import com.ar.crm2.application.ai.port.out.ContactoLecturaPort;
import com.ar.crm2.application.ai.port.out.FichaLecturaPort;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.ai.port.out.FindAiConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiMemoriaPort;
import com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.GenerarChatAsistentePort;
import com.ar.crm2.application.ai.port.out.GenerarEmbeddingPort;
import com.ar.crm2.application.ai.port.out.ListAiConversacionesPort;
import com.ar.crm2.application.ai.port.out.ListPendingAiAccionesPort;
import com.ar.crm2.application.ai.port.out.SaveAiAccionPort;
import com.ar.crm2.application.ai.port.out.SaveAiConversacionPort;
import com.ar.crm2.application.ai.port.out.SaveAiMensajePort;
import com.ar.crm2.application.ai.port.out.SaveAiResumenPort;
import com.ar.crm2.application.ai.port.out.UpdateEstadoAccionPort;
import com.ar.crm2.application.ai.port.out.WhatsappConversacionLecturaPort;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.application.ai.service.AnalizarChatService;
import com.ar.crm2.application.ai.service.ConfirmarAccionService;
import com.ar.crm2.application.ai.service.ExpirarAccionService;
import com.ar.crm2.application.ai.service.ListarAccionesPendientesService;
import com.ar.crm2.application.ai.service.ListarConversacionesAsistenteService;
import com.ar.crm2.application.ai.service.ObtenerAccionService;
import com.ar.crm2.application.ai.service.ObtenerConversacionAsistenteService;
import com.ar.crm2.application.ai.service.ProponerAccionService;
import com.ar.crm2.application.ai.service.RechazarAccionService;
import com.ar.crm2.application.ai.service.RegistrarAccionService;
import com.ar.crm2.application.ai.service.RegistrarMensajeAsistenteService;
import com.ar.crm2.application.ai.service.SaveAiAccionPortBridge;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * AI assistant wiring configuration. Lives alongside {@link WiringConfig}
 * but is gated by the master kill-switch
 * {@code ai-assistant.enabled}. When the flag is {@code false}
 * (the default), no AI bean is created — the AI surface is fully
 * absent from the runtime context. When the flag is {@code true},
 * every AI service, repository adapter, Spring AI tool, and the
 * {@link ChatClient} is registered.
 *
 * <p><b>Why a separate {@code @Configuration} class?</b>
 * <ul>
 *   <li>Module-loading isolation: boot-wiring tests
 *       ({@code FichaWiringTest}, etc.) can load only
 *       {@link WiringConfig} and never trigger AI bean
 *       instantiation, eliminating PR4-scope
 *       {@code ApplicationContext} failures caused by
 *       un-mocked AI repository dependencies.</li>
 *   <li>Feature-flag faithfulness: {@code ai-assistant.enabled=false}
 *       is no longer dead config — turning the flag off
 *       removes the entire AI surface (controller, services,
 *       tools, ChatClient, JPA repositories) from the
 *       runtime graph.</li>
 *   <li>Reversibility: rollback is a single property flip
 *       rather than a code revert.</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(name = "ai-assistant.enabled", havingValue = "true", matchIfMissing = false)
public class AiWiringConfig {

    // ── AI: Tool Context Holder + Output Adapter ─────────────────

    @Bean
    public ThreadLocalAiToolContextHolder aiToolContextHolder() {
        return new ThreadLocalAiToolContextHolder();
    }

    @Bean
    public com.ar.crm2.adapter.out.ai.AiToolContextAdapter aiToolContextAdapter(
            ThreadLocalAiToolContextHolder holder
    ) {
        return new com.ar.crm2.adapter.out.ai.AiToolContextAdapter(holder);
    }

    // ── AI: Per-Turn Safety Limit Policies (Phase B) ──────────────
    //
    // The configured limits were defined as application.yml keys but
    // never read by any Java code before Phase B. They are wired here
    // as injectable policy collaborators so:
    //   1. The OpenAiChatAdapter can consult them at the boundary it
    //      controls (assertToolCallBudget, getTurnTimeoutMillis).
    //   2. Tests can substitute hand-rolled policies to verify the
    //      contract without booting a Spring context.
    //
    // Spring AI 2.0 does NOT expose a built-in per-turn tool-call
    // budget knob; real production enforcement therefore requires a
    // custom CallAdvisor injected into the ChatClient chain (see the
    // design decision memo in apply-progress.md). The current wiring
    // exposes the policy as a deterministic test seam.

    @Bean
    public com.ar.crm2.adapter.out.ai.policy.ToolCallBudgetPolicy toolCallBudgetPolicy(
            @Value("${ai-assistant.max-tool-calls-per-turn:5}") int maxToolCallsPerTurn
    ) {
        return new com.ar.crm2.adapter.out.ai.policy.ConfiguredToolCallBudgetPolicy(
            maxToolCallsPerTurn
        );
    }

    @Bean
    public com.ar.crm2.adapter.out.ai.policy.TurnTimeoutPolicy turnTimeoutPolicy(
            @Value("${ai-assistant.turn-timeout-ms:25000}") long turnTimeoutMs
    ) {
        return new com.ar.crm2.adapter.out.ai.policy.ConfiguredTurnTimeoutPolicy(
            turnTimeoutMs
        );
    }

    // ── AI: Embedding Stub (PR 3 — phase-1 deterministic placeholder) ──

    @Bean
    public GenerarEmbeddingPort generarEmbeddingPort() {
        return new com.ar.crm2.adapter.out.ai.spring.GenerarEmbeddingStubAdapter();
    }

    // ── AI: Tool Input Adapters (PR 3 — read-only @Tool beans) ────────

    @Bean
    public ObtenerMensajesRecientesTool obtenerMensajesRecientesTool(
            AiToolContextPort aiToolContextPort,
            WhatsappMensajeLecturaPort whatsappMensajeLecturaPort
    ) {
        return new ObtenerMensajesRecientesTool(aiToolContextPort, whatsappMensajeLecturaPort);
    }

    @Bean
    public ObtenerResumenChatTool obtenerResumenChatTool(
            AiToolContextPort aiToolContextPort,
            FindAiResumenPort findAiResumenPort
    ) {
        return new ObtenerResumenChatTool(aiToolContextPort, findAiResumenPort);
    }

    @Bean
    public BuscarClientePorTelefonoTool buscarClientePorTelefonoTool(
            AiToolContextPort aiToolContextPort,
            ContactoLecturaPort contactoLecturaPort
    ) {
        return new BuscarClientePorTelefonoTool(aiToolContextPort, contactoLecturaPort);
    }

    @Bean
    public ListarColumnasTableroTool listarColumnasTableroTool(
            ColumnaLecturaPort columnaLecturaPort
    ) {
        return new ListarColumnasTableroTool(columnaLecturaPort);
    }

    // ── AI: Propose-only tool (PR 3) ─────────────────────────────

    @Bean
    public ProponerAccionTool proponerAccionTool(ProponerAccionUseCase proponerAccionUseCase) {
        return new ProponerAccionTool(proponerAccionUseCase);
    }

    // ── AI: Tenant Scope (Empresa-owned port, architecture correction) ──

    /**
     * Empresa-owned tenant-scope service, exposed as the concrete
     * class. AI services depend on the
     * {@link ActorEmpresaScopePort} interface, so Spring injects the
     * same bean by interface at every AI service constructor site.
     *
     * <p>The previous thin resolver layer was removed: it only delegated
     * to this service and translated the neutral domain exception into
     * the AI-public one. The
     * translation now lives at the AI service call site via
     * {@code AiTenantExceptionTranslator}, so the Empresa port stays
     * neutral and no AI-specific resolver service is needed.
     */
    @Bean
    public ActorEmpresaScopeService actorEmpresaScopeService(
            com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort findEmpresasByCreadorPort
    ) {
        return new ActorEmpresaScopeService(findEmpresasByCreadorPort);
    }

    // ── AI: Persistence Adapter Beans (PR 2) ────────────────────────

    @Bean
    public AiAccionRepositoryAdapter aiAccionRepositoryAdapter(
            AiAccionSpringDataRepository repository
    ) {
        return new AiAccionRepositoryAdapter(repository);
    }

    @Bean
    public com.ar.crm2.adapter.out.persistence.ai.AiConversacionRepositoryAdapter aiConversacionRepositoryAdapter(
            AiConversacionSpringDataRepository repository
    ) {
        return new com.ar.crm2.adapter.out.persistence.ai.AiConversacionRepositoryAdapter(repository);
    }

    @Bean
    public com.ar.crm2.adapter.out.persistence.ai.AiConversacionSaveAdapter aiConversacionSaveAdapter(
            AiConversacionSpringDataRepository repository
    ) {
        return new com.ar.crm2.adapter.out.persistence.ai.AiConversacionSaveAdapter(repository);
    }

    @Bean
    public com.ar.crm2.adapter.out.persistence.ai.AiMensajeRepositoryAdapter aiMensajeRepositoryAdapter(
            AiMensajeSpringDataRepository repository
    ) {
        return new com.ar.crm2.adapter.out.persistence.ai.AiMensajeRepositoryAdapter(repository);
    }

    @Bean
    public com.ar.crm2.adapter.out.persistence.ai.AiResumenContextoRepositoryAdapter aiResumenContextoRepositoryAdapter(
            AiResumenContextoSpringDataRepository repository
    ) {
        return new com.ar.crm2.adapter.out.persistence.ai.AiResumenContextoRepositoryAdapter(repository);
    }

    @Bean
    public com.ar.crm2.adapter.out.persistence.ai.AiMemoriaRepositoryAdapter aiMemoriaRepositoryAdapter(
            AiMemoriaSpringDataRepository repository,
            @Value("${ai-assistant.phase1.memory-writes-enabled:false}") boolean memoryWritesEnabled
    ) {
        return new com.ar.crm2.adapter.out.persistence.ai.AiMemoriaRepositoryAdapter(
            repository,
            memoryWritesEnabled
        );
    }

    // ── AI: Read Ports Bridged from Existing Repositories (PR 2) ────

    @Bean(name = "aiContactoLecturaPort")
    public ContactoLecturaPort aiContactoLecturaPort(
            com.ar.crm2.adapter.out.persistence.ContactoRepositoryAdapter delegate
    ) {
        return delegate;
    }

    @Bean(name = "aiFichaLecturaPort")
    public FichaLecturaPort aiFichaLecturaPort(
            com.ar.crm2.adapter.out.persistence.FichaRepositoryAdapter delegate
    ) {
        return delegate;
    }

    @Bean(name = "aiColumnaLecturaPort")
    public ColumnaLecturaPort aiColumnaLecturaPort(
            com.ar.crm2.adapter.out.persistence.ColumnaRepositoryAdapter delegate
    ) {
        return delegate;
    }

    // ── AI: WhatsApp Read Ports Bridged from Existing Repositories (PR 4) ──

    @Bean
    public WhatsappConversacionLecturaAdapter whatsappConversacionLecturaAdapter(
            com.ar.crm2.adapter.out.persistence.ConversacionRepositoryAdapter conversacionAdapter,
            com.ar.crm2.adapter.out.persistence.CanalWhatsappRepositoryAdapter canalAdapter
    ) {
        return new WhatsappConversacionLecturaAdapter(conversacionAdapter, canalAdapter);
    }

    @Bean
    public WhatsappConversacionLecturaPort whatsappConversacionLecturaPort(
            WhatsappConversacionLecturaAdapter adapter
    ) {
        return adapter;
    }

    @Bean
    public WhatsappMensajeLecturaAdapter whatsappMensajeLecturaAdapter(
            com.ar.crm2.adapter.out.persistence.MensajeRepositoryAdapter mensajeAdapter
    ) {
        return new WhatsappMensajeLecturaAdapter(mensajeAdapter);
    }

    @Bean
    public WhatsappMensajeLecturaPort whatsappMensajeLecturaPort(
            WhatsappMensajeLecturaAdapter adapter
    ) {
        return adapter;
    }

    // ── AI: ChatClient (Spring AI) + OpenAiChatAdapter (PR 4) ────────

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public OpenAiChatAdapter openAiChatAdapter(
            ChatClient chatClient,
            BuscarClientePorTelefonoTool buscarClientePorTelefonoTool,
            ListarColumnasTableroTool listarColumnasTableroTool,
            ObtenerMensajesRecientesTool obtenerMensajesRecientesTool,
            ObtenerResumenChatTool obtenerResumenChatTool,
            ProponerAccionTool proponerAccionTool,
            SpringAiPromptMapper promptMapper,
            SpringAiChatResponseMapper responseMapper,
            ThreadLocalAiToolContextHolder toolContextHolder,
            com.ar.crm2.adapter.out.ai.policy.ToolCallBudgetPolicy toolCallBudgetPolicy,
            com.ar.crm2.adapter.out.ai.policy.TurnTimeoutPolicy turnTimeoutPolicy,
            @Value("${spring.ai.openai.chat.options.model:claude-haiku-4-5-20251001}") String modelId
    ) {
        List<Object> toolObjects = List.of(
            buscarClientePorTelefonoTool,
            listarColumnasTableroTool,
            obtenerMensajesRecientesTool,
            obtenerResumenChatTool,
            proponerAccionTool
        );
        return new OpenAiChatAdapter(
            chatClient, toolObjects, promptMapper, responseMapper, modelId,
            toolContextHolder, toolCallBudgetPolicy, turnTimeoutPolicy
        );
    }

    // ── AI: Application Service Beans (PR 1 + PR 2 + PR 4 corrective) ──

    @Bean
    public RegistrarAccionService registrarAccionService(SaveAiAccionPort saveAiAccionPort) {
        return new RegistrarAccionService(saveAiAccionPort);
    }

    @Bean
    public RegistrarAccionUseCase registrarAccionUseCase(RegistrarAccionService service) {
        return service;
    }

    @Bean
    public SaveAiAccionPortBridge saveAiAccionPortBridge(SaveAiAccionPort port) {
        return new SaveAiAccionPortBridge(port);
    }

    @Bean
    public ProponerAccionService proponerAccionService(
            AiToolContextPort contextResolver,
            RegistrarAccionUseCase registrarAccionUseCase
    ) {
        return new ProponerAccionService(contextResolver, registrarAccionUseCase);
    }

    @Bean
    public ProponerAccionUseCase proponerAccionUseCase(ProponerAccionService service) {
        return service;
    }

    @Bean
    public AnalizarChatService analizarChatService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            WhatsappConversacionLecturaPort whatsappConversacionPort,
            WhatsappMensajeLecturaPort whatsappMensajePort,
            FindAiConversacionPort findAiConversacionPort,
            FindAiResumenPort findAiResumenPort,
            FindAiMemoriaPort findAiMemoriaPort,
            FindAiMensajesByConversacionPort findAiMensajesPort,
            SaveAiConversacionPort saveAiConversacionPort,
            SaveAiMensajePort saveAiMensajePort,
            SaveAiResumenPort saveAiResumenPort,
            GenerarChatAsistentePort generarChatPort
    ) {
        return new AnalizarChatService(
            actorEmpresaScopePort, whatsappConversacionPort, whatsappMensajePort,
            findAiConversacionPort, findAiResumenPort, findAiMemoriaPort, findAiMensajesPort,
            saveAiConversacionPort, saveAiMensajePort, saveAiResumenPort, generarChatPort
        );
    }

    @Bean
    public AnalizarChatUseCase analizarChatUseCase(AnalizarChatService service) {
        return service;
    }

    @Bean
    public RegistrarMensajeAsistenteService registrarMensajeAsistenteService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            WhatsappMensajeLecturaPort whatsappMensajePort,
            FindAiConversacionPort findAiConversacionPort,
            FindAiMensajesByConversacionPort findAiMensajesPort,
            FindAiResumenPort findAiResumenPort,
            FindAiMemoriaPort findAiMemoriaPort,
            SaveAiMensajePort saveAiMensajePort,
            SaveAiResumenPort saveAiResumenPort,
            GenerarChatAsistentePort generarChatPort
    ) {
        return new RegistrarMensajeAsistenteService(
            actorEmpresaScopePort, whatsappMensajePort,
            findAiConversacionPort, findAiMensajesPort, findAiResumenPort, findAiMemoriaPort,
            saveAiMensajePort, saveAiResumenPort, generarChatPort
        );
    }

    @Bean
    public RegistrarMensajeAsistenteUseCase registrarMensajeAsistenteUseCase(
            RegistrarMensajeAsistenteService service
    ) {
        return service;
    }

    @Bean
    public ObtenerConversacionAsistenteService obtenerConversacionAsistenteService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            FindAiConversacionPort findAiConversacionPort,
            FindAiMensajesByConversacionPort findAiMensajesPort
    ) {
        return new ObtenerConversacionAsistenteService(
            actorEmpresaScopePort, findAiConversacionPort, findAiMensajesPort
        );
    }

    @Bean
    public ObtenerConversacionAsistenteUseCase obtenerConversacionAsistenteUseCase(
            ObtenerConversacionAsistenteService service
    ) {
        return service;
    }

    @Bean
    public ListarConversacionesAsistenteService listarConversacionesAsistenteService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            ListAiConversacionesPort listAiConversacionesPort
    ) {
        return new ListarConversacionesAsistenteService(actorEmpresaScopePort, listAiConversacionesPort);
    }

    @Bean
    public ListarConversacionesAsistenteUseCase listarConversacionesAsistenteUseCase(
            ListarConversacionesAsistenteService service
    ) {
        return service;
    }

    @Bean
    public ListarAccionesPendientesService listarAccionesPendientesService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            ListPendingAiAccionesPort listPendingAiAccionesPort
    ) {
        return new ListarAccionesPendientesService(actorEmpresaScopePort, listPendingAiAccionesPort);
    }

    @Bean
    public ListarAccionesPendientesUseCase listarAccionesPendientesUseCase(
            ListarAccionesPendientesService service
    ) {
        return service;
    }

    @Bean
    public ObtenerAccionService obtenerAccionService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            FindAiAccionPort findAiAccionPort
    ) {
        return new ObtenerAccionService(actorEmpresaScopePort, findAiAccionPort);
    }

    @Bean
    public ObtenerAccionUseCase obtenerAccionUseCase(ObtenerAccionService service) {
        return service;
    }

    @Bean
    public ConfirmarAccionService confirmarAccionService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            FindAiAccionPort findAiAccionPort,
            SaveAiAccionPortBridge savePort,
            CreateContactoUseCase createContactoUseCase,
            CreateTratoUseCase createTratoUseCase,
            CreateTareaUseCase createTareaUseCase,
            MoverColumnaFichaUseCase moverColumnaFichaUseCase
    ) {
        return new ConfirmarAccionService(
            actorEmpresaScopePort, findAiAccionPort, savePort,
            createContactoUseCase, createTratoUseCase, createTareaUseCase, moverColumnaFichaUseCase
        );
    }

    @Bean
    public ConfirmarAccionUseCase confirmarAccionUseCase(ConfirmarAccionService service) {
        return service;
    }

    @Bean
    public RechazarAccionService rechazarAccionService(
            ActorEmpresaScopePort actorEmpresaScopePort,
            FindAiAccionPort findAiAccionPort,
            SaveAiAccionPortBridge savePort
    ) {
        return new RechazarAccionService(actorEmpresaScopePort, findAiAccionPort, savePort);
    }

    @Bean
    public RechazarAccionUseCase rechazarAccionUseCase(RechazarAccionService service) {
        return service;
    }

    @Bean
    public ExpirarAccionService expirarAccionService(UpdateEstadoAccionPort updateEstadoAccionPort) {
        return new ExpirarAccionService(updateEstadoAccionPort);
    }

    @Bean
    public ExpirarAccionUseCase expirarAccionUseCase(ExpirarAccionService service) {
        return service;
    }
}
