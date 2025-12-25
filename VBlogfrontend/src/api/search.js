import request from '../utils/request'

/**
 * 搜索相关 API
 * 
 * API 接口文档：
 * - 搜索用户: GET /search/users?q=keyword&page=1&size=10
 * - 搜索帖子: GET /search/posts?q=keyword&page=1&size=10
 * - 获取热门搜索话题: GET /search/topics/hot
 */

/**
 * 搜索用户
 * @param {string} keyword - 搜索关键词
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回 { list: [], total: 0, page: 1, size: 10 }
 */
export function searchUsers(keyword, page = 1, size = 10) {
  return request({
    url: '/search/users',
    method: 'get',
    params: { 
      q: keyword, 
      page, 
      size 
    }
  })
}

/**
 * 搜索帖子
 * @param {string} keyword - 搜索关键词
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 10）
 * @returns {Promise} 返回 { list: [], total: 0, page: 1, size: 10 }
 */
export function searchPosts(keyword, page = 1, size = 10) {
  return request({
    url: '/search/posts',
    method: 'get',
    params: { 
      q: keyword, 
      page, 
      size 
    }
  })
}

/**
 * 获取热门搜索话题
 * @param {number} limit - 返回数量限制（默认 10）
 * @returns {Promise} 返回热门话题列表
 */
export function getHotSearchTopics(limit = 10) {
  return request({
    url: '/search/topics/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取搜索历史记录（本地存储）
 * @returns {Promise} 返回搜索历史数组
 */
export function getSearchHistory() {
  // 从 localStorage 获取搜索历史
  const history = localStorage.getItem('searchHistory')
  return Promise.resolve(history ? JSON.parse(history) : [])
}

/**
 * 添加搜索历史记录（本地存储）
 * @param {string} keyword - 搜索关键词
 */
export function addSearchHistory(keyword) {
  const history = JSON.parse(localStorage.getItem('searchHistory') || '[]')
  if (keyword && !history.includes(keyword)) {
    history.unshift(keyword)
    if (history.length > 10) {
      history.pop() // 只保留最近10条
    }
    localStorage.setItem('searchHistory', JSON.stringify(history))
  }
}

/**
 * 清空搜索历史记录（本地存储）
 */
export function clearSearchHistory() {
  localStorage.removeItem('searchHistory')
  return Promise.resolve()
}

