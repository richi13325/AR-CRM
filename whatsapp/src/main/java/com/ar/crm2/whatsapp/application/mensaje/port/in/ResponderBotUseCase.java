package com.ar.crm2.whatsapp.application.mensaje.port.in;

import com.ar.crm2.whatsapp.application.mensaje.command.ResponderBotCommand;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;

public interface ResponderBotUseCase {
    Mensaje responder(ResponderBotCommand command);
}
