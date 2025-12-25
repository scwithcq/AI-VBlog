<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore'
import { getUserInfo, getUserStats, updateUserInfo, getCurrentUserInfo } from '../api/user'
import { getUserPosts } from '../api/post'
import { uploadImage } from '../api/file'
import FollowButton from '../components/FollowButton.vue'
import PostCard from '../components/PostCard.vue'
import Layout from '../components/Layout.vue'
import { ElCard, ElAvatar, ElButton, ElRow, ElCol, ElStatistic, ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElUpload, ElIcon, ElRadioGroup, ElRadio } from 'element-plus'
import { Edit, Upload, Lock, Location, Link as LinkIcon, Message, Iphone } from '@element-plus/icons-vue'

// 导入本地图片
import avatar1 from '../assets/Images/boy.png'
import avatar2 from '../assets/Images/girl.png'
import avatar3 from '../assets/Images/smilepeople.png'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = ref(parseInt(route.params.userId) || userStore.userId)
const userInfo = ref(null)
const stats = ref({
  followerCount: 0,
  followingCount: 0,
  postCount: 0
})
const posts = ref([])
const loading = ref(false)
const postsLoading = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 是否是当前登录用户的个人页面
const isOwnProfile = computed(() => userId.value === userStore.userId)

// 编辑资料对话框
const showEditDialog = ref(false)
const editFormRef = ref(null)
const editForm = ref({
  username: '',
  bio: '',
  email: '',
  phone: '',
  gender: '',
  location: '',
  website: ''
})
const editFormLoading = ref(false)

// 默认头像列表
const defaultAvatars = [
  avatar1,
  avatar2,
  avatar3
]
const selectedAvatar = ref('')  // 选中的头像URL

const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  website: [
    { type: 'url', message: '请输入正确的网址', trigger: 'blur' }
  ]
}

// 修改密码对话框
const showPasswordDialog = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordFormLoading = ref(false)

// 头像上传
const avatarUploading = ref(false)

const loadUserData = async () => {
  loading.value = true
  try {
    const [userData, statsData] = await Promise.all([
      getUserInfo(userId.value),
      getUserStats(userId.value)
    ])
    userInfo.value = userData
    stats.value = statsData

    // 如果是当前用户的资料页，更新编辑表单
    if (isOwnProfile.value) {
      editForm.value = {
        username: userData.username || '',
        bio: userData.bio || '',
        email: userData.email || '',
        phone: userData.phone || '',
        gender: userData.gender || '',
        location: userData.location || '',
        website: userData.website || ''
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

const loadUserPosts = async () => {
  postsLoading.value = true
  try {
    const result = await getUserPosts(userId.value, page.value, pageSize.value)
    // 兼容不同的响应格式
    posts.value = result.list || result.records || (Array.isArray(result) ? result : [])

    // 确保帖子有用户信息
    // 如果帖子已经包含用户信息，就不需要再加载
    // 如果没有，需要加载用户信息（这里假设已经有了，因为是用户自己的帖子）
  } catch (error) {
    console.error('加载用户帖子失败:', error)
  } finally {
    postsLoading.value = false
  }
}

const handleFollowed = (result) => {
  // 更新粉丝数量（如果后端返回了新的数量）
  if (result && result.followerCount !== undefined) {
    stats.value.followerCount = result.followerCount
  } else {
    // 否则手动增加
    stats.value.followerCount++
  }
}

const handleUnfollowed = (result) => {
  // 更新粉丝数量（如果后端返回了新的数量）
  if (result && result.followerCount !== undefined) {
    stats.value.followerCount = result.followerCount
  } else {
    // 否则手动减少
    stats.value.followerCount = Math.max(0, stats.value.followerCount - 1)
  }
}

// 打开编辑资料对话框
const handleEditProfile = () => {
  editForm.value = {
    username: userInfo.value?.username || '',
    bio: userInfo.value?.bio || '',
    email: userInfo.value?.email || '',
    phone: userInfo.value?.phone || '',
    gender: userInfo.value?.gender || '',
    location: userInfo.value?.location || '',
    website: userInfo.value?.website || ''
  }
  showEditDialog.value = true
}

// 提交编辑资料
const handleSubmitEdit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editFormLoading.value = true
      try {
        const updateData = { ...editForm.value }

        // 如果选择了新头像，包含在更新数据中
        if (selectedAvatar.value) {
          updateData.avatar = selectedAvatar.value
        }

        const updatedInfo = await updateUserInfo(updateData)

        userInfo.value = updatedInfo
        userStore.updateUserInfo(updatedInfo)

        showEditDialog.value = false
        selectedAvatar.value = ''  // 清空选中的头像
        ElMessage.success('个人资料更新成功')
      } catch (error) {
        ElMessage.error('更新失败，请稍后重试')
      } finally {
        editFormLoading.value = false
      }
    } else {
      ElMessage.warning('请检查输入项是否正确')
    }
  })
}

