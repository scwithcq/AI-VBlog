import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUserInfo } from '../api/user'

export const useUserStore = defineStore('user', () => {
  // State - 从 localStorage 恢复
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isInitialized = ref(false)

  // Computed
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.userId)
  const username = computed(() => userInfo.value?.username)
  const avatar = computed(() => userInfo.value?.avatar)

  // Actions
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  // 初始化用户信息（从 token 恢复用户信息）
  async function initUserInfo() {
    if (isInitialized.value) return

    try {
      // 如果有 token 但没有 userInfo，自动获取
      if (token.value && !userInfo.value) {
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
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
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

