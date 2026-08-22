import { fetchStatus } from '../api/client'
import { useAsync } from '../hooks/useAsync'

export function StatusBar() {
  const { data, loading, error } = useAsync(fetchStatus, [])

  return (
    <header className="status-bar">
      <span className="status-bar__brand">gothdb</span>
      {loading && <span className="status-bar__state">connecting…</span>}
      {error && (
        <span className="status-bar__state status-bar__state--error">disconnected — {error}</span>
      )}
      {data && (
        <span className="status-bar__state">
          <span className="status-dot" />
          {data.database} {data.databaseVersion} · {data.driver}
        </span>
      )}
    </header>
  )
}
