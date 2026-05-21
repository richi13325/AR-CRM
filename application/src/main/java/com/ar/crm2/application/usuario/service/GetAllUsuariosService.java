package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.usuario.port.in.GetAllUsuariosUseCase;
import com.ar.crm2.application.usuario.port.out.FindAllUsuariosPort;
import com.ar.crm2.model.entity.Usuario;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service implementing GetAllUsuariosUseCase.
 * Returns all Usuarios from the outbound port.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class GetAllUsuariosService implements GetAllUsuariosUseCase {

    private final FindAllUsuariosPort findAllPort;

    @Override
    public List<Usuario> getAll() {
        return findAllPort.findAll();
    }
}