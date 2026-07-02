package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.EmpresaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.EmpresaMapper;
import com.ar.crm2.adapter.out.persistence.repository.EmpresaRepository;
import com.ar.crm2.application.empresa.port.out.DeleteEmpresaByIdPort;
import com.ar.crm2.application.empresa.port.out.ExistsTratosByEmpresaIdPort;
import com.ar.crm2.application.empresa.port.out.FindAllEmpresasPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresaByIdPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.port.out.SaveEmpresaPort;
import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EmpresaRepositoryAdapter implements SaveEmpresaPort, FindAllEmpresasPort, FindEmpresaByIdPort, DeleteEmpresaByIdPort, ExistsTratosByEmpresaIdPort, FindEmpresasByCreadorPort {

    private final EmpresaRepository repository;

    @Override
    public Empresa save(Empresa empresa) {
        EmpresaEntity entity = EmpresaMapper.toEntity(empresa);
        EmpresaEntity saved = repository.save(entity);
        return EmpresaMapper.toDomain(saved);
    }

    @Override
    public List<Empresa> findAll() {
        return repository.findAll().stream()
            .map(EmpresaMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Empresa> findById(EmpresaId id) {
        return repository.findById(id.value().toString())
            .map(EmpresaMapper::toDomain);
    }

    @Override
    public void deleteById(EmpresaId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public boolean existsTratosByEmpresaId(EmpresaId empresaId) {
        return repository.existsTratosByEmpresaId(empresaId.value().toString());
    }

    /**
     * PR 2 (add-crm-ai-assistant-spring-ai) — tenant bridge.
     *
     * <p>Resolves the set of companies owned by the requester using
     * {@code Empresa.creadoPor == usuarioId}. Returns a lightweight
     * {@link EmpresaId} projection; full Empresa hydration is not
     * needed at this boundary (the AI services only check membership
     * to authorize access to WhatsApp conversations and AI resources).
     */
    @Override
    public List<EmpresaId> findEmpresasByCreador(UsuarioId creadoPor) {
        return repository.findByCreadoPor(creadoPor.value().toString()).stream()
            .map(entity -> EmpresaId.from(java.util.UUID.fromString(entity.getId())))
            .toList();
    }
}
