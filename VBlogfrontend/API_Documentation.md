# V-Blog API 接口文档

本文档描述了 V-Blog 项目后端的 API 接口，供前端开发人员参考对接。

## 1. 概述

- **基础 URL**: `http://localhost:8080/api`
- **数据格式**: JSON
- **字符编码**: UTF-8

### 1.1 通用响应格式

所有接口均返回统一的 JSON 格式：

```json
{
  "code": 0,          // 状态码：0 表示成功，非 0 表示失败
  "msg": "成功",      // 提示信息
  "data": { ... },    // 业务数据
  "traceId": "..."    // 追踪ID，用于排查问题
}
```

**常见状态码：**

- `0`: 成功
- `400xx`: 客户端错误 (如参数校验失败)
- `401xx`: 认证失败 (如未登录或 Token 过期)
- `403xx`: 权限不足
- `404xx`: 资源未找到
- `500xx`: 服务器内部错误

### 1.2 认证方式

- **机制**: JWT (JSON Web Token)
- **Header**: 在需要认证的接口请求头中添加 `Authorization` 字段。
- **格式**: `Authorization: Bearer <your_token>`

---

## 2. 用户接口 (User API)

**Base Path**: `/user`

### 2.1 用户注册

- **URL**: `/user/register`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`
- **参数**:
  - `username` (String, 必填): 用户名
  - `password` (String, 必填): 密码
- **响应**:
  ```json
  {
    "code": 0,
    "msg": "注册成功",
    "data": {
      "user": { "userId": 1, "username": "test", ... },
      "token": "eyJhbGciOiJIUzI1Ni..."
    }
  }
  ```

### 2.2 用户登录

- **URL**: `/user/login`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`
- **参数**:
  - `username` (String, 必填): 用户名
  - `password` (String, 必填): 密码
- **响应**:
  ```json
  {
    "code": 0,
    "msg": "登录成功",
    "data": {
      "user": { "userId": 1, "username": "test", ... },
      "token": "eyJhbGciOiJIUzI1Ni..."
    }
  }
  ```

### 2.3 获取当前用户信息

- **URL**: `/user/info`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应**:
  ```json
  {
    "code": 0,
    "data": { "userId": 1, "username": "test", "nickname": "昵称", "avatar": "url...", ... }
  }
  ```

### 2.4 更新用户信息

- **URL**: `/user/info`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `application/json`
- **Body**:
  ```json
  {
    "nickname": "新昵称",
    "gender": 1,
    "bio": "个人简介",
    "location": "北京",
    "website": "https://blog.example.com"
  }
  ```
- **响应**: 更新后的用户对象

### 2.5 根据 ID 获取用户信息

- **URL**: `/user/{userId}`
- **Method**: `GET`
- **响应**: 用户对象信息

### 2.6 获取用户统计信息

- **URL**: `/user/{userId}/stats`
- **Method**: `GET`
- **响应**:
  ```json
  {
    "code": 0,
    "data": {
      "postCount": 10, // 发布帖子数
      "followingCount": 5, // 关注数
      "followerCount": 20 // 粉丝数
    }
  }
  ```

### 2.7 修改密码

- **URL**: `/user/password`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `application/x-www-form-urlencoded`
- **参数**:
  - `oldPassword` (String, 必填): 旧密码
  - `newPassword` (String, 必填): 新密码

### 2.8 上传头像

- **URL**: `/user/avatar`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `file` (File, 必填): 图片文件
- **响应**: 更新后的用户对象（包含新头像 URL）

### 2.9 上传背景图

- **URL**: `/user/background`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `file` (File, 必填): 图片文件

---

## 3. 帖子接口 (Post API)

**Base Path**: `/post`

### 3.1 发布帖子

- **URL**: `/post/publish`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `application/x-www-form-urlencoded`
- **参数**:
  - `content` (String, 选填): 帖子内容
  - `images` (String, 选填): 图片 URL 的 JSON 数组字符串，如 `["url1", "url2"]`
  - `videoUrl` (String, 选填): 视频 URL
- **响应**: 新发布的帖子对象

