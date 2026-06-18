package com.ar.crm2.adapter.out.persistence.repository;

import com.ar.crm2.adapter.out.persistence.entity.BotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BotRepository extends JpaRepository<BotEntity, String> {

    Optional<BotEntity> findByApiAccessToken(String apiAccessToken);

    List<BotEntity> findByActivoTrueAndCanalIdIsNull();

    List<BotEntity> findByActivoTrueAndCanalId(String canalId);
}
