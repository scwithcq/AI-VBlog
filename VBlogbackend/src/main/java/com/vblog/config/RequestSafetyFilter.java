package com.vblog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/** Bounded, process-local abuse protection; forwarding headers are deliberately not trusted. */
@Component
public class RequestSafetyFilter extends OncePerRequestFilter {
    private final Map<String, Window> attempts = new HashMap<>();
    private record Window(long expires, int count) {}

    private synchronized boolean allow(String key, int max) {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(e -> e.getValue().expires < now);
        Window old = attempts.get(key);
        if (old == null && attempts.size() >= 20000) return false;
        Window next = new Window(old == null ? now + 600000 : old.expires, old == null ? 1 : old.count + 1);
        attempts.put(key, next);
        return next.count <= max;
    }

    public boolean allowWrite(Long userId, boolean upload) {
        return allow((upload ? "upload:" : "write:") + userId, upload ? 20 : 120);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        String path = request.getServletPath();
        if (!path.startsWith("/uploads/")) response.setHeader("Cache-Control", "no-store");
        if ("POST".equals(request.getMethod()) && (path.equals("/user/login") || path.equals("/user/register"))) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            if (username != null && username.length() > 50 || password != null && password.length() > 256) {
                response.sendError(400); return;
            }
            boolean registration = path.endsWith("register");
            String account = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
            if (!allow(path + request.getRemoteAddr(), registration ? 20 : 100)
                    || !registration && !allow("account:" + account, 20)) {
                response.setStatus(429);
                response.setHeader("Retry-After", "600");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"msg\":\"操作过于频繁，请稍后重试\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
