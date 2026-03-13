<template>
  <div class="state-panel" :class="[`state-${state}`, `tone-${tone}`, { compact }]">
    <div class="state-badge">
      <span class="state-core"></span>
    </div>
    <p class="state-title">{{ resolvedTitle }}</p>
    <p v-if="description" class="state-description">{{ description }}</p>
    <div v-if="$slots.actions" class="state-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    state?: 'loading' | 'empty'
    title?: string
    description?: string
    tone?: 'portal' | 'volunteer'
    compact?: boolean
  }>(),
  {
    state: 'empty',
    title: '',
    description: '',
    tone: 'volunteer',
    compact: false
  }
)

const resolvedTitle = computed(() => {
  if (props.title) {
    return props.title
  }

  return props.state === 'loading' ? '正在加载内容' : '当前暂无内容'
})
</script>

<style scoped>
.state-panel {
  display: grid;
  justify-items: center;
  text-align: center;
  gap: 10px;
  padding: 28px 18px;
  border-radius: 18px;
  border: 1px dashed var(--state-border);
  background: linear-gradient(180deg, var(--state-surface), rgba(255, 255, 255, 0.86));
  --state-border: rgba(31, 122, 84, 0.18);
  --state-surface: rgba(228, 241, 234, 0.72);
  --state-accent: #1f7a54;
}

.state-panel.tone-portal {
  --state-border: rgba(32, 111, 82, 0.22);
  --state-surface: rgba(242, 247, 244, 0.88);
  --state-accent: #2a7a5f;
}

.state-badge {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: inset 0 0 0 8px rgba(255, 255, 255, 0.12);
}

.state-core {
  width: 18px;
  height: 18px;
  border-radius: 6px;
  background: var(--state-accent);
  transform: rotate(12deg);
}

.state-loading .state-badge {
  border-top-color: var(--state-accent);
  border-right-color: rgba(255, 255, 255, 0.28);
  border-bottom-color: rgba(255, 255, 255, 0.28);
  border-left-color: rgba(255, 255, 255, 0.28);
  animation: spin 0.9s linear infinite;
}

.state-loading .state-core {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  transform: none;
}

.state-title {
  margin: 0;
  font-size: 16px;
  font-weight: var(--cvs-font-weight-bold);
  color: var(--cvs-text-main);
}

.state-description {
  margin: 0;
  max-width: 440px;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.state-actions {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.state-panel.compact {
  padding: 22px 14px;
  border-radius: 16px;
}

.state-panel.compact .state-badge {
  width: 48px;
  height: 48px;
}

.state-panel.compact .state-title {
  font-size: 14px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
