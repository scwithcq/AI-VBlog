<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from './store/userStore'
import { useRealtimeStore } from './store/realtimeStore'

const userStore = useUserStore()
const realtimeStore = useRealtimeStore()

watch(() => [userStore.userId, userStore.token], ([userId, token]) => {
  realtimeStore.start(userId, token)
}, { immediate: true })

// 应用启动时初始化用户信息
onMounted(() => {
  userStore.initUserInfo()
})

onUnmounted(() => realtimeStore.stop())
</script>

<template>
  <router-view :key="$route.path" />
</template>

<style>
/* 移除重复的全局样式，已在 style.css 中定义 */
</style>
