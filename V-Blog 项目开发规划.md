## V-Blog 项目开发规划

### 阶段一：数据库设计与基础架构完善

目标：完成数据库表结构创建，完善基础配置

后端任务：

1. 创建数据库建表 SQL 脚本

- tb_user（用户表）

- tb_post（微博表，含 images JSON 字段、source_id 转发字段）

- tb_comment（评论表，支持二级回复）

- tb_topic（话题表，含热度字段）

- tb_follow（关注表）

- tb_post_like（点赞表）

1. 完善配置类

- 跨域配置（WebMvcConfig）

- JWT 拦截器配置

- 文件上传配置（图片/视频）

1. 工具类补充

- 密码加密工具（BCrypt）

- 文件上传工具（FileUtil）

- XSS 过滤工具

前端任务：

1. 完善 Axios 拦截器（请求/响应拦截，Token 管理）

1. 创建 Pinia Store 基础结构

- userStore.js（用户状态管理）

- appStore.js（应用全局状态）

1. 创建基础布局组件

- Layout.vue（三栏布局骨架）

- Header.vue（顶部导航栏）

- Sidebar.vue（左侧导航栏）

------

### 阶段二：用户与鉴权子系统

目标：实现用户注册、登录、个人主页

后端任务（按顺序）：

1. Entity 层

- User.java（继承 BaseEntity，字段映射）

1. Mapper 层

- UserMapper.java（继承 BaseMapper）

1. Service 层

- UserService.java（接口）

- UserServiceImpl.java（实现）

- register()：注册（密码 BCrypt 加密）

- login()：登录（验证密码，生成 JWT）

- getUserInfo()：获取用户信息

- updateUserInfo()：更新用户信息

- getUserStats()：统计关注数、粉丝数、微博数

1. Controller 层

- UserController.java

- POST /user/register：注册

- POST /user/login：登录

- GET /user/info：获取当前用户信息（需 Token）

- PUT /user/info：更新用户信息（需 Token）

- GET /user/{userId}：获取指定用户信息

- GET /user/{userId}/stats：获取用户统计数据

前端任务：

1. API 模块

- src/api/user.js（用户相关 API 调用）

1. Pinia Store

- 完善 userStore.js（登录、登出、用户信息管理）

1. 页面组件

- views/Login.vue（登录页）

- views/Register.vue（注册页）

- views/Profile.vue（个人主页，展示用户信息、微博列表）

1. 路由配置

- 添加登录、注册、个人主页路由

- 配置路由守卫（未登录跳转登录页）

------

### 阶段三：内容发布系统

目标：实现微博发布功能（文本、图片、视频、话题解析）

后端任务（按顺序）：

1. Entity 层

- Post.java（content、images JSON、source_id、video_url 等）

- Topic.java（name、heat）

1. Mapper 层

- PostMapper.java

- TopicMapper.java

1. Service 层

- PostService.java（接口）

- PostServiceImpl.java（实现）

- publishPost()：发布微博

- 解析内容中的 #话题#（正则）

- 更新或创建话题记录，增加热度

- 处理图片 JSON 数组（FastJson2）

- 保存视频 URL

- getPostList()：获取信息流（分页）

- getPostById()：获取单条微博详情

- getUserPosts()：获取指定用户的微博列表

- TopicService.java（接口）

- TopicServiceImpl.java（实现）

- parseTopics()：从内容中提取话题

- updateTopicHeat()：更新话题热度

1. Controller 层

- PostController.java

- POST /post/publish：发布微博（需 Token，文件上传）

- GET /post/list：获取信息流（分页）

- GET /post/{postId}：获取微博详情

- GET /post/user/{userId}：获取用户微博列表

- FileController.java

- POST /file/upload：文件上传（图片/视频）

前端任务：

1. API 模块

- src/api/post.js（微博相关 API）

- src/api/file.js（文件上传 API）

1. 组件开发

- components/PostEditor.vue（发布器组件）

- 文本输入（2000 字限制）

- 图片上传（支持多选，预览）

- 视频上传（MP4）

- 话题输入提示

- 发布按钮

