package org.khr.microservice.common.dto;

import lombok.Data;

/**
 * 订单事件 DTO
 */
@Data
public class OrderEventDTO {
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String eventType;  // ORDER_CONFIRMED, ORDER_CANCELLED
}
