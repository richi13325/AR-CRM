package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.EtiquetaEntity;
import com.ar.crm2.adapter.out.persistence.entity.FichaEtiquetaEntity;
import com.ar.crm2.adapter.out.persistence.repository.EtiquetaRepository;
import com.ar.crm2.adapter.out.persistence.repository.FichaEtiquetaRepository;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EtiquetaRepositoryAdapter}.
 *
 * <p>The adapter delegates to two Spring Data repositories and is the
 * implementation site for the application ports. The transaction boundary
 * for the cascade-delete flow is on the adapter (annotated
 * {@code @Transactional}); this test class does not exercise the boundary
 * itself — that is verified by the integration tests where a real
 * Hibernate transaction manager is in play.
 */
@ExtendWith(MockitoExtension.class)
class EtiquetaRepositoryAdapterTest {

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private FichaEtiquetaRepository fichaEtiquetaRepository;

    @InjectMocks
    private EtiquetaRepositoryAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private Etiqueta createDomain(UUID id, String nombre, TipoEtiqueta tipo, String color) {
        return Etiqueta.reconstitute(
            EtiquetaId.from(id), nombre, tipo, color, LocalDateTime.now()
        );
    }

    private EtiquetaEntity createEntity(String id, String nombre, TipoEtiqueta tipo, String color) {
        return EtiquetaEntity.builder()
            .id(id)
            .nombre(nombre)
            .tipoEtiqueta(tipo)
            .color(color)
            .creadoEn(LocalDateTime.now())
            .build();
    }

    // ── save (SaveEtiquetaPort) ─────────────────────────────────────

    @Test
    void save_shouldDelegateToRepositoryAndReturnDomain() {
        UUID id = UUID.randomUUID();
        Etiqueta domain = createDomain(id, "Urgent", TipoEtiqueta.TAREA, "#FF0000");
        EtiquetaEntity savedEntity = createEntity(id.toString(), "Urgent", TipoEtiqueta.TAREA, "#FF0000");

        when(etiquetaRepository.saveAndFlush(any(EtiquetaEntity.class))).thenReturn(savedEntity);

        Etiqueta result = adapter.save(domain);

        assertEquals(id, result.getId().value());
        assertEquals("Urgent", result.getNombre());
    }

