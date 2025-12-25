import request from '../utils/request'

/**
 * 系统通知相关 API
 * 
 * API 接口文档：
 * - 获取通知列表: GET /system-notification/list
 * - 获取未读通知: GET /system-notification/unread
 * - 按类型获取通知: GET /system-notification/type/{type}
 * - 获取未读通知数: GET /system-notification/unread/count
 * - 获取类型统计: GET /system-notification/unread/count/by-type
 * - 标记通知为已读: PUT /system-notification/{id}/read
 * - 批量标记已读: PUT /system-notification/read/batch
 * - 标记所有通知为已读: PUT /system-notification/read/all
 * - 按类型标记已读: PUT /system-notification/read/all/{type}
 * - 删除通知: DELETE /system-notification/{id}
 * - 清理旧通知: DELETE /system-notification/cleanup
 */

/**
 * 获取通知列表（分页）
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 20）
 * @returns {Promise} 返回通知列表
 */
export function getNotificationList(page = 1, size = 20) {
  return request({
    url: '/system-notification/list',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取未读通知列表（分页）
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 20）
 * @returns {Promise} 返回未读通知列表
 */
export function getUnreadNotifications(page = 1, size = 20) {
  return request({
    url: '/system-notification/unread',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 按类型获取通知列表
 * @param {string} type - 通知类型（LIKE/COMMENT/MENTION/FOLLOW/REPOST）
 * @param {number} page - 页码（默认 1）
 * @param {number} size - 每页数量（默认 20）
 * @returns {Promise} 返回指定类型的通知列表
 */
export function getNotificationsByType(type, page = 1, size = 20) {
  return request({
    url: `/system-notification/type/${type}`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取未读通知总数
 * @returns {Promise} 返回未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/system-notification/unread/count',
    method: 'get'
  })
}

/**
 * 获取各类型未读通知统计
 * @returns {Promise} 返回各类型未读数 { LIKE: 3, COMMENT: 2, FOLLOW: 1, ... }
 */
export function getUnreadCountByType() {
  return request({
    url: '/system-notification/unread/count/by-type',
    method: 'get'
  })
}

/**
 * 标记单个通知为已读
 * @param {number|string} notificationId - 通知ID
 * @returns {Promise} 返回标记结果
 */
export function markAsRead(notificationId) {
  return request({
    url: `/system-notification/${notificationId}/read`,
    method: 'put'
  })
}

/**
 * 批量标记通知为已读
 * @param {Array<number>} notificationIds - 通知ID数组
 * @returns {Promise} 返回标记结果
 */
export function markBatchAsRead(notificationIds) {
  return request({
    url: '/system-notification/read/batch',
    method: 'put',
    data: notificationIds
  })
}

/**
 * 标记所有通知为已读
 * @returns {Promise} 返回标记结果
 */
export function markAllAsRead() {
  return request({
    url: '/system-notification/read/all',
    method: 'put'
  })
}

/**
 * 标记某类型的所有通知为已读
 * @param {string} type - 通知类型（LIKE/COMMENT/MENTION/FOLLOW/REPOST）
 * @returns {Promise} 返回标记结果
 */
export function markTypeAsRead(type) {
  return request({
    url: `/system-notification/read/all/${type}`,
    method: 'put'
  })
}

/**
 * 删除单个通知
 * @param {number|string} notificationId - 通知ID
 * @returns {Promise} 返回删除结果
 */
export function deleteNotification(notificationId) {
  return request({
    url: `/system-notification/${notificationId}`,
    method: 'delete'
  })
}

/**
 * 清理已读的旧通知
 * @param {number} days - 清理多少天前的通知（默认 30）
 * @returns {Promise} 返回清理结果
 */
export function cleanupOldNotifications(days = 30) {
  return request({
    url: '/system-notification/cleanup',
    method: 'delete',
    params: { days }
  })
}
