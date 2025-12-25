package com.vblog.config;

import com.alibaba.fastjson2.JSON;
import com.vblog.common.Result;
import com.vblog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * JWT拦截器
 * 验证请求头中的JWT令牌
 * 
 * @author V-Blog团队
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS请求用于CORS预检
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        try {
            // 从请求头获取令牌
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                // 未提供令牌 - 返回401
                return writeUnauthorizedResponse(response, "未提供认证令牌");
            }

            // 移除 "Bearer " 前缀（如果存在）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 验证令牌
            if (!jwtUtil.validateToken(token)) {
                return writeUnauthorizedResponse(response, "登录已过期，请重新登录");
            }

            // 将用户信息存储到请求属性中
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            return true;
        } catch (Exception e) {
            log.error("Failed to process token: ", e);
            return writeUnauthorizedResponse(response, "令牌解析失败");
        }
    }

    /**
     * 写入统一的未授权响应
     */
    private boolean writeUnauthorizedResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Result<?> result = Result.unauthorized(message);
            String json = JSON.toJSONString(result);

            PrintWriter writer = response.getWriter();
            writer.print(json);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.error("Failed to write response: ", e);
        }
        return false;
    }
}
