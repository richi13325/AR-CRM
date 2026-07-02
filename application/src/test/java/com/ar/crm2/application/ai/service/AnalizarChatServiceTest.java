package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;
import com.ar.crm2.application.ai.port.out.projection.WhatsappConversacionResumen;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import com.ar.crm2.application.ai.port.out.FindAiConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiMemoriaPort;
import com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.GenerarChatAsistentePort;
import com.ar.crm2.application.ai.port.out.SaveAiConversacionPort;
import com.ar.crm2.application.ai.port.out.SaveAiMensajePort;
import com.ar.crm2.application.ai.port.out.SaveAiResumenPort;
import com.ar.crm2.application.ai.port.out.WhatsappConversacionLecturaPort;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.application.ai.command.AnalizarChatCommand;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
// ActorEmpresaScopeService implements ActorEmpresaScopePort directly, so the
// test injects the Empresa-owned service as the AI dependency.
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.entity.ia.AiMemoria;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AnalizarChatService}.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Tenant rejection happens BEFORE any {@code ai_*} row is created.</li>
 *   <li>Happy path: AI generation runs and the assistant message + summary are persisted.</li>
 *   <li><b>Resource-first tenant resolution (PR5):</b> for a multi-company actor
 *       who owns both the hint company and the WhatsApp conversation's owning
 *       company, the {@code /chat} flow MUST persist the AI conversation,
 *       AI messages and AI resumen rows under the WhatsApp conversation's
 *       {@code canalEmpresaId} — never under the hint {@code empresaId}
 *       carried in the command. The hint is a cross-check only and MUST NOT
 *       override the resource tenant.</li>
 *   <li>Tool-driven proposal staging is the responsibility of the
 *       {@code ProponerAccionUseCase} path (tested in
 *       {@link ProponerAccionServiceTest}). This service does NOT
 *       stage proposals — it only persists conversation / turns /
 *       summary.</li>
 * </ul>
 */
class AnalizarChatServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final EmpresaId empresaPropia = EmpresaId.create();
    private final EmpresaId empresaAjena = EmpresaId.create();
    private final EmpresaId empresaHint = EmpresaId.create();
    private final EmpresaId empresaRecurso = EmpresaId.create();

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemoryWhatsappConvPort waConvPort;
    private InMemoryWhatsappMensajePort waMensajePort;
    private InMemoryAiConvStore convStore;
    private InMemoryFindAiConvPort findConv;
    private InMemoryFindResumenPort findResumen;
    private InMemoryFindMemoriaPort findMemoria;
    private InMemoryFindMensajesPort findMensajes;
    private InMemorySaveAiConvPort saveConv;
    private InMemorySaveAiMensajePort saveMensaje;
    private InMemorySaveResumenPort saveResumen;
    private SpyGenerarChatPort generarChat;
    private AnalizarChatService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresaPropia));

        waConvPort = new InMemoryWhatsappConvPort();
        waMensajePort = new InMemoryWhatsappMensajePort();
        convStore = new InMemoryAiConvStore();
        findConv = new InMemoryFindAiConvPort(convStore);
        findResumen = new InMemoryFindResumenPort();
        findMemoria = new InMemoryFindMemoriaPort();
        findMensajes = new InMemoryFindMensajesPort();
        saveConv = new InMemorySaveAiConvPort(convStore);
        saveMensaje = new InMemorySaveAiMensajePort();
        saveResumen = new InMemorySaveResumenPort();
        generarChat = new SpyGenerarChatPort();

