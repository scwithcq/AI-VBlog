import boyAvatar from '../assets/Images/boy.png'
import girlAvatar from '../assets/Images/girl.png'
import smileAvatar from '../assets/Images/smilepeople.png'

/**
 * 图片 URL 处理工具
 *
 * 后端返回的图片路径格式：/uploads/images/uuid.jpg
 * 需要拼接完整的访问路径才能正确显示
 */

/**
 * 获取完整的图片访问路径
 * @param {string} imagePath - 后端返回的图片路径（例如：/uploads/images/uuid.jpg）
 * @returns {string} 完整的图片访问路径
 */
export function getImageUrl(imagePath) {
  if (typeof imagePath !== 'string' || !imagePath.trim()) {
    return ''
  }
  if (imagePath === '/uploads/default-avatar.png') {
    return '/default-avatar.jpg'
  }

  // Migrate old local development URLs to the current site.
  if (/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?\//i.test(imagePath)) {
    imagePath = new URL(imagePath).pathname
  } else if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }

  const presets = { boy: boyAvatar, girl: girlAvatar, smilepeople: smileAvatar }
  const preset = imagePath.match(/^(?:\/src\/assets\/Images\/|\/avatars\/)(boy|girl|smilepeople)\.png$/)
  if (preset) return presets[preset[1]]

  const path = imagePath.startsWith('/') ? imagePath : `/${imagePath}`
  return path.startsWith('/uploads/') ? `/api${path}` : path
}

/**
 * 处理图片数组，转换为完整 URL 数组
 * @param {string|Array<string>} images - 图片路径（JSON 字符串或数组）
 * @returns {Array<string>} 完整的图片 URL 数组
 */
export function parseImages(images) {
  if (!images) {
    return []
  }

  try {
    // 如果是字符串，尝试解析为 JSON
    const imageArray = typeof images === 'string' ? JSON.parse(images) : images

    // 确保是数组
    if (!Array.isArray(imageArray)) {
      return []
    }

    // 转换每个路径为完整 URL
    return imageArray.map(img => getImageUrl(img))
  } catch (error) {
    console.error('解析图片数据失败:', error)
    return []
  }
}

/**
 * 验证图片文件类型
 * @param {File} file - 文件对象
 * @returns {boolean} 是否为有效的图片文件
 */
export function isValidImage(file) {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  return validTypes.includes(file.type)
}

/**
 * 验证图片文件大小
 * @param {File} file - 文件对象
 * @param {number} maxSize - 最大文件大小（MB），默认 5MB
 * @returns {boolean} 是否符合大小限制
 */
export function isValidImageSize(file, maxSize = 5) {
  const maxSizeBytes = maxSize * 1024 * 1024
  return file.size <= maxSizeBytes
}

/**
 * 压缩图片（如果需要）
 * @param {File} file - 原始图片文件
 * @param {number} maxWidth - 最大宽度
 * @param {number} quality - 压缩质量 (0-1)
 * @returns {Promise<Blob>} 压缩后的图片 Blob
 */
export function compressImage(file, maxWidth = 1920, quality = 0.8) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()

    reader.onload = (e) => {
      const img = new Image()

      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height

        // 如果图片宽度超过最大宽度，等比例缩放
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }

        canvas.width = width
        canvas.height = height

        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)

        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('图片压缩失败'))
            }
          },
          file.type,
          quality
        )
      }

      img.onerror = () => {
        reject(new Error('图片加载失败'))
      }

      img.src = e.target.result
    }

    reader.onerror = () => {
      reject(new Error('文件读取失败'))
    }

    reader.readAsDataURL(file)
  })
}

// Normalize avatar fields consistently for posts, comments, friends and notifications.
export function normalizeMediaFields(value) {
  if (!value || typeof value !== 'object') return value
  if (Array.isArray(value)) return value.map(normalizeMediaFields)
  return Object.fromEntries(Object.entries(value).map(([key, item]) => [
    key, /avatar$/i.test(key) ? getImageUrl(item) || '/default-avatar.jpg' : normalizeMediaFields(item)
  ]))
}
