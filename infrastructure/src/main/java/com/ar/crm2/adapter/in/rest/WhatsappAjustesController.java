package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.whatsapp.application.ajustes.port.out.AjustesWaPort;
import com.ar.crm2.whatsapp.domain.entity.AjustesWa;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wa/ajustes")
@RequiredArgsConstructor
public class WhatsappAjustesController {

    private final AjustesWaPort ajustesPort;

    public record AjustesDto(boolean autoAsignar, boolean bienvenidaActiva, String bienvenidaTexto,
                             boolean horarioActivo, String horarioInicio, String horarioFin,
                             String horarioDias, String fueraHorarioTexto, boolean csatActivo,
                             String csatTexto) {
        static AjustesDto from(AjustesWa a) {
            return new AjustesDto(a.autoAsignar(), a.bienvenidaActiva(), a.bienvenidaTexto(),
                    a.horarioActivo(), a.horarioInicio(), a.horarioFin(), a.horarioDias(),
                    a.fueraHorarioTexto(), a.csatActivo(), a.csatTexto());
        }
        AjustesWa toDomain() {
            return new AjustesWa(autoAsignar, bienvenidaActiva, bienvenidaTexto,
                    horarioActivo, horarioInicio, horarioFin, horarioDias, fueraHorarioTexto, csatActivo,
                    csatTexto);
        }
    }

    @GetMapping
    public ResponseEntity<AjustesDto> get() {
        return ResponseEntity.ok(AjustesDto.from(ajustesPort.get()));
    }

    @PutMapping
    public ResponseEntity<AjustesDto> update(@RequestBody AjustesDto dto) {
        return ResponseEntity.ok(AjustesDto.from(ajustesPort.save(dto.toDomain())));
    }
}
