package io.github.lessmade.gothdb.core.model;

public record IndexInfo(
        String catalog,
        String schema,
        String table,
        String name,
        boolean unique,
        int ordinalPosition,
        String columnName,
        String sortOrder) {
}
