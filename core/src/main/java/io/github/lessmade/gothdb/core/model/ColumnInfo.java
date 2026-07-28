package io.github.lessmade.gothdb.core.model;

public record ColumnInfo(
        String catalog,
        String schema,
        String table,
        String name,
        int position,
        int jdbcType,
        String typeName,
        Integer size,
        Integer scale,
        boolean nullable,
        String defaultValue,
        boolean autoIncrement) {
}
