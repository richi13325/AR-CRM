package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.NotaTratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaTratoRepository extends JpaRepository<NotaTratoEntity, String> {
    List<NotaTratoEntity> findByTratoIdOrderByCreadoEnDesc(String tratoId);
}
