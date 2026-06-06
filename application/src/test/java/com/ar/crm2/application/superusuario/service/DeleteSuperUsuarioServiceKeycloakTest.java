package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.identity.port.out.SetIdentityEnabledPort;
import com.ar.crm2.application.superusuario.command.DeleteSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.out.DeleteSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
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
 * Unit tests for {@link DeleteSuperUsuarioService} Keycloak disable flow.
 * Parity with {@link DeleteUsuarioServiceKeycloakTest}: disable then delete success,
 * Keycloak disable failure, null keycloakId handling.
 */
@ExtendWith(MockitoExtension.class)
class DeleteSuperUsuarioServiceKeycloakTest {

    private static final String CORREO = "admin@crm2.com";
    private static final String KEYCLOAK_ID = "kc-su-uuid-delete-888";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Mock
    private FindSuperUsuarioByIdPort findPort;

    @Mock
    private DeleteSuperUsuarioByIdPort deletePort;

    @Mock
    private SetIdentityEnabledPort setEnabledPort;

    @InjectMocks
    private DeleteSuperUsuarioService service;

    // ── Happy path ─────────────────────────────────────────────────

    @Nested
    @DisplayName("delete disables Keycloak user then deletes local")
    class DeleteExitoso {

        @Test
        @DisplayName("disables Keycloak user first, then deletes local record")
        void delete_exitoso_desactivaKeycloakAntesDeLocal() {
            // Given
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                suId, CORREO,
                AHORA.minusDays(1), true, KEYCLOAK_ID
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            DeleteSuperUsuarioCommand cmd = new DeleteSuperUsuarioCommand(id);

            // When
            service.delete(cmd);

            // Then — disable called first, then local delete
            verify(setEnabledPort).setEnabled(KEYCLOAK_ID, false);
            verify(deletePort).deleteById(suId);
        }

        @Test
        @DisplayName("deletes local record even when keycloakId is null")
        void delete_sinKeycloakId_eliminaLocalSolamente() {
            // Given
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                suId, CORREO,
                AHORA.minusDays(1), true, null
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            DeleteSuperUsuarioCommand cmd = new DeleteSuperUsuarioCommand(id);

            // When
            service.delete(cmd);

            // Then — no Keycloak call, only local delete
            verify(setEnabledPort, never()).setEnabled(any(), anyBoolean());
            verify(deletePort).deleteById(suId);
        }
    }

    // ── Keycloak disable failure ─────────────────────────────────

    @Nested
    @DisplayName("Keycloak disable fails — local delete still proceeds")
    class KeycloakDisableFalla {

        @Test
        @DisplayName("throws when Keycloak disable throws — local delete not attempted")
        void delete_keycloakFalla_lanzaExcepcion() {
            // Given
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                suId, CORREO,
                AHORA.minusDays(1), true, KEYCLOAK_ID
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));
            doThrow(new RuntimeException("Keycloak unreachable"))
                .when(setEnabledPort).setEnabled(KEYCLOAK_ID, false);

            DeleteSuperUsuarioCommand cmd = new DeleteSuperUsuarioCommand(id);

            // When / Then
            assertThrows(RuntimeException.class, () -> service.delete(cmd));
            // Local delete is NOT attempted when Keycloak disable fails (consistent with Usuario)
            verify(deletePort, never()).deleteById(any());
        }
    }

    // ── Not found ─────────────────────────────────────────────────

    @Nested
    @DisplayName("superusuario not found")
    class SuperUsuarioNoEncontrado {

        @Test
        @DisplayName("throws SuperUsuarioNotFoundException when entity does not exist")
        void delete_entidadNoExistente_lanzaExcepcion() {
            // Given
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            when(findPort.findById(suId)).thenReturn(Optional.empty());

            DeleteSuperUsuarioCommand cmd = new DeleteSuperUsuarioCommand(id);

            // When / Then
            assertThrows(SuperUsuarioNotFoundException.class, () -> service.delete(cmd));
            verify(deletePort, never()).deleteById(any());
            verify(setEnabledPort, never()).setEnabled(any(), anyBoolean());
        }
    }
}