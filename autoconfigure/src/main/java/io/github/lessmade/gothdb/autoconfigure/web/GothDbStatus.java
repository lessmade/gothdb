package io.github.lessmade.gothdb.autoconfigure.web;

public record GothDbStatus(
        String status,
        String database,
        String databaseVersion,
        String driver) {
}
