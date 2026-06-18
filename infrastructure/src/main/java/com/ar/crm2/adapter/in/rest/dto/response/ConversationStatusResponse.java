package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Contrato Chatwoot/AmbarCRM: GET .../conversations/{id} → {status, bot_activo, labels}. */
public record ConversationStatusResponse(
        String status,
        @JsonProperty("bot_activo") boolean botActivo,
        List<String> labels
) {
    public static ConversationStatusResponse fromDomain(Conversacion c) {
        String status = switch (c.getEstado()) {
            case ABIERTA -> "open";
            case EN_ESPERA -> "pending";
            case CERRADA -> "resolved";
        };
        return new ConversationStatusResponse(status, c.isBotActivo(), List.copyOf(c.getLabels()));
    }
}
