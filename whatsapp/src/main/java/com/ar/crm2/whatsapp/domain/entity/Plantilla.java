package com.ar.crm2.whatsapp.domain.entity;

import com.ar.crm2.shared.DomainAssert;

import java.time.LocalDateTime;
import java.util.UUID;

/** Plantilla de mensaje rápido (con variables tipo {{nombre}}). */
public record Plantilla(UUID id, String titulo, String contenido, LocalDateTime creadoEn) {

    public static Plantilla create(String titulo, String contenido) {
        DomainAssert.lengthBetween(titulo, "titulo", 1, 120);
        DomainAssert.notBlank(contenido, "contenido");
        return new Plantilla(
                UUID.randomUUID(),
                titulo.trim(),
                contenido.trim(),
                LocalDateTime.now());
    }
}
