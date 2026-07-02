package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.AnalizarChatCommand;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.application.ai.port.out.dto.MensajeChat;
import com.ar.crm2.application.ai.port.in.result.ResultadoAnalisisChat;
import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;
import com.ar.crm2.application.ai.port.out.projection.WhatsappConversacionResumen;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import com.ar.crm2.application.ai.port.in.AnalizarChatUseCase;
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
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.enums.RolMensajeAi;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service that analyzes a WhatsApp conversation using the
 * AI assistant.
 *
 * <p><b>Resource-first tenant resolution (PR5):</b> the tenant scope
 * for every AI row created by this service (AiConversacion, AiMensaje,
 * AiResumenContexto) is ALWAYS derived from the addressed WhatsApp
 * conversation's {@code canalEmpresaId}. The {@code empresaId} carried
 * in {@link AnalizarChatCommand} is an optional hint / cross-check and
 * MUST NOT override the resource tenant. If the resource tenant is
 * not owned by the authenticated actor, the request is rejected with
 * {@link AsistenteTenantException} BEFORE any {@code ai_*} row is
 * created — zero leak of cross-tenant AI state.
 *
 * <p>Flow: load WhatsApp conversation FIRST → derive tenant from
 * {@code canalEmpresaId} → validate actor ownership → load scoped
 * summary + memory → build request → invoke AI generation port →
 * persist AI conversation + turns + summary refresh.
 *
 * <p><b>Tool-driven staging:</b> this service does NOT stage
 * proposals itself. Tool-driven staging is owned by
 * {@code ProponerAccionUseCase}, invoked by the Spring AI tool
 * adapter when the model decides to suggest a mutation. The analyze
 * and follow-up flows are coordination-only: they persist the
 * conversation, the user/assistant turns, and refresh the summary.
 *
 * <p><b>Command access (PR5 corrective):</b> scalar validation of
 * command fields lives in {@link AnalizarChatCommand}'s canonical
 * constructor; this service does NOT re-validate those fields.
 * Local variables are avoided in favour of reading directly from the
 * command, except where type conversion (UUID / VO) is required and
 * used consistently in the immediately following block.
 *
 * <p><b>Tenant resolution (architecture correction):</b> this service
 * depends on the Empresa-owned {@link ActorEmpresaScopePort} port,
 * not on any AI-specific resolver service. It only uses
 * {@code empresasDelActor(...)} for the ownership cross-check, so no
 * AI-public exception translation is needed here — the resource-first
 * tenant check throws {@link AsistenteTenantException} directly.
 */
@RequiredArgsConstructor
public class AnalizarChatService implements AnalizarChatUseCase {

    /** Default TTL (minutes) for proposals staged by the analyze flow. */
    public static final int DEFAULT_PROPOSAL_TTL_MINUTES = 60;

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final WhatsappConversacionLecturaPort whatsappConversacionPort;
    private final WhatsappMensajeLecturaPort whatsappMensajePort;
    private final FindAiConversacionPort findAiConversacionPort;
    private final FindAiResumenPort findAiResumenPort;
    private final FindAiMemoriaPort findAiMemoriaPort;
    private final FindAiMensajesByConversacionPort findAiMensajesPort;
    private final SaveAiConversacionPort saveAiConversacionPort;
    private final SaveAiMensajePort saveAiMensajePort;
    private final SaveAiResumenPort saveAiResumenPort;
    private final GenerarChatAsistentePort generarChatPort;

