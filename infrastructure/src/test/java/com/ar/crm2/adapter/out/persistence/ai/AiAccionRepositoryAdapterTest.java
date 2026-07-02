package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiAccionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiAccionMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiAccionSpringDataRepository;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AiAccionRepositoryAdapter}.
 *
 * <p>RED-first sample for the PR 2 Slice Pattern Review Gate. The tests
 * describe the contract that the persistence adapter must fulfill:
 * map domain {@link AiAccion} ↔ JPA {@link AiAccionJpaEntity} and delegate
 * to the Spring Data repository using String id at the persistence
 * boundary. The adapter implements {@code SaveAiAccionPort} and
 * {@code FindAiAccionPort} at minimum so the application ports from
 * PR 1 have a real persistence implementation to call.
 *
 * <p>Test layer: pure Mockito unit (no Spring context, no DB) — same
 * style as {@code EtiquetaRepositoryAdapterTest} and
 * {@code ColumnaRepositoryAdapterTest}. Integration coverage (H2
 * round-trip, optimistic lock) lives in {@code *RepositoryAdapterIT}
 * classes added in the bulk PR 2 slice.
 */
@ExtendWith(MockitoExtension.class)
class AiAccionRepositoryAdapterTest {

    @Mock
    private AiAccionSpringDataRepository repository;

    @InjectMocks
    private AiAccionRepositoryAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private static final String EMPRESA_ID = "00000000-0000-0000-0000-000000000001";
    private static final String SOLICITANTE_ID = "00000000-0000-0000-0000-000000000002";
    private static final String AI_CONV_ID = "00000000-0000-0000-0000-000000000003";

