package com.inkcloud.cart_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponseDto {

    private UUID id;

    private String userId;

    private Long productId;

    private int quantity;

    private String productStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
}
