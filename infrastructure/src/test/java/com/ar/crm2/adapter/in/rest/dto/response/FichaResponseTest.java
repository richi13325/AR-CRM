package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.entity.FichaEtiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.EtiquetaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TareaId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FichaResponse.
 * Verifies domain -> DTO mapping, including the etiqueta list extension.
 */
class FichaResponseTest {

    @Test
    void fromDomain_withEmptyEtiquetas_shouldReturnEmptyList() {
        Ficha ficha = Ficha.reconstitute(
            FichaId.from(UUID.randomUUID()),
            ColumnaId.from(UUID.randomUUID()),
            TipoFicha.TAREA,
            null,
            TareaId.from(UUID.randomUUID()),
            Instant.now(),
            List.of()
        );

        FichaResponse response = FichaResponse.fromDomain(ficha);

        assertNotNull(response.etiquetas());
        assertTrue(response.etiquetas().isEmpty());
    }

    @Test
    void fromDomain_withEtiquetas_shouldMapEachAsCompactDto() {
        EtiquetaId etiquetaId1 = EtiquetaId.from(UUID.randomUUID());
        EtiquetaId etiquetaId2 = EtiquetaId.from(UUID.randomUUID());
        Ficha ficha = Ficha.reconstitute(
            FichaId.from(UUID.randomUUID()),
            ColumnaId.from(UUID.randomUUID()),
            TipoFicha.TAREA,
            null,
            TareaId.from(UUID.randomUUID()),
            Instant.now(),
            List.of(
                FichaEtiqueta.create(etiquetaId1, TipoEtiqueta.TAREA),
                FichaEtiqueta.create(etiquetaId2, TipoEtiqueta.TAREA)
            )
        );

        FichaResponse response = FichaResponse.fromDomain(ficha);

        assertEquals(2, response.etiquetas().size());
        assertEquals(etiquetaId1.value(), response.etiquetas().get(0).id());
        assertEquals("TAREA", response.etiquetas().get(0).tipoEtiqueta());
        assertEquals(etiquetaId2.value(), response.etiquetas().get(1).id());
    }

    @Test
    void fromDomain_preservesExistingFields() {
        UUID fichaId = UUID.randomUUID();
        UUID columnaId = UUID.randomUUID();
        UUID tareaId = UUID.randomUUID();
        Instant now = Instant.now();
        Ficha ficha = Ficha.reconstitute(
            FichaId.from(fichaId),
            ColumnaId.from(columnaId),
            TipoFicha.TAREA,
            null,
            TareaId.from(tareaId),
            now,
            List.of()
        );

        FichaResponse response = FichaResponse.fromDomain(ficha);

        assertEquals(fichaId, response.id());
        assertEquals(columnaId, response.columnaId());
        assertEquals(TipoFicha.TAREA, response.tipoFicha());
        assertNull(response.tratoId());
        assertEquals(tareaId, response.tareaId());
        assertEquals(now, response.actualizadoEn());
    }
}
