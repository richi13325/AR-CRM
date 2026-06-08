package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;

import java.util.List;
import java.util.Optional;

/**
 * Granular outbound port for listing Etiquetas with optional type filter.
 */
public interface FindAllEtiquetasPort {
    List<Etiqueta> findAll(Optional<TipoEtiqueta> tipoEtiqueta);
}