    @Test
    void save_shouldMapDomainToEntityBeforePersisting() {
        UUID id = UUID.randomUUID();
        Etiqueta domain = createDomain(id, "Premium", TipoEtiqueta.TRATO, "#0000FF");
        when(etiquetaRepository.saveAndFlush(any(EtiquetaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(domain);

        ArgumentCaptor<EtiquetaEntity> captor = ArgumentCaptor.forClass(EtiquetaEntity.class);
        verify(etiquetaRepository).saveAndFlush(captor.capture());
        EtiquetaEntity captured = captor.getValue();
        assertEquals(id.toString(), captured.getId());
        assertEquals("Premium", captured.getNombre());
        assertEquals(TipoEtiqueta.TRATO, captured.getTipoEtiqueta());
        assertEquals("#0000FF", captured.getColor());
    }

    @Test
    void save_shouldWrapDataIntegrityViolationWithClearMessage() {
        Etiqueta domain = createDomain(UUID.randomUUID(), "Dup", TipoEtiqueta.TAREA, "#FFFFFF");
        DataIntegrityViolationException raw = new DataIntegrityViolationException("uk_violation");

        when(etiquetaRepository.saveAndFlush(any(EtiquetaEntity.class))).thenThrow(raw);

        DataIntegrityViolationException thrown = assertThrows(
            DataIntegrityViolationException.class,
            () -> adapter.save(domain)
        );

        assertNotNull(thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Ya existe una etiqueta con el mismo nombre y tipo"));
        assertEquals(raw, thrown.getCause());
    }

    // ── findById (FindEtiquetaByIdPort) ────────────────────────────

    @Test
    void findById_shouldReturnEmptyWhenMissing() {
        when(etiquetaRepository.findById(any())).thenReturn(Optional.empty());

        Optional<Etiqueta> result = adapter.findById(EtiquetaId.create());

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldConvertStringIdAndReturnDomain() {
        UUID id = UUID.randomUUID();
        EtiquetaEntity entity = createEntity(id.toString(), "X", TipoEtiqueta.TAREA, "#FFFFFF");
        when(etiquetaRepository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<Etiqueta> result = adapter.findById(EtiquetaId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
    }

    // ── findAll (FindAllEtiquetasPort) ─────────────────────────────

    @Test
    void findAll_withoutFilter_shouldReturnAllRowsMappedToDomain() {
        EtiquetaEntity e1 = createEntity(UUID.randomUUID().toString(), "A", TipoEtiqueta.TAREA, "#FFFFFF");
        EtiquetaEntity e2 = createEntity(UUID.randomUUID().toString(), "B", TipoEtiqueta.TRATO, "#000000");
        when(etiquetaRepository.findAll()).thenReturn(List.of(e1, e2));

        List<Etiqueta> result = adapter.findAll(Optional.empty());

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getNombre());
        assertEquals("B", result.get(1).getNombre());
    }

    @Test
    void findAll_withFilter_shouldCallFindByTipoEtiqueta() {
        EtiquetaEntity e1 = createEntity(UUID.randomUUID().toString(), "A", TipoEtiqueta.TAREA, "#FFFFFF");
        when(etiquetaRepository.findByTipoEtiqueta(TipoEtiqueta.TAREA)).thenReturn(List.of(e1));

        List<Etiqueta> result = adapter.findAll(Optional.of(TipoEtiqueta.TAREA));

        assertEquals(1, result.size());
        assertEquals(TipoEtiqueta.TAREA, result.get(0).getTipoEtiqueta());
        verify(etiquetaRepository, never()).findAll();
    }

    // ── exists (ExistsEtiquetaByNombreAndTipoPort) ─────────────────

    @Test
    void exists_withNullExcludeId_shouldPassNullAsExcludeIdString() {
        when(etiquetaRepository.existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TAREA, null
        )).thenReturn(true);

        boolean result = adapter.exists("Urgent", TipoEtiqueta.TAREA, null);

        assertTrue(result);
        verify(etiquetaRepository).existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TAREA, null
        );
    }

    @Test
    void exists_withExcludeId_shouldPassExcludeIdAsString() {
        EtiquetaId id = EtiquetaId.create();
        when(etiquetaRepository.existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TAREA, id.value().toString()
        )).thenReturn(false);

        boolean result = adapter.exists("Urgent", TipoEtiqueta.TAREA, id);

        assertFalse(result);
        verify(etiquetaRepository).existsByNombreAndTipoEtiquetaAndIdNot(
            "Urgent", TipoEtiqueta.TAREA, id.value().toString()
        );
    }

    // ── deleteById (DeleteEtiquetaByIdPort) ────────────────────────

    @Test
    void deleteById_shouldCascadeDeleteRelationsAndThenCatalogRow() {
        UUID id = UUID.randomUUID();
        when(fichaEtiquetaRepository.deleteByEtiquetaId(id.toString())).thenReturn(3);

        adapter.deleteById(EtiquetaId.from(id));

        // Order matters: relations must be removed first so the FK does not
        // block the catalog row delete.
        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(fichaEtiquetaRepository, etiquetaRepository);
        inOrder.verify(fichaEtiquetaRepository).deleteByEtiquetaId(id.toString());
        inOrder.verify(etiquetaRepository).deleteById(id.toString());
    }

    @Test
    void deleteById_withNoRelations_shouldStillCallBothRepositories() {
        UUID id = UUID.randomUUID();
        when(fichaEtiquetaRepository.deleteByEtiquetaId(id.toString())).thenReturn(0);

        adapter.deleteById(EtiquetaId.from(id));

        verify(fichaEtiquetaRepository).deleteByEtiquetaId(id.toString());
        verify(etiquetaRepository).deleteById(id.toString());
    }

    // ── deleteByEtiquetaId (DeleteFichaEtiquetasByEtiquetaIdPort) ──

    @Test
    void deleteByEtiquetaId_shouldCallRepositoryWithStringId() {
        UUID id = UUID.randomUUID();
        when(fichaEtiquetaRepository.deleteByEtiquetaId(id.toString())).thenReturn(2);

        adapter.deleteByEtiquetaId(EtiquetaId.from(id));

        verify(fichaEtiquetaRepository).deleteByEtiquetaId(id.toString());
    }

    // ── countByEtiquetaId (CountFichaEtiquetasByEtiquetaIdPort) ────

    @Test
    void countByEtiquetaId_shouldReturnRepositoryCount() {
        UUID id = UUID.randomUUID();
        when(fichaEtiquetaRepository.countByEtiquetaId(id.toString())).thenReturn(7L);

        long result = adapter.countByEtiquetaId(EtiquetaId.from(id));

        assertEquals(7L, result);
    }

    @Test
    void countByEtiquetaId_withZeroRelations_shouldReturnZero() {
        UUID id = UUID.randomUUID();
        when(fichaEtiquetaRepository.countByEtiquetaId(id.toString())).thenReturn(0L);

        long result = adapter.countByEtiquetaId(EtiquetaId.from(id));

        assertEquals(0L, result);
    }

    // ── findByIds (FindEtiquetasByIdsPort) ─────────────────────────

    @Test
    void findByIds_shouldReturnEmptyListWhenIdsIsNull() {
        List<Etiqueta> result = adapter.findByIds(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(etiquetaRepository);
    }

    @Test
    void findByIds_shouldReturnEmptyListWhenIdsIsEmpty() {
        List<Etiqueta> result = adapter.findByIds(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(etiquetaRepository);
    }

    @Test
    void findByIds_shouldDelegateToFindAllByIdWithConvertedIds() {
        EtiquetaId id1 = EtiquetaId.create();
        EtiquetaId id2 = EtiquetaId.create();
        EtiquetaEntity e1 = createEntity(id1.value().toString(), "A", TipoEtiqueta.TAREA, "#FFFFFF");
        EtiquetaEntity e2 = createEntity(id2.value().toString(), "B", TipoEtiqueta.TAREA, "#000000");
        when(etiquetaRepository.findAllById(List.of(id1.value().toString(), id2.value().toString())))
            .thenReturn(List.of(e1, e2));

        List<Etiqueta> result = adapter.findByIds(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(id1, result.get(0).getId());
        assertEquals(id2, result.get(1).getId());
    }

    @Test
    void findByIds_shouldReturnOnlyRowsThatExistInCatalog() {
        EtiquetaId id1 = EtiquetaId.create();
        EtiquetaEntity e1 = createEntity(id1.value().toString(), "A", TipoEtiqueta.TAREA, "#FFFFFF");
        when(etiquetaRepository.findAllById(any())).thenReturn(List.of(e1));

        List<Etiqueta> result = adapter.findByIds(List.of(id1, EtiquetaId.create()));

        assertEquals(1, result.size());
        assertEquals(id1, result.get(0).getId());
    }
}
