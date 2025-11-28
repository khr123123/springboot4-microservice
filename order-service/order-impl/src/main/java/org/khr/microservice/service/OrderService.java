package org.khr.microservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.common.dto.OrderEventDTO;
import org.khr.microservice.common.context.UserContext;
import org.khr.microservice.config.RedisLock;
import org.khr.microservice.inventory.api.InventoryService;
import org.khr.microservice.model.Order;
import org.khr.microservice.repository.OrderRepository;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    @Transactional
    public Order createOrder(Order order) {
        Long userId = Long.valueOf(UserContext.getUser());
        log.info("新規注文を作成: UserID={}, ProductID={}, Quantity={}",
            userId, order.getProductId(), order.getQuantity());
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
            // ✅ 阶段1: 预扣库存
            boolean reserved = inventoryService.reserveInventory(
                order.getProductId(),
                order.getQuantity()
            );

            if (!reserved) {
                throw new IllegalStateException("在庫が不足しています（预扣失败）");
            }

            order.setStatus(Order.OrderStatus.PENDING);

            // 保存订单
            Order savedOrder = orderRepository.save(order);
            log.info("订单保存成功: OrderID={}", savedOrder.getId());

            // ✅ 注册事务同步 - 成功后发送确认消息
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        try {
                            log.info("订单事务提交成功，发送确认消息: OrderID={}", savedOrder.getId());

                            // 发送确认消息
                            OrderEventDTO event = new OrderEventDTO();
                            event.setOrderId(savedOrder.getId());
                            event.setProductId(savedOrder.getProductId());
                            event.setQuantity(savedOrder.getQuantity());
                            event.setEventType("ORDER_CONFIRMED");  // ✅ 确认事件

                            streamBridge.send("orderOutput-out-0", event);

                        } catch (Exception e) {
                            log.error("确认消息发送失败: OrderID={}", savedOrder.getId(), e);
                            // TODO: 记录到补偿表，定时重试
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            log.warn("订单事务回滚，发送取消消息: OrderID={}", savedOrder.getId());

                            try {
                                // ✅ 发送取消消息
                                OrderEventDTO event = new OrderEventDTO();
                                event.setOrderId(savedOrder.getId());
                                event.setProductId(savedOrder.getProductId());
                                event.setQuantity(savedOrder.getQuantity());
                                event.setEventType("ORDER_CANCELLED");  // ✅ 取消事件

                                streamBridge.send("orderOutput-out-0", event);

                            } catch (Exception e) {
                                log.error("取消消息发送失败，需手动回滚库存: OrderID={}", savedOrder.getId(), e);
                                // TODO: 记录到补偿表
                            }
                        }
                    }
                }
            );

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
