package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ContactoEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ContactoMapper;
import com.ar.crm2.adapter.out.persistence.repository.ContactoRepository;
import com.ar.crm2.application.ai.port.out.ContactoLecturaPort;
import com.ar.crm2.application.contacto.port.out.DeleteContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.ExistsTratosByContactoIdPort;
import com.ar.crm2.application.contacto.port.out.FindAllContactosPort;
import com.ar.crm2.application.contacto.port.out.FindContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.SaveContactoPort;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * Single persistence adapter that satisfies every Contacto outbound
 * port, including the AI read port ({@link ContactoLecturaPort}). The
 * {@code findById(ContactoId)} method has the same signature for the
 * CRM and AI read ports, so this adapter is the only implementation
 * needed (no separate bridge class).
 *
 * <p>Tenant authorization is enforced by the AI application service
 * that calls {@link ContactoLecturaPort}; this adapter is
 * tenant-blind. The {@code (empresaId, telefono)} lookup delegates
 * to the existing Spring Data query
 * {@code ContactoRepository.findByEmpresaIdAndTelefono(String, String)}
 * that the WhatsApp directory-sync flow already uses.
 */
@RequiredArgsConstructor
public class ContactoRepositoryAdapter
        implements SaveContactoPort, FindAllContactosPort, FindContactoByIdPort,
                   DeleteContactoByIdPort, ExistsTratosByContactoIdPort,
                   ContactoLecturaPort {

    private final ContactoRepository repository;

    @Override
    public Contacto save(Contacto contacto) {
        ContactoEntity entity = ContactoMapper.toEntity(contacto);
        ContactoEntity saved = repository.save(entity);
        return ContactoMapper.toDomain(saved);
    }

    @Override
    public List<Contacto> findAll() {
        return repository.findAll().stream()
            .map(ContactoMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Contacto> findById(ContactoId id) {
        return repository.findById(id.value().toString())
            .map(ContactoMapper::toDomain);
    }

    @Override
    public void deleteById(ContactoId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public boolean existsTratosByContactoId(ContactoId contactoId) {
        return repository.existsTratosByContactoId(contactoId.value().toString());
    }

    @Override
    public Optional<Contacto> findByEmpresaIdAndTelefono(EmpresaId empresaId, String telefono) {
        if (empresaId == null || telefono == null || telefono.isBlank()) {
            return Optional.empty();
        }
        return repository.findByEmpresaIdAndTelefono(
                empresaId.value().toString(), telefono
            )
            .map(ContactoMapper::toDomain);
    }
}
