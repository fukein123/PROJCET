<template>
  <div class="rich-editor" :style="editorStyle">
    <div class="toolbar-shell">
      <div class="toolbar-row">
        <div class="toolbar-group toolbar-group-selects">
          <select class="tool-select" :value="toolbarState.block" @change="handleBlockChange">
            <option v-for="option in blockOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <select class="tool-select" :value="toolbarState.fontFamily" @change="handleFontFamilyChange">
            <option v-for="option in fontFamilyOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <select class="tool-select" :value="toolbarState.fontSize" @change="handleFontSizeChange">
            <option v-for="option in fontSizeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <select class="tool-select" :value="toolbarState.lineHeight" @change="handleLineHeightChange">
            <option v-for="option in lineHeightOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>

        <div class="toolbar-group">
          <button
            v-for="action in markActions"
            :key="action.key"
            type="button"
            class="tool-button tool-button-mark"
            :class="{ active: toolbarState[action.key] }"
            @mousedown.prevent
            @click="runAction(action.key)"
          >
            <span class="tool-letter" :class="`tool-letter-${action.key}`">{{ action.label }}</span>
          </button>
        </div>

        <div class="toolbar-group">
          <button
            v-for="action in historyActions"
            :key="action.key"
            type="button"
            class="tool-button"
            @mousedown.prevent
            @click="runAction(action.key)"
          >
            {{ action.label }}
          </button>
        </div>
      </div>

      <div class="toolbar-row">
        <div class="toolbar-group">
          <button
            v-for="action in listActions"
            :key="action.key"
            type="button"
            class="tool-button tool-button-wide"
            :class="{ active: toolbarState[action.key] }"
            @mousedown.prevent
            @click="runAction(action.key)"
          >
            {{ action.label }}
          </button>
        </div>

        <div class="toolbar-group">
          <button
            v-for="action in alignActions"
            :key="action.key"
            type="button"
            class="tool-button"
            :class="{ active: toolbarState.align === action.key }"
            @mousedown.prevent
            @click="runAction(action.key)"
          >
            {{ action.label }}
          </button>
        </div>

        <div class="toolbar-group toolbar-group-color">
          <label class="color-tool">
            <span>字色</span>
            <input type="color" :value="toolbarState.textColor" @input="handleTextColorChange" />
          </label>
          <label class="color-tool">
            <span>底色</span>
            <input type="color" :value="toolbarState.highlightColor" @input="handleHighlightColorChange" />
          </label>
        </div>

        <div class="toolbar-group">
          <button type="button" class="tool-button tool-button-wide" @mousedown.prevent @click="insertLink">链接</button>
          <button
            type="button"
            class="tool-button tool-button-wide"
            :disabled="imageUploading"
            @mousedown.prevent
            @click="openImagePicker"
          >
            {{ imageUploading ? '上传中...' : '图片' }}
          </button>
          <button type="button" class="tool-button tool-button-wide" @mousedown.prevent @click="insertVideo">视频</button>
          <button type="button" class="tool-button tool-button-wide" @mousedown.prevent @click="insertTable">表格</button>
          <button type="button" class="tool-button tool-button-wide" @mousedown.prevent @click="runAction('divider')">
            分隔线
          </button>
        </div>

        <div class="toolbar-group">
          <button
            v-for="item in emojiActions"
            :key="item"
            type="button"
            class="tool-button"
            @mousedown.prevent
            @click="insertEmoji(item)"
          >
            {{ item }}
          </button>
        </div>
      </div>

      <div class="toolbar-meta">
        <span class="toolbar-tip">安全白名单已扩展，标题、颜色、对齐、表格、图片和视频会在提交前自动清洗。</span>
      </div>
    </div>

    <input
      ref="fileInputRef"
      class="image-input"
      type="file"
      accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
      @change="handleImagePick"
    />

    <div
      ref="editorRef"
      class="editor-area"
      contenteditable="true"
      :data-placeholder="placeholder"
      @input="handleInput"
      @blur="handleBlur"
      @focus="handleFocus"
      @keyup="syncToolbarState"
      @mouseup="syncToolbarState"
      @paste="handlePaste"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadImageApi } from '@/api/common'
import { sanitizeRichText } from '@/utils/rich-text'
import { validateImageFile } from '@/utils/upload'

