package org.khr.microservice.inventory.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigTestController {

    @Value("${app.message:default-common}")
    private String commonMessage;

    @Value("${inventory.message:default-private}")
    private String privateMessage;

    @GetMapping("/config-test")
    public String testConfig() {
        return "Common: " + commonMessage + "\nPrivate: " + privateMessage;
    }
}
