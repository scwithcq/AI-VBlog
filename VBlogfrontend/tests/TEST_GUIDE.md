# V-Blog 消息和好友系统 API 测试指南

## 📋 测试准备

### 1. 环境要求

- 后端服务运行在 `http://localhost:8080`
- 已注册至少 2 个测试账号
- 浏览器支持 WebSocket

### 2. 获取 JWT Token

```javascript
// 方法1: 登录后从 localStorage 获取
const token = localStorage.getItem("token");
console.log("Token:", token);

// 方法2: 从登录响应中获取
// 登录接口返回: { user: {...}, token: "xxx" }
```

## 🧪 测试方式

### 方式一：使用测试 HTML 页面（推荐）

1. 打开测试页面

   ```
   q:\gr_work\AI_work\V-Blog-Project\VBlogfrontend\tests\api-test.html
   ```

2. 配置 JWT Token

   - 在"JWT Token"输入框中粘贴你的 token

3. 设置测试用户 ID

   - 在"测试用户 ID"输入框中填入另一个用户的 ID

4. 点击测试按钮

   - 可以点击"运行全部测试"自动执行所有测试
   - 或者单独点击各个功能按钮进行测试

5. 查看结果
   - 控制台会实时显示测试日志
   - 顶部统计卡片显示测试通过率

### 方式二：使用浏览器控制台

1. 打开浏览器开发者工具（F12）

2. 在 Console 标签中加载测试脚本

   ```javascript
   // 方法1: 复制 tests/api-test.js 的内容到控制台

   // 方法2: 如果项目已运行，可以创建 script 标签加载
   const script = document.createElement("script");
   script.src = "/tests/api-test.js";
   document.head.appendChild(script);
   ```

3. 设置 Token

   ```javascript
   AUTH_TOKEN = "your_jwt_token_here";
   ```

4. 运行测试

   ```javascript
   // 运行所有测试
   await runAllTests();

   // 或运行单个测试
   await testSearchUsers();
   await testGetFriendList();
   await testGetConversations();
   ```

### 方式三：在应用中直接测试

1. 登录到应用
2. 打开浏览器控制台（F12）
3. 使用以下代码测试各个功能：

```javascript
// 导入 API 函数
import { searchUsers, getFriendList } from "./src/api/friendship.js";
import { getConversations, sendMessage } from "./src/api/message.js";

// 测试搜索用户
const searchResult = await searchUsers("test");
console.log("搜索结果:", searchResult);

// 测试获取好友列表
const friendList = await getFriendList();
console.log("好友列表:", friendList);

// 测试获取会话列表
const conversations = await getConversations();
console.log("会话列表:", conversations);
```

## 📝 测试清单

### 好友系统测试

#### ✅ 1. 搜索用户

- **接口**: `GET /friendship/search`
- **参数**: `keyword` (搜索关键词)
- **预期结果**: 返回匹配的用户列表
- **测试步骤**:
  1. 输入已存在的用户名
  2. 检查返回的用户列表
  3. 验证用户信息完整性

#### ✅ 2. 发送好友申请

- **接口**: `POST /friendship/request`
- **参数**: `toUserId`, `message` (可选)
- **预期结果**: 成功发送好友申请
- **测试步骤**:
  1. 选择一个非好友用户
  2. 发送好友申请
  3. 验证申请已发送
- **注意**: 避免向同一用户重复发送申请

#### ✅ 3. 获取收到的好友申请

- **接口**: `GET /friendship/requests/received`
- **参数**: `status` (0=待处理, 1=已同意, 2=已拒绝)
- **预期结果**: 返回收到的好友申请列表
- **测试步骤**:
  1. 查询待处理的申请
  2. 验证申请信息完整性
  3. 检查申请人信息

#### ✅ 4. 获取发送的好友申请

- **接口**: `GET /friendship/requests/sent`
- **参数**: `status` (可选)
- **预期结果**: 返回发送的好友申请列表
- **测试步骤**:
  1. 查询已发送的申请
  2. 验证申请状态
  3. 检查目标用户信息

#### ✅ 5. 处理好友申请

- **接口**: `PUT /friendship/request/{requestId}`
- **参数**: `accept` (true=同意, false=拒绝)
- **预期结果**: 成功处理申请
- **测试步骤**:
  1. 获取待处理的申请
  2. 同意或拒绝申请
  3. 验证申请状态更新
  4. 如果同意，验证好友列表更新

#### ✅ 6. 获取好友列表

