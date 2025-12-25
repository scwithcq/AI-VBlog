import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * API 请求统一处理模块
 * 功能：
 * 1. 自动添加 Authorization Token
 * 2. 统一处理响应数据格式
 * 3. 统一处理错误异常（网络错误、401未授权、服务器错误等）
 * 4. 支持 application/json 和 multipart/form-data 两种请求格式
 */

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 (自动携带 Token 和参数验证)
service.interceptors.request.use(
  config => {
    // 1. 从 localStorage 获取并添加 Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 2. 处理不同类型的数据格式
    if (config.data instanceof FormData) {
      // FormData：不要手动设置 Content-Type，让浏览器自动设置 boundary
      delete config.headers['Content-Type']
    } else if (config.data instanceof URLSearchParams) {
      // URLSearchParams：确保使用 form-urlencoded 格式
      config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
    } else if (config.data && typeof config.data === 'object' && !Array.isArray(config.data)) {
      // 普通对象：使用 JSON 格式
      config.headers['Content-Type'] = 'application/json; charset=utf-8'
    }

    // 3. 调试日志：打印请求信息
    if (process.env.NODE_ENV === 'development') {
      console.log(`[Request] ${config.method?.toUpperCase()} ${config.url}`, {
        data: config.data,
        headers: config.headers
      })
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 (统一处理错误和数据格式)
service.interceptors.response.use(
  response => {
    const res = response.data

    // 检查响应中是否包含 code 字段（后端 Result 结构）
    // 注意：后端的成功状态码是 0（不是 200）
    if (res.code !== undefined) {
      // 成功状态码为 0 或 200
      if (res.code === 0 || res.code === 200) {
        // 调试日志
        if (process.env.NODE_ENV === 'development') {
          console.log(`[Response] Success (code: ${res.code})`, {
            fullResponse: res,
            data: res.data
          })
        }
        // 返回 data 字段，如果不存在则返回整个 res
        return res.data !== undefined ? res.data : res
      } else if (res.code === 401) {
        // 401：Token 无效或已过期
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.error('登录已过期，请重新登录')
        // 延迟跳转，避免被其他请求覆盖
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
        return Promise.reject(new Error('未授权: Token 无效或已过期'))
      } else if (res.code === 404) {
        // 404：资源不存在
        const errorMsg = res.message || res.msg || '请求的资源不存在'
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      } else if (res.code === 500) {
        // 500：服务器内部错误
        const errorMsg = res.message || res.msg || '服务器内部错误，请稍后重试'
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      } else {
        // 其他错误状态码
        const errorMsg = res.message || res.msg || `请求失败: ${res.code}`
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      }
    } else {
      // 无 code 字段的响应，直接返回
      return res
    }
  },
  error => {
    // 处理网络和 HTTP 层面的错误
    let errorMessage = '网络错误，请检查连接'

    if (error.response) {
      const { status, data, statusText } = error.response

      // 对于没有 code 字段的 HTTP 错误响应
      if (data?.message || data?.msg) {
        errorMessage = data.message || data.msg
      } else {
        errorMessage = `请求失败 [${status}]: ${statusText}`
      }

      // 特殊处理 401 和 403
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        errorMessage = '登录已过期，请重新登录'
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
      } else if (status === 403) {
        errorMessage = '没有权限访问该资源'
      } else if (status === 404) {
        errorMessage = '请求的资源不存在'
      } else if (status === 500) {
        errorMessage = '服务器内部错误，请稍后重试'
      }
    } else if (error.request) {
      // 请求已发出但未收到响应
      errorMessage = '服务器无响应，请检查网络连接'
    } else if (error.code === 'ECONNABORTED') {
      // 请求超时
      errorMessage = '请求超时，请稍后重试'
    }

    ElMessage.error(errorMessage)
    return Promise.reject(new Error(errorMessage))
  }
)

export default service