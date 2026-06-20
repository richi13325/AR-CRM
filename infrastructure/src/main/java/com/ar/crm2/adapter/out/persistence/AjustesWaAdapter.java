package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.AjustesWaEntity;
import com.ar.crm2.adapter.out.persistence.repository.AjustesWaRepository;
import com.ar.crm2.whatsapp.application.ajustes.port.out.AjustesWaPort;
import com.ar.crm2.whatsapp.domain.entity.AjustesWa;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AjustesWaAdapter implements AjustesWaPort {

    private static final String SINGLETON_ID = "default";

    private final AjustesWaRepository repository;

    @Override
    public AjustesWa get() {
        return repository.findById(SINGLETON_ID).map(this::toDomain).orElseGet(AjustesWa::defaults);
    }

    @Override
    public AjustesWa save(AjustesWa a) {
        AjustesWaEntity entity = AjustesWaEntity.builder()
                .id(SINGLETON_ID)
                .autoAsignar(a.autoAsignar())
                .bienvenidaActiva(a.bienvenidaActiva())
                .bienvenidaTexto(a.bienvenidaTexto())
                .horarioActivo(a.horarioActivo())
                .horarioInicio(a.horarioInicio())
                .horarioFin(a.horarioFin())
                .horarioDias(a.horarioDias())
                .fueraHorarioTexto(a.fueraHorarioTexto())
                .csatActivo(a.csatActivo())
                .csatTexto(a.csatTexto())
                .build();
        return toDomain(repository.save(entity));
    }

    private AjustesWa toDomain(AjustesWaEntity e) {
        return new AjustesWa(
                e.isAutoAsignar(), e.isBienvenidaActiva(), e.getBienvenidaTexto(),
                e.isHorarioActivo(), e.getHorarioInicio(), e.getHorarioFin(), e.getHorarioDias(),
                e.getFueraHorarioTexto(), e.isCsatActivo(),
                e.getCsatTexto() != null ? e.getCsatTexto() : AjustesWa.defaults().csatTexto());
    }
}
