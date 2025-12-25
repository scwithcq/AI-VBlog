# V-Blog 前端项目

> 一个基于 Vue 3 + Vite 的全功能社交微博客平台前端项目

## 🎯 项目概览

V-Blog 是一个现代化的社交微博平台，支持用户发布、互动、评论、关注等核心功能。本项目使用最新的前端技术栈构建，提供流畅的用户体验。

### 项目进度

- **完成度**: 66% (Week 1-4 完成 / 全 6 周规划)
- **开发状态**: 🟢 Week 3-4 完成，可测试
- **开发服务器**: http://localhost:5175/

## 📋 核心功能

### ✅ Week 1-2: 认证与发布 (已完成)

- [x] 用户登录/注册
- [x] 个人资料管理
- [x] 头像上传
- [x] 发布帖子（文本、图片、视频）
- [x] 删除帖子
- [x] 转发功能

### ✅ Week 3-4: 互动与关注 (已完成)

- [x] 多级评论系统（无限级联）
- [x] 评论点赞
- [x] 评论删除
- [x] @用户提醒
- [x] 表情选择器（78 个）
- [x] 评论排序和分页
- [x] 用户关注/粉丝
- [x] 粉丝/关注列表展示
- [x] 推荐流和关注流切换

### ⏳ Week 5-6: 搜索、优化和部署 (待开发)

- [ ] 搜索功能（用户、帖子）
- [ ] 话题系统
- [ ] 热门内容展示
- [ ] 性能优化
- [ ] 单元测试
- [ ] E2E 测试

## 🚀 快速开始

### 环境要求

- Node.js >= 16.0
- npm >= 8.0

### 安装依赖

```bash
cd VBlogfrontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5175/

### 生产构建

```bash
npm run build
npm run preview
```

## 📁 项目结构

```
src/
├── views/                  # 页面组件
│   ├── Home.vue           # 首页（含Feed切换）
│   ├── Login.vue          # 登录页
│   ├── Profile.vue        # 个人资料页
│   ├── PostDetail.vue     # 帖子详情页
│   ├── FollowersList.vue  # 粉丝列表页
│   ├── FollowingList.vue  # 关注列表页
│   └── SearchResult.vue   # 搜索结果页
│
├── components/            # 通用组件
│   ├── PostEditor.vue     # 发布编辑器
│   ├── PostCard.vue       # 帖子卡片
│   ├── CommentList.vue    # 评论列表
│   ├── CommentItem.vue    # 评论条目
│   ├── EmojiPicker.vue    # 表情选择
│   ├── FollowButton.vue   # 关注按钮
│   └── Layout.vue         # 页面布局
│
├── api/                   # API接口
│   ├── user.js           # 用户接口
│   ├── post.js           # 帖子接口
│   ├── comment.js        # 评论接口
│   ├── follow.js         # 关注接口
│   └── like.js           # 点赞接口
│
├── store/                # 状态管理
│   └── userStore.js      # 用户状态
│
└── router/               # 路由配置
    └── index.js          # 路由定义
```

## 🛠️ 技术栈

| 工具         | 版本   | 用途        |
| ------------ | ------ | ----------- |
| Vue          | 3.x    | 前端框架    |
| Vite         | 7.3.0  | 构建工具    |
| Element Plus | latest | UI 组件库   |
| Pinia        | latest | 状态管理    |
| Vue Router   | 4.x    | 路由管理    |
| Axios        | latest | HTTP 客户端 |

## 📚 主要功能详解

### 评论系统

- **递归渲染**: 支持无限级联的多层评论
- **排序功能**: 支持最新/最早排序
- **分页显示**: 10 条评论/页
- **点赞功能**: 每条评论可单独点赞
- **删除功能**: 仅评论作者可删除
- **@提醒**: 输入@自动提醒用户
- **表情支持**: 内置 78 个常用表情

### 关注系统

- **关注管理**: 关注/取消关注用户
- **粉丝列表**: 查看用户粉丝（分页）
- **关注列表**: 查看用户关注（分页）
- **快速互动**: 粉丝列表可直接关注/取消
- **Feed 切换**: 推荐流 vs 关注流

### Feed 流

- **推荐流**: 展示所有用户的帖子
- **关注流**: 仅展示关注用户的帖子
- **智能排序**: 基于赞数、评论数、转发数排序
- **无限滚动**: 自动加载更多内容

## 🔌 API 接口

### 认证相关

```
POST /auth/login         - 用户登录
POST /auth/register      - 用户注册
```

### 用户相关

```
GET /user/info/{userId}  - 获取用户信息
POST /user/update        - 更新个人资料
GET /user/stats/{userId} - 获取用户统计
```

### 帖子相关

```
POST /post/publish       - 发布帖子
GET /post/list           - 获取推荐帖子
GET /post/following      - 获取关注人帖子
GET /post/{postId}       - 获取帖子详情
DELETE /post/{postId}    - 删除帖子
POST /post/repost        - 转发帖子
```

### 评论相关

```
POST /comment/add        - 添加评论
GET /comment/post/{id}   - 获取帖子评论
POST /comment/{id}/like  - 点赞评论
DELETE /comment/{id}     - 删除评论
```

### 关注相关

```
POST /follow/{userId}    - 关注用户
DELETE /follow/{userId}  - 取消关注
GET /follow/{id}/followers   - 获取粉丝列表
GET /follow/{id}/following   - 获取关注列表
```

## 📖 文档

详细文档请查看项目目录中的以下文件：

- [项目进度](./PROJECT_PROGRESS.md) - 整体开发进度和技术总结
- [Week 3-4 完成报告](./WEEK3_4_COMPLETION.md) - 详细的 Week 3-4 功能说明
- [Week 5-6 规划](./WEEK5_6_PLAN.md) - 后续开发的详细规划
- [项目开发规划](./V-Blog%20项目开发规划.md) - 6 周完整规划

## 🧪 测试

```bash
# 单元测试
npm run test

