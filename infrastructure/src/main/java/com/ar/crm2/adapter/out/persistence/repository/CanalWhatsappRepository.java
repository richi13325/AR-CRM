package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.CanalWhatsappEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CanalWhatsappRepository extends JpaRepository<CanalWhatsappEntity, String> {
    List<CanalWhatsappEntity> findByEmpresaId(String empresaId);
}
