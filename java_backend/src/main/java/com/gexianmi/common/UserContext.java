package com.gexianmi.common;

/**
 * 当前请求用户上下文（ThreadLocal）。
 * <p>
 * JwtInterceptor 在 preHandle 中设置，
 * 业务层任意位置通过 {@link #get()} 获取当前用户信息，
 * 无需逐层传参。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-07-13
 */
public final class UserContext {

    private static final ThreadLocal<Info> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    /** 设置当前请求的用户信息 */
    public static void set(Long userId, String username, String role) {
        HOLDER.set(new Info(userId, username, role));
    }

    /** 获取当前请求的用户信息（未登录时返回 null） */
    public static Info get() {
        return HOLDER.get();
    }

    /** 请求结束后清理，防止内存泄漏 */
    public static void clear() {
        HOLDER.remove();
    }

    // ---- 便捷方法 ----

    public static Long userId() {
        Info u = get();
        return u != null ? u.userId : null;
    }

    public static String username() {
        Info u = get();
        return u != null ? u.username : null;
    }

    public static String role() {
        Info u = get();
        return u != null ? u.role : null;
    }

    public static boolean isAdmin() {
        return "admin".equals(role());
    }

    // ---- 内部数据类 ----

    public record Info(Long userId, String username, String role) {}
}
