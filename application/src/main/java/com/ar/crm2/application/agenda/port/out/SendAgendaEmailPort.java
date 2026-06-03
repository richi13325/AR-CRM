package com.ar.crm2.application.agenda.port.out;

import com.ar.crm2.model.entity.Agenda;

public interface SendAgendaEmailPort {

    void sendAgendaCreatedEmail(Agenda agenda, String recipientEmail);

    void sendReminderEmail(Agenda agenda, String recipientEmail);
}
