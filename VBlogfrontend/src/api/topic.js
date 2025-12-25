import request from '../utils/request'

/**
 * 话题相关 API
 * 
 * API 接口文档：
 * - 获取热门话题: GET /topic/hot
 * - 根据名称获取话题: GET /topic/name/{name}
 * - 根据话题名称获取帖子: GET /topic/name/{name}/posts
 */

/**
 * 获取热门话题列表
 * @param {number} limit - 返回数量，默认 10
 * @returns {Promise} 返回热门话题列表
 */
export function getHotTopics(limit = 10) {
  return request({
    url: '/topic/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 根据名称获取话题
 * @param {string} name - 话题名称
 * @returns {Promise} 返回话题信息
 */
export function getTopicByName(name) {
  return request({
    url: `/topic/name/${encodeURIComponent(name)}`,
    method: 'get'
  })
}

/**
 * 根据话题名称获取帖子
 * @param {string} name - 话题名称
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回该话题的帖子列表
 */
export function getPostsByTopicName(name, page = 1, size = 10) {
  return request({
    url: `/topic/name/${encodeURIComponent(name)}/posts`,
    method: 'get',
    params: { page, size }
  })
}


