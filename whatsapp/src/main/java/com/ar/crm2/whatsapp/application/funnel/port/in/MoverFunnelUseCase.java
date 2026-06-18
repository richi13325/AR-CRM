package com.ar.crm2.whatsapp.application.funnel.port.in;

import java.util.UUID;

public interface MoverFunnelUseCase {
    /** @return true si se movió la oportunidad asociada a la conversación; false si no tiene una activa. */
    boolean mover(UUID conversacionId, String nombreEtapa);
}
