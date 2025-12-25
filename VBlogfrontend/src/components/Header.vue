<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore'
import { ElInput, ElButton, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar, ElIcon, ElLoading } from 'element-plus'
import { Search, User, Bell, Plus, Setting, Star, Message, HomeFilled, VideoCameraFilled, Compass } from '@element-plus/icons-vue'
import { searchUsers, searchPosts } from '../api/search'
import { getUnreadCount } from '../api/notification'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const showSearchResults = ref(false)
const searchLoading = ref(false)
const searchResultsData = ref({
  users: [],
  posts: []
})
const activeSearchFilter = ref('all') // all, posts, users
const unreadNotificationCount = ref(0)
let debounceTimer = null

// 搜索功能 - 使用防抖
const performSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResultsData.value = { users: [], posts: [] }
    showSearchResults.value = false
    return
  }

  searchLoading.value = true
  showSearchResults.value = true

  // 实际的搜索逻辑
  const doSearch = async () => {
    try {
      const promises = []

      if (activeSearchFilter.value === 'all' || activeSearchFilter.value === 'users') {
        promises.push(searchUsers(keyword, 1, 5))
      }

      if (activeSearchFilter.value === 'all' || activeSearchFilter.value === 'posts') {
        promises.push(searchPosts(keyword, 1, 5))
      }

      const results = await Promise.all(promises)

      console.log('搜索结果:', results) // 调试日志

      if (activeSearchFilter.value === 'all') {
        searchResultsData.value = {
          users: results[0]?.list || results[0]?.records || (Array.isArray(results[0]) ? results[0] : []),
          posts: results[1]?.list || results[1]?.records || (Array.isArray(results[1]) ? results[1] : [])
        }
      } else if (activeSearchFilter.value === 'users') {
        searchResultsData.value = {
          users: results[0]?.list || results[0]?.records || (Array.isArray(results[0]) ? results[0] : []),
          posts: []
        }
      } else if (activeSearchFilter.value === 'posts') {
        searchResultsData.value = {
          users: [],
          posts: results[0]?.list || results[0]?.records || (Array.isArray(results[0]) ? results[0] : [])
        }
      }

      console.log('解析后的搜索结果:', searchResultsData.value) // 调试日志
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      searchLoading.value = false
    }
  }

  doSearch()
}

// 防抖包装器
const debouncedSearch = () => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  debounceTimer = setTimeout(() => {
    performSearch()
  }, 300)
}

// 监听搜索关键词变化
watch(searchKeyword, () => {
  debouncedSearch()
})

// 监听筛选器变化
watch(activeSearchFilter, () => {
  if (searchKeyword.value.trim()) {
    performSearch()
  }
})

// 加载未读通知数
const loadUnreadNotifications = async () => {
  try {
    const result = await getUnreadCount()
    // 兼容多种响应格式
    unreadNotificationCount.value = result.count || result.unreadCount || result || 0
  } catch (error) {
    console.error('加载未读通知数失败:', error)
    // 错误时不显示数字
    unreadNotificationCount.value = 0
  }
}

// 组件挂载时加载通知数
onMounted(() => {
  if (userStore.isLoggedIn) {
    loadUnreadNotifications()
    // 每隔5分钟刷新一次
    setInterval(loadUnreadNotifications, 5 * 60 * 1000)
  }
})

// 计算显示的搜索结果
const displayedResults = computed(() => {
  if (activeSearchFilter.value === 'users') {
    return { users: searchResultsData.value.users, posts: [] }
  } else if (activeSearchFilter.value === 'posts') {
    return { users: [], posts: searchResultsData.value.posts }
  }
  return searchResultsData.value
})

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    showSearchResults.value = false
    router.push(`/search?q=${encodeURIComponent(keyword)}`)
  }
}

const handleSearchFocus = () => {
  if (searchKeyword.value.trim()) {
    showSearchResults.value = true
  }
}

const closeSearchResults = () => {
  setTimeout(() => {
    showSearchResults.value = false
  }, 200)
}

const goToProfile = (userId) => {
  showSearchResults.value = false
  router.push(`/profile/${userId}`)
}

const goToPost = (postId) => {
  showSearchResults.value = false
  router.push(`/post/${postId}`)
}

const viewAllResults = () => {
  handleSearch()
}

