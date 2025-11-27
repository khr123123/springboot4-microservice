package org.khr.microservice.common.config;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.khr.microservice.common.constant.TokenConstant;
import org.khr.microservice.common.context.UserContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String token = request.getHeader(TokenConstant.X_USERID);
        // 没有 token：拒绝访问 + 返回异常信息
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Missing X-USERID header\"}");
            return; // 不要继续过滤链
        }
        // 有 token：进入 ScopedValue 上下文
        UserContext.run(token, () -> {
            try {
                chain.doFilter(req, res);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
