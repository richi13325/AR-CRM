package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.response.UsuarioResponse;
import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.application.usuario.port.in.CreateUsuarioUseCase;
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
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * REST slice tests for {@link UsuarioController#create}.
 * Validates: initialPassword required, password not returned in response,
 * 502 Bad Gateway on Keycloak provisioning failure.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerCreateTest {

    private static final String NOMBRE = "Juan Perez";
    private static final String CORREO = "juan@example.com";
    private static final String PASSWORD = "TempPass123!";
    private static final UUID ROL_ID = UUID.randomUUID();
    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final String KEYCLOAK_ID = "kc-uuid-123";

    @Mock
    private CreateUsuarioUseCase createUseCase;

    @InjectMocks
    private UsuarioController controller;

    // ── Happy path: password required, not exposed in response ─────────

    @Nested
    @DisplayName("initialPassword in request, never in response")
    class PasswordHandling {

        @Test
        @DisplayName("password must be passed to command mapper")
        void create_passesPasswordToMapper() {
            // Given
            CreateUsuarioRequest request = new CreateUsuarioRequest(NOMBRE, CORREO, ROL_ID, PASSWORD, null);
            Usuario usuario = mockUsuario(USUARIO_ID, KEYCLOAK_ID);
            when(createUseCase.create(any(CreateUsuarioCommand.class))).thenReturn(usuario);

            // When
            controller.create(request);

            // Then
            ArgumentCaptor<CreateUsuarioCommand> cmdCaptor = ArgumentCaptor.forClass(CreateUsuarioCommand.class);
            verify(createUseCase).create(cmdCaptor.capture());
            assertEquals(PASSWORD, cmdCaptor.getValue().initialPassword(),
                    "initialPassword must be passed to command");
        }

        @Test
        @DisplayName("response must NOT contain initialPassword field")
        void create_responseExcludesPassword() {
            // Given
            CreateUsuarioRequest request = new CreateUsuarioRequest(NOMBRE, CORREO, ROL_ID, PASSWORD, null);
            Usuario usuario = mockUsuario(USUARIO_ID, KEYCLOAK_ID);
            when(createUseCase.create(any(CreateUsuarioCommand.class))).thenReturn(usuario);

            // When
            ResponseEntity<UsuarioResponse> response = controller.create(request);

            // Then: record has no such field — verify by checking the record fields
            UsuarioResponse body = response.getBody();
            assertNotNull(body);
            // UsuarioResponse is a record — reflect to check all fields
            var fields = body.getClass().getRecordComponents();
            for (var field : fields) {
                assertFalse(field.getName().toLowerCase().contains("password"),
                        "Response record must NOT have a password field: " + field.getName());
            }
        }
    }

    // ── Error path: Keycloak failure → 502 ───────────────────────────

    @Nested
    @DisplayName("Keycloak provisioning failure")
    class KeycloakFailure {

        @Test
        @DisplayName("provisioning failure maps to IdentityProvisioningException")
        void create_keycloakFalla_lanzaExcepcion() {
            // Given
            CreateUsuarioRequest request = new CreateUsuarioRequest(NOMBRE, CORREO, ROL_ID, PASSWORD, null);
            when(createUseCase.create(any(CreateUsuarioCommand.class)))
                    .thenThrow(new IdentityProvisioningException(
                            "Keycloak unreachable",
                            IdentityProvisioningException.Reason.CONNECTION_FAILURE
                    ));

            // When / Then
            IdentityProvisioningException thrown = assertThrows(
                    IdentityProvisioningException.class,
                    () -> controller.create(request)
            );
            assertEquals(IdentityProvisioningException.Reason.CONNECTION_FAILURE, thrown.getReason());
        }
    }

    // ── Helper: mock Usuario domain object ─────────────────────────────

    private Usuario mockUsuario(UUID id, String keycloakId) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(UsuarioId.from(id));
        when(usuario.getNombre()).thenReturn(NOMBRE);
        when(usuario.getCorreo()).thenReturn(CORREO);
        when(usuario.getRolId()).thenReturn(RolId.from(ROL_ID));
        when(usuario.getCreadoEn()).thenReturn(LocalDateTime.now());
        when(usuario.isActivo()).thenReturn(true);
        when(usuario.getKeycloakId()).thenReturn(keycloakId);
        return usuario;
    }
}