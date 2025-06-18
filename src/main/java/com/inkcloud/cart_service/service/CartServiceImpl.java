package com.inkcloud.cart_service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;

    // 상품 서비스 엔드포인트 (Gateway 경유 or Eureka 서비스명 사용)
    @Value("${product.service.url}")
    private String productServiceUrl;

    @Override
    public CartResponseDto addToCart(CartRequestDto dto) {

        log.info("[1] 요청 DTO 수신: {}", dto);

        // 1. 상품 존재 여부 확인
        try {
            validateProductExists(dto.getProductId());
            log.info("[2] 상품 존재 확인 완료: productId={}", dto.getProductId());
        } catch (Exception e) {
            log.error("[2-ERROR] 상품 존재 확인 실패: {}", e.getMessage(), e);
            throw e;
        }

        // 2. 상품 상세 정보 조회
        ProductDto product = null;
        try {
            product = fetchProduct(dto.getProductId());
            log.info("[3] 상품 정보 조회 완료: {}", product);
        } catch (Exception e) {
            log.error("[3-ERROR] 상품 정보 조회 실패: {}", e.getMessage(), e);
            throw e;
        }

        // 3. 상태 확인
        if (!"ON_SALE".equals(product.getStatus())) {
            log.warn("[4-WARN] 상품 상태 비정상: productId={}, status={}", dto.getProductId(), product.getStatus());
            throw new InvalidProductStatusException("해당 상품은 현재 장바구니에 담을 수 없습니다. (판매 중 아님)");
        }

        // 4. 기존 장바구니 여부 확인
        Optional<Cart> existing = cartRepository.findByUserIdAndProductId(dto.getUserId(), dto.getProductId());
        Cart savedCart;

        if (existing.isPresent()) {
            Cart cart = existing.get();
            log.info("[5] 기존 장바구니 항목 존재: cartId={}, quantity={}", cart.getId(), cart.getQuantity());
            cart.increaseQuantity();
            savedCart = cartRepository.save(cart);
            log.info("[6] 장바구니 수량 증가 후 저장 완료: {}", savedCart);
        } else {
            Cart cart = Cart.builder()
                    .id(UUID.randomUUID())
                    .userId(dto.getUserId())
                    .productId(dto.getProductId())
                    .quantity(Math.max(1, dto.getQuantity()))
                    .build();
            savedCart = cartRepository.save(cart);
            log.info("[5] 새 장바구니 항목 생성 및 저장 완료: {}", savedCart);
        }

        CartResponseDto responseDto = toDto(savedCart);
        log.info("[7] 응답 DTO 반환: {}", responseDto);
        return responseDto;
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
                            .productName(product.getName())
                            .productImage(product.getImage())
                            .productPrice(product.getPrice())
                            .productAuthor(product.getAuthor())
                            .productPublisher(product.getPublisher())
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
            log.info("Success!!");
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
