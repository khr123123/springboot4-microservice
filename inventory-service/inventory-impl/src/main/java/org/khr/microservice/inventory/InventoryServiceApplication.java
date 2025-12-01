package org.khr.microservice.inventory;

import io.seata.spring.boot.autoconfigure.SeataSpringFenceAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    exclude = {SeataSpringFenceAutoConfiguration.class} // 排除 Fence 自动装配
)public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
