<template>
  <div class="rich-text-renderer rich-text-content" v-html="resolvedHtml"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { sanitizeRichText } from '@/utils/rich-text'

const props = withDefaults(
  defineProps<{
    value?: string | null
    emptyHtml?: string
  }>(),
  {
    value: '',
    emptyHtml: '<p>暂无正文内容。</p>'
  }
)

const resolvedHtml = computed(() => sanitizeRichText(props.value) || props.emptyHtml)
</script>

<style scoped>
.rich-text-renderer {
  color: #3f4f4a;
  line-height: 1.95;
  font-size: 15px;
}

.rich-text-renderer :deep(h1),
.rich-text-renderer :deep(h2),
.rich-text-renderer :deep(h3),
.rich-text-renderer :deep(h4),
.rich-text-renderer :deep(h5),
.rich-text-renderer :deep(h6) {
  margin: 20px 0 12px;
  color: #1b2f28;
  line-height: 1.25;
}

.rich-text-renderer :deep(h1) {
  font-size: 34px;
}

.rich-text-renderer :deep(h2) {
  font-size: 28px;
}

.rich-text-renderer :deep(h3) {
  font-size: 24px;
}

.rich-text-renderer :deep(h4) {
  font-size: 20px;
}

.rich-text-renderer :deep(p),
.rich-text-renderer :deep(ul),
.rich-text-renderer :deep(ol),
.rich-text-renderer :deep(blockquote),
.rich-text-renderer :deep(hr),
.rich-text-renderer :deep(table),
.rich-text-renderer :deep(pre),
.rich-text-renderer :deep(video),
.rich-text-renderer :deep(iframe) {
  margin: 0 0 16px;
}

.rich-text-renderer :deep(ul),
.rich-text-renderer :deep(ol) {
  padding-left: 24px;
}

.rich-text-renderer :deep(ul[data-list='check']) {
  list-style: none;
  padding-left: 0;
}

.rich-text-renderer :deep(ul[data-list='check'] li) {
  position: relative;
  padding-left: 30px;
  margin-bottom: 10px;
}

.rich-text-renderer :deep(ul[data-list='check'] li::before) {
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

.rich-text-renderer :deep(ul[data-list='check'] li[data-checked='true']::before) {
  background: linear-gradient(135deg, #1a6c49, #33a06f);
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.88);
}

.rich-text-renderer :deep(blockquote) {
  padding: 14px 16px;
  border-left: 4px solid rgba(31, 122, 84, 0.24);
  background: linear-gradient(90deg, rgba(228, 241, 234, 0.82), rgba(255, 255, 255, 0.96));
  border-radius: 0 14px 14px 0;
  color: #4b5d57;
}

.rich-text-renderer :deep(a) {
  color: #1e7a56;
  text-decoration: underline;
  word-break: break-all;
}

.rich-text-renderer :deep(hr) {
  border: 0;
  border-top: 1px solid rgba(35, 109, 79, 0.18);
}

.rich-text-renderer :deep(img),
.rich-text-renderer :deep(video),
.rich-text-renderer :deep(iframe) {
  display: block;
  width: min(100%, 100%);
  max-width: 100%;
  border-radius: 16px;
  border: 1px solid rgba(201, 213, 207, 0.92);
  background: #f5f8f6;
  overflow: hidden;
}

.rich-text-renderer :deep(video),
.rich-text-renderer :deep(iframe) {
  aspect-ratio: 16 / 9;
}

.rich-text-renderer :deep(table) {
  width: 100%;
  border-collapse: collapse;
  overflow: hidden;
  border-radius: 14px;
  border-style: hidden;
  box-shadow: 0 0 0 1px rgba(204, 214, 209, 0.96);
}

.rich-text-renderer :deep(th),
.rich-text-renderer :deep(td) {
  border: 1px solid rgba(212, 220, 216, 0.95);
  padding: 10px 12px;
  vertical-align: top;
}

.rich-text-renderer :deep(th) {
  background: rgba(241, 246, 243, 0.96);
  color: #214234;
  font-weight: 700;
}

.rich-text-renderer :deep(pre) {
  padding: 14px 16px;
  border-radius: 16px;
  background: #15251f;
  color: #e7f2ed;
  overflow-x: auto;
}

.rich-text-renderer :deep(code) {
  font-family: 'Cascadia Code', 'Fira Code', Consolas, monospace;
}
</style>
