package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.whatsapp.application.mensaje.port.out.MediaStoragePort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Media de WhatsApp:
 * - GET /{archivo}: sirve un archivo ya guardado (público, para que lo cargue el <img>).
 * - POST /upload: sube un adjunto (base64) que el agente quiere enviar; lo guarda y
 *   devuelve la URL servible. Requiere JWT (no matchea el matcher GET público).
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaStoragePort storage;

    public record UploadRequest(@NotBlank String base64, @NotBlank String mime) {}

    @GetMapping("/{archivo}")
    public ResponseEntity<byte[]> get(@PathVariable String archivo) {
        byte[] bytes = storage.leer(archivo);
        if (bytes == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storage.mimeDeArchivo(archivo)))
                .body(bytes);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@Valid @RequestBody UploadRequest req) {
        String url = storage.guardarBase64(req.base64(), req.mime());
        if (url == null) return ResponseEntity.unprocessableEntity().build();
        return ResponseEntity.ok(Map.of("url", url));
    }
}
