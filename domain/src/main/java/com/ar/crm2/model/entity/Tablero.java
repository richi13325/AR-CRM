package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.TableroId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Domain entity for Tablero.
 *
 * Identity: TableroId.
 * Equality: by id only.
 * Constructor is private; use static factory methods create() and reconstitute().
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tablero {

    @EqualsAndHashCode.Include
    private final TableroId id;

    private final String nombre;
    private final String descripcion;
    private final List<Columna> columnas;
    private final TipoTablero tipoTablero;
    private final LocalDateTime creadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Tablero.
     * Generates id and creadoEn internally.
     */
    public static Tablero create(String nombre, String descripcion, TipoTablero tipoTablero, List<Columna> columnasPredeterminadas) {
        DomainAssert.notNull(columnasPredeterminadas, "columnasPredeterminadas is mandatory");

        Optional.of(columnasPredeterminadas)
            .filter(columnas -> columnas.size() == 4)
            .orElseThrow(() -> new InvariantViolationException("Tablero must have 4 default columns"));

        Optional.of(columnasPredeterminadas)
            .filter(columnas -> columnas.stream()
                .allMatch(columna -> columna != null && TipoColumna.PREDETERMINADA.equals(columna.getTipoColumna())))
            .orElseThrow(() -> new InvariantViolationException(
                "Tablero default columns must be non-null and PREDETERMINADA"));

        return new Tablero(
            TableroId.create(),
            DomainAssert.lengthBetween(nombre, 1, 100, "nombre must be 1-100 chars"),
            DomainAssert.notBlank(descripcion, "descripcion is mandatory"),
            List.copyOf(columnasPredeterminadas),
            DomainAssert.notNull(tipoTablero, "tipoTablero is mandatory"),
            LocalDateTime.now()
        );
    }

    /**
     * Reconstitutes an existing Tablero from persistence.
     */
    public static Tablero reconstitute(
        TableroId id,
        String nombre,
        String descripcion,
        List<Columna> columnas,
        TipoTablero tipoTablero,
        LocalDateTime creadoEn
    ) {
        return new Tablero(
            DomainAssert.notNull(id, "id is mandatory"),
            DomainAssert.lengthBetween(nombre, 1, 100, "nombre must be 1-100 chars"),
            DomainAssert.notBlank(descripcion, "descripcion is mandatory"),
            List.copyOf(DomainAssert.notNull(columnas, "columnas is mandatory")),
            DomainAssert.notNull(tipoTablero, "tipoTablero is mandatory"),
            DomainAssert.notNull(creadoEn, "creadoEn is mandatory")
        );
    }
}
