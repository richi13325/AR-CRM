package com.ar.crm2.model.entity;

import com.ar.crm2.model.vo.SuperUsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SuperUsuario} domain behavior, focused on keycloakId linkage.
 * Spring-free — pure domain unit tests.
 */
class SuperUsuarioTest {

    private static final String CORREO = "admin@example.com";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    // ── create() with keycloakId ───────────────────────────────────

    @Nested
    @DisplayName("create() with keycloakId")
    class CrearConKeycloakId {

        @Test
        @DisplayName("succeeds when keycloakId is a non-blank string within max length")
        void crear_conKeycloakIdValido_exitoso() {
            String keycloakId = "superuser-keycloak-uuid-abc123";

            assertThatCode(() -> SuperUsuario.create(
                    CORREO,
                    keycloakId
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("sets keycloakId field when provided")
        void crear_conKeycloakIdValido_keycloakIdQuedaEstablecido() {
            String keycloakId = "superuser-keycloak-uuid-abc123";

            SuperUsuario superUsuario = SuperUsuario.create(
                    CORREO,
                    keycloakId
            );

            assertThat(superUsuario.getKeycloakId()).isEqualTo(keycloakId);
        }

        @Test
        @DisplayName("sets null when keycloakId is omitted")
        void crear_sinKeycloakId_keycloakIdEsNull() {
            SuperUsuario superUsuario = SuperUsuario.create(
                    CORREO,
                    null
            );

            assertThat(superUsuario.getKeycloakId()).isNull();
        }

        @Test
        @DisplayName("normalizes blank keycloakId to null")
        void crear_conKeycloakIdBlank_normalizaANull() {
            SuperUsuario superUsuario = SuperUsuario.create(
                    CORREO,
                    "   "
            );

            assertThat(superUsuario.getKeycloakId()).isNull();
        }

        @Test
        @DisplayName("throws when keycloakId exceeds 255 characters")
        void crear_conKeycloakIdMuyLargo_lanzaExcepcion() {
            String keycloakIdTooLong = "b".repeat(256);

            assertThatThrownBy(() -> SuperUsuario.create(
                    CORREO,
                    keycloakIdTooLong
            )).isInstanceOf(Exception.class);
        }
    }

    // ── reconstitute() with keycloakId ───────────────────────────────────

    @Nested
    @DisplayName("reconstitute() with keycloakId")
    class ReconstituirConKeycloakId {

        @Test
        @DisplayName("preserves keycloakId from persistence when value is present")
        void reconstituir_conKeycloakId_preservaElValor() {
            SuperUsuarioId id = SuperUsuarioId.create();
            String keycloakId = "persisted-superuser-keycloak";
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);

            SuperUsuario superUsuario = SuperUsuario.reconstitute(
                    id,
                    CORREO,
                    haceUnaSemana,
                    true,
                    keycloakId
            );

            assertThat(superUsuario.getKeycloakId()).isEqualTo(keycloakId);
        }

        @Test
        @DisplayName("preserves null keycloakId when persistence has no linkage")
        void reconstituir_sinKeycloakId_preservaNull() {
            SuperUsuarioId id = SuperUsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);

            SuperUsuario superUsuario = SuperUsuario.reconstitute(
                    id,
                    CORREO,
                    haceUnaSemana,
                    true,
                    null
            );

            assertThat(superUsuario.getKeycloakId()).isNull();
        }

        @Test
        @DisplayName("trims whitespace from keycloakId during reconstitution")
        void reconstituir_conKeycloakIdConEspacios_recortaYGuarda() {
            SuperUsuarioId id = SuperUsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);
            String keycloakIdWithSpaces = "  trimmed-super-id  ";

            SuperUsuario superUsuario = SuperUsuario.reconstitute(
                    id,
                    CORREO,
                    haceUnaSemana,
                    true,
                    keycloakIdWithSpaces
            );

            assertThat(superUsuario.getKeycloakId()).isEqualTo("trimmed-super-id");
        }
    }

    // ── withKeycloakId() ─────────────────────────────────────────────

    @Nested
    @DisplayName("withKeycloakId()")
    class ConKeycloakId {

        @Test
        @DisplayName("returns new SuperUsuario with updated keycloakId")
        void withKeycloakId_cambiaKeycloakId() {
            SuperUsuarioId id = SuperUsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);
            SuperUsuario original = SuperUsuario.reconstitute(
                    id, CORREO, haceUnaSemana, true, null
            );

            SuperUsuario linked = original.withKeycloakId("new-superuser-keycloak");

            assertThat(linked.getKeycloakId()).isEqualTo("new-superuser-keycloak");
            assertThat(linked.getId()).isEqualTo(id);
            assertThat(linked.getCorreo()).isEqualTo(CORREO);
        }

        @Test
        @DisplayName("original SuperUsuario remains unchanged — immutable")
        void withKeycloakId_inmutable() {
            SuperUsuarioId id = SuperUsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);
            SuperUsuario original = SuperUsuario.reconstitute(
                    id, CORREO, haceUnaSemana, true, "original-super-id"
            );

            SuperUsuario linked = original.withKeycloakId("new-super-id");

            assertThat(original.getKeycloakId()).isEqualTo("original-super-id");
            assertThat(original).isNotSameAs(linked);
        }
    }
}