package com.ar.crm2.whatsapp.application.canal.command;

import java.util.UUID;

// apiUrl/apiKey pueden venir vacíos: significa "no cambiar" (ver CanalWhatsapp.editar).
public record EditCanalCommand(
        UUID canalId,
        String nombre,
        String apiUrl,
        String apiKey
) {
    public EditCanalCommand {
        if (canalId == null) throw new IllegalArgumentException("canalId es requerido");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre es requerido");
    }
}
