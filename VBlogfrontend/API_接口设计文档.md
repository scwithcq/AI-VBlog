# V-Blog API 接口设计文档

本文档描述了 V-Blog 后端项目的 API 接口设计。

## 1. 用户管理 (User Management)

### 1.1 用户注册

- **描述**: 注册新用户
- **方法**: `POST`
- **URL**: `/user/register`
- **参数**:
  - `username` (Query, String, 必填): 用户名
  - `password` (Query, String, 必填): 密码
- **响应**: `Result<Map<String, Object>>` (包含 user 和 token)

### 1.2 用户登录

- **描述**: 用户登录
- **方法**: `POST`
- **URL**: `/user/login`
- **参数**:
  - `username` (Query, String, 必填): 用户名
  - `password` (Query, String, 必填): 密码
- **响应**: `Result<Map<String, Object>>` (包含 user 和 token)

### 1.3 获取当前用户信息

- **描述**: 获取当前登录用户的详细信息
- **方法**: `GET`
- **URL**: `/user/info`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<User>`

### 1.4 更新用户信息

- **描述**: 更新当前用户的个人信息
- **方法**: `PUT`
- **URL**: `/user/info`
- **Header**: `Authorization: Bearer <token>`
- **Body**: `User` 对象 (JSON)
- **响应**: `Result<User>`

### 1.5 根据 ID 获取用户信息

- **描述**: 获取指定用户的公开信息
- **方法**: `GET`
- **URL**: `/user/{userId}`
- **参数**:
  - `userId` (Path, Long, 必填): 用户 ID
- **响应**: `Result<User>`

### 1.6 获取用户统计信息

- **描述**: 获取用户的关注数、粉丝数等统计数据
- **方法**: `GET`
- **URL**: `/user/{userId}/stats`
- **参数**:
  - `userId` (Path, Long, 必填): 用户 ID
- **响应**: `Result<UserStats>`

### 1.7 修改密码

- **描述**: 修改当前用户的密码
- **方法**: `PUT`
- **URL**: `/user/password`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `oldPassword` (Query, String, 必填): 旧密码
  - `newPassword` (Query, String, 必填): 新密码
- **响应**: `Result<?>`

### 1.8 上传用户头像

- **描述**: 上传并更新用户头像
- **方法**: `POST`
- **URL**: `/user/avatar`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `file` (FormData, File, 必填): 图片文件
- **响应**: `Result<User>`

### 1.9 上传用户背景图片

- **描述**: 上传并更新用户个人主页背景图
- **方法**: `POST`
- **URL**: `/user/background`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `file` (FormData, File, 必填): 图片文件
- **响应**: `Result<User>`

### 1.10 获取所有用户

- **描述**: 获取系统所有用户列表
- **方法**: `GET`
- **URL**: `/user/all`
- **响应**: `Result<List<User>>`

---

## 2. 帖子管理 (Post Management)

### 2.1 发布新帖子

- **描述**: 发布一条新帖子
- **方法**: `POST`
- **URL**: `/post/publish`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `content` (Query, String, 可选): 帖子内容
  - `images` (Query, String, 可选): 图片 URL 列表的 JSON 字符串
  - `videoUrl` (Query, String, 可选): 视频 URL
  - `sourceId` (Query, Long, 可选): 转发源帖子 ID
- **响应**: `Result<Post>`

### 2.2 获取帖子列表 (信息流)

- **描述**: 分页获取帖子列表
- **方法**: `GET`
- **URL**: `/post/list`
- **参数**:
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<Post>>`

### 2.3 根据 ID 获取帖子

- **描述**: 获取单条帖子的详情
- **方法**: `GET`
- **URL**: `/post/{postId}`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
- **响应**: `Result<Post>`

### 2.4 根据用户 ID 获取帖子

