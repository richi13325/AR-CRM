package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.whatsapp.application.mensaje.port.out.MediaStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sirve el media entrante de WhatsApp ya descargado y guardado en disco.
 * Solo lectura de archivos previamente generados por el CRM.
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaStoragePort storage;

    @GetMapping("/{archivo}")
    public ResponseEntity<byte[]> get(@PathVariable String archivo) {
        byte[] bytes = storage.leer(archivo);
        if (bytes == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storage.mimeDeArchivo(archivo)))
                .body(bytes);
    }
}
