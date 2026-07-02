package com.ar.crm2.adapter.out.whatsapp;

import java.util.UUID;

/**
 * Thrown by the AI read-bridge adapters when a WhatsApp conversation
 * is missing in the persistence layer.
 *
 * <p>This is an infrastructure-level control-flow exception used
 * internally by
 * {@link WhatsappConversacionLecturaAdapter} to make the missing-row
 * case explicit. The bridge catches it and surfaces an
 * {@link java.util.Optional#empty()} to honor the application port
 * contract ({@code WhatsappConversacionLecturaPort.findById} returns
 * {@code Optional}).
 *
 * <p>Tenant authorization is still enforced by the AI application
 * service that consumes the port.
 */
public class WhatsappConversacionNotFoundException extends RuntimeException {

    private final UUID waConversacionId;

    private WhatsappConversacionNotFoundException(UUID waConversacionId, String message) {
        super(message);
        this.waConversacionId = waConversacionId;
    }

    public static WhatsappConversacionNotFoundException forId(UUID waConversacionId) {
        return new WhatsappConversacionNotFoundException(
            waConversacionId,
            "WhatsApp conversation " + waConversacionId + " was not found."
        );
    }

    public UUID waConversacionId() {
        return waConversacionId;
    }
}