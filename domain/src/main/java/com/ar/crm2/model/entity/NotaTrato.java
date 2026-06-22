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
        DomainAssert.notNull(tratoId, "tratoId");
        DomainAssert.notNull(autorId, "autorId");
        DomainAssert.lengthBetween(contenido, "contenido", 1, 2000);

        return new NotaTrato(
            NotaTratoId.create(),
            tratoId,
            autorId,
            TipoNota.NOTA,
            contenido.trim(),
            LocalDateTime.now()
        );
    }

    /** Evento automático del sistema (sin autor). */
    public static NotaTrato crearEvento(TratoId tratoId, String contenido) {
        DomainAssert.notNull(tratoId, "tratoId");
        DomainAssert.lengthBetween(contenido, "contenido", 1, 2000);

        return new NotaTrato(
            NotaTratoId.create(),
            tratoId,
            null,
            TipoNota.EVENTO,
            contenido.trim(),
            LocalDateTime.now()
        );
    }

    public static NotaTrato reconstitute(
        NotaTratoId id, TratoId tratoId, UsuarioId autorId, TipoNota tipo,
        String contenido, LocalDateTime creadoEn
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(tratoId, "tratoId");
        DomainAssert.notNull(tipo, "tipo");
        DomainAssert.notNull(creadoEn, "creadoEn");

        return new NotaTrato(
            id,
            tratoId,
            autorId,
            tipo,
            contenido,
            creadoEn
        );
    }
}
