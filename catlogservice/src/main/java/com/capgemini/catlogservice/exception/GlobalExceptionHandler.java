package com.capgemini.catlogservice.exception;

import com.capgemini.catlogservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(500)
                .message("Error: "+exception.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(500).body(errorResponse);
    }
}
