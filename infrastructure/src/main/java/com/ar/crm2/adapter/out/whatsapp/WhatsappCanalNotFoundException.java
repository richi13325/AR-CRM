package com.ar.crm2.adapter.out.whatsapp;

import java.util.UUID;

/**
 * Thrown by the AI read-bridge adapters when a WhatsApp channel
 * referenced by an existing conversation is missing in the
 * persistence layer (orphan canal reference).
 *
 * <p>This is an infrastructure-level control-flow exception used
 * internally by
 * {@link WhatsappConversacionLecturaAdapter} to make the
 * missing-canal case explicit. The bridge catches it and surfaces an
 * {@link java.util.Optional#empty()} to honor the application port
 * contract ({@code WhatsappConversacionLecturaPort.findById} returns
 * {@code Optional}).
 *
 * <p>Tenant authorization is still enforced by the AI application
 * service that consumes the port.
 */
public class WhatsappCanalNotFoundException extends RuntimeException {

    private final UUID canalId;

    private WhatsappCanalNotFoundException(UUID canalId, String message) {
        super(message);
        this.canalId = canalId;
    }

    public static WhatsappCanalNotFoundException forId(UUID canalId) {
        return new WhatsappCanalNotFoundException(
            canalId,
            "WhatsApp channel " + canalId + " was not found."
        );
    }

    public UUID canalId() {
        return canalId;
    }
}