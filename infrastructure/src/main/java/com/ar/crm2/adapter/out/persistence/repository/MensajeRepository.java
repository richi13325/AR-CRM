package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.MensajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MensajeRepository extends JpaRepository<MensajeEntity, String> {
    boolean existsByWaMessageId(String waMessageId);
    Optional<MensajeEntity> findByWaMessageId(String waMessageId);
    List<MensajeEntity> findByConversacionIdOrderByCreadoEnAsc(String conversacionId);
}
