package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.TableroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Tablero aggregate root persistence.
 * Uses String id matching the project convention (UUID stored as VARCHAR).
 */
@Repository
public interface TableroRepository extends JpaRepository<TableroEntity, String> {

    /**
     * Checks whether a Columna is already assigned to the given Tablero.
     *
     * @param tableroId the string form of the TableroId
     * @param columnaId the string form of the ColumnaId
     * @return true if the column is already assigned to the board
     */
    boolean existsByTableroIdAndColumnaId(String tableroId, String columnaId);

    /**
     * Checks whether a Columna is assigned to any Tablero.
     *
     * @param columnaId the string form of the ColumnaId
     * @return true if the column is assigned to at least one board
     */
    boolean existsByColumnaId(String columnaId);
}