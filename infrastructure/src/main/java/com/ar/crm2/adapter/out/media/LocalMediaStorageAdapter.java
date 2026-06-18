package com.ar.crm2.adapter.out.media;

import com.ar.crm2.whatsapp.application.mensaje.port.out.MediaStoragePort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

/**
 * Guarda el media en disco (volumen Docker en prod) y lo sirve por /api/media/<archivo>.
 * Espejo de AmbarCRM/.../lib/storage.ts.
 */
public class LocalMediaStorageAdapter implements MediaStoragePort {

    private static final Map<String, String> MIME_EXT = Map.ofEntries(
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/png", "png"),
            Map.entry("image/webp", "webp"),
            Map.entry("image/gif", "gif"),
            Map.entry("video/mp4", "mp4"),
            Map.entry("audio/ogg", "ogg"),
            Map.entry("audio/mpeg", "mp3"),
            Map.entry("audio/webm", "weba"),
            Map.entry("application/pdf", "pdf")
    );

    private final Path dir;
    private final SecureRandom random = new SecureRandom();

    public LocalMediaStorageAdapter() {
        String configured = System.getenv("UPLOAD_DIR");
        this.dir = Paths.get(configured != null && !configured.isBlank() ? configured : "uploads");
    }

    @Override
    public String guardarBase64(String base64, String mime) {
        try {
            Files.createDirectories(dir);
            String limpio = base64.contains(",") ? base64.substring(base64.indexOf(',') + 1) : base64;
            byte[] bytes = Base64.getDecoder().decode(limpio);
            byte[] rnd = new byte[6];
            random.nextBytes(rnd);
            String nombre = System.currentTimeMillis() + "-" + HexFormat.of().formatHex(rnd) + "." + extDeMime(mime);
            Files.write(dir.resolve(nombre), bytes);
            return "/api/media/" + nombre;
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public byte[] leer(String nombre) {
        try {
            // basename evita path traversal
            Path file = dir.resolve(Paths.get(nombre).getFileName());
            return Files.exists(file) ? Files.readAllBytes(file) : null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String mimeDeArchivo(String nombre) {
        String ext = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase() : "";
        return MIME_EXT.entrySet().stream()
                .filter(e -> e.getValue().equals(ext))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("application/octet-stream");
    }

    private String extDeMime(String mime) {
        if (mime == null) return "bin";
        String base = mime.contains(";") ? mime.substring(0, mime.indexOf(';')).trim() : mime.trim();
        return MIME_EXT.getOrDefault(base, "bin");
    }
}