- **接口**: `GET /friendship/friends`
- **预期结果**: 返回所有好友列表
- **测试步骤**:
  1. 获取好友列表
  2. 验证好友信息完整性
  3. 检查头像、昵称等字段

#### ✅ 7. 检查好友关系

- **接口**: `GET /friendship/check/{userId}`
- **预期结果**: 返回是否为好友
- **测试步骤**:
  1. 检查已知好友
  2. 检查非好友用户
  3. 验证返回值正确性

#### ✅ 8. 获取好友数量

- **接口**: `GET /friendship/count`
- **预期结果**: 返回好友总数
- **测试步骤**:
  1. 获取好友数量
  2. 验证数量与好友列表一致

#### ✅ 9. 删除好友

- **接口**: `DELETE /friendship/friend/{friendId}`
- **预期结果**: 成功删除好友
- **测试步骤**:
  1. 选择要删除的好友
  2. 执行删除操作
  3. 验证好友列表更新
- **注意**: 谨慎测试，删除后需重新添加

### 消息系统测试

#### ✅ 10. 生成会话 ID

- **接口**: `GET /message/conversation/id`
- **参数**: `otherUserId`
- **预期结果**: 返回会话 ID
- **测试步骤**:
  1. 传入好友用户 ID
  2. 验证返回的会话 ID 格式
  3. 验证 ID 格式为 `小ID_大ID`

#### ✅ 11. 发送消息

- **接口**: `POST /message/send`
- **参数**: `toUserId`, `content`, `messageType`
- **预期结果**: 消息发送成功
- **测试步骤**:
  1. 向好友发送测试消息
  2. 验证消息发送成功
  3. 检查返回的消息对象
- **注意**: 只能向好友发送消息

#### ✅ 12. 获取会话列表

- **接口**: `GET /message/conversations`
- **预期结果**: 返回所有会话列表
- **测试步骤**:
  1. 获取会话列表
  2. 验证会话信息完整性
  3. 检查最后消息和时间

#### ✅ 13. 获取会话消息列表

- **接口**: `GET /message/conversation/{conversationId}`
- **参数**: `page`, `size`
- **预期结果**: 返回指定会话的消息列表
- **测试步骤**:
  1. 选择一个会话
  2. 获取消息列表
  3. 验证消息内容和顺序
  4. 测试分页功能

#### ✅ 14. 标记消息为已读

- **接口**: `PUT /message/conversation/{conversationId}/read`
- **预期结果**: 消息标记为已读
- **测试步骤**:
  1. 选择有未读消息的会话
  2. 标记为已读
  3. 验证未读数清零

#### ✅ 15. 获取未读消息总数

- **接口**: `GET /message/unread/count`
- **预期结果**: 返回未读消息总数
- **测试步骤**:
  1. 获取未读消息数
  2. 验证数量准确性
  3. 标记已读后重新验证

#### ✅ 16. 撤回消息

- **接口**: `PUT /message/{messageId}/recall`
- **预期结果**: 消息撤回成功
- **测试步骤**:
  1. 发送一条消息
  2. 2 分钟内撤回
  3. 验证消息状态更新
- **注意**: 仅支持 2 分钟内的消息

#### ✅ 17. 删除消息

- **接口**: `DELETE /message/{messageId}`
- **预期结果**: 消息删除成功
- **测试步骤**:
  1. 选择要删除的消息
  2. 执行删除操作
  3. 验证消息已删除

### WebSocket 测试

#### ✅ 18. WebSocket 连接

- **地址**: `ws://localhost:8080/ws/message?token={token}`
- **预期结果**: 连接成功并能收发消息
- **测试步骤**:
  1. 建立 WebSocket 连接
  2. 验证连接成功
  3. 发送心跳包
  4. 接收服务器推送
  5. 测试断线重连

#### ✅ 19. 实时消息推送

- **预期结果**: 实时接收新消息
- **测试步骤**:
  1. 保持 WebSocket 连接
  2. 让另一个账号发送消息
  3. 验证实时收到消息推送
  4. 验证消息内容完整性

## 🎯 完整测试流程

### 场景 1: 添加好友并聊天（推荐完整测试）

