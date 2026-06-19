package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.whatsapp.application.canal.port.in.ReconfigurarWebhookUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.EvolutionConectarPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ReconfigurarWebhookService implements ReconfigurarWebhookUseCase {

    private final FindCanalByIdPort findPort;
    private final EvolutionConectarPort evolutionPort;

    @Override
    public void reconfigurar(UUID canalId) {
        CanalWhatsapp canal = findPort.findById(CanalWhatsappId.from(canalId))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + canalId));

        evolutionPort.configurarWebhook(
                canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(), canalId);
    }
}
