package org.khr.microservice.common.context;

/**
 * @author KK
 * @create 2025-11-26-14:53
 */
public class UserContext {

    // 用户上下文（不可变，更安全）
    private static final ScopedValue<String> USER = ScopedValue.newInstance();

    // 设置上下文（必须使用 runWhere 包裹作用域）
    public static void run(String userId, Runnable runnable) {
        ScopedValue.where(USER, userId).run(runnable);
    }

    // 读取上下文
    public static String getUser() {
        return USER.get();
    }
}
