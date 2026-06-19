import type { ReactNode } from 'react'

export function Field(props: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="field">
      <span>{props.label}</span>
      {props.children}
      {props.error ? <span className="status bad">{props.error}</span> : null}
    </label>
  )
}

export function Metric(props: { label: string; value: number; helper?: string }) {
  return (
    <div className="metric">
      <span>{props.label}</span>
      <strong>{props.value.toLocaleString()}</strong>
      {props.helper ? <small>{props.helper}</small> : null}
    </div>
  )
}

export function Row(props: { colSpan: number; text: string }) {
  return (
    <tr>
      <td colSpan={props.colSpan}>{props.text}</td>
    </tr>
  )
}

const statusLabels: Record<string, string> = {
  AUTHORIZED: '승인 완료',
  CANCELLED: '취소',
  CREATED: '생성',
  EXCHANGED: '교환 완료',
  ISSUED: '적립 중',
  ON_SALE: '판매 중',
  PAID: '결제 완료',
  REFUNDED: '환불',
  SOLD_OUT: '품절',
  VOIDED: '회수',
}

export function statusLabel(value: string) {
  return statusLabels[value] ?? value
}

export function StatusBadge(props: { value: string }) {
  const kind = props.value === 'PAID' || props.value === 'ISSUED' || props.value === 'AUTHORIZED' || props.value === 'ON_SALE'
    ? 'ok'
    : props.value === 'CREATED' || props.value === 'EXCHANGED'
      ? 'warn'
      : 'bad'
  return <span className={`status ${kind}`}>{statusLabel(props.value)}</span>
}

export function Notice(props: { text?: string; error?: string }) {
  if (!props.text && !props.error) {
    return null
  }
  return (
    <div className={`notice ${props.error ? 'error' : ''}`} role={props.error ? 'alert' : 'status'}>
      {props.error ? props.error : props.text}
    </div>
  )
}
