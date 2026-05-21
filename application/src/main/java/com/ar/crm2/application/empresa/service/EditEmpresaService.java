package com.ar.crm2.application.empresa.service;

import com.ar.crm2.application.empresa.command.EditEmpresaCommand;
import com.ar.crm2.application.empresa.exception.EmpresaNotFoundException;
import com.ar.crm2.application.empresa.port.in.EditEmpresaUseCase;
import com.ar.crm2.application.empresa.port.out.FindEmpresaByIdPort;
import com.ar.crm2.application.empresa.port.out.SaveEmpresaPort;
import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing EditEmpresaUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update, and saving.
 */
@RequiredArgsConstructor
public class EditEmpresaService implements EditEmpresaUseCase {

    private final FindEmpresaByIdPort findPort;
    private final SaveEmpresaPort savePort;

    @Override
    public Empresa edit(EditEmpresaCommand command) {
        EmpresaId empresaId = EmpresaId.from(command.id());

        Empresa existing = findPort.findById(empresaId)
                .orElseThrow(() -> EmpresaNotFoundException.forId(command.id()));

        Empresa updated = Empresa.reconstitute(
                existing.getId(),
                command.nombre(),
                command.sector(),
                command.telefono(),
                command.paginaWeb(),
                command.facebook(),
                command.instagram(),
                command.twitter(),
                command.estadoRelacion(),
                command.responsableId() != null ? UsuarioId.from(command.responsableId()) : null,
                existing.getCreadoPor(),
                command.notas(),
                existing.getCreadoEn(),
                LocalDateTime.now()
        );

        return savePort.save(updated);
    }
}