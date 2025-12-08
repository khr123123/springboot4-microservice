package org.khr.microservice.common.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.khr.microservice.common.context.UserContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class UserContextFilter implements Filter {

    private static final Set<String> WHITE_LIST = Set.of(
        "/api/users/login",
        "/api/users/register",
        "/actuator/health"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        // ✅ ① 白名单直接放行
        if (WHITE_LIST.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        // ✅ ② 读取 Header
        String userId = request.getHeader("X-UserId");

        if (userId == null || userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Missing X-UserId header\"}");
            return;
        }

        try {
            // ✅ ③ 放入 ThreadLocal
            UserContext.setUser(userId);

            // ✅ ④ 正常放行请求
            chain.doFilter(req, res);
        } finally {
            // ✅ ⑤ 必须清理，防止线程复用串数据（超级重要）
            UserContext.clear();
        }
    }
}
