package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.CanalWhatsappEntity;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;

import java.util.UUID;

public final class CanalWhatsappMapper {

    private CanalWhatsappMapper() {}

    public static CanalWhatsappEntity toEntity(CanalWhatsapp domain) {
        return CanalWhatsappEntity.builder()
                .id(domain.getId().value().toString())
                .empresaId(domain.getEmpresaId().value().toString())
                .nombre(domain.getNombre())
                .instanceName(domain.getInstanceName())
                .proveedor(domain.getProveedor())
                .estado(domain.getEstado())
                .apiUrl(domain.getApiUrl())
                .apiKey(domain.getApiKey())
                .creadoEn(domain.getCreadoEn())
                .actualizadoEn(domain.getActualizadoEn())
                .build();
    }

    public static CanalWhatsapp toDomain(CanalWhatsappEntity entity) {
        return CanalWhatsapp.reconstitute(
                CanalWhatsappId.from(UUID.fromString(entity.getId())),
                EmpresaId.from(UUID.fromString(entity.getEmpresaId())),
                entity.getNombre(),
                entity.getInstanceName(),
                entity.getProveedor(),
                entity.getEstado(),
                entity.getApiUrl(),
                entity.getApiKey(),
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
