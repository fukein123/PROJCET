<template>
  <div class="brand-mark" :class="[`tone-${tone}`, { compact }]">
    <img class="brand-icon" src="/community-favicon.svg" alt="社区志愿服务图标" />
    <div class="brand-copy">
      <span class="brand-title">社区志愿服务平台</span>
      <span v-if="subtitle" class="brand-subtitle">{{ subtitle }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    subtitle?: string
    tone?: 'light' | 'dark'
    compact?: boolean
  }>(),
  {
    subtitle: '',
    tone: 'dark',
    compact: false
  }
)
</script>

<style scoped>
.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: var(--cvs-space-3);
  min-width: 0;
  --brand-title-color: #153728;
  --brand-subtitle-color: rgba(21, 55, 40, 0.72);
  --brand-icon-surface: rgba(255, 255, 255, 0.2);
  --brand-icon-border: rgba(255, 255, 255, 0.22);
}

.brand-mark.tone-light {
  --brand-title-color: #fffdf8;
  --brand-subtitle-color: rgba(255, 253, 248, 0.78);
  --brand-icon-surface: rgba(255, 255, 255, 0.14);
  --brand-icon-border: rgba(255, 255, 255, 0.22);
}

.brand-mark.compact {
  gap: var(--cvs-space-2);
}

.brand-icon {
  width: 42px;
  height: 42px;
  display: block;
  border-radius: 14px;
  padding: 6px;
  object-fit: contain;
  background: var(--brand-icon-surface);
  border: 1px solid var(--brand-icon-border);
  box-shadow: 0 12px 24px rgba(16, 44, 31, 0.16);
  flex-shrink: 0;
}

.brand-copy {
  display: grid;
  min-width: 0;
}

.brand-title {
  color: var(--brand-title-color);
  font-size: 16px;
  font-weight: var(--cvs-font-weight-heavy);
  line-height: 1.2;
  letter-spacing: 0.03em;
}

.brand-subtitle {
  color: var(--brand-subtitle-color);
  font-size: var(--cvs-font-size-xs);
  line-height: 1.4;
  margin-top: 2px;
  white-space: nowrap;
}

.brand-mark.compact .brand-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
}

.brand-mark.compact .brand-title {
  font-size: 14px;
}

@media (max-width: 640px) {
  .brand-subtitle {
    display: none;
  }
}
</style>
