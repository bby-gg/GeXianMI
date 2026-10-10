package com.gexianmi.config;

import com.gexianmi.common.UserContext;
import com.gexianmi.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * JWT 拦截器，放行登录、注册、传感器数据上报接口，校验其余 /api/** 请求的 Token。
 * 对于标注了 @RequireAdmin 注解的方法，额外校验管理员角色。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * 请求预处理，验证 Token 有效性和管理员权限。
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  处理器
     * @return true 表示放行，false 表示拦截
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // OPTIONS 预检请求直接放行，避免跨域请求被拦截
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        // 白名单路径：登录、注册、传感器数据上报无需 Token 校验
        if (uri.contains("/api/auth/login") || uri.contains("/api/auth/register")
                || uri.contains("/api/environment/data")) {
            return true;
        }

        // 从 Authorization 请求头提取 Bearer Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validate(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token 已过期，请重新登录\",\"data\":null}");
            return false;
        }

        // 解析 Token → 设入 UserContext（业务层随时可取，无需层层传参）
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        UserContext.set(userId, username, role);
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        // 检查目标方法是否标注了 @RequireAdmin，若是则校验管理员角色
        if (handler instanceof HandlerMethod hm) {
            Method method = hm.getMethod();
            if (method.isAnnotationPresent(RequireAdmin.class)) {
                if (!"admin".equals(role)) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"msg\":\"权限不足，仅管理员可操作\",\"data\":null}");
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
