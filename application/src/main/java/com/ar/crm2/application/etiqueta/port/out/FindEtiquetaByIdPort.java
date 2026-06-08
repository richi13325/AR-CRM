package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.vo.EtiquetaId;

import java.util.Optional;

/**
 * Granular outbound port for finding an Etiqueta by its id.
 */
public interface FindEtiquetaByIdPort {
    Optional<Etiqueta> findById(EtiquetaId id);
}
