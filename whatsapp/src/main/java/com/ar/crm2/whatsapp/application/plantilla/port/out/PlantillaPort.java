package com.ar.crm2.whatsapp.application.plantilla.port.out;

import com.ar.crm2.whatsapp.domain.entity.Plantilla;

import java.util.List;
import java.util.UUID;

public interface PlantillaPort {
    List<Plantilla> findAll();
    Plantilla save(Plantilla plantilla);
    void deleteById(UUID id);
}
