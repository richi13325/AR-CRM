package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMemoriaJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiMemoriaMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMemoriaSpringDataRepository;
import com.ar.crm2.model.entity.ia.AiMemoria;
import com.ar.crm2.model.enums.OrigenMemoria;
import com.ar.crm2.model.enums.VisibilidadMemoria;
import com.ar.crm2.model.vo.AiMemoriaId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AiMemoriaRepositoryAdapter}.
 *
 * <p>Pure Mockito style. The adapter implements
 * {@code SaveAiMemoriaPort}, {@code FindAiMemoriaPort} and
 * {@code DeleteAiMemoriaPort}. Memory is always private to a
 * requester + company + (waConversacionId | contactoId) scope.
 */
@ExtendWith(MockitoExtension.class)
class AiMemoriaRepositoryAdapterTest {

    @Mock
    private AiMemoriaSpringDataRepository repository;

    // ── Helpers ─────────────────────────────────────────────────────

    private AiMemoriaJpaEntity createEntity(String id, String actor, String empresa, String waConv) {
        return AiMemoriaJpaEntity.builder()
            .id(id)
            .actorUsuarioId(actor)
            .empresaId(empresa)
            .waConversacionId(waConv)
            .contactoId(null)
            .visibilidad(VisibilidadMemoria.CONVERSACION_SCOPED)
            .contenido("Cliente prefiere email")
            .origenTipo(OrigenMemoria.MANUAL)
            .origenId("wa-msg-1")
            .version(1L)
            .creadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .actualizadoEn(LocalDateTime.of(2026, 6, 23, 15, 0))
            .expiresAt(LocalDateTime.of(2026, 6, 24, 15, 0))
            .superseded(false)
            .expirada(false)
            .build();
    }

    // ── save (SaveAiMemoriaPort) ────────────────────────────────────

    @Test
    void save_shouldMapDomainToEntityAndReturnDomain() {
        AiMemoriaRepositoryAdapter adapter = new AiMemoriaRepositoryAdapter(repository, true);
        AiMemoria domain = AiMemoria.crear(
            UsuarioId.from(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            EmpresaId.from(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            "wa-conv-1",
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Cliente prefiere email",
            OrigenMemoria.MANUAL,
            "wa-msg-1",
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 24, 15, 0)
        );

        when(repository.save(any(AiMemoriaJpaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        AiMemoria result = adapter.save(domain);

        assertNotNull(result);
        assertEquals("Cliente prefiere email", result.getContenido());
        verify(repository).save(any(AiMemoriaJpaEntity.class));
    }

    @Test
    void save_whenPhase1MemoryWritesDisabled_shouldRejectWithoutPersisting() {
        AiMemoriaRepositoryAdapter adapter = new AiMemoriaRepositoryAdapter(repository, false);
        AiMemoria domain = AiMemoria.crear(
            UsuarioId.from(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            EmpresaId.from(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            "wa-conv-1",
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Cliente prefiere email",
            OrigenMemoria.MANUAL,
            "wa-msg-1",
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 24, 15, 0)
        );

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> adapter.save(domain));

        assertTrue(error.getMessage().contains("disabled"));
        verify(repository, never()).save(any(AiMemoriaJpaEntity.class));
    }

    // ── findActivasByConversacionId (FindAiMemoriaPort) ─────────────

    @Test
    void findActivasByConversacionId_shouldReturnMappedDomainList() {
        AiMemoriaRepositoryAdapter adapter = new AiMemoriaRepositoryAdapter(repository, true);
        UUID actor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID empresa = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID waConv = UUID.fromString("00000000-0000-0000-0000-000000000003");
        AiMemoriaJpaEntity entity = createEntity(
            UUID.randomUUID().toString(),
            actor.toString(), empresa.toString(), waConv.toString()
        );

        when(repository
            .findActiveMemories(
                eq(actor.toString()), eq(empresa.toString()), eq(waConv.toString()), any(LocalDateTime.class)
            )).thenReturn(List.of(entity));

        List<AiMemoria> result = adapter.findActivasByConversacionId(waConv, actor, empresa);

        assertEquals(1, result.size());
        assertEquals("Cliente prefiere email", result.get(0).getContenido());
    }

    @Test
    void findActivasByConversacionId_emptyResult_shouldReturnEmptyList() {
        AiMemoriaRepositoryAdapter adapter = new AiMemoriaRepositoryAdapter(repository, true);
        when(repository
            .findActiveMemories(
                anyString(), anyString(), anyString(), any(LocalDateTime.class)
            )).thenReturn(List.of());

        List<AiMemoria> result = adapter.findActivasByConversacionId(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        assertTrue(result.isEmpty());
    }

    // ── delete (DeleteAiMemoriaPort) ────────────────────────────────

    @Test
    void delete_shouldCallRepositoryDeleteByIdWithStringId() {
        AiMemoriaRepositoryAdapter adapter = new AiMemoriaRepositoryAdapter(repository, true);
        AiMemoria domain = AiMemoria.crear(
            UsuarioId.from(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            EmpresaId.from(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            "wa-conv-1",
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Cliente prefiere email",
            OrigenMemoria.MANUAL,
            "wa-msg-1",
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 24, 15, 0)
        );

        adapter.delete(domain);

        verify(repository).deleteById(domain.getId().value().toString());
    }

    @Test
    void delete_whenPhase1MemoryWritesDisabled_shouldRejectWithoutDeleting() {
        AiMemoriaRepositoryAdapter adapter = new AiMemoriaRepositoryAdapter(repository, false);
        AiMemoria domain = AiMemoria.crear(
            UsuarioId.from(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            EmpresaId.from(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            "wa-conv-1",
            null,
            VisibilidadMemoria.CONVERSACION_SCOPED,
            "Cliente prefiere email",
            OrigenMemoria.MANUAL,
            "wa-msg-1",
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 24, 15, 0)
        );

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> adapter.delete(domain));

        assertTrue(error.getMessage().contains("disabled"));
        verifyNoInteractions(repository);
    }
}
