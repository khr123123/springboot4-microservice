package org.khr.microservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 @author KK
 @create 2025-11-27-09:19
 */
@SpringBootTest(
    properties = {
        "gateway.white-list=/auth/login,/auth/register"
    },
    classes = WhiteListTest.class // 不加载整个项目
)
public class WhiteListTest {

    @Value("${gateway.white-list}")
    private String[] whiteList;

    @Test
    void contextLoads() {
        System.out.println("whiteList = " + whiteList);
    }

}
