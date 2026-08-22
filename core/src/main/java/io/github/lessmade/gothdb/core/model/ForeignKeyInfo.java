package io.github.lessmade.gothdb.core.model;

public record ForeignKeyInfo(
        String name,
        String catalog,
        String schema,
        String table,
        String columnName,
        String referencedCatalog,
        String referencedSchema,
        String referencedTable,
        String referencedColumn,
        int keySequence,
        String updateRule,
        String deleteRule) {
}
