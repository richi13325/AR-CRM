package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.ConversacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversacionRepository extends JpaRepository<ConversacionEntity, String> {

    Optional<ConversacionEntity> findByCanalIdAndNumeroTelefono(String canalId, String numeroTelefono);

    @Query("SELECT c FROM ConversacionEntity c WHERE c.canalId IN " +
           "(SELECT ca.id FROM CanalWhatsappEntity ca WHERE ca.empresaId = :empresaId) " +
           "ORDER BY c.ultimoMensajeAt DESC NULLS LAST")
    List<ConversacionEntity> findByEmpresaId(@Param("empresaId") String empresaId);

    long countByAsignadoA(String asignadoA);
}
