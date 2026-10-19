import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUnreadCount as getMessageUnreadCount } from '../api/message'
import { getUnreadCount as getNotificationUnreadCount } from '../api/notification'
import { getReceivedRequests } from '../api/friendship'

export const useRealtimeStore = defineStore('realtime', () => {
  const unreadMessages = ref(0)
  const unreadNotifications = ref(0)
  const pendingFriendRequests = ref(0)
  let socket = null
  let reconnectTimer = null
  let pollingTimer = null
  let currentUserId = null
  let currentToken = ''
  let retryCount = 0
  let refreshVersion = 0

  async function refresh() {
    if (!currentUserId) return
    const userId = currentUserId
    const version = ++refreshVersion
    const [messages, notifications, requests] = await Promise.allSettled([
      getMessageUnreadCount(), getNotificationUnreadCount(), getReceivedRequests(0)
    ])
    if (currentUserId !== userId || version !== refreshVersion) return
    if (messages.status === 'fulfilled') unreadMessages.value = Number(messages.value?.count) || 0
    if (notifications.status === 'fulfilled') unreadNotifications.value = Number(notifications.value?.count ?? notifications.value) || 0
    if (requests.status === 'fulfilled') pendingFriendRequests.value = Array.isArray(requests.value) ? requests.value.length : 0
  }

  function setPolling(intervalMs) {
    if (pollingTimer) clearInterval(pollingTimer)
    pollingTimer = setInterval(refresh, intervalMs)
  }

  function connect() {
    if (!currentUserId || !currentToken) return
    const userId = currentUserId
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const url = `${protocol}//${window.location.host}/api/ws/message?token=${encodeURIComponent(currentToken)}`
    const connection = new WebSocket(url)
    socket = connection
    connection.onopen = () => {
      if (currentUserId !== userId || socket !== connection) return
      retryCount = 0
      setPolling(60000)
      refresh()
    }
    connection.onmessage = (event) => {
      if (currentUserId !== userId || socket !== connection) return
      try {
        const message = JSON.parse(event.data)
        if (message.type === 'new_message') {
          ElMessage.info('收到新私信')
        } else if (message.type === 'friend_request') {
          ElMessage.info('收到新的好友申请')
        } else if (message.type === 'system_notification') {
          ElMessage.info(message.data?.content || '收到新通知')
        }
        if (['new_message', 'message_recall', 'friend_request', 'friend_accepted', 'system_notification'].includes(message.type)) {
          refresh()
        }
      } catch (error) {
        console.error('实时通知解析失败:', error)
      }
    }
    connection.onclose = () => {
      if (currentUserId !== userId || socket !== connection) return
      setPolling(15000)
      retryCount += 1
      reconnectTimer = setTimeout(connect, Math.min(1000 * 2 ** retryCount, 30000))
    }
    connection.onerror = () => connection.close()
  }

  function stop() {
    refreshVersion++
    currentUserId = null
    currentToken = ''
    if (reconnectTimer) clearTimeout(reconnectTimer)
    if (pollingTimer) clearInterval(pollingTimer)
    reconnectTimer = null
    pollingTimer = null
    if (socket) socket.close()
    socket = null
    unreadMessages.value = 0
    unreadNotifications.value = 0
    pendingFriendRequests.value = 0
  }

  function start(userId, token) {
    if (currentUserId === userId && currentToken === token) return
    stop()
    if (!userId || !token) return
    currentUserId = userId
    currentToken = token
    setPolling(15000)
    refresh()
    connect()
  }

  return { unreadMessages, unreadNotifications, pendingFriendRequests, refresh, start, stop }
})
