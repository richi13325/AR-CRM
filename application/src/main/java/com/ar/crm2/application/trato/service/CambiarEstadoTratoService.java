package com.ar.crm2.application.trato.service;

import com.ar.crm2.application.trato.exception.TratoNotFoundException;
import com.ar.crm2.application.trato.port.in.CambiarEstadoTratoUseCase;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.application.trato.port.out.SaveTratoPort;
import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.vo.TratoId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CambiarEstadoTratoService implements CambiarEstadoTratoUseCase {

    private final FindTratoByIdPort findPort;
    private final SaveTratoPort savePort;

    @Override
    public Trato ganar(UUID id) {
        return savePort.save(cargar(id).ganar());
    }

    @Override
    public Trato perder(UUID id, String motivo) {
        return savePort.save(cargar(id).perder(motivo));
    }

    private Trato cargar(UUID id) {
        return findPort.findById(TratoId.from(id))
                .orElseThrow(() -> TratoNotFoundException.forId(id));
    }
}
