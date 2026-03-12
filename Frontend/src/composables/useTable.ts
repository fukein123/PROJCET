import { reactive, ref } from 'vue'

export interface TableQuery {
  current: number
  size: number
}

interface PagedResult<T> {
  total: number
  records: T[]
}

interface UseTableOptions<TRecord, TQuery extends TableQuery> {
  initialQuery: TQuery
  fetcher: (params: TQuery) => Promise<PagedResult<TRecord>>
}

function cloneInitialQuery<TQuery extends TableQuery>(initialQuery: TQuery): TQuery {
  return { ...initialQuery }
}

export function useTable<TRecord, TQuery extends TableQuery>({
  initialQuery,
  fetcher
}: UseTableOptions<TRecord, TQuery>) {
  const loading = ref(false)
  const total = ref(0)
  const records = ref<TRecord[]>([])
  const query = reactive(cloneInitialQuery(initialQuery)) as TQuery

  const load = async (overrides?: Partial<TQuery>) => {
    if (overrides) {
      Object.assign(query, overrides)
    }

    loading.value = true
    try {
      const res = await fetcher({ ...query })
      total.value = res.total
      records.value = res.records
    } finally {
      loading.value = false
    }
  }

  const handlePage = (page: number) => load({ current: page } as Partial<TQuery>)

  const handleSizeChange = (size: number) =>
    load({
      current: 1,
      size
    } as Partial<TQuery>)

  const reset = (overrides?: Partial<TQuery>) => {
    Object.assign(query, cloneInitialQuery(initialQuery), overrides)
    return load()
  }

  return {
    loading,
    total,
    records,
    query,
    load,
    reset,
    handlePage,
    handleSizeChange
  }
}
