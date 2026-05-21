package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateRolRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditRolRequest;
import com.ar.crm2.adapter.in.rest.dto.response.RolResponse;
import com.ar.crm2.adapter.in.rest.mapper.RolCommandMapper;
import com.ar.crm2.application.rol.command.CreateRolCommand;
import com.ar.crm2.application.rol.command.DeleteRolCommand;
import com.ar.crm2.application.rol.command.EditRolCommand;
import com.ar.crm2.application.rol.command.GetRolByIdCommand;
import com.ar.crm2.application.rol.port.in.CreateRolUseCase;
import com.ar.crm2.application.rol.port.in.DeleteRolUseCase;
import com.ar.crm2.application.rol.port.in.EditRolUseCase;
import com.ar.crm2.application.rol.port.in.GetAllRolesUseCase;
import com.ar.crm2.application.rol.port.in.GetRolByIdUseCase;
import com.ar.crm2.model.entity.Rol;
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
 * REST controller for Rol operations.
 * Implements inbound adapter pattern, delegates to application UseCase contracts.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final CreateRolUseCase createUseCase;
    private final GetAllRolesUseCase getAllUseCase;
    private final GetRolByIdUseCase getByIdUseCase;
    private final EditRolUseCase editUseCase;
    private final DeleteRolUseCase deleteUseCase;

    /**
     * Creates a new Rol.
     */
    @PostMapping("/create")
    public ResponseEntity<RolResponse> create(@Valid @RequestBody CreateRolRequest request) {
        CreateRolCommand command = RolCommandMapper.toCommand(request);
        Rol rol = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(RolResponse.fromDomain(rol));
    }

    /**
     * Retrieves all Roles.
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<RolResponse>> getAll() {
        List<Rol> roles = getAllUseCase.getAll();
        List<RolResponse> responses = roles.stream().map(RolResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a Rol by its id.
     */
    @GetMapping("/get-by-id")
    public ResponseEntity<RolResponse> getById(@RequestParam UUID id) {
        GetRolByIdCommand command = RolCommandMapper.toGetByIdCommand(id);
        Rol rol = getByIdUseCase.getById(command);
        return ResponseEntity.ok(RolResponse.fromDomain(rol));
    }

    /**
     * Updates an existing Rol.
     */
    @PutMapping("/edit")
    public ResponseEntity<RolResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditRolRequest request) {
        EditRolCommand command = RolCommandMapper.toCommand(id, request);
        Rol rol = editUseCase.edit(command);
        return ResponseEntity.ok(RolResponse.fromDomain(rol));
    }

    /**
     * Deletes an existing Rol.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        DeleteRolCommand command = RolCommandMapper.toDeleteCommand(id);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }
}