package com.ar.crm2.application.agenda.service;

import com.ar.crm2.application.agenda.command.CreateAgendaCommand;
import com.ar.crm2.application.agenda.port.out.SaveAgendaPort;
import com.ar.crm2.application.agenda.port.in.CreateAgendaUseCase;
import com.ar.crm2.application.agenda.port.out.SendAgendaEmailPort;
import com.ar.crm2.application.tarea.exception.TareaNotFoundException;
import com.ar.crm2.application.tarea.port.out.FindTareaByIdPort;
import com.ar.crm2.application.trato.exception.TratoNotFoundException;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateAgendaService implements CreateAgendaUseCase {

    private final SaveAgendaPort savePort;
    private final FindTareaByIdPort findTareaByIdPort;
    private final FindTratoByIdPort findTratoByIdPort;
    private final FindUsuarioByIdPort findUsuarioByIdPort;
    private final SendAgendaEmailPort sendAgendaEmailPort;

    @Override
    public Agenda create(CreateAgendaCommand command) {
        if (command.tareaId() != null) {
            TareaId tareaId = TareaId.from(command.tareaId());
            findTareaByIdPort.findById(tareaId)
                    .orElseThrow(() -> TareaNotFoundException.forId(command.tareaId()));
        }

        if (command.tratoId() != null) {
            TratoId tratoId = TratoId.from(command.tratoId());
            findTratoByIdPort.findById(tratoId)
                    .orElseThrow(() -> TratoNotFoundException.forId(command.tratoId()));
        }

        Agenda agenda = Agenda.create(
            command.tipo(),
            command.asunto(),
            command.descripcion(),
            command.fecha(),
            command.horaInicio(),
            command.horaFin(),
            command.tareaId() != null ? TareaId.from(command.tareaId()) : null,
            command.tratoId() != null ? TratoId.from(command.tratoId()) : null,
            command.ubicacion(),
            command.linkVideollamada(),
            UsuarioId.from(command.creadoPor()),
            command.recordatorioHabilitado(),
            command.minutosAntes()
        );

        Agenda saved = savePort.save(agenda);

        sendCreatedEmail(saved);

        return saved;
    }

    private void sendCreatedEmail(Agenda agenda) {
        try {
            UsuarioId creatorId = agenda.getCreadoPor();
            Usuario usuario = findUsuarioByIdPort.findById(creatorId).orElse(null);

            if (usuario == null || usuario.getCorreo() == null) {
                return;
            }

            sendAgendaEmailPort.sendAgendaCreatedEmail(agenda, usuario.getCorreo());

        } catch (Exception e) {
            // Notification failures must not prevent Agenda creation.
        }
    }
}
