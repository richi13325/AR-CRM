package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.MensajeGrupoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeGrupoRepository extends JpaRepository<MensajeGrupoEntity, String> {

    boolean existsByWaMessageId(String waMessageId);

    List<MensajeGrupoEntity> findByGrupoIdOrderByTimestampAsc(String grupoId);
}
