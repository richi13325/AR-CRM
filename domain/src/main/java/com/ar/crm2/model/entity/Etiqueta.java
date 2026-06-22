package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvalidColorFormatException;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Rich domain entity for the global Etiqueta (tag) catalog.
 *
 * <p>An Etiqueta is a reusable tag that any Ficha may be associated with
 * through a {@link FichaEtiqueta} relation. The catalog row is the source
 * of truth for name and color; renaming or recoloring affects every
 * Ficha that references it.
 *
 * <p>Identity: EtiquetaId (UUID).
 * Equality: by id only.
 * Constructor is private; use static factory methods create() and reconstitute().
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Etiqueta {

    /** Hex color format: '#' followed by exactly 6 hex characters. */
    private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @EqualsAndHashCode.Include
    private final EtiquetaId id;

    private final String nombre;
    private final TipoEtiqueta tipoEtiqueta;
    private final String color;
    private final LocalDateTime creadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Etiqueta with the given name, type and color.
     * Generates id and creadoEn internally.
     *
     * @param nombre       mandatory, 1–50 characters after trim
     * @param tipoEtiqueta mandatory, TAREA or TRATO
     * @param color        mandatory, hex format "#RRGGBB" (e.g. "#FF0000")
     */
    public static Etiqueta create(String nombre, TipoEtiqueta tipoEtiqueta, String color) {
        DomainAssert.notNull(tipoEtiqueta, "tipoEtiqueta");
        DomainAssert.lengthBetween(nombre, "nombre", 1, 50);
        String normalizedColor = validarColor(color);

        return new Etiqueta(
            EtiquetaId.create(),
            nombre.trim(),
            tipoEtiqueta,
            normalizedColor,
            LocalDateTime.now()
        );
    }

    /**
     * Reconstitutes an existing Etiqueta from persistence.
     * Skips uniqueness validation (uniqueness is enforced by the persistence layer).
     */
    public static Etiqueta reconstitute(
        EtiquetaId id,
        String nombre,
        TipoEtiqueta tipoEtiqueta,
        String color,
        LocalDateTime creadoEn
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(tipoEtiqueta, "tipoEtiqueta");
        DomainAssert.notNull(creadoEn, "creadoEn");
        DomainAssert.lengthBetween(nombre, "nombre", 1, 50);
        String normalizedColor = validarColor(color);

        return new Etiqueta(id, nombre.trim(), tipoEtiqueta, normalizedColor, creadoEn);
    }

    // ── Domain Behavior ────────────────────────────────────────────

    /**
     * Returns a new Etiqueta with the given name. Identity and color are preserved.
     *
     * @param nuevoNombre the new name (mandatory, 1–50 characters after trim)
     * @return a new Etiqueta instance with the updated name
     */
    public Etiqueta rename(String nuevoNombre) {
        DomainAssert.lengthBetween(nuevoNombre, "nombre", 1, 50);
        return new Etiqueta(this.id, nuevoNombre.trim(), this.tipoEtiqueta, this.color, this.creadoEn);
    }

    /**
     * Returns a new Etiqueta with the given color. Identity, name and type are preserved.
     *
     * @param nuevoColor the new color (mandatory, hex format "#RRGGBB")
     * @return a new Etiqueta instance with the updated color
     */
    public Etiqueta recolor(String nuevoColor) {
        String normalizedColor = validarColor(nuevoColor);
        return new Etiqueta(this.id, this.nombre, this.tipoEtiqueta, normalizedColor, this.creadoEn);
    }

    // ── Internal validation ──────────────────────────────────────

    private static String validarColor(String color) {
        if (color == null || color.isBlank()) {
            throw InvalidColorFormatException.required();
        }
        String trimmed = color.trim();
        if (!COLOR_PATTERN.matcher(trimmed).matches()) {
            throw InvalidColorFormatException.malformed();
        }
        return trimmed.toUpperCase();
    }
}
