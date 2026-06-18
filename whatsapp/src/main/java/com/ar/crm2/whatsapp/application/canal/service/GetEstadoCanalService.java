package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.whatsapp.application.canal.port.in.GetEstadoCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.EvolutionConectarPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.SaveCanalPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetEstadoCanalService implements GetEstadoCanalUseCase {

    private final FindCanalByIdPort findPort;
    private final SaveCanalPort savePort;
    private final EvolutionConectarPort evolutionPort;

    @Override
    public EstadoCanal getEstado(UUID canalId) {
        CanalWhatsapp canal = findPort.findById(CanalWhatsappId.from(canalId))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + canalId));

        String evolutionState = evolutionPort.consultarEstado(
                canal.getApiUrl(),
                canal.getApiKey(),
                canal.getInstanceName()
        );

        EstadoCanal nuevoEstado = mapEstado(evolutionState);

        if (!nuevoEstado.equals(canal.getEstado())) {
            savePort.save(canal.cambiarEstado(nuevoEstado));
        }

        return nuevoEstado;
    }

    private EstadoCanal mapEstado(String evolutionState) {
        return switch (evolutionState) {
            case "open" -> EstadoCanal.ACTIVO;
            default -> EstadoCanal.DESCONECTADO;
        };
    }
}
