import { chromium } from 'playwright'

const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5173'
const username = process.env.SMOKE_USERNAME || 'volunteer'
const password = process.env.SMOKE_PASSWORD || '123456'

async function readToast(page) {
  const toast = page.locator('.el-message .el-message__content').last()
  await toast.waitFor({ state: 'visible', timeout: 6000 })
  return (await toast.textContent())?.trim() || ''
}

async function login(page) {
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => {
    localStorage.clear()
    sessionStorage.clear()
    localStorage.setItem('cvs:last-login-role', 'VOLUNTEER')
  })
  await page.context().clearCookies()
  await page.goto(`${baseUrl}/login`, { waitUntil: 'domcontentloaded' })

  for (let attempt = 1; attempt <= 2; attempt++) {
    await page.getByPlaceholder('请输入账号').fill(username)
    await page.getByPlaceholder('请输入密码').fill(password)
    const captcha = (await page.locator('button.captcha').innerText()).trim()
    await page.getByPlaceholder('输入右侧验证码').fill(captcha)
    await page.getByRole('button', { name: '登录' }).click()

    try {
      await page.waitForURL(/\/volunteer\/home/, { timeout: 10000 })
      return
    } catch {
      if (attempt === 2) {
        let message = ''
        try {
          message = await readToast(page)
        } catch {
          message = '登录失败，未进入志愿者首页'
        }
        throw new Error(message || '登录失败，未进入志愿者首页')
      }
      await page.locator('button.captcha').click()
    }
  }
}

async function applyActivity(page) {
  await page.goto(`${baseUrl}/portal/activities`, { waitUntil: 'domcontentloaded' })
  const applyButton = page.getByRole('button', { name: '立即报名' }).first()
  await applyButton.waitFor({ state: 'visible', timeout: 12000 })
  await applyButton.click()
  return readToast(page)
}

async function commentPost(page) {
  await page.goto(`${baseUrl}/portal/forum`, { waitUntil: 'domcontentloaded' })
  const firstInput = page.locator('.comment-input input').first()
  await firstInput.waitFor({ state: 'visible', timeout: 12000 })
  await firstInput.fill(`社区服务体验反馈 ${new Date().toISOString()}`)
  await page.locator('.comment-input .el-button', { hasText: '发布' }).first().click()
  return readToast(page)
}

async function logout(page) {
  await page.goto(`${baseUrl}/portal`, { waitUntil: 'domcontentloaded' })
  for (let i = 0; i < 2; i++) {
    const logoutButton = page.getByRole('button', { name: '退出' }).first()
    await logoutButton.waitFor({ state: 'visible', timeout: 12000 })
    await logoutButton.click()
    await page.waitForTimeout(800)
    const token = await page.evaluate(() => localStorage.getItem('cvs_token') || '')
    if (!token) {
      return
    }
  }
  throw new Error('退出失败：本地登录态仍存在')
}

const browser = await chromium.launch({ headless: true })
const page = await browser.newPage()

try {
  await login(page)
  const applyResult = await applyActivity(page)
  const commentResult = await commentPost(page)
  await logout(page)
  console.log('登录：成功')
  console.log(`报名：${applyResult}`)
  console.log(`评论：${commentResult}`)
  console.log('退出：成功')
} finally {
  await browser.close()
}
