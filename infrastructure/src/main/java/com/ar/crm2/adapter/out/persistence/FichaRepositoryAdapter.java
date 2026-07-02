package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.FichaEntity;
import com.ar.crm2.adapter.out.persistence.mapper.FichaMapper;
import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.application.ai.port.out.FichaLecturaPort;
import com.ar.crm2.application.ficha.port.out.DeleteFichaByIdPort;
import com.ar.crm2.application.ficha.port.out.ExistsFichasByColumnaIdPort;
import com.ar.crm2.application.ficha.port.out.FindAllFichasPort;
import com.ar.crm2.application.ficha.port.out.FindFichaByIdPort;
import com.ar.crm2.application.ficha.port.out.SaveFichaPort;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * Single persistence adapter that satisfies every Ficha outbound port,
 * including the AI read port ({@link FichaLecturaPort}). The
 * {@code findById(FichaId)} method has the same signature for the CRM
 * and AI read ports, so this adapter is the only implementation needed
 * (no separate bridge class).
 *
 * <p>Tenant authorization is enforced by the AI application service
 * that calls {@link FichaLecturaPort}; this adapter is tenant-blind.
 */
@RequiredArgsConstructor
public class FichaRepositoryAdapter
        implements SaveFichaPort, FindAllFichasPort, FindFichaByIdPort, DeleteFichaByIdPort,
                   ExistsFichasByColumnaIdPort,
                   FichaLecturaPort {

    private final FichaRepository repository;

    @Override
    public Ficha save(Ficha ficha) {
        FichaEntity entity = FichaMapper.toEntity(ficha);
        FichaEntity saved = repository.save(entity);
        return FichaMapper.toDomain(saved);
    }

    @Override
    public List<Ficha> findAll() {
        return repository.findAll().stream()
            .map(FichaMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Ficha> findById(FichaId id) {
        return repository.findById(id.value().toString())
            .map(FichaMapper::toDomain);
    }

    @Override
    public void deleteById(FichaId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public boolean existsFichasByColumnaId(ColumnaId id) {
        return repository.existsByColumnaId(id.value().toString());
    }
}
