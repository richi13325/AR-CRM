package com.ar.crm2.application.empresa.port.out;

import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.List;

/**
 * Granular outbound port for resolving the companies owned by a
 * requester user.
 *
 * <p>Tenant ownership is derived from
 * {@code Empresa.creadoPor == usuarioId} per design.md §Security
 * and the current phase-1 decision. The infrastructure adapter
 * implements this with a derived Spring Data query
 * {@code findByCreadoPor(String creadoPor)}.
 */
public interface FindEmpresasByCreadorPort {

    /**
     * Returns the list of company ids created by the supplied user.
     *
     * @param creadoPor the user id to look up
     * @return the list of company ids owned by that user; empty if none
     */
    List<EmpresaId> findEmpresasByCreador(UsuarioId creadoPor);
}