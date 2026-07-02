package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.ConfirmarAccionCommand;
import com.ar.crm2.application.ai.port.in.result.ResultadoEjecucionAccion;

/**
 * Inbound use case: confirm a pending AI action proposal.
 *
 * <p><b>Safety boundary:</b> this is the only AI service allowed to
 * invoke real CRM mutation use cases. Owner + tenant + state + version
 * + expiry checks are all enforced here before the dispatch. On
 * dispatch failure the proposal is marked FAILED before returning so
 * the lifecycle stays observable.
 */
public interface ConfirmarAccionUseCase {

    ResultadoEjecucionAccion confirmar(ConfirmarAccionCommand command);
}