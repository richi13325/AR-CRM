package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.AgregarColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.AsignarColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.CreateTableroRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditTableroRequest;
import com.ar.crm2.adapter.in.rest.dto.request.ReordenarColumnasRequest;
import com.ar.crm2.adapter.in.rest.dto.response.TableroResponse;
import com.ar.crm2.adapter.in.rest.mapper.TableroCommandMapper;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.application.tablero.command.AgregarColumnaTableroCommand;
import com.ar.crm2.application.tablero.command.CreateTableroCommand;
import com.ar.crm2.application.tablero.command.DeleteTableroCommand;
import com.ar.crm2.application.tablero.command.EditTableroCommand;
import com.ar.crm2.application.tablero.command.EliminarColumnaDelTableroCommand;
import com.ar.crm2.application.tablero.command.GetTableroByIdCommand;
import com.ar.crm2.application.tablero.command.ReordenarColumnasCommand;
import com.ar.crm2.application.tablero.port.in.AgregarColumnaTableroUseCase;
import com.ar.crm2.application.tablero.port.in.AsignarColumnaTableroUseCase;
import com.ar.crm2.application.tablero.port.in.CreateTableroUseCase;
import com.ar.crm2.application.tablero.port.in.DeleteTableroUseCase;
import com.ar.crm2.application.tablero.port.in.EditTableroUseCase;
import com.ar.crm2.application.tablero.port.in.EliminarColumnaDelTableroUseCase;
import com.ar.crm2.application.tablero.port.in.GetAllTablerosUseCase;
import com.ar.crm2.application.tablero.port.in.GetTableroByIdUseCase;
import com.ar.crm2.application.tablero.port.in.ReordenarColumnasUseCase;
import com.ar.crm2.model.entity.Tablero;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Tablero operations (CRUD + column management).
 * Implements inbound adapter pattern, delegates to application UseCase contracts.
 */
@RestController
@RequestMapping("/api/tableros")
@RequiredArgsConstructor
public class TableroController {

    private final CreateTableroUseCase createUseCase;
    private final GetAllTablerosUseCase getAllUseCase;
    private final GetTableroByIdUseCase getByIdUseCase;
    private final EditTableroUseCase editUseCase;
    private final DeleteTableroUseCase deleteUseCase;
    private final AgregarColumnaTableroUseCase agregarColumnaUseCase;
    private final AsignarColumnaTableroUseCase asignarColumnaUseCase;
    private final EliminarColumnaDelTableroUseCase eliminarColumnaUseCase;
    private final ReordenarColumnasUseCase reordenarColumnasUseCase;
    private final ColumnaRepository columnaRepository;

    // ── Tablero CRUD ────────────────────────────────────────────────

    /**
     * Creates a new Tablero with 4 default columns.
     */
    @PostMapping("/create")
    public ResponseEntity<TableroResponse> create(@Valid @RequestBody CreateTableroRequest request) {
        CreateTableroCommand command = TableroCommandMapper.toCommand(request);
        Tablero tablero = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(TableroResponse.fromDomain(tablero, columnaRepository));
    }

    /**
     * Retrieves all Tableros.
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<TableroResponse>> getAll() {
        List<Tablero> tableros = getAllUseCase.getAll();
        List<TableroResponse> responses = tableros.stream()
            .map(t -> TableroResponse.fromDomain(t, columnaRepository))
            .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a single Tablero by id.
     */
    @GetMapping("/get-by-id")
    public ResponseEntity<TableroResponse> getById(@RequestParam UUID id) {
        GetTableroByIdCommand command = TableroCommandMapper.toGetByIdCommand(id);
        Tablero tablero = getByIdUseCase.getById(command);
        return ResponseEntity.ok(TableroResponse.fromDomain(tablero, columnaRepository));
    }

    /**
     * Updates an existing Tablero's name and description.
     */
    @PutMapping("/edit")
    public ResponseEntity<TableroResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditTableroRequest request) {
        EditTableroCommand command = TableroCommandMapper.toCommand(id, request);
        Tablero tablero = editUseCase.edit(command);
        return ResponseEntity.ok(TableroResponse.fromDomain(tablero, columnaRepository));
    }

    /**
     * Deletes an existing Tablero.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        DeleteTableroCommand command = TableroCommandMapper.toDeleteCommand(id);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }

    // ── Column Operations ───────────────────────────────────────────

    /**
     * Adds a new column to an existing Tablero.
     */
    @PostMapping("/agregar-columna")
    public ResponseEntity<TableroResponse> agregarColumna(
            @RequestParam UUID id,
            @Valid @RequestBody AgregarColumnaRequest request
    ) {
        AgregarColumnaTableroCommand command = TableroCommandMapper.toCommand(id, request);
        Tablero tablero = agregarColumnaUseCase.agregarColumna(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(TableroResponse.fromDomain(tablero, columnaRepository));
    }

    /**
     * Removes a column from an existing Tablero.
     * Fails with 409 Conflict if the column contains fichas.
     */
    @DeleteMapping("/eliminar-columna")
    public ResponseEntity<Void> eliminarColumna(
            @RequestParam UUID id,
            @RequestParam UUID columnaId
    ) {
        EliminarColumnaDelTableroCommand command = TableroCommandMapper.toDeleteColumnCommand(id, columnaId);
        eliminarColumnaUseCase.eliminarColumna(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assigns an existing catalog Columna to an existing Tablero.
     * Uses the catalog Columna as the source of truth for column definition.
     */
    @PostMapping("/asignar-columna")
    public ResponseEntity<TableroResponse> asignarColumna(
            @RequestParam UUID id,
            @RequestParam UUID columnaId,
            @Valid @RequestBody AsignarColumnaRequest request
    ) {
        Tablero tablero = asignarColumnaUseCase.asignarColumna(
                TableroCommandMapper.toAsignarCommand(id, columnaId, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TableroResponse.fromDomain(tablero, columnaRepository));
    }

    /**
     * Reorders the columns of an existing Tablero.
     */
    @PutMapping("/reordenar-columnas")
    public ResponseEntity<TableroResponse> reordenarColumnas(
            @RequestParam UUID id,
            @Valid @RequestBody ReordenarColumnasRequest request
    ) {
        ReordenarColumnasCommand command = TableroCommandMapper.toCommand(id, request);
        Tablero tablero = reordenarColumnasUseCase.reordenar(command);
        return ResponseEntity.ok(TableroResponse.fromDomain(tablero, columnaRepository));
    }
}