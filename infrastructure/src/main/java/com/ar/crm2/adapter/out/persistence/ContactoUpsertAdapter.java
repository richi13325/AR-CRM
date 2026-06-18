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
                .map(entity -> ContactoId.from(UUID.fromString(entity.getId())))
                .orElseGet(() -> crearContacto(empresaId, telefono, nombre));
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
