package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.response.UsuarioResponse;
import com.ar.crm2.adapter.in.rest.mapper.UsuarioCommandMapper;
import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.application.usuario.command.DeleteUsuarioCommand;
import com.ar.crm2.application.usuario.command.EditUsuarioCommand;
import com.ar.crm2.application.usuario.port.in.CreateUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.DeleteUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.EditUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.GetAllUsuariosUseCase;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * REST slice tests for UsuarioController.
 *
 * Uses @ExtendWith(MockitoExtension.class) — the working pattern for this project.
 * Validates: JSON mapping, HTTP status codes, password exclusion, validation, and
 * GlobalExceptionHandler integration for IdentityProvisioningException.
 *
 * NOTE: @WebMvcTest is NOT used here because infrastructure module lacks a
 * @SpringBootConfiguration class (the Spring Boot entry point lives in the boot module).
 * The working alternative is direct controller unit tests with mocked use cases.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private static final String NOMBRE = "Juan Perez";
    private static final String CORREO = "juan@example.com";
    private static final String PASSWORD = "TempPass123!";
    private static final UUID ROL_ID = UUID.randomUUID();
    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final String KEYCLOAK_ID = "kc-uuid-123";

    @Mock
    private CreateUsuarioUseCase createUseCase;

    @Mock
    private GetAllUsuariosUseCase getAllUseCase;

    @Mock
    private EditUsuarioUseCase editUseCase;

    @Mock
    private DeleteUsuarioUseCase deleteUseCase;

    @InjectMocks
    private UsuarioController controller;

    // ── Helpers ─────────────────────────────────────────────────────

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

    // ════════════════════════════════════════════════════════════════
    // POST /api/usuarios/create
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/usuarios/create")
    class CreateEndpoint {

        @Test
        @DisplayName("success → 201 Created, response excludes initialPassword")
        void create_success_returns201ExcludesPassword() {
            // Given
            CreateUsuarioRequest request = new CreateUsuarioRequest(NOMBRE, CORREO, ROL_ID, PASSWORD, null);
            Usuario usuario = mockUsuario(USUARIO_ID, KEYCLOAK_ID);
            when(createUseCase.create(any(CreateUsuarioCommand.class))).thenReturn(usuario);

            // When
            ResponseEntity<UsuarioResponse> response = controller.create(request);

            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            UsuarioResponse body = response.getBody();
            assertNotNull(body);
            assertEquals(USUARIO_ID, body.id());
            assertEquals(NOMBRE, body.nombre());
            assertEquals(CORREO, body.correo());
            assertEquals(ROL_ID, body.rolId());
            assertTrue(body.activo());
            assertEquals(KEYCLOAK_ID, body.keycloakId());
            // UsuarioResponse is a record — verify no password field exists
            for (var field : body.getClass().getRecordComponents()) {
                String name = field.getName().toLowerCase();
                assertFalse(name.contains("password"),
                        "Response record must NOT have a password field: " + field.getName());
            }
        }

        @Test
        @DisplayName("success → initialPassword passed to use case command")
        void create_passesPasswordToCommand() {
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
        @DisplayName("Keycloak CONNECTION_FAILURE → GlobalExceptionHandler maps to 502 Bad Gateway")
        void create_keycloakConnectionFailure_throwsIdentityProvisioningException() {
            // Given
            CreateUsuarioRequest request = new CreateUsuarioRequest(NOMBRE, CORREO, ROL_ID, PASSWORD, null);
            when(createUseCase.create(any(CreateUsuarioCommand.class)))
                    .thenThrow(new IdentityProvisioningException(
                            "Keycloak unreachable",
                            IdentityProvisioningException.Reason.CONNECTION_FAILURE
                    ));

            // When / Then — exception propagates to GlobalExceptionHandler which returns 502
            IdentityProvisioningException thrown = assertThrows(
                    IdentityProvisioningException.class,
                    () -> controller.create(request)
            );
            assertEquals(IdentityProvisioningException.Reason.CONNECTION_FAILURE, thrown.getReason());
        }

        @Test
        @DisplayName("Keycloak USER_ALREADY_EXISTS → GlobalExceptionHandler maps to 409 Conflict")
        void create_keycloakUserAlreadyExists_throwsIdentityProvisioningException() {
            // Given
            CreateUsuarioRequest request = new CreateUsuarioRequest(NOMBRE, CORREO, ROL_ID, PASSWORD, null);
            when(createUseCase.create(any(CreateUsuarioCommand.class)))
                    .thenThrow(new IdentityProvisioningException(
                            "kc-123",
                            "User already exists",
                            IdentityProvisioningException.Reason.USER_ALREADY_EXISTS
                    ));

            // When / Then — exception propagates to GlobalExceptionHandler which returns 409
            IdentityProvisioningException thrown = assertThrows(
                    IdentityProvisioningException.class,
                    () -> controller.create(request)
            );
            assertEquals(IdentityProvisioningException.Reason.USER_ALREADY_EXISTS, thrown.getReason());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // PUT /api/usuarios/edit
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/usuarios/edit")
    class EditEndpoint {

        @Test
        @DisplayName("success → 200 OK with JSON response body")
        void edit_success_returns200WithJson() {
            // Given
            EditUsuarioRequest request = new EditUsuarioRequest("Juan Actualizado", CORREO, ROL_ID, null);
            Usuario usuario = mockUsuario(USUARIO_ID, KEYCLOAK_ID);
            when(editUseCase.edit(any(EditUsuarioCommand.class))).thenReturn(usuario);

            // When
            ResponseEntity<UsuarioResponse> response = controller.edit(USUARIO_ID, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            UsuarioResponse body = response.getBody();
            assertNotNull(body);
            assertEquals(USUARIO_ID, body.id());
            assertEquals(NOMBRE, body.nombre());
            assertEquals(KEYCLOAK_ID, body.keycloakId());
        }

        @Test
        @DisplayName("success → correct fields passed to EditUsuarioCommand via mapper")
        void edit_passesCorrectFieldsToCommand() {
            // Given
            String nuevoNombre = "Juan Actualizado";
            String nuevoCorreo = "nuevo@example.com";
            UUID nuevoRolId = UUID.randomUUID();
            EditUsuarioRequest request = new EditUsuarioRequest(nuevoNombre, nuevoCorreo, nuevoRolId, null);
            Usuario usuario = mockUsuario(USUARIO_ID, KEYCLOAK_ID);
            when(editUseCase.edit(any(EditUsuarioCommand.class))).thenReturn(usuario);

            // When
            controller.edit(USUARIO_ID, request);

            // Then
            ArgumentCaptor<EditUsuarioCommand> cmdCaptor = ArgumentCaptor.forClass(EditUsuarioCommand.class);
            verify(editUseCase).edit(cmdCaptor.capture());
            EditUsuarioCommand cmd = cmdCaptor.getValue();
            assertEquals(USUARIO_ID, cmd.id());
            assertEquals(nuevoNombre, cmd.nombre());
            assertEquals(nuevoCorreo, cmd.correo());
            assertEquals(nuevoRolId, cmd.rolId());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // DELETE /api/usuarios/delete
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/usuarios/delete")
    class DeleteEndpoint {

        @Test
        @DisplayName("success → 204 No Content")
        void delete_success_returns204() {
            // Given
            doNothing().when(deleteUseCase).delete(any(DeleteUsuarioCommand.class));

            // When
            ResponseEntity<Void> response = controller.delete(USUARIO_ID);

            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("success → correct UUID passed to DeleteUsuarioCommand via mapper")
        void delete_passesCorrectIdToCommand() {
            // Given
            doNothing().when(deleteUseCase).delete(any(DeleteUsuarioCommand.class));

            // When
            controller.delete(USUARIO_ID);

            // Then
            ArgumentCaptor<DeleteUsuarioCommand> cmdCaptor = ArgumentCaptor.forClass(DeleteUsuarioCommand.class);
            verify(deleteUseCase).delete(cmdCaptor.capture());
            assertEquals(USUARIO_ID, cmdCaptor.getValue().id());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // GET /api/usuarios/get-all (sanity check)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/usuarios/get-all")
    class GetAllEndpoint {

        @Test
        @DisplayName("returns 200 OK with list of UsuarioResponse")
        void getAll_returns200WithList() {
            // Given
            Usuario u1 = mockUsuario(UUID.randomUUID(), "kc-1");
            Usuario u2 = mockUsuario(UUID.randomUUID(), "kc-2");
            when(getAllUseCase.getAll()).thenReturn(List.of(u1, u2));

            // When
            ResponseEntity<List<UsuarioResponse>> response = controller.getAll();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
        }
    }
}