// 处理头像上传
const handleAvatarUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }

  avatarUploading.value = true
  try {
    const response = await uploadImage(file.raw)
    const avatarUrl = response.url

    // 更新头像
    selectedAvatar.value = avatarUrl
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
  return false
}

// 选择默认头像
const selectDefaultAvatar = (avatarUrl) => {
  selectedAvatar.value = avatarUrl
  ElMessage.success('已选择默认头像')
  avatarUploading.value = false
  return false
}


// 打开修改密码对话框
const handleChangePassword = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showPasswordDialog.value = true
}

// 提交修改密码
const handleSubmitPassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value

  if (!oldPassword || !newPassword || !confirmPassword) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }

  if (newPassword !== confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  // TODO: 调用修改密码API（需要后端实现）
  // passwordFormLoading.value = true
  // try {
  //   await changePassword(oldPassword, newPassword)
  //   showPasswordDialog.value = false
  //   ElMessage.success('密码修改成功')
  // } catch (error) {
  //   ElMessage.error('密码修改失败')
  // } finally {
  //   passwordFormLoading.value = false
  // }

  ElMessage.info('修改密码功能即将推出')
}

// 处理帖子删除
const handlePostDeleted = (postId) => {
  posts.value = posts.value.filter(post => post.postId !== postId)
  // 更新统计数据
  if (stats.value.postCount > 0) {
    stats.value.postCount--
  }
}

function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.getFullYear() + '年' + (date.getMonth() + 1) + '月'
}

onMounted(async () => {
  await loadUserData()
  loadUserPosts()
})
</script>

