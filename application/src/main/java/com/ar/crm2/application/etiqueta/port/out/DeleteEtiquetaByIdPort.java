package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.vo.EtiquetaId;

/**
 * Granular outbound port for deleting an Etiqueta by id.
 */
public interface DeleteEtiquetaByIdPort {
    void deleteById(EtiquetaId id);
}
