<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore'
import { getUserInfo } from '../api/user'
import { getFollowing } from '../api/follow'
import { ElCard, ElPagination, ElButton, ElAvatar, ElEmpty, ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import FollowButton from '../components/FollowButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = ref(parseInt(route.params.userId))
const user = ref(null)
const following = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadFollowing = async () => {
  loading.value = true
  try {
    const result = await getFollowing(userId.value, currentPage.value, pageSize.value)
    // 兼容多种响应格式：{ list: [] } 或 { records: [] } 或 { data: [] } 或直接数组
    following.value = result.list || result.records || result.data || (Array.isArray(result) ? result : [])
    total.value = result.total || 0
  } catch (error) {
    console.error('加载关注列表失败:', error)
    ElMessage.error('加载关注列表失败')
  } finally {
    loading.value = false
  }
}

// 处理取消关注
const handleUnfollow = () => {
  // 重新加载列表以更新状态
  loadFollowing()
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
  loadFollowing()
}

onMounted(() => {
  loadUserInfo()
  loadFollowing()
})
</script>

<template>
  <Layout>
    <div class="following-container">
      <!-- 用户头部信息 -->
      <el-card v-if="user" class="user-header">
        <div class="user-info-row">
          <el-avatar :src="user.avatar" :size="60" />
          <div class="user-info">
            <h3>{{ user.username }} 的关注</h3>
            <p>关注了 {{ total }} 个用户</p>
          </div>
        </div>
      </el-card>

      <!-- 关注列表 -->
      <div v-loading="loading" class="following-list">
        <el-empty v-if="following.length === 0 && !loading" description="还没有关注任何人" :image-size="120">
          <template #description>
            <span class="empty-description">发现感兴趣的人，开始关注吧～</span>
          </template>
        </el-empty>

        <div v-else>
          <el-card v-for="followUser in following" :key="followUser.userId" class="follow-card">
            <div class="follow-item">
              <el-avatar :src="followUser.avatar || '/default-avatar.jpg'" :size="40"
                @click="goToProfile(followUser.userId)" style="cursor: pointer" />
              <div class="follow-info">
                <h4 @click="goToProfile(followUser.userId)" style="cursor: pointer; margin: 0">
                  {{ followUser.username }}
                </h4>
                <p class="follow-bio">{{ followUser.bio || '这个人很懒，什么都没留下~' }}</p>
              </div>
              <follow-button :target-user-id="followUser.userId" @unfollowed="handleUnfollow" />
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
.following-container {
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

.empty-description {
  color: var(--text-secondary);
}

.user-info h3 {
  color: var(--text-main);
  margin: 0;
  font-size: 18px;
}

.user-info p {
  margin: 4px 0 0 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.following-list {
  min-height: 300px;
}

.follow-card {
  margin-bottom: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.follow-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.15);
}

.follow-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.follow-info {
  flex: 1;
  min-width: 0;
}

.follow-info h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
  transition: color 0.3s ease;
}

.follow-info h4:hover {
  color: var(--primary-color);
}

.follow-bio {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--glass-border);
}
</style>
