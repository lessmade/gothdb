package io.github.lessmade.gothdb.core.model;

public record TableInfo(
        String catalog,
        String schema,
        String name,
        String type,
        String remarks) {
}
