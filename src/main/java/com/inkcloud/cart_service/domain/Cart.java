package com.inkcloud.cart_service.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}),
       indexes = {
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_product_id", columnList = "product_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "product_id", nullable = false, length = 100)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.id = UUID.randomUUID();
        this.quantity = Math.max(1, this.quantity); // 최소 1 이상
    }

    public void changeQuantity(int newQuantity) {

        if (newQuantity >= 1) {
            this.quantity = newQuantity;
        }
    }

    public void increaseQuantity() {

        this.quantity += 1;
    }

    public void decreaseQuantity() {
        
        if (this.quantity > 1) {
            this.quantity -= 1;
        }
    }
}