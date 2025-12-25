package com.vblog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Interceptor Configuration
 * Register JWT interceptor and configure excluded paths
 * 
 * @author V-Blog Team
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * Paths that don't require authentication
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/user/register",
            "/user/login",
            "/uploads/**",
            "/file/upload/image",
            "/file/upload/video"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}

