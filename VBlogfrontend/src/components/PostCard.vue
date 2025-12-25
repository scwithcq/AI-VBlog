<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElCard, ElAvatar, ElButton, ElImage, ElDropdown, ElDropdownMenu, ElDropdownItem, ElMessageBox } from 'element-plus'
import { getSourcePost, deletePost } from '../api/post'
import { getUserInfo } from '../api/user'
import { getLikeStatus, likePost, unlikePost } from '../api/like'
import { useUserStore } from '../store/userStore'
import { ElMessage } from 'element-plus'
import { Share, ChatDotRound, Star, StarFilled, MoreFilled, Warning, Delete } from '@element-plus/icons-vue'
import RepostDialog from './RepostDialog.vue'
import CommentList from './CommentList.vue'
import { parseImages } from '../utils/image'
import { getImageUrl } from '../utils/image'

const props = defineProps({
  post: {
    type: Object,
    required: true
  },
  user: {
    type: Object,
    default: null
  },
  showSource: {
    type: Boolean,
    default: true
  }
})

const router = useRouter()
const userStore = useUserStore()
const sourcePost = ref(null)
const sourceUser = ref(null)
const loadingSource = ref(false)
const sourceDeleted = ref(false)
const repostDialogVisible = ref(false)
const showComments = ref(false)
// 从props.post初始化点赞状态，确保每个帖子有独立的状态
const isLiked = ref(props.post.isLiked || false)
const likeCount = ref(props.post.likeCount || 0)
const liking = ref(false)

const emit = defineEmits(['reposted', 'commented', 'liked', 'deleted'])

const handleRepost = () => {
  repostDialogVisible.value = true
}

const handleComment = () => {
  showComments.value = !showComments.value
  if (showComments.value) {
    emit('commented', props.post.postId)
  }
}

const handleCommented = () => {
  emit('commented', props.post.postId)
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  if (liking.value) return

  liking.value = true
  try {
    if (isLiked.value) {
      // 取消点赞
      const result = await unlikePost(props.post.postId)
      isLiked.value = false
      // 使用后端返回的最新点赞数
      if (result.likeCount !== undefined) {
        likeCount.value = result.likeCount
      } else {
        // 如果后端没有返回，则本地减1（兜底逻辑）
        likeCount.value = Math.max(0, likeCount.value - 1)
      }
    } else {
      // 点赞
      const result = await likePost(props.post.postId)
      isLiked.value = true
      // 使用后端返回的最新点赞数
      if (result.likeCount !== undefined) {
        likeCount.value = result.likeCount
      } else {
        // 如果后端没有返回，则本地加1（兜底逻辑）
        likeCount.value = likeCount.value + 1
      }
    }
    emit('liked', props.post.postId)
  } catch (error) {
    console.error(`Like operation failed for post ${props.post.postId}:`, error)
    ElMessage.error('操作失败')
  } finally {
    liking.value = false
  }
}

const loadLikeStatus = async () => {
  if (!userStore.isLoggedIn) {
    // 未登录时，使用post对象中的点赞数，但不显示点赞状态
    likeCount.value = props.post.likeCount || 0
    isLiked.value = false
    return
  }

  try {
    const result = await getLikeStatus(props.post.postId)
    isLiked.value = result.isLiked || false
    // 使用API返回的点赞数，确保使用当前帖子的真实点赞数
    if (result.likeCount !== undefined) {
      likeCount.value = result.likeCount
    } else {
      // 如果API没有返回点赞数，保留post对象中的初始值
      likeCount.value = props.post.likeCount || 0
    }
  } catch (error) {
    console.error(`Failed to load like status for post ${props.post.postId}:`, error)
    // 加载失败时，使用post对象中的初始数据
    likeCount.value = props.post.likeCount || 0
    isLiked.value = false
  }
}

const handleReposted = () => {
  emit('reposted', props.post.postId)
}

// 判断是否是自己的帖子
const isOwnPost = computed(() => {
  return props.post.userId === userStore.userId
})

// 删除帖子
const handleDeletePost = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条微博吗？删除后将无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await deletePost(props.post.postId)
    ElMessage.success('删除成功')
    emit('deleted', props.post.postId)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 下拉菜单命令处理
const handleCommand = (command) => {
  if (command === 'delete') {
    handleDeletePost()
  }
}

