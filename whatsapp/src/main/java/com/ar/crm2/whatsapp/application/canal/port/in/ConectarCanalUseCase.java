package com.ar.crm2.whatsapp.application.canal.port.in;

import com.ar.crm2.whatsapp.application.canal.command.ConectarCanalCommand;

public interface ConectarCanalUseCase {
    ConectarCanalResult conectar(ConectarCanalCommand command);
}
