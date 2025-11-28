package org.khr.microservice.common.dto;

import lombok.Data;

/**
 * 订单 DTO - 用于消息传递
 */
@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String status;
}
