<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllUsers } from '../api/user'
import { sendFriendRequest, getFriendList } from '../api/friendship'
import { useUserStore } from '../store/userStore'
import { ElMessage } from 'element-plus'
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
const DEFAULT_AVATAR = '/default-avatar.jpg'

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

// 发送好友申请
const handleAddFriend = async (user, event) => {
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
        <h3>推荐好友</h3>
      </div>
      <button type="button" class="header-more" @click="router.push('/search')">
        <span>更多</span>
        <el-icon>
          <ArrowRight />
        </el-icon>
      </button>
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
      <div v-for="user in recommendUsers" :key="user.userId" class="user-item">
        <el-avatar :src="getUserAvatar(user.avatar)" :size="40" class="user-avatar" @error="handleAvatarError">
          <el-icon :size="24">
            <UserFilled />
          </el-icon>
        </el-avatar>
        <button type="button" class="user-info" @click="handleUserClick(user)">
          <div class="user-name">{{ user.username }}</div>
          <div class="user-bio" v-if="user.bio">{{ user.bio }}</div>
        </button>
        <el-button :type="user.isFriend ? 'info' : 'primary'" size="small" :disabled="user.isFriend || user.requestSent"
          @click="handleAddFriend(user, $event)" class="follow-btn">
          {{ user.isFriend ? '已是好友' : (user.requestSent ? '已申请' : '加好友') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 推荐好友卡片 */
.hot-topics-card {
  padding: 22px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--glass-border);
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
  padding: 5px 0 5px 8px;
  border: 0;
  background: transparent;
  box-shadow: none;
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
  padding: 10px 8px;
  border-radius: 12px;
  transition: all 0.2s;
}

.user-item:hover {
  background: rgba(255, 121, 100, 0.08);
}

/* 用户头像 */
.user-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(255, 121, 100, 0.16);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.user-item:hover .user-avatar {
  border-color: var(--primary-color);
  box-shadow: 0 2px 12px rgba(255, 121, 100, 0.22);
}

/* 用户信息 */
.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  box-shadow: none;
  backdrop-filter: none;
  cursor: pointer;
}
.user-info:hover { background: transparent; border: 0; }

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
