package io.github.lessmade.gothdb.exception;

public record GothDbError(int status, String error, String message) {
}
