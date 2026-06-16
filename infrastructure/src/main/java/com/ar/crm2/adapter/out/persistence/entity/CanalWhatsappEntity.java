package com.ar.crm2.adapter.out.persistence.entity;

import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import com.ar.crm2.whatsapp.domain.enums.ProveedorCanal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wa_canal_whatsapp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanalWhatsappEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "empresa_id", nullable = false)
    private String empresaId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "instance_name", nullable = false, length = 100)
    private String instanceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor", nullable = false, length = 30)
    private ProveedorCanal proveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoCanal estado;

    @Column(name = "api_url", nullable = false, length = 500)
    private String apiUrl;

    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