const formatTime = (time) => {
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

const handleTopicClick = (topicName) => {
  router.push(`/topic/${encodeURIComponent(topicName)}`)
}

// 解析图片数组并转换为完整 URL
const images = computed(() => {
  if (!props.post.images) return []
  return parseImages(props.post.images)
})

// 解析原帖图片数组
const sourceImages = computed(() => {
  if (!sourcePost.value?.images) return []
  return parseImages(sourcePost.value.images)
})

const parseContent = (content) => {
  if (!content) return ''
  // Parse topics and make them clickable
  return content.replace(/#([^#]+)#/g, (match, topicName) => {
    return `<span class="topic-link" data-topic="${topicName}">${match}</span>`
  })
}

const goToProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

const loadSourcePost = async () => {
  if (!props.post.sourceId || !props.showSource) return

  loadingSource.value = true
  try {
    const post = await getSourcePost(props.post.postId)
    if (post) {
      sourcePost.value = post
      // Load source post user info
      if (post.userId) {
        try {
          sourceUser.value = await getUserInfo(post.userId)
        } catch (error) {
          console.error('Failed to load source user:', error)
        }
      }
    } else {
      sourceDeleted.value = true
    }
  } catch (error) {
    sourceDeleted.value = true
  } finally {
    loadingSource.value = false
  }
}

const getImageGridClass = (count) => {
  if (count === 1) return 'single'
  if (count <= 4) return 'grid-4'
  return 'grid-9'
}

const getNestedImageGridClass = (count) => {
  if (count === 1) return 'single'
  if (count === 2) return 'grid-2'
  return ''  // 默认使用3列网格
}

onMounted(() => {
  if (props.post.sourceId) {
    loadSourcePost()
  }
  loadLikeStatus()
})
</script>

<template>
  <div class="post-card glass-card">
    <div class="post-header">
      <el-avatar :src="user?.avatar || '/default-avatar.png'" :size="48" @click="goToProfile(post.userId)"
        class="user-avatar" />
      <div class="post-info">
        <div class="username" @click="goToProfile(post.userId)">
          {{ user?.username || '未知用户' }}
        </div>
        <div class="time">{{ formatTime(post.createTime) }}</div>
      </div>
      <el-dropdown v-if="isOwnPost" trigger="click" @command="handleCommand">
        <el-button text circle class="more-btn">
          <el-icon>
            <MoreFilled />
          </el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="delete">
              <el-icon>
                <Delete />
              </el-icon>
              <span>删除</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button v-else text circle class="more-btn" disabled>
        <el-icon>
          <MoreFilled />
        </el-icon>
      </el-button>
    </div>

    <div class="post-content">
      <!-- Repost indicator -->
      <div v-if="post.sourceId" class="repost-indicator">
        <el-icon>
          <Share />
        </el-icon> 转发了
      </div>

      <!-- Current post content -->
      <div v-if="!post.sourceId || post.content" class="current-post-content">
        <div v-if="post.content" class="text-content" v-html="parseContent(post.content)" @click="(e) => {
          if (e.target.classList.contains('topic-link')) {
            handleTopicClick(e.target.dataset.topic)
          }
        }"></div>

        <div v-if="images.length > 0" class="image-grid" :class="getImageGridClass(images.length)">
          <el-image v-for="(image, index) in images" :key="index" :src="image" fit="cover" :preview-src-list="images"
            :initial-index="index" class="post-image" loading="lazy" />
        </div>

        <div v-if="post.videoUrl" class="video-container">
          <video :src="getImageUrl(post.videoUrl)" controls preload="metadata">
            <source :src="getImageUrl(post.videoUrl)" type="video/mp4" />
            您的浏览器不支持视频播放。
          </video>
        </div>
      </div>

      <!-- Original post content (if repost) -->
      <div v-if="post.sourceId" class="source-post-container">
        <div v-if="loadingSource" class="loading-source">加载中...</div>
        <div v-else-if="sourceDeleted" class="deleted-source">
          <el-icon>
            <Warning />
          </el-icon> 原内容不可见
        </div>
        <div v-else class="nested-post">
          <div class="nested-post-header" @click="goToProfile(sourcePost.userId)">
            <span class="nested-username">@{{ sourceUser?.username || '未知用户' }}</span>
          </div>
          <div v-if="sourcePost.content" class="nested-content" v-html="parseContent(sourcePost.content)"></div>
          <div v-if="sourceImages.length > 0" class="nested-images"
            :class="getNestedImageGridClass(sourceImages.length)">
            <el-image v-for="(img, idx) in sourceImages" :key="idx" :src="img" fit="cover" class="nested-image"
              :preview-src-list="sourceImages" :initial-index="idx" loading="lazy" />
          </div>
        </div>
      </div>
    </div>

    <div class="post-actions">
      <div class="action-item" @click="handleRepost">
        <el-icon>
          <Share />
        </el-icon>
        <span>转发</span>
      </div>
      <div class="action-item" @click="handleComment">
        <el-icon>
          <ChatDotRound />
        </el-icon>
        <span>{{ showComments ? '收起' : '评论' }}</span>
      </div>
      <div class="action-item" :class="{ active: isLiked }" @click="handleLike">
        <el-icon>
          <component :is="isLiked ? 'StarFilled' : 'Star'" />
        </el-icon>
        <span>{{ likeCount > 0 ? likeCount : '赞' }}</span>
      </div>
    </div>

    <RepostDialog v-model:visible="repostDialogVisible" :post="post" :user="user" @reposted="handleReposted" />

    <div v-if="showComments" class="comments-section">
      <CommentList :post-id="post.postId" @commented="handleCommented" />
    </div>
  </div>
