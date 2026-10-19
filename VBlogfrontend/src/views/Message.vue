<script setup>
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox, ElDialog, ElInput } from 'element-plus'
import Header from '../components/Header.vue'
import { ElAvatar, ElButton, ElBadge, ElIcon, ElScrollbar } from 'element-plus'
import { Search, Plus, MoreFilled, Picture, Paperclip, Bell, User } from '@element-plus/icons-vue'
import { useUserStore } from '../store/userStore'
import { useRealtimeStore } from '../store/realtimeStore'
import { getConversations, getConversationMessages, sendMessage as sendMessageApi, markConversationRead, generateConversationId } from '../api/message'
import { getFriendList, searchUsers, sendFriendRequest, getReceivedRequests, handleFriendRequest, deleteFriend } from '../api/friendship'
import { getUserInfo } from '../api/user'
import { getImageUrl } from '../utils/image'

const userStore = useUserStore()
const realtimeStore = useRealtimeStore()
const currentUser = computed(() => userStore.userInfo)
const currentUserAvatar = computed(() => {
  // 确保头像路径正确，如果是相对路径则转换为完整URL
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return '/default-avatar.jpg'
  // 如果是相对路径，添加API前缀
  return getImageUrl(avatar)
})

const activeConversationId = ref('')
const activeOtherUser = ref(null)
const pendingConversationId = ref('')
const messageInput = ref('')
const searchKeyword = ref('')
const scrollbarRef = ref(null)
const conversations = ref([])
const messages = ref([])
const loading = ref(false)
let messageLoadId = 0
let conversationLoadId = 0
let disposed = false
let reconnectTimer = null

// 添加好友相关
const showAddFriendDialog = ref(false)
const showFriendRequestDialog = ref(false)
const showFriendListDialog = ref(false)
const friendSearchKeyword = ref('')
const friendSearchResults = ref([])
const friendRequests = ref([])
const friendList = ref([])
const friendRequestMessage = ref('')
const unreadFriendRequestCount = ref(0)

// WebSocket 和轮询相关
const ws = ref(null)
const wsConnected = ref(false)
const wsReconnectAttempts = ref(0)
const maxReconnectAttempts = 3
const pollingTimer = ref(null)
const pollingInterval = 3000 // 3秒轮询一次

// WebSocket 连接
const connectWebSocket = () => {
  if (disposed) return
  const token = sessionStorage.getItem('token')
  if (!token) {
    console.warn('未找到 Token，无法连接 WebSocket')
    startPolling() // 启动轮询
    return
  }

  try {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${protocol}//${window.location.host}/api/ws/message?token=${encodeURIComponent(token)}`
    ws.value = new WebSocket(wsUrl)

    ws.value.onopen = () => {
      console.log('✅ WebSocket 连接成功')
      wsConnected.value = true
      wsReconnectAttempts.value = 0
      stopPolling() // 停止轮询
    }

    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleWebSocketMessage(data)
      } catch (error) {
        console.error('WebSocket 消息解析失败:', error)
      }
    }

    ws.value.onerror = (error) => {
      // WebSocket连接错误，静默处理
      wsConnected.value = false
    }

    ws.value.onclose = (event) => {
      if (disposed) return
      console.log('⚠️ WebSocket 连接关闭')
      wsConnected.value = false

      // 尝试重连
      if (wsReconnectAttempts.value < maxReconnectAttempts) {
        wsReconnectAttempts.value++
        const delay = Math.min(1000 * Math.pow(2, wsReconnectAttempts.value), 10000)
        console.log(`尝试第 ${wsReconnectAttempts.value} 次重连 WebSocket (延迟 ${delay}ms)...`)
        reconnectTimer = setTimeout(() => {
          connectWebSocket()
        }, delay)
      } else {
        console.log('🔄 WebSocket 重连失败，已切换到轮询模式')
        // 不显示警告消息，静默切换
        startPolling() // 启动轮询
      }
    }
  } catch (error) {
    console.error('WebSocket 初始化失败:', error)
    startPolling() // 启动轮询
  }
}

