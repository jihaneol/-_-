export type ApiEnvelope<T> = {
  code: string
  message: string
  data: T
}

export type ApiError = {
  code: string
  message: string
  fieldErrors?: Record<string, string>
}

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })
  const payload = await response.json().catch(() => undefined)
  if (!response.ok) {
    throw normalizeError(payload)
  }
  return (payload as ApiEnvelope<T>).data
}

function normalizeError(payload: unknown): ApiError {
  if (payload && typeof payload === 'object' && 'message' in payload) {
    const error = payload as { code?: string; message?: string }
    return {
      code: error.code ?? 'UNKNOWN_ERROR',
      message: error.message ?? '요청 처리에 실패했습니다.',
    }
  }
  return {
    code: 'UNKNOWN_ERROR',
    message: '요청 처리에 실패했습니다.',
  }
}
