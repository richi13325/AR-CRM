package com.ar.crm2.application.etiqueta.port.out;

import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;

/**
 * Granular outbound port for checking uniqueness of an Etiqueta by (nombre, tipo).
 *
 * <p>The {@code excludeId} parameter scopes the existence check so the same port
 * can be reused by both the create and edit use cases. For the create flow the
 * caller passes {@code null} (no row should be excluded). For the edit flow the
 * caller passes the id of the Etiqueta being edited, so the adapter ignores
 * the row being updated when checking uniqueness — otherwise a rename to the
 * same name (idempotent rename) or a recolor-only operation would be falsely
 * rejected by a naive "exists by (nombre, tipo)" query that matches the row
 * to itself.
 */
public interface ExistsEtiquetaByNombreAndTipoPort {
    boolean exists(String nombre, TipoEtiqueta tipoEtiqueta, EtiquetaId excludeId);
}
