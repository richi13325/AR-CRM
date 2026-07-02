package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.ContactoEntity;
import com.ar.crm2.adapter.out.persistence.entity.EmpresaEntity;
import com.ar.crm2.adapter.out.persistence.entity.TratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Empresa persistence.
 */
@Repository
public interface EmpresaRepository extends JpaRepository<EmpresaEntity, String> {

    @Query("""
        SELECT COUNT(t) > 0
        FROM TratoEntity t
        JOIN ContactoEntity c ON t.contactoId = c.id
        WHERE c.empresaId = :empresaId
        """)
    boolean existsTratosByEmpresaId(@Param("empresaId") String empresaId);

    /**
     * PR 2 (add-crm-ai-assistant-spring-ai) — tenant scope derived query.
     *
     * <p>Returns the Empresa rows created by the given user. Used by
     * {@code FindEmpresasByCreadorPort} so the AI assistant can resolve
     * the set of {@code empresaId} values a requester owns (per
     * design.md §Security: {@code Empresa.creadoPor == usuarioId}).
     */
    List<EmpresaEntity> findByCreadoPor(String creadoPor);
}