### 3.2 获取帖子列表 (首页信息流)

- **URL**: `/post/list`
- **Method**: `GET`
- **参数**:
  - `page` (int, 默认 1): 页码
  - `size` (int, 默认 10): 每页数量
- **响应**: 分页对象 `Page<Post>`

### 3.3 获取帖子详情

- **URL**: `/post/{postId}`
- **Method**: `GET`
- **响应**: 帖子详情对象

### 3.4 获取用户的帖子列表

- **URL**: `/post/user/{userId}`
- **Method**: `GET`
- **参数**:
  - `page` (int, 默认 1): 页码
  - `size` (int, 默认 10): 每页数量
- **响应**: 分页对象 `Page<Post>`

### 3.5 转发帖子

- **URL**: `/post/repost`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `sourceId` (Long, 必填): 原帖子 ID
  - `content` (String, 选填): 转发附言

### 3.6 获取源帖子 (用于显示转发内容)

- **URL**: `/post/{postId}/source`
- **Method**: `GET`
- **响应**: 原帖子对象

### 3.7 删除帖子

- **URL**: `/post/{postId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token>`

### 3.8 更新帖子

- **URL**: `/post/{postId}`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `content` (String, 选填)
  - `images` (String, 选填)
  - `videoUrl` (String, 选填)

### 3.9 根据话题获取帖子

- **URL**: `/post/topic/{topicId}`
- **Method**: `GET`
- **参数**:
  - `page`, `size`

---

## 4. 评论接口 (Comment API)

**Base Path**: `/comment`

### 4.1 发表评论

- **URL**: `/comment/add`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `postId` (Long, 必填): 帖子 ID
  - `content` (String, 必填): 评论内容
  - `parentId` (Long, 选填): 父评论 ID（用于回复）

### 4.2 获取帖子评论列表

- **URL**: `/comment/post/{postId}`
- **Method**: `GET`
- **响应**: 评论树列表 `List<CommentTree>`

### 4.3 删除评论

- **URL**: `/comment/{commentId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token>`

### 4.4 点赞评论

