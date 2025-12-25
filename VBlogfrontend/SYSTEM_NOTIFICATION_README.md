# 系统互动通知功能说明文档

## 📋 功能概述

系统互动通知功能用于记录和管理用户之间的互动行为，包括点赞、评论、关注、@提及等。该功能与好友聊天通知（`tb_notification`）独立管理，提供更好的用户体验和系统性能。

## 🗂️ 文件结构

```
src/main/java/com/vblog/
├── entity/
│   └── SystemNotification.java          # 系统通知实体类
├── mapper/
│   └── SystemNotificationMapper.java    # Mapper接口
├── service/
│   ├── SystemNotificationService.java   # Service接口
│   └── impl/
│       └── SystemNotificationServiceImpl.java  # Service实现
└── controller/
    └── SystemNotificationController.java       # Controller控制器

src/main/resources/mapper/
└── SystemNotificationMapper.xml          # MyBatis映射文件
```

## 📊 数据库表结构

表名：`tb_system_notification`

| 字段名          | 类型         | 说明                       |
| --------------- | ------------ | -------------------------- |
| id              | bigint(20)   | 通知 ID（主键）            |
| recipient_id    | bigint(20)   | 接收者用户 ID              |
| sender_id       | bigint(20)   | 发送者用户 ID              |
| sender_username | varchar(50)  | 发送者用户名（冗余字段）   |
| sender_avatar   | varchar(255) | 发送者头像（冗余字段）     |
| type            | varchar(20)  | 通知类型                   |
| post_id         | bigint(20)   | 相关帖子 ID                |
| comment_id      | bigint(20)   | 相关评论 ID                |
| content         | varchar(500) | 通知内容文本               |
| is_read         | tinyint(1)   | 是否已读（0-未读，1-已读） |
| create_time     | datetime     | 创建时间                   |
| update_time     | datetime     | 更新时间                   |

## 🎯 通知类型

| 类型 | 枚举值  | 说明              | 触发时机             |
| ---- | ------- | ----------------- | -------------------- |
| 点赞 | LIKE    | 用户点赞帖子      | 点赞帖子时自动触发   |
| 评论 | COMMENT | 用户评论或回复    | 发表评论时自动触发   |
| 提及 | MENTION | 用户在内容中@某人 | 帖子/评论包含@时触发 |
| 关注 | FOLLOW  | 用户关注          | 关注用户时自动触发   |
| 转发 | REPOST  | 用户转发帖子      | 转发帖子时自动触发   |

## 🔄 自动触发机制

### 1. 点赞通知

- **触发位置**: `PostLikeServiceImpl.likePost()`
- **触发条件**: 用户点赞帖子，且不是自己的帖子
- **通知内容**: "{用户名} 赞了你的帖子"

### 2. 评论通知

- **触发位置**: `CommentServiceImpl.addComment()`
- **触发条件**:
  - 用户评论帖子，通知帖子作者
  - 用户回复评论，同时通知帖子作者和被回复者
- **通知内容**: "{用户名} 评论了你: {评论内容预览}"

### 3. 关注通知

- **触发位置**: `FollowServiceImpl.followUser()`
- **触发条件**: 用户关注其他用户
- **通知内容**: "{用户名} 关注了你"

## 🔌 API 接口列表

### 1. 查询接口

| 接口           | 方法 | 路径                                        | 说明             |
| -------------- | ---- | ------------------------------------------- | ---------------- |
| 获取通知列表   | GET  | `/system-notification/list`                 | 分页获取所有通知 |
| 获取未读通知   | GET  | `/system-notification/unread`               | 分页获取未读通知 |
| 按类型获取通知 | GET  | `/system-notification/type/{type}`          | 获取指定类型通知 |
| 获取未读数     | GET  | `/system-notification/unread/count`         | 获取未读通知总数 |
| 获取类型统计   | GET  | `/system-notification/unread/count/by-type` | 各类型未读数统计 |

### 2. 操作接口

| 接口             | 方法   | 路径                                   | 说明               |
| ---------------- | ------ | -------------------------------------- | ------------------ |
| 标记已读         | PUT    | `/system-notification/{id}/read`       | 标记单个通知已读   |
| 批量标记已读     | PUT    | `/system-notification/read/batch`      | 批量标记已读       |
| 全部标记已读     | PUT    | `/system-notification/read/all`        | 标记所有通知已读   |
| 类型全部标记已读 | PUT    | `/system-notification/read/all/{type}` | 标记某类型全部已读 |
| 删除通知         | DELETE | `/system-notification/{id}`            | 删除单个通知       |
| 清理旧通知       | DELETE | `/system-notification/cleanup`         | 清理已读旧通知     |

