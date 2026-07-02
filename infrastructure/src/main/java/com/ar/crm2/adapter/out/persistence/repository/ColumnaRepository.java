package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for ColumnaEntity persistence operations.
 * Uses String as the id type to match the String ID boundary convention.
 */
@Repository
public interface ColumnaRepository extends JpaRepository<ColumnaEntity, String> {

    /**
     * Returns every catalog column whose {@code tipo_tablero} column
     * matches the supplied value. The {@code tipo_tablero} column is
     * stored as the {@link com.ar.crm2.model.enums.TipoTablero}
     * {@code name()} (e.g. {@code "TAREAS"} or {@code "TRATOS"}).
     *
     * <p>Used by the AI {@code ListarColumnasTableroTool} to
     * enumerate the valid target columns for a Kanban move
     * proposal.
     */
    List<ColumnaEntity> findByTipoTablero(String tipoTablero);
}