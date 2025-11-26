package org.khr.microservice.intercetor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.context.UserContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Configuration
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        log.info("=== セキュリティチェック開始 ===");
        log.info("URI: {}", request.getRequestURI());
        log.info("Token: {}", token);
        UserContext.setUser(token);
        // 没有 Token
        //if (token == null || token.isBlank()) {
        //    log.warn("未認証リクエスト: Token がありません");
        //    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //    return false;
        //}
        return true;
    }

}
