package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiConversacionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiConversacionMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiConversacionSpringDataRepository;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the write-side {@link AiConversacionSaveAdapter}.
 *
 * <p>The adapter implements {@code SaveAiConversacionPort} which
 * declares both {@code save(...)} and {@code findById(UUID)} (the
 * latter returning the entity or {@code null} on miss — a different
 * contract from {@code FindAiConversacionPort.findById(UUID)} that
 * returns {@code Optional<AiConversacion>}). The two ports are split
 * into separate adapters because Java does not allow overloading by
 * return type.
 */
@ExtendWith(MockitoExtension.class)
class AiConversacionSaveAdapterTest {

    @Mock
    private AiConversacionSpringDataRepository repository;

    @InjectMocks
    private AiConversacionSaveAdapter adapter;

    // ── save ────────────────────────────────────────────────────────

    @Test
    void save_shouldMapDomainToEntityAndReturnReconstitutedDomain() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        AiConversacion domain = AiConversacion.crear(
            EmpresaId.from(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            UsuarioId.from(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            "wa-conv-1",
            null,
            ahora
        );

        when(repository.save(any(AiConversacionJpaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AiConversacion result = adapter.save(domain);

        assertNotNull(result);
        verify(repository).save(any(AiConversacionJpaEntity.class));
    }

    // ── findById(UUID) returning AiConversacion or null ──────────────

    @Test
    void findById_shouldReturnDomainWhenPresent() {
        UUID id = UUID.randomUUID();
        AiConversacionJpaEntity entity = AiConversacionJpaEntity.builder()
            .id(id.toString())
            .empresaId("00000000-0000-0000-0000-000000000001")
            .actorUsuarioId("00000000-0000-0000-0000-000000000002")
            .waConversacionId("wa-conv-1")
            .contactoId(null)
            .archivada(false)
            .creadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .actualizadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .build();

        when(repository.findById(id.toString())).thenReturn(java.util.Optional.of(entity));

        AiConversacion result = adapter.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId().value());
    }

    @Test
    void findById_shouldReturnNullWhenMissing() {
        when(repository.findById(any())).thenReturn(java.util.Optional.empty());

        AiConversacion result = adapter.findById(UUID.randomUUID());

        assertNull(result);
    }
}
