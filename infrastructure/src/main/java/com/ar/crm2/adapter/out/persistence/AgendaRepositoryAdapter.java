package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.AgendaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.AgendaMapper;
import com.ar.crm2.adapter.out.persistence.repository.AgendaRepository;
import com.ar.crm2.application.agenda.port.out.DeleteAgendaByIdPort;
import com.ar.crm2.application.agenda.port.out.FindAgendasByUserIdPort;
import com.ar.crm2.application.agenda.port.out.FindAgendaByIdPort;
import com.ar.crm2.application.agenda.port.out.FindDueAgendasPort;
import com.ar.crm2.application.agenda.port.out.SaveAgendaPort;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.enums.RecordatorioEstado;
import com.ar.crm2.model.vo.AgendaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AgendaRepositoryAdapter implements SaveAgendaPort, FindAgendaByIdPort, DeleteAgendaByIdPort, FindAgendasByUserIdPort, FindDueAgendasPort {

    private final AgendaRepository repository;

    @Override
    public Agenda save(Agenda agenda) {
        AgendaEntity entity = AgendaMapper.toEntity(agenda);
        AgendaEntity saved = repository.save(entity);
        return AgendaMapper.toDomain(saved);
    }

    @Override
    public Optional<Agenda> findById(AgendaId id) {
        return repository.findById(id.value().toString())
            .map(AgendaMapper::toDomain);
    }

    @Override
    public void deleteById(AgendaId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public List<Agenda> findByCreadoPor(UsuarioId usuarioId) {
        return repository.findByCreadoPor(usuarioId.value().toString()).stream()
            .map(AgendaMapper::toDomain)
            .toList();
    }

    @Override
    public List<Agenda> findDueReminders() {
        List<RecordatorioEstado> estados = Arrays.asList(RecordatorioEstado.PENDIENTE, RecordatorioEstado.FALLIDO);
        LocalDateTime now = LocalDateTime.now();

        return repository.findRemindersWithStatus(estados).stream()
            .map(AgendaMapper::toDomain)
            .filter(Agenda::isRecordatorioDue)
            .toList();
    }
}
