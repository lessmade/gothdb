import { fetchTables } from '../api/client'
import type { TableInfo } from '../api/types'
import { useAsync } from '../hooks/useAsync'
import { ListPanel } from './ListPanel'

interface TableListProps {
  schema: string
  selected: string | null
  onSelect: (table: string) => void
}

export function TableList({ schema, selected, onSelect }: TableListProps) {
  const { data, loading, error } = useAsync(() => fetchTables(schema), [schema])

  return (
    <ListPanel<TableInfo>
      title="Tables"
      loading={loading}
      error={error}
      items={data}
      getKey={(table) => table.name}
      isSelected={(table) => table.name === selected}
      onSelect={(table) => onSelect(table.name)}
      renderItem={(table) => (
        <>
          <span>{table.name}</span>
          {table.type === 'VIEW' && <span className="tag">VIEW</span>}
        </>
      )}
      emptyLabel="No tables found"
    />
  )
}
