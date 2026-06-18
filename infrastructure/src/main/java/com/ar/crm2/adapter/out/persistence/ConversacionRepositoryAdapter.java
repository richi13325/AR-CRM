package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.mapper.ConversacionMapper;
import com.ar.crm2.adapter.out.persistence.repository.ConversacionRepository;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindAllConversacionesByEmpresaPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByTelefonoYCanalPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ConversacionRepositoryAdapter
        implements SaveConversacionPort, FindConversacionByIdPort,
                   FindConversacionByTelefonoYCanalPort, FindAllConversacionesByEmpresaPort {

    private final ConversacionRepository repository;

    @Override
    public Conversacion save(Conversacion conversacion) {
        return ConversacionMapper.toDomain(repository.save(ConversacionMapper.toEntity(conversacion)));
    }

    @Override
    public Optional<Conversacion> findById(ConversacionId id) {
        return repository.findById(id.value().toString()).map(ConversacionMapper::toDomain);
    }

    @Override
    public Optional<Conversacion> findByTelefonoAndCanal(String numeroTelefono, CanalWhatsappId canalId) {
        return repository.findByCanalIdAndNumeroTelefono(canalId.value().toString(), numeroTelefono)
                .map(ConversacionMapper::toDomain);
    }

    @Override
    public List<Conversacion> findAllByEmpresaId(EmpresaId empresaId) {
        return repository.findByEmpresaId(empresaId.value().toString()).stream()
                .map(ConversacionMapper::toDomain)
                .toList();
    }
}
