package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for ColumnaEntity persistence operations.
 * Uses String as the id type to match the String ID boundary convention.
 */
@Repository
public interface ColumnaRepository extends JpaRepository<ColumnaEntity, String> {
}