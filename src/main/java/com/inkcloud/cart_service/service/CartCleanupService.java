package com.inkcloud.cart_service.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.inkcloud.cart_service.repository.CartRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartCleanupService {

    private final CartRepository cartRepository;

    @Transactional
    public int deleteExpiredCarts() {
        LocalDateTime deadline = LocalDateTime.now().minusWeeks(1);
        return cartRepository.deleteByCreatedAtBefore(deadline);
    }
}