    @Override
    public ResultadoAnalisisChat analizar(AnalizarChatCommand command) {
        LocalDateTime ahora = LocalDateTime.now();

        // ── Resource-first tenant resolution (PR5) ───────────────
        // Load the WhatsApp conversation FIRST. Tenant authority is
        // derived from `canalEmpresaId` — never from the command's
        // hint empresaId. Validate that the actor owns the resource
        // tenant before any AI aggregate is created.
        UUID waConvId = UUID.fromString(command.waConversacionId());
        List<EmpresaId> empresasOwned = actorEmpresaScopePort.empresasDelActor(command.actorUsuarioId());
        WhatsappConversacionResumen conversacion = whatsappConversacionPort.findById(waConvId)
                .filter(c -> empresasOwned.stream()
                        .anyMatch(e -> e.value().equals(c.canalEmpresaId())))
                .orElseThrow(() -> AsistenteTenantException.chatNoPerteneceAlActor(
                        command.actorUsuarioId().toString(), waConvId.toString()
                ));

        // Resource tenant is AUTHORITATIVE. The hint empresaId in the
        // command is a cross-check only and MUST NOT override.
        EmpresaId empresaId = EmpresaId.from(conversacion.canalEmpresaId());

        // Find or create the AI conversation for this scope. We use a
        // deterministic id derived from the (actor, waConv) tuple so a
        // repeated analyze call returns the same conversation. When the
        // row does not exist we MUST save it with the same deterministic
        // id (via reconstitute), otherwise lookups by id will diverge
        // from the persisted id and follow-up turns cannot reuse this
        // conversation.
        UUID aiConvIdValue = UUID.nameUUIDFromBytes(
                (command.actorUsuarioId().toString() + ":" + waConvId.toString()).getBytes()
        );
        AiConversacionId aiConvId = AiConversacionId.from(aiConvIdValue);
        Optional<AiConversacion> existing = findAiConversacionPort.findById(aiConvIdValue);
        AiConversacion aiConv = existing.orElseGet(() -> saveAiConversacionPort.save(
                AiConversacion.reconstitute(
                        aiConvId,
                        empresaId,
                        UsuarioId.from(command.actorUsuarioId()),
                        waConvId.toString(),
                        conversacion.contactoId() == null
                                ? null
                                : com.ar.crm2.model.vo.ContactoId.from(conversacion.contactoId()),
                        false,
                        ahora,
                        ahora
                )
        ));

        // Load context for the assistant request. The WhatsApp transcript
        // is the source of truth per ai-assistant/spec.md; the AI must
        // reason over persisted messages, not over an in-memory summary.
        List<WhatsappMensajeResumen> waMensajes = whatsappMensajePort.findByConversacionId(waConvId);
        List<AiMensaje> historialAi = findAiMensajesPort.findByConversacionId(aiConvIdValue);
        AiResumenContexto resumen = findAiResumenPort.findByConversacionId(aiConvIdValue).orElse(null);

        List<MensajeChat> historialChat = new ArrayList<>();
        for (AiMensaje m : historialAi) {
            historialChat.add(new MensajeChat(
                    m.getRol().name(), m.getContenido(), m.getToolCallJson()
            ));
        }
        List<MensajeChat> memoriaChat = findAiMemoriaPort.findActivasByConversacionId(
                waConvId, command.actorUsuarioId(), empresaId.value()
        ).stream()
                .map(mem -> new MensajeChat("SYSTEM", mem.getContenido(), null))
                .toList();

        // ChatAsistenteRequest carries the RESOURCE tenant (not the
        // command hint) so downstream AI tool calls and memory writes
        // are scoped to the conversation's owning company.
        ChatAsistenteRequest solicitud = new ChatAsistenteRequest(
                aiConvIdValue,
                command.actorUsuarioId(),
                empresaId.value(),
                waConvId.toString(),
                historialChat,
                memoriaChat,
                waMensajes,
                resumen != null ? resumen.getFacts() : "",
                resumen != null ? resumen.getInferences() : "",
                command.mensajeUsuario()
        );

        RespuestaAsistente respuesta = generarChatPort.generar(solicitud);

        // Persist user turn (if any) and assistant turn.
        if (command.mensajeUsuario() != null && !command.mensajeUsuario().isBlank()) {
            saveAiMensajePort.save(AiMensaje.crear(
                    aiConvId, RolMensajeAi.USER, command.mensajeUsuario(),
                    null, null, null, null, null, ahora
            ));
        }
        saveAiMensajePort.save(AiMensaje.crear(
                aiConvId, RolMensajeAi.ASSISTANT, respuesta.contenido(),
                respuesta.modelo(), respuesta.promptTokens(), respuesta.completionTokens(),
                respuesta.latencyMs(), null, ahora
        ));

        // Refresh summary.
        if (resumen == null) {
            saveAiResumenPort.save(AiResumenContexto.crear(
                    UsuarioId.from(command.actorUsuarioId()), empresaId, waConvId.toString(),
                    conversacion.contactoId() == null
                            ? null
                            : com.ar.crm2.model.vo.ContactoId.from(conversacion.contactoId()),
                    "Initial analysis", respuesta.contenido(), null,
                    (long) waMensajes.size(), aiConvId, ahora
            ));
        } else {
            AiResumenContexto reemplazo = resumen.reemplazarCon(
                    resumen.getFacts(), respuesta.contenido(), null,
                    (long) waMensajes.size(), ahora
            );
            saveAiResumenPort.save(reemplazo);
        }

        return new ResultadoAnalisisChat(
                aiConvIdValue,
                respuesta.contenido(),
                respuesta.modelo()
        );
    }
}
