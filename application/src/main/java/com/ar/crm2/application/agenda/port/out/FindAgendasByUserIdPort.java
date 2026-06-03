package com.ar.crm2.application.agenda.port.out;

import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.List;

public interface FindAgendasByUserIdPort {
    List<Agenda> findByCreadoPor(UsuarioId usuarioId);
}
