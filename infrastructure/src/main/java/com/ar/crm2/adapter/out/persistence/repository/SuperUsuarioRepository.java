package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.SuperUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data JPA repository for SuperUsuario persistence.
 */
@Repository
public interface SuperUsuarioRepository extends JpaRepository<SuperUsuarioEntity, String> {

    boolean existsByKeycloakId(String keycloakId);

    Optional<SuperUsuarioEntity> findByKeycloakId(String keycloakId);
}