type BlockActionKey = 'paragraph' | 'h2' | 'h3' | 'h4' | 'quote'
type MarkActionKey = 'bold' | 'italic' | 'underline' | 'strikeThrough'
type ListActionKey = 'ul' | 'ol' | 'checklist'
type AlignActionKey = 'left' | 'center' | 'right' | 'justify'
type HistoryActionKey = 'undo' | 'redo'
type ToolbarActionKey =
  | BlockActionKey
  | MarkActionKey
  | ListActionKey
  | AlignActionKey
  | HistoryActionKey
  | 'divider'

interface OptionItem<T extends string> {
  value: T
  label: string
}

const props = withDefaults(
  defineProps<{
    modelValue?: string
    placeholder?: string
    minHeight?: number
  }>(),
  {
    modelValue: '',
    placeholder: '请输入正文内容',
    minHeight: 220
  }
)

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void
  (event: 'blur'): void
}>()

const blockOptions: OptionItem<BlockActionKey>[] = [
  { value: 'paragraph', label: '正文' },
  { value: 'h2', label: '大标题' },
  { value: 'h3', label: '中标题' },
  { value: 'h4', label: '小标题' },
  { value: 'quote', label: '引用块' }
]

const fontFamilyOptions = [
  { value: 'default', label: '默认字体', match: ['system', 'yahei', 'sans'] },
  { value: 'Microsoft YaHei', label: '微软雅黑', match: ['yahei'] },
  { value: 'Source Han Sans SC', label: '思源黑体', match: ['source han sans', '思源黑体'] },
  { value: 'KaiTi', label: '楷体', match: ['kaiti', '楷体'] },
  { value: 'Georgia', label: 'Georgia', match: ['georgia'] }
] as const

const fontSizeOptions = [
  { value: '2', label: '13px', size: 13 },
  { value: '3', label: '15px', size: 15 },
  { value: '4', label: '18px', size: 18 },
  { value: '5', label: '24px', size: 24 },
  { value: '6', label: '30px', size: 30 },
  { value: '7', label: '36px', size: 36 }
] as const

const lineHeightOptions = [
  { value: '1.5', label: '行距 1.5' },
  { value: '1.8', label: '行距 1.8' },
  { value: '2', label: '行距 2.0' },
  { value: '2.4', label: '行距 2.4' }
] as const

const markActions: Array<{ key: MarkActionKey; label: string }> = [
  { key: 'bold', label: 'B' },
  { key: 'italic', label: 'I' },
  { key: 'underline', label: 'U' },
  { key: 'strikeThrough', label: 'S' }
]

const listActions: Array<{ key: ListActionKey; label: string }> = [
  { key: 'ul', label: '无序列表' },
  { key: 'ol', label: '有序列表' },
  { key: 'checklist', label: '清单' }
]

const alignActions: Array<{ key: AlignActionKey; label: string }> = [
  { key: 'left', label: '左对齐' },
  { key: 'center', label: '居中' },
  { key: 'right', label: '右对齐' },
  { key: 'justify', label: '两端对齐' }
]

const historyActions: Array<{ key: HistoryActionKey; label: string }> = [
  { key: 'undo', label: '撤销' },
  { key: 'redo', label: '重做' }
]

const emojiActions = ['😀', '🎉', '🌱', '❤️']
const textBlocksSelector = 'p, div, h1, h2, h3, h4, h5, h6, blockquote, li, pre, td, th'

const editorRef = ref<HTMLDivElement>()
const fileInputRef = ref<HTMLInputElement>()
const savedRange = ref<Range | null>(null)
const imageUploading = ref(false)
const toolbarState = reactive({
  block: 'paragraph' as BlockActionKey,
  fontFamily: 'default',
  fontSize: '3',
  lineHeight: '1.8',
  align: 'left' as AlignActionKey,
  textColor: '#1f4034',
  highlightColor: '#fff2a8',
  bold: false,
  italic: false,
  underline: false,
  strikeThrough: false,
  ul: false,
  ol: false,
  checklist: false
})

const editorStyle = computed(() => ({
  '--editor-min-height': `${Math.max(props.minHeight, 180)}px`
}))

function setEditorHtml(value?: string) {
  if (!editorRef.value) {
    return
  }

  const sanitized = sanitizeRichText(value)
  if (editorRef.value.innerHTML === sanitized) {
    return
  }

  editorRef.value.innerHTML = sanitized
}

