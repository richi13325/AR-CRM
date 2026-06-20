package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.mapper.NotaTratoMapper;
import com.ar.crm2.adapter.out.persistence.repository.NotaTratoRepository;
import com.ar.crm2.application.notatrato.port.out.FindNotasByTratoPort;
import com.ar.crm2.application.notatrato.port.out.SaveNotaTratoPort;
import com.ar.crm2.model.entity.NotaTrato;
import com.ar.crm2.model.vo.TratoId;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class NotaTratoRepositoryAdapter implements SaveNotaTratoPort, FindNotasByTratoPort {

    private final NotaTratoRepository repository;

    @Override
    public NotaTrato save(NotaTrato nota) {
        return NotaTratoMapper.toDomain(repository.save(NotaTratoMapper.toEntity(nota)));
    }

    @Override
    public List<NotaTrato> findByTrato(TratoId tratoId) {
        return repository.findByTratoIdOrderByCreadoEnDesc(tratoId.value().toString())
            .stream().map(NotaTratoMapper::toDomain).toList();
    }
}
