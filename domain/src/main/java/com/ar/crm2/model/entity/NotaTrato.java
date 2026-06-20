package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.TipoNota;
import com.ar.crm2.model.vo.NotaTratoId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entrada del timeline de un Trato: nota manual (NOTA) o evento del sistema (EVENTO).
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotaTrato {

    @EqualsAndHashCode.Include
    private final NotaTratoId id;

    private final TratoId tratoId;
    private final UsuarioId autorId;   // nullable para eventos del sistema
    private final TipoNota tipo;
    private final String contenido;
    private final LocalDateTime creadoEn;

    /** Nota manual escrita por un usuario. */
    public static NotaTrato crearNota(TratoId tratoId, UsuarioId autorId, String contenido) {
        return new NotaTrato(
            NotaTratoId.create(),
            DomainAssert.notNull(tratoId, "tratoId"),
            DomainAssert.notNull(autorId, "autorId"),
            TipoNota.NOTA,
            DomainAssert.lengthBetween(contenido, "contenido", 1, 2000),
            LocalDateTime.now()
        );
    }

    /** Evento automático del sistema (sin autor). */
    public static NotaTrato crearEvento(TratoId tratoId, String contenido) {
        return new NotaTrato(
            NotaTratoId.create(),
            DomainAssert.notNull(tratoId, "tratoId"),
            null,
            TipoNota.EVENTO,
            DomainAssert.lengthBetween(contenido, "contenido", 1, 2000),
            LocalDateTime.now()
        );
    }

    public static NotaTrato reconstitute(
        NotaTratoId id, TratoId tratoId, UsuarioId autorId, TipoNota tipo,
        String contenido, LocalDateTime creadoEn
    ) {
        return new NotaTrato(
            DomainAssert.notNull(id, "id"),
            DomainAssert.notNull(tratoId, "tratoId"),
            autorId,
            DomainAssert.notNull(tipo, "tipo"),
            contenido,
            DomainAssert.notNull(creadoEn, "creadoEn")
        );
    }
}
