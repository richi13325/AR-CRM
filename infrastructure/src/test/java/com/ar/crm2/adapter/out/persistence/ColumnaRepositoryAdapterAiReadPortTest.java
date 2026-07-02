package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ColumnaMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.application.ai.port.out.ColumnaLecturaPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link ColumnaRepositoryAdapter} satisfies the
 * application-owned AI read port contract
 * ({@link ColumnaLecturaPort}). The existing repository adapter is the
 * single port implementation — no separate bridge class is needed.
 *
 * <p>Tenant authorization is enforced by the AI application service
 * that consumes this port; the adapter itself is tenant-blind.
 */
@ExtendWith(MockitoExtension.class)
class ColumnaRepositoryAdapterAiReadPortTest {

    @Mock
    private ColumnaRepository repository;

    @Mock
    private TableroRepository tableroRepository;

    @InjectMocks
    private ColumnaRepositoryAdapter adapter;

    private Columna createDomain(UUID id) {
        return Columna.reconstitute(
            ColumnaId.from(id),
            "Backlog",
            "#FFFFFF",
            TipoTablero.TAREAS,
            TipoColumna.PREDETERMINADA,
            false
        );
    }

    @Test
    void findById_throughColumnaLecturaPort_shouldReturnMappedDomain() {
        ColumnaLecturaPort port = adapter;
        UUID id = UUID.randomUUID();
        ColumnaEntity entity = ColumnaMapper.toEntity(createDomain(id));

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<Columna> result = port.findById(ColumnaId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals("Backlog", result.get().getColumnanombre());
        verify(repository).findById(id.toString());
    }

    @Test
    void findById_throughColumnaLecturaPort_shouldReturnEmptyWhenRepositoryMisses() {
        ColumnaLecturaPort port = adapter;
        UUID id = UUID.randomUUID();

        when(repository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<Columna> result = port.findById(ColumnaId.from(id));

        assertTrue(result.isEmpty());
        verify(repository).findById(id.toString());
    }
}