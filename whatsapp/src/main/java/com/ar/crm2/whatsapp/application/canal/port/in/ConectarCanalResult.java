package com.ar.crm2.whatsapp.application.canal.port.in;

import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;

public record ConectarCanalResult(String qrBase64, EstadoCanal estado) {}
