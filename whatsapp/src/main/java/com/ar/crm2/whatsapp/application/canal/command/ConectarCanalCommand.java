package com.ar.crm2.whatsapp.application.canal.command;

import java.util.UUID;

public record ConectarCanalCommand(UUID canalId) {
    public ConectarCanalCommand {
        if (canalId == null) throw new IllegalArgumentException("canalId es requerido");
    }
}
