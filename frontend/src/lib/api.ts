import i18n from '../i18n'
export type Credentials = {
  email: string
  password: string
}

export type Flight = {
  id: number
  flightNumber: string
  airlineName: string
  origin: string
  destination: string
  departureTime: string
  arrivalTime: string
  basePrice: number
  currency: string
  availableSeats: number
}

export type PassengerDetails = {
  firstName: string
  lastName: string
  dateOfBirth: string
}

export type Booking = {
  reference: string
  flightId: number
  flightNumber: string
  returnFlightId: number | null
  returnFlightNumber: string | null
  passengers: number
  totalPrice: number
  currency: string
  status: string
}

export type Page<T> = {
  content: T[]
  totalElements: number
  totalPages: number
}

export async function api<T>(
  path: string,
  options: RequestInit = {},
  credentials?: Credentials,
): Promise<T> {
  const headers = new Headers(options.headers)

  if (credentials) {
    headers.set(
      'Authorization',
      'Basic ' + btoa(`${credentials.email}:${credentials.password}`),
    )
  }

  if (options.body) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(
    `${import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? ''}/api/v1${path}`,
    {
    ...options,
    headers,
    cache: 'no-store',
  })

  if (response.status === 401) {
    throw new Error(i18n.t('messages.invalidCredentials'))
  }

  if (response.status === 403) {
    throw new Error(i18n.t('messages.forbidden'))
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null)

    throw new Error(
      body?.detail ||
      body?.message ||
      i18n.t('messages.requestFailed', { status: response.status }),
    )
  }

  return response.json() as Promise<T>
}
