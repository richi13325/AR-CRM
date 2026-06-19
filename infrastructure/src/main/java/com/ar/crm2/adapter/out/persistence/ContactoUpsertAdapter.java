package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ContactoEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ContactoMapper;
import com.ar.crm2.adapter.out.persistence.repository.ContactoRepository;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.whatsapp.application.canal.port.out.UpsertContactoPort;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ContactoUpsertAdapter implements UpsertContactoPort {

    private final ContactoRepository repository;

    @Override
    public ContactoId upsertPorTelefono(EmpresaId empresaId, String telefono, String nombre) {
        if (telefono == null || telefono.isBlank()) return null;

        return repository.findByEmpresaIdAndTelefono(empresaId.value().toString(), telefono)
                .map(entity -> {
                    actualizarNombreSiMejora(entity, telefono, nombre);
                    return ContactoId.from(UUID.fromString(entity.getId()));
                })
                .orElseGet(() -> crearContacto(empresaId, telefono, nombre));
    }

    // Si el contacto existente solo tenía el teléfono como nombre (o ninguno) y
    // ahora llega un pushName real, lo completamos. Igual que AmbarCRM
    // (importar/route.ts): el contacto "se enriquece" con el nombre cuando llega.
    private void actualizarNombreSiMejora(ContactoEntity entity, String telefono, String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.matches("\\d+")) return;
        String actual = entity.getNombre();
        boolean sinNombreReal = actual == null || actual.isBlank() || actual.equals(telefono);
        if (sinNombreReal && !nombre.equals(actual)) {
            entity.setNombre(nombre);
            repository.save(entity);
        }
    }

    private ContactoId crearContacto(EmpresaId empresaId, String telefono, String nombre) {
        Contacto nuevo = Contacto.create(
                empresaId,
                (nombre == null || nombre.isBlank()) ? telefono : nombre,
                null,
                EstadoRelacion.PROSPECTO,
                null,
                null,
                telefono,
                null,
                "whatsapp"
        );
        ContactoEntity guardado = repository.save(ContactoMapper.toEntity(nuevo));
        return ContactoId.from(UUID.fromString(guardado.getId()));
    }
}
