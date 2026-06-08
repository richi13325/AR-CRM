package com.ar.crm2.application.columna.service;

import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.shared.DomainAssert;

import java.util.List;
import java.util.Optional;

public final class ColumnaNamePolicy {

    private ColumnaNamePolicy() {
    }

    public static String normalize(String nombre) {
        return DomainAssert.lengthBetween(nombre, "nombre", 1, 80);
    }

    public static boolean hasDuplicateForCreate(List<Columna> columnas, TipoTablero tipoTablero, String nombre) {
        String normalizedNombre = normalize(nombre);

        return columnas.stream()
            .filter(columna -> columna.getTipoTablero() == tipoTablero)
            .anyMatch(columna -> normalize(columna.getColumnanombre()).equals(normalizedNombre));
    }

    public static boolean hasDuplicateForEdit(List<Columna> columnas, ColumnaId columnaId, TipoTablero tipoTablero, String nombre) {
        String normalizedNombre = normalize(nombre);

        return columnas.stream()
            .filter(columna -> columna.getTipoTablero() == tipoTablero)
            .filter(columna -> !columna.getId().equals(columnaId))
            .anyMatch(columna -> normalize(columna.getColumnanombre()).equals(normalizedNombre));
    }

    public static Optional<Columna> findDefaultCatalogColumn(List<Columna> columnas, TipoTablero tipoTablero, String nombre) {
        String normalizedNombre = normalize(nombre);

        return columnas.stream()
            .filter(columna -> columna.getTipoTablero() == tipoTablero)
            .filter(columna -> columna.getTipoColumna() == TipoColumna.PREDETERMINADA)
            .filter(columna -> normalize(columna.getColumnanombre()).equals(normalizedNombre))
            .findFirst();
    }
}
