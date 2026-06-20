package com.ar.crm2.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wa_ajustes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjustesWaEntity {

    @Id
    @Column(name = "id")
    private String id;   // fila única: "default"

    @Column(name = "auto_asignar", nullable = false)
    private boolean autoAsignar;

    @Column(name = "bienvenida_activa", nullable = false)
    private boolean bienvenidaActiva;

    @Column(name = "bienvenida_texto", length = 2000)
    private String bienvenidaTexto;

    @Column(name = "horario_activo", nullable = false)
    private boolean horarioActivo;

    @Column(name = "horario_inicio", length = 5)
    private String horarioInicio;

    @Column(name = "horario_fin", length = 5)
    private String horarioFin;

    @Column(name = "horario_dias", length = 30)
    private String horarioDias;

    @Column(name = "fuera_horario_texto", length = 2000)
    private String fueraHorarioTexto;

    @Column(name = "csat_activo", nullable = false)
    private boolean csatActivo;

    @Column(name = "csat_texto", length = 2000)
    private String csatTexto;
}
