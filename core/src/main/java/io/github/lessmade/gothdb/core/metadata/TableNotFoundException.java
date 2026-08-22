package io.github.lessmade.gothdb.core.metadata;

public class TableNotFoundException extends RuntimeException {

    public TableNotFoundException(String schema, String table) {
        super("Table not found: " + schema + "." + table);
    }
}
