package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.SuperUsuarioEntity;
import com.ar.crm2.adapter.out.persistence.mapper.SuperUsuarioMapper;
import com.ar.crm2.adapter.out.persistence.repository.SuperUsuarioRepository;
import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SuperUsuarioRepositoryAdapter} keycloakId behaviors.
 */
@ExtendWith(MockitoExtension.class)
class SuperUsuarioRepositoryAdapterKeycloakTest {

    @Mock
    private SuperUsuarioRepository repository;

    @InjectMocks
    private SuperUsuarioRepositoryAdapter adapter;

    private static final String CORREO = "admin@example.com";
    private static final String KEYCLOAK_ID = "super-kc-uuid-xyz";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    // ── findByKeycloakId ───────────────────────────────────────────────

    @Nested
    @DisplayName("findByKeycloakId()")
    class FindByKeycloakId {

        @Test
        @DisplayName("returns empty when no SuperUsuario with that keycloakId exists")
        void findByKeycloakId_notFound_returnsEmpty() {
            when(repository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());

            Optional<SuperUsuario> result = adapter.findByKeycloakId(KEYCLOAK_ID);

            assertTrue(result.isEmpty());
            verify(repository).findByKeycloakId(KEYCLOAK_ID);
        }

        @Test
        @DisplayName("returns SuperUsuario domain when found by keycloakId")
        void findByKeycloakId_found_returnsMappedDomain() {
            String id = UUID.randomUUID().toString();
            SuperUsuarioEntity entity = SuperUsuarioEntity.builder()
                    .id(id)
                    .correo(CORREO)
                    .creadoEn(AHORA)
                    .activo(true)
                    .keycloakId(KEYCLOAK_ID)
                    .build();

            when(repository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(entity));

            Optional<SuperUsuario> result = adapter.findByKeycloakId(KEYCLOAK_ID);

            assertTrue(result.isPresent());
            assertEquals(KEYCLOAK_ID, result.get().getKeycloakId());
        }
    }

    // ── save preserves keycloakId ────────────────────────────────────

    @Nested
    @DisplayName("save() preserves keycloakId")
    class SavePreservesKeycloakId {

        @Test
        @DisplayName("save with keycloakId round-trips correctly")
        void save_conKeycloakId_roundTrips() {
            SuperUsuarioId suId = SuperUsuarioId.create();
            SuperUsuario domain = SuperUsuario.reconstitute(
                    suId,
                    CORREO,
                    AHORA,
                    true,
                    KEYCLOAK_ID
            );

            String id = UUID.randomUUID().toString();
            SuperUsuarioEntity savedEntity = SuperUsuarioEntity.builder()
                    .id(id)
                    .correo(CORREO)
                    .creadoEn(AHORA)
                    .activo(true)
                    .keycloakId(KEYCLOAK_ID)
                    .build();

            when(repository.save(any(SuperUsuarioEntity.class))).thenReturn(savedEntity);

            SuperUsuario result = adapter.save(domain);

            assertEquals(KEYCLOAK_ID, result.getKeycloakId());
        }
    }
}