```javascript
// 1. 登录账号A
// 在登录后获取 token

// 2. 搜索用户B
const searchResult = await testSearchUsers();
// 记录用户B的 userId

// 3. 发送好友申请
await testSendFriendRequest(); // 输入用户B的ID

// 4. 切换到账号B，查看好友申请
await testGetReceivedRequests();

// 5. 同意好友申请
// 需要从申请列表中获取 requestId
// await testHandleFriendRequest(requestId, true)

// 6. 切换回账号A，查看好友列表
await testGetFriendList();

// 7. 生成会话ID
await testGenerateConversationId(); // 输入用户B的ID

// 8. 发送消息
await testSendMessage(); // 发送测试消息

// 9. 获取会话列表
await testGetConversations();

// 10. 获取消息列表
// 需要从会话列表中获取 conversationId
// await testGetConversationMessages(conversationId)

// 11. 测试 WebSocket
testWebSocket();
// 让用户B发送消息，验证实时接收
```

## 📊 测试结果记录

### 测试环境

- 日期: ****\_\_\_****
- 后端版本: ****\_\_\_****
- 测试人员: ****\_\_\_****

### 测试结果

| 序号 | 测试项         | 预期结果     | 实际结果 | 状态          | 备注 |
| ---- | -------------- | ------------ | -------- | ------------- | ---- |
| 1    | 搜索用户       | 返回用户列表 |          | ☐ 通过 ☐ 失败 |      |
| 2    | 发送好友申请   | 申请发送成功 |          | ☐ 通过 ☐ 失败 |      |
| 3    | 获取收到的申请 | 返回申请列表 |          | ☐ 通过 ☐ 失败 |      |
| 4    | 获取发送的申请 | 返回申请列表 |          | ☐ 通过 ☐ 失败 |      |
| 5    | 处理好友申请   | 处理成功     |          | ☐ 通过 ☐ 失败 |      |
| 6    | 获取好友列表   | 返回好友列表 |          | ☐ 通过 ☐ 失败 |      |
| 7    | 检查好友关系   | 返回正确状态 |          | ☐ 通过 ☐ 失败 |      |
| 8    | 获取好友数量   | 返回正确数量 |          | ☐ 通过 ☐ 失败 |      |
| 9    | 删除好友       | 删除成功     |          | ☐ 通过 ☐ 失败 |      |
| 10   | 生成会话 ID    | 返回会话 ID  |          | ☐ 通过 ☐ 失败 |      |
| 11   | 发送消息       | 消息发送成功 |          | ☐ 通过 ☐ 失败 |      |
| 12   | 获取会话列表   | 返回会话列表 |          | ☐ 通过 ☐ 失败 |      |
| 13   | 获取消息列表   | 返回消息列表 |          | ☐ 通过 ☐ 失败 |      |
| 14   | 标记消息已读   | 已读成功     |          | ☐ 通过 ☐ 失败 |      |
| 15   | 获取未读数     | 返回未读数   |          | ☐ 通过 ☐ 失败 |      |
| 16   | 撤回消息       | 撤回成功     |          | ☐ 通过 ☐ 失败 |      |
| 17   | 删除消息       | 删除成功     |          | ☐ 通过 ☐ 失败 |      |
| 18   | WebSocket 连接 | 连接成功     |          | ☐ 通过 ☐ 失败 |      |
| 19   | 实时消息推送   | 实时接收     |          | ☐ 通过 ☐ 失败 |      |

### 统计

- 总测试数: **\_**
- 通过数: **\_**
- 失败数: **\_**
- 通过率: **\_**%

## 🐛 常见问题

### 1. Token 无效

**症状**: 所有接口返回 401 错误
**解决**:

- 检查 token 是否正确
- 确认 token 未过期
- 重新登录获取新 token

### 2. WebSocket 连接失败

**症状**: WebSocket 无法建立连接
**解决**:

- 检查后端 WebSocket 服务是否运行
- 确认 token 有效
- 检查浏览器是否支持 WebSocket

### 3. 无法向用户发送消息

**症状**: 发送消息返回错误
**解决**:

- 确认对方是好友关系
- 检查用户 ID 是否正确
- 验证消息内容不为空

### 4. 好友申请发送失败

**症状**: 发送好友申请返回错误
**解决**:

- 检查目标用户是否存在
- 确认未重复发送申请
- 验证不是向自己发送申请

### 5. CORS 跨域问题

**症状**: 浏览器控制台显示跨域错误
**解决**:

- 确认后端配置了 CORS
- 检查请求地址是否正确
- 使用代理或配置允许的源

## 📚 参考资料

- [API 文档](../API_Documentation.md)
- [消息系统文档](./MESSAGE_SYSTEM.md)
- [后端接口规范](../docs/BACKEND_API.md)

## 📞 技术支持

如有问题，请联系：

- 开发团队邮箱: dev@vblog.com
- GitHub Issues: https://github.com/your-repo/issues
