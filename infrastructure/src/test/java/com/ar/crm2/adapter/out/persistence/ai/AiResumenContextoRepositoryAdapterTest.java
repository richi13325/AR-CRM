package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiResumenContextoJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiResumenContextoMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiResumenContextoSpringDataRepository;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiResumenContextoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AiResumenContextoRepositoryAdapter}.
 *
 * <p>Pure Mockito style, matching the project convention. The adapter
 * implements {@code SaveAiResumenPort} and {@code FindAiResumenPort}.
 */
@ExtendWith(MockitoExtension.class)
class AiResumenContextoRepositoryAdapterTest {

    @Mock
    private AiResumenContextoSpringDataRepository repository;

    @InjectMocks
    private AiResumenContextoRepositoryAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private AiResumenContextoJpaEntity createEntity(String id, String aiConvId) {
        return AiResumenContextoJpaEntity.builder()
            .id(id)
            .actorUsuarioId("00000000-0000-0000-0000-000000000001")
            .empresaId("00000000-0000-0000-0000-000000000002")
            .waConversacionId("wa-conv-1")
            .contactoId(null)
            .facts("Cliente quiere demo")
            .inferences("Probabilidad alta")
            .sourceWaMensajeId(null)
            .sourceWatermark(5L)
            .aiConversacionId(aiConvId)
            .creadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .actualizadoEn(LocalDateTime.of(2026, 6, 23, 15, 5))
            .build();
    }

    private AiResumenContexto createDomain(String aiConvId) {
        return AiResumenContexto.crear(
            UsuarioId.from(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            EmpresaId.from(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            "wa-conv-1",
            null,
            "Cliente quiere demo",
            "Probabilidad alta",
            null,
            5L,
            AiConversacionId.from(UUID.fromString(aiConvId)),
            LocalDateTime.of(2026, 6, 23, 15, 0)
        );
    }

    // ── save (SaveAiResumenPort) ─────────────────────────────────────

    @Test
    void save_shouldMapDomainToEntityAndReturnDomain() {
        AiResumenContexto domain = createDomain("00000000-0000-0000-0000-000000000003");

        when(repository.save(any(AiResumenContextoJpaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AiResumenContexto result = adapter.save(domain);

        assertNotNull(result);
        assertEquals("Cliente quiere demo", result.getFacts());
        assertEquals(5L, result.getSourceWatermark());
        verify(repository).save(any(AiResumenContextoJpaEntity.class));
    }

    // ── findByConversacionId (FindAiResumenPort) ─────────────────────

    @Test
    void findByConversacionId_shouldReturnMappedDomainWhenPresent() {
        UUID aiConv = UUID.fromString("00000000-0000-0000-0000-000000000003");
        AiResumenContextoJpaEntity entity = createEntity(UUID.randomUUID().toString(), aiConv.toString());

        when(repository.findFirstByAiConversacionIdOrderByActualizadoEnDesc(aiConv.toString()))
            .thenReturn(Optional.of(entity));

        Optional<AiResumenContexto> result = adapter.findByConversacionId(aiConv);

        assertTrue(result.isPresent());
        assertEquals(aiConv, result.get().getAiConversacionId().value());
    }

    @Test
    void findByConversacionId_shouldReturnEmptyWhenMissing() {
        UUID aiConv = UUID.randomUUID();
        when(repository.findFirstByAiConversacionIdOrderByActualizadoEnDesc(aiConv.toString()))
            .thenReturn(Optional.empty());

        Optional<AiResumenContexto> result = adapter.findByConversacionId(aiConv);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByConversacionId_shouldAlwaysSortByActualizadoEnDesc() {
        UUID aiConv = UUID.randomUUID();
        when(repository.findFirstByAiConversacionIdOrderByActualizadoEnDesc(aiConv.toString()))
            .thenReturn(Optional.empty());

        adapter.findByConversacionId(aiConv);

        // The freshness sort is what makes "the most recent summary" the
        // correct concept — if the method accidentally queries a
        // different order, stale summaries would be served to the
        // chat analyzer.
        verify(repository).findFirstByAiConversacionIdOrderByActualizadoEnDesc(aiConv.toString());
    }
}
