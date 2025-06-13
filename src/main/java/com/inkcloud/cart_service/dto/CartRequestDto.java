package com.inkcloud.cart_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CartRequestDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String userId;

    private Long productId;

    private int quantity;

}
