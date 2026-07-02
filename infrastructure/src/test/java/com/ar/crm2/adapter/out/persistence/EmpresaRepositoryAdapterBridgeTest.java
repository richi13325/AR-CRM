package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.EmpresaEntity;
import com.ar.crm2.adapter.out.persistence.repository.EmpresaRepository;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@code FindEmpresasByCreadorPort} bridge added to
 * {@link EmpresaRepositoryAdapter} for the PR 2 Slice Pattern Review
 * Gate.
 *
 * <p>Tenant ownership is derived from
 * {@code Empresa.creadoPor == usuarioId} (see design.md §Security).
 * The adapter must implement the new port by calling the derived Spring
 * Data query {@code findByCreadoPor(String creadoPor)} on the existing
 * {@link EmpresaRepository}, returning {@link EmpresaId} values
 * (lightweight projection) — no full {@code Empresa} hydration needed
 * at this boundary.
 */
@ExtendWith(MockitoExtension.class)
class EmpresaRepositoryAdapterBridgeTest {

    @Mock
    private EmpresaRepository repository;

    @InjectMocks
    private EmpresaRepositoryAdapter adapter;

    // ── findEmpresasByCreador (FindEmpresasByCreadorPort) ───────────

    @Test
    void findEmpresasByCreador_shouldDelegateToDerivedQueryAndMapIds() {
        UsuarioId creadoPor = UsuarioId.from(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        List<EmpresaEntity> rows = List.of(
            EmpresaEntity.builder().id("11111111-1111-1111-1111-111111111111").build(),
            EmpresaEntity.builder().id("22222222-2222-2222-2222-222222222222").build()
        );
        when(repository.findByCreadoPor(creadoPor.value().toString())).thenReturn(rows);

        List<EmpresaId> result = adapter.findEmpresasByCreador(creadoPor);

        assertEquals(2, result.size());
        assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), result.get(0).value());
        assertEquals(UUID.fromString("22222222-2222-2222-2222-222222222222"), result.get(1).value());
        verify(repository).findByCreadoPor(creadoPor.value().toString());
    }

    @Test
    void findEmpresasByCreador_shouldReturnEmptyListWhenNoMatches() {
        UsuarioId creadoPor = UsuarioId.create();
        when(repository.findByCreadoPor(creadoPor.value().toString())).thenReturn(List.of());

        List<EmpresaId> result = adapter.findEmpresasByCreador(creadoPor);

        assertTrue(result.isEmpty());
    }
}
