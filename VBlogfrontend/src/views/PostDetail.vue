<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore'
import { getPostById, repost, deletePost } from '../api/post'
import { getUserInfo } from '../api/user'
import { getLikeStatus, likePost, unlikePost } from '../api/like'
import { ElCard, ElButton, ElAvatar, ElMessage, ElMessageBox, ElImage, ElIcon } from 'element-plus'
import { Heart, MessageCircle, Repeat2, Share, Delete, ArrowLeft } from '@element-plus/icons-vue'
import CommentList from '../components/CommentList.vue'
import RepostDialog from '../components/RepostDialog.vue'
import Layout from '../components/Layout.vue'
import { getImageUrl } from '../utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const postId = ref(parseInt(route.params.id))
const post = ref(null)
const author = ref(null)
const loading = ref(false)
const isLiked = ref(false)
const likeCount = ref(0)
const liking = ref(false)
const showRepostDialog = ref(false)

const isOwnPost = computed(() => post.value?.userId === userStore.userId)

const loadPostData = async () => {
  loading.value = true
  try {
    const postData = await getPostById(postId.value)
    post.value = postData

    // 加载作者信息
    if (postData.userId) {
      const userData = await getUserInfo(postData.userId)
      author.value = userData
    }

    // 加载点赞状态
    if (userStore.isLoggedIn) {
      const likeData = await getLikeStatus(postId.value)
      // 使用正确的字段名 isLiked
      isLiked.value = likeData.isLiked || false
      // 优先使用API返回的点赞数
      likeCount.value = likeData.likeCount !== undefined ? likeData.likeCount : (postData.likeCount || 0)
    } else {
      likeCount.value = postData.likeCount || 0
      isLiked.value = false
    }
  } catch (error) {
    ElMessage.error('加载帖子失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.info('请先登录')
    router.push('/login')
    return
  }

  liking.value = true
  try {
    if (isLiked.value) {
      // 取消点赞
      const result = await unlikePost(postId.value)
      isLiked.value = false
      // 使用后端返回的最新点赞数
      if (result.likeCount !== undefined) {
        likeCount.value = result.likeCount
      } else {
        likeCount.value = Math.max(0, likeCount.value - 1)
      }
    } else {
      // 点赞
      const result = await likePost(postId.value)
      isLiked.value = true
      // 使用后端返回的最新点赞数
      if (result.likeCount !== undefined) {
        likeCount.value = result.likeCount
      } else {
        likeCount.value = likeCount.value + 1
      }
    }
  } catch (error) {
    console.error(`Like operation failed for post ${postId.value}:`, error)
    // 错误已处理
  } finally {
    liking.value = false
  }
}

const handleRepost = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.info('请先登录')
    router.push('/login')
    return
  }
  showRepostDialog.value = true
}

