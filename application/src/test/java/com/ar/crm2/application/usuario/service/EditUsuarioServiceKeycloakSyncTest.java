package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.usuario.command.EditUsuarioCommand;
import com.ar.crm2.application.usuario.exception.UsuarioNotFoundException;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.application.usuario.port.out.SaveUsuarioPort;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EditUsuarioService} Keycloak sync flows.
 * Covers: email sync success, Keycloak email failure (no local update),
 * enabled flag sync.
 */
@ExtendWith(MockitoExtension.class)
class EditUsuarioServiceKeycloakSyncTest {

    private static final String NOMBRE = "Juan Perez";
    private static final String CORREO_OLD = "juan@old.com";
    private static final String CORREO_NEW = "juan@new.com";
    private static final String KEYCLOAK_ID = "kc-uuid-edit-test-456";
    private static final UUID ROL_ID = UUID.randomUUID();
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Mock
    private FindUsuarioByIdPort findPort;

    @Mock
    private SaveUsuarioPort savePort;

    @Mock
    private IdentityProviderUserPort identityPort;

    @InjectMocks
    private EditUsuarioService service;

    // ── Email sync ────────────────────────────────────────────────

    @Nested
    @DisplayName("email sync to Keycloak")
    class SincronizacionEmail {

        @Test
        @DisplayName("syncs email to Keycloak when correo changes, then updates local")
        void edit_emailCambia_sincronizaKeycloakAntesDeLocal() {
            // Given
            UUID id = UUID.randomUUID();
            UsuarioId usuarioId = UsuarioId.from(id);
            Usuario existing = Usuario.reconstitute(
                usuarioId, NOMBRE, CORREO_OLD, RolId.create(),
                AHORA.minusDays(1), true, KEYCLOAK_ID
            );
            when(findPort.findById(usuarioId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditUsuarioCommand cmd = new EditUsuarioCommand(
                id, NOMBRE, CORREO_NEW, ROL_ID, KEYCLOAK_ID
            );

            // When
            Usuario result = service.edit(cmd);

            // Then — email synced first
            verify(identityPort).syncEmail(KEYCLOAK_ID, CORREO_NEW);
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(savePort).save(captor.capture());
            assertEquals(CORREO_NEW, captor.getValue().getCorreo());
        }

        @Test
        @DisplayName("does not call Keycloak email sync when correo unchanged")
        void edit_emailSinCambio_noSincroniza() {
            // Given
            UUID id = UUID.randomUUID();
            UsuarioId usuarioId = UsuarioId.from(id);
            Usuario existing = Usuario.reconstitute(
                usuarioId, NOMBRE, CORREO_OLD, RolId.create(),
                AHORA.minusDays(1), true, KEYCLOAK_ID
            );
            when(findPort.findById(usuarioId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditUsuarioCommand cmd = new EditUsuarioCommand(
                id, NOMBRE, CORREO_OLD, ROL_ID, KEYCLOAK_ID
            );

            // When
            service.edit(cmd);

            // Then
            verify(identityPort, never()).syncEmail(any(), any());
        }

        @Test
        @DisplayName("fails and does not update local when Keycloak email sync fails")
        void edit_emailSyncFalla_noActualizaLocal() {
            // Given
            UUID id = UUID.randomUUID();
            UsuarioId usuarioId = UsuarioId.from(id);
            Usuario existing = Usuario.reconstitute(
                usuarioId, NOMBRE, CORREO_OLD, RolId.create(),
                AHORA.minusDays(1), true, KEYCLOAK_ID
            );
            when(findPort.findById(usuarioId)).thenReturn(Optional.of(existing));
            doThrow(new IdentityProvisioningException(
                "Keycloak error",
                IdentityProvisioningException.Reason.SERVER_ERROR
            )).when(identityPort).syncEmail(KEYCLOAK_ID, CORREO_NEW);

            EditUsuarioCommand cmd = new EditUsuarioCommand(
                id, NOMBRE, CORREO_NEW, ROL_ID, KEYCLOAK_ID
            );

            // When / Then
            assertThrows(IdentityProvisioningException.class, () -> service.edit(cmd));
            verify(savePort, never()).save(any());
        }
    }

    // ── Enabled flag sync ─────────────────────────────────────────

    @Nested
    @DisplayName("enabled flag sync to Keycloak")
    class SincronizacionEnabled {

        @Test
        @DisplayName("syncs enabled=true when usuario is active")
        void edit_activo_sincronizaEnabledTrue() {
            // Given
            UUID id = UUID.randomUUID();
            UsuarioId usuarioId = UsuarioId.from(id);
            Usuario existing = Usuario.reconstitute(
                usuarioId, NOMBRE, CORREO_OLD, RolId.create(),
                AHORA.minusDays(1), true, KEYCLOAK_ID
            );
            when(findPort.findById(usuarioId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditUsuarioCommand cmd = new EditUsuarioCommand(
                id, NOMBRE, CORREO_OLD, ROL_ID, KEYCLOAK_ID
            );

            // When
            service.edit(cmd);

            // Then
            verify(identityPort).setEnabled(KEYCLOAK_ID, true);
        }

        @Test
        @DisplayName("syncs enabled=false when usuario is inactive")
        void edit_inactivo_sincronizaEnabledFalse() {
            // Given
            UUID id = UUID.randomUUID();
            UsuarioId usuarioId = UsuarioId.from(id);
            Usuario existing = Usuario.reconstitute(
                usuarioId, NOMBRE, CORREO_OLD, RolId.create(),
                AHORA.minusDays(1), false, KEYCLOAK_ID
            );
            when(findPort.findById(usuarioId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditUsuarioCommand cmd = new EditUsuarioCommand(
                id, NOMBRE, CORREO_OLD, ROL_ID, KEYCLOAK_ID
            );

            // When
            service.edit(cmd);

            // Then
            verify(identityPort).setEnabled(KEYCLOAK_ID, false);
        }

        @Test
        @DisplayName("skips Keycloak sync when keycloakId is null")
        void edit_sinKeycloakId_noSincroniza() {
            // Given
            UUID id = UUID.randomUUID();
            UsuarioId usuarioId = UsuarioId.from(id);
            Usuario existing = Usuario.reconstitute(
                usuarioId, NOMBRE, CORREO_OLD, RolId.create(),
                AHORA.minusDays(1), true, null
            );
            when(findPort.findById(usuarioId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditUsuarioCommand cmd = new EditUsuarioCommand(
                id, NOMBRE, CORREO_NEW, ROL_ID, null
            );

            // When
            service.edit(cmd);

            // Then
            verify(identityPort, never()).syncEmail(any(), any());
            verify(identityPort, never()).setEnabled(any(), anyBoolean());
        }
    }
}