package com.ar.crm2.application.etiqueta.exception;

/**
 * Exception thrown when an Etiqueta in use is requested to be deleted
 * without explicit confirmation. Caller must re-issue the delete with
 * confirm=true to cascade the relation rows and the catalog row.
 */
public class EtiquetaRequiresConfirmationException extends RuntimeException {

    public EtiquetaRequiresConfirmationException() {
        super("La etiqueta está en uso; se requiere confirmación explícita para eliminarla.");
    }

    public EtiquetaRequiresConfirmationException(String message) {
        super(message);
    }
}
