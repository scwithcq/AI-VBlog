# 🧪 API 测试工具包

这是 V-Blog 消息和好友系统的完整测试工具包，包含多种测试方式供您选择。

## 📁 文件说明

- **api-test.html** - 可视化测试页面（推荐使用）
- **api-test.js** - 命令行测试脚本
- **TEST_GUIDE.md** - 详细测试指南文档

## 🚀 快速开始

### 方法 1: 使用可视化测试页面（最简单）

1. 双击打开 `api-test.html` 文件

2. 在页面中输入你的 JWT Token：

   ```
   登录后从浏览器控制台运行: localStorage.getItem('token')
   ```

3. 点击"运行全部测试"按钮，或单独测试各个功能

4. 在控制台区域查看测试结果

**优点**:

- ✅ 图形化界面，使用简单
- ✅ 实时显示测试结果
- ✅ 统计数据一目了然
- ✅ 无需编程基础

![测试页面预览](https://via.placeholder.com/800x400?text=API+Test+Tool)

### 方法 2: 使用浏览器控制台

1. 打开 V-Blog 应用并登录

2. 按 F12 打开浏览器开发者工具

3. 在 Console 标签中粘贴 `api-test.js` 的内容

4. 设置 Token 并运行测试：

   ```javascript
   // 设置 Token
   AUTH_TOKEN = localStorage.getItem("token");

   // 运行所有测试
   await runAllTests();

   // 或运行单个测试
   await testSearchUsers();
   await testGetFriendList();
   ```

**优点**:

- ✅ 灵活控制测试流程
- ✅ 可查看详细的响应数据
- ✅ 适合开发调试

### 方法 3: 在应用中测试

直接在应用的消息页面进行功能测试：

1. 登录应用
2. 进入消息页面
3. 使用界面功能：
   - 点击 "+" 添加好友
   - 点击 "铃铛" 查看好友申请
   - 点击 "人像" 查看好友列表
   - 选择好友发送消息

**优点**:

- ✅ 最真实的用户体验
- ✅ 测试实际功能流程
- ✅ 验证 UI 交互

## 📋 测试前准备

### 1. 确保后端服务运行

```bash
# 检查后端是否运行
curl http://localhost:8080/api/user/info
```

### 2. 准备测试账号

至少需要 2 个测试账号：

- 账号 A: 用于发起测试
- 账号 B: 用于接收测试（好友申请、消息等）

### 3. 获取 JWT Token

登录后运行：

```javascript
console.log(localStorage.getItem("token"));
```

## 🎯 测试覆盖范围

### 好友系统 (9 个接口)

- ✅ 搜索用户
- ✅ 发送好友申请
- ✅ 获取收到的好友申请
- ✅ 获取发送的好友申请
- ✅ 处理好友申请（同意/拒绝）
- ✅ 获取好友列表
- ✅ 检查好友关系
- ✅ 修改好友备注
- ✅ 删除好友

### 消息系统 (8 个接口)

- ✅ 生成会话 ID
- ✅ 发送消息
- ✅ 获取会话列表
- ✅ 获取会话消息列表
- ✅ 标记消息为已读
- ✅ 获取未读消息总数
- ✅ 撤回消息
- ✅ 删除消息

### WebSocket (实时通信)

- ✅ 建立连接
- ✅ 心跳机制
- ✅ 实时消息推送
- ✅ 断线重连

## 📊 测试示例输出

```
========================================
开始测试好友系统 API
========================================

【测试1】搜索用户
接口: GET /friendship/search
请求参数: { keyword: 'test' }
响应结果: {
  "code": 0,
  "msg": "成功",
  "data": [
    {
      "userId": 2,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": "http://..."
    }
  ]
}
测试结果: ✅ 成功
---

【测试6】获取好友列表
接口: GET /friendship/friends
请求参数: {}
响应结果: {
  "code": 0,
  "msg": "成功",
  "data": [...]
}
测试结果: ✅ 成功
---

╔════════════════════════════════════════╗
║          测试结果摘要                  ║
╚════════════════════════════════════════╝
总测试数: 15
✅ 通过: 15
❌ 失败: 0
通过率: 100.00%
```

## 🔧 配置说明

在 `api-test.html` 或 `api-test.js` 中可以配置：

```javascript
// API 基础地址
const BASE_URL = "http://localhost:8080/api";

// JWT Token
let AUTH_TOKEN = "your_token_here";

// 测试用户ID
const TEST_USER_ID = 2;
```

## 📝 完整测试流程示例

```javascript
// 1. 设置环境
AUTH_TOKEN = "your_jwt_token";

// 2. 测试好友系统
await testSearchUsers(); // 搜索用户
await testSendFriendRequest(); // 发送好友申请
await testGetReceivedRequests(); // 查看收到的申请
await testGetFriendList(); // 获取好友列表

// 3. 测试消息系统
await testGenerateConversationId(); // 生成会话ID
await testSendMessage(); // 发送消息
await testGetConversations(); // 获取会话列表
await testGetUnreadCount(); // 获取未读数

// 4. 测试 WebSocket
testWebSocket(); // 测试实时通信

// 5. 运行所有测试
await runAllTests(); // 自动化测试所有接口
```

## ⚠️ 注意事项

1. **避免重复测试**

   - 不要频繁发送好友申请
   - 不要重复删除好友
   - 发送测试消息时注意数量

2. **数据安全**

   - 不要在生产环境运行测试
   - 保护好你的 JWT Token
   - 测试完成后清理测试数据

3. **网络要求**

   - 确保后端服务可访问
   - WebSocket 测试需要稳定网络
   - 注意跨域问题

4. **浏览器兼容性**
   - 推荐使用 Chrome/Edge
   - 需要支持 WebSocket
   - 需要支持 ES6+

## 🐛 常见问题

### Token 无效

```javascript
// 重新获取 token
AUTH_TOKEN = localStorage.getItem("token");
```

### 后端未运行

```bash
# 启动后端服务
cd backend
npm start  # 或 java -jar xxx.jar
```

### WebSocket 连接失败

检查后端 WebSocket 服务是否启动在 `ws://localhost:8080/ws/message`

### CORS 错误

确保后端配置了正确的 CORS 策略

## 📚 更多信息

- 📖 [详细测试指南](./TEST_GUIDE.md)
- 📖 [API 接口文档](../API_Documentation.md)
- 📖 [消息系统文档](../docs/MESSAGE_SYSTEM.md)

## 💡 提示

**首次测试建议**：

1. 先使用 `api-test.html` 可视化工具
2. 按顺序测试各个功能模块
3. 记录测试结果和问题
4. 参考 TEST_GUIDE.md 了解详细步骤

**问题排查**：

1. 检查浏览器控制台错误信息
2. 验证 Token 是否有效
3. 确认后端服务状态
4. 查看网络请求详情

## 🎉 开始测试

现在就打开 `api-test.html` 开始测试吧！

```bash
# 在浏览器中打开
start api-test.html  # Windows
open api-test.html   # macOS
xdg-open api-test.html  # Linux
```

祝测试顺利！🚀
