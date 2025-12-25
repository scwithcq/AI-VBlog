/**
 * V-Blog 消息和好友系统 API 测试脚本
 * 
 * 使用方法：
 * 1. 确保后端服务运行在 http://localhost:8080
 * 2. 在浏览器控制台中运行此脚本
 * 3. 或者在 Node.js 环境中使用 axios 运行
 * 
 * 注意：需要先登录获取 token
 */

// 配置
const BASE_URL = 'http://localhost:8080/api'
let AUTH_TOKEN = '' // 需要填入实际的 JWT token

// 工具函数
const request = async (url, method = 'GET', data = null, isFormData = false) => {
  const options = {
    method,
    headers: {
      'Authorization': `Bearer ${AUTH_TOKEN}`,
      ...(isFormData ? {} : { 'Content-Type': 'application/json' })
    }
  }

  if (data) {
    if (isFormData) {
      const formData = new URLSearchParams()
      Object.keys(data).forEach(key => formData.append(key, data[key]))
      options.body = formData
    } else if (method !== 'GET') {
      options.body = JSON.stringify(data)
    }
  }

  try {
    const response = await fetch(`${BASE_URL}${url}`, options)
    const result = await response.json()
    return result
  } catch (error) {
    console.error('请求失败:', error)
    return { code: -1, msg: error.message }
  }
}

// ==================== 好友系统测试 ====================

console.log('========================================')
console.log('开始测试好友系统 API')
console.log('========================================\n')

/**
 * 测试1: 搜索用户
 */