    private AiAccion createDomainAccion(UUID id, String tipoAccion) {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 23, 15, 0);
        return AiAccion.reconstitute(
            AiAccionId.from(id),
            EmpresaId.from(UUID.fromString(EMPRESA_ID)),
            UsuarioId.from(UUID.fromString(SOLICITANTE_ID)),
            "wa-conv-1",
            null,
            AiConversacionId.from(UUID.fromString(AI_CONV_ID)),
            tipoAccion,
            "{\"contactoId\":\"abc\"}",
            "create the contact from chat",
            1,
            ahora.plusMinutes(60),
            null,
            null,
            EstadoAccion.PENDING,
            ahora,
            ahora
        );
    }

    private AiAccionJpaEntity createEntity(String id, String tipoAccion) {
        return AiAccionJpaEntity.builder()
            .id(id)
            .empresaId(EMPRESA_ID)
            .solicitadaPor(SOLICITANTE_ID)
            .waConversacionId("wa-conv-1")
            .waMensajeId(null)
            .aiConversacionId(AI_CONV_ID)
            .tipoAccion(tipoAccion)
            .estado(EstadoAccion.PENDING)
            .payloadJson("{\"contactoId\":\"abc\"}")
            .rationale("create the contact from chat")
            .version(1)
            .expiresAt(LocalDateTime.of(2026, 6, 23, 16, 0))
            .creadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .actualizadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .build();
    }

    // ── save (SaveAiAccionPort) ─────────────────────────────────────

    @Test
    void save_shouldMapDomainToEntityBeforePersisting() {
        UUID id = UUID.randomUUID();
        AiAccion domain = createDomainAccion(id, "CREATE_CONTACTO");

        when(repository.save(any(AiAccionJpaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(domain);

        ArgumentCaptor<AiAccionJpaEntity> captor = ArgumentCaptor.forClass(AiAccionJpaEntity.class);
        verify(repository).save(captor.capture());

        AiAccionJpaEntity captured = captor.getValue();
        assertEquals(id.toString(), captured.getId(),
            "domain UUID must be serialized as String for the JPA column");
        assertEquals("CREATE_CONTACTO", captured.getTipoAccion());
        assertEquals(EstadoAccion.PENDING, captured.getEstado());
        assertEquals(1, captured.getVersion(),
            "version starts at 1 for a new PENDING proposal — the optimistic lock baseline");
    }

    @Test
    void save_shouldReturnReconstitutedDomainWithPersistedEntity() {
        UUID id = UUID.randomUUID();
        AiAccion domain = createDomainAccion(id, "CREATE_TAREA");
        AiAccionJpaEntity savedEntity = createEntity(id.toString(), "CREATE_TAREA");

        when(repository.save(any(AiAccionJpaEntity.class))).thenReturn(savedEntity);

        AiAccion result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(id, result.getId().value(),
            "returned domain must carry the same UUID the adapter sent to the DB");
        assertEquals("CREATE_TAREA", result.getTipoAccion());
        assertEquals(EstadoAccion.PENDING, result.getEstado());
    }

    // ── findById (FindAiAccionPort) ─────────────────────────────────

    @Test
    void findById_shouldReturnEmptyWhenMissing() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        Optional<AiAccion> result = adapter.findById(AiAccionId.create());

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldMapEntityToDomain() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        AiAccionJpaEntity entity = createEntity(id.toString(), "MOVE_KANBAN_FICHA");

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<AiAccion> result = adapter.findById(AiAccionId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals("MOVE_KANBAN_FICHA", result.get().getTipoAccion());
        assertEquals(EstadoAccion.PENDING, result.get().getEstado());
    }

    // ── findPendingExpired (UpdateEstadoAccionPort) ──────────────────

    @Test
    void findPendingExpired_shouldReturnMappedDomainList() {
        AiAccionJpaEntity entity = createEntity(
            UUID.randomUUID().toString(), "CREATE_TAREA"
        );
        when(repository.findByEstadoAndExpiresAtBefore(
            eq(EstadoAccion.PENDING), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of(entity));

        List<AiAccion> result = adapter.findPendingExpired(10);

        assertEquals(1, result.size());
        assertEquals(EstadoAccion.PENDING, result.get(0).getEstado());
        assertEquals("CREATE_TAREA", result.get(0).getTipoAccion());
    }

    @Test
    void findPendingExpired_withEmptyResult_shouldReturnEmptyList() {
        when(repository.findByEstadoAndExpiresAtBefore(
            eq(EstadoAccion.PENDING), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of());

        List<AiAccion> result = adapter.findPendingExpired(10);

        assertTrue(result.isEmpty());
    }

    @Test
    void findPendingExpired_shouldAlwaysQueryWithPendingState() {
        when(repository.findByEstadoAndExpiresAtBefore(
            eq(EstadoAccion.PENDING), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of());

        adapter.findPendingExpired(5);

        // Confirms the adapter never confuses PENDING with another state
        // — a wrong-state sweep would flip non-PENDING proposals and
        // bypass the safety boundary.
        verify(repository).findByEstadoAndExpiresAtBefore(
            eq(EstadoAccion.PENDING), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void findPendingExpired_zeroOrNegativeLimite_shouldStillRequestAtLeastOneRow() {
        when(repository.findByEstadoAndExpiresAtBefore(
            eq(EstadoAccion.PENDING), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of());

        adapter.findPendingExpired(0);
        adapter.findPendingExpired(-1);

        // The adapter sanitizes the page size via Math.max(1, limite) so
        // a zero or negative limit never produces a Pageable that would
        // silently match every PENDING proposal in the database. Capture
        // the Pageable on each call and assert the page size is at least 1.
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, org.mockito.Mockito.atLeastOnce())
            .findByEstadoAndExpiresAtBefore(
                eq(EstadoAccion.PENDING), any(LocalDateTime.class), captor.capture()
            );
        for (Pageable pageable : captor.getAllValues()) {
            assertTrue(pageable.getPageSize() >= 1,
                "PageRequest page size must be sanitized to at least 1 to avoid "
                    + "an unbounded sweep; got pageSize=" + pageable.getPageSize());
        }
    }

    @Test
    void save_shouldMapAuditLinkFieldsToEntity() {
        UUID id = UUID.randomUUID();
        UUID aiConvId = UUID.fromString(AI_CONV_ID);
        AiAccion domain = AiAccion.reconstitute(
            AiAccionId.from(id),
            EmpresaId.from(UUID.fromString(EMPRESA_ID)),
            UsuarioId.from(UUID.fromString(SOLICITANTE_ID)),
            "wa-conv-1",
            "wa-msg-42",
            AiConversacionId.from(aiConvId),
            "CREATE_CONTACTO",
            "{}",
            "r",
            1,
            LocalDateTime.of(2026, 6, 23, 16, 0),
            null, null,
            EstadoAccion.PENDING,
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 23, 15, 0)
        );

        when(repository.save(any(AiAccionJpaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(domain);

        ArgumentCaptor<AiAccionJpaEntity> captor = ArgumentCaptor.forClass(AiAccionJpaEntity.class);
        verify(repository).save(captor.capture());

        AiAccionJpaEntity captured = captor.getValue();
        assertEquals(aiConvId.toString(), captured.getAiConversacionId(),
            "aiConversacionId must be serialized as String for the JPA column");
        assertEquals("wa-msg-42", captured.getWaMensajeId(),
            "waMensajeId must be serialized as String for the JPA column");
    }

    @Test
    void findById_shouldMapAuditLinkFieldsBackToDomain() {
        UUID id = UUID.randomUUID();
        AiAccionJpaEntity entity = createEntity(id.toString(), "CREATE_TAREA");
        entity.setWaMensajeId("wa-msg-7");
        entity.setAiConversacionId(AI_CONV_ID);

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<AiAccion> result = adapter.findById(AiAccionId.from(id));

        assertTrue(result.isPresent());
        assertEquals(AI_CONV_ID, result.get().getAiConversacionId().value().toString(),
            "aiConversacionId must round-trip back to the AiConversacionId VO");
        assertEquals("wa-msg-7", result.get().getWaMensajeId(),
            "audit-link waMensajeId must round-trip back to the domain String");
    }

    // ── listPendingByActor (PR7 tenant-scoped selector) ─────────────

    @Test
    void listPendingByActor_shouldUseTenantScopedQuery() {
        // PR7 contract: the actor + PENDING + empresaId triple filter
        // MUST be applied at the SQL boundary, NOT silently fall back
        // to the actor-only query. This pins the new query method so
        // the application service cannot accidentally broaden the
        // inbox scope.
        AiAccionJpaEntity entity = createEntity(UUID.randomUUID().toString(), "CREATE_CONTACTO");
        when(repository.findBySolicitadaPorAndEstadoAndEmpresaId(
                eq(SOLICITANTE_ID),
                eq(EstadoAccion.PENDING),
                eq(EMPRESA_ID),
                any(Pageable.class)
        )).thenReturn(List.of(entity));

        List<AiAccion> result = adapter.listPendingByActor(
                UUID.fromString(SOLICITANTE_ID),
                UUID.fromString(EMPRESA_ID),
                10
        );

        assertEquals(1, result.size());
        assertEquals(EstadoAccion.PENDING, result.get(0).getEstado());
        assertEquals("CREATE_CONTACTO", result.get(0).getTipoAccion());

        verify(repository).findBySolicitadaPorAndEstadoAndEmpresaId(
                eq(SOLICITANTE_ID),
                eq(EstadoAccion.PENDING),
                eq(EMPRESA_ID),
                any(Pageable.class)
        );
    }

    @Test
    void listPendingByActor_emptyResult_shouldReturnEmptyList() {
        when(repository.findBySolicitadaPorAndEstadoAndEmpresaId(
                any(), any(), any(), any(Pageable.class)
        )).thenReturn(List.of());

        List<AiAccion> result = adapter.listPendingByActor(
                UUID.fromString(SOLICITANTE_ID),
                UUID.fromString(EMPRESA_ID),
                10
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void listPendingByActor_zeroOrNegativeLimite_stillRequestAtLeastOneRow() {
        // The adapter sanitizes the page size via Math.max(1, limite) so a
        // zero or negative limit never produces a Pageable that would
        // silently match every PENDING proposal in the database.
        when(repository.findBySolicitadaPorAndEstadoAndEmpresaId(
                any(), any(), any(), any(Pageable.class)
        )).thenReturn(List.of());

        adapter.listPendingByActor(UUID.fromString(SOLICITANTE_ID), UUID.fromString(EMPRESA_ID), 0);
        adapter.listPendingByActor(UUID.fromString(SOLICITANTE_ID), UUID.fromString(EMPRESA_ID), -1);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, org.mockito.Mockito.atLeastOnce())
                .findBySolicitadaPorAndEstadoAndEmpresaId(
                        any(), any(), any(), captor.capture()
                );
        for (Pageable pageable : captor.getAllValues()) {
            assertTrue(pageable.getPageSize() >= 1,
                    "PageRequest page size must be sanitized to at least 1; got pageSize="
                            + pageable.getPageSize());
        }
    }
}

