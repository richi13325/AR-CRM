package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateBotRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditBotRequest;
import com.ar.crm2.adapter.in.rest.dto.response.BotResponse;
import com.ar.crm2.whatsapp.application.bot.command.CreateBotCommand;
import com.ar.crm2.whatsapp.application.bot.command.EditBotCommand;
import com.ar.crm2.whatsapp.application.bot.port.in.CreateBotUseCase;
import com.ar.crm2.whatsapp.application.bot.port.in.DeleteBotUseCase;
import com.ar.crm2.whatsapp.application.bot.port.in.EditBotUseCase;
import com.ar.crm2.whatsapp.application.bot.port.in.GetAllBotsUseCase;
import com.ar.crm2.whatsapp.application.bot.port.in.GetBotByIdUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** CRUD de bots de n8n (Agent Bot estilo Chatwoot). Requiere JWT — administración del CRM. */
@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
public class BotController {

    private final CreateBotUseCase createUseCase;
    private final GetAllBotsUseCase getAllUseCase;
    private final GetBotByIdUseCase getByIdUseCase;
    private final EditBotUseCase editUseCase;
    private final DeleteBotUseCase deleteUseCase;

    @PostMapping
    public ResponseEntity<BotResponse> create(@Valid @RequestBody CreateBotRequest request) {
        var bot = createUseCase.create(new CreateBotCommand(request.nombre(), request.canalId(), request.webhookUrl()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BotResponse.fromDomain(bot));
    }

    @GetMapping
    public ResponseEntity<List<BotResponse>> getAll() {
        return ResponseEntity.ok(getAllUseCase.getAll().stream().map(BotResponse::fromDomain).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BotResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(BotResponse.fromDomain(getByIdUseCase.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BotResponse> edit(@PathVariable UUID id, @Valid @RequestBody EditBotRequest request) {
        var bot = editUseCase.edit(new EditBotCommand(id, request.nombre(), request.canalId(), request.webhookUrl()));
        return ResponseEntity.ok(BotResponse.fromDomain(bot));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
