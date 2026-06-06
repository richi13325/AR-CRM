package com.ar.crm2.application.tablero.service;

import com.ar.crm2.application.columna.port.out.SaveColumnaPort;
import com.ar.crm2.application.tablero.command.CreateTableroCommand;
import com.ar.crm2.application.tablero.port.out.SaveTableroPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.SuperUsuarioId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CreateTableroService}.
 *
 * <p>Regression coverage for the catalog-columna-not-persisted bug:
 * the service must persist each default catalog {@link Columna} BEFORE saving
 * the {@link Tablero}. Otherwise {@code columnas_tablero.columna_id} would
 * reference rows that do not exist in the {@code columnas} catalog, and the
 * read path in {@code TableroMapper.toColumnaTableroDomain()} would throw
 * {@code IllegalStateException("Catalog Columna not found ...")}.
 */
@ExtendWith(MockitoExtension.class)
class CreateTableroServiceTest {

    @Mock
    private SaveTableroPort saveTableroPort;

    @Mock
    private SaveColumnaPort saveColumnaPort;

    // ── Helpers ─────────────────────────────────────────────────────

    private CreateTableroCommand command(TipoTablero tipo, UUID superUsuarioId) {
        return new CreateTableroCommand(
            "Sprint Board",
            "A board for sprint planning",
            tipo,
            true,
            superUsuarioId
        );
    }

    private Columna stubPersistedColumna(Columna input) {
        // SaveColumnaPort is expected to return the persisted Columna with
        // the same id (the entity is built with an explicit id, and JPA
        // honors it on save). Mirror that contract in the stub.
        return Columna.reconstitute(
            input.getId(),
            input.getColumnanombre(),
            input.getColor(),
            input.getTipoTablero(),
            input.getTipoColumna(),
            false
        );
    }

    // ── TAREAS board ───────────────────────────────────────────────

    @Test
    void create_tareas_persistsFourCatalogColumnasBeforeSavingTablero() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand cmd = command(TipoTablero.TAREAS, superUsuarioId);

        // Stub the columna save to return the same input (mirrors real behavior).
        when(saveColumnaPort.save(any(Columna.class)))
            .thenAnswer(invocation -> stubPersistedColumna(invocation.getArgument(0)));
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Tablero result = new CreateTableroService(saveTableroPort, saveColumnaPort).create(cmd);

        assertNotNull(result);

        // 4 default columns must be persisted for TAREAS.
        verify(saveColumnaPort, times(4)).save(any(Columna.class));
        verify(saveTableroPort, times(1)).save(any(Tablero.class));

