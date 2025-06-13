package com.inkcloud.cart_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inkcloud.cart_service.domain.Cart;

public interface CartRepository extends JpaRepository<Cart, UUID>, CustomCartRepository{

    Optional<Cart> findByUserIdAndProductId(String userId, Long productId);
    
    List<Cart> findAllByUserId(String userId);

}
