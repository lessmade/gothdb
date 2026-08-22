import { fetchTableDetails } from '../api/client'
import { useAsync } from '../hooks/useAsync'
import { formatType, keyMarkers } from '../lib/columnFormat'

interface ColumnsPanelProps {
  schema: string
  table: string
}

export function ColumnsPanel({ schema, table }: ColumnsPanelProps) {
  const { data, loading, error } = useAsync(() => fetchTableDetails(schema, table), [schema, table])

  return (
    <div className="columns-panel">
      {loading && <div className="state-message">Loading columns…</div>}
      {error && <div className="state-message state-message--error">{error}</div>}

      {!loading && !error && data && data.columns.length === 0 && (
        <div className="state-message">No columns found</div>
      )}

      {!loading && !error && data && data.columns.length > 0 && (
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Null</th>
              <th>Default</th>
              <th>Key</th>
            </tr>
          </thead>
          <tbody>
            {data.columns.map((column) => {
              const markers = keyMarkers(column, data.primaryKeys, data.foreignKeys, data.indexes)
              return (
                <tr key={column.name}>
                  <td>{column.name}</td>
                  <td>{formatType(column)}</td>
                  <td>{column.nullable ? 'YES' : 'NO'}</td>
                  <td>{column.defaultValue ?? '—'}</td>
                  <td>
                    {markers.map((marker) => (
                      <span className="tag" key={marker}>
                        {marker}
                      </span>
                    ))}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      )}
    </div>
  )
}
