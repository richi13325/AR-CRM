package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.ContactoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Contacto persistence.
 */
@Repository
public interface ContactoRepository extends JpaRepository<ContactoEntity, String> {

    /**
     * Checks whether any Tratos are associated with the given Contacto.
     * Uses JPQL over TratoEntity (not native SQL) per project conventions.
     */
    @Query("""
        SELECT COUNT(t) > 0
        FROM TratoEntity t
        WHERE t.contactoId = :contactoId
        """)
    boolean existsTratosByContactoId(@Param("contactoId") String contactoId);
}