package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMensajeWaRequest(
        @NotNull TipoMensaje tipo,
        @NotBlank String contenido,
        String mediaUrl
) {}
