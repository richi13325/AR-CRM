package com.ar.crm2.whatsapp.application.canal.service;

import com.ar.crm2.whatsapp.application.canal.command.ConectarCanalCommand;
import com.ar.crm2.whatsapp.application.canal.port.in.ConectarCanalResult;
import com.ar.crm2.whatsapp.application.canal.port.in.ConectarCanalUseCase;
import com.ar.crm2.whatsapp.application.canal.port.out.EvolutionConectarPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.canal.port.out.SaveCanalPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.enums.EstadoCanal;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConectarCanalService implements ConectarCanalUseCase {

    private final FindCanalByIdPort findPort;
    private final SaveCanalPort savePort;
    private final EvolutionConectarPort evolutionPort;

    @Override
    public ConectarCanalResult conectar(ConectarCanalCommand command) {
        CanalWhatsapp canal = findPort.findById(CanalWhatsappId.from(command.canalId()))
                .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + command.canalId()));

        String qrBase64 = evolutionPort.conectarInstancia(
                canal.getApiUrl(),
                canal.getApiKey(),
                canal.getInstanceName()
        );

        String estadoEvolution = evolutionPort.consultarEstado(
                canal.getApiUrl(),
                canal.getApiKey(),
                canal.getInstanceName()
        );

        EstadoCanal nuevoEstado = mapEstado(estadoEvolution);

        // Si Evolution no entrego QR y el canal tampoco quedo ACTIVO, no hay nada que
        // mostrarle al usuario: fallar explicito en vez de devolver 200 con qrBase64 null,
        // que deja el dialog de conexion girando para siempre sin error visible.
        if (qrBase64 == null && nuevoEstado != EstadoCanal.ACTIVO) {
            throw new IllegalStateException(
                    "No se pudo obtener el codigo QR de Evolution API. Intenta de nuevo en unos segundos.");
        }

        CanalWhatsapp actualizado = canal.cambiarEstado(nuevoEstado);
        savePort.save(actualizado);

        // Best-effort: deja a Evolution mandando los mensajes entrantes en
        // tiempo real a nuestro webhook (no-op si no hay URL pública configurada).
        evolutionPort.configurarWebhook(canal.getApiUrl(), canal.getApiKey(), canal.getInstanceName(), command.canalId());

        return new ConectarCanalResult(qrBase64, nuevoEstado);
    }

    private EstadoCanal mapEstado(String evolutionState) {
        return switch (evolutionState) {
            case "open" -> EstadoCanal.ACTIVO;
            case "connecting" -> EstadoCanal.DESCONECTADO;
            default -> EstadoCanal.DESCONECTADO;
        };
    }
}
