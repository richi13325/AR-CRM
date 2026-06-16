package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReceiveWebhookRequest(
        @NotNull UUID canalId,
        @NotBlank String waMessageId,
        @NotBlank String numeroTelefono,
        String nombreContacto,
        @NotNull TipoMensaje tipo,
        String contenido,
        String mediaUrl
) {}
