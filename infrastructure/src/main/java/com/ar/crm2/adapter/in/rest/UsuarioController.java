package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.response.UsuarioResponse;
import com.ar.crm2.adapter.in.rest.mapper.UsuarioCommandMapper;
import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.application.usuario.command.DeleteUsuarioCommand;
import com.ar.crm2.application.usuario.command.EditUsuarioCommand;
import com.ar.crm2.application.usuario.command.GetUsuarioByIdCommand;
import com.ar.crm2.application.usuario.port.in.CreateUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.DeleteUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.EditUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.GetAllUsuariosUseCase;
import com.ar.crm2.application.usuario.port.in.GetUsuarioByIdUseCase;
import com.ar.crm2.model.entity.Usuario;
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
 * REST controller for Usuario operations.
 * Implements inbound adapter pattern, delegates to application UseCase contracts.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final CreateUsuarioUseCase createUseCase;
    private final GetAllUsuariosUseCase getAllUseCase;
    private final GetUsuarioByIdUseCase getByIdUseCase;
    private final EditUsuarioUseCase editUseCase;
    private final DeleteUsuarioUseCase deleteUseCase;

    /**
     * Creates a new Usuario.
     */
    @PostMapping("/create")
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody CreateUsuarioRequest request) {
        CreateUsuarioCommand command = UsuarioCommandMapper.toCommand(request);
        Usuario usuario = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.fromDomain(usuario));
    }

    /**
     * Retrieves all Usuarios.
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<UsuarioResponse>> getAll() {
        List<Usuario> usuarios = getAllUseCase.getAll();
        List<UsuarioResponse> responses = usuarios.stream().map(UsuarioResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a Usuario by its id.
     */
    @GetMapping("/get-by-id")
    public ResponseEntity<UsuarioResponse> getById(@RequestParam UUID id) {
        GetUsuarioByIdCommand command = UsuarioCommandMapper.toGetByIdCommand(id);
        Usuario usuario = getByIdUseCase.getById(command);
        return ResponseEntity.ok(UsuarioResponse.fromDomain(usuario));
    }

    /**
     * Updates an existing Usuario.
     */
    @PutMapping("/edit")
    public ResponseEntity<UsuarioResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditUsuarioRequest request) {
        EditUsuarioCommand command = UsuarioCommandMapper.toCommand(id, request);
        Usuario usuario = editUseCase.edit(command);
        return ResponseEntity.ok(UsuarioResponse.fromDomain(usuario));
    }

    /**
     * Deletes an existing Usuario.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        DeleteUsuarioCommand command = UsuarioCommandMapper.toDeleteCommand(id);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }
}