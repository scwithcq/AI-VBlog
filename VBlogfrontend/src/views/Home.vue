<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '../store/userStore'
import { getPostList } from '../api/post'
import { getUserInfo } from '../api/user'
import PostEditor from '../components/PostEditor.vue'
import PostCard from '../components/PostCard.vue'
import Layout from '../components/Layout.vue'
import HotTopics from '../components/HotTopics.vue'
import { ElCard, ElEmpty, ElPagination, ElTabs, ElTabPane } from 'element-plus'

const userStore = useUserStore()

const posts = ref([])
const users = ref({}) // Cache user info
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// Feed 类型：目前只支持 'all'（因为后端API文档中没有关注流接口）
const feedType = ref('all')

// 无限滚动相关
const loadingMore = ref(false)
const hasMore = computed(() => page.value * pageSize.value < total.value)
const observerTarget = ref(null)

const loadPosts = async (append = false) => {
  loading.value = true
  try {
    let response

    // 加载所有帖子列表
    response = await getPostList(page.value, pageSize.value)

    // 适配不同的响应格式（可能是 list 或 records）
    const newPosts = response.list || response.records || response || []
    total.value = response.total || 0

    // append: true 表示追加（无限滚动），false 表示覆盖（分页）
    if (append) {
      posts.value = [...posts.value, ...newPosts]
    } else {
      posts.value = newPosts
    }

    // 加载每个帖子对应的用户信息
    const userIds = [...new Set(newPosts.map(p => p.userId))]
    for (const userId of userIds) {
      if (!users.value[userId]) {
        try {
          const userInfo = await getUserInfo(userId)
          users.value[userId] = userInfo
        } catch (error) {
          console.error(`加载用户 ${userId} 失败:`, error)
        }
      }
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    // 错误已在 request.js 中通过 ElMessage.error 显示
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多（无限滚动）
const loadMore = async () => {
  if (!hasMore.value || loadingMore.value || loading.value) {
    return
  }

  loadingMore.value = true
  page.value++
  await loadPosts(true)
}

// 监听滚动，实现无限滚动
const setupIntersectionObserver = () => {
  if (!observerTarget.value) return

  const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value && !loadingMore.value) {
      loadMore()
    }
  }, { threshold: 0.1 })

  observer.observe(observerTarget.value)
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadPosts(false)
  window.scrollTo(0, 0)
}

const handleFeedTypeChange = (newType) => {
  feedType.value = newType
  page.value = 1 // 重置分页
  posts.value = [] // 清空当前帖子列表
  loadPosts(false)
}

const handlePostPublished = () => {
  page.value = 1 // 返回第一页
  loadPosts(false)
}

const handlePostReposted = () => {
  loadPosts(false)
}

const handlePostCommented = () => {
  // Comments are loaded within PostCard, no need to reload all posts
}

const handlePostLiked = () => {
  // Like status is updated within PostCard, no need to reload all posts
}

const handlePostDeleted = (postId) => {
  // 从列表中移除已删除的帖子
  posts.value = posts.value.filter(post => post.postId !== postId)
}

onMounted(() => {
  loadPosts(false)
  nextTick(() => {
    setupIntersectionObserver()
  })
})
</script>

<template>
  <Layout>
    <template #right>
      <div class="glass-card user-card-right">
        <div class="user-cover"></div>
        <div class="user-info-right">
          <el-avatar :src="userStore.userInfo?.avatar" :size="70" class="user-avatar-right" />
          <div class="user-details-right">
            <div class="username-right">{{ userStore.userInfo?.username }}</div>
            <div class="bio-right" v-if="userStore.userInfo?.bio">{{ userStore.userInfo.bio }}</div>
            <div class="stats-right">
              <div class="stat-item">
                <span class="stat-num">0</span>
                <span class="stat-label">关注</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">0</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">0</span>
                <span class="stat-label">微博</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <HotTopics />
    </template>

    <div class="home-container">
      <PostEditor @published="handlePostPublished" />

      <!-- Feed 切换选项卡 -->
      <div class="feed-tabs glass-card">
        <div class="tab-item active">
          <span>🌍 推荐</span>
        </div>
        <!-- 关注功能暂时禁用，等待后端API支持 -->
        <!-- <div class="tab-item" :class="{ active: feedType === 'following' }" @click="handleFeedTypeChange('following')">
          <span>👥 关注</span>
        </div> -->
      </div>

      <div v-loading="loading" class="posts-container">
        <PostCard v-for="post in posts" :key="post.postId" :post="post" :user="users[post.userId]"
          @reposted="handlePostReposted" @commented="handlePostCommented" @liked="handlePostLiked"
          @deleted="handlePostDeleted" />

        <el-empty v-if="!loading && posts.length === 0" description="暂无动态，去发布一条吧！" />

        <!-- 无限滚动加载指示器 -->
        <div v-if="loadingMore" class="loading-more">
          <el-icon class="is-loading">
            <Loading />
          </el-icon> 加载中...
        </div>

        <!-- 滚动到底部观察点 -->
        <div ref="observerTarget" class="observer-target" />

        <!-- 没有更多内容提示 -->
        <div v-if="!hasMore && posts.length > 0" class="no-more">
          - 到底了，去看看别的吧 -
        </div>
      </div>
    </div>
  </Layout>
</template>

<style scoped>
.home-container {
  max-width: 100%;
}

.user-card-right {
  margin-bottom: 20px;
  overflow: hidden;
  padding: 0;
}

.user-cover {
  height: 80px;
  background: var(--primary-gradient);
}

.user-info-right {
  padding: 0 20px 20px;
  margin-top: -35px;
  position: relative;
  text-align: center;
}

.user-avatar-right {
  border: 4px solid var(--glass-card-bg);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.username-right {
  font-size: 18px;
  font-weight: 600;
  margin-top: 8px;
  color: var(--text-main);
}

.bio-right {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
  margin-bottom: 16px;
}

.stats-right {
  display: flex;
  justify-content: space-around;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-main);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.feed-tabs {
  display: flex;
  padding: 4px;
  margin-bottom: 20px;
  gap: 4px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  cursor: pointer;
  border-radius: var(--border-radius-md);
  font-weight: 600;
  color: var(--text-secondary);
  transition: all 0.3s;
}

.tab-item:hover {
  background: rgba(139, 92, 246, 0.1);
  color: var(--text-main);
}

.tab-item.active {
  background: var(--glass-card-bg);
  color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(112, 77, 194, 0.3);
}

.posts-container {
  min-height: 400px;
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.observer-target {
  height: 1px;
  margin: 20px 0;
}

.no-more {
  text-align: center;
  padding: 30px;
  color: var(--text-secondary);
  font-size: 13px;
  opacity: 0.8;
}
</style>
