package com.ar.crm2.whatsapp.application.mensaje.port.in;

import com.ar.crm2.whatsapp.application.mensaje.command.SendMensajeCommand;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;

public interface SendMensajeUseCase {
    Mensaje send(SendMensajeCommand command);
}
