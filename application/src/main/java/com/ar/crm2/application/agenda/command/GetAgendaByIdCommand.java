package com.ar.crm2.application.agenda.command;

import java.util.UUID;

public record GetAgendaByIdCommand(UUID id) {

    public GetAgendaByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}
