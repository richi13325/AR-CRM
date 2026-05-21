package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.TareaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for TareaEntity persistence operations.
 * Uses String as the id type to match the String ID boundary convention.
 */
public interface TareaRepository extends JpaRepository<TareaEntity, String> {
}