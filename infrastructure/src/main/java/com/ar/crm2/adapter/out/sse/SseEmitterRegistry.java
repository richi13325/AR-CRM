package com.ar.crm2.adapter.out.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(UUID usuarioId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String key = usuarioId.toString();
        emitters.put(key, emitter);
        emitter.onTimeout(() -> emitters.remove(key));
        emitter.onError(e -> emitters.remove(key));
        emitter.onCompletion(() -> emitters.remove(key));
        return emitter;
    }

    public Map<String, SseEmitter> getAll() {
        return Map.copyOf(emitters);
    }
}
