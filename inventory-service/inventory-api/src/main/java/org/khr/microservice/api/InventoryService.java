package org.khr.microservice.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

// 移除基础路径，因为在 HttpClientConfig 中已经设置
@HttpExchange
public interface InventoryService {

    @GetExchange("/check/{productId}/{quantity}")
    Boolean checkInventory(@PathVariable Long productId, @PathVariable Integer quantity);

    @PutExchange("/reduce/{productId}/{quantity}")
    void reduceInventory(@PathVariable Long productId, @PathVariable Integer quantity);
}
