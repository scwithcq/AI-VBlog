# 消息页面优化说明

## 优化内容

### 1. 用户头像显示优化 ✅

**问题：** 消息页面的当前用户头像显示不正确

**解决方案：**

- 修复了 `currentUser` 的引用，从 `userStore.user` 改为 `userStore.userInfo`
- 新增 `currentUserAvatar` computed 属性，统一处理头像路径
- 自动处理相对路径和绝对路径
- 如果头像以 `/` 开头，自动添加 API 前缀 `http://localhost:8080`
- 如果没有头像，使用默认头像

**代码位置：**

- [Message.vue](../src/views/Message.vue#L13-L23) - currentUserAvatar 定义
- [Message.vue](../src/views/Message.vue#L522) - 侧边栏头像显示
- [Message.vue](../src/views/Message.vue#L605) - 聊天消息头像显示

### 2. 好友申请通知功能 ✅

**功能：** 当有新的好友申请时，在进入消息页面时显示通知

**实现细节：**

#### 2.1 未读好友申请数量显示

- 在好友申请图标上显示红色徽章，显示未读申请数量
- 数量超过 99 时显示 "99+"
- 无未读申请时徽章隐藏

#### 2.2 进入页面时提醒

- `onMounted` 时自动加载未读好友申请数量
- 如果有未读申请，弹出提示消息："您有 X 条新的好友申请"
- 提示消息显示 3 秒后自动关闭

#### 2.3 WebSocket 实时通知

- 当收到新的好友申请时，实时更新未读数量
- 显示提示消息

#### 2.4 打开好友申请对话框

- 打开对话框时加载完整的好友申请列表
- 加载后自动更新未读数量

**新增函数：**

- `loadUnreadFriendRequestCount()` - 获取未读好友申请数量

**修改的函数：**

- `loadFriendRequests()` - 加载后更新未读数量
- `handleRequest()` - 处理后刷新未读数量
- `handleWebSocketMessage()` - 收到好友申请时更新数量
- `onMounted()` - 页面加载时检查并提醒

**代码位置：**

- [Message.vue](../src/views/Message.vue#L35) - unreadFriendRequestCount 变量定义
- [Message.vue](../src/views/Message.vue#L312-L325) - loadUnreadFriendRequestCount 函数
- [Message.vue](../src/views/Message.vue#L531-L537) - Badge 徽章显示
- [Message.vue](../src/views/Message.vue#L495-L509) - onMounted 提醒逻辑

## 样式改进

### Badge 样式

```css
.badge-item {
  line-height: 1;
}

.badge-item .el-badge__content {
  background-color: #f56c6c; /* 红色背景 */
  border-radius: 10px;
  color: #fff;
  font-size: 12px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
  text-align: center;
  white-space: nowrap;
  border: 1px solid rgba(17, 24, 39, 0.6); /* 深色边框，与背景协调 */
}
```

## 测试步骤

### 1. 测试头像显示

1. 登录账号
2. 进入消息页面
3. 检查左上角的用户头像是否正确显示
4. 在聊天区域发送消息，检查自己的头像是否正确显示

### 2. 测试好友申请通知

1. 用另一个账号向当前账号发送好友申请
2. 刷新消息页面或重新进入
3. 应该看到提示消息："您有 X 条新的好友申请"
4. 检查好友申请图标上的红色数字徽章

### 3. 测试实时通知

1. 保持消息页面打开
2. 用另一个账号发送好友申请
3. 应该实时看到：
   - 提示消息弹出
   - 好友申请图标的数字更新

### 4. 测试处理好友申请

1. 点击好友申请图标
2. 查看待处理的申请列表
3. 接受或拒绝申请
4. 检查徽章数字是否减少

## 技术细节

### 头像路径处理逻辑

```javascript
const currentUserAvatar = computed(() => {
  const avatar = userStore.userInfo?.avatar;
  if (!avatar) return "https://api.dicebear.com/7.x/avataaars/svg?seed=default";
  if (avatar.startsWith("/")) {
    return `http://localhost:8080${avatar}`;
  }
  return avatar;
});
```

### 未读数量管理

- 使用响应式变量 `unreadFriendRequestCount` 存储数量
- 在多个时机更新：
  1. 页面加载时（onMounted）
  2. 收到 WebSocket 通知时
  3. 打开好友申请对话框时
  4. 处理好友申请后

## 注意事项

1. **头像路径**：确保后端返回的头像路径格式正确

   - 相对路径（如 `/src/assets/Images/girl.png`）会自动添加 API 前缀
   - 绝对路径（如 `http://...`）直接使用
   - 无头像时使用默认头像

2. **WebSocket 连接**：需要后端 WebSocket 服务正常运行才能接收实时通知

3. **性能优化**：未读数量只在必要时更新，避免频繁请求

4. **用户体验**：
   - 通知消息显示 3 秒后自动关闭
   - 徽章数字超过 99 时显示 "99+"
   - 无未读申请时徽章自动隐藏

## 相关文件

- [Message.vue](../src/views/Message.vue) - 主要实现文件
- [userStore.js](../src/store/userStore.js) - 用户状态管理
- [friendship.js](../src/api/friendship.js) - 好友相关 API
- [user.js](../src/api/user.js) - 用户信息 API

## 后续优化建议

1. **头像缓存**：考虑实现头像本地缓存机制
2. **批量标记已读**：一键标记所有好友申请为已读
3. **申请过滤**：支持筛选已处理/未处理的申请
4. **申请历史**：保存已处理的申请记录
5. **头像上传**：允许用户在消息页面快速更换头像
