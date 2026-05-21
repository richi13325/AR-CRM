package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.SuperUsuarioEntity;
import com.ar.crm2.adapter.out.persistence.mapper.SuperUsuarioMapper;
import com.ar.crm2.adapter.out.persistence.repository.SuperUsuarioRepository;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.out.DeleteSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.FindAllSuperUsuariosPort;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.SaveSuperUsuarioPort;
import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing SuperUsuario outbound ports.
 * Bridges application outbound port contracts to Spring Data JPA storage.
 */
@Repository
@RequiredArgsConstructor
public class SuperUsuarioRepositoryAdapter implements SaveSuperUsuarioPort, FindAllSuperUsuariosPort, FindSuperUsuarioByIdPort, DeleteSuperUsuarioByIdPort {

    private final SuperUsuarioRepository repository;

    @Override
    public SuperUsuario save(SuperUsuario usuario) {
        SuperUsuarioEntity entity = SuperUsuarioMapper.toEntity(usuario);
        SuperUsuarioEntity saved = repository.save(entity);
        return SuperUsuarioMapper.toDomain(saved);
    }

    @Override
    public List<SuperUsuario> findAll() {
        return repository.findAll().stream()
            .map(SuperUsuarioMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<SuperUsuario> findById(SuperUsuarioId id) {
        return repository.findById(id.value().toString())
            .map(SuperUsuarioMapper::toDomain);
    }

    @Override
    public void deleteById(SuperUsuarioId id) {
        repository.deleteById(id.value().toString());
    }
}