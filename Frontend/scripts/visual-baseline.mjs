import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { chromium } from 'playwright'

const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5173'
const apiBaseUrl = process.env.API_BASE_URL || 'http://127.0.0.1:8080'
const outputDir = path.resolve(process.cwd(), process.env.VISUAL_OUTPUT_DIR || 'output/playwright/baseline')

const volunteerCredentials = {
  username: process.env.SMOKE_VOLUNTEER_USERNAME || process.env.SMOKE_USERNAME || 'volunteer',
  password: process.env.SMOKE_VOLUNTEER_PASSWORD || process.env.SMOKE_PASSWORD || '123456',
  role: 'VOLUNTEER'
}

const pages = [
  { key: 'portal-home', route: '/portal/home', label: '门户首页' },
  { key: 'portal-activities', route: '/portal/activities', label: '活动列表' },
  { key: 'portal-forum', route: '/portal/forum', label: '社区论坛' },
  { key: 'volunteer-home', route: '/volunteer/home', label: '志愿者首页', auth: 'volunteer' },
  { key: 'volunteer-activity-center', route: '/volunteer/activity-center', label: '活动中心', auth: 'volunteer' },
  { key: 'volunteer-profile', route: '/volunteer/profile', label: '个人中心', auth: 'volunteer' },
  { key: 'volunteer-my-posts', route: '/volunteer/my-posts', label: '我的帖子', auth: 'volunteer' }
]

async function ensureReachable(url) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Cannot reach ${url}: HTTP ${response.status}`)
  }
}

async function loginByApi() {
  const response = await fetch(`${apiBaseUrl}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(volunteerCredentials)
  })
  const payload = await response.json()
  if (!response.ok || payload?.code !== 0) {
    throw new Error(`Volunteer login failed: ${payload?.message || response.status}`)
  }
  return payload.data
}

async function injectVolunteerAuth(page, auth) {
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
  await page.evaluate((data) => {
    localStorage.setItem('cvs_token', data.token)
    localStorage.setItem('cvs_role', data.role)
    localStorage.setItem('cvs_username', data.username)
  }, auth)
}

async function capturePage(page, target) {
  const targetUrl = `${baseUrl}${target.route}`
  await page.goto(targetUrl, { waitUntil: 'networkidle' })
  await page.waitForTimeout(500)

  const outputPath = path.join(outputDir, `${target.key}.png`)
  await page.screenshot({
    path: outputPath,
    fullPage: true
  })

  return {
    key: target.key,
    label: target.label,
    route: target.route,
    file: path.relative(process.cwd(), outputPath).replaceAll('\\', '/')
  }
}

async function main() {
  await ensureReachable(baseUrl)
  await ensureReachable(`${apiBaseUrl}/api/content/home`)
  await fs.mkdir(outputDir, { recursive: true })

  const browser = await chromium.launch({ headless: process.env.VISUAL_HEADLESS !== 'false' })
  const page = await browser.newPage({
    viewport: {
      width: 1440,
      height: 1080
    }
  })

  const manifest = {
    generatedAt: new Date().toISOString(),
    baseUrl,
    apiBaseUrl,
    pages: []
  }

  const volunteerAuth = await loginByApi()

  for (const target of pages) {
    if (target.auth === 'volunteer') {
      await injectVolunteerAuth(page, volunteerAuth)
    } else {
      await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
      await page.evaluate(() => {
        localStorage.removeItem('cvs_token')
        localStorage.removeItem('cvs_role')
        localStorage.removeItem('cvs_username')
      })
    }

    const entry = await capturePage(page, target)
    manifest.pages.push(entry)
    console.log(`${target.key}: ${entry.file}`)
  }

  const manifestPath = path.join(outputDir, 'manifest.json')
  await fs.writeFile(manifestPath, JSON.stringify(manifest, null, 2))
  console.log(`manifest: ${path.relative(process.cwd(), manifestPath).replaceAll('\\', '/')}`)

  await browser.close()
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : error)
  process.exit(1)
})
