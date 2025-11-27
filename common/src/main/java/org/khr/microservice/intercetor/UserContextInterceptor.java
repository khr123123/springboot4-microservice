package org.khr.microservice.intercetor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.khr.microservice.context.UserContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Configuration
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    @NullMarked
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("X-UserId");
        UserContext.setUser(token);
        return true;
    }

}
