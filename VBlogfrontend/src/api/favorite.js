import request from '../utils/request'

export const getFavoriteStatus = (postId) => request.get(`/favorite/${postId}/status`)
export const favoritePost = (postId) => request.post(`/favorite/${postId}`)
export const unfavoritePost = (postId) => request.delete(`/favorite/${postId}`)
export const getMyFavorites = (page = 1, size = 10) =>
  request.get('/favorite/my', { params: { page, size } })
