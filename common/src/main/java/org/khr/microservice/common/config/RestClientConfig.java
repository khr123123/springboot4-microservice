package org.khr.microservice.common.config;

import io.seata.core.context.RootContext;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClient() {
        return RestClient.builder()
            .requestInterceptor((request, body, execution) -> {
                String xid = RootContext.getXID();
                if (xid != null) {
                    request.getHeaders().add(RootContext.KEY_XID, xid);
                }
                return execution.execute(request, body);
            });
    }
}
