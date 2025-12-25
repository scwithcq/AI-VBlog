import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // State
  const loading = ref(false)
  const activeMenu = ref('home')

  // Actions
  function setLoading(value) {
    loading.value = value
  }

  function setActiveMenu(menu) {
    activeMenu.value = menu
  }

  return {
    loading,
    activeMenu,
    setLoading,
    setActiveMenu
  }
})

