package org.khr.microservice.service;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.common.context.UserContext;
import org.khr.microservice.config.RedisLock;
import org.khr.microservice.inventory.api.InventoryService;
import org.khr.microservice.model.Order;
import org.khr.microservice.repository.OrderRepository;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 注文サービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final RedisLock redisLock;
    private final StreamBridge streamBridge;

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

    @GlobalTransactional
    public Order createOrder(Order order) {
        Long userId = Long.valueOf(UserContext.getUser());
        order.setUserId(userId);
        String lockKey = "product_lock_" + order.getProductId();
        String lockValue = null;
        while (lockValue == null) {
            lockValue = redisLock.tryLock(lockKey, 10);
            if (lockValue == null) {
                try {
                    Thread.sleep(90);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("线程被中断", e);
                }
            }
        }
        try {
            boolean reserved = inventoryService.checkInventory(order.getProductId(), order.getQuantity());
            if (!reserved) {
                throw new IllegalStateException("在庫が不足しています（预扣失败）");
            }
            inventoryService.reduceInventory(order.getProductId(), order.getQuantity());
           String a = null;
           a.toLowerCase();
            // 保存订单
            order.setStatus(Order.OrderStatus.PENDING);
            Order savedOrder = orderRepository.save(order);
            log.info("订单保存成功: OrderID={}", savedOrder.getId());
            return savedOrder;
        } catch (Exception e) {
            // 预扣失败或其他异常，事务会自动回滚
            log.error("订单创建失败: {}", e.getMessage(), e);
            throw e;

        } finally {
            redisLock.unlock(lockKey, lockValue);
        }
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
}
