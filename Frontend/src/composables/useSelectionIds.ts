import { computed, ref } from 'vue'

interface IdentifiableRecord {
  id?: number | null
}

export function useSelectionIds<TRecord extends IdentifiableRecord>() {
  const selectedIds = ref<number[]>([])
  const hasSelection = computed(() => selectedIds.value.length > 0)

  const handleSelectionChange = (rows: TRecord[]) => {
    selectedIds.value = rows
      .map((row) => row.id)
      .filter((id): id is number => typeof id === 'number')
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  return {
    selectedIds,
    hasSelection,
    handleSelectionChange,
    clearSelection
  }
}
