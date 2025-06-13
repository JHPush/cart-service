package com.inkcloud.cart_service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.inkcloud.cart_service.domain.Cart;
import com.inkcloud.cart_service.dto.CartRequestDto;
import com.inkcloud.cart_service.dto.CartResponseDto;
import com.inkcloud.cart_service.dto.ProductDto;
import com.inkcloud.cart_service.exception.CartItemNotFoundException;
import com.inkcloud.cart_service.exception.InvalidCartQuantityException;
import com.inkcloud.cart_service.exception.InvalidProductStatusException;
import com.inkcloud.cart_service.exception.ProductNotFoundException;
import com.inkcloud.cart_service.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;

    // 상품 서비스 엔드포인트 (Gateway 경유 or Eureka 서비스명 사용)
    private final String productServiceUrl = "http://api-gateway-service:25000/api/v1/products";

    @Override
    public CartResponseDto addToCart(CartRequestDto dto) {

        // 상품 유효성 검증
        validateProductExists(dto.getProductId());
        
        // 상품 유효성 및 상태 확인
        ProductDto product = fetchProduct(dto.getProductId());

        if (!"ON_SALE".equals(product.getStatus())) {
            throw new InvalidProductStatusException("해당 상품은 현재 장바구니에 담을 수 없습니다. (판매 중 아님)");
        }

        Optional<Cart> existing = cartRepository.findByUserIdAndProductId(dto.getUserId(), dto.getProductId());

        Cart savedCart;
        if (existing.isPresent()) {
            Cart cart = existing.get();
            cart.increaseQuantity();
            savedCart = cartRepository.save(cart);
        } else {
            Cart cart = Cart.builder()
                    .id(UUID.randomUUID())
                    .userId(dto.getUserId())
                    .productId(dto.getProductId())
                    .quantity(Math.max(1, dto.getQuantity()))
                    .build();
            savedCart = cartRepository.save(cart);
        }

        return toDto(savedCart);
    }


    @Override
    public List<CartResponseDto> getCartByUserId(String userId) {

        List<Cart> carts = cartRepository.findAllByUserId(userId);

        return carts.stream()
                .map(cart -> {
                    ProductDto product = fetchProduct(cart.getProductId());

                    return CartResponseDto.builder()
                            .id(cart.getId())
                            .userId(cart.getUserId())
                            .productId(cart.getProductId())
                            .quantity(cart.getQuantity())
                            .productStatus(product.getStatus())
                            .createdAt(cart.getCreatedAt())
                            .updatedAt(cart.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void clearCart(String userId) {

        List<Cart> userCart = cartRepository.findAllByUserId(userId);
        cartRepository.deleteAll(userCart);
    }

    @Override
    public void removeItem(UUID cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartItemNotFoundException("장바구니 항목을 찾을 수 없습니다."));
        cartRepository.delete(cart);
    }

    @Override
    public CartResponseDto updateQuantity(UUID cartId, int quantity) {

        if (quantity < 1) {
            throw new InvalidCartQuantityException("수량은 1 이상이어야 합니다.");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartItemNotFoundException("장바구니 항목을 찾을 수 없습니다."));
        cart.changeQuantity(quantity);
        Cart updated = cartRepository.save(cart);

        return toDto(updated);
    }

    //상품 유효성 검사
    private void validateProductExists(Long productId) {

        String url = productServiceUrl + "/" + productId;

        try {
            restTemplate.getForEntity(url, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("해당 상품이 존재하지 않습니다. productId=" + productId);
        }
    }

    private ProductDto fetchProduct(Long productId) {

        String url = productServiceUrl + "/" + productId;

        try {
            return restTemplate.getForObject(url, ProductDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("해당 상품이 존재하지 않습니다. productId=" + productId);
        }
    }


    private CartResponseDto toDto(Cart cart) {
        return CartResponseDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .productId(cart.getProductId())
                .quantity(cart.getQuantity())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
