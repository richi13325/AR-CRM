package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.adapter.in.rest.dto.request.ReceiveWebhookRequest;
import com.ar.crm2.adapter.in.rest.dto.response.MensajeWaResponse;
import com.ar.crm2.whatsapp.application.mensaje.command.ReceiveMensajeCommand;
import com.ar.crm2.whatsapp.application.mensaje.port.in.ReceiveMensajeUseCase;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wa/webhook")
@RequiredArgsConstructor
public class WhatsappWebhookController {

    private final ReceiveMensajeUseCase receiveMensajeUseCase;

    @PostMapping
    public ResponseEntity<?> receive(@Valid @RequestBody ReceiveWebhookRequest request) {
        try {
            ReceiveMensajeCommand command = new ReceiveMensajeCommand(
                    request.canalId(),
                    request.waMessageId(),
                    request.numeroTelefono(),
                    request.nombreContacto(),
                    request.tipo(),
                    request.contenido(),
                    request.mediaUrl()
            );
            Mensaje mensaje = receiveMensajeUseCase.receive(command);
            return ResponseEntity.ok(MensajeWaResponse.fromDomain(mensaje));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
