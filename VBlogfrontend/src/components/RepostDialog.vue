<script setup>
import { ref, computed } from 'vue'
import { ElDialog, ElInput, ElButton, ElMessage, ElAvatar } from 'element-plus'
import { repost } from '../api/post'
import { useUserStore } from '../store/userStore'
import { parseImages } from '../utils/image'

const userStore = useUserStore()

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  post: {
    type: Object,
    required: true
  },
  user: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'reposted'])

const repostContent = ref('')
const loading = ref(false)

const MAX_CONTENT_LENGTH = 2000

// 处理原帖图片数组
const postImages = computed(() => {
  if (!props.post?.images) return []
  return parseImages(props.post.images)
})

const handleRepost = async () => {
  if (repostContent.value.length > MAX_CONTENT_LENGTH) {
    ElMessage.warning(`转发内容不能超过${MAX_CONTENT_LENGTH}字`)
    return
  }

  loading.value = true
  try {
    await repost(props.post.postId, repostContent.value)
    ElMessage.success('转发成功！')
    emit('reposted')
    handleClose()
  } catch (error) {
    ElMessage.error('转发失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  repostContent.value = ''
  emit('update:visible', false)
}

const remainingChars = computed(() => MAX_CONTENT_LENGTH - repostContent.value.length)

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const getImageGridClass = (count) => {
  if (count === 1) return 'single'
  if (count === 2) return 'grid-2'
  return ''  // 默认使用3列网格
}
</script>

<template>
  <el-dialog v-model="dialogVisible" width="750px" :show-close="false" class="repost-dialog" :append-to-body="true"
    :lock-scroll="true" :modal="true" :close-on-click-modal="false" :close-on-press-escape="true" :z-index="3000"
    destroy-on-close @close="handleClose">
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <span class="dialog-title">✨ 转发微博</span>
          <span class="dialog-subtitle">分享你的想法给大家</span>
        </div>
        <el-button text @click="handleClose" class="close-btn">
          <span class="close-icon">✕</span>
        </el-button>
      </div>
    </template>

    <div class="repost-dialog-content">
      <!-- 当前用户信息 -->
      <div class="current-user-info">
        <el-avatar :src="userStore.userInfo?.avatar" :size="40" />
        <span class="username">{{ userStore.userInfo?.username }}</span>
      </div>

      <!-- 转发输入区域 -->
      <div class="repost-input-area">
        <el-input v-model="repostContent" type="textarea" :rows="6" placeholder="说说你的想法吧...可以@好友或添加话题#"
          :maxlength="MAX_CONTENT_LENGTH" class="repost-textarea" resize="none"
          @focus="$event.target.style.borderColor = 'var(--primary-color)'"
          @blur="$event.target.style.borderColor = ''" />
        <div class="input-footer">
          <div class="input-tools">
            <span class="tool-item" title="表情">😊</span>
            <span class="tool-item" title="图片">🖼️</span>
            <span class="tool-item" title="话题">#</span>
          </div>
          <div class="char-count" :class="{ warning: remainingChars < 100, danger: remainingChars < 0 }">
            <span v-if="remainingChars >= 0">还可输入 {{ remainingChars }} 字</span>
            <span v-else style="color: var(--danger-color);">超出 {{ Math.abs(remainingChars) }} 字</span>
          </div>
        </div>
      </div>

      <!-- 原始微博预览 -->
      <div class="original-post-preview">
        <div class="preview-header">
          <span class="preview-label">📌 原微博</span>
        </div>
        <div class="original-post-content">
          <div class="post-user">
            <el-avatar :src="user?.avatar" :size="36" />
            <div class="user-info">
              <div class="user-name">{{ user?.username || '未知用户' }}</div>
              <div class="post-time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
          <div class="post-text">{{ post.content }}</div>
          <div v-if="postImages.length > 0" class="post-images" :class="getImageGridClass(postImages.length)">
            <template v-if="postImages.length <= 9">
              <img v-for="(img, index) in postImages" :key="index" :src="img" class="post-image" alt="图片" />
            </template>
            <template v-else>
              <img v-for="(img, index) in postImages.slice(0, 8)" :key="index" :src="img" class="post-image" alt="图片" />
              <div class="more-images">
                +{{ postImages.length - 8 }}
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large" class="cancel-btn">
          取消
        </el-button>
        <el-button type="primary" :loading="loading" @click="handleRepost" size="large" class="repost-btn"
          :disabled="remainingChars < 0">
          <span v-if="!loading">🚀 立即转发</span>
          <span v-else>转发中...</span>
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 对话框容器顶层悬浮 */
.repost-dialog :deep(.el-overlay) {
  z-index: 3000 !important;
  backdrop-filter: blur(8px);
  background-color: rgba(0, 0, 0, 0.7) !important;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes floatDialog {

  0%,
  100% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-5px);
  }
}

.repost-dialog :deep(.el-dialog) {
  border-radius: 20px;
  background: var(--glass-card-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.7),
    0 0 0 1px rgba(139, 92, 246, 0.2),
    0 0 60px rgba(139, 92, 246, 0.15);
  animation: slideUp 0.4s cubic-bezier(0.34, 1.56, 0.64, 1),
    floatDialog 3s ease-in-out 0.5s infinite;
  z-index: 3001 !important;
  position: relative;
}

.repost-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.repost-dialog :deep(.el-dialog__body) {
  padding: 24px 32px;
  max-height: 70vh;
  overflow-y: auto;
}

.repost-dialog :deep(.el-dialog__footer) {
  padding: 20px 32px;
  border-top: 1px solid var(--glass-border);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid var(--glass-border);
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.05), rgba(139, 92, 246, 0.02));
  position: relative;
  z-index: 1;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(139, 92, 246, 0.5),
    0 0 20px rgba(139, 92, 246, 0.3);
  animation: titleGlow 2s ease-in-out infinite alternate;
}

