package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMensajeJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiMensajeMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMensajeSpringDataRepository;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.enums.RolMensajeAi;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiMensajeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AiMensajeRepositoryAdapter}.
 *
 * <p>Pure Mockito style, matching the project convention used by
 * {@code EtiquetaRepositoryAdapterTest} and the other AI adapter
 * tests. The adapter implements {@code SaveAiMensajePort} and
 * {@code FindAiMensajesByConversacionPort}.
 */
@ExtendWith(MockitoExtension.class)
class AiMensajeRepositoryAdapterTest {

    @Mock
    private AiMensajeSpringDataRepository repository;

    @InjectMocks
    private AiMensajeRepositoryAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private AiMensajeJpaEntity createEntity(String id, RolMensajeAi rol) {
        return AiMensajeJpaEntity.builder()
            .id(id)
            .aiConversacionId("00000000-0000-0000-0000-000000000001")
            .rol(rol)
            .contenido("hola")
            .modelo("gpt-4o-mini")
            .promptTokens(10)
            .completionTokens(20)
            .latencyMs(150L)
            .toolCallJson(null)
            .creadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .build();
    }

    private AiMensaje createDomain(UUID id, RolMensajeAi rol) {
        return AiMensaje.crear(
            AiConversacionId.from(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            rol,
            "hola",
            "gpt-4o-mini",
            10, 20, 150L, null,
            LocalDateTime.of(2026, 6, 23, 15, 0)
        );
    }

    // ── save (SaveAiMensajePort) ────────────────────────────────────

    @Test
    void save_shouldMapDomainToEntityAndReturnDomain() {
        AiMensaje domain = createDomain(UUID.randomUUID(), RolMensajeAi.USER);

        when(repository.save(any(AiMensajeJpaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AiMensaje result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(RolMensajeAi.USER, result.getRol());
        verify(repository).save(any(AiMensajeJpaEntity.class));
    }

    // ── findByConversacionId (FindAiMensajesByConversacionPort) ─────

    @Test
    void findByConversacionId_shouldReturnMappedDomainListInOrder() {
        UUID aiConv = UUID.fromString("00000000-0000-0000-0000-000000000001");
        AiMensajeJpaEntity userMsg = createEntity(UUID.randomUUID().toString(), RolMensajeAi.USER);
        AiMensajeJpaEntity assistantMsg = createEntity(UUID.randomUUID().toString(), RolMensajeAi.ASSISTANT);
        when(repository.findByAiConversacionIdOrderByCreadoEnAsc(aiConv.toString()))
            .thenReturn(List.of(userMsg, assistantMsg));

        List<AiMensaje> result = adapter.findByConversacionId(aiConv);

        assertEquals(2, result.size());
        assertEquals(RolMensajeAi.USER, result.get(0).getRol());
        assertEquals(RolMensajeAi.ASSISTANT, result.get(1).getRol());
    }

    @Test
    void findByConversacionId_emptyResult_shouldReturnEmptyList() {
        UUID aiConv = UUID.randomUUID();
        when(repository.findByAiConversacionIdOrderByCreadoEnAsc(aiConv.toString()))
            .thenReturn(List.of());

        List<AiMensaje> result = adapter.findByConversacionId(aiConv);

        assertTrue(result.isEmpty());
    }
}
