package org.khr.microservice.common.intercetor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.common.context.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Set<String> WHITE_LIST = Set.of(
        "/api/users/login",
        "/api/users/register",
        "/actuator/health"
    );

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (WHITE_LIST.contains(path)) {
            return true;
        }
        String user = UserContext.getUser();
        log.info("User: {}", user);
        return true;
    }

}
