<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../store/userStore'
import { followUser, unfollowUser, getFollowStatus } from '../api/follow'
import { ElButton, ElMessage } from 'element-plus'

const props = defineProps({
  targetUserId: {
    type: Number,
    required: true
  }
})

const userStore = useUserStore()
const isFollowing = ref(false)
const isFriend = ref(false)
const loading = ref(false)
const checking = ref(false)
const isHovering = ref(false)

const emit = defineEmits(['followed', 'unfollowed'])

// 判断是否为当前用户
const isCurrentUser = computed(() => {
  return userStore.isLoggedIn && props.targetUserId === userStore.userId
})

// 判断是否应该显示按钮
const shouldShowButton = computed(() => {
  return userStore.isLoggedIn && !isCurrentUser.value
})

// 按钮显示文本
const buttonText = computed(() => {
  if (loading.value || checking.value) {
    return isFollowing.value ? '已关注' : '关注'
  }
  if (isFriend.value) return '好友·互相关注'
  if (isFollowing.value && isHovering.value) {
    return '取消关注'
  }
  return isFollowing.value ? '已关注' : '关注'
})

const onHover = () => {
  isHovering.value = true
}

const onLeave = () => {
  isHovering.value = false
}

const loadFollowStatus = async () => {
  if (!shouldShowButton.value) {
    return
  }

  checking.value = true
  try {
    const result = await getFollowStatus(props.targetUserId)
    // 兼容多种响应格式
    if (typeof result === 'boolean') {
      isFollowing.value = result
    } else {
      isFollowing.value = result.isFollowing || false
      isFriend.value = result.isFriend || false
    }
  } catch (error) {
    console.error('加载关注状态失败:', error)
    // 静默失败，不影响用户体验
  } finally {
    checking.value = false
  }
}

const handleFollow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  if (isCurrentUser.value) {
    ElMessage.warning('不能关注自己')
    return
  }

  const previousState = isFollowing.value
  loading.value = true

  try {
    if (isFollowing.value) {
      const result = await unfollowUser(props.targetUserId)
      isFollowing.value = false
      ElMessage.success('取消关注成功')
      emit('unfollowed', result)
    } else {
      const result = await followUser(props.targetUserId)
      isFollowing.value = true
      ElMessage.success('关注成功')
      emit('followed', result)
    }
  } catch (error) {
    // 恢复原来的状态
    isFollowing.value = previousState

    console.error('关注操作失败:', error)

    // request.js已经显示了错误消息，这里只记录日志
    // 如果需要特殊处理某些错误，可以在这里添加
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFollowStatus()
})
</script>

<template>
  <el-button v-if="shouldShowButton" :type="isFollowing ? 'default' : 'primary'"
    :class="{ 'follow-button': true, 'is-following': isFollowing }" size="small" :loading="loading || checking"
    :disabled="isFriend" @click="handleFollow" @mouseenter="onHover" @mouseleave="onLeave">
    {{ buttonText }}
  </el-button>
</template>

<style scoped>
.follow-button {
  min-width: 80px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.follow-button.is-following {
  border-color: #dcdfe6;
}

.follow-button.is-following:hover {
  color: #f56c6c;
  border-color: #f56c6c;
  background-color: #fef0f0;
}
</style>
