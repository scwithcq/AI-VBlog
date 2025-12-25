<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '../store/appStore'
import { useUserStore } from '../store/userStore'
import {
  HomeFilled, Bell, Star, Message,
  VideoCamera, Setting, User
} from '@element-plus/icons-vue'
import { getUnreadCount } from '../api/notification'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const unreadNotificationCount = ref(0)

// 加载未读通知数
const loadUnreadCount = async () => {
  try {
    const result = await getUnreadCount()
    unreadNotificationCount.value = result.count || result.unreadCount || result || 0
  } catch (error) {
    console.error('加载未读通知数失败:', error)
    unreadNotificationCount.value = 0
  }
}

// 侧边栏导航项
const menuItems = [
  // 主要导航
  { key: 'home', label: '首页', icon: HomeFilled, path: '/' },
  { key: 'message', label: '消息', icon: Message, path: '/message' },
  { key: 'notification', label: '通知', icon: Bell, path: '/notification' },
  // { key: 'favorite', label: '收藏', icon: Star, path: '/favorite' },

  // 发现与热门
  { key: 'video', label: '视频', icon: VideoCamera, path: '/video' },

  // 个人相关
  { key: 'profile', label: '我的主页', icon: User, path: `/profile/${userStore.userInfo?.userId || ''}` },

  // 设置
  // { key: 'setting', label: '设置', icon: Setting, path: '/setting' }
]

const handleMenuClick = (item) => {
  appStore.setActiveMenu(item.key)
  router.push(item.path)
}

onMounted(() => {
  loadUnreadCount()
  // 每5分钟刷新一次未读数
  setInterval(loadUnreadCount, 5 * 60 * 1000)
})
</script>

<template>
  <div class="sidebar glass-card">
    <!-- 个人信息卡片 -->
    <div class="user-profile-card">
      <div class="user-info" @click="router.push(`/profile/${userStore.userInfo?.userId || ''}`)">
        <el-avatar :size="64" :src="userStore.userInfo?.avatar" class="sidebar-avatar">
          <UserFilled />
        </el-avatar>
        <div class="user-details">
          <div class="user-name">{{ userStore.userInfo?.username || '未登录' }}</div>
          <div class="user-stats">
            <span class="stat-item">
              <span class="stat-number">0</span>
              <span class="stat-label">关注</span>
            </span>
            <span class="stat-divider"></span>
            <span class="stat-item">
              <span class="stat-number">0</span>
              <span class="stat-label">粉丝</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 侧边栏菜单 -->
    <div class="menu-list">
      <div v-for="item in menuItems" :key="item.key" class="menu-item"
        :class="{ active: appStore.activeMenu === item.key }" @click="handleMenuClick(item)">
        <el-icon class="menu-icon" :size="20">
          <component :is="item.icon" />
        </el-icon>
        <span class="menu-label">{{ item.label }}</span>
        <span v-if="item.key === 'notification' && unreadNotificationCount > 0" class="menu-badge">
          {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
        </span>
      </div>
    </div>

    <!-- 侧边栏底部 -->
    <div class="sidebar-footer">
      <div class="footer-item">
        <el-icon class="footer-icon">
          <Setting />
        </el-icon>
        <span>设置</span>
      </div>
      <div class="footer-item">
        <el-icon class="footer-icon">
          <QuestionFilled />
        </el-icon>
        <span>帮助</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 侧边栏容器 */
.sidebar {
  padding: 24px 16px;
  height: fit-content;
  min-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.user-profile-card {
  margin-bottom: 24px;
  padding: 0 8px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
}

.sidebar-avatar {
  border: 3px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 12px;
  transition: transform 0.3s;
}

.user-info:hover .sidebar-avatar {
  transform: scale(1.05);
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 8px;
}

.user-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-item {
  display: flex;
  gap: 4px;
}

.stat-number {
  font-weight: 600;
  color: var(--text-main);
}

.menu-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-regular);
  font-weight: 500;
}

.menu-item:hover {
  background: rgba(0, 0, 0, 0.03);
  color: var(--primary-color);
}

.menu-item.active {
  background: var(--primary-gradient);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 154, 158, 0.3);
}

.menu-icon {
  margin-right: 12px;
}

.menu-label {
  font-size: 15px;
}

.menu-badge {
  margin-left: auto;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-around;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
}

.footer-item:hover {
  color: var(--text-main);
}
</style>
display: flex;
flex-direction: column;
justify-content: space-between;
}

/* 用户信息卡片 */
.user-profile-card {
padding: 20px 0;
margin-bottom: 20px;
}

.user-info {
display: flex;
flex-direction: column;
align-items: center;
cursor: pointer;
transition: var(--transition-base);
padding: 12px;
border-radius: var(--border-radius-lg);
}

.user-info:hover {
background-color: rgba(139, 92, 246, 0.15);
}

.user-details {
text-align: center;
margin-top: 12px;
}

.user-name {
font-size: 18px;
font-weight: 600;
color: inherit;
margin-bottom: 8px;
}

.user-stats {
display: flex;
align-items: center;
justify-content: center;
gap: 12px;
font-size: 14px;
color: var(--info-color);
}

.stat-item {
display: flex;
align-items: center;
gap: 4px;
}

.stat-number {
font-weight: 600;
color: inherit;
}

.stat-divider {
color: var(--info-color);
opacity: 0.5;
}

/* 菜单列表 */
.menu-list {
flex: 1;
display: flex;
flex-direction: column;
gap: 4px;
}

/* 菜单项样式 */
.menu-item {
display: flex;
align-items: center;
padding: 12px 16px;
cursor: pointer;
transition: var(--transition-base);
border-radius: var(--border-radius-md);
color: inherit;
font-size: 15px;
font-weight: 500;
position: relative;
}

.menu-item:hover {
background-color: rgba(139, 92, 246, 0.15);
}

.menu-item.active {
background-color: rgba(64, 158, 255, 0.2);
color: var(--primary-color);
}

/* 菜单图标 */
.menu-icon {
font-size: 20px;
margin-right: 12px;
flex-shrink: 0;
}

/* 菜单标签 */
.menu-label {
flex: 1;
text-align: left;
}

/* 菜单徽章 */
.menu-badge {
background-color: var(--danger-color);
color: white;
font-size: 12px;
font-weight: 600;
padding: 2px 8px;
border-radius: var(--border-radius-full);
min-width: 20px;
text-align: center;
}

/* 侧边栏底部 */
.sidebar-footer {
margin-top: 20px;
padding-top: 20px;
border-top: 1px solid var(--glass-border);
}

.footer-item {
display: flex;
align-items: center;
gap: 8px;
padding: 8px 16px;
cursor: pointer;
transition: var(--transition-base);
border-radius: var(--border-radius-md);
font-size: 14px;
color: var(--info-color);
}

.footer-item:hover {
color: inherit;
background-color: rgba(139, 92, 246, 0.15);
}

.footer-icon {
font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 992px) {
.sidebar {
padding: 16px 12px;
}

.menu-label {
font-size: 14px;
}
}

@media (max-width: 768px) {
.sidebar {
display: none;
}
}