function emitCurrentValue() {
  if (!editorRef.value) {
    return
  }

  const sanitized = sanitizeRichText(editorRef.value.innerHTML)
  if (editorRef.value.innerHTML !== sanitized) {
    editorRef.value.innerHTML = sanitized
  }
  emit('update:modelValue', sanitized)
}

function focusEditor() {
  if (!editorRef.value) {
    return
  }

  editorRef.value.focus()
  if (!editorRef.value.innerHTML.trim()) {
    editorRef.value.innerHTML = '<p><br></p>'
  }
  placeCursorAtEnd(editorRef.value)
  saveSelection()
}

function handleFocus() {
  saveSelection()
  syncToolbarState()
}

function handleInput() {
  saveSelection()
  emitCurrentValue()
  syncToolbarState()
}

function handleBlur() {
  saveSelection()
  emitCurrentValue()
  syncToolbarState()
  emit('blur')
}

function handlePaste(event: ClipboardEvent) {
  event.preventDefault()
  const html = event.clipboardData?.getData('text/html')
  const text = event.clipboardData?.getData('text/plain') || ''
  insertHtml(html ? sanitizeRichText(html) : escapeHtml(text).replace(/\n/g, '<br>'))
}

function handleBlockChange(event: Event) {
  const target = event.target as HTMLSelectElement
  runAction(target.value as BlockActionKey)
}

function handleFontFamilyChange(event: Event) {
  const target = event.target as HTMLSelectElement
  applyExecCommand('fontName', target.value === 'default' ? 'Microsoft YaHei' : target.value)
  toolbarState.fontFamily = target.value
}

function handleFontSizeChange(event: Event) {
  const target = event.target as HTMLSelectElement
  applyExecCommand('fontSize', target.value)
  toolbarState.fontSize = target.value
}

function handleLineHeightChange(event: Event) {
  const target = event.target as HTMLSelectElement
  applyBlockStyle('line-height', target.value)
  toolbarState.lineHeight = target.value
}

function handleTextColorChange(event: Event) {
  const target = event.target as HTMLInputElement
  toolbarState.textColor = target.value
  applyExecCommand('foreColor', target.value)
}

function handleHighlightColorChange(event: Event) {
  const target = event.target as HTMLInputElement
  toolbarState.highlightColor = target.value
  applyExecCommand('hiliteColor', target.value)
}

function runAction(action: ToolbarActionKey) {
  if (!editorRef.value) {
    return
  }

  if (action === 'checklist') {
    insertChecklist()
    return
  }

  if (action === 'divider') {
    insertHtml('<hr>')
    return
  }

  if (action === 'paragraph') {
    applyExecCommand('formatBlock', 'p')
    toolbarState.block = 'paragraph'
    return
  }

  if (action === 'h2' || action === 'h3' || action === 'h4') {
    applyExecCommand('formatBlock', action)
    toolbarState.block = action
    return
  }

  if (action === 'quote') {
    applyExecCommand('formatBlock', 'blockquote')
    toolbarState.block = 'quote'
    return
  }

  if (action === 'ul') {
    applyExecCommand('insertUnorderedList')
    return
  }

  if (action === 'ol') {
    applyExecCommand('insertOrderedList')
    return
  }

  if (action === 'left') {
    applyExecCommand('justifyLeft')
    toolbarState.align = 'left'
    return
  }

  if (action === 'center') {
    applyExecCommand('justifyCenter')
    toolbarState.align = 'center'
    return
  }

  if (action === 'right') {
    applyExecCommand('justifyRight')
    toolbarState.align = 'right'
    return
  }

  if (action === 'justify') {
    applyExecCommand('justifyFull')
    toolbarState.align = 'justify'
    return
  }

  applyExecCommand(action)
}

function applyExecCommand(command: string, value?: string) {
  restoreSelection()
  focusEditableRange()
  if (command === 'fontName' || command === 'fontSize' || command === 'foreColor' || command === 'hiliteColor') {
    document.execCommand('styleWithCSS', false, 'true')
  } else {
    document.execCommand('styleWithCSS', false, 'false')
  }
  document.execCommand(command, false, value)
  syncAfterCommand()
}

