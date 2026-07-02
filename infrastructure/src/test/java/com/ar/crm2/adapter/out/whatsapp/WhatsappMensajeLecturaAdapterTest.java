package com.ar.crm2.adapter.out.whatsapp;

import com.ar.crm2.adapter.out.persistence.MensajeRepositoryAdapter;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import com.ar.crm2.whatsapp.domain.vo.MensajeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WhatsappMensajeLecturaAdapter}.
 *
 * <p>Verifies the bridge contract: the adapter delegates to the
 * existing {@link MensajeRepositoryAdapter} for the ordered transcript
 * and maps each {@code whatsapp.domain.entity.Mensaje} into the
 * application-owned {@link WhatsappMensajeResumen} projection. The
 * application layer must never import the {@code whatsapp} module
 * (see design.md §Module Boundaries); this adapter is the only
 * boundary that does the translation.
 */
@ExtendWith(MockitoExtension.class)
class WhatsappMensajeLecturaAdapterTest {

    @Mock
    private MensajeRepositoryAdapter mensajeRepositoryAdapter;

    @InjectMocks
    private WhatsappMensajeLecturaAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private Mensaje createMensaje(String id, String conversacionId, DireccionMensaje dir) {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        return Mensaje.reconstitute(
            MensajeId.from(UUID.fromString(id)),
            ConversacionId.from(UUID.fromString(conversacionId)),
            "wa-msg-1",
            TipoMensaje.TEXTO,
            dir,
            "hola",
            null,
            StatusMensaje.ENTREGADO,
            null,
            false,
            ahora
        );
    }

    // ── findByConversacionId ────────────────────────────────────────

    @Test
    void findByConversacionId_shouldDelegateToMensajeAdapterAndMapToApplicationProjection() {
        UUID waConv = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Mensaje incoming = createMensaje(
            "11111111-1111-1111-1111-111111111111",
            waConv.toString(), DireccionMensaje.ENTRANTE
        );
        Mensaje outgoing = createMensaje(
            "22222222-2222-2222-2222-222222222222",
            waConv.toString(), DireccionMensaje.SALIENTE
        );
        when(mensajeRepositoryAdapter.findByConversacionId(ConversacionId.from(waConv)))
            .thenReturn(List.of(incoming, outgoing));

        List<WhatsappMensajeResumen> result = adapter.findByConversacionId(waConv);

        assertEquals(2, result.size());
        assertEquals("ENTRANTE", result.get(0).direccion());
        assertEquals("SALIENTE", result.get(1).direccion());
        assertEquals("TEXTO", result.get(0).tipo());
        assertEquals(waConv, result.get(0).waConversacionId());
        verify(mensajeRepositoryAdapter).findByConversacionId(ConversacionId.from(waConv));
    }

    @Test
    void findByConversacionId_emptyResult_shouldReturnEmptyList() {
        UUID waConv = UUID.randomUUID();
        when(mensajeRepositoryAdapter.findByConversacionId(ConversacionId.from(waConv)))
            .thenReturn(List.of());

        List<WhatsappMensajeResumen> result = adapter.findByConversacionId(waConv);

        assertTrue(result.isEmpty());
    }
}
