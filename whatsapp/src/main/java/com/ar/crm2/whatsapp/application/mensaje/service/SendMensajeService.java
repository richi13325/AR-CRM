package com.ar.crm2.whatsapp.application.mensaje.service;

import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.application.mensaje.command.SendMensajeCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.SendMensajeUseCase;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyNewMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SaveMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SendWhatsappMessagePort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SendMensajeService implements SendMensajeUseCase {

    private final FindConversacionByIdPort findConversacionPort;
    private final FindCanalByIdPort findCanalPort;
    private final SendWhatsappMessagePort sendWhatsappPort;
    private final SaveMensajePort saveMensajePort;
    private final NotifyNewMensajePort notifyPort;
    private final SaveConversacionPort saveConversacionPort;

    @Override
    public Mensaje send(SendMensajeCommand command) {
        Conversacion conversacion = findConversacionPort.findById(ConversacionId.from(command.conversacionId()))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + command.conversacionId()));

        String waMessageId;
        if (command.interna()) {
            // Nota interna: NO se envía a WhatsApp, solo queda en el hilo del CRM.
            waMessageId = "nota-" + java.util.UUID.randomUUID();
        } else {
            CanalWhatsapp canal = findCanalPort.findById(CanalWhatsappId.from(conversacion.getCanalId().value()))
                    .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + conversacion.getCanalId()));
            waMessageId = sendWhatsappPort.send(
                    canal,
                    conversacion.getNumeroTelefono(),
                    command.tipo(),
                    command.contenido(),
                    command.mediaUrl()
            );
        }

        Mensaje mensaje = Mensaje.createSaliente(
                ConversacionId.from(command.conversacionId()),
                waMessageId,
                command.tipo(),
                command.contenido(),
                command.mediaUrl(),
                UsuarioId.from(command.enviadoPor()),
                command.interna()
        );

        Mensaje saved = saveMensajePort.save(mensaje);
        notifyPort.notify(saved);

        // El agente respondió: actualiza preview/orden y marca la conversación como leída.
        String base = command.contenido() != null && !command.contenido().isBlank()
                ? command.contenido() : "📎 Adjunto";
        String preview = command.interna() ? "📝 Nota: " + base : base;
        saveConversacionPort.save(
                conversacion.registrarMensajeSaliente(preview, saved.getCreadoEn()).marcarLeido());
        return saved;
    }
}
