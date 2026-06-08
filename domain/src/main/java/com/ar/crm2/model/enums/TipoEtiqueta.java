package com.ar.crm2.model.enums;

import com.ar.crm2.exception.InvariantViolationException;

/**
 * Tag (etiqueta) type supported by the global catalog.
 * Each Ficha may only be associated with Etiquetas of its own type
 * (TAREA fiches with TAREA etiquetas, TRATO fiches with TRATO etiquetas).
 */
public enum TipoEtiqueta {
    TAREA,
    TRATO;

    /**
     * Maps a {@link TipoFicha} to the corresponding tag type.
     * Throws when given a null ficha type.
     *
     * @param tipoFicha the ficha type to translate
     * @return the matching tag type
     * @throws InvariantViolationException if tipoFicha is null
     */
    public static TipoEtiqueta fromFicha(TipoFicha tipoFicha) {
        if (tipoFicha == null) {
            throw new InvariantViolationException("tipoFicha es obligatorio para derivar TipoEtiqueta.");
        }
        return tipoFicha == TipoFicha.TAREA ? TAREA : TRATO;
    }
}
