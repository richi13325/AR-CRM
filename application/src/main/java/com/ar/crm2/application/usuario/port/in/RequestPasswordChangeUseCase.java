package com.ar.crm2.application.usuario.port.in;

import com.ar.crm2.application.usuario.command.RequestPasswordChangeCommand;

public interface RequestPasswordChangeUseCase {

    void requestChange(RequestPasswordChangeCommand command);
}
