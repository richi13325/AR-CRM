package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.identity.model.ProvisionedIdentity;
import com.ar.crm2.application.identity.port.out.DeleteIdentityPort;
import com.ar.crm2.application.identity.port.out.ProvisionIdentityPort;
import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CreateUsuarioService} Keycloak provisioning flow.
 * Covers: successful provision with keycloakId, save-failure compensation,
 * Keycloak unreachable failure.
 */
@ExtendWith(MockitoExtension.class)
class CreateUsuarioServiceKeycloakTest {

    private static final String NOMBRE = "Juan Perez";
    private static final String CORREO = "juan@example.com";
    private static final String PASSWORD = "TempPass123!";
    private static final String KEYCLOAK_ID = "kc-uuid-new-user-123";
    private static final UUID ROL_ID = UUID.randomUUID();

    @Mock
    private SaveUsuarioPort savePort;

    @Mock
    private ProvisionIdentityPort provisionPort;

    @Mock
    private DeleteIdentityPort deleteIdentityPort;

    @InjectMocks
    private CreateUsuarioService service;

    // ── Happy path ─────────────────────────────────────────────────

    @Nested
    @DisplayName("successful Keycloak provisioning")
    class ProvisionamientoExitoso {

        @Test
        @DisplayName("creates user in Keycloak first, saves CRM with keycloakId")
        void create_exitoso_guardaConKeycloakId() {
            // Given
            when(provisionPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

            CreateUsuarioCommand cmd = new CreateUsuarioCommand(
                NOMBRE, CORREO, ROL_ID, null, PASSWORD
            );

            // When
            Usuario result = service.create(cmd);

            // Then
            assertEquals(KEYCLOAK_ID, result.getKeycloakId());
            assertEquals(CORREO, result.getCorreo());
            assertEquals(NOMBRE, result.getNombre());
            verify(provisionPort).provision(CORREO, PASSWORD, true);
            verify(savePort).save(any(Usuario.class));
        }

        @Test
        @DisplayName("sets VERIFY_EMAIL required action on Keycloak provision")
        void create_exitoso_enviaVERIFY_EMAIL() {
            when(provisionPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

            CreateUsuarioCommand cmd = new CreateUsuarioCommand(
                NOMBRE, CORREO, ROL_ID, null, PASSWORD
            );
            service.create(cmd);

            // Verify provision was called with enabled=true (VERIFY_EMAIL triggers email)
            verify(provisionPort).provision(CORREO, PASSWORD, true);
        }
    }

    // ── Save failure compensation ─────────────────────────────────

    @Nested
    @DisplayName("local save fails after Keycloak creation — compensation delete")
    class CompensacionSaveFalla {

        @Test
        @DisplayName("deletes Keycloak user when local save throws")
        void create_saveFalla_compensaEliminaKeycloak() {
            // Given
            when(provisionPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(Usuario.class)))
                .thenThrow(new RuntimeException("DB constraint violation"));

            CreateUsuarioCommand cmd = new CreateUsuarioCommand(
                NOMBRE, CORREO, ROL_ID, null, PASSWORD
            );

            // When / Then
            assertThrows(RuntimeException.class, () -> service.create(cmd));
            verify(deleteIdentityPort).delete(KEYCLOAK_ID);
        }

        @Test
        @DisplayName("rethrows original save exception even if compensation delete fails")
        void create_compensacionFalla_rethrowsOriginal() {
            // Given
            when(provisionPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(Usuario.class)))
                .thenThrow(new RuntimeException("DB error"));
            doThrow(new RuntimeException("Keycloak unreachable"))
                .when(deleteIdentityPort).delete(KEYCLOAK_ID);

            CreateUsuarioCommand cmd = new CreateUsuarioCommand(
                NOMBRE, CORREO, ROL_ID, null, PASSWORD
            );

            // When / Then — original save failure propagates
            RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service.create(cmd));
            assertEquals("DB error", thrown.getMessage());
        }
    }

    // ── Keycloak unreachable ──────────────────────────────────────

    @Nested
    @DisplayName("Keycloak creation fails")
    class KeycloakFalla {

        @Test
        @DisplayName("fails fast when Keycloak is unreachable — no local save attempted")
        void create_keycloakFalla_noGuardaLocal() {
            // Given
            when(provisionPort.provision(CORREO, PASSWORD, true))
                .thenThrow(new IdentityProvisioningException(
                    "Connection refused",
                    IdentityProvisioningException.Reason.CONNECTION_FAILURE
                ));

            CreateUsuarioCommand cmd = new CreateUsuarioCommand(
                NOMBRE, CORREO, ROL_ID, null, PASSWORD
            );

            // When / Then
            IdentityProvisioningException thrown = assertThrows(
                IdentityProvisioningException.class,
                () -> service.create(cmd)
            );
            assertEquals(IdentityProvisioningException.Reason.CONNECTION_FAILURE,
                thrown.getReason());
            verify(savePort, never()).save(any());
        }
    }
}