<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getAllUsers } from '../api/user'
import { sendFriendRequest, getFriendList } from '../api/friendship'
import { useUserStore } from '../store/userStore'
import { ElAvatar, ElButton, ElEmpty, ElSkeleton, ElSkeletonItem, ElMessage, ElIcon } from 'element-plus'
import { UserFilled, ArrowRight } from '@element-plus/icons-vue'
import { getImageUrl } from '../utils/image'

const router = useRouter()
const userStore = useUserStore()
const recommendUsers = ref([])
const loading = ref(false)
const friendIds = ref(new Set())

// 获取好友列表
const loadFriendList = async () => {
  try {
    const data = await getFriendList()
    if (data && Array.isArray(data)) {
      // 将好友ID存储到Set中，方便快速查找
      friendIds.value = new Set(data.map(friend => friend.userId))
    }
  } catch (error) {
    console.error('Failed to load friend list:', error)
  }
}

// 获取推荐用户列表
const loadRecommendUsers = async () => {
  loading.value = true
  try {
    // 调用获取所有用户接口
    const data = await getAllUsers()

    if (data && Array.isArray(data)) {
      // 过滤掉当前用户自己
      recommendUsers.value = data
        .filter(user => user.userId !== userStore.userInfo?.userId)
        .slice(0, 8) // 只显示前8个用户
        .map(user => ({
          ...user,
          isFriend: friendIds.value.has(user.userId),
          requestSent: false
        }))
    } else {
      recommendUsers.value = []
    }
  } catch (error) {
    console.error('Failed to load recommend users:', error)
    recommendUsers.value = []
  } finally {
    loading.value = false
  }
}

// 默认头像URL
const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 处理用户头像路径
const getUserAvatar = (avatar) => {
  if (!avatar || avatar.trim() === '') {
    return DEFAULT_AVATAR
  }
  // 使用统一的图片URL处理函数
  return getImageUrl(avatar)
}

// 处理头像加载失败
const handleAvatarError = (event) => {
  event.target.src = DEFAULT_AVATAR
}

// 跳转到用户主页
const handleUserClick = (user) => {
  router.push(`/profile/${user.userId}`)
}

// 关注用户
const handleFollow = async (user, event) => {
  event.stopPropagation() // 阻止事件冒泡

  try {
    const params = new URLSearchParams()
    params.append('toUserId', user.userId)
    params.append('message', '你好，我想加你为好友')

    await sendFriendRequest(params)
    ElMessage.success('好友申请已发送')

    // 更新用户状态
    user.requestSent = true
  } catch (error) {
    console.error('Failed to send friend request:', error)
    ElMessage.error('发送好友申请失败，请稍后重试')
  }
}

onMounted(() => {
  loadFriendList().then(() => {
    loadRecommendUsers()
  })
})
</script>

