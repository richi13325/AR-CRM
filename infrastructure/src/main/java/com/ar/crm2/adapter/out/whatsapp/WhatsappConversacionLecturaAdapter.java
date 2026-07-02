package com.ar.crm2.adapter.out.whatsapp;

import com.ar.crm2.adapter.out.persistence.CanalWhatsappRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.ConversacionRepositoryAdapter;
import com.ar.crm2.application.ai.port.out.projection.WhatsappConversacionResumen;
import com.ar.crm2.application.ai.port.out.WhatsappConversacionLecturaPort;
import com.ar.crm2.whatsapp.domain.entity.CanalWhatsapp;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * Read-side bridge adapter that exposes a WhatsApp conversation
 * summary through the application-owned
 * {@link WhatsappConversacionLecturaPort}.
 *
 * <p>The summary carries {@code canalEmpresaId} so the application
 * layer can perform tenant-scope checks against
 * {@code FindEmpresasByCreadorPort} without ever importing the
 * {@code whatsapp} module (see design.md §Module Boundaries).
 *
 * <p><b>Safety boundary:</b> read-only bridge. Never mutates WhatsApp
 * persistence and never invokes AI mutation use cases.
 *
 * <p><b>Control flow:</b> the adapter uses
 * {@link WhatsappConversacionNotFoundException} /
 * {@link WhatsappCanalNotFoundException} internally for clarity (each
 * branch is a single named intent) and catches them at the boundary
 * so the port contract — {@code Optional<WhatsappConversacionResumen>}
 * — is preserved without manual {@code if (x.isEmpty()) return Optional.empty()}
 * ladders.
 */
@RequiredArgsConstructor
public class WhatsappConversacionLecturaAdapter implements WhatsappConversacionLecturaPort {

    private final ConversacionRepositoryAdapter conversacionRepositoryAdapter;
    private final CanalWhatsappRepositoryAdapter canalWhatsappRepositoryAdapter;

    @Override
    public Optional<WhatsappConversacionResumen> findById(UUID waConversacionId) {
        try {
            return Optional.of(loadProjection(waConversacionId));
        } catch (WhatsappConversacionNotFoundException | WhatsappCanalNotFoundException e) {
            // Application port contract: return Optional.empty() when the
            // WhatsApp conversation or its channel is missing. The
            // application service performs the tenant authorization check.
            return Optional.empty();
        }
    }

    /**
     * Loads the projection for an existing conversation + its owning
     * channel. Throws a named infrastructure exception when either
     * reference is missing so the missing-row case is explicit at the
     * call site.
     */
    private WhatsappConversacionResumen loadProjection(UUID waConversacionId) {
        Conversacion conv = conversacionRepositoryAdapter
            .findById(ConversacionId.from(waConversacionId))
            .orElseThrow(() -> WhatsappConversacionNotFoundException.forId(waConversacionId));

        CanalWhatsapp canal = canalWhatsappRepositoryAdapter
            .findById(conv.getCanalId())
            .orElseThrow(() -> WhatsappCanalNotFoundException.forId(conv.getCanalId().value()));

        return new WhatsappConversacionResumen(
            conv.getId().value(),
            conv.getCanalId().value(),
            canal.getEmpresaId().value(),
            conv.getContactoId() != null ? conv.getContactoId().value() : null,
            conv.getNumeroTelefono(),
            conv.getNombreContacto()
        );
    }
}