package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.SuperUsuarioEntity;
import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SuperUsuarioMapper} keycloakId mapping behavior.
 * Tests the static mapper methods for entity ↔ domain conversion with keycloakId.
 */
class SuperUsuarioMapperTest {

    private static final String CORREO = "admin@example.com";
    private static final String PASSWORD_HASH = "secret-hash-xyz";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    // ── toDomain() with keycloakId ─────────────────────────────────

    @Nested
    @DisplayName("toDomain() with keycloakId")
    class ToDomainConKeycloakId {

        @Test
        @DisplayName("maps keycloakId from entity to domain when present")
        void toDomain_conKeycloakId_mappeaCorrectamente() {
            String keycloakId = "super-entity-keycloak-789";
            SuperUsuarioEntity entity = SuperUsuarioEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .correo(CORREO)
                    .passwordHash(PASSWORD_HASH)
                    .creadoEn(AHORA)
                    .activo(true)
                    .keycloakId(keycloakId)
                    .build();

            SuperUsuario domain = SuperUsuarioMapper.toDomain(entity);

            assertEquals(keycloakId, domain.getKeycloakId());
        }

        @Test
        @DisplayName("maps null keycloakId from entity to domain")
        void toDomain_sinKeycloakId_mappeaNull() {
            SuperUsuarioEntity entity = SuperUsuarioEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .correo(CORREO)
                    .passwordHash(PASSWORD_HASH)
                    .creadoEn(AHORA)
                    .activo(true)
                    .keycloakId(null)
                    .build();

            SuperUsuario domain = SuperUsuarioMapper.toDomain(entity);

            assertNull(domain.getKeycloakId());
        }

        @Test
        @DisplayName("throws when entity id is null")
        void toDomain_conIdNulo_lanzaExcepcion() {
            SuperUsuarioEntity entity = SuperUsuarioEntity.builder()
                    .id(null)
                    .correo(CORREO)
                    .passwordHash(PASSWORD_HASH)
                    .creadoEn(AHORA)
                    .activo(true)
                    .build();

            assertThrows(IllegalArgumentException.class, () -> SuperUsuarioMapper.toDomain(entity));
        }
    }

    // ── toEntity() with keycloakId ─────────────────────────────────

    @Nested
    @DisplayName("toEntity() with keycloakId")
    class ToEntityConKeycloakId {

        @Test
        @DisplayName("maps keycloakId from domain to entity when present")
        void toEntity_conKeycloakId_mappeaCorrectamente() {
            String keycloakId = "super-domain-keycloak-456";
            SuperUsuario domain = SuperUsuario.reconstitute(
                    SuperUsuarioId.create(),
                    CORREO,
                    PASSWORD_HASH,
                    AHORA,
                    true,
                    keycloakId
            );

            SuperUsuarioEntity entity = SuperUsuarioMapper.toEntity(domain);

            assertEquals(keycloakId, entity.getKeycloakId());
        }

        @Test
        @DisplayName("maps null keycloakId from domain to entity")
        void toEntity_sinKeycloakId_mappeaNull() {
            SuperUsuario domain = SuperUsuario.reconstitute(
                    SuperUsuarioId.create(),
                    CORREO,
                    PASSWORD_HASH,
                    AHORA,
                    true,
                    null
            );

            SuperUsuarioEntity entity = SuperUsuarioMapper.toEntity(domain);

            assertNull(entity.getKeycloakId());
        }

        @Test
        @DisplayName("returns null when domain is null")
        void toEntity_conDomainNulo_retornaNull() {
            SuperUsuarioEntity entity = SuperUsuarioMapper.toEntity(null);

            assertNull(entity);
        }
    }
}