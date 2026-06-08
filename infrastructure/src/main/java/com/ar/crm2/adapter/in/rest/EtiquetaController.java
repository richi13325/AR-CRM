package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateEtiquetaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditEtiquetaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.EtiquetaResponse;
import com.ar.crm2.adapter.in.rest.mapper.EtiquetaCommandMapper;
import com.ar.crm2.application.etiqueta.command.CreateEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.DeleteEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.EditEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.GetAllEtiquetasCommand;
import com.ar.crm2.application.etiqueta.command.GetEtiquetaByIdCommand;
import com.ar.crm2.application.etiqueta.port.in.CreateEtiquetaUseCase;
import com.ar.crm2.application.etiqueta.port.in.DeleteEtiquetaUseCase;
import com.ar.crm2.application.etiqueta.port.in.EditEtiquetaUseCase;
import com.ar.crm2.application.etiqueta.port.in.GetAllEtiquetasUseCase;
import com.ar.crm2.application.etiqueta.port.in.GetEtiquetaByIdUseCase;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;
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
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for Etiqueta operations.
 * Implements the inbound adapter pattern, delegating to application UseCase
 * contracts. All ids via {@code @RequestParam} (consistent with the rest of
 * the controllers in the project). Routes use named actions.
 */
@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final CreateEtiquetaUseCase createUseCase;
    private final GetAllEtiquetasUseCase getAllUseCase;
    private final GetEtiquetaByIdUseCase getByIdUseCase;
    private final EditEtiquetaUseCase editUseCase;
    private final DeleteEtiquetaUseCase deleteUseCase;

    /**
     * Creates a new Etiqueta in the global catalog.
     */
    @PostMapping("/create")
    public ResponseEntity<EtiquetaResponse> create(@Valid @RequestBody CreateEtiquetaRequest request) {
        CreateEtiquetaCommand command = EtiquetaCommandMapper.toCreateCommand(request);
        Etiqueta etiqueta = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(EtiquetaResponse.fromDomain(etiqueta));
    }

    /**
     * Retrieves all Etiquetas, optionally filtered by {@code tipoEtiqueta}.
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<EtiquetaResponse>> getAll(
            @RequestParam(required = false) TipoEtiqueta tipoEtiqueta
    ) {
        GetAllEtiquetasCommand command = EtiquetaCommandMapper.toGetAllCommand(tipoEtiqueta);
        List<Etiqueta> etiquetas = getAllUseCase.getAll(Optional.ofNullable(command.tipoEtiqueta()));
        List<EtiquetaResponse> responses = etiquetas.stream().map(EtiquetaResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a single Etiqueta by id.
     */
    @GetMapping("/get-by-id")
    public ResponseEntity<EtiquetaResponse> getById(@RequestParam UUID id) {
        GetEtiquetaByIdCommand command = EtiquetaCommandMapper.toGetByIdCommand(id);
        Etiqueta etiqueta = getByIdUseCase.getById(command);
        return ResponseEntity.ok(EtiquetaResponse.fromDomain(etiqueta));
    }

    /**
     * Updates an existing Etiqueta's name and color. Tipo is immutable.
     */
    @PutMapping("/edit")
    public ResponseEntity<EtiquetaResponse> edit(
            @RequestParam UUID id,
            @Valid @RequestBody EditEtiquetaRequest request
    ) {
        EditEtiquetaCommand command = EtiquetaCommandMapper.toEditCommand(id, request);
        Etiqueta etiqueta = editUseCase.edit(command);
        return ResponseEntity.ok(EtiquetaResponse.fromDomain(etiqueta));
    }

    /**
     * Deletes an existing Etiqueta.
     * <p>When the Etiqueta is in use, the caller must pass
     * {@code confirm=true} or the application service rejects the call with
     * {@link com.ar.crm2.application.etiqueta.exception.EtiquetaRequiresConfirmationException}.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(
            @RequestParam UUID id,
            @RequestParam(defaultValue = "false") boolean confirm
    ) {
        DeleteEtiquetaCommand command = EtiquetaCommandMapper.toDeleteCommand(id, confirm);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }
}
