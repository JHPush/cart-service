package com.inkcloud.cart_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.inkcloud.cart_service.dto.CartRequestDto;
import com.inkcloud.cart_service.dto.CartResponseDto;
import com.inkcloud.cart_service.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping
    public CartResponseDto addToCart(@RequestBody CartRequestDto requestDto,
                                     @AuthenticationPrincipal Jwt jwt) {

        log.info("requestDto : {}", requestDto);
        
        String userId = jwt.getSubject();
        log.info("userId: {}", userId);

        requestDto.setUserId(userId);

        return cartService.addToCart(requestDto);
    }


    @GetMapping
    public List<CartResponseDto> getCart(@AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();

        return cartService.getCartByUserId(userId);
    }


    @PutMapping("/{cartId}")
    public CartResponseDto updateQuantity(@PathVariable UUID cartId,
                                          @RequestParam int quantity) {

        return cartService.updateQuantity(cartId, quantity);
    }


    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID cartId) {

        cartService.removeItem(cartId);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        cartService.clearCart(userId);

        return ResponseEntity.noContent().build();
    }

}
