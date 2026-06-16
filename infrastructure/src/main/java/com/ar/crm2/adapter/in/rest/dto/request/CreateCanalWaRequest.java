package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.whatsapp.domain.enums.ProveedorCanal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCanalWaRequest(
        @NotNull UUID empresaId,
        @NotBlank String nombre,
        @NotBlank String instanceName,
        @NotNull ProveedorCanal proveedor,
        @NotBlank String apiUrl,
        @NotBlank String apiKey
) {}