async function insertLink() {
  if (!editorRef.value) {
    return
  }

  saveSelection()
  const selectedText = window.getSelection()?.toString().trim() || ''

  try {
    const { value } = await ElMessageBox.prompt('请输入完整链接地址，仅支持 http 或 https', '插入链接', {
      confirmButtonText: '插入',
      cancelButtonText: '取消',
      inputPlaceholder: 'https://example.com',
      inputPattern: /^https?:\/\/.+$/i,
      inputErrorMessage: '请输入完整的 http/https 链接'
    })

    restoreSelection()
    focusEditableRange()
    if (selectedText) {
      document.execCommand('createLink', false, value.trim())
    } else {
      insertHtml(`<a href="${escapeAttribute(value.trim())}" target="_blank" rel="noopener noreferrer">${escapeHtml(value.trim())}</a>`)
    }
    syncAfterCommand()
  } catch {
    return
  }
}

async function insertVideo() {
  saveSelection()
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入视频地址。直链会插入播放器，YouTube / Bilibili / Vimeo 嵌入链接也会被保留。',
      '插入视频',
      {
        confirmButtonText: '插入',
        cancelButtonText: '取消',
        inputPlaceholder: 'https://...',
        inputPattern: /^https?:\/\/.+$/i,
        inputErrorMessage: '请输入完整的 http/https 视频地址'
      }
    )

    const url = value.trim()
    if (/\.(mp4|webm|ogg)(\?.*)?$/i.test(url)) {
      insertHtml(`<p><video controls playsinline src="${escapeAttribute(url)}"></video></p>`)
      return
    }

    const embedUrl = resolveEmbedUrl(url)
    if (!embedUrl) {
      ElMessage.warning('仅支持公开视频直链或 YouTube / Bilibili / Vimeo 嵌入地址')
      return
    }

    insertHtml(`<p><iframe src="${escapeAttribute(embedUrl)}" allowfullscreen></iframe></p>`)
  } catch {
    return
  }
}

function insertTable() {
  insertHtml(`
    <table>
      <thead>
        <tr>
          <th>列 1</th>
          <th>列 2</th>
          <th>列 3</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>内容</td>
          <td>内容</td>
          <td>内容</td>
        </tr>
        <tr>
          <td>内容</td>
          <td>内容</td>
          <td>内容</td>
        </tr>
      </tbody>
    </table>
  `)
}

function insertChecklist() {
  insertHtml(`
    <ul data-list="check">
      <li data-checked="false">待办事项 1</li>
      <li data-checked="false">待办事项 2</li>
    </ul>
  `)
}

function insertEmoji(emoji: string) {
  insertHtml(emoji)
}

function openImagePicker() {
  saveSelection()
  fileInputRef.value?.click()
}

async function handleImagePick(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  target.value = ''

  if (!file) {
    return
  }

  if (!validateImageFile(file)) {
    return
  }

  imageUploading.value = true
  try {
    const res = await uploadImageApi(file)
    insertHtml(`<p><img src="${escapeAttribute(res.url)}" alt="${escapeAttribute(res.originalName || '插图')}" /></p>`)
  } finally {
    imageUploading.value = false
  }
}

function insertHtml(html: string) {
  restoreSelection()
  focusEditableRange()
  document.execCommand('insertHTML', false, html)
  syncAfterCommand()
}

function applyBlockStyle(name: string, value: string) {
  restoreSelection()
  focusEditableRange()
  const blocks = resolveSelectedBlocks()
  if (!blocks.length) {
    const fallback = resolveSelectionElement()?.closest<HTMLElement>(textBlocksSelector)
    if (fallback) {
      fallback.style.setProperty(name, value)
    }
  } else {
    blocks.forEach((block) => block.style.setProperty(name, value))
  }
  syncAfterCommand()
}

function resolveSelectedBlocks() {
  const selection = window.getSelection()
  const root = editorRef.value
  if (!selection || !selection.rangeCount || !root) {
    return [] as HTMLElement[]
  }

  const range = selection.getRangeAt(0)
  const blocks = Array.from(root.querySelectorAll<HTMLElement>(textBlocksSelector))
  return blocks.filter((block) => range.intersectsNode(block))
}

function syncAfterCommand() {
  nextTick(() => {
    emitCurrentValue()
    saveSelection()
    syncToolbarState()
  })
}

