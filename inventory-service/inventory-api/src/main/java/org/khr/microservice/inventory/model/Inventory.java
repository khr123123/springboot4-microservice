package org.khr.microservice.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 在庫エンティティ
 * Spring 7の新機能を活用
 */
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "商品IDは必須です")
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @NotBlank(message = "商品名は必須です")
    @Column(name = "product_name", nullable = false)
    private String productName;

    @NotNull(message = "数量は必須です")
    @Min(value = 0, message = "数量は0以上である必要があります")
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 利用可能な在庫数を取得
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * 在庫が十分にあるかチェック
     */
    public boolean hasEnoughStock(Integer requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }
}
