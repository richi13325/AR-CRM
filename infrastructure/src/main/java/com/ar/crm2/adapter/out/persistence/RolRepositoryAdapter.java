package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.RolEntity;
import com.ar.crm2.adapter.out.persistence.mapper.RolMapper;
import com.ar.crm2.adapter.out.persistence.repository.RolRepository;
import com.ar.crm2.application.rol.port.out.DeleteRolByIdPort;
import com.ar.crm2.application.rol.port.out.FindAllRolesPort;
import com.ar.crm2.application.rol.port.out.FindRolByIdPort;
import com.ar.crm2.application.rol.port.out.SaveRolPort;
import com.ar.crm2.model.entity.Rol;
import com.ar.crm2.model.vo.RolId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RolRepositoryAdapter implements SaveRolPort, FindAllRolesPort, FindRolByIdPort, DeleteRolByIdPort {

    private final RolRepository repository;

    @Override
    public Rol save(Rol rol) {
        RolEntity entity = RolMapper.toEntity(rol);
        RolEntity saved = repository.save(entity);
        return RolMapper.toDomain(saved);
    }

    @Override
    public List<Rol> findAll() {
        return repository.findAll().stream()
            .map(RolMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Rol> findById(RolId id) {
        return repository.findById(id.value().toString())
            .map(RolMapper::toDomain);
    }

    @Override
    public void deleteById(RolId id) {
        repository.deleteById(id.value().toString());
    }
}
