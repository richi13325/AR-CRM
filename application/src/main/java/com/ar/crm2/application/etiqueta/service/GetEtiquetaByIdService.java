package com.ar.crm2.application.etiqueta.service;

import com.ar.crm2.application.etiqueta.exception.EtiquetaNotFoundException;
import com.ar.crm2.application.etiqueta.port.in.GetEtiquetaByIdUseCase;
import com.ar.crm2.application.etiqueta.port.out.FindEtiquetaByIdPort;
import com.ar.crm2.application.etiqueta.command.GetEtiquetaByIdCommand;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import lombok.RequiredArgsConstructor;

/**
 * Application service for retrieving a single Etiqueta by id.
 *
 * <p>Note: Transaction boundary is owned by the infrastructure adapter.
 */
@RequiredArgsConstructor
public class GetEtiquetaByIdService implements GetEtiquetaByIdUseCase {

    private final FindEtiquetaByIdPort findPort;

    @Override
    public Etiqueta getById(GetEtiquetaByIdCommand command) {
        EtiquetaId id = EtiquetaId.from(command.id());
        return findPort.findById(id)
            .orElseThrow(() -> EtiquetaNotFoundException.forId(command.id()));
    }
}
