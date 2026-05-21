package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.application.columna.port.out.ExistsColumnaAsignadaPort;
import com.ar.crm2.model.vo.ColumnaId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Adapter for {@link ExistsColumnaAsignadaPort}.
 * Checks whether a catalog Columna is assigned to any Tablero via the
 * {@code columnas_tablero} relation table.
 */
@Repository
@RequiredArgsConstructor
public class ExistsColumnaAsignadaAdapter implements ExistsColumnaAsignadaPort {

    private final TableroRepository tableroRepository;

    @Override
    public boolean existsByColumnaId(ColumnaId columnaId) {
        return tableroRepository.existsByColumnaId(columnaId.value().toString());
    }
}