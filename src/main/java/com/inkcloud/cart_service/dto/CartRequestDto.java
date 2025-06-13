package com.inkcloud.cart_service.dto;

import lombok.Data;

@Data
public class CartRequestDto {

    private String userId;

    private String productId;

    private int quantity;

}
