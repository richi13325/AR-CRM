package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.application.tablero.port.out.ExistsFichasByColumnaIdPort;
import com.ar.crm2.model.vo.ColumnaId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistsFichasByColumnaIdAdapter implements ExistsFichasByColumnaIdPort {

    private final FichaRepository fichaRepository;

    @Override
    public boolean existsFichasByColumnaId(ColumnaId columnaId) {
        return fichaRepository.existsByColumnaId(columnaId.value().toString());
    }
}
