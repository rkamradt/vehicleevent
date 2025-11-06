package net.kamradtfamily.vehicleevent.vehiclequery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, ServerHttpRequest request) {
        String requestId = getRequestId(request);

        log.warn("Resource not found - Request ID: {}, Message: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .errorCode("NOT_FOUND")
                        .message(ex.getMessage())
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .path(request.getPath().value())
                        .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, ServerHttpRequest request) {
        String requestId = getRequestId(request);

        log.warn("Bad request - Request ID: {}, Message: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .errorCode("BAD_REQUEST")
                        .message(ex.getMessage())
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .path(request.getPath().value())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, ServerHttpRequest request) {
        String requestId = getRequestId(request);

        log.warn("Validation error - Request ID: {}, Message: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .errorCode("VALIDATION_ERROR")
                        .message(ex.getMessage())
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .path(request.getPath().value())
                        .build());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(WebExchangeBindException ex, ServerHttpRequest request) {
        String requestId = getRequestId(request);

        log.warn("Request validation failed - Request ID: {}", requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .errorCode("VALIDATION_ERROR")
                        .message("Request validation failed: " + ex.getMessage())
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .path(request.getPath().value())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex, ServerHttpRequest request) {
        String requestId = getRequestId(request);

        log.error("Unhandled exception - Request ID: {}, Path: {}", requestId, request.getPath().value(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .errorCode("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .path(request.getPath().value())
                        .build());
    }

    private String getRequestId(ServerHttpRequest request) {
        List<String> requestIdHeaders = request.getHeaders().get("x-request-id");
        return requestIdHeaders != null && !requestIdHeaders.isEmpty()
                ? requestIdHeaders.get(0)
                : "unknown";
    }
}
