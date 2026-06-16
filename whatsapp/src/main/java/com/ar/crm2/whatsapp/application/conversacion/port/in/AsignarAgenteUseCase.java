package com.ar.crm2.whatsapp.application.conversacion.port.in;

import com.ar.crm2.whatsapp.application.conversacion.command.AsignarAgenteCommand;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;

public interface AsignarAgenteUseCase {
    Conversacion asignar(AsignarAgenteCommand command);
}
