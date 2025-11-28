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
     * 可用库存 = 实际库存 - 预扣库存
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * 检查是否有足够的可用库存
     */
    public boolean hasEnoughStock(Integer requestedQty) {
        return getAvailableQuantity() >= requestedQty;
    }

    /**
     * ✅ 预扣库存（第一阶段）
     */
    public void reserve(Integer qty) {
        if (!hasEnoughStock(qty)) {
            throw new IllegalStateException("库存不足: 可用=" + getAvailableQuantity() + ", 需要=" + qty);
        }
        this.reservedQuantity += qty;
    }

    /**
     * ✅ 确认扣减（第二阶段 - 成功）
     */
    public void confirmReserve(Integer qty) {
        if (this.reservedQuantity < qty) {
            throw new IllegalStateException("预扣库存不足");
        }
        this.quantity -= qty;
        this.reservedQuantity -= qty;
    }

    /**
     * ✅ 取消预扣（第二阶段 - 失败回滚）
     */
    public void cancelReserve(Integer qty) {
        if (this.reservedQuantity < qty) {
            throw new IllegalStateException("预扣库存不足");
        }
        this.reservedQuantity -= qty;
    }

}
