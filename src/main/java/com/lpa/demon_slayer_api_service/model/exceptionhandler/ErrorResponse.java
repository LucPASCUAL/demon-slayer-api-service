package com.lpa.demon_slayer_api_service.model.exceptionhandler;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(LocalDateTime time, int status, String message) {}
