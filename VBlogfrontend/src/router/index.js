import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/userStore'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Auth.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Auth.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/profile/:userId',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('../views/PostDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/search',
    name: 'SearchResult',
    component: () => import('../views/SearchResult.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/topic/:topicName',
    name: 'TopicDetail',
    component: () => import('../views/TopicDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/followers/:userId',
    name: 'FollowersList',
    component: () => import('../views/FollowersList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/following/:userId',
    name: 'FollowingList',
    component: () => import('../views/FollowingList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/message',
    name: 'Message',
    component: () => import('../views/Message.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notification',
    name: 'Notification',
    component: () => import('../views/Notification.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 记录登录前的目标路由
let redirectUrl = null

// Route guard - 路由前置守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn

  // 需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    // 记录用户试图访问的页面
    redirectUrl = to.fullPath
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  }
  // 已登录用户访问登录/注册页面 - 重定向到首页或之前访问的页面
  else if (!to.meta.requiresAuth && to.path !== '/login' && to.path !== '/register' && isLoggedIn) {
    // 如果是从已认证页面跳转，继续；否则重定向到首页
    if (from.matched.some(record => record.meta.requiresAuth)) {
      next()
    } else {
      next('/')
    }
  }
  else {
    next()
  }
})

// Route guard - 路由后置守卫（用于页面标题、加载状态等）
router.afterEach((to) => {
  // 更新页面标题
  const title = getPageTitle(to)
  document.title = title ? `${title} - V-Blog` : 'V-Blog'
})

// 根据路由名称获取页面标题
function getPageTitle(route) {
  const titleMap = {
    'Home': '首页',
    'Login': '登录',
    'Register': '注册',
    'Profile': '个人资料',
    'PostDetail': '帖子详情',
    'SearchResult': '搜索结果',
    'TopicDetail': '话题详情',
    'FollowersList': '粉丝列表',
    'FollowingList': '关注列表'
  }
  return titleMap[route.name] || ''
}

export default router