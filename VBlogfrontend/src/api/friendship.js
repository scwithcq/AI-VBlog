import request from '../utils/request'

/**
 * 搜索用户
 */
export const searchUsers = (keyword) => {
  return request({
    url: '/friendship/search',
    method: 'GET',
    params: { keyword }
  })
}

/**
 * 发送好友申请
 */
export const sendFriendRequest = (data) => {
  return request({
    url: '/friendship/request',
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data
  })
}

/**
 * 获取收到的好友申请
 */
export const getReceivedRequests = (status) => {
  return request({
    url: '/friendship/requests/received',
    method: 'GET',
    params: status !== undefined ? { status } : {}
  })
}

/**
 * 获取发送的好友申请
 */
export const getSentRequests = (status) => {
  return request({
    url: '/friendship/requests/sent',
    method: 'GET',
    params: status !== undefined ? { status } : {}
  })
}

/**
 * 处理好友申请
 */
export const handleFriendRequest = (requestId, accept) => {
  return request({
    url: `/friendship/request/${requestId}`,
    method: 'PUT',
    params: { accept }
  })
}

/**
 * 获取好友列表
 */
export const getFriendList = () => {
  return request({
    url: '/friendship/friends',
    method: 'GET'
  })
}

/**
 * 检查好友关系
 */
export const checkFriendship = (userId) => {
  return request({
    url: `/friendship/check/${userId}`,
    method: 'GET'
  })
}

/**
 * 修改好友备注
 */
export const updateFriendRemark = (data) => {
  return request({
    url: '/friendship/remark',
    method: 'PUT',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data
  })
}

/**
 * 删除好友
 */
export const deleteFriend = (friendId) => {
  return request({
    url: `/friendship/friend/${friendId}`,
    method: 'DELETE'
  })
}

/**
 * 获取好友数量
 */
export const getFriendCount = () => {
  return request({
    url: '/friendship/count',
    method: 'GET'
  })
}
