package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.ColumnaResponse;
import com.ar.crm2.adapter.in.rest.mapper.ColumnaCommandMapper;
import com.ar.crm2.application.columna.command.CreateColumnaCommand;
import com.ar.crm2.application.columna.command.DeleteColumnaCommand;
import com.ar.crm2.application.columna.command.EditColumnaCommand;
import com.ar.crm2.application.columna.command.GetColumnaByIdCommand;
import com.ar.crm2.application.columna.port.in.CreateColumnaUseCase;
import com.ar.crm2.application.columna.port.in.DeleteColumnaUseCase;
import com.ar.crm2.application.columna.port.in.EditColumnaUseCase;
import com.ar.crm2.application.columna.port.in.GetAllColumnasUseCase;
import com.ar.crm2.application.columna.port.in.GetColumnaByIdUseCase;
import com.ar.crm2.model.entity.Columna;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Columna operations.
 * Implements inbound adapter pattern, delegates to application UseCase contracts.
 * All ids via @RequestParam, never @PathVariable. Routes use named actions.
 */
@RestController
@RequestMapping("/api/columnas")
@RequiredArgsConstructor
public class ColumnaController {

    private final CreateColumnaUseCase createUseCase;
    private final GetAllColumnasUseCase getAllUseCase;
    private final GetColumnaByIdUseCase getByIdUseCase;
    private final EditColumnaUseCase editUseCase;
    private final DeleteColumnaUseCase deleteUseCase;

    /**
     * Creates a new Columna.
     */
    @PostMapping("/create")
    public ResponseEntity<ColumnaResponse> create(@Valid @RequestBody CreateColumnaRequest request) {
        CreateColumnaCommand command = ColumnaCommandMapper.toCommand(request);
        Columna columna = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ColumnaResponse.fromDomain(columna));
    }

    /**
     * Retrieves all Columnas.
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<ColumnaResponse>> getAll() {
        List<Columna> columnas = getAllUseCase.getAll();
        List<ColumnaResponse> responses = columnas.stream().map(ColumnaResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a Columna by its id.
     */
    @GetMapping("/get-by-id")
    public ResponseEntity<ColumnaResponse> getById(@RequestParam UUID id) {
        GetColumnaByIdCommand command = ColumnaCommandMapper.toGetByIdCommand(id);
        Columna columna = getByIdUseCase.getById(command);
        return ResponseEntity.ok(ColumnaResponse.fromDomain(columna));
    }

    /**
     * Updates an existing Columna.
     */
    @PutMapping("/edit")
    public ResponseEntity<ColumnaResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditColumnaRequest request) {
        EditColumnaCommand command = ColumnaCommandMapper.toCommand(id, request);
        Columna columna = editUseCase.edit(command);
        return ResponseEntity.ok(ColumnaResponse.fromDomain(columna));
    }

    /**
     * Deletes an existing Columna.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        DeleteColumnaCommand command = ColumnaCommandMapper.toDeleteCommand(id);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }
}