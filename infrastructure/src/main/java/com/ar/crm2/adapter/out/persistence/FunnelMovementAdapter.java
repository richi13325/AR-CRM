package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.adapter.out.persistence.entity.ColumnaTableroEntity;
import com.ar.crm2.adapter.out.persistence.entity.FichaEntity;
import com.ar.crm2.adapter.out.persistence.entity.TableroEntity;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.adapter.out.persistence.repository.TratoRepository;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.whatsapp.application.funnel.port.out.MoverContactoAEtapaPort;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Mueve la oportunidad (Trato) de un contacto a la columna del tablero de TRATOS
 * cuyo nombre coincide con la etapa solicitada por el bot (tool actualizar_funnel de n8n).
 *
 * <p>Limitación conocida: a diferencia de AmbarCRM, este dominio no tiene un Trato/Ficha
 * por defecto al crear un contacto, ni columnas tipadas como ganado/perdido. Si el contacto
 * no tiene una oportunidad activa, no se crea una automáticamente — se devuelve false.
 */
@RequiredArgsConstructor
public class FunnelMovementAdapter implements MoverContactoAEtapaPort {

    private final TratoRepository tratoRepository;
    private final FichaRepository fichaRepository;
    private final TableroRepository tableroRepository;
    private final ColumnaRepository columnaRepository;

    @Override
    public boolean mover(ContactoId contactoId, String nombreEtapa) {
        if (nombreEtapa == null || nombreEtapa.isBlank()) return false;

        var trato = tratoRepository.findFirstByContactoIdOrderByCreadoEnDesc(contactoId.value().toString())
                .orElse(null);
        if (trato == null) return false;

        FichaEntity ficha = fichaRepository.findByTratoId(trato.getId()).orElse(null);
        if (ficha == null) return false;

        String columnaId = resolverColumnaId(nombreEtapa);
        if (columnaId == null) return false;

        ficha.setColumnaId(columnaId);
        ficha.setActualizadoEn(Instant.now());
        fichaRepository.save(ficha);
        return true;
    }

    private String resolverColumnaId(String nombreEtapa) {
        TableroEntity tablero = tableroRepository.findFirstByTipoTablero(TipoTablero.TRATOS).orElse(null);
        if (tablero == null) return null;

        for (ColumnaTableroEntity ct : tablero.getColumnasTablero()) {
            ColumnaEntity columna = columnaRepository.findById(ct.getColumnaId()).orElse(null);
            if (columna != null && columna.getColumnaNombre().equalsIgnoreCase(nombreEtapa.trim())) {
                return columna.getId();
            }
        }
        return null;
    }
}
