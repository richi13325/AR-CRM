package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.UsuarioEntity;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UsuarioMapper} keycloakId mapping behavior.
 * Tests the static mapper methods for entity ↔ domain conversion with keycloakId.
 */
class UsuarioMapperTest {

    private static final String NOMBRE = "Juan Perez";
    private static final String CORREO = "juan@example.com";
    private static final String PASSWORD_HASH = "hash-abc-123";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    // ── toDomain() with keycloakId ─────────────────────────────────

    @Nested
    @DisplayName("toDomain() with keycloakId")
    class ToDomainConKeycloakId {

        @Test
        @DisplayName("maps keycloakId from entity to domain when present")
        void toDomain_conKeycloakId_mappeaCorrectamente() {
            String keycloakId = "entity-keycloak-uuid-123";
            UsuarioEntity entity = UsuarioEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .nombre(NOMBRE)
                    .correo(CORREO)
                    .passwordHash(PASSWORD_HASH)
                    .rolId(UUID.randomUUID().toString())
                    .creadoEn(AHORA)
                    .activo(true)
                    .keycloakId(keycloakId)
                    .build();

            Usuario domain = UsuarioMapper.toDomain(entity);

            assertEquals(keycloakId, domain.getKeycloakId());
        }

        @Test
        @DisplayName("maps null keycloakId from entity to domain")
        void toDomain_sinKeycloakId_mappeaNull() {
            UsuarioEntity entity = UsuarioEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .nombre(NOMBRE)
                    .correo(CORREO)
                    .passwordHash(PASSWORD_HASH)
                    .rolId(UUID.randomUUID().toString())
                    .creadoEn(AHORA)
                    .activo(true)
                    .keycloakId(null)
                    .build();

            Usuario domain = UsuarioMapper.toDomain(entity);

            assertNull(domain.getKeycloakId());
        }

        @Test
        @DisplayName("throws when entity id is null")
        void toDomain_conIdNulo_lanzaExcepcion() {
            UsuarioEntity entity = UsuarioEntity.builder()
                    .id(null)
                    .nombre(NOMBRE)
                    .correo(CORREO)
                    .passwordHash(PASSWORD_HASH)
                    .rolId(UUID.randomUUID().toString())
                    .creadoEn(AHORA)
                    .activo(true)
                    .build();

            assertThrows(IllegalArgumentException.class, () -> UsuarioMapper.toDomain(entity));
        }
    }

    // ── toEntity() with keycloakId ─────────────────────────────────

    @Nested
    @DisplayName("toEntity() with keycloakId")
    class ToEntityConKeycloakId {

        @Test
        @DisplayName("maps keycloakId from domain to entity when present")
        void toEntity_conKeycloakId_mappeaCorrectamente() {
            String keycloakId = "domain-keycloak-456";
            Usuario domain = Usuario.reconstitute(
                    UsuarioId.create(),
                    NOMBRE,
                    CORREO,
                    PASSWORD_HASH,
                    RolId.create(),
                    AHORA,
                    true,
                    keycloakId
            );

            UsuarioEntity entity = UsuarioMapper.toEntity(domain);

            assertEquals(keycloakId, entity.getKeycloakId());
        }

        @Test
        @DisplayName("maps null keycloakId from domain to entity")
        void toEntity_sinKeycloakId_mappeaNull() {
            Usuario domain = Usuario.reconstitute(
                    UsuarioId.create(),
                    NOMBRE,
                    CORREO,
                    PASSWORD_HASH,
                    RolId.create(),
                    AHORA,
                    true,
                    null
            );

            UsuarioEntity entity = UsuarioMapper.toEntity(domain);

            assertNull(entity.getKeycloakId());
        }

        @Test
        @DisplayName("returns null when domain is null")
        void toEntity_conDomainNulo_retornaNull() {
            UsuarioEntity entity = UsuarioMapper.toEntity(null);

            assertNull(entity);
        }
    }
}