- **URL**: `/comment/like/{commentId}`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`

### 4.5 取消点赞评论

- **URL**: `/comment/like/{commentId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token>`

### 4.6 获取评论点赞状态

- **URL**: `/comment/like/{commentId}/status`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `{ "isLiked": true/false, "likeCount": 10 }`

---

## 5. 关注接口 (Follow API)

**Base Path**: `/follow`

### 5.1 关注用户

- **URL**: `/follow/{userId}`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`

### 5.2 取消关注

- **URL**: `/follow/{userId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token>`

### 5.3 获取关注状态

- **URL**: `/follow/{userId}/status`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `{ "isFollowing": true/false }`

### 5.4 获取粉丝列表

- **URL**: `/follow/{userId}/followers`
- **Method**: `GET`
- **参数**: `page`, `size`

### 5.5 获取关注列表

- **URL**: `/follow/{userId}/following`
- **Method**: `GET`
- **参数**: `page`, `size`

---

## 6. 点赞接口 (Post Like API)

**Base Path**: `/like`

### 6.1 点赞帖子

- **URL**: `/like/{postId}`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`

### 6.2 取消点赞帖子

- **URL**: `/like/{postId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token>`

### 6.3 获取帖子点赞状态

- **URL**: `/like/{postId}/status`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `{ "isLiked": true/false, "likeCount": 100 }`

### 6.4 获取帖子点赞数

- **URL**: `/like/{postId}/count`
- **Method**: `GET`

---

## 7. 话题接口 (Topic API)

**Base Path**: `/topic`

### 7.1 获取热门话题

- **URL**: `/topic/hot`
- **Method**: `GET`
- **参数**: `limit` (默认 10)

### 7.2 根据名称获取话题

- **URL**: `/topic/name/{name}`
- **Method**: `GET`

### 7.3 根据话题名称获取帖子

- **URL**: `/topic/name/{name}/posts`
- **Method**: `GET`
- **参数**: `page`, `size`

---

## 8. 通知接口 (Notification API)

**Base Path**: `/notification`

### 8.1 获取通知列表

- **URL**: `/notification/list`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **参数**: `page`, `size`

### 8.2 获取未读通知数

- **URL**: `/notification/unread/count`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`

### 8.3 标记通知为已读

- **URL**: `/notification/{notificationId}/read`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`

### 8.4 标记所有通知为已读

- **URL**: `/notification/all/read`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`

### 8.5 删除通知

- **URL**: `/notification/{notificationId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token>`

---

## 9. 搜索接口 (Search API)

**Base Path**: `/search`

### 9.1 搜索用户

- **URL**: `/search/users`
- **Method**: `GET`
- **参数**:
  - `q` (String): 搜索关键词
  - `page`, `size`

### 9.2 搜索帖子

- **URL**: `/search/posts`
- **Method**: `GET`
- **参数**:
  - `q` (String): 搜索关键词
  - `page`, `size`

### 9.3 获取热门搜索话题

- **URL**: `/search/topics/hot`
- **Method**: `GET`

---

## 10. 文件接口 (File API)

**Base Path**: `/file`

### 10.1 上传图片

- **URL**: `/file/upload/image`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `file` (File): 图片文件
- **响应**: `{ "url": "http://..." }`

### 10.2 上传视频

- **URL**: `/file/upload/video`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `file` (File): 视频文件
- **响应**: `{ "url": "http://..." }`

---

## 11. 好友关系接口 (Friendship API)

**Base Path**: `/friendship`

### 11.1 搜索用户

- **URL**: `/friendship/search`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **请求参数**:
  - `keyword` (String, 必填): 搜索关键词（用户名或昵称）
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": [
      {
        "userId": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "avatar": "http://example.com/avatar.jpg",
        "bio": "个人简介"
      }
    ],
    "traceId": "..."
  }
  ```

### 11.2 发送好友申请

- **URL**: `/friendship/request`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `application/x-www-form-urlencoded`
- **请求参数**:
  - `toUserId` (Long, 必填): 目标用户 ID
  - `message` (String, 可选): 申请留言
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "好友申请已发送",
    "data": {
      "requestId": 1,
      "fromUserId": 1,
      "toUserId": 2,
      "message": "你好，加个好友吧",
      "status": 0,
      "createTime": "2025-12-23 20:00:00"
    },
    "traceId": "..."
  }
  ```

### 11.3 获取收到的好友申请

- **URL**: `/friendship/requests/received`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **请求参数**:
  - `status` (Integer, 可选): 申请状态（0-待处理，1-已同意，2-已拒绝）
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": [
      {
        "requestId": 1,
        "fromUserId": 2,
        "toUserId": 1,
        "message": "你好，加个好友吧",
        "status": 0,
        "createTime": "2025-12-23 20:00:00"
      }
    ],
    "traceId": "..."
  }
  ```

### 11.4 获取发送的好友申请

- **URL**: `/friendship/requests/sent`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **请求参数**:
  - `status` (Integer, 可选): 申请状态（0-待处理，1-已同意，2-已拒绝）
- **响应示例**: 同 11.3

### 11.5 处理好友申请

- **URL**: `/friendship/request/{requestId}`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`
- **请求参数**:
  - `accept` (Boolean, 必填): 是否同意（true-同意，false-拒绝）
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": "已同意好友申请",
    "traceId": "..."
  }
  ```

### 11.6 获取好友列表

- **URL**: `/friendship/friends`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": [
      {
        "userId": 2,
        "username": "friend1",
        "nickname": "好友1",
        "avatar": "http://example.com/avatar.jpg",
        "bio": "个人简介"
      }
    ],
    "traceId": "..."
  }
  ```

### 11.7 检查好友关系

- **URL**: `/friendship/check/{userId}`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": true,
    "traceId": "..."
  }
  ```

### 11.8 修改好友备注

- **URL**: `/friendship/remark`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `application/x-www-form-urlencoded`
- **请求参数**:
  - `friendId` (Long, 必填): 好友 ID
  - `remark` (String, 必填): 备注名称
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": "备注已更新",
    "traceId": "..."
  }
  ```

