<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchUsers, searchPosts, getSearchHistory, addSearchHistory, clearSearchHistory, getHotSearchTopics } from '../api/search'
import { getUserInfo } from '../api/user'
import PostCard from '../components/PostCard.vue'
import Layout from '../components/Layout.vue'
import { ElTabs, ElTabPane, ElCard, ElAvatar, ElEmpty, ElPagination, ElButton, ElTag } from 'element-plus'

const route = useRoute()
const router = useRouter()

const keyword = ref(route.query.q || '')
const activeTab = ref('all')

const users = ref([])
const posts = ref([])
const postUsers = ref({}) // Cache user info for posts
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchHistory = ref([])
const hotKeywords = ref([])
const hasSearched = ref(!!route.query.q)

const loadSearchResults = async () => {
  if (!keyword.value.trim()) {
    return
  }

  hasSearched.value = true
  loading.value = true
  try {
    // 保存搜索历史
    addSearchHistory(keyword.value)

    if (activeTab.value === 'all') {
      // 同时搜索用户和帖子
      const [userResult, postResult] = await Promise.all([
        searchUsers(keyword.value, page.value, pageSize.value),
        searchPosts(keyword.value, page.value, pageSize.value)
      ])

      users.value = userResult.list || userResult.records || userResult || []
      posts.value = postResult.list || postResult.records || postResult || []
      total.value = (userResult.total || 0) + (postResult.total || 0)

      // 加载帖子对应的用户信息
      const userIds = [...new Set(posts.value.map(p => p.userId))]
      for (const userId of userIds) {
        if (!postUsers.value[userId]) {
          try {
            const userInfo = await getUserInfo(userId)
            postUsers.value[userId] = userInfo
          } catch (error) {
            console.error(`加载用户 ${userId} 失败:`, error)
          }
        }
      }
    } else if (activeTab.value === 'users') {
      // 根据 API 文档，搜索用户的响应格式为 { list: [], total: 0, page: 1, size: 10 }
      const result = await searchUsers(keyword.value, page.value, pageSize.value)
      users.value = result.list || result.records || result || []
      posts.value = []
      total.value = result.total || 0
    } else if (activeTab.value === 'posts') {
      // 根据 API 文档，搜索帖子的响应格式为 { list: [], total: 0, page: 1, size: 10 }
      const result = await searchPosts(keyword.value, page.value, pageSize.value)
      posts.value = result.list || result.records || result || []
      users.value = []
      total.value = result.total || 0

      // 加载帖子对应的用户信息
      const userIds = [...new Set(posts.value.map(p => p.userId))]
      for (const userId of userIds) {
        if (!postUsers.value[userId]) {
          try {
            const userInfo = await getUserInfo(userId)
            postUsers.value[userId] = userInfo
          } catch (error) {
            console.error(`加载用户 ${userId} 失败:`, error)
          }
        }
      }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    // 错误已在 request.js 中通过 ElMessage.error 显示
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1
  loadSearchResults()
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadSearchResults()
}

const goToProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

const loadSearchHistoryAndHotKeywords = async () => {
  try {
    const history = await getSearchHistory()
    searchHistory.value = history

    const hotData = await getHotSearchTopics()
    hotKeywords.value = (Array.isArray(hotData) ? hotData : hotData?.list || []).map(topic => topic.name || topic)
  } catch (error) {
    console.error('加载搜索历史或热搜词失败:', error)
  }
}

const quickSearch = (searchKeyword) => {
  keyword.value = searchKeyword
  page.value = 1
  loadSearchResults()
}

const handleClearHistory = () => {
  clearSearchHistory()
  searchHistory.value = []
}

watch(() => route.query.q, (newQ) => {
  keyword.value = newQ || ''
  page.value = 1
  loadSearchResults()
})

onMounted(() => {
  loadSearchHistoryAndHotKeywords()
  if (keyword.value) {
    loadSearchResults()
  }
})
</script>

<template>
  <Layout>
    <div class="search-result-container">
      <el-card>
        <template #header>
          <h2>搜索</h2>
        </template>

        <!-- 当未进行搜索时显示历史和热搜 -->
        <div v-if="!hasSearched" class="search-suggestions">
          <!-- 热搜词 -->
          <div v-if="hotKeywords.length > 0" class="suggestion-section">
            <h3>🔥 热搜</h3>
            <div class="keywords-grid">
              <el-tag v-for="(k, index) in hotKeywords.slice(0, 10)" :key="index" clickable @click="quickSearch(k)">
                #{{ k }}
              </el-tag>
            </div>
          </div>

          <!-- 搜索历史 -->
          <div v-if="searchHistory.length > 0" class="suggestion-section">
            <h3>
              ⏱ 搜索历史
              <el-button text type="info" size="small" @click="handleClearHistory">清空</el-button>
            </h3>
            <div class="keywords-grid">
              <el-tag v-for="(item, index) in searchHistory" :key="index" closable @click="quickSearch(item)"
                @close="searchHistory.splice(index, 1)">
                {{ item }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div v-if="hasSearched" class="search-results">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <!-- 全部选项卡 -->
            <el-tab-pane label="全部" name="all">
              <div v-loading="loading" class="results-container">
                <!-- 用户结果 -->
                <div v-if="users.length > 0" class="result-section">
                  <h3 class="section-title">用户</h3>
                  <div v-for="user in users" :key="user.userId" class="user-item" @click="goToProfile(user.userId)">
                    <el-avatar :src="user.avatar" :size="50" />
                    <div class="user-info">
                      <div class="username">{{ user.username }}</div>
                      <div class="bio" v-if="user.bio">{{ user.bio }}</div>
                    </div>
                  </div>
                </div>

                <!-- 帖子结果 -->
                <div v-if="posts.length > 0" class="result-section">
                  <h3 class="section-title">帖子</h3>
                  <PostCard v-for="post in posts" :key="post.postId" :post="post" :user="postUsers[post.userId]" />
                </div>

                <el-empty v-if="!loading && users.length === 0 && posts.length === 0" description="暂无结果" />
              </div>
            </el-tab-pane>

            <!-- 帖子选项卡 -->
            <el-tab-pane label="帖子" name="posts">
              <div v-loading="loading" class="results-container">
                <PostCard v-for="post in posts" :key="post.postId" :post="post" :user="postUsers[post.userId]" />
                <el-empty v-if="!loading && posts.length === 0" description="暂无结果" />
              </div>
            </el-tab-pane>

            <!-- 用户选项卡 -->
            <el-tab-pane label="用户" name="users">
              <div v-loading="loading" class="results-container">
                <div v-for="user in users" :key="user.userId" class="user-item" @click="goToProfile(user.userId)">
                  <el-avatar :src="user.avatar" :size="50" />
                  <div class="user-info">
                    <div class="username">{{ user.username }}</div>
                    <div class="bio" v-if="user.bio">{{ user.bio }}</div>
                  </div>
                </div>
                <el-empty v-if="!loading && users.length === 0" description="暂无结果" />
              </div>
            </el-tab-pane>
          </el-tabs>

          <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="pageSize" :total="total"
            layout="prev, pager, next" @current-change="handlePageChange"
            style="margin-top: 20px; justify-content: center" />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<style scoped>
.search-result-container {
  max-width: 100%;
}

.search-suggestions {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.suggestion-section {
  padding: 20px;
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--glass-border);
}

.suggestion-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.keywords-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.keywords-grid :deep(.el-tag) {
  cursor: pointer;
  transition: all 0.3s ease;
}

.keywords-grid :deep(.el-tag:hover) {
  background-color: #409eff;
  color: white;
}

.search-results {
  margin-top: 20px;
}

.results-container {
  min-height: 400px;
}

.result-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-main);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--primary-color);
}

.user-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-item:hover {
  background-color: rgba(139, 92, 246, 0.1);
}

.user-info {
  flex: 1;
}

.username {
  font-weight: 500;
  font-size: 16px;
  margin-bottom: 4px;
}

.bio {
  font-size: 14px;
  color: #909399;
}
</style>
