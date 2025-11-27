package org.khr.microservice.common.intercetor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.common.constant.TokenConstant;
import org.khr.microservice.common.context.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {
        String token = request.getHeader(TokenConstant.X_USERID);
        String FUCK = request.getHeader("FUCK");
        if (!"caonimadebi".equals(FUCK)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden");
            return false;
        }
        if (token == null) {
            log.warn("X_USERID header missing");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        log.info("X_USERID: {}", token);
        UserContext.setUser(token);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception ex) {
        UserContext.clear();
    }
}
