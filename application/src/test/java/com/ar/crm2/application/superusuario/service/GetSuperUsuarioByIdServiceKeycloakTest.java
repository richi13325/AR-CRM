package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.superusuario.command.GetSuperUsuarioByIdCommand;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.in.GetSuperUsuarioByIdUseCase;
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
 * Unit tests for {@link GetSuperUsuarioByIdService} retrieval path — verifying keycloakId
 * is correctly retrieved and exposed through the service layer (not just at mapper level).
 */
@ExtendWith(MockitoExtension.class)
class GetSuperUsuarioByIdServiceKeycloakTest {

    private static final String CORREO = "admin@example.com";
    private static final String PASSWORD_HASH = "super-secret-hash-456";
    private static final String KEYCLOAK_ID = "kc-uuid-super-retrieval-789";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Mock
    private FindSuperUsuarioByIdPort findPort;

    @InjectMocks
    private GetSuperUsuarioByIdService service;

    // ── retrieval with keycloakId ──────────────────────────────────

    @Nested
    @DisplayName("getById() retrieves keycloakId correctly")
    class RecuperaKeycloakId {

        @Test
        @DisplayName("returns SuperUsuario with keycloakId when entity has one linked")
        void getById_conKeycloakId_retornaConKeycloakId() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO, PASSWORD_HASH,
                    AHORA.minusDays(1), true, KEYCLOAK_ID
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            GetSuperUsuarioByIdCommand cmd = new GetSuperUsuarioByIdCommand(id);
            SuperUsuario result = service.getById(cmd);

            assertEquals(KEYCLOAK_ID, result.getKeycloakId());
            assertEquals(CORREO, result.getCorreo());
        }

        @Test
        @DisplayName("returns SuperUsuario with null keycloakId when entity has none")
        void getById_sinKeycloakId_retornaConKeycloakIdNulo() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO, PASSWORD_HASH,
                    AHORA.minusDays(1), true, null
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            GetSuperUsuarioByIdCommand cmd = new GetSuperUsuarioByIdCommand(id);
            SuperUsuario result = service.getById(cmd);

            assertNull(result.getKeycloakId());
        }

        @Test
        @DisplayName("throws SuperUsuarioNotFoundException when entity not found")
        void getById_entidadNoExistente_lanzaExcepcion() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            when(findPort.findById(suId)).thenReturn(Optional.empty());

            GetSuperUsuarioByIdCommand cmd = new GetSuperUsuarioByIdCommand(id);

            assertThrows(SuperUsuarioNotFoundException.class, () -> service.getById(cmd));
        }
    }
}
