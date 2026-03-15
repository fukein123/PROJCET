const ALLOWED_TAGS = new Set([
  'P',
  'DIV',
  'BR',
  'STRONG',
  'EM',
  'U',
  'S',
  'SPAN',
  'UL',
  'OL',
  'LI',
  'BLOCKQUOTE',
  'H1',
  'H2',
  'H3',
  'H4',
  'H5',
  'H6',
  'HR',
  'A',
  'IMG',
  'TABLE',
  'THEAD',
  'TBODY',
  'TR',
  'TH',
  'TD',
  'PRE',
  'CODE',
  'VIDEO',
  'IFRAME'
])

const BLOCK_TAGS = new Set([
  'P',
  'DIV',
  'UL',
  'OL',
  'LI',
  'BLOCKQUOTE',
  'H1',
  'H2',
  'H3',
  'H4',
  'H5',
  'H6',
  'TABLE',
  'THEAD',
  'TBODY',
  'TR',
  'TH',
  'TD',
  'PRE'
])

const URL_PROTOCOLS = new Set(['http:', 'https:'])
const SAFE_STYLE_PROPERTIES = new Set([
  'text-align',
  'color',
  'background-color',
  'font-size',
  'font-family',
  'line-height'
])

const FONT_SIZE_MAP: Record<string, string> = {
  '1': '12px',
  '2': '13px',
  '3': '15px',
  '4': '18px',
  '5': '24px',
  '6': '30px',
  '7': '36px'
}

const TAG_ALIAS: Record<string, string> = {
  B: 'STRONG',
  I: 'EM',
  STRIKE: 'S',
  FONT: 'SPAN'
}

export function sanitizeRichText(value?: string | null) {
  if (!value) {
    return ''
  }

  const documentRef = createHtmlDocument(value)
  const container = documentRef.createElement('div')

  Array.from(documentRef.body.childNodes)
    .map((node) => sanitizeNode(node, documentRef))
    .filter((node): node is Node => Boolean(node))
    .forEach((node) => container.appendChild(node))

  return container.innerHTML.trim()
}

export function richTextToPlainText(value?: string | null) {
  const sanitized = sanitizeRichText(value)
  if (!sanitized) {
    return ''
  }

  const documentRef = createHtmlDocument(sanitized)
  return documentRef.body.textContent?.replace(/\s+/g, ' ').trim() || ''
}

export function hasMeaningfulRichText(value?: string | null) {
  const sanitized = sanitizeRichText(value)
  if (!sanitized) {
    return false
  }

  if (richTextToPlainText(sanitized)) {
    return true
  }

  return /<(img|video|iframe|table)\b/i.test(sanitized)
}

function createHtmlDocument(html: string) {
  if (typeof DOMParser !== 'undefined') {
    return new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html')
  }

  const fallback = document.implementation.createHTMLDocument('')
  fallback.body.innerHTML = html
  return fallback
}

function sanitizeNode(node: Node, documentRef: Document): Node | null {
  if (node.nodeType === Node.TEXT_NODE) {
    return documentRef.createTextNode(node.textContent || '')
  }

  if (node.nodeType !== Node.ELEMENT_NODE) {
    return null
  }

  const source = node as HTMLElement
  const sourceTag = source.tagName.toUpperCase()
  if (sourceTag === 'SCRIPT' || sourceTag === 'STYLE') {
    return null
  }

  const targetTag = TAG_ALIAS[sourceTag] || sourceTag
  if (!ALLOWED_TAGS.has(targetTag)) {
    const fragment = documentRef.createDocumentFragment()
    Array.from(source.childNodes)
      .map((child) => sanitizeNode(child, documentRef))
      .filter((child): child is Node => Boolean(child))
      .forEach((child) => fragment.appendChild(child))
    return fragment
  }

  const target = documentRef.createElement(targetTag)
  copySafeAttributes(source, target)

  Array.from(source.childNodes)
    .map((child) => sanitizeNode(child, documentRef))
    .filter((child): child is Node => Boolean(child))
    .forEach((child) => target.appendChild(child))

  if (targetTag === 'A' && !target.getAttribute('href')) {
    return cloneChildrenAsFragment(target, documentRef)
  }

  if (targetTag === 'IMG' && !target.getAttribute('src')) {
    return null
  }

  if (targetTag === 'VIDEO' && !target.getAttribute('src')) {
    return null
  }

  if (targetTag === 'IFRAME' && !target.getAttribute('src')) {
    return null
  }

  if (BLOCK_TAGS.has(targetTag) && !target.textContent?.trim() && !target.querySelector('img, video, iframe, table, hr, br')) {
    if (targetTag === 'TD' || targetTag === 'TH') {
      target.innerHTML = '&nbsp;'
      return target
    }
    if (targetTag === 'LI') {
      target.appendChild(documentRef.createElement('br'))
      return target
    }
  }

  return target
}

