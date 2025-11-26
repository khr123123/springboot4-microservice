package org.khr.microservice.config;

import org.khr.microservice.api.InventoryService;
import org.khr.microservice.context.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    /**
     * 使用负载均衡的 RestClient.Builder
     */
    @Bean
    public InventoryService inventoryClient(RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder
            .baseUrl("http://inventory-service/api/inventory")
            .requestInterceptor((request, body, execution) -> {
                // 从 ThreadLocal 获取 token 并添加到请求头
                String token = UserContext.getUser();
                if (token != null && !token.isBlank()) {
                    request.getHeaders().set("Authorization",token);
                }
                request.getHeaders().set("X-Caller-Service", "order-service");
                return execution.execute(request, body);
            })
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(adapter)
            .build();

        return factory.createClient(InventoryService.class);
    }
}
