package com.lpa.demon_slayer_api_service.controller;

import com.lpa.demon_slayer_api_service.exception.DemonSlayerApiException;
import com.lpa.demon_slayer_api_service.model.exceptionhandler.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DemonSlayerApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(DemonSlayerApiException dsEx) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .time(LocalDateTime.now())
                .status(dsEx.getStatusCode())
                .message(dsEx.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, dsEx.getStatus());
    }
}
