package com.ar.crm2.application.agenda.exception;

import java.util.UUID;

public class AgendaNotFoundException extends RuntimeException {

    public AgendaNotFoundException(String message) {
        super(message);
    }

    public static AgendaNotFoundException forId(UUID id) {
        return new AgendaNotFoundException("Agenda con id " + id + " no encontrada");
    }
}
