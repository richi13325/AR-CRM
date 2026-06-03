package com.ar.crm2.application.usuario.command;

public record ForgotPasswordCommand(
    String correo
) {

    public ForgotPasswordCommand {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo is required");
        }
    }
}
