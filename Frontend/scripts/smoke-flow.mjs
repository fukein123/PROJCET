import { chromium } from 'playwright'

const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5173'
const apiBaseUrl = process.env.API_BASE_URL || 'http://127.0.0.1:8080'
const volunteerUsername = process.env.SMOKE_VOLUNTEER_USERNAME || process.env.SMOKE_USERNAME || 'volunteer'
const volunteerPassword = process.env.SMOKE_VOLUNTEER_PASSWORD || process.env.SMOKE_PASSWORD || '123456'
const adminUsername = process.env.SMOKE_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.SMOKE_ADMIN_PASSWORD || '123456'
const headless = process.env.SMOKE_HEADLESS !== 'false'
const testDataMarker = process.env.SMOKE_TEST_DATA_MARKER || `smoke:${Date.now()}`
const TEST_DATA_MARKER_STORAGE_KEY = 'cvs_test_data_marker'
const DEFAULT_DYNAMIC_IMAGE =
  'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=900&q=80'

const UI = {
  usernamePlaceholder: '请输入账号',
  passwordPlaceholder: '请输入密码',
  captchaPlaceholder: '输入右侧验证码',
  loginButton: '登录',
  logoutButton: '退出',
  publishButton: '发布',
  favoriteButton: '收藏',
  searchButton: '查询',
  batchArchiveButton: '批量归档',
  batchRestoreButton: '恢复发布',
  batchDeleteButton: '批量删除',
  contentDynamicTab: '信息动态',
  contentNoticeTab: '系统公告'
}

