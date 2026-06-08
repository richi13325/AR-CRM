package com.ar.crm2.application.etiqueta.port.in;

import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;

import java.util.List;
import java.util.Optional;

/**
 * Inbound input port for listing Etiquetas, optionally filtered by type.
 */
public interface GetAllEtiquetasUseCase {

    /**
     * Returns the list of Etiquetas in the global catalog, filtered by the
     * given tipoEtiqueta when present.
     *
     * @param tipoEtiqueta optional type filter; empty/absent returns all
     * @return the matching list of Etiquetas
     */
    List<Etiqueta> getAll(Optional<TipoEtiqueta> tipoEtiqueta);
}
