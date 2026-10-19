import { normalizeMediaFields, getImageUrl } from '../utils/image'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUserInfo } from '../api/user'

export const useUserStore = defineStore('user', () => {
  // 登录状态按标签页隔离，允许同一浏览器的不同标签页登录不同用户。
  const token = ref(sessionStorage.getItem('token') || '')
  let savedUserInfo = null
  try {
    savedUserInfo = JSON.parse(sessionStorage.getItem('userInfo') || 'null')
  } catch {
    sessionStorage.removeItem('userInfo')
  }
  const userInfo = ref(normalizeMediaFields(savedUserInfo))
  const isInitialized = ref(false)

  // Computed
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.userId)
  const username = computed(() => userInfo.value?.username)
  const avatar = computed(() => getImageUrl(userInfo.value?.avatar) || '/default-avatar.jpg')

  // Actions
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      sessionStorage.setItem('token', newToken)
    } else {
      sessionStorage.removeItem('token')
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info) {
      sessionStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      sessionStorage.removeItem('userInfo')
    }
  }

  // 初始化用户信息（从 token 恢复用户信息）
  async function initUserInfo() {
    if (isInitialized.value) return

    try {
      // 每次打开应用都同步服务端资料，避免头像等信息停留在旧缓存。
      if (token.value) {
        const info = await getCurrentUserInfo()
        setUserInfo(info)
      }
    } catch (error) {
      console.error('初始化用户信息失败:', error)
      logout() // 如果获取失败，清除登录状态
    } finally {
      isInitialized.value = true
    }
  }

  // 更新用户信息（不清除 token）
  function updateUserInfo(newInfo) {
    userInfo.value = { ...userInfo.value, ...newInfo }
    sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  // 退出登录
  function logout() {
    setToken('')
    setUserInfo(null)
    isInitialized.value = false
  }

  return {
    // State
    token,
    userInfo,
    isInitialized,

    // Computed
    isLoggedIn,
    userId,
    username,
    avatar,

    // Methods
    setToken,
    setUserInfo,
    updateUserInfo,
    initUserInfo,
    logout
  }
})