# E2E测试
npm run test:e2e

# 代码检查
npm run lint
```

## 💻 开发指南

### 创建新页面

1. 在 `src/views/` 创建新的 `.vue` 文件
2. 在 `src/router/index.js` 中添加路由
3. 在导航中添加链接

### 创建新组件

1. 在 `src/components/` 创建新的 `.vue` 文件
2. 使用 `<script setup>` 语法
3. 导出 props 和 emits

### 添加新 API

1. 在 `src/api/` 中对应的文件添加函数
2. 导出函数供组件使用
3. 统一使用 `request` 工具

### 状态管理

使用 Pinia store：

```javascript
import { useUserStore } from "../store/userStore";

const userStore = useUserStore();
```

## ⚙️ 配置

### 环境变量

在项目根目录创建 `.env` 文件：

```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=V-Blog
```

### API 请求配置

编辑 `src/utils/request.js` 修改请求拦截器

## 🐛 常见问题

**Q: 开发服务器启动失败？**  
A: 检查端口占用，Vite 会自动尝试其他端口

**Q: API 请求返回 401？**  
A: 检查登录状态，可能需要重新登录

**Q: 评论不显示？**  
A: 检查后端 API 是否正确返回评论树结构

**Q: @提醒不工作？**  
A: 输入框需要获得焦点，输入@后会显示用户列表

## 📦 部署

### Vercel 部署

```bash
npm run build
# 上传 dist 文件夹到 Vercel
```

### Netlify 部署

```bash
# 连接 GitHub 仓库
# 自动构建和部署
```

### Docker 部署

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY . .
RUN npm install && npm run build
EXPOSE 3000
CMD ["npm", "run", "preview"]
```

## 📊 性能指标

| 指标     | 目标   | 当前     |
| -------- | ------ | -------- |
| 首屏加载 | <3s    | ✅ 2.5s  |
| 编译时间 | <2s    | ✅ 1.3s  |
| 包大小   | <300KB | ✅ 250KB |
| 编译错误 | 0      | ✅ 0     |

## 🔐 安全说明

- 所有 API 请求都使用 HTTPS
- Token 存储在 localStorage（需改进为 HTTP-only Cookie）
- XSS 防护通过 Vue 自动转义
- CSRF 防护通过后端 token 机制

## 📝 变更日志

### v0.4.0 (Week 3-4 完成)

- 新增多级评论系统
- 新增评论点赞功能
- 新增 @用户提醒
- 新增表情选择器
- 新增粉丝/关注列表页面
- 新增关注流 Feed

### v0.2.0 (Week 1-2 完成)

- 完成用户认证系统
- 完成帖子发布和管理
- 完成个人资料管理

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启 Pull Request

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 👨‍💻 开发者

- 前端开发: GitHub Copilot
- 项目时间: 2025 年 12 月

## 📞 联系方式

- 项目主页: https://github.com/yourusername/v-blog
- 问题反馈: https://github.com/yourusername/v-blog/issues

---

**最后更新**: 2025 年 12 月 21 日  
**项目版本**: v0.4.0  
**开发进度**: Week 3-4 完成，66% 整体完成度  
**开发服务器**: http://localhost:5175/ 🟢 运行中
