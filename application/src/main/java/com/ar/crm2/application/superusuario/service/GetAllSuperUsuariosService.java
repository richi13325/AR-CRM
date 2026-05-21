package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.superusuario.port.in.GetAllSuperUsuariosUseCase;
import com.ar.crm2.application.superusuario.port.out.FindAllSuperUsuariosPort;
import com.ar.crm2.model.entity.SuperUsuario;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service implementing GetAllSuperUsuariosUseCase.
 * Returns all SuperUsuarios from the outbound port.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class GetAllSuperUsuariosService implements GetAllSuperUsuariosUseCase {

    private final FindAllSuperUsuariosPort findAllPort;

    @Override
    public List<SuperUsuario> getAll() {
        return findAllPort.findAll();
    }
}