// 启动轮询机制
const startPolling = () => {
  if (pollingTimer.value) return // 已经在轮询了

  console.log('🔄 启动轮询模式，间隔:', pollingInterval, 'ms')

  // 立即载入一次
  pollMessages()

  // 设置定时轮询
  pollingTimer.value = setInterval(() => {
    pollMessages()
  }, pollingInterval)
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer.value) {
    console.log('⏸️ 停止轮询模式')
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

// 轮询获取消息
const pollMessages = async () => {
  try {
    // 只在有活动会话时轮询消息（静默加载）
    if (activeConversationId.value) {
      // 确保conversationId是字符串
      const convId = typeof activeConversationId.value === 'object'
        ? activeConversationId.value.conversationId
        : String(activeConversationId.value)

      if (convId) {
        await loadMessages(convId, { silent: true, skipScroll: false })
      }
    }
    // 静默刷新会话列表
    await loadConversations()
  } catch (error) {
    console.error('轮询消息失败:', error)
  }
}

// 处理 WebSocket 消息
const handleWebSocketMessage = (data) => {
  if (!data || !data.type) {
    console.warn('收到无效的 WebSocket 消息:', data)
    return
  }

  switch (data.type) {
    case 'new_message':
      // 确保activeConversationId是字符串
      const currentConvId = typeof activeConversationId.value === 'object'
        ? activeConversationId.value.conversationId
        : String(activeConversationId.value)

      if (data.data && String(data.data.conversationId) === currentConvId) {
        // 当前会话收到新消息，添加到消息列表
        if (!messages.value.some(msg => msg.messageId === data.data.messageId)) {
          messages.value.push({
            ...data.data,
            type: data.data.fromUserId === currentUser.value.userId ? 'sent' : 'received',
            time: formatMessageTime(data.data.createTime)
          })
          scrollToBottom()
        }
        markConversationRead(currentConvId).then(() => {
          const active = conversations.value.find(conv => conv.conversationId === currentConvId)
          if (active) active.unread = 0
          loadConversations()
          realtimeStore.refresh()
        }).catch(error => console.error('标记消息已读失败:', error))
      }
      // 更新会话列表
      loadConversations()
      break
    case 'message_read':
      // 消息已读通知
      console.log('消息已读:', data.data)
      break
    case 'message_recall':
      // 消息撤回通知
      if (data.data && data.data.messageId) {
        const recalledIndex = messages.value.findIndex(m => m.messageId === data.data.messageId)
        if (recalledIndex !== -1) {
          messages.value[recalledIndex].status = 2
          messages.value[recalledIndex].content = '消息已撤回'
        }
        loadConversations()
        realtimeStore.refresh()
      }
      break
    case 'friend_request':
      // 好友申请通知
      loadUnreadFriendRequestCount()
      break
    case 'pong':
      // 心跳响应
      console.log('收到心跳响应')
      break
    default:
      console.log('未处理的消息类型:', data.type)
  }
}

// 加载会话列表
const loadConversations = async () => {
  try {
    const loadId = ++conversationLoadId
    const userId = currentUser.value?.userId
    if (!userId) return
    const data = await getConversations()
    if (loadId !== conversationLoadId || currentUser.value?.userId !== userId) return
    if (data && Array.isArray(data)) {
      const loadedConversations = await Promise.all(data.map(async conv => {
        // 获取对方用户ID
        const otherUserId = conv.user1Id === userId ? conv.user2Id : conv.user1Id

        // 获取对方用户信息
        let otherUser = null
        try {
          otherUser = await getUserInfo(otherUserId)
        } catch (error) {
          console.error(`获取用户 ${otherUserId} 信息失败:`, error)
        }

        // 计算未读数
        const unreadCount = conv.user1Id === userId
          ? (conv.user1UnreadCount || 0)
          : (conv.user2UnreadCount || 0)

        return {
          conversationId: conv.conversationId,
          otherUserId,
          username: otherUser?.nickname || otherUser?.username || '未知用户',
          avatar: otherUser?.avatar || '/default-avatar.jpg',
          lastMsg: conv.lastMessageContent || '',
          time: formatConversationTime(conv.lastMessageTime),
          unread: unreadCount,
          otherUser
        }
      }))
      if (loadId !== conversationLoadId || currentUser.value?.userId !== userId) return
      conversations.value = loadedConversations

      if (activeConversationId.value) {
        const activeConv = conversations.value.find(c => c.conversationId === activeConversationId.value)
        if (activeConv) {
          activeOtherUser.value = activeConv.otherUser
          pendingConversationId.value = ''
        } else if (activeConversationId.value !== pendingConversationId.value) {
          activeConversationId.value = ''
          activeOtherUser.value = null
          messages.value = []
        }
      }
    } else {
      conversations.value = []
      if (activeConversationId.value && activeConversationId.value !== pendingConversationId.value) {
        activeConversationId.value = ''
        activeOtherUser.value = null
        messages.value = []
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    conversations.value = []
    ElMessage.error('加载会话列表失败，请稍后重试')
  }
}

// 加载消息列表
const loadMessages = async (conversationId, options = {}) => {
  if (!conversationId) return

  // 确保 conversationId 是字符串，而不是对象
  let convIdStr
  if (typeof conversationId === 'object') {
    console.warn('警告：conversationId 是对象，尝试提取字符串:', conversationId)
    convIdStr = conversationId.conversationId || conversationId.data || String(conversationId)
  } else {
    convIdStr = String(conversationId)
  }

  if (!convIdStr || convIdStr === '[object Object]') {
    console.error('错误：无法从conversationId提取有效字符串:', conversationId)
    return
  }

  const { silent = false, skipScroll = false } = options
  const loadId = ++messageLoadId

  if (!silent) {
    loading.value = true
  }

  try {
    const data = await getConversationMessages(convIdStr, { page: 1, size: 100 })
    if (loadId !== messageLoadId || activeConversationId.value !== convIdStr) return
    if (data && data.records && Array.isArray(data.records)) {
      const oldLength = messages.value.length
      const newMessages = data.records
        .map(msg => ({
          ...msg,
          type: msg.fromUserId === currentUser.value.userId ? 'sent' : 'received',
          content: msg.status === 2 ? '消息已撤回' : msg.content,
          time: formatMessageTime(msg.createTime)
        }))
        // 按时间升序排列（旧消息在前，新消息在后）
        .sort((a, b) => new Date(a.createTime) - new Date(b.createTime))

      messages.value = newMessages

      // 只在有新消息时滚动，且不是静默加载
      if (!skipScroll && (newMessages.length > oldLength || oldLength === 0)) {
        scrollToBottom()
      }

      // 当前会话有未读消息时及时标记；轮询到新消息时也要标记。
      if (newMessages.some(msg => msg.toUserId === currentUser.value?.userId && msg.status === 0) ||
          conversations.value.find(conv => conv.conversationId === convIdStr)?.unread > 0) {
        await markConversationRead(convIdStr)
        const active = conversations.value.find(conv => conv.conversationId === convIdStr)
        if (active) active.unread = 0
        await loadConversations()
        realtimeStore.refresh()
      }
    } else {
      messages.value = []
    }
  } catch (error) {
    if (!silent) {
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败，请稍后重试')
    }
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

// 切换会话
const switchConversation = (conversation) => {
  if (!conversation || !conversation.conversationId) {
    console.error('无效的会话对象:', conversation)
    return
  }

  // 确保 conversationId 是字符串
  const convId = String(conversation.conversationId)

  activeConversationId.value = convId
  pendingConversationId.value = ''
  activeOtherUser.value = conversation.otherUser
  messages.value = []
  loadMessages(convId)
}

// 发送消息
const sendMessage = async () => {
  if (!messageInput.value.trim()) return
  if (!activeConversationId.value) {
    ElMessage.warning('请选择一个会话')
    return
  }

  try {
    const otherUserId = activeOtherUser.value?.userId
    if (!otherUserId) {
      ElMessage.warning('无法获取对方用户信息')
      return
    }

    const formData = new URLSearchParams()
    formData.append('toUserId', otherUserId)
    formData.append('content', messageInput.value)
    formData.append('messageType', 1)

    const data = await sendMessageApi(formData)
    if (data) {
      messages.value.push({
        ...data,
        type: 'sent',
        time: formatMessageTime(data.createTime)
      })
      messageInput.value = ''
      scrollToBottom()
      // 刷新会话列表
      loadConversations()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请检查网络连接')
  }
}

// 搜索好友
const searchFriends = async () => {
  if (!friendSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    console.log('开始搜索用户，关键词:', friendSearchKeyword.value)
    const data = await searchUsers(friendSearchKeyword.value)
    console.log('搜索API响应:', data)

    // request 工具在成功时直接返回 data 字段（数组）
    if (data && Array.isArray(data)) {
      // 加载好友列表用于检查
      await loadFriendList()
      const friendIds = new Set(friendList.value.map(f => f.userId))

      // 为每个搜索结果添加isFriend字段
      friendSearchResults.value = data.map(user => ({
        ...user,
        isFriend: friendIds.has(user.userId)
      }))

      console.log('搜索结果数量:', friendSearchResults.value.length)
      console.log('搜索结果详情:', friendSearchResults.value)

      if (friendSearchResults.value.length === 0) {
        ElMessage.info('未找到相关用户')
      } else {
        ElMessage.success(`找到 ${friendSearchResults.value.length} 个用户`)
      }
    } else {
      friendSearchResults.value = []
      ElMessage.error('搜索结果格式错误')
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    friendSearchResults.value = []
    ElMessage.error('搜索失败，请检查网络连接')
  }
}

// 发送好友申请
const sendRequest = async (userId) => {
  try {
    const formData = new URLSearchParams()
    formData.append('toUserId', userId)
    if (friendRequestMessage.value) {
      formData.append('message', friendRequestMessage.value)
    }

    await sendFriendRequest(formData)
    ElMessage.success('好友申请已发送')
    friendRequestMessage.value = ''
  } catch (error) {
    console.error('发送好友申请失败:', error)
    ElMessage.error('发送好友申请失败，请检查网络连接')
  }
}

// 加载未读好友申请数量
const loadUnreadFriendRequestCount = async () => {
  try {
    const data = await getReceivedRequests(0) // 0 表示待处理
    if (data && Array.isArray(data)) {
      unreadFriendRequestCount.value = data.length
      return data.length
    }
    return 0
  } catch (error) {
    console.error('获取好友申请数量失败:', error)
    return 0
  }
}

// 加载好友申请列表
const loadFriendRequests = async () => {
  try {
    const data = await getReceivedRequests(0) // 0 表示待处理
    if (data && Array.isArray(data)) {
      friendRequests.value = await Promise.all(data.map(async req => {
        let user = null
        try {
          user = await getUserInfo(req.fromUserId)
        } catch (error) {
          console.error(`获取用户 ${req.fromUserId} 信息失败:`, error)
        }
        return {
          ...req,
          user
        }
      }))
      unreadFriendRequestCount.value = friendRequests.value.length
    } else {
      friendRequests.value = []
      unreadFriendRequestCount.value = 0
    }
  } catch (error) {
    console.error('加载好友申请失败:', error)
    ElMessage.error('加载好友申请失败')
  }
}

// 处理好友申请
const handleRequest = async (requestId, accept) => {
  try {
    await handleFriendRequest(requestId, accept)
    ElMessage.success(accept ? '已同意好友申请' : '已拒绝好友申请')
    await loadFriendRequests()
    if (accept) {
      loadFriendList()
    }
    realtimeStore.refresh()
  } catch (error) {
    console.error('处理好友申请失败:', error)
    ElMessage.error('操作失败，请检查网络连接')
  }
}

// 加载好友列表
const loadFriendList = async () => {
  try {
    const data = await getFriendList()
    friendList.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('加载好友列表失败:', error)
    friendList.value = []
    ElMessage.error('加载好友列表失败')
  }
}

// 开始与好友聊天
const startChat = async (friend) => {
  try {
    const conversationIdResponse = await generateConversationId(friend.userId)
    console.log('生成会话ID响应:', conversationIdResponse, '类型:', typeof conversationIdResponse)

    // 提取真正的conversationId字符串
    let conversationId
    if (typeof conversationIdResponse === 'string') {
      conversationId = conversationIdResponse
    } else if (conversationIdResponse && typeof conversationIdResponse === 'object') {
      // 如果返回的是对象，尝试提取conversationId字段
      conversationId = conversationIdResponse.conversationId || conversationIdResponse.data || conversationIdResponse
    }

    if (!conversationId) {
      throw new Error('无法获取会话ID')
    }

    // 确保conversationId是字符串
    const convIdStr = String(conversationId)
    console.log('最终会话ID:', convIdStr)

    // 先设置活动会话
    activeConversationId.value = convIdStr
    pendingConversationId.value = convIdStr
    activeOtherUser.value = friend

    // 加载消息
    await loadMessages(convIdStr)

    // 然后重新加载会话列表，确保会话显示在列表中
    await loadConversations()

    // 检查会话是否在列表中
    let conversation = conversations.value.find(c => c.conversationId === convIdStr)

    if (!conversation) {
      // 如果会话不在列表中，手动创建
      conversation = {
        conversationId: convIdStr,
        otherUserId: friend.userId,
        username: friend.nickname || friend.username,
        avatar: friend.avatar,
        lastMsg: '',
        time: '',
        unread: 0,
        otherUser: friend
      }
      conversations.value.unshift(conversation)
    }

    // 关闭对话框
    showFriendListDialog.value = false
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败，请检查网络连接')
  }
}

// 删除好友
const removeFriend = async (friendId) => {
  try {
    await ElMessageBox.confirm('确定要删除该好友吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteFriend(friendId)
    ElMessage.success('已删除好友')
    if (activeOtherUser.value?.userId === friendId) {
      activeConversationId.value = ''
      pendingConversationId.value = ''
      activeOtherUser.value = null
      messages.value = []
    }
    loadFriendList()
    loadConversations()
    realtimeStore.refresh()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除好友失败:', error)
      ElMessage.error('删除好友失败')
    }
  }
}

// 格式化时间
const formatMessageTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const formatConversationTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  // 今天
  if (diff < 86400000 && date.getDate() === now.getDate()) {
    return formatMessageTime(timeStr)
  }

  // 昨天
  if (diff < 172800000 && date.getDate() === now.getDate() - 1) {
    return '昨天'
  }

  // 本年
  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}-${date.getDate()}`
  }

  // 跨年
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      const wrap = scrollbarRef.value.wrapRef
      wrap.scrollTop = wrap.scrollHeight
    }
  })
}

onMounted(async () => {
  if (currentUser.value) {
    loadConversations()
    connectWebSocket()

    // 加载未读好友申请数量并提醒
    const count = await loadUnreadFriendRequestCount()
    if (count > 0) {
      ElMessage({
        message: `您有 ${count} 条新的好友申请`,
        type: 'info',
        duration: 3000,
        showClose: true
      })
    }
  }
  scrollToBottom()
})

// 组件卸载时清理资源
onUnmounted(() => {
  disposed = true
  if (reconnectTimer) clearTimeout(reconnectTimer)
  // 关闭 WebSocket 连接
  if (ws.value) {
    ws.value.close()
    ws.value = null
  }

  // 停止轮询
  stopPolling()

  console.log('📤 Message 组件已卸载，资源已清理')
})

// 打开添加好友对话框
const openAddFriendDialog = () => {
  showAddFriendDialog.value = true
  friendSearchResults.value = []
  friendSearchKeyword.value = ''
}

// 打开好友申请对话框
const openFriendRequestDialog = () => {
  showFriendRequestDialog.value = true
  loadFriendRequests()
  // 打开对话框后，未读数量会在loadFriendRequests中更新
}

// 打开好友列表对话框
const openFriendListDialog = () => {
  showFriendListDialog.value = true
  loadFriendList()
}
</script>

<template>
  <div class="message-page">
    <Header />

    <div class="chat-container">
      <!-- 左侧会话列表 (深色模式) -->
      <div class="sidebar-panel">
        <!-- 顶部用户信息 -->
        <div class="sidebar-header">
          <div class="user-info">
            <el-avatar :size="36" :src="currentUserAvatar" />
            <span class="username">{{ currentUser?.nickname || currentUser?.username }}</span>
          </div>
          <div class="header-actions">
            <el-icon class="action-icon" @click="openAddFriendDialog" title="添加好友">
              <Plus />
            </el-icon>
            <el-badge :value="unreadFriendRequestCount" :hidden="unreadFriendRequestCount === 0" :max="99"
              class="badge-item">
              <el-icon class="action-icon" @click="openFriendRequestDialog" title="好友申请">
                <Bell />
              </el-icon>
            </el-badge>
            <el-icon class="action-icon" @click="openFriendListDialog" title="好友列表">
              <User />
            </el-icon>
          </div>
        </div>

        <!-- 搜索框 -->
        <div class="search-wrapper">
          <div class="search-box">
            <el-icon>
              <Search />
            </el-icon>
            <input v-model="searchKeyword" type="text" placeholder="查找联系人" />
          </div>
        </div>

        <!-- 会话列表 -->
        <div class="session-list">
          <div v-if="conversations.length === 0" class="empty-state">
            <p>暂无会话</p>
            <el-button type="primary" size="small" @click="openFriendListDialog">选择好友聊天</el-button>
          </div>
          <div v-for="conversation in conversations" :key="conversation.conversationId" class="session-item"
            :class="{ active: activeConversationId === conversation.conversationId }"
            @click="switchConversation(conversation)">
            <div class="avatar-wrapper">
              <el-avatar :size="40" :src="conversation.avatar" />
            </div>

            <div class="session-content">
              <div class="session-top">
                <span class="session-name">{{ conversation.username }}</span>
                <span class="session-time">{{ conversation.time }}</span>
              </div>
              <p class="session-preview">{{ conversation.lastMsg }}</p>
            </div>

            <div v-if="conversation.unread > 0" class="unread-badge">
              {{ conversation.unread > 99 ? '99+' : conversation.unread }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧聊天窗口 (浅色模式) -->
      <div class="chat-panel">
        <template v-if="activeConversationId">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <span class="chat-title">{{ activeOtherUser?.nickname || activeOtherUser?.username }}</span>
            <div class="chat-actions">
              <el-icon class="action-icon" @click="openFriendListDialog">
                <User />
              </el-icon>
            </div>
          </div>

          <!-- 消息内容区 -->
          <div class="messages-area" v-loading="loading">
            <el-scrollbar ref="scrollbarRef">
              <div class="messages-content">
                <div v-for="msg in messages" :key="msg.messageId" class="message-row" :class="msg.type">
                  <!-- 接收方消息：头像在左边 -->
                  <el-avatar v-if="msg.type === 'received'" :size="36" :src="activeOtherUser?.avatar"
                    class="msg-avatar" />

                  <div class="bubble-wrapper">
                    <div class="message-bubble">
                      {{ msg.content }}
                    </div>
                    <span class="msg-time">{{ msg.time }}</span>
                  </div>

                  <!-- 发送方消息：头像在右边 -->
                  <el-avatar v-if="msg.type === 'sent'" :size="36" :src="currentUserAvatar" class="msg-avatar" />
                </div>
              </div>
            </el-scrollbar>
          </div>

          <!-- 底部输入区 -->
          <div class="input-area">
            <div class="toolbar">
              <el-icon class="tool-icon"><span class="emoji-icon">☺</span></el-icon>
              <el-icon class="tool-icon">
                <Picture />
              </el-icon>
              <el-icon class="tool-icon">
                <Paperclip />
              </el-icon>
            </div>

            <textarea v-model="messageInput" class="message-textarea" placeholder="输入消息..."
              @keydown.enter.exact.prevent="sendMessage"></textarea>

            <div class="input-footer">
              <span class="tip">按下Enter发送内容 / Ctrl+Enter换行</span>
            </div>
          </div>
        </template>
        <div v-else class="empty-chat">
          <p>请选择一个会话开始聊天</p>
        </div>
      </div>
    </div>

    <!-- 添加好友对话框 -->
    <el-dialog v-model="showAddFriendDialog" title="添加好友" width="500px">
      <div class="friend-search-box">
        <el-input v-model="friendSearchKeyword" placeholder="输入用户名或昵称搜索" @keyup.enter="searchFriends">
          <template #append>
            <el-button @click="searchFriends" :icon="Search">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="search-results">
        <div v-if="friendSearchResults.length === 0" class="empty-result">
          <p>暂无搜索结果</p>
        </div>
        <div v-for="user in friendSearchResults" :key="user.userId" class="user-result-item">
          <el-avatar :size="50" :src="user.avatar" />
          <div class="user-info-box">
            <div class="user-name">{{ user.nickname || user.username }}</div>
            <div class="user-bio">{{ user.bio || '这个人很懒，什么都没写' }}</div>
          </div>
          <el-button v-if="user.userId === currentUser?.userId" type="info" size="small" disabled>
            当前用户
          </el-button>
          <el-button v-else-if="user.isFriend" type="success" size="small" disabled>
            对方已是好友
          </el-button>
          <el-button v-else type="primary" size="small" @click="sendRequest(user.userId)">
            添加好友
          </el-button>
        </div>
      </div>

      <el-input v-if="friendSearchResults.length > 0" v-model="friendRequestMessage" type="textarea" :rows="2"
        placeholder="输入验证消息（可选）" style="margin-top: 10px;" />
    </el-dialog>

    <!-- 好友申请对话框 -->
    <el-dialog v-model="showFriendRequestDialog" title="好友申请" width="500px">
      <div class="friend-requests">
        <div v-if="friendRequests.length === 0" class="empty-result">
          <p>暂无好友申请</p>
        </div>
        <div v-for="request in friendRequests" :key="request.requestId" class="request-item">
          <el-avatar :size="50" :src="request.user?.avatar" />
          <div class="request-info">
            <div class="request-name">{{ request.user?.nickname || request.user?.username }}</div>
            <div class="request-message">{{ request.message || '请求添加你为好友' }}</div>
            <div class="request-time">{{ request.createTime }}</div>
          </div>
          <div class="request-actions">
            <el-button type="primary" size="small" @click="handleRequest(request.requestId, true)">
              同意
            </el-button>
            <el-button size="small" @click="handleRequest(request.requestId, false)">
              拒绝
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 好友列表对话框 -->
    <el-dialog v-model="showFriendListDialog" title="好友列表" width="500px">
      <div class="friend-list">
        <div v-if="friendList.length === 0" class="empty-result">
          <p>暂无好友</p>
          <el-button type="primary" size="small" @click="showFriendListDialog = false; openAddFriendDialog()">
            添加好友
          </el-button>
        </div>
        <div v-for="friend in friendList" :key="friend.userId" class="friend-item">
          <el-avatar :size="50" :src="friend.avatar" />
          <div class="friend-info">
            <div class="friend-name">{{ friend.nickname || friend.username }}</div>
            <div class="friend-bio">{{ friend.bio || '这个人很懒，什么都没写' }}</div>
          </div>
          <div class="friend-actions">
            <el-button type="primary" size="small" @click="startChat(friend)">
              发消息
            </el-button>
            <el-button type="danger" size="small" @click="removeFriend(friend.userId)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.message-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
}

.chat-container {
  flex: 1;
  display: flex;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
  height: calc(100vh - 60px);
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  box-shadow: 0 0 40px rgba(0, 0, 0, 0.5);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

/* 左侧侧边栏 */
.sidebar-panel {
  width: 280px;
  background: rgba(17, 24, 39, 0.6);
  display: flex;
  flex-direction: column;
  color: var(--text-main);
  border-right: 1px solid var(--glass-border);
}

.sidebar-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info .username {
  font-size: 16px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.badge-item {
  line-height: 1;
}

.badge-item .el-badge__content {
  background-color: #f56c6c;
  border-radius: 10px;
  color: #fff;
  font-size: 12px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
  text-align: center;
  white-space: nowrap;
  border: 1px solid rgba(17, 24, 39, 0.6);
}

.action-icon {
  font-size: 20px;
  cursor: pointer;
  color: #999;
  transition: color 0.2s;
}

.action-icon:hover {
  color: var(--primary-color);
}

.search-wrapper {
  padding: 0 16px 16px;
}

.search-box {
  background: rgba(31, 41, 55, 0.5);
  border-radius: 8px;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(139, 92, 246, 0.2);
}

.search-box .el-icon {
  color: var(--text-secondary);
}

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-main);
  font-size: 13px;
  width: 100%;
}

.search-box input::placeholder {
  color: var(--text-secondary);
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-secondary);
}

.empty-state p {
  margin-bottom: 15px;
}

.session-item {
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
}

.session-item:hover {
  background: rgba(139, 92, 246, 0.1);
}

.session-item.active {
  background: rgba(139, 92, 246, 0.2);
  border-left: 3px solid var(--primary-color);
}

.avatar-wrapper {
  position: relative;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.session-name {
  font-size: 14px;
  color: var(--text-main);
  font-weight: 500;
}

.session-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.session-preview {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.unread-badge {
  position: absolute;
  right: 16px;
  bottom: 12px;
  background: #fa5151;
  color: #fff;
  font-size: 10px;
  padding: 0 6px;
  height: 16px;
  line-height: 16px;
  border-radius: 8px;
}

/* 右侧聊天面板 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(17, 24, 39, 0.3);
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  font-size: 16px;
}

.chat-header {
  height: 60px;
  border-bottom: 1px solid var(--glass-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
}

.messages-area {
  flex: 1;
  overflow: hidden;
  background: transparent;
}

.messages-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息行基础样式 */
.message-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  width: 100%;
}

/* 接收方消息（对方发的）：头像在左，消息在右，整体靠左 */
.message-row.received {
  justify-content: flex-start;
  flex-direction: row;
}

/* 发送方消息（自己发的）：头像在右，消息在左，整体靠右 */
.message-row.sent {
  justify-content: flex-end;
  flex-direction: row;
}

.msg-avatar {
  flex-shrink: 0;
  align-self: flex-start;
}

.bubble-wrapper {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 接收方的气泡和时间靠左对齐 */
.message-row.received .bubble-wrapper {
  align-items: flex-start;
  order: 2;
}

.message-row.received .msg-avatar {
  order: 1;
}

/* 发送方的气泡和时间靠右对齐 */
.message-row.sent .bubble-wrapper {
  align-items: flex-end;
  order: 1;
}

.message-row.sent .msg-avatar {
  order: 2;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  max-width: 100%;
}

/* 接收方消息气泡样式（白色/浅色背景） */
.message-row.received .message-bubble {
  background: #ffffff;
  color: #333333;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

/* 发送方消息气泡样式（绿色/紫色背景） */
.message-row.sent .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.msg-time {
  font-size: 11px;
  color: var(--text-secondary);
  padding: 0 4px;
}

/* 输入区域 */
.input-area {
  height: 160px;
  border-top: 1px solid var(--glass-border);
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  display: flex;
  flex-direction: column;
}

.toolbar {
  padding: 10px 20px;
  display: flex;
  gap: 20px;
}

.tool-icon {
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
}

.tool-icon:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.emoji-icon {
  font-style: normal;
}

.message-textarea {
  flex: 1;
  border: none;
  resize: none;
  padding: 0 20px;
  font-size: 14px;
  outline: none;
  font-family: inherit;
  background: transparent;
  color: var(--text-main);
}

.message-textarea::placeholder {
  color: var(--text-secondary);
}

.input-footer {
  padding: 10px 20px;
  display: flex;
  justify-content: flex-end;
}

.tip {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 对话框样式 */
.friend-search-box {
  margin-bottom: 20px;
}

.search-results,
.friend-requests,
.friend-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-result {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-secondary);
}

.empty-result p {
  margin-bottom: 15px;
}

.user-result-item,
.request-item,
.friend-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-bottom: 1px solid var(--glass-border);
  transition: background 0.2s;
}

.user-result-item:hover,
.request-item:hover,
.friend-item:hover {
  background: rgba(139, 92, 246, 0.05);
}

.user-info-box,
.request-info,
.friend-info {
  flex: 1;
  min-width: 0;
}

.user-name,
.request-name,
.friend-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-main);
  margin-bottom: 5px;
}

.user-bio,
.request-message,
.friend-bio {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 5px;
}

.request-actions,
.friend-actions {
  display: flex;
  gap: 8px;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-thumb {
  background: rgba(139, 92, 246, 0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

/* Element Plus 对话框深色主题适配 */
:deep(.el-dialog) {
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid var(--glass-border);
}

:deep(.el-dialog__title) {
  color: var(--text-main);
}

:deep(.el-dialog__body) {
  color: var(--text-main);
}

:deep(.el-input__inner) {
  line-height: 1.5;
  border: 1px solid rgba(139, 92, 246, 0.2);
}

.system-content a {
  color: var(--primary-color);
  text-decoration: none;
}

.bubble-wrapper {
  max-width: 60%;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  position: relative;
  word-wrap: break-word;
}

.received .message-bubble {
  background: var(--glass-card-bg);
  color: var(--text-main);
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(var(--glass-blur));
}

.sent .message-bubble {
  background: var(--primary-gradient);
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

/* 输入区域 */
.input-area {
  height: 160px;
  border-top: 1px solid var(--glass-border);
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  display: flex;
  flex-direction: column;
}

.toolbar {
  padding: 10px 20px;
  display: flex;
  gap: 20px;
}

.tool-icon {
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
}

.tool-icon:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.emoji-icon {
  font-style: normal;
}

.message-textarea {
  flex: 1;
  border: none;
  resize: none;
  padding: 0 20px;
  font-size: 14px;
  outline: none;
  font-family: inherit;
  background: transparent;
  color: var(--text-main);
}

.message-textarea::placeholder {
  color: var(--text-secondary);
}

.input-footer {
  padding: 10px 20px;
  display: flex;
  justify-content: flex-end;
}

.tip {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

::-webkit-scrollbar-track {
  background: transparent;
}
</style>