### 11.9 删除好友

- **URL**: `/friendship/friend/{friendId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": "已删除好友关系",
    "traceId": "..."
  }
  ```

### 11.10 获取好友数量

- **URL**: `/friendship/count`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": 10,
    "traceId": "..."
  }
  ```

---

## 12. 消息接口 (Message API)

**Base Path**: `/message`

### 12.1 发送消息

- **URL**: `/message/send`
- **Method**: `POST`
- **Header**: `Authorization: Bearer <token>`
- **Content-Type**: `application/x-www-form-urlencoded`
- **请求参数**:
  - `toUserId` (Long, 必填): 接收者用户 ID
  - `content` (String, 必填): 消息内容
  - `messageType` (Integer, 可选, 默认 1): 消息类型（1-文本，2-图片，3-语音，4-视频，5-文件）
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": {
      "messageId": 1,
      "conversationId": "1_2",
      "fromUserId": 1,
      "toUserId": 2,
      "content": "你好！",
      "messageType": 1,
      "status": 0,
      "createTime": "2025-12-23 20:00:00"
    },
    "traceId": "..."
  }
  ```

### 12.2 获取会话消息列表

- **URL**: `/message/conversation/{conversationId}`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **请求参数**:
  - `page` (Integer, 可选, 默认 1): 页码
  - `size` (Integer, 可选, 默认 20): 每页大小
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": {
      "records": [
        {
          "messageId": 1,
          "conversationId": "1_2",
          "fromUserId": 1,
          "toUserId": 2,
          "content": "你好！",
          "messageType": 1,
          "status": 1,
          "createTime": "2025-12-23 20:00:00"
        }
      ],
      "total": 1,
      "current": 1,
      "size": 20
    },
    "traceId": "..."
  }
  ```

### 12.3 获取会话列表

- **URL**: `/message/conversations`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": [
      {
        "conversationId": "1_2",
        "user1Id": 1,
        "user2Id": 2,
        "lastMessage": "你好！",
        "lastMessageTime": "2025-12-23 20:00:00",
        "user1UnreadCount": 0,
        "user2UnreadCount": 1
      }
    ],
    "traceId": "..."
  }
  ```

### 12.4 标记会话中消息为已读

- **URL**: `/message/conversation/{conversationId}/read`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": "已标记为已读",
    "traceId": "..."
  }
  ```

### 12.5 撤回消息

- **URL**: `/message/{messageId}/recall`
- **Method**: `PUT`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": "消息已撤回",
    "traceId": "..."
  }
  ```
- **说明**: 仅支持撤回 2 分钟内的消息

### 12.6 删除消息

- **URL**: `/message/{messageId}`
- **Method**: `DELETE`
- **Header**: `Authorization: Bearer <token)`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": "消息已删除",
    "traceId": "..."
  }
  ```

### 12.7 获取未读消息总数

- **URL**: `/message/unread/count`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": 5,
    "traceId": "..."
  }
  ```

### 12.8 生成会话 ID

- **URL**: `/message/conversation/id`
- **Method**: `GET`
- **Header**: `Authorization: Bearer <token>`
- **请求参数**:
  - `otherUserId` (Long, 必填): 对方用户 ID
- **响应示例**:
  ```json
  {
    "code": 0,
    "msg": "成功",
    "data": "1_2",
    "traceId": "..."
  }
  ```

**说明**:

- 消息永久存储在数据库中
- 会话 ID 格式为：较小用户 ID\_较大用户 ID
- 只能向好友发送消息
- 消息状态：0-未读，1-已读，2-已撤回，3-已删除

---

## 13. WebSocket 实时消息推送

### 13.1 连接地址

```
ws://localhost:8080/ws/message?token=<your_jwt_token>
```

### 13.2 连接说明

- 认证方式: 通过 URL 参数传递 JWT token
- 连接保持: 支持心跳机制保持长连接
- 自动重连: 建议客户端实现断线自动重连

### 13.3 消息格式

**服务端推送消息示例**:

