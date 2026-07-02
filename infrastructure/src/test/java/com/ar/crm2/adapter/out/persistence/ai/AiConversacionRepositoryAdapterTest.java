package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiConversacionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiConversacionMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiConversacionSpringDataRepository;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the read-side {@link AiConversacionRepositoryAdapter}.
 * Pure Mockito style, matching the project convention used by
 * {@code EtiquetaRepositoryAdapterTest} and {@code AiAccionRepositoryAdapterTest}.
 */
@ExtendWith(MockitoExtension.class)
class AiConversacionRepositoryAdapterTest {

    @Mock
    private AiConversacionSpringDataRepository repository;

    @InjectMocks
    private AiConversacionRepositoryAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private AiConversacionJpaEntity createEntity(String id, boolean archivada) {
        return AiConversacionJpaEntity.builder()
            .id(id)
            .empresaId("00000000-0000-0000-0000-000000000001")
            .actorUsuarioId("00000000-0000-0000-0000-000000000002")
            .waConversacionId("wa-conv-1")
            .contactoId("00000000-0000-0000-0000-000000000003")
            .archivada(archivada)
            .creadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .actualizadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .build();
    }

    // ── findById (FindAiConversacionPort) ───────────────────────────

    @Test
    void findById_shouldReturnEmptyWhenMissing() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        Optional<AiConversacion> result = adapter.findById(AiConversacionId.create());

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        AiConversacionJpaEntity entity = createEntity(id.toString(), false);

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<AiConversacion> result = adapter.findById(AiConversacionId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertFalse(result.get().isArchivada());
        assertEquals("wa-conv-1", result.get().getWaConversacionId());
    }

    // ── listByActor (ListAiConversacionesPort) ───────────────────────

    @Test
    void listByActor_shouldPassActorEmpresaAndLimitToDerivedQuery() {
        UUID actor = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        UUID empresa = UUID.fromString("11111111-2222-3333-4444-555555555555");
        when(repository.findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(
            eq(actor.toString()), eq(empresa.toString()), any(Pageable.class)
        )).thenReturn(List.of(
            createEntity(UUID.randomUUID().toString(), false),
            createEntity(UUID.randomUUID().toString(), true)
        ));

        List<AiConversacion> result = adapter.listByActor(actor, empresa, 25);

        assertEquals(2, result.size());
        verify(repository).findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(
            actor.toString(), empresa.toString(), PageRequest.of(0, 25)
        );
    }

    @Test
    void listByActor_zeroOrNegativeLimite_shouldStillRequestAtLeastOneRow() {
        UUID actor = UUID.randomUUID();
        UUID empresa = UUID.randomUUID();
        when(repository.findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(
            any(), any(), any(Pageable.class)
        )).thenReturn(List.of());

        adapter.listByActor(actor, empresa, 0);
        adapter.listByActor(actor, empresa, -3);

        verify(repository, org.mockito.Mockito.atLeastOnce())
            .findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(
                any(), any(), any(Pageable.class)
            );
    }
}
