package com.ar.crm2.model.entity;

import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Usuario} domain behavior, focused on keycloakId linkage.
 * Spring-free — pure domain unit tests.
 */
class UsuarioTest {

    private static final String NOMBRE = "Juan Perez";
    private static final String CORREO = "juan@example.com";
    private static final RolId ROL_ID = RolId.create();
    private static final LocalDateTime AHORA = LocalDateTime.now();

    // ── create() with keycloakId ───────────────────────────────────

    @Nested
    @DisplayName("create() with keycloakId")
    class CrearConKeycloakId {

        @Test
        @DisplayName("succeeds when keycloakId is a non-blank string within max length")
        void crear_conKeycloakIdValido_exitoso() {
            String keycloakId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

            assertThatCode(() -> Usuario.create(
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    keycloakId
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("sets keycloakId field when provided")
        void crear_conKeycloakIdValido_keycloakIdQuedaEstablecido() {
            String keycloakId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

            Usuario usuario = Usuario.create(
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    keycloakId
            );

            assertThat(usuario.getKeycloakId()).isEqualTo(keycloakId);
        }

        @Test
        @DisplayName("normalizes blank keycloakId to null")
        void crear_conKeycloakIdBlank_normalizaANull() {
            Usuario usuario = Usuario.create(
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    "   "
            );

            assertThat(usuario.getKeycloakId()).isNull();
        }

        @Test
        @DisplayName("sets null when keycloakId is omitted")
        void crear_sinKeycloakId_keycloakIdEsNull() {
            Usuario usuario = Usuario.create(
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    null
            );

            assertThat(usuario.getKeycloakId()).isNull();
        }

        @Test
        @DisplayName("throws when keycloakId exceeds 255 characters")
        void crear_conKeycloakIdMuyLargo_lanzaExcepcion() {
            String keycloakIdTooLong = "a".repeat(256);

            assertThatThrownBy(() -> Usuario.create(
                    NOMBRE,
                    CORREO,
                    ROL_ID,
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
            UsuarioId id = UsuarioId.create();
            String keycloakId = "persisted-keycloak-uuid";
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);

            Usuario usuario = Usuario.reconstitute(
                    id,
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    haceUnaSemana,
                    true,
                    keycloakId
            );

            assertThat(usuario.getKeycloakId()).isEqualTo(keycloakId);
        }

        @Test
        @DisplayName("preserves null keycloakId when persistence has no linkage")
        void reconstituir_sinKeycloakId_preservaNull() {
            UsuarioId id = UsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);

            Usuario usuario = Usuario.reconstitute(
                    id,
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    haceUnaSemana,
                    true,
                    null
            );

            assertThat(usuario.getKeycloakId()).isNull();
        }

        @Test
        @DisplayName("normalizes blank keycloakId from persistence to null")
        void reconstituir_conKeycloakIdBlank_normalizaANull() {
            UsuarioId id = UsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);

            Usuario usuario = Usuario.reconstitute(
                    id,
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    haceUnaSemana,
                    true,
                    "  "
            );

            assertThat(usuario.getKeycloakId()).isNull();
        }

        @Test
        @DisplayName("trims whitespace from keycloakId during reconstitution")
        void reconstituir_conKeycloakIdConEspacios_recortaYGuarda() {
            UsuarioId id = UsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);
            String keycloakIdWithSpaces = "  trimmed-id-123  ";

            Usuario usuario = Usuario.reconstitute(
                    id,
                    NOMBRE,
                    CORREO,
                    ROL_ID,
                    haceUnaSemana,
                    true,
                    keycloakIdWithSpaces
            );

            assertThat(usuario.getKeycloakId()).isEqualTo("trimmed-id-123");
        }
    }

    // ── withKeycloakId() ─────────────────────────────────────────────

    @Nested
    @DisplayName("withKeycloakId()")
    class ConKeycloakId {

        @Test
        @DisplayName("returns new Usuario with updated keycloakId")
        void withKeycloakId_cambiaKeycloakId() {
            UsuarioId id = UsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);
            Usuario original = Usuario.reconstitute(
                    id, NOMBRE, CORREO, ROL_ID, haceUnaSemana, true, null
            );

            Usuario linked = original.withKeycloakId("new-keycloak-id");

            assertThat(linked.getKeycloakId()).isEqualTo("new-keycloak-id");
            assertThat(linked.getId()).isEqualTo(id);
            assertThat(linked.getCorreo()).isEqualTo(CORREO);
        }

        @Test
        @DisplayName("original Usuario remains unchanged — immutable")
        void withKeycloakId_inmutable() {
            UsuarioId id = UsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);
            Usuario original = Usuario.reconstitute(
                    id, NOMBRE, CORREO, ROL_ID, haceUnaSemana, true, "original-id"
            );

            Usuario linked = original.withKeycloakId("new-id");

            assertThat(original.getKeycloakId()).isEqualTo("original-id");
            assertThat(original).isNotSameAs(linked);
        }

        @Test
        @DisplayName("clears keycloakId when null is passed")
        void withKeycloakId_conNull_limpiaKeycloakId() {
            UsuarioId id = UsuarioId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);
            Usuario original = Usuario.reconstitute(
                    id, NOMBRE, CORREO, ROL_ID, haceUnaSemana, true, "existing-id"
            );

            Usuario cleared = original.withKeycloakId(null);

            assertThat(cleared.getKeycloakId()).isNull();
        }
    }
}