import request from '../utils/request'

/**
 * 评论相关 API
 * 
 * API 接口文档：
 * - 添加评论: POST /comment/add
 * - 获取帖子评论列表: GET /comment/post/{postId}
 * - 获取单个评论: GET /comment/{commentId}
 */

/**
 * 添加评论
 * @param {number|string} postId - 帖子ID
 * @param {string} content - 评论内容
 * @param {number|string} parentId - 父评论ID（回复评论时使用，可选）
 * @returns {Promise} 返回添加的评论信息
 */
export function addComment(postId, content, parentId = null) {
  const params = new URLSearchParams()
  params.append('postId', postId)
  params.append('content', content)
  if (parentId) params.append('parentId', parentId)

  return request({
    url: '/comment/add',
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 获取帖子的所有评论列表
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回评论树结构数组
 */
export function getCommentsByPostId(postId) {
  return request({
    url: `/comment/post/${postId}`,
    method: 'get'
  })
}

/**
 * 获取单个评论详情
 * @param {number|string} commentId - 评论ID
 * @returns {Promise} 返回评论详细信息
 */
export function getCommentById(commentId) {
  return request({
    url: `/comment/${commentId}`,
    method: 'get'
  })
}

/**
 * 删除评论
 * @param {number|string} commentId - 评论ID
 * @returns {Promise} 返回删除结果
 */
export function deleteComment(commentId) {
  return request({
    url: `/comment/${commentId}`,
    method: 'delete'
  })
}

/**
 * 点赞评论
 * @param {number|string} commentId - 评论id
 * @returns {Promise} 返回点赞结果
 */
export function likeComment(commentId) {
  return request({
    url: `/comment/like/${commentId}`,
    method: 'post'
  })
}

/**
 * 取消点赞评论
 * @param {number|string} commentId - 评论id
 * @returns {Promise} 返回取消点赞结果
 */
export function unlikeComment(commentId) {
  return request({
    url: `/comment/like/${commentId}`,
    method: 'delete'
  })
}

/**
 * 获取评论的点赞状态
 * @param {number|string} commentId - 评论ID
 * @returns {Promise} 返回 { isLiked: boolean, likeCount: number }
 */
export function getCommentLikeStatus(commentId) {
  return request({
    url: `/comment/like/${commentId}/status`,
    method: 'get'
  })
}

