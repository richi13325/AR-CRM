package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateContactoRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditContactoRequest;
import com.ar.crm2.adapter.in.rest.dto.response.ContactoResponse;
import com.ar.crm2.adapter.in.rest.mapper.ContactoCommandMapper;
import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.application.contacto.command.DeleteContactoCommand;
import com.ar.crm2.application.contacto.command.EditContactoCommand;
import com.ar.crm2.application.contacto.command.GetContactoByIdCommand;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.contacto.port.in.DeleteContactoUseCase;
import com.ar.crm2.application.contacto.port.in.EditContactoUseCase;
import com.ar.crm2.application.contacto.port.in.GetAllContactosUseCase;
import com.ar.crm2.application.contacto.port.in.GetContactoByIdUseCase;
import com.ar.crm2.model.entity.Contacto;
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
 * REST controller for Contacto operations.
 * Implements inbound adapter pattern, delegates to application UseCase contracts.
 */
@RestController
@RequestMapping("/api/contactos")
@RequiredArgsConstructor
public class ContactoController {

    private final CreateContactoUseCase createUseCase;
    private final GetAllContactosUseCase getAllUseCase;
    private final GetContactoByIdUseCase getByIdUseCase;
    private final EditContactoUseCase editUseCase;
    private final DeleteContactoUseCase deleteUseCase;

    /**
     * Creates a new Contacto.
     */
    @PostMapping("/create")
    public ResponseEntity<ContactoResponse> create(@Valid @RequestBody CreateContactoRequest request) {
        CreateContactoCommand command = ContactoCommandMapper.toCommand(request);
        Contacto contacto = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ContactoResponse.fromDomain(contacto));
    }

    /**
     * Retrieves all Contactos.
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<ContactoResponse>> getAll() {
        List<Contacto> contactos = getAllUseCase.getAll();
        List<ContactoResponse> responses = contactos.stream().map(ContactoResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a Contacto by its id.
     */
    @GetMapping("/get-by-id")
    public ResponseEntity<ContactoResponse> getById(@RequestParam UUID id) {
        GetContactoByIdCommand command = ContactoCommandMapper.toGetByIdCommand(id);
        Contacto contacto = getByIdUseCase.getById(command);
        return ResponseEntity.ok(ContactoResponse.fromDomain(contacto));
    }

    /**
     * Updates an existing Contacto.
     */
    @PutMapping("/edit")
    public ResponseEntity<ContactoResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditContactoRequest request) {
        EditContactoCommand command = ContactoCommandMapper.toCommand(id, request);
        Contacto contacto = editUseCase.edit(command);
        return ResponseEntity.ok(ContactoResponse.fromDomain(contacto));
    }

    /**
     * Deletes an existing Contacto.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        DeleteContactoCommand command = ContactoCommandMapper.toDeleteCommand(id);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }
}