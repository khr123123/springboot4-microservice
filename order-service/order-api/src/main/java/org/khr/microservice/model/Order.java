package org.khr.microservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 注文エンティティ
 * Spring 7の新機能を活用したエンティティ設計
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "ユーザーIDは必須です")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotNull(message = "商品IDは必須です")
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上である必要があります")
    @Column(nullable = false)
    private Integer quantity;
    
    @NotNull(message = "価格は必須です")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum OrderStatus {
        PENDING,      // 保留中
        CONFIRMED,    // 確認済み
        SHIPPED,      // 出荷済み
        DELIVERED,    // 配達済み
        CANCELLED     // キャンセル
    }
}
