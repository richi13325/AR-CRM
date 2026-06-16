package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.whatsapp.application.canal.port.in.DeleteCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.DeleteCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteCanalService implements DeleteCanalUseCase {

    private final FindCanalByIdPort findPort;
    private final DeleteCanalByIdPort deletePort;

    @Override
    public void delete(UUID canalId) {
        CanalWhatsappId id = CanalWhatsappId.from(canalId);
        findPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + canalId));
        deletePort.deleteById(id);
    }
}
