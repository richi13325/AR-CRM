package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.RegistrarMensajeAsistenteCommand;
import com.ar.crm2.application.ai.exception.ConversacionAsistenteNoEncontradaException;
import com.ar.crm2.application.ai.port.out.dto.MensajeChat;
import com.ar.crm2.application.ai.port.in.result.ResultadoAnalisisChat;
import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import com.ar.crm2.application.ai.port.in.RegistrarMensajeAsistenteUseCase;
import com.ar.crm2.application.ai.port.out.FindAiConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiMemoriaPort;
import com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.GenerarChatAsistentePort;
import com.ar.crm2.application.ai.port.out.SaveAiMensajePort;
import com.ar.crm2.application.ai.port.out.SaveAiResumenPort;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.enums.RolMensajeAi;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service that registers a follow-up user message in an
 * existing AI conversation and returns the assistant's next reply.
 *
 * <p>The follow-up turn reloads AI history, summary, memory AND the
 * persisted WhatsApp transcript for the same scope. The assistant is
 * not allowed to reason over fresh messages that have not yet been
 * persisted — the persisted CRM data is the source of truth per
 * {@code ai-assistant/spec.md}.
 *
 * <p><b>Tool-driven staging:</b> this service does NOT stage
 * proposals itself. Tool-driven staging is owned by
 * {@code ProponerAccionUseCase}, invoked by the Spring AI tool
 * adapter when the model decides to suggest a mutation. This
 * follow-up flow is coordination-only.
 *
 * <p><b>Tenant resolution (architecture correction):</b> this service
 * depends on the Empresa-owned {@link ActorEmpresaScopePort} port,
 * not on a class named adapter or on an AI-specific resolver
 * service. The neutral domain exception raised by the port is
 * translated to the AI-public one via
 * {@link AiTenantExceptionTranslator} at the call site.
 */
@RequiredArgsConstructor
public class RegistrarMensajeAsistenteService implements RegistrarMensajeAsistenteUseCase {

    private final ActorEmpresaScopePort actorEmpresaScopePort;
    private final WhatsappMensajeLecturaPort whatsappMensajePort;
    private final FindAiConversacionPort findAiConversacionPort;
    private final FindAiMensajesByConversacionPort findAiMensajesPort;
    private final FindAiResumenPort findAiResumenPort;
    private final FindAiMemoriaPort findAiMemoriaPort;
    private final SaveAiMensajePort saveAiMensajePort;
    private final SaveAiResumenPort saveAiResumenPort;
    private final GenerarChatAsistentePort generarChatPort;

    @Override
    public ResultadoAnalisisChat registrar(RegistrarMensajeAsistenteCommand command) {
        LocalDateTime ahora = LocalDateTime.now();
        EmpresaId empresaId = AiTenantExceptionTranslator.resolve(
                actorEmpresaScopePort, command.actorUsuarioId(), command.empresaId()
        );
        UsuarioId actor = UsuarioId.from(command.actorUsuarioId());

        AiConversacion aiConv = findAiConversacionPort.findById(command.aiConversacionId())
                .orElseThrow(() -> ConversacionAsistenteNoEncontradaException.forId(
                        command.aiConversacionId().toString()
                ));

        aiConv.requireOwnedBy(actor, empresaId);

        // Build request from scoped history. Transcript = persisted WhatsApp
        // messages for this wa_conversacion_id, loaded via the same port
        // used by AnalizarChatService so follow-up reasoning matches the
        // first turn's context.
        UUID waConvId = UUID.fromString(aiConv.getWaConversacionId());
        List<WhatsappMensajeResumen> waMensajes = whatsappMensajePort.findByConversacionId(waConvId);
        List<AiMensaje> historialAi = findAiMensajesPort.findByConversacionId(command.aiConversacionId());
        List<MensajeChat> historialChat = new ArrayList<>();
        for (AiMensaje m : historialAi) {
            historialChat.add(new MensajeChat(m.getRol().name(), m.getContenido(), m.getToolCallJson()));
        }
        AiResumenContexto resumen = findAiResumenPort.findByConversacionId(command.aiConversacionId()).orElse(null);
        List<MensajeChat> memoriaChat = findAiMemoriaPort.findActivasByConversacionId(
                waConvId,
                command.actorUsuarioId(), empresaId.value()
        ).stream()
                .map(mem -> new MensajeChat("SYSTEM", mem.getContenido(), null))
                .toList();

        ChatAsistenteRequest solicitud = new ChatAsistenteRequest(
                command.aiConversacionId(),
                command.actorUsuarioId(),
                empresaId.value(),
                aiConv.getWaConversacionId(),
                historialChat,
                memoriaChat,
                waMensajes,
                resumen != null ? resumen.getFacts() : "",
                resumen != null ? resumen.getInferences() : "",
                command.mensajeUsuario()
        );

        RespuestaAsistente respuesta = generarChatPort.generar(solicitud);

        // Persist user turn + assistant turn.
        saveAiMensajePort.save(AiMensaje.crear(
                aiConv.getId(), RolMensajeAi.USER, command.mensajeUsuario(),
                null, null, null, null, null, ahora
        ));
        saveAiMensajePort.save(AiMensaje.crear(
                aiConv.getId(), RolMensajeAi.ASSISTANT, respuesta.contenido(),
                respuesta.modelo(), respuesta.promptTokens(), respuesta.completionTokens(),
                respuesta.latencyMs(), null, ahora
        ));

        // Refresh summary when present.
        if (resumen != null) {
            AiResumenContexto reemplazo = resumen.reemplazarCon(
                    resumen.getFacts(), respuesta.contenido(), null,
                    resumen.getSourceWatermark(), ahora
            );
            saveAiResumenPort.save(reemplazo);
        }

        return new ResultadoAnalisisChat(
                command.aiConversacionId(),
                respuesta.contenido(),
                respuesta.modelo()
        );
    }
}
