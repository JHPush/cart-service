package com.inkcloud.cart_service.service;

import java.util.List;
import java.util.UUID;

import com.inkcloud.cart_service.dto.CartRequestDto;
import com.inkcloud.cart_service.dto.CartResponseDto;

public interface CartService {

    CartResponseDto addToCart(CartRequestDto requestDto);

    List<CartResponseDto> getCartByUserId(String userId);
    
    CartResponseDto updateQuantity(UUID cartId, int quantity);
    
    void removeItem(UUID cartId);
    
    void clearCart(String userId);

}
