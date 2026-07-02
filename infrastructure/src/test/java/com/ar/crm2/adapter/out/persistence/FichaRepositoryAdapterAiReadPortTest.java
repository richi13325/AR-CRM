package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.FichaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.FichaMapper;
import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.application.ai.port.out.FichaLecturaPort;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TareaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link FichaRepositoryAdapter} satisfies the
 * application-owned AI read port contract
 * ({@link FichaLecturaPort}). The existing repository adapter is the
 * single port implementation — no separate bridge class is needed.
 *
 * <p>Tenant authorization is enforced by the AI application service
 * that consumes this port; the adapter itself is tenant-blind.
 */
@ExtendWith(MockitoExtension.class)
class FichaRepositoryAdapterAiReadPortTest {

    @Mock
    private FichaRepository repository;

    @InjectMocks
    private FichaRepositoryAdapter adapter;

    private Ficha createDomain(UUID id) {
        return Ficha.reconstitute(
            FichaId.from(id),
            ColumnaId.create(),
            TipoFicha.TAREA,
            null,
            TareaId.create(),
            Instant.now(),
            List.of()
        );
    }

    @Test
    void findById_throughFichaLecturaPort_shouldReturnMappedDomain() {
        FichaLecturaPort port = adapter;
        UUID id = UUID.randomUUID();
        FichaEntity entity = FichaMapper.toEntity(createDomain(id));

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<Ficha> result = port.findById(FichaId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals(TipoFicha.TAREA, result.get().getTipoFicha());
        verify(repository).findById(id.toString());
    }

    @Test
    void findById_throughFichaLecturaPort_shouldReturnEmptyWhenRepositoryMisses() {
        FichaLecturaPort port = adapter;
        UUID id = UUID.randomUUID();

        when(repository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<Ficha> result = port.findById(FichaId.from(id));

        assertTrue(result.isEmpty());
        verify(repository).findById(id.toString());
    }
}