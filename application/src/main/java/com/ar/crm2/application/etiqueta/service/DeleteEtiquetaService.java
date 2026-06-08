package com.ar.crm2.application.etiqueta.service;

import com.ar.crm2.application.etiqueta.command.DeleteEtiquetaCommand;
import com.ar.crm2.application.etiqueta.exception.EtiquetaNotFoundException;
import com.ar.crm2.application.etiqueta.exception.EtiquetaRequiresConfirmationException;
import com.ar.crm2.application.etiqueta.port.in.DeleteEtiquetaUseCase;
import com.ar.crm2.application.etiqueta.port.out.CountFichaEtiquetasByEtiquetaIdPort;
import com.ar.crm2.application.etiqueta.port.out.DeleteEtiquetaByIdPort;
import com.ar.crm2.application.etiqueta.port.out.DeleteFichaEtiquetasByEtiquetaIdPort;
import com.ar.crm2.application.etiqueta.port.out.FindEtiquetaByIdPort;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import lombok.RequiredArgsConstructor;

/**
 * Application service for deleting an Etiqueta.
 *
 * <p>When the Etiqueta is referenced by one or more FichaEtiqueta rows,
 * the caller must pass {@code confirm=true} or the call is rejected
 * with {@link EtiquetaRequiresConfirmationException}.
 *
 * <p>On confirmed delete: the FichaEtiqueta rows are removed first, then
 * the catalog row. The infrastructure adapter must wrap this call in a
 * single transaction so the two deletes are atomic.
 */
@RequiredArgsConstructor
public class DeleteEtiquetaService implements DeleteEtiquetaUseCase {

    private final FindEtiquetaByIdPort findPort;
    private final CountFichaEtiquetasByEtiquetaIdPort countPort;
    private final DeleteFichaEtiquetasByEtiquetaIdPort deleteRelPort;
    private final DeleteEtiquetaByIdPort deleteByIdPort;

    @Override
    public void delete(DeleteEtiquetaCommand command) {
        EtiquetaId id = EtiquetaId.from(command.id());
        Etiqueta existing = findPort.findById(id)
            .orElseThrow(() -> EtiquetaNotFoundException.forId(command.id()));

        long relationCount = countPort.countByEtiquetaId(existing.getId());
        if (relationCount > 0 && !command.confirm()) {
            throw new EtiquetaRequiresConfirmationException();
        }

        deleteRelPort.deleteByEtiquetaId(existing.getId());
        deleteByIdPort.deleteById(existing.getId());
    }
}
