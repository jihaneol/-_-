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

export function StatusBadge(props: { value: string }) {
  const kind = props.value === 'PAID' || props.value === 'ISSUED' || props.value === 'AUTHORIZED' || props.value === 'ON_SALE'
    ? 'ok'
    : props.value === 'CREATED'
      ? 'warn'
      : 'bad'
  return <span className={`status ${kind}`}>{props.value}</span>
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
