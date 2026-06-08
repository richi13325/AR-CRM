package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EtiquetaResponse.
 * Verifies domain -> DTO mapping.
 */
class EtiquetaResponseTest {

    @Test
    void fromDomain_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime creadoEn = LocalDateTime.of(2026, 6, 8, 12, 0);
        Etiqueta etiqueta = Etiqueta.reconstitute(
            EtiquetaId.from(id),
            "Urgent",
            TipoEtiqueta.TAREA,
            "#FF0000",
            creadoEn
        );

        EtiquetaResponse response = EtiquetaResponse.fromDomain(etiqueta);

        assertEquals(id, response.id());
        assertEquals("Urgent", response.nombre());
        assertEquals("TAREA", response.tipoEtiqueta());
        assertEquals("#FF0000", response.color());
        assertEquals(creadoEn, response.creadoEn());
    }

    @Test
    void fromDomain_shouldExposeTipoAsStringName() {
        Etiqueta e1 = Etiqueta.reconstitute(
            EtiquetaId.from(UUID.randomUUID()),
            "X", TipoEtiqueta.TAREA, "#000000", LocalDateTime.now()
        );
        Etiqueta e2 = Etiqueta.reconstitute(
            EtiquetaId.from(UUID.randomUUID()),
            "Y", TipoEtiqueta.TRATO, "#FFFFFF", LocalDateTime.now()
        );

        assertEquals("TAREA", EtiquetaResponse.fromDomain(e1).tipoEtiqueta());
        assertEquals("TRATO", EtiquetaResponse.fromDomain(e2).tipoEtiqueta());
    }
}
