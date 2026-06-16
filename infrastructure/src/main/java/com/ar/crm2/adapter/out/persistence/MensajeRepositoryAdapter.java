package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.mapper.MensajeMapper;
import com.ar.crm2.adapter.out.persistence.repository.MensajeRepository;
import com.ar.crm2.whatsapp.application.mensaje.port.out.ExistsMensajeByWaMessageIdPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.FindMensajesByConversacionIdPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SaveMensajePort;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MensajeRepositoryAdapter
        implements SaveMensajePort, FindMensajesByConversacionIdPort, ExistsMensajeByWaMessageIdPort {

    private final MensajeRepository repository;

    @Override
    public Mensaje save(Mensaje mensaje) {
        return MensajeMapper.toDomain(repository.save(MensajeMapper.toEntity(mensaje)));
    }

    @Override
    public List<Mensaje> findByConversacionId(ConversacionId conversacionId) {
        return repository.findByConversacionIdOrderByCreadoEnAsc(conversacionId.value().toString()).stream()
                .map(MensajeMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByWaMessageId(String waMessageId) {
        return repository.existsByWaMessageId(waMessageId);
    }
}
