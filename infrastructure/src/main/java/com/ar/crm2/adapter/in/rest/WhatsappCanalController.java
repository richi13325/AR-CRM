package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateCanalWaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.CanalWaResponse;
import com.ar.crm2.adapter.in.rest.dto.response.ConectarCanalResponse;
import com.ar.crm2.adapter.in.rest.dto.response.EstadoCanalResponse;
import com.ar.crm2.whatsapp.application.canal.command.ConectarCanalCommand;
import com.ar.crm2.whatsapp.application.canal.command.CreateCanalCommand;
import com.ar.crm2.adapter.in.rest.dto.request.EditCanalWaRequest;
import com.ar.crm2.whatsapp.application.canal.command.EditCanalCommand;
import com.ar.crm2.whatsapp.application.canal.port.in.ConectarCanalResult;
import com.ar.crm2.whatsapp.application.canal.port.in.ConectarCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.CreateCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.DeleteCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.EditCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.GetAllCanalesUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.GetCanalByIdUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.GetEstadoCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.ReconfigurarWebhookUseCase;
import com.ar.crm2.whatsapp.application.canal.port.in.SyncChatsUseCase;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import com.ar.crm2.whatsapp.domain.enums.ProveedorCanal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wa/canales")
@RequiredArgsConstructor
public class WhatsappCanalController {

    private final CreateCanalUseCase createUseCase;
    private final GetAllCanalesUseCase getAllUseCase;
    private final GetCanalByIdUseCase getByIdUseCase;
    private final EditCanalUseCase editUseCase;
    private final DeleteCanalUseCase deleteUseCase;
    private final ConectarCanalUseCase conectarUseCase;
    private final GetEstadoCanalUseCase getEstadoUseCase;
    private final SyncChatsUseCase syncChatsUseCase;
    private final ReconfigurarWebhookUseCase reconfigurarWebhookUseCase;

    @Value("${crm2.wa.evolution.api-url:}")
    private String globalEvolutionApiUrl;

    @Value("${crm2.wa.evolution.api-key:}")
    private String globalEvolutionApiKey;

    @PostMapping("/create")
    public ResponseEntity<CanalWaResponse> create(@Valid @RequestBody CreateCanalWaRequest request) {
        String apiUrl = notBlank(request.apiUrl()) ? request.apiUrl() : globalEvolutionApiUrl;
        String apiKey = notBlank(request.apiKey()) ? request.apiKey() : globalEvolutionApiKey;
        String instanceName = notBlank(request.instanceName()) ? request.instanceName() : generateInstanceName(request.nombre());

        CreateCanalCommand command = new CreateCanalCommand(
                request.empresaId(),
                request.nombre(),
                instanceName,
                ProveedorCanal.EVOLUTION_API,
                apiUrl,
                apiKey
        );
        CanalWhatsapp canal = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CanalWaResponse.fromDomain(canal));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<CanalWaResponse>> getAll(@RequestParam(required = false) UUID empresaId) {
        List<CanalWhatsapp> canales = empresaId != null
                ? getAllUseCase.getAll(empresaId)
                : getAllUseCase.getAll();
        return ResponseEntity.ok(canales.stream().map(CanalWaResponse::fromDomain).toList());
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<CanalWaResponse> getById(@RequestParam UUID id) {
        CanalWhatsapp canal = getByIdUseCase.getById(id);
        return ResponseEntity.ok(CanalWaResponse.fromDomain(canal));
    }

    @PutMapping("/edit")
    public ResponseEntity<CanalWaResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditCanalWaRequest request) {
        CanalWhatsapp existing = getByIdUseCase.getById(id);
        String apiUrl = notBlank(existing.getApiUrl()) ? existing.getApiUrl() : globalEvolutionApiUrl;
        String apiKey = notBlank(globalEvolutionApiKey) ? globalEvolutionApiKey : existing.getApiKey();
        EditCanalCommand command = new EditCanalCommand(id, request.nombre(), apiUrl, apiKey);
        CanalWhatsapp updated = editUseCase.edit(command);
        return ResponseEntity.ok(CanalWaResponse.fromDomain(updated));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        deleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conectar")
    public ResponseEntity<ConectarCanalResponse> conectar(@RequestParam UUID id) {
        ConectarCanalResult result = conectarUseCase.conectar(new ConectarCanalCommand(id));
        return ResponseEntity.ok(new ConectarCanalResponse(result.qrBase64(), result.estado()));
    }

    @GetMapping("/estado")
    public ResponseEntity<EstadoCanalResponse> getEstado(@RequestParam UUID id) {
        EstadoCanal estado = getEstadoUseCase.getEstado(id);
        return ResponseEntity.ok(new EstadoCanalResponse(estado));
    }

    @PostMapping("/sync-chats")
    public ResponseEntity<Map<String, Integer>> syncChats(@RequestParam UUID canalId) {
        int imported = syncChatsUseCase.syncChats(canalId);
        return ResponseEntity.ok(Map.of("imported", imported));
    }

    // Re-registra el webhook de Evolution para un canal ya conectado (sin re-escanear QR).
    // Usar tras configurar/cambiar WA_WEBHOOK_BASE_URL para que lleguen mensajes en vivo.
    @PostMapping("/reconfigurar-webhook")
    public ResponseEntity<Void> reconfigurarWebhook(@RequestParam UUID id) {
        reconfigurarWebhookUseCase.reconfigurar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String generateInstanceName(String nombre) {
        String slug = nombre.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (slug.length() > 25) slug = slug.substring(0, 25);
        return slug + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
}
