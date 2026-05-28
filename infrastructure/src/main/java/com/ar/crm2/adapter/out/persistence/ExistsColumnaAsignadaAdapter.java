package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.application.columna.port.out.ExistsColumnaAsignadaPort;
import com.ar.crm2.model.vo.ColumnaId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistsColumnaAsignadaAdapter implements ExistsColumnaAsignadaPort {

    private final TableroRepository tableroRepository;

    @Override
    public boolean existsByColumnaId(ColumnaId columnaId) {
        return tableroRepository.existsByColumnasTableroColumnaId(columnaId.value().toString());
    }
}
