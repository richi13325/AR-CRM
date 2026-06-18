package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.mapper.CanalWhatsappMapper;
import com.ar.crm2.adapter.out.persistence.repository.CanalWhatsappRepository;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.application.canal.port.out.DeleteCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindAllCanalesByEmpresaPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindAllCanalesPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.SaveCanalPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CanalWhatsappRepositoryAdapter
        implements SaveCanalPort, FindCanalByIdPort, FindAllCanalesByEmpresaPort, FindAllCanalesPort, DeleteCanalByIdPort {

    private final CanalWhatsappRepository repository;

    @Override
    public CanalWhatsapp save(CanalWhatsapp canal) {
        return CanalWhatsappMapper.toDomain(repository.save(CanalWhatsappMapper.toEntity(canal)));
    }

    @Override
    public Optional<CanalWhatsapp> findById(CanalWhatsappId id) {
        return repository.findById(id.value().toString()).map(CanalWhatsappMapper::toDomain);
    }

    @Override
    public List<CanalWhatsapp> findAllByEmpresaId(EmpresaId empresaId) {
        return repository.findByEmpresaId(empresaId.value().toString()).stream()
                .map(CanalWhatsappMapper::toDomain)
                .toList();
    }

    @Override
    public List<CanalWhatsapp> findAll() {
        return repository.findAll().stream().map(CanalWhatsappMapper::toDomain).toList();
    }

    @Override
    public void deleteById(CanalWhatsappId id) {
        repository.deleteById(id.value().toString());
    }
}
