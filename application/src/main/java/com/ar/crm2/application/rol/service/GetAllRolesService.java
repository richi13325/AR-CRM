package com.ar.crm2.application.rol.service;

import com.ar.crm2.application.rol.port.in.GetAllRolesUseCase;
import com.ar.crm2.application.rol.port.out.FindAllRolesPort;
import com.ar.crm2.model.entity.Rol;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service implementing GetAllRolesUseCase.
 * Delegates listing directly to the outbound FindAllRolesPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class GetAllRolesService implements GetAllRolesUseCase {

    private final FindAllRolesPort findAllPort;

    @Override
    public List<Rol> getAll() {
        return findAllPort.findAll();
    }
}