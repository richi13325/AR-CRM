package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Rich domain entity for Empresa.
 *
 * Identity: EmpresaId (wraps UUID).
 * Equality: by id only (not full attribute equality).
 * No public setters — state changes go through business methods that preserve invariants.
 * Constructor is private; use static factory methods create() and reconstitute().
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Empresa {

    @EqualsAndHashCode.Include
    private final EmpresaId id;

    private final String nombre;
    private final String sector;
    private final String telefono;
    private final String paginaWeb;
    private final String facebook;
    private final String instagram;
    private final String twitter;
    private final EstadoRelacion estadoRelacion;
    private final UsuarioId responsableId;
    private final UsuarioId creadoPor;
    private final String notas;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Empresa.
     * Generates id and timestamps internally.
     * All fields except nombre are optional.
     */
    public static Empresa create(
        String nombre,
        String sector,
        String telefono,
        String paginaWeb,
        String facebook,
        String instagram,
        String twitter,
        EstadoRelacion estadoRelacion,
        UsuarioId responsableId,
        UsuarioId creadoPor,
        String notas
    ) {
        return Empresa.builder()
            .id(EmpresaId.create())
            .nombre(DomainAssert.lengthBetween(nombre, 1, 200, "nombre must be 1-200 chars"))
            .sector(sector)
            .telefono(telefono)
            .paginaWeb(paginaWeb)
            .facebook(facebook)
            .instagram(instagram)
            .twitter(twitter)
            .estadoRelacion(estadoRelacion)
            .responsableId(responsableId)
            .creadoPor(creadoPor)
            .notas(notas)
            .creadoEn(LocalDateTime.now())
            .build();
    }

    /**
     * Reconstitutes an existing Empresa from persistence.
     */
    public static Empresa reconstitute(
        EmpresaId id,
        String nombre,
        String sector,
        String telefono,
        String paginaWeb,
        String facebook,
        String instagram,
        String twitter,
        EstadoRelacion estadoRelacion,
        UsuarioId responsableId,
        UsuarioId creadoPor,
        String notas,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
    ) {
        return Empresa.builder()
            .id(DomainAssert.notNull(id, "id is mandatory"))
            .nombre(DomainAssert.lengthBetween(nombre, 1, 200, "nombre must be 1-200 chars"))
            .sector(sector)
            .telefono(telefono)
            .paginaWeb(paginaWeb)
            .facebook(facebook)
            .instagram(instagram)
            .twitter(twitter)
            .estadoRelacion(estadoRelacion)
            .responsableId(responsableId)
            .creadoPor(creadoPor)
            .notas(notas)
            .creadoEn(DomainAssert.notNull(creadoEn, "creadoEn is mandatory"))
            .actualizadoEn(actualizadoEn)
            .build();
    }
}