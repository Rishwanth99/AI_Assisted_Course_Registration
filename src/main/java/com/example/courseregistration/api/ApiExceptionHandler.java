package com.example.courseregistration.api;

import com.example.courseregistration.api.response.ApiErrorResponse;
import com.example.courseregistration.application.CourseOfferingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CourseOfferingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOfferingNotFound(CourseOfferingNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "OFFERING_NOT_FOUND", exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s %s".formatted(error.getField(), error.getDefaultMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request validation failed.", details);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            List<String> details
    ) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(Instant.now(), status.value(), code, message, details));
    }
}
