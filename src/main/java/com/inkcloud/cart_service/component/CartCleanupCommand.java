package com.inkcloud.cart_service.component;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.inkcloud.cart_service.service.CartCleanupService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartCleanupCommand implements CommandLineRunner {

    private final CartCleanupService cleanupService;

    @Override
    public void run(String... args) {
        int count = cleanupService.deleteExpiredCarts();
        System.out.println("🧹 삭제된 장바구니 항목 수: " + count);
    }
}
