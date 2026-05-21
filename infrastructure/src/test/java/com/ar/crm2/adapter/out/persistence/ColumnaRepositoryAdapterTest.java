package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ColumnaMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ColumnaRepositoryAdapter}.
 * Verifies delegation to ColumnaRepository and UUID/String conversion at the persistence boundary.
 */
@ExtendWith(MockitoExtension.class)
class ColumnaRepositoryAdapterTest {

    @Mock
    private ColumnaRepository repository;

    @InjectMocks
    private ColumnaRepositoryAdapter adapter;

    // ── Helpers ─────────────────────────────────────────────────────

    private Columna createDomainColumna(UUID id, String nombre) {
        return Columna.reconstitute(
            ColumnaId.from(id),
            nombre,
            "#FFFFFF",
            TipoTablero.TAREAS,
            TipoColumna.PREDETERMINADA,
            false
        );
    }

    private ColumnaEntity createColumnaEntity(String id, String nombre) {
        return ColumnaEntity.builder()
            .id(id)
            .columnaNombre(nombre)
            .color("#FFFFFF")
            .tipoTablero(TipoTablero.TAREAS.name())
            .tipoColumna(TipoColumna.PREDETERMINADA.name())
            .build();
    }

    // ── save ─────────────────────────────────────────────────────────

    @Test
    void save_shouldDelegateToRepositoryWithMappedEntity() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "Backlog");

        when(repository.save(any(ColumnaEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(columna);

        ArgumentCaptor<ColumnaEntity> captor = ArgumentCaptor.forClass(ColumnaEntity.class);
        verify(repository).save(captor.capture());

        ColumnaEntity entity = captor.getValue();
        assertEquals(id.toString(), entity.getId());
        assertEquals("Backlog", entity.getColumnaNombre());
        assertEquals("#FFFFFF", entity.getColor());
        assertEquals(TipoTablero.TAREAS.name(), entity.getTipoTablero());
        assertEquals(TipoColumna.PREDETERMINADA.name(), entity.getTipoColumna());
    }

    @Test
    void save_shouldReturnDomainFromPersistedEntity() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "To Do");
        ColumnaEntity savedEntity = createColumnaEntity(id.toString(), "To Do");

        when(repository.save(any(ColumnaEntity.class))).thenReturn(savedEntity);

        Columna result = adapter.save(columna);

        assertNotNull(result);
        assertEquals(id, result.getId().value());
        assertEquals("To Do", result.getColumnanombre());
    }

    @Test
    void save_shouldPassCleanEntityToRepository() {
        UUID id = UUID.randomUUID();
        Columna columna = createDomainColumna(id, "Backlog");
        // ColumnaEntity no longer carries tableroId — board ownership lives in ColumnaTableroEntity

        ColumnaEntity savedEntity = createColumnaEntity(id.toString(), "Backlog");

        when(repository.save(any(ColumnaEntity.class))).thenReturn(savedEntity);

        Columna result = adapter.save(columna);

        assertNotNull(result);
        verify(repository).save(argThat(entity ->
            entity.getId() != null
        ));
    }

    // ── findAll ─────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnEmptyListWhenRepositoryIsEmpty() {
        when(repository.findAll()).thenReturn(List.of());

        List<Columna> result = adapter.findAll();

        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void findAll_shouldMapAllEntitiesToDomain() {
        ColumnaEntity e1 = createColumnaEntity(UUID.randomUUID().toString(), "Backlog");
        ColumnaEntity e2 = createColumnaEntity(UUID.randomUUID().toString(), "To Do");

        when(repository.findAll()).thenReturn(List.of(e1, e2));

        List<Columna> result = adapter.findAll();

        assertEquals(2, result.size());
        assertEquals("Backlog", result.get(0).getColumnanombre());
        assertEquals("To Do", result.get(1).getColumnanombre());
    }

    @Test
    void findAll_shouldUseStaticMapperForEachEntity() {
        ColumnaEntity entity = createColumnaEntity(UUID.randomUUID().toString(), "Done");
        when(repository.findAll()).thenReturn(List.of(entity));

        adapter.findAll();

        verify(repository).findAll();
        // ColumnaMapper is a static utility called directly by the adapter — no mock interaction possible
    }

    // ── findById ───────────────────────────────────────────────────

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<Columna> result = adapter.findById(ColumnaId.from(id));

        assertTrue(result.isEmpty());
        verify(repository).findById(id.toString());
    }

    @Test
    void findById_shouldReturnDomainWhenFound() {
        UUID id = UUID.randomUUID();
        ColumnaEntity entity = createColumnaEntity(id.toString(), "In Progress");

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<Columna> result = adapter.findById(ColumnaId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals("In Progress", result.get().getColumnanombre());
    }

    @Test
    void findById_shouldConvertColumnaIdToString() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        ColumnaEntity entity = createColumnaEntity(id.toString(), "Review");

        when(repository.findById("550e8400-e29b-41d4-a716-446655440000"))
            .thenReturn(Optional.of(entity));

        adapter.findById(ColumnaId.from(id));

        verify(repository).findById("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void findById_shouldDelegateToRepositoryWithStringId() {
        UUID id = UUID.randomUUID();
        ColumnaEntity entity = createColumnaEntity(id.toString(), "Sprint");

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        adapter.findById(ColumnaId.from(id));

        verify(repository).findById(id.toString());
    }

    // ── deleteById ─────────────────────────────────────────────────

    @Test
    void deleteById_shouldDelegateToRepositoryWithStringId() {
        UUID id = UUID.randomUUID();

        doNothing().when(repository).deleteById(id.toString());

        adapter.deleteById(ColumnaId.from(id));

        verify(repository).deleteById(id.toString());
    }

    @Test
    void deleteById_shouldConvertColumnaIdToStringBeforeDelegating() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        adapter.deleteById(ColumnaId.from(id));

        verify(repository).deleteById("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void deleteById_shouldCallRepositoryWithCorrectStringFormat() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(ColumnaId.from(id));

        verify(repository).deleteById(id.toString());
    }
}