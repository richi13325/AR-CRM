package com.ar.crm2.application.trato.service;

import com.ar.crm2.application.notatrato.port.out.SaveNotaTratoPort;
import com.ar.crm2.application.trato.exception.TratoNotFoundException;
import com.ar.crm2.application.trato.port.in.CambiarEstadoTratoUseCase;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.application.trato.port.out.SaveTratoPort;
import com.ar.crm2.model.entity.NotaTrato;
import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.vo.TratoId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CambiarEstadoTratoService implements CambiarEstadoTratoUseCase {

    private final FindTratoByIdPort findPort;
    private final SaveTratoPort savePort;
    private final SaveNotaTratoPort notaPort;

    @Override
    public Trato ganar(UUID id) {
        Trato t = savePort.save(cargar(id).ganar());
        notaPort.save(NotaTrato.crearEvento(t.getId(), "Oportunidad marcada como GANADA"));
        return t;
    }

    @Override
    public Trato perder(UUID id, String motivo) {
        Trato t = savePort.save(cargar(id).perder(motivo));
        notaPort.save(NotaTrato.crearEvento(t.getId(), "Oportunidad marcada como PERDIDA: " + motivo));
        return t;
    }

    private Trato cargar(UUID id) {
        return findPort.findById(TratoId.from(id))
                .orElseThrow(() -> TratoNotFoundException.forId(id));
    }
}