function assertCondition(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

function formatDateTime(date) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`
}

function formatDisplayDateTime(value) {
  return typeof value === 'string' && value.length >= 16 ? value.slice(0, 16) : '-'
}

function formatDisplayRange(startTime, endTime) {
  const start = formatDisplayDateTime(startTime)
  const end = formatDisplayDateTime(endTime)
  if (start === '-' && end === '-') {
    return '-'
  }
  return `${start} - ${end}`
}

async function apiRequest(path, { method = 'GET', token, params, body, headers } = {}) {
  const url = new URL(path, apiBaseUrl)
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
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(headers || {})
    },
    body: body === undefined ? undefined : JSON.stringify(body)
  })

  let payload
  try {
    payload = await response.json()
  } catch {
    throw new Error(`Response is not JSON: ${method} ${url}`)
  }

  if (!response.ok) {
    const message = payload?.message || `HTTP ${response.status}`
    throw new Error(`${method} ${url.pathname} failed: ${message}`)
  }

  if (payload?.code !== 0) {
    throw new Error(payload?.message || `${method} ${url.pathname} business failure`)
  }

  return payload.data
}

async function loginByApi(username, password, role) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    body: { username, password, role }
  })
}

async function findActivityByTitle(title, { includeArchived = true } = {}) {
  const activityPage = await apiRequest('/api/activity/page', {
    params: {
      current: 1,
      size: 50,
      keyword: title,
      includeArchived
    }
  })

  return activityPage.records.find((item) => item.title === title) || null
}

async function prepareSmokeActivity(adminSession, overrides = {}) {
  const categories = await apiRequest('/api/activity/categories')
  const categoryId = overrides.categoryId ?? categories?.[0]?.id
  if (!categoryId) {
    throw new Error('Smoke setup failed: no activity category available')
  }

  const now = new Date()
  const draft = {
    title: overrides.title ?? `Smoke Activity ${Date.now()}`,
    categoryId,
    startTime: overrides.startTime ?? formatDateTime(new Date(now.getTime() + 5 * 60 * 1000)),
    endTime: overrides.endTime ?? formatDateTime(new Date(now.getTime() + 12 * 60 * 1000)),
    address: overrides.address ?? 'Community Service Center',
    targetCount: overrides.targetCount ?? 5,
    volunteerQuota: overrides.volunteerQuota ?? 5,
    content: overrides.content ?? 'Smoke flow audit and sign verification',
    description: overrides.description ?? 'Temporary activity for the smoke flow.',
    status: overrides.status ?? 'PUBLISHED'
  }

  await apiRequest('/api/activity', {
    method: 'POST',
    token: adminSession.token,
    body: draft
  })

  const activity = await findActivityByTitle(draft.title)
  if (!activity?.id) {
    throw new Error(`Smoke setup failed: created activity not found (${draft.title})`)
  }

  return { id: activity.id, ...draft }
}

async function updateSmokeActivity(adminSession, activity) {
  await apiRequest(`/api/activity/${activity.id}`, {
    method: 'PUT',
    token: adminSession.token,
    body: {
      title: activity.title,
      categoryId: activity.categoryId,
      startTime: activity.startTime,
      endTime: activity.endTime,
      address: activity.address,
      targetCount: activity.targetCount,
      volunteerQuota: activity.volunteerQuota,
      content: activity.content,
      description: activity.description,
      status: activity.status
    }
  })
}

async function findDynamicByTitle(title) {
  const dynamicPage = await apiRequest('/api/content/dynamics/page', {
    params: {
      current: 1,
      size: 50,
      keyword: title,
      onlyPublished: false
    }
  })

  return dynamicPage.records.find((item) => item.title === title) || null
}

async function prepareSmokeDynamic(adminSession) {
  const draft = {
    title: `Smoke Dynamic ${Date.now()}`,
    content: 'Smoke dynamic archive and restore verification.',
    imageUrl: DEFAULT_DYNAMIC_IMAGE,
    type: 'NEWS',
    status: 1
  }

  await apiRequest('/api/content/dynamics', {
    method: 'POST',
    token: adminSession.token,
    body: draft
  })

  const dynamic = await findDynamicByTitle(draft.title)
  if (!dynamic?.id) {
    throw new Error(`Smoke setup failed: created dynamic not found (${draft.title})`)
  }

  return { id: dynamic.id, ...draft }
}

async function findNoticeByTitle(title) {
  const noticePage = await apiRequest('/api/content/notices/page', {
    params: {
      current: 1,
      size: 200,
      onlyPublished: false
    }
  })

  return noticePage.records.find((item) => item.title === title) || null
}

async function prepareSmokeNotice(adminSession) {
  const draft = {
    title: `Smoke Notice ${Date.now()}`,
    content: 'Smoke notice archive and restore verification.',
    status: 1
  }

  await apiRequest('/api/content/notices', {
    method: 'POST',
    token: adminSession.token,
    body: draft
  })

  const notice = await findNoticeByTitle(draft.title)
  if (!notice?.id) {
    throw new Error(`Smoke setup failed: created notice not found (${draft.title})`)
  }

  return { id: notice.id, ...draft }
}

async function prepareSmokeCategories(adminSession) {
  const prefix = `Smoke Category ${Date.now()}`
  const drafts = [
    { name: `${prefix} A`, description: 'Temporary category for batch delete smoke', sort: 901, status: 1 },
    { name: `${prefix} B`, description: 'Temporary category for batch delete smoke', sort: 902, status: 1 }
  ]

  for (const draft of drafts) {
    await apiRequest('/api/activity/categories', {
      method: 'POST',
      token: adminSession.token,
      body: draft
    })
  }

  const categories = await apiRequest('/api/activity/categories')
  const created = drafts
    .map((draft) => categories.find((category) => category.name === draft.name))
    .filter((category) => category?.id)

  if (created.length !== drafts.length) {
    throw new Error(`Smoke setup failed: created categories not found (${prefix})`)
  }

  return created.map((category) => ({
    id: category.id,
    name: category.name
  }))
}

async function cleanupSmokeActivity(adminSession, activityId) {
  if (!activityId) {
    return
  }

  try {
    await apiRequest(`/api/activity/${activityId}`, {
      method: 'DELETE',
      token: adminSession.token
    })
  } catch (error) {
    console.warn(`cleanup-activity: ${(error && error.message) || error}`)
  }
}

async function cleanupSmokeDynamic(adminSession, dynamicId) {
  if (!dynamicId) {
    return
  }

  try {
    await apiRequest(`/api/content/dynamics/${dynamicId}`, {
      method: 'DELETE',
      token: adminSession.token
    })
  } catch (error) {
    console.warn(`cleanup-dynamic: ${(error && error.message) || error}`)
  }
}

async function cleanupSmokeNotice(adminSession, noticeId) {
  if (!noticeId) {
    return
  }

  try {
    await apiRequest(`/api/content/notices/${noticeId}`, {
      method: 'DELETE',
      token: adminSession.token
    })
  } catch (error) {
    console.warn(`cleanup-notice: ${(error && error.message) || error}`)
  }
}

async function cleanupSmokeFavorite(volunteerSession, activityId) {
  if (!activityId) {
    return
  }

  try {
    await apiRequest(`/api/content/favorites/${activityId}`, {
      method: 'DELETE',
      token: volunteerSession.token
    })
  } catch (error) {
    console.warn(`cleanup-favorite: ${(error && error.message) || error}`)
  }
}

async function cleanupSmokeCategories(adminSession, categories) {
  if (!categories?.length) {
    return
  }

  try {
    const allCategories = await apiRequest('/api/activity/categories')
    const existingIds = categories.map((item) => item.id).filter((id) => allCategories.some((category) => category.id === id))
    if (!existingIds.length) {
      return
    }

    await apiRequest('/api/activity/categories/batch-delete', {
      method: 'POST',
      token: adminSession.token,
      body: { ids: existingIds }
    })
  } catch (error) {
    console.warn(`cleanup-categories: ${(error && error.message) || error}`)
  }
}

async function listTaggedComments(adminSession, marker) {
  if (!marker) {
    return []
  }

  const commentPage = await apiRequest('/api/content/comments/page', {
    token: adminSession.token,
    params: {
      current: 1,
      size: 200,
      targetType: 'POST',
      includeTestData: true
    }
  })

  return (commentPage?.records || []).filter((item) => item.testDataTag === marker)
}

async function cleanupSmokeComments(adminSession, marker) {
  try {
    const taggedComments = await listTaggedComments(adminSession, marker)
    const ids = taggedComments.map((item) => item.id)
    if (!ids.length) {
      return
    }

    await apiRequest('/api/content/comments/batch-delete', {
      method: 'POST',
      token: adminSession.token,
      body: { ids }
    })
  } catch (error) {
    console.warn(`cleanup-comments: ${(error && error.message) || error}`)
  }
}

async function assertNoTaggedComments(adminSession, marker) {
  const taggedComments = await listTaggedComments(adminSession, marker)
  if (taggedComments.length) {
    throw new Error(`Comment cleanup verification failed: ${taggedComments.length} tagged comments remain`)
  }
}

async function readToast(page, previousText = '') {
  const toast = page.locator('.el-message .el-message__content').last()
  await toast.waitFor({ state: 'visible', timeout: 8000 })
  if (previousText) {
    await page.waitForFunction(
      (lastText) => {
        const messages = Array.from(document.querySelectorAll('.el-message .el-message__content'))
        const current = messages.at(-1)?.textContent?.trim() || ''
        return Boolean(current) && current !== lastText
      },
      previousText,
      { timeout: 8000 }
    )
  }
  return (await toast.textContent())?.trim() || ''
}

async function setAuthStorage(page, session) {
  await page.context().clearCookies()
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
  await page.evaluate(
    ({ auth, marker, markerStorageKey }) => {
      localStorage.clear()
      sessionStorage.clear()
      localStorage.setItem('cvs_token', auth.token)
      localStorage.setItem('cvs_role', auth.role)
      localStorage.setItem('cvs_username', auth.username)
      localStorage.setItem('cvs:last-login-role', auth.role)
      localStorage.setItem(markerStorageKey, marker)
    },
    {
      auth: session,
      marker: testDataMarker,
      markerStorageKey: TEST_DATA_MARKER_STORAGE_KEY
    }
  )
}

async function openAsRole(page, session, path) {
  await setAuthStorage(page, session)
  await page.goto(`${baseUrl}${path}`, { waitUntil: 'domcontentloaded' })
}

async function loginVolunteerByUi(page) {
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
  await page.evaluate(
    ({ marker, markerStorageKey }) => {
      localStorage.clear()
      sessionStorage.clear()
      localStorage.setItem('cvs:last-login-role', 'VOLUNTEER')
      localStorage.setItem(markerStorageKey, marker)
    },
    {
      marker: testDataMarker,
      markerStorageKey: TEST_DATA_MARKER_STORAGE_KEY
    }
  )
  await page.context().clearCookies()
  await page.goto(`${baseUrl}/login`, { waitUntil: 'domcontentloaded' })

  for (let attempt = 1; attempt <= 2; attempt++) {
    await page.getByPlaceholder(UI.usernamePlaceholder).fill(volunteerUsername)
    await page.getByPlaceholder(UI.passwordPlaceholder).fill(volunteerPassword)
    const captcha = (await page.locator('button.captcha').innerText()).trim()
    await page.getByPlaceholder(UI.captchaPlaceholder).fill(captcha)
    await page.getByRole('button', { name: UI.loginButton }).click()

    try {
      await page.waitForURL(/\/(portal|volunteer\/home)/, { timeout: 10000 })
      return
    } catch {
      if (attempt === 2) {
        let message = ''
        try {
          message = await readToast(page)
        } catch {
          message = 'Volunteer login failed'
        }
        throw new Error(message || 'Volunteer login failed')
      }
      await page.locator('button.captcha').click()
    }
  }
}

function actionButton(scope, text) {
  return scope.locator(`button:visible:has-text("${text}")`).first()
}

async function waitForTableRow(page, text) {
  const row = page.locator('.el-table__body tr:visible').filter({ hasText: text }).first()
  await row.waitFor({ state: 'visible', timeout: 12000 })
  return row
}

async function toggleRowSelection(row) {
  const checkbox = row.locator('.el-checkbox:visible').first()
  await checkbox.waitFor({ state: 'visible', timeout: 12000 })
  await checkbox.click()
}

async function confirmDialog(page, promptValue) {
  if (promptValue !== undefined) {
    const promptInput = page.locator('.el-message-box__input textarea, .el-message-box__input input').first()
    await promptInput.waitFor({ state: 'visible', timeout: 8000 })
    await promptInput.fill(promptValue)
  }

  const confirmButton = page.locator('.el-message-box__btns .el-button--primary').last()
  await confirmButton.waitFor({ state: 'visible', timeout: 8000 })
  await confirmButton.click()
}

async function applyPreparedActivity(page, activityId, previousToast = '') {
  await page.goto(`${baseUrl}/portal/activities?apply=${activityId}`, { waitUntil: 'domcontentloaded' })
  await confirmDialog(page, 'Smoke test volunteer is available for the full activity window')
  return readToast(page, previousToast)
}

async function addFavoriteFromActivityCenter(page, volunteerSession, activityTitle, previousToast = '') {
  await openAsRole(page, volunteerSession, '/volunteer/activity-center')
  const keywordInput = page.getByPlaceholder('活动名称').first()
  await keywordInput.waitFor({ state: 'visible', timeout: 12000 })
  await keywordInput.fill(activityTitle)
  await page.getByRole('button', { name: UI.searchButton }).click()

  const row = await waitForTableRow(page, activityTitle)
  await actionButton(row, UI.favoriteButton).click()
  await confirmDialog(page)
  return readToast(page, previousToast)
}

async function verifyFavoriteSnapshot(page, volunteerSession, snapshot) {
  await openAsRole(page, volunteerSession, '/volunteer/my-favorites')
  const row = await waitForTableRow(page, snapshot.expectedTitle)
  const rowText = ((await row.textContent()) || '').replace(/\s+/g, ' ')

  assertCondition(rowText.includes(snapshot.expectedTitle), 'Favorite snapshot verification failed: title is not rendered')
  assertCondition(rowText.includes(snapshot.expectedAddress), 'Favorite snapshot verification failed: address is not rendered')
  assertCondition(rowText.includes(snapshot.expectedSchedule), 'Favorite snapshot verification failed: schedule is not rendered')
  assertCondition(
    !rowText.includes(snapshot.unexpectedTitle),
    'Favorite snapshot verification failed: current activity title leaked into favorites'
  )
  assertCondition(
    !rowText.includes(snapshot.unexpectedAddress),
    'Favorite snapshot verification failed: current activity address leaked into favorites'
  )
}

async function commentPost(page, previousToast = '') {
  await page.goto(`${baseUrl}/portal/forum`, { waitUntil: 'domcontentloaded' })
  const firstInput = page.locator('.comment-input input').first()
  await firstInput.waitFor({ state: 'visible', timeout: 12000 })
  await firstInput.fill(`Smoke forum feedback ${new Date().toISOString()}`)
  await page.locator('.comment-input .el-button', { hasText: UI.publishButton }).first().click()
  return readToast(page, previousToast)
}

async function approveApplication(page, adminSession, activityTitle, previousToast = '') {
  await openAsRole(page, adminSession, '/admin/application-audit')
  const row = await waitForTableRow(page, activityTitle)
  await row.locator('button:visible').first().click()
  await confirmDialog(page)
  return readToast(page, previousToast)
}

async function signInAndOut(page, volunteerSession, activityTitle, previousToast = '') {
  await openAsRole(page, volunteerSession, '/volunteer/apply-records')

  let row = await waitForTableRow(page, activityTitle)
  await row.locator('button:visible').first().click()
  const signInResult = await readToast(page, previousToast)

  await page.waitForTimeout(600)
  row = await waitForTableRow(page, activityTitle)
  await row.locator('button:visible').nth(1).click()
  const signOutResult = await readToast(page, signInResult)

  await openAsRole(page, volunteerSession, '/volunteer/check-records')
  await waitForTableRow(page, activityTitle)

  return { signInResult, signOutResult }
}

async function rebuildWeeklyRanking(page, adminSession, username, previousToast = '') {
  await openAsRole(page, adminSession, '/admin/weekly-ranking')
  const rebuildButton = page.locator('.head-right .el-button--primary').first()
  await rebuildButton.waitFor({ state: 'visible', timeout: 12000 })
  await rebuildButton.click()
  const rebuildResult = await readToast(page, previousToast)

  await waitForTableRow(page, username)
  return rebuildResult
}

async function archiveAndRestoreActivity(page, adminSession, activity, previousToast = '') {
  await openAsRole(page, adminSession, '/admin/activity-manage')

  let row = await waitForTableRow(page, activity.title)
  await toggleRowSelection(row)
  await actionButton(page, UI.batchArchiveButton).click()
  await confirmDialog(page)
  const archiveResult = await readToast(page, previousToast)
  const archivedActivity = await findActivityByTitle(activity.title)
  assertCondition(archivedActivity?.status === 'ARCHIVED', `Activity archive verification failed: ${activity.title}`)

  await page.waitForTimeout(500)
  row = await waitForTableRow(page, activity.title)
  await toggleRowSelection(row)
  await actionButton(page, UI.batchRestoreButton).click()
  await confirmDialog(page)
  const restoreResult = await readToast(page, archiveResult)
  const restoredActivity = await findActivityByTitle(activity.title)
  assertCondition(restoredActivity?.status === 'PUBLISHED', `Activity restore verification failed: ${activity.title}`)

  return { archiveResult, restoreResult }
}

async function openContentManageTab(page, adminSession, tabLabel) {
  await openAsRole(page, adminSession, '/admin/content-manage')
  if (tabLabel !== UI.contentDynamicTab) {
    const tab = page.locator(`.el-tabs__item:visible:has-text("${tabLabel}")`).first()
    await tab.waitFor({ state: 'visible', timeout: 12000 })
    await tab.click()
  }
}

async function archiveAndRestoreDynamic(page, adminSession, dynamic, previousToast = '') {
  await openContentManageTab(page, adminSession, UI.contentDynamicTab)

  let row = await waitForTableRow(page, dynamic.title)
  await toggleRowSelection(row)
  await actionButton(page, UI.batchArchiveButton).click()
  await confirmDialog(page)
  const archiveResult = await readToast(page, previousToast)
  const archivedDynamic = await findDynamicByTitle(dynamic.title)
  assertCondition(archivedDynamic?.status === 0, `Dynamic archive verification failed: ${dynamic.title}`)

  await page.waitForTimeout(500)
  row = await waitForTableRow(page, dynamic.title)
  await toggleRowSelection(row)
  await actionButton(page, UI.batchRestoreButton).click()
  await confirmDialog(page)
  const restoreResult = await readToast(page, archiveResult)
  const restoredDynamic = await findDynamicByTitle(dynamic.title)
  assertCondition(restoredDynamic?.status === 1, `Dynamic restore verification failed: ${dynamic.title}`)

  return { archiveResult, restoreResult }
}

async function archiveAndRestoreNotice(page, adminSession, notice, previousToast = '') {
  await openContentManageTab(page, adminSession, UI.contentNoticeTab)

  let row = await waitForTableRow(page, notice.title)
  await toggleRowSelection(row)
  await actionButton(page, UI.batchArchiveButton).click()
  await confirmDialog(page)
  const archiveResult = await readToast(page, previousToast)
  const archivedNotice = await findNoticeByTitle(notice.title)
  assertCondition(archivedNotice?.status === 0, `Notice archive verification failed: ${notice.title}`)

  await page.waitForTimeout(500)
  row = await waitForTableRow(page, notice.title)
  await toggleRowSelection(row)
  await actionButton(page, UI.batchRestoreButton).click()
  await confirmDialog(page)
  const restoreResult = await readToast(page, archiveResult)
  const restoredNotice = await findNoticeByTitle(notice.title)
  assertCondition(restoredNotice?.status === 1, `Notice restore verification failed: ${notice.title}`)

  return { archiveResult, restoreResult }
}

async function batchDeleteCategories(page, adminSession, categories, previousToast = '') {
  await openAsRole(page, adminSession, '/admin/activity-category')

  for (const category of categories) {
    const row = await waitForTableRow(page, category.name)
    await toggleRowSelection(row)
  }

  await actionButton(page, UI.batchDeleteButton).click()
  await confirmDialog(page)

  const batchDeleteResult = await readToast(page, previousToast)
  const categoryList = await apiRequest('/api/activity/categories')
  const remaining = categories.filter((item) => categoryList.some((category) => category.id === item.id))

  if (remaining.length) {
    throw new Error(`Batch delete verification failed: ${remaining.map((item) => item.name).join(', ')}`)
  }

  return batchDeleteResult
}

async function logout(page, volunteerSession) {
  await openAsRole(page, volunteerSession, '/portal')

  for (let attempt = 1; attempt <= 2; attempt++) {
    const logoutButton = page.getByRole('button', { name: UI.logoutButton }).first()
    await logoutButton.waitFor({ state: 'visible', timeout: 12000 })
    await logoutButton.click()
    await page.waitForTimeout(800)

    const token = await page.evaluate(() => localStorage.getItem('cvs_token') || '')
    if (!token) {
      return
    }
  }

  throw new Error('Logout failed: auth token still exists in localStorage')
}

const adminSession = await loginByApi(adminUsername, adminPassword, 'ADMIN')
const volunteerSession = await loginByApi(volunteerUsername, volunteerPassword, 'VOLUNTEER')

const now = Date.now()
const favoriteWindowStart = formatDateTime(new Date(now + 30 * 60 * 1000))
const favoriteWindowEnd = formatDateTime(new Date(now + 90 * 60 * 1000))
const favoriteWindowStartUpdated = formatDateTime(new Date(now + 24 * 60 * 60 * 1000))
const favoriteWindowEndUpdated = formatDateTime(new Date(now + 25 * 60 * 60 * 1000))

let smokeActivity
let favoriteSnapshotActivity
let smokeDynamic
let smokeNotice
let smokeCategories = []
let browser
const results = []
let executionError
let cleanupError

try {
  smokeActivity = await prepareSmokeActivity(adminSession)
  favoriteSnapshotActivity = await prepareSmokeActivity(adminSession, {
    title: `Smoke Favorite Snapshot ${now}`,
    startTime: favoriteWindowStart,
    endTime: favoriteWindowEnd,
    address: 'Harmony Garden Plaza',
    targetCount: 3,
    volunteerQuota: 3,
    content: 'Favorite snapshot regression verification',
    description: 'Temporary activity for favorite snapshot verification.'
  })
  smokeDynamic = await prepareSmokeDynamic(adminSession)
  smokeNotice = await prepareSmokeNotice(adminSession)
  smokeCategories = await prepareSmokeCategories(adminSession)

  browser = await chromium.launch({ headless })
  const page = await browser.newPage()
  let lastToastText = ''

  await loginVolunteerByUi(page)
  results.push(['login', 'success'])

  const applyResult = await applyPreparedActivity(page, smokeActivity.id, lastToastText)
  lastToastText = applyResult
  results.push(['activity', smokeActivity.title])
  results.push(['apply', applyResult])

  const favoriteResult = await addFavoriteFromActivityCenter(
    page,
    volunteerSession,
    favoriteSnapshotActivity.title,
    lastToastText
  )
  lastToastText = favoriteResult
  results.push(['favorite', favoriteResult])

  const favoriteSnapshotMutation = {
    ...favoriteSnapshotActivity,
    title: `${favoriteSnapshotActivity.title} Updated`,
    address: 'North Community Hub',
    startTime: favoriteWindowStartUpdated,
    endTime: favoriteWindowEndUpdated
  }
  await updateSmokeActivity(adminSession, favoriteSnapshotMutation)

  await verifyFavoriteSnapshot(page, volunteerSession, {
    expectedTitle: favoriteSnapshotActivity.title,
    expectedAddress: favoriteSnapshotActivity.address,
    expectedSchedule: formatDisplayRange(favoriteSnapshotActivity.startTime, favoriteSnapshotActivity.endTime),
    unexpectedTitle: favoriteSnapshotMutation.title,
    unexpectedAddress: favoriteSnapshotMutation.address
  })
  results.push(['favorite-snapshot', 'verified'])

  const commentResult = await commentPost(page, lastToastText)
  lastToastText = commentResult
  results.push(['comment', commentResult])

  const auditResult = await approveApplication(page, adminSession, smokeActivity.title, lastToastText)
  lastToastText = auditResult
  results.push(['audit', auditResult])

  const { signInResult, signOutResult } = await signInAndOut(page, volunteerSession, smokeActivity.title, lastToastText)
  lastToastText = signOutResult
  results.push(['sign-in', signInResult])
  results.push(['sign-out', signOutResult])

  const rankingResult = await rebuildWeeklyRanking(page, adminSession, volunteerUsername, lastToastText)
  lastToastText = rankingResult
  results.push(['weekly-ranking', rankingResult])

  const { archiveResult: activityArchiveResult, restoreResult: activityRestoreResult } = await archiveAndRestoreActivity(
    page,
    adminSession,
    smokeActivity,
    lastToastText
  )
  lastToastText = activityRestoreResult
  results.push(['activity-archive', activityArchiveResult])
  results.push(['activity-restore', activityRestoreResult])

  const { archiveResult: dynamicArchiveResult, restoreResult: dynamicRestoreResult } = await archiveAndRestoreDynamic(
    page,
    adminSession,
    smokeDynamic,
    lastToastText
  )
  lastToastText = dynamicRestoreResult
  results.push(['dynamic-archive', dynamicArchiveResult])
  results.push(['dynamic-restore', dynamicRestoreResult])

  const { archiveResult: noticeArchiveResult, restoreResult: noticeRestoreResult } = await archiveAndRestoreNotice(
    page,
    adminSession,
    smokeNotice,
    lastToastText
  )
  lastToastText = noticeRestoreResult
  results.push(['notice-archive', noticeArchiveResult])
  results.push(['notice-restore', noticeRestoreResult])

  const batchDeleteResult = await batchDeleteCategories(page, adminSession, smokeCategories, lastToastText)
  lastToastText = batchDeleteResult
  results.push(['batch-delete', batchDeleteResult])

  await logout(page, volunteerSession)
  results.push(['logout', 'success'])
} catch (error) {
  executionError = error
} finally {
  await browser?.close()

  try {
    await cleanupSmokeComments(adminSession, testDataMarker)
    await cleanupSmokeFavorite(volunteerSession, favoriteSnapshotActivity?.id)
    await cleanupSmokeDynamic(adminSession, smokeDynamic?.id)
    await cleanupSmokeNotice(adminSession, smokeNotice?.id)
    await cleanupSmokeActivity(adminSession, favoriteSnapshotActivity?.id)
    await cleanupSmokeActivity(adminSession, smokeActivity?.id)
    await cleanupSmokeCategories(adminSession, smokeCategories)
    await assertNoTaggedComments(adminSession, testDataMarker)
    results.push(['comment-cleanup', 'verified'])
  } catch (error) {
    cleanupError = error
  }
}

if (executionError && cleanupError) {
  executionError.message = `${executionError.message} | cleanup failed: ${cleanupError.message}`
  throw executionError
}

if (executionError) {
  throw executionError
}

if (cleanupError) {
  throw cleanupError
}

for (const [label, value] of results) {
  console.log(`${label}: ${value}`)
}
