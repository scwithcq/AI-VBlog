<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '../store/userStore'
import { useUserStatsStore } from '../store/userStatsStore'
import { getPostList } from '../api/post'
import { getUserInfo } from '../api/user'
import PostEditor from '../components/PostEditor.vue'
import PostCard from '../components/PostCard.vue'
import Layout from '../components/Layout.vue'
import HotTopics from '../components/HotTopics.vue'
import { ElCard, ElEmpty, ElPagination, ElTabs, ElTabPane } from 'element-plus'

const userStore = useUserStore()
const statsStore = useUserStatsStore()
const todayLabel = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())

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
  statsStore.refresh()
}

const handlePostReposted = () => {
  loadPosts(false)
  statsStore.refresh()
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
  statsStore.refresh()
}

onMounted(() => {
  loadPosts(false)
  statsStore.refresh()
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
              <router-link class="stat-item" :to="`/following/${userStore.userId}`" aria-label="查看我的关注">
                <span class="stat-num">{{ statsStore.stats.followingCount }}</span>
                <span class="stat-label">关注</span>
              </router-link>
              <router-link class="stat-item" :to="`/followers/${userStore.userId}`" aria-label="查看我的粉丝">
                <span class="stat-num">{{ statsStore.stats.followerCount }}</span>
                <span class="stat-label">粉丝</span>
              </router-link>
              <router-link class="stat-item" :to="`/profile/${userStore.userId}`" aria-label="查看我的微博">
                <span class="stat-num">{{ statsStore.stats.postCount }}</span>
                <span class="stat-label">微博</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <HotTopics />
    </template>

    <div class="home-container">
      <section class="welcome-banner" aria-label="社区欢迎语">
        <div class="welcome-copy">
          <span class="welcome-date">{{ todayLabel }} · V-Blog 社区</span>
          <h1>让每一种声音，<span>都被看见。</span></h1>
          <p>记录此刻的新鲜事，和有趣的人分享你的世界。</p>
        </div>
        <div class="welcome-orbit" aria-hidden="true"><span>V</span></div>
      </section>
      <PostEditor @published="handlePostPublished" />

      <div class="feed-heading">
        <div>
          <span class="section-kicker">COMMUNITY FEED</span>
          <h2>正在发生</h2>
        </div>
        <span class="latest-pill"><span class="latest-dot"></span>最新动态</span>
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

.welcome-banner {
  position: relative;
  overflow: hidden;
  min-height: 184px;
  margin-bottom: 20px;
  padding: 30px 34px;
  border: 1px solid rgba(255, 189, 155, 0.23);
  border-radius: 24px;
  background: linear-gradient(116deg, #35283b 0%, #272a43 50%, #1c2941 100%);
  box-shadow: 0 24px 44px rgba(6, 10, 24, 0.2);
}

.welcome-banner::before {
  content: '';
  position: absolute;
  width: 360px;
  height: 360px;
  right: -74px;
  top: -135px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 135, 111, 0.32), transparent 66%);
}

.welcome-copy { position: relative; z-index: 1; max-width: 440px; }
.welcome-date, .section-kicker {
  color: #ffc6b0;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
}
.welcome-copy h1 {
  margin: 12px 0 9px;
  color: #fff;
  font-size: clamp(25px, 3vw, 34px);
  line-height: 1.25;
  letter-spacing: -0.03em;
}
.welcome-copy h1 span { color: #ffac8d; }
.welcome-copy p { color: #c9c8d8; font-size: 14px; }
.welcome-orbit {
  position: absolute;
  right: 45px;
  top: 18px;
  width: 156px;
  height: 156px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 204, 184, 0.4);
  border-radius: 50%;
  box-shadow: inset 0 0 35px rgba(255, 126, 103, 0.15), 0 0 50px rgba(255, 126, 103, 0.1);
  transform: rotate(-15deg);
}
.welcome-orbit::after {
  content: '';
  position: absolute;
  inset: 16px;
  border: 1px dashed rgba(255, 214, 186, 0.36);
  border-radius: 50%;
}
.welcome-orbit span {
  font-size: 73px;
  font-weight: 900;
  font-style: italic;
  color: #ff987e;
  text-shadow: 0 0 35px rgba(255, 118, 94, 0.5);
}

.feed-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  margin: 30px 2px 17px;
}
.feed-heading h2 { margin-top: 4px; font-size: 22px; line-height: 1.2; color: var(--text-main); }
.section-kicker { color: var(--text-secondary); font-size: 10px; }
.latest-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 12px;
  color: #ffc1aa;
  background: rgba(255, 121, 100, 0.1);
  border: 1px solid rgba(255, 121, 100, 0.16);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}
.latest-dot { width: 6px; height: 6px; border-radius: 50%; background: #ff8e70; box-shadow: 0 0 8px #ff8e70; }

.user-card-right {
  margin-bottom: 20px;
  overflow: hidden;
  padding: 0;
}

.user-cover {
  height: 80px;
  background: radial-gradient(circle at 85% 15%, rgba(255, 199, 151, .55), transparent 30%), linear-gradient(120deg, #624056, #363b63 65%, #303e66);
}

.user-info-right {
  padding: 0 20px 20px;
  margin-top: -35px;
  position: relative;
  text-align: center;
}

.user-avatar-right {
  border: 4px solid #1c2335;
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
  border-top: 1px solid var(--glass-border);
}

@media (max-width: 600px) {
  .welcome-banner { min-height: 164px; padding: 26px 22px; }
  .welcome-copy { max-width: 280px; }
  .welcome-copy h1 { font-size: 25px; }
  .welcome-copy p { font-size: 12px; }
  .welcome-orbit { width: 105px; height: 105px; right: -30px; top: 43px; opacity: .46; }
  .welcome-orbit span { font-size: 50px; }
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  padding: 8px 4px;
  border-radius: 10px;
  text-decoration: none;
  transition: background-color 0.2s;
}

.stat-item:hover,
.stat-item:focus-visible {
  background: rgba(255, 142, 112, 0.12);
}

.stat-item:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.stat-item:hover .stat-num,
.stat-item:hover .stat-label {
  color: var(--primary-color);
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
