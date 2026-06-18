package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateBotRequest(
        @NotBlank String nombre,
        UUID canalId,
        @NotBlank String webhookUrl
) {
}
