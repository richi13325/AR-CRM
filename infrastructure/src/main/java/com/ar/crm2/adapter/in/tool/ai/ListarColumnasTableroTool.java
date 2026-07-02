package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ListarColumnasTableroRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.ListarColumnasTableroResponse;
import com.ar.crm2.application.ai.port.out.ColumnaLecturaPort;
import com.ar.crm2.model.entity.Columna;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

/**
 * {@code @Tool}-annotated input adapter that lists the catalog
 * columns for a given board type so the model can pick a valid
 * target column before proposing {@code MOVE_KANBAN_FICHA}.
 *
 * <p><b>Read-only by design.</b> the tool depends ONLY on the
 * {@link ColumnaLecturaPort} read port and never on a real CRM
 * mutation use case. The constructor guard in
 * {@code ListarColumnasTableroToolTest#constructor_doesNotInjectRealMutationUseCases}
 * pins this contract.
 */
@Slf4j
@RequiredArgsConstructor
public class ListarColumnasTableroTool {

    private final ColumnaLecturaPort columnaLecturaPort;

    /**
     * Returns the catalog columns for the requested board type.
     * Used by the model before proposing {@code MOVE_KANBAN_FICHA}
     * so it can supply a valid {@code targetColumnaId}.
     *
     * @param request model-supplied board type
     * @return the columns for that board type; never null
     */
    @Tool(
        name = "listarColumnasTablero",
        description = "Returns the catalog columns for a board type (TAREAS or TRATOS). "
            + "Use this BEFORE proposing MOVE_KANBAN_FICHA so the model can pick a valid "
            + "target column id. Read-only — does NOT create, update, or move any entity."
    )
    public List<ListarColumnasTableroResponse> listarColumnasTablero(
            ListarColumnasTableroRequest request) {
        List<Columna> columnas = columnaLecturaPort
            .findByTipoTablero(request.tipoTableroAsEnum());

        List<ListarColumnasTableroResponse> out =
            ListarColumnasTableroToolMapper.toResponseList(columnas);

        log.info("AI tool listarColumnasTablero: tipoTablero={} returned={}",
            request.tipoTablero(), out.size());
        return out;
    }
}