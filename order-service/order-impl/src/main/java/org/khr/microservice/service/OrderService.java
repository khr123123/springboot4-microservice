package org.khr.microservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.api.InventoryService;
import org.khr.microservice.context.UserContext;
import org.khr.microservice.model.Order;
import org.khr.microservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 注文サービス
 * Spring 7の新機能: リアクティブプログラミング統合
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        log.info("全注文を取得");
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        log.info("注文を取得: ID={}", id);
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        log.info("ユーザーの注文を取得: UserID={}", userId);
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Order createOrder(Order order) {
        Long userId = Long.valueOf(UserContext.getUser());
        log.info("新規注文を作成: UserID={}, ProductID={}, Quantity={}", userId, order.getProductId(),
            order.getQuantity());
        order.setUserId(userId);
        log.info("inventoryService示例 {}", inventoryService.hashCode());
        // 在庫サービスをチェック（Spring WebClientを使用）
        boolean inventoryAvailable = inventoryService.checkInventory(order.getProductId(), order.getQuantity());

        if (!inventoryAvailable) {
            throw new IllegalStateException("在庫が不足しています");
        }

        order.setStatus(Order.OrderStatus.PENDING);
        log.info("新規注文を作成: ProductID={}, Quantity={}", order.getProductId(), order.getQuantity());

        Order savedOrder = orderRepository.save(order);

        // 在庫を減らす
        inventoryService.reduceInventory(order.getProductId(), order.getQuantity());

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("注文が見つかりません: ID=" + id));

        order.setStatus(status);
        log.info("注文ステータスを更新: ID={}, Status={}", id, status);

        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("注文が見つかりません: ID=" + id);
        }
        log.info("注文を削除: ID={}", id);
        orderRepository.deleteById(id);
    }

//    /**
//     * 在庫サービスに在庫を確認
//     * Spring 7のWebClient（リアクティブ）を使用
//     */
//    private boolean checkInventory(Long productId, Integer quantity) {
//        try {
//            String result = webClientBuilder.build()
//                .get()
//                .uri("http://localhost:8083/api/inventory/check/{productId}/{quantity}",
//                    productId, quantity)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//            return "true".equals(result);
//        } catch (Exception e) {
//            log.error("在庫チェックエラー: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    /**
//     * 在庫を更新
//     */
//    private void updateInventory(Long productId, Integer quantity) {
//        try {
//            webClientBuilder.build()
//                .put()
//                .uri("http://localhost:8083/api/inventory/reduce/{productId}/{quantity}",
//                    productId, quantity)
//                .retrieve()
//                .bodyToMono(Void.class)
//                .block();
//        } catch (Exception e) {
//            log.error("在庫更新エラー: {}", e.getMessage());
//        }
//    }
}
