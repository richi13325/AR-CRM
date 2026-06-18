package com.ar.crm2.whatsapp.application.mensaje.service;

import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.application.mensaje.command.ResponderBotCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.ResponderBotUseCase;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyNewMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SaveMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SendWhatsappMessagePort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import com.ar.crm2.whatsapp.domain.vo.MensajeId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta del bot de n8n vía api_access_token (no hay UsuarioId humano de por medio).
 * Contrato: POST .../conversations/{id}/messages — {@code private:true} = nota interna,
 * no se manda a WhatsApp.
 */
@RequiredArgsConstructor
public class ResponderBotService implements ResponderBotUseCase {

    private final FindConversacionByIdPort findConversacionPort;
    private final FindCanalByIdPort findCanalPort;
    private final SendWhatsappMessagePort sendWhatsappPort;
    private final SaveMensajePort saveMensajePort;
    private final NotifyNewMensajePort notifyPort;
    private final SaveConversacionPort saveConversacionPort;

    @Override
    public Mensaje responder(ResponderBotCommand command) {
        Conversacion conversacion = findConversacionPort.findById(ConversacionId.from(command.conversacionId()))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + command.conversacionId()));

        String waMessageId = command.privado()
                ? "nota-" + UUID.randomUUID()
                : enviarPorCanal(conversacion, command.contenido());

        Mensaje mensaje = Mensaje.reconstitute(
                MensajeId.create(), conversacion.getId(), waMessageId,
                TipoMensaje.TEXTO, DireccionMensaje.SALIENTE, command.contenido(), null,
                StatusMensaje.ENVIADO, null, LocalDateTime.now());

        Mensaje saved = saveMensajePort.save(mensaje);
        notifyPort.notify(saved);

        if (!command.privado()) {
            saveConversacionPort.save(
                    conversacion.registrarMensajeSaliente(command.contenido(), saved.getCreadoEn()).marcarLeido());
        }
        return saved;
    }

    private String enviarPorCanal(Conversacion conversacion, String contenido) {
        CanalWhatsapp canal = findCanalPort.findById(CanalWhatsappId.from(conversacion.getCanalId().value()))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + conversacion.getCanalId()));
        return sendWhatsappPort.send(canal, conversacion.getNumeroTelefono(), TipoMensaje.TEXTO, contenido, null);
    }
}
