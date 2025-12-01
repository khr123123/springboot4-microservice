package org.khr.microservice;

import io.seata.spring.boot.autoconfigure.SeataSpringFenceAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication(exclude = {SeataSpringFenceAutoConfiguration.class})
public class OrderServiceApplication {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return _ -> {
            log.info("Order Service Started on port {}", env.getProperty("khr.fuck"));
        };
    }


}
