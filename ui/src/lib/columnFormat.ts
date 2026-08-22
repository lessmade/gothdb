import type { ColumnInfo, ForeignKeyInfo, IndexInfo, PrimaryKeyInfo } from '../api/types'

const SIZELESS_JDBC_TYPES = new Set([-6, 5, 4, -5, 16, 91, 93])

export function formatType(column: ColumnInfo): string {
  if (column.size == null || SIZELESS_JDBC_TYPES.has(column.jdbcType)) {
    return column.typeName
  }
  if (column.scale != null && column.scale > 0) {
    return `${column.typeName}(${column.size}, ${column.scale})`
  }
  return `${column.typeName}(${column.size})`
}

export function keyMarkers(
  column: ColumnInfo,
  primaryKeys: PrimaryKeyInfo[],
  foreignKeys: ForeignKeyInfo[],
  indexes: IndexInfo[],
): string[] {
  const markers: string[] = []

  if (primaryKeys.some((key) => key.columnName === column.name)) {
    markers.push('PK')
  }

  const foreignKey = foreignKeys.find((key) => key.columnName === column.name)
  if (foreignKey) {
    markers.push(`FK → ${foreignKey.referencedTable}.${foreignKey.referencedColumn}`)
  }

  if (indexes.some((index) => index.unique && index.columnName === column.name)) {
    markers.push('UNIQUE')
  }

  return markers
}
