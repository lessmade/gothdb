import { useState } from 'react'
import { fetchRows } from '../api/client'
import { useAsync } from '../hooks/useAsync'
import { formatCellValue } from '../lib/formatCell'

interface RowsPanelProps {
  schema: string
  table: string
}

const PAGE_SIZE = 50

export function RowsPanel({ schema, table }: RowsPanelProps) {
  const [page, setPage] = useState(0)
  const { data, loading, error } = useAsync(() => fetchRows(schema, table, page, PAGE_SIZE), [schema, table, page])

  const columnNames = data && data.rows.length > 0 ? Object.keys(data.rows[0]) : []
  const totalPages = data?.totalElements == null ? null : Math.max(1, Math.ceil(data.totalElements / PAGE_SIZE))
  const rangeStart = data && data.rows.length > 0 ? page * PAGE_SIZE + 1 : 0
  const rangeEnd = data
    ? data.totalElements == null
      ? page * PAGE_SIZE + data.rows.length
      : Math.min(data.totalElements, (page + 1) * PAGE_SIZE)
    : 0
  const hasNext = data
    ? data.totalElements == null
      ? data.rows.length === PAGE_SIZE
      : (page + 1) * PAGE_SIZE < data.totalElements
    : false

  return (
    <div className="rows-panel">
      {loading && <div className="state-message">Loading rows…</div>}
      {error && <div className="state-message state-message--error">{error}</div>}

      {!loading && !error && data && data.rows.length === 0 && (
        <div className="state-message">No rows found</div>
      )}

      {!loading && !error && data && data.rows.length > 0 && (
        <>
          <div className="rows-panel__scroll">
            <table className="data-table">
              <thead>
                <tr>
                  {columnNames.map((name) => (
                    <th key={name}>{name}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {data.rows.map((row, index) => (
                  <tr key={index}>
                    {columnNames.map((name) => (
                      <td key={name}>{formatCellValue(row[name])}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="rows-panel__footer">
            <span>
              {rangeStart}–{rangeEnd}{data.totalElements == null ? '' : ` of ${data.totalElements}`}
              {!data.stableOrder && ' · unstable order (no primary key)'}
            </span>
            <div className="rows-panel__nav">
              <button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
                Prev
              </button>
              <span>
                {page + 1}{totalPages == null ? '' : ` / ${totalPages}`}
              </span>
              <button
                type="button"
                disabled={!hasNext}
                onClick={() => setPage((current) => current + 1)}
              >
                Next
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
