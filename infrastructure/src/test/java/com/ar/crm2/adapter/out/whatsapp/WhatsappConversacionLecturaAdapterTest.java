package com.ar.crm2.adapter.out.whatsapp;

import com.ar.crm2.adapter.out.persistence.CanalWhatsappRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.ConversacionRepositoryAdapter;
import com.ar.crm2.application.ai.port.out.projection.WhatsappConversacionResumen;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import com.ar.crm2.whatsapp.domain.enums.EstadoConversacion;
import com.ar.crm2.whatsapp.domain.enums.ProveedorCanal;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WhatsappConversacionLecturaAdapter}.
 *
 * <p>Verifies the bridge contract: the adapter delegates to
 * {@link ConversacionRepositoryAdapter} and
 * {@link CanalWhatsappRepositoryAdapter}, joins the conversation's
 * channel to its owning company, and exposes the result as the
 * application-owned {@link WhatsappConversacionResumen}. The
 * {@code canalEmpresaId} field is the tenant key the application
 * services use to authorize access without importing the
 * {@code whatsapp} module.
 */
@ExtendWith(MockitoExtension.class)
class WhatsappConversacionLecturaAdapterTest {

    @Mock
    private ConversacionRepositoryAdapter conversacionRepositoryAdapter;

    @Mock
    private CanalWhatsappRepositoryAdapter canalWhatsappRepositoryAdapter;

    @InjectMocks
    private WhatsappConversacionLecturaAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private Conversacion createConversacion(String id, String canalId) {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        return Conversacion.reconstitute(
            ConversacionId.from(UUID.fromString(id)),
            CanalWhatsappId.from(UUID.fromString(canalId)),
            null,
            "+54911...",
            "Juan",
            EstadoConversacion.ABIERTA,
            null,
            0,
            ahora,
            null,
            Set.of(),
            true,
            null,
            null,
            null,
            null,
            ahora,
            ahora
        );
    }

    private CanalWhatsapp createCanal(String id, String empresaId) {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        return CanalWhatsapp.reconstitute(
            CanalWhatsappId.from(UUID.fromString(id)),
            EmpresaId.from(UUID.fromString(empresaId)),
            "Canal Test",
            "instance-1",
            ProveedorCanal.EVOLUTION_API,
            EstadoCanal.ACTIVO,
            "https://example.com",
            "apikey",
            ahora,
            ahora
        );
    }

    // ── findById ────────────────────────────────────────────────────

    @Test
    void findById_shouldMapConversacionAndCanalToApplicationProjection() {
        UUID waConv = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID canal = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID canalEmpresa = UUID.fromString("00000000-0000-0000-0000-000000000003");

        Conversacion conv = createConversacion(waConv.toString(), canal.toString());
        CanalWhatsapp canalRow = createCanal(canal.toString(), canalEmpresa.toString());

        when(conversacionRepositoryAdapter.findById(ConversacionId.from(waConv)))
            .thenReturn(Optional.of(conv));
        when(canalWhatsappRepositoryAdapter.findById(CanalWhatsappId.from(canal)))
            .thenReturn(Optional.of(canalRow));

        Optional<WhatsappConversacionResumen> result = adapter.findById(waConv);

        assertTrue(result.isPresent());
        assertEquals(waConv, result.get().waConversacionId());
        assertEquals(canal, result.get().canalId());
        assertEquals(canalEmpresa, result.get().canalEmpresaId(),
            "canalEmpresaId is the tenant key the application uses for ownership checks");
        verify(conversacionRepositoryAdapter).findById(ConversacionId.from(waConv));
        verify(canalWhatsappRepositoryAdapter).findById(CanalWhatsappId.from(canal));
    }

    @Test
    void findById_shouldReturnEmptyWhenConversacionMissing() {
        UUID waConv = UUID.randomUUID();
        when(conversacionRepositoryAdapter.findById(ConversacionId.from(waConv)))
            .thenReturn(Optional.empty());

        Optional<WhatsappConversacionResumen> result = adapter.findById(waConv);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnEmptyWhenOrphanCanalReference() {
        // If a Conversacion points to a Canal that no longer exists,
        // the application must see an empty projection so the chat
        // analyzer can reject the request with a tenant error rather
        // than crash on a null canal lookup.
        UUID waConv = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID canal = UUID.fromString("00000000-0000-0000-0000-000000000002");

        Conversacion conv = createConversacion(waConv.toString(), canal.toString());

        when(conversacionRepositoryAdapter.findById(ConversacionId.from(waConv)))
            .thenReturn(Optional.of(conv));
        when(canalWhatsappRepositoryAdapter.findById(CanalWhatsappId.from(canal)))
            .thenReturn(Optional.empty());

        Optional<WhatsappConversacionResumen> result = adapter.findById(waConv);

        assertTrue(result.isEmpty(),
            "orphan canal reference must surface as empty so the application rejects it");
    }
}
