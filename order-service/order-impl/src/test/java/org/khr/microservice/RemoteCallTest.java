package org.khr.microservice;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.khr.microservice.api.InventoryService;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

/**
 @author KK
 @create 2025-11-25-11:50
 */
@SpringBootTest
public class RemoteCallTest {


    @Resource
    private InventoryService echoService;

    @Test
    void call() {
    }

}
