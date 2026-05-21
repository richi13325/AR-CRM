package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateSuperUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditSuperUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.response.SuperUsuarioResponse;
import com.ar.crm2.adapter.in.rest.mapper.SuperUsuarioCommandMapper;
import com.ar.crm2.application.superusuario.command.CreateSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.command.DeleteSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.command.EditSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.command.GetSuperUsuarioByIdCommand;
import com.ar.crm2.application.superusuario.port.in.CreateSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.in.DeleteSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.in.EditSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.in.GetAllSuperUsuariosUseCase;
import com.ar.crm2.application.superusuario.port.in.GetSuperUsuarioByIdUseCase;
import com.ar.crm2.model.entity.SuperUsuario;
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
 * REST controller for SuperUsuario operations.
 * Implements inbound adapter pattern, delegates to application UseCase contracts.
 * Uses @RequestParam for all id parameters.
 */
@RestController
@RequestMapping("/api/superusuarios")
@RequiredArgsConstructor
public class SuperUsuarioController {

    private final CreateSuperUsuarioUseCase createUseCase;
    private final GetAllSuperUsuariosUseCase getAllUseCase;
    private final GetSuperUsuarioByIdUseCase getByIdUseCase;
    private final EditSuperUsuarioUseCase editUseCase;
    private final DeleteSuperUsuarioUseCase deleteUseCase;

    /**
     * Creates a new SuperUsuario.
     */
    @PostMapping("/create")
    public ResponseEntity<SuperUsuarioResponse> create(@Valid @RequestBody CreateSuperUsuarioRequest request) {
        CreateSuperUsuarioCommand command = SuperUsuarioCommandMapper.toCommand(request);
        SuperUsuario usuario = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuperUsuarioResponse.fromDomain(usuario));
    }

    /**
     * Retrieves all SuperUsuarios.
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<SuperUsuarioResponse>> getAll() {
        List<SuperUsuario> usuarios = getAllUseCase.getAll();
        List<SuperUsuarioResponse> responses = usuarios.stream().map(SuperUsuarioResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a SuperUsuario by its id.
     */
    @GetMapping("/get-by-id")
    public ResponseEntity<SuperUsuarioResponse> getById(@RequestParam UUID id) {
        GetSuperUsuarioByIdCommand command = SuperUsuarioCommandMapper.toGetByIdCommand(id);
        SuperUsuario usuario = getByIdUseCase.getById(command);
        return ResponseEntity.ok(SuperUsuarioResponse.fromDomain(usuario));
    }

    /**
     * Updates an existing SuperUsuario.
     */
    @PutMapping("/edit")
    public ResponseEntity<SuperUsuarioResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditSuperUsuarioRequest request) {
        EditSuperUsuarioCommand command = SuperUsuarioCommandMapper.toCommand(id, request);
        SuperUsuario usuario = editUseCase.edit(command);
        return ResponseEntity.ok(SuperUsuarioResponse.fromDomain(usuario));
    }

    /**
     * Deletes an existing SuperUsuario.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        DeleteSuperUsuarioCommand command = SuperUsuarioCommandMapper.toDeleteCommand(id);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }
}
