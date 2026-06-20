package com.ar.crm2.application.notatrato.service;

import com.ar.crm2.application.notatrato.port.in.CrearNotaTratoUseCase;
import com.ar.crm2.application.notatrato.port.out.SaveNotaTratoPort;
import com.ar.crm2.application.trato.exception.TratoNotFoundException;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.model.entity.NotaTrato;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CrearNotaTratoService implements CrearNotaTratoUseCase {

    private final FindTratoByIdPort findTratoPort;
    private final SaveNotaTratoPort savePort;

    @Override
    public NotaTrato crear(UUID tratoId, UUID autorId, String contenido) {
        TratoId id = TratoId.from(tratoId);
        if (findTratoPort.findById(id).isEmpty()) {
            throw TratoNotFoundException.forId(tratoId);
        }
        return savePort.save(NotaTrato.crearNota(id, UsuarioId.from(autorId), contenido));
    }
}
