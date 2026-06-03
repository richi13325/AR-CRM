package com.ar.crm2.application.agenda.command;

import java.util.UUID;

public record DeleteAgendaCommand(UUID id) {

    public DeleteAgendaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}
