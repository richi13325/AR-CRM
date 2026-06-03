package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.RecordatorioEstado;
import com.ar.crm2.model.enums.TipoAgenda;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendaEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoAgenda tipo;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "tarea_id")
    private String tareaId;

    @Column(name = "trato_id")
    private String tratoId;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "link_videollamada", length = 500)
    private String linkVideollamada;

    @Column(name = "creado_por", nullable = false)
    private String creadoPor;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(name = "recordatorio_habilitado", nullable = false)
    private boolean recordatorioHabilitado;

    @Column(name = "minutos_antes")
    private Integer minutosAntes;

    @Enumerated(EnumType.STRING)
    @Column(name = "recordatorio_estado", length = 20)
    private RecordatorioEstado recordatorioEstado;

    @Column(name = "recordatorio_enviado_en")
    private LocalDateTime recordatorioEnviadoEn;

    @Column(name = "ultimo_intento_en")
    private LocalDateTime ultimoIntentoEn;
}
