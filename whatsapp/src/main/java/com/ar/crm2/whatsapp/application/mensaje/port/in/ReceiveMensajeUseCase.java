package com.ar.crm2.whatsapp.application.mensaje.port.in;

import com.ar.crm2.whatsapp.application.mensaje.command.ReceiveMensajeCommand;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;

public interface ReceiveMensajeUseCase {
    Mensaje receive(ReceiveMensajeCommand command);
}
