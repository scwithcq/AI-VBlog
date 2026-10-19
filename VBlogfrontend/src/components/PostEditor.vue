<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '../store/userStore'
import { publishPost } from '../api/post'
import { uploadImage, uploadVideo } from '../api/file'
import { ElInput, ElButton, ElUpload, ElMessage, ElIcon, ElAutocomplete, ElTag, ElAvatar } from 'element-plus'
import { Picture, VideoCamera, Delete, PriceTag } from '@element-plus/icons-vue'
import { getImageUrl } from '../utils/image'

const userStore = useUserStore()

const content = ref('')
const images = ref([])
const videoUrl = ref('')
const uploading = ref(false)
const imageUploading = ref(false)
const videoUploading = ref(false)
const topicSuggestions = ref([])
const showTopicSuggestions = ref(false)

const MAX_CONTENT_LENGTH = 2000
const MAX_IMAGES = 9

// 提取话题建议
const extractTopics = () => {
  const topicPattern = /#([^#]+)#/g
  const topics = []
  let match
  while ((match = topicPattern.exec(content.value)) !== null) {
    if (!topics.includes(match[1])) {
      topics.push(match[1])
    }
  }
  topicSuggestions.value = topics
  return topics
}

const handleImageUpload = async (file) => {
  if (imageUploading.value || images.value.length >= MAX_IMAGES) {
    ElMessage.warning(`最多只能上传${MAX_IMAGES}张图片`)
    return false
  }

  imageUploading.value = true
  try {
    const result = await uploadImage(file.raw)
    images.value.push(result.url)
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  } finally {
    imageUploading.value = false
  }
  return false // Prevent auto upload
}

const handleVideoUpload = async (file) => {
  const videoFile = file.raw

  // 验证文件大小（最大100MB）
  const maxSize = 100 * 1024 * 1024
  if (videoFile.size > maxSize) {
    ElMessage.warning('视频文件不能超过100MB')
    return false
  }

  // 验证文件类型
  const allowedTypes = ['video/mp4', 'video/quicktime', 'video/x-msvideo', 'video/avi', 'video/webm']
  if (!allowedTypes.includes(videoFile.type)) {
    ElMessage.warning('不支持的视频格式，请上传 MP4、MOV 或 AVI 格式')
    return false
  }

  videoUploading.value = true
  ElMessage.info('视频上传中，请稍候...')

  try {
    const result = await uploadVideo(videoFile)
    videoUrl.value = result.url
    ElMessage.success('视频上传成功')
  } catch (error) {
    console.error('视频上传失败:', error)
    ElMessage.error(error.message || '视频上传失败，请重试')
  } finally {
    videoUploading.value = false
  }
  return false // Prevent auto upload
}

const removeImage = (index) => {
  images.value.splice(index, 1)
}

const removeVideo = () => {
  videoUrl.value = ''
}

const handlePublish = async () => {
  if (uploading.value || imageUploading.value || videoUploading.value) return
  if (!content.value.trim() && images.value.length === 0 && !videoUrl.value) {
    ElMessage.warning('请输入内容或上传图片/视频')
    return
  }

  if (content.value.length > MAX_CONTENT_LENGTH) {
    ElMessage.warning(`内容不能超过${MAX_CONTENT_LENGTH}字`)
    return
  }

  uploading.value = true
  try {
    // 根据 API 文档，发布帖子需要发送以下参数：
    // - content: 帖子内容（字符串）
    // - images: 图片URL数组（JSON字符串格式）
    // - videoUrl: 视频URL（字符串）
    // - sourceId: 转发源帖子ID（可选）

    const postData = {
      content: content.value || '',
      images: images.value.length > 0 ? images.value : [],
      videoUrl: videoUrl.value || ''
    }

    // 调用发布帖子接口
    await publishPost(postData)
    ElMessage.success('发布成功')

    // 重置表单
    content.value = ''
    images.value = []
    videoUrl.value = ''

    // 触发事件刷新帖子列表
    emit('published')
  } catch (error) {
    // 错误已在 request.js 的拦截器中处理
    console.error('发布失败:', error)
  } finally {
    uploading.value = false
  }
}

const emit = defineEmits(['published'])

const remainingChars = computed(() => MAX_CONTENT_LENGTH - content.value.length)
</script setup>

