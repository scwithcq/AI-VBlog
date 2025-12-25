# V-Blog 消息系统使用说明

## 功能概述

消息系统已完全集成真实后端 API，支持以下功能：

### 1. 好友管理

#### 1.1 添加好友

- 点击左侧栏顶部的 **+** 图标打开添加好友对话框
- 输入用户名或昵称进行搜索
- 点击搜索结果中的"添加好友"按钮
- 可选：输入验证消息
- 发送好友申请

#### 1.2 好友申请

- 点击左侧栏顶部的 **铃铛** 图标查看好友申请
- 查看待处理的好友申请列表
- 可以选择"同意"或"拒绝"申请

#### 1.3 好友列表

- 点击左侧栏顶部的 **人像** 图标查看好友列表
- 查看所有已添加的好友
- 可以直接点击"发消息"开始聊天
- 可以删除好友

### 2. 消息功能

#### 2.1 会话列表

- 左侧显示所有聊天会话
- 显示最后一条消息和时间
- 显示未读消息数量（红色角标）
- 点击会话切换聊天对象

#### 2.2 发送消息

- 在右侧输入框中输入消息内容
- 按 **Enter** 键发送消息
- 按 **Ctrl + Enter** 换行

#### 2.3 接收消息

- 通过 WebSocket 实时接收新消息
- 自动滚动到最新消息
- 自动标记为已读

#### 2.4 消息状态

- 已发送：显示在右侧，紫色渐变背景
- 已接收：显示在左侧，半透明背景
- 已撤回：显示"消息已撤回"

### 3. WebSocket 实时通信

系统使用 WebSocket 实现实时消息推送：

- 自动连接到 `ws://localhost:8080/ws/message`
- 使用 JWT Token 进行身份验证
- 支持断线自动重连（5 秒后重试）
- 接收以下类型的消息：
  - `new_message`: 新消息通知
  - `message_read`: 消息已读通知
  - `message_recall`: 消息撤回通知
  - `friend_request`: 好友申请通知

## API 接口

### 消息相关接口

- `POST /message/send` - 发送消息
- `GET /message/conversations` - 获取会话列表
- `GET /message/conversation/{conversationId}` - 获取会话消息
- `PUT /message/conversation/{conversationId}/read` - 标记消息为已读
- `PUT /message/{messageId}/recall` - 撤回消息
- `DELETE /message/{messageId}` - 删除消息
- `GET /message/unread/count` - 获取未读消息数
- `GET /message/conversation/id` - 生成会话 ID

### 好友关系接口

- `GET /friendship/search` - 搜索用户
- `POST /friendship/request` - 发送好友申请
- `GET /friendship/requests/received` - 获取收到的好友申请
- `GET /friendship/requests/sent` - 获取发送的好友申请
- `PUT /friendship/request/{requestId}` - 处理好友申请
- `GET /friendship/friends` - 获取好友列表
- `GET /friendship/check/{userId}` - 检查好友关系
- `PUT /friendship/remark` - 修改好友备注
- `DELETE /friendship/friend/{friendId}` - 删除好友
- `GET /friendship/count` - 获取好友数量

## 使用流程

### 典型使用场景

1. **首次使用**

   ```
   登录 → 添加好友 → 等待对方同意 → 开始聊天
   ```

2. **接收好友申请**

   ```
   收到通知 → 查看好友申请 → 同意/拒绝 → 开始聊天
   ```

3. **发送消息**

   ```
   选择好友 → 进入会话 → 输入消息 → 发送
   ```

4. **查看好友列表**
   ```
   打开好友列表 → 选择好友 → 点击发消息 → 创建会话
   ```

## 注意事项

1. **好友关系**：只能向好友发送消息，需要先添加好友并等待对方同意

2. **消息撤回**：消息发送后 2 分钟内可以撤回

3. **会话 ID 格式**：`较小用户ID_较大用户ID`，系统自动生成

4. **未读消息**：切换会话时自动标记为已读

5. **WebSocket 连接**：需要有效的 JWT Token，断线会自动重连

6. **消息状态**：
   - 0: 未读
   - 1: 已读
   - 2: 已撤回
   - 3: 已删除

## 文件结构

```
src/
├── api/
│   ├── message.js        # 消息API接口
│   └── friendship.js     # 好友关系API接口
├── views/
│   └── Message.vue       # 消息页面主组件
└── store/
    └── userStore.js      # 用户状态管理
```

## 技术栈

- **Vue 3** - 组合式 API
- **Element Plus** - UI 组件库
- **WebSocket** - 实时通信
- **Axios** - HTTP 请求
- **Pinia** - 状态管理

## 开发建议

1. 确保后端服务运行在 `http://localhost:8080`
2. 确保 WebSocket 服务正常运行
3. 登录后才能使用消息功能
4. Token 需要存储在 `localStorage` 中
5. 开发时可以打开浏览器控制台查看 WebSocket 连接状态

## 未来扩展

可以考虑添加以下功能：

- [ ] 图片消息
- [ ] 语音消息
- [ ] 视频消息
- [ ] 文件传输
- [ ] 消息引用回复
- [ ] 消息搜索
- [ ] 会话置顶
- [ ] 消息免打扰
- [ ] 表情包支持
- [ ] 群聊功能
