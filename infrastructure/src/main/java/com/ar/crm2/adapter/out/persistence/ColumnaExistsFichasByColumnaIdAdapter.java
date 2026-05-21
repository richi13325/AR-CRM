package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.application.columna.port.out.ExistsFichasByColumnaIdPort;
import com.ar.crm2.model.vo.ColumnaId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Adapter for {@link ExistsFichasByColumnaIdPort}.
 * Implements the columna's own port contract (NOT the tablero one).
 * Delegates to real {@link FichaRepository#existsByColumnaId(String)} query.
 */
@Repository
@RequiredArgsConstructor
public class ColumnaExistsFichasByColumnaIdAdapter implements ExistsFichasByColumnaIdPort {

    private final FichaRepository fichaRepository;

    @Override
    public boolean existsFichasByColumnaId(ColumnaId columnaId) {
        return fichaRepository.existsByColumnaId(columnaId.value().toString());
    }
}