<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../store/userStore'
import { getCommentsByPostId, addComment } from '../api/comment'
import { getUserInfo } from '../api/user'
import CommentItem from './CommentItem.vue'
import EmojiPicker from './EmojiPicker.vue'
import { ElInput, ElButton, ElEmpty, ElMessage, ElPagination, ElRadioGroup, ElRadioButton, ElAutocomplete } from 'element-plus'

const props = defineProps({
  postId: {
    type: Number,
    required: true
  }
})

const userStore = useUserStore()
const comments = ref([])
const users = ref({}) // Cache user info
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const totalComments = ref(0)

// 排序参数
const sortBy = ref('newest') // 'newest' 或 'oldest'

// @提及相关
const mentionUsers = ref([]) // @提及的用户列表
const showUserSuggestions = ref(false)
const userSuggestions = ref([])
const commentInputElement = ref(null)

// 计算分页后的评论列表
const paginatedComments = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return comments.value.slice(start, end)
})

const loadComments = async () => {
  loading.value = true
  try {
    const result = await getCommentsByPostId(props.postId)
    let commentList = result || []

    // 按日期排序
    if (sortBy.value === 'newest') {
      commentList.sort((a, b) => new Date(b.comment.createTime) - new Date(a.comment.createTime))
    } else {
      commentList.sort((a, b) => new Date(a.comment.createTime) - new Date(b.comment.createTime))
    }

    comments.value = commentList
    totalComments.value = commentList.length
    currentPage.value = 1 // 重置到第一页

    // Load user info for all comments
    const userIds = new Set()
    const collectUserIds = (commentTree) => {
      if (commentTree.comment && commentTree.comment.userId) {
        userIds.add(commentTree.comment.userId)
      }
      if (commentTree.replies) {
        commentTree.replies.forEach(collectUserIds)
      }
    }
    comments.value.forEach(collectUserIds)

    for (const userId of userIds) {
      if (!users.value[userId]) {
        try {
          const userInfo = await getUserInfo(userId)
          users.value[userId] = userInfo
        } catch (error) {
          console.error(`Failed to load user ${userId}:`, error)
        }
      }
    }
  } catch (error) {
    console.error('Failed to load comments:', error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  submitting.value = true
  try {
    await addComment(props.postId, commentContent.value, null)
    ElMessage.success('评论成功')
    commentContent.value = ''
    mentionUsers.value = []
    loadComments()
    emit('commented')
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const handleReply = async (parentId, content) => {
  if (!content.trim()) {
    return
  }

  try {
    await addComment(props.postId, content, parentId)
    ElMessage.success('回复成功')
    loadComments()
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

// 排序方式改变
const handleSortChange = () => {
  currentPage.value = 1 // 重置到第一页
  loadComments()
}

// 页码改变
const handlePageChange = (page) => {
  currentPage.value = page
}

// 处理输入框内容变化，支持@提及
const handleCommentInput = (event) => {
  const text = commentContent.value
  const cursorPos = event.target.selectionStart || text.length
  const textBeforeCursor = text.substring(0, cursorPos)
  const lastAtIndex = textBeforeCursor.lastIndexOf('@')

  if (lastAtIndex !== -1) {
    const searchText = textBeforeCursor.substring(lastAtIndex + 1)
    // 简单的@提及检测：@后跟字符且不包含空格
    if (searchText && !searchText.includes(' ')) {
      // 这里可以搜索用户，现在显示缓存的用户
      userSuggestions.value = Object.values(users.value)
        .filter(u => u?.username?.includes(searchText))
        .slice(0, 5) // 最多显示5个建议
      showUserSuggestions.value = userSuggestions.value.length > 0
    } else {
      showUserSuggestions.value = false
    }
  } else {
    showUserSuggestions.value = false
  }
}

// 选择@用户
const selectMentionUser = (user) => {
  const text = commentContent.value
  const lastAtIndex = text.lastIndexOf('@')
  if (lastAtIndex !== -1) {
    const beforeAt = text.substring(0, lastAtIndex)
    const newText = beforeAt + '@' + user.username + ' '
    commentContent.value = newText
    mentionUsers.value.push(user.userId)
    showUserSuggestions.value = false
  }
}

const emit = defineEmits(['commented'])

onMounted(() => {
  loadComments()
})
</script>

<template>
  <div class="comment-list">
    <div class="comment-input-section">
      <div class="input-container">
        <el-input ref="commentInputElement" v-model="commentContent" type="textarea" :rows="3"
          placeholder="写评论... (可以使用 @ 来提及用户)" :maxlength="500" show-word-limit @input="handleCommentInput" />
        <!-- 用户提议下拉框 -->
        <div v-if="showUserSuggestions" class="user-suggestions">
          <div v-for="user in userSuggestions" :key="user.userId" class="suggestion-item"
            @click="selectMentionUser(user)">
            <img :src="user.avatar || '/default-avatar.png'" :alt="user.username" class="suggestion-avatar" />
            <span>{{ user.username }}</span>
          </div>
        </div>
      </div>
      <div class="input-actions">
        <emoji-picker v-model="commentContent" />
        <el-button type="primary" size="small" :loading="submitting" :disabled="!userStore.isLoggedIn"
          @click="handleSubmit">
          评论
        </el-button>
      </div>
    </div>

    <!-- 评论统计和排序 -->
    <div v-if="comments.length > 0" class="comment-header">
      <span class="comment-count">共 {{ totalComments }} 条评论</span>
      <div class="sort-options">
        <el-radio-group v-model="sortBy" @change="handleSortChange" size="small">
          <el-radio-button label="newest">最新</el-radio-button>
          <el-radio-button label="oldest">最早</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-loading="loading" class="comments-container">
      <div v-if="comments.length === 0 && !loading" class="empty-comments">
        <el-empty description="暂无评论" :image-size="80" />
      </div>

      <div v-else class="comments-wrapper">
        <CommentItem v-for="commentTree in paginatedComments" :key="commentTree.comment.commentId"
          :comment="commentTree.comment" :user="users[commentTree.comment.userId]" :replies="commentTree.replies"
          :users="users" @reply="handleReply" />
      </div>

      <!-- 分页 -->
      <div v-if="totalComments > pageSize" class="pagination-section">
        <el-pagination :current-page="currentPage" :page-size="pageSize" :total="totalComments"
          @current-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-list {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.comment-input-section {
  margin-bottom: 20px;
}

.input-container {
  position: relative;
  margin-bottom: 8px;
}

.user-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: rgba(139, 92, 246, 0.1);
  }
}

.suggestion-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-count {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.sort-options {
  display: flex;
  gap: 10px;
}

.comments-container {
  min-height: 100px;
}

.comments-wrapper {
  margin-bottom: 20px;
}

.empty-comments {
  padding: 40px 0;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
}
</style>
