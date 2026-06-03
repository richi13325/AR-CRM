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

        return new Agenda(
            AgendaId.create(),
            DomainAssert.notNull(tipo, "tipo"),
            DomainAssert.lengthBetween(asunto, "asunto", 1, 200),
            DomainAssert.optionalLength(descripcion, 1000, "descripcion"),
            DomainAssert.notNull(fecha, "fecha"),
            DomainAssert.notNull(horaInicio, "horaInicio"),
            horaFin,
            tareaId,
            tratoId,
            DomainAssert.optionalLength(ubicacion, 200, "ubicacion"),
            DomainAssert.optionalLength(linkVideollamada, 500, "linkVideollamada"),
            DomainAssert.notNull(creadoPor, "creadoPor"),
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

        return new Agenda(
            DomainAssert.notNull(id, "id"),
            DomainAssert.notNull(tipo, "tipo"),
            DomainAssert.lengthBetween(asunto, "asunto", 1, 200),
            DomainAssert.optionalLength(descripcion, 1000, "descripcion"),
            DomainAssert.notNull(fecha, "fecha"),
            DomainAssert.notNull(horaInicio, "horaInicio"),
            horaFin,
            tareaId,
            tratoId,
            DomainAssert.optionalLength(ubicacion, 200, "ubicacion"),
            DomainAssert.optionalLength(linkVideollamada, 500, "linkVideollamada"),
            DomainAssert.notNull(creadoPor, "creadoPor"),
            DomainAssert.notNull(creadoEn, "creadoEn"),
            DomainAssert.notNull(actualizadoEn, "actualizadoEn"),
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
}
