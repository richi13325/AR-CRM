package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.TratoEntity;
import com.ar.crm2.adapter.out.persistence.mapper.TratoMapper;
import com.ar.crm2.adapter.out.persistence.repository.TratoRepository;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.enums.EstadoTrato;
import com.ar.crm2.model.enums.TipoContrato;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link TratoRepositoryAdapter} satisfies the
 * application-owned {@link FindTratoByIdPort} contract.
 *
 * <p>The same {@code findById(TratoId)} implementation is consumed by
 * CRM use cases ({@code GetTratoByIdService}, etc.) and AI assistant
 * flows — there is no separate AI read port anymore.
 *
 * <p>Tenant authorization is enforced by the AI application service
 * that consumes this port; the adapter itself is tenant-blind.
 */
@ExtendWith(MockitoExtension.class)
class TratoRepositoryAdapterFindByIdTest {

    @Mock
    private TratoRepository repository;

    @InjectMocks
    private TratoRepositoryAdapter adapter;

    private Trato createDomain(UUID id) {
        return Trato.reconstitute(
            TratoId.from(id),
            ContactoId.create(),
            UsuarioId.create(),
            "Demo Q3",
            new BigDecimal("10000.00"),
            50,
            LocalDate.of(2026, 9, 30),
            TipoContrato.SERVICIO,
            EstadoTrato.ABIERTO,
            null,
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 23, 15, 0)
        );
    }

    @Test
    void findById_shouldReturnMappedDomain() {
        FindTratoByIdPort port = adapter;
        UUID id = UUID.randomUUID();
        TratoEntity entity = TratoMapper.toEntity(createDomain(id));

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<Trato> result = port.findById(TratoId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals("Demo Q3", result.get().getNombre());
        verify(repository).findById(id.toString());
    }

    @Test
    void findById_shouldReturnEmptyWhenRepositoryMisses() {
        FindTratoByIdPort port = adapter;
        UUID id = UUID.randomUUID();

        when(repository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<Trato> result = port.findById(TratoId.from(id));

        assertTrue(result.isEmpty());
        verify(repository).findById(id.toString());
    }
}
