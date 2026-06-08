package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.vo.EtiquetaId;

/**
 * Granular outbound port for counting FichaEtiqueta rows referencing a given Etiqueta.
 * Used to decide whether a delete requires explicit confirmation.
 */
public interface CountFichaEtiquetasByEtiquetaIdPort {
    long countByEtiquetaId(EtiquetaId id);
}
