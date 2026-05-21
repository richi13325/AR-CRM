package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.model.enums.EstadoRelacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity for contactos persistence.
 * Maps to the 'contactos' table with all documented columns.
 * Uses String id to match database convention (UUID stored as VARCHAR).
 */
@Entity
@Table(name = "contactos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "empresa_id")
    private String empresaId;

    @Column(name = "responsable_id")
    private String responsableId;

    @Column(name = "creado_por")
    private String creadoPor;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "como_nos_conocio", length = 200)
    private String comoNosConocio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_relacion")
    private EstadoRelacion estadoRelacion;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}