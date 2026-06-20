package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.TipoNota;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "nota_trato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaTratoEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "trato_id", nullable = false)
    private String tratoId;

    @Column(name = "autor_id")
    private String autorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNota tipo;

    @Column(name = "contenido", nullable = false, length = 2000)
    private String contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