@keyframes titleGlow {
  from {
    text-shadow: 0 2px 8px rgba(139, 92, 246, 0.2);
  }

  to {
    text-shadow: 0 2px 12px rgba(139, 92, 246, 0.4);
  }
}

.dialog-subtitle {
  font-size: 13px;
  color: #d1d5db;
  opacity: 0.9;
}

.close-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  color: #d1d5db;
}

.close-btn:hover {
  background: rgba(139, 92, 246, 0.1);
  color: var(--primary-color);
  transform: rotate(90deg);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.close-icon {
  font-size: 20px;
  font-weight: 300;
}

.repost-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.current-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(139, 92, 246, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(139, 92, 246, 0.1);
}

.current-user-info .username {
  font-weight: 600;
  color: #ffffff;
  font-size: 15px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.repost-input-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.repost-textarea :deep(.el-textarea__inner) {
  border-radius: 16px;
  background: rgba(31, 41, 55, 0.6);
  border: 2px solid rgba(139, 92, 246, 0.2);
  padding: 16px;
  font-size: 15px;
  line-height: 1.6;
  color: #ffffff;
  transition: all 0.3s;
  resize: none;
}

.repost-textarea :deep(.el-textarea__inner:focus) {
  background: rgba(31, 41, 55, 0.5);
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15),
    0 0 20px rgba(139, 92, 246, 0.2);
  animation: inputPulse 2s ease-in-out infinite;
}

@keyframes inputPulse {

  0%,
  100% {
    box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15),
      0 0 20px rgba(139, 92, 246, 0.2);
  }

  50% {
    box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.2),
      0 0 30px rgba(139, 92, 246, 0.3);
  }
}

.repost-textarea :deep(.el-textarea__inner::placeholder) {
  color: #9ca3af;
  opacity: 0.8;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px;
}

.input-tools {
  display: flex;
  gap: 16px;
}

.tool-item {
  cursor: pointer;
  font-size: 20px;
  transition: all 0.3s;
  opacity: 0.6;
}

.tool-item:hover {
  opacity: 1;
  transform: scale(1.2);
}

.char-count {
  font-size: 13px;
  color: #d1d5db;
  font-weight: 500;
  transition: color 0.3s;
}

.char-count.warning {
  color: var(--warning-color);
}

.char-count.danger {
  color: var(--danger-color);
  font-weight: 600;
}

.original-post-preview {
  background: rgba(31, 41, 55, 0.4);
  border-radius: 16px;
  border: 1px solid rgba(139, 92, 246, 0.15);
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.original-post-preview:hover {
  border-color: rgba(139, 92, 246, 0.3);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.preview-header {
  padding: 12px 16px;
  background: rgba(139, 92, 246, 0.1);
  border-bottom: 1px solid rgba(139, 92, 246, 0.15);
}

.preview-label {
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.original-post-content {
  padding: 16px;
}

.post-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.post-time {
  font-size: 12px;
  color: #d1d5db;
  opacity: 0.8;
}

.post-text {
  font-size: 14px;
  line-height: 1.6;
  color: #f3f4f6;
  margin-bottom: 12px;
  word-break: break-word;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.post-images {
  display: grid;
  gap: 8px;
  margin-top: 8px;
  grid-template-columns: repeat(3, 1fr);
  max-width: 100%;
}

.post-images.single {
  grid-template-columns: 1fr;
  max-width: 250px;
}

.post-images.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 300px;
}

.post-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid rgba(139, 92, 246, 0.2);
  transition: all 0.2s ease;
  cursor: pointer;
}

.post-image:hover {
  transform: scale(1.05);
  border-color: var(--primary-color);
}

.more-images {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  background: rgba(139, 92, 246, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--text-main);
  font-size: 16px;
  border: 1px solid rgba(139, 92, 246, 0.3);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  min-width: 100px;
  border-radius: 12px;
  border: 1px solid rgba(139, 92, 246, 0.3);
  background: transparent;
  color: var(--text-main);
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: rgba(139, 92, 246, 0.1);
  border-color: var(--primary-color);
}

.repost-btn {
  min-width: 140px;
  border-radius: 12px;
  background: var(--primary-gradient);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
  transition: all 0.3s;
}

.repost-btn:hover:not(:disabled) {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 8px 30px rgba(139, 92, 246, 0.7),
    0 0 0 2px rgba(139, 92, 246, 0.2);
}

.repost-btn:active:not(:disabled) {
  transform: translateY(-1px) scale(0.98);
  box-shadow: 0 4px 15px rgba(139, 92, 246, 0.5);
}

.repost-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 滚动条样式 */
.repost-dialog-content::-webkit-scrollbar {
  width: 6px;
}

.repost-dialog-content::-webkit-scrollbar-track {
  background: rgba(139, 92, 246, 0.05);
  border-radius: 3px;
}

.repost-dialog-content::-webkit-scrollbar-thumb {
  background: rgba(139, 92, 246, 0.3);
  border-radius: 3px;
}

.repost-dialog-content::-webkit-scrollbar-thumb:hover {
  background: rgba(139, 92, 246, 0.5);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .repost-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }

  .dialog-header,
  .repost-dialog :deep(.el-dialog__body),
  .repost-dialog :deep(.el-dialog__footer) {
    padding: 16px 20px;
  }

  .post-images {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  .post-images.single {
    grid-template-columns: 1fr !important;
    max-width: 200px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .repost-textarea :deep(.el-textarea__inner) {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .repost-dialog :deep(.el-dialog) {
    width: 95% !important;
  }

  .input-tools {
    gap: 12px;
  }

  .tool-item {
    font-size: 18px;
  }
}
</style>