</template>

<style scoped>
.post-card {
  margin-bottom: 16px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-info {
  flex: 1;
}

.username {
  font-weight: 500;
  color: #f3f4f6;
  margin-bottom: 4px;
  cursor: pointer;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.username:hover {
  color: #a78bfa;
}

.user-avatar {
  cursor: pointer;
}

.time {
  font-size: 12px;
  color: #d1d5db;
  opacity: 0.9;
}

.post-content {
  margin-bottom: 16px;
}

.text-content {
  margin-bottom: 12px;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
  color: #f9fafb;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.text-content :deep(.topic-link) {
  color: #a78bfa;
  cursor: pointer;
  transition: opacity 0.2s ease;
  text-shadow: 0 0 10px rgba(167, 139, 250, 0.3);
}

.text-content :deep(.topic-link:hover) {
  opacity: 0.8;
  text-decoration: underline;
}

.image-grid {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.image-grid.single {
  grid-template-columns: 1fr;
  max-width: 100%;
}

.image-grid.grid-4 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 100%;
}

.image-grid.grid-9 {
  grid-template-columns: repeat(3, 1fr);
  max-width: 100%;
}

@media (max-width: 768px) {
  .image-grid.single {
    max-width: 100%;
  }

  .image-grid.grid-4,
  .image-grid.grid-9 {
    grid-template-columns: repeat(2, 1fr);
  }
}

.post-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease;
  overflow: hidden;
}

.post-image:hover {
  transform: scale(1.02);
}

.post-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-container {
  margin-top: 12px;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.video-container video {
  width: 100%;
  display: block;
  max-height: 500px;
}

.post-actions {
  display: flex;
  gap: 24px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.repost-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #d1d5db;
  font-size: 13px;
  margin-bottom: 12px;
  font-weight: 500;
}

.repost-indicator .el-icon {
  color: var(--primary-color);
}

.source-post-container {
  background-color: rgba(31, 41, 55, 0.5);
  border-radius: var(--border-radius-base);
  padding: 16px;
  margin-top: 12px;
  margin-bottom: 12px;
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.source-post-container:hover {
  background-color: rgba(31, 41, 55, 0.7);
  border-color: rgba(139, 92, 246, 0.3);
}

.loading-source,
.deleted-source {
  color: #d1d5db;
  font-size: 12px;
  text-align: center;
  padding: 20px;
}

.nested-post {
  margin: 0;
}

.nested-post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nested-post-header:hover .nested-username {
  color: var(--primary-color);
}

.nested-username {
  font-size: 14px;
  font-weight: 600;
  color: #f3f4f6;
  transition: color 0.2s ease;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.nested-content {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  color: #f9fafb;
  word-break: break-word;
  white-space: pre-wrap;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.nested-content :deep(.topic-link) {
  color: #a78bfa;
  cursor: pointer;
  transition: opacity 0.2s ease;
  text-shadow: 0 0 8px rgba(167, 139, 250, 0.3);
}

.nested-content :deep(.topic-link:hover) {
  opacity: 0.8;
  text-decoration: underline;
}

.nested-images {
  display: grid;
  gap: 8px;
  margin-top: 8px;
  grid-template-columns: repeat(3, 1fr);
  max-width: 100%;
}

.nested-images.single {
  grid-template-columns: 1fr;
  max-width: 300px;
}

.nested-images.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 400px;
}

.nested-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease, opacity 0.2s ease;
  overflow: hidden;
}

.nested-image:hover {
  transform: scale(1.02);
  opacity: 0.9;
}

.nested-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.current-post-content {
  margin-top: 8px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .nested-images {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  .nested-images.single {
    grid-template-columns: 1fr !important;
    max-width: 200px;
  }

  .source-post-container {
    padding: 12px;
  }

  .post-card {
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .nested-images:not(.single) {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  .image-grid.grid-9 {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>