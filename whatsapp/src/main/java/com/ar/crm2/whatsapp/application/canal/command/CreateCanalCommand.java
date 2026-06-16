package com.ar.crm2.whatsapp.application.canal.command;

import com.ar.crm2.whatsapp.domain.enums.ProveedorCanal;

import java.util.UUID;

public record CreateCanalCommand(
        UUID empresaId,
        String nombre,
        String instanceName,
        ProveedorCanal proveedor,
        String apiUrl,
        String apiKey
) {
    public CreateCanalCommand {
        if (empresaId == null) throw new IllegalArgumentException("empresaId es requerido");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre es requerido");
        if (instanceName == null || instanceName.isBlank()) throw new IllegalArgumentException("instanceName es requerido");
        if (proveedor == null) throw new IllegalArgumentException("proveedor es requerido");
        if (apiUrl == null || apiUrl.isBlank()) throw new IllegalArgumentException("apiUrl es requerido");
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("apiKey es requerido");
    }
}
