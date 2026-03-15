<template>
  <section class="workspace-hero fade-up" :class="[`tone-${tone}`, { compact }]">
    <div class="hero-main">
      <p v-if="eyebrow" class="hero-eyebrow">{{ eyebrow }}</p>
      <h1 class="hero-title">{{ title }}</h1>
      <p v-if="description" class="hero-description">{{ description }}</p>
      <div v-if="$slots.actions" class="hero-actions">
        <slot name="actions" />
      </div>
    </div>
    <aside v-if="$slots.aside" class="hero-aside">
      <slot name="aside" />
    </aside>
  </section>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    eyebrow?: string
    title: string
    description?: string
    tone?: 'portal' | 'volunteer'
    compact?: boolean
  }>(),
  {
    eyebrow: '',
    description: '',
    tone: 'volunteer',
    compact: false
  }
)
</script>

<style scoped>
.workspace-hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(240px, 0.8fr);
  gap: var(--cvs-space-5);
  padding: 28px;
  border-radius: 24px;
  color: #fffdfa;
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: var(--cvs-shadow-soft);
  background:
    radial-gradient(circle at 85% 16%, var(--hero-glow), transparent 34%),
    linear-gradient(135deg, var(--hero-start), var(--hero-end));
  --hero-start: #0f4d35;
  --hero-end: #1d8c63;
  --hero-glow: rgba(148, 255, 208, 0.24);
  --hero-copy-width: 760px;
}

.workspace-hero::before {
  content: '';
  position: absolute;
  inset: auto -18% -42% auto;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  filter: blur(12px);
}

.workspace-hero.tone-portal {
  --hero-start: #134f36;
  --hero-end: #2a7a5f;
  --hero-glow: rgba(239, 194, 100, 0.28);
}

.workspace-hero.compact {
  padding: 24px;
  border-radius: 22px;
}

.hero-main,
.hero-aside {
  position: relative;
  z-index: 1;
}

.hero-main {
  max-width: var(--hero-copy-width);
}

.hero-eyebrow {
  margin: 0;
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 253, 250, 0.82);
}

.hero-title {
  margin: 10px 0 0;
  font-size: clamp(28px, 3vw, 42px);
  line-height: 1.16;
  letter-spacing: 0.01em;
}

.workspace-hero.compact .hero-title {
  font-size: clamp(24px, 2.4vw, 34px);
}

.hero-description {
  margin: 14px 0 0;
  max-width: 680px;
  line-height: 1.78;
  color: rgba(255, 253, 250, 0.9);
}

.hero-actions {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-aside {
  display: grid;
  align-content: start;
}

.hero-aside :deep(.hero-stat-grid) {
  display: grid;
  gap: 10px;
}

.hero-aside :deep(.hero-stat) {
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(11, 31, 22, 0.18);
  padding: 14px 16px;
  backdrop-filter: blur(10px);
}

.hero-aside :deep(.hero-stat h3) {
  margin: 0;
  font-size: var(--cvs-font-size-sm);
  font-weight: var(--cvs-font-weight-semibold);
  color: rgba(255, 253, 250, 0.78);
}

.hero-aside :deep(.hero-stat strong) {
  display: block;
  margin-top: 8px;
  font-size: 30px;
  line-height: 1.1;
  letter-spacing: -0.03em;
}

.hero-aside :deep(.hero-stat span) {
  display: block;
  margin-top: 6px;
  font-size: var(--cvs-font-size-xs);
  color: rgba(255, 253, 250, 0.74);
  line-height: 1.5;
}

@media (max-width: 1080px) {
  .workspace-hero {
    grid-template-columns: 1fr;
  }

  .hero-aside :deep(.hero-stat-grid) {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .hero-aside :deep(.hero-stat-grid) {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .workspace-hero {
    padding: 22px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-actions :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }
}
</style>
