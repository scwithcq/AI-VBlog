<script setup>
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import PostCard from '../components/PostCard.vue'
import { getMyFavorites } from '../api/favorite'
import { getUserInfo } from '../api/user'

const posts = ref([])
const users = ref({})
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 10

const loadFavorites = async () => {
  loading.value = true
  try {
    const result = await getMyFavorites(page.value, pageSize)
    posts.value = result.records || []
    total.value = Number(result.total) || 0
    const ids = [...new Set(posts.value.map(post => post.userId))]
    await Promise.all(ids.map(async id => {
      if (!users.value[id]) users.value[id] = await getUserInfo(id)
    }))
  } finally {
    loading.value = false
  }
}

const handleFavoriteChanged = ({ postId, isFavorited }) => {
  if (!isFavorited) {
    posts.value = posts.value.filter(post => post.postId !== postId)
    total.value = Math.max(0, total.value - 1)
    if (!posts.value.length && page.value > 1) page.value -= 1
    loadFavorites()
  }
}

onMounted(loadFavorites)
</script>

<template>
  <Layout>
    <div class="favorite-page" v-loading="loading">
      <h2>我的收藏</h2>
      <el-empty v-if="!loading && !posts.length" description="还没有收藏的微博" />
      <PostCard v-for="post in posts" :key="post.postId" :post="post" :user="users[post.userId]"
        @favorited="handleFavoriteChanged" @deleted="loadFavorites" />
      <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize"
        :total="total" layout="prev, pager, next, total" @current-change="loadFavorites" />
    </div>
  </Layout>
</template>

<style scoped>
.favorite-page { max-width: 760px; margin: 0 auto; }
.favorite-page h2 { margin: 0 0 18px; }
</style>
