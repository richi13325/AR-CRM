package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.TableroEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ColumnaMapper;
import com.ar.crm2.adapter.out.persistence.mapper.TableroMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.application.tablero.port.out.DeleteTableroByIdPort;
import com.ar.crm2.application.tablero.port.out.ExistsColumnaEnTableroPort;
import com.ar.crm2.application.tablero.port.out.FindAllTablerosPort;
import com.ar.crm2.application.tablero.port.out.FindColumnaByIdPort;
import com.ar.crm2.application.tablero.port.out.FindTableroByIdPort;
import com.ar.crm2.application.tablero.port.out.SaveTableroPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.TableroId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TableroRepositoryAdapter implements SaveTableroPort, FindAllTablerosPort, FindTableroByIdPort, DeleteTableroByIdPort, ExistsColumnaEnTableroPort, FindColumnaByIdPort {

    private final TableroRepository repository;
    private final ColumnaRepository columnaRepository;
    private final TableroMapper mapper;

    @Override
    public Tablero save(Tablero tablero) {
        TableroEntity entity = mapper.toEntity(tablero);
        TableroEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Tablero> findAll() {
        return repository.findAll().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Tablero> findById(TableroId id) {
        return repository.findById(id.value().toString())
            .map(mapper::toDomain);
    }

    @Override
    public void deleteById(TableroId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public boolean existsByTableroIdAndColumnaId(TableroId tableroId, ColumnaId columnaId) {
        return repository.existsByIdAndColumnasTableroColumnaId(
            tableroId.value().toString(),
            columnaId.value().toString()
        );
    }

    @Override
    public Optional<Columna> findById(ColumnaId id) {
        return columnaRepository.findById(id.value().toString())
            .map(ColumnaMapper::toDomain);
    }
}
