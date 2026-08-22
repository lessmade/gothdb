import type {
  ColumnInfo,
  ForeignKeyInfo,
  IndexInfo,
  PrimaryKeyInfo,
  RowPage,
  SchemaInfo,
  StatusInfo,
  TableDetails,
  TableInfo,
} from './types'

const API_BASE = '/gothdb/api'

interface ApiErrorBody {
  status: number
  error: string
  message: string
}

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`)
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiErrorBody | null
    throw new Error(body?.message ?? `Request failed with status ${response.status}`)
  }
  return (await response.json()) as T
}

function encode(value: string): string {
  return encodeURIComponent(value)
}

export function fetchStatus(): Promise<StatusInfo> {
  return request<StatusInfo>('/status')
}

export function fetchSchemas(): Promise<SchemaInfo[]> {
  return request<SchemaInfo[]>('/schemas')
}

export function fetchTables(schema: string): Promise<TableInfo[]> {
  return request<TableInfo[]>(`/schemas/${encode(schema)}/tables`)
}

export function fetchColumns(schema: string, table: string): Promise<ColumnInfo[]> {
  return request<ColumnInfo[]>(`/schemas/${encode(schema)}/tables/${encode(table)}/columns`)
}

export function fetchPrimaryKeys(schema: string, table: string): Promise<PrimaryKeyInfo[]> {
  return request<PrimaryKeyInfo[]>(`/schemas/${encode(schema)}/tables/${encode(table)}/primary-key`)
}

export function fetchForeignKeys(schema: string, table: string): Promise<ForeignKeyInfo[]> {
  return request<ForeignKeyInfo[]>(`/schemas/${encode(schema)}/tables/${encode(table)}/foreign-keys`)
}

export function fetchIndexes(schema: string, table: string): Promise<IndexInfo[]> {
  return request<IndexInfo[]>(`/schemas/${encode(schema)}/tables/${encode(table)}/indexes`)
}

export function fetchRows(schema: string, table: string, page: number, size: number): Promise<RowPage> {
  return request<RowPage>(`/schemas/${encode(schema)}/tables/${encode(table)}/rows?page=${page}&size=${size}`)
}

export async function fetchTableDetails(schema: string, table: string): Promise<TableDetails> {
  const [columns, primaryKeys, foreignKeys, indexes] = await Promise.all([
    fetchColumns(schema, table),
    fetchPrimaryKeys(schema, table),
    fetchForeignKeys(schema, table),
    fetchIndexes(schema, table),
  ])
  return { columns, primaryKeys, foreignKeys, indexes }
}
