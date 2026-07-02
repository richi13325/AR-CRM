package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.TareaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.TareaMapper;
import com.ar.crm2.adapter.out.persistence.repository.TareaRepository;
import com.ar.crm2.application.tarea.port.out.FindTareaByIdPort;
import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link TareaRepositoryAdapter} satisfies the
 * application-owned {@link FindTareaByIdPort} contract.
 *
 * <p>The same {@code findById(TareaId)} implementation is consumed by
 * CRM use cases ({@code GetTareaByIdService}, etc.) and AI assistant
 * flows — there is no separate AI read port anymore.
 *
 * <p>Tenant authorization is enforced by the AI application service
 * that consumes this port; the adapter itself is tenant-blind.
 */
@ExtendWith(MockitoExtension.class)
class TareaRepositoryAdapterFindByIdTest {

    @Mock
    private TareaRepository repository;

    @InjectMocks
    private TareaRepositoryAdapter adapter;

    private Tarea createDomain(UUID id) {
        return Tarea.reconstitute(
            TareaId.from(id),
            TratoId.create(),
            UsuarioId.create(),
            "Llamar al cliente",
            "Confirmar presupuesto",
            TipoTarea.SEGUIMIENTO,
            PrioridadTarea.ALTA,
            LocalDateTime.of(2026, 6, 25, 12, 0),
            null,
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 23, 15, 0)
        );
    }

    @Test
    void findById_shouldReturnMappedDomain() {
        FindTareaByIdPort port = adapter;
        UUID id = UUID.randomUUID();
        TareaEntity entity = TareaMapper.toEntity(createDomain(id));

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<Tarea> result = port.findById(TareaId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals("Llamar al cliente", result.get().getTitulo());
        verify(repository).findById(id.toString());
    }

    @Test
    void findById_shouldReturnEmptyWhenRepositoryMisses() {
        FindTareaByIdPort port = adapter;
        UUID id = UUID.randomUUID();

        when(repository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<Tarea> result = port.findById(TareaId.from(id));

        assertTrue(result.isEmpty());
        verify(repository).findById(id.toString());
    }
}