function copySafeAttributes(source: HTMLElement, target: HTMLElement) {
  const tagName = target.tagName.toUpperCase()
  if (tagName === 'A') {
    const href = normalizeUrl(source.getAttribute('href'))
    if (href) {
      target.setAttribute('href', href)
      target.setAttribute('target', '_blank')
      target.setAttribute('rel', 'noopener noreferrer nofollow')
    }
  }

  if (tagName === 'IMG') {
    const src = normalizeUrl(source.getAttribute('src'))
    if (src) {
      target.setAttribute('src', src)
    }
    const alt = (source.getAttribute('alt') || '').trim()
    if (alt) {
      target.setAttribute('alt', alt.slice(0, 120))
    }
  }

  if (tagName === 'VIDEO') {
    const src = normalizeUrl(source.getAttribute('src'))
    if (src) {
      target.setAttribute('src', src)
      target.setAttribute('controls', 'true')
      target.setAttribute('playsinline', 'true')
    }
  }

  if (tagName === 'IFRAME') {
    const src = normalizeEmbedUrl(source.getAttribute('src'))
    if (src) {
      target.setAttribute('src', src)
      target.setAttribute('loading', 'lazy')
      target.setAttribute('allowfullscreen', 'true')
      target.setAttribute('referrerpolicy', 'no-referrer')
    }
  }

  if (tagName === 'UL' || tagName === 'OL') {
    const listType = source.getAttribute('data-list')
    if (listType === 'check') {
      target.setAttribute('data-list', 'check')
    }
  }

  if (tagName === 'LI') {
    const checked = source.getAttribute('data-checked')
    if (checked === 'true' || checked === 'false') {
      target.setAttribute('data-checked', checked)
    }
  }

  const styleText = buildSafeStyle(source)
  if (styleText) {
    target.setAttribute('style', styleText)
  }

  if (source.tagName.toUpperCase() === 'FONT') {
    mergeFontAttributes(source, target)
  }
}

function buildSafeStyle(source: HTMLElement) {
  const pairs: string[] = []

  Array.from({ length: source.style.length }, (_, index) => source.style.item(index).toLowerCase())
    .filter((name) => SAFE_STYLE_PROPERTIES.has(name))
    .forEach((name) => {
      const rawValue = source.style.getPropertyValue(name)
      const normalized = normalizeStyleValue(name, rawValue)
      if (normalized) {
        pairs.push(`${name}:${normalized}`)
      }
    })

  return pairs.join(';')
}

function normalizeStyleValue(name: string, rawValue: string) {
  const value = rawValue.trim()
  if (!value) {
    return ''
  }

  if (name === 'text-align') {
    return ['left', 'center', 'right', 'justify'].includes(value) ? value : ''
  }

  if (name === 'color' || name === 'background-color') {
    return /^#[0-9a-f]{3,8}$/i.test(value) || /^rgba?\([^)]+\)$/i.test(value) ? value : ''
  }

  if (name === 'font-size') {
    return /^(\d+(\.\d+)?)(px|rem|em|%)$/i.test(value) ? value : ''
  }

  if (name === 'font-family') {
    const normalized = value.replace(/["']/g, '').trim()
    return /^[\w\s,-]+$/.test(normalized) ? normalized : ''
  }

  if (name === 'line-height') {
    return /^(\d+(\.\d+)?)(px|rem|em|%)?$/i.test(value) ? value : ''
  }

  return ''
}

function mergeFontAttributes(source: HTMLElement, target: HTMLElement) {
  const pairs = new Map<string, string>()
  const existing = target.getAttribute('style') || ''
  existing
    .split(';')
    .map((item) => item.trim())
    .filter(Boolean)
    .forEach((item) => {
      const [key, value] = item.split(':')
      if (key && value) {
        pairs.set(key.trim(), value.trim())
      }
    })

  const color = source.getAttribute('color')
  if (color && normalizeStyleValue('color', color)) {
    pairs.set('color', color.trim())
  }

  const face = source.getAttribute('face')
  if (face) {
    const normalizedFace = normalizeStyleValue('font-family', face)
    if (normalizedFace) {
      pairs.set('font-family', normalizedFace)
    }
  }

  const size = source.getAttribute('size')
  if (size && FONT_SIZE_MAP[size]) {
    pairs.set('font-size', FONT_SIZE_MAP[size])
  }

  const styleText = Array.from(pairs.entries())
    .map(([key, value]) => `${key}:${value}`)
    .join(';')

  if (styleText) {
    target.setAttribute('style', styleText)
  }
}

function normalizeUrl(rawUrl?: string | null) {
  const value = (rawUrl || '').trim()
  if (!value) {
    return ''
  }

  try {
    const parsed = new URL(value, 'http://localhost')
    if (!URL_PROTOCOLS.has(parsed.protocol)) {
      return ''
    }
    return parsed.href.replace('http://localhost', '')
  } catch {
    return ''
  }
}

function normalizeEmbedUrl(rawUrl?: string | null) {
  const href = normalizeUrl(rawUrl)
  if (!href) {
    return ''
  }

  try {
    const url = new URL(href, 'https://localhost')
    const host = url.hostname.replace(/^www\./, '')
    if (
      host === 'youtube.com' ||
      host === 'www.youtube.com' ||
      host === 'youtu.be' ||
      host === 'player.bilibili.com' ||
      host === 'bilibili.com' ||
      host === 'vimeo.com' ||
      host === 'player.vimeo.com'
    ) {
      return url.href.replace('https://localhost', '')
    }
  } catch {
    return ''
  }

  return ''
}

function cloneChildrenAsFragment(element: HTMLElement, documentRef: Document) {
  const fragment = documentRef.createDocumentFragment()
  Array.from(element.childNodes).forEach((child) => fragment.appendChild(child.cloneNode(true)))
  return fragment
}
