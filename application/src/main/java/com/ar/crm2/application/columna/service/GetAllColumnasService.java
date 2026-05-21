package com.ar.crm2.application.columna.service;

import com.ar.crm2.application.columna.port.in.GetAllColumnasUseCase;
import com.ar.crm2.application.columna.port.out.FindAllColumnasPort;
import com.ar.crm2.model.entity.Columna;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service implementing GetAllColumnasUseCase.
 * Delegates listing directly to the outbound FindAllColumnasPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class GetAllColumnasService implements GetAllColumnasUseCase {

    private final FindAllColumnasPort findAllPort;

    @Override
    public List<Columna> getAll() {
        return findAllPort.findAll();
    }
}