package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.PlantillaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantillaRepository extends JpaRepository<PlantillaEntity, String> {
    List<PlantillaEntity> findAllByOrderByTituloAsc();
}