- components/PostCard.vue（微博卡片组件）

- 用户信息展示

- 内容展示（支持 Emoji）

- 图片九宫格展示（1-9 张自适应布局）

- 视频播放

- 话题链接渲染

1. 页面更新

- views/Home.vue（首页）

- 集成 PostEditor

- 信息流列表（使用 PostCard）

- 分页加载

------

### 阶段四：内容展示与信息流

目标：完善信息流展示，支持转发嵌套显示

后端任务：

1. Service 层扩展

- PostServiceImpl.java 扩展

- getRepostChain()：获取转发链（递归查询 source_id）

- checkPostExists()：检查原微博是否存在（用于转发显示）

1. Controller 层扩展

- PostController.java 扩展

- GET /post/{postId}/source：获取转发源微博（用于嵌套显示）

前端任务：

1. 组件扩展

- PostCard.vue 扩展

- 支持嵌套卡片显示（转发微博时显示原微博）

- 原微博被删时显示“原内容不可见”

- 转发标识和转发内容展示

1. 信息流优化

- 实现下拉刷新

- 实现上拉加载更多（分页）

- 时间格式化显示（相对时间：2 分钟前）

------

### 阶段五：社交互动系统 - 转发功能

目标：实现微博转发功能

后端任务（按顺序）：

1. Service 层扩展

- PostServiceImpl.java 扩展

- repost()：转发微博

- 创建新微博，设置 source_id 为原微博 ID

- 可选转发理由（content 字段）

1. Controller 层扩展

- PostController.java 扩展

- POST /post/repost：转发微博（需 Token）

前端任务：

1. API 模块扩展

- src/api/post.js 添加转发接口

1. 组件扩展

- PostCard.vue 添加转发按钮

- components/RepostDialog.vue（转发对话框组件）

- 显示原微博内容

- 输入转发理由

- 确认转发

------

### 阶段六：社交互动系统 - 评论功能

目标：实现评论和二级回复

后端任务（按顺序）：

1. Entity 层

- Comment.java（post_id、parent_id 用于二级回复、content）

1. Mapper 层

- CommentMapper.java

1. Service 层

- CommentService.java（接口）

- CommentServiceImpl.java（实现）

- addComment()：添加评论

- getCommentsByPostId()：获取微博评论列表（支持二级回复树形结构）

- replyComment()：回复评论（设置 parent_id）

1. Controller 层

- CommentController.java

- POST /comment/add：添加评论（需 Token）

- GET /comment/post/{postId}：获取微博评论列表

- POST /comment/reply：回复评论（需 Token）

前端任务：

1. API 模块

- src/api/comment.js（评论相关 API）

1. 组件开发

- components/CommentList.vue（评论列表组件）

- 展示评论列表

- 支持二级回复（树形结构）

- 评论输入框

- components/CommentItem.vue（单条评论组件）

- 评论内容展示

- 回复按钮

- 嵌套显示回复

1. 组件集成

- PostCard.vue 集成评论列表

- 点击评论按钮展开/收起评论列表

------

### 阶段七：社交互动系统 - 点赞功能

目标：实现点赞与取消点赞

后端任务（按顺序）：

1. Entity 层

- PostLike.java（post_id、user_id 联合唯一）

1. Mapper 层

- PostLikeMapper.java

1. Service 层

- PostLikeService.java（接口）

- PostLikeServiceImpl.java（实现）

- likePost()：点赞（检查是否已赞，防止重复）

- unlikePost()：取消点赞

- isLiked()：检查是否已点赞

- getLikeCount()：获取点赞数

1. Controller 层

- PostLikeController.java

- POST /like/{postId}：点赞（需 Token）

- DELETE /like/{postId}：取消点赞（需 Token）

- GET /like/{postId}/status：检查点赞状态（需 Token）

- GET /like/{postId}/count：获取点赞数

前端任务：

1. API 模块

- src/api/like.js（点赞相关 API）

1. 组件扩展

- PostCard.vue 添加点赞功能

- 点赞按钮（已赞/未赞状态）

- 点赞数显示

- 点击切换点赞状态（防重复点击）

