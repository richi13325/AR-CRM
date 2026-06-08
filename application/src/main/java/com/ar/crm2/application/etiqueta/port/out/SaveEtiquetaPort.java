package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.entity.Etiqueta;

/**
 * Granular outbound port for persisting a new or updated Etiqueta.
 */
public interface SaveEtiquetaPort {
    Etiqueta save(Etiqueta etiqueta);
}
