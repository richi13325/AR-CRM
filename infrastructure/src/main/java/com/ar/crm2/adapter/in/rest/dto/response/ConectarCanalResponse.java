package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;

public record ConectarCanalResponse(String qrBase64, EstadoCanal estado) {}
