import request from '../utils/request'

/**
 * 点赞相关 API
 * 
 * API 接口文档：
 * - 点赞帖子: POST /like/{postId}
 * - 取消点赞: DELETE /like/{postId}
 * - 获取点赞状态: GET /like/{postId}/status（可选）
 * - 获取点赞数: GET /like/{postId}/count（可选）
 */

/**
 * 点赞帖子
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回 { liked: true, likeCount: number }
 */
export function likePost(postId) {
  return request({
    url: `/like/${postId}`,
    method: 'post'
  })
}

/**
 * 取消点赞
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回 { unliked: true, likeCount: number }
 */
export function unlikePost(postId) {
  return request({
    url: `/like/${postId}`,
    method: 'delete'
  })
}

/**
 * 获取帖子的点赞状态
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回 { isLiked: boolean }
 */
export function getLikeStatus(postId) {
  return request({
    url: `/like/${postId}/status`,
    method: 'get'
  })
}

/**
 * 获取帖子的点赞总数
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回 { likeCount: number }
 */
export function getLikeCount(postId) {
  return request({
    url: `/like/${postId}/count`,
    method: 'get'
  })
}

