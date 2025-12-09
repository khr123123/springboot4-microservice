package org.khr.microservice.service;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.common.context.UserContext;
import org.khr.microservice.inventory.api.InventoryService;
import org.khr.microservice.model.Order;
import org.khr.microservice.repository.OrderRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 注文サービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final StreamBridge streamBridge;
    private final RedissonClient redisson;

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
        // 获取锁对象
        RLock lock = redisson.getLock(lockKey);
        try {
            if (!lock.tryLock(1, 30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("系统繁忙，请稍后再试");
            }
            boolean reserved = inventoryService.checkInventory(order.getProductId(), order.getQuantity());
            if (!reserved) {
                throw new IllegalStateException("在庫が不足しています（预扣失败）");
            }
            inventoryService.reduceInventory(order.getProductId(), order.getQuantity());
            // 保存订单
            order.setStatus(Order.OrderStatus.PENDING);
            Order savedOrder = orderRepository.save(order);
            log.info("订单保存成功: OrderID={}", savedOrder.getId());
            return savedOrder;
        } catch (InterruptedException e) {
            throw new RuntimeException("订单创建失败:" + e.getMessage());
        } catch (Exception e) {
            // 预扣失败或其他异常，事务会自动回滚
            log.error("订单创建失败: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
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
