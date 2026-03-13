<template>
  <section class="volunteer-page-section fade-up">
    <div v-if="hasHeader" class="section-head">
      <div class="section-copy">
        <p v-if="eyebrow" class="section-eyebrow">{{ eyebrow }}</p>
        <h2 v-if="title" class="section-title">{{ title }}</h2>
        <p v-if="description" class="section-description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="section-actions">
        <slot name="actions" />
      </div>
    </div>

    <div class="section-body">
      <slot />
    </div>

    <div v-if="$slots.footer" class="section-footer">
      <slot name="footer" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(
  defineProps<{
    eyebrow?: string
    title?: string
    description?: string
  }>(),
  {
    eyebrow: '',
    title: '',
    description: ''
  }
)

const slots = useSlots()
const hasHeader = computed(() => Boolean(props.eyebrow || props.title || props.description || slots.actions))
</script>

<style scoped>
.volunteer-page-section {
  border: 1px solid var(--cvs-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  padding: 18px;
  box-shadow: var(--cvs-shadow-soft);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.section-copy {
  min-width: 0;
}

.section-eyebrow {
  margin: 0 0 6px;
  color: var(--cvs-primary);
  font-size: var(--cvs-font-size-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.section-title {
  margin: 0;
  font-size: var(--cvs-font-size-xl);
  font-weight: var(--cvs-font-weight-heavy);
  color: var(--cvs-text-main);
}

.section-description {
  margin: 8px 0 0;
  max-width: 720px;
  color: var(--cvs-text-sub);
  line-height: 1.7;
}

.section-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.section-body {
  min-width: 0;
}

.section-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .volunteer-page-section {
    padding: 16px;
  }

  .section-head {
    flex-direction: column;
  }

  .section-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
