package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.identity.model.ProvisionedIdentity;
import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.superusuario.command.CreateSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.port.out.SaveSuperUsuarioPort;
import com.ar.crm2.model.entity.SuperUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CreateSuperUsuarioService} Keycloak provisioning flow.
 * Parity with {@link CreateUsuarioServiceKeycloakTest}: successful provision with keycloakId,
 * save-failure compensation (delete Keycloak user), Keycloak unreachable failure.
 */
@ExtendWith(MockitoExtension.class)
class CreateSuperUsuarioServiceKeycloakTest {

    private static final String CORREO = "admin@crm2.com";
    private static final String PASSWORD = "AdminTemp123!";
    private static final String KEYCLOAK_ID = "kc-su-uuid-new-999";

    @Mock
    private SaveSuperUsuarioPort savePort;

    @Mock
    private IdentityProviderUserPort identityPort;

    @InjectMocks
    private CreateSuperUsuarioService service;

    // ── Happy path ─────────────────────────────────────────────────

    @Nested
    @DisplayName("successful Keycloak provisioning")
    class ProvisionamientoExitoso {

        @Test
        @DisplayName("creates superusuario in Keycloak first, saves CRM with keycloakId")
        void create_exitoso_guardaConKeycloakId() {
            // Given
            when(identityPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(SuperUsuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

            CreateSuperUsuarioCommand cmd = new CreateSuperUsuarioCommand(CORREO, null, PASSWORD);

            // When
            SuperUsuario result = service.create(cmd);

            // Then
            assertEquals(KEYCLOAK_ID, result.getKeycloakId());
            assertEquals(CORREO, result.getCorreo());
            verify(identityPort).provision(CORREO, PASSWORD, true);
            verify(savePort).save(any(SuperUsuario.class));
        }

        @Test
        @DisplayName("sets enabled=true on Keycloak provision (VERIFY_EMAIL triggers email)")
        void create_exitoso_enviaVERIFY_EMAIL() {
            // Given
            when(identityPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(SuperUsuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

            CreateSuperUsuarioCommand cmd = new CreateSuperUsuarioCommand(CORREO, null, PASSWORD);

            // When
            service.create(cmd);

            // Then — provision called with enabled=true
            verify(identityPort).provision(CORREO, PASSWORD, true);
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
            when(identityPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(SuperUsuario.class)))
                .thenThrow(new RuntimeException("DB constraint violation"));

            CreateSuperUsuarioCommand cmd = new CreateSuperUsuarioCommand(CORREO, null, PASSWORD);

            // When / Then
            assertThrows(RuntimeException.class, () -> service.create(cmd));
            verify(identityPort).delete(KEYCLOAK_ID);
        }

        @Test
        @DisplayName("rethrows original save exception even if compensation delete fails")
        void create_compensacionFalla_rethrowsOriginal() {
            // Given
            when(identityPort.provision(CORREO, PASSWORD, true))
                .thenReturn(new ProvisionedIdentity(KEYCLOAK_ID, CORREO));
            when(savePort.save(any(SuperUsuario.class)))
                .thenThrow(new RuntimeException("DB error"));
            doThrow(new RuntimeException("Keycloak unreachable"))
                .when(identityPort).delete(KEYCLOAK_ID);

            CreateSuperUsuarioCommand cmd = new CreateSuperUsuarioCommand(CORREO, null, PASSWORD);

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
            when(identityPort.provision(CORREO, PASSWORD, true))
                .thenThrow(new IdentityProvisioningException(
                    "Connection refused",
                    IdentityProvisioningException.Reason.CONNECTION_FAILURE
                ));

            CreateSuperUsuarioCommand cmd = new CreateSuperUsuarioCommand(CORREO, null, PASSWORD);

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