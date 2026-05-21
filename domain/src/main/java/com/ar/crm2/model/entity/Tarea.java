package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;
import com.ar.crm2.model.vo.TareaId;
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
 * Rich domain entity for Tarea (Task/Deal Task).
 *
 * Identity: TareaId (wraps UUID).
 * Equality: by id only (not full attribute equality).
 * No public setters — state changes go through business methods that preserve invariants.
 * Constructor is private; use static factory methods create() and reconstitute().
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tarea {

    @EqualsAndHashCode.Include
    private final TareaId id;

    private final TratoId tratoId;
    private final UsuarioId responsableId;
    private final String titulo;
    private final String descripcion;
    private final TipoTarea tipo;
    private final PrioridadTarea prioridad;
    private final LocalDateTime fechaLimite;
    private final LocalDateTime fechaCompletada;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Tarea.
     * Generates id and timestamps internally.
     */
    public static Tarea create(
        TratoId tratoId,
        UsuarioId responsableId,
        String titulo,
        String descripcion,
        TipoTarea tipo,
        PrioridadTarea prioridad,
        LocalDateTime fechaLimite
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new Tarea(
            TareaId.create(),
            DomainAssert.notNull(tratoId, "tratoId"),
            DomainAssert.notNull(responsableId, "responsableId"),
            DomainAssert.lengthBetween(titulo, "titulo", 1, 200),
            descripcion, // nullable
            DomainAssert.notNull(tipo, "tipo"),
            DomainAssert.notNull(prioridad, "prioridad"),
            DomainAssert.notNull(fechaLimite, "fechaLimite"),
            null, // fechaCompletada — nullable, set when task is completed
            now,
            now
        );
    }

    /**
     * Reconstitutes an existing Tarea from persistence.
     */
    public static Tarea reconstitute(
        TareaId id,
        TratoId tratoId,
        UsuarioId responsableId,
        String titulo,
        String descripcion,
        TipoTarea tipo,
        PrioridadTarea prioridad,
        LocalDateTime fechaLimite,
        LocalDateTime fechaCompletada,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
    ) {
        return new Tarea(
            DomainAssert.notNull(id, "id"),
            DomainAssert.notNull(tratoId, "tratoId"),
            DomainAssert.notNull(responsableId, "responsableId"),
            DomainAssert.lengthBetween(titulo, "titulo", 1, 200),
            descripcion, // nullable
            DomainAssert.notNull(tipo, "tipo"),
            DomainAssert.notNull(prioridad, "prioridad"),
            DomainAssert.notNull(fechaLimite, "fechaLimite"),
            fechaCompletada, // nullable
            DomainAssert.notNull(creadoEn, "creadoEn"),
            DomainAssert.notNull(actualizadoEn, "actualizadoEn")
        );
    }

    }