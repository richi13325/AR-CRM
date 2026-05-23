package com.ar.crm2.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for SuperUsuario persistence.
 * Maps to the 'super_usuarios' table with all documented columns.
 * Uses String id to match database convention (UUID stored as VARCHAR).
 */
@Entity
@Table(name = "super_usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class SuperUsuarioEntity {

    @jakarta.persistence.Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "correo", nullable = false, length = 120)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "creado_en", nullable = false)
    private java.time.LocalDateTime creadoEn;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "keycloak_id", nullable = true, length = 255)
    private String keycloakId;
}