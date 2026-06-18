package com.ar.crm2.whatsapp.application.canal.port.in;

import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import java.util.UUID;

public interface GetEstadoCanalUseCase {
    EstadoCanal getEstado(UUID canalId);
}