const handleDelete = () => {
  ElMessageBox.confirm('确定要删除这条微博吗？删除后无法恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePost(postId.value)
      ElMessage.success('删除成功')
      router.back()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

const goBack = () => {
  router.back()
}

const getImageGridClass = (count) => {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count === 3) return 'grid-3'
  if (count === 4) return 'grid-4'
  return 'grid-9'
}

onMounted(() => {
  loadPostData()
})
</script>

<template>
  <Layout>
    <div class="post-detail-container" v-loading="loading">
      <!-- 返回按钮 -->
      <div class="header-actions">
        <el-button :icon="ArrowLeft" circle class="back-btn" @click="goBack" />
        <span class="page-title">帖子详情</span>
      </div>

      <!-- 帖子内容卡片 -->
      <div class="post-content-card glass-card">
        <div class="post-header">
          <div class="author-info" @click="router.push(`/profile/${author?.userId}`)" v-if="author">
            <el-avatar :src="author.avatar" :size="48" class="author-avatar" />
            <div class="author-meta">
              <h4 class="author-name">{{ author.username }}</h4>
              <p class="post-time">{{ formatTime(post?.createTime) }}</p>
            </div>
          </div>

          <el-dropdown v-if="isOwnPost" trigger="click">
            <el-button type="text" class="more-btn">
              <el-icon>
                <MoreFilled />
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleDelete" class="danger-item">
                  <el-icon>
                    <Delete />
                  </el-icon>删除帖子
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="post-body">
          <p v-if="post?.content" class="post-text">{{ post.content }}</p>

          <!-- 图片展示 -->
          <div v-if="post?.images && post.images.length > 0" class="post-images"
            :class="getImageGridClass(post.images.length)">
            <el-image v-for="(image, index) in post.images" :key="index" :src="image" :preview-src-list="post.images"
              :initial-index="index" fit="cover" class="post-image" />
          </div>

          <!-- 视频展示 -->
          <div v-if="post?.videoUrl" class="post-video">
            <video :src="getImageUrl(post.videoUrl)" controls preload="metadata">
              <source :src="getImageUrl(post.videoUrl)" type="video/mp4" />
              您的浏览器不支持视频播放。
            </video>
          </div>
        </div>

        <div class="post-stats">
          <div class="stat-item">
            <span class="label">转发</span>
            <span class="value">{{ post?.repostCount || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">评论</span>
            <span class="value">{{ post?.commentCount || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">点赞</span>
            <span class="value">{{ likeCount }}</span>
          </div>
        </div>

        <div class="post-actions">
          <el-button class="action-btn" @click="handleRepost">
            <el-icon>
              <Repeat2 />
            </el-icon>
            <span>转发</span>
          </el-button>
          <el-button class="action-btn">
            <el-icon>
              <MessageCircle />
            </el-icon>
            <span>评论</span>
          </el-button>
          <el-button class="action-btn" :class="{ active: isLiked }" @click="handleLike" :loading="liking">
            <el-icon v-if="isLiked">
              <HeartFilled />
            </el-icon>
            <el-icon v-else>
              <Heart />
            </el-icon>
            <span>{{ isLiked ? '已赞' : '点赞' }}</span>
          </el-button>
          <el-button class="action-btn">
            <el-icon>
              <Share />
            </el-icon>
            <span>分享</span>
          </el-button>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comments-section glass-card">
        <h3 class="section-title">评论</h3>
        <CommentList :post-id="postId" />
      </div>

      <!-- 转发对话框 -->
      <RepostDialog v-model="showRepostDialog" :source-post="post" @reposted="loadPostData" />
    </div>
  </Layout>
</template>

<style scoped>
.post-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main);
}

.post-content-card {
  padding: 24px;
  margin-bottom: 24px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  gap: 12px;
  cursor: pointer;
}

.author-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.author-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.author-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
}

.post-time {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.post-text {
  font-size: 16px;
  line-height: 1.6;
  color: var(--text-main);
  margin-bottom: 16px;
  white-space: pre-wrap;
}

/* 图片网格布局 */
.post-images {
  display: grid;
  gap: 8px;
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.grid-1 {
  grid-template-columns: 1fr;
}

.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.grid-4 {
  grid-template-columns: repeat(2, 1fr);
}

.grid-5,
.grid-6,
.grid-7,
.grid-8,
.grid-9 {
  grid-template-columns: repeat(3, 1fr);
}

.post-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  aspect-ratio: 1;
  border-radius: 8px;
  cursor: zoom-in;
}

.grid-1 .post-image {
  max-height: 400px;
  aspect-ratio: auto;
}

.post-video {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
  background: #000;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.post-video video {
  width: 100%;
  display: block;
  max-height: 600px;
}

.post-stats {
  display: flex;
  gap: 24px;
  padding: 16px 0;
  border-top: 1px solid var(--glass-border);
  border-bottom: 1px solid var(--glass-border);
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-item .value {
  font-weight: 600;
  color: var(--text-main);
}

.post-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.action-btn {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-weight: 500;
  padding: 12px;
  height: auto;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.03);
  color: var(--primary-color);
}

.action-btn.active {
  color: var(--danger-color);
}

.comments-section {
  padding: 24px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
}

.danger-item {
  color: var(--danger-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-detail-container {
    padding: 12px;
  }
}
</style>

<script>
function formatTime(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'

  return date.getFullYear() + '-' +
    String(date.getMonth() + 1).padStart(2, '0') + '-' +
    String(date.getDate()).padStart(2, '0')
}
</script>
