// Use an isolated DB and upload directory; never run write tests on real user data.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

async function main() {
  assert.equal(process.env.VBLOG_E2E_DISPOSABLE, 'true')
  const base = process.env.VBLOG_E2E_BASE_URL || 'http://localhost:5173'
  assert.match(base, /^http:\/\/(localhost|127\.0\.0\.1):\d+$/)
  const suffix = Date.now()
  const password = 'media-chat-test-password'
  async function api(method, endpoint, actor, data) {
    const response = await fetch(`${base}/api${endpoint}`, {
      method,
      headers: { ...(actor ? { Authorization: `Bearer ${actor.token}` } : {}), ...(data ? { 'Content-Type': 'application/x-www-form-urlencoded' } : {}) },
      ...(data ? { body: new URLSearchParams(data) } : {})
    })
    const result = await response.json()
    assert.equal(result.code, 0, `${endpoint}: ${result.msg}`)
    return result.data
  }
  const alice = await api('POST', '/user/register', null, { username: `media_a_${suffix}`, password })
  const bob = await api('POST', '/user/register', null, { username: `media_b_${suffix}`, password })
  // Built-in avatars use stable public URLs after a production build.
  await fetch(`${base}/api/user/info`, { method: 'PUT', headers: { Authorization: `Bearer ${alice.token}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ avatar: '/avatars/boy.png' }) })
  const browser = await chromium.launch({ headless: true, executablePath: process.env.VBLOG_E2E_BROWSER_PATH })
  const errors = []
  const results = []
  const receivedFrames = []
  try {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } })
    const a = await context.newPage()
    const b = await context.newPage()
    for (const page of [a, b]) {
      page.setDefaultTimeout(15000)
      page.on('websocket', socket => socket.on('framereceived', frame => {
        try { receivedFrames.push(JSON.parse(frame.payload)) } catch {}
      }))
      page.on('pageerror', error => errors.push(error.message))
      page.on('console', msg => { if (msg.type() === 'error') errors.push(msg.text()) })
      page.on('response', async response => {
        if (response.status() >= 400) errors.push(`${response.status()} ${response.url()}`)
        if (response.url().includes('/api/') && response.headers()['content-type']?.includes('application/json')) {
          try { const json = await response.json(); if (json.code !== undefined && json.code !== 0 && json.code !== 200) errors.push(`${response.url()}: ${json.msg}`) } catch {}
        }
      })
    }
    async function login(page, actor) {
      await page.goto(`${base}/login`)
      await page.getByPlaceholder('请输入用户名', { exact: true }).fill(actor.user.username)
      await page.getByPlaceholder('请输入密码', { exact: true }).fill(password)
      await page.getByRole('button', { name: /登\s*录/ }).click()
      await page.waitForURL(`${base}/`)
      await page.locator('.post-editor').waitFor()
    }
    async function checkImages(page) {
      await page.waitForFunction(() => [...document.images].every(img => img.loading === 'lazy' || (img.complete && img.naturalWidth > 0)))
    }
    await login(a, alice)
    await checkImages(a)
    const resources = path.resolve(__dirname, '../../资源测试')
    const image = fs.readdirSync(resources).find(name => name.endsWith('.png'))
    const video = fs.readdirSync(resources).filter(name => name.endsWith('.mp4')).sort((x, y) => fs.statSync(path.join(resources, x)).size - fs.statSync(path.join(resources, y)).size)[0]
    const uploadInputs = a.locator('.post-editor input[type=file]')
    await uploadInputs.nth(0).setInputFiles(path.join(resources, image))
    await a.locator('.post-editor .image-item img').waitFor()
    await checkImages(a)
    await uploadInputs.nth(1).setInputFiles(path.join(resources, video))
    await a.locator('.post-editor video').waitFor()
    const text = `media-${suffix} #media-topic-${suffix}#`
    await a.locator('.post-editor textarea').fill(text)
    await a.locator('.post-editor .publish-btn').click()
    const card = a.locator('.post-card').filter({ hasText: text })
    await card.waitFor()
    await card.locator('.post-image').scrollIntoViewIfNeeded()
    await a.waitForFunction(() => [...document.querySelectorAll('.post-image img')].some(img => img.complete && img.naturalWidth > 0))
    const player = card.locator('video')
    await player.evaluate(async video => { video.muted = true; await video.play() })
    await a.waitForFunction(() => [...document.querySelectorAll('.post-card video')].some(v => v.currentTime > 0.2 && v.videoWidth > 0))
    await player.evaluate(video => { video.pause(); video.currentTime = Math.min(3, video.duration / 2) })
    results.push('UI image/video upload, published image decode, MP4 metadata/playback/seek, built-in avatar')
    await a.reload()
    await card.waitFor()
    await card.locator('video').evaluate(video => new Promise((resolve, reject) => {
      if (video.readyState >= 1) return resolve()
      video.addEventListener('loadedmetadata', resolve, { once: true })
      video.addEventListener('error', () => reject(new Error('video reload failed')), { once: true })
    }))
    await card.locator('.favorite-action').click()
    await card.locator('.favorite-action').getByText('收藏 1', { exact: true }).waitFor()
    await a.goto(`${base}/favorite`)
    await a.locator('.post-card').filter({ hasText: text }).waitFor()
    await a.goto(`${base}/topic/media-topic-${suffix}`)
    await a.getByText(text, { exact: true }).first().waitFor()
    await login(b, bob)
    const request = await api('POST', '/friendship/request', alice, { toUserId: bob.user.userId })
    await api('PUT', `/friendship/request/${request.requestId}`, bob, { accept: true })
    await a.goto(`${base}/message`)
    await b.goto(`${base}/message`)
    await a.getByTitle('好友列表', { exact: true }).click()
    await a.locator('.friend-item').filter({ hasText: bob.user.username }).getByRole('button', { name: '发消息' }).click()
    await a.getByPlaceholder('输入消息...').fill(`live-message-${suffix}`)
    await a.getByPlaceholder('输入消息...').press('Enter')
    const session = b.locator('.session-item').filter({ hasText: alice.user.username })
    await session.waitFor()
    await session.locator('.unread-badge').getByText('1', { exact: true }).waitFor()
    await session.click()
    await b.locator('.message-bubble').getByText(`live-message-${suffix}`, { exact: true }).waitFor()
    await b.waitForFunction(() => document.querySelectorAll('.session-item .unread-badge').length === 0)
    assert.equal((await api('GET', '/message/unread/count', bob)).count, 0)
    await b.getByPlaceholder('输入消息...').fill(`reply-${suffix}`)
    const readReceipt = a.waitForResponse(response => response.url().includes('/message/conversation/') && response.url().endsWith('/read') && response.request().method() === 'PUT')
    await b.getByPlaceholder('输入消息...').press('Enter')
    await a.locator('.message-bubble').getByText(`reply-${suffix}`, { exact: true }).waitFor()
    await readReceipt
    await a.waitForLoadState('networkidle')
    await a.waitForFunction(() => !document.querySelector('[title="私信"] .badge'))
    assert.equal((await api('GET', '/message/unread/count', alice)).count, 0)
    assert(receivedFrames.some(event => event.type === 'new_message' && event.data?.content === `reply-${suffix}`), 'Reply must arrive through WebSocket')
    const conversationId = (await api('GET', `/message/conversation/id?friendId=${bob.user.userId}`, alice)).conversationId
    const sentMessages = (await api('GET', `/message/conversation/${conversationId}`, alice)).records
    const toRecall = sentMessages.find(message => message.content === `live-message-${suffix}`)
    assert(toRecall)
    await api('PUT', `/message/${toRecall.messageId}/recall`, alice)
    await a.locator('.message-bubble').getByText('消息已撤回', { exact: true }).waitFor()
    await b.locator('.message-bubble').getByText('消息已撤回', { exact: true }).waitFor()
    assert.equal(await a.locator('.sidebar-header .username').textContent(), alice.user.username)
    assert.equal(await b.locator('.sidebar-header .username').textContent(), bob.user.username)
    results.push('same-browser two-user chat, real-time arrival, unread badge, opening conversation marks read, WebSocket reply and live recall')
    // Visit every remaining top-level page and load all images in its initial viewport.
    for (const route of [`/profile/${alice.user.userId}`, `/following/${alice.user.userId}`, `/followers/${alice.user.userId}`, '/search?q=media', '/notification', '/']) {
      await a.goto(`${base}${route}`)
      await a.waitForLoadState('networkidle')
      await checkImages(a)
    }
    results.push('profile, following, followers, search, notifications, favorites, topic and home routes')
    await a.screenshot({ path: path.resolve(__dirname, '../../VBlogbackend/target/media-regression-ui.png'), fullPage: true })
    assert.deepEqual(errors, [], 'No page errors, console errors, HTTP errors or business errors')
    console.log(JSON.stringify({ status: 'PASS', results, errors }, null, 2))
  } catch (error) {
    console.error('Browser errors:', errors)
    for (const page of browser.contexts().flatMap(c => c.pages())) console.error(page.url(), (await page.locator('body').innerText()).slice(-4500))
    throw error
  } finally { await browser.close() }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
