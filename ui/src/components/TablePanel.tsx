import { useState } from 'react'
import { ColumnsPanel } from './ColumnsPanel'
import { RowsPanel } from './RowsPanel'

interface TablePanelProps {
  schema: string
  table: string
}

type Tab = 'columns' | 'rows'

export function TablePanel({ schema, table }: TablePanelProps) {
  const [tab, setTab] = useState<Tab>('columns')

  return (
    <div className="table-panel">
      <div className="table-panel__header">
        <div className="table-panel__title">
          <span>{schema}</span>
          <span className="table-panel__sep">/</span>
          <span>{table}</span>
        </div>
        <div className="tab-bar">
          <button
            type="button"
            className={tab === 'columns' ? 'tab-bar__tab tab-bar__tab--active' : 'tab-bar__tab'}
            onClick={() => setTab('columns')}
          >
            Columns
          </button>
          <button
            type="button"
            className={tab === 'rows' ? 'tab-bar__tab tab-bar__tab--active' : 'tab-bar__tab'}
            onClick={() => setTab('rows')}
          >
            Data
          </button>
        </div>
      </div>
      <div className="table-panel__body">
        {tab === 'columns' ? (
          <ColumnsPanel schema={schema} table={table} />
        ) : (
          <RowsPanel key={`${schema}.${table}`} schema={schema} table={table} />
        )}
      </div>
    </div>
  )
}
