package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.FichaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for FichaEntity persistence operations.
 * Uses String as the id type to match the String ID boundary convention.
 */
@Repository
public interface FichaRepository extends JpaRepository<FichaEntity, String> {

    /**
     * Checks whether any Ficha exists in the given column.
     *
     * @param columnaId the column id as String
     * @return true if at least one Ficha exists in the column
     */
    @Query("SELECT COUNT(f) > 0 FROM FichaEntity f WHERE f.columnaId = :columnaId")
    boolean existsByColumnaId(@Param("columnaId") String columnaId);
}