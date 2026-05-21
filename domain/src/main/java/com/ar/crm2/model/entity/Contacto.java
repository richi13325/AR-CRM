package com.ar.crm2.model.entity;

import com.ar.crm2.exception.ContactoStateTransitionException;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.ContactoId;
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
 * Rich domain entity for Contacto.
 *
 * Identity: ContactoId (wraps UUID).
 * Equality: by id only (not full attribute equality).
 * No public setters — state changes go through business methods that preserve invariants.
 * Constructor is private; use static factory methods create() and reconstitute().
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Contacto {

    @EqualsAndHashCode.Include
    private final ContactoId id;

    private final EmpresaId empresaId;
    private final UsuarioId responsableId;
    private final UsuarioId creadoPor;
    private final String nombre;
    private final String correo;
    private final String telefono;
    private final String cargo;
    private final String comoNosConocio;
    private final EstadoRelacion estadoRelacion;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Contacto.
     * Generates id and timestamps internally.
     * Required fields: empresaId, nombre.
     * correo is optional; if provided it must be a valid email (max 150 chars).
     */
    public static Contacto create(
        EmpresaId empresaId,
        String nombre,
        String correo,
        EstadoRelacion estadoRelacion,
        UsuarioId responsableId,
        UsuarioId creadoPor,
        String telefono,
        String cargo,
        String comoNosConocio
    ) {
        return Contacto.builder()
            .id(ContactoId.create())
            .empresaId(DomainAssert.notNull(empresaId, "empresaId"))
            .nombre(DomainAssert.lengthBetween(nombre, "nombre", 1, 150))
            .correo(correo == null || correo.isBlank() ? null : DomainAssert.email(correo.strip(), "correo"))
            .estadoRelacion(DomainAssert.notNull(estadoRelacion, "estadoRelacion"))
            .responsableId(responsableId)
            .creadoPor(creadoPor)
            .telefono(telefono)
            .cargo(cargo)
            .comoNosConocio(comoNosConocio)
            .creadoEn(LocalDateTime.now())
            .build();
    }

    /**
     * Reconstitutes an existing Contacto from persistence.
     * correo is optional; if present it must be a valid email (max 150 chars).
     */
    public static Contacto reconstitute(
        ContactoId id,
        EmpresaId empresaId,
        UsuarioId responsableId,
        UsuarioId creadoPor,
        String nombre,
        String correo,
        String telefono,
        String cargo,
        String comoNosConocio,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        EstadoRelacion estadoRelacion
    ) {
        return Contacto.builder()
            .id(DomainAssert.notNull(id, "id"))
            .empresaId(DomainAssert.notNull(empresaId, "empresaId"))
            .nombre(DomainAssert.lengthBetween(nombre, "nombre", 1, 150))
            .correo(correo == null || correo.isBlank() ? null : DomainAssert.email(correo.strip(), "correo"))
            .responsableId(responsableId)
            .creadoPor(creadoPor)
            .telefono(telefono)
            .cargo(cargo)
            .comoNosConocio(comoNosConocio)
            .creadoEn(DomainAssert.notNull(creadoEn, "creadoEn"))
            .actualizadoEn(actualizadoEn)
            .estadoRelacion(DomainAssert.notNull(estadoRelacion, "estadoRelacion"))
            .build();
    }

    // ── Business Methods ─────────────────────────────────────────────────────

    /**
     * Changes the contact's relationship state.
     *
     * <p>Rules:
     * <ul>
     *   <li>{@code nuevoEstado} is required.</li>
     *   <li>Same-state change is idempotent (returns unchanged instance).</li>
     *   <li>ACTIVO → PROSPECTO is forbidden.</li>
     *   <li>INACTIVO → PROSPECTO is forbidden.</li>
     *   <li>Any → INACTIVO is forbidden when {@code tieneTratosActivos == true}.</li>
     * </ul>
     *
     * @param nuevoEstado       the target state (required)
     * @param tieneTratosActivos indicates whether the contact has active deals
     * @return a new Contacto instance with updated estadoRelacion and actualizadoEn;
     *         or the same instance if the state is unchanged (idempotent)
     * @throws ContactoStateTransitionException on invalid transitions
     */
    public Contacto cambiarEstadoRelacion(EstadoRelacion nuevoEstado, boolean tieneTratosActivos) {
        DomainAssert.notNull(nuevoEstado, "nuevoEstado");

        if (nuevoEstado == this.estadoRelacion) {
            return this;
        }

        // Rule: cannot go back to PROSPECTO once left
        if (nuevoEstado == EstadoRelacion.PROSPECTO && this.estadoRelacion != EstadoRelacion.PROSPECTO) {
            throw ContactoStateTransitionException.transicionAProspectoNoPermitida(this.estadoRelacion.name());
        }

        // Rule: cannot mark as INACTIVO if there are active deals
        if (nuevoEstado == EstadoRelacion.INACTIVO && tieneTratosActivos) {
            throw ContactoStateTransitionException.inactivoConTratosActivos();
        }

        return Contacto.builder()
                .id(this.id)
                .empresaId(this.empresaId)
                .responsableId(this.responsableId)
                .creadoPor(this.creadoPor)
                .nombre(this.nombre)
                .correo(this.correo)
                .telefono(this.telefono)
                .cargo(this.cargo)
                .comoNosConocio(this.comoNosConocio)
                .estadoRelacion(nuevoEstado)
                .creadoEn(this.creadoEn)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }
}
