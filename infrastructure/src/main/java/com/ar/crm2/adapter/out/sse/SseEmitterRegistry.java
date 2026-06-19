package com.ar.crm2.adapter.out.sse;

import org.springframework.scheduling.annotation.Scheduled;
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

    // Sin esto, un proxy/load balancer entre el navegador y el back puede cerrar
    // la conexión SSE por inactividad sin avisar (sin disparar onError/onTimeout),
    // dejando al front "colgado" creyendo que sigue conectado.
    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
                emitters.remove(userId);
            }
        });
    }
}
