package com.ar.crm2.application.ai.port.out.projection;

import java.util.UUID;

/**
 * Application-owned projection of a WhatsApp conversation.
 *
 * <p>The application layer cannot depend on the {@code whatsapp}
 * module (per design.md module matrix). The infrastructure adapter
 * maps {@code whatsapp.domain.entity.Conversacion} into this record
 * before crossing the port boundary.
 *
 * <p>Lives in {@code port.out.projection} because it is the
 * application-owned projection that the
 * {@code WhatsappConversacionLecturaPort} returns to its callers.
 *
* <p>The {@code canalEmpresaId} field is the company that owns the
 * WhatsApp channel — it is the tenant key the AI application services
 * cross-check against the Empresa-owned {@code ActorEmpresaScopePort}.
 */
public record WhatsappConversacionResumen(
    UUID waConversacionId,
    UUID canalId,
    UUID canalEmpresaId,
    UUID contactoId,
    String numeroTelefono,
    String nombreContacto
) {

    public WhatsappConversacionResumen {
        if (waConversacionId == null) {
            throw new IllegalArgumentException("waConversacionId is required");
        }
        if (canalId == null) {
            throw new IllegalArgumentException("canalId is required");
        }
        if (canalEmpresaId == null) {
            throw new IllegalArgumentException("canalEmpresaId is required");
        }
    }
}
