package org.khr.microservice.common.context;

public class UserContext {

    private static final ThreadLocal<String> USER = new ThreadLocal<>();

    public static void setUser(String user) {
        USER.set(user);
    }

    public static String getUser() {
        return USER.get(); // 不会再抛异常，只可能是 null
    }

    public static void clear() {
        USER.remove(); // 防止内存泄漏（非常重要）
    }
}
