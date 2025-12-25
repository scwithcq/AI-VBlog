# 图片上传与显示处理指南

## 📋 后端逻辑说明

后端接收文件上传后：

1. 生成唯一文件名：`uuid.jpg`
2. 保存到物理路径：`Q:\gr_work\AI_work\V-Blog-Project\VBlogbackend\src\main\resources\vblog_upload\uuid.jpg`
3. 返回给前端的 URL 路径：`/image/uuid.jpg`（注意：不是物理路径！）

## 🔧 前端处理方案

### 1. 图片工具函数 (`src/utils/image.js`)

已创建完整的图片处理工具函数：

```javascript
import {
  getImageUrl,
  parseImages,
  isValidImage,
  isValidImageSize,
  compressImage,
} from "@/utils/image";

// 获取完整图片URL
const fullUrl = getImageUrl("/image/uuid.jpg");
// 结果：http://localhost:8080/image/uuid.jpg

// 解析图片数组（自动处理 JSON 字符串）
const imageUrls = parseImages('["<boltExport>image/001.jpg","/image/002.jpg"]');
// 结果：['http://localhost:8080/image/001.jpg', 'http://localhost:8080/image/002.jpg']
```

### 2. 文件上传 API (`src/api/file.js`)

```javascript
import { uploadImage } from "@/api/file";

// 上传图片
const file = event.target.files[0];
const result = await uploadImage(file);
// result.url = "/image/uuid.jpg"
```

### 3. 组件中使用示例

#### PostEditor 组件（发布微博）

```vue
<script setup>
import { uploadImage } from "../api/file";
import { getImageUrl } from "../utils/image";

const images = ref([]);

const handleImageUpload = async (file) => {
  try {
    const result = await uploadImage(file.raw);
    // 后端返回: { url: "/image/uuid.jpg" }
    images.value.push(result.url); // 存储相对路径
    ElMessage.success("图片上传成功");
  } catch (error) {
    ElMessage.error("图片上传失败");
  }
};
</script>

<template>
  <!-- 预览时转换为完整URL -->
  <img
    v-for="(image, index) in images"
    :key="index"
    :src="getImageUrl(image)"
    alt="preview"
  />
</template>
```

#### PostCard 组件（显示微博）

```vue
<script setup>
import { parseImages } from "../utils/image";

const props = defineProps({
  post: { type: Object, required: true },
});

// 自动解析并转换为完整URL
const images = computed(() => {
  return parseImages(props.post.images);
});
</script>

<template>
  <el-image
    v-for="(image, index) in images"
    :key="index"
    :src="image"
    fit="cover"
  />
</template>
```

### 4. 图片验证

```javascript
import { isValidImage, isValidImageSize } from "@/utils/image";

const beforeUpload = (file) => {
  // 验证文件类型
  if (!isValidImage(file)) {
    ElMessage.error("只能上传 JPG/PNG/GIF/WEBP 格式的图片");
    return false;
  }

  // 验证文件大小（默认5MB）
  if (!isValidImageSize(file, 5)) {
    ElMessage.error("图片大小不能超过 5MB");
    return false;
  }

  return true;
};
```

### 5. 图片压缩（可选）

```javascript
import { compressImage } from "@/utils/image";

const handleImageUpload = async (file) => {
  try {
    // 压缩大图片（超过1920px宽度，质量80%）
    const compressedBlob = await compressImage(file.raw, 1920, 0.8);
    const compressedFile = new File([compressedBlob], file.raw.name, {
      type: file.raw.type,
    });

    // 上传压缩后的文件
    const result = await uploadImage(compressedFile);
    images.value.push(result.url);
  } catch (error) {
    ElMessage.error("图片处理失败");
  }
};
```

## 🌍 环境配置

### 开发环境 (`.env.development`)

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 生产环境 (`.env.production`)

```env
VITE_API_BASE_URL=http://your-production-domain.com
```

## 📝 数据库存储格式

**数据库中存储的是相对路径（不是完整 URL）：**

```json
{
  "postId": 123,
  "content": "这是一条微博",
  "images": "[\"<boltExport>image/uuid-001.jpg\",\"/image/uuid-002.jpg\"]",
  "createTime": "2025-12-21 10:00:00"
}
```

**前端显示时自动转换为完整 URL：**

```javascript
// 原始数据
const post = {
  images: '["<boltExport>image/001.jpg","/image/002.jpg"]',
};

// 使用工具函数解析
const imageUrls = parseImages(post.images);
// ['http://localhost:8080/image/001.jpg', 'http://localhost:8080/image/002.jpg']
```

## ✅ 已更新的组件

- ✅ `src/utils/image.js` - 图片工具函数（新建）
- ✅ `src/components/PostCard.vue` - 微博卡片显示
- ✅ `src/components/PostEditor.vue` - 发布微博编辑器
- ✅ `src/components/RepostDialog.vue` - 转发对话框
- ✅ `.env.development` - 开发环境配置
- ✅ `.env.production` - 生产环境配置

## 🔍 调试技巧

### 检查图片是否正确加载

```javascript
// 在浏览器控制台查看
console.log("原始路径:", post.images);
console.log("解析后:", parseImages(post.images));
console.log("完整URL:", getImageUrl("/image/uuid.jpg"));
```

### 检查后端返回

```javascript
const result = await uploadImage(file);
console.log("后端返回:", result);
// 应该看到: { url: "/image/uuid.jpg" }
```

### 检查网络请求

1. 打开浏览器开发者工具 → Network
2. 上传图片，查看 POST `/api/file/upload/image` 请求
3. 响应应该包含：`{ code: 200, message: "图片上传成功", data: { url: "/image/uuid.jpg" } }`
4. 查看图片加载，应该请求：`http://localhost:8080/image/uuid.jpg`

## 🚨 常见问题

### 1. 图片显示 404

**原因**：后端静态资源映射未配置

**解决**：确保后端有如下配置：

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:Q:/gr_work/AI_work/V-Blog-Project/VBlogbackend/src/main/resources/vblog_upload/");
    }
}
```

### 2. 图片路径错误

**问题**：前端显示 `http://localhost:8080//image/uuid.jpg`（双斜杠）

**原因**：路径拼接问题

**解决**：使用 `getImageUrl()` 函数自动处理

### 3. CORS 跨域问题

**问题**：开发环境图片无法加载

**解决**：确保 `vite.config.js` 中配置了代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 📚 相关文件

- `src/utils/image.js` - 图片处理工具
- `src/api/file.js` - 文件上传 API
- `vite.config.js` - Vite 代理配置
- `.env.development` - 开发环境变量
- `.env.production` - 生产环境变量
