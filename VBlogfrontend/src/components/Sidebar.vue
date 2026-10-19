<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '../store/appStore'
import { useUserStore } from '../store/userStore'
import { useUserStatsStore } from '../store/userStatsStore'
import { useRealtimeStore } from '../store/realtimeStore'
import {
  HomeFilled, Bell, Star, Message,
  Compass, User
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const statsStore = useUserStatsStore()
const realtimeStore = useRealtimeStore()

// 侧边栏导航项
const menuItems = [
  // 主要导航
  { key: 'home', label: '首页', icon: HomeFilled, path: '/' },
  { key: 'message', label: '消息', icon: Message, path: '/message' },
  { key: 'notification', label: '通知', icon: Bell, path: '/notification' },
  { key: 'favorite', label: '收藏', icon: Star, path: '/favorite' },

  // 发现与热门
  { key: 'discover', label: '发现', icon: Compass, path: '/search' },

  // 个人相关
  { key: 'profile', label: '我的主页', icon: User, path: `/profile/${userStore.userInfo?.userId || ''}` },

  // 设置
  // { key: 'setting', label: '设置', icon: Setting, path: '/setting' }
]

const handleMenuClick = (item) => {
  appStore.setActiveMenu(item.key)
  router.push(item.key === 'profile' ? `/profile/${userStore.userId}` : item.path)
}

const isMenuActive = (item) => item.key === 'home'
  ? route.path === '/'
  : item.key === 'profile'
    ? route.path === `/profile/${userStore.userId}`
    : route.path.startsWith(item.path)

onMounted(() => {
  statsStore.refresh()
})
</script>

<template>
  <div class="sidebar glass-card">
    <!-- 个人信息卡片 -->
    <div class="user-profile-card">
      <span class="sidebar-kicker">MY CORNER</span>
      <div class="user-info" @click="router.push(`/profile/${userStore.userInfo?.userId || ''}`)">
        <el-avatar :size="64" :src="userStore.userInfo?.avatar" class="sidebar-avatar">
          <UserFilled />
        </el-avatar>
        <div class="user-details">
          <div class="user-name">{{ userStore.userInfo?.username || '未登录' }}</div>
          <div class="user-stats">
            <span class="stat-item">
              <span class="stat-number">{{ statsStore.stats.followingCount }}</span>
              <span class="stat-label">关注</span>
            </span>
            <span class="stat-divider"></span>
            <span class="stat-item">
              <span class="stat-number">{{ statsStore.stats.followerCount }}</span>
              <span class="stat-label">粉丝</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 侧边栏菜单 -->
    <div class="menu-list">
      <div v-for="item in menuItems" :key="item.key" class="menu-item"
        :class="{ active: isMenuActive(item) }" role="button" tabindex="0"
        :aria-current="isMenuActive(item) ? 'page' : undefined"
        @click="handleMenuClick(item)" @keydown.enter="handleMenuClick(item)" @keydown.space.prevent="handleMenuClick(item)">
        <el-icon class="menu-icon" :size="20">
          <component :is="item.icon" />
        </el-icon>
        <span class="menu-label">{{ item.label }}</span>
        <span v-if="item.key === 'notification' && realtimeStore.unreadNotifications > 0" class="menu-badge">
          {{ realtimeStore.unreadNotifications > 99 ? '99+' : realtimeStore.unreadNotifications }}
        </span>
        <span v-if="item.key === 'message' && realtimeStore.unreadMessages > 0" class="menu-badge">
          {{ realtimeStore.unreadMessages > 99 ? '99+' : realtimeStore.unreadMessages }}
        </span>
      </div>
    </div>

    <div class="sidebar-footer">分享此刻，遇见同频的人。</div>
  </div>
</template>

<style scoped>
/* 侧边栏容器 */
.sidebar {
  padding: 22px 15px;
  height: fit-content;
  min-height: 620px;
  display: flex;
  flex-direction: column;
}

.user-profile-card {
  margin-bottom: 20px;
  padding: 0 8px 20px;
  border-bottom: 1px solid var(--glass-border);
}

.sidebar-kicker { display: block; margin: 0 0 16px 8px; color: #ffab92; font-size: 10px; font-weight: 800; letter-spacing: .17em; }

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
}

.sidebar-avatar {
  border: 3px solid rgba(255, 184, 153, 0.4);
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
  gap: 6px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 13px 15px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-regular);
  font-weight: 500;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.055);
  color: #ffb29b;
}

.menu-item.active {
  background: linear-gradient(90deg, rgba(255, 121, 100, .2), rgba(255, 121, 100, .05));
  color: #ffb89d;
  box-shadow: inset 3px 0 #ff9075;
}

.menu-icon {
  margin-right: 12px;
}

.menu-label {
  font-size: 15px;
}

.menu-badge {
  margin-left: auto;
  background: var(--danger-color);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid var(--glass-border);
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
