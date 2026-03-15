import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { chromium } from 'playwright'

const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5173'
const apiBaseUrl = process.env.API_BASE_URL || 'http://127.0.0.1:8080'
const outputDir = path.resolve(process.cwd(), process.env.FORUM_SMOKE_OUTPUT_DIR || 'output/playwright')
const testDataMarker = process.env.SMOKE_TEST_DATA_MARKER || `forum-smoke:${Date.now()}`
const coverPath = path.join(outputDir, 'forum-smoke-cover.png')
const undoShotPath = path.join(outputDir, 'forum-flow-undo.png')
const commentShotPath = path.join(outputDir, 'forum-flow-comment.png')
const resultPath = path.join(outputDir, 'forum-flow-result.json')

const volunteerCredentials = {
  username: process.env.SMOKE_VOLUNTEER_USERNAME || process.env.SMOKE_USERNAME || 'volunteer',
  password: process.env.SMOKE_VOLUNTEER_PASSWORD || process.env.SMOKE_PASSWORD || '123456',
  role: 'VOLUNTEER'
}

const adminCredentials = {
  username: process.env.SMOKE_ADMIN_USERNAME || 'admin',
  password: process.env.SMOKE_ADMIN_PASSWORD || '123456',
  role: 'ADMIN'
}

const tinyPngBase64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO1W7WQAAAAASUVORK5CYII='

