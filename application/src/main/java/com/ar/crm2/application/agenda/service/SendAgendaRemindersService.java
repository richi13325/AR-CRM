package com.ar.crm2.application.agenda.service;

import com.ar.crm2.application.agenda.port.in.SendAgendaRemindersUseCase;
import com.ar.crm2.application.agenda.port.out.FindDueAgendasPort;
import com.ar.crm2.application.agenda.port.out.SaveAgendaPort;
import com.ar.crm2.application.agenda.port.out.SendAgendaEmailPort;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SendAgendaRemindersService implements SendAgendaRemindersUseCase {

    private final FindDueAgendasPort findDueAgendasPort;
    private final FindUsuarioByIdPort findUsuarioByIdPort;
    private final SendAgendaEmailPort sendEmailPort;
    private final SaveAgendaPort saveAgendaPort;

    @Override
    public void sendDueReminders() {
        List<Agenda> dueAgendas = findDueAgendasPort.findDueReminders();

        for (Agenda agenda : dueAgendas) {
            try {
                UsuarioId creatorId = agenda.getCreadoPor();
                Usuario usuario = findUsuarioByIdPort.findById(creatorId)
                        .orElse(null);

                if (usuario == null || usuario.getCorreo() == null) {
                    Agenda failed = agenda.withRecordatorioFallido();
                    saveAgendaPort.save(failed);
                    continue;
                }

                sendEmailPort.sendReminderEmail(agenda, usuario.getCorreo());

                Agenda sent = agenda.withRecordatorioEnviado();
                saveAgendaPort.save(sent);

            } catch (Exception e) {
                Agenda failed = agenda.withRecordatorioFallido();
                saveAgendaPort.save(failed);
            }
        }
    }
}