## 💡 使用示例

### 前端获取未读通知数（用于显示小红点）

```javascript
// 获取未读通知总数
fetch("/api/system-notification/unread/count", {
  headers: { Authorization: "Bearer " + token },
})
  .then((res) => res.json())
  .then((data) => {
    console.log("未读通知数:", data.data); // 例如: 5
  });

// 获取各类型未读数
fetch("/api/system-notification/unread/count/by-type", {
  headers: { Authorization: "Bearer " + token },
})
  .then((res) => res.json())
  .then((data) => {
    console.log("点赞未读:", data.data.LIKE); // 例如: 3
    console.log("评论未读:", data.data.COMMENT); // 例如: 2
    console.log("关注未读:", data.data.FOLLOW); // 例如: 0
  });
```

### 前端获取通知列表

```javascript
fetch("/api/system-notification/list?page=1&size=20", {
  headers: { Authorization: "Bearer " + token },
})
  .then((res) => res.json())
  .then((data) => {
    const notifications = data.data.list;
    notifications.forEach((n) => {
      console.log(`${n.senderUsername} ${n.content}`);
    });
  });
```

### 前端标记已读

```javascript
// 标记单个通知已读
fetch(`/api/system-notification/${notificationId}/read`, {
  method: "PUT",
  headers: { Authorization: "Bearer " + token },
});

// 标记所有通知已读
fetch("/api/system-notification/read/all", {
  method: "PUT",
  headers: { Authorization: "Bearer " + token },
});

// 批量标记已读
fetch("/api/system-notification/read/batch", {
  method: "PUT",
  headers: {
    Authorization: "Bearer " + token,
    "Content-Type": "application/json",
  },
  body: JSON.stringify([1, 2, 3, 4, 5]),
});
```

## ⚙️ 性能优化

### 1. 冗余字段设计

- `sender_username` 和 `sender_avatar` 是冗余字段
- 避免每次查询通知时都关联用户表
- 提高查询性能

### 2. 索引优化

已创建的索引：

- `idx_recipient_id`: 按接收者查询
- `idx_sender_id`: 按发送者查询
- `idx_is_read`: 按已读状态查询
- `idx_recipient_read`: 组合索引，查询某用户的未读通知
- `idx_recipient_time`: 组合索引，按时间排序某用户的通知

### 3. 定期清理

建议定期清理已读的旧通知，减少数据量：

```sql
-- 清理30天前的已读通知
DELETE FROM tb_system_notification
WHERE is_read = 1
AND create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

或使用 API 接口：

```bash
DELETE /api/system-notification/cleanup?days=30
```

## 🚀 部署步骤

1. **创建数据库表**

   ```bash
   # 在MySQL中执行SQL脚本
   mysql -u root -p vblog_db < create_system_notification_table.sql
   ```

2. **重启应用**

   ```bash
   mvn spring-boot:run
   ```

3. **测试接口**
   - 使用 `test_system_notification.http` 文件进行接口测试
   - 或使用 Postman 等工具测试

## 🔍 故障排查

### 1. 通知没有生成

- 检查相关业务逻辑是否正常执行
- 查看日志中是否有 "Failed to create notification" 错误
- 确认发送者和接收者不是同一个用户

### 2. 查询不到通知

- 确认 `recipient_id` 是否正确
- 检查数据库表中是否有数据
- 查看日志确认 SQL 执行情况

### 3. 性能问题

- 检查数据库索引是否创建
- 定期清理旧的已读通知
- 考虑使用 Redis 缓存未读数统计

## 📝 扩展建议

### 1. 实时推送

集成 WebSocket 或 SSE，实现通知的实时推送：

```java
@Service
public class NotificationPushService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void pushNotification(Long userId, SystemNotification notification) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
    }
}
```

### 2. 消息聚合

对于同类型的多条通知进行聚合显示：

- "张三、李四和其他 3 人赞了你的帖子"
- "王五、赵六回复了你的评论"

### 3. 通知偏好设置

允许用户自定义通知接收偏好：

- 选择接收哪些类型的通知
- 设置免打扰时间段

## 📞 技术支持

如有问题，请联系开发团队或查看完整 API 文档。
