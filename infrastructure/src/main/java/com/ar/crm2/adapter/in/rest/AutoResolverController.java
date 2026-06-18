package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.whatsapp.application.conversacion.port.in.AutoResolverConversacionesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Cierra conversaciones inactivas. Llamado por un Schedule Trigger de n8n (header x-api-key). */
@RestController
@RequiredArgsConstructor
public class AutoResolverController {

    private final AutoResolverConversacionesUseCase autoResolverUseCase;

    @Value("${crm2.wa.auto-resolver-horas:24}")
    private int horasInactividadDefault;

    @PostMapping("/api/cron/auto-resolver")
    public ResponseEntity<Map<String, Integer>> autoResolver() {
        int cerradas = autoResolverUseCase.resolverInactivas(horasInactividadDefault);
        return ResponseEntity.ok(Map.of("cerradas", cerradas));
    }
}
