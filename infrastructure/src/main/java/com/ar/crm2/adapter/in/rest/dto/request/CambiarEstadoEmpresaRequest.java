package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.EstadoRelacion;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoEmpresaRequest(
    @NotNull(message = "nuevoEstado is required")
    EstadoRelacion nuevoEstado
) {}