- **描述**: 获取指定用户发布的帖子列表
- **方法**: `GET`
- **URL**: `/post/user/{userId}`
- **参数**:
  - `userId` (Path, Long, 必填): 用户 ID
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<Post>>`

### 2.5 获取源帖子

- **描述**: 获取转发帖子对应的源帖子信息
- **方法**: `GET`
- **URL**: `/post/{postId}/source`
- **参数**:
  - `postId` (Path, Long, 必填): 当前帖子 ID
- **响应**: `Result<Post>`

### 2.6 转发帖子

- **描述**: 转发一条帖子
- **方法**: `POST`
- **URL**: `/post/repost`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `sourceId` (Query, Long, 必填): 源帖子 ID
  - `content` (Query, String, 可选): 转发附言
- **响应**: `Result<Post>`

### 2.7 删除帖子

- **描述**: 删除指定帖子
- **方法**: `DELETE`
- **URL**: `/post/{postId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
- **响应**: `Result<?>`

### 2.8 更新帖子

- **描述**: 更新帖子内容
- **方法**: `PUT`
- **URL**: `/post/{postId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
  - `content` (Query, String, 可选): 内容
  - `images` (Query, String, 可选): 图片 JSON
  - `videoUrl` (Query, String, 可选): 视频 URL
- **响应**: `Result<Post>`

### 2.9 根据话题 ID 获取帖子列表

- **描述**: 获取属于指定话题的帖子
- **方法**: `GET`
- **URL**: `/post/topic/{topicId}`
- **参数**:
  - `topicId` (Path, Long, 必填): 话题 ID
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<Post>>`

---

## 3. 评论管理 (Comment Management)

### 3.1 添加评论

- **描述**: 对帖子进行评论
- **方法**: `POST`
- **URL**: `/comment/add`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `postId` (Query, Long, 必填): 帖子 ID
  - `content` (Query, String, 必填): 评论内容
  - `parentId` (Query, Long, 可选): 父评论 ID（回复评论时使用）
- **响应**: `Result<Comment>`

### 3.2 根据帖子 ID 获取评论

- **描述**: 获取指定帖子的评论列表（树形结构）
- **方法**: `GET`
- **URL**: `/comment/post/{postId}`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
- **响应**: `Result<List<CommentTree>>`

### 3.3 根据 ID 获取评论

- **描述**: 获取单条评论详情
- **方法**: `GET`
- **URL**: `/comment/{commentId}`
- **参数**:
  - `commentId` (Path, Long, 必填): 评论 ID
- **响应**: `Result<Comment>`

### 3.4 删除评论

- **描述**: 删除指定评论
- **方法**: `DELETE`
- **URL**: `/comment/{commentId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `commentId` (Path, Long, 必填): 评论 ID
- **响应**: `Result<?>`

### 3.5 更新评论

- **描述**: 修改评论内容
- **方法**: `PUT`
- **URL**: `/comment/{commentId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `commentId` (Path, Long, 必填): 评论 ID
  - `content` (Query, String, 必填): 新内容
- **响应**: `Result<Comment>`

### 3.6 点赞评论

- **描述**: 点赞某条评论
- **方法**: `POST`
- **URL**: `/comment/like/{commentId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `commentId` (Path, Long, 必填): 评论 ID
- **响应**: `Result<?>`

### 3.7 取消点赞评论

- **描述**: 取消对某条评论的点赞
- **方法**: `DELETE`
- **URL**: `/comment/like/{commentId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `commentId` (Path, Long, 必填): 评论 ID
- **响应**: `Result<?>`

### 3.8 获取评论点赞状态

- **描述**: 查询当前用户是否点赞了某条评论
- **方法**: `GET`
- **URL**: `/comment/like/{commentId}/status`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `commentId` (Path, Long, 必填): 评论 ID
- **响应**: `Result<?>` (包含 isLiked, likeCount)

---

## 4. 文件上传 (File Upload)

### 4.1 上传图片

- **描述**: 上传图片文件
- **方法**: `POST`
- **URL**: `/file/upload/image`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `file` (FormData, File, 必填): 图片文件
- **响应**: `Result<Map<String, String>>` (包含 url)

### 4.2 上传视频

- **描述**: 上传视频文件
- **方法**: `POST`
- **URL**: `/file/upload/video`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `file` (FormData, File, 必填): 视频文件
- **响应**: `Result<Map<String, String>>` (包含 url)

---

## 5. 关注系统 (Follow System)

### 5.1 关注用户

- **描述**: 关注指定用户
- **方法**: `POST`
- **URL**: `/follow/{userId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `userId` (Path, Long, 必填): 目标用户 ID
- **响应**: `Result<Map<String, Object>>`

### 5.2 取消关注用户

- **描述**: 取消关注指定用户
- **方法**: `DELETE`
- **URL**: `/follow/{userId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `userId` (Path, Long, 必填): 目标用户 ID
- **响应**: `Result<Map<String, Object>>`

### 5.3 检查关注状态

- **描述**: 检查当前用户是否关注了指定用户
- **方法**: `GET`
- **URL**: `/follow/{userId}/status`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `userId` (Path, Long, 必填): 目标用户 ID
- **响应**: `Result<Map<String, Object>>`

### 5.4 获取粉丝列表

- **描述**: 获取指定用户的粉丝列表
- **方法**: `GET`
- **URL**: `/follow/{userId}/followers`
- **参数**:
  - `userId` (Path, Long, 必填): 用户 ID
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<User>>`

### 5.5 获取关注列表

- **描述**: 获取指定用户关注的人列表
- **方法**: `GET`
- **URL**: `/follow/{userId}/following`
- **参数**:
  - `userId` (Path, Long, 必填): 用户 ID
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<User>>`

---

## 6. 好友系统 (Friendship System)

### 6.1 搜索用户

- **描述**: 根据关键词搜索用户
- **方法**: `GET`
- **URL**: `/friendship/search`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `keyword` (Query, String, 必填): 搜索关键词
- **响应**: `Result<List<User>>`

### 6.2 发送好友申请

- **描述**: 向指定用户发送好友申请
- **方法**: `POST`
- **URL**: `/friendship/request`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `toUserId` (Query, Long, 必填): 目标用户 ID
  - `message` (Query, String, 可选): 申请信息
- **响应**: `Result<FriendRequest>`

### 6.3 获取收到的好友申请

- **描述**: 获取当前用户收到的好友申请列表
- **方法**: `GET`
- **URL**: `/friendship/requests/received`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `status` (Query, Integer, 可选): 状态过滤
- **响应**: `Result<List<FriendRequest>>`

### 6.4 获取发送的好友申请

- **描述**: 获取当前用户发送的好友申请列表
- **方法**: `GET`
- **URL**: `/friendship/requests/sent`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `status` (Query, Integer, 可选): 状态过滤
- **响应**: `Result<List<FriendRequest>>`

### 6.5 处理好友申请

- **描述**: 同意或拒绝好友申请
- **方法**: `PUT`
- **URL**: `/friendship/request/{requestId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `requestId` (Path, Long, 必填): 申请 ID
  - `accept` (Query, Boolean, 必填): 是否同意
- **响应**: `Result<?>`

### 6.6 获取好友列表

- **描述**: 获取当前用户的好友列表
- **方法**: `GET`
- **URL**: `/friendship/friends`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<List<User>>`

### 6.7 删除好友

- **描述**: 删除指定好友
- **方法**: `DELETE`
- **URL**: `/friendship/friend/{friendId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `friendId` (Path, Long, 必填): 好友 ID
- **响应**: `Result<?>`

### 6.8 修改好友备注

- **描述**: 修改好友的备注名
- **方法**: `PUT`
- **URL**: `/friendship/friend/{friendId}/remark`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `friendId` (Path, Long, 必填): 好友 ID
  - `remark` (Query, String, 必填): 新备注
- **响应**: `Result<?>`

### 6.9 检查好友关系

- **描述**: 检查与指定用户是否为好友
- **方法**: `GET`
- **URL**: `/friendship/check/{targetUserId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `targetUserId` (Path, Long, 必填): 目标用户 ID
- **响应**: `Result<Map<String, Object>>`

### 6.10 获取好友数量

- **描述**: 获取当前用户的好友总数
- **方法**: `GET`
- **URL**: `/friendship/count`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<Map<String, Object>>`

---

## 7. 消息系统 (Message System)

### 7.1 发送消息

- **描述**: 发送私信
- **方法**: `POST`
- **URL**: `/message/send`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `toUserId` (Query, Long, 必填): 接收者 ID
  - `content` (Query, String, 必填): 消息内容
  - `messageType` (Query, Integer, 默认 1): 消息类型
- **响应**: `Result<Message>`

### 7.2 获取会话消息列表

- **描述**: 分页获取指定会话的消息记录
- **方法**: `GET`
- **URL**: `/message/conversation/{conversationId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `conversationId` (Path, String, 必填): 会话 ID
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 20): 每页数量
- **响应**: `Result<Page<Message>>`

### 7.3 获取会话列表

- **描述**: 获取当前用户的最近会话列表
- **方法**: `GET`
- **URL**: `/message/conversations`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<List<Conversation>>`

### 7.4 标记消息为已读

- **描述**: 标记单条消息为已读
- **方法**: `PUT`
- **URL**: `/message/{messageId}/read`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `messageId` (Path, Long, 必填): 消息 ID
- **响应**: `Result<?>`

### 7.5 标记会话所有消息为已读

- **描述**: 标记整个会话的消息为已读
- **方法**: `PUT`
- **URL**: `/message/conversation/{conversationId}/read`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `conversationId` (Path, String, 必填): 会话 ID
- **响应**: `Result<?>`

### 7.6 撤回消息

- **描述**: 撤回已发送的消息
- **方法**: `PUT`
- **URL**: `/message/{messageId}/recall`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `messageId` (Path, Long, 必填): 消息 ID
- **响应**: `Result<?>`

### 7.7 删除消息

- **描述**: 删除消息（仅对自己不可见）
- **方法**: `DELETE`
- **URL**: `/message/{messageId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `messageId` (Path, Long, 必填): 消息 ID
- **响应**: `Result<?>`

### 7.8 获取未读消息总数

- **描述**: 获取所有未读私信的总数
- **方法**: `GET`
- **URL**: `/message/unread/count`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<Map<String, Object>>`

### 7.9 生成会话 ID

- **描述**: 根据对方 ID 生成会话 ID
- **方法**: `GET`
- **URL**: `/message/conversation/id`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `friendId` (Query, Long, 必填): 对方用户 ID
- **响应**: `Result<Map<String, Object>>`

---

## 8. 通知系统 (Notification System)

### 8.1 获取通知列表

- **描述**: 分页获取用户的通知列表
- **方法**: `GET`
- **URL**: `/notification/list`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<Notification>>`

### 8.2 获取未读通知数量

- **描述**: 获取未读通知总数
- **方法**: `GET`
- **URL**: `/notification/unread/count`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<Long>`

### 8.3 标记通知为已读

- **描述**: 标记单条通知为已读
- **方法**: `PUT`
- **URL**: `/notification/{notificationId}/read`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `notificationId` (Path, Long, 必填): 通知 ID
- **响应**: `Result<?>`

### 8.4 标记所有通知为已读

- **描述**: 一键标记所有通知为已读
- **方法**: `PUT`
- **URL**: `/notification/all/read`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<?>`

### 8.5 删除通知

- **描述**: 删除指定通知
- **方法**: `DELETE`
- **URL**: `/notification/{notificationId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `notificationId` (Path, Long, 必填): 通知 ID
- **响应**: `Result<?>`

---

## 9. 系统互动通知 (System Notification)

### 9.1 获取通知列表

- **描述**: 分页获取系统互动通知
- **方法**: `GET`
- **URL**: `/system-notification/list`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 20): 每页数量
- **响应**: `Result<Page<SystemNotification>>`

### 9.2 获取未读通知列表

- **描述**: 获取未读的系统互动通知
- **方法**: `GET`
- **URL**: `/system-notification/unread`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 20): 每页数量
- **响应**: `Result<Page<SystemNotification>>`

### 9.3 根据类型获取通知列表

- **描述**: 获取指定类型的通知
- **方法**: `GET`
- **URL**: `/system-notification/type/{type}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `type` (Path, String, 必填): 通知类型
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 20): 每页数量
- **响应**: `Result<Page<SystemNotification>>`

### 9.4 获取未读通知数

- **描述**: 获取系统互动通知未读总数
- **方法**: `GET`
- **URL**: `/system-notification/unread/count`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<Integer>`

### 9.5 获取各类型未读通知数统计

- **描述**: 获取不同类型的未读通知数量
- **方法**: `GET`
- **URL**: `/system-notification/unread/count/by-type`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<Map<String, Integer>>`

### 9.6 标记通知为已读

- **描述**: 标记单条系统通知为已读
- **方法**: `PUT`
- **URL**: `/system-notification/{notificationId}/read`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `notificationId` (Path, Long, 必填): 通知 ID
- **响应**: `Result<?>`

### 9.7 批量标记为已读

- **描述**: 批量标记系统通知为已读
- **方法**: `PUT`
- **URL**: `/system-notification/read/batch`
- **Header**: `Authorization: Bearer <token>`
- **Body**: `List<Long>` (通知 ID 列表)
- **响应**: `Result<?>`

### 9.8 标记所有通知为已读

- **描述**: 标记所有系统通知为已读
- **方法**: `PUT`
- **URL**: `/system-notification/read/all`
- **Header**: `Authorization: Bearer <token>`
- **响应**: `Result<?>`

### 9.9 标记某类型所有通知为已读

- **描述**: 标记指定类型的所有通知为已读
- **方法**: `PUT`
- **URL**: `/system-notification/read/all/{type}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `type` (Path, String, 必填): 通知类型
- **响应**: `Result<?>`

### 9.10 删除通知

- **描述**: 删除指定系统通知
- **方法**: `DELETE`
- **URL**: `/system-notification/{notificationId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `notificationId` (Path, Long, 必填): 通知 ID
- **响应**: `Result<?>`

---

## 10. 帖子点赞 (Post Like)

### 10.1 点赞帖子

- **描述**: 点赞指定帖子
- **方法**: `POST`
- **URL**: `/like/{postId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
- **响应**: `Result<Map<String, Object>>`

### 10.2 取消点赞帖子

- **描述**: 取消对帖子的点赞
- **方法**: `DELETE`
- **URL**: `/like/{postId}`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
- **响应**: `Result<Map<String, Object>>`

### 10.3 检查点赞状态

- **描述**: 检查当前用户是否点赞了指定帖子
- **方法**: `GET`
- **URL**: `/like/{postId}/status`
- **Header**: `Authorization: Bearer <token>`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
- **响应**: `Result<Map<String, Object>>`

### 10.4 获取帖子点赞数

- **描述**: 获取帖子的总点赞数
- **方法**: `GET`
- **URL**: `/like/{postId}/count`
- **参数**:
  - `postId` (Path, Long, 必填): 帖子 ID
- **响应**: `Result<Long>`

---

## 11. 搜索 (Search)

### 11.1 搜索用户

- **描述**: 搜索用户
- **方法**: `GET`
- **URL**: `/search/users`
- **参数**:
  - `q` (Query, String, 可选): 搜索关键词
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<User>>`

### 11.2 搜索帖子

- **描述**: 搜索帖子
- **方法**: `GET`
- **URL**: `/search/posts`
- **参数**:
  - `q` (Query, String, 可选): 搜索关键词
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<Post>>`

### 11.3 获取热门话题

- **描述**: 获取热门话题列表
- **方法**: `GET`
- **URL**: `/search/topics/hot`
- **参数**:
  - `limit` (Query, int, 默认 10): 限制数量
- **响应**: `Result<List<Topic>>`

---

## 12. 话题 (Topic)

### 12.1 获取热门话题

- **描述**: 获取热门话题列表
- **方法**: `GET`
- **URL**: `/topic/hot`
- **参数**:
  - `limit` (Query, int, 默认 10): 限制数量
- **响应**: `Result<List<Topic>>`

### 12.2 根据名称获取话题

- **描述**: 根据话题名称获取话题详情
- **方法**: `GET`
- **URL**: `/topic/name/{name}`
- **参数**:
  - `name` (Path, String, 必填): 话题名称
- **响应**: `Result<Topic>`

### 12.3 根据话题名称获取帖子列表

- **描述**: 获取指定话题下的帖子列表
- **方法**: `GET`
- **URL**: `/topic/name/{name}/posts`
- **参数**:
  - `name` (Path, String, 必填): 话题名称
  - `page` (Query, int, 默认 1): 页码
  - `size` (Query, int, 默认 10): 每页数量
- **响应**: `Result<Page<Post>>`
