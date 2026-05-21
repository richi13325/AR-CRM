package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for RolEntity persistence operations.
 * Uses String as the id type to match the String ID boundary convention.
 *
 * NOTE: existsUsuariosByRolId query is temporarily removed because UsuarioEntity
 * does not yet exist. RolRepositoryAdapter.existsUsuariosByRolId throws
 * UnsupportedOperationException as a safe fail-closed strategy until
 * Usuario infrastructure is available. When UsuarioEntity is created and the
 * usuarios table is modeled, re-add the query:
 *
 * @Query("SELECT COUNT(u) > 0 FROM UsuarioEntity u WHERE u.rolId = :rolId")
 * boolean existsUsuariosByRolId(@Param("rolId") String rolId);
 */
@Repository
public interface RolRepository extends JpaRepository<RolEntity, String> {
}