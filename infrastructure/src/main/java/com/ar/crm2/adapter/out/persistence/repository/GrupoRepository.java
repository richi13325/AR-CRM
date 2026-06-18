package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.GrupoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<GrupoEntity, String> {

    Optional<GrupoEntity> findByJid(String jid);

    List<GrupoEntity> findAllByOrderByUltimoMensajeAtDesc();
}
