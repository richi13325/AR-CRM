package com.ar.crm2.application.usuario.port.in;

import com.ar.crm2.application.usuario.command.ForgotPasswordCommand;

public interface ForgotPasswordUseCase {

    void requestReset(ForgotPasswordCommand command);
}
