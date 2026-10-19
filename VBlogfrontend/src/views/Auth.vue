<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/userStore'
import { login, register } from '../api/user'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElIcon } from 'element-plus'
import { User, Lock, View, Hide, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前模式：'login' 或 'register'
const mode = ref('login')
const isLogin = computed(() => mode.value === 'login')

onMounted(() => {
  // 根据路由路径设置初始模式
  if (route.path === '/register') {
    mode.value = 'register'
  }
})

// 表单数据
const form = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)

// 切换模式
const toggleMode = () => {
  mode.value = isLogin.value ? 'register' : 'login'
  // 更新 URL 但不刷新页面
  const newPath = isLogin.value ? '/login' : '/register'
  router.replace(newPath)

  // 清空表单
  form.value = {
    username: '',
    password: '',
    confirmPassword: ''
  }
}

// 验证表单
const validateForm = () => {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return false
  }
  if (!form.value.password) {
    ElMessage.warning('请输入密码')
    return false
  }
  if (!isLogin.value) {
    if (form.value.password !== form.value.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return false
    }
  }
  return true
}

// 处理提交
const handleSubmit = async () => {
  if (!validateForm()) return

  loading.value = true
  try {
    let response
    if (isLogin.value) {
      response = await login(form.value.username, form.value.password)
    } else {
      response = await register(form.value.username, form.value.password)
    }

    // 适配响应数据
    let token = response.token
    let user = response.user

    if (!token || !user) {
      if (response.data) {
        token = response.data.token
        user = response.data.user
      }
    }

    if (token && user) {
      // 确保用户信息中有 username，如果没有则使用账号名
      if (!user.username) {
        user.username = form.value.username
      }

      userStore.setToken(token)
      userStore.setUserInfo(user)

      ElMessage.success(isLogin.value ? '欢迎回来' : '注册成功，欢迎加入')

      setTimeout(() => {
        router.push('/')
      }, 500)
    } else {
      throw new Error('响应数据异常')
    }
  } catch (error) {
    console.error(isLogin.value ? '登录失败:' : '注册失败:', error)
    // 错误已在 request.js 中处理，这里不需要重复提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="auth-card glass-effect">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo">
            <div class="logo-symbol">V</div>
            <h1>V-Blog</h1>
          </div>
          <p class="slogan">连接世界的每一个瞬间</p>
          <div class="brand-footer">
            <span>Discover</span>
            <span>Share</span>
            <span>Connect</span>
          </div>
        </div>
        <!-- 抽象背景纹理 -->
        <div class="brand-bg-texture"></div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="form-wrapper">
          <div class="form-header">
            <h2>{{ isLogin ? '欢迎回来' : '加入我们' }}</h2>
            <p class="subtitle">
              {{ isLogin ? '登录您的账号以继续' : '创建一个新账号开启旅程' }}
            </p>
          </div>

          <el-form :model="form" class="auth-form" @submit.prevent="handleSubmit">
            <div class="input-group">
              <div class="input-field">
                <label>用户名</label>
                <div class="custom-input">
                  <el-icon class="input-icon">
                    <User />
                  </el-icon>
                  <input v-model="form.username" type="text" placeholder="请输入用户名" @keyup.enter="handleSubmit" />
                </div>
              </div>

              <div class="input-field">
                <label>密码</label>
                <div class="custom-input">
                  <el-icon class="input-icon">
                    <Lock />
                  </el-icon>
                  <input v-model="form.password" type="password" :placeholder="isLogin ? '请输入密码' : '至少10位，包含字母/数字/符号中的两类'" @keyup.enter="handleSubmit" />
                </div>
              </div>

              <div v-if="!isLogin" class="input-field fade-in">
                <label>确认密码</label>
                <div class="custom-input">
                  <el-icon class="input-icon">
                    <Lock />
                  </el-icon>
                  <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"
                    @keyup.enter="handleSubmit" />
                </div>
              </div>
            </div>

            <div class="form-actions">
              <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
                {{ isLogin ? '登 录' : '注 册' }}
              </el-button>
            </div>

            <div class="auth-switch">
              <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
              <a href="#" @click.prevent="toggleMode" class="switch-link">
                {{ isLogin ? '立即注册' : '立即登录' }}
              </a>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 动态背景 */
.animated-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.blob-1 {
  width: 600px;
  height: 600px;
  background: rgba(139, 92, 246, 0.3);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: rgba(99, 102, 241, 0.3);
  bottom: -100px;
  right: -100px;
  animation-delay: -5s;
}

.blob-3 {
  width: 400px;
  height: 400px;
  background: rgba(109, 40, 217, 0.25);
  top: 40%;
  left: 40%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
  opacity: 0.4;
}

@keyframes float {

  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }

  33% {
    transform: translate(30px, -50px) scale(1.1);
  }

  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* 卡片主体 */
.auth-card {
  display: flex;
  width: 1000px;
  height: 600px;
  background: var(--glass-card-bg);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 32px;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(139, 92, 246, 0.2) inset;
  overflow: hidden;
  z-index: 1;
  position: relative;
}

/* 左侧品牌区 */
.brand-section {
  flex: 0 0 45%;
  background: var(--primary-gradient);
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;
  position: relative;
  overflow: hidden;
}

/* 抽象纹理 */
.brand-bg-texture {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    radial-gradient(at 80% 0%, hsla(189, 100%, 56%, 1) 0px, transparent 50%),
    radial-gradient(at 0% 50%, hsla(355, 100%, 93%, 1) 0px, transparent 50%),
    radial-gradient(at 80% 50%, hsla(340, 100%, 76%, 1) 0px, transparent 50%),
    radial-gradient(at 0% 100%, hsla(22, 100%, 77%, 1) 0px, transparent 50%),
    radial-gradient(at 80% 100%, hsla(242, 100%, 70%, 1) 0px, transparent 50%),
    radial-gradient(at 0% 0%, hsla(343, 100%, 76%, 1) 0px, transparent 50%);
  opacity: 0.8;
  z-index: 1;
  mix-blend-mode: overlay;
}

.brand-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-symbol {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #667eea 100%);
  border-radius: 12px;
  color: #fff;
  font-size: 28px;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.brand-section h1 {
  font-size: 32px;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.slogan {
  font-size: 42px;
  font-weight: 700;
  line-height: 1.2;
  margin: auto 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.brand-footer {
  display: flex;
  gap: 24px;
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
  letter-spacing: 1px;
  text-transform: uppercase;
}

/* 右侧表单区 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(20px);
  padding: 40px;
  border-left: 1px solid var(--glass-border);
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 40px;
  text-align: left;
}

.form-header h2 {
  font-size: 28px;
  color: var(--text-main);
  margin: 0 0 8px 0;
  font-weight: 700;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 15px;
}

/* 输入框样式 */
.input-group {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 32px;
}

.input-field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-regular);
  margin-bottom: 8px;
  margin-left: 4px;
}

.custom-input {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(31, 41, 55, 0.5);
  border-radius: 16px;
  padding: 4px 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(139, 92, 246, 0.2);
}

.custom-input:hover {
  background: rgba(31, 41, 55, 0.7);
}

.custom-input:focus-within {
  background: rgba(31, 41, 55, 0.8);
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.2);
}

.input-icon {
  font-size: 18px;
  color: var(--text-secondary);
  margin-right: 12px;
  transition: color 0.3s;
}

.custom-input:focus-within .input-icon {
  color: var(--primary-color);
}

.custom-input input {
  flex: 1;
  border: none;
  background: transparent;
  height: 48px;
  font-size: 15px;
  color: var(--text-main);
  outline: none;
  font-weight: 500;
}

.custom-input input::placeholder {
  color: var(--text-secondary);
  font-weight: 400;
}

/* 按钮样式 */
.submit-btn {
  width: 100%;
  height: 56px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-gradient);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 20px rgba(139, 92, 246, 0.4);
  letter-spacing: 1px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(139, 92, 246, 0.6);
}

.submit-btn:active {
  transform: translateY(1px);
  box-shadow: 0 5px 10px rgba(255, 154, 158, 0.2);
}

/* 切换链接 */
.auth-switch {
  margin-top: 32px;
  text-align: center;
  font-size: 14px;
  color: #8c8c8c;
}

.switch-link {
  color: #ff9a9e;
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
  transition: color 0.3s;
}

.switch-link:hover {
  color: #ff7a80;
  text-decoration: underline;
}

/* 动画 */
.fade-in {
  animation: slideUpFade 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes slideUpFade {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media (max-width: 900px) {
  .auth-card {
    width: 90%;
    height: auto;
    min-height: 600px;
    flex-direction: column;
  }

  .brand-section {
    flex: 0 0 200px;
    padding: 40px;
  }

  .slogan {
    font-size: 28px;
    margin: 20px 0;
  }

  .brand-footer {
    display: none;
  }

  .form-section {
    padding: 40px 24px;
  }
}
</style>
