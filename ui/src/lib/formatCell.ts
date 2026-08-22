export function formatCellValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '—'
  }
  return String(value)
}
