package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ContactoEntity;
import com.ar.crm2.adapter.out.persistence.mapper.ContactoMapper;
import com.ar.crm2.adapter.out.persistence.repository.ContactoRepository;
import com.ar.crm2.application.contacto.port.out.DeleteContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.ExistsTratosByContactoIdPort;
import com.ar.crm2.application.contacto.port.out.FindAllContactosPort;
import com.ar.crm2.application.contacto.port.out.FindContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.SaveContactoPort;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.ContactoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing granular outbound ports.
 * Bridges application outbound port contracts to Spring Data JPA storage.
 */
@Repository
@RequiredArgsConstructor
public class ContactoRepositoryAdapter implements SaveContactoPort, FindAllContactosPort, FindContactoByIdPort, DeleteContactoByIdPort, ExistsTratosByContactoIdPort {

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
}