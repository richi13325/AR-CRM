package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.whatsapp.application.ajustes.port.out.AjustesWaPort;
import com.ar.crm2.whatsapp.application.canal.port.out.FindCanalByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.in.CerrarConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.application.mensaje.port.out.SendWhatsappMessagePort;
import com.ar.crm2.whatsapp.domain.entity.AjustesWa;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.enums.EstadoConversacion;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CerrarConversacionService implements CerrarConversacionUseCase {

    // Texto fijo de la encuesta (AjustesWa no lo configura aún; mismo wording que AmbarCRM).
    private static final String CSAT_TEXTO =
            "¿Cómo calificarías nuestra atención del 1 (mala) al 5 (excelente)?";

    private final FindConversacionByIdPort findPort;
    private final SaveConversacionPort savePort;
    private final AjustesWaPort ajustesPort;
    private final FindCanalByIdPort findCanalPort;
    private final SendWhatsappMessagePort sendWhatsappPort;

    @Override
    public Conversacion cerrar(UUID conversacionId) {
        Conversacion conversacion = findPort.findById(ConversacionId.from(conversacionId))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + conversacionId));

        Conversacion cerrada = conversacion.cambiarEstado(EstadoConversacion.CERRADA);

        // CSAT: al cerrar, si está activo y no se mandó antes, enviamos la encuesta.
        AjustesWa aj = ajustesPort.get();
        if (aj.csatActivo() && cerrada.getCsatEnviadoEn() == null && cerrada.getCsatScore() == null) {
            CanalWhatsapp canal = findCanalPort.findById(cerrada.getCanalId()).orElse(null);
            if (canal != null) {
                try {
                    sendWhatsappPort.send(canal, cerrada.getNumeroTelefono(), TipoMensaje.TEXTO, CSAT_TEXTO, null);
                    cerrada = cerrada.marcarCsatEnviado();
                } catch (Exception ignored) {
                    // No bloquear el cierre por un fallo de envío de la encuesta.
                }
            }
        }

        return savePort.save(cerrada);
    }
}
