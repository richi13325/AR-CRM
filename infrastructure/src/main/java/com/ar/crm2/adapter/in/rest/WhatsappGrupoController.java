package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.whatsapp.application.grupo.service.GrupoService;
import com.ar.crm2.whatsapp.domain.entity.Grupo;
import com.ar.crm2.whatsapp.domain.entity.MensajeGrupo;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wa/grupos")
@RequiredArgsConstructor
public class WhatsappGrupoController {

    private final GrupoService grupoService;

    public record GrupoResponse(String id, String canalId, String jid, String nombre,
                                int noLeidos, String ultimoMensajeAt) {
        static GrupoResponse from(Grupo g) {
            return new GrupoResponse(
                    g.getId().value().toString(),
                    g.getCanalId() != null ? g.getCanalId().value().toString() : null,
                    g.getJid(), g.getNombre(), g.getNoLeidos(),
                    g.getUltimoMensajeAt() != null ? g.getUltimoMensajeAt().toString() : null);
        }
    }

    public record MensajeGrupoResponse(String id, String grupoId, String direccion, String tipo,
                                       String contenido, String mediaUrl, String remitente,
                                       String remitenteTel, String status, String timestamp) {
        static MensajeGrupoResponse from(MensajeGrupo m) {
            return new MensajeGrupoResponse(
                    m.getId().value().toString(), m.getGrupoId().value().toString(),
                    m.getDireccion().name(), m.getTipo().name(), m.getContenido(), m.getMediaUrl(),
                    m.getRemitente(), m.getRemitenteTel(), m.getStatus().name(),
                    m.getTimestamp() != null ? m.getTimestamp().toString() : null);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<GrupoResponse>> getAll() {
        return ResponseEntity.ok(grupoService.getAll().stream().map(GrupoResponse::from).toList());
    }

    public record SendMensajeGrupoRequest(@NotNull TipoMensaje tipo, String contenido, String mediaUrl) {}

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<MensajeGrupoResponse>> getMensajes(@PathVariable UUID id) {
        return ResponseEntity.ok(grupoService.getMensajes(id).stream().map(MensajeGrupoResponse::from).toList());
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeGrupoResponse> enviar(
            @PathVariable UUID id, @Valid @RequestBody SendMensajeGrupoRequest req) {
        MensajeGrupo saved = grupoService.enviarMensaje(id, req.tipo(), req.contenido(), req.mediaUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeGrupoResponse.from(saved));
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Integer>> importar(@RequestParam UUID canalId) {
        return ResponseEntity.ok(Map.of("imported", grupoService.importar(canalId)));
    }

    @PostMapping("/{id}/marcar-leido")
    public ResponseEntity<GrupoResponse> marcarLeido(@PathVariable UUID id) {
        return ResponseEntity.ok(GrupoResponse.from(grupoService.marcarLeido(id)));
    }
}
