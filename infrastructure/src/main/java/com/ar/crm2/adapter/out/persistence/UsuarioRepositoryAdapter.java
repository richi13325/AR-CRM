package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.UsuarioEntity;
import com.ar.crm2.adapter.out.persistence.mapper.UsuarioMapper;
import com.ar.crm2.adapter.out.persistence.repository.UsuarioRepository;
import com.ar.crm2.application.rol.port.out.ExistsUsuariosByRolIdPort;
import com.ar.crm2.application.usuario.port.out.DeleteUsuarioByIdPort;
import com.ar.crm2.application.usuario.port.out.FindAllUsuariosPort;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByKeycloakIdPort;
import com.ar.crm2.application.usuario.port.out.SaveUsuarioPort;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing Usuario outbound ports and ExistsUsuariosByRolIdPort.
 * Bridges application outbound port contracts to Spring Data JPA storage.
 * Replaces the temporary fail-closed implementation in RolRepositoryAdapter.
 */
@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements SaveUsuarioPort, FindAllUsuariosPort, FindUsuarioByIdPort, DeleteUsuarioByIdPort, ExistsUsuariosByRolIdPort, FindUsuarioByKeycloakIdPort {

    private final UsuarioRepository repository;

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = UsuarioMapper.toEntity(usuario);
        UsuarioEntity saved = repository.save(entity);
        return UsuarioMapper.toDomain(saved);
    }

    @Override
    public List<Usuario> findAll() {
        return repository.findAll().stream()
            .map(UsuarioMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Usuario> findById(UsuarioId id) {
        return repository.findById(id.value().toString())
            .map(UsuarioMapper::toDomain);
    }

    @Override
    public void deleteById(UsuarioId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public boolean existsUsuariosByRolId(RolId rolId) {
        return repository.existsByRolId(rolId.value().toString());
    }

    @Override
    public Optional<Usuario> findByKeycloakId(String keycloakId) {
        return repository.findByKeycloakId(keycloakId)
            .map(UsuarioMapper::toDomain);
    }
}