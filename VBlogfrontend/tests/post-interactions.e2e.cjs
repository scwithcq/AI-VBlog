// Run only against a local server connected to a disposable test database.
// PLAYWRIGHT_MODULE_PATH may point to an existing Playwright installation.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright')
const assert = require('node:assert/strict')
const path = require('node:path')

async function main() {
  assert.equal(process.env.VBLOG_E2E_DISPOSABLE, 'true', 'A disposable local database is required')
  const baseURL = process.env.VBLOG_E2E_BASE_URL || 'http://localhost:5173'
  assert.match(baseURL, /^http:\/\/(localhost|127\.0\.0\.1):\d+$/)
  const suffix = Date.now()
  const password = 'local-ui-test-password'
  async function api(endpoint, form, token) {
    const response = await fetch(`${baseURL}/api${endpoint}`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: new URLSearchParams(form)
    })
    const body = await response.json()
    assert.equal(body.code, 0, body.msg)
    return body.data
  }
  const alice = await api('/user/register', { username: `ui_alice_${suffix}`, password })
  const bob = await api('/user/register', { username: `ui_bob_${suffix}`, password })
  const browser = await chromium.launch({
    headless: true,
    ...(process.env.VBLOG_E2E_BROWSER_PATH ? { executablePath: process.env.VBLOG_E2E_BROWSER_PATH } : {})
  })
  const issues = []
  let currentPage
  try {
    // One browser context, two tabs: sessionStorage must isolate the two users.
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } })
    const first = await context.newPage()
    currentPage = first
    const second = await context.newPage()
    for (const page of [first, second]) {
      page.on('pageerror', error => issues.push(error.message))
      page.on('console', message => { if (message.type() === 'error') issues.push(message.text()) })
      page.on('response', async response => {
        if (!response.url().includes('/api/') || response.request().resourceType() === 'image') return
        try {
          const result = await response.json()
          if (result.code !== undefined && result.code !== 0 && result.code !== 200) {
            issues.push(`${response.request().method()} ${new URL(response.url()).pathname}: ${result.msg}`)
          }
        } catch {}
      })
    }
    async function login(page, actor) {
      await page.goto(`${baseURL}/login`)
      await page.getByPlaceholder('请输入用户名', { exact: true }).fill(actor.user.username)
      await page.getByPlaceholder('请输入密码', { exact: true }).fill(password)
      await page.getByRole('button', { name: /登\s*录/ }).click()
      await page.waitForURL(`${baseURL}/`)
      await page.locator('.post-editor').waitFor()
    }
    await login(first, alice)
    const firstText = `first-post-${suffix}`
    const secondText = `second-post-${suffix}`
    for (const text of [firstText, secondText]) {
      await first.locator('.post-editor textarea').fill(text)
      await first.locator('.post-editor .publish-btn').click()
      await first.locator('.post-card').filter({ hasText: text }).waitFor()
    }
    await login(second, bob)
    const cardOne = second.locator('.post-card').filter({ hasText: firstText })
    const cardTwo = second.locator('.post-card').filter({ hasText: secondText })
    async function likes(card, count) {
      await card.locator('.like-action').getByText(`点赞 ${count}`, { exact: true }).waitFor()
    }
    await likes(cardOne, 0)
    await likes(cardTwo, 0)
    await cardOne.locator('.like-action').click()
    await likes(cardOne, 1)
    await likes(cardTwo, 0)
    await cardTwo.locator('.like-action').click()
    await likes(cardTwo, 1)
    await likes(cardOne, 1)
    await cardOne.locator('.like-action').click()
    await likes(cardOne, 0)
    await likes(cardTwo, 1)
    await cardOne.getByRole('button', { name: '评论', exact: true }).click()
    await cardOne.locator('textarea').fill('comment-only-on-first')
    await cardOne.locator('.comment-input-section').getByRole('button', { name: '评论', exact: true }).click()
    await cardOne.getByText('comment-only-on-first', { exact: true }).waitFor()
    await cardTwo.getByRole('button', { name: '评论', exact: true }).click()
    await cardTwo.getByText('暂无评论', { exact: true }).waitFor()
    assert.equal(await cardTwo.getByText('comment-only-on-first', { exact: true }).count(), 0)
    await cardTwo.locator('textarea').fill('comment-only-on-second')
    await cardTwo.locator('.comment-input-section').getByRole('button', { name: '评论', exact: true }).click()
    await cardTwo.getByText('comment-only-on-second', { exact: true }).waitFor()
    assert.equal(await cardOne.getByText('comment-only-on-second', { exact: true }).count(), 0)
    await second.reload()
    await likes(cardOne, 0)
    await likes(cardTwo, 1)
    await cardOne.getByRole('button', { name: '评论', exact: true }).click()
    await cardOne.getByText('comment-only-on-first', { exact: true }).waitFor()
    assert.equal(await cardOne.getByText('comment-only-on-second', { exact: true }).count(), 0)
    await cardOne.locator('.comment-actions').getByRole('button', { name: /赞/ }).click()
    await cardOne.locator('.comment-actions').getByText('👍 已赞(1)', { exact: true }).waitFor()
    await second.reload()
    await cardOne.getByRole('button', { name: '评论', exact: true }).click()
    await cardOne.locator('.comment-actions').getByText('👍 已赞(1)', { exact: true }).waitFor()
    await first.goto(`${baseURL}/notification`)
    const notification = first.locator('.notification-item').filter({ hasText: '评论了你' }).first()
    await notification.waitFor()
    await notification.locator('.username').click()
    await first.waitForURL(`${baseURL}/profile/${bob.user.userId}`)
    await first.locator('.profile-container').getByText(bob.user.username, { exact: true }).first().waitFor()
    await first.goto(`${baseURL}/notification`)
    await first.locator('.notification-item').filter({ hasText: '评论了你' }).first().click()
    await first.waitForURL(/\/post\/\d+$/)
    await first.locator('.post-content-card').waitFor()
    await second.screenshot({ path: path.resolve(__dirname, '../../VBlogbackend/target/post-interactions-ui.png'), fullPage: true })
    assert.deepEqual(issues, [], 'No JavaScript errors or failed business requests are allowed')
    console.log('PASS: same-browser account isolation, independent post likes, comments, comment-like persistence, notification sender and post navigation; no page errors or failed API responses.')
  } catch (error) {
    console.error('Captured browser issues:', issues)
    if (currentPage) {
      console.error('Current page:', currentPage.url(), await currentPage.locator('body').innerText())
      await currentPage.screenshot({ path: path.resolve(__dirname, '../../VBlogbackend/target/post-interactions-failure.png'), fullPage: true })
    }
    throw error
  } finally {
    await browser.close()
  }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
