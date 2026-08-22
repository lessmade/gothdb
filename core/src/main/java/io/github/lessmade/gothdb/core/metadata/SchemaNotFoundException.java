package io.github.lessmade.gothdb.core.metadata;

public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException(String schema) {
        super("Schema not found: " + schema);
    }
}