<template>
  <div class="hot-topics-card glass-card">
    <div class="card-header">
      <div class="header-title">
        <el-icon :size="20" class="header-icon">
          <UserFilled />
        </el-icon>
        <h3>推荐关注</h3>
      </div>
      <div class="header-more" @click="router.push('/search')">
        <span>更多</span>
        <el-icon>
          <ArrowRight />
        </el-icon>
      </div>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <el-skeleton animated>
        <template #template>
          <div v-for="i in 5" :key="i" class="skeleton-item">
            <el-skeleton-item variant="circle" style="width: 40px; height: 40px; margin-right: 12px" />
            <el-skeleton-item variant="text" style="width: 100%; flex: 1" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <div v-else-if="recommendUsers.length === 0" class="empty-users">
      <el-empty description="暂无推荐用户" :image-size="60" />
    </div>

    <div v-else class="users-list">
      <div v-for="user in recommendUsers" :key="user.userId" class="user-item" @click="handleUserClick(user)">
        <el-avatar :src="getUserAvatar(user.avatar)" :size="40" class="user-avatar" @error="handleAvatarError">
          <el-icon :size="24">
            <UserFilled />
          </el-icon>
        </el-avatar>
        <div class="user-info">
          <div class="user-name">{{ user.username }}</div>
          <div class="user-bio" v-if="user.bio">{{ user.bio }}</div>
        </div>
        <el-button :type="user.isFriend ? 'info' : 'primary'" size="small" :disabled="user.isFriend || user.requestSent"
          @click="handleFollow(user, $event)" class="follow-btn">
          {{ user.isFriend ? '已关注' : (user.requestSent ? '已申请' : '关注') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 推荐关注卡片容器 */
.hot-topics-card {
  padding: 20px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
}

.header-more {
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 2px;
  transition: color 0.2s;
}

.header-more:hover {
  color: var(--primary-color);
}

/* 用户列表 */
.users-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 用户项 */
.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-item:hover {
  background: rgba(139, 92, 246, 0.08);
  transform: translateX(2px);
}

/* 用户头像 */
.user-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(139, 92, 246, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.user-item:hover .user-avatar {
  border-color: var(--primary-color);
  box-shadow: 0 2px 12px rgba(139, 92, 246, 0.3);
}

/* 用户信息 */
.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-bio {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 关注按钮 */
.follow-btn {
  flex-shrink: 0;
  padding: 5px 12px;
  font-size: 12px;
}

/* 空状态 */
.empty-users {
  padding: 24px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 加载骨架屏 */
.loading-skeleton {
  padding: 10px 0;
}

.skeleton-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
</style>


/* 卡片头部 */
.card-header {
display: flex;
justify-content: space-between;
align-items: center;
margin-bottom: 16px;
}

.header-title {
display: flex;
align-items: center;
gap: 8px;
}

.header-icon {
color: var(--danger-color);
font-size: 20px;
}

.card-header h3 {
margin: 0;
font-size: 18px;
font-weight: 600;
color: inherit;
}

.header-more {
cursor: pointer;
font-size: 14px;
color: var(--info-color);
transition: var(--transition-base);
}

.header-more:hover {
color: var(--primary-color);
}

/* 空状态样式 */
.empty-topics {
padding: 24px 0;
display: flex;
justify-content: center;
align-items: center;
}

/* 加载骨架屏 */
.loading-skeleton {
padding: 8px 0;
}

.skeleton-item {
margin-bottom: 12px;
}

/* 话题列表 */
.topics-list {
display: flex;
flex-direction: column;
gap: 12px;
}

/* 话题项 */
.topic-item {
display: flex;
align-items: flex-start;
gap: 12px;
padding: 8px;
border-radius: var(--border-radius-md);
cursor: pointer;
transition: var(--transition-base);
}

.topic-item:hover {
background-color: rgba(139, 92, 246, 0.15);
transform: translateX(4px);
}

/* 排名样式 */
.topic-rank {
width: 24px;
text-align: center;
font-weight: bold;
font-size: 14px;
line-height: 20px;
color: var(--info-color);
flex-shrink: 0;
margin-top: 2px;
}

/* 前三名特殊样式 */
.topic-rank.top-one {
color: #f56c6c;
font-size: 16px;
}

.topic-rank.top-two {
color: #e6a23c;
font-size: 15px;
}

.topic-rank.top-three {
color: #67c23a;
font-size: 15px;
}

/* 话题内容 */
.topic-content {
flex: 1;
min-width: 0;
display: flex;
flex-direction: column;
gap: 4px;
}

/* 话题名称 */
.topic-name {
font-size: 14px;
color: inherit;
font-weight: 500;
line-height: 1.4;
word-break: break-word;
}

/* 话题描述 */
.topic-desc {
font-size: 12px;
color: var(--info-color);
line-height: 1.3;
word-break: break-word;
}

/* 热度标签 */
.heat-tag {
margin-left: auto;
background-color: rgba(245, 108, 108, 0.2);
color: var(--danger-color);
border: none;
font-size: 12px;
font-weight: 500;
padding: 2px 8px;
border-radius: var(--border-radius-full);
flex-shrink: 0;
margin-top: 2px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
.hot-topics-card {
padding: 12px;
}

.topic-name {
font-size: 13px;
}

.heat-tag {
font-size: 11px;
}
}
