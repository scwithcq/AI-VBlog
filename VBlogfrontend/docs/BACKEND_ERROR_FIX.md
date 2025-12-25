# 后端错误修复指南

## 当前错误

```
Invalid bound statement (not found): com.vblog.mapper.NotificationMapper.insert
```

## 错误原因

这是一个后端 MyBatis 映射错误，表示`NotificationMapper.insert`方法没有找到对应的 XML 映射配置。

## 可能的原因

1. **NotificationMapper.xml 文件缺失或位置不正确**

   - 检查`src/main/resources/mapper/NotificationMapper.xml`是否存在
   - 确保 XML 文件在正确的 mapper 目录下

2. **Mapper 接口与 XML 方法名不匹配**

   - 检查`NotificationMapper.java`中的`insert`方法签名
   - 确保 XML 中有对应的`<insert id="insert">`标签

3. **MyBatis 配置问题**
   - 检查`application.yml`或`application.properties`中的 mapper 扫描路径
   - 确保配置了正确的 mapper-locations

## 后端修复步骤

### 步骤 1：检查 Mapper 接口

```java
// NotificationMapper.java
public interface NotificationMapper {
    int insert(Notification notification);
    // 其他方法...
}
```

### 步骤 2：创建/检查 XML 映射文件

在`src/main/resources/mapper/NotificationMapper.xml`中添加：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.vblog.mapper.NotificationMapper">

    <insert id="insert" parameterType="com.vblog.entity.Notification">
        INSERT INTO notification (
            user_id,
            type,
            content,
            related_id,
            create_time
        ) VALUES (
            #{userId},
            #{type},
            #{content},
            #{relatedId},
            #{createTime}
        )
    </insert>

    <!-- 其他SQL映射 -->
</mapper>
```

### 步骤 3：检查 MyBatis 配置

在`application.yml`中确保有正确的配置：

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.vblog.entity
  configuration:
    map-underscore-to-camel-case: true
```

### 步骤 4：检查表结构

确保数据库中有`notification`表：

```sql
CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收通知的用户ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型：FOLLOW, LIKE, COMMENT, REPOST',
    content TEXT COMMENT '通知内容',
    related_id BIGINT COMMENT '相关ID（帖子ID、用户ID等）',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
);
```

## 前端已实施的改进

虽然这是后端问题，但前端已经做了以下改进：

1. ✅ **状态回滚机制**：关注失败时自动恢复之前的关注状态
2. ✅ **错误日志记录**：详细记录错误信息到控制台，便于调试
3. ✅ **友好的错误提示**：避免重复显示错误消息
4. ✅ **优雅降级**：即使 API 失败，界面仍然可用

## 临时解决方案

如果暂时无法修复后端，可以考虑：

1. **禁用通知功能**：在 FollowService 中暂时注释掉创建通知的代码
2. **使用消息队列**：将通知创建改为异步处理，即使失败也不影响关注功能

## 测试验证

修复后，测试以下场景：

1. ✅ 关注用户成功
2. ✅ 取消关注成功
3. ✅ 被关注的用户收到通知
4. ✅ 通知正确存入数据库
5. ✅ 前端界面状态正确更新

## 相关文件

- 后端：

  - `NotificationMapper.java`
  - `NotificationMapper.xml`
  - `NotificationService.java`
  - `FollowService.java`

- 前端：
  - `src/components/FollowButton.vue`
  - `src/api/follow.js`
  - `src/utils/request.js`
