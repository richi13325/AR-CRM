package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.BuscarClientePorTelefonoRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.BuscarClientePorTelefonoResponse;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.ContactoLecturaPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.model.entity.Contacto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Optional;

/**
 * {@code @Tool}-annotated input adapter that resolves an existing
 * {@code Contacto} by phone number, scoped to the trusted tenant.
 *
 * <p><b>Tenant boundary.</b> the {@code empresaId} is sourced from
 * the trusted {@link AiToolContextPort} so the model cannot probe
 * a foreign tenant — only the phone number is supplied by the
 * model.
 *
 * <p><b>Read-only by design.</b> the tool depends ONLY on the
 * {@link ContactoLecturaPort} read port and never on a real CRM
 * mutation use case. The constructor guard in
 * {@code BuscarClientePorTelefonoToolTest#constructor_doesNotInjectRealMutationUseCases}
 * pins this contract.
 *
 * <p><b>Safety note.</b> the {@link ContactoLecturaPort} uses
 * the trusted {@code empresaId} for the lookup key, so even if the
 * tool accidentally accepted a foreign phone, the database query
 * would never return rows from another tenant.
 *
 * <p><b>PII logging contract.</b> the model-supplied phone number
 * is customer PII and MUST NOT appear in clear text in any log
 * record. The tool routes the raw phone through
 * {@link LogMasking#maskPhone(String)} before it reaches the log
 * statement so only the country/area prefix and last-four digits
 * survive. The raw PII stays in {@code BuscarClientePorTelefonoRequest}
 * and downstream DTOs (request-scoped data) but never in the log
 * sink. The contract is pinned by
 * {@code BuscarClientePorTelefonoToolTest#logStatement_doesNotEmitRawTelefono}
 * and {@code logStatement_routesTelefonoThroughLogMaskingHelper}
 * (source-guard assertions on the tool's own source).
 */
@Slf4j
@RequiredArgsConstructor
public class BuscarClientePorTelefonoTool {

    private final AiToolContextPort aiToolContextPort;
    private final ContactoLecturaPort contactoLecturaPort;

    /**
     * Looks up an existing Contacto by phone number, scoped to the
     * trusted tenant. Returns a {@code encontrado=false} response when
     * no match is found so the model can decide whether to propose
     * {@code CREATE_CONTACTO}.
     *
     * @param request model-supplied phone number
     * @return the matched Contacto or a miss placeholder; never null
     */
    @Tool(
        name = "buscarClientePorTelefono",
        description = "Looks up an existing Contacto by phone number, scoped to the trusted tenant. "
            + "Use this BEFORE proposing CREATE_CONTACTO to avoid duplicates. Read-only — does "
            + "NOT create, update, or delete any entity. The lookup is scoped to the current "
            + "user's tenant; foreign tenants cannot be probed."
    )
    public BuscarClientePorTelefonoResponse buscarClientePorTelefono(
            BuscarClientePorTelefonoRequest request) {
        AiToolContext ctx = aiToolContextPort.resolve();

        Optional<Contacto> match = contactoLecturaPort
            .findByEmpresaIdAndTelefono(
                com.ar.crm2.model.vo.EmpresaId.from(ctx.empresaId()),
                request.telefono()
            );

        BuscarClientePorTelefonoResponse out = BuscarClientePorTelefonoToolMapper
            .toResponse(match.orElse(null));

        // PII masking (Slice 13): customer phone numbers must NEVER appear
        // in clear text in log records. Route the model-supplied phone
        // through LogMasking.maskPhone(...) so only the country/area
        // prefix and last-four survive. The full PII value stays in the
        // BuscarClientePorTelefonoRequest (and downstream DTOs) where it
        // is request-scoped data, never a log sink.
        String maskedTelefono = LogMasking.maskPhone(request.telefono());
        log.info(
            "AI tool buscarClientePorTelefono: telefono={} encontrado={} empresaId={}",
            maskedTelefono, out.encontrado(), ctx.empresaId()
        );
        return out;
    }
}