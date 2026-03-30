package com.capgemini.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    // ErrorResponse → standard structure for returning API error details
    private int status;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
