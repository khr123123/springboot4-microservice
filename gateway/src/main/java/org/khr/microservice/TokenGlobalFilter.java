package org.khr.microservice;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TokenGlobalFilter implements GlobalFilter, Ordered {

    private static final String TOKEN_SECRET = "your_secret_key";

    /**
     * 白名单从 yml 注入
     */
    private static final Set<String> WHITE_LIST = Set.of(
        "/users/api/users/login",
        "/users/api/users/register"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // ① 白名单直接放行
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // ② 以下都是需要 token 的接口
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || token.isEmpty()) {
            return writeUnauthorized(exchange, "Missing Authorization Header");
        }

        if (!token.startsWith("Bearer ")) {
            return writeUnauthorized(exchange, "Invalid Token Format");
        }

        String rawToken = token.substring(7);

        try {
            // ③ 解析 token
            JWT jwt = JWTUtil.parseToken(rawToken);

            // ④ 验签
            boolean verify = jwt.setKey(TOKEN_SECRET.getBytes()).verify();
            if (!verify) {
                return writeUnauthorized(exchange, "Invalid Token Signature");
            }

            // ⑤ 校验过期时间
            JWTValidator.of(rawToken).validateDate(new Date());

            // ⑥ 提取用户信息
            String userId = jwt.getPayload("userId").toString();

            // ⑦ 下游注入 userId
            ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .header("Authorization", token)
                .header("X-UserId", userId)
                .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            return writeUnauthorized(exchange, "Token Invalid or Expired");
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 白名单判断（路径前缀规则）
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 返回 401 JSON
     */
    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);

        try {
            String jsonStr = JSONUtil.toJsonStr(body);
            byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            return response.setComplete();
        }
    }
}
