package io.github.lessmade.gothdb.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.github.lessmade.gothdb.core.exception.DatabaseMetadataException;
import io.github.lessmade.gothdb.core.exception.SchemaNotFoundException;
import io.github.lessmade.gothdb.core.exception.TableNotFoundException;

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GothDbError> onTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + exception.getName() + "'");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GothDbError> onResourceNotFound(NoResourceFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "Resource not found");
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
