package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ListarConversacionesAsistenteCommand;
import com.ar.crm2.application.ai.port.out.ListAiConversacionesPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
// ActorEmpresaScopeService implements ActorEmpresaScopePort directly, so the
// test injects the Empresa-owned service as the AI dependency.
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListarConversacionesAsistenteServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final EmpresaId empresa = EmpresaId.create();

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemoryListAiConversacionesPort listPort;
    private ListarConversacionesAsistenteService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresa));
        listPort = new InMemoryListAiConversacionesPort();
service = new ListarConversacionesAsistenteService(
                new ActorEmpresaScopeService(findEmpresasPort), listPort
        );
    }

    @Test
    @DisplayName("listar devuelve conversaciones del actor y pasa el limite al port")
    void listar_delegateAlPort() {
        listPort.rows.add(AiConversacion.crear(empresa, actor, "wa-1", null, LocalDateTime.now()));
        List<AiConversacion> result = service.listar(
                new ListarConversacionesAsistenteCommand(actor.value(), empresa.value(), 50)
        );
        assertEquals(1, result.size());
        assertEquals(50, listPort.lastLimite);
        assertEquals(actor.value(), listPort.lastActor);
        assertEquals(empresa.value(), listPort.lastEmpresa);
    }

    private static final class InMemoryFindEmpresasByCreadorPort implements FindEmpresasByCreadorPort {
        private final List<Row> rows = new ArrayList<>();
        void ownedBy(UsuarioId u, List<EmpresaId> e) { rows.add(new Row(u, e)); }
        @Override public List<EmpresaId> findEmpresasByCreador(UsuarioId u) {
            return rows.stream().filter(r -> r.user.equals(u)).findFirst()
                    .map(r -> r.empresas).orElse(List.of());
        }
        private record Row(UsuarioId user, List<EmpresaId> empresas) {}
    }

    private static final class InMemoryListAiConversacionesPort implements ListAiConversacionesPort {
        final List<AiConversacion> rows = new ArrayList<>();
        UUID lastActor;
        UUID lastEmpresa;
        int lastLimite;
        @Override public List<AiConversacion> listByActor(UUID actor, UUID empresa, int limite) {
            lastActor = actor;
            lastEmpresa = empresa;
            lastLimite = limite;
            return new ArrayList<>(rows);
        }
    }
}
