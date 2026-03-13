<template>
  <section class="admin-list-scaffold fade-up">
    <SearchForm v-if="hasFilters" @search="emit('search')" @reset="emit('reset')">
      <slot name="filters" />
    </SearchForm>

    <el-card class="admin-list-card" shadow="never">
      <template v-if="hasHeader" #header>
        <div class="admin-list-header">
          <div class="title-block">
            <h2 v-if="title" class="title">{{ title }}</h2>
            <p v-if="description" class="description">{{ description }}</p>
          </div>
          <div v-if="hasActions" class="actions">
            <slot name="actions" />
          </div>
        </div>
      </template>

      <div class="body">
        <slot />
      </div>

      <div v-if="hasPagination" class="footer">
        <slot name="pagination" />
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'
import SearchForm from '@/components/SearchForm.vue'

const props = withDefaults(
  defineProps<{
    title?: string
    description?: string
  }>(),
  {
    title: '',
    description: ''
  }
)

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
}>()

const slots = useSlots()

const hasFilters = computed(() => Boolean(slots.filters))
const hasActions = computed(() => Boolean(slots.actions))
const hasPagination = computed(() => Boolean(slots.pagination))
const hasHeader = computed(() => Boolean(props.title || props.description || slots.actions))
</script>

<style scoped>
.admin-list-scaffold {
  display: grid;
  gap: var(--cvs-space-3);
}

.admin-list-card {
  border: 1px solid var(--cvs-border);
  border-radius: var(--cvs-radius-lg);
}

.admin-list-card :deep(.el-card__header) {
  padding: var(--cvs-space-4) var(--cvs-space-4) var(--cvs-space-3);
  border-bottom: 1px solid var(--cvs-border);
}

.admin-list-card :deep(.el-card__body) {
  padding: var(--cvs-space-4);
}

.admin-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--cvs-space-4);
}

.title-block {
  display: grid;
  gap: var(--cvs-space-1);
}

.title {
  margin: 0;
  font-size: var(--cvs-font-size-lg);
  font-weight: var(--cvs-font-weight-heavy);
  color: var(--cvs-text-main);
}

.description {
  margin: 0;
  font-size: var(--cvs-font-size-sm);
  color: var(--cvs-text-sub);
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
  margin-top: var(--cvs-space-3);
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .admin-list-header {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    justify-content: flex-start;
  }

  .footer {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>