<template>
  <div class="post-editor glass-card">
    <div class="composer-heading">
      <div><span class="composer-kicker">CREATE A POST</span><h2>分享此刻</h2></div>
      <span class="composer-hint">你的新鲜事，值得被听见</span>
    </div>
    <div class="editor-header">
      <el-avatar :src="userStore.userInfo?.avatar" :size="48" class="user-avatar" />
      <div class="editor-input-wrapper">
        <el-input v-model="content" type="textarea" :rows="3" placeholder="这一刻，你想和大家聊些什么？" :maxlength="MAX_CONTENT_LENGTH"
          class="content-input" @input="extractTopics" resize="none" />
      </div>
    </div>

    <!-- 话题提示 -->
    <div v-if="topicSuggestions.length > 0" class="topic-hints">
      <el-tag v-for="topic in topicSuggestions" :key="topic" closable class="topic-hint" effect="plain" round>
        #{{ topic }}#
      </el-tag>
    </div>

    <!-- 预览区域 -->
    <div v-if="images.length > 0" class="media-preview">
      <div v-for="(image, index) in images" :key="index" class="image-item">
        <img :src="getImageUrl(image)" alt="preview" />
        <div class="remove-mask" @click="removeImage(index)">
          <el-icon>
            <Delete />
          </el-icon>
        </div>
      </div>
    </div>

    <div v-if="videoUrl" class="media-preview">
      <div class="video-item">
        <video :src="getImageUrl(videoUrl)" controls preload="metadata">
          <source :src="getImageUrl(videoUrl)" type="video/mp4" />
          您的浏览器不支持视频播放。
        </video>
        <el-button type="danger" circle class="remove-video-btn" @click="removeVideo">
          <el-icon>
            <Delete />
          </el-icon>
        </el-button>
      </div>
    </div>

    <div class="editor-footer">
      <div class="toolbar">
        <el-upload :auto-upload="false" :on-change="handleImageUpload" :show-file-list="false" accept="image/*"
          :disabled="imageUploading || images.length >= MAX_IMAGES">
          <el-button text class="tool-btn" :disabled="images.length >= MAX_IMAGES" title="上传图片">
            <el-icon :size="18">
              <Picture />
            </el-icon><span>图片</span>
          </el-button>
        </el-upload>

        <el-upload :auto-upload="false" :on-change="handleVideoUpload" :show-file-list="false"
          accept="video/mp4,video/quicktime,video/x-msvideo,video/avi,video/webm" :disabled="videoUploading || !!videoUrl">
          <el-button text class="tool-btn" :disabled="!!videoUrl" :title="videoUrl ? '已上传视频' : '上传视频 (最大100MB)'">
            <el-icon :size="18" :class="{ 'is-loading': videoUploading }">
              <VideoCamera />
            </el-icon><span>视频</span>
          </el-button>
        </el-upload>

        <el-button text class="tool-btn" title="插入话题" @click="content += '#话题#'">
          <el-icon :size="18">
            <PriceTag />
          </el-icon><span>话题</span>
        </el-button>
      </div>

      <div class="publish-section">
        <span class="char-count" :class="{ warning: remainingChars < 50 }">
          {{ content.length }}/{{ MAX_CONTENT_LENGTH }}
        </span>
        <el-button type="primary" round :loading="uploading" :disabled="imageUploading || videoUploading" @click="handlePublish" class="publish-btn">
          发布
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-editor {
  padding: 24px 26px;
  margin-bottom: 24px;
  border-color: rgba(255, 165, 135, 0.2);
  background: linear-gradient(145deg, rgba(35, 40, 60, 0.98), rgba(27, 34, 52, 0.98));
}

.composer-heading { display: flex; align-items: end; justify-content: space-between; margin-bottom: 22px; }
.composer-kicker { color: #ffa389; font-size: 10px; font-weight: 800; letter-spacing: .16em; }
.composer-heading h2 { margin-top: 3px; font-size: 20px; line-height: 1.2; color: var(--text-main); }
.composer-hint { color: var(--text-secondary); font-size: 12px; }

.editor-header {
  display: flex;
  gap: 16px;
  margin-bottom: 18px;
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.editor-input-wrapper {
  flex: 1;
}

/* 深度选择器修改 Element Plus 输入框样式 */
:deep(.el-textarea__inner) {
  background: rgba(15, 21, 34, 0.38) !important;
  box-shadow: none !important;
  padding: 14px 16px;
  font-size: 16px;
  border: 1px solid var(--glass-border) !important;
  border-radius: 14px;
  resize: none;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: none !important;
}

.topic-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-left: 64px;
  margin-bottom: 12px;
}

.media-preview {
  margin-left: 64px;
  margin-bottom: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-item:hover img {
  transform: scale(1.05);
}

.remove-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
  font-size: 20px;
}

.image-item:hover .remove-mask {
  opacity: 1;
}

.video-item {
  position: relative;
  max-width: 500px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  background: #000;
}

.video-item video {
  width: 100%;
  display: block;
  max-height: 360px;
}

.remove-video-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  color: #fff;
  backdrop-filter: blur(4px);
  transition: all 0.3s;
}

.remove-video-btn:hover {
  background: rgba(220, 38, 38, 0.9);
  transform: scale(1.1);
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--glass-border);
  margin-left: 64px;
}

.toolbar {
  display: flex;
  gap: 4px;
}

.tool-btn {
  height: 36px;
  padding: 0 10px;
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  transition: background 0.2s, color 0.2s;
}

.tool-btn:hover {
  background: rgba(255, 121, 100, 0.1);
  color: #ffad94;
}

.publish-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.char-count {
  font-size: 12px;
  color: #909399;
}

.char-count.warning {
  color: #f56c6c;
}

.publish-btn {
  padding: 10px 28px;
  font-size: 14px;
  letter-spacing: .03em;
}

@media (max-width: 600px) {
  .post-editor { padding: 20px 16px; }
  .composer-hint { display: none; }
  .editor-header { gap: 10px; }
  .editor-footer, .topic-hints, .media-preview { margin-left: 0; }
  .tool-btn { padding: 0 7px; font-size: 12px; }
  .publish-section { gap: 9px; }
  .publish-btn { padding: 9px 17px; }
}
</style>
