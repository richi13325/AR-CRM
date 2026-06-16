package com.ar.crm2.whatsapp.application.conversacion.port.out;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;

import java.util.Optional;

public interface FindConversacionByTelefonoYCanalPort {
    Optional<Conversacion> findByTelefonoAndCanal(String numeroTelefono, CanalWhatsappId canalId);
}
