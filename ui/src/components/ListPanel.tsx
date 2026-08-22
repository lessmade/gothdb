import type { ReactNode } from 'react'

interface ListPanelProps<T> {
  title: string
  loading: boolean
  error: string | null
  items: T[] | null
  getKey: (item: T) => string
  isSelected: (item: T) => boolean
  onSelect: (item: T) => void
  renderItem: (item: T) => ReactNode
  emptyLabel: string
}

export function ListPanel<T>({
  title,
  loading,
  error,
  items,
  getKey,
  isSelected,
  onSelect,
  renderItem,
  emptyLabel,
}: ListPanelProps<T>) {
  return (
    <div className="list-panel">
      <div className="list-panel__title">{title}</div>
      <div className="list-panel__body">
        {loading && <div className="state-message">Loading…</div>}
        {error && <div className="state-message state-message--error">{error}</div>}
        {!loading && !error && items && items.length === 0 && (
          <div className="state-message">{emptyLabel}</div>
        )}
        {!loading && !error && items && items.length > 0 && (
          <ul className="list-panel__list">
            {items.map((item) => (
              <li key={getKey(item)}>
                <button
                  type="button"
                  className={isSelected(item) ? 'list-row list-row--selected' : 'list-row'}
                  onClick={() => onSelect(item)}
                >
                  {renderItem(item)}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
