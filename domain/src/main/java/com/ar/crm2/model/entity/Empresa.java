package com.ar.crm2.model.entity;

import com.ar.crm2.exception.EmpresaStateTransitionException;
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
            .nombre(DomainAssert.lengthBetween(nombre, "nombre", 1, 200))
            .sector(sector)
            .telefono(telefono)
            .paginaWeb(paginaWeb == null || paginaWeb.isBlank() ? null : DomainAssert.link(paginaWeb, "paginaWeb"))
            .facebook(facebook == null || facebook.isBlank() ? null : DomainAssert.link(facebook, "facebook"))
            .instagram(instagram == null || instagram.isBlank() ? null : DomainAssert.link(instagram, "instagram"))
            .twitter(twitter == null || twitter.isBlank() ? null : DomainAssert.link(twitter, "twitter"))
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
            .id(DomainAssert.notNull(id, "id"))
            .nombre(DomainAssert.lengthBetween(nombre, "nombre", 1, 200))
            .sector(sector)
            .telefono(telefono)
            .paginaWeb(paginaWeb == null || paginaWeb.isBlank() ? null : DomainAssert.link(paginaWeb, "paginaWeb"))
            .facebook(facebook == null || facebook.isBlank() ? null : DomainAssert.link(facebook, "facebook"))
            .instagram(instagram == null || instagram.isBlank() ? null : DomainAssert.link(instagram, "instagram"))
            .twitter(twitter == null || twitter.isBlank() ? null : DomainAssert.link(twitter, "twitter"))
            .estadoRelacion(estadoRelacion)
            .responsableId(responsableId)
            .creadoPor(creadoPor)
            .notas(notas)
            .creadoEn(DomainAssert.notNull(creadoEn, "creadoEn"))
            .actualizadoEn(actualizadoEn)
            .build();
    }

    // ── Business Methods ─────────────────────────────────────────────────────

    /**
     * Changes the company's relationship state.
     *
     * <p>Rules (CLIENTE maps to enum value ACTIVO):
     * <ul>
     *   <li>{@code nuevoEstado} is required.</li>
     *   <li>Same-state change is idempotent (returns unchanged instance).</li>
     *   <li>ACTIVO → PROSPECTO is forbidden.</li>
     *   <li>INACTIVO → PROSPECTO is forbidden.</li>
     *   <li>Any → INACTIVO is forbidden when {@code tieneTratosActivos == true}.</li>
     * </ul>
     *
     * @param nuevoEstado       the target state (required)
     * @param tieneTratosActivos indicates whether the company has active deals
     * @return a new Empresa instance with updated estadoRelacion and actualizadoEn;
     *         or the same instance if the state is unchanged (idempotent)
     * @throws EmpresaStateTransitionException on invalid transitions
     */
    public Empresa cambiarEstadoRelacion(EstadoRelacion nuevoEstado, boolean tieneTratosActivos) {
        DomainAssert.notNull(nuevoEstado, "nuevoEstado");

        if (nuevoEstado == this.estadoRelacion) {
            return this;
        }

        // Rule: cannot go back to PROSPECTO once left
        if (nuevoEstado == EstadoRelacion.PROSPECTO && this.estadoRelacion != EstadoRelacion.PROSPECTO) {
            throw EmpresaStateTransitionException.transicionAProspectoNoPermitida(this.estadoRelacion.name());
        }

        // Rule: cannot mark as INACTIVO if there are active deals
        if (nuevoEstado == EstadoRelacion.INACTIVO && tieneTratosActivos) {
            throw EmpresaStateTransitionException.inactivoConTratosActivos();
        }

        return Empresa.builder()
                .id(this.id)
                .nombre(this.nombre)
                .sector(this.sector)
                .telefono(this.telefono)
                .paginaWeb(this.paginaWeb)
                .facebook(this.facebook)
                .instagram(this.instagram)
                .twitter(this.twitter)
                .estadoRelacion(nuevoEstado)
                .responsableId(this.responsableId)
                .creadoPor(this.creadoPor)
                .notas(this.notas)
                .creadoEn(this.creadoEn)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    }