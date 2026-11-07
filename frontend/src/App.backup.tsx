import { useState, type FormEvent } from 'react'
import { api, type Booking, type Credentials, type Flight, type Page } from './lib/api'
import './App.css'

type User = {
  id: number
  email: string
  role: string
}

type Payment = {
  status: string
}

function App() {
  const [credentials, setCredentials] = useState<Credentials | null>(null)
  const [user, setUser] = useState<User | null>(null)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [registerMode, setRegisterMode] = useState(false)
  const [origin, setOrigin] = useState('WAW')
  const [destination, setDestination] = useState('NBJ')
  const [date, setDate] = useState('2026-12-15')
  const [passengers, setPassengers] = useState(1)
  const [flights, setFlights] = useState<Flight[]>([])
  const [bookings, setBookings] = useState<Booking[]>([])
  const [view, setView] = useState<'flights' | 'bookings'>('flights')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const money = (amount: number, currency: string) =>
    new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency,
    }).format(amount)

  async function authenticate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    setMessage('')

    const auth = { email, password }

    try {
      if (registerMode) {
        await api<User>('/auth/register', {
          method: 'POST',
          body: JSON.stringify(auth),
        })
      }

      const current = await api<User>('/auth/me', {}, auth)

      // Guardamos as credenciais apenas na memória desta sessão local.
      setCredentials(auth)
      setUser(current)
      setPassword('')
      setMessage(`Welcome, ${current.email}`)
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function searchFlights(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    setMessage('')

    try {
      const query = new URLSearchParams({
        origin: origin.trim().toUpperCase(),
        destination: destination.trim().toUpperCase(),
        date,
        passengers: String(passengers),
      })

      const result = await api<Page<Flight>>(`/flights?${query}`)

      setFlights(result.content)
      setMessage(`${result.totalElements} flights found`)
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function loadBookings(auth = credentials) {
    if (!auth) return

    try {
      const result = await api<Page<Booking>>(
        '/bookings/mine?page=0&size=50',
        {},
        auth,
      )
      setBookings(result.content)
    } catch (error) {
      setMessage((error as Error).message)
    }
  }

  async function bookFlight(flightId: number) {
    if (!credentials) {
      setMessage('Please sign in before booking.')
      return
    }

    setLoading(true)
    setMessage('')

    try {
      const booking = await api<Booking>(
        '/bookings',
        {
          method: 'POST',
          body: JSON.stringify({
            flightId,
            passengers,
          }),
        },
        credentials,
      )

      setMessage(`Booking created: ${booking.reference}`)
      await loadBookings(credentials)
      setView('bookings')
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function bookingAction(reference: string, action: 'pay' | 'cancel') {
    if (!credentials) return

    setLoading(true)
    setMessage('')

    try {
      if (action === 'pay') {
        const result = await api<Payment>(
          `/bookings/${reference}/pay`,
          { method: 'POST' },
          credentials,
        )
        setMessage(`Demo payment: ${result.status}`)
      } else {
        await api<Booking>(
          `/bookings/${reference}/cancel`,
          { method: 'POST' },
          credentials,
        )
        setMessage('Booking cancelled.')
      }

      await loadBookings()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setLoading(false)
    }
  }

  function logout() {
    setCredentials(null)
    setUser(null)
    setBookings([])
    setView('flights')
    setMessage('Signed out.')
  }

  return (
    <div className="app">
      <header className="header">
        <a href="/" className="brand">
          <span className="brand-icon">✈</span>
          Viajar<span>PeloMundo</span>
        </a>

        <nav>
          <button onClick={() => setView('flights')}>Explore flights</button>
          {user && (
            <button onClick={() => {
              setView('bookings')
              void loadBookings()
            }}>
              My bookings
            </button>
          )}
          {user && <button onClick={logout}>Sign out</button>}
        </nav>
      </header>

      <section className="hero">
        <div className="container">
          <p className="eyebrow">DISCOVER THE WORLD WITH US</p>
          <h1>The world is waiting.<br /><span>Where to next?</span></h1>
          <p className="hero-description">
            Explore destinations, discover flights and manage your next adventure.
          </p>
        </div>
      </section>

      <main className="container main">
        {message && <div className="notice" role="status">{message}</div>}

        {view === 'flights' && (
          <>
            <section className="panel">
              <p className="eyebrow">PLAN YOUR JOURNEY</p>
              <h2>Find your next flight</h2>

              <form className="search-form" onSubmit={searchFlights}>
                <label>
                  From
                  <input
                    value={origin}
                    maxLength={3}
                    onChange={event => setOrigin(event.target.value)}
                    required
                  />
                </label>

                <label>
                  To
                  <input
                    value={destination}
                    maxLength={3}
                    onChange={event => setDestination(event.target.value)}
                    required
                  />
                </label>

                <label>
                  Departure
                  <input
                    type="date"
                    value={date}
                    onChange={event => setDate(event.target.value)}
                    required
                  />
                </label>

                <label>
                  Passengers
                  <select
                    value={passengers}
                    onChange={event => setPassengers(Number(event.target.value))}
                  >
                    {Array.from({ length: 9 }, (_, index) => (
                      <option key={index + 1} value={index + 1}>
                        {index + 1}
                      </option>
                    ))}
                  </select>
                </label>

                <button className="primary-button" disabled={loading}>
                  {loading ? 'Searching...' : 'Search flights'}
                </button>
              </form>
            </section>

            <section className="results">
              <p className="eyebrow">EXPLORE THE POSSIBILITIES</p>
              <h2>Available flights</h2>

              {flights.length === 0 && (
                <div className="empty-state">
                  Search for a destination to discover available flights.
                </div>
              )}

              {flights.map(flight => (
                <article className="flight-card" key={flight.id}>
                  <div>
                    <p className="eyebrow">{flight.airlineName}</p>
                    <h3>{flight.origin} → {flight.destination}</h3>
                    <p>{flight.flightNumber}</p>
                    <p>{new Date(flight.departureTime).toLocaleString()}</p>
                    <small>{flight.availableSeats} seats available</small>
                  </div>

                  <div className="price">
                    <strong>{money(flight.basePrice, flight.currency)}</strong>
                    <small>per passenger</small>
                    <button
                      className="primary-button"
                      disabled={loading}
                      onClick={() => void bookFlight(flight.id)}
                    >
                      Book flight
                    </button>
                  </div>
                </article>
              ))}
            </section>
          </>
        )}

        {view === 'bookings' && user && (
          <section className="panel">
            <p className="eyebrow">YOUR JOURNEYS</p>
            <h2>My bookings</h2>

            {bookings.length === 0 && (
              <div className="empty-state">No bookings found.</div>
            )}

            {bookings.map(booking => (
              <article className="booking-card" key={booking.reference}>
                <div>
                  <h3>{booking.flightNumber}</h3>
                  <p>Reference: {booking.reference}</p>
                  <p>Status: {booking.status}</p>
                  <p>
                    {booking.passengers} passenger(s) ·{' '}
                    {money(booking.totalPrice, booking.currency)}
                  </p>
                </div>

                {booking.status === 'CONFIRMED' && (
                  <div className="booking-actions">
                    <button
                      className="primary-button"
                      disabled={loading}
                      onClick={() => void bookingAction(booking.reference, 'pay')}
                    >
                      Simulate payment
                    </button>
                    <button
                      className="secondary-button"
                      disabled={loading}
                      onClick={() => void bookingAction(booking.reference, 'cancel')}
                    >
                      Cancel booking
                    </button>
                  </div>
                )}
              </article>
            ))}
          </section>
        )}

        {!user && (
          <section className="panel account-panel">
            <div>
              <p className="eyebrow">YOUR ACCOUNT</p>
              <h2>{registerMode ? 'Join ViajarPeloMundo' : 'Welcome back'}</h2>
              <p>Sign in to book flights and manage your journeys.</p>
            </div>

            <form className="account-form" onSubmit={authenticate}>
              <label>
                Email address
                <input
                  type="email"
                  value={email}
                  onChange={event => setEmail(event.target.value)}
                  required
                />
              </label>

              <label>
                Password
                <input
                  type="password"
                  value={password}
                  onChange={event => setPassword(event.target.value)}
                  minLength={registerMode ? 12 : undefined}
                  required
                />
              </label>

              <button className="primary-button" disabled={loading}>
                {registerMode ? 'Create account' : 'Sign in'}
              </button>

              <button
                className="text-button"
                type="button"
                onClick={() => setRegisterMode(!registerMode)}
              >
                {registerMode
                  ? 'Already registered? Sign in'
                  : 'New here? Create an account'}
              </button>
            </form>
          </section>
        )}
      </main>

      <footer>
        <strong>ViajarPeloMundo</strong>
        <span>Java Full-Stack Portfolio Project · Simulated bookings and payments</span>
      </footer>
    </div>
  )
}

export default App
