package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.whatsapp.application.conversacion.port.in.GetCsatResumenUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.CsatResumenPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.CsatResumenPort.CsatResumen;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetCsatResumenService implements GetCsatResumenUseCase {

    private final CsatResumenPort port;

    @Override
    public CsatResumen get() {
        return port.obtener();
    }
}