const handleCommand = (command) => {
  if (command === 'profile') {
    if (userStore.userInfo) {
      router.push(`/profile/${userStore.userInfo.userId}`)
    }
  } else if (command === 'message') {
    router.push('/message')
  } else if (command === 'favorite') {
    router.push('/favorite')
  } else if (command === 'setting') {
    router.push('/setting')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

const handlePublish = () => {
  // 发布功能逻辑，这里暂时跳转到首页
  router.push('/')
}

const handleVideoClick = () => {
  window.open('https://weibo.com/tv/home', '_blank')
}

const formatPostTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const postTime = new Date(time)
  const diff = now - postTime
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return postTime.toLocaleDateString('zh-CN')
}
</script>

<template>
  <header class="header">
    <div class="header-content">
      <!-- 左侧Logo和主要导航 -->
      <div class="header-left">
        <div class="logo" @click="router.push('/')">
          <span class="logo-text">V-Blog</span>
        </div>

        <nav class="main-nav">
          <div class="nav-item active" @click="router.push('/')">
            <el-icon>
              <HomeFilled />
            </el-icon>
            <span>首页</span>
          </div>
          <div class="nav-item" @click="handleVideoClick">
            <el-icon>
              <VideoCameraFilled />
            </el-icon>
            <span>视频</span>
          </div>
          <!-- <div class="nav-item">
            <el-icon>
              <Compass />
            </el-icon>
            <span>发现</span>
          </div> -->
        </nav>
      </div>

      <!-- 中间搜索框 -->
      <div class="header-center">
        <div class="search-container">
          <el-input v-model="searchKeyword" placeholder="搜索用户、内容、话题..." class="search-input" @keyup.enter="handleSearch"
            @focus="handleSearchFocus" @blur="closeSearchResults" size="large">
            <template #prefix>
              <el-icon class="search-icon">
                <Search />
              </el-icon>
            </template>
          </el-input>

          <!-- 搜索结果下拉框 -->
          <div v-if="showSearchResults" class="search-results-dropdown">
            <!-- 筛选器 -->
            <div class="search-filters">
              <div class="filter-item" :class="{ active: activeSearchFilter === 'all' }"
                @click="activeSearchFilter = 'all'">
                全部
              </div>
              <div class="filter-item" :class="{ active: activeSearchFilter === 'posts' }"
                @click="activeSearchFilter = 'posts'">
                帖子
              </div>
              <div class="filter-item" :class="{ active: activeSearchFilter === 'users' }"
                @click="activeSearchFilter = 'users'">
                用户
              </div>
            </div>

            <!-- 加载状态 -->
            <div v-if="searchLoading" class="search-loading">
              <el-icon class="is-loading">
                <i class="el-icon-loading"></i>
              </el-icon>
              <span>搜索中...</span>
            </div>

            <!-- 搜索结果内容 -->
            <div v-else class="search-results-content">
              <!-- 用户结果 -->
              <div v-if="displayedResults.users.length > 0" class="result-section">
                <div class="section-title">用户</div>
                <div v-for="user in displayedResults.users" :key="user.userId" class="result-item user-result"
                  @click="goToProfile(user.userId)">
                  <el-avatar :src="user.avatar" :size="36" />
                  <div class="result-info">
                    <div class="result-name">{{ user.username }}</div>
                    <div v-if="user.bio" class="result-bio">{{ user.bio }}</div>
                  </div>
                </div>
              </div>

              <!-- 帖子结果 -->
              <div v-if="displayedResults.posts.length > 0" class="result-section">
                <div class="section-title">帖子</div>
                <div v-for="post in displayedResults.posts" :key="post.postId" class="result-item post-result"
                  @click="goToPost(post.postId)">
                  <div class="result-info">
                    <div class="result-content">{{ post.content }}</div>
                    <div class="result-meta">
                      <span class="post-author">@{{ post.username || '用户' }}</span>
                      <span class="post-time">{{ formatPostTime(post.createTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 无结果 -->
              <div v-if="displayedResults.users.length === 0 && displayedResults.posts.length === 0" class="no-results">
                <el-icon>
                  <Search />
                </el-icon>
                <span>未找到相关结果</span>
              </div>

              <!-- 查看全部结果 -->
              <div v-if="displayedResults.users.length > 0 || displayedResults.posts.length > 0" class="view-all"
                @click="viewAllResults">
                查看全部结果 →
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧用户功能区 -->
      <div class="header-right">
        <!-- 发布按钮 -->
        <el-button class="publish-btn" type="primary" size="large" @click="handlePublish">
          <el-icon>
            <Plus />
          </el-icon>
          <span>发布</span>
        </el-button>

        <!-- 消息通知 -->
        <div class="header-icon" @click="router.push('/notification')">
          <el-icon class="icon-with-badge">
            <Bell />
            <span v-if="unreadNotificationCount > 0" class="badge">{{ unreadNotificationCount > 99 ? '99+' :
              unreadNotificationCount }}</span>
          </el-icon>
        </div>

        <!-- 用户菜单 -->
        <el-dropdown v-if="userStore.isLoggedIn" @command="handleCommand" trigger="click">
          <div class="user-menu">
            <el-avatar :src="userStore.userInfo?.avatar" :size="36" class="user-avatar">
              <el-icon>
                <User />
              </el-icon>
            </el-avatar>
            <span v-if="userStore.userInfo" class="username">{{ userStore.userInfo.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="glass-dropdown">
              <el-dropdown-item command="profile">
                <el-icon>
                  <User />
                </el-icon>
                <span>个人主页</span>
              </el-dropdown-item>
              <el-dropdown-item command="message">
                <el-icon>
                  <Message />
                </el-icon>
                <span>消息中心</span>
              </el-dropdown-item>
              <el-dropdown-item command="favorite">
                <el-icon>
                  <Star />
                </el-icon>
                <span>我的收藏</span>
              </el-dropdown-item>
              <el-dropdown-item command="setting">
                <el-icon>
                  <Setting />
                </el-icon>
                <span>设置</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 未登录状态 -->
        <div v-else class="login-register">
          <el-button type="text" @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </div>
      </div>
    </div>
  </header>
</template>


<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  transition: var(--transition-base);
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  gap: 24px;
}

/* 左侧Logo和导航 */
.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
  flex: 0 0 auto;
}

.logo {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 26px;
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

/* 主要导航 */
.main-nav {
  display: flex;
  gap: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-regular);
}

.nav-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--primary-color);
}

.nav-item.active {
  background-color: rgba(139, 92, 246, 0.2);
  color: var(--primary-color);
}

/* 中间搜索框 */
.header-center {
  flex: 1;
  max-width: 480px;
}

.search-container {
  position: relative;
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  background-color: rgba(31, 41, 55, 0.6);
  box-shadow: none;
  padding-left: 16px;
  transition: all 0.3s;
  border: 1px solid var(--glass-border);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background-color: rgba(31, 41, 55, 0.8);
  box-shadow: 0 0 0 2px var(--primary-color);
}

.search-icon {
  color: var(--text-secondary);
}

/* 搜索结果下拉框 */
.search-results-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-lg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  max-height: 500px;
  overflow-y: auto;
  z-index: 1000;
}

/* 筛选器 */
.search-filters {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--glass-border);
  background: rgba(31, 41, 55, 0.3);
}

