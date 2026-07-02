package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.BuscarClientePorTelefonoResponse;
import com.ar.crm2.model.entity.Contacto;

/**
 * Mapper that translates between the domain {@link Contacto}
 * aggregate and the {@code BuscarClientePorTelefonoResponse} wire DTO.
 *
 * <p>The mapper is stateless and side-effect free. It only reads from
 * the aggregate; no business validation lives here.
 */
public final class BuscarClientePorTelefonoToolMapper {

    private BuscarClientePorTelefonoToolMapper() {}

    public static BuscarClientePorTelefonoResponse toResponse(Contacto contacto) {
        if (contacto == null) {
            return BuscarClientePorTelefonoResponse.miss();
        }
        return BuscarClientePorTelefonoResponse.hit(
            contacto.getId().value().toString(),
            contacto.getNombre(),
            contacto.getTelefono(),
            contacto.getCorreo(),
            contacto.getEstadoRelacion(),
            contacto.getEmpresaId().value().toString()
        );
    }
}