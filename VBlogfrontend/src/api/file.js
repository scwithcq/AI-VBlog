import request from '../utils/request'

/**
 * 文件上传相关 API
 * 
 * API 接口文档：
 * - 上传图片: POST /file/upload/image (multipart/form-data)
 * - 上传视频: POST /file/upload/video (multipart/form-data)
 */

/**
 * 上传图片文件
 * @param {File} file - 图片文件对象
 * @returns {Promise} 返回 { url: 'upload/path/image.png' }
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/file/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传视频文件
 * @param {File} file - 视频文件对象
 * @returns {Promise} 返回 { url: '/uploads/videos/xxx.mp4' }
 */
export function uploadVideo(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/file/upload/video',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000 // 视频上传超时时间设置为5分钟
  })
}

/**
 * 通用文件上传
 * @param {FormData} formData - FormData 对象（已包含 file 字段）
 * @returns {Promise} 返回上传结果 { url: '...' }
 */
export function uploadFile(formData) {
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}