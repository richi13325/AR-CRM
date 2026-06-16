package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.MensajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<MensajeEntity, String> {
    boolean existsByWaMessageId(String waMessageId);
    List<MensajeEntity> findByConversacionIdOrderByCreadoEnAsc(String conversacionId);
}
