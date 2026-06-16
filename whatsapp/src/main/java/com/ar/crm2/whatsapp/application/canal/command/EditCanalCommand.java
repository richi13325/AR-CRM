package com.ar.crm2.whatsapp.application.canal.command;

import java.util.UUID;

public record EditCanalCommand(
        UUID canalId,
        String nombre,
        String apiUrl,
        String apiKey
) {
    public EditCanalCommand {
        if (canalId == null) throw new IllegalArgumentException("canalId es requerido");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre es requerido");
        if (apiUrl == null || apiUrl.isBlank()) throw new IllegalArgumentException("apiUrl es requerido");
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("apiKey es requerido");
    }
}
