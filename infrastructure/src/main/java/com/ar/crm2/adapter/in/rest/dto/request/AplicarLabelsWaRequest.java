package com.ar.crm2.adapter.in.rest.dto.request;

import java.util.List;

/** Toggle de bot/handoff desde el panel de chat humano (JWT). Mismo contrato de labels que el bot. */
public record AplicarLabelsWaRequest(List<String> labels) {
}
