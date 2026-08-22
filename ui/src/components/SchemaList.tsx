import { fetchSchemas } from '../api/client'
import type { SchemaInfo } from '../api/types'
import { useAsync } from '../hooks/useAsync'
import { ListPanel } from './ListPanel'

interface SchemaListProps {
  selected: string | null
  onSelect: (schema: string) => void
}

export function SchemaList({ selected, onSelect }: SchemaListProps) {
  const { data, loading, error } = useAsync(fetchSchemas, [])

  return (
    <ListPanel<SchemaInfo>
      title="Schemas"
      loading={loading}
      error={error}
      items={data}
      getKey={(schema) => schema.name}
      isSelected={(schema) => schema.name === selected}
      onSelect={(schema) => onSelect(schema.name)}
      renderItem={(schema) => schema.name}
      emptyLabel="No schemas found"
    />
  )
}
