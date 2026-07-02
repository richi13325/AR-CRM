package com.ar.crm2.application.ai.service;

import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

import java.time.LocalDateTime;

/**
 * Helper used by tests to fabricate an {@link AiAccion}
 * without going through {@code AiAccion.crear} directly when
 * a stub port is needed.
 */
final class AiAccionStub {

    private AiAccionStub() {
    }

    static AiAccion make(AiConversacionId tempId) {
        AiConversacionId aiConv = tempId != null ? tempId : AiConversacionId.create();
        return AiAccion.reconstitute(
                AiAccionId.create(),
                EmpresaId.create(),
                UsuarioId.create(),
                "wa-conv",
                null,
                aiConv,
                "CREATE_CONTACTO",
                "{\"nombre\":\"x\"}",
                "r",
                1,
                LocalDateTime.now().plusHours(1),
                null, null,
                EstadoAccion.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}