async function testSearchUsers() {
  console.log('【测试1】搜索用户')
  console.log('接口: GET /friendship/search')
  
  const keyword = 'test' // 修改为实际要搜索的关键词
  const result = await request(`/friendship/search?keyword=${keyword}`)
  
  console.log('请求参数:', { keyword })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试2: 发送好友申请
 */
async function testSendFriendRequest() {
  console.log('【测试2】发送好友申请')
  console.log('接口: POST /friendship/request')
  
  const data = {
    toUserId: 2, // 修改为实际的用户ID
    message: '你好，我想加你为好友'
  }
  
  const formData = new URLSearchParams()
  formData.append('toUserId', data.toUserId)
  formData.append('message', data.message)
  
  const result = await fetch(`${BASE_URL}/friendship/request`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${AUTH_TOKEN}`,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData
  }).then(res => res.json())
  
  console.log('请求参数:', data)
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试3: 获取收到的好友申请
 */
async function testGetReceivedRequests() {
  console.log('【测试3】获取收到的好友申请')
  console.log('接口: GET /friendship/requests/received')
  
  const result = await request('/friendship/requests/received?status=0')
  
  console.log('请求参数:', { status: 0 })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试4: 获取发送的好友申请
 */
async function testGetSentRequests() {
  console.log('【测试4】获取发送的好友申请')
  console.log('接口: GET /friendship/requests/sent')
  
  const result = await request('/friendship/requests/sent')
  
  console.log('请求参数:', {})
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试5: 处理好友申请
 */
async function testHandleFriendRequest(requestId) {
  console.log('【测试5】处理好友申请')
  console.log('接口: PUT /friendship/request/{requestId}')
  
  const accept = true // true=同意, false=拒绝
  const result = await request(`/friendship/request/${requestId}?accept=${accept}`, 'PUT')
  
  console.log('请求参数:', { requestId, accept })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试6: 获取好友列表
 */
async function testGetFriendList() {
  console.log('【测试6】获取好友列表')
  console.log('接口: GET /friendship/friends')
  
  const result = await request('/friendship/friends')
  
  console.log('请求参数:', {})
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试7: 检查好友关系
 */
async function testCheckFriendship(userId) {
  console.log('【测试7】检查好友关系')
  console.log('接口: GET /friendship/check/{userId}')
  
  const result = await request(`/friendship/check/${userId}`)
  
  console.log('请求参数:', { userId })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试8: 获取好友数量
 */
async function testGetFriendCount() {
  console.log('【测试8】获取好友数量')
  console.log('接口: GET /friendship/count')
  
  const result = await request('/friendship/count')
  
  console.log('请求参数:', {})
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

// ==================== 消息系统测试 ====================

console.log('========================================')
console.log('开始测试消息系统 API')
console.log('========================================\n')

/**
 * 测试9: 生成会话ID
 */
async function testGenerateConversationId(otherUserId) {
  console.log('【测试9】生成会话ID')
  console.log('接口: GET /message/conversation/id')
  
  const result = await request(`/message/conversation/id?otherUserId=${otherUserId}`)
  
  console.log('请求参数:', { otherUserId })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试10: 发送消息
 */
async function testSendMessage(toUserId, content) {
  console.log('【测试10】发送消息')
  console.log('接口: POST /message/send')
  
  const formData = new URLSearchParams()
  formData.append('toUserId', toUserId)
  formData.append('content', content)
  formData.append('messageType', 1)
  
  const result = await fetch(`${BASE_URL}/message/send`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${AUTH_TOKEN}`,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData
  }).then(res => res.json())
  
  console.log('请求参数:', { toUserId, content, messageType: 1 })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试11: 获取会话列表
 */
async function testGetConversations() {
  console.log('【测试11】获取会话列表')
  console.log('接口: GET /message/conversations')
  
  const result = await request('/message/conversations')
  
  console.log('请求参数:', {})
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试12: 获取会话消息列表
 */
async function testGetConversationMessages(conversationId) {
  console.log('【测试12】获取会话消息列表')
  console.log('接口: GET /message/conversation/{conversationId}')
  
  const result = await request(`/message/conversation/${conversationId}?page=1&size=20`)
  
  console.log('请求参数:', { conversationId, page: 1, size: 20 })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试13: 标记会话消息为已读
 */
async function testMarkConversationRead(conversationId) {
  console.log('【测试13】标记会话消息为已读')
  console.log('接口: PUT /message/conversation/{conversationId}/read')
  
  const result = await request(`/message/conversation/${conversationId}/read`, 'PUT')
  
  console.log('请求参数:', { conversationId })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试14: 获取未读消息总数
 */
async function testGetUnreadCount() {
  console.log('【测试14】获取未读消息总数')
  console.log('接口: GET /message/unread/count')
  
  const result = await request('/message/unread/count')
  
  console.log('请求参数:', {})
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试15: 撤回消息
 */
async function testRecallMessage(messageId) {
  console.log('【测试15】撤回消息')
  console.log('接口: PUT /message/{messageId}/recall')
  
  const result = await request(`/message/${messageId}/recall`, 'PUT')
  
  console.log('请求参数:', { messageId })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

/**
 * 测试16: 删除消息
 */
async function testDeleteMessage(messageId) {
  console.log('【测试16】删除消息')
  console.log('接口: DELETE /message/{messageId}')
  
  const result = await request(`/message/${messageId}`, 'DELETE')
  
  console.log('请求参数:', { messageId })
  console.log('响应结果:', result)
  console.log('测试结果:', result.code === 0 ? '✅ 成功' : '❌ 失败')
  console.log('---\n')
  
  return result
}

// ==================== WebSocket 测试 ====================

/**
 * 测试17: WebSocket 连接
 */
function testWebSocket() {
  console.log('【测试17】WebSocket 连接测试')
  console.log('接口: ws://localhost:8080/ws/message')
  
  const wsUrl = `ws://localhost:8080/ws/message?token=${AUTH_TOKEN}`
  const ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('✅ WebSocket 连接成功')
    
    // 发送心跳
    ws.send(JSON.stringify({ type: 'ping' }))
  }
  
  ws.onmessage = (event) => {
    console.log('📨 收到消息:', event.data)
    const data = JSON.parse(event.data)
    console.log('消息类型:', data.type)
    console.log('消息内容:', data.data)
  }
  
  ws.onerror = (error) => {
    console.error('❌ WebSocket 错误:', error)
  }
  
  ws.onclose = () => {
    console.log('⚠️ WebSocket 连接关闭')
  }
  
  console.log('WebSocket 连接已建立，等待消息...')
  console.log('---\n')
  
  return ws
}

// ==================== 执行测试 ====================

/**
 * 运行所有测试（顺序执行）
 */
async function runAllTests() {
  console.log('\n')
  console.log('╔════════════════════════════════════════╗')
  console.log('║   V-Blog API 完整测试套件              ║')
  console.log('╚════════════════════════════════════════╝')
  console.log('\n')
  
  // 检查 token
  if (!AUTH_TOKEN) {
    console.error('❌ 错误: 请先设置 AUTH_TOKEN')
    console.log('使用方法: AUTH_TOKEN = "your_jwt_token"')
    return
  }
  
  const testResults = {
    total: 0,
    passed: 0,
    failed: 0
  }
  
  try {
    // 好友系统测试
    console.log('\n🔵 好友系统测试\n')
    
    const searchResult = await testSearchUsers()
    testResults.total++
    if (searchResult.code === 0) testResults.passed++
    else testResults.failed++
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 如果搜索到用户，尝试发送好友申请
    if (searchResult.code === 0 && searchResult.data && searchResult.data.length > 0) {
      // await testSendFriendRequest()
      // testResults.total++
      console.log('⚠️ 跳过发送好友申请测试（避免重复申请）')
    }
    
    const receivedResult = await testGetReceivedRequests()
    testResults.total++
    if (receivedResult.code === 0) testResults.passed++
    else testResults.failed++
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const sentResult = await testGetSentRequests()
    testResults.total++
    if (sentResult.code === 0) testResults.passed++
    else testResults.failed++
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const friendListResult = await testGetFriendList()
    testResults.total++
    if (friendListResult.code === 0) testResults.passed++
    else testResults.failed++
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const friendCountResult = await testGetFriendCount()
    testResults.total++
    if (friendCountResult.code === 0) testResults.passed++
    else testResults.failed++
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 消息系统测试
    console.log('\n🔵 消息系统测试\n')
    
    const conversationsResult = await testGetConversations()
    testResults.total++
    if (conversationsResult.code === 0) testResults.passed++
    else testResults.failed++
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const unreadCountResult = await testGetUnreadCount()
    testResults.total++
    if (unreadCountResult.code === 0) testResults.passed++
    else testResults.failed++
    
    // 如果有好友，测试消息相关功能
    if (friendListResult.code === 0 && friendListResult.data && friendListResult.data.length > 0) {
      const friendId = friendListResult.data[0].userId
      
      await new Promise(resolve => setTimeout(resolve, 500))
      
      const convIdResult = await testGenerateConversationId(friendId)
      testResults.total++
      if (convIdResult.code === 0) testResults.passed++
      else testResults.failed++
      
      // 测试发送消息
      // await testSendMessage(friendId, '这是一条测试消息')
      // testResults.total++
      console.log('⚠️ 跳过发送消息测试（避免发送测试消息）')
      
      // 如果有会话，测试获取消息
      if (conversationsResult.code === 0 && conversationsResult.data && conversationsResult.data.length > 0) {
        const conversationId = conversationsResult.data[0].conversationId
        
        await new Promise(resolve => setTimeout(resolve, 500))
        
        const messagesResult = await testGetConversationMessages(conversationId)
        testResults.total++
        if (messagesResult.code === 0) testResults.passed++
        else testResults.failed++
      }
    }
    
    // WebSocket 测试
    console.log('\n🔵 WebSocket 测试\n')
    console.log('⚠️ WebSocket 测试需要手动验证')
    // testWebSocket()
    
  } catch (error) {
    console.error('测试过程中出现错误:', error)
  }
  
  // 输出测试结果摘要
  console.log('\n')
  console.log('╔════════════════════════════════════════╗')
  console.log('║          测试结果摘要                  ║')
  console.log('╚════════════════════════════════════════╝')
  console.log(`总测试数: ${testResults.total}`)
  console.log(`✅ 通过: ${testResults.passed}`)
  console.log(`❌ 失败: ${testResults.failed}`)
  console.log(`通过率: ${((testResults.passed / testResults.total) * 100).toFixed(2)}%`)
  console.log('\n')
}

// ==================== 使用说明 ====================

console.log('\n')
console.log('════════════════════════════════════════')
console.log('         V-Blog API 测试工具            ')
console.log('════════════════════════════════════════')
console.log('\n使用方法：')
console.log('1. 设置 token:')
console.log('   AUTH_TOKEN = "your_jwt_token_here"')
console.log('\n2. 运行所有测试:')
console.log('   runAllTests()')
console.log('\n3. 或运行单个测试:')
console.log('   await testSearchUsers()')
console.log('   await testGetFriendList()')
console.log('   await testGetConversations()')
console.log('   // ... 更多测试函数')
console.log('\n4. WebSocket 测试:')
console.log('   testWebSocket()')
console.log('\n════════════════════════════════════════\n')

// 导出函数供外部使用
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    runAllTests,
    testSearchUsers,
    testSendFriendRequest,
    testGetReceivedRequests,
    testGetSentRequests,
    testHandleFriendRequest,
    testGetFriendList,
    testCheckFriendship,
    testGetFriendCount,
    testGenerateConversationId,
    testSendMessage,
    testGetConversations,
    testGetConversationMessages,
    testMarkConversationRead,
    testGetUnreadCount,
    testRecallMessage,
    testDeleteMessage,
    testWebSocket
  }
}
