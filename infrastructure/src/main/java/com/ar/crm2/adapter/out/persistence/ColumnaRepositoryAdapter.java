package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ColumnaMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.application.columna.port.out.DeleteColumnaByIdPort;
import com.ar.crm2.application.columna.port.out.ExistsColumnaAsignadaPort;
import com.ar.crm2.application.columna.port.out.FindAllColumnasPort;
import com.ar.crm2.application.columna.port.out.FindColumnaByIdPort;
import com.ar.crm2.application.columna.port.out.SaveColumnaPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.vo.ColumnaId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that fulfills every Columna outbound port. Also satisfies
 * {@link ExistsColumnaAsignadaPort} by delegating to the board-side query on
 * {@link TableroRepository}; the {@code TableroRepository} is an infrastructure
 * detail and does not leak across the port boundary.
 */
@RequiredArgsConstructor
public class ColumnaRepositoryAdapter
        implements SaveColumnaPort, FindAllColumnasPort, FindColumnaByIdPort, DeleteColumnaByIdPort,
                   ExistsColumnaAsignadaPort {

    private final ColumnaRepository repository;
    private final TableroRepository tableroRepository;

    @Override
    public Columna save(Columna columna) {
        ColumnaEntity entity = ColumnaMapper.toEntity(columna);
        ColumnaEntity saved = repository.save(entity);
        return ColumnaMapper.toDomain(saved);
    }

    @Override
    public List<Columna> findAll() {
        return repository.findAll().stream()
            .map(ColumnaMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Columna> findById(ColumnaId id) {
        return repository.findById(id.value().toString())
            .map(ColumnaMapper::toDomain);
    }

    @Override
    public void deleteById(ColumnaId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public boolean existsByColumnaId(ColumnaId columnaId) {
        return tableroRepository.existsByColumnasTableroColumnaId(columnaId.value().toString());
    }
}
