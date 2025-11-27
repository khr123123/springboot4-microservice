package org.khr.microservice.common.intercetor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.common.constant.TokenConstant;
import org.khr.microservice.common.context.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(TokenConstant.X_USERID);
        if (token == null) {
            log.warn("X_USERID header missing");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        UserContext.setUser(token);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception ex) {
        UserContext.clear();
    }
}