```json
{
  "type": "new_message",
  "data": {
    "messageId": 101,
    "conversationId": "1_2",
    "fromUserId": 1,
    "toUserId": 2,
    "content": "你好！",
    "messageType": 1,
    "status": 0,
    "createTime": "2025-12-23 20:00:00"
  },
  "timestamp": 1703304600000
}
```

**消息类型**:

- `connect`: 连接成功确认
- `new_message`: 新消息通知
- `message_read`: 消息已读通知
- `message_recall`: 消息撤回通知
- `friend_request`: 好友申请通知

**客户端心跳**:

```json
{
  "type": "ping"
}
```

**服务端心跳响应**:

```json
{
  "type": "pong",
  "timestamp": 1703304600000
}
```

---

## 附录 A：用户 API 标准化示例

以下为用户模块的统一接口说明与响应示例，用于与前端对齐。

### A.1 用户注册 `/user/register` (POST)

- 请求：`application/x-www-form-urlencoded`
- 参数：`username`(必填), `password`(必填)
- 响应示例：

```json
{
  "code": 0,
  "msg": "注册成功",
  "data": {
    "user": { "userId": 101, "username": "u1", "nickname": "", "avatar": "" },
    "token": "<jwt>"
  },
  "traceId": "..."
}
```

### A.2 用户登录 `/user/login` (POST)

- 请求：`application/x-www-form-urlencoded`
- 参数：`username`(必填), `password`(必填)
- 响应示例：同上（`msg`为“登录成功”）。

### A.3 获取当前用户信息 `/user/info` (GET)

