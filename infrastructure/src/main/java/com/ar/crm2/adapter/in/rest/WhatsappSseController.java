package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.out.sse.SseEmitterRegistry;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.security.ActorContextRequestAttributeFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/wa/stream")
@RequiredArgsConstructor
public class WhatsappSseController {

    private final SseEmitterRegistry registry;

    @GetMapping(produces = "text/event-stream")
    public SseEmitter stream(HttpServletRequest httpRequest) {
        ActorContext actor = (ActorContext) httpRequest.getAttribute(
                ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE);
        return registry.register(actor.usuarioId().value());
    }
}
