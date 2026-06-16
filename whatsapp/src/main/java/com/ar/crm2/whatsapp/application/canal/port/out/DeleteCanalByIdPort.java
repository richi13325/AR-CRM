package com.ar.crm2.whatsapp.application.canal.port.out;

import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;

public interface DeleteCanalByIdPort {
    void deleteById(CanalWhatsappId id);
}
