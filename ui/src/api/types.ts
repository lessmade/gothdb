export interface StatusInfo {
  status: string
  database: string
  databaseVersion: string
  driver: string
}

export interface SchemaInfo {
  catalog: string | null
  name: string
}

export interface TableInfo {
  catalog: string | null
  schema: string
  name: string
  type: string
  remarks: string | null
}

export interface ColumnInfo {
  catalog: string | null
  schema: string
  table: string
  name: string
  position: number
  jdbcType: number
  typeName: string
  size: number | null
  scale: number | null
  nullable: boolean
  defaultValue: string | null
  autoIncrement: boolean
}

export interface PrimaryKeyInfo {
  catalog: string | null
  schema: string
  table: string
  columnName: string
  keySequence: number
  primaryKeyName: string | null
}

export interface ForeignKeyInfo {
  name: string | null
  catalog: string | null
  schema: string
  table: string
  columnName: string
  referencedCatalog: string | null
  referencedSchema: string
  referencedTable: string
  referencedColumn: string
  keySequence: number
  updateRule: string
  deleteRule: string
}

export interface IndexInfo {
  catalog: string | null
  schema: string
  table: string
  name: string | null
  unique: boolean
  ordinalPosition: number
  columnName: string
  sortOrder: string | null
}

export interface TableDetails {
  columns: ColumnInfo[]
  primaryKeys: PrimaryKeyInfo[]
  foreignKeys: ForeignKeyInfo[]
  indexes: IndexInfo[]
}

export interface RowPage {
  page: number
  size: number
  totalElements: number | null
  stableOrder: boolean
  rows: Record<string, unknown>[]
}
