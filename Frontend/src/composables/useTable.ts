import { reactive, ref } from 'vue'

export interface TableQuery {
  current: number
  size: number
  [key: string]: string | number | undefined
}

export function useTable<T extends object>(
  fetcher: (params: TableQuery) => Promise<{ total: number; records: T[] }>
) {
  const loading = ref(false)
  const total = ref(0)
  const records = ref<T[]>([])
  const query = reactive({
    current: 1,
    size: 10
  } as TableQuery)

  const load = async () => {
    loading.value = true
    try {
      const res = await fetcher(query)
      total.value = res.total
      records.value = res.records
    } finally {
      loading.value = false
    }
  }

  return { loading, total, records, query, load }
}
