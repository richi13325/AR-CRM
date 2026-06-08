package com.ar.crm2.application.etiqueta.exception;

import com.ar.crm2.model.vo.EtiquetaId;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Exception thrown when an Etiqueta cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class EtiquetaNotFoundException extends RuntimeException {

    private final UUID etiquetaId;
    private final List<UUID> missingIds;

    private EtiquetaNotFoundException(UUID etiquetaId, String message) {
        super(message);
        this.etiquetaId = etiquetaId;
        this.missingIds = List.of();
    }

    private EtiquetaNotFoundException(String message, List<UUID> missingIds) {
        super(message);
        this.etiquetaId = null;
        this.missingIds = missingIds == null ? List.of() : List.copyOf(missingIds);
    }

    public static EtiquetaNotFoundException forId(UUID id) {
        return new EtiquetaNotFoundException(id, "Etiqueta no encontrada con id: " + id);
    }

    /**
     * Builds an exception describing a set of EtiquetaIds that the caller
     * requested but the catalog could not resolve. The aggregated ids are
     * exposed via {@link #getMissingIds()} so callers can surface a precise
     * validation error instead of silently dropping the missing tags.
     */
    public static EtiquetaNotFoundException forMissingIds(List<EtiquetaId> missing) {
        List<UUID> uuids = missing.stream()
            .map(EtiquetaId::value)
            .collect(Collectors.toUnmodifiableList());
        return new EtiquetaNotFoundException(
            "No se encontraron las siguientes etiquetas en el catálogo: " + uuids,
            uuids
        );
    }

    public UUID getEtiquetaId() {
        return etiquetaId;
    }

    /**
     * @return the list of ids the caller requested that were not found in the catalog.
     *         Empty when the exception was raised for a single id.
     */
    public List<UUID> getMissingIds() {
        return missingIds;
    }
}
