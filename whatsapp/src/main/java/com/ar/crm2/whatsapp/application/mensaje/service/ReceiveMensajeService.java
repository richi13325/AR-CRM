package com.ar.crm2.whatsapp.application.mensaje.service;

import com.ar.crm2.whatsapp.application.conversacion.port.in.GetOrCreateConversacionUseCase;
import com.ar.crm2.whatsapp.application.mensaje.command.ReceiveMensajeCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.ReceiveMensajeUseCase;
import com.ar.crm2.whatsapp.application.mensaje.port.out.ExistsMensajeByWaMessageIdPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.NotifyNewMensajePort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SaveMensajePort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReceiveMensajeService implements ReceiveMensajeUseCase {

    private final ExistsMensajeByWaMessageIdPort existsPort;
    private final GetOrCreateConversacionUseCase getOrCreateConversacion;
    private final SaveMensajePort saveMensajePort;
    private final NotifyNewMensajePort notifyPort;

    @Override
    public Mensaje receive(ReceiveMensajeCommand command) {
        // Idempotencia: si ya procesamos este mensaje de Evolution API, lo ignoramos
        if (existsPort.existsByWaMessageId(command.waMessageId())) {
            throw new IllegalStateException("Mensaje ya procesado: " + command.waMessageId());
        }

        Conversacion conversacion = getOrCreateConversacion.getOrCreate(
                command.canalId(),
                command.numeroTelefono(),
                command.nombreContacto()
        );

        Mensaje mensaje = Mensaje.createEntrante(
                conversacion.getId(),
                command.waMessageId(),
                command.tipo(),
                command.contenido(),
                command.mediaUrl()
        );

        Mensaje saved = saveMensajePort.save(mensaje);
        notifyPort.notify(saved);
        return saved;
    }
}