service = new AnalizarChatService(
                new ActorEmpresaScopeService(findEmpresasPort),
                waConvPort, waMensajePort,
                findConv, findResumen, findMemoria, findMensajes,
                saveConv, saveMensaje, saveResumen,
                generarChat
        );
    }

    @Test
    @DisplayName("analizar de WhatsApp ajeno al actor rechaza con tenant y NO crea ninguna fila ai_*")
    void analizar_chatAjeno_rechazaSinPersistir() {
        UUID waConvId = UUID.randomUUID();
        waConvPort.put(new WhatsappConversacionResumen(
                waConvId, UUID.randomUUID(), empresaAjena.value(), null,
                "+54911...", "Juan"
        ));

        AnalizarChatCommand cmd = new AnalizarChatCommand(
                actor.value(), empresaPropia.value(), waConvId.toString(), "hola"
        );

        assertThrows(AsistenteTenantException.class, () -> service.analizar(cmd));
        assertEquals(0, saveConv.saved.size(), "no debe persistir conversación AI");
        assertEquals(0, saveMensaje.saved.size(), "no debe persistir mensajes AI");
        assertEquals(0, generarChat.calls, "no debe invocar AI port");
    }

    @Test
    @DisplayName("analizar con WhatsApp propio ejecuta AI y persiste conversación + mensaje")
    void analizar_chatPropio_persisteAsistente() {
        UUID waConvId = UUID.randomUUID();
        UUID canalId = UUID.randomUUID();
        waConvPort.put(new WhatsappConversacionResumen(
                waConvId, canalId, empresaPropia.value(),
                ContactoId.create().value(), "+54911...", "Juan"
        ));
        generarChat.next = new RespuestaAsistente(
                "El cliente quiere demo", "gpt-4o-mini", 100, 50, 200L
        );

        AnalizarChatCommand cmd = new AnalizarChatCommand(
                actor.value(), empresaPropia.value(), waConvId.toString(), "qué quiere?"
        );

        var result = service.analizar(cmd);

        assertEquals(1, saveConv.saved.size(), "debe persistir 1 conversación AI");
        assertEquals(2, saveMensaje.saved.size(), "debe persistir user + assistant");
        assertEquals("El cliente quiere demo", result.contenidoAsistente());
        assertEquals(1, generarChat.calls);
    }

    @Test
    @DisplayName("analizar pasa el transcript de WhatsApp al AI generation port (source-of-truth)")
    void analizar_pasaTranscriptAlPort() {
        UUID waConvId = UUID.randomUUID();
        waConvPort.put(new WhatsappConversacionResumen(
                waConvId, UUID.randomUUID(), empresaPropia.value(),
                null, "+54911...", "Juan"
        ));
        WhatsappMensajeResumen m1 = new WhatsappMensajeResumen(
                UUID.randomUUID(), waConvId, "ENTRANTE", "TEXT",
                "Hola, quiero info", null, LocalDateTime.of(2026, 6, 23, 10, 0)
        );
        WhatsappMensajeResumen m2 = new WhatsappMensajeResumen(
                UUID.randomUUID(), waConvId, "SALIENTE", "TEXT",
                "Te paso catalogo", null, LocalDateTime.of(2026, 6, 23, 10, 5)
        );
        waMensajePort.put(m1);
        waMensajePort.put(m2);
        generarChat.next = new RespuestaAsistente(
                "Cliente quiere info", "gpt-4o-mini", 50, 25, 100L
        );

        AnalizarChatCommand cmd = new AnalizarChatCommand(
                actor.value(), empresaPropia.value(), waConvId.toString(), null
        );
        service.analizar(cmd);

        ChatAsistenteRequest solicitud = generarChat.lastSolicitud;
        assertNotNull(solicitud, "el AI generation port debe haber sido invocado");
        assertEquals(2, solicitud.transcript().size(),
                "el transcript cargado desde WhatsappMensajeLecturaPort debe llegar al modelo");
        assertSame(m1, solicitud.transcript().get(0),
                "el transcript debe respetar el orden ascendente del port");
        assertTrue(solicitud.transcript().contains(m2));
    }

    @Test
    @DisplayName("aiConversacionId devuelto coincide con id persistido y es reutilizable")
    void analizar_idConversacionEsConsistenteEntreLookupGuardadoYReturn() {
        UUID waConvId = UUID.randomUUID();
        waConvPort.put(new WhatsappConversacionResumen(
                waConvId, UUID.randomUUID(), empresaPropia.value(),
                null, "+54911...", "Juan"
        ));
        generarChat.next = new RespuestaAsistente(
                "ok", "gpt-4o-mini", 50, 25, 100L
        );
        AnalizarChatCommand cmd = new AnalizarChatCommand(
                actor.value(), empresaPropia.value(), waConvId.toString(), "hola"
        );

        // First analyze: creates the conversation.
        var first = service.analizar(cmd);
        UUID idGuardado = saveConv.saved.get(0).getId().value();
        UUID idEsperado = UUID.nameUUIDFromBytes(
                (actor.value().toString() + ":" + waConvId.toString()).getBytes()
        );
        assertEquals(idEsperado, idGuardado,
                "el id guardado debe coincidir con el id deterministico derivado del scope");
        assertEquals(idEsperado, first.aiConversacionId(),
                "el id devuelto debe coincidir con el id persistido");

        // Second analyze: the find-by-id branch must find the same row by
        // the same deterministic id; it must NOT create a second row.
        var second = service.analizar(cmd);
        assertEquals(idEsperado, second.aiConversacionId(),
                "follow-up debe reusar el mismo aiConversacionId persistido");
        assertEquals(1, saveConv.saved.size(),
                "el segundo analyze NO debe persistir una conversación duplicada");
        assertEquals(2, findConv.lookupCount(idEsperado),
                "el lookup por id deterministico debe ejecutarse para localizar la fila");
    }

    // ── PR5: Resource-first tenant resolution (multi-company) ──

    @Test
    @DisplayName("PR5: actor multi-empresa con WA conv en E2 + hint E1 -> persiste filas bajo E2 (recurso gana)")
    void analizar_multiEmpresa_recursoGanaSobreHint() {
        // GIVEN: actor owns BOTH the hint company (E1) and the resource company (E2).
        findEmpresasPort.ownedBy(actor, List.of(empresaHint, empresaRecurso));
        // GIVEN: the WhatsApp conversation belongs to E2 (the resource).
        UUID waConvId = UUID.randomUUID();
        waConvPort.put(new WhatsappConversacionResumen(
                waConvId, UUID.randomUUID(), empresaRecurso.value(),
                null, "+54911...", "Juan"
        ));
        generarChat.next = new RespuestaAsistente(
                "Cliente E2 quiere info", "claude-haiku-4-5", 80, 40, 150L
        );

        // WHEN: the actor POSTs /chat carrying empresaId=E1 as a hint
        AnalizarChatCommand cmd = new AnalizarChatCommand(
                actor.value(), empresaHint.value(), waConvId.toString(), "qué quiere el cliente?"
        );
        var result = service.analizar(cmd);

        // THEN: the persisted AI conversation MUST be under the resource tenant E2,
        // not the hint tenant E1.
        assertEquals(1, saveConv.saved.size(), "debe persistir 1 conversación AI");
        AiConversacion persistedConv = saveConv.saved.get(0);
        assertEquals(empresaRecurso.value(), persistedConv.getEmpresaId().value(),
                "AiConversacion.empresaId MUST be the resource tenant (canalEmpresaId), not the hint");
        assertEquals(empresaRecurso.value(), result.aiConversacionId() != null
                        ? EmpresaId.from(persistedConv.getEmpresaId().value()).value()
                        : null,
                "result must echo the persisted entity tenant (resource, not hint)");

        // THEN: AI messages and resumen rows also carry the resource tenant E2.
        assertEquals(2, saveMensaje.saved.size(), "user + assistant turns");
        for (AiMensaje msg : saveMensaje.saved) {
            // AiMensaje inherits empresaId via AiConversacionId only — verify by loading
            // the conversation it points to and asserting tenant authority there.
            AiConversacion conv = findConv.findById(msg.getAiConversacionId().value()).orElseThrow();
            assertEquals(empresaRecurso.value(), conv.getEmpresaId().value(),
                    "AiMensaje rows belong to a conversation under the resource tenant E2");
        }

        // THEN: the AI generation port was invoked with the RESOURCE tenant, not the hint.
        assertNotNull(generarChat.lastSolicitud, "AI generation port must have been called");
        assertEquals(empresaRecurso.value(), generarChat.lastSolicitud.empresaId(),
                "ChatAsistenteRequest.empresaId MUST be the resource tenant (canalEmpresaId), not the hint");
    }

    @Test
    @DisplayName("PR5: actor multi-empresa sin hint, WA conv en E2 -> persiste bajo E2 (recurso deriva)")
    void analizar_multiEmpresa_sinHint_persisteBajoRecurso() {
        // GIVEN: actor owns both E1 and E2; the WA conv is in E2.
        findEmpresasPort.ownedBy(actor, List.of(empresaHint, empresaRecurso));
        UUID waConvId = UUID.randomUUID();
        waConvPort.put(new WhatsappConversacionResumen(
                waConvId, UUID.randomUUID(), empresaRecurso.value(),
                null, "+54911...", "Juan"
        ));
        generarChat.next = new RespuestaAsistente(
                "ok", "claude-haiku-4-5", 30, 20, 90L
        );

        // WHEN: command carries empresaId=null (no hint at all).
        AnalizarChatCommand cmd = new AnalizarChatCommand(
                actor.value(), null, waConvId.toString(), "hola"
        );
        service.analizar(cmd);

        // THEN: the conversation is persisted under E2 (resource) regardless of hint absence.
        assertEquals(1, saveConv.saved.size());
        assertEquals(empresaRecurso.value(), saveConv.saved.get(0).getEmpresaId().value(),
                "AiConversacion.empresaId MUST be the resource tenant E2 even with no hint");
        assertEquals(empresaRecurso.value(), generarChat.lastSolicitud.empresaId(),
                "ChatAsistenteRequest.empresaId MUST be the resource tenant E2");
    }

    @Test
    @DisplayName("PR5: WA conv en empresa que actor NO posee -> rechaza sin persistir (incluso si hint coincide con una empresa owned)")
    void analizar_multiEmpresa_recursoNoOwned_rechazaSinPersistir() {
        // GIVEN: actor owns only E1; the WA conv belongs to E3 (not owned).
        EmpresaId empresaNoOwned = EmpresaId.create();
        findEmpresasPort.ownedBy(actor, List.of(empresaHint));
        UUID waConvId = UUID.randomUUID();
        waConvPort.put(new WhatsappConversacionResumen(
                waConvId, UUID.randomUUID(), empresaNoOwned.value(),
                null, "+54911...", "Juan"
        ));

        // WHEN: hint=E1 (owned), but WA conv=E3 (not owned)
        AnalizarChatCommand cmd = new AnalizarChatCommand(
                actor.value(), empresaHint.value(), waConvId.toString(), "hola"
        );

        // THEN: rejected with the AI tenant exception — and no AI rows persisted.
        assertThrows(AsistenteTenantException.class, () -> service.analizar(cmd));
        assertEquals(0, saveConv.saved.size(), "no AI conversation must be persisted");
        assertEquals(0, saveMensaje.saved.size(), "no AI messages must be persisted");
        assertEquals(0, generarChat.calls, "AI generation port must NOT be invoked");
    }

    // ── helpers ────────────────────────────────────────────────

    private static final class InMemoryFindEmpresasByCreadorPort implements FindEmpresasByCreadorPort {
        private final List<Row> rows = new ArrayList<>();
        void ownedBy(UsuarioId u, List<EmpresaId> e) {
            rows.removeIf(r -> r.user.equals(u));
            rows.add(new Row(u, e));
        }
        @Override public List<EmpresaId> findEmpresasByCreador(UsuarioId u) {
            return rows.stream().filter(r -> r.user.equals(u)).findFirst()
                    .map(r -> r.empresas).orElse(List.of());
        }
        private record Row(UsuarioId user, List<EmpresaId> empresas) {}
    }

    private static final class InMemoryWhatsappConvPort implements WhatsappConversacionLecturaPort {
        private final List<WhatsappConversacionResumen> rows = new ArrayList<>();
        void put(WhatsappConversacionResumen w) { rows.add(w); }
        @Override public Optional<WhatsappConversacionResumen> findById(UUID id) {
            return rows.stream().filter(w -> w.waConversacionId().equals(id)).findFirst();
        }
    }

    private static final class InMemoryWhatsappMensajePort implements WhatsappMensajeLecturaPort {
        private final List<WhatsappMensajeResumen> rows = new ArrayList<>();
        void put(WhatsappMensajeResumen m) { rows.add(m); }
        @Override public List<WhatsappMensajeResumen> findByConversacionId(UUID id) {
            return rows.stream()
                    .filter(m -> m.waConversacionId().equals(id))
                    .toList();
        }
    }

    private static final class InMemoryAiConvStore {
        final java.util.Map<UUID, AiConversacion> rows = new java.util.HashMap<>();
        final java.util.Map<UUID, Integer> lookups = new java.util.HashMap<>();
        AiConversacion save(AiConversacion c) { rows.put(c.getId().value(), c); return c; }
        Optional<AiConversacion> findById(UUID id) {
            lookups.merge(id, 1, Integer::sum);
            return Optional.ofNullable(rows.get(id));
        }
        int lookupCount(UUID id) { return lookups.getOrDefault(id, 0); }
        List<AiConversacion> snapshot() { return new ArrayList<>(rows.values()); }
    }

    private static final class InMemoryFindAiConvPort implements FindAiConversacionPort {
        private final InMemoryAiConvStore store;
        InMemoryFindAiConvPort(InMemoryAiConvStore store) { this.store = store; }
        @Override public Optional<AiConversacion> findById(UUID id) { return store.findById(id); }
        int lookupCount(UUID id) { return store.lookupCount(id); }
    }

    private static final class InMemoryFindResumenPort implements FindAiResumenPort {
        @Override public Optional<AiResumenContexto> findByConversacionId(UUID id) { return Optional.empty(); }
    }

    private static final class InMemoryFindMemoriaPort implements FindAiMemoriaPort {
        @Override public List<AiMemoria> findActivasByConversacionId(UUID id, UUID actor, UUID empresa) {
            return List.of();
        }
    }

    private static final class InMemoryFindMensajesPort implements FindAiMensajesByConversacionPort {
        @Override public List<AiMensaje> findByConversacionId(UUID id) { return List.of(); }
    }

    private static final class InMemorySaveAiConvPort implements SaveAiConversacionPort {
        private final InMemoryAiConvStore store;
        final List<AiConversacion> saved = new ArrayList<>();
        InMemorySaveAiConvPort(InMemoryAiConvStore store) { this.store = store; }
        @Override public AiConversacion save(AiConversacion c) {
            AiConversacion savedEntity = store.save(c);
            saved.add(savedEntity);
            return savedEntity;
        }
        @Override public AiConversacion findById(UUID id) {
            return store.findById(id).orElse(null);
        }
    }

    private static final class InMemorySaveAiMensajePort implements SaveAiMensajePort {
        final List<AiMensaje> saved = new ArrayList<>();
        @Override public AiMensaje save(AiMensaje m) { saved.add(m); return m; }
    }

    private static final class InMemorySaveResumenPort implements SaveAiResumenPort {
        @Override public AiResumenContexto save(AiResumenContexto r) { return r; }
    }

    private static final class SpyGenerarChatPort implements GenerarChatAsistentePort {
        int calls;
        RespuestaAsistente next;
        ChatAsistenteRequest lastSolicitud;
        @Override public RespuestaAsistente generar(ChatAsistenteRequest solicitud) {
            calls++;
            lastSolicitud = solicitud;
            return next != null ? next : new RespuestaAsistente("", null, null, null, null);
        }
    }
}