.filter-item {
  padding: 6px 16px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-regular);
  transition: all 0.3s;
  border: 1px solid transparent;
}

.filter-item:hover {
  background-color: rgba(139, 92, 246, 0.1);
  color: var(--primary-color);
}

.filter-item.active {
  background: var(--primary-gradient);
  color: white;
  border-color: transparent;
}

/* 搜索加载状态 */
.search-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px;
  color: var(--text-secondary);
}

/* 搜索结果内容 */
.search-results-content {
  padding: 8px 0;
}

.result-section {
  margin-bottom: 8px;
}

.section-title {
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.result-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.result-item:hover {
  background-color: rgba(139, 92, 246, 0.1);
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-name {
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 4px;
}

.result-bio {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-content {
  color: var(--text-main);
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.result-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.post-author {
  font-weight: 500;
}

/* 无结果 */
.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px;
  color: var(--text-secondary);
}

.no-results .el-icon {
  font-size: 32px;
  opacity: 0.5;
}

/* 查看全部结果 */
.view-all {
  padding: 12px 16px;
  text-align: center;
  color: var(--primary-color);
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  border-top: 1px solid var(--glass-border);
  transition: all 0.3s;
}

.view-all:hover {
  background-color: rgba(139, 92, 246, 0.1);
}

/* 右侧用户功能区 */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 0 0 auto;
}

/* 发布按钮 */
.publish-btn {
  border-radius: 20px;
  font-weight: 600;
  padding: 8px 24px;
  background: var(--primary-gradient);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 154, 158, 0.4);
  transition: all 0.3s;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 154, 158, 0.5);
}

/* 头部图标 */
.header-icon {
  position: relative;
  padding: 8px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-regular);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--primary-color);
}

/* 带徽章的图标 */
.icon-with-badge {
  font-size: 22px;
}

.badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: var(--danger-color);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
  border: 2px solid #fff;
  line-height: 1;
}

/* 用户菜单 */
.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 4px 4px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.user-menu:hover {
  background-color: rgba(139, 92, 246, 0.15);
  border-color: var(--glass-border);
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.username {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-main);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 登录注册按钮 */
.login-register {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .header-left {
    gap: 20px;
  }

  .main-nav {
    gap: 8px;
  }

  .nav-item span {
    display: none;
  }

  .header-center {
    max-width: 300px;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
    gap: 12px;
  }

  .main-nav {
    display: none;
  }

  .header-center {
    flex: 1;
  }

  .publish-btn span {
    display: none;
  }

  .publish-btn {
    padding: 8px;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .username {
    display: none;
  }
}
</style>
