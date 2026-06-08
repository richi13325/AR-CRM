package com.ar.crm2.application.ficha.service;

import com.ar.crm2.application.etiqueta.exception.EtiquetaNotFoundException;
import com.ar.crm2.application.etiqueta.port.out.FindEtiquetasByIdsPort;
import com.ar.crm2.exception.EtiquetaTypeMismatchException;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.entity.FichaEtiqueta;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Shared utility for resolving a list of EtiquetaIds into a list of
 * FichaEtiqueta relations, asserting that every requested id resolves
 * to a real Etiqueta AND every resolved Etiqueta has the expected
 * TipoEtiqueta.
 *
 * <p>Package-private to keep the helper scoped to Ficha application services.
 */
final class FichaEtiquetaResolver {

    private FichaEtiquetaResolver() {
    }

    /**
     * Resolves the given etiquetaIds into FichaEtiqueta relations.
     *
     * <p>Strict resolution: if the catalog returns fewer Etiquetas than
     * requested ids, the missing ids are aggregated and the call is
     * rejected with {@link EtiquetaNotFoundException} (carrying the
     * missing-ids list) instead of silently dropping them. This is
     * important to avoid losing tags in a Ficha save when one of the
     * referenced etiquetas has been deleted out-of-band.
     *
     * @param findEtiquetasPort port to load Etiquetas
     * @param etiquetaIds       list of UUIDs to resolve
     * @param tipoEsperado      the TipoEtiqueta that all resolved Etiquetas must match
     * @return a list of FichaEtiqueta relations, one per resolved Etiqueta
     * @throws EtiquetaNotFoundException    if any requested id is not found in the catalog
     * @throws EtiquetaTypeMismatchException if any resolved Etiqueta has a different type
     */
    static List<FichaEtiqueta> resolve(
        FindEtiquetasByIdsPort findEtiquetasPort,
        List<UUID> etiquetaIds,
        TipoEtiqueta tipoEsperado
    ) {
        List<EtiquetaId> ids = etiquetaIds.stream().map(EtiquetaId::from).toList();
        List<Etiqueta> etiquetas = findEtiquetasPort.findByIds(ids);

        if (etiquetas.size() < ids.size()) {
            Set<UUID> found = etiquetas.stream()
                .map(e -> e.getId().value())
                .collect(Collectors.toSet());
            List<EtiquetaId> missing = ids.stream()
                .filter(id -> !found.contains(id.value()))
                .toList();
            throw EtiquetaNotFoundException.forMissingIds(missing);
        }

        List<FichaEtiqueta> relations = new ArrayList<>(etiquetas.size());
        for (Etiqueta e : etiquetas) {
            if (!e.getTipoEtiqueta().equals(tipoEsperado)) {
                throw new EtiquetaTypeMismatchException();
            }
            relations.add(FichaEtiqueta.create(e.getId(), e.getTipoEtiqueta()));
        }
        return relations;
    }
}
