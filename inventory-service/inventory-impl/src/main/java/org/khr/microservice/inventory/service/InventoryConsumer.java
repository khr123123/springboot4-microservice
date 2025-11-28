package org.khr.microservice.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.common.dto.OrderEventDTO;
import org.khr.microservice.inventory.service.InventoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 库存消费者 - 监听订单事件
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public Consumer<Message<String>> orderInput() {
        return message -> {
            try {
                // 1. 幂等性检查
                String messageId = Objects.requireNonNull(message.getHeaders().getId()).toString();
                String idempotentKey = "order_event_processed:" + messageId;

                Boolean isFirstTime = redisTemplate.opsForValue()
                    .setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);

                if (Boolean.FALSE.equals(isFirstTime)) {
                    log.warn("重复消息，跳过处理: MessageID={}", messageId);
                    return;
                }

                // 2. 解析事件
                String payload = message.getPayload();
                OrderEventDTO event = objectMapper.readValue(payload, OrderEventDTO.class);

                log.info("收到订单事件: Type={}, OrderID={}, ProductID={}, Quantity={}",
                    event.getEventType(), event.getOrderId(), event.getProductId(), event.getQuantity());

                // 3. 根据事件类型处理
                switch (event.getEventType()) {
                    case "ORDER_CONFIRMED":
                        // ✅ 确认扣减库存
                        inventoryService.confirmInventory(event.getProductId(), event.getQuantity());
                        log.info("库存确认扣减成功: OrderID={}, ProductID={}",
                            event.getOrderId(), event.getProductId());
                        break;

                    case "ORDER_CANCELLED":
                        // ✅ 取消预扣，释放库存
                        inventoryService.cancelInventory(event.getProductId(), event.getQuantity());
                        log.info("库存预扣取消成功: OrderID={}, ProductID={}",
                            event.getOrderId(), event.getProductId());
                        break;

                    default:
                        log.warn("未知事件类型: {}", event.getEventType());
                }

            } catch (Exception e) {
                log.error("处理订单事件失败: {}", e.getMessage(), e);
                // ⚠️ 抛出异常触发重试
                throw new RuntimeException("库存操作失败", e);
            }
        };
    }
}
