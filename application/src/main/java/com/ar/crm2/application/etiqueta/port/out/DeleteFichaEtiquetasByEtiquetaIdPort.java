package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.vo.EtiquetaId;

/**
 * Granular outbound port for cascade-deleting all FichaEtiqueta rows
 * that reference a given Etiqueta id.
 */
public interface DeleteFichaEtiquetasByEtiquetaIdPort {
    void deleteByEtiquetaId(EtiquetaId id);
}
