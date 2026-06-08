package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.EtiquetaEntity;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link EtiquetaMapper}.
 * The mapper is a pure static utility, so no mocks are needed.
 */
class EtiquetaMapperTest {

    // ── toEntity ────────────────────────────────────────────────────

    @Test
    void toEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Etiqueta domain = Etiqueta.reconstitute(
            EtiquetaId.from(id), "Urgent", TipoEtiqueta.TAREA, "#FF0000", now
        );

        EtiquetaEntity entity = EtiquetaMapper.toEntity(domain);

        assertEquals(id.toString(), entity.getId());
        assertEquals("Urgent", entity.getNombre());
        assertEquals(TipoEtiqueta.TAREA, entity.getTipoEtiqueta());
        assertEquals("#FF0000", entity.getColor());
        assertEquals(now, entity.getCreadoEn());
    }

    @Test
    void toEntity_shouldHandleNullDomain() {
        assertNull(EtiquetaMapper.toEntity(null));
    }

    // ── toDomain ────────────────────────────────────────────────────

    @Test
    void toDomain_shouldMapAllFields() {
        String idStr = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        EtiquetaEntity entity = EtiquetaEntity.builder()
            .id(idStr)
            .nombre("Premium")
            .tipoEtiqueta(TipoEtiqueta.TRATO)
            .color("#0000FF")
            .creadoEn(now)
            .build();

        Etiqueta domain = EtiquetaMapper.toDomain(entity);

        assertEquals(UUID.fromString(idStr), domain.getId().value());
        assertEquals("Premium", domain.getNombre());
        assertEquals(TipoEtiqueta.TRATO, domain.getTipoEtiqueta());
        assertEquals("#0000FF", domain.getColor());
        assertEquals(now, domain.getCreadoEn());
    }

    @Test
    void toDomain_shouldHandleNullEntity() {
        assertNull(EtiquetaMapper.toDomain(null));
    }

    @Test
    void toDomain_shouldThrowWhenEntityIdIsNull() {
        EtiquetaEntity entity = EtiquetaEntity.builder()
            .id(null)
            .nombre("X")
            .tipoEtiqueta(TipoEtiqueta.TAREA)
            .color("#FFFFFF")
            .creadoEn(LocalDateTime.now())
            .build();

        assertThrows(IllegalArgumentException.class, () -> EtiquetaMapper.toDomain(entity));
    }

    @Test
    void toDomain_shouldThrowWhenEntityCreadoEnIsNull() {
        EtiquetaEntity entity = EtiquetaEntity.builder()
            .id(UUID.randomUUID().toString())
            .nombre("X")
            .tipoEtiqueta(TipoEtiqueta.TAREA)
            .color("#FFFFFF")
            .creadoEn(null)
            .build();

        assertThrows(IllegalArgumentException.class, () -> EtiquetaMapper.toDomain(entity));
    }

    // ── Round-trip ──────────────────────────────────────────────────

    @Test
    void roundTrip_shouldPreserveAllFields() {
        Etiqueta original = Etiqueta.create("Urgent", TipoEtiqueta.TAREA, "#FF0000");
        EtiquetaEntity entity = EtiquetaMapper.toEntity(original);
        Etiqueta back = EtiquetaMapper.toDomain(entity);

        assertNotNull(back);
        assertEquals(original.getId(), back.getId());
        assertEquals(original.getNombre(), back.getNombre());
        assertEquals(original.getTipoEtiqueta(), back.getTipoEtiqueta());
        assertEquals(original.getColor(), back.getColor());
        assertEquals(original.getCreadoEn(), back.getCreadoEn());
    }
}
