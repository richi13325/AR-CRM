package com.ar.crm2.application.notatrato.service;

import com.ar.crm2.application.notatrato.port.in.GetNotasByTratoUseCase;
import com.ar.crm2.application.notatrato.port.out.FindNotasByTratoPort;
import com.ar.crm2.model.entity.NotaTrato;
import com.ar.crm2.model.vo.TratoId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetNotasByTratoService implements GetNotasByTratoUseCase {

    private final FindNotasByTratoPort findPort;

    @Override
    public List<NotaTrato> getByTrato(UUID tratoId) {
        return findPort.findByTrato(TratoId.from(tratoId));
    }
}
