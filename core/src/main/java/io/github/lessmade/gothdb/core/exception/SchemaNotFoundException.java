package io.github.lessmade.gothdb.core.exception;

public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException(String schema) {
        super("Schema not found: " + schema);
    }
}
