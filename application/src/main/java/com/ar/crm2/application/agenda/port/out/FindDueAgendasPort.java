package com.ar.crm2.application.agenda.port.out;

import com.ar.crm2.model.entity.Agenda;

import java.util.List;

public interface FindDueAgendasPort {

    List<Agenda> findDueReminders();
}
