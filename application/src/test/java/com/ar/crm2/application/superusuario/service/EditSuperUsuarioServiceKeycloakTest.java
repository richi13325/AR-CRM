package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.superusuario.command.EditSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.in.EditSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.SaveSuperUsuarioPort;
import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;
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
 * Unit tests for {@link EditSuperUsuarioService} keycloakId preserve-vs-update semantics.
 * Verifies: preserve when command.keycloakId() is null, update when provided.
 */
@ExtendWith(MockitoExtension.class)
class EditSuperUsuarioServiceKeycloakTest {

    private static final String CORREO = "admin@example.com";
    private static final String KEYCLOAK_ID_EXISTING = "existing-super-kc-uuid";
    private static final String KEYCLOAK_ID_NEW = "new-super-kc-uuid-789";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Mock
    private FindSuperUsuarioByIdPort findPort;

    @Mock
    private SaveSuperUsuarioPort savePort;

    @Mock
    private IdentityProviderUserPort identityPort;

    @InjectMocks
    private EditSuperUsuarioService service;

    // ── keycloakId PRESERVE semantics (command.keycloakId == null) ─────

    @Nested
    @DisplayName("keycloakId preserve — command.keycloakId() is null")
    class PreservaKeycloakId {

        @Test
        @DisplayName("preserves existing keycloakId when command.keycloakId() is null")
        void edit_conKeycloakIdNulo_preservaExistente() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    AHORA.minusDays(1), true, KEYCLOAK_ID_EXISTING
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO + ".ar", null);

            service.edit(cmd);

            ArgumentCaptor<SuperUsuario> captor = ArgumentCaptor.forClass(SuperUsuario.class);
            verify(savePort).save(captor.capture());
            assertEquals(KEYCLOAK_ID_EXISTING, captor.getValue().getKeycloakId());
        }

        @Test
        @DisplayName("preserves null keycloakId when existing has none and command omits it")
        void edit_sinKeycloakIdExistente_preservaNull() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    AHORA.minusDays(1), true, null
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO + ".ar", null);

            service.edit(cmd);

            ArgumentCaptor<SuperUsuario> captor = ArgumentCaptor.forClass(SuperUsuario.class);
            verify(savePort).save(captor.capture());
            assertNull(captor.getValue().getKeycloakId());
        }
    }

    // ── keycloakId UPDATE semantics (command.keycloakId != null) ─────

    @Nested
    @DisplayName("keycloakId update — command.keycloakId() is non-null")
    class ActualizaKeycloakId {

        @Test
        @DisplayName("updates to new keycloakId when command provides one")
        void edit_conKeycloakIdNuevo_actualiza() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    AHORA.minusDays(1), true, KEYCLOAK_ID_EXISTING
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO + ".ar", KEYCLOAK_ID_NEW);

            service.edit(cmd);

            ArgumentCaptor<SuperUsuario> captor = ArgumentCaptor.forClass(SuperUsuario.class);
            verify(savePort).save(captor.capture());
            assertEquals(KEYCLOAK_ID_NEW, captor.getValue().getKeycloakId());
        }
    }

    // ── aggregate identity preservation ───────────────────────────────

    @Nested
    @DisplayName("aggregate identity fields are preserved from existing SuperUsuario")
    class PreservaIdentidad {

        @Test
        @DisplayName("preserves existing id, creadoEn, activo")
        void edit_preservaCamposDeIdentidad() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            LocalDateTime originalCreadoEn = AHORA.minusDays(5);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    originalCreadoEn, true, KEYCLOAK_ID_EXISTING
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO + ".ar", KEYCLOAK_ID_NEW);

            service.edit(cmd);

            ArgumentCaptor<SuperUsuario> captor = ArgumentCaptor.forClass(SuperUsuario.class);
            verify(savePort).save(captor.capture());
            SuperUsuario saved = captor.getValue();
            assertEquals(suId, saved.getId());
            assertEquals(originalCreadoEn, saved.getCreadoEn());
            assertTrue(saved.isActivo());
        }

        @Test
        @DisplayName("throws SuperUsuarioNotFoundException when entity does not exist")
        void edit_conEntidadInexistente_lanzaExcepcion() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            when(findPort.findById(suId)).thenReturn(Optional.empty());

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO + ".ar", KEYCLOAK_ID_NEW);

            assertThrows(SuperUsuarioNotFoundException.class, () -> service.edit(cmd));
            verify(savePort, never()).save(any());
        }
    }

    // ── Keycloak sync failure — email ──────────────────────────────

    @Nested
    @DisplayName("Keycloak email sync fails — local update not attempted")
    class EmailSyncFalla {

        @Test
        @DisplayName("throws IdentityProvisioningException when syncEmail fails — local not saved")
        void edit_emailSyncFalla_noActualizaLocal() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    AHORA.minusDays(1), true, KEYCLOAK_ID_EXISTING
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));
            doThrow(new IdentityProvisioningException(
                "Keycloak error",
                IdentityProvisioningException.Reason.SERVER_ERROR
            )).when(identityPort).syncEmail(KEYCLOAK_ID_EXISTING, CORREO + ".ar");

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO + ".ar", KEYCLOAK_ID_EXISTING);

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
        @DisplayName("syncs enabled=true when superusuario is active")
        void edit_activo_sincronizaEnabledTrue() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    AHORA.minusDays(1), true, KEYCLOAK_ID_EXISTING
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(SuperUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO, KEYCLOAK_ID_EXISTING);

            service.edit(cmd);

            verify(identityPort).setEnabled(KEYCLOAK_ID_EXISTING, true);
        }

        @Test
        @DisplayName("syncs enabled=false when superusuario is inactive")
        void edit_inactivo_sincronizaEnabledFalse() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    AHORA.minusDays(1), false, KEYCLOAK_ID_EXISTING
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(SuperUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO, KEYCLOAK_ID_EXISTING);

            service.edit(cmd);

            verify(identityPort).setEnabled(KEYCLOAK_ID_EXISTING, false);
        }

        @Test
        @DisplayName("skips Keycloak sync when keycloakId is null")
        void edit_sinKeycloakId_noSincroniza() {
            UUID id = UUID.randomUUID();
            SuperUsuarioId suId = SuperUsuarioId.from(id);
            SuperUsuario existing = SuperUsuario.reconstitute(
                    suId, CORREO,
                    AHORA.minusDays(1), true, null
            );
            when(findPort.findById(suId)).thenReturn(Optional.of(existing));
            when(savePort.save(any(SuperUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

            EditSuperUsuarioCommand cmd = new EditSuperUsuarioCommand(id, CORREO + ".ar", null);

            service.edit(cmd);

            verify(identityPort, never()).syncEmail(any(), any());
            verify(identityPort, never()).setEnabled(any(), anyBoolean());
        }
    }
}