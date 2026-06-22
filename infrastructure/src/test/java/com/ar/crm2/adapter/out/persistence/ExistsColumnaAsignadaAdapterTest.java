package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.model.vo.ColumnaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link ExistsColumnaAsignadaPort} contract as implemented
 * by {@link ColumnaRepositoryAdapter}.
 *
 * <p>After the Clean Architecture cleanup the dedicated
 * {@code ExistsColumnaAsignadaAdapter} was retired; the port is now satisfied
 * by {@link ColumnaRepositoryAdapter}, which delegates to
 * {@link TableroRepository#existsByColumnasTableroColumnaId(String)} for the
 * column-in-use check used during column deletion.
 */
@ExtendWith(MockitoExtension.class)
class ExistsColumnaAsignadaAdapterTest {

    @Mock
    private ColumnaRepository columnaRepository;

    @Mock
    private TableroRepository tableroRepository;

    @InjectMocks
    private ColumnaRepositoryAdapter adapter;

    // ── existsByColumnaId ───────────────────────────────────────────────

    @Test
    void existsByColumnaId_shouldReturnTrueWhenColumnaIsAssigned() {
        UUID columnaId = UUID.randomUUID();

        when(tableroRepository.existsByColumnasTableroColumnaId(columnaId.toString())).thenReturn(true);

        boolean result = adapter.existsByColumnaId(ColumnaId.from(columnaId));

        assertTrue(result);
        verify(tableroRepository).existsByColumnasTableroColumnaId(columnaId.toString());
    }

    @Test
    void existsByColumnaId_shouldReturnFalseWhenColumnaIsNotAssigned() {
        UUID columnaId = UUID.randomUUID();

        when(tableroRepository.existsByColumnasTableroColumnaId(columnaId.toString())).thenReturn(false);

        boolean result = adapter.existsByColumnaId(ColumnaId.from(columnaId));

        assertFalse(result);
    }

    @Test
    void existsByColumnaId_shouldConvertColumnaIdToString() {
        UUID columnaId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        when(tableroRepository.existsByColumnasTableroColumnaId("550e8400-e29b-41d4-a716-446655440000"))
            .thenReturn(false);

        adapter.existsByColumnaId(ColumnaId.from(columnaId));

        verify(tableroRepository).existsByColumnasTableroColumnaId("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void existsByColumnaId_shouldDelegateToTableroRepository() {
        UUID columnaId = UUID.randomUUID();

        adapter.existsByColumnaId(ColumnaId.from(columnaId));

        verify(tableroRepository).existsByColumnasTableroColumnaId(columnaId.toString());
    }

    @Test
    void existsByColumnaId_shouldReturnTrueForCatalogColumnInUse() {
        // Simulates a PREDETERMINADA catalog column that has been added to a board
        UUID columnaId = UUID.randomUUID();

        when(tableroRepository.existsByColumnasTableroColumnaId(columnaId.toString())).thenReturn(true);

        boolean result = adapter.existsByColumnaId(ColumnaId.from(columnaId));

        assertTrue(result);
    }

    @Test
    void existsByColumnaId_shouldReturnFalseForUnusedCatalogColumn() {
        // Simulates a catalog column that has never been assigned to any board
        UUID columnaId = UUID.randomUUID();

        when(tableroRepository.existsByColumnasTableroColumnaId(columnaId.toString())).thenReturn(false);

        boolean result = adapter.existsByColumnaId(ColumnaId.from(columnaId));

        assertFalse(result);
    }
}