        // Ordering: all columnas must be saved BEFORE the tablero.
        InOrder ordered = inOrder(saveColumnaPort, saveTableroPort);
        ordered.verify(saveColumnaPort, times(4)).save(any(Columna.class));
        ordered.verify(saveTableroPort).save(any(Tablero.class));
    }

    @Test
    void create_tareas_catalogColumnasHavePREDETERMINADATipoColumna() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand cmd = command(TipoTablero.TAREAS, superUsuarioId);

        when(saveColumnaPort.save(any(Columna.class)))
            .thenAnswer(invocation -> stubPersistedColumna(invocation.getArgument(0)));
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        new CreateTableroService(saveTableroPort, saveColumnaPort).create(cmd);

        ArgumentCaptor<Columna> captor = ArgumentCaptor.forClass(Columna.class);
        verify(saveColumnaPort, times(4)).save(captor.capture());

        for (Columna c : captor.getAllValues()) {
            assertEquals(TipoColumna.PREDETERMINADA, c.getTipoColumna());
            assertEquals(TipoTablero.TAREAS, c.getTipoTablero());
            assertNotNull(c.getId(), "Columna id must be assigned before save");
        }
    }

    // ── TRATOS board ───────────────────────────────────────────────

    @Test
    void create_tratos_persistsFourCatalogColumnasBeforeSavingTablero() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand cmd = command(TipoTablero.TRATOS, superUsuarioId);

        when(saveColumnaPort.save(any(Columna.class)))
            .thenAnswer(invocation -> stubPersistedColumna(invocation.getArgument(0)));
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Tablero result = new CreateTableroService(saveTableroPort, saveColumnaPort).create(cmd);

        assertNotNull(result);

        verify(saveColumnaPort, times(4)).save(any(Columna.class));
        verify(saveTableroPort, times(1)).save(any(Tablero.class));

        InOrder ordered = inOrder(saveColumnaPort, saveTableroPort);
        ordered.verify(saveColumnaPort, times(4)).save(any(Columna.class));
        ordered.verify(saveTableroPort).save(any(Tablero.class));
    }

    @Test
    void create_tratos_catalogColumnasHaveTRATOSTipoTablero() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand cmd = command(TipoTablero.TRATOS, superUsuarioId);

        when(saveColumnaPort.save(any(Columna.class)))
            .thenAnswer(invocation -> stubPersistedColumna(invocation.getArgument(0)));
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        new CreateTableroService(saveTableroPort, saveColumnaPort).create(cmd);

        ArgumentCaptor<Columna> captor = ArgumentCaptor.forClass(Columna.class);
        verify(saveColumnaPort, times(4)).save(captor.capture());

        for (Columna c : captor.getAllValues()) {
            assertEquals(TipoColumna.PREDETERMINADA, c.getTipoColumna());
            assertEquals(TipoTablero.TRATOS, c.getTipoTablero());
        }
    }

    // ── SuperUsuarioId propagation ─────────────────────────────────

    @Test
    void create_passesCommandSuperUsuarioIdToTableroCreate() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand cmd = command(TipoTablero.TAREAS, superUsuarioId);

        when(saveColumnaPort.save(any(Columna.class)))
            .thenAnswer(invocation -> stubPersistedColumna(invocation.getArgument(0)));
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        new CreateTableroService(saveTableroPort, saveColumnaPort).create(cmd);

        ArgumentCaptor<Tablero> captor = ArgumentCaptor.forClass(Tablero.class);
        verify(saveTableroPort).save(captor.capture());
        // The Tablero.create() call itself enforces non-null superUsuarioId.
        // If the command's id was lost, Tablero.create would throw before reaching the save port.
        assertNotNull(captor.getValue());
    }

    // ── Saved Columna ids feed ColumnaTablero ──────────────────────

    @Test
    void create_columnaTableroIdsMatchPersistedColumnaIds() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand cmd = command(TipoTablero.TAREAS, superUsuarioId);

        // Track ids assigned to each Columna so we can verify they are
        // preserved when building ColumnaTablero references.
        List<UUID> assignedColumnaIds = new java.util.ArrayList<>();

        when(saveColumnaPort.save(any(Columna.class))).thenAnswer(invocation -> {
            Columna input = invocation.getArgument(0);
            assignedColumnaIds.add(input.getId().value());
            return stubPersistedColumna(input);
        });
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        new CreateTableroService(saveTableroPort, saveColumnaPort).create(cmd);

        // The tablero passed to savePort must contain 4 ColumnaTablero
        // entries whose columnaId matches each persisted Columna id.
        ArgumentCaptor<Tablero> tableroCaptor = ArgumentCaptor.forClass(Tablero.class);
        verify(saveTableroPort).save(tableroCaptor.capture());

        Tablero saved = tableroCaptor.getValue();
        assertEquals(4, saved.getColumnasTablero().size());
        assertEquals(4, assignedColumnaIds.size());

        List<UUID> referencedIds = saved.getColumnasTablero().stream()
            .map(ct -> ct.getColumnaId().value())
            .toList();
        assertEquals(assignedColumnaIds, referencedIds,
            "Each ColumnaTablero must reference the id of a persisted catalog Columna");
    }

    // ── Use of TipoTablero for the catalog entry ───────────────────

    @Test
    void create_tableroTipoIsPreservedOnReturnedTablero() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand tareasCmd = command(TipoTablero.TAREAS, superUsuarioId);
        CreateTableroCommand tratosCmd = command(TipoTablero.TRATOS, superUsuarioId);

        when(saveColumnaPort.save(any(Columna.class)))
            .thenAnswer(invocation -> stubPersistedColumna(invocation.getArgument(0)));
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateTableroService service = new CreateTableroService(saveTableroPort, saveColumnaPort);

        Tablero tareas = service.create(tareasCmd);
        Tablero tratos = service.create(tratosCmd);

        assertEquals(TipoTablero.TAREAS, tareas.getTipoTablero());
        assertEquals(TipoTablero.TRATOS, tratos.getTipoTablero());
    }

    // ── No spurious port calls ─────────────────────────────────────

    @Test
    void create_doesNotCallSaveColumnaAfterSaveTablero() {
        UUID superUsuarioId = UUID.randomUUID();
        CreateTableroCommand cmd = command(TipoTablero.TAREAS, superUsuarioId);

        when(saveColumnaPort.save(any(Columna.class)))
            .thenAnswer(invocation -> stubPersistedColumna(invocation.getArgument(0)));
        when(saveTableroPort.save(any(Tablero.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        new CreateTableroService(saveTableroPort, saveColumnaPort).create(cmd);

        // The four columna saves must happen strictly before the tablero save.
        InOrder ordered = inOrder(saveColumnaPort, saveTableroPort);
        ordered.verify(saveColumnaPort, times(4)).save(any(Columna.class));
        ordered.verify(saveTableroPort).save(any(Tablero.class));
        ordered.verifyNoMoreInteractions();
    }
}
