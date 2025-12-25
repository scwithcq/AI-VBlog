import request from '../utils/request'

/**
 * 发送消息
 */
export const sendMessage = (data) => {
  return request({
    url: '/message/send',
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data
  })
}

/**
 * 获取会话消息列表
 */
export const getConversationMessages = (conversationId, params) => {
  return request({
    url: `/message/conversation/${conversationId}`,
    method: 'GET',
    params
  })
}

/**
 * 获取会话列表
 */
export const getConversations = () => {
  return request({
    url: '/message/conversations',
    method: 'GET'
  })
}

/**
 * 标记会话中消息为已读
 */
export const markConversationRead = (conversationId) => {
  return request({
    url: `/message/conversation/${conversationId}/read`,
    method: 'PUT'
  })
}

/**
 * 撤回消息
 */
export const recallMessage = (messageId) => {
  return request({
    url: `/message/${messageId}/recall`,
    method: 'PUT'
  })
}

/**
 * 删除消息
 */
export const deleteMessage = (messageId) => {
  return request({
    url: `/message/${messageId}`,
    method: 'DELETE'
  })
}

/**
 * 获取未读消息总数
 */
export const getUnreadCount = () => {
  return request({
    url: '/message/unread/count',
    method: 'GET'
  })
}

/**
 * 生成会话ID
 */
export const generateConversationId = (friendId) => {
  return request({
    url: '/message/conversation/id',
    method: 'GET',
    params: { friendId }
  })
}
