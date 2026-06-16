package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import com.ar.crm2.whatsapp.domain.enums.ProveedorCanal;

import java.time.LocalDateTime;
import java.util.UUID;

public record CanalWaResponse(
        UUID id,
        UUID empresaId,
        String nombre,
        String instanceName,
        ProveedorCanal proveedor,
        EstadoCanal estado,
        String apiUrl,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {
    public static CanalWaResponse fromDomain(CanalWhatsapp c) {
        return new CanalWaResponse(
                c.getId().value(),
                c.getEmpresaId().value(),
                c.getNombre(),
                c.getInstanceName(),
                c.getProveedor(),
                c.getEstado(),
                c.getApiUrl(),
                c.getCreadoEn(),
                c.getActualizadoEn()
        );
    }
}
