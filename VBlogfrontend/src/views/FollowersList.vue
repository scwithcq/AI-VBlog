<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore'
import { getUserInfo } from '../api/user'
import { getFollowers } from '../api/follow'
import { ElCard, ElPagination, ElButton, ElAvatar, ElEmpty, ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import FollowButton from '../components/FollowButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = ref(parseInt(route.params.userId))
const user = ref(null)
const followers = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadFollowers = async () => {
  loading.value = true
  try {
    const result = await getFollowers(userId.value, currentPage.value, pageSize.value)
    // 兼容多种响应格式：{ list: [] } 或 { records: [] } 或 { data: [] } 或直接数组
    followers.value = result.list || result.records || result.data || (Array.isArray(result) ? result : [])
    total.value = result.total || 0
  } catch (error) {
    console.error('加载粉丝列表失败:', error)
    ElMessage.error('加载粉丝列表失败')
  } finally {
    loading.value = false
  }
}

// 处理关注状态变化
const handleFollowChange = () => {
  // 可以选择重新加载列表或只更新统计信息
  // 这里选择不重新加载，让FollowButton自己管理状态
}

const loadUserInfo = async () => {
  try {
    const userData = await getUserInfo(userId.value)
    user.value = userData
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

const goToProfile = (uid) => {
  router.push(`/profile/${uid}`)
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadFollowers()
}

onMounted(() => {
  loadUserInfo()
  loadFollowers()
})
</script>

<template>
  <Layout>
    <div class="followers-container">
      <!-- 用户头部信息 -->
      <el-card v-if="user" class="user-header">
        <div class="user-info-row">
          <el-avatar :src="user.avatar" :size="60" />
          <div class="user-info">
            <h3>{{ user.username }} 的粉丝</h3>
            <p>共 {{ total }} 位粉丝</p>
          </div>
        </div>
      </el-card>

      <!-- 粉丝列表 -->
      <div v-loading="loading" class="followers-list">
        <el-empty v-if="followers.length === 0 && !loading" description="还没有粉丝" :image-size="120">
          <template #description>
            <span style="color: #909399;">分享精彩内容，吸引更多粉丝吧～</span>
          </template>
        </el-empty>

        <div v-else>
          <el-card v-for="follower in followers" :key="follower.userId" class="follower-card">
            <div class="follower-item">
              <el-avatar :src="follower.avatar || '/default-avatar.png'" :size="40"
                @click="goToProfile(follower.userId)" style="cursor: pointer" />
              <div class="follower-info">
                <h4 @click="goToProfile(follower.userId)" style="cursor: pointer; margin: 0">
                  {{ follower.username }}
                </h4>
                <p class="follower-bio">{{ follower.bio || '这个人很懒，什么都没留下~' }}</p>
              </div>
              <follow-button :target-user-id="follower.userId" @followed="handleFollowChange"
                @unfollowed="handleFollowChange" />
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination">
          <el-pagination :current-page="currentPage" :page-size="pageSize" :total="total"
            layout="prev, pager, next, total" @current-change="handlePageChange" />
        </div>
      </div>
    </div>
  </Layout>
</template>

<style scoped>
.followers-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.user-header {
  margin-bottom: 20px;
}

.user-info-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info h3 {
  margin: 0;
  font-size: 18px;
}

.user-info p {
  margin: 4px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.followers-list {
  min-height: 300px;
}

.follower-card {
  margin-bottom: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.follower-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.15);
}

.follower-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.follower-info {
  flex: 1;
  min-width: 0;
}

.follower-info h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
  transition: color 0.3s ease;
}

.follower-info h4:hover {
  color: var(--primary-color);
}

.follower-bio {
  margin: 0;
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
