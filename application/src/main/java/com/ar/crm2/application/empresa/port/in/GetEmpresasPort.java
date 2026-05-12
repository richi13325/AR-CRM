package com.ar.crm2.application.empresa.port.in;

import com.ar.crm2.model.entity.Empresa;

import java.util.List;

/**
 * Input port for retrieving all Empresas.
 */
public interface GetEmpresasPort {

    /**
     * Retrieves all Empresas for listing.
     *
     * @return list of all domain entities
     */
    List<Empresa> getAll();
}