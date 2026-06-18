package com.ar.crm2.adapter.in.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/** Body de POST .../conversations/{id}/messages — contrato Chatwoot/AmbarCRM. */
public record BotMessageRequest(
        @NotBlank String content,
        @JsonProperty("message_type") String messageType,
        @JsonProperty("private") boolean privado
) {
}
