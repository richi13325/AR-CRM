package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateEmpresaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.EmpresaResponse;
import com.ar.crm2.adapter.in.rest.mapper.EmpresaCommandMapper;
import com.ar.crm2.application.empresa.port.in.CreateEmpresaPort;
import com.ar.crm2.application.empresa.port.in.GetEmpresasPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Empresa operations.
 * Implements inbound adapter pattern, delegates to application input ports.
 */
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final CreateEmpresaPort createPort;
    private final GetEmpresasPort getPort;

    /**
     * Creates a new Empresa.
     */
    @PostMapping
    public ResponseEntity<EmpresaResponse> create(@Valid @RequestBody CreateEmpresaRequest request) {
        var command = EmpresaCommandMapper.toCommand(request);
        var empresa = createPort.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmpresaResponse.fromDomain(empresa));
    }

    /**
     * Retrieves all Empresas.
     */
    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> getAll() {
        var empresas = getPort.getAll();
        var responses = empresas.stream().map(EmpresaResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }
}
