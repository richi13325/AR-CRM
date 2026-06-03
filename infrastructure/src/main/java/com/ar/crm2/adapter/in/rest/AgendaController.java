package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.CreateAgendaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditAgendaRequest;
import com.ar.crm2.adapter.in.rest.dto.response.AgendaResponse;
import com.ar.crm2.adapter.in.rest.mapper.AgendaCommandMapper;
import com.ar.crm2.application.agenda.command.CreateAgendaCommand;
import com.ar.crm2.application.agenda.command.DeleteAgendaCommand;
import com.ar.crm2.application.agenda.command.EditAgendaCommand;
import com.ar.crm2.application.agenda.command.GetAgendaByIdCommand;
import com.ar.crm2.application.agenda.command.GetAgendasByUserCommand;
import com.ar.crm2.application.agenda.port.in.CreateAgendaUseCase;
import com.ar.crm2.application.agenda.port.in.DeleteAgendaUseCase;
import com.ar.crm2.application.agenda.port.in.EditAgendaUseCase;
import com.ar.crm2.application.agenda.port.in.GetAgendasByUserUseCase;
import com.ar.crm2.application.agenda.port.in.GetAgendaByIdUseCase;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.application.security.exception.AuthenticatedUsuarioRequiredException;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.security.ActorContextRequestAttributeFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendas")
@RequiredArgsConstructor
public class AgendaController {

    private final CreateAgendaUseCase createUseCase;
    private final GetAgendasByUserUseCase getByUserUseCase;
    private final GetAgendaByIdUseCase getByIdUseCase;
    private final EditAgendaUseCase editUseCase;
    private final DeleteAgendaUseCase deleteUseCase;

    @PostMapping("/create")
    public ResponseEntity<AgendaResponse> create(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateAgendaRequest request
    ) {
        ActorContext actorContext = (ActorContext) httpRequest.getAttribute(
                ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE);
        CreateAgendaCommand command = AgendaCommandMapper.toCommand(request, actorContext);
        Agenda agenda = createUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AgendaResponse.fromDomain(agenda));
    }

    @GetMapping("/get-all-by-user")
    public ResponseEntity<List<AgendaResponse>> getAllByUser(HttpServletRequest httpRequest) {
        ActorContext actorContext = (ActorContext) httpRequest.getAttribute(
                ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE);
        if (actorContext == null) {
            throw AuthenticatedUsuarioRequiredException.forMissingActorContext();
        }

        UUID usuarioId = actorContext.usuarioId()
                .orElseThrow(AuthenticatedUsuarioRequiredException::forMissingUsuarioId);
        GetAgendasByUserCommand command = new GetAgendasByUserCommand(usuarioId);
        List<Agenda> agendas = getByUserUseCase.getByUser(command);
        List<AgendaResponse> responses = agendas.stream().map(AgendaResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<AgendaResponse> getById(@RequestParam UUID id) {
        GetAgendaByIdCommand command = AgendaCommandMapper.toGetByIdCommand(id);
        Agenda agenda = getByIdUseCase.getById(command);
        return ResponseEntity.ok(AgendaResponse.fromDomain(agenda));
    }

    @PutMapping("/edit")
    public ResponseEntity<AgendaResponse> edit(@RequestParam UUID id, @Valid @RequestBody EditAgendaRequest request) {
        EditAgendaCommand command = AgendaCommandMapper.toCommand(id, request);
        Agenda agenda = editUseCase.edit(command);
        return ResponseEntity.ok(AgendaResponse.fromDomain(agenda));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        DeleteAgendaCommand command = AgendaCommandMapper.toDeleteCommand(id);
        deleteUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }
}
