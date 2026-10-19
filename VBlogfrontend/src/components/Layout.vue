<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/userStore'
import { useAppStore } from '../store/appStore'
import { useRealtimeStore } from '../store/realtimeStore'
import { HomeFilled, Message, Bell, User } from '@element-plus/icons-vue'
import Header from './Header.vue'
import Sidebar from './Sidebar.vue'
import HotTopics from './HotTopics.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const realtimeStore = useRealtimeStore()
const mobileNav = [
  { key: 'home', label: '首页', icon: HomeFilled, path: '/' },
  { key: 'message', label: '消息', icon: Message, path: '/message' },
  { key: 'notification', label: '通知', icon: Bell, path: '/notification' },
  { key: 'profile', label: '我的', icon: User, path: '/profile' }
]
const mobilePath = (item) => item.key === 'profile' ? `/profile/${userStore.userId}` : item.path
const mobileActive = (item) => item.key === 'home' ? route.path === '/' : route.path.startsWith(mobilePath(item))

// 深色模式状态
const isDarkMode = ref(false)

// 检查登录状态
if (!userStore.isLoggedIn && route.path !== '/login' && route.path !== '/register') {
  router.push('/login')
}

// 计算当前是否在登录或注册页面
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

// 初始化深色模式
const initDarkMode = () => {
  const savedDarkMode = localStorage.getItem('darkMode')
  if (savedDarkMode) {
    isDarkMode.value = JSON.parse(savedDarkMode)
  } else {
    // 检查系统偏好
    isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  applyDarkMode()
}

// 应用深色模式
const applyDarkMode = () => {
  const html = document.documentElement
  if (isDarkMode.value) {
    html.classList.add('dark-mode')
  } else {
    html.classList.remove('dark-mode')
  }
  localStorage.setItem('darkMode', JSON.stringify(isDarkMode.value))
}

// 切换深色模式
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  applyDarkMode()
}

onMounted(() => {
  initDarkMode()
})

// 导出切换函数供 Header 使用
defineExpose({ toggleDarkMode, isDarkMode })
</script>

<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <Header />

    <!-- 主要内容区域（非登录注册页面） -->
    <div v-if="!isAuthPage" class="layout-content">
      <div class="content-wrapper">
        <!-- 左侧边栏 -->
        <aside class="sidebar-left">
          <Sidebar />
        </aside>

        <!-- 中间主内容区 -->
        <main class="main-content">
          <slot />
        </main>

        <!-- 右侧信息栏 -->
        <aside class="sidebar-right">
          <slot name="right">
            <!-- 热门话题和推荐关注 -->
            <HotTopics />
          </slot>
        </aside>
      </div>
    </div>

    <!-- 登录注册页面内容 -->
    <div v-else class="auth-page">
      <router-view />
    </div>
    <nav v-if="!isAuthPage" class="mobile-nav" aria-label="移动端主导航">
      <button v-for="item in mobileNav" :key="item.key" type="button" class="mobile-nav-item"
        :class="{ active: mobileActive(item) }" :aria-current="mobileActive(item) ? 'page' : undefined"
        @click="router.push(mobilePath(item))">
        <el-icon :size="21"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
        <i v-if="item.key === 'message' && realtimeStore.unreadMessages > 0" class="mobile-unread"></i>
        <i v-if="item.key === 'notification' && realtimeStore.unreadNotifications > 0" class="mobile-unread"></i>
      </button>
    </nav>
  </div>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  width: 100%;
}

/* 主要内容区域 */
.layout-content {
  width: 100%;
  padding: 25px 0 50px;
  background: transparent;
}

.content-wrapper {
  max-width: 1380px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr) 300px;
  gap: 24px;
  padding: 0 20px;
}

/* 左侧边栏 */
.sidebar-left {
  position: sticky;
  top: 94px;
  height: fit-content;
  min-height: calc(100vh - 100px);
}

/* 中间主内容区 */
.main-content {
  min-height: calc(100vh - 100px);
  /* 移除之前的强背景，让内容自然流淌 */
  background: transparent;
  border-radius: var(--border-radius-lg);
  overflow: visible;
  /* 允许子元素阴影溢出 */
}

/* 右侧信息栏 */
.sidebar-right {
  position: sticky;
  top: 94px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mobile-nav { display: none; }

/* 页脚信息 */
.sidebar-footer {
  margin-top: 20px;
  padding: 16px;
  font-size: 12px;
  color: var(--info-color);
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--glass-shadow);
}

.links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.links a {
  color: var(--info-color);
  text-decoration: none;
  transition: var(--transition-base);
}

.links a:hover {
  color: var(--primary-color);
}

.copyright {
  margin-top: 8px;
  opacity: 0.8;
}

/* 登录注册页面样式 */
.auth-page {
  width: 100%;
  min-height: calc(100vh - 60px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 210px minmax(0, 1fr) 270px;
    gap: 16px;
  }
}

@media (max-width: 992px) {
  .content-wrapper {
    grid-template-columns: 200px 1fr;
  }

  .sidebar-right {
    display: none;
  }
}

@media (max-width: 768px) {
  .layout-content { padding: 16px 0 96px; }
  .content-wrapper {
    grid-template-columns: 1fr;
    padding: 0 12px;
  }

  .sidebar-left {
    display: none;
  }

  .mobile-nav {
    position: fixed;
    z-index: 1001;
    inset: auto 0 0;
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
    background: rgba(18, 24, 39, .96);
    border-top: 1px solid var(--glass-border);
    box-shadow: 0 -12px 35px rgba(4, 8, 18, .28);
    backdrop-filter: blur(20px);
  }
  .mobile-nav-item {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 3px;
    min-height: 49px;
    padding: 5px;
    background: transparent;
    border: 0;
    color: var(--text-secondary);
    font-size: 10px;
  }
  .mobile-nav-item.active { color: #ffaf93; background: rgba(255, 121, 100, .1); }
  .mobile-unread { position: absolute; top: 4px; right: calc(50% - 17px); width: 7px; height: 7px; border-radius: 50%; background: #ff667f; }
}
</style>
