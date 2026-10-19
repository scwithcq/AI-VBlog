import request from '../utils/request'

// Cards mounted in the same render cycle share one status request.
let pending = []
let scheduled = false

function flush() {
  scheduled = false
  const batch = pending
  pending = []
  for (let start = 0; start < batch.length; start += 100) {
    const group = batch.slice(start, start + 100)
    const ids = [...new Set(group.map(item => item.postId))]
    request.get('/post/interactions', { params: { postIds: ids.join(',') } })
      .then(rows => {
        const byId = new Map((rows || []).map(row => [Number(row.postId), row]))
        group.forEach(item => item.resolve(byId.get(item.postId) || null))
      })
      .catch(error => group.forEach(item => item.reject(error)))
  }
}

export function getPostInteractionStatus(postId) {
  return new Promise((resolve, reject) => {
    pending.push({ postId: Number(postId), resolve, reject })
    if (!scheduled) {
      scheduled = true
      queueMicrotask(flush)
    }
  })
}
