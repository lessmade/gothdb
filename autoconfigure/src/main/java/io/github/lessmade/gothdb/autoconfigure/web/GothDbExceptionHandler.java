package io.github.lessmade.gothdb.autoconfigure.web;

import io.github.lessmade.gothdb.core.metadata.DatabaseMetadataException;
import io.github.lessmade.gothdb.core.metadata.SchemaNotFoundException;
import io.github.lessmade.gothdb.core.metadata.TableNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GothDbExceptionHandler {

    @ExceptionHandler({ SchemaNotFoundException.class, TableNotFoundException.class })
    public ResponseEntity<GothDbError> onNotFound(RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GothDbError> onBadRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DatabaseMetadataException.class)
    public ResponseEntity<GothDbError> onDatabaseFailure(DatabaseMetadataException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read database metadata");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GothDbError> onUnexpectedFailure(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private static ResponseEntity<GothDbError> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new GothDbError(status.value(), status.getReasonPhrase(), message));
    }
}
