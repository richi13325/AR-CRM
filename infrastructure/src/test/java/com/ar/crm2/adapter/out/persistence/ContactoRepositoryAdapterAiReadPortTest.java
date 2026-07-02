package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ContactoEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ContactoMapper;
import com.ar.crm2.adapter.out.persistence.repository.ContactoRepository;
import com.ar.crm2.application.ai.port.out.ContactoLecturaPort;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
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
 * Tests that {@link ContactoRepositoryAdapter} satisfies the
 * application-owned AI read port contract
 * ({@link ContactoLecturaPort}). The existing repository adapter is the
 * single port implementation — no separate bridge class is needed
 * because {@code findById(ContactoId)} already exposes the right return
 * type and parameters for the AI read port.
 *
 * <p>Tenant authorization is enforced by the AI application service
 * that consumes this port; the adapter itself is tenant-blind.
 */
@ExtendWith(MockitoExtension.class)
class ContactoRepositoryAdapterAiReadPortTest {

    @Mock
    private ContactoRepository repository;

    @InjectMocks
    private ContactoRepositoryAdapter adapter;

    private Contacto createDomain(UUID id) {
        return Contacto.reconstitute(
            ContactoId.from(id),
            EmpresaId.create(),
            UsuarioId.create(),
            UsuarioId.create(),
            "Juan",
            "juan@example.com",
            "+54911...",
            "CEO",
            "LinkedIn",
            LocalDateTime.of(2026, 6, 23, 15, 0),
            LocalDateTime.of(2026, 6, 23, 15, 0),
            EstadoRelacion.ACTIVO
        );
    }

    @Test
    void findById_throughContactoLecturaPort_shouldReturnMappedDomain() {
        ContactoLecturaPort port = adapter;
        UUID id = UUID.randomUUID();
        ContactoEntity entity = ContactoMapper.toEntity(createDomain(id));

        when(repository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<Contacto> result = port.findById(ContactoId.from(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals("Juan", result.get().getNombre());
        verify(repository).findById(id.toString());
    }

    @Test
    void findById_throughContactoLecturaPort_shouldReturnEmptyWhenRepositoryMisses() {
        ContactoLecturaPort port = adapter;
        UUID id = UUID.randomUUID();

        when(repository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<Contacto> result = port.findById(ContactoId.from(id));

        assertTrue(result.isEmpty());
        verify(repository).findById(id.toString());
    }
}