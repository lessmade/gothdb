package io.github.lessmade.gothdb.core.model;

public record PrimaryKeyInfo(
        String catalog,
        String schema,
        String table,
        String columnName,
        int keySequence,
        String primaryKeyName) {
}