function saveSelection() {
  const selection = window.getSelection()
  if (!selection || !selection.rangeCount || !editorRef.value) {
    return
  }

  const range = selection.getRangeAt(0)
  if (!editorRef.value.contains(range.commonAncestorContainer)) {
    return
  }

  savedRange.value = range.cloneRange()
}

function restoreSelection() {
  if (!savedRange.value) {
    return
  }

  const selection = window.getSelection()
  if (!selection) {
    return
  }

  selection.removeAllRanges()
  selection.addRange(savedRange.value)
}

function focusEditableRange() {
  editorRef.value?.focus()
}

function syncToolbarState() {
  const current = resolveSelectionElement()
  if (!current || !editorRef.value?.contains(current)) {
    return
  }

  saveSelection()
  toolbarState.block = resolveBlock(current)
  toolbarState.fontFamily = resolveFontFamily(current)
  toolbarState.fontSize = resolveFontSize(current)
  toolbarState.lineHeight = resolveLineHeight(current)
  toolbarState.align = resolveAlign()
  toolbarState.bold = document.queryCommandState('bold')
  toolbarState.italic = document.queryCommandState('italic')
  toolbarState.underline = document.queryCommandState('underline')
  toolbarState.strikeThrough = document.queryCommandState('strikeThrough')
  toolbarState.ul = document.queryCommandState('insertUnorderedList')
  toolbarState.ol = document.queryCommandState('insertOrderedList')
  toolbarState.checklist = Boolean(current.closest("ul[data-list='check'], ol[data-list='check']"))
}

function resolveSelectionElement() {
  const selection = window.getSelection()
  if (!selection || !selection.rangeCount) {
    return null
  }

  let node: Node | null = selection.anchorNode
  if (!node) {
    return null
  }

  if (node.nodeType === Node.TEXT_NODE) {
    node = node.parentNode
  }

  return node instanceof HTMLElement ? node : null
}

function resolveBlock(element: HTMLElement) {
  const block = element.closest<HTMLElement>('h2, h3, h4, blockquote, p, div')
  if (!block) {
    return 'paragraph'
  }

  if (block.tagName === 'H2') return 'h2'
  if (block.tagName === 'H3') return 'h3'
  if (block.tagName === 'H4') return 'h4'
  if (block.tagName === 'BLOCKQUOTE') return 'quote'
  return 'paragraph'
}

function resolveFontFamily(element: HTMLElement) {
  const family = window.getComputedStyle(element).fontFamily.toLowerCase()
  const matched = fontFamilyOptions.find((option) => option.match.some((keyword) => family.includes(keyword.toLowerCase())))
  return matched?.value || 'default'
}

function resolveFontSize(element: HTMLElement) {
  const size = Number.parseFloat(window.getComputedStyle(element).fontSize || '15')
  const nearest = fontSizeOptions.reduce((closest, current) =>
    Math.abs(current.size - size) < Math.abs(closest.size - size) ? current : closest
  )
  return nearest.value
}

function resolveLineHeight(element: HTMLElement) {
  const computed = window.getComputedStyle(element)
  const fontSize = Number.parseFloat(computed.fontSize || '15')
  const lineHeight = Number.parseFloat(computed.lineHeight || `${fontSize * 1.8}`)
  const ratio = Number.isFinite(lineHeight / fontSize) ? lineHeight / fontSize : 1.8
  const nearest = lineHeightOptions.reduce((closest, current) =>
    Math.abs(Number.parseFloat(current.value) - ratio) < Math.abs(Number.parseFloat(closest.value) - ratio)
      ? current
      : closest
  )
  return nearest.value
}

function resolveAlign() {
  if (document.queryCommandState('justifyCenter')) return 'center'
  if (document.queryCommandState('justifyRight')) return 'right'
  if (document.queryCommandState('justifyFull')) return 'justify'
  return 'left'
}

function placeCursorAtEnd(element: HTMLElement) {
  const selection = window.getSelection()
  if (!selection) {
    return
  }

  const range = document.createRange()
  range.selectNodeContents(element)
  range.collapse(false)
  selection.removeAllRanges()
  selection.addRange(range)
}

