package io.github.lessmade.gothdb.autoconfigure.web;

public record GothDbError(int status, String error, String message) {
}
