package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.RegistrarMensajeAsistenteCommand;
import com.ar.crm2.application.ai.exception.ConversacionAsistenteNoEncontradaException;
import com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException;
import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import com.ar.crm2.application.ai.port.out.FindAiConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiMemoriaPort;
import com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.GenerarChatAsistentePort;
import com.ar.crm2.application.ai.port.out.SaveAiMensajePort;
import com.ar.crm2.application.ai.port.out.SaveAiResumenPort;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
// ActorEmpresaScopeService implements ActorEmpresaScopePort directly, so the
// test injects the Empresa-owned service as the AI dependency.
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.entity.ia.AiMemoria;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.enums.RolMensajeAi;
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

/**
 * Tests for {@link RegistrarMensajeAsistenteService}.
 *
 * <p>Coverage targets for the follow-up slice (addresses
 * {@code ai-assistant/spec.md} scenario "Follow-up instruction uses prior
 * context" and sdd-verify finding #3):
 * <ul>
 *   <li>Tenant / owner rejection happens BEFORE any {@code ai_*} row is
 *       created — security boundary.</li>
 *   <li>Happy path: assistant reply is persisted, prior transcript is
 *       forwarded to the AI generation port as source-of-truth.</li>
 *   <li>Returned {@code aiConversacionId} equals the supplied id so the
 *       caller can chain further turns without drift.</li>
 *   <li>Missing conversation is rejected with the typed exception so the
 *       REST adapter can map it to 404.</li>
 * </ul>
 *
 * <p>Tool-driven proposal staging is the responsibility of
 * {@code ProponerAccionUseCase} (tested in
 * {@link ProponerAccionServiceTest}). This follow-up flow does NOT
 * stage proposals — it only persists user/assistant turns + summary
 * refresh.
 */
class RegistrarMensajeAsistenteServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final UsuarioId otroActor = UsuarioId.create();
    private final EmpresaId empresaPropia = EmpresaId.create();
    private final UUID waConvId = UUID.randomUUID();
    private final UUID aiConvIdValue = AiConversacionId.create().value();

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemoryWhatsappMensajePort waMensajePort;
    private InMemoryFindAiConvPort findConv;
    private InMemoryFindResumenPort findResumen;
    private InMemoryFindMemoriaPort findMemoria;
    private InMemoryFindMensajesPort findMensajes;
    private InMemorySaveAiMensajePort saveMensaje;
    private InMemorySaveResumenPort saveResumen;
    private SpyGenerarChatPort generarChat;
    private RegistrarMensajeAsistenteService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresaPropia));

        waMensajePort = new InMemoryWhatsappMensajePort();
        findConv = new InMemoryFindAiConvPort();
        findResumen = new InMemoryFindResumenPort();
        findMemoria = new InMemoryFindMemoriaPort();
        findMensajes = new InMemoryFindMensajesPort();
        saveMensaje = new InMemorySaveAiMensajePort();
        saveResumen = new InMemorySaveResumenPort();
        generarChat = new SpyGenerarChatPort();

