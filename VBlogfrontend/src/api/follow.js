import request from '../utils/request'

/**
 * 关注相关 API
 * 
 * API 接口文档：
 * - 关注用户: POST /follow/{userId}
 * - 取消关注: DELETE /follow/{userId}
 * - 获取关注状态: GET /follow/{userId}/status（可选）
 * - 获取粉丝列表: GET /follow/{userId}/followers（可选）
 * - 获取关注列表: GET /follow/{userId}/following（可选）
 */

/**
 * 关注用户
 * @param {number|string} userId - 要关注的用户ID
 * @returns {Promise} 返回 { followed: true, followerCount: number }
 */
export function followUser(userId) {
  return request({
    url: `/follow/${userId}`,
    method: 'post'
  })
}

/**
 * 取消关注用户
 * @param {number|string} userId - 要取消关注的用户ID
 * @returns {Promise} 返回 { unfollowed: true, followerCount: number }
 */
export function unfollowUser(userId) {
  return request({
    url: `/follow/${userId}`,
    method: 'delete'
  })
}

/**
 * 获取关注状态
 * @param {number|string} userId - 用户ID
 * @returns {Promise} 返回 { isFollowing: boolean }
 */
export function getFollowStatus(userId) {
  return request({
    url: `/follow/${userId}/status`,
    method: 'get'
  })
}

/**
 * 获取用户的粉丝列表
 * @param {number|string} userId - 用户ID
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回粉丝列表
 */
export function getFollowers(userId, page = 1, size = 10) {
  return request({
    url: `/follow/${userId}/followers`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取用户的关注列表
 * @param {number|string} userId - 用户ID
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回关注列表
 */
export function getFollowing(userId, page = 1, size = 10) {
  return request({
    url: `/follow/${userId}/following`,
    method: 'get',
    params: { page, size }
  })
}

