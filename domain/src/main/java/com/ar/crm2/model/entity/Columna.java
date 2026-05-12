package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.EstadoVinculado;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.TableroId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain entity for Columna.
 *
 * Identity: ColumnaId.
 * Equality: by id only.
 * Constructor is private; use static factory methods create() and reconstitute().
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Columna {

    @EqualsAndHashCode.Include
    private final ColumnaId id;

    private final TableroId tableroId;
    private final String nombre;
    private final String color;
    private final Integer posicion;
    private final Integer limiteWip;
    private final EstadoVinculado estadoVinculado;
    private final TipoColumna tipoColumna;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Columna.
     */
    public static Columna create(
        TableroId tableroId,
        TipoTablero tipoTablero,
        String nombre,
        String color,
        Integer posicion,
        Integer limiteWip,
        EstadoVinculado estadoVinculado,
        TipoColumna tipoColumna
    ) {
        DomainAssert.notNull(estadoVinculado, "estadoVinculado is mandatory")
            .assertCompatibleCon(tipoTablero);

        return new Columna(
            ColumnaId.create(),
            DomainAssert.notNull(tableroId, "tableroId is mandatory"),
            DomainAssert.lengthBetween(nombre, 1, 80, "nombre must be 1-80 chars"),
            DomainAssert.lengthBetween(color, 1, 7, "color must be 1-7 chars"),
            DomainAssert.notNull(posicion, "posicion is mandatory"),
            DomainAssert.notNull(limiteWip, "limiteWip is mandatory"),
            DomainAssert.notNull(estadoVinculado, "estadoVinculado is mandatory"),
            DomainAssert.notNull(tipoColumna, "tipoColumna is mandatory")
        );
    }

    /**
     * Reconstitutes an existing Columna from persistence.
     */
    public static Columna reconstitute(
        ColumnaId id,
        TableroId tableroId,
        TipoTablero tipoTablero,
        String nombre,
        String color,
        Integer posicion,
        Integer limiteWip,
        EstadoVinculado estadoVinculado,
        TipoColumna tipoColumna
    ) {
        DomainAssert.notNull(estadoVinculado, "estadoVinculado is mandatory")
            .assertCompatibleCon(tipoTablero);

        return new Columna(
            DomainAssert.notNull(id, "id is mandatory"),
            DomainAssert.notNull(tableroId, "tableroId is mandatory"),
            DomainAssert.lengthBetween(nombre, 1, 80, "nombre must be 1-80 chars"),
            DomainAssert.lengthBetween(color, 1, 7, "color must be 1-7 chars"),
            DomainAssert.notNull(posicion, "posicion is mandatory"),
            DomainAssert.notNull(limiteWip, "limiteWip is mandatory"),
            DomainAssert.notNull(estadoVinculado, "estadoVinculado is mandatory"),
            DomainAssert.notNull(tipoColumna, "tipoColumna is mandatory")
        );
    }

    /**
     * Returns a new Columna with the same identity and the updated estadoVinculado,
     * after validating that nuevoEstado belongs to the given tipoTablero.
     */
    public Columna cambiarEstado(TipoTablero tipoTablero, EstadoVinculado nuevoEstado) {
        DomainAssert.notNull(tipoTablero, "tipoTablero is mandatory");
        DomainAssert.notNull(nuevoEstado, "nuevoEstado is mandatory");
        nuevoEstado.assertCompatibleCon(tipoTablero);
        return new Columna(
            this.id,
            this.tableroId,
            this.nombre,
            this.color,
            this.posicion,
            this.limiteWip,
            nuevoEstado,
            this.tipoColumna
        );
    }
}
