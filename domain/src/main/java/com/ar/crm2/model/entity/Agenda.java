package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.RecordatorioEstado;
import com.ar.crm2.model.enums.TipoAgenda;
import com.ar.crm2.model.vo.AgendaId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Agenda {

    @EqualsAndHashCode.Include
    private final AgendaId id;

    private final TipoAgenda tipo;
    private final String asunto;
    private final String descripcion;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final TareaId tareaId;
    private final TratoId tratoId;
    private final String ubicacion;
    private final String linkVideollamada;
    private final UsuarioId creadoPor;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    private final boolean recordatorioHabilitado;
    private final Integer minutosAntes;
    private final RecordatorioEstado recordatorioEstado;
    private final LocalDateTime recordatorioEnviadoEn;
    private final LocalDateTime ultimoIntentoEn;

    public static Agenda create(
        TipoAgenda tipo,
        String asunto,
        String descripcion,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        TareaId tareaId,
        TratoId tratoId,
        String ubicacion,
        String linkVideollamada,
        UsuarioId creadoPor,
        boolean recordatorioHabilitado,
        Integer minutosAntes
    ) {
        LocalDateTime now = LocalDateTime.now();

        if (horaFin != null && horaInicio != null && horaFin.isBefore(horaInicio)) {
            throw new IllegalArgumentException("horaFin must be after horaInicio");
        }

        if (recordatorioHabilitado) {
            DomainAssert.notNull(minutosAntes, "minutosAntes");
            if (minutosAntes <= 0) {
                throw new IllegalArgumentException("minutosAntes must be greater than 0 when reminder is enabled");
            }
        }

        DomainAssert.notNull(tipo, "tipo");
        DomainAssert.notNull(fecha, "fecha");
        DomainAssert.notNull(horaInicio, "horaInicio");
        DomainAssert.notNull(creadoPor, "creadoPor");
        DomainAssert.lengthBetween(asunto, "asunto", 1, 200);

        String resolvedDescripcion = normalizeOptionalText(descripcion, 1000, "descripcion");
        String resolvedUbicacion = normalizeOptionalText(ubicacion, 200, "ubicacion");
        String resolvedLinkVideollamada = normalizeOptionalText(linkVideollamada, 500, "linkVideollamada");

        return new Agenda(
            AgendaId.create(),
            tipo,
            asunto.trim(),
            resolvedDescripcion,
            fecha,
            horaInicio,
            horaFin,
            tareaId,
            tratoId,
            resolvedUbicacion,
            resolvedLinkVideollamada,
            creadoPor,
            now,
            now,
            recordatorioHabilitado,
            recordatorioHabilitado ? minutosAntes : null,
            recordatorioHabilitado ? RecordatorioEstado.PENDIENTE : null,
            null,
            null
        );
    }

    public static Agenda reconstitute(
        AgendaId id,
        TipoAgenda tipo,
        String asunto,
        String descripcion,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        TareaId tareaId,
        TratoId tratoId,
        String ubicacion,
        String linkVideollamada,
        UsuarioId creadoPor,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        boolean recordatorioHabilitado,
        Integer minutosAntes,
        RecordatorioEstado recordatorioEstado,
        LocalDateTime recordatorioEnviadoEn,
        LocalDateTime ultimoIntentoEn
    ) {
        if (horaFin != null && horaInicio != null && horaFin.isBefore(horaInicio)) {
            throw new IllegalArgumentException("horaFin must be after horaInicio");
        }

        if (recordatorioHabilitado) {
            DomainAssert.notNull(minutosAntes, "minutosAntes");
            if (minutosAntes <= 0) {
                throw new IllegalArgumentException("minutosAntes must be greater than 0 when reminder is enabled");
            }
        }

        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(tipo, "tipo");
        DomainAssert.notNull(fecha, "fecha");
        DomainAssert.notNull(horaInicio, "horaInicio");
        DomainAssert.notNull(creadoPor, "creadoPor");
        DomainAssert.notNull(creadoEn, "creadoEn");
        DomainAssert.notNull(actualizadoEn, "actualizadoEn");
        DomainAssert.lengthBetween(asunto, "asunto", 1, 200);

        String resolvedDescripcion = normalizeOptionalText(descripcion, 1000, "descripcion");
        String resolvedUbicacion = normalizeOptionalText(ubicacion, 200, "ubicacion");
        String resolvedLinkVideollamada = normalizeOptionalText(linkVideollamada, 500, "linkVideollamada");

        return new Agenda(
            id,
            tipo,
            asunto.trim(),
            resolvedDescripcion,
            fecha,
            horaInicio,
            horaFin,
            tareaId,
            tratoId,
            resolvedUbicacion,
            resolvedLinkVideollamada,
            creadoPor,
            creadoEn,
            actualizadoEn,
            recordatorioHabilitado,
            recordatorioHabilitado ? minutosAntes : null,
            recordatorioHabilitado ? recordatorioEstado : null,
            recordatorioHabilitado && recordatorioEstado == RecordatorioEstado.ENVIADO ? recordatorioEnviadoEn : null,
            recordatorioHabilitado ? ultimoIntentoEn : null
        );
    }

    public boolean isRecordatorioHabilitado() {
        return recordatorioHabilitado;
    }

    public boolean isRecordatorioPendiente() {
        return recordatorioHabilitado && recordatorioEstado == RecordatorioEstado.PENDIENTE;
    }

    public boolean isRecordatorioFallido() {
        return recordatorioHabilitado && recordatorioEstado == RecordatorioEstado.FALLIDO;
    }

    public LocalDateTime getRecordatorioDueTime() {
        if (!recordatorioHabilitado || minutosAntes == null) {
            return null;
        }
        return LocalDateTime.of(fecha, horaInicio).minus(minutosAntes, ChronoUnit.MINUTES);
    }

    public boolean isRecordatorioDue() {
        if (!recordatorioHabilitado) {
            return false;
        }
        LocalDateTime dueTime = getRecordatorioDueTime();
        return dueTime != null && !LocalDateTime.now().isBefore(dueTime);
    }

    public Agenda withRecordatorioEnviado() {
        LocalDateTime now = LocalDateTime.now();
        return new Agenda(
            id, tipo, asunto, descripcion, fecha, horaInicio, horaFin,
            tareaId, tratoId, ubicacion, linkVideollamada,
            creadoPor, creadoEn, now,
            recordatorioHabilitado, minutosAntes, RecordatorioEstado.ENVIADO,
            now, now
        );
    }

    public Agenda withRecordatorioFallido() {
        LocalDateTime now = LocalDateTime.now();
        return new Agenda(
            id, tipo, asunto, descripcion, fecha, horaInicio, horaFin,
            tareaId, tratoId, ubicacion, linkVideollamada,
            creadoPor, creadoEn, now,
            recordatorioHabilitado, minutosAntes, RecordatorioEstado.FALLIDO,
            recordatorioEnviadoEn, now
        );
    }

    // ── Internal normalization helpers ──────────────────────────────

    /**
     * Normalizes an optional text field: null/blank becomes null,
     * otherwise validates the length (max 500 by default) and returns the trimmed value.
     */
    private static String normalizeOptionalText(String value, int maxLen, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        DomainAssert.optionalLength(value, maxLen, fieldName);
        return value.trim();
    }
}
