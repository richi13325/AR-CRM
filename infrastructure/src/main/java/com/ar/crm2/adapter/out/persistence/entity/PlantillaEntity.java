package com.ar.crm2.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wa_plantilla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "contenido", nullable = false, length = 2000)
    private String contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
