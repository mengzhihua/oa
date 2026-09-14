export function fmtDateTime(value) {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  return text.length >= 16 ? text.slice(0, 16) : text
}

export function fmtDate(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 10)
}

export function fmtMoney(value) {
  const amount = Number(value || 0)
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}