service = new RegistrarMensajeAsistenteService(
                new ActorEmpresaScopeService(findEmpresasPort),
                waMensajePort,
                findConv, findMensajes, findResumen, findMemoria,
                saveMensaje, saveResumen,
                generarChat
        );
    }

    // ── Tenant / ownership ──────────────────────────────────────────────────

    @Test
    @DisplayName("registrar con AI conversation inexistente lanza ConversacionAsistenteNoEncontrada")
    void registrar_conversationInexistente_rechaza() {
        RegistrarMensajeAsistenteCommand cmd = new RegistrarMensajeAsistenteCommand(
                actor.value(), UUID.randomUUID(), empresaPropia.value(), "seguime"
        );
        assertThrows(ConversacionAsistenteNoEncontradaException.class,
                () -> service.registrar(cmd));
        assertEquals(0, saveMensaje.saved.size(), "no debe persistir mensajes AI");
        assertEquals(0, generarChat.calls, "no debe invocar AI port");
    }

    @Test
    @DisplayName("registrar de AI conversation ajena lanza ConversacionAsistenteNotOwnedByActorException y NO persiste")
    void registrar_deOtroUsuario_rechazaSinPersistir() {
        // Both actors own the same empresa so the resolver passes; the
        // entity-level ownership check on AiConversacion is what must fire.
        findEmpresasPort.ownedBy(otroActor, List.of(empresaPropia));

        AiConversacion ajena = AiConversacion.crear(
                empresaPropia, otroActor, waConvId.toString(), null, LocalDateTime.now()
        );
        findConv.put(ajena);

        RegistrarMensajeAsistenteCommand cmd = new RegistrarMensajeAsistenteCommand(
                actor.value(), ajena.getId().value(), empresaPropia.value(), "y?"
        );

        assertThrows(ConversacionAsistenteNotOwnedByActorException.class, () -> service.registrar(cmd));
        assertEquals(0, saveMensaje.saved.size(), "no debe persistir turnos AI ajenos");
        assertEquals(0, generarChat.calls, "no debe invocar AI port");
    }

    @Test
    @DisplayName("registrar de AI conversation de otra empresa rechaza con ConversacionAsistenteNotOwnedByActorException")
    void registrar_deOtraEmpresa_rechaza() {
        // The actor owns both empresas so the resolver passes; the
        // entity-level tenant check on AiConversacion is what must fire
        // because the conversation was started under otraEmpresa.
        EmpresaId otraEmpresa = EmpresaId.create();
        findEmpresasPort.ownedBy(actor, List.of(empresaPropia, otraEmpresa));

        AiConversacion ajena = AiConversacion.crear(
                otraEmpresa, actor, waConvId.toString(), null, LocalDateTime.now()
        );
        findConv.put(ajena);

        RegistrarMensajeAsistenteCommand cmd = new RegistrarMensajeAsistenteCommand(
                actor.value(), ajena.getId().value(), empresaPropia.value(), "y?"
        );

        assertThrows(ConversacionAsistenteNotOwnedByActorException.class, () -> service.registrar(cmd));
    }

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar happy path persiste user + assistant y devuelve mismo aiConversacionId")
    void registrar_happyPath_persisteTurnosYReusaId() {
        AiConversacion aiConv = AiConversacion.crear(
                empresaPropia, actor, waConvId.toString(),
                ContactoId.create(), LocalDateTime.now()
        );
        findConv.put(aiConv);

        WhatsappMensajeResumen transcriptEntry = new WhatsappMensajeResumen(
                UUID.randomUUID(), waConvId, "ENTRANTE", "TEXT",
                "ok dale", null, LocalDateTime.now()
        );
        waMensajePort.put(transcriptEntry);

        AiMensaje previo = AiMensaje.crear(
                aiConv.getId(), RolMensajeAi.ASSISTANT, "anterior",
                "gpt-4o-mini", 10, 20, 100L, null, LocalDateTime.now().minusMinutes(1)
        );
        findMensajes.put(previo);

        generarChat.next = new RespuestaAsistente(
                "Listo, creado", "gpt-4o-mini", 50, 25, 90L
        );

        RegistrarMensajeAsistenteCommand cmd = new RegistrarMensajeAsistenteCommand(
                actor.value(), aiConv.getId().value(), empresaPropia.value(), "dale"
        );

        var result = service.registrar(cmd);

        assertEquals(aiConv.getId().value(), result.aiConversacionId(),
                "el id devuelto debe coincidir con el id de la conversación existente");
        assertEquals(2, saveMensaje.saved.size(),
                "debe persistir turno user + turno assistant");
        assertEquals(RolMensajeAi.USER, saveMensaje.saved.get(0).getRol());
        assertEquals("dale", saveMensaje.saved.get(0).getContenido());
        assertEquals(RolMensajeAi.ASSISTANT, saveMensaje.saved.get(1).getRol());
        assertEquals("Listo, creado", saveMensaje.saved.get(1).getContenido());
    }

    @Test
    @DisplayName("registrar forward transcript de WhatsApp al AI port (source-of-truth)")
    void registrar_pasaTranscriptAlPort() {
        AiConversacion aiConv = AiConversacion.crear(
                empresaPropia, actor, waConvId.toString(), null, LocalDateTime.now()
        );
        findConv.put(aiConv);

        WhatsappMensajeResumen m1 = new WhatsappMensajeResumen(
                UUID.randomUUID(), waConvId, "ENTRANTE", "TEXT",
                "primero", null, LocalDateTime.now().minusMinutes(2)
        );
        WhatsappMensajeResumen m2 = new WhatsappMensajeResumen(
                UUID.randomUUID(), waConvId, "SALIENTE", "TEXT",
                "segundo", null, LocalDateTime.now().minusMinutes(1)
        );
        waMensajePort.put(m1);
        waMensajePort.put(m2);
        generarChat.next = new RespuestaAsistente(
                "ok", "gpt-4o-mini", 50, 25, 100L
        );

        service.registrar(new RegistrarMensajeAsistenteCommand(
                actor.value(), aiConv.getId().value(), empresaPropia.value(), "y?"
        ));

        ChatAsistenteRequest solicitud = generarChat.lastSolicitud;
        assertNotNull(solicitud);
        assertEquals(2, solicitud.transcript().size(),
                "el transcript cargado desde WhatsappMensajeLecturaPort debe llegar al modelo");
        assertSame(m1, solicitud.transcript().get(0),
                "el transcript debe respetar el orden ascendente del port");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static final class InMemoryFindEmpresasByCreadorPort implements FindEmpresasByCreadorPort {
        private final List<Row> rows = new ArrayList<>();
        /** Replaces the existing row for {@code u} (idempotent). */
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

    private static final class InMemoryWhatsappMensajePort implements WhatsappMensajeLecturaPort {
        private final List<WhatsappMensajeResumen> rows = new ArrayList<>();
        void put(WhatsappMensajeResumen m) { rows.add(m); }
        @Override public List<WhatsappMensajeResumen> findByConversacionId(UUID id) {
            return rows.stream()
                    .filter(m -> m.waConversacionId().equals(id))
                    .toList();
        }
    }

    private static final class InMemoryFindAiConvPort implements FindAiConversacionPort {
        private final java.util.Map<UUID, AiConversacion> rows = new java.util.HashMap<>();
        void put(AiConversacion c) { rows.put(c.getId().value(), c); }
        @Override public Optional<AiConversacion> findById(UUID id) {
            return Optional.ofNullable(rows.get(id));
        }
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
        private final List<AiMensaje> rows = new ArrayList<>();
        void put(AiMensaje m) { rows.add(m); }
        @Override public List<AiMensaje> findByConversacionId(UUID id) {
            return rows.stream().filter(m -> m.getAiConversacionId().value().equals(id)).toList();
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
