package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ExpirarAccionCommand;
import com.ar.crm2.application.ai.port.out.UpdateEstadoAccionPort;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpirarAccionServiceTest {

    private InMemoryUpdateEstadoAccionPort port;
    private ExpirarAccionService service;

    @BeforeEach
    void setUp() {
        port = new InMemoryUpdateEstadoAccionPort();
        service = new ExpirarAccionService(port);
    }

    @Test
    @DisplayName("expirar marca PENDING pendientes como EXPIRED y devuelve el conteo")
    void expirar_marcaPendingExpired() {
        AiAccion p1 = pending();
        AiAccion p2 = pending();
        port.pendientes.add(p1);
        port.pendientes.add(p2);

        int count = service.expirar(new ExpirarAccionCommand(LocalDateTime.now().plusHours(2), 100));

        assertEquals(2, count);
        // expirar() returns a new instance with EXPIRED state — the port saves it.
        assertEquals(EstadoAccion.EXPIRED, port.savedExpiradas.get(0).getEstado());
        assertEquals(EstadoAccion.EXPIRED, port.savedExpiradas.get(1).getEstado());
    }

    @Test
    @DisplayName("expirar con lista vacía devuelve 0")
    void expirar_listaVacia_devuelveCero() {
        int count = service.expirar(new ExpirarAccionCommand(LocalDateTime.now(), 100));
        assertEquals(0, count);
    }

    private AiAccion pending() {
        return AiAccion.crear(
                EmpresaId.create(), UsuarioId.create(), "wa", null,
                com.ar.crm2.model.vo.AiConversacionId.create(),
                "CREATE_TAREA",
                "{\"titulo\":\"x\"}", "r", 60, LocalDateTime.now()
        );
    }

    private static final class InMemoryUpdateEstadoAccionPort implements UpdateEstadoAccionPort {
        final List<AiAccion> pendientes = new ArrayList<>();
        final List<AiAccion> savedExpiradas = new ArrayList<>();
        @Override public List<AiAccion> findPendingExpired(int limite) { return new ArrayList<>(pendientes); }
        @Override public AiAccion save(AiAccion p) { savedExpiradas.add(p); return p; }
    }
}