function resolveEmbedUrl(value: string) {
  try {
    const url = new URL(value)
    const hostname = url.hostname.replace(/^www\./, '')

    if (hostname === 'youtu.be') {
      const id = url.pathname.replace('/', '')
      return id ? `https://www.youtube.com/embed/${id}` : ''
    }

    if (hostname === 'youtube.com' && url.pathname === '/watch') {
      const id = url.searchParams.get('v')
      return id ? `https://www.youtube.com/embed/${id}` : ''
    }

    if (hostname === 'youtube.com' && url.pathname.startsWith('/embed/')) {
      return value
    }

    if (hostname === 'bilibili.com') {
      const match = url.pathname.match(/\/video\/([^/?]+)/i)
      return match ? `https://player.bilibili.com/player.html?bvid=${match[1]}` : ''
    }

    if (hostname === 'player.bilibili.com') {
      return value
    }

    if (hostname === 'vimeo.com') {
      const id = url.pathname.replace('/', '')
      return id ? `https://player.vimeo.com/video/${id}` : ''
    }

    if (hostname === 'player.vimeo.com') {
      return value
    }
  } catch {
    return ''
  }

  return ''
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function escapeAttribute(value: string) {
  return escapeHtml(value)
}

function handleSelectionChange() {
  const selection = window.getSelection()
  if (!selection || !selection.anchorNode || !editorRef.value?.contains(selection.anchorNode)) {
    return
  }
  syncToolbarState()
}

watch(
  () => props.modelValue,
  (value) => {
    if (document.activeElement === editorRef.value) {
      return
    }
    setEditorHtml(value)
    syncToolbarState()
  }
)

onMounted(() => {
  setEditorHtml(props.modelValue)
  document.addEventListener('selectionchange', handleSelectionChange)
  syncToolbarState()
})

onBeforeUnmount(() => {
  document.removeEventListener('selectionchange', handleSelectionChange)
})

defineExpose({
  focusEditor
})
</script>

<style scoped>
.rich-editor {
  display: grid;
  gap: 12px;
}

.toolbar-shell {
  border: 1px solid rgba(205, 214, 209, 0.95);
  border-radius: 22px;
  background:
    radial-gradient(circle at top left, rgba(242, 248, 244, 0.95), transparent 42%),
    linear-gradient(180deg, rgba(251, 252, 250, 0.98), rgba(244, 247, 245, 0.96));
  padding: 14px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.82),
    0 14px 26px rgba(31, 52, 42, 0.04);
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
}

.toolbar-row + .toolbar-row {
  margin-top: 10px;
}

.toolbar-group {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  padding: 6px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(212, 220, 216, 0.95);
}

.toolbar-group-selects {
  gap: 8px;
}

.toolbar-group-color {
  gap: 12px;
}

.tool-select {
  min-width: 106px;
  border: 0;
  background: transparent;
  color: #365247;
  font-size: 13px;
  padding: 8px 10px;
  outline: none;
}

.tool-button {
  border: 0;
  border-radius: 11px;
  background: transparent;
  color: #365247;
  padding: 8px 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  transition:
    background var(--cvs-motion-fast) var(--cvs-ease-standard),
    color var(--cvs-motion-fast) var(--cvs-ease-standard),
    box-shadow var(--cvs-motion-fast) var(--cvs-ease-standard);
}

.tool-button:hover {
  background: rgba(28, 117, 79, 0.1);
  color: #154f37;
}

.tool-button:disabled {
  opacity: 0.56;
  cursor: not-allowed;
}

.tool-button.active {
  background: linear-gradient(135deg, rgba(28, 117, 79, 0.18), rgba(28, 117, 79, 0.08));
  color: #14452f;
  box-shadow: inset 0 0 0 1px rgba(28, 117, 79, 0.18);
}

.tool-button-mark {
  min-width: 40px;
}

.tool-button-wide {
  padding-inline: 12px;
}

.tool-letter {
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}

.tool-letter-italic {
  font-style: italic;
}

.tool-letter-underline {
  text-decoration: underline;
}

.tool-letter-strikeThrough {
  text-decoration: line-through;
}

.color-tool {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 6px;
  color: #537065;
  font-size: 12px;
}

.color-tool input {
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  padding: 0;
  cursor: pointer;
}

.toolbar-meta {
  display: flex;
  justify-content: flex-start;
  margin-top: 10px;
}

.toolbar-tip {
  color: #6a7a74;
  font-size: 12px;
}

.image-input {
  display: none;
}

