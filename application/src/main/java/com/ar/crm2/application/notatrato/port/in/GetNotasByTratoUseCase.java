package com.ar.crm2.application.notatrato.port.in;

import com.ar.crm2.model.entity.NotaTrato;

import java.util.List;
import java.util.UUID;

public interface GetNotasByTratoUseCase {
    List<NotaTrato> getByTrato(UUID tratoId);
}