<template>
  <Layout>
    <div class="profile-container">
      <el-card v-loading="loading">
        <div class="profile-header">
          <div class="avatar-wrapper">
            <el-avatar :src="userInfo?.avatar" :size="100" />
            <el-upload v-if="isOwnProfile" class="avatar-uploader" :auto-upload="false" @change="handleAvatarUpload"
              :show-file-list="false" accept="image/*">
              <template #trigger>
                <el-button :icon="Upload" size="small" :loading="avatarUploading" circle type="primary" />
              </template>
            </el-upload>
          </div>

          <div class="profile-info">
            <div class="profile-name-row">
              <h2>{{ userInfo?.username }}</h2>
              <div class="action-buttons">
                <FollowButton v-if="!isOwnProfile && userInfo" :target-user-id="userInfo.userId"
                  @followed="handleFollowed" @unfollowed="handleUnfollowed" />
                <el-button v-if="isOwnProfile" :icon="Edit" @click="handleEditProfile" type="primary" plain>
                  编辑资料
                </el-button>
                <el-button v-if="isOwnProfile" :icon="Lock" @click="handleChangePassword" plain>
                  修改密码
                </el-button>
              </div>
            </div>
            <p v-if="userInfo?.bio" class="bio">{{ userInfo.bio }}</p>
            <p v-else class="no-bio">这个人很懒，什么都没有留下</p>

            <div class="extra-info">
              <span v-if="userInfo?.location" class="info-item">
                <el-icon>
                  <Location />
                </el-icon> {{ userInfo.location }}
              </span>
              <span v-if="userInfo?.website" class="info-item">
                <el-icon>
                  <LinkIcon />
                </el-icon> <a :href="userInfo.website" target="_blank">{{ userInfo.website }}</a>
              </span>
            </div>

            <p class="create-time">加入于 {{ formatDate(userInfo?.createTime) }}</p>
          </div>
        </div>

        <el-row :gutter="20" class="stats-row">
          <el-col :span="8">
            <el-statistic title="关注" :value="stats.followingCount" />
            <el-button v-if="userInfo" text size="small" @click="router.push(`/following/${userInfo.userId}`)"
              style="width: 100%; margin-top: 8px;">
              查看关注列表
            </el-button>
          </el-col>
          <el-col :span="8">
            <el-statistic title="粉丝" :value="stats.followerCount" />
            <el-button v-if="userInfo" text size="small" @click="router.push(`/followers/${userInfo.userId}`)"
              style="width: 100%; margin-top: 8px;">
              查看粉丝列表
            </el-button>
          </el-col>
          <el-col :span="8">
            <el-statistic title="微博" :value="stats.postCount" />
          </el-col>
        </el-row>
      </el-card>

      <el-card class="posts-card" style="margin-top: 20px" v-loading="postsLoading">
        <template #header>
          <h3>{{ isOwnProfile ? '我的微博' : '他的微博' }}</h3>
        </template>
        <div class="posts-list">
          <PostCard v-for="post in posts" :key="post.postId" :post="post" :user="userInfo"
            @deleted="handlePostDeleted" />
          <p v-if="!postsLoading && posts.length === 0" class="empty-text">暂无微博</p>
        </div>
      </el-card>

      <!-- 编辑资料对话框 -->
      <el-dialog v-model="showEditDialog" title="编辑个人资料" width="500px" class="profile-dialog" append-to-body>
        <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">

          <!-- 头像选择 -->
          <el-form-item label="头像">
            <div class="avatar-selection">
              <div class="current-avatar">
                <el-avatar :src="selectedAvatar || userInfo?.avatar || '/default-avatar.png'" :size="80" />
                <div class="avatar-tip">当前头像</div>
              </div>

              <div class="avatar-options">
                <div class="upload-avatar">
                  <el-upload :auto-upload="false" :on-change="handleAvatarUpload" :show-file-list="false"
                    accept="image/*" :disabled="avatarUploading">
                    <el-button type="primary" :loading="avatarUploading" size="small">
                      <el-icon>
                        <Upload />
                      </el-icon>
                      上传头像
                    </el-button>
                  </el-upload>
                  <div class="tip">支持 JPG、PNG，不超过 2MB</div>
                </div>

                <div class="default-avatars">
                  <div class="default-title">或选择默认头像：</div>
                  <div class="avatar-list">
                    <el-avatar v-for="(avatar, index) in defaultAvatars" :key="index" :src="avatar" :size="60"
                      :class="{ 'selected': selectedAvatar === avatar }" @click="selectDefaultAvatar(avatar)"
                      class="avatar-item" />
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username" placeholder="请输入用户名" />
          </el-form-item>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="editForm.gender">
              <el-radio label="male">男</el-radio>
              <el-radio label="female">女</el-radio>
              <el-radio label="other">其他</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="editForm.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="所在地" prop="location">
            <el-input v-model="editForm.location" placeholder="请输入所在地" />
          </el-form-item>

          <el-form-item label="个人网站" prop="website">
            <el-input v-model="editForm.website" placeholder="请输入个人网站地址" />
          </el-form-item>

          <el-form-item label="个人简介" prop="bio">
            <el-input v-model="editForm.bio" type="textarea" placeholder="请输入个人简介" rows="4" maxlength="200"
              show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" :loading="editFormLoading" @click="handleSubmitEdit">
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 修改密码对话框 -->
      <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px" class="profile-dialog" append-to-body>
        <el-form :model="passwordForm" label-width="100px">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" :loading="passwordFormLoading" @click="handleSubmitPassword">
            确认修改
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>
<style scoped>
/* 玻璃态卡片样式覆盖 - 与首页风格统一 */
.profile-container :deep(.el-card) {
  background: var(--glass-card-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(var(--glass-blur));
  box-shadow: var(--glass-shadow);
}

/* 确保帖子卡片中的文字为白色 */
.profile-container :deep(.el-card__body) {
  color: #e5e7eb;
}

.profile-container :deep(.post-card) {
  color: #e5e7eb;
}

.profile-container :deep(.post-card .username),
.profile-container :deep(.post-card .content),
.profile-container :deep(.post-card .post-text) {
  color: #ffffff !important;
}

.profile-container :deep(.post-card .time),
.profile-container :deep(.post-card .post-time) {
  color: #d1d5db !important;
}

.profile-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 30px;
  padding: 20px 0;
}

