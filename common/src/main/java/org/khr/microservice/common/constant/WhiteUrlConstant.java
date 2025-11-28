package org.khr.microservice.common.constant;

import java.util.Set;

/**
 @author KK
 @create 2025-11-28-11:29
 */
public interface WhiteUrlConstant {

    Set<String> WHITE_LIST = Set.of(
        "/api/users/login",
        "/api/users/register"
    );
}
