package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.UsuarioEntity;
import com.ar.crm2.adapter.out.persistence.repository.ConversacionRepository;
import com.ar.crm2.adapter.out.persistence.repository.UsuarioRepository;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SugerirResponsablePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class SugerirResponsableAdapter implements SugerirResponsablePort {

    private final UsuarioRepository usuarioRepository;
    private final ConversacionRepository conversacionRepository;

    @Override
    public Optional<UsuarioId> sugerirMenosCargado() {
        List<UsuarioEntity> activos = usuarioRepository.findByActivoTrue();
        if (activos.isEmpty()) return Optional.empty();

        UsuarioEntity mejor = null;
        long min = Long.MAX_VALUE;
        for (UsuarioEntity u : activos) {
            long carga = conversacionRepository.countByAsignadoA(u.getId());
            if (carga < min) {
                min = carga;
                mejor = u;
            }
        }
        return mejor != null ? Optional.of(UsuarioId.from(UUID.fromString(mejor.getId()))) : Optional.empty();
    }
}
