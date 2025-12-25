<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './store/userStore'

const userStore = useUserStore()

// 应用启动时初始化用户信息
onMounted(() => {
  userStore.initUserInfo()
})
</script>

<template>
  <router-view />
</template>

<style>
/* 移除重复的全局样式，已在 style.css 中定义 */
</style>
