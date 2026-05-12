package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.repository.EmpresaRepository;
import com.ar.crm2.application.empresa.port.out.EmpresaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Persistence adapter implementing EmpresaRepositoryPort.
 * Bridges application outbound port to Spring Data JPA storage.
 */
@Repository
@RequiredArgsConstructor
public class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

    private final EmpresaRepository repository;

    @Override
    public com.ar.crm2.model.entity.Empresa save(com.ar.crm2.model.entity.Empresa empresa) {
        var entity = com.ar.crm2.adapter.out.persistence.mapper.EmpresaPersistenceMapper.toEntity(empresa);
        var saved = repository.save(entity);
        return com.ar.crm2.adapter.out.persistence.mapper.EmpresaPersistenceMapper.toDomain(saved);
    }

    @Override
    public java.util.List<com.ar.crm2.model.entity.Empresa> findAll() {
        return repository.findAll().stream()
            .map(com.ar.crm2.adapter.out.persistence.mapper.EmpresaPersistenceMapper::toDomain)
            .toList();
    }

    @Override
    public com.ar.crm2.model.entity.Empresa findById(com.ar.crm2.model.vo.EmpresaId id) {
        return repository.findById(id.value())
            .map(com.ar.crm2.adapter.out.persistence.mapper.EmpresaPersistenceMapper::toDomain)
            .orElse(null);
    }
}