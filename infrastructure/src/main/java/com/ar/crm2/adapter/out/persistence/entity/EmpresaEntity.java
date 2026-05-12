package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.EstadoRelacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for Empresa persistence.
 * Maps to the 'empresas' table with all documented columns.
 */
@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "sector")
    private String sector;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "pagina_web")
    private String paginaWeb;

    @Column(name = "facebook")
    private String facebook;

    @Column(name = "instagram")
    private String instagram;

    @Column(name = "twitter")
    private String twitter;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_relacion")
    private EstadoRelacion estadoRelacion;

    @Column(name = "responsable_id")
    private UUID responsableId;

    @Column(name = "creado_por")
    private UUID creadoPor;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}