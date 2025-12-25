<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPostList } from '../api/post'
import { getUserInfo } from '../api/user'
import { getTopicByName } from '../api/topic'
import PostCard from '../components/PostCard.vue'
import Layout from '../components/Layout.vue'
import { ElCard, ElEmpty, ElTag } from 'element-plus'

const route = useRoute()
const topicName = ref(decodeURIComponent(route.params.topicName))
const topic = ref(null)
const posts = ref([])
const users = ref({})
const loading = ref(false)

const loadTopicPosts = async () => {
  loading.value = true
  try {
    // 根据话题名称获取话题数据（后端应该返回相关的帖子列表）
    // 如果后端有专门的话题接口，应该调用 getTopicByName
    // 否则就获取全部帖子后本地过滤
    
    // 获取帖子列表并本地过滤
    // 根据 API 文档，响应格式为 { list: [], total: 0, page: 1, size: 10 }
    const result = await getPostList(1, 100) // 获取更多帖子用于本地过滤
    const allPosts = result.list || result.records || result || []
    
    // 过滤包含该话题的帖子
    posts.value = allPosts.filter(post => {
      if (!post.content) return false
      const topicPattern = new RegExp(`#${topicName.value}#`, 'i')
      return topicPattern.test(post.content)
    })
    
    // 加载用户信息
    const userIds = [...new Set(posts.value.map(p => p.userId))]
    for (const userId of userIds) {
      if (!users.value[userId]) {
        try {
          const userInfo = await getUserInfo(userId)
          users.value[userId] = userInfo
        } catch (error) {
          console.error(`加载用户 ${userId} 失败:`, error)
        }
      }
    }
  } catch (error) {
    console.error('加载话题帖子失败:', error)
    // 错误已在 request.js 中通过 ElMessage.error 显示
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTopicPosts()
})
</script>

<template>
  <Layout>
    <div class="topic-detail-container">
      <el-card>
        <template #header>
          <div class="topic-header">
            <h2>#{{ topicName }}#</h2>
            <el-tag type="danger" size="large">话题</el-tag>
          </div>
        </template>
        
        <div v-loading="loading" class="posts-container">
          <PostCard
            v-for="post in posts"
            :key="post.postId"
            :post="post"
            :user="users[post.userId]"
          />
          <el-empty v-if="!loading && posts.length === 0" description="暂无相关微博" />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<style scoped>
.topic-detail-container {
  max-width: 100%;
}

.topic-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topic-header h2 {
  margin: 0;
  color: #409eff;
}

.posts-container {
  min-height: 400px;
}
</style>


