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
  if (!imagePath) {
    return ''
  }

  // 如果已经是完整的 URL (http/https)，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }

  // 如果是相对路径，拼接 API 基础路径
  // 开发环境：http://localhost:8080/uploads/images/uuid.jpg
  // 生产环境：通过 Vite 代理转发到后端
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  
  // 确保路径以 / 开头
  const path = imagePath.startsWith('/') ? imagePath : `/${imagePath}`
  
  return `${baseURL}${path}`
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
