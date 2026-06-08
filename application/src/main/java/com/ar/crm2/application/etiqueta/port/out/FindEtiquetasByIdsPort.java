package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.vo.EtiquetaId;

import java.util.List;

/**
 * Granular outbound port for finding Etiquetas by a set of ids.
 * Used by Ficha creation/edit to resolve etiquetaIds into domain entities.
 */
public interface FindEtiquetasByIdsPort {
    List<Etiqueta> findByIds(List<EtiquetaId> ids);
}
