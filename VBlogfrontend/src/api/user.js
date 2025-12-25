import request from '../utils/request'

/**
 * 用户相关 API
 * 
 * API 接口文档：
 * - 用户注册: POST /user/register
 * - 用户登录: POST /user/login  
 * - 获取当前用户信息: GET /user/info
 * - 更新用户信息: PUT /user/info
 * - 获取用户信息（按ID）: GET /user/{userId}
 */

/**
 * 用户注册
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 返回 { user: {...}, token: '...' }
 */
export function register(username, password) {
  // 后端可能期望 application/x-www-form-urlencoded 格式
  const params = new URLSearchParams()
  params.append('username', username)
  params.append('password', password)

  return request({
    url: '/user/register',
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 用户登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 返回 { user: {...}, token: '...' }
 */
export function login(username, password) {
  // 后端可能期望 application/x-www-form-urlencoded 格式
  // 尝试两种方式：JSON 和 form-urlencoded
  const params = new URLSearchParams()
  params.append('username', username)
  params.append('password', password)

  return request({
    url: '/user/login',
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 返回当前用户信息对象
 */
export function getCurrentUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {object} userInfo - 用户信息对象 (可包含: username, avatar, bio 等字段)
 * @returns {Promise} 返回更新后的用户信息
 */
export function updateUserInfo(userInfo) {
  return request({
    url: '/user/info',
    method: 'put',
    data: userInfo
  })
}

/**
 * 获取指定用户的信息（按 ID）
 * @param {number|string} userId - 用户ID
 * @returns {Promise} 返回用户信息对象
 */
export function getUserInfo(userId) {
  return request({
    url: `/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取用户统计信息
 * @param {number|string} userId - 用户ID
 * @returns {Promise} 返回包含粉丝数、关注数、帖子数等统计信息
 */
export function getUserStats(userId) {
  return request({
    url: `/user/${userId}/stats`,
    method: 'get'
  })
}

/**
 * 修改密码
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 * @returns {Promise} 返回修改结果
 */
export function changePassword(oldPassword, newPassword) {
  const params = new URLSearchParams()
  params.append('oldPassword', oldPassword)
  params.append('newPassword', newPassword)

  return request({
    url: '/user/password',
    method: 'put',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 上传头像
 * @param {File} file - 图片文件
 * @returns {Promise} 返回更新后的用户对象（包含新头像URL）
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传背景图
 * @param {File} file - 图片文件
 * @returns {Promise} 返回更新后的用户对象（包含新背景图URL）
 */
export function uploadBackground(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/user/background',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取所有用户列表
 * @returns {Promise} 返回用户列表
 */
export function getAllUsers() {
  return request({
    url: '/user/all',
    method: 'get'
  })
}

