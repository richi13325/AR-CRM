package com.ar.crm2.application.agenda.service;

import com.ar.crm2.application.agenda.command.EditAgendaCommand;
import com.ar.crm2.application.agenda.exception.AgendaNotFoundException;
import com.ar.crm2.application.agenda.port.in.EditAgendaUseCase;
import com.ar.crm2.application.agenda.port.out.FindAgendaByIdPort;
import com.ar.crm2.application.agenda.port.out.SaveAgendaPort;
import com.ar.crm2.application.tarea.exception.TareaNotFoundException;
import com.ar.crm2.application.tarea.port.out.FindTareaByIdPort;
import com.ar.crm2.application.trato.exception.TratoNotFoundException;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.enums.RecordatorioEstado;
import com.ar.crm2.model.vo.AgendaId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class EditAgendaService implements EditAgendaUseCase {

    private final FindAgendaByIdPort findPort;
    private final SaveAgendaPort savePort;
    private final FindTareaByIdPort findTareaByIdPort;
    private final FindTratoByIdPort findTratoByIdPort;

    @Override
    public Agenda edit(EditAgendaCommand command) {
        AgendaId agendaId = AgendaId.from(command.id());

        Agenda existing = findPort.findById(agendaId)
                .orElseThrow(() -> AgendaNotFoundException.forId(command.id()));

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

        RecordatorioEstado estadoAnterior = existing.getRecordatorioEstado();
        boolean yaEnviado = estadoAnterior == RecordatorioEstado.ENVIADO;

        Agenda updated = Agenda.reconstitute(
                existing.getId(),
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
                existing.getCreadoPor(),
                existing.getCreadoEn(),
                LocalDateTime.now(),
                command.recordatorioHabilitado(),
                command.recordatorioHabilitado() ? command.minutosAntes() : null,
                yaEnviado ? RecordatorioEstado.ENVIADO :
                        (command.recordatorioHabilitado() ? RecordatorioEstado.PENDIENTE : null),
                yaEnviado ? existing.getRecordatorioEnviadoEn() : null,
                yaEnviado ? existing.getUltimoIntentoEn() : null
        );

        return savePort.save(updated);
    }
}
