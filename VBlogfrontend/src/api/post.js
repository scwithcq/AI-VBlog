import request from '../utils/request'

/**
 * 帖子相关 API
 * 
 * API 接口文档：
 * - 发布帖子: POST /post/publish
 * - 获取帖子列表: GET /post/list
 * - 获取帖子详情: GET /post/{postId}
 * - 转发帖子: POST /post/repost（需要转发源帖子ID）
 */

/**
 * 发布帖子
 * @param {object} postData - 帖子数据对象
 * @param {string} postData.content - 帖子内容（可选）
 * @param {array} postData.images - 图片URL数组（可选）
 * @param {string} postData.videoUrl - 视频URL（可选）
 * @returns {Promise} 返回发布后的帖子信息
 */
export function publishPost(postData) {
  const params = new URLSearchParams()
  if (postData.content) params.append('content', postData.content)
  if (postData.images && postData.images.length > 0) {
    params.append('images', JSON.stringify(postData.images))
  }
  if (postData.videoUrl) params.append('videoUrl', postData.videoUrl)

  return request({
    url: '/post/publish',
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 获取帖子列表（分页）
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回 { list: [], total: 0, page: 1, size: 10 }
 */
export function getPostList(page = 1, size = 10) {
  return request({
    url: '/post/list',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取帖子详情
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回帖子完整信息
 */
export function getPostById(postId) {
  return request({
    url: `/post/${postId}`,
    method: 'get'
  })
}

/**
 * 获取指定用户的帖子列表
 * @param {number|string} userId - 用户ID
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回该用户的帖子列表
 */
export function getUserPosts(userId, page = 1, size = 10) {
  return request({
    url: `/post/user/${userId}`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取源帖子信息（用于转发时获取原帖子内容）
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回源帖子信息
 */
export function getSourcePost(postId) {
  return request({
    url: `/post/${postId}/source`,
    method: 'get'
  })
}

/**
 * 转发帖子
 * @param {number|string} sourceId - 源帖子ID
 * @param {string} content - 转发时的额外内容（可选）
 * @returns {Promise} 返回转发后的帖子信息
 */
export function repost(sourceId, content = '') {
  const params = new URLSearchParams()
  params.append('sourceId', sourceId)
  if (content) params.append('content', content)

  return request({
    url: '/post/repost',
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 删除帖子
 * @param {number|string} postId - 帖子ID
 * @returns {Promise} 返回删除结果
 */
export function deletePost(postId) {
  return request({
    url: `/post/${postId}`,
    method: 'delete'
  })
}

/**
 * 更新帖子
 * @param {number|string} postId - 帖子ID
 * @param {object} postData - 帖子数据对象
 * @param {string} postData.content - 帖子内容（可选）
 * @param {array} postData.images - 图片URL数组（可选）
 * @param {string} postData.videoUrl - 视频URL（可选）
 * @returns {Promise} 返回更新后的帖子信息
 */
export function updatePost(postId, postData) {
  const params = new URLSearchParams()
  if (postData.content) params.append('content', postData.content)
  if (postData.images && postData.images.length > 0) {
    params.append('images', JSON.stringify(postData.images))
  }
  if (postData.videoUrl) params.append('videoUrl', postData.videoUrl)

  return request({
    url: `/post/${postId}`,
    method: 'put',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 获取话题相关的帖子列表
 * @param {number|string} topicId - 话题ID
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回该话题的帖子列表
 */
export function getTopicPosts(topicId, page = 1, size = 10) {
  return request({
    url: `/post/topic/${topicId}`,
    method: 'get',
    params: { page, size }
  })
}