- Header：`Authorization: Bearer <token>`
- 响应示例：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "userId": 101,
    "username": "u1",
    "nickname": "昵称",
    "avatar": "url",
    "bio": ""
  },
  "traceId": "..."
}
```

### A.4 更新用户信息 `/user/info` (PUT)

- Header：`Authorization`
- 请求：`application/json`
- Body 字段：`nickname`,`gender`,`bio`,`location`,`website`
- 响应示例：返回更新后的用户对象。

### A.5 修改密码 `/user/password` (PUT)

- Header：`Authorization`
- 表单参数：`oldPassword`,`newPassword`
- 响应：`{"code":0,"msg":"成功"}`

### A.6 上传头像 `/user/avatar` (POST)

- Header：`Authorization`
- 表单：`multipart/form-data`，`file`
- 响应：更新后的用户对象（含新的 `avatar`）。

### A.7 上传背景图 `/user/background` (POST)

- Header：`Authorization`
- 表单：`multipart/form-data`，`file`
- 响应：更新后的用户对象（如有持久化字段）。

### A.8 获取用户统计 `/user/{userId}/stats` (GET)

- 响应示例：

```json
{
  "code": 0,
  "data": { "postCount": 10, "followingCount": 5, "followerCount": 20 }
}
```

---

## 附录 B：帖子 API 标准化示例

### B.1 发布帖子 `/post/publish` (POST)

- Header：`Authorization`
- 表单：`content`(选),`images`(选,JSON 数组字符串),`videoUrl`(选)
- 响应：新帖子对象。

### B.2 帖子列表 `/post/list` (GET)

- 参数：`page`(默认 1),`size`(默认 10)
- 响应：`Page<Post>`，示例：

```json
{
  "code": 0,
  "data": {
    "records": [{ "postId": 1, "content": "..." }],
    "total": 100,
    "current": 1,
    "size": 10
  },
  "traceId": "..."
}
```

### B.3 帖子详情 `/post/{postId}` (GET)

- 响应：帖子对象。

### B.4 用户帖子 `/post/user/{userId}` (GET)

- 参数：`page`,`size`
- 响应：分页对象 `Page<Post>`。

### B.5 转发帖子 `/post/repost` (POST)

- Header：`Authorization`
- 表单：`sourceId`(必填),`content`(选)
- 响应：转发后的帖子对象。

### B.6 删除帖子 `/post/{postId}` (DELETE)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### B.7 更新帖子 `/post/{postId}` (PUT)

- Header：`Authorization`
- 表单：`content`(选),`images`(选),`videoUrl`(选)
- 响应：更新后的帖子对象。

### B.8 话题帖子 `/post/topic/{topicId}` (GET)

- 参数：`page`,`size`
- 响应：分页对象。

---

## 附录 C：评论 API 标准化示例

### C.1 发表评论 `/comment/add` (POST)

- Header：`Authorization`
- 表单：`postId`(必填),`content`(必填),`parentId`(选)
- 响应：新评论对象。

### C.2 帖子评论列表 `/comment/post/{postId}` (GET)

- 响应：评论树数组 `List<CommentTree>`。

### C.3 删除评论 `/comment/{commentId}` (DELETE)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### C.4 点赞评论 `/comment/like/{commentId}` (POST)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### C.5 取消点赞评论 `/comment/like/{commentId}` (DELETE)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### C.6 评论点赞状态 `/comment/like/{commentId}/status` (GET)

- 响应：

```json
{ "code": 0, "data": { "isLiked": true, "likeCount": 10 } }
```

---

## 附录 D：关注 API 标准化示例

### D.1 关注 `/follow/{userId}` (POST)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### D.2 取消关注 `/follow/{userId}` (DELETE)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### D.3 关注状态 `/follow/{userId}/status` (GET)

- Header：`Authorization`
- 响应：`{ "code":0, "data": { "isFollowing": true } }`

### D.4 粉丝列表 `/follow/{userId}/followers` (GET)

- 参数：`page`,`size`
- 响应：分页用户列表。

### D.5 关注列表 `/follow/{userId}/following` (GET)

- 参数：`page`,`size`
- 响应：分页用户列表。

---

## 附录 E：点赞 API 标准化示例

### E.1 点赞帖子 `/like/{postId}` (POST)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### E.2 取消点赞帖子 `/like/{postId}` (DELETE)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### E.3 帖子点赞状态 `/like/{postId}/status` (GET)

- Header：`Authorization`
- 响应：`{ "code":0, "data": { "isLiked": true, "likeCount": 100 } }`

### E.4 帖子点赞数 `/like/{postId}/count` (GET)

- 响应：`{ "code":0, "data": 100 }`

---

## 附录 F：话题 API 标准化示例

### F.1 热门话题 `/topic/hot` (GET)

- 参数：`limit`(默认 10)
- 响应：话题列表。

### F.2 根据名称获取话题 `/topic/name/{name}` (GET)

- 响应：话题对象。

### F.3 根据话题名称获取帖子 `/topic/name/{name}/posts` (GET)

- 参数：`page`,`size`
- 响应：分页帖子列表。

---

## 附录 G：通知 API 标准化示例

### G.1 通知列表 `/notification/list` (GET)

- Header：`Authorization`
- 参数：`page`,`size`
- 响应：分页通知列表。

### G.2 未读通知数 `/notification/unread/count` (GET)

- Header：`Authorization`
- 响应：`{ "code":0, "data": 3 }`

### G.3 标记通知为已读 `/notification/{notificationId}/read` (PUT)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### G.4 标记所有通知为已读 `/notification/all/read` (PUT)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

### G.5 删除通知 `/notification/{notificationId}` (DELETE)

- Header：`Authorization`
- 响应：`{"code":0,"msg":"成功"}`

---

## 附录 H：搜索 API 标准化示例

### H.1 搜索用户 `/search/users` (GET)

- 参数：`q`,`page`,`size`
- 响应：用户列表分页。

### H.2 搜索帖子 `/search/posts` (GET)

- 参数：`q`,`page`,`size`
- 响应：帖子列表分页。

### H.3 热门搜索话题 `/search/topics/hot` (GET)

- 响应：话题列表。

---

## 附录 I：文件 API 标准化示例

### I.1 上传图片 `/file/upload/image` (POST)

- Header：`Authorization`
- 表单：`multipart/form-data`，`file`
- 响应：`{ "code":0, "data": { "url": "http://..." } }`

### I.2 上传视频 `/file/upload/video` (POST)

- Header：`Authorization`
- 表单：`multipart/form-data`，`file`
- 响应：`{ "code":0, "data": { "url": "http://..." } }`
