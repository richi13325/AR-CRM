package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.entity.FichaEtiqueta;
import com.ar.crm2.model.enums.TipoFicha;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST response DTO for Ficha (Kanban card).
 * Exposes the card's position, type, related entity ids, and the compact
 * etiqueta references the Ficha owns. Each EtiquetaRefDto carries only
 * the catalog id and the snapshotted tipo — the catalog Etiqueta's name
 * and color are resolved separately when needed (catalog is the source
 * of truth, "edit updates universally").
 */
public record FichaResponse(
    UUID id,
    UUID columnaId,
    TipoFicha tipoFicha,
    UUID tratoId,
    UUID tareaId,
    Instant actualizadoEn,
    List<EtiquetaRefDto> etiquetas
) {
    /**
     * Compact DTO carrying only what a Ficha view needs to render an
     * etiqueta reference without resolving the full catalog row.
     */
    public record EtiquetaRefDto(UUID id, String tipoEtiqueta) {
        public static EtiquetaRefDto fromDomain(FichaEtiqueta fe) {
            return new EtiquetaRefDto(
                fe.getEtiquetaId().value(),
                fe.getTipoEtiqueta().name()
            );
        }
    }

    public static FichaResponse fromDomain(Ficha ficha) {
        List<EtiquetaRefDto> etiquetas = ficha.getEtiquetas() == null
            ? List.of()
            : ficha.getEtiquetas().stream().map(EtiquetaRefDto::fromDomain).toList();
        return new FichaResponse(
            ficha.getId().value(),
            ficha.getColumnaId().value(),
            ficha.getTipoFicha(),
            ficha.getTratoId() != null ? ficha.getTratoId().value() : null,
            ficha.getTareaId() != null ? ficha.getTareaId().value() : null,
            ficha.getActualizadoEn(),
            etiquetas
        );
    }
}