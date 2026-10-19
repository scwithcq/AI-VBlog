import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserStats } from '../api/user'
import { useUserStore } from './userStore'

export const useUserStatsStore = defineStore('userStats', () => {
  const stats = ref({ followerCount: 0, followingCount: 0, postCount: 0 })
  const loadedUserId = ref(null)

  async function refresh() {
    const userId = useUserStore().userId
    if (!userId) {
      stats.value = { followerCount: 0, followingCount: 0, postCount: 0 }
      loadedUserId.value = null
      return
    }
    const data = await getUserStats(userId)
    if (useUserStore().userId === userId) {
      stats.value = data
      loadedUserId.value = userId
    }
  }

  return { stats, loadedUserId, refresh }
})
