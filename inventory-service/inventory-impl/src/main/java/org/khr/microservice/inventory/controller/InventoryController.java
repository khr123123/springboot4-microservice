package org.khr.microservice.inventory.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.inventory.model.Inventory;
import org.khr.microservice.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在庫コントローラー
 * Spring Boot 4の新機能を活用
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@Valid @RequestBody Inventory inventory,
        HttpServletRequest httpServletRequest) {
        String authorization = httpServletRequest.getHeader("Authorization");
        log.info("Authorization: {}", authorization);
        Inventory createdInventory = inventoryService.createInventory(inventory);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(
        @PathVariable Long id,
        @Valid @RequestBody Inventory inventory) {
        Inventory updatedInventory = inventoryService.updateInventory(id, inventory);
        return ResponseEntity.ok(updatedInventory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 在庫チェックAPI（注文サービスから呼ばれる）
     */
    @GetMapping("/check/{productId}/{quantity}")
    public ResponseEntity<Boolean> checkInventory(
        @PathVariable Long productId,
        @PathVariable Integer quantity) {
        boolean available = inventoryService.checkInventory(productId, quantity);
        return ResponseEntity.ok(available);
    }

    /**
     * 在庫削減API（注文サービスから呼ばれる）
     */
    @PutMapping("/reduce/{productId}/{quantity}")
    public ResponseEntity<Boolean> reduceInventory(
        @PathVariable Long productId,
        @PathVariable Integer quantity, HttpServletRequest httpServletRequest) {
        String authorization = httpServletRequest.getHeader("Authorization");
        log.info("Authorization: {}", authorization);
        return ResponseEntity.ok(inventoryService.reserveInventory(productId, quantity));
    }

    /**
     * 在庫追加API
     */
    @PutMapping("/increase/{productId}/{quantity}")
    public ResponseEntity<Void> increaseInventory(
        @PathVariable Long productId,
        @PathVariable Integer quantity) {
        inventoryService.increaseInventory(productId, quantity);
        return ResponseEntity.ok().build();
    }
}
