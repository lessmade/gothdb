package io.github.lessmade.gothdb.core.schema;

@FunctionalInterface
public interface SchemaFilter {

    boolean isVisible(String schema);
}