------

### 阶段八：关注系统

目标：实现用户关注与粉丝功能

后端任务（按顺序）：

1. Entity 层

- Follow.java（follower_id、followee_id）

1. Mapper 层

- FollowMapper.java

1. Service 层扩展

- UserServiceImpl.java 扩展

- followUser()：关注用户

- unfollowUser()：取消关注

- isFollowing()：检查是否已关注

- getFollowList()：获取关注列表

- getFollowerList()：获取粉丝列表

1. Controller 层扩展

- UserController.java 扩展

- POST /user/follow/{userId}：关注用户（需 Token）

- DELETE /user/follow/{userId}：取消关注（需 Token）

- GET /user/{userId}/followers：获取粉丝列表

- GET /user/{userId}/followings：获取关注列表

前端任务：

1. API 模块扩展

- src/api/user.js 添加关注相关接口

1. 组件开发

- components/FollowButton.vue（关注按钮组件）

- 显示关注/已关注状态

- 点击切换关注状态

1. 页面扩展

- views/Profile.vue 显示关注数、粉丝数

- 添加关注/粉丝列表页面

------

### 阶段九：搜索与发现系统

目标：实现全局搜索和热搜榜

后端任务（按顺序）：

1. Service 层

- SearchService.java（接口）

- SearchServiceImpl.java（实现）

- searchUsers()：搜索用户（模糊匹配 username）

- searchPosts()：搜索微博（模糊匹配 content）

- getHotTopics()：获取热搜榜 Top 10

- 热度算法：话题发博数 + 点击数（加权计算）

1. Controller 层

- SearchController.java

- GET /search/users：搜索用户（query 参数）

- GET /search/posts：搜索微博（query 参数）

- GET /search/topics/hot：获取热搜榜

前端任务：

1. API 模块

- src/api/search.js（搜索相关 API）

1. 组件开发

- components/SearchBar.vue（顶部搜索框组件）

- 输入框

- 搜索建议（可选）

- 跳转搜索结果页

- components/HotTopics.vue（热搜榜组件）

- 展示 Top 10 话题

- 点击跳转话题页

1. 页面开发

- views/SearchResult.vue（搜索结果页）

- 用户搜索结果

- 微博搜索结果

- Tab 切换

1. 页面扩展

- views/Home.vue 右侧栏集成热搜榜组件

- views/TopicDetail.vue（话题详情页）

- 展示话题信息

- 展示该话题下的所有微博

------

### 阶段十：优化与完善

目标：性能优化、安全加固、体验优化

后端任务：

1. 性能优化

- 信息流查询优化（索引优化）

- 缓存热点数据（可选：Redis）

- 分页查询优化

1. 安全加固

- XSS 过滤完善

- SQL 注入防护（MyBatis Plus 已防护）

- 接口限流（可选）

1. 功能完善

- 话题点击量统计

- 热搜算法优化

- 文件上传大小限制

- 文件类型校验

前端任务：

1. 性能优化

- 图片懒加载

- 虚拟滚动（长列表优化）

- 路由懒加载

1. 体验优化

- 加载状态提示

- 错误提示优化

- 空状态展示

- 响应式布局优化

1. UI 完善

- 统一主题色

- 动画效果

- 交互反馈优化

------

## 开发顺序总结

1. 阶段一：数据库与基础架构

1. 阶段二：用户与鉴权

1. 阶段三：内容发布

1. 阶段四：内容展示

1. 阶段五：转发功能

1. 阶段六：评论功能

1. 阶段七：点赞功能

1. 阶段八：关注系统

1. 阶段九：搜索与发现

1. 阶段十：优化与完善

## 注意事项

1. 每个阶段遵循“先后端 API → 后前端 UI”的顺序

1. 每个功能模块遵循“Entity → Mapper → Service → Controller”的分层架构

1. 所有接口返回统一的 Result<T> 格式

1. 前端所有 API 调用必须封装在 src/api/ 目录下

1. 前端必须使用 Composition API (<script setup>)

1. 数据库字段使用 snake_case，Java 实体使用 camelCase