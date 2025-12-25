<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '../components/Layout.vue'
import { ElTabs, ElTabPane, ElAvatar, ElButton, ElEmpty, ElMessage, ElBadge } from 'element-plus'
import { ChatDotRound, Star, User, Connection } from '@element-plus/icons-vue'
import { getNotificationList, getNotificationsByType, markAsRead, markAllAsRead, markTypeAsRead, getUnreadCountByType } from '../api/notification'
import { followUser } from '../api/follow'

const router = useRouter()
const activeTab = ref('all')
const notifications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const unreadCounts = ref({
  LIKE: 0,
  COMMENT: 0,
  MENTION: 0,
  FOLLOW: 0
})

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    let result
    // 根据当前标签页加载不同类型的通知
    if (activeTab.value === 'all') {
      result = await getNotificationList(currentPage.value, pageSize.value)
    } else {
      // 将前端的小写类型转换为后端的大写类型
      const backendType = activeTab.value.toUpperCase()
      result = await getNotificationsByType(backendType, currentPage.value, pageSize.value)
    }

    // 兼容多种响应格式
    notifications.value = result.list || result.records || result.data || (Array.isArray(result) ? result : [])
    total.value = result.total || 0
  } catch (error) {
    console.error('加载通知列表失败:', error)
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数统计
const loadUnreadCounts = async () => {
  try {
    const result = await getUnreadCountByType()
    if (result) {
      // 更新各类型未读数
      unreadCounts.value = {
        LIKE: result.LIKE || 0,
        COMMENT: result.COMMENT || 0,
        MENTION: result.MENTION || 0,
        FOLLOW: result.FOLLOW || 0
      }
    }
  } catch (error) {
    console.error('加载未读数失败:', error)
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 2 * day) {
    return '昨天'
  } else {
    return date.toLocaleDateString()
  }
}

// 根据类型筛选通知（现在由 loadNotifications 直接按类型加载，此处直接返回）
const filteredNotifications = computed(() => {
  return notifications.value
})

// 监听标签页切换，重新加载对应类型的通知
watch(activeTab, () => {
  currentPage.value = 1
  loadNotifications()
})

// 标记通知为已读
const handleMarkAsRead = async (notificationId) => {
  try {
    await markAsRead(notificationId)
    // 更新本地状态
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.isRead = true
    }
    // 刷新未读数统计
    loadUnreadCounts()
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 标记所有为已读
const handleMarkAllAsRead = async () => {
  try {
    // 如果在特定类型标签页，标记该类型全部已读；否则标记所有通知已读
    if (activeTab.value !== 'all') {
      const backendType = activeTab.value.toUpperCase()
      await markTypeAsRead(backendType)
      ElMessage.success(`已将所有${getTypeLabel(activeTab.value)}标记为已读`)
    } else {
      await markAllAsRead()
      ElMessage.success('已全部标记为已读')
    }

    // 更新本地状态
    notifications.value.forEach(n => n.isRead = true)
    // 刷新未读数统计
    loadUnreadCounts()
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('操作失败')
  }
}

// 获取类型标签文本
const getTypeLabel = (type) => {
  const labels = {
    like: '赞',
    comment: '评论',
    mention: '@我的',
    follow: '关注'
  }
  return labels[type] || '通知'
}

// 回粉操作
const handleFollowBack = async (userId) => {
  try {
    await followUser(userId)
    ElMessage.success('已关注')
    // 可选：重新加载通知列表
    loadNotifications()
  } catch (error) {
    console.error('关注失败:', error)
    ElMessage.error('关注失败')
  }
}

// 点击通知项
const handleNotificationClick = async (notification) => {
  // 标记为已读
  if (!notification.isRead) {
    await handleMarkAsRead(notification.id)
  }

  // 根据通知类型跳转
  const type = notification.type?.toUpperCase()

  // 点赞、评论、@我的类型：跳转到帖子详情
  if ((type === 'LIKE' || type === 'COMMENT' || type === 'MENTION') && notification.postId) {
    router.push(`/post/${notification.postId}`)
  }
  // 关注类型：跳转到发送者主页
  else if (type === 'FOLLOW' && notification.senderId) {
    router.push(`/profile/${notification.senderId}`)
  }
  // 如果有帖子ID优先跳转帖子
  else if (notification.postId) {
    router.push(`/post/${notification.postId}`)
  }
  // 否则跳转到发送者主页
  else if (notification.senderId) {
    router.push(`/profile/${notification.senderId}`)
  }
}

onMounted(() => {
  loadNotifications()
  loadUnreadCounts()
})

const getIcon = (type) => {
  // 兼容大写和小写类型
  const typeUpper = type?.toUpperCase()
  switch (typeUpper) {
    case 'LIKE': return Star
    case 'COMMENT': return ChatDotRound
    case 'FOLLOW': return User
    case 'MENTION': return Connection
    default: return Star
  }
}

const getIconColor = (type) => {
  const typeUpper = type?.toUpperCase()
  switch (typeUpper) {
    case 'LIKE': return '#ff4757'
    case 'COMMENT': return '#2ed573'
    case 'FOLLOW': return '#1e90ff'
    case 'MENTION': return '#ffa502'
    default: return '#ccc'
  }
}
</script>

<template>
  <Layout>
    <div class="notification-container glass-card">
      <div class="page-header">
        <div>
          <h2>通知中心</h2>
        </div>
        <el-button v-if="notifications.length > 0" size="small" @click="handleMarkAllAsRead">全部已读</el-button>
      </div>

      <el-tabs v-model="activeTab" class="notification-tabs">
        <el-tab-pane name="all">
          <template #label>
            <el-badge
              :value="(unreadCounts.LIKE + unreadCounts.COMMENT + unreadCounts.MENTION + unreadCounts.FOLLOW) || null"
              :max="99"
              :hidden="(unreadCounts.LIKE + unreadCounts.COMMENT + unreadCounts.MENTION + unreadCounts.FOLLOW) === 0">
              全部
            </el-badge>
          </template>
          <div v-loading="loading" class="notification-list">
            <el-empty v-if="filteredNotifications.length === 0 && !loading" description="暂无通知" :image-size="120">
              <template #description>
                <span style="color: #909399;">暂时还没有新的通知～</span>
              </template>
            </el-empty>

            <div v-else>
              <div v-for="item in filteredNotifications" :key="item.id" class="notification-item"
                :class="{ 'unread': !item.isRead }" @click="handleNotificationClick(item)">
                <div class="icon-wrapper"
                  :style="{ background: getIconColor(item.type) + '20', color: getIconColor(item.type) }">
                  <el-icon :size="20">
                    <component :is="getIcon(item.type)" />
                  </el-icon>
                </div>
                <el-avatar :src="item.senderAvatar || '/default-avatar.png'" :size="48" class="user-avatar" />
                <div class="content-wrapper">
                  <div class="top-row">
                    <span class="username">{{ item.senderUsername || '用户' }}</span>
                    <span class="time">{{ formatTime(item.createTime) }}</span>
                  </div>
                  <p class="content-text">{{ item.content }}</p>
                </div>
                <el-button v-if="item.type === 'FOLLOW' && item.senderId" size="small" round
                  @click.stop="handleFollowBack(item.senderId)">
                  回粉
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="mention">
          <template #label>
            <el-badge :value="unreadCounts.MENTION || null" :max="99" :hidden="!unreadCounts.MENTION">
              @我的
            </el-badge>
          </template>
          <div v-loading="loading" class="notification-list">
            <el-empty v-if="filteredNotifications.length === 0 && !loading" description="暂无@消息" />
            <div v-else>
              <div v-for="item in filteredNotifications" :key="item.id" class="notification-item"
                :class="{ 'unread': !item.isRead }" @click="handleNotificationClick(item)">
                <div class="icon-wrapper"
                  :style="{ background: getIconColor(item.type) + '20', color: getIconColor(item.type) }">
                  <el-icon :size="20">
                    <component :is="getIcon(item.type)" />
                  </el-icon>
                </div>
                <el-avatar :src="item.senderAvatar || '/default-avatar.png'" :size="48" class="user-avatar" />
                <div class="content-wrapper">
                  <div class="top-row">
                    <span class="username">{{ item.senderUsername || '用户' }}</span>
                    <span class="time">{{ formatTime(item.createTime) }}</span>
                  </div>
                  <p class="content-text">{{ item.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="comment">
          <template #label>
            <el-badge :value="unreadCounts.COMMENT || null" :max="99" :hidden="!unreadCounts.COMMENT">
              评论
            </el-badge>
          </template>
          <div v-loading="loading" class="notification-list">
            <el-empty v-if="filteredNotifications.length === 0 && !loading" description="暂无新评论" />
            <div v-else>
              <div v-for="item in filteredNotifications" :key="item.id" class="notification-item"
                :class="{ 'unread': !item.isRead }" @click="handleNotificationClick(item)">
                <div class="icon-wrapper"
                  :style="{ background: getIconColor(item.type) + '20', color: getIconColor(item.type) }">
                  <el-icon :size="20">
                    <component :is="getIcon(item.type)" />
                  </el-icon>
                </div>
                <el-avatar :src="item.senderAvatar || '/default-avatar.png'" :size="48" class="user-avatar" />
                <div class="content-wrapper">
                  <div class="top-row">
                    <span class="username">{{ item.senderUsername || '用户' }}</span>
                    <span class="time">{{ formatTime(item.createTime) }}</span>
                  </div>
                  <p class="content-text">{{ item.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="like">
          <template #label>
            <el-badge :value="unreadCounts.LIKE || null" :max="99" :hidden="!unreadCounts.LIKE">
              赞
            </el-badge>
          </template>
          <div v-loading="loading" class="notification-list">
            <el-empty v-if="filteredNotifications.length === 0 && !loading" description="暂无新赞" />
            <div v-else>
              <div v-for="item in filteredNotifications" :key="item.id" class="notification-item"
                :class="{ 'unread': !item.isRead }" @click="handleNotificationClick(item)">
                <div class="icon-wrapper"
                  :style="{ background: getIconColor(item.type) + '20', color: getIconColor(item.type) }">
                  <el-icon :size="20">
                    <component :is="getIcon(item.type)" />
                  </el-icon>
                </div>
                <el-avatar :src="item.senderAvatar || '/default-avatar.png'" :size="48" class="user-avatar" />
                <div class="content-wrapper">
                  <div class="top-row">
                    <span class="username">{{ item.senderUsername || '用户' }}</span>
                    <span class="time">{{ formatTime(item.createTime) }}</span>
                  </div>
                  <p class="content-text">{{ item.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="follow">
          <template #label>
            <el-badge :value="unreadCounts.FOLLOW || null" :max="99" :hidden="!unreadCounts.FOLLOW">
              关注
            </el-badge>
          </template>
          <div v-loading="loading" class="notification-list">
            <el-empty v-if="filteredNotifications.length === 0 && !loading" description="暂无新关注" />
            <div v-else>
              <div v-for="item in filteredNotifications" :key="item.id" class="notification-item"
                :class="{ 'unread': !item.isRead }" @click="handleNotificationClick(item)">
                <div class="icon-wrapper"
                  :style="{ background: getIconColor(item.type) + '20', color: getIconColor(item.type) }">
                  <el-icon :size="20">
                    <component :is="getIcon(item.type)" />
                  </el-icon>
                </div>
                <el-avatar :src="item.senderAvatar || '/default-avatar.png'" :size="48" class="user-avatar" />
                <div class="content-wrapper">
                  <div class="top-row">
                    <span class="username">{{ item.senderUsername || '用户' }}</span>
                    <span class="time">{{ formatTime(item.createTime) }}</span>
                  </div>
                  <p class="content-text">{{ item.content }}</p>
                </div>
                <el-button v-if="item.senderId" size="small" round @click.stop="handleFollowBack(item.senderId)">
                  回粉
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </Layout>
</template>

<style scoped>
.notification-container {
  max-width: 800px;
  margin: 0 auto;
  min-height: calc(100vh - 100px);
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  color: var(--text-main);
}

.dev-tip {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #ffa502;
  background: rgba(255, 165, 2, 0.1);
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #ffa502;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-bottom: 1px solid var(--glass-border);
  transition: all 0.3s ease;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
}

.notification-item:hover {
  background: rgba(139, 92, 246, 0.15);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notification-item:active {
  transform: translateX(2px);
}

.notification-item.unread {
  background: rgba(139, 92, 246, 0.08);
  border-left: 3px solid var(--primary-color);
}

.notification-item.unread:hover {
  background: rgba(139, 92, 246, 0.18);
}

.icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content-wrapper {
  flex: 1;
}

.top-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.username {
  font-weight: 600;
  color: var(--text-main);
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
}

.content-text {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

.notification-item:hover .content-text {
  color: var(--text-main);
}

.notification-item:hover .username {
  color: var(--primary-color);
}

/* 覆盖 Element Plus Tabs 样式以适应玻璃风格 */
:deep(.el-tabs__nav-wrap::after) {
  background-color: transparent;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  color: var(--text-secondary);
}

:deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  font-weight: 600;
}
</style>
