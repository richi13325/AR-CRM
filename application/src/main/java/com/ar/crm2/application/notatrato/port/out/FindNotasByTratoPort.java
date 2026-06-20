package com.ar.crm2.application.notatrato.port.out;

import com.ar.crm2.model.entity.NotaTrato;
import com.ar.crm2.model.vo.TratoId;

import java.util.List;

public interface FindNotasByTratoPort {
    /** Notas de un trato, más recientes primero. */
    List<NotaTrato> findByTrato(TratoId tratoId);
}