.editor-area {
  min-height: var(--editor-min-height);
  padding: 20px 20px 22px;
  border: 1px solid rgba(203, 214, 208, 0.95);
  border-radius: 20px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(251, 252, 250, 0.94)),
    radial-gradient(circle at top left, rgba(243, 247, 244, 0.9), transparent 36%);
  font-size: 15px;
  line-height: 1.9;
  color: var(--cvs-text-main);
  outline: none;
  overflow-wrap: anywhere;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.88),
    0 12px 24px rgba(31, 52, 42, 0.05);
}

.editor-area:focus {
  border-color: rgba(28, 117, 79, 0.34);
  box-shadow:
    0 0 0 3px rgba(28, 117, 79, 0.08),
    0 12px 24px rgba(31, 52, 42, 0.07);
}

.editor-area:empty::before {
  content: attr(data-placeholder);
  color: #97a6a0;
}

.editor-area :deep(h2),
.editor-area :deep(h3),
.editor-area :deep(h4) {
  margin: 0 0 14px;
  color: #173a2b;
  line-height: 1.35;
}

.editor-area :deep(h2) {
  font-size: 30px;
}

.editor-area :deep(h3) {
  font-size: 24px;
}

.editor-area :deep(h4) {
  font-size: 20px;
}

.editor-area :deep(p),
.editor-area :deep(ul),
.editor-area :deep(ol),
.editor-area :deep(blockquote),
.editor-area :deep(hr),
.editor-area :deep(table),
.editor-area :deep(pre) {
  margin: 0 0 14px;
}

.editor-area :deep(ul),
.editor-area :deep(ol) {
  padding-left: 24px;
}

.editor-area :deep(ul[data-list='check']) {
  list-style: none;
  padding-left: 0;
}

.editor-area :deep(ul[data-list='check'] li) {
  position: relative;
  padding-left: 30px;
}

.editor-area :deep(ul[data-list='check'] li::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0.42em;
  width: 16px;
  height: 16px;
  border-radius: 5px;
  border: 1px solid rgba(27, 117, 79, 0.42);
  background: #ffffff;
}

.editor-area :deep(ul[data-list='check'] li[data-checked='true']::before) {
  background: linear-gradient(135deg, #1a6c49, #33a06f);
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.88);
}

.editor-area :deep(blockquote) {
  padding: 12px 14px;
  border-left: 4px solid rgba(27, 111, 76, 0.28);
  background: linear-gradient(90deg, rgba(30, 122, 84, 0.08), rgba(255, 255, 255, 0));
  color: #476159;
  border-radius: 0 14px 14px 0;
}

.editor-area :deep(a) {
  color: #1d7a56;
  text-decoration: underline;
}

.editor-area :deep(hr) {
  border: 0;
  border-top: 1px solid rgba(27, 111, 76, 0.16);
}

.editor-area :deep(img),
.editor-area :deep(video),
.editor-area :deep(iframe) {
  max-width: 100%;
  border-radius: 14px;
  display: block;
  margin: 14px auto;
  border: 1px solid rgba(202, 214, 208, 0.94);
  background: #f6f9f7;
}

.editor-area :deep(video),
.editor-area :deep(iframe) {
  width: min(100%, 100%);
  aspect-ratio: 16 / 9;
}

.editor-area :deep(table) {
  width: 100%;
  border-collapse: collapse;
  overflow: hidden;
  border-style: hidden;
  box-shadow: 0 0 0 1px rgba(204, 214, 209, 0.96);
}

.editor-area :deep(th),
.editor-area :deep(td) {
  border: 1px solid rgba(212, 220, 216, 0.95);
  padding: 10px 12px;
  vertical-align: top;
}

.editor-area :deep(th) {
  background: rgba(241, 246, 243, 0.96);
}

.editor-area :deep(pre) {
  padding: 14px 16px;
  border-radius: 16px;
  background: #15251f;
  color: #e7f2ed;
  overflow-x: auto;
}

.editor-area :deep(code) {
  font-family: 'Cascadia Code', 'Fira Code', Consolas, monospace;
}

@media (max-width: 960px) {
  .toolbar-row,
  .toolbar-group {
    width: 100%;
  }

  .toolbar-group {
    justify-content: flex-start;
  }

  .tool-select {
    flex: 1 1 140px;
  }
}
</style>
