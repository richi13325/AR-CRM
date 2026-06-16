package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateCanalWaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.CanalWaResponse;
import com.ar.crm2.whatsapp.application.canal.command.CreateCanalCommand;
import com.ar.crm2.whatsapp.application.canal.port.in.CreateCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.DeleteCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.GetAllCanalesUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.GetCanalByIdUseCase;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wa/canales")
@RequiredArgsConstructor
public class WhatsappCanalController {

    private final CreateCanalUseCase createUseCase;
    private final GetAllCanalesUseCase getAllUseCase;
    private final GetCanalByIdUseCase getByIdUseCase;
    private final DeleteCanalUseCase deleteUseCase;

    @PostMapping("/create")
    public ResponseEntity<CanalWaResponse> create(@Valid @RequestBody CreateCanalWaRequest request) {
        CreateCanalCommand command = new CreateCanalCommand(
                request.empresaId(),
                request.nombre(),
                request.instanceName(),
                request.proveedor(),
                request.apiUrl(),
                request.apiKey()
        );
        CanalWhatsapp canal = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CanalWaResponse.fromDomain(canal));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<CanalWaResponse>> getAll(@RequestParam UUID empresaId) {
        List<CanalWaResponse> result = getAllUseCase.getAll(empresaId).stream()
                .map(CanalWaResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<CanalWaResponse> getById(@RequestParam UUID id) {
        CanalWhatsapp canal = getByIdUseCase.getById(id);
        return ResponseEntity.ok(CanalWaResponse.fromDomain(canal));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        deleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
