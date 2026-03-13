<template>
  <section class="admin-content-section">
    <div v-if="hasHeader" class="header">
      <div class="copy">
        <h3 v-if="title" class="title">{{ title }}</h3>
        <p v-if="description" class="description">{{ description }}</p>
      </div>
      <div v-if="hasActions" class="actions">
        <slot name="actions" />
      </div>
    </div>

    <div class="body">
      <StatePanel
        v-if="loading"
        state="loading"
        tone="portal"
        compact
        :title="loadingTitle"
        :description="loadingDescription"
      />
      <StatePanel
        v-else-if="empty"
        state="empty"
        tone="portal"
        compact
        :title="emptyTitle"
        :description="emptyDescription"
      >
        <template v-if="hasEmptyActions" #actions>
          <slot name="emptyActions" />
        </template>
      </StatePanel>
      <slot v-else />
    </div>

    <div v-if="hasPagination && !loading && !empty" class="footer">
      <slot name="pagination" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'
import StatePanel from '@/components/shared/StatePanel.vue'

const props = withDefaults(
  defineProps<{
    title?: string
    description?: string
    loading?: boolean
    empty?: boolean
    loadingTitle?: string
    loadingDescription?: string
    emptyTitle?: string
    emptyDescription?: string
  }>(),
  {
    title: '',
    description: '',
    loading: false,
    empty: false,
    loadingTitle: '正在加载内容',
    loadingDescription: '请稍候，系统正在同步最新列表数据。',
    emptyTitle: '当前暂无内容',
    emptyDescription: '可以先新增一条记录，后续会在这里展示。'
  }
)

const slots = useSlots()

const hasActions = computed(() => Boolean(slots.actions))
const hasPagination = computed(() => Boolean(slots.pagination))
const hasEmptyActions = computed(() => Boolean(slots.emptyActions))
const hasHeader = computed(() => Boolean(props.title || props.description || slots.actions))
</script>

<style scoped>
.admin-content-section {
  display: grid;
  gap: var(--cvs-space-3);
}

.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--cvs-space-4);
  padding: var(--cvs-space-4);
  border: 1px solid var(--cvs-border);
  border-radius: var(--cvs-radius-md);
  background:
    linear-gradient(135deg, rgba(242, 247, 244, 0.96), rgba(255, 255, 255, 0.88)),
    var(--cvs-card);
}

.copy {
  display: grid;
  gap: var(--cvs-space-1);
}

.title {
  margin: 0;
  font-size: var(--cvs-font-size-base);
  font-weight: var(--cvs-font-weight-heavy);
  color: var(--cvs-text-main);
}

.description {
  margin: 0;
  color: var(--cvs-text-sub);
  font-size: var(--cvs-font-size-sm);
  line-height: 1.7;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--cvs-space-2);
}

.body {
  display: grid;
  gap: var(--cvs-space-3);
}

.footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .header {
    flex-direction: column;
  }

  .actions,
  .footer {
    justify-content: flex-start;
  }
}
</style>
