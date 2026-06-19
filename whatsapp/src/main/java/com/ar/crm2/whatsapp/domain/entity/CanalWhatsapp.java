package com.ar.crm2.whatsapp.domain.entity;

import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.shared.DomainAssert;
import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import com.ar.crm2.whatsapp.domain.enums.ProveedorCanal;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "apiKey") // nunca exponer la API key de Evolution en logs
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CanalWhatsapp {

    @EqualsAndHashCode.Include
    private final CanalWhatsappId id;

    private final EmpresaId empresaId;
    private final String nombre;
    private final String instanceName;
    private final ProveedorCanal proveedor;
    private final EstadoCanal estado;
    private final String apiUrl;
    private final String apiKey;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    public static CanalWhatsapp create(
            EmpresaId empresaId,
            String nombre,
            String instanceName,
            ProveedorCanal proveedor,
            String apiUrl,
            String apiKey
    ) {
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notBlank(nombre, "nombre");
        DomainAssert.notBlank(instanceName, "instanceName");
        DomainAssert.notNull(proveedor, "proveedor");
        DomainAssert.notBlank(apiUrl, "apiUrl");
        DomainAssert.notBlank(apiKey, "apiKey");

        LocalDateTime now = LocalDateTime.now();
        return CanalWhatsapp.builder()
                .id(CanalWhatsappId.create())
                .empresaId(empresaId)
                .nombre(nombre)
                .instanceName(instanceName)
                .proveedor(proveedor)
                .estado(EstadoCanal.ACTIVO)
                .apiUrl(apiUrl)
                .apiKey(apiKey)
                .creadoEn(now)
                .actualizadoEn(now)
                .build();
    }

    public static CanalWhatsapp reconstitute(
            CanalWhatsappId id,
            EmpresaId empresaId,
            String nombre,
            String instanceName,
            ProveedorCanal proveedor,
            EstadoCanal estado,
            String apiUrl,
            String apiKey,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        return CanalWhatsapp.builder()
                .id(id)
                .empresaId(empresaId)
                .nombre(nombre)
                .instanceName(instanceName)
                .proveedor(proveedor)
                .estado(estado)
                .apiUrl(apiUrl)
                .apiKey(apiKey)
                .creadoEn(creadoEn)
                .actualizadoEn(actualizadoEn)
                .build();
    }

    public CanalWhatsapp cambiarEstado(EstadoCanal nuevoEstado) {
        DomainAssert.notNull(nuevoEstado, "estado");
        return CanalWhatsapp.builder()
                .id(this.id)
                .empresaId(this.empresaId)
                .nombre(this.nombre)
                .instanceName(this.instanceName)
                .proveedor(this.proveedor)
                .estado(nuevoEstado)
                .apiUrl(this.apiUrl)
                .apiKey(this.apiKey)
                .creadoEn(this.creadoEn)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    // apiUrl/apiKey vacíos = "no cambiar" — el front nunca recibe la apiKey real
    // del backend (no se expone por seguridad), así que no puede reenviarla tal
    // cual al editar solo el nombre del canal.
    public CanalWhatsapp editar(String nombre, String apiUrl, String apiKey) {
        DomainAssert.notBlank(nombre, "nombre");
        return CanalWhatsapp.builder()
                .id(this.id)
                .empresaId(this.empresaId)
                .nombre(nombre)
                .instanceName(this.instanceName)
                .proveedor(this.proveedor)
                .estado(this.estado)
                .apiUrl(apiUrl != null && !apiUrl.isBlank() ? apiUrl : this.apiUrl)
                .apiKey(apiKey != null && !apiKey.isBlank() ? apiKey : this.apiKey)
                .creadoEn(this.creadoEn)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    public CanalWhatsapp conectar() {
        return this.cambiarEstado(EstadoCanal.ACTIVO);
    }

    public CanalWhatsapp desconectar() {
        return this.cambiarEstado(EstadoCanal.DESCONECTADO);
    }
}
