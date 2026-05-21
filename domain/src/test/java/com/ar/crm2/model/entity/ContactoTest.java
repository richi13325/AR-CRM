package com.ar.crm2.model.entity;

import com.ar.crm2.exception.ContactoStateTransitionException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Contacto} domain behavior.
 * Spring-free — pure domain unit tests.
 */
class ContactoTest {

    private static final EmpresaId EMPRESA_ID = EmpresaId.create();
    private static final UsuarioId RESPONSABLE_ID = UsuarioId.create();
    private static final UsuarioId CREADO_POR_ID = UsuarioId.create();
    private static final LocalDateTime AHORA = LocalDateTime.now();

    // ── create() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Crear {

        @Test
        @DisplayName("succeeds when estadoRelacion is provided")
        void crear_conEstadoRelacion_exitoso() {
            assertThatCode(() -> Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.PROSPECTO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when estadoRelacion is null")
        void crear_conEstadoRelacionNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    null,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            ))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("estadoRelacion");
        }

        @Test
        @DisplayName("succeeds when estadoRelacion is ACTIVO")
        void crear_conEstadoActivo_exitoso() {
            assertThatCode(() -> Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.ACTIVO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("succeeds when estadoRelacion is INACTIVO")
        void crear_conEstadoInactivo_exitoso() {
            assertThatCode(() -> Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.INACTIVO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            )).doesNotThrowAnyException();
        }
    }

    // ── reconstitute() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("succeeds when estadoRelacion is provided")
        void reconstituir_conEstadoRelacion_exitoso() {
            ContactoId id = ContactoId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);

            assertThatCode(() -> Contacto.reconstitute(
                    id,
                    EMPRESA_ID,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "Juan Perez",
                    "juan@example.com",
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn",
                    haceUnaSemana,
                    haceUnaSemana,
                    EstadoRelacion.ACTIVO
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when estadoRelacion is null")
        void reconstituir_conEstadoRelacionNulo_lanzaExcepcion() {
            ContactoId id = ContactoId.create();
            LocalDateTime haceUnaSemana = AHORA.minusDays(7);

            assertThatThrownBy(() -> Contacto.reconstitute(
                    id,
                    EMPRESA_ID,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "Juan Perez",
                    "juan@example.com",
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn",
                    haceUnaSemana,
                    haceUnaSemana,
                    null
            ))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("estadoRelacion");
        }
    }

    // ── cambiarEstadoRelacion() ───────────────────────────────────────────────

    @Nested
    @DisplayName("cambiarEstadoRelacion()")
    class CambiarEstadoRelacion {

        @Test
        @DisplayName("returns same instance when target state equals current state (idempotent)")
        void cambiarEstadoRelacion_mismoEstado_retornaMismaInstancia() {
            Contacto contacto = Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.PROSPECTO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            );

            Contacto resultado = contacto.cambiarEstadoRelacion(EstadoRelacion.PROSPECTO, false);

            assertThat(resultado).isSameAs(contacto);
        }

        @Test
        @DisplayName("succeeds when transitioning from PROSPECTO to ACTIVO")
        void cambiarEstadoRelacion_prospectoAActivo_exitoso() {
            Contacto contacto = Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.PROSPECTO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            );

            Contacto resultado = contacto.cambiarEstadoRelacion(EstadoRelacion.ACTIVO, false);

            assertThat(resultado).isNotSameAs(contacto);
            assertThat(resultado.getEstadoRelacion()).isEqualTo(EstadoRelacion.ACTIVO);
            assertThat(resultado.getActualizadoEn()).isAfterOrEqualTo(contacto.getCreadoEn());
        }

        @Test
        @DisplayName("throws when transitioning from ACTIVO back to PROSPECTO")
        void cambiarEstadoRelacion_activoAProspecto_lanzaExcepcion() {
            Contacto contacto = Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.ACTIVO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            );

            assertThatThrownBy(() -> contacto.cambiarEstadoRelacion(EstadoRelacion.PROSPECTO, false))
                    .isInstanceOf(ContactoStateTransitionException.class)
                    .hasMessageContaining("No se puede volver a Prospecto");
        }

        @Test
        @DisplayName("throws when transitioning from INACTIVO back to PROSPECTO")
        void cambiarEstadoRelacion_inactivoAProspecto_lanzaExcepcion() {
            Contacto contacto = Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.INACTIVO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            );

            assertThatThrownBy(() -> contacto.cambiarEstadoRelacion(EstadoRelacion.PROSPECTO, false))
                    .isInstanceOf(ContactoStateTransitionException.class)
                    .hasMessageContaining("No se puede volver a Prospecto");
        }

        @Test
        @DisplayName("throws when transitioning to INACTIVO with tieneTratosActivos=true")
        void cambiarEstadoRelacion_activoAInactivoConTratosActivos_lanzaExcepcion() {
            Contacto contacto = Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.ACTIVO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            );

            assertThatThrownBy(() -> contacto.cambiarEstadoRelacion(EstadoRelacion.INACTIVO, true))
                    .isInstanceOf(ContactoStateTransitionException.class)
                    .hasMessageContaining("No se puede marcar como inactivo un contacto con tratos activos");
        }

        @Test
        @DisplayName("succeeds when transitioning to INACTIVO with tieneTratosActivos=false")
        void cambiarEstadoRelacion_activoAInactivoSinTratosActivos_exitoso() {
            Contacto contacto = Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.ACTIVO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            );

            Contacto resultado = contacto.cambiarEstadoRelacion(EstadoRelacion.INACTIVO, false);

            assertThat(resultado).isNotSameAs(contacto);
            assertThat(resultado.getEstadoRelacion()).isEqualTo(EstadoRelacion.INACTIVO);
        }

        @Test
        @DisplayName("throws when nuevoEstado is null")
        void cambiarEstadoRelacion_conNull_lanzaExcepcion() {
            Contacto contacto = Contacto.create(
                    EMPRESA_ID,
                    "Juan Perez",
                    "juan@example.com",
                    EstadoRelacion.PROSPECTO,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    "+5491112345678",
                    "Gerente de Ventas",
                    "LinkedIn"
            );

            assertThatThrownBy(() -> contacto.cambiarEstadoRelacion(null, false))
                    .isInstanceOf(InvariantViolationException.class)
                    .hasMessageContaining("nuevoEstado");
        }
    }
}