package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.PlantillaEntity;
import com.ar.crm2.adapter.out.persistence.repository.PlantillaRepository;
import com.ar.crm2.whatsapp.application.plantilla.port.out.PlantillaPort;
import com.ar.crm2.whatsapp.domain.entity.Plantilla;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PlantillaAdapter implements PlantillaPort {

    private final PlantillaRepository repository;

    @Override
    public List<Plantilla> findAll() {
        return repository.findAllByOrderByTituloAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public Plantilla save(Plantilla p) {
        PlantillaEntity entity = PlantillaEntity.builder()
                .id(p.id().toString())
                .titulo(p.titulo())
                .contenido(p.contenido())
                .creadoEn(p.creadoEn())
                .build();
        return toDomain(repository.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id.toString());
    }

    private Plantilla toDomain(PlantillaEntity e) {
        return new Plantilla(UUID.fromString(e.getId()), e.getTitulo(), e.getContenido(), e.getCreadoEn());
    }
}