.avatar-wrapper {
  position: relative;
}

.avatar-uploader {
  position: absolute;
  bottom: 0;
  right: 0;
}

.profile-info {
  flex: 1;
}

.profile-info h2 {
  margin: 0 0 10px 0;
  font-size: 26px;
  color: #ffffff;
  font-weight: 600;
}

.profile-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.profile-info p {
  margin: 8px 0;
  color: #e5e7eb;
}

.bio {
  font-size: 14px;
  line-height: 1.5;
  color: #e5e7eb;
}

.extra-info {
  display: flex;
  gap: 15px;
  margin: 10px 0;
  color: #e5e7eb;
  font-size: 14px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-item a {
  color: #a78bfa;
  text-decoration: none;
  font-weight: 500;
}

.info-item a:hover {
  color: #c4b5fd;
}

.no-bio {
  color: #d1d5db;
  font-style: italic;
}

.create-time {
  font-size: 12px;
  color: #d1d5db;
}

.stats-row {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--glass-border);
}

.posts-card {
  min-height: 400px;
}

.posts-list {
  min-height: 300px;
}

.empty-text {
  text-align: center;
  color: #d1d5db;
  padding: 40px 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    gap: 20px;
  }

  .action-buttons {
    margin-left: 0;
    flex-wrap: wrap;
  }
}
</style>

<style>
.profile-dialog {
  background-color: var(--glass-bg) !important;
  backdrop-filter: blur(var(--glass-blur)) !important;
  border-radius: var(--border-radius-lg) !important;
  border: 1px solid var(--glass-border) !important;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2) !important;
}

.profile-dialog .el-dialog__header {
  margin-right: 0;
  border-bottom: 1px solid #f0f0f0;
  padding: 20px;
}

.profile-dialog .el-dialog__body {
  padding: 20px 30px;
}

.profile-dialog .el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
}

/* 头像选择样式 */
.avatar-selection {
  display: flex;
  gap: 30px;
  padding: 20px;
  background: rgba(31, 41, 55, 0.3);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--glass-border);
}

.current-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-tip {
  font-size: 12px;
  color: var(--text-secondary);
}

.avatar-options {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.upload-avatar {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-avatar .tip {
  font-size: 12px;
  color: var(--text-secondary);
}

.default-avatars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.default-title {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.avatar-list {
  display: flex;
  gap: 15px;
}

.avatar-item {
  cursor: pointer;
  transition: all 0.3s;
  border: 3px solid transparent;
  border-radius: 50%;
}

.avatar-item:hover {
  transform: scale(1.1);
  border-color: var(--primary-color);
}

.avatar-item.selected {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.2);
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .profile-dialog {
    background-color: #2d2d2d !important;
  }

  .profile-dialog .el-dialog__header,
  .profile-dialog .el-dialog__footer {
    border-color: #3d3d3d;
  }

  .profile-dialog .el-dialog__title {
    color: #fff;
  }
}
</style>
