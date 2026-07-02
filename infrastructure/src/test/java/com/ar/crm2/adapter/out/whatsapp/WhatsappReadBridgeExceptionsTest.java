package com.ar.crm2.adapter.out.whatsapp;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the read-bridge exception factories used by
 * {@link WhatsappConversacionLecturaAdapter} to make missing-row
 * branches explicit at the call site.
 *
 * <p>The exceptions are caught and mapped to {@code Optional.empty()}
 * at the adapter boundary so the application port contract is
 * preserved; the factories themselves are the testable surface.
 */
class WhatsappReadBridgeExceptionsTest {

    @Test
    void whatsappConversacionNotFoundException_forId_keepsIdAndBuildsMessage() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");

        WhatsappConversacionNotFoundException ex =
            WhatsappConversacionNotFoundException.forId(id);

        assertNotNull(ex);
        assertEquals(id, ex.waConversacionId());
        assertTrue(ex.getMessage().contains(id.toString()),
            "message should mention the missing id for operator/debug clarity");
    }

    @Test
    void whatsappCanalNotFoundException_forId_keepsIdAndBuildsMessage() {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");

        WhatsappCanalNotFoundException ex = WhatsappCanalNotFoundException.forId(id);

        assertNotNull(ex);
        assertEquals(id, ex.canalId());
        assertTrue(ex.getMessage().contains(id.toString()),
            "message should mention the missing canal id for operator/debug clarity");
    }
}