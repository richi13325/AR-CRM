package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCanalWaRequest(
        @NotNull UUID empresaId,
        @NotBlank String nombre,
        String instanceName,
        String apiUrl,
        String apiKey
) {}
