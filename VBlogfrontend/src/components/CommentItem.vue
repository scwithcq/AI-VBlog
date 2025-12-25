<script setup>
import { ref, computed } from 'vue'
import { ElAvatar, ElButton, ElInput, ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/userStore'
import { useRouter } from 'vue-router'
import { deleteComment, likeComment, unlikeComment, getCommentLikeStatus } from '../api/comment'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  user: {
    type: Object,
    default: null
  },
  replies: {
    type: Array,
    default: () => []
  },
  users: {
    type: Object,
    default: () => ({})
  }
})

const userStore = useUserStore()
const router = useRouter()
const showReplyInput = ref(false)
const replyContent = ref('')
const isLiked = ref(false)
const likeCount = ref(props.comment.likeCount || 0)
const loadingLike = ref(false)
const loadingDelete = ref(false)

const emit = defineEmits(['reply', 'delete', 'like'])

// 计算是否为当前用户的评论
const isOwnComment = computed(() => {
  return userStore.userId === props.comment.userId
})

const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const commentTime = new Date(time)
  const diff = now - commentTime
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return commentTime.toLocaleDateString('zh-CN')
}

const handleReply = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  showReplyInput.value = true
}

const submitReply = () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  emit('reply', props.comment.commentId, replyContent.value)
  replyContent.value = ''
  showReplyInput.value = false
}

const cancelReply = () => {
  replyContent.value = ''
  showReplyInput.value = false
}

const goToProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  loadingLike.value = true
  try {
    if (isLiked.value) {
      // 取消点赞
      await unlikeComment(props.comment.commentId)
      isLiked.value = false
      likeCount.value--
    } else {
      // 点赞
      await likeComment(props.comment.commentId)
      isLiked.value = true
      likeCount.value++
    }
    emit('like', props.comment.commentId, isLiked.value)
  } catch (error) {
    ElMessage.error(isLiked.value ? '取消点赞失败' : '点赞失败')
  } finally {
    loadingLike.value = false
  }
}

const handleDelete = async () => {
  ElMessageBox.confirm('确定删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    loadingDelete.value = true
    try {
      await deleteComment(props.comment.commentId)
      ElMessage.success('删除成功')
      emit('delete', props.comment.commentId)
    } catch (error) {
      ElMessage.error('删除失败')
    } finally {
      loadingDelete.value = false
    }
  }).catch(() => {
    // 用户取消删除
  })
}
</script>

<template>
  <div class="comment-item">
    <div class="comment-main">
      <el-avatar :src="user?.avatar || '/default-avatar.png'" :size="32" @click="goToProfile(comment.userId)"
        style="cursor: pointer" />
      <div class="comment-content">
        <div class="comment-header">
          <span class="username" @click="goToProfile(comment.userId)" style="cursor: pointer">
            {{ user?.username || '未知用户' }}
          </span>
          <span class="time">{{ formatTime(comment.createTime) }}</span>
        </div>
        <div class="comment-text">{{ comment.content }}</div>
        <div class="comment-actions">
          <el-button type="text" size="small" :class="{ liked: isLiked }" :loading="loadingLike" @click="handleLike">
            {{ isLiked ? '👍 已赞' : '👍 赞' }}{{ likeCount > 0 ? `(${likeCount})` : '' }}
          </el-button>
          <el-button type="text" size="small" @click="handleReply">回复</el-button>
          <el-button v-if="isOwnComment" type="text" size="small" :loading="loadingDelete" @click="handleDelete">
            删除
          </el-button>
        </div>

        <!-- Reply input -->
        <div v-if="showReplyInput" class="reply-input">
          <el-input v-model="replyContent" type="textarea" :rows="2" placeholder="写回复..." :maxlength="500" />
          <div class="reply-actions">
            <el-button size="small" @click="cancelReply">取消</el-button>
            <el-button type="primary" size="small" @click="submitReply">回复</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Nested replies -->
    <div v-if="replies && replies.length > 0" class="replies">
      <CommentItem v-for="replyTree in replies" :key="replyTree.comment.commentId" :comment="replyTree.comment"
        :user="users[replyTree.comment.userId]" :replies="replyTree.replies || []" :users="users"
        @reply="(parentId, content) => emit('reply', parentId, content)"
        @delete="(commentId) => emit('delete', commentId)"
        @like="(commentId, liked) => emit('like', commentId, liked)" />
    </div>
  </div>
</template>

<style scoped>
.comment-item {
  margin-bottom: 16px;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.username {
  font-weight: 500;
  color: #409eff;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 8px;
  word-break: break-word;
  white-space: pre-wrap;
}

.comment-actions {
  margin-top: 4px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-actions :deep(.el-button) {
  padding: 0 6px;
  font-size: 12px;
}

.comment-actions :deep(.el-button--text):not(.liked) {
  color: #909399;
}

.comment-actions :deep(.el-button--text.liked) {
  color: #409eff;
  font-weight: 500;
}

.reply-input {
  margin-top: 12px;
  padding: 12px;
  background-color: rgba(31, 41, 55, 0.4);
  border-radius: var(--border-radius-base);
  border: 1px solid var(--glass-border);
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.replies {
  margin-left: 44px;
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #ebeef5;
}
</style>
