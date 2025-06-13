package com.inkcloud.cart_service.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class ErrorResponse {

    private final int status;

    private final String message;

    private final LocalDateTime timestamp;
    
}