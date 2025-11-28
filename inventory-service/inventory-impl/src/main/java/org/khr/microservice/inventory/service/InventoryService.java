package org.khr.microservice.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.inventory.model.Inventory;
import org.khr.microservice.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 在庫サービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        log.info("全在庫を取得");
        return inventoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Inventory> getInventoryById(Long id) {
        log.info("在庫を取得: ID={}", id);
        return inventoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Inventory> getInventoryByProductId(Long productId) {
        log.info("商品の在庫を取得: ProductID={}", productId);
        return inventoryRepository.findByProductId(productId);
    }

    @Transactional
    public Inventory createInventory(Inventory inventory) {
        if (inventoryRepository.existsByProductId(inventory.getProductId())) {
            throw new IllegalArgumentException("商品IDが既に存在します: " + inventory.getProductId());
        }
        log.info("新規在庫を作成: ProductID={}, Quantity={}", inventory.getProductId(), inventory.getQuantity());
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory updateInventory(Long id, Inventory inventoryDetails) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("在庫が見つかりません: ID=" + id));
        inventory.setProductName(inventoryDetails.getProductName());
        inventory.setQuantity(inventoryDetails.getQuantity());
        log.info("在庫を更新: ID={}, Quantity={}", id, inventoryDetails.getQuantity());
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public void deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new IllegalArgumentException("在庫が見つかりません: ID=" + id);
        }
        log.info("在庫を削除: ID={}", id);
        inventoryRepository.deleteById(id);
    }

    /**
     * ✅ 阶段1: 预扣库存（Try）
     */
    @Transactional
    public boolean reserveInventory(Long productId, Integer quantity) {
        try {
            Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ProductID=" + productId));
            // 检查并预扣
            inventory.reserve(quantity);
            inventoryRepository.save(inventory);
            log.info("库存预扣成功: ProductID={}, Quantity={}, Available={}",
                productId, quantity, inventory.getAvailableQuantity());
            return true;
        } catch (Exception e) {
            log.error("库存预扣失败: ProductID={}, Quantity={}, Error={}",
                productId, quantity, e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 阶段2-成功: 确认扣减（Confirm）
     */
    @Transactional
    public void confirmInventory(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ProductID=" + productId));
        inventory.confirmReserve(quantity);
        inventoryRepository.save(inventory);

        log.info("库存扣减确认: ProductID={}, Quantity={}, Remaining={}",
            productId, quantity, inventory.getQuantity());
    }

    /**
     * ✅ 阶段2-失败: 取消预扣（Cancel）
     */
    @Transactional
    public void cancelInventory(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ProductID=" + productId));

        inventory.cancelReserve(quantity);
        inventoryRepository.save(inventory);

        log.info("库存预扣取消: ProductID={}, Quantity={}, Available={}",
            productId, quantity, inventory.getAvailableQuantity());
    }

    /**
     * 检查可用库存
     */
    @Transactional(readOnly = true)
    public boolean checkInventory(Long productId, Integer quantity) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);

        if (inventoryOpt.isEmpty()) {
            log.warn("商品が見つかりません: ProductID={}", productId);
            return false;
        }

        Inventory inventory = inventoryOpt.get();
        boolean hasStock = inventory.hasEnoughStock(quantity);

        log.info("在庫チェック: ProductID={}, RequestedQty={}, AvailableQty={}, Result={}",
            productId, quantity, inventory.getAvailableQuantity(), hasStock);

        return hasStock;
    }

    /**
     * 增加库存（旧方法保留）
     */
    @Transactional
    public void increaseInventory(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ProductID=" + productId));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);

        log.info("在庫を増やしました: ProductID={}, IncreasedQty={}, CurrentQty={}",
            productId, quantity, inventory.getQuantity());
    }

}
