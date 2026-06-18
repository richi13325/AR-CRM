package com.ar.crm2.whatsapp.application.ajustes.port.out;

import com.ar.crm2.whatsapp.domain.entity.AjustesWa;

public interface AjustesWaPort {
    /** Devuelve los ajustes guardados, o los defaults si aún no existen. */
    AjustesWa get();
    AjustesWa save(AjustesWa ajustes);
}
