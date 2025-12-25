package com.vblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * Configure CORS and static resources
 *
 * @author V-Blog Team
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Configure CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前项目根目录
        String projectPath = System.getProperty("user.dir");

        // 统一路径分隔符
        projectPath = projectPath.replace("\\", "/");

        // 构建上传文件的物理路径
        // 注意：addResourceLocations 需要 "file:" 前缀，且路径末尾必须有 "/"
        // Windows 下建议使用 "file:///" + 盘符
        String uploadPath = "file:///" + projectPath + "/src/main/resources/vblog_upload/";

        // 映射规则：
        // 前端请求: http://localhost:8080/uploads/images/abc.jpg
        // 实际映射到: 项目根目录/src/main/resources/vblog_upload/images/abc.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);

        System.out.println("✅ 静态资源映射已配置:");
        System.out.println("   URL路径: /uploads/**");
        System.out.println("   物理路径: " + uploadPath);
    }

    /**
     * 配置MultipartResolver，处理文件上传
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }
}