## 后端静态资源配置指南

### 问题描述
前端需要访问后端上传的图片文件，路径格式为：`/uploads/images/uuid.jpg`

### 后端 Spring Boot 配置

在你的 Spring Boot 项目中添加静态资源映射配置：

```java
package com.example.vblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 请求到物理磁盘路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:Q:/gr_work/AI_work/V-Blog-Project/VBlogbackend/src/main/resources/vblog_upload/");
    }
}
```

### 路径映射说明

| 请求路径 | 物理路径 |
|---------|---------|
| `http://localhost:8080/uploads/images/uuid.jpg` | `Q:\gr_work\AI_work\V-Blog-Project\VBlogbackend\src\main\resources\vblog_upload\images\uuid.jpg` |

### 文件上传接口返回格式

确保文件上传接口返回的路径格式正确：

```java
@PostMapping("/upload/image")
public Result<Map<String, String>> uploadImage(
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request) {
    try {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "." + FileUtil.getSuffix(originalFilename);

        // 保存到物理路径
        File dest = new File("Q:\\gr_work\\AI_work\\V-Blog-Project\\VBlogbackend\\src\\main\\resources\\vblog_upload\\images\\" + fileName);
        file.transferTo(dest);

        // 返回 URL 路径（注意：是 /uploads/images/ 而不是物理路径！）
        String url = "/uploads/images/" + fileName;
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return Result.success("图片上传成功", result);
    } catch (Exception e) {
        return Result.error("上传失败");
    }
}
```

### 前端处理

前端会自动将相对路径转换为完整 URL：

```javascript
// 后端返回
{
  "images": "[\"/uploads/images/uuid.jpg\"]"
}

// 前端自动转换为
http://localhost:8080/uploads/images/uuid.jpg
```

### 验证配置

1. **上传图片测试**：
   - 上传一张图片
   - 查看返回的 URL：`/uploads/images/uuid.jpg`
   - 检查物理文件是否存在

2. **访问图片测试**：
   - 在浏览器直接访问：`http://localhost:8080/uploads/images/uuid.jpg`
   - 应该能看到图片

3. **前端显示测试**：
   - 发布包含图片的微博
   - 刷新页面，图片应该正常显示

### 常见问题

#### 1. 图片 404 Not Found

**原因**：静态资源映射未配置

**解决**：添加上述 `WebMvcConfig` 配置类

#### 2. 图片路径错误

**原因**：文件上传接口返回了物理路径而不是 URL 路径

**错误示例**：
```java
// ❌ 错误：返回物理路径
String url = "Q:\\...\\vblog_upload\\images\\uuid.jpg";
```

**正确示例**：
```java
// ✅ 正确：返回 URL 路径
String url = "/uploads/images/" + fileName;
```

#### 3. CORS 跨域问题

如果遇到跨域问题，在后端添加 CORS 配置：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 目录结构建议

```
VBlogbackend/
└── src/
    └── main/
        └── resources/
            └── vblog_upload/
                └── images/        ← 图片存储目录
                    ├── uuid-1.jpg
                    ├── uuid-2.png
                    └── ...
```

### 生产环境部署

生产环境建议将文件存储到独立的文件服务器或云存储（如阿里云 OSS、腾讯云 COS）：

```java
// 开发环境：本地存储
String url = "/uploads/images/" + fileName;

// 生产环境：云存储
String url = "https://your-cdn.com/images/" + fileName;
```