function assertCondition(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

async function ensureReachable(url) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Cannot reach ${url}: HTTP ${response.status}`)
  }
}

async function apiRequest(endpoint, { method = 'GET', token, params, body, headers } = {}) {
  const url = new URL(endpoint, apiBaseUrl)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, String(value))
      }
    })
  }

  const response = await fetch(url, {
    method,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(headers || {})
    },
    body: body === undefined ? undefined : JSON.stringify(body)
  })

  const payload = await response.json()
  if (!response.ok || payload?.code !== 0) {
    throw new Error(payload?.message || `HTTP ${response.status}: ${method} ${url.pathname}`)
  }

  return payload.data
}

async function loginByApi(credentials) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    body: credentials
  })
}

async function writeTinyCover() {
  await fs.mkdir(outputDir, { recursive: true })
  await fs.writeFile(coverPath, Buffer.from(tinyPngBase64, 'base64'))
}

async function injectVolunteerAuth(page, session) {
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
  await page.evaluate(
    ({ auth, marker }) => {
      localStorage.clear()
      sessionStorage.clear()
      localStorage.setItem('cvs_token', auth.token)
      localStorage.setItem('cvs_role', auth.role)
      localStorage.setItem('cvs_username', auth.username)
      localStorage.setItem('cvs:last-login-role', auth.role)
      localStorage.setItem('cvs_test_data_marker', marker)
    },
    {
      auth: session,
      marker: testDataMarker
    }
  )
}

async function waitForToast(page, text) {
  const toast = page.locator('.el-message .el-message__content').filter({ hasText: text }).last()
  await toast.waitFor({ state: 'visible', timeout: 10000 })
  return (await toast.textContent())?.trim() || text
}

async function setRichEditorContent(locator, lines) {
  const text = lines.join('\n')
  await locator.click()

  try {
    await locator.fill(text)
  } catch {
    await locator.evaluate((node, payload) => {
      const html = payload.lines.map((line) => `<p>${line}</p>`).join('')
      node.innerHTML = html
      node.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: payload.text }))
    }, { lines, text })
  }
}

async function cleanupTaggedComments(adminSession, marker) {
  const pageData = await apiRequest('/api/content/comments/page', {
    token: adminSession.token,
    params: {
      current: 1,
      size: 200,
      targetType: 'POST',
      includeTestData: true
    }
  })

  const taggedIds = (pageData?.records || []).filter((item) => item.testDataTag === marker).map((item) => item.id)
  if (!taggedIds.length) {
    return 0
  }

  await apiRequest('/api/content/comments/batch-delete', {
    method: 'POST',
    token: adminSession.token,
    body: { ids: taggedIds }
  })

  return taggedIds.length
}

async function main() {
  await ensureReachable(baseUrl)
  await ensureReachable(`${apiBaseUrl}/api/content/home`)
  await writeTinyCover()

  const adminSession = await loginByApi(adminCredentials)
  const volunteerSession = await loginByApi(volunteerCredentials)

  const browser = await chromium.launch({
    headless: process.env.SMOKE_HEADLESS !== 'false'
  })

  const page = await browser.newPage({
    viewport: {
      width: 1440,
      height: 1080
    }
  })

  const result = {
    status: 'passed',
    generatedAt: new Date().toISOString(),
    testDataMarker,
    undoWindowSeconds: 30,
    coverUploadStatus: null,
    postStatus: null,
    postId: null,
    undoStatus: null,
    undoBannerText: '',
    commentStatus: null,
    commentTargetTitle: '',
    commentCleanupCount: 0,
    screenshots: {
      undo: path.relative(process.cwd(), undoShotPath).replaceAll('\\', '/'),
      comment: path.relative(process.cwd(), commentShotPath).replaceAll('\\', '/')
    }
  }

  try {
    await injectVolunteerAuth(page, volunteerSession)
    await page.goto(`${baseUrl}/portal/forum`, { waitUntil: 'networkidle' })

    await page.getByRole('button', { name: '我要发帖' }).first().click()

    const dialog = page.locator('.forum-composer-dialog').last()
    await dialog.waitFor({ state: 'visible', timeout: 10000 })

    const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
    const postTitle = `论坛冒烟验证 ${timestamp}`
    const postSummary = '用于验证论坛发帖模态、30 秒撤销提示与当前页评论联动。'
    const postLines = [
      '这是一次浏览器级冒烟验证。',
      '本次重点检查富文本编辑器、封面上传、发帖撤销与内联评论链路。',
      '如果这条帖子随后被撤销，说明限时撤销机制工作正常。'
    ]

    await dialog.getByRole('textbox', { name: '*帖子标题' }).fill(postTitle)
    await dialog.getByRole('textbox', { name: '*帖子摘要' }).fill(postSummary)

    const uploadPromise = page.waitForResponse(
      (response) => response.url().includes('/api/common/upload') && response.request().method() === 'POST',
      { timeout: 15000 }
    )
    await dialog.locator('input.el-upload__input[type="file"]').setInputFiles(coverPath)
    const uploadResponse = await uploadPromise
    result.coverUploadStatus = uploadResponse.status()
    assertCondition(uploadResponse.ok(), `Cover upload failed: HTTP ${uploadResponse.status()}`)

    await page.waitForFunction(() => {
      const preview = document.querySelector('.forum-composer-dialog .cover-preview')
      const src = preview?.getAttribute('src') || ''
      return Boolean(src) && !src.includes('forum-default-cover')
    })

    await setRichEditorContent(dialog.locator('.editor-area'), postLines)

    const submitPromise = page.waitForResponse(
      (response) =>
        response.url().includes('/api/content/forum/posts/my') && response.request().method() === 'POST',
      { timeout: 15000 }
    )
    await dialog.getByRole('button', { name: '提交审核' }).click()
    const submitResponse = await submitPromise
    result.postStatus = submitResponse.status()
    assertCondition(submitResponse.ok(), `Post submit failed: HTTP ${submitResponse.status()}`)

    const submitPayload = await submitResponse.json()
    result.postId = submitPayload?.data?.id ?? null
    assertCondition(Boolean(result.postId), 'Post submit succeeded but no post id returned')

    const undoBanner = page.locator('.timed-undo-notification')
    await undoBanner.waitFor({ state: 'visible', timeout: 10000 })
    result.undoBannerText = ((await undoBanner.textContent()) || '').replace(/\s+/g, ' ').trim()
    assertCondition(
      /30\s*秒内可撤销/.test(result.undoBannerText),
      `Undo banner did not show the 30 second window: ${result.undoBannerText}`
    )

    await page.screenshot({
      path: undoShotPath,
      fullPage: true
    })

    const undoPromise = page.waitForResponse(
      (response) =>
        response.url().includes(`/api/content/forum/posts/my/${result.postId}/undo`) &&
        response.request().method() === 'DELETE',
      { timeout: 15000 }
    )
    await page.getByRole('button', { name: '撤销发帖' }).click()
    const undoResponse = await undoPromise
    result.undoStatus = undoResponse.status()
    assertCondition(undoResponse.ok(), `Undo request failed: HTTP ${undoResponse.status()}`)
    await waitForToast(page, '帖子已撤销')

    const firstPostCard = page.locator('.post-card').first()
    result.commentTargetTitle = ((await firstPostCard.locator('h3').textContent()) || '').trim()
    assertCondition(Boolean(result.commentTargetTitle), 'Cannot determine the inline comment target title')

    await firstPostCard.getByRole('button', { name: '我要评论' }).click()

    const commentEditor = page.locator('.comment-inline .editor-area')
    await commentEditor.waitFor({ state: 'visible', timeout: 10000 })
    await setRichEditorContent(commentEditor, [
      `当前页评论冒烟验证：${testDataMarker}`,
      '评论区域展开、提交与成功提示正常。'
    ])

    await page.screenshot({
      path: commentShotPath,
      fullPage: true
    })

    const commentPromise = page.waitForResponse(
      (response) => response.url().includes('/api/content/comments') && response.request().method() === 'POST',
      { timeout: 15000 }
    )
    await page.getByRole('button', { name: '发布评论' }).click()
    const commentResponse = await commentPromise
    result.commentStatus = commentResponse.status()
    assertCondition(commentResponse.ok(), `Comment submit failed: HTTP ${commentResponse.status()}`)
    await waitForToast(page, '评论发布成功')
    await page.locator('.comment-inline').waitFor({ state: 'hidden', timeout: 10000 })
  } finally {
    await browser.close()
    result.commentCleanupCount = await cleanupTaggedComments(adminSession, testDataMarker)
    await fs.writeFile(resultPath, JSON.stringify(result, null, 2))
  }

  console.log(JSON.stringify(result, null, 2))
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : error)